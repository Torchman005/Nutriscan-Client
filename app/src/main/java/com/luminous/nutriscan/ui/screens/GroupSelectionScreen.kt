package com.example.foodnutritionaiassistant.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodnutritionaiassistant.ui.viewmodel.GroupCategory
import com.example.foodnutritionaiassistant.ui.viewmodel.UserViewModel

@Composable
fun GroupSelectionScreen(
    viewModel: UserViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var selectedGroup by remember { mutableStateOf(viewModel.userProfile.groupCategory) }

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
            progress = { 0.75f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(3.dp)),
            color = Color(0xFF66BB6A),
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("选择您的所属群体", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("为您推荐更适合的内容", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        GroupOption(
            category = GroupCategory.HEALTH,
            isSelected = selectedGroup == GroupCategory.HEALTH,
            onClick = { selectedGroup = GroupCategory.HEALTH },
            color = Color(0xFFA5D6A7)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        GroupOption(
            category = GroupCategory.FITNESS,
            isSelected = selectedGroup == GroupCategory.FITNESS,
            onClick = { selectedGroup = GroupCategory.FITNESS },
            color = Color(0xFF90CAF9)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        GroupOption(
            category = GroupCategory.TODDLER,
            isSelected = selectedGroup == GroupCategory.TODDLER,
            onClick = { selectedGroup = GroupCategory.TODDLER },
            color = Color(0xFFFFF59D)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = {
                viewModel.updateGroupCategory(selectedGroup)
                onNext()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A))
        ) {
            Text("继续", fontSize = 18.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun GroupOption(category: GroupCategory, isSelected: Boolean, onClick: () -> Unit, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.3f) else Color.White
        ),
        border = if (isSelected) BorderStroke(2.dp, color) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isSelected) Color.Black else Color.Gray
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
