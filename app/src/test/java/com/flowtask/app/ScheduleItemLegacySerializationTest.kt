package com.flowtask.app

import com.flowtask.app.data.local.model.ScheduleItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleItemLegacySerializationTest {

    /**
     * Pure JVM JSON parser helper for testing legacy JSON payload parsing
     * without relying on android.jar's unmocked org.json stubs on JVM.
     */
    private fun parseLegacyJsonItem(jsonStr: String): ScheduleItem {
        fun extractString(key: String): String? {
            val regex = Regex("\"$key\"\\s*:\\s*(?:\"([^\"]*)\"|(null))")
            val match = regex.find(jsonStr) ?: return null
            return if (match.groupValues[2] == "null") null else match.groupValues[1]
        }

        fun extractInt(key: String): Int? {
            val regex = Regex("\"$key\"\\s*:\\s*(-?\\d+|null)")
            val match = regex.find(jsonStr) ?: return null
            val value = match.groupValues[1]
            return if (value == "null") null else value.toIntOrNull()
        }

        val id = extractString("id") ?: "default-id"
        val dayOfWeek = extractInt("dayOfWeek") ?: 1
        // Legacy JSON contains "subject", with fallback to generalized "title"
        val subject = extractString("subject") ?: extractString("title") ?: ""
        val startMinutes = extractInt("startMinutes") ?: 540
        val endMinutes = extractInt("endMinutes")
        // Legacy JSON contains "roomOrNote", with fallback to generalized "locationOrNote"
        val roomOrNote = extractString("roomOrNote") ?: extractString("locationOrNote")

        return ScheduleItem(
            id = id,
            dayOfWeek = dayOfWeek,
            subject = subject,
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            roomOrNote = roomOrNote
        )
    }

    private fun serializeToLegacyJson(item: ScheduleItem): String {
        val endPart = if (item.endMinutes != null) ""","endMinutes":${item.endMinutes}""" else ""
        val roomPart = if (item.roomOrNote != null) ""","roomOrNote":"${item.roomOrNote}"""" else ""
        return """{"id":"${item.id}","dayOfWeek":${item.dayOfWeek},"subject":"${item.subject}","startMinutes":${item.startMinutes}$endPart$roomPart}"""
    }

    @Test
    fun testLegacyJsonDeserializationAndHelperProperties() {
        // Legacy JSON simulating SharedPreferences content stored before generalization
        val legacyJson1 = """
            {
                "id": "legacy-item-1",
                "dayOfWeek": 1,
                "subject": "Висша математика 1",
                "startMinutes": 540,
                "endMinutes": 630,
                "roomOrNote": "Зала 204"
            }
        """.trimIndent()

        val legacyJson2 = """
            {
                "id": "legacy-item-2",
                "dayOfWeek": 3,
                "subject": "Програмиране",
                "startMinutes": 660,
                "endMinutes": null,
                "roomOrNote": null
            }
        """.trimIndent()

        val item1 = parseLegacyJsonItem(legacyJson1)
        val item2 = parseLegacyJsonItem(legacyJson2)

        // Item 1 verification: legacy fields present
        assertEquals("legacy-item-1", item1.id)
        assertEquals(1, item1.dayOfWeek)
        assertEquals(540, item1.startMinutes)
        assertEquals(630, item1.endMinutes)
        // Backward compatibility
        assertEquals("Висша математика 1", item1.subject)
        assertEquals("Зала 204", item1.roomOrNote)
        // Generalized terminology getters
        assertEquals("Висша математика 1", item1.title)
        assertEquals("Зала 204", item1.locationOrNote)

        // Item 2 verification: null roomOrNote
        assertEquals("legacy-item-2", item2.id)
        assertEquals(3, item2.dayOfWeek)
        assertEquals(660, item2.startMinutes)
        assertNull(item2.endMinutes)
        // Backward compatibility
        assertEquals("Програмиране", item2.subject)
        assertNull(item2.roomOrNote)
        // Generalized terminology getters
        assertEquals("Програмиране", item2.title)
        assertNull(item2.locationOrNote)
    }

    @Test
    fun testSerializationPreservesLegacyFields() {
        val item = ScheduleItem(
            id = "new-item-42",
            dayOfWeek = 5,
            subject = "Екипен ретроспектив",
            startMinutes = 900,
            endMinutes = 960,
            roomOrNote = "Конферентна зала Б"
        )

        val json = serializeToLegacyJson(item)

        // Must preserve legacy keys in JSON representation
        assertTrue("Serialized JSON must contain 'subject'", json.contains(""""subject":"Екипен ретроспектив""""))
        assertTrue("Serialized JSON must contain 'roomOrNote'", json.contains(""""roomOrNote":"Конферентна зала Б""""))
        assertTrue("Serialized JSON must contain 'startMinutes'", json.contains(""""startMinutes":900"""))
        assertTrue("Serialized JSON must contain 'endMinutes'", json.contains(""""endMinutes":960"""))

        // Reconstruct from serialized json
        val reconstructed = parseLegacyJsonItem(json)
        assertEquals(item.id, reconstructed.id)
        assertEquals(item.dayOfWeek, reconstructed.dayOfWeek)
        assertEquals(item.subject, reconstructed.subject)
        assertEquals(item.title, reconstructed.title)
        assertEquals(item.roomOrNote, reconstructed.roomOrNote)
        assertEquals(item.locationOrNote, reconstructed.locationOrNote)
        assertEquals(item.startMinutes, reconstructed.startMinutes)
        assertEquals(item.endMinutes, reconstructed.endMinutes)
    }

    @Test
    fun testTitleAndLocationOrNoteInAllVariations() {
        // Test empty strings, whitespace, special characters
        val specialItem = ScheduleItem(
            dayOfWeek = 7,
            subject = "Йога & Медитация #1 (Специална)",
            startMinutes = 420,
            roomOrNote = "Парк / Студио 3 & Онлайн"
        )

        assertEquals("Йога & Медитация #1 (Специална)", specialItem.title)
        assertEquals("Парк / Студио 3 & Онлайн", specialItem.locationOrNote)
        assertEquals(specialItem.subject, specialItem.title)
        assertEquals(specialItem.roomOrNote, specialItem.locationOrNote)

        val emptyItem = ScheduleItem(
            dayOfWeek = 4,
            subject = "",
            startMinutes = 600,
            roomOrNote = null
        )
        assertEquals("", emptyItem.title)
        assertNull(emptyItem.locationOrNote)
    }
}
