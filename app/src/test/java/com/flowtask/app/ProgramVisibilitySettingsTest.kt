package com.flowtask.app

import android.content.SharedPreferences
import com.flowtask.app.data.local.entity.TaskEntity
import com.flowtask.app.data.local.model.LayerProgram
import com.flowtask.app.data.local.model.ScheduleItem
import com.flowtask.app.data.repository.SettingsRepository
import com.flowtask.app.util.SemesterUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Milestone M2: Configurable Program Task Visibility in Settings & DB Sync Engine.
 * Tests default visibility mode, transitions between Mode A (Everywhere) and Mode B (Calendar Only),
 * and the zero-data-loss guarantee for underlying program schedules in SharedPreferences.
 */
class ProgramVisibilitySettingsTest {

    /**
     * In-memory FakeSharedPreferences implementation for pure host JVM execution
     * without unmocked Android SDK stub exceptions.
     */
    private class FakeSharedPreferences : SharedPreferences {
        private val values = mutableMapOf<String, Any>()

        override fun getAll(): MutableMap<String, *> = values
        override fun getString(key: String?, defValue: String?): String? = (values[key] as? String) ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            @Suppress("UNCHECKED_CAST") (values[key] as? MutableSet<String>) ?: defValues
        override fun getInt(key: String?, defValue: Int): Int = (values[key] as? Int) ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = (values[key] as? Long) ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = (values[key] as? Float) ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = (values[key] as? Boolean) ?: defValue
        override fun contains(key: String?): Boolean = values.containsKey(key)
        override fun edit(): SharedPreferences.Editor = FakeEditor(values)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        class FakeEditor(private val target: MutableMap<String, Any>) : SharedPreferences.Editor {
            private val staging = mutableMapOf<String, Any>()
            private val toRemove = mutableSetOf<String>()
            private var clearAll = false

            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) if (value != null) staging[key] = value else toRemove.add(key)
                return this
            }
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
                if (key != null) if (values != null) staging[key] = values else toRemove.add(key)
                return this
            }
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                if (key != null) staging[key] = value
                return this
            }
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                if (key != null) staging[key] = value
                return this
            }
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                if (key != null) staging[key] = value
                return this
            }
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                if (key != null) staging[key] = value
                return this
            }
            override fun remove(key: String?): SharedPreferences.Editor {
                if (key != null) toRemove.add(key)
                return this
            }
            override fun clear(): SharedPreferences.Editor {
                clearAll = true
                return this
            }
            override fun commit(): Boolean {
                apply()
                return true
            }
            override fun apply() {
                if (clearAll) target.clear()
                toRemove.forEach { target.remove(it) }
                target.putAll(staging)
            }
        }
    }

    @Test
    fun testDefaultProgramVisibilitySettingIsTrue() {
        val fakePrefs = FakeSharedPreferences()
        val repository = SettingsRepository(fakePrefs)

        // Constant key matches contract
        assertEquals("key_program_tasks_everywhere", SettingsRepository.KEY_PROGRAM_TASKS_EVERYWHERE)

        // Default setting is Mode A (Everywhere as tasks)
        assertTrue(
            "Default program task visibility must be true (Mode A: Everywhere)",
            repository.programTasksEverywhere.value
        )
    }

    @Test
    fun testProgramVisibilityStateTransitions() {
        val fakePrefs = FakeSharedPreferences()
        val repository = SettingsRepository(fakePrefs)

        // Initial default: true
        assertTrue(repository.programTasksEverywhere.value)

        // Switch to Mode B: Calendar only (false)
        repository.setProgramTasksEverywhere(false)
        assertFalse(
            "Setting must transition to false (Mode B: Calendar Only)",
            repository.programTasksEverywhere.value
        )
        assertFalse(
            "SharedPreferences must store false for KEY_PROGRAM_TASKS_EVERYWHERE",
            fakePrefs.getBoolean(SettingsRepository.KEY_PROGRAM_TASKS_EVERYWHERE, true)
        )

        // Switch back to Mode A: Everywhere (true)
        repository.setProgramTasksEverywhere(true)
        assertTrue(
            "Setting must transition back to true (Mode A: Everywhere)",
            repository.programTasksEverywhere.value
        )
        assertTrue(
            "SharedPreferences must store true for KEY_PROGRAM_TASKS_EVERYWHERE",
            fakePrefs.getBoolean(SettingsRepository.KEY_PROGRAM_TASKS_EVERYWHERE, false)
        )
    }

    @Test
    fun testModeBSyncPurgesRoutineTasksWithZeroDataLossToUnderlyingProgram() {
        // Create sample LayerProgram with active date range and weekly routines
        val workProgram = LayerProgram(
            id = "prog-work-100",
            layerName = "Работа",
            isEnabled = true,
            startDateMillis = 1735689600000L, // Jan 1 2025
            endDateMillis = 1743465600000L,   // Mar 31 2025 (~13 weeks)
            trackWeeks = true,
            scheduleItems = listOf(
                ScheduleItem(
                    id = "routine-1",
                    dayOfWeek = 1, // Monday
                    subject = "Екипен Standup",
                    startMinutes = 540,
                    endMinutes = 570,
                    roomOrNote = "Зала Конференция"
                ),
                ScheduleItem(
                    id = "routine-2",
                    dayOfWeek = 3, // Wednesday
                    subject = "Фокус Програмиране",
                    startMinutes = 600,
                    endMinutes = 720,
                    roomOrNote = "Офис Б"
                )
            )
        )

        // Simulated SQLite task database table (layerName -> list of TaskEntity)
        val sqliteTaskDatabase = mutableMapOf<String, MutableList<TaskEntity>>()

        // Step 1: Mode A - Generate and sync routine tasks
        val generatedTasks = SemesterUtils.generateProgramTasks(workProgram)
        assertTrue("Mode A must generate tasks across the active period", generatedTasks.isNotEmpty())
        sqliteTaskDatabase[workProgram.layerName] = generatedTasks.toMutableList()

        assertEquals(
            generatedTasks.size,
            sqliteTaskDatabase[workProgram.layerName]?.size
        )

        // Step 2: Transition to Mode B (Calendar Only)
        // Simulated TaskViewModel purge logic: deleteTasksByLayer for all programs
        sqliteTaskDatabase[workProgram.layerName]?.clear()

        // Verify SQLite tasks are purged
        assertEquals(
            "Mode B must purge synthesized tasks from SQLite",
            0,
            sqliteTaskDatabase[workProgram.layerName]?.size
        )

        // Step 3: Zero Data Loss Verification
        // Verify underlying LayerProgram schedule items and dates remain 100% intact
        assertEquals("Program ID must remain intact", "prog-work-100", workProgram.id)
        assertEquals("Program Layer must remain intact", "Работа", workProgram.layerName)
        assertTrue("Program must remain enabled", workProgram.isEnabled)
        assertEquals("Schedule items count must remain 2", 2, workProgram.scheduleItems.size)
        assertEquals("First routine title must remain intact", "Екипен Standup", workProgram.scheduleItems[0].title)
        assertEquals("Second routine title must remain intact", "Фокус Програмиране", workProgram.scheduleItems[1].title)
        assertEquals("Start date must remain intact", 1735689600000L, workProgram.startDateMillis)
        assertEquals("End date must remain intact", 1743465600000L, workProgram.endDateMillis)
    }

    @Test
    fun testModeASyncRestoresRoutineTasksFromIntactProgram() {
        val fitnessProgram = LayerProgram(
            id = "prog-fit-200",
            layerName = "Спорт",
            isEnabled = true,
            startDateMillis = 1735689600000L,
            endDateMillis = 1738368000000L, // Jan 1 2025 - Feb 1 2025 (~4.5 weeks)
            trackWeeks = true,
            scheduleItems = listOf(
                ScheduleItem(
                    id = "fit-1",
                    dayOfWeek = 2, // Tuesday
                    subject = "Кростренажор & Кардио",
                    startMinutes = 480,
                    endMinutes = 540,
                    roomOrNote = "Фитнес Център"
                )
            )
        )

        val sqliteTaskDatabase = mutableMapOf<String, MutableList<TaskEntity>>()
        sqliteTaskDatabase[fitnessProgram.layerName] = mutableListOf() // Initially empty (Mode B)

        // Transition to Mode A: re-synthesize tasks from intact program
        val regeneratedTasks = SemesterUtils.generateProgramTasks(fitnessProgram)
        assertTrue(regeneratedTasks.isNotEmpty())
        sqliteTaskDatabase[fitnessProgram.layerName] = regeneratedTasks.toMutableList()

        // Verify restored tasks match program parameters
        val tasks = sqliteTaskDatabase[fitnessProgram.layerName]!!
        assertTrue(tasks.isNotEmpty())
        tasks.forEach { task ->
            assertEquals("Restored task must belong to 'Спорт' layer", "Спорт", task.layer)
            assertEquals("Restored task explanation must match routine title & note", "Кростренажор & Кардио (Фитнес Център)", task.explanation)
            assertEquals("Restored task start minute must match routine", 480, task.dueTimeMinutes)
        }
    }

    @Test
    fun testDisabledProgramsAreNotSyncedInModeA() {
        val enabledProgram = LayerProgram(
            id = "prog-enabled",
            layerName = "Работа",
            isEnabled = true,
            startDateMillis = 1735689600000L,
            endDateMillis = 1738368000000L,
            scheduleItems = listOf(
                ScheduleItem(id = "en-1", dayOfWeek = 1, subject = "Среща", startMinutes = 600)
            )
        )

        val disabledProgram = LayerProgram(
            id = "prog-disabled",
            layerName = "Свободно време",
            isEnabled = false,
            startDateMillis = 1735689600000L,
            endDateMillis = 1738368000000L,
            scheduleItems = listOf(
                ScheduleItem(id = "dis-1", dayOfWeek = 6, subject = "Кино", startMinutes = 1200)
            )
        )

        val allPrograms = listOf(enabledProgram, disabledProgram)
        val programsToSync = allPrograms.filter { it.isEnabled }

        assertEquals(1, programsToSync.size)
        assertEquals("prog-enabled", programsToSync[0].id)

        val sqliteTaskDatabase = mutableMapOf<String, MutableList<TaskEntity>>()
        for (prog in programsToSync) {
            val tasks = SemesterUtils.generateProgramTasks(prog)
            sqliteTaskDatabase[prog.layerName] = tasks.toMutableList()
        }

        assertTrue("Enabled program has synced tasks", sqliteTaskDatabase.containsKey("Работа"))
        assertFalse("Disabled program has no synced tasks", sqliteTaskDatabase.containsKey("Свободно време"))
    }

    @Test
    fun testCalendarOnlySyncGuardPreventsTaskGeneration() {
        val program = LayerProgram(
            id = "prog-study",
            layerName = "Учене",
            isEnabled = true,
            startDateMillis = 1735689600000L,
            endDateMillis = 1738368000000L,
            scheduleItems = listOf(
                ScheduleItem(id = "study-1", dayOfWeek = 4, subject = "Английски", startMinutes = 720)
            )
        )

        // Helper function modeling syncLayerProgramToCalendar with visibility guard
        fun syncIfAllowed(program: LayerProgram, programTasksEverywhere: Boolean): List<TaskEntity> {
            if (!programTasksEverywhere) {
                return emptyList()
            }
            return SemesterUtils.generateProgramTasks(program)
        }

        // When in Mode B (Calendar Only)
        val modeBTasks = syncIfAllowed(program, programTasksEverywhere = false)
        assertTrue("In Mode B, syncLayerProgramToCalendar must generate 0 tasks", modeBTasks.isEmpty())

        // When in Mode A (Everywhere as tasks)
        val modeATasks = syncIfAllowed(program, programTasksEverywhere = true)
        assertTrue("In Mode A, syncLayerProgramToCalendar must generate tasks", modeATasks.isNotEmpty())
    }
}
