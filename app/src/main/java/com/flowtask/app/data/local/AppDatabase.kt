package com.flowtask.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.flowtask.app.data.local.dao.InstantDao
import com.flowtask.app.data.local.dao.ProjectDao
import com.flowtask.app.data.local.dao.TaskDao
import com.flowtask.app.data.local.entity.InstantFolderEntity
import com.flowtask.app.data.local.entity.InstantSnapEntity
import com.flowtask.app.data.local.entity.ProjectEntity
import com.flowtask.app.data.local.entity.ProjectFolderEntity
import com.flowtask.app.data.local.entity.ProjectNoteEntity
import com.flowtask.app.data.local.entity.ProjectSubTaskEntity
import com.flowtask.app.data.local.entity.ProjectTaskEntity
import com.flowtask.app.data.local.entity.SubTaskEntity
import com.flowtask.app.data.local.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        TaskEntity::class,
        SubTaskEntity::class,
        ProjectEntity::class,
        ProjectFolderEntity::class,
        ProjectTaskEntity::class,
        ProjectSubTaskEntity::class,
        ProjectNoteEntity::class,
        InstantSnapEntity::class,
        InstantFolderEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun instantDao(): InstantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN layer TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `instant_snaps` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `imagePath` TEXT NOT NULL,
                        `noteText` TEXT,
                        `folderName` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_instant_snaps_folderName` ON `instant_snaps` (`folderName`)")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `instant_folders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_instant_folders_name` ON `instant_folders` (`name`)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN startDateMillis INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE projects ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flowtask.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                populateInitialData(database.taskDao(), database.projectDao())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(taskDao: TaskDao, projectDao: ProjectDao) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val todayStart = cal.timeInMillis

            // Sample Today Tasks with Layers
            val task1 = TaskEntity(
                explanation = "Сутрешно планиране на деня и преглед на целите",
                dueDateMillis = todayStart,
                dueTimeMinutes = 9 * 60,
                layer = "Лично",
                isCompleted = false,
                sortOrder = 0
            )
            val task1Id = taskDao.insertTask(task1)
            taskDao.insertSubTasks(
                listOf(
                    SubTaskEntity(taskId = task1Id, explanation = "Преглед на важните приоритети", isCompleted = false, sortOrder = 0),
                    SubTaskEntity(taskId = task1Id, explanation = "Разпределяне на време за почивка", isCompleted = false, sortOrder = 1),
                    SubTaskEntity(taskId = task1Id, explanation = "Подготовка на работната среда", isCompleted = false, sortOrder = 2)
                )
            )

            val task2 = TaskEntity(
                explanation = "Завършване на новия дизайн на приложението",
                dueDateMillis = todayStart,
                dueTimeMinutes = null,
                layer = "Работа",
                isCompleted = false,
                sortOrder = 1
            )
            val task2Id = taskDao.insertTask(task2)
            taskDao.insertSubTasks(
                listOf(
                    SubTaskEntity(taskId = task2Id, explanation = "Тестване на анимациите за прелистване", isCompleted = false, sortOrder = 0),
                    SubTaskEntity(taskId = task2Id, explanation = "Проверка на цветовите контрасти", isCompleted = false, sortOrder = 1)
                )
            )

            val task3 = TaskEntity(
                explanation = "Пазаруване и подреждане на кухнята",
                dueDateMillis = todayStart,
                dueTimeMinutes = 17 * 60 + 30,
                layer = "Дом",
                isCompleted = false,
                sortOrder = 2
            )
            taskDao.insertTask(task3)

            // Sample Upcoming Tasks with Layers
            val in2Days = cal.apply { add(Calendar.DAY_OF_YEAR, 2) }.timeInMillis
            val task4 = TaskEntity(
                explanation = "Седмична среща с екипа и отчет на резултатите",
                dueDateMillis = in2Days,
                dueTimeMinutes = 11 * 60,
                layer = "Работа",
                isCompleted = false,
                sortOrder = 0
            )
            taskDao.insertTask(task4)

            val in3Days = cal.apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
            val task5 = TaskEntity(
                explanation = "Планиране на ремонт в хола и поправки",
                dueDateMillis = in3Days,
                dueTimeMinutes = null,
                layer = "Дом",
                isCompleted = false,
                sortOrder = 0
            )
            val task5Id = taskDao.insertTask(task5)
            taskDao.insertSubTasks(
                listOf(
                    SubTaskEntity(taskId = task5Id, explanation = "Списък с материали", isCompleted = false, sortOrder = 0),
                    SubTaskEntity(taskId = task5Id, explanation = "Избор на боя", isCompleted = false, sortOrder = 1)
                )
            )

            // Sample Project: "Нов софтуерен продукт"
            val proj1 = ProjectEntity(
                name = "Нова идея за мобилно приложение",
                description = "Концепция, дизайн и разработка на персонален органайзер",
                colorHex = "#2A8575",
                iconName = "rocket"
            )
            val proj1Id = projectDao.insertProject(proj1)

            // Folders for Project 1
            val folderDesignId = projectDao.insertFolder(
                ProjectFolderEntity(
                    projectId = proj1Id,
                    name = "Дизайн & UX",
                    colorHex = "#6366F1"
                )
            )

            val folderDevId = projectDao.insertFolder(
                ProjectFolderEntity(
                    projectId = proj1Id,
                    name = "Разработка (Kotlin)",
                    colorHex = "#2563EB"
                )
            )

            // Tasks in Folder Design
            val projTask1Id = projectDao.insertProjectTask(
                ProjectTaskEntity(
                    projectId = proj1Id,
                    folderId = folderDesignId,
                    explanation = "Избор на цветова палитра и типография за светла тема",
                    isCompleted = true
                )
            )
            projectDao.insertProjectSubTasks(
                listOf(
                    ProjectSubTaskEntity(projectTaskId = projTask1Id, explanation = "Проверка на контраста за четимост", isCompleted = true),
                    ProjectSubTaskEntity(projectTaskId = projTask1Id, explanation = "Създаване на палитра с меки пастелни тонове", isCompleted = true)
                )
            )

            val projTask2Id = projectDao.insertProjectTask(
                ProjectTaskEntity(
                    projectId = proj1Id,
                    folderId = folderDesignId,
                    explanation = "Проектиране на анимации за отмятане на задачи и плъзгане на страници",
                    isCompleted = false
                )
            )

            // Note in Folder Design
            projectDao.insertProjectNote(
                ProjectNoteEntity(
                    projectId = proj1Id,
                    folderId = folderDesignId,
                    title = "UX насоки",
                    content = "Приложението трябва да бъде максимално плавно, без излишни полета за заглавия. Обяснението на задачата е централен елемент."
                )
            )

            // Tasks in Folder Dev
            val projTask3Id = projectDao.insertProjectTask(
                ProjectTaskEntity(
                    projectId = proj1Id,
                    folderId = folderDevId,
                    explanation = "Имплементация на йерархична структура за проекти и папки с Room",
                    isCompleted = false
                )
            )

            // Note in Root Project
            projectDao.insertProjectNote(
                ProjectNoteEntity(
                    projectId = proj1Id,
                    folderId = null,
                    title = "Основна визия за проекта",
                    content = "Свободно пространство за развитие на идеи без ограничения от стриктни крайни срокове и часове."
                )
            )
        }
    }
}
