package com.flowtask.app

import com.flowtask.app.data.local.entity.ProjectEntity
import com.flowtask.app.data.local.entity.ProjectFolderEntity
import com.flowtask.app.data.local.entity.ProjectNoteEntity
import com.flowtask.app.data.local.entity.ProjectTaskEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectDomainTest {

    @Test
    fun testProjectEntityCreation() {
        val project = ProjectEntity(
            name = "Стартиране на подкаст",
            description = "Епизоди, теми и гости",
            colorHex = "#2A8575",
            iconName = "mic"
        )
        assertEquals("Стартиране на подкаст", project.name)
        assertEquals("Епизоди, теми и гости", project.description)
    }

    @Test
    fun testNestedFolderHierarchy() {
        val rootFolder = ProjectFolderEntity(
            id = 1,
            projectId = 100,
            parentFolderId = null,
            name = "Епизод 1"
        )
        assertNull(rootFolder.parentFolderId)

        val subFolder = ProjectFolderEntity(
            id = 2,
            projectId = 100,
            parentFolderId = 1,
            name = "Аудио обработка"
        )
        assertEquals(1L, subFolder.parentFolderId)
        assertEquals("Аудио обработка", subFolder.name)
    }

    @Test
    fun testProjectTasksAreUndated() {
        // Project tasks operate at their own pace without dates or times
        val projectTask = ProjectTaskEntity(
            projectId = 100,
            folderId = 2,
            explanation = "Монтаж на интрото и почистване на фонов шум",
            isCompleted = false
        )
        assertEquals("Монтаж на интрото и почистване на фонов шум", projectTask.explanation)
        assertFalse(projectTask.isCompleted)
    }

    @Test
    fun testProjectNotes() {
        val note = ProjectNoteEntity(
            projectId = 100,
            folderId = 2,
            title = "Списък с въпроси",
            content = "1. Как започна в сферата?\n2. Кои бяха най-големите трудности?"
        )
        assertEquals("Списък с въпроси", note.title)
        assertTrue(note.content.contains("Как започна"))
    }

    @Test
    fun testProjectPinningDefaultAndToggle() {
        val project = ProjectEntity(
            name = "Стартиране на подкаст",
            description = "Епизоди, теми и гости",
            colorHex = "#2A8575",
            iconName = "mic"
        )
        assertFalse(project.isPinned)

        val pinnedProject = project.copy(isPinned = true)
        assertTrue(pinnedProject.isPinned)
    }

    @Test
    fun testProjectSortingPinnedFirst() {
        val p1 = ProjectEntity(id = 1, name = "Alpha", isPinned = false, sortOrder = 0, createdAtMillis = 1000)
        val p2 = ProjectEntity(id = 2, name = "Beta", isPinned = true, sortOrder = 1, createdAtMillis = 2000)
        val p3 = ProjectEntity(id = 3, name = "Gamma", isPinned = false, sortOrder = 0, createdAtMillis = 3000)
        val p4 = ProjectEntity(id = 4, name = "Delta", isPinned = true, sortOrder = 0, createdAtMillis = 1500)

        val list = listOf(p1, p2, p3, p4)
        val sorted = list.sortedWith(
            compareByDescending<ProjectEntity> { it.isPinned }
                .thenBy { it.sortOrder }
                .thenByDescending { it.createdAtMillis }
        )

        // Pinned projects first: p4 (sortOrder 0) then p2 (sortOrder 1)
        assertEquals(4L, sorted[0].id)
        assertEquals(2L, sorted[1].id)
        // Then unpinned projects: p3 (sortOrder 0, createdAt 3000) then p1 (sortOrder 0, createdAt 1000)
        assertEquals(3L, sorted[2].id)
        assertEquals(1L, sorted[3].id)
    }

    @Test
    fun testIsolatedReorderingPinnedAndUnpinnedLists() {
        val pinned = mutableListOf(
            ProjectEntity(id = 1, name = "P1", isPinned = true, sortOrder = 0),
            ProjectEntity(id = 2, name = "P2", isPinned = true, sortOrder = 1),
            ProjectEntity(id = 3, name = "P3", isPinned = true, sortOrder = 2)
        )
        val unpinned = mutableListOf(
            ProjectEntity(id = 4, name = "U1", isPinned = false, sortOrder = 0),
            ProjectEntity(id = 5, name = "U2", isPinned = false, sortOrder = 1)
        )

        // Swap P1 and P3 inside pinned list
        val movedPinned = pinned.removeAt(0)
        pinned.add(2, movedPinned)
        assertEquals(listOf(2L, 3L, 1L), pinned.map { it.id })

        // Swap U1 and U2 inside unpinned list
        val movedUnpinned = unpinned.removeAt(0)
        unpinned.add(1, movedUnpinned)
        assertEquals(listOf(5L, 4L), unpinned.map { it.id })

        // Combined reordered list persists without crossing sections
        val combined = (pinned + unpinned).mapIndexed { idx, p -> p.copy(sortOrder = idx) }
        assertEquals(listOf(2L, 3L, 1L, 5L, 4L), combined.map { it.id })
        assertEquals(listOf(0, 1, 2, 3, 4), combined.map { it.sortOrder })
        assertTrue(combined.take(3).all { it.isPinned })
        assertTrue(combined.drop(3).none { it.isPinned })
    }

    @Test
    fun testGlobalFolderHierarchyMatching() {
        val project1 = ProjectEntity(id = 10, name = "Подкаст", isPinned = false)
        val f1 = ProjectFolderEntity(id = 1, projectId = 10, parentFolderId = null, name = "Епизод 1")
        val f2 = ProjectFolderEntity(id = 2, projectId = 10, parentFolderId = 1, name = "Аудио обработка")
        val f3 = ProjectFolderEntity(id = 3, projectId = 10, parentFolderId = 2, name = "Мастеринг")

        val allFolders = listOf(f1, f2, f3)
        val foldersById = allFolders.associateBy { it.id }

        // Test path generation for deep nested folder f3
        val path = mutableListOf<ProjectFolderEntity>()
        var curr: ProjectFolderEntity? = f3
        val visited = mutableSetOf<Long>()
        while (curr != null && visited.add(curr.id)) {
            path.add(0, curr)
            curr = curr.parentFolderId?.let { foldersById[it] }
        }

        val pathString = path.joinToString(" > ") { it.name }
        assertEquals("Епизод 1 > Аудио обработка > Мастеринг", pathString)
        assertEquals(listOf(1L, 2L, 3L), path.map { it.id })

        val match = com.flowtask.app.ui.viewmodel.FolderMatch(
            folder = f3,
            pathString = pathString,
            hierarchy = path
        )
        assertEquals("Мастеринг", match.folder.name)
        assertEquals(3, match.hierarchy.size)
    }

    @Test
    fun testSearchPrioritization() {
        val p1 = ProjectEntity(id = 1, name = "Блог Дизайн", isPinned = false, sortOrder = 0)
        val p2 = ProjectEntity(id = 2, name = "Уеб Дизайн", isPinned = true, sortOrder = 1)
        val p3 = ProjectEntity(id = 3, name = "Мобилно Приложение", isPinned = false, sortOrder = 2)

        // p3 matches via a nested folder "Дизайн система", p1 matches directly by name, p2 matches directly by name and is pinned
        val m1 = com.flowtask.app.ui.viewmodel.ProjectSearchMatch(
            project = p1,
            isDirectNameMatch = true,
            matchingFolders = emptyList()
        )
        val m2 = com.flowtask.app.ui.viewmodel.ProjectSearchMatch(
            project = p2,
            isDirectNameMatch = true,
            matchingFolders = emptyList()
        )
        val m3 = com.flowtask.app.ui.viewmodel.ProjectSearchMatch(
            project = p3,
            isDirectNameMatch = false,
            matchingFolders = listOf(
                com.flowtask.app.ui.viewmodel.FolderMatch(
                    folder = ProjectFolderEntity(id = 9, projectId = 3, parentFolderId = null, name = "Дизайн система"),
                    pathString = "Дизайн система",
                    hierarchy = emptyList()
                )
            )
        )

        val matches = listOf(m1, m3, m2)
        val sorted = matches.sortedWith(
            compareByDescending<com.flowtask.app.ui.viewmodel.ProjectSearchMatch> { it.project.isPinned }
                .thenByDescending { it.isDirectNameMatch }
                .thenBy { it.project.sortOrder }
        )

        // 1st: m2 (Pinned + direct name match)
        assertEquals(2L, sorted[0].project.id)
        // 2nd: m1 (Unpinned + direct name match)
        assertEquals(1L, sorted[1].project.id)
        // 3rd: m3 (Unpinned + folder-only match)
        assertEquals(3L, sorted[2].project.id)
    }
}
