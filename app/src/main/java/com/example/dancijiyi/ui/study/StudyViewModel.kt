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

data class StudyUiState(
    val loading: Boolean = true,
    val words: List<WordEntity> = emptyList(),
    val index: Int = 0,
    val flipped: Boolean = false,
    val finished: Boolean = false
) {
    val current: WordEntity? get() = words.getOrNull(index)
    val progressText: String get() = if (words.isEmpty()) "0 / 0" else "${index + 1} / ${words.size}"
    val progress: Float get() = if (words.isEmpty()) 0f else (index + 1f) / words.size
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
                finished = words.isEmpty()
            )
        }
    }

    fun flip() {
        state = state.copy(flipped = !state.flipped)
    }

    fun next() {
        val s = state
        if (s.index + 1 >= s.words.size) {
            state = s.copy(finished = true)
        } else {
            state = s.copy(index = s.index + 1, flipped = false)
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
    }
}
