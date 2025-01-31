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
} 