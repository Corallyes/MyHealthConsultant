package com.example.myhealthconsultant.presentation.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * 简单的Markdown渲染器 - 支持常见格式
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val lines = remember(markdown) { markdown.lines() }
    val bodyStyle = MaterialTheme.typography.bodyMedium
    val color = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            when {
                // 空行
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                // 标题 ### / ## / #
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### ").trim(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## ").trim(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }
                line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("# ").trim(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                    )
                }
                // 无序列表 - / * / •
                line.trimStart().let { it.startsWith("- ") || it.startsWith("* ") || it.startsWith("• ") } -> {
                    val content = line.trimStart().let {
                        when {
                            it.startsWith("- ") -> it.removePrefix("- ")
                            it.startsWith("* ") -> it.removePrefix("* ")
                            else -> it.removePrefix("• ")
                        }
                    }
                    Text(
                        text = buildAnnotatedString {
                            append("  •  ")
                            appendInlineMarkdown(content)
                        },
                        style = bodyStyle,
                        color = color
                    )
                }
                // 有序列表 1. 2. 3.
                line.trimStart().let { s -> s.isNotEmpty() && s[0].isDigit() && s.contains(". ") } -> {
                    val trimmed = line.trimStart()
                    val dotIndex = trimmed.indexOf(". ")
                    if (dotIndex in 1..3) {
                        val num = trimmed.substring(0, dotIndex)
                        val content = trimmed.substring(dotIndex + 2)
                        Text(
                            text = buildAnnotatedString {
                                append("  $num. ")
                                appendInlineMarkdown(content)
                            },
                            style = bodyStyle,
                            color = color
                        )
                    } else {
                        Text(text = parseInlineMarkdown(line), style = bodyStyle, color = color)
                    }
                }
                // 普通行
                else -> {
                    Text(text = parseInlineMarkdown(line), style = bodyStyle, color = color)
                }
            }
            i++
        }
    }
}

/**
 * 解析行内Markdown格式（粗体、斜体等）
 */
private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        appendInlineMarkdown(text)
    }
}

/**
 * 在AnnotatedString.Builder中追加行内Markdown
 */
private fun AnnotatedString.Builder.appendInlineMarkdown(text: String) {
    var i = 0
    while (i < text.length) {
        // 粗体 **text**
        if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
            val end = text.indexOf("**", i + 2)
            if (end != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(text.substring(i + 2, end))
                }
                i = end + 2
                continue
            }
        }
        // 粗体 __text__
        if (i + 1 < text.length && text[i] == '_' && text[i + 1] == '_') {
            val end = text.indexOf("__", i + 2)
            if (end != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(text.substring(i + 2, end))
                }
                i = end + 2
                continue
            }
        }
        // 斜体 *text*
        if (text[i] == '*' && (i + 1 < text.length && text[i + 1] != '*')) {
            val end = text.indexOf('*', i + 1)
            if (end != -1 && end > i + 1) {
                withStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
                    append(text.substring(i + 1, end))
                }
                i = end + 1
                continue
            }
        }
        // 行内代码 `code`
        if (text[i] == '`') {
            val end = text.indexOf('`', i + 1)
            if (end != -1) {
                withStyle(SpanStyle(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )) {
                    append(text.substring(i + 1, end))
                }
                i = end + 1
                continue
            }
        }
        // 普通字符
        append(text[i])
        i++
    }
}
