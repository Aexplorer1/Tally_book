package com.mushroom.worklog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushroom.worklog.database.WorkRecordDao
import com.mushroom.worklog.database.WorkerDao
import com.mushroom.worklog.model.Worker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val workRecordDao: WorkRecordDao,
    private val workerDao: WorkerDao
) : ViewModel() {
    private val _statistics = MutableStateFlow<Map<Worker, Double>>(emptyMap())
    val statistics: StateFlow<Map<Worker, Double>> = _statistics

    private val _selectedTimeRange = MutableStateFlow(TimeRange.YEAR)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount

    private val _totalWorkerCount = MutableStateFlow(0)
    val totalWorkerCount: StateFlow<Int> = _totalWorkerCount

    fun setTimeRange(range: TimeRange) {
        viewModelScope.launch {
            _selectedTimeRange.value = range
            loadStatistics(range)
        }
    }

    private fun loadStatistics(range: TimeRange) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            
            // 设置结束时间为今天的23:59:59
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endDate = calendar.timeInMillis

            // 设置开始时间
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            when (range) {
                TimeRange.YEAR -> {
                    // 设置为本年初1月1日
                    calendar.set(Calendar.MONTH, Calendar.JANUARY)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                }
                TimeRange.MONTH -> {
                    // 设置为本月1日
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                }
                TimeRange.WEEK -> {
                    // 获取本周一
                    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                    }
                }
            }
            val startDate = calendar.timeInMillis

            // 获取所有记录
            combine(
                workRecordDao.getRecordsByDateRange(
                    workerId = null,
                    startDate = startDate,
                    endDate = endDate
                ),
                workerDao.getAllWorkers()
            ) { records, workers ->
                // 按工人分组并计算总金额
                val workerStats = workers.associateWith { worker ->
                    records.filter { it.workerId == worker.id }.sumOf { it.amount }
                }.filterValues { it > 0 }  // 只保留有记录的工人
                
                _statistics.value = workerStats
                _totalAmount.value = workerStats.values.sum()
                _totalWorkerCount.value = workerStats.size
            }.collect()
        }
    }

    enum class TimeRange {
        WEEK, MONTH, YEAR
    }

    init {
        loadStatistics(TimeRange.YEAR)  // 默认显示本年数据
    }
} 