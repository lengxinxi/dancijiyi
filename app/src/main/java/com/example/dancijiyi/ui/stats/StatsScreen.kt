package com.example.dancijiyi.ui.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dancijiyi.ui.theme.DancijiyiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    state: StatsUiState,
    onRefresh: () -> Unit,
    onExit: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("学习统计", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    TextButton(onClick = onExit) { Text("返回") }
                },
                actions = {
                    TextButton(onClick = onRefresh) { Text("刷新") }
                }
            )
        }
    ) { innerPadding ->
        if (state.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("正在统计…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            SectionTitle("今日")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem("学习词数", state.todayLearned.toString(), "个")
                    MetricItem("判分次数", state.todayTotal.toString(), "次")
                    MetricItem("正确率", state.todayAccuracy.toString(), "%")
                }
            }
            if (state.todayTotal == 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "今天还没开始学习，去背一组吧",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("词库进度")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("已掌握 ${state.mastered} / ${state.totalWords}", fontWeight = FontWeight.Medium)
                        Text("待复习 ${state.reviewing}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "掌握率 ${(state.masteredRatio * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("累计")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem("累计判分", state.totalRecords.toString(), "次")
                    MetricItem("累计正确率", state.totalAccuracy.toString(), "%")
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("最近 7 天")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                WeekBarChart(
                    week = state.week,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            if (!state.hasAnyRecord) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "还没有学习记录，学完一组后这里就会有数据",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun MetricItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = unit,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 最近 7 天柱状图：柱高按单日最大判分次数归一化，
 * 今天高亮；柱顶显示当日次数，柱底显示日期与正确率。
 * 用普通 Compose 布局（Box 背景色块）实现，不依赖第三方图表库。
 */
@Composable
private fun WeekBarChart(week: List<DayStat>, modifier: Modifier = Modifier) {
    val max = week.maxOfOrNull { it.total }?.coerceAtLeast(1) ?: 1
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        week.forEach { day ->
            val isToday = day.label == "今天"
            val barColor = if (isToday) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (day.total == 0) "" else day.total.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (day.total > 0) {
                        val fraction by animateFloatAsState(
                            targetValue = day.total.toFloat() / max,
                            animationSpec = tween(durationMillis = 500),
                            label = "barHeight"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.62f)
                                .height(120.dp * fraction)
                                .background(
                                    barColor,
                                    RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isToday) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1
                )
                Text(
                    text = if (day.total == 0) "—" else "${day.accuracy}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsPreview() {
    DancijiyiTheme {
        StatsScreen(
            state = StatsUiState(
                loading = false,
                todayLearned = 12,
                todayTotal = 20,
                todayCorrect = 14,
                totalWords = 280,
                mastered = 36,
                reviewing = 88,
                totalRecords = 240,
                totalCorrect = 168,
                week = listOf(
                    DayStat("2026-11-09", "11/09", 20, 12),
                    DayStat("2026-11-10", "11/10", 0, 0),
                    DayStat("2026-11-11", "11/11", 40, 30),
                    DayStat("2026-11-12", "11/12", 20, 15),
                    DayStat("2026-11-13", "11/13", 0, 0),
                    DayStat("2026-11-14", "11/14", 20, 18),
                    DayStat("2026-11-15", "今天", 20, 14)
                )
            ),
            onRefresh = {},
            onExit = {}
        )
    }
}
