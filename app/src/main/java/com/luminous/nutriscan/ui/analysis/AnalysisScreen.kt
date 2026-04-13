package com.example.foodnutritionaiassistant.ui.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.example.foodnutritionaiassistant.ui.viewmodel.UserViewModel

@Composable
fun AnalysisScreen(viewModel: UserViewModel) {
    val scrollState = rememberScrollState()
    var showWeeklyReport by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.userProfile.id) {
        if (viewModel.userProfile.id != null) {
            viewModel.fetchTodayStatistics()
            viewModel.fetchWeekStatistics()
        }
    }

    if (showWeeklyReport) {
        WeightWeeklyReportSheet(
            weekStats = viewModel.weekStatistics,
            onDismiss = { showWeeklyReport = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9),
                        Color(0xFFFFFFFF)
                    )
                )
            )
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "分析",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        UserInfoSection(
            viewModel = viewModel,
            onWeeklyReportClick = { showWeeklyReport = true }
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        MetricsGrid(viewModel.todayStatistics)

        Spacer(modifier = Modifier.height(24.dp))
        
        FilterTabs()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ChartSection(
            weekStats = viewModel.weekStatistics,
            currentWeight = viewModel.todayStatistics?.weight ?: viewModel.userProfile.weight.toDouble(),
            targetWeight = viewModel.todayStatistics?.targetWeight ?: (viewModel.userProfile.targetWeight ?: 0.0)
        )
    }
}

@Composable
fun UserInfoSection(
    viewModel: UserViewModel,
    onWeeklyReportClick: () -> Unit
) {
    val userProfile = viewModel.userProfile
    val context = LocalContext.current
    val targetWeight = userProfile.targetWeight ?: 0.0
    val hasTarget = targetWeight > 0.0
    val targetDistance = if (hasTarget) {
        targetWeight - userProfile.weight.toDouble()
    } else {
        null
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFF81C784)),
            contentAlignment = Alignment.Center
        ) {
            if (!userProfile.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(userProfile.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("User", color = Color.White, fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (userProfile.nickname.isNotBlank()) userProfile.nickname else "User",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (viewModel.isLoggedIn) "${String.format(java.util.Locale.US, "%.1f", userProfile.weight)}kg" else "-- kg",
                        color = Color(0xFFEF5350),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = if (viewModel.isLoggedIn && targetDistance != null) {
                    "距离目标${String.format(java.util.Locale.US, "%.1f", targetDistance)}kg"
                } else {
                    "距离目标--"
                },
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        
        Button(
            onClick = onWeeklyReportClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text("体重周报", color = Color.Black, fontSize = 12.sp)
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        }
    }
}

@Composable
fun MetricsGrid(stats: com.example.foodnutritionaiassistant.data.network.UserStatistics?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricCard(
            title = "目标达成率",
            value = "${stats?.achievementRate ?: 0}%",
            subtitle = "${stats?.dayStreak ?: 0}天",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = "BMI",
            value = String.format(java.util.Locale.US, "%.1f", stats?.bmi ?: 19.6),
            tag = when {
                (stats?.bmi ?: 25.0) < 18.5 -> "过轻"
                (stats?.bmi ?: 25.0) < 24.0 -> "理想"
                (stats?.bmi ?: 25.0) < 28.0 -> "超重"
                else -> "肥胖"
            },
            tagColor = when {
                (stats?.bmi ?: 25.0) < 18.5 -> Color(0xFFFF6B6B)
                (stats?.bmi ?: 25.0) < 24.0 -> Color(0xFF66BB6A)
                (stats?.bmi ?: 25.0) < 28.0 -> Color(0xFFFF9800)
                else -> Color(0xFFFF5252)
            },
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = "基础代谢",
            value = "${String.format(java.util.Locale.US, "%.0f", stats?.bmr ?: 1500.0)}",
            subtitle = "kcal/天",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    tag: String? = null,
    tagColor: Color = Color.Green,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.height(80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 12.sp, color = Color.Gray)
                if (tag != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        color = tagColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = tag,
                            color = Color.White,
                            fontSize = 8.sp,
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterTabs() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FilterItem(color = Color(0xFFF48FB1), text = "我的")
            FilterItem(color = Color(0xFF9FA8DA), text = "同基数平均")
            FilterItem(color = Color(0xFF80CBC4), text = "同基数优秀")
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("最近7天", fontSize = 12.sp, color = Color.Gray)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun FilterItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ChartSection(
    weekStats: List<com.example.foodnutritionaiassistant.data.network.UserStatistics>,
    currentWeight: Double,
    targetWeight: Double
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val dateFormat = java.text.SimpleDateFormat("yyyy/MM/dd")
            val hasTarget = targetWeight > 0.0
            val targetDistance = if (hasTarget) {
                targetWeight - currentWeight
            } else {
                null
            }

            val dayFormatter = DateTimeFormatter.ISO_DATE
            val today = LocalDate.now()
            val dateRange = (6 downTo 0).map { today.minusDays(it.toLong()) }
            val statsByDate = weekStats.mapNotNull { stat ->
                val date = stat.date?.let { runCatching { LocalDate.parse(it, dayFormatter) }.getOrNull() }
                val weight = stat.weight
                if (date != null && weight != null) {
                    date to weight
                } else {
                    null
                }
            }.toMap()

            val weightsByDate = dateRange.map { date -> statsByDate[date] }
            val dataWeights = weightsByDate.filterNotNull()
            val hasAnyData = dataWeights.isNotEmpty()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(dateFormat.format(java.util.Date()), fontSize = 12.sp, color = Color.Gray)
                    if (targetDistance != null) {
                        Text(
                            text = "距离目标 ${String.format(java.util.Locale.US, "%.1f", targetDistance)}kg",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
                Text("单位：kg", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (!hasAnyData) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无体重记录", fontSize = 12.sp, color = Color.Gray)
                    }
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        if (!hasAnyData) {
                            return@Canvas
                        }

                        val minWeight = dataWeights.minOrNull() ?: 0.0
                        val maxWeight = dataWeights.maxOrNull() ?: 0.0
                        val padding = if (minWeight == maxWeight) 1.0 else 0.5
                        val chartMin = minWeight - padding
                        val chartMax = maxWeight + padding
                        val chartRange = (chartMax - chartMin).takeIf { it > 0 } ?: 1.0

                        val steps = 4
                        val stepHeight = height / steps
                        for (i in 0..steps) {
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.5f),
                                start = Offset(0f, i * stepHeight),
                                end = Offset(width, i * stepHeight),
                                strokeWidth = 1f
                            )
                        }

                        val pointCount = weightsByDate.size.coerceAtLeast(2)
                        val spacing = width / (pointCount - 1)
                        val path = Path()
                        var hasPathStart = false

                        weightsByDate.forEachIndexed { index, weight ->
                            if (weight == null) {
                                hasPathStart = false
                                return@forEachIndexed
                            }
                            val x = spacing * index
                            val normalized = (weight - chartMin) / chartRange
                            val y = height - (normalized * height).toFloat()

                            if (!hasPathStart) {
                                path.moveTo(x, y)
                                hasPathStart = true
                            } else {
                                path.lineTo(x, y)
                            }

                            drawCircle(
                                color = Color(0xFF66BB6A),
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }

                        if (hasPathStart) {
                            drawPath(
                                path = path,
                                color = Color(0xFF66BB6A),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("最近7天", fontSize = 12.sp, color = Color.Gray)
                    val avgWeight = if (dataWeights.isNotEmpty()) {
                        dataWeights.average()
                    } else {
                        null
                    }
                    Text(
                        avgWeight?.let { String.format(java.util.Locale.US, "%.1f", it) + " kg" } ?: "--",
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("目标体重", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        if (hasTarget) String.format(java.util.Locale.US, "%.1f", targetWeight) + " kg" else "--",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightWeeklyReportSheet(
    weekStats: List<com.example.foodnutritionaiassistant.data.network.UserStatistics>,
    onDismiss: () -> Unit
) {
    val dayFormatter = DateTimeFormatter.ISO_DATE
    val displayFormatter = DateTimeFormatter.ofPattern("MM/dd")
    val today = LocalDate.now()
    val dateRange = (6 downTo 0).map { today.minusDays(it.toLong()) }

    val statsByDate = weekStats.mapNotNull { stat ->
        val date = stat.date?.let { runCatching { LocalDate.parse(it, dayFormatter) }.getOrNull() }
        val weight = stat.weight
        if (date != null && weight != null) date to weight else null
    }.toMap()

    val weightsByDate = dateRange.map { date -> statsByDate[date] }
    val dataWeights = weightsByDate.filterNotNull()

    val startWeight = dataWeights.firstOrNull()
    val endWeight = dataWeights.lastOrNull()
    val change = if (startWeight != null && endWeight != null) endWeight - startWeight else null
    val avgWeight = dataWeights.takeIf { it.isNotEmpty() }?.average()
    val minWeight = dataWeights.minOrNull()
    val maxWeight = dataWeights.maxOrNull()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("体重周报", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (dataWeights.isEmpty()) "暂无体重记录" else "近7天体重变化",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ReportMetric("起始体重", startWeight)
                ReportMetric("结束体重", endWeight)
                ReportMetric("变化", change, showSign = true)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ReportMetric("平均", avgWeight)
                ReportMetric("最低", minWeight)
                ReportMetric("最高", maxWeight)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7F7F7), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                dateRange.forEachIndexed { index, date ->
                    val weight = weightsByDate[index]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(date.format(displayFormatter), fontSize = 12.sp, color = Color.Gray)
                        Text(
                            weight?.let { String.format(java.util.Locale.US, "%.1f kg", it) } ?: "--",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (index != dateRange.lastIndex) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReportMetric(label: String, value: Double?, showSign: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(
            value?.let {
                val formatted = String.format(java.util.Locale.US, "%.1f", it)
                if (showSign && it > 0) "+$formatted kg" else "$formatted kg"
            } ?: "--",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
