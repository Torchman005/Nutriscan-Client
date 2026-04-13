package com.example.foodnutritionaiassistant.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.foodnutritionaiassistant.ui.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.foodnutritionaiassistant.ui.viewmodel.GroupCategory

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: UserViewModel,
    onBack: () -> Unit
) {
    val userProfile = viewModel.userProfile
    var nickname by remember { mutableStateOf(userProfile.nickname) }
    var gender by remember { mutableStateOf(userProfile.gender) }
    var birthDate by remember { mutableStateOf(userProfile.birthDate) }
    var region by remember { mutableStateOf(userProfile.region) }
    var bio by remember { mutableStateOf(userProfile.bio) }
    var height by remember { mutableStateOf(userProfile.height.toString()) }
    var weight by remember { mutableStateOf(userProfile.weight.toString()) }
    var selectedGroup by remember { mutableStateOf(userProfile.groupCategory) }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    
    var showGenderDialog by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        avatarUri = uri
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "编辑个人资料",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    viewModel.updateUserProfile(
                        nickname = nickname,
                        gender = gender,
                        birthDate = birthDate,
                        region = region,
                        bio = bio,
                        avatarUri = avatarUri,
                        context = context,
                        height = height.toFloatOrNull(),
                        weight = weight.toFloatOrNull(),
                        groupCategory = selectedGroup
                    ) { success ->
                        if (success) {
                            android.widget.Toast.makeText(context, "保存成功", android.widget.Toast.LENGTH_SHORT).show()
                            onBack()
                        } else {
                            android.widget.Toast.makeText(context, "保存失败，请重试", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("保存", fontWeight = FontWeight.Bold)
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { launcher.launch("image/*") }
                ) {
                    if (avatarUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(avatarUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!userProfile.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(userProfile.avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Change Avatar", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("昵称") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = when(gender) {
                        1 -> "男"
                        2 -> "女"
                        else -> "保密"
                    },
                    onValueChange = {},
                    label = { Text("性别") },
                    modifier = Modifier.fillMaxWidth().clickable { showGenderDialog = true },
                    shape = RoundedCornerShape(25.dp),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                
                OutlinedTextField(
                    value = birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    onValueChange = {},
                    label = { Text("生日") },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    shape = RoundedCornerShape(25.dp),
                    enabled = false,
                    trailingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("身高 (cm)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("体重 (kg)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = selectedGroup.displayName,
                    onValueChange = {},
                    label = { Text("所属群体") },
                    modifier = Modifier.fillMaxWidth().clickable { showGroupDialog = true },
                    shape = RoundedCornerShape(25.dp),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = region ?: "",
                    onValueChange = { region = it },
                    label = { Text("地区") },
                    placeholder = { Text("未填写") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    singleLine = true
                )
                
                // Phone (Read Only)
                val displayPhone = userProfile.phoneNumber?.takeIf { it.isNotEmpty() } ?: "未绑定"
                OutlinedTextField(
                    value = displayPhone,
                    onValueChange = {},
                    label = { Text("手机号") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                
                OutlinedTextField(
                    value = bio ?: "",
                    onValueChange = { bio = it },
                    label = { Text("个人简介") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(25.dp),
                    maxLines = 5
                )
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
        
        if (showGenderDialog) {
            AlertDialog(
                onDismissRequest = { showGenderDialog = false },
                title = { Text("选择性别") },
                text = {
                    Column {
                        listOf("男" to 1, "女" to 2, "保密" to 0).forEach { (label, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        gender = value
                                        showGenderDialog = false
                                    }
                                    .padding(vertical = 25.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = gender == value,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showGenderDialog = false }) { Text("取消") }
                }
            )
        }
        
        if (showGroupDialog) {
            AlertDialog(
                onDismissRequest = { showGroupDialog = false },
                title = { Text("选择所属群体") },
                text = {
                    Column {
                        GroupCategory.entries.forEach { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedGroup = category
                                        showGroupDialog = false
                                    }
                                    .padding(vertical = 25.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedGroup == category,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(category.displayName)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showGroupDialog = false }) { Text("取消") }
                }
            )
        }
        
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = birthDate.toEpochDay() * 24 * 60 * 60 * 1000
            )
            
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            birthDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        }
                        showDatePicker = false
                    }) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("取消") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
