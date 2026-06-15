package com.example.myhealthconsultant.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.myhealthconsultant.R
import com.example.myhealthconsultant.ui.theme.*
import kotlinx.coroutines.delay

/**
 * 登录页面 - 极简设计
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var phone by remember { mutableStateOf(uiState.savedPhone) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 登录成功后跳转
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // Logo - 极简设计
        Image(
            painter = painterResource(id = R.drawable.logo_transparent),
            contentDescription = "青囊Logo",
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(0.dp)),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "青囊",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "个人用药辅助",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))

        // 错误提示 - 极简设计
        if (uiState.error != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 手机号输入 - 极简设计
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it.filter { c -> c.isDigit() }.take(11) },
            placeholder = { Text("手机号", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (phone.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 密码输入 - 极简设计
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("密码", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (password.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 记住密码 & 忘记密码
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = uiState.rememberMe,
                    onCheckedChange = { viewModel.setRememberMe(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
                Text(
                    "记住密码",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = { showForgotPasswordDialog = true }) {
                Text(
                    "忘记密码?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 登录按钮 - 极简设计
        Button(
            onClick = { viewModel.login(phone, password) },
            enabled = phone.length == 11 && password.length >= 6 && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                disabledContentColor = MaterialTheme.colorScheme.surface
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "登 录",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 分隔线 - 极简设计
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                " 或 ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 微信登录 - 极简设计
        OutlinedButton(
            onClick = { viewModel.wechatLogin(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(0.dp),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                Icons.Default.Chat,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "微信登录",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // 注册入口
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "还没有账号?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    "立即注册",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    // 忘记密码对话框
    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            viewModel = viewModel,
            onDismiss = { showForgotPasswordDialog = false }
        )
    }

}

/**
 * 忘记密码对话框
 */
@Composable
private fun ForgotPasswordDialog(
    viewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var phone by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(0) }
    var resetSuccess by remember { mutableStateOf(false) }

    // 验证码倒计时
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    // 重置成功后自动关闭
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            resetSuccess = true
            delay(1500)
            viewModel.clearSuccessMessage()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("重置密码", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (uiState.error != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (resetSuccess) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        color = Success.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "密码重置成功！",
                            color = Success,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // 手机号
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { c -> c.isDigit() }.take(11) },
                    placeholder = { Text("注册手机号", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                // 验证码
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it.filter { c -> c.isDigit() }.take(6) },
                        placeholder = { Text("验证码", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Button(
                        onClick = {
                            viewModel.sendVerificationCode(phone)
                            countdown = 60
                        },
                        enabled = countdown == 0 && phone.length == 11,
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    ) {
                        Text(
                            if (countdown > 0) "${countdown}s" else "获取",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // 新密码
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = { Text("新密码 (至少8位)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                // 确认密码
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("确认新密码", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword,
                    supportingText = if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                        { Text("两次密码不一致", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        confirmButton = {
            val canReset = phone.length == 11
                    && verificationCode.length == 6
                    && newPassword.length >= 8
                    && newPassword == confirmPassword
                    && !uiState.isLoading

            Button(
                onClick = { viewModel.resetPassword(phone, verificationCode, newPassword) },
                enabled = canReset,
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("重置密码")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.clearError()
                onDismiss()
            }) {
                Text("取消")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )
}

/**
 * 注册页面 - 极简设计
 */
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var phone by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(0) }
    var showAgreementDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    // 注册成功后跳转
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onRegisterSuccess()
        }
    }

    // 验证码倒计时
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Image(
            painter = painterResource(id = R.drawable.logo_transparent),
            contentDescription = "青囊Logo",
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(0.dp)),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "创建账号",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "注册后即可使用全部功能",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        // 错误提示 - 极简设计
        if (uiState.error != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 验证码发送成功提示
        if (uiState.codeSentMessage != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                color = Success.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Success
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.codeSentMessage!!,
                        color = Success,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 3秒后自动清除消息
            LaunchedEffect(Unit) {
                delay(3000)
                viewModel.clearCodeSentMessage()
            }
        }

        // 昵称 - 极简设计
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            placeholder = { Text("昵称 (选填)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (nickname.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 手机号 - 极简设计
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it.filter { c -> c.isDigit() }.take(11) },
            placeholder = { Text("手机号", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (phone.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 验证码 - 极简设计
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it.filter { c -> c.isDigit() }.take(6) },
                placeholder = { Text("验证码", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(0.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
    Button(
                onClick = {
                    viewModel.sendVerificationCode(phone)
                    countdown = 60
                },
                enabled = countdown == 0 && phone.length == 11,
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Text(
                    if (countdown > 0) "${countdown}s" else "获取验证码",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 密码 - 极简设计
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("设置密码 (至少8位)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (password.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            isError = password.isNotEmpty() && password.length < 8,
            supportingText = if (password.isNotEmpty() && password.length < 8) {
                { Text("密码至少8位", color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 确认密码 - 极简设计
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = { Text("确认密码", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (confirmPassword.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            isError = confirmPassword.isNotEmpty() && password != confirmPassword,
            supportingText = if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                { Text("两次密码不一致", color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 用户协议 - 极简设计
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = agreedToTerms,
                onCheckedChange = { agreedToTerms = it },
                modifier = Modifier.size(24.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    "我已阅读并同意",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { showAgreementDialog = true },
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    modifier = Modifier.height(20.dp)
                ) {
                    Text(
                        "《用户协议》",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    "和",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { showPrivacyDialog = true },
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    modifier = Modifier.height(20.dp)
                ) {
                    Text(
                        "《隐私政策》",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 注册按钮 - 极简设计
        val canRegister = phone.length == 11
                && verificationCode.length == 6
                && password.length >= 8
                && password == confirmPassword
                && agreedToTerms
                && !uiState.isLoading

Button(
            onClick = { viewModel.register(phone, password, nickname, verificationCode) },
            enabled = canRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                disabledContentColor = MaterialTheme.colorScheme.surface
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "注 册",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 登录入口
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "已有账号?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    "立即登录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    // 用户协议对话框
    if (showAgreementDialog) {
        AuthUserAgreementDialog(onDismiss = { showAgreementDialog = false })
    }

    // 隐私政策对话框
    if (showPrivacyDialog) {
        AuthPrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }
}

@Composable
private fun AuthUserAgreementDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("用户协议", fontWeight = FontWeight.Bold) },
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
            TextButton(onClick = onDismiss) { Text("我已阅读") }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )
}

@Composable
private fun AuthPrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("隐私政策", fontWeight = FontWeight.Bold) },
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
            TextButton(onClick = onDismiss) { Text("我已阅读") }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )
}
