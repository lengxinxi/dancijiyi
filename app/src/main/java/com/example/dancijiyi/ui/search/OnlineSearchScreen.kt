package com.example.dancijiyi.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dancijiyi.data.OnlineDefinition
import com.example.dancijiyi.data.OnlineWord
import com.example.dancijiyi.ui.theme.DancijiyiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineSearchScreen(
    state: OnlineSearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit,
    onExit: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("联网查词", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    TextButton(onClick = onExit) { Text("返回") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "联网查询任意英文单词：音标、英文释义、例句与真人发音",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("输入英文单词，如 abandon") },
                    label = { Text("单词") }
                )
                Spacer(Modifier.height(0.dp))
                Button(
                    onClick = onSearch,
                    enabled = !state.loading,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                ) { Text("查询") }
            }

            Spacer(Modifier.height(16.dp))

            when {
                state.loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "正在联网查询…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                state.error != null -> Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                state.result != null -> ResultBlock(
                    word = state.result!!,
                    playing = state.playing,
                    onPlayAudio = onPlayAudio,
                    onStopAudio = onStopAudio
                )

                else -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "输入单词后点「查询」\n联网结果来自 Free Dictionary API",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ResultBlock(
    word: OnlineWord,
    playing: Boolean,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 单词头卡：单词 + 音标 + 发音按钮
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = word.word,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (word.phonetic.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = word.phonetic,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                if (word.audioUrl != null) {
                    Spacer(Modifier.height(14.dp))
                    if (playing) {
                        OutlinedButton(onClick = onStopAudio) { Text("停止发音") }
                    } else {
                        Button(onClick = { onPlayAudio(word.audioUrl) }) { Text("▶ 播放发音") }
                    }
                } else {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "该词暂无在线发音",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = "英文释义（${word.definitions.size} 条）",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        if (word.definitions.isEmpty()) {
            Text(
                text = "暂无释义数据",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            word.definitions.forEach { def ->
                DefinitionCard(def)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DefinitionCard(def: OnlineDefinition) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (def.partOfSpeech.isNotBlank()) {
                Text(
                    text = def.partOfSpeech,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = def.definition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!def.example.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "例：${def.example}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnlineSearchPreview() {
    DancijiyiTheme {
        OnlineSearchScreen(
            state = OnlineSearchUiState(
                query = "abandon",
                result = OnlineWord(
                    word = "abandon",
                    phonetic = "/əˈbæn.dən/",
                    audioUrl = "https://example.com/abandon.mp3",
                    definitions = listOf(
                        OnlineDefinition(
                            partOfSpeech = "verb",
                            definition = "To give up or relinquish control of something.",
                            example = "He abandoned his plan."
                        )
                    )
                )
            ),
            onQueryChange = {},
            onSearch = {},
            onPlayAudio = {},
            onStopAudio = {},
            onExit = {}
        )
    }
}
