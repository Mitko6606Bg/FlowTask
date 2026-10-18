package com.flowtask.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import com.flowtask.app.data.repository.SettingsRepository
import com.flowtask.app.ui.screens.MainPagerScreen
import com.flowtask.app.ui.theme.FlowTaskTheme
import com.flowtask.app.ui.viewmodel.InstantViewModel
import com.flowtask.app.ui.viewmodel.ProjectViewModel
import com.flowtask.app.ui.viewmodel.SettingsViewModel
import com.flowtask.app.ui.viewmodel.SettingsViewModelFactory
import com.flowtask.app.ui.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {

    private val taskViewModel: TaskViewModel by viewModels()
    private val projectViewModel: ProjectViewModel by viewModels()
    private val instantViewModel: InstantViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsRepository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val crashFile = java.io.File(getExternalFilesDir(null), "crash.log")
                crashFile.writeText("${java.util.Date()}\nThread: ${thread.name}\n${throwable.stackTraceToString()}\n")
            } catch (_: Exception) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            val language by settingsViewModel.language.collectAsState()
            val accentColor by settingsViewModel.accentColor.collectAsState()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val amoledDarkMode by settingsViewModel.amoledDarkMode.collectAsState()
            val dynamicColorStyle by settingsViewModel.dynamicColorStyle.collectAsState()

            var crashLogText by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                try {
                    val crashFile = java.io.File(getExternalFilesDir(null), "crash.log")
                    if (crashFile.exists()) {
                        val text = crashFile.readText()
                        if (text.isNotBlank()) {
                            crashLogText = text
                        }
                    }
                } catch (_: Exception) {}
            }

            FlowTaskTheme(
                language = language,
                accentColorHex = accentColor,
                themeMode = themeMode,
                amoledDarkMode = amoledDarkMode,
                dynamicColorStyle = dynamicColorStyle
            ) {
                MainPagerScreen(
                    taskViewModel = taskViewModel,
                    projectViewModel = projectViewModel,
                    settingsViewModel = settingsViewModel,
                    instantViewModel = instantViewModel
                )

                if (crashLogText != null) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { crashLogText = null },
                        title = { androidx.compose.material3.Text("Crash Report") },
                        text = {
                            androidx.compose.foundation.text.selection.SelectionContainer {
                                androidx.compose.material3.Text(
                                    text = crashLogText!!,
                                    modifier = androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                                    fontSize = 12.sp
                                )
                            }
                        },
                        confirmButton = {
                            androidx.compose.material3.Button(onClick = {
                                val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Crash Log", crashLogText))
                                val crashFile = java.io.File(getExternalFilesDir(null), "crash.log")
                                crashFile.delete()
                                crashLogText = null
                            }) {
                                androidx.compose.material3.Text("Copy & Clear")
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = {
                                val crashFile = java.io.File(getExternalFilesDir(null), "crash.log")
                                crashFile.delete()
                                crashLogText = null
                            }) {
                                androidx.compose.material3.Text("Dismiss")
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        val programId = intent?.getStringExtra(EXTRA_OPEN_PROGRAM_ID)
        if (!programId.isNullOrBlank()) {
            intent.removeExtra(EXTRA_OPEN_PROGRAM_ID)
            taskViewModel.selectCalendarProgram(programId)
            taskViewModel.openProgramDetails(programId)
        }
    }

    companion object {
        const val EXTRA_OPEN_PROGRAM_ID = "extra_open_program_id"
    }
}
