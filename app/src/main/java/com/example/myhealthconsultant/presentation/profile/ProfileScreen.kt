package com.example.myhealthconsultant.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myhealthconsultant.R
import com.example.myhealthconsultant.ui.theme.*
import java.io.File

/**
 * 用户设置页面 - 微扁平化设计
 * 账户设置、安全设置、应用设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditNicknameDialog by remember { mutableStateOf(false) }
    var showEditPhoneDialog by remember { mutableStateOf(false) }
    var showEditPasswordDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showAiDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAgreementDialog by remember { mutableStateOf(false) }
    var showMedicineCabinet by remember { mutableStateOf(false) }

    // 如果显示医药箱
    if (showMedicineCabinet) {
        com.example.myhealthconsultant.presentation.cabinet.MedicineCabinetScreen(
            onBack = { showMedicineCabinet = false }
        )
        return
    }

    // 拍照临时文件
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // 从相册选择
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.updateAvatar(it.toString())
        }
    }

    // 拍照
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            viewModel.updateAvatar(photoUri.toString())
        }
    }

    // 显示消息提示
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "设置",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 用户信息头部卡片
                item {
                    UserProfileHeaderCard(
                        nickname = uiState.user?.nickname ?: "未设置昵称",
                        phone = uiState.user?.phone ?: "",
                        avatarUrl = uiState.user?.avatarUrl,
                        onAvatarClick = { showAvatarDialog = true }
                    )
                }

                // 账户设置
                item {
                    SettingsSectionTitle(title = "账户设置")
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.Badge,
                        title = "昵称",
                        subtitle = uiState.user?.nickname ?: "未设置",
                        onClick = { showEditNicknameDialog = true }
                    )
                }

                // 安全设置
                item {
                    SettingsSectionTitle(title = "安全设置")
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.Phone,
                        title = "手机号",
                        subtitle = uiState.user?.phone?.let {
                            if (it.length >= 7) it.take(3) + "****" + it.takeLast(4)
                            else it
                        } ?: "未绑定",
                        onClick = { showEditPhoneDialog = true }
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "修改密码",
                        subtitle = "定期更换密码保障账户安全",
                        onClick = { showEditPasswordDialog = true }
                    )
                }

                // 应用设置
                item {
                    SettingsSectionTitle(title = "应用设置")
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        title = "深色模式",
                        trailing = {
                            Switch(
                                checked = uiState.isDarkMode,
                                onCheckedChange = { viewModel.toggleDarkMode() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "通知设置",
                        subtitle = if (uiState.isNotificationEnabled) "已开启 · 提前${uiState.reminderAdvanceMinutes}分钟提醒" else "已关闭",
                        onClick = { showNotificationDialog = true }
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.SmartToy,
                        title = "AI服务设置",
                        subtitle = uiState.aiProvider,
                        onClick = { showAiDialog = true }
                    )
                }

                // 我的医药箱
                item {
                    SettingsItem(
                        icon = Icons.Default.Inventory2,
                        title = "我的医药箱",
                        subtitle = "管理药品、过期提醒与医嘱记录",
                        onClick = { showMedicineCabinet = true }
                    )
                }

                // 其他
                item {
                    SettingsSectionTitle(title = "其他")
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "关于APP",
                        subtitle = "版本 1.0.0",
                        onClick = { showAboutDialog = true }
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.PrivacyTip,
                        title = "隐私政策",
                        onClick = { showPrivacyDialog = true }
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.Description,
                        title = "用户协议",
                        onClick = { showAgreementDialog = true }
                    )
                }

                // 退出登录按钮
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "退出登录",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 消息提示
            uiState.message?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    containerColor = if (message.contains("成功")) Success else MaterialTheme.colorScheme.error
                ) {
                    Text(message)
                }
            }
        }
    }

    // 对话框
    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showEditNicknameDialog) {
        EditNicknameDialog(
            currentNickname = uiState.user?.nickname ?: "",
            onConfirm = { newNickname ->
                viewModel.updateNickname(newNickname)
                showEditNicknameDialog = false
            },
            onDismiss = { showEditNicknameDialog = false }
        )
    }

    if (showEditPhoneDialog) {
        EditPhoneDialog(
            currentPhone = uiState.user?.phone ?: "",
            onConfirm = { newPhone ->
                viewModel.updatePhone(newPhone)
                showEditPhoneDialog = false
            },
            onDismiss = { showEditPhoneDialog = false }
        )
    }

    if (showEditPasswordDialog) {
        EditPasswordDialog(
            onConfirm = { oldPassword, newPassword ->
                viewModel.updatePassword(oldPassword, newPassword)
                showEditPasswordDialog = false
            },
            onDismiss = { showEditPasswordDialog = false }
        )
    }

    if (showAvatarDialog) {
        AvatarSelectionDialog(
            onSelectFromGallery = {
                showAvatarDialog = false
                pickImageLauncher.launch("image/*")
            },
            onTakePhoto = {
                showAvatarDialog = false
                val photoFile = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
                photoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                takePictureLauncher.launch(photoUri!!)
            },
            onDismiss = { showAvatarDialog = false }
        )
    }

    if (showNotificationDialog) {
        NotificationSettingsDialog(
            isEnabled = uiState.isNotificationEnabled,
            advanceMinutes = uiState.reminderAdvanceMinutes,
            onToggle = { viewModel.toggleNotification() },
            onAdvanceMinutesChange = { viewModel.setReminderAdvanceMinutes(it) },
            onDismiss = { showNotificationDialog = false }
        )
    }

    if (showAiDialog) {
        AiServiceDialog(
            currentProvider = uiState.aiProvider,
            onSelect = { viewModel.setAiProvider(it) },
            onDismiss = { showAiDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutAppDialog(onDismiss = { showAboutDialog = false })
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }

    if (showAgreementDialog) {
        UserAgreementDialog(onDismiss = { showAgreementDialog = false })
    }
}

/**
 * 用户信息头部卡片
 */
@Composable
private fun UserProfileHeaderCard(
    nickname: String,
    phone: String,
    avatarUrl: String?,
    onAvatarClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像 - 可点击更换
            Box(
                modifier = Modifier.clickable(onClick = onAvatarClick)
            ) {
                if (avatarUrl != null && avatarUrl.isNotEmpty()) {
                    // 显示用户头像
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "用户头像",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 默认头像
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                // 编辑图标
                Surface(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "更换头像",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = nickname,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (phone.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = phone.let {
                        if (it.length >= 7) it.take(3) + "****" + it.takeLast(4)
                        else it
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 设置分组标题
 */
@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

/**
 * 设置项 - 微扁平化设计
 */
@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(0.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 退出登录确认对话框
 */
@Composable
private fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "确认退出",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                "退出后需要重新登录才能使用完整功能",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("退出登录", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )
}

/**
 * 修改昵称对话框
 */
@Composable
private fun EditNicknameDialog(
    currentNickname: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nickname by remember { mutableStateOf(currentNickname) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "修改昵称",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Text(
                    "请输入新的昵称",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("请输入昵称", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(0.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(nickname) },
                enabled = nickname.isNotBlank()
            ) {
                Text("确定", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )
}

/**
 * 修改手机号对话框
 */
@Composable
private fun EditPhoneDialog(
    currentPhone: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }
    var codeMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "修改手机号",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Text(
                    "当前手机号：${currentPhone.let {
                        if (it.length >= 7) it.take(3) + "****" + it.takeLast(4)
                        else it
                    }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { c -> c.isDigit() }.take(11) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("请输入新手机号", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(0.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it.filter { c -> c.isDigit() }.take(6) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("验证码", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(0.dp)
                    )
                    Button(
                        onClick = {
                            if (phone.length == 11) {
                                codeSent = true
                                codeMessage = "验证码已发送至 $phone"
                            }
                        },
                        enabled = phone.length == 11 && !codeSent,
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            if (codeSent) "已发送" else "发送验证码",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                if (codeMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = codeMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(phone) },
                enabled = phone.length == 11 && verificationCode.length == 6
            ) {
                Text("确定", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )
}

/**
 * 修改密码对话框
 */
@Composable
private fun EditPasswordDialog(
    onConfirm: (oldPassword: String, newPassword: String) -> Unit,
    onDismiss: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val passwordsMatch = newPassword == confirmPassword
    val isValid = oldPassword.length >= 6 && newPassword.length >= 6 && passwordsMatch

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "修改密码",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                // 原密码
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("请输入原密码", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    },
                    trailingIcon = {
                        IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                            Icon(
                                if (oldPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (oldPasswordVisible) "隐藏" else "显示",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    },
                    visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(0.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 新密码
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("请输入新密码（至少6位）", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    },
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (newPasswordVisible) "隐藏" else "显示",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    },
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(0.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 确认新密码
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("请再次输入新密码", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (confirmPasswordVisible) "隐藏" else "显示",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(0.dp)
                )

                if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "两次输入的密码不一致",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(oldPassword, newPassword) },
                enabled = isValid
            ) {
                Text("确定", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )
}

/**
 * 头像选择对话框
 */
@Composable
private fun AvatarSelectionDialog(
    onSelectFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "更换头像",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 从相册选择
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSelectFromGallery),
                    shape = RoundedCornerShape(0.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "从相册选择",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // 拍照
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onTakePhoto),
                    shape = RoundedCornerShape(0.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "拍照",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )
}

@Composable
fun NotificationSettingsDialog(
    isEnabled: Boolean,
    advanceMinutes: Int,
    onToggle: () -> Unit,
    onAdvanceMinutesChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("通知设置", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("用药提醒通知", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { onToggle() }
                    )
                }

                if (isEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "提前提醒时间",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15, 30).forEach { minutes ->
                                FilterChip(
                                    selected = advanceMinutes == minutes,
                                    onClick = { onAdvanceMinutesChange(minutes) },
                                    label = { Text("${minutes}分钟") }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )
}

@Composable
fun AboutAppDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("关于APP", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_transparent),
                    contentDescription = "青囊Logo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(0.dp)),
                    contentScale = ContentScale.Fit
                )
                Text(
                    "青囊",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "版本 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                Text(
                    "一款智能健康管理应用，提供用药提醒、药品查询、AI健康咨询等功能，帮助您更好地管理个人健康。",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    "免责声明：本应用提供的健康信息仅供参考，不能替代专业医疗诊断。如有健康问题请及时就医。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("隐私政策", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("最后更新日期：2024年1月1日", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text("一、数据收集", fontWeight = FontWeight.SemiBold)
                Text("本应用仅收集您主动提供的个人信息（如昵称、手机号），用于账户管理和个性化服务。")

                Text("二、数据存储", fontWeight = FontWeight.SemiBold)
                Text("您的所有健康数据（用药记录、健康日志等）均存储在本地设备中，不会上传至任何服务器。")

                Text("三、权限使用", fontWeight = FontWeight.SemiBold)
                Text("• 相机权限：仅用于拍照识药功能\n• 通知权限：仅用于用药提醒\n• 网络权限：仅用于AI咨询服务")

                Text("四、数据安全", fontWeight = FontWeight.SemiBold)
                Text("我们采用加密存储技术保护您的个人信息，确保数据安全。")

                Text("五、第三方服务", fontWeight = FontWeight.SemiBold)
                Text("本应用使用的AI服务可能涉及数据传输，我们将严格遵守相关法律法规保护您的隐私。")

                Text("六、联系我们", fontWeight = FontWeight.SemiBold)
                Text("如有任何隐私相关问题，请通过应用内反馈功能联系我们。")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("我已阅读")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )
}

@Composable
fun UserAgreementDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("用户协议", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("最后更新日期：2024年1月1日", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text("一、服务说明", fontWeight = FontWeight.SemiBold)
                Text("本应用提供健康管理相关服务，包括用药提醒、药品查询、AI健康咨询等。所有健康信息仅供参考，不构成医疗建议。")

                Text("二、使用规范", fontWeight = FontWeight.SemiBold)
                Text("您应合法使用本应用，不得利用本应用从事违法违规活动。您应对账户安全负责。")

                Text("三、免责声明", fontWeight = FontWeight.SemiBold)
                Text("本应用提供的健康信息仅供参考，不能替代专业医疗诊断和治疗。因使用本应用信息而产生的任何后果，由用户自行承担。")

                Text("四、知识产权", fontWeight = FontWeight.SemiBold)
                Text("本应用的所有内容（包括但不限于文字、图片、软件）均受知识产权法律保护。")

                Text("五、协议变更", fontWeight = FontWeight.SemiBold)
                Text("我们有权根据需要修改本协议，修改后的协议将在应用内公布。继续使用本应用即表示您同意修改后的协议。")

                Text("六、适用法律", fontWeight = FontWeight.SemiBold)
                Text("本协议适用中华人民共和国法律。")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("我已阅读")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )
}

/**
 * AI服务设置对话框
 */
@Composable
private fun AiServiceDialog(
    currentProvider: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val providers = listOf(
        "GLM-4-Flash" to "智谱AI · 免费",
        "Qwen3.5-4B" to "硅基流动 · 免费"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("AI服务设置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column {
                Text(
                    "选择AI健康咨询使用的模型",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                providers.forEach { (name, desc) ->
                    val isSelected = currentProvider == name
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(0.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(name); onDismiss() }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelect(name); onDismiss() },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )
}
