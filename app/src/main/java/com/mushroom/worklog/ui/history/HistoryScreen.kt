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
    var startDate by remember { mutableStateOf(
        Calendar.getInstance().apply {
            add(Calendar.MONTH, -1) // 默认显示最近一个月的记录
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
    )}
    var endDate by remember { mutableStateOf(System.currentTimeMillis()) }
    val records by viewModel.records.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE) }

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
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 工人选择
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

                    // 日期范围选择
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
                                .padding(end = 8.dp),
                            trailingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = "选择开始日期")
                            }
                        )
                        OutlinedTextField(
                            value = dateFormatter.format(Date(endDate)),
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("结束日期") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            trailingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = "选择结束日期")
                            }
                        )
                    }
                }
            }

            // 记录统计
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "记录统计",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "总记录数: ${records.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "总金额: ¥${String.format("%.2f", records.sumOf { it.amount })}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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

        // 工人选择对话框
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
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            onDateSelected(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
} 