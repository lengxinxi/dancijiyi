package com.example.dancijiyi.ui.study

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dancijiyi.data.WordEntity
import com.example.dancijiyi.data.WordRepository
import com.example.dancijiyi.ui.theme.DancijiyiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    state: StudyUiState,
    onFlip: () -> Unit,
    onGrade: (Int) -> Unit,
    onPrevious: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("卡片学习", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    TextButton(onClick = onExit) { Text("返回") }
                },
                actions = {
                    if (!state.loading && !state.finished) {
                        Text(
                            text = state.progressText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                state.loading -> CenterHint("正在准备单词…")

                state.finished -> FinishedBlock(
                    hasWords = state.words.isNotEmpty(),
                    state = state,
                    onRestart = onRestart,
                    onExit = onExit
                )

                else -> {
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    state.current?.let { word ->
                        FlipCard(word = word, flipped = state.flipped, onFlip = onFlip)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (state.flipped) "凭记忆给自己判个分吧" else "点击卡片翻面查看释义",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    if (state.flipped) {
                        GradeRow(
                            state = state,
                            onGrade = onGrade
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 28.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onPrevious,
                                enabled = state.index > 0,
                                modifier = Modifier.weight(1f)
                            ) { Text("上一个") }
                            Button(
                                onClick = onFlip,
                                modifier = Modifier.weight(1f)
                            ) { Text("翻面") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CenterHint(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * 三档判分按钮：不认识 / 模糊 / 认识。
 * 已判过的词重新回到这里时按钮仍然可用（只改界面状态，不重复写库）。
 */
@Composable
private fun GradeRow(state: StudyUiState, onGrade: (Int) -> Unit) {
    val gradedHint = state.currentGraded
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (gradedHint) {
            val label = when (state.currentResult) {
                WordRepository.RESULT_KNOWN -> "上次判分：认识"
                WordRepository.RESULT_FUZZY -> "上次判分：模糊"
                else -> "上次判分：不认识"
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onGrade(WordRepository.RESULT_UNKNOWN) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.weight(1f)
            ) { Text("不认识") }
            OutlinedButton(
                onClick = { onGrade(WordRepository.RESULT_FUZZY) },
                modifier = Modifier.weight(1f)
            ) { Text("模糊") }
            Button(
                onClick = { onGrade(WordRepository.RESULT_KNOWN) },
                modifier = Modifier.weight(1f)
            ) { Text("认识") }
        }
    }
}

@Composable
private fun FinishedBlock(
    hasWords: Boolean,
    state: StudyUiState,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (hasWords) "这一组已学完" else "暂时没有可学习的单词",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(16.dp))
        if (hasWords && state.gradedCount > 0) {
            Text(
                text = "认识 ${state.knownCount} · 模糊 ${state.fuzzyCount} · 不认识 ${state.unknownCount}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "本组正确率 ${state.accuracyText}（共 ${state.gradedCount} 词）",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }
        Text(
            text = if (hasWords) "稍后可以在统计页查看学习记录" else "去词库里看看，或者明天再来",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        if (hasWords) {
            Button(onClick = onRestart) { Text("再来一组") }
            Spacer(Modifier.height(8.dp))
        }
        TextButton(onClick = onExit) { Text("返回列表") }
    }
}

/**
 * 翻面卡片：沿 Y 轴旋转 180°，正反面内容在两半动画中切换，避免出现镜像文字。
 */
@Composable
private fun FlipCard(word: WordEntity, flipped: Boolean, onFlip: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "cardRotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { onFlip() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                CardFront(word)
            } else {
                Box(
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                ) {
                    CardBack(word)
                }
            }
        }
    }
}

@Composable
private fun CardFront(word: WordEntity) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            text = word.word,
            fontSize = 38.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (word.phonetic.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = word.phonetic,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (word.lastStudyAt > 0) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "学过 ${word.correctCount} 次答对",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CardBack(word: WordEntity) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            text = word.meaning,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (word.example.isNotBlank()) {
            Spacer(Modifier.height(18.dp))
            Text(
                text = word.example,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (word.exampleTrans.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = word.exampleTrans,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StudyPreview() {
    DancijiyiTheme {
        StudyScreen(
            state = StudyUiState(
                loading = false,
                words = listOf(
                    WordEntity(
                        id = 1,
                        word = "abandon",
                        phonetic = "/əˈbændən/",
                        meaning = "v. 放弃；抛弃",
                        example = "He abandoned his plan.",
                        exampleTrans = "他放弃了计划。"
                    )
                )
            ),
            onFlip = {},
            onGrade = {},
            onPrevious = {},
            onRestart = {},
            onExit = {}
        )
    }
}
