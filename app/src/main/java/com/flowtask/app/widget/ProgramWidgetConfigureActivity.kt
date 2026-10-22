package com.flowtask.app.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.MainActivity
import com.flowtask.app.R
import com.flowtask.app.data.local.model.LayerProgram
import com.flowtask.app.data.repository.SemesterRepository
import com.flowtask.app.data.repository.SettingsRepository
import com.flowtask.app.ui.theme.FlowTaskTheme
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.DateTimeUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.WindowManager
import androidx.activity.SystemBarStyle

class ProgramWidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(0.35f)

        // Set result to CANCELED initially so launcher knows if user backed out
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val settingsRepo = SettingsRepository(applicationContext)
        val semesterRepo = SemesterRepository(applicationContext)

        setContent {
            val language by settingsRepo.language.collectAsStateWithLifecycle(initialValue = "bg")
            val accentColor by settingsRepo.accentColor.collectAsStateWithLifecycle(initialValue = "#1D7868")
            val themeMode by settingsRepo.themeMode.collectAsStateWithLifecycle(initialValue = "system")

            FlowTaskTheme(
                language = language,
                accentColorHex = accentColor,
                themeMode = themeMode,
                updateWindowBackground = false // Preserves transparent window over homescreen
            ) {
                ProgramWidgetConfigureScreen(
                    appWidgetId = appWidgetId,
                    semesterRepo = semesterRepo,
                    onSave = { selectedProgramId ->
                        saveAndFinish(selectedProgramId)
                    },
                    onDismiss = {
                        finish()
                    }
                )
            }
        }
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    private fun saveAndFinish(programId: String) {
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            ProgramWidgetPrefs.setSelectedProgramId(this, appWidgetId, programId)
            val appWidgetManager = AppWidgetManager.getInstance(this)
            ProgramRoutinesWidgetProvider.updateWidget(this, appWidgetManager, appWidgetId)

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
        }
        finish()
    }
}

@Composable
fun ProgramWidgetConfigureScreen(
    appWidgetId: Int,
    semesterRepo: SemesterRepository,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val programs by semesterRepo.layerPrograms.collectAsStateWithLifecycle()
    val savedId = remember(appWidgetId) {
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            ProgramWidgetPrefs.getSelectedProgramId(context, appWidgetId)
        } else null
    }

    var selectedProgramId by remember(programs, savedId) {
        val initial = if (!savedId.isNullOrBlank() && programs.any { it.id == savedId }) {
            savedId
        } else {
            programs.firstOrNull { it.isEnabled }?.id ?: programs.firstOrNull()?.id
        }
        mutableStateOf(initial)
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    fun animateClose() {
        isVisible = false
    }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(220)
            onDismiss()
        }
    }

    // Predictive back gesture handling
    var backProgress by remember { mutableFloatStateOf(0f) }
    var backSwipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

    PredictiveBackHandler(enabled = true) { progressFlow: Flow<BackEventCompat> ->
        try {
            progressFlow.collect { backEvent ->
                backProgress = backEvent.progress
                backSwipeEdge = backEvent.swipeEdge
            }
            animateClose()
        } catch (_: CancellationException) {
            // Cancelled gesture -> springs back
        } finally {
            backProgress = 0f
        }
    }

    val predictiveScale = 1f - (backProgress * 0.08f)
    val predictiveTranslationX = if (backSwipeEdge == BackEventCompat.EDGE_LEFT) {
        backProgress * 28f
    } else {
        -backProgress * 28f
    }
    val predictiveAlpha = 1f - (backProgress * 0.18f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = (0.10f * (1f - backProgress)).coerceIn(0f, 0.10f)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .clickable(onClick = { animateClose() }),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .graphicsLayer {
                        scaleX = predictiveScale
                        scaleY = predictiveScale
                        translationX = predictiveTranslationX
                        alpha = predictiveAlpha
                    }
                    .clickable(enabled = false, onClick = {}),
                shape = RoundedCornerShape(28.dp),
                color = SurfaceContainerLowest,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    // Header Bar (Symmetrical Centering)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LocalAccentColor.current.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventRepeat,
                                contentDescription = null,
                                tint = LocalAccentColor.current,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.widget_select_program),
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = stringResource(R.string.widget_select_program_subtitle),
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            )
                        }

                        IconButton(
                            onClick = { animateClose() },
                            modifier = Modifier
                                .size(36.dp)
                                .expressiveBounce(pressScale = 0.90f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (programs.isEmpty()) {
                        // Empty State: No programs yet
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = SurfaceContainerLow,
                            border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventRepeat,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.widget_no_programs_create_one),
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.widget_open_program_manager),
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        val intent = Intent(context, MainActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        }
                                        context.startActivity(intent)
                                        animateClose()
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LocalAccentColor.current,
                                        contentColor = TextOnAccent
                                    ),
                                    modifier = Modifier.expressiveBounce(pressScale = 0.94f)
                                ) {
                                    Text(
                                        text = stringResource(R.string.widget_open_program_manager),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // List of Programs to choose from
                        val defaultAccent = LocalAccentColor.current
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(
                                programs,
                                key = { it.id },
                                contentType = { "program" }
                            ) { program ->
                                val isSelected = program.id == selectedProgramId
                                val progColor = remember(program.colorHex, defaultAccent) {
                                    if (!program.colorHex.isNullOrBlank()) {
                                        try {
                                            Color(AndroidColor.parseColor(program.colorHex))
                                        } catch (_: Exception) {
                                            defaultAccent
                                        }
                                    } else {
                                        defaultAccent
                                    }
                                }

                                ProgramSelectCard(
                                    program = program,
                                    isSelected = isSelected,
                                    programColor = progColor,
                                    onClick = {
                                        selectedProgramId = program.id
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Save Button
                        Button(
                            onClick = {
                                selectedProgramId?.let { id ->
                                    onSave(id)
                                }
                            },
                            enabled = selectedProgramId != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .expressiveBounce(pressScale = 0.95f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LocalAccentColor.current,
                                contentColor = TextOnAccent
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.widget_save_for_widget),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramSelectCard(
    program: LayerProgram,
    isSelected: Boolean,
    programColor: Color,
    onClick: () -> Unit
) {
    val currentAccent = LocalAccentColor.current
    val borderStroke = if (isSelected) {
        BorderStroke(2.dp, currentAccent)
    } else {
        BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.4f))
    }
    val cardBg = if (isSelected) {
        currentAccent.copy(alpha = 0.08f)
    } else {
        SurfaceContainerLow
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .expressiveBounce(pressScale = 0.97f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = borderStroke
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Program Color Swatch
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(programColor.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(programColor)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = program.layerName,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                val routineCountText = stringResource(
                    R.string.widget_routines_count_format,
                    program.scheduleItems.size
                )
                Text(
                    text = routineCountText,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Selection Radio / Check Indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(currentAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = TextOnAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Unselected",
                    tint = TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
