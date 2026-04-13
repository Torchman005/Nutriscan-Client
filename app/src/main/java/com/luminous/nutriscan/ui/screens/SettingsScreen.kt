package com.example.foodnutritionaiassistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodnutritionaiassistant.ui.viewmodel.UserViewModel

import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: UserViewModel,
    onBack: () -> Unit
) {
    var showEditProfile by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (showEditProfile) {
        EditProfileScreen(viewModel = viewModel, onBack = { showEditProfile = false })
        return
    }

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
                    text = "设置",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider()

            Column(modifier = Modifier.padding(16.dp)) {
                SettingsItem(
                    title = "个人资料",
                    icon = Icons.Default.Person,
                    onClick = { showEditProfile = true }
                )
                
                SettingsItem(
                    title = "账号安全",
                    icon = Icons.Default.Lock,
                    onClick = { /* TODO: Implement Account Security */ }
                )
                
                SettingsItem(
                    title = "通知设置",
                    icon = Icons.Default.Notifications,
                    onClick = { /* TODO: Implement Notifications */ }
                )
                
                SettingsItem(
                    title = "隐私政策",
                    icon = Icons.Default.Security,
                    onClick = { /* TODO: Show Privacy Policy */ }
                )
                
                SettingsItem(
                    title = "清除缓存",
                    icon = Icons.Default.Refresh,
                    onClick = { showClearCacheDialog = true }
                )
                
                SettingsItem(
                    title = "关于我们",
                    icon = Icons.Default.Info,
                    onClick = { showAboutDialog = true }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { 
                    viewModel.logout(context)
                    onBack() // Go back to profile (which will show login prompt)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color.Red)
            ) {
                Text("退出登录")
            }
        }
        
        if (showClearCacheDialog) {
            AlertDialog(
                onDismissRequest = { showClearCacheDialog = false },
                title = { Text("清除缓存") },
                text = { Text("确定要清除所有图片缓存吗？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                context.imageLoader.memoryCache?.clear()
                                context.imageLoader.diskCache?.clear()
                                android.widget.Toast.makeText(context, "缓存已清除", android.widget.Toast.LENGTH_SHORT).show()
                                showClearCacheDialog = false
                            }
                        }
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearCacheDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
        
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("关于我们") },
                text = { 
                    Column {
                        Text("NutriScan AI Assistant")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("版本: 1.0.0")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("我们致力于利用 AI 技术帮助您更好地管理饮食与健康。")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("确定")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}
