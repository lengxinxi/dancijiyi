package com.example.dancijiyi.ui.stats

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dancijiyi.data.WordRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** 某一天的学习汇总 */
data class DayStat(
    val date: String,
    val label: String,
    val total: Int,
    val correct: Int
) {
    val accuracy: Int get() = if (total == 0) 0 else correct * 100 / total
}

data class StatsUiState(
    val loading: Boolean = true,
    /** 今日：学过的不同单词数 / 判分次数 / 认识次数 */
    val todayLearned: Int = 0,
    val todayTotal: Int = 0,
    val todayCorrect: Int = 0,
    /** 词库：总数 / 已掌握 / 待复习 */
    val totalWords: Int = 0,
    val mastered: Int = 0,
    val reviewing: Int = 0,
    /** 累计：判分次数 / 认识次数 */
    val totalRecords: Int = 0,
    val totalCorrect: Int = 0,
    /** 最近 7 天（含今天，日期升序，缺失的天补 0） */
    val week: List<DayStat> = emptyList()
) {
    val todayAccuracy: Int get() = if (todayTotal == 0) 0 else todayCorrect * 100 / todayTotal
    val totalAccuracy: Int get() = if (totalRecords == 0) 0 else totalCorrect * 100 / totalRecords
    val masteredRatio: Float get() = if (totalWords == 0) 0f else mastered.toFloat() / totalWords

    /** 一周里单日最大值，画图时用来算相对高度 */
    val weekMax: Int get() = (week.maxOfOrNull { it.total } ?: 0).coerceAtLeast(1)
    val hasAnyRecord: Boolean get() = totalRecords > 0
}

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WordRepository.get(application)

    var state by mutableStateOf(StatsUiState())
        private set

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            state = state.copy(loading = true)
            val today = todayString()
            val raw = repository.dailyStats().associateBy { it.date }
            val week = buildLastSevenDays(today, raw)

            state = StatsUiState(
                loading = false,
                todayLearned = repository.todayLearned(today),
                todayTotal = repository.todayTotal(today),
                todayCorrect = repository.todayCorrect(today),
                totalWords = repository.totalWords(),
                mastered = repository.masteredWords(),
                reviewing = repository.reviewingWords(),
                totalRecords = repository.totalRecords(),
                totalCorrect = repository.totalCorrectRecords(),
                week = week
            )
        }
    }

    companion object {
        private val KEY_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        private val LABEL_FORMAT = SimpleDateFormat("MM/dd", Locale.US)

        fun todayString(): String = KEY_FORMAT.format(Date())

        /**
         * 数据库只返回有记录的日子，这里补齐最近 7 天，
         * 保证图表横轴稳定（没有学习的那天是 0）。
         */
        private fun buildLastSevenDays(
            today: String,
            raw: Map<String, com.example.dancijiyi.data.DailyStat>
        ): List<DayStat> {
            val calendar = Calendar.getInstance()
            val days = ArrayList<DayStat>(7)
            for (offset in 6 downTo 0) {
                val c = calendar.clone() as Calendar
                c.add(Calendar.DAY_OF_YEAR, -offset)
                val key = KEY_FORMAT.format(c.time)
                val stat = raw[key]
                days.add(
                    DayStat(
                        date = key,
                        label = if (offset == 0) "今天" else LABEL_FORMAT.format(c.time),
                        total = stat?.total ?: 0,
                        correct = stat?.correct ?: 0
                    )
                )
            }
            return days
        }
    }
}
