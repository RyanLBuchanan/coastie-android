package ai.adaskids.coastie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ai.adaskids.coastie.data.CoastieApi
import ai.adaskids.coastie.data.local.HistoryRepository
import ai.adaskids.coastie.data.local.db.CoastieDatabase
import ai.adaskids.coastie.ui.AppState
import ai.adaskids.coastie.ui.chat.ChatScreen
import ai.adaskids.coastie.ui.chat.ChatViewModel
import ai.adaskids.coastie.ui.exports.ExportsScreen
import ai.adaskids.coastie.ui.history.HistoryScreen
import ai.adaskids.coastie.ui.history.HistoryViewModel
import ai.adaskids.coastie.ui.scenarios.PromptEditorScreen
import ai.adaskids.coastie.ui.theme.CoastieTheme
import androidx.compose.foundation.layout.padding

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoastieTheme { CoastieApp() }
        }
    }
}

sealed class Screen(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
) {
    data object Scenarios : Screen("scenarios", "Scenarios", {
        Icon(Icons.Filled.ListAlt, contentDescription = "Scenarios")
    })

    data object Chat : Screen("chat", "Chat", {
        Icon(Icons.Filled.ChatBubbleOutline, contentDescription = "Chat")
    })

    data object Exports : Screen("exports", "Exports", {
        Icon(Icons.Filled.FolderOpen, contentDescription = "Exports")
    })

    data object History : Screen("history", "History", {
        Icon(Icons.Filled.History, contentDescription = "History")
    })

    data object PromptEditor : Screen("prompt_editor", "Prompt", { })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoastieApp() {
    val navController = rememberNavController()
    val appState = remember { AppState() }
    val ctx = LocalContext.current

    // Room + repo (singletons for demo)
    val db = remember { CoastieDatabase.get(ctx) }
    val historyRepo = remember { HistoryRepository(db.historyDao()) }

    val tabs = listOf(Screen.Scenarios, Screen.Chat, Screen.Exports, Screen.History)

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Screen.Scenarios.route

    Scaffold(
        topBar = { TopAppBar(title = { Text("Coastie") }) },
        bottomBar = {
            NavigationBar {
                tabs.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { screen.icon() },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Scenarios.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(Screen.Scenarios.route) {
                ai.adaskids.coastie.ui.scenarios.ScenariosScreen(
                    appState = appState,
                    onGoEdit = { navController.navigate(Screen.PromptEditor.route) }
                )
            }

            composable(Screen.PromptEditor.route) {
                PromptEditorScreen(
                    appState = appState,
                    onRun = {
                        navController.navigate(Screen.Chat.route) {
                            popUpTo(Screen.PromptEditor.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Chat.route) {
                val api = remember {
                    CoastieApi("https://adaskids-preview.netlify.app/.netlify/functions/coastie-chat")
                }
                val vm = remember { ChatViewModel(api, historyRepo) }

                // Prefill chat input from pending scenario prompt (if any)
                val pending by appState.pending.collectAsState()
                LaunchedEffect(pending) {
                    pending?.let {
                        vm.setInput(it.prompt)
                        vm.setScenarioTitle(it.title) // captures scenario title into History entries
                    }
                }

                ChatScreen(vm)
            }

            composable(Screen.Exports.route) {
                ExportsScreen()
            }

            composable(Screen.History.route) {
                val vm = remember { HistoryViewModel(historyRepo) }
                HistoryScreen(vm)
            }
        }
    }
}

@Composable
private fun Placeholder(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(label)
    }
}
