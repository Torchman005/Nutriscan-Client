package com.example.foodnutritionaiassistant.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodnutritionaiassistant.ui.viewmodel.UserViewModel
import java.time.LocalDate
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction

@Composable
fun HomeScreen(
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    userViewModel: UserViewModel? = null
) {
    if (!isLoggedIn) {
        LoginPrompt(onLoginClick)
    } else {
        HomeContent(userViewModel)
    }
}

@Composable
fun LoginPrompt(onLoginClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("请先登录", style = MaterialTheme.typography.headlineSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onLoginClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAED581))
        ) {
            Text("去登录", color = Color.White)
        }
    }
}

@Composable
fun HomeContent(userViewModel: UserViewModel?) {
    val scrollState = rememberScrollState()
    var showAddCalorieDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userViewModel?.userProfile?.id) {
        if (userViewModel?.userProfile?.id != null) {
            userViewModel.fetchTodayStatistics()
            userViewModel.fetchWeekStatistics()
        }
    }

    if (showAddCalorieDialog && userViewModel != null) {
        AddCalorieDialog(
            onDismiss = { showAddCalorieDialog = false },
            onConfirm = { foodName, calories, onResult ->
                userViewModel.addManualCalorieRecord(foodName, calories, onResult)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchBar()
        
        Spacer(modifier = Modifier.height(32.dp))
        
        val stats = userViewModel?.todayStatistics
        val progress = if (stats != null && stats.calorieGoal > 0)
            (stats.totalCalories / stats.calorieGoal).toFloat()
        else 0f

        CalorieRing(
            totalCalories = stats?.totalCalories ?: 0.0,
            calorieGoal = stats?.calorieGoal ?: 2000.0,
            progress = progress,
            onRecordClick = { showAddCalorieDialog = true }
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        WeeklyStats(userViewModel?.weekStatistics ?: emptyList())
    }
}

@Composable
fun SearchBar() {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }

    fun openSearch() {
        val trimmed = query.trim()
        val url = if (trimmed.isBlank()) {
            "https://www.bing.com"
        } else {
            "https://www.bing.com/search?q=${Uri.encode(trimmed)}"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        placeholder = { Text("即刻了解你想吃的食材", color = Color.LightGray, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        trailingIcon = {
            IconButton(onClick = { openSearch() }) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF42A5F5),
            unfocusedBorderColor = Color(0xFF42A5F5),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { openSearch() }
        )
    )
}

@Composable
fun CalorieRing(
    totalCalories: Double,
    calorieGoal: Double,
    progress: Float,
    onRecordClick: () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 40.dp.toPx()
            val radius = size.minDimension / 2 - strokeWidth / 2

            drawCircle(
                color = Color(0xFFE8F5E9),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = Color(0xFFAED581),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(String.format("%.1f", totalCalories), fontSize = 48.sp, fontWeight = FontWeight.Bold)
                Text(" 千卡", fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
            
            Text("目标: ${String.format("%.0f", calorieGoal)} 千卡", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF1F8E9),
                modifier = Modifier.clickable { onRecordClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("点击记录卡路里", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun WeeklyStats(weekStats: List<com.example.foodnutritionaiassistant.data.network.UserStatistics>) {
    val today = LocalDate.now()
    val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("MM-dd")
    val dateRange = "${today.minusDays(6).format(dateFormatter)} - ${today.format(dateFormatter)}"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(dateRange, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("饮食记录", color = Color.Gray, fontSize = 14.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val last7Days = (0..6).map { today.minusDays(6 - it.toLong()) }

            last7Days.forEach { date ->
                val dayLabel = when {
                    date.isEqual(today) -> "今"
                    else -> when(date.dayOfWeek) {
                        java.time.DayOfWeek.MONDAY -> "一"
                        java.time.DayOfWeek.TUESDAY -> "二"
                        java.time.DayOfWeek.WEDNESDAY -> "三"
                        java.time.DayOfWeek.THURSDAY -> "四"
                        java.time.DayOfWeek.FRIDAY -> "五"
                        java.time.DayOfWeek.SATURDAY -> "六"
                        java.time.DayOfWeek.SUNDAY -> "日"
                    }
                }

                val stat = weekStats.find {
                    it.date != null && LocalDate.parse(it.date).isEqual(date)
                }

                DayStat(dayLabel, stat?.totalCalories ?: 0.0, stat?.calorieGoal ?: 2000.0)
            }
        }
    }
}

@Composable
fun DayStat(day: String, calories: Double, goal: Double) {
    val maxHeight = 80.dp
    val ratio = if (goal > 0) (calories / goal).toFloat() else 0f
    val barHeight = (maxHeight * ratio).coerceIn(0.dp, maxHeight)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val emoji = when {
            calories == 0.0 -> "😴"
            calories < goal * 0.5 -> "😕"
            calories < goal -> "😊"
            calories < goal * 1.2 -> "😄"
            else -> "😅"
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0xFFE0E0E0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(String.format("%.0f", calories), fontSize = 10.sp, color = Color(0xFF7986CB))
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(barHeight)
                .background(Color(0xFFAED581), RoundedCornerShape(4.dp))
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(day, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3949AB))
    }
}

@Composable
fun AddCalorieDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, (Boolean, String?) -> Unit) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var caloriesStr by remember { mutableStateOf("") }
    var foodNameError by remember { mutableStateOf(false) }
    var caloriesError by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录卡路里") },
        text = {
            Column {
                OutlinedTextField(
                    value = foodName,
                    onValueChange = {
                        foodName = it.trim()
                        foodNameError = false
                        submitError = null
                    },
                    label = { Text("食物名称（必须为中文）") },
                    isError = foodNameError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (foodNameError) {
                    Text("请输入中文食物名称", color = Color.Red, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = caloriesStr,
                    onValueChange = {
                        caloriesStr = it.trim()
                        caloriesError = false
                        submitError = null
                    },
                    label = { Text("卡路里 (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = caloriesError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (caloriesError) {
                    Text("请输入有效的卡路里数值", color = Color.Red, fontSize = 12.sp)
                }
                if (!submitError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(submitError ?: "", color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val calories = caloriesStr.toDoubleOrNull()
                    val isChinese = foodName.matches(Regex("^[\\u4e00-\\u9fa5]+$"))
                    if (!isChinese) {
                        foodNameError = true
                        return@Button
                    }
                    if (foodName.isBlank()) {
                        foodNameError = true
                        return@Button
                    }
                    if (calories == null || calories <= 0) {
                        caloriesError = true
                        return@Button
                    }
                    isSubmitting = true
                    onConfirm(foodName, calories) { success, message ->
                        isSubmitting = false
                        if (success) {
                            onDismiss()
                        } else {
                            submitError = message ?: "保存失败"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            ) {
                Text(if (isSubmitting) "保存中..." else "确定")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, enabled = !isSubmitting) {
                Text("取消")
            }
        }
    )
}
