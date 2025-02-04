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
class WorkRecordViewModel @Inject constructor(
    private val workRecordDao: WorkRecordDao,
    private val workerDao: WorkerDao
) : ViewModel() {
    private val _workers = MutableStateFlow<List<Worker>>(emptyList())
    val workers: StateFlow<List<Worker>> = _workers

    private val _uiState = MutableStateFlow<UiState>(UiState.Success)
    val uiState: StateFlow<UiState> = _uiState

    init {
        loadWorkers()
    }

    private fun loadWorkers() {
        viewModelScope.launch {
            workerDao.getAllWorkers()
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "加载工人列表失败")
                }
                .collect { workers ->
                    _workers.value = workers
                }
        }
    }

    fun addWorkRecord(
        workerId: Long,
        date: Long,
        workType: String,
        hours: Double,
        pieces: Int,
        amount: Double,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                val record = WorkRecord(
                    workerId = workerId,
                    date = date,
                    workType = workType,
                    hours = hours,
                    pieces = pieces,
                    amount = amount,
                    notes = notes
                )
                workRecordDao.insertWorkRecord(record)
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "添加工作记录失败")
            }
        }
    }

    sealed class UiState {
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    fun validateRecord(
        workerId: Long?,
        workType: String,
        hours: String,
        pieces: String,
        amount: String
    ): ValidationResult {
        // 检查是否选择了工人
        if (workerId == null) {
            return ValidationResult.Error("请选择工人")
        }

        // 检查工作类型
        if (workType.isBlank()) {
            return ValidationResult.Error("请输入工作类型")
        }

        // 检查工时和计件数
        val hoursValue = hours.toDoubleOrNull() ?: 0.0
        val piecesValue = pieces.toIntOrNull() ?: 0
        if (hoursValue == 0.0 && piecesValue == 0) {
            return ValidationResult.Error("请输入工时或计件数量")
        }

        // 检查金额
        val amountValue = amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            return ValidationResult.Error("请输入有效的工资金额")
        }

        return ValidationResult.Success
    }
} 