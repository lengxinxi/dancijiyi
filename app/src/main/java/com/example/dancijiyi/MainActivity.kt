package com.example.dancijiyi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dancijiyi.ui.theme.DancijiyiTheme
import com.example.dancijiyi.ui.wordlist.WordListScreen
import com.example.dancijiyi.ui.wordlist.WordListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DancijiyiTheme {
                val viewModel: WordListViewModel = viewModel()
                WordListScreen(
                    state = viewModel.state,
                    onKeywordChange = viewModel::onKeywordChange
                )
            }
        }
    }
}
