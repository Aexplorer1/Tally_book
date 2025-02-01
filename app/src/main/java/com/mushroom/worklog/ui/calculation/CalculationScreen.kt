package com.mushroom.worklog.ui.calculation

import android.content.Context
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mushroom.worklog.model.Worker
import com.mushroom.worklog.model.WorkRecord
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.*
import com.mushroom.worklog.viewmodel.CalculationViewModel
import com.mushroom.worklog.navigation.Screen
import androidx.navigation.NavController

@Composable
fun CalculationScreen(
    viewModel: CalculationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    navController: NavController
) {
    val workerTotals by viewModel.workerTotals.collectAsState()
    val selectedWorkerRecords by viewModel.selectedWorkerRecords.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    var showRecordsDialog by remember { mutableStateOf(false) }
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var showSettleDialog by remember { mutableStateOf<Pair<Worker, Double>?>(null) }
    
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("工资结算") },
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
            // 日期选择
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "选择时间范围",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 开始日期
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = dateFormatter.format(Date(startDate)),
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("开始日期") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 8.dp),
                                trailingIcon = {
                                    Icon(Icons.Default.DateRange, "选择开始日期")
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        val calendar = Calendar.getInstance().apply {
                                            timeInMillis = startDate
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
                                                viewModel.setDateRange(
                                                    calendar.timeInMillis,
                                                    endDate
                                                )
                                            },
                                            calendar.get(Calendar.YEAR),
                                            calendar.get(Calendar.MONTH),
                                            calendar.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                            )
                        }

                        // 结束日期
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = dateFormatter.format(Date(endDate)),
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("结束日期") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp),
                                trailingIcon = {
                                    Icon(Icons.Default.DateRange, "选择结束日期")
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        val calendar = Calendar.getInstance().apply {
                                            timeInMillis = endDate
                                            set(Calendar.HOUR_OF_DAY, 23)
                                            set(Calendar.MINUTE, 59)
                                            set(Calendar.SECOND, 59)
                                            set(Calendar.MILLISECOND, 999)
                                        }
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                calendar.set(Calendar.YEAR, year)
                                                calendar.set(Calendar.MONTH, month)
                                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                                viewModel.setDateRange(
                                                    startDate,
                                                    calendar.timeInMillis
                                                )
                                            },
                                            calendar.get(Calendar.YEAR),
                                            calendar.get(Calendar.MONTH),
                                            calendar.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                            )
                        }
                    }
                }
            }

            // 工资统计列表
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(workerTotals.entries.toList()) { (worker, total) ->
                    WorkerSalaryCard(
                        worker = worker,
                        total = total,
                        records = selectedWorkerRecords,
                        onSettle = {
                            viewModel.settleWorkerSalary(worker.id)
                        },
                        onClick = {
                            navController.navigate(
                                Screen.WorkerRecords.createRoute(
                                    workerId = worker.id,
                                    startDate = startDate,
                                    endDate = endDate
                                )
                            )
                        },
                        onSettleClick = { w, t -> showSettleDialog = w to t }
                    )
                }
            }
        }

        // 工作记录详情对话框
        if (showRecordsDialog && selectedWorker != null) {
            AlertDialog(
                onDismissRequest = { showRecordsDialog = false },
                title = { Text("${selectedWorker?.name}的工作记录") },
                text = {
                    LazyColumn {
                        items(selectedWorkerRecords) { record ->
                            RecordItem(record = record, dateFormatter = dateFormatter)
                            Divider()
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRecordsDialog = false }) {
                        Text("关闭")
                    }
                }
            )
        }

        // 添加结算确认对话框
        showSettleDialog?.let { (worker, total) ->
            AlertDialog(
                onDismissRequest = { showSettleDialog = null },
                title = { Text("确认结算工资") },
                text = {
                    Column {
                        Text("确定要结算 ${worker.name} 的工资吗？")
                        Text(
                            text = "结算金额：¥${String.format("%.2f", total)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.settleWorkerSalary(worker.id)
                            showSettleDialog = null
                        }
                    ) {
                        Text("确认结算")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSettleDialog = null }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
private fun WorkerSalaryCard(
    worker: Worker,
    total: Double,
    records: List<WorkRecord>,
    onSettle: () -> Unit,
    onClick: () -> Unit,
    onSettleClick: (Worker, Double) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
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
                
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "¥${String.format("%.2f", total)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (total > 0) {
                        Text(
                            text = "未结清",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(
                            onClick = { onSettleClick(worker, total) }
                        ) {
                            Text("结算工资")
                        }
                    } else {
                        Text(
                            text = "已结清",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordItem(
    record: WorkRecord,
    dateFormatter: SimpleDateFormat
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dateFormatter.format(Date(record.date)),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "¥${String.format("%.2f", record.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = "工作类型: ${record.workType}",
            style = MaterialTheme.typography.bodySmall
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