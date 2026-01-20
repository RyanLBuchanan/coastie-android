package ai.adaskids.coastie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ai.adaskids.coastie.data.CoastieApi
import ai.adaskids.coastie.ui.chat.ChatScreen
import ai.adaskids.coastie.ui.chat.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ai.adaskids.coastie.ui.theme.CoastieTheme { CoastieApp() }

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoastieApp() {
    val navController = rememberNavController()
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
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
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
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Scenarios.route) {
                ai.adaskids.coastie.ui.scenarios.ScenariosScreen(
                    onPickScenario = { scenario ->
                        // v1: just jump to Chat tab; next step we prefill the prompt
                        navController.navigate(Screen.Chat.route)
                    }
                )
            }


            composable(Screen.Chat.route) {
                // TODO: Replace with your real Netlify URL
                val api = remember {
                    CoastieApi("https://adaskids-preview.netlify.app/.netlify/functions/coastie-chat")
                }
                val vm = remember { ChatViewModel(api) }
                ChatScreen(vm)
            }

            composable(Screen.Exports.route) {
                Placeholder("Exports (next)")
            }

            composable(Screen.History.route) {
                Placeholder("History (next)")
            }
        }
    }
}

@Composable
private fun Placeholder(label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(label)
    }
}
