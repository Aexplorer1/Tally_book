package com.mushroom.worklog.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mushroom.worklog.navigation.Screen
import com.mushroom.worklog.ui.record.AddRecordScreen
import com.mushroom.worklog.ui.calculation.CalculationScreen
import com.mushroom.worklog.ui.history.HistoryScreen
import com.mushroom.worklog.ui.workers.WorkersScreen
import com.mushroom.worklog.ui.records.WorkerRecordsScreen
import com.mushroom.worklog.ui.settings.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkLogApp()
        }
    }
}

@Composable
fun WorkLogApp() {
    val navController = rememberNavController()

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(navController = navController, startDestination = Screen.Home.route) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToWorkers = { navController.navigate(Screen.Workers.route) },
                        onNavigateToAddRecord = { navController.navigate(Screen.AddRecord.route) },
                        onNavigateToCalculation = { navController.navigate(Screen.Calculation.route) },
                        onNavigateToHistory = { navController.navigate(Screen.History.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                    )
                }
                composable(Screen.Workers.route) {
                    WorkersScreen()
                }
                composable(Screen.AddRecord.route) {
                    AddRecordScreen(
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
                composable(Screen.Calculation.route) {
                    CalculationScreen(
                        onNavigateBack = { navController.navigateUp() },
                        navController = navController
                    )
                }
                composable(Screen.History.route) {
                    HistoryScreen(
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
                composable(
                    route = Screen.WorkerRecords.route,
                    arguments = listOf(
                        navArgument("workerId") { type = NavType.LongType },
                        navArgument("startDate") { type = NavType.LongType },
                        navArgument("endDate") { type = NavType.LongType }
                    )
                ) { backStackEntry ->
                    WorkerRecordsScreen(
                        workerId = backStackEntry.arguments?.getLong("workerId") ?: 0L,
                        startDate = backStackEntry.arguments?.getLong("startDate") ?: 0L,
                        endDate = backStackEntry.arguments?.getLong("endDate") ?: 0L,
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToWorkers: () -> Unit,
    onNavigateToAddRecord: () -> Unit,
    onNavigateToCalculation: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToWorkers
        ) {
            Text("增加工人")
        }
        
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToAddRecord
        ) {
            Text("添加工作记录")
        }
        
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToCalculation
        ) {
            Text("工资结算")
        }
        
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToHistory
        ) {
            Text("历史记录")
        }
        
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToSettings,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("删除工人")
        }
    }
} 