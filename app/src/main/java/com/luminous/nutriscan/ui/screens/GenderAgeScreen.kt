package com.example.foodnutritionaiassistant.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodnutritionaiassistant.ui.viewmodel.UserViewModel
import java.time.LocalDate

import androidx.compose.material.icons.filled.DateRange
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderAgeScreen(
    viewModel: UserViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var selectedGender by remember { mutableIntStateOf(viewModel.userProfile.gender) }
    var birthDate by remember { mutableStateOf(viewModel.userProfile.birthDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF1F8E9), Color(0xFFFFFFFF))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.clickable(onClick = onBack)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LinearProgressIndicator(
            progress = { 0.5f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(3.dp)),
            color = Color(0xFF66BB6A),
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("性别和年龄", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("有助于为您提供更匹配的健康方案", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        GenderOption(
            text = "女性",
            isSelected = selectedGender == 2,
            onClick = { selectedGender = 2 },
            color = Color(0xFFEF9A9A)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        GenderOption(
            text = "男性",
            isSelected = selectedGender == 1,
            onClick = { selectedGender = 1 },
            color = Color(0xFF90CAF9)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("出生日期", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFF66BB6A)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF66BB6A))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = birthDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = birthDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            )
            
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            birthDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        }
                        showDatePicker = false
                    }) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("取消")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = {
                viewModel.updateGender(selectedGender)
                viewModel.updateBirthDate(birthDate)
                onNext()
            },
            enabled = selectedGender != 0,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A))
        ) {
            Text("继续", fontSize = 18.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun GenderOption(text: String, isSelected: Boolean, onClick: () -> Unit, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.2f) else Color.White
        ),
        border = if (isSelected) BorderStroke(1.dp, color) else null
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
            Text(text, fontWeight = FontWeight.Bold, color = if (isSelected) color else Color.Black)
        }
    }
}
