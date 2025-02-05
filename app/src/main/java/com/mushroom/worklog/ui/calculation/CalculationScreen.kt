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
import androidx.compose.foundation.shape.CircleShape
import android.content.DialogInterface
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.mushroom.worklog.utils.SoundHelper

@Composable
fun CalculationScreen(
    viewModel: CalculationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    navController: NavController
) {
    val workerTotals by viewModel.workerTotals.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    var showSettleDialog by remember { mutableStateOf<Pair<Worker, Double>?>(null) }
    
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE) }
    val soundHelper = remember { SoundHelper(context) }
    
    // 在组件销毁时释放音效资源
    DisposableEffect(Unit) {
        onDispose {
            soundHelper.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("工资结算")
                        Text(
                            "共 ${workerTotals.size} 位工人待结算",
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
            // 日期选择卡片
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
                    Text(
                        text = "选择结算时间范围",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
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
                                            viewModel.setDateRange(date, endDate)
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
                                            viewModel.setDateRange(startDate, date)
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

            if (workerTotals.isEmpty()) {
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
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "暂无待结算工资",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "所选时间范围内没有未结算的工资记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 工资统计列表
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(workerTotals.entries.toList()) { (worker, total) ->
                        WorkerSalaryCard(
                            worker = worker,
                            total = total,
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

                // 总计金额
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "总计金额",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "¥${String.format("%.2f", workerTotals.values.sum())}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // 结算确认对话框
        showSettleDialog?.let { (worker, total) ->
            AlertDialog(
                onDismissRequest = { showSettleDialog = null },
                title = { Text("确认结算工资") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("确定要结算 ${worker.name} 的工资吗？")
                        Text(
                            text = "¥${String.format("%.2f", total)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "结算后，这些工作记录将被标记为已结算",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.settleWorkerSalary(worker.id)
                            // 播放结算音效
                            soundHelper.playSettleSound()
                            showSettleDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
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
    onClick: () -> Unit,
    onSettleClick: (Worker, Double) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 头像
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = worker.name.take(1),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            text = worker.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (worker.phoneNumber.isNotBlank()) {
                            Text(
                                text = worker.phoneNumber,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "点击查看工作记录",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "¥${String.format("%.2f", total)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Button(
                        onClick = { onSettleClick(worker, total) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("结算工资")
                    }
                }
            }
        }
    }
}

private fun showDatePicker(
    context: Context,
    initialDate: Long,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = initialDate
    }
    
    DatePickerDialog(
        context,
        android.R.style.Theme_DeviceDefault_Light_Dialog,  // 使用系统亮色主题
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
        // 设置中文按钮文字
        setButton(DialogInterface.BUTTON_POSITIVE, "确定", this)
        setButton(DialogInterface.BUTTON_NEGATIVE, "取消", this)
    }.show()
} 