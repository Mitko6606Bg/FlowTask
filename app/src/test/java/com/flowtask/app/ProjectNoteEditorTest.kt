package com.flowtask.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.OpenInFull

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.flowtask.app.ui.components.NoteTextFormatEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectNoteEditorTest {

    @Test
    fun testWrapSelection_withSelectedText() {
        // Selection around "bold"
        val initial = TextFieldValue(
            text = "Make bold word",
            selection = TextRange(5, 9)
        )
        val result = NoteTextFormatEngine.wrapSelection(initial, "**", "**")
        assertEquals("Make **bold** word", result.text)
        assertEquals(13, result.selection.start)
        assertEquals(13, result.selection.end)
    }

    @Test
    fun testWrapSelection_emptySelectionPlacesCursorBetweenTags() {
        val initial = TextFieldValue(
            text = "Hello world",
            selection = TextRange(5, 5)
        )
        val result = NoteTextFormatEngine.wrapSelection(initial, "**", "**")
        assertEquals("Hello**** world", result.text)
        // Cursor placed right inside the tags
        assertEquals(7, result.selection.start)
        assertEquals(7, result.selection.end)
    }

    @Test
    fun testWrapSelection_italicAndCodeTags() {
        val italicInit = TextFieldValue(
            text = "italics",
            selection = TextRange(0, 7)
        )
        val italicResult = NoteTextFormatEngine.wrapSelection(italicInit, "*", "*")
        assertEquals("*italics*", italicResult.text)

        val codeInit = TextFieldValue(
            text = "val x = 42",
            selection = TextRange(0, 10)
        )
        val codeResult = NoteTextFormatEngine.wrapSelection(codeInit, "`", "`")
        assertEquals("`val x = 42`", codeResult.text)
    }

    @Test
    fun testInsertLinePrefix_atLineStart() {
        val initial = TextFieldValue(
            text = "First line item",
            selection = TextRange(0, 0)
        )
        val result = NoteTextFormatEngine.insertLinePrefix(initial, "# ")
        assertEquals("# First line item", result.text)
        assertEquals(2, result.selection.start)
    }

    @Test
    fun testInsertLinePrefix_cursorInMiddleOfLinePrefixesLineStart() {
        val initial = TextFieldValue(
            text = "First line\nSecond line is here",
            selection = TextRange(18, 18) // Inside "Second line is here"
        )
        val result = NoteTextFormatEngine.insertLinePrefix(initial, "- [ ] ")
        assertEquals("First line\n- [ ] Second line is here", result.text)
        assertEquals(18 + 6, result.selection.start)
    }

    @Test
    fun testInsertLinePrefix_bulletAndNumberedList() {
        val bulletInit = TextFieldValue(
            text = "Item",
            selection = TextRange(2, 2)
        )
        val bulletResult = NoteTextFormatEngine.insertLinePrefix(bulletInit, "• ")
        assertEquals("• Item", bulletResult.text)

        val numInit = TextFieldValue(
            text = "First step",
            selection = TextRange(4, 4)
        )
        val numResult = NoteTextFormatEngine.insertLinePrefix(numInit, "1. ")
        assertEquals("1. First step", numResult.text)
    }

    @Test
    fun testInsertSnippet_divider() {
        val initial = TextFieldValue(
            text = "Paragraph one\nParagraph two",
            selection = TextRange(13, 13)
        )
        val result = NoteTextFormatEngine.insertSnippet(initial, "\n---\n")
        assertEquals("Paragraph one\n---\n\nParagraph two", result.text)
        assertEquals(13 + 5, result.selection.start)
    }

    @Test
    fun testEdgeCases_emptyContent() {
        val empty = TextFieldValue("", TextRange.Zero)
        val wrapped = NoteTextFormatEngine.wrapSelection(empty, "**", "**")
        assertEquals("****", wrapped.text)
        assertEquals(2, wrapped.selection.start)

        val prefixed = NoteTextFormatEngine.insertLinePrefix(empty, "# ")
        assertEquals("# ", prefixed.text)
        assertEquals(2, prefixed.selection.start)
    }

    @Test
    fun testStringsLoading() {
        val bg = com.flowtask.app.util.BulgarianStrings
        assertEquals("FlowTask", bg.appName)
        val en = com.flowtask.app.util.EnglishStrings
        assertEquals("FlowTask", en.appName)
    }

    @Test
    fun testIcons() {
        val i1 = androidx.compose.material.icons.Icons.Default.OpenInFull
        val i2 = androidx.compose.material.icons.Icons.Default.CloseFullscreen
        org.junit.Assert.assertNotNull(i1)
        org.junit.Assert.assertNotNull(i2)
    }

}
