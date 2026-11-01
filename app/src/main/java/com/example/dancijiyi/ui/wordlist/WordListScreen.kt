package com.example.dancijiyi.ui.wordlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dancijiyi.data.WordEntity
import com.example.dancijiyi.ui.theme.DancijiyiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    state: WordListUiState,
    onKeywordChange: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("单词记忆", fontWeight = FontWeight.SemiBold)
            })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "共 ${state.allWords.size} 词 · 已掌握 ${state.masteredCount} · 待复习 ${state.reviewingCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("搜索单词或释义") }
            )
            Spacer(Modifier.height(12.dp))

            when {
                state.loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("词库载入中…") }

                state.visibleWords.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("没有匹配的单词") }

                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.visibleWords, key = { it.id }) { word ->
                        WordRow(word)
                    }
                }
            }
        }
    }
}

@Composable
private fun WordRow(word: WordEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = word.word,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (word.phonetic.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = word.phonetic,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = word.meaning,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (word.mastered) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "已掌握",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WordListPreview() {
    DancijiyiTheme {
        WordListScreen(
            state = WordListUiState(
                loading = false,
                allWords = listOf(
                    WordEntity(
                        id = 1,
                        word = "abandon",
                        phonetic = "/əˈbændən/",
                        meaning = "v. 放弃；抛弃",
                        example = "He abandoned his plan.",
                        exampleTrans = "他放弃了计划。"
                    )
                ),
                masteredCount = 0,
                reviewingCount = 0
            ),
            onKeywordChange = {}
        )
    }
}
