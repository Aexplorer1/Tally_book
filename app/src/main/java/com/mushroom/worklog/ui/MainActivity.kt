package com.mushroom.worklog.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.mushroom.worklog.ui.statistics.StatisticsScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) }
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
                composable(Screen.Statistics.route) {
                    StatisticsScreen(
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
    onNavigateToSettings: () -> Unit,
    onNavigateToStatistics: () -> Unit
) {
    var showDeleteWarning by remember { mutableStateOf(false) }
    var countDown by remember { mutableStateOf(5) }
    var isCountingDown by remember { mutableStateOf(false) }

    // 倒计时效果
    LaunchedEffect(isCountingDown) {
        if (isCountingDown && countDown > 0) {
            while (countDown > 0) {
                delay(1000)
                countDown--
            }
            isCountingDown = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题区域
        Text(
            text = "工作记录",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // 主要功能区
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 增加工人
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClick = onNavigateToWorkers),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "增加工人",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // 添加记录
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClick = onNavigateToAddRecord),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        Icons.Default.AddTask,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "添加工作记录",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // 工资结算区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable(onClick = onNavigateToCalculation),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Icon(
                        Icons.Default.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "工资结算",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        // 历史记录区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable(onClick = onNavigateToHistory),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "历史记录",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 记账统计区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable(onClick = onNavigateToStatistics),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "记账统计",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 删除工人（放在底部）
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable { showDeleteWarning = true },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "删除工人",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // 危险操作警告对话框
    if (showDeleteWarning) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteWarning = false
                countDown = 5
                isCountingDown = false
            },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("危险操作警告") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "您即将进入删除工人界面",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "删除工人将永久删除其所有工作记录！",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    if (countDown > 0) {
                        Text(
                            text = "请等待 $countDown 秒...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LinearProgressIndicator(
                            progress = countDown / 5f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onNavigateToSettings()
                        showDeleteWarning = false
                        countDown = 5
                        isCountingDown = false
                    },
                    enabled = countDown == 0,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("继续")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteWarning = false
                        countDown = 5
                        isCountingDown = false
                    }
                ) {
                    Text("取消")
                }
            }
        )

        // 开始倒计时
        LaunchedEffect(showDeleteWarning) {
            isCountingDown = true
        }
    }
} 