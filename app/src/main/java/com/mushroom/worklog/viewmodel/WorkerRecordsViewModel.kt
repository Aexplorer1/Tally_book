package com.mushroom.worklog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushroom.worklog.database.WorkRecordDao
import com.mushroom.worklog.database.WorkerDao
import com.mushroom.worklog.model.Worker
import com.mushroom.worklog.model.WorkRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkerRecordsViewModel @Inject constructor(
    private val workRecordDao: WorkRecordDao,
    private val workerDao: WorkerDao
) : ViewModel() {
    private val _records = MutableStateFlow<List<WorkRecord>>(emptyList())
    val records: StateFlow<List<WorkRecord>> = _records

    private val _worker = MutableStateFlow<Worker?>(null)
    val worker: StateFlow<Worker?> = _worker

    fun loadWorkerAndRecords(workerId: Long, startDate: Long, endDate: Long) {
        viewModelScope.launch {
            workRecordDao.getUnsettledWorkerRecordsByDateRange(workerId, startDate, endDate)
                .collect { records ->
                    _records.value = records.sortedByDescending { it.date }
                }
        }
        
        viewModelScope.launch {
            workerDao.getAllWorkers()
                .collect { workers ->
                    _worker.value = workers.find { it.id == workerId }
                }
        }
    }
} 