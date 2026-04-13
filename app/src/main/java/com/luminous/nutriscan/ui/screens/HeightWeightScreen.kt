package com.example.foodnutritionaiassistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodnutritionaiassistant.ui.viewmodel.UserViewModel

import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeightWeightScreen(
    viewModel: UserViewModel,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    var height by remember { mutableFloatStateOf(170f) }
    var weight by remember { mutableFloatStateOf(60f) }
    var isJin by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
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
            progress = { 1.0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(3.dp)),
            color = Color(0xFF66BB6A),
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("身高和体重", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("有助于为您提供更匹配的健康方案", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("身高", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { height -= 0.5f }) { Text("-", fontSize = 24.sp) }
                    
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF37474F), RoundedCornerShape(20.dp))
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = String.format("%.1f", height),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    IconButton(onClick = { height += 0.5f }) { Icon(Icons.Default.Add, null) }
                }
                
                Slider(
                    value = height,
                    onValueChange = { height = it },
                    valueRange = 100f..250f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF66BB6A), activeTrackColor = Color(0xFF66BB6A))
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("100", fontSize = 12.sp, color = Color.Gray)
                    Text("250", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("体重", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(
                        modifier = Modifier
                            .border(1.dp, Color(0xFF66BB6A), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .background(if (!isJin) Color(0xFF66BB6A) else Color.Transparent)
                                .clickable { isJin = false }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("公斤", color = if (!isJin) Color.White else Color(0xFF66BB6A), fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier
                                .background(if (isJin) Color(0xFF66BB6A) else Color.Transparent)
                                .clickable { isJin = true }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("斤", color = if (isJin) Color.White else Color(0xFF66BB6A), fontSize = 12.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { weight -= 0.5f }) { Text("-", fontSize = 24.sp) }
                    
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF37474F), RoundedCornerShape(20.dp))
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = String.format("%.1f", weight),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    IconButton(onClick = { weight += 0.5f }) { Icon(Icons.Default.Add, null) }
                }
                
                Slider(
                    value = weight,
                    onValueChange = { weight = it },
                    valueRange = 30f..150f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF66BB6A), activeTrackColor = Color(0xFF66BB6A))
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("30", fontSize = 12.sp, color = Color.Gray)
                    Text("150", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = {
                viewModel.updateHeight(height)
                viewModel.updateWeight(weight)
                viewModel.submitProfile(context)
                onFinish()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A))
        ) {
            Text("继续", fontSize = 18.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
