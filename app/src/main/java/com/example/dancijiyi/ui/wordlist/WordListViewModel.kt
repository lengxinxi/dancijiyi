package com.example.dancijiyi.ui.wordlist

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dancijiyi.data.DailyGoalStore
import com.example.dancijiyi.data.WordEntity
import com.example.dancijiyi.data.WordRepository
import kotlinx.coroutines.launch

data class WordListUiState(
    val loading: Boolean = true,
    val allWords: List<WordEntity> = emptyList(),
    val keyword: String = "",
    val masteredCount: Int = 0,
    val reviewingCount: Int = 0,
    /** 今日已学（去重单词数）与每日目标 */
    val todayLearned: Int = 0,
    val dailyGoal: Int = DailyGoalStore.DEFAULT_GOAL
) {
    /** 按关键字过滤后的列表（单词 / 释义均可匹配） */
    val visibleWords: List<WordEntity>
        get() = if (keyword.isBlank()) {
            allWords
        } else {
            val k = keyword.trim().lowercase()
            allWords.filter {
                it.word.lowercase().contains(k) || it.meaning.lowercase().contains(k)
            }
        }

    /** 今日进度 0..1，目标为 0 时视为未设置 */
    val todayProgress: Float
        get() = if (dailyGoal <= 0) 0f else (todayLearned.toFloat() / dailyGoal).coerceIn(0f, 1f)
    val goalReached: Boolean get() = dailyGoal > 0 && todayLearned >= dailyGoal
}

class WordListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WordRepository.get(application)

    var state by mutableStateOf(WordListUiState())
        private set

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            repository.seedIfNeeded()
            state = state.copy(
                loading = true
            )
            val words = repository.allWords()
            val today = WordRepository.todayString()
            state = WordListUiState(
                loading = false,
                allWords = words,
                keyword = state.keyword,
                masteredCount = words.count { it.mastered },
                reviewingCount = words.count { !it.mastered && it.lastStudyAt > 0 },
                todayLearned = repository.todayLearned(today),
                dailyGoal = repository.dailyGoal()
            )
        }
    }

    fun onKeywordChange(keyword: String) {
        state = state.copy(keyword = keyword)
    }
}
