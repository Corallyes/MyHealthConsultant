package com.example.myhealthconsultant.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myhealthconsultant.ui.theme.*

/**
 * 自定义卡片组件
 * 提供极简设计，避免Material默认卡片的沉重感
 */
@Composable
fun HealthCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 按下时的缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "cardScale"
    )
    
    // 统一的圆角和阴影
    val shape = RoundedCornerShape(0.dp)
    
    if (onClick != null) {
        // 可点击的卡片
        Card(
            onClick = onClick,
            modifier = modifier.scale(scale),
            enabled = enabled,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = containerColor.copy(alpha = 0.5f),
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp,
                disabledElevation = 0.dp
            ),
            interactionSource = interactionSource,
            content = {
                Column(
                    modifier = Modifier.padding(16.dp),
                    content = content
                )
            }
        )
    } else {
        // 不可点击的卡片
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            content = {
                Column(
                    modifier = Modifier.padding(16.dp),
                    content = content
                )
            }
        )
    }
}

/**
 * 列表项卡片组件
 */
@Composable
fun HealthListItemCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "listItemScale"
    )
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        enabled = enabled,
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            disabledElevation = 0.dp
        ),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 前置内容（图标、图片等）
            if (leadingContent != null) {
                Box(modifier = Modifier.size(40.dp)) {
                    leadingContent()
                }
            }
            
            // 主要内容区域
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 标题
                headlineContent()
                
                // 支持文本
                if (supportingContent != null) {
                    supportingContent()
                }
            }
            
            // 尾部内容（箭头、按钮等）
            if (trailingContent != null) {
                Box(modifier = Modifier.size(24.dp)) {
                    trailingContent()
                }
            }
        }
    }
}