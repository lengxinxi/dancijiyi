package com.example.dancijiyi.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** 网络查词返回的一条释义 */
data class OnlineDefinition(
    val partOfSpeech: String,
    val definition: String,
    val example: String?
)

/** 网络查词返回的完整词条 */
data class OnlineWord(
    val word: String,
    val phonetic: String,
    val audioUrl: String?,
    val definitions: List<OnlineDefinition>
)

/** 词库中查不到这个词（HTTP 404） */
class WordNotFoundException(word: String) : IOException("No definition found for $word")

/**
 * 联网查词数据源：使用 Android 自带的 HttpURLConnection + org.json，
 * 不引入 Retrofit/OkHttp 等第三方库。
 * 数据源：Free Dictionary API（免费、无需 key）。
 *
 * 关键点：必须带 User-Agent，否则服务端返回 403。
 */
object OnlineDictionary {

    suspend fun lookup(input: String): OnlineWord = withContext(Dispatchers.IO) {
        val word = input.trim().lowercase()
        val encoded = URLEncoder.encode(word, "UTF-8")
        val connection = URL("$BASE_URL$encoded").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("User-Agent", USER_AGENT)

        try {
            when (val code = connection.responseCode) {
                HTTP_OK -> {
                    val body = connection.inputStream.bufferedReader().use { it.readText() }
                    parse(body, word)
                }
                HTTP_NOT_FOUND -> throw WordNotFoundException(word)
                else -> throw IOException("HTTP $code")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parse(body: String, fallbackWord: String): OnlineWord {
        val array = JSONArray(body)
        if (array.length() == 0) throw WordNotFoundException(fallbackWord)

        val first = array.getJSONObject(0)
        val word = text(first, "word").ifBlank { fallbackWord }

        // 音标：优先用顶层 phonetic，没有就从 phonetics[] 里找第一条非空的 text
        var phonetic = text(first, "phonetic")
        // 发音 mp3：从 phonetics[] 里找第一条非空 audio
        var audioUrl: String? = null
        val phonetics = first.optJSONArray("phonetics")
        if (phonetics != null) {
            for (i in 0 until phonetics.length()) {
                val p = phonetics.optJSONObject(i) ?: continue
                if (phonetic.isBlank()) phonetic = text(p, "text")
                if (audioUrl == null) {
                    val a = text(p, "audio")
                    if (a.isNotBlank()) {
                        // 有些条目返回 "//..." 协议相对地址，补上 https:
                        audioUrl = if (a.startsWith("//")) "https:$a" else a
                    }
                }
            }
        }

        val definitions = ArrayList<OnlineDefinition>()
        val meanings = first.optJSONArray("meanings")
        if (meanings != null) {
            for (i in 0 until meanings.length()) {
                val m = meanings.optJSONObject(i) ?: continue
                val pos = text(m, "partOfSpeech")
                val defs = m.optJSONArray("definitions") ?: continue
                // 每个词性最多取 2 条释义，避免页面过长
                for (j in 0 until minOf(defs.length(), 2)) {
                    val d = defs.optJSONObject(j) ?: continue
                    val example = text(d, "example")
                    definitions.add(
                        OnlineDefinition(
                            partOfSpeech = pos,
                            definition = text(d, "definition"),
                            example = example.ifBlank { null }
                        )
                    )
                }
            }
        }

        return OnlineWord(
            word = word,
            phonetic = phonetic,
            audioUrl = audioUrl,
            definitions = definitions
        )
    }

    /** org.json 的 optString 对 JSON null 会返回字符串 "null"，这里统一返回空串 */
    private fun text(obj: JSONObject, key: String): String {
        val value = obj.opt(key) ?: return ""
        if (value === JSONObject.NULL) return ""
        return value.toString().trim()
    }

    private const val BASE_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/"
    private const val USER_AGENT = "WordMemoApp/1.0 (Android; course project)"
    private const val HTTP_OK = 200
    private const val HTTP_NOT_FOUND = 404
}
