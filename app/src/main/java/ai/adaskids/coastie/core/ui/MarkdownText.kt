// file: core/ui/MarkdownText.kt
package com.coastal.coastie.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    // Very light parser: handles ```code```, # headings, - bullets, **bold**, `inline`
    val blocks = splitCodeBlocks(text)

    SelectionContainer {
        Surface(modifier = modifier) {
            androidx.compose.foundation.layout.Column {
                blocks.forEach { block ->
                    if (block.isCode) {
                        Text(
                            text = block.content.trimEnd(),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp)
                        )
                    } else {
                        Text(
                            text = toAnnotatedMarkdown(block.content),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class Block(val isCode: Boolean, val content: String)

private fun splitCodeBlocks(input: String): List<Block> {
    val parts = mutableListOf<Block>()
    val regex = Regex("```([\\s\\S]*?)```")
    var lastIndex = 0
    regex.findAll(input).forEach { match ->
        val start = match.range.first
        val end = match.range.last + 1
        if (start > lastIndex) {
            parts += Block(false, input.substring(lastIndex, start))
        }
        parts += Block(true, match.groupValues[1])
        lastIndex = end
    }
    if (lastIndex < input.length) parts += Block(false, input.substring(lastIndex))
    return parts.filter { it.content.isNotBlank() }
}

private fun toAnnotatedMarkdown(input: String): AnnotatedString {
    val lines = input.lines()

    return buildAnnotatedString {
        lines.forEachIndexed { idx, raw ->
            val line = raw.trimEnd()

            when {
                line.startsWith("# ") -> {
                    pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
                    append(line.removePrefix("# ").trim())
                    pop()
                }
                line.startsWith("## ") -> {
                    pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
                    append(line.removePrefix("## ").trim())
                    pop()
                }
                line.startsWith("- ") -> {
                    append("• ")
                    append(inlineStyles(line.removePrefix("- ").trim()))
                }
                else -> {
                    append(inlineStyles(line))
                }
            }

            if (idx != lines.lastIndex) append("\n")
        }
    }
}

private fun inlineStyles(line: String): AnnotatedString {
    // **bold** and `code`
    val boldRegex = Regex("\\*\\*(.+?)\\*\\*")
    val codeRegex = Regex("`(.+?)`")

    var s = line

    // First handle code by replacing with sentinel tokens
    val codePieces = mutableListOf<String>()
    s = codeRegex.replace(s) {
        codePieces += it.groupValues[1]
        "⟦CODE_${codePieces.size - 1}⟧"
    }

    // Then bold similarly
    val boldPieces = mutableListOf<String>()
    s = boldRegex.replace(s) {
        boldPieces += it.groupValues[1]
        "⟦BOLD_${boldPieces.size - 1}⟧"
    }

    return buildAnnotatedString {
        val tokenRegex = Regex("⟦(CODE|BOLD)_(\\d+)⟧")
        var last = 0
        tokenRegex.findAll(s).forEach { m ->
            val start = m.range.first
            val end = m.range.last + 1
            if (start > last) append(s.substring(last, start))

            val type = m.groupValues[1]
            val index = m.groupValues[2].toInt()

            when (type) {
                "CODE" -> {
                    pushStyle(SpanStyle(fontFamily = FontFamily.Monospace))
                    append(codePieces[index])
                    pop()
                }
                "BOLD" -> {
                    pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
                    append(boldPieces[index])
                    pop()
                }
            }

            last = end
        }
        if (last < s.length) append(s.substring(last))
    }
}
