package com.example.foodnutritionaiassistant.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodnutritionaiassistant.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    viewModel: UserViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var content by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("OTHER") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }

    val feedbackTypes = mapOf(
        "BUG" to "功能故障",
        "SUGGESTION" to "产品建议",
        "OTHER" to "其他问题"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "问题反馈",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("问题类型", fontWeight = FontWeight.Bold)
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = feedbackTypes[selectedType] ?: "其他问题",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTypeDropdown = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledTrailingIconColor = Color.Black
                        )
                    )
                    
                    DropdownMenu(
                        expanded = showTypeDropdown,
                        onDismissRequest = { showTypeDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        feedbackTypes.forEach { (key, value) ->
                            DropdownMenuItem(
                                text = { Text(value) },
                                onClick = {
                                    selectedType = key
                                    showTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                Text("反馈内容", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("请详细描述您遇到的问题或建议...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF66BB6A),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Text("联系方式 (选填)", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    placeholder = { Text("手机号/邮箱/微信") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (content.isBlank()) {
                            Toast.makeText(context, "请填写反馈内容", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        isSubmitting = true
                        viewModel.submitFeedback(content, contactInfo, selectedType) { success ->
                            isSubmitting = false
                            if (success) {
                                Toast.makeText(context, "提交成功，感谢您的反馈！", Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                Toast.makeText(context, "提交失败，请重试", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("提交反馈", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
