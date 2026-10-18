package com.flowtask.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import java.util.UUID

enum class NoteBlockType {
    PARAGRAPH,
    HEADING_1,
    HEADING_2,
    HEADING_3,
    CHECKLIST,
    BULLET,
    NUMBERED,
    QUOTE,
    DIVIDER,
    CODE_BLOCK
}

data class NoteBlock(
    val id: String = UUID.randomUUID().toString(),
    val type: NoteBlockType = NoteBlockType.PARAGRAPH,
    val text: String = "",
    val isChecked: Boolean = false
)

object NoteRichParser {

    fun parseMarkdownToBlocks(content: String): List<NoteBlock> {
        if (content.isBlank()) {
            return listOf(NoteBlock(type = NoteBlockType.PARAGRAPH, text = ""))
        }
        val result = mutableListOf<NoteBlock>()
        val lines = content.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()
            when {
                trimmed.startsWith("### ") || trimmed == "###" -> {
                    result.add(NoteBlock(type = NoteBlockType.HEADING_3, text = if (trimmed == "###") "" else trimmed.substring(4).trim()))
                }
                trimmed.startsWith("## ") || trimmed == "##" -> {
                    result.add(NoteBlock(type = NoteBlockType.HEADING_2, text = if (trimmed == "##") "" else trimmed.substring(3).trim()))
                }
                trimmed.startsWith("# ") || trimmed == "#" -> {
                    result.add(NoteBlock(type = NoteBlockType.HEADING_1, text = if (trimmed == "#") "" else trimmed.substring(2).trim()))
                }
                trimmed.startsWith("- [x] ", ignoreCase = true) || trimmed.startsWith("- [X] ") || trimmed.equals("- [x]", ignoreCase = true) -> {
                    result.add(NoteBlock(type = NoteBlockType.CHECKLIST, text = if (trimmed.length <= 6) "" else trimmed.substring(6).trim(), isChecked = true))
                }
                trimmed.startsWith("- [ ] ") || trimmed == "- [ ]" -> {
                    result.add(NoteBlock(type = NoteBlockType.CHECKLIST, text = if (trimmed.length <= 6) "" else trimmed.substring(6).trim(), isChecked = false))
                }
                trimmed.startsWith("• ") || trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed == "•" || trimmed == "-" || trimmed == "*" -> {
                    val text = if (trimmed.length <= 2) "" else trimmed.substring(2)
                    result.add(NoteBlock(type = NoteBlockType.BULLET, text = text.trim()))
                }
                trimmed.matches(Regex("""^\d+\.\s.*""")) || trimmed.matches(Regex("""^\d+\.""")) -> {
                    val clean = trimmed.replaceFirst(Regex("""^\d+\.\s*"""), "").trim()
                    result.add(NoteBlock(type = NoteBlockType.NUMBERED, text = clean))
                }
                trimmed.startsWith("> ") || trimmed == ">" -> {
                    result.add(NoteBlock(type = NoteBlockType.QUOTE, text = if (trimmed == ">") "" else trimmed.substring(2).trim()))
                }
                trimmed == "---" || trimmed == "***" -> {
                    result.add(NoteBlock(type = NoteBlockType.DIVIDER, text = ""))
                }
                trimmed.startsWith("```") -> {
                    val codeLines = mutableListOf<String>()
                    var j = i + 1
                    while (j < lines.size && !lines[j].trim().startsWith("```")) {
                        codeLines.add(lines[j])
                        j++
                    }
                    result.add(NoteBlock(type = NoteBlockType.CODE_BLOCK, text = codeLines.joinToString("\n")))
                    i = if (j < lines.size) j else lines.size - 1
                }
                else -> {
                    result.add(NoteBlock(type = NoteBlockType.PARAGRAPH, text = line))
                }
            }
            i++
        }
        if (result.isEmpty()) {
            result.add(NoteBlock(type = NoteBlockType.PARAGRAPH, text = ""))
        }
        return result
    }

    fun serializeBlocksToMarkdown(blocks: List<NoteBlock>): String {
        var numberedIdx = 1
        return blocks.joinToString("\n") { block ->
            when (block.type) {
                NoteBlockType.HEADING_1 -> "# ${block.text}"
                NoteBlockType.HEADING_2 -> "## ${block.text}"
                NoteBlockType.HEADING_3 -> "### ${block.text}"
                NoteBlockType.CHECKLIST -> "- [${if (block.isChecked) "x" else " "}] ${block.text}"
                NoteBlockType.BULLET -> "- ${block.text}"
                NoteBlockType.NUMBERED -> "${numberedIdx++}. ${block.text}"
                NoteBlockType.QUOTE -> "> ${block.text}"
                NoteBlockType.DIVIDER -> "---"
                NoteBlockType.CODE_BLOCK -> "```\n${block.text}\n```"
                NoteBlockType.PARAGRAPH -> block.text
            }.also {
                if (block.type != NoteBlockType.NUMBERED) numberedIdx = 1
            }
        }
    }

    /**
     * Parsed inline markdown document with multi-span styling maps and bidirectional offset tables.
     */
    data class ParsedInlineDoc(
        val cleanText: String,
        val isDelimiter: BooleanArray,
        val boldMap: BooleanArray,
        val italicMap: BooleanArray,
        val strikeMap: BooleanArray,
        val codeMap: BooleanArray,
        val rawToTrans: IntArray,
        val transToRaw: IntArray
    )

    /**
     * Parses all inline markdown styles (nested bold, italic, strike, code), identifying delimiters
     * and computing full character-level style maps and offset mappings.
     */
    fun parseInlineMarkdown(raw: String): ParsedInlineDoc {
        val n = raw.length
        if (n == 0) {
            return ParsedInlineDoc(
                cleanText = "",
                isDelimiter = BooleanArray(0),
                boldMap = BooleanArray(0),
                italicMap = BooleanArray(0),
                strikeMap = BooleanArray(0),
                codeMap = BooleanArray(0),
                rawToTrans = IntArray(1) { 0 },
                transToRaw = IntArray(1) { 0 }
            )
        }

        val isDelimiter = BooleanArray(n)
        val boldMap = BooleanArray(n)
        val italicMap = BooleanArray(n)
        val strikeMap = BooleanArray(n)
        val codeMap = BooleanArray(n)

        // 1. Inline code: `...`
        var i = 0
        while (i < n) {
            if (raw[i] == '`') {
                val close = raw.indexOf('`', i + 1)
                if (close != -1) {
                    isDelimiter[i] = true
                    isDelimiter[close] = true
                    for (k in (i + 1) until close) {
                        codeMap[k] = true
                    }
                    i = close + 1
                    continue
                }
            }
            i++
        }

        // 2. Strikethrough: ~~...~~
        i = 0
        while (i < n - 1) {
            if (!codeMap[i] && !isDelimiter[i] && raw.startsWith("~~", i)) {
                var close = -1
                var j = i + 2
                while (j < n - 1) {
                    if (!codeMap[j] && !isDelimiter[j] && raw.startsWith("~~", j)) {
                        close = j
                        break
                    }
                    j++
                }
                if (close != -1) {
                    isDelimiter[i] = true
                    isDelimiter[i + 1] = true
                    isDelimiter[close] = true
                    isDelimiter[close + 1] = true
                    for (k in (i + 2) until close) {
                        if (!codeMap[k]) {
                            strikeMap[k] = true
                        }
                    }
                    i = close + 2
                    continue
                }
            }
            i++
        }

        // 3. Bold + Italic: ***...***
        i = 0
        while (i < n - 2) {
            if (!codeMap[i] && !isDelimiter[i] && raw.startsWith("***", i)) {
                var close = -1
                var j = i + 3
                while (j < n - 2) {
                    if (!codeMap[j] && !isDelimiter[j] && raw.startsWith("***", j)) {
                        close = j
                        break
                    }
                    j++
                }
                if (close != -1) {
                    isDelimiter[i] = true
                    isDelimiter[i + 1] = true
                    isDelimiter[i + 2] = true
                    isDelimiter[close] = true
                    isDelimiter[close + 1] = true
                    isDelimiter[close + 2] = true
                    for (k in (i + 3) until close) {
                        if (!codeMap[k]) {
                            boldMap[k] = true
                            italicMap[k] = true
                        }
                    }
                    i = close + 3
                    continue
                }
            }
            i++
        }

        // 4. Bold: **...** or __...__
        i = 0
        while (i < n - 1) {
            if (!codeMap[i] && !isDelimiter[i]) {
                val isDoubleStar = raw.startsWith("**", i)
                val isDoubleUnder = raw.startsWith("__", i)
                if (isDoubleStar || isDoubleUnder) {
                    val token = if (isDoubleStar) "**" else "__"
                    var close = -1
                    var j = i + 2
                    while (j < n - 1) {
                        if (!codeMap[j] && !isDelimiter[j] && raw.startsWith(token, j)) {
                            close = j
                            break
                        }
                        j++
                    }
                    if (close != -1) {
                        isDelimiter[i] = true
                        isDelimiter[i + 1] = true
                        isDelimiter[close] = true
                        isDelimiter[close + 1] = true
                        for (k in (i + 2) until close) {
                            if (!codeMap[k]) {
                                boldMap[k] = true
                            }
                        }
                        i = close + 2
                        continue
                    }
                }
            }
            i++
        }

        // 5. Italic: *...* or _..._
        i = 0
        while (i < n) {
            if (!codeMap[i] && !isDelimiter[i]) {
                val isStar = raw[i] == '*'
                val isUnder = raw[i] == '_'
                if (isStar || isUnder) {
                    val delimChar = raw[i]
                    var close = -1
                    var j = i + 1
                    while (j < n) {
                        if (!codeMap[j] && !isDelimiter[j] && raw[j] == delimChar) {
                            close = j
                            break
                        }
                        j++
                    }
                    if (close != -1) {
                        isDelimiter[i] = true
                        isDelimiter[close] = true
                        for (k in (i + 1) until close) {
                            if (!codeMap[k]) {
                                italicMap[k] = true
                            }
                        }
                        i = close + 1
                        continue
                    }
                }
            }
            i++
        }

        // Build clean string and bidirectional offset mapping
        val sb = java.lang.StringBuilder()
        val rawToTrans = IntArray(n + 1)
        val transToRawList = ArrayList<Int>(n + 1)
        var tIdx = 0

        for (k in 0 until n) {
            rawToTrans[k] = tIdx
            if (!isDelimiter[k]) {
                transToRawList.add(k)
                sb.append(raw[k])
                tIdx++
            }
        }
        rawToTrans[n] = tIdx
        transToRawList.add(n)

        return ParsedInlineDoc(
            cleanText = sb.toString(),
            isDelimiter = isDelimiter,
            boldMap = boldMap,
            italicMap = italicMap,
            strikeMap = strikeMap,
            codeMap = codeMap,
            rawToTrans = rawToTrans,
            transToRaw = transToRawList.toIntArray()
        )
    }

    /**
     * Renders inline markdown to an AnnotatedString, completely stripping syntax tokens
     * and correctly combining bold, italic, strikethrough, and code styles.
     */
    fun renderInlineMarkdown(
        raw: String,
        baseColor: Color,
        accentColor: Color,
        isStrikethrough: Boolean = false
    ): AnnotatedString {
        if (raw.isEmpty()) return AnnotatedString("")

        val doc = parseInlineMarkdown(raw)
        return buildAnnotatedString {
            append(doc.cleanText)
            for (t in doc.cleanText.indices) {
                val rawIdx = doc.transToRaw[t]
                if (doc.codeMap.getOrElse(rawIdx) { false }) {
                    addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = accentColor.copy(alpha = 0.14f),
                            color = accentColor
                        ),
                        t,
                        t + 1
                    )
                } else {
                    val isBold = doc.boldMap.getOrElse(rawIdx) { false }
                    val isItalic = doc.italicMap.getOrElse(rawIdx) { false }
                    val isStrike = isStrikethrough || doc.strikeMap.getOrElse(rawIdx) { false }

                    val style = SpanStyle(
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                        textDecoration = if (isStrike) TextDecoration.LineThrough else null,
                        color = if (isStrike) baseColor.copy(alpha = 0.65f) else baseColor
                    )
                    addStyle(style, t, t + 1)
                }
            }
        }
    }

    /**
     * Checks whether a specific format (**, *, ~~, `) is active at the specified cursor position.
     */
    fun isFormatActive(text: String, cursor: Int, format: String): Boolean {
        if (text.isEmpty()) return false
        val doc = parseInlineMarkdown(text)
        val idx = cursor.coerceIn(0, text.length)
        val checkIdx = if (idx < text.length) idx else (idx - 1).coerceAtLeast(0)
        return when (format) {
            "**" -> doc.boldMap.getOrElse(checkIdx) { false }
            "*" -> doc.italicMap.getOrElse(checkIdx) { false }
            "~~" -> doc.strikeMap.getOrElse(checkIdx) { false }
            "`" -> doc.codeMap.getOrElse(checkIdx) { false }
            else -> false
        }
    }

    /**
     * Toggles inline format (**prefix...suffix**) around selection or current word.
     */
    fun toggleInlineFormat(
        current: TextFieldValue,
        prefix: String,
        suffix: String
    ): TextFieldValue {
        val text = current.text
        val sel = current.selection

        // 1. Text is selected
        if (!sel.collapsed && sel.min < sel.max) {
            val start = sel.min.coerceIn(0, text.length)
            val end = sel.max.coerceIn(0, text.length)
            val selectedText = text.substring(start, end)

            // Unwrap if selected text itself starts and ends with prefix/suffix
            if (selectedText.startsWith(prefix) && selectedText.endsWith(suffix) && selectedText.length >= prefix.length + suffix.length) {
                val unwrapped = selectedText.substring(prefix.length, selectedText.length - suffix.length)
                val newText = text.substring(0, start) + unwrapped + text.substring(end)
                return TextFieldValue(newText, TextRange(start, start + unwrapped.length))
            }

            // Unwrap if outer characters match
            val hasOuterPrefix = start >= prefix.length && text.substring(start - prefix.length, start) == prefix
            val hasOuterSuffix = end + suffix.length <= text.length && text.substring(end, end + suffix.length) == suffix
            if (hasOuterPrefix && hasOuterSuffix) {
                val newText = text.substring(0, start - prefix.length) + selectedText + text.substring(end + suffix.length)
                val newStart = start - prefix.length
                return TextFieldValue(newText, TextRange(newStart, newStart + selectedText.length))
            }

            // Wrap selection
            val wrapped = prefix + selectedText + suffix
            val newText = text.substring(0, start) + wrapped + text.substring(end)
            return TextFieldValue(newText, TextRange(start + prefix.length, start + prefix.length + selectedText.length))
        }

        // 2. No selection (cursor inside a word or empty text)
        val cursor = sel.start.coerceIn(0, text.length)
        if (text.isEmpty()) {
            val newText = prefix + suffix
            return TextFieldValue(newText, TextRange(prefix.length))
        }

        // Locate token boundaries around cursor (stopping at whitespace)
        var wordStart = cursor
        while (wordStart > 0 && !text[wordStart - 1].isWhitespace()) {
            wordStart--
        }
        var wordEnd = cursor
        while (wordEnd < text.length && !text[wordEnd].isWhitespace()) {
            wordEnd++
        }

        if (wordStart < wordEnd) {
            val token = text.substring(wordStart, wordEnd)
            val newToken = toggleFormatOnWord(token, prefix)
            val newText = text.substring(0, wordStart) + newToken + text.substring(wordEnd)
            val newCursor = (wordStart + newToken.length).coerceIn(0, newText.length)
            return TextFieldValue(newText, TextRange(newCursor))
        }

        // Empty space -> insert tokens
        val newText = text.substring(0, cursor) + prefix + suffix + text.substring(cursor)
        return TextFieldValue(newText, TextRange(cursor + prefix.length))
    }

    private fun toggleFormatOnWord(token: String, format: String): String {
        return when (format) {
            "**" -> {
                when {
                    token.contains("***") -> token.replace("***", "*")
                    token.startsWith("~~**") && token.endsWith("**~~") -> token.replace("~~**", "~~").replace("**~~", "~~")
                    token.startsWith("**~~") && token.endsWith("~~**") -> token.replace("**~~", "~~").replace("~~**", "~~")
                    token.startsWith("**") && token.endsWith("**") && token.length >= 4 -> token.substring(2, token.length - 2)
                    token.startsWith("~~") && token.endsWith("~~") && token.length >= 4 -> "~~**${token.substring(2, token.length - 2)}**~~"
                    token.startsWith("*") && token.endsWith("*") && token.length >= 2 -> "***${token.substring(1, token.length - 1)}***"
                    else -> "**$token**"
                }
            }
            "*" -> {
                when {
                    token.contains("***") -> token.replace("***", "**")
                    token.startsWith("~~*") && token.endsWith("*~~") -> token.replace("~~*", "~~").replace("*~~", "~~")
                    token.startsWith("*~~") && token.endsWith("~~*") -> token.replace("*~~", "~~").replace("~~*", "~~")
                    token.startsWith("*") && token.endsWith("*") && !token.startsWith("**") && token.length >= 2 -> token.substring(1, token.length - 1)
                    token.startsWith("~~") && token.endsWith("~~") && token.length >= 4 -> "~~*${token.substring(2, token.length - 2)}*~~"
                    token.startsWith("**") && token.endsWith("**") && token.length >= 4 -> "***${token.substring(2, token.length - 2)}***"
                    else -> "*$token*"
                }
            }
            "~~" -> {
                when {
                    token.startsWith("~~**") && token.endsWith("**~~") -> token.replace("~~**", "**").replace("**~~", "**")
                    token.startsWith("**~~") && token.endsWith("~~**") -> token.replace("**~~", "**").replace("~~**", "**")
                    token.startsWith("~~*") && token.endsWith("*~~") -> token.replace("~~*", "*").replace("*~~", "*")
                    token.startsWith("*~~") && token.endsWith("~~*") -> token.replace("*~~", "*").replace("~~*", "*")
                    token.startsWith("~~") && token.endsWith("~~") && token.length >= 4 -> token.substring(2, token.length - 2)
                    token.startsWith("**") && token.endsWith("**") && token.length >= 4 -> "**~~${token.substring(2, token.length - 2)}~~**"
                    token.startsWith("*") && token.endsWith("*") && token.length >= 2 -> "*~~${token.substring(1, token.length - 1)}~~*"
                    else -> "~~$token~~"
                }
            }
            "`" -> {
                when {
                    token.startsWith("`") && token.endsWith("`") && token.length >= 2 -> token.substring(1, token.length - 1)
                    else -> "`$token`"
                }
            }
            else -> token
        }
    }
}

/**
 * Visual transformation that completely hides Markdown inline tokens (**, *, ~~, `)
 * while rendering text with rich styles, with precise, crash-proof offset mapping.
 */
class CleanMarkdownVisualTransformation(
    private val textPrimary: Color,
    private val accentColor: Color
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val doc = NoteRichParser.parseInlineMarkdown(raw)
        val transformed = buildAnnotatedString {
            append(doc.cleanText)
            for (t in doc.cleanText.indices) {
                val rawIdx = doc.transToRaw[t]
                if (doc.codeMap.getOrElse(rawIdx) { false }) {
                    addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = accentColor.copy(alpha = 0.14f),
                            color = accentColor
                        ),
                        t,
                        t + 1
                    )
                } else {
                    val isBold = doc.boldMap.getOrElse(rawIdx) { false }
                    val isItalic = doc.italicMap.getOrElse(rawIdx) { false }
                    val isStrike = doc.strikeMap.getOrElse(rawIdx) { false }

                    if (isBold || isItalic || isStrike) {
                        addStyle(
                            SpanStyle(
                                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                                textDecoration = if (isStrike) TextDecoration.LineThrough else null,
                                color = if (isStrike) textPrimary.copy(alpha = 0.65f) else textPrimary
                            ),
                            t,
                            t + 1
                        )
                    }
                }
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                return doc.rawToTrans[clamped].coerceIn(0, doc.cleanText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, doc.cleanText.length)
                return doc.transToRaw.getOrElse(clamped) { raw.length }.coerceIn(0, raw.length)
            }
        }

        return TransformedText(transformed, offsetMapping)
    }
}

object NoteTextFormatEngine {
    fun wrapSelection(current: TextFieldValue, prefix: String, suffix: String): TextFieldValue {
        val text = current.text
        val selStart = minOf(current.selection.start, current.selection.end).coerceIn(0, text.length)
        val selEnd = maxOf(current.selection.start, current.selection.end).coerceIn(0, text.length)

        return if (selStart != selEnd) {
            val before = text.substring(0, selStart)
            val selected = text.substring(selStart, selEnd)
            val after = text.substring(selEnd)
            val newText = before + prefix + selected + suffix + after
            val newCursor = selStart + prefix.length + selected.length + suffix.length
            TextFieldValue(newText, selection = TextRange(newCursor))
        } else {
            val before = text.substring(0, selStart)
            val after = text.substring(selStart)
            val newText = before + prefix + suffix + after
            val newCursor = selStart + prefix.length
            TextFieldValue(newText, selection = TextRange(newCursor))
        }
    }

    fun insertLinePrefix(current: TextFieldValue, prefix: String): TextFieldValue {
        val text = current.text
        val cursor = current.selection.start.coerceIn(0, text.length)
        val lastNewline = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0))
        val lineStart = if (lastNewline == -1) 0 else lastNewline + 1
        val before = text.substring(0, lineStart)
        val after = text.substring(lineStart)
        val newText = before + prefix + after
        val newCursor = cursor + prefix.length
        return TextFieldValue(newText, selection = TextRange(newCursor))
    }

    fun insertSnippet(current: TextFieldValue, snippet: String): TextFieldValue {
        val text = current.text
        val cursor = current.selection.start.coerceIn(0, text.length)
        val before = text.substring(0, cursor)
        val after = text.substring(cursor)
        val newText = before + snippet + after
        val newCursor = cursor + snippet.length
        return TextFieldValue(newText, selection = TextRange(newCursor))
    }
}
