package com.mushroom.worklog.ui.calculation

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

@Composable
fun CalculationScreen(
    viewModel: CalculationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis()) }
    val records by viewModel.records.collectAsState()
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    LaunchedEffect(startDate, endDate) {
        viewModel.setDateRange(startDate, endDate)
    }

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

            // 工资列表
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records.entries.toList()) { (worker, workerRecords) ->
                    WorkerSalaryCard(
                        worker = worker,
                        records = workerRecords,
                        totalAmount = viewModel.calculateTotal(worker, workerRecords)
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerSalaryCard(
    worker: Worker,
    records: List<WorkRecord>,
    totalAmount: Double
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = worker.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "工作记录: ${records.size}条",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "总金额: ¥${String.format("%.2f", totalAmount)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp 
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "收起" else "展开"
                    )
                }
            }

            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                records.sortedByDescending { it.date }.forEach { record ->
                    RecordItem(record = record, dateFormatter = dateFormatter)
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
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