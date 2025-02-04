package com.mushroom.worklog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushroom.worklog.database.WorkRecordDao
import com.mushroom.worklog.database.WorkerDao
import com.mushroom.worklog.model.WorkRecord
import com.mushroom.worklog.model.Worker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val workRecordDao: WorkRecordDao,
    private val workerDao: WorkerDao
) : ViewModel() {
    private val _records = MutableStateFlow<List<WorkRecord>>(emptyList())
    val records: StateFlow<List<WorkRecord>> = _records

    private val _workers = MutableStateFlow<List<Worker>>(emptyList())
    val workers: StateFlow<List<Worker>> = _workers

    init {
        loadWorkers()
    }

    private fun loadWorkers() {
        viewModelScope.launch {
            try {
                workerDao.getAllWorkers()
                    .catch { emit(emptyList()) }
                    .collect { workers ->
                        _workers.value = workers
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadRecords(workerId: Long?, startDate: Long, endDate: Long) {
        viewModelScope.launch {
            try {
                workRecordDao.getRecordsByDateRange(workerId, startDate, endDate)
                    .catch { emit(emptyList()) }
                    .collect { records ->
                        _records.value = records.sortedWith(
                            compareBy<WorkRecord> { it.isSettled }
                                .thenByDescending { it.date }
                        )
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
} 