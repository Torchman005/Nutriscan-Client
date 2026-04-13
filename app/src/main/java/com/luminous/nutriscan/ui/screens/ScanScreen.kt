package com.example.foodnutritionaiassistant.ui.screens

import android.widget.Toast
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.foodnutritionaiassistant.ui.viewmodel.FoodAnalysisViewModel
import java.io.File

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.foodnutritionaiassistant.ui.viewmodel.UserViewModel

@Composable
fun ScanScreen(
    viewModel: FoodAnalysisViewModel,
    userViewModel: UserViewModel,
    onHistoryClick: () -> Unit,
    onAnalysisComplete: () -> Unit
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val userId = userViewModel.userProfile.id
    
    val tempUri = remember {
        val directory = File(context.filesDir, "images")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File.createTempFile("camera_image", ".jpg", directory)
        val authority = "${context.packageName}.provider" 
        FileProvider.getUriForFile(context, authority, file)
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri = tempUri
                viewModel.analyzeImage(context, tempUri, userId)
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                cameraLauncher.launch(tempUri)
            } else {
                Toast.makeText(context, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
            }
        }
    )
    
    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.fetchHistory(userId)
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                imageUri = uri
                viewModel.analyzeImage(context, uri, userId)
            }
        }
    )

    LaunchedEffect(viewModel.analysisResult) {
        if (viewModel.analysisResult != null) {
            onAnalysisComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (imageUri == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { 
                        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(tempUri)
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("拍照识别")
                }
                
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("从相册选择")
                }
                
                Button(
                    onClick = onHistoryClick,
                    modifier = Modifier.padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("查看识别历史")
                }
                
                /* 
                if (viewModel.history.isNotEmpty()) {
                    Text(
                        text = "识别历史", 
                        style = MaterialTheme.typography.titleMedium, 
                        modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
                    )
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.history) { record ->
                            Card(
                                modifier = Modifier.size(100.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(record.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = record.foodName ?: "Food Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
                */
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
                
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    Text("正在分析食物...")
                } else if (viewModel.error != null) {
                    Text("Error: ${viewModel.error}", color = Color.Red, modifier = Modifier.padding(16.dp))
                    Button(onClick = { 
                        imageUri = null 
                        viewModel.clearResult()
                    }) {
                        Text("重试")
                    }
                }
            }
        }
    }
}
