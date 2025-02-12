package com.mushroom.worklog.ui.workers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.animateContentSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.mushroom.worklog.model.Worker
import com.mushroom.worklog.viewmodel.WorkerViewModel
import androidx.compose.ui.text.input.ImeAction
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.accompanist.permissions.rememberPermissionState
import com.mushroom.worklog.utils.ContactsHelper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear

@Composable
fun WorkerCard(
    worker: Worker,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                
                // 信息
                Column {
                    Text(
                        text = worker.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (worker.phoneNumber.isNotBlank()) {
                        Text(
                            text = worker.phoneNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 编辑按钮
            IconButton(
                onClick = onEditClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "编辑",
                    modifier = Modifier.size(20.dp)
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
    var showContactsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val contactsHelper = remember { ContactsHelper(context) }
    
    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showContactsDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("增加工人")
                        Text(
                            "已添加 ${workers.size} 位工人",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 导入通讯录按钮
                FloatingActionButton(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.ContactPhone, contentDescription = "从通讯录导入")
                }
                
                // 原有的添加按钮
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加工人")
                }
            }
        }
    ) { padding ->
        if (workers.isEmpty()) {
            // 空状态展示
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "还没有添加工人",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "点击右下角按钮添加工人",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(workers) { worker ->
                    WorkerCard(
                        worker = worker,
                        onEditClick = { editingWorker = worker }
                    )
                }
            }
        }

        if (showAddDialog || editingWorker != null) {
            AddWorkerDialog(
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

        // 通讯录联系人选择对话框
        if (showContactsDialog) {
            val contacts = remember { contactsHelper.getContacts() }
            var searchQuery by remember { mutableStateOf("") }
            val filteredContacts = remember(searchQuery, contacts) {
                if (searchQuery.isBlank()) {
                    contacts
                } else {
                    contacts.filter { contact ->
                        contact.name.contains(searchQuery, ignoreCase = true) ||
                        contact.phoneNumber.contains(searchQuery)
                    }
                }
            }
            
            AlertDialog(
                onDismissRequest = { showContactsDialog = false },
                title = { Text("选择联系人") },
                text = {
                    Column {
                        // 搜索框
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("搜索联系人") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "搜索"
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "清除"
                                        )
                                    }
                                }
                            }
                        )

                        // 联系人列表
                        if (filteredContacts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isBlank()) "没有联系人" else "未找到匹配的联系人",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredContacts) { contact ->
                                    ListItem(
                                        headlineContent = { Text(contact.name) },
                                        supportingContent = { Text(contact.phoneNumber) },
                                        leadingContent = {
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
                                                        text = contact.name.take(1),
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .clickable {
                                                viewModel.addWorker(contact.name, contact.phoneNumber)
                                                showContactsDialog = false
                                            }
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showContactsDialog = false }) {
                        Text("关闭")
                    }
                }
            )
        }
    }
}

@Composable
private fun AddWorkerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf<String?>(null) }
    val viewModel: WorkerViewModel = hiltViewModel()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加工人") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    isError = name.isBlank(),
                    supportingText = if (name.isBlank()) {
                        { Text("请输入工人姓名") }
                    } else null
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { 
                        // 只允许输入数字，且最多11位
                        if (it.length <= 11 && it.all { char -> char.isDigit() }) {
                            phoneNumber = it
                        }
                    },
                    label = { Text("电话（选填）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    isError = phoneNumber.isNotBlank() && !phoneNumber.matches(Regex("^1[3-9]\\d{9}$")),
                    supportingText = if (phoneNumber.isNotBlank() && !phoneNumber.matches(Regex("^1[3-9]\\d{9}$"))) {
                        { Text("请输入正确的11位手机号码") }
                    } else null
                )

                if (showError != null) {
                    Text(
                        text = showError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val validationResult = viewModel.validateWorker(name, phoneNumber)
                    when (validationResult) {
                        is WorkerViewModel.ValidationResult.Success -> {
                            onConfirm(name, phoneNumber)
                            onDismiss()
                        }
                        is WorkerViewModel.ValidationResult.Error -> {
                            showError = validationResult.message
                        }
                    }
                },
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