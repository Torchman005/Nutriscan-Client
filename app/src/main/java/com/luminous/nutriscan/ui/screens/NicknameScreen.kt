package com.example.foodnutritionaiassistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodnutritionaiassistant.ui.viewmodel.UserViewModel

@Composable
fun NicknameScreen(
    viewModel: UserViewModel,
    onNext: () -> Unit
) {
    var nickname by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF1F8E9),
                        Color(0xFFFFFFFF)
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "怎么称呼您？",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "给自己起个好听的名字吧",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(60.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            BasicTextField(
                value = nickname,
                onValueChange = { if (it.length <= 10) nickname = it },
                textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (nickname.isEmpty()) {
                        Text("请输入昵称 (10字以内)", color = Color.LightGray, fontSize = 18.sp)
                    }
                    innerTextField()
                }
            )
            if (nickname.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { nickname = "" },
                    tint = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = {
                if (nickname.isNotBlank()) {
                    viewModel.updateNickname(nickname)
                    onNext()
                }
            },
            enabled = nickname.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF66BB6A),
                disabledContainerColor = Color(0xFFC8E6C9)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("下一步", fontSize = 18.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
