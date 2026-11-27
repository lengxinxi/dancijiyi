package com.example.dancijiyi.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 单词数据仓库：负责从 assets 导入内置词库，并对外暴露数据访问入口。
 */
class WordRepository(context: Context) {

    private val appContext = context.applicationContext
    private val wordDao = AppDatabase.get(appContext).wordDao()
    private val recordDao = AppDatabase.get(appContext).studyRecordDao()
    private val goalStore = DailyGoalStore.get(appContext)

    /**
     * 首次启动时把 assets/words.json 导入数据库。
     * 已导入过（表里已有数据）则直接返回，避免重复插入。
     *
     * @return 本次实际导入的单词数量
     */
    suspend fun seedIfNeeded(): Int = withContext(Dispatchers.IO) {
        if (wordDao.count() > 0) return@withContext 0

        val json = appContext.assets.open(ASSET_NAME)
            .bufferedReader()
            .use { it.readText() }
        val array = JSONArray(json)
        val words = ArrayList<WordEntity>(array.length())
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            words.add(
                WordEntity(
                    word = o.getString("word"),
                    phonetic = o.optString("phonetic"),
                    meaning = o.getString("meaning"),
                    example = o.optString("example"),
                    exampleTrans = o.optString("exampleTrans")
                )
            )
        }
        wordDao.insertAll(words)
        words.size
    }

    suspend fun totalWords(): Int = withContext(Dispatchers.IO) { wordDao.count() }

    suspend fun masteredWords(): Int = withContext(Dispatchers.IO) { wordDao.masteredCount() }

    suspend fun reviewingWords(): Int = withContext(Dispatchers.IO) { wordDao.reviewingCount() }

    suspend fun nextStudyWords(limit: Int): List<WordEntity> =
        withContext(Dispatchers.IO) { wordDao.randomUnmastered(limit) }

    suspend fun reviewWords(limit: Int): List<WordEntity> =
        withContext(Dispatchers.IO) { wordDao.randomForReview(limit) }

    suspend fun allWords(): List<WordEntity> = withContext(Dispatchers.IO) { wordDao.getAll() }

    suspend fun submitResult(word: WordEntity, result: Int, date: String) =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            recordDao.insert(
                StudyRecordEntity(
                    wordId = word.id,
                    result = result,
                    studiedAt = now,
                    date = date
                )
            )
            // 记忆盒算法：认识升级、模糊原地不动、不认识回退；升到满级视为已掌握
            val newBox = when (result) {
                RESULT_KNOWN -> (word.box + 1).coerceAtMost(MAX_BOX)
                RESULT_FUZZY -> word.box
                else -> (word.box - 1).coerceAtLeast(0)
            }
            wordDao.updateAfterStudy(
                id = word.id,
                box = newBox,
                mastered = newBox >= MAX_BOX,
                lastStudyAt = now,
                correctCount = word.correctCount + if (result == RESULT_KNOWN) 1 else 0,
                wrongCount = word.wrongCount + if (result == RESULT_UNKNOWN) 1 else 0
            )
        }

    suspend fun todayLearned(date: String): Int =
        withContext(Dispatchers.IO) { recordDao.distinctWordsByDate(date) }

    suspend fun todayTotal(date: String): Int =
        withContext(Dispatchers.IO) { recordDao.countByDate(date) }

    suspend fun todayCorrect(date: String): Int =
        withContext(Dispatchers.IO) { recordDao.correctByDate(date) }

    suspend fun dailyStats(): List<DailyStat> = withContext(Dispatchers.IO) { recordDao.dailyStats() }

    suspend fun totalRecords(): Int = withContext(Dispatchers.IO) { recordDao.totalCount() }

    suspend fun totalCorrectRecords(): Int =
        withContext(Dispatchers.IO) { recordDao.totalCorrectCount() }

    /** 每日目标（同步读 SharedPreferences，开销可忽略） */
    fun dailyGoal(): Int = goalStore.get()

    /** 修改每日目标，返回收敛后的合法值 */
    fun setDailyGoal(goal: Int): Int = goalStore.set(goal)

    companion object {
        const val ASSET_NAME = "words.json"
        const val RESULT_FUZZY = 0
        const val RESULT_KNOWN = 1
        const val RESULT_UNKNOWN = 2
        private const val MAX_BOX = 5

        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        /** 当天日期（yyyy-MM-dd），与 study_records.date 的格式一致 */
        fun todayString(): String = DATE_FORMAT.format(Date())

        @Volatile
        private var instance: WordRepository? = null

        fun get(context: Context): WordRepository =
            instance ?: synchronized(this) {
                instance ?: WordRepository(context).also { instance = it }
            }
    }
}
