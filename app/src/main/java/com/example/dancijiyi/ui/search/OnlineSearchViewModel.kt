package com.example.dancijiyi.ui.search

import android.app.Application
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dancijiyi.data.OnlineDictionary
import com.example.dancijiyi.data.OnlineWord
import com.example.dancijiyi.data.WordNotFoundException
import kotlinx.coroutines.launch

data class OnlineSearchUiState(
    val query: String = "",
    val loading: Boolean = false,
    val result: OnlineWord? = null,
    val error: String? = null,
    /** 是否正在播放发音 */
    val playing: Boolean = false
)

class OnlineSearchViewModel(application: Application) : AndroidViewModel(application) {

    var state by mutableStateOf(OnlineSearchUiState())
        private set

    private var player: MediaPlayer? = null

    fun onQueryChange(value: String) {
        state = state.copy(query = value, error = null)
    }

    /** 联网查询；结果为空时提示，不抛给用户看堆栈 */
    fun search() {
        val word = state.query.trim()
        if (word.isBlank()) {
            state = state.copy(error = "请输入要查询的单词")
            return
        }
        stopAudio()
        viewModelScope.launch {
            state = state.copy(loading = true, error = null, result = null)
            try {
                val online = OnlineDictionary.lookup(word)
                state = state.copy(loading = false, result = online)
            } catch (e: WordNotFoundException) {
                state = state.copy(loading = false, error = "查不到这个词，检查拼写试试")
            } catch (e: Exception) {
                val msg = e.message?.takeIf { it.isNotBlank() } ?: "未知错误"
                state = state.copy(loading = false, error = "联网失败：$msg")
            }
        }
    }

    /** 播放在线发音（mp3 URL），播放结束/出错自动复位 */
    fun playAudio(url: String) {
        stopAudio()
        try {
            player = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener {
                    state = state.copy(playing = true)
                    it.start()
                }
                setOnCompletionListener {
                    state = state.copy(playing = false)
                    releasePlayer()
                }
                setOnErrorListener { mp, _, _ ->
                    state = state.copy(playing = false, error = "发音加载失败，请检查网络")
                    mp.release()
                    player = null
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            state = state.copy(playing = false, error = "无法播放发音")
        }
    }

    fun stopAudio() {
        state = state.copy(playing = false)
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onCleared() {
        releasePlayer()
        super.onCleared()
    }
}
