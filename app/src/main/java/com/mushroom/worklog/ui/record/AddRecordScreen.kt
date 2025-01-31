package com.mushroom.worklog.ui.record

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.text.KeyboardOptions
import com.mushroom.worklog.model.Worker
import com.mushroom.worklog.viewmodel.WorkRecordViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.content.DialogInterface

@Composable
fun AddRecordScreen(
    viewModel: WorkRecordViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val workers by viewModel.workers.collectAsState()
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var workType by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var pieces by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加工作记录") },
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
            // 选择工人
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedWorker?.name ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("选择工人") },
                    trailingIcon = {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp 
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = "选择工人"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    workers.forEach { worker ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(worker.name)
                                    if (worker.phoneNumber.isNotBlank()) {
                                        Text(
                                            worker.phoneNumber,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            },
                            onClick = { 
                                selectedWorker = worker
                                expanded = false
                            }
                        )
                    }
                }
            }

            // 选择日期
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dateFormatter.format(Date(selectedDate)),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("选择日期") },
                    trailingIcon = {
                        IconButton(onClick = {
                            showChineseDatePicker(context, selectedDate) { date ->
                                selectedDate = date
                            }
                        }) {
                            Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                // 添加一个透明的可点击层
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            onClick = {
                                showChineseDatePicker(context, selectedDate) { date ->
                                    selectedDate = date
                                }
                            }
                        )
                )
            }

            // 工作类型
            OutlinedTextField(
                value = workType,
                onValueChange = { workType = it },
                label = { Text("工作类型") },
                placeholder = { Text("如：采摘、装箱等") },
                modifier = Modifier.fillMaxWidth()
            )

            // 工时
            OutlinedTextField(
                value = hours,
                onValueChange = { hours = it },
                label = { Text("工时(小时)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // 计件数量
            OutlinedTextField(
                value = pieces,
                onValueChange = { pieces = it },
                label = { Text("计件数量(件)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // 金额
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("金额(元)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // 备注
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth()
            )

            // 保存按钮
            Button(
                onClick = {
                    selectedWorker?.let { worker ->
                        viewModel.addWorkRecord(
                            workerId = worker.id,
                            date = selectedDate,
                            workType = workType,
                            hours = hours.toDoubleOrNull() ?: 0.0,
                            pieces = pieces.toIntOrNull() ?: 0,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            notes = notes
                        )
                        onNavigateBack()
                    }
                },
                enabled = selectedWorker != null && workType.isNotBlank() && amount.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }
    }
}

private fun showChineseDatePicker(
    context: android.content.Context,
    initialDate: Long,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance(Locale.CHINESE).apply {
        timeInMillis = initialDate
    }

    val datePickerDialog = DatePickerDialog(
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
    }

    try {
        datePickerDialog.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Preview(showBackground = true)
@Composable
fun AddRecordScreenPreview() {
    MaterialTheme {
        AddRecordScreen(
            onNavigateBack = {}
        )
    }
} 