package com.mushroom.worklog.ui.history

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.mushroom.worklog.model.Worker
import com.mushroom.worklog.model.WorkRecord
import com.mushroom.worklog.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var showWorkerDialog by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis()) }
    val records by viewModel.records.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    LaunchedEffect(selectedWorker, startDate, endDate) {
        viewModel.loadRecords(selectedWorker?.id, startDate, endDate)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史记录") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 筛选条件
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showWorkerDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(selectedWorker?.name ?: "选择工人")
                }
                if (selectedWorker != null) {
                    IconButton(onClick = { selectedWorker = null }) {
                        Icon(Icons.Default.Clear, contentDescription = "清除选择")
                    }
                }
            }

            // 日期选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = dateFormatter.format(Date(startDate)),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("开始日期") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .clickable {
                            showDatePicker(context, startDate) { date ->
                                startDate = date
                            }
                        }
                )
                OutlinedTextField(
                    value = dateFormatter.format(Date(endDate)),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("结束日期") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .clickable {
                            showDatePicker(context, endDate) { date ->
                                endDate = date
                            }
                        }
                )
            }

            // 记录列表
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records) { record ->
                    RecordCard(
                        record = record,
                        worker = workers.find { it.id == record.workerId },
                        dateFormatter = dateFormatter
                    )
                }
            }
        }

        if (showWorkerDialog) {
            AlertDialog(
                onDismissRequest = { showWorkerDialog = false },
                title = { Text("选择工人") },
                text = {
                    LazyColumn {
                        items(workers) { worker ->
                            TextButton(
                                onClick = {
                                    selectedWorker = worker
                                    showWorkerDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(worker.name)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showWorkerDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
private fun RecordCard(
    record: WorkRecord,
    worker: Worker?,
    dateFormatter: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = worker?.name ?: "未知工人",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "¥${String.format("%.2f", record.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = dateFormatter.format(Date(record.date)),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "工作类型: ${record.workType}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (record.hours > 0) {
                Text(
                    text = "工时: ${record.hours}小时",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (record.pieces > 0) {
                Text(
                    text = "计件: ${record.pieces}件",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (record.notes.isNotBlank()) {
                Text(
                    text = "备注: ${record.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

private fun showDatePicker(
    context: android.content.Context,
    initialDate: Long,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = initialDate
    }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            onDateSelected(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
} 