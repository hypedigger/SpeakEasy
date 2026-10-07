package com.teacher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * Rendu Markdown minimal, suffisant pour les pages de grammaire :
 * titres, listes, gras, italique, code et surtout tableaux de declinaison.
 * Les tableaux larges defilent horizontalement plutot que de deborder.
 */
@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier) {
    val blocks = remember(markdown) { parseBlocks(markdown) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Heading -> Text(
                    text = inline(block.text),
                    style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineSmall
                        2 -> MaterialTheme.typography.titleLarge
                        else -> MaterialTheme.typography.titleMedium
                    },
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )

                is MdBlock.Paragraph -> Text(
                    text = inline(block.text),
                    style = MaterialTheme.typography.bodyMedium,
                )

                is MdBlock.Bullet -> Row(Modifier.fillMaxWidth()) {
                    Text(
                        text = if (block.ordinal == null) "•" else "${block.ordinal}.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(if (block.ordinal == null) 16.dp else 24.dp),
                    )
                    Text(
                        text = inline(block.text),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                is MdBlock.Rule -> HorizontalDivider(Modifier.padding(vertical = 4.dp))

                is MdBlock.Table -> MarkdownTable(block)
            }
        }
    }
}

@Composable
private fun MarkdownTable(table: MdBlock.Table) {
    val columns = table.header.size
    val widths = remember(table) {
        List(columns) { column ->
            val longest = (listOf(table.header) + table.rows)
                .maxOf { it.getOrNull(column)?.length ?: 0 }
            (longest * 9 + 24).coerceIn(72, 260).dp
        }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 4.dp)
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            table.header.forEachIndexed { index, cell ->
                Text(
                    text = inline(cell),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(widths[index]),
                )
            }
        }
        HorizontalDivider()
        table.rows.forEach { row ->
            Row(
                Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEachIndexed { index, cell ->
                    Text(
                        text = inline(cell),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(widths.getOrElse(index) { 120.dp }),
                    )
                }
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}

private sealed interface MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
    data class Bullet(val text: String, val ordinal: Int?) : MdBlock
    data class Table(val header: List<String>, val rows: List<List<String>>) : MdBlock
    data object Rule : MdBlock
}

private fun parseBlocks(markdown: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val lines = markdown.lines()
    var index = 0
    val paragraph = StringBuilder()

    fun flushParagraph() {
        if (paragraph.isNotBlank()) blocks += MdBlock.Paragraph(paragraph.toString().trim())
        paragraph.clear()
    }

    while (index < lines.size) {
        val line = lines[index]
        val trimmed = line.trim()

        when {
            trimmed.isEmpty() -> {
                flushParagraph()
                index++
            }

            trimmed.startsWith("#") -> {
                flushParagraph()
                val level = trimmed.takeWhile { it == '#' }.length
                blocks += MdBlock.Heading(level, trimmed.drop(level).trim())
                index++
            }

            trimmed.startsWith("---") && trimmed.all { it == '-' } -> {
                flushParagraph()
                blocks += MdBlock.Rule
                index++
            }

            trimmed.startsWith("|") -> {
                flushParagraph()
                val tableLines = mutableListOf<String>()
                while (index < lines.size && lines[index].trim().startsWith("|")) {
                    tableLines += lines[index].trim()
                    index++
                }
                parseTable(tableLines)?.let { blocks += it }
                    ?: tableLines.forEach { blocks += MdBlock.Paragraph(it) }
            }

            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                flushParagraph()
                blocks += MdBlock.Bullet(trimmed.drop(2).trim(), null)
                index++
            }

            trimmed.matches(Regex("^\\d+\\.\\s.*")) -> {
                flushParagraph()
                val ordinal = trimmed.takeWhile { it.isDigit() }.toIntOrNull()
                blocks += MdBlock.Bullet(trimmed.substringAfter(". ").trim(), ordinal)
                index++
            }

            else -> {
                if (paragraph.isNotEmpty()) paragraph.append(' ')
                paragraph.append(trimmed)
                index++
            }
        }
    }
    flushParagraph()
    return blocks
}

private fun parseTable(lines: List<String>): MdBlock.Table? {
    if (lines.size < 2) return null
    fun cells(row: String) = row.trim('|').split("|").map { it.trim() }
    val header = cells(lines[0])
    val separator = cells(lines[1])
    if (separator.none { it.contains("-") }) return null
    val rows = lines.drop(2).map { cells(it) }.filter { it.any { cell -> cell.isNotEmpty() } }
    return MdBlock.Table(header, rows)
}

/** Applique le gras, l'italique et le code inline. */
private fun inline(text: String): AnnotatedString = buildAnnotatedString {
    var index = 0
    while (index < text.length) {
        val bold = text.indexOf("**", index)
        val code = text.indexOf('`', index)
        val italic = findItalic(text, index)

        val next = listOfNotNull(
            bold.takeIf { it >= 0 }?.let { it to Marker.BOLD },
            code.takeIf { it >= 0 }?.let { it to Marker.CODE },
            italic.takeIf { it >= 0 }?.let { it to Marker.ITALIC },
        ).minByOrNull { it.first }

        if (next == null) {
            append(text.substring(index))
            return@buildAnnotatedString
        }

        val (start, marker) = next
        append(text.substring(index, start))

        when (marker) {
            Marker.BOLD -> {
                val end = text.indexOf("**", start + 2)
                if (end < 0) {
                    append(text.substring(start)); return@buildAnnotatedString
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(text.substring(start + 2, end))
                }
                index = end + 2
            }

            Marker.CODE -> {
                val end = text.indexOf('`', start + 1)
                if (end < 0) {
                    append(text.substring(start)); return@buildAnnotatedString
                }
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                    append(text.substring(start + 1, end))
                }
                index = end + 1
            }

            Marker.ITALIC -> {
                val end = text.indexOf('*', start + 1)
                if (end < 0) {
                    append(text.substring(start)); return@buildAnnotatedString
                }
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(text.substring(start + 1, end))
                }
                index = end + 1
            }
        }
    }
}

private enum class Marker { BOLD, CODE, ITALIC }

/** Trouve une etoile isolee (italique), en ignorant les doubles etoiles du gras. */
private fun findItalic(text: String, from: Int): Int {
    var i = from
    while (i < text.length) {
        if (text[i] == '*') {
            val doubled = (i + 1 < text.length && text[i + 1] == '*') ||
                (i > 0 && text[i - 1] == '*')
            if (!doubled) return i
            i += 2
        } else {
            i++
        }
    }
    return -1
}
