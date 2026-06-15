package com.example.myhealthconsultant.presentation.ai

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealthconsultant.data.local.entity.ChatHistory
import com.example.myhealthconsultant.ui.theme.*

/**
 * AI问答页面 - 极简设计
 * 避免传统聊天气泡的沉重感，采用克制的对话样式
 */
@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // 自动滚动到底部
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 模型选择栏
        ModelSelector(
            selectedModel = uiState.selectedModel,
            onModelSelect = { viewModel.selectModel(it) }
        )

        // 聊天消息列表
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 欢迎消息 + 快速提问（合并为一个紧凑区域）
            if (uiState.messages.isEmpty()) {
                item {
                    Column {
                        WelcomeMessage()
                        Spacer(modifier = Modifier.height(12.dp))
                        QuickSuggestionChips(
                            onSuggestionClick = { suggestion ->
                                viewModel.sendMessage(suggestion)
                            }
                        )
                    }
                }
            }

            items(uiState.messages, key = { it.id }) { message ->
                ChatMessage(message = message)
            }

            // 加载指示器
            if (uiState.isLoading) {
                item {
                    LoadingIndicator()
                }
            }
        }

        // 输入框 - 极简设计
        ChatInputBar(
            text = inputText,
            onTextChange = { inputText = it },
            onSendClick = {
                Log.d("AiChatScreen", "onSendClick called: inputText='$inputText', isNotBlank=${inputText.isNotBlank()}")
                if (inputText.isNotBlank()) {
                    viewModel.sendMessage(inputText.trim())
                    inputText = ""
                }
            },
            isLoading = uiState.isLoading
        )
    }

    // 错误提示 - 使用Toast显示
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
}

/**
 * 欢迎消息 - 紧凑设计
 */
@Composable
private fun WelcomeMessage() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(0.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.MedicalServices,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "健康助手",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "用药建议 · 健康咨询 · 用药指导",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 聊天消息 - 克制设计，避免传统气泡
 */
@Composable
private fun ChatMessage(message: ChatHistory) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // 消息标签
        Text(
            text = if (isUser) "您" else "助手",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // 消息内容
        Surface(
            modifier = Modifier
                .widthIn(max = 320.dp),
            shape = RoundedCornerShape(0.dp),
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = if (isUser) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ) {
            // AI消息左侧有主色细条标记
            if (!isUser) {
                Row {
                    // 左侧主色细条
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(IntrinsicSize.Max)
                            .background(MaterialTheme.colorScheme.primary)
                    )

                    MarkdownText(
                        markdown = message.content,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * 加载指示器 - 使用"分析中..."文案
 */
@Composable
private fun LoadingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Column {
            Text(
                text = "助手",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Surface(
                modifier = Modifier
                    .widthIn(max = 280.dp),
                shape = RoundedCornerShape(0.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row {
                    // 左侧主色细条
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(48.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )

                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "分析中…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 快速问题建议 - 紧凑设计
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickSuggestionChips(
    onSuggestionClick: (String) -> Unit
) {
    val suggestions = listOf(
        "感冒用药",
        "头痛处理",
        "发烧护理",
        "腹泻用药",
        "过敏处理"
    )

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        suggestions.forEach { suggestion ->
            Surface(
                onClick = { onSuggestionClick(suggestion) },
                shape = RoundedCornerShape(0.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = suggestion,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

/**
 * 模型选择下拉菜单
 */
@Composable
private fun ModelSelector(
    selectedModel: AiModel,
    onModelSelect: (AiModel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "模型",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))

            Box {
                Surface(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(0.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedModel.displayName,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    availableModels.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = model.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                onModelSelect(model)
                                expanded = false
                            },
                            leadingIcon = {
                                if (model.id == selectedModel.id) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 输入框 - 极简设计
 */
@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 文本输入框
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(0.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    placeholder = {
                        Text(
                            "输入健康问题...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    enabled = !isLoading,
                    maxLines = 3,
                    shape = RoundedCornerShape(0.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 发送按钮 - 主色圆形按钮
            IconButton(
                onClick = onSendClick,
                enabled = text.isNotBlank() && !isLoading,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (text.isNotBlank() && !isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        shape = RoundedCornerShape(0.dp)
                    )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "发送",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}
