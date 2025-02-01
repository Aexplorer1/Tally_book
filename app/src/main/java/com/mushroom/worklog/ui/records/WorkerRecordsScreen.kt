package com.mushroom.worklog.ui.records

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mushroom.worklog.model.WorkRecord
import java.text.SimpleDateFormat
import java.util.*
import com.mushroom.worklog.viewmodel.WorkerRecordsViewModel

@Composable
fun WorkerRecordsScreen(
    workerId: Long,
    startDate: Long,
    endDate: Long,
    viewModel: WorkerRecordsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val records by viewModel.records.collectAsState()
    val worker by viewModel.worker.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE) }

    LaunchedEffect(workerId, startDate, endDate) {
        viewModel.loadWorkerAndRecords(workerId, startDate, endDate)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(worker?.name ?: "工作记录")
                        Text(
                            text = "未结算记录",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(records.sortedByDescending { it.date }) { record ->
                RecordCard(record = record, dateFormatter = dateFormatter)
            }
        }
    }
}

@Composable
private fun RecordCard(
    record: WorkRecord,
    dateFormatter: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateFormatter.format(Date(record.date)),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "¥${String.format("%.2f", record.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = "工作类型：${record.workType}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            if (record.hours > 0) {
                Text(
                    text = "工时：${record.hours}小时",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (record.pieces > 0) {
                Text(
                    text = "计件：${record.pieces}件",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (record.notes.isNotBlank()) {
                Text(
                    text = "备注：${record.notes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
} 