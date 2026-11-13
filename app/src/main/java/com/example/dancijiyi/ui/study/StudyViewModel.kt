package com.example.dancijiyi.ui.study

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dancijiyi.data.WordEntity
import com.example.dancijiyi.data.WordRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StudyUiState(
    val loading: Boolean = true,
    val words: List<WordEntity> = emptyList(),
    val index: Int = 0,
    val flipped: Boolean = false,
    val finished: Boolean = false,
    /** 与 words 对齐的判分结果：null=未判分，0=模糊 1=认识 2=不认识 */
    val results: List<Int?> = emptyList()
) {
    val current: WordEntity? get() = words.getOrNull(index)
    val progressText: String get() = if (words.isEmpty()) "0 / 0" else "${index + 1} / ${words.size}"
    val progress: Float get() = if (words.isEmpty()) 0f else (index + 1f) / words.size

    /** 当前词是否已判过分（返回上一张时不重复写库） */
    val currentGraded: Boolean get() = results.getOrNull(index) != null
    val currentResult: Int? get() = results.getOrNull(index)

    val knownCount: Int get() = results.count { it == WordRepository.RESULT_KNOWN }
    val fuzzyCount: Int get() = results.count { it == WordRepository.RESULT_FUZZY }
    val unknownCount: Int get() = results.count { it == WordRepository.RESULT_UNKNOWN }
    val gradedCount: Int get() = results.count { it != null }
    val accuracyText: String
        get() = if (gradedCount == 0) "0%" else "${knownCount * 100 / gradedCount}%"
}

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WordRepository.get(application)

    var state by mutableStateOf(StudyUiState())
        private set

    init {
        newSession()
    }

    /** 开始一组新的学习：优先取没学过的词，不够就用待复习的词补齐 */
    fun newSession() {
        viewModelScope.launch {
            state = StudyUiState(loading = true)
            var words = repository.nextStudyWords(SESSION_SIZE)
            if (words.isEmpty()) {
                words = repository.reviewWords(SESSION_SIZE)
            }
            state = StudyUiState(
                loading = false,
                words = words,
                index = 0,
                flipped = false,
                finished = words.isEmpty(),
                results = List(words.size) { null }
            )
        }
    }

    fun flip() {
        state = state.copy(flipped = !state.flipped)
    }

    /**
     * 对当前词判分：首次判分写入 study_records 并按记忆盒算法更新单词等级，
     * 之后自动进入下一张（最后一张则结束本组）。
     * 返回上一张重新判分时只更新界面状态，不重复写库。
     */
    fun grade(result: Int) {
        val s = state
        val idx = s.index
        val word = s.words.getOrNull(idx) ?: return

        viewModelScope.launch {
            if (s.results.getOrNull(idx) == null) {
                repository.submitResult(word, result, todayString())
            }
            val newResults = s.results.toMutableList().also { it[idx] = result }
            if (idx + 1 >= s.words.size) {
                state = s.copy(results = newResults, finished = true)
            } else {
                state = s.copy(
                    results = newResults,
                    index = idx + 1,
                    flipped = false,
                    finished = false
                )
            }
        }
    }

    fun previous() {
        val s = state
        if (s.index > 0) {
            state = s.copy(index = s.index - 1, flipped = false, finished = false)
        }
    }

    companion object {
        const val SESSION_SIZE = 20

        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        fun todayString(): String = DATE_FORMAT.format(Date())
    }
}
