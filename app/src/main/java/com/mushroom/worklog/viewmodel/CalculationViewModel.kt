package com.mushroom.worklog.viewmodel

import android.icu.util.Calendar
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

    private val _selectedWorkerRecords = MutableStateFlow<List<WorkRecord>>(emptyList())
    val selectedWorkerRecords: StateFlow<List<WorkRecord>> = _selectedWorkerRecords

    private val _workerTotals = MutableStateFlow<Map<Worker, Double>>(emptyMap())
    val workerTotals: StateFlow<Map<Worker, Double>> = _workerTotals

    private val _startDate = MutableStateFlow(
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    )
    val startDate: StateFlow<Long> = _startDate

    private val _endDate = MutableStateFlow(
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    )
    val endDate: StateFlow<Long> = _endDate

    init {
        loadWorkers()
        loadTotals()
    }

    private fun loadWorkers() {
        viewModelScope.launch {
            workerDao.getAllWorkers()
                .collect { workers ->
                    _workers.value = workers
                }
        }
    }

    fun setDateRange(start: Long, end: Long) {
        viewModelScope.launch {
            _startDate.value = start
            _endDate.value = end
            loadTotals()
        }
    }

    private fun loadTotals() {
        viewModelScope.launch {
            try {
                combine(
                    workerDao.getAllWorkers(),
                    workRecordDao.getWorkerTotalsByDateRange(startDate.value, endDate.value)
                ) { workersList, totals ->
                    workersList.associateWith { worker ->
                        totals.find { it.workerId == worker.id }?.total ?: 0.0
                    }
                }.catch { e ->
                    e.printStackTrace()
                    emit(emptyMap())
                }.collect { totals ->
                    _workerTotals.value = totals
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _workerTotals.value = emptyMap()
            }
        }
    }

    fun loadWorkerRecords(workerId: Long) {
        viewModelScope.launch {
            try {
                workRecordDao.getWorkerRecordsByDateRange(
                    workerId,
                    startDate.value,
                    endDate.value
                ).catch { emit(emptyList()) }
                .collect { records ->
                    _selectedWorkerRecords.value = records
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun calculateTotal(worker: Worker, records: List<WorkRecord>): Double {
        return records.sumOf { it.amount }
    }

    fun settleWorkerSalary(workerId: Long) {
        viewModelScope.launch {
            try {
                // 获取未结算的记录
                val records = workRecordDao.getWorkerRecordsByDateRange(
                    workerId = workerId,
                    startDate = startDate.value,
                    endDate = endDate.value
                ).first().filter { !it.isSettled }
                
                // 更新结算状态
                val recordIds = records.map { it.id }
                if (recordIds.isNotEmpty()) {
                    workRecordDao.updateSettleStatus(recordIds, true)
                    // 刷新数据
                    loadTotals()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
} 