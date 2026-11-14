package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.DecoCurriculum
import com.example.ui.screens.DocsScreen
import com.example.ui.screens.LabHubScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.Unit1Screen
import com.example.ui.screens.Unit2Screen
import com.example.ui.screens.Unit3Screen
import com.example.ui.screens.Unit4Screen
import com.example.ui.screens.Unit5Screen
import com.example.ui.theme.DecoLabTheme
import com.example.ui.theme.MonoValueStyle
import kotlinx.coroutines.launch

private enum class DecoDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HUB("hub", "Lab Hub", Icons.Filled.Home, Icons.Outlined.Home),
    LABS("labs", "Simulators", Icons.Filled.Memory, Icons.Outlined.Memory),
    QUIZ("quiz", "Quiz", Icons.Filled.Quiz, Icons.Outlined.Quiz),
    DOCS("docs", "Report", Icons.Filled.Article, Icons.Outlined.Article)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val progress by viewModel.progressState.collectAsStateWithLifecycle()
            val selectedUnitId by viewModel.selectedUnitId.collectAsStateWithLifecycle()
            val selectedQuizUnitId by viewModel.selectedQuizUnitId.collectAsStateWithLifecycle()

            DecoLabTheme(darkTheme = progress.isDarkTheme) {
                DecoLabAppShell(
                    isDarkTheme = progress.isDarkTheme,
                    totalScore = progress.totalBestScore,
                    maxScore = progress.maxTotalScore,
                    selectedUnitId = selectedUnitId,
                    selectedQuizUnitId = selectedQuizUnitId,
                    progress = progress,
                    onToggleTheme = { viewModel.toggleDarkTheme() },
                    onSelectUnit = { viewModel.selectUnit(it) },
                    onSelectQuizUnit = { viewModel.selectQuizUnit(it) },
                    onQuizCompleted = { unitId, score -> viewModel.recordQuizResult(unitId, score) },
                    onResetScores = { viewModel.resetQuizScores() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DecoLabAppShell(
    isDarkTheme: Boolean,
    totalScore: Int,
    maxScore: Int,
    selectedUnitId: Int,
    selectedQuizUnitId: Int,
    progress: com.example.data.UserProgressState,
    onToggleTheme: () -> Unit,
    onSelectUnit: (Int) -> Unit,
    onSelectQuizUnit: (Int) -> Unit,
    onQuizCompleted: (Int, Int) -> Unit,
    onResetScores: () -> Unit
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: DecoDestination.HUB.route
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 680.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    title = {
                        Text(
                            text = "DECO Lab",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        Surface(
                            modifier = Modifier.padding(start = 12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "$totalScore/$maxScore",
                                style = MonoValueStyle,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .testTag("theme_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                contentDescription = if (isDarkTheme) "Switch to light mode" else "Switch to dark mode"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        DecoDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label, maxLines = 1) },
                                modifier = Modifier.testTag("nav_${dest.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        DecoDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) }
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = DecoDestination.HUB.route,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    composable(DecoDestination.HUB.route) {
                        LabHubScreen(
                            progress = progress,
                            onOpenUnitLab = { unitId ->
                                onSelectUnit(unitId)
                                navigateTo(DecoDestination.LABS.route)
                            },
                            onOpenUnitQuiz = { unitId ->
                                onSelectQuizUnit(unitId)
                                navigateTo(DecoDestination.QUIZ.route)
                            },
                            onOpenDocs = { navigateTo(DecoDestination.DOCS.route) }
                        )
                    }

                    composable(DecoDestination.LABS.route) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Unit 1..5 Selector Strip
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DecoCurriculum.units.forEach { unit ->
                                        FilterChip(
                                            selected = selectedUnitId == unit.id,
                                            onClick = { onSelectUnit(unit.id) },
                                            label = { Text("U${unit.id}: ${unit.shortTitle}") },
                                            modifier = Modifier.testTag("sim_unit_chip_${unit.id}")
                                        )
                                    }
                                }
                            }
                            when (selectedUnitId) {
                                1 -> Unit1Screen(modifier = Modifier.weight(1f))
                                2 -> Unit2Screen(modifier = Modifier.weight(1f))
                                3 -> Unit3Screen(modifier = Modifier.weight(1f))
                                4 -> Unit4Screen(modifier = Modifier.weight(1f))
                                else -> Unit5Screen(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    composable(DecoDestination.QUIZ.route) {
                        QuizScreen(
                            selectedUnitId = selectedQuizUnitId,
                            progress = progress,
                            onSelectUnit = onSelectQuizUnit,
                            onQuizCompleted = onQuizCompleted,
                            onResetScores = onResetScores
                        )
                    }

                    composable(DecoDestination.DOCS.route) {
                        DocsScreen(
                            onShowMessage = { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        )
                    }
                }
            }
        }
    }
}
