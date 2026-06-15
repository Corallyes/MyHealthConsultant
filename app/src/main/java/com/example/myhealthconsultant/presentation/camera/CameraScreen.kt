package com.example.myhealthconsultant.presentation.camera

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealthconsultant.presentation.components.DraggableFloatingButton
import com.example.myhealthconsultant.ui.theme.*
import java.io.File

/**
 * 拍照识药页面 - 极简设计
 * 相机界面 + 识别结果卡片，强调"结果可信度"
 */
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // 拍照
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            viewModel.recognizeDrug(photoUri!!)
        }
    }

    // 从相册选择
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.recognizeDrug(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 上部：相机操作区（占2/5）
        if (!hasCameraPermission) {
            PermissionRequestContent(
                onRequestPermission = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
            ) {
                CameraPreviewArea(
                    capturedImageUri = uiState.capturedImageUri,
                    onTakePicture = {
                        val photoFile = File(context.cacheDir, "drug_photo_${System.currentTimeMillis()}.jpg")
                        photoUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile
                        )
                        takePictureLauncher.launch(photoUri!!)
                    },
                    onPickFromGallery = {
                        pickImageLauncher.launch("image/*")
                    }
                )
            }
        }

        // 下部：结果区（占3/5）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(3f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 模糊警告
                if (uiState.blurWarning != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(0.dp),
                        color = Warning.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Warning
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.blurWarning!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = Warning
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 识别结果
                if (uiState.isLoading) {
                    LoadingIndicator()
                } else if (uiState.result != null) {
                    if (uiState.showDetail) {
                        RecognitionDetailCard(
                            result = uiState.result!!,
                            isDetailLoading = uiState.isDetailLoading,
                            onBack = { viewModel.hideDrugDetail() }
                        )
                    } else {
                        RecognitionNameCard(
                            result = uiState.result!!,
                            onShowDetail = { viewModel.showDrugDetail() }
                        )
                    }
                } else if (uiState.error != null) {
                    ErrorCard(
                        message = uiState.error!!,
                        onRetry = { uiState.capturedImageUri?.let { viewModel.recognizeDrug(it) } }
                    )
                } else {
                    InstructionCard()
                }
            }
        }
    }

    // 可拖拽的扫描历史按钮
    DraggableFloatingButton(
        onClick = onNavigateToHistory,
        onPositionChange = { x, y -> viewModel.saveFabPosition(x, y) },
        savedPosition = uiState.fabPosition
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = "扫描历史",
            modifier = Modifier.size(24.dp)
        )
    }
    } // end Box
}

/**
 * 权限请求内容 - 极简设计
 */
@Composable
private fun PermissionRequestContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 图标
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(0.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "需要相机权限",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "拍照识药功能需要使用相机拍摄药品照片",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "授权相机权限",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * 相机预览区域 - 极简设计
 */
@Composable
private fun CameraPreviewArea(
    capturedImageUri: Uri?,
    onTakePicture: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    // 包装点击事件：未拍照时点击区域触发拍照
    val onAreaClick: () -> Unit = if (capturedImageUri == null) onTakePicture else { {} }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 相机预览占位 - 填充可用空间，点击可拍照
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(
                    if (capturedImageUri == null)
                        Modifier.clickable { onTakePicture() }
                    else Modifier
                ),
            shape = RoundedCornerShape(0.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (capturedImageUri != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Success
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "已拍摄照片",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "请拍摄药品或药盒",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "点击此处拍照",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 操作按钮 - 极简设计
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 拍照按钮
            Button(
                onClick = onTakePicture,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("拍照")
            }

            // 相册按钮
            OutlinedButton(
                onClick = onPickFromGallery,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(0.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("相册")
            }
        }
    }
}

/**
 * 加载指示器 - "正在识别..."文案
 */
@Composable
private fun LoadingIndicator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "正在识别…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 识别结果卡片 - 极简设计，强调"结果可信度"
 */
@Composable
private fun RecognitionResultCard(
    result: DrugRecognitionResult
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 药品图标和名称
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(0.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Medication,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = result.drugName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 匹配度 - 颜色区分
                // 模糊标记
                if (result.isBlurry) {
                    Surface(
                        shape = RoundedCornerShape(0.dp),
                        color = Warning.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "图片模糊",
                            style = MaterialTheme.typography.labelSmall,
                            color = Warning,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                val confidencePercent = (result.confidence * 100).toInt()
                val confidenceColor = when {
                    confidencePercent >= 90 -> Success
                    confidencePercent >= 70 -> Warning
                    else -> Error
                }
                val confidenceBg = when {
                    confidencePercent >= 90 -> Success.copy(alpha = 0.1f)
                    confidencePercent >= 70 -> Warning.copy(alpha = 0.1f)
                    else -> Error.copy(alpha = 0.1f)
                }

                Surface(
                    shape = RoundedCornerShape(0.dp),
                    color = confidenceBg
                ) {
                    Text(
                        text = "${confidencePercent}%匹配",
                        style = MaterialTheme.typography.labelMedium,
                        color = confidenceColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 置信度进度条
            LinearProgressIndicator(
                progress = { result.confidence },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(0.dp)),
                color = when {
                    result.confidence >= 0.9f -> Success
                    result.confidence >= 0.7f -> Warning
                    else -> Error
                },
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 信息列表
            InfoRow("类型", result.category)
            InfoRow("适应症", result.indications)
            InfoRow("用法用量", result.dosage)
            if (result.contraindications.isNotEmpty()) {
                InfoRow("禁忌", result.contraindications)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 提醒 - 克制设计
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "请核对药品包装信息，识别结果仅供参考",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 信息行 - 极简设计
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 识别结果 - 药品名称卡片（第一步）
 * 只显示药品名和置信度，点击后查看详细信息
 */
@Composable
private fun RecognitionNameCard(
    result: DrugRecognitionResult,
    onShowDetail: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 药品图标和名称
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(0.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Medication,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = result.drugName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (result.source == "ai") {
                            Text(
                                text = "AI识别",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // 置信度
                val confidencePercent = (result.confidence * 100).toInt()
                val confidenceColor = when {
                    confidencePercent >= 90 -> Success
                    confidencePercent >= 70 -> Warning
                    else -> Error
                }
                Surface(
                    shape = RoundedCornerShape(0.dp),
                    color = confidenceColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${confidencePercent}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = confidenceColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 查看详细信息按钮
            Button(
                onClick = onShowDetail,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "查看用药说明",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * 识别结果 - 详细信息卡片（第二步）
 * 显示完整药品信息，AI药品会异步加载
 */
@Composable
private fun RecognitionDetailCard(
    result: DrugRecognitionResult,
    isDetailLoading: Boolean,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .heightIn(max = 400.dp)
        ) {
            // 顶部：返回按钮 + 药品名
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.drugName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            if (isDetailLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "正在查询用药信息…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // 可滚动的信息列表
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier.verticalScroll(scrollState)
                ) {
                    if (result.category.isNotEmpty()) {
                        InfoRow("类型", result.category)
                    }
                    if (result.indications.isNotEmpty()) {
                        InfoRow("适应症", result.indications)
                    }
                    if (result.dosage.isNotEmpty()) {
                        InfoRow("用法用量", result.dosage)
                    }
                    if (result.contraindications.isNotEmpty()) {
                        InfoRow("禁忌", result.contraindications)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "请核对药品包装信息，识别结果仅供参考",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 错误卡片 - 极简设计
 */
@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("重试")
            }
        }
    }
}

/**
 * 使用说明 - 极简设计
 */
@Composable
private fun InstructionCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "使用说明",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            InstructionItem("1", "点击「拍照」按钮拍摄药品或药盒")
            InstructionItem("2", "也可以从相册选择已有照片")
            InstructionItem("3", "系统将自动识别药品信息")
            InstructionItem("4", "请核对包装信息确保识别准确")

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "拍照建议",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            InstructionItem("✔", "保持光线充足，避免逆光拍摄")
            InstructionItem("✔", "对准药品名称，保持手机稳定")
            InstructionItem("✔", "镜头距药品 15-30cm 为佳")
            InstructionItem("✔", "确保药品包装上的文字清晰可见")
        }
    }
}

/**
 * 说明项 - 极简设计
 */
@Composable
private fun InstructionItem(number: String, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(20.dp),
            shape = RoundedCornerShape(0.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
