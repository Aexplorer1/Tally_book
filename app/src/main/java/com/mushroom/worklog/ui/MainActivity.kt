package com.mushroom.worklog.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mushroom.worklog.navigation.Screen
import com.mushroom.worklog.ui.record.AddRecordScreen
import com.mushroom.worklog.ui.calculation.CalculationScreen
import com.mushroom.worklog.ui.history.HistoryScreen
import com.mushroom.worklog.ui.workers.WorkersScreen
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
                        onNavigateToHistory = { navController.navigate(Screen.History.route) }
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
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
                composable(Screen.History.route) {
                    HistoryScreen(
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
    onNavigateToHistory: () -> Unit
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
            Text("工人管理")
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
    }
} 