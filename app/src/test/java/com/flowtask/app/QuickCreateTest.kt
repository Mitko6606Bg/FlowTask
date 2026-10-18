package com.flowtask.app

import com.flowtask.app.ui.quickcreate.QuickCreateTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickCreateTest {

    @Test
    fun testQuickCreateTabsExcludeProject() {
        val tabNames = QuickCreateTab.entries.map { it.name }
        assertEquals(2, tabNames.size)
        assertTrue(tabNames.contains("TASK"))
        assertTrue(tabNames.contains("INSTANT"))
        assertFalse("PROJECT tab must be removed from QuickCreateTab", tabNames.contains("PROJECT"))
    }
}
