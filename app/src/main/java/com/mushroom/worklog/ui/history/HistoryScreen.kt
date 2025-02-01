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
import androidx.compose.foundation.shape.CircleShape
import android.content.DialogInterface

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var showWorkerDialog by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(
        Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
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
                title = { 
                    Column {
                        Text("历史记录")
                        Text(
                            text = selectedWorker?.let { "查看 ${it.name} 的记录" } ?: "查看所有记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 筛选条件卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "筛选条件",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        TextButton(
                            onClick = {
                                selectedWorker = null
                                startDate = Calendar.getInstance().apply {
                                    // 设置一个很早的时间，比如2020年1月1日
                                    set(2020, Calendar.JANUARY, 1, 0, 0, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                                endDate = Calendar.getInstance().apply {
                                    // 设置一个未来的时间，比如2030年12月31日
                                    set(2030, Calendar.DECEMBER, 31, 23, 59, 59)
                                    set(Calendar.MILLISECOND, 999)
                                }.timeInMillis
                            }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "查看所有记录",
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("查看所有记录")
                            }
                        }
                    }

                    // 工人选择
                    OutlinedTextField(
                        value = selectedWorker?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("选择工人") },
                        placeholder = { Text("全部工人") },
                        trailingIcon = {
                            if (selectedWorker != null) {
                                IconButton(onClick = { selectedWorker = null }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "清除选择",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = { showWorkerDialog = true },
                                    modifier = Modifier.size(48.dp)  // 增大按钮点击区域
                                ) {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "选择工人",
                                        modifier = Modifier.size(32.dp)  // 增大图标尺寸
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    // 日期范围选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dateFormatter.format(Date(startDate)),
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("开始日期") },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        showDatePicker(context, startDate) { date ->
                                            startDate = date
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = "选择开始日期")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = dateFormatter.format(Date(endDate)),
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("结束日期") },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        showDatePicker(context, endDate) { date ->
                                            endDate = date
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = "选择结束日期")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // 统计卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "记录统计",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    // 总记录统计
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "总记录数：${records.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "总金额：¥${String.format("%.2f", records.sumOf { it.amount })}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))

                    // 已结算记录统计
                    val settledRecords = records.filter { it.isSettled }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "已结算：${settledRecords.size}条",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "¥${String.format("%.2f", settledRecords.sumOf { it.amount })}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 未结算记录统计
                    val unsettledRecords = records.filter { !it.isSettled }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "未结算：${unsettledRecords.size}条",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "¥${String.format("%.2f", unsettledRecords.sumOf { it.amount })}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (records.isEmpty()) {
                // 空状态展示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "暂无工作记录",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "所选条件下没有找到工作记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 记录列表
                LazyColumn(
                    modifier = Modifier.weight(1f),
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
        }

        // 工人选择对话框
        if (showWorkerDialog) {
            AlertDialog(
                onDismissRequest = { showWorkerDialog = false },
                title = { Text("选择工人") },
                text = {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 添加"全部工人"选项
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedWorker = null
                                        showWorkerDialog = false
                                    },
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "全部工人",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }

                        items(workers) { worker ->
                            WorkerSelectItem(
                                worker = worker,
                                onClick = {
                                    selectedWorker = worker
                                    showWorkerDialog = false
                                }
                            )
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
private fun WorkerSelectItem(
    worker: Worker,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = worker.name.take(1),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Column {
                Text(
                    text = worker.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (worker.phoneNumber.isNotBlank()) {
                    Text(
                        text = worker.phoneNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = dateFormatter.format(Date(record.date)),
                        style = MaterialTheme.typography.titleMedium
                    )
                    worker?.let {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "¥${String.format("%.2f", record.amount)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (record.isSettled) "已结算" else "未结算",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (record.isSettled) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // 工作详情
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "工作类型：${record.workType}",
                    style = MaterialTheme.typography.bodyMedium
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
        android.R.style.Theme_DeviceDefault_Light_Dialog,
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
    ).apply {
        setButton(DialogInterface.BUTTON_POSITIVE, "确定", this)
        setButton(DialogInterface.BUTTON_NEGATIVE, "取消", this)
    }.show()
} 