package com.mushroom.worklog.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mushroom.worklog.model.Worker
import com.mushroom.worklog.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<Worker?>(null) }
    val workers by viewModel.workers.collectAsState()
    var countDown by remember { mutableStateOf(3) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 删除工人部分
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "工人管理",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(workers) { worker ->
                            WorkerDeleteItem(
                                worker = worker,
                                onDeleteClick = { showDeleteDialog = worker }
                            )
                        }
                    }
                }
            }
        }

        // 删除确认对话框
        showDeleteDialog?.let { worker ->
            AlertDialog(
                onDismissRequest = { 
                    showDeleteDialog = null
                    countDown = 3
                    isCountingDown = false
                },
                icon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("危险操作") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "确定要删除工人 ${worker.name} 吗？",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "删除后，该工人的所有工作记录也将被删除！",
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
                                progress = countDown / 3f,
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
                            viewModel.deleteWorker(worker)
                            showDeleteDialog = null
                            countDown = 3
                            isCountingDown = false
                        },
                        enabled = countDown == 0,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("删除")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showDeleteDialog = null
                            countDown = 3
                            isCountingDown = false
                        }
                    ) {
                        Text("取消")
                    }
                }
            )

            // 开始倒计时
            LaunchedEffect(showDeleteDialog) {
                isCountingDown = true
            }
        }
    }
}

@Composable
private fun WorkerDeleteItem(
    worker: Worker,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = worker.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (worker.phoneNumber.isNotBlank()) {
                    Text(
                        text = worker.phoneNumber,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            IconButton(
                onClick = onDeleteClick
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 