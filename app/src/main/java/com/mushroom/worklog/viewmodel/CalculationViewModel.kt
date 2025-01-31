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
class CalculationViewModel @Inject constructor(
    private val workRecordDao: WorkRecordDao,
    private val workerDao: WorkerDao
) : ViewModel() {
    private val _workers = MutableStateFlow<List<Worker>>(emptyList())
    val workers: StateFlow<List<Worker>> = _workers

    private val _records = MutableStateFlow<Map<Worker, List<WorkRecord>>>(emptyMap())
    val records: StateFlow<Map<Worker, List<WorkRecord>>> = _records

    private val _startDate = MutableStateFlow<Long>(0)
    private val _endDate = MutableStateFlow<Long>(0)

    init {
        loadWorkers()
    }

    private fun loadWorkers() {
        viewModelScope.launch {
            workerDao.getAllWorkers()
                .collect { workers ->
                    _workers.value = workers
                }
        }
    }

    fun setDateRange(startDate: Long, endDate: Long) {
        _startDate.value = startDate
        _endDate.value = endDate
        loadRecords()
    }

    private fun loadRecords() {
        viewModelScope.launch {
            val startDate = _startDate.value
            val endDate = _endDate.value
            if (startDate == 0L || endDate == 0L) return@launch

            workRecordDao.getRecordsByDateRange(startDate, endDate)
                .collect { records ->
                    val recordsByWorker = records.groupBy { record ->
                        _workers.value.find { it.id == record.workerId }!!
                    }
                    _records.value = recordsByWorker
                }
        }
    }

    fun calculateTotal(worker: Worker, records: List<WorkRecord>): Double {
        return records.sumOf { it.amount }
    }
} 