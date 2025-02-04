package com.mushroom.worklog.ui.record

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import com.mushroom.worklog.model.Worker
import com.mushroom.worklog.viewmodel.WorkRecordViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.content.DialogInterface
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.input.ImeAction

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
    var showError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("添加工作记录")
                        selectedWorker?.let {
                            Text(
                                it.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
            // 选择工人卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "选择工人",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedWorker?.name ?: "",
                            onValueChange = { },
                            readOnly = true,
                            placeholder = { Text("请选择工人") },
                            trailingIcon = {
                                Icon(
                                    if (expanded) Icons.Default.KeyboardArrowUp 
                                    else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "选择工人"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            workers.forEach { worker ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // 头像
                                            Surface(
                                                modifier = Modifier.size(24.dp),
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = worker.name.take(1),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                            
                                            Column {
                                                Text(worker.name)
                                                if (worker.phoneNumber.isNotBlank()) {
                                                    Text(
                                                        worker.phoneNumber,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
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
                }
            }

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
                        .padding(16.dp)
                ) {
                    Text(
                        "选择日期",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = dateFormatter.format(Date(selectedDate)),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("选择日期") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    showDatePicker(context, selectedDate) { date ->
                                        selectedDate = date
                                    }
                                }
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // 工作详情卡片
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
                        "工作详情",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = workType,
                        onValueChange = { workType = it },
                        label = { Text("工作类型") },
                        placeholder = { Text("如：采摘、装箱等") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = hours,
                            onValueChange = { hours = it },
                            label = { Text("工时") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            suffix = { Text("小时") },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = pieces,
                            onValueChange = { pieces = it },
                            label = { Text("计件") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            suffix = { Text("件") },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                amount = it
                            }
                        },
                        label = { Text("金额") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        prefix = { Text("¥") },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        isError = amount.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) <= 0
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("备注") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 保存按钮
            Button(
                onClick = {
                    val validationResult = viewModel.validateRecord(
                        workerId = selectedWorker?.id,
                        workType = workType,
                        hours = hours,
                        pieces = pieces,
                        amount = amount
                    )

                    when (validationResult) {
                        is WorkRecordViewModel.ValidationResult.Success -> {
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
                        }
                        is WorkRecordViewModel.ValidationResult.Error -> {
                            showError = validationResult.message
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("保存记录")
            }

            // 错误提示
            if (showError != null) {
                AlertDialog(
                    onDismissRequest = { showError = null },
                    title = { Text("提示") },
                    text = { Text(showError!!) },
                    confirmButton = {
                        TextButton(onClick = { showError = null }) {
                            Text("确定")
                        }
                    }
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