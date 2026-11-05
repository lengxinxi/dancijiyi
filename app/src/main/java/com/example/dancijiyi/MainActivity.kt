package com.example.dancijiyi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dancijiyi.ui.study.StudyScreen
import com.example.dancijiyi.ui.study.StudyViewModel
import com.example.dancijiyi.ui.theme.DancijiyiTheme
import com.example.dancijiyi.ui.wordlist.WordListScreen
import com.example.dancijiyi.ui.wordlist.WordListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DancijiyiTheme {
                AppRoot()
            }
        }
    }
}

private enum class Screen { List, Study }

@Composable
private fun AppRoot() {
    var screen by rememberSaveable { mutableStateOf(Screen.List) }
    val listViewModel: WordListViewModel = viewModel()
    val studyViewModel: StudyViewModel = viewModel()

    when (screen) {
        Screen.List -> WordListScreen(
            state = listViewModel.state,
            onKeywordChange = listViewModel::onKeywordChange,
            onStartStudy = { screen = Screen.Study }
        )

        Screen.Study -> {
            BackHandler { screen = Screen.List }
            StudyScreen(
                state = studyViewModel.state,
                onFlip = studyViewModel::flip,
                onNext = studyViewModel::next,
                onPrevious = studyViewModel::previous,
                onRestart = studyViewModel::newSession,
                onExit = {
                    // 回到列表时刷新一次，保证掌握状态/计数是最新的
                    listViewModel.load()
                    screen = Screen.List
                }
            )
        }
    }
}
