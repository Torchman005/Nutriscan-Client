package com.luminous.nutriscan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.foodnutritionaiassistant.ui.viewmodel.FoodAnalysisViewModel
import com.example.foodnutritionaiassistant.ui.viewmodel.UserViewModel

import android.media.MediaPlayer
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultScreen(
    viewModel: FoodAnalysisViewModel,
    userViewModel: UserViewModel,
    onBack: () -> Unit
) {
    val result = viewModel.analysisResult
    val context = LocalContext.current
    val userProfile = userViewModel.userProfile
    
    val audioUrl = result?.get("audioUrl") as? String
    val mediaPlayer = remember { MediaPlayer() }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        mediaPlayer.setOnCompletionListener {
            isPlaying = false
        }
        onDispose {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
    }

    fun togglePlayback() {
        if (isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
        } else {
            if (audioUrl != null) {
                try {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(audioUrl)
                    mediaPlayer.prepareAsync()
                    mediaPlayer.setOnPreparedListener {
                        it.start()
                        isPlaying = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分析结果") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarUrl = userProfile.avatarUrl
                        if (!avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "User Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (audioUrl != null) {
                FloatingActionButton(
                    onClick = { togglePlayback() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Stop Audio" else "Play Audio"
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (result == null) {
                Text("暂无结果")
                return@Column
            }

            var dataMap = result.valueAsMap("data") ?: result
            
            val dataString = result.valueAsString("data")
            if (dataString.isNotBlank() && dataString.trim().startsWith("{")) {
                try {
                    val gson = com.google.gson.Gson()
                    val parsedData = gson.fromJson(dataString, Map::class.java)
                    if (parsedData != null) {
                        dataMap = parsedData as Map<String, Any>
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val rawContent = dataMap.valueAsString("output").ifBlank { 
                dataMap.valueAsString("result") 
            }.ifBlank {
                result.valueAsString("output")
            }.ifBlank {
                result.valueAsString("data")
            }
            
            val jsonString = dataMap.valueAsString("json").ifBlank {
                result.valueAsString("json")
            }

            var structuredDataMap = dataMap
            if (jsonString.isNotBlank() && jsonString.trim().startsWith("{")) {
                try {
                    val gson = com.google.gson.Gson()
                    val parsedJson = gson.fromJson(jsonString, Map::class.java)
                    if (parsedJson != null) {
                        structuredDataMap = parsedJson as Map<String, Any>
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val parsedText = parseMarkdownSections(rawContent)

            // 仅展示纯文本
            if (rawContent.isNotBlank() && parsedText.hasContent() && structuredDataMap.valueAsList("foods").isEmpty()) {
                TextResultCard(parsedText)
                return@Column
            }

            val sceneType = structuredDataMap.valueAsString("scene_type").ifBlank { "未识别场景" }
            val foods = structuredDataMap.valueAsList("foods")
            val globalAlert = structuredDataMap.valueAsMap("global_conflict_alert")

            SummaryCard(
                sceneType = sceneType,
                foodCount = foods.size,
                allergenCount = globalAlert?.valueAsList("common_allergens")?.size ?: 0,
                riskCount = globalAlert?.valueAsList("high_risk_foods")?.size ?: 0
            )

            foods.forEachIndexed { index, anyFood ->
                val food = anyFood as? Map<*, *> ?: return@forEachIndexed
                FoodCard(index + 1, food)
            }

            if (globalAlert != null) {
                GlobalAlertCard(globalAlert)
            }

            // 在底部显示综合建议
            if (rawContent.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionCard(
                    title = "AI 综合建议",
                    body = rawContent,
                    bg = Color(0xFFF8FAFC),
                    titleColor = Color(0xFF475467),
                    bodyColor = Color(0xFF344054)
                )
            }
        }
    }
}

@Composable
private fun TextResultCard(content: ParsedAnalysisContent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "识别结果",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (content.dishName.isNotBlank()) {
                Surface(
                    color = Color(0xFFEEF2FF),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "菜品名称",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4338CA)
                        )
                        Text(
                            text = content.dishName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF312E81)
                        )
                    }
                }
            }

            if (content.mainIngredients.isNotBlank()) {
                SectionCard(
                    title = "主要原材料",
                    body = content.mainIngredients,
                    bg = Color(0xFFEFF6FF),
                    titleColor = Color(0xFF1D4ED8),
                    bodyColor = Color(0xFF1E3A8A)
                )
            }

            if (content.potentialAllergens.isNotBlank()) {
                SectionCard(
                    title = "潜在过敏原",
                    body = content.potentialAllergens,
                    bg = Color(0xFFFFF1F2),
                    titleColor = Color(0xFFBE123C),
                    bodyColor = Color(0xFF9F1239)
                )
            }

            if (content.suggestion.isNotBlank()) {
                SectionCard(
                    title = "食用建议",
                    body = content.suggestion,
                    bg = Color(0xFFF0FDF4),
                    titleColor = Color(0xFF15803D),
                    bodyColor = Color(0xFF166534)
                )
            }

            if (content.visualRecognition.isNotBlank()) {
                SectionCard(
                    title = "视觉识别",
                    body = content.visualRecognition,
                    bg = Color(0xFFF8FAFC),
                    titleColor = Color(0xFF475467),
                    bodyColor = Color(0xFF344054)
                )
            }

            if (content.riskAssessment.isNotBlank()) {
                SectionCard(
                    title = "风险评估",
                    body = content.riskAssessment,
                    bg = Color(0xFFFFFBEB),
                    titleColor = Color(0xFFB54708),
                    bodyColor = Color(0xFF93370D)
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    body: String,
    bg: Color,
    titleColor: Color,
    bodyColor: Color
) {
    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = bodyColor
            )
        }
    }
}

@Composable
private fun SummaryCard(
    sceneType: String,
    foodCount: Int,
    allergenCount: Int,
    riskCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F8FF)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("分析总览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("场景：$sceneType")
            Text("识别食物：$foodCount 项")
            Text("全局过敏原：$allergenCount 项    高风险：$riskCount 项")
        }
    }
}

@Composable
private fun FoodCard(index: Int, food: Map<*, *>) {
    val name = food.valueAsString("name").ifBlank { "未命名食物" }
    val confidence = food.valueAsDouble("confidence").coerceIn(0.0, 1.0).toFloat()
    val quantity = food.valueAsString("quantity").ifBlank { "未提供" }
    val dietaryLabels = food.valueAsStringList("dietary_labels")
    val specialNote = food.valueAsString("special_note")
    val ingredients = food.valueAsList("ingredients")

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("食物 $index", style = MaterialTheme.typography.titleSmall, color = Color(0xFF667085))
            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Text("置信度：${(confidence * 100).toInt()}%")
            LinearProgressIndicator(progress = confidence, modifier = Modifier.fillMaxWidth())

            Text("份量：$quantity")

            if (dietaryLabels.isNotEmpty()) {
                LabelFlow("饮食标签", dietaryLabels)
            }

            if (ingredients.isNotEmpty()) {
                Text("配料", fontWeight = FontWeight.SemiBold)
                ingredients.forEach { anyIng ->
                    val ing = anyIng as? Map<*, *> ?: return@forEach
                    IngredientRow(ing)
                }
            }

            if (specialNote.isNotBlank()) {
                Surface(color = Color(0xFFFFF7ED), shape = RoundedCornerShape(10.dp)) {
                    Text(
                        text = "说明：$specialNote",
                        color = Color(0xFF9A3412),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IngredientRow(ing: Map<*, *>) {
    val name = ing.valueAsString("name").ifBlank { "未知配料" }
    val category = ing.valueAsString("category").ifBlank { "未分类" }
    val isAllergen = ing.valueAsBoolean("allergen")
    val allergenTypes = ing.valueAsStringList("allergen_type")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("$name（$category）")
            if (isAllergen && allergenTypes.isNotEmpty()) {
                Text(
                    text = "过敏原：${allergenTypes.joinToString("、")}",
                    color = Color(0xFFB42318),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (isAllergen) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFFEE4E2), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("过敏原", color = Color(0xFFB42318), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun GlobalAlertCard(alert: Map<*, *>) {
    val commonAllergens = alert.valueAsStringList("common_allergens")
    val religiousRestrictions = alert.valueAsStringList("religious_restrictions")
    val highRiskFoods = alert.valueAsStringList("high_risk_foods")

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4ED)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "全局冲突预警",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9A3412)
            )

            if (commonAllergens.isNotEmpty()) {
                LabelFlow("常见过敏原", commonAllergens)
            }
            if (religiousRestrictions.isNotEmpty()) {
                LabelFlow("宗教限制", religiousRestrictions)
            }
            if (highRiskFoods.isNotEmpty()) {
                LabelFlow("高风险食物", highRiskFoods)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LabelFlow(title: String, labels: List<String>) {
    Text(title, fontWeight = FontWeight.SemiBold)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        labels.forEach { raw ->
            val label = raw.ifBlank { return@forEach }
            val (chipBg, chipFg) = riskLevelColor(label)
            Box(
                modifier = Modifier
                    .background(chipBg, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(label, color = chipFg, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

private data class ParsedAnalysisContent(
    val dishName: String = "",
    val mainIngredients: String = "",
    val potentialAllergens: String = "",
    val visualRecognition: String = "",
    val riskAssessment: String = "",
    val suggestion: String = ""
) {
    fun hasContent(): Boolean {
        return dishName.isNotBlank() ||
            mainIngredients.isNotBlank() ||
            potentialAllergens.isNotBlank() ||
            visualRecognition.isNotBlank() ||
            riskAssessment.isNotBlank() ||
            suggestion.isNotBlank()
    }
}

private fun parseMarkdownSections(text: String): ParsedAnalysisContent {
    if (text.isBlank()) return ParsedAnalysisContent()

    val normalized = text.replace("\r\n", "\n")
    val lines = normalized.lines()

    var currentTitle = ""
    val dishName = StringBuilder()
    val mainIngredients = StringBuilder()
    val potentialAllergens = StringBuilder()
    val visual = StringBuilder()
    val risk = StringBuilder()
    val suggestion = StringBuilder()

    fun appendLine(target: StringBuilder, line: String) {
        val cleaned = line
            .trim()
            .removePrefix("- ")
            .removePrefix("• ")
            .trim()
        if (cleaned.isBlank()) return
        if (target.isNotEmpty()) target.append("\n")
        target.append(cleaned)
    }

    fun appendToCurrentSection(line: String) {
        when {
            currentTitle.contains("菜品名称") -> appendLine(dishName, line)
            currentTitle.contains("主要原材料") -> appendLine(mainIngredients, line)
            currentTitle.contains("潜在过敏原") -> appendLine(potentialAllergens, line)
            currentTitle.contains("视觉识别") -> appendLine(visual, line)
            currentTitle.contains("风险评估") -> appendLine(risk, line)
            currentTitle.contains("食用建议") -> appendLine(suggestion, line)
        }
    }

    lines.forEach { rawLine ->
        val line = rawLine.trim()
        when {
            line.startsWith("### ") -> {
                val header = line.removePrefix("### ").trim()
                currentTitle = header.substringBefore("：").substringBefore(":").trim()
                val inlineValue = header.substringAfter("：", "").ifBlank {
                    header.substringAfter(":", "")
                }.trim()
                if (inlineValue.isNotBlank()) {
                    appendToCurrentSection(inlineValue)
                }
            }

            line.isNotBlank() -> appendToCurrentSection(line)
        }
    }

    return ParsedAnalysisContent(
        dishName = dishName.toString(),
        mainIngredients = mainIngredients.toString(),
        potentialAllergens = potentialAllergens.toString(),
        visualRecognition = visual.toString(),
        riskAssessment = risk.toString(),
        suggestion = suggestion.toString()
    )
}

private fun Map<*, *>.valueAsString(key: String): String {
    val value = this[key] ?: return ""
    return (value as? String) ?: value.toString()
}

private fun Map<*, *>.valueAsBoolean(key: String): Boolean {
    val value = this[key] ?: return false
    return when (value) {
        is Boolean -> value
        is String -> value.equals("true", ignoreCase = true)
        is Number -> value.toInt() != 0
        else -> false
    }
}

private fun Map<*, *>.valueAsDouble(key: String): Double {
    val value = this[key] ?: return 0.0
    return when (value) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
}

private fun Map<*, *>.valueAsList(key: String): List<*> {
    return this[key] as? List<*> ?: emptyList<Any>()
}

private fun Map<*, *>.valueAsMap(key: String): Map<*, *>? {
    return this[key] as? Map<*, *>
}

private fun Map<*, *>.valueAsStringList(key: String): List<String> {
    val list = this[key] as? List<*> ?: return emptyList()
    return list.mapNotNull { it?.toString() }
}

private fun riskLevelColor(text: String): Pair<Color, Color> {
    val t = text.lowercase()
    return when {
        t.contains("高") || t.contains("过敏") || t.contains("风险") -> Color(0xFFFEE4E2) to Color(0xFFB42318)
        t.contains("限制") || t.contains("注意") -> Color(0xFFFFEDD5) to Color(0xFF9A3412)
        else -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
    }
}
