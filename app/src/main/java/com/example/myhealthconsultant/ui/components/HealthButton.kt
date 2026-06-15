package com.example.myhealthconsultant.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.myhealthconsultant.ui.theme.*

/**
 * 按钮变体枚举
 */
enum class ButtonVariant {
    Primary,    // 主要按钮，实心背景
    Secondary,  // 次要按钮，边框
    Text        // 文本按钮，无背景
}

/**
 * 自定义按钮组件
 * 避免Material默认样式，提供统一的按钮设计
 */
@Composable
fun HealthButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    leadingIcon: ImageVector? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 按下时的缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "buttonScale"
    )
    
    // 根据变体选择颜色
    val colors = when (variant) {
        ButtonVariant.Primary -> ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = Color.White,
            disabledContainerColor = Primary.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        ButtonVariant.Secondary -> ButtonDefaults.outlinedButtonColors(
            contentColor = Primary
        )
        ButtonVariant.Text -> ButtonDefaults.textButtonColors(
            contentColor = Primary
        )
    }
    
    // 统一的形状和阴影
    val shape = RoundedCornerShape(0.dp)
    
    // 根据变体选择按钮类型
    when (variant) {
        ButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .height(48.dp)
                    .scale(scale),
                enabled = enabled,
                colors = colors,
                shape = shape,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp,
                    disabledElevation = 0.dp
                ),
                interactionSource = interactionSource,
                content = {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    content()
                }
            )
        }
        ButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier
                    .height(48.dp)
                    .scale(scale),
                enabled = enabled,
                colors = colors,
                shape = shape,
                border = ButtonDefaults.outlinedButtonBorder(enabled),
                interactionSource = interactionSource,
                content = {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    content()
                }
            )
        }
        ButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier
                    .height(48.dp)
                    .scale(scale),
                enabled = enabled,
                colors = colors,
                shape = shape,
                interactionSource = interactionSource,
                content = {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    content()
                }
            )
        }
    }
}

/**
 * 图标按钮组件
 */
@Composable
fun HealthIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = Primary,
    contentColor: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "iconButtonScale"
    )
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .scale(scale),
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(0.dp),
            color = if (enabled) containerColor else containerColor.copy(alpha = 0.5f),
            contentColor = contentColor
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}