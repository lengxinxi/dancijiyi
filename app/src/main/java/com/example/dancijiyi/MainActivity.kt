package com.example.dancijiyi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dancijiyi.data.WordRepository
import com.example.dancijiyi.ui.theme.DancijiyiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = WordRepository.get(this)
        setContent {
            DancijiyiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WordBankOverview(
                        repository = repository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * 词库载入情况概览：用于验证内置词库是否已成功导入数据库。
 */
@Composable
fun WordBankOverview(repository: WordRepository, modifier: Modifier = Modifier) {
    var total by remember { mutableStateOf(-1) }
    var mastered by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        repository.seedIfNeeded()
        total = repository.totalWords()
        mastered = repository.masteredWords()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (total < 0) "词库载入中…" else "词库已载入 $total 个单词",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "已掌握 $mastered 个",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OverviewPreview() {
    DancijiyiTheme {
        Text("词库预览")
    }
}
