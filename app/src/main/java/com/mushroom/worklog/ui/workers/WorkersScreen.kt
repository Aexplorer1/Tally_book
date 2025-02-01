package com.mushroom.worklog.ui.workers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.mushroom.worklog.model.Worker
import com.mushroom.worklog.viewmodel.WorkerViewModel

@Composable
fun WorkerCard(
    worker: Worker,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
            IconButton(onClick = onEditClick) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "编辑"
                )
            }
        }
    }
}

@Composable
fun WorkersScreen(
    viewModel: WorkerViewModel = hiltViewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingWorker by remember { mutableStateOf<Worker?>(null) }
    val workers by viewModel.workers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("增加工人") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加工人")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(workers) { worker ->
                WorkerCard(
                    worker = worker,
                    onEditClick = { editingWorker = worker }
                )
            }
        }

        if (showAddDialog || editingWorker != null) {
            WorkerDialog(
                worker = editingWorker,
                onDismiss = {
                    showAddDialog = false
                    editingWorker = null
                },
                onConfirm = { name, phone ->
                    if (editingWorker != null) {
                        viewModel.updateWorker(editingWorker!!.copy(
                            name = name,
                            phoneNumber = phone
                        ))
                    } else {
                        viewModel.addWorker(name, phone)
                    }
                    showAddDialog = false
                    editingWorker = null
                }
            )
        }
    }
}

@Composable
private fun WorkerDialog(
    worker: Worker?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf(worker?.name ?: "") }
    var phone by remember { mutableStateOf(worker?.phoneNumber ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (worker == null) "添加工人" else "编辑工人") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("电话") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, phone) },
                enabled = name.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
} 