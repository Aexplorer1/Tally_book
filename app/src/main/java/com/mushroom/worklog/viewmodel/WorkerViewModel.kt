package com.mushroom.worklog.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushroom.worklog.database.WorkerDao
import com.mushroom.worklog.model.Worker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkerViewModel @Inject constructor(
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
                    Log.e("WorkerViewModel", "Error loading workers", e)
                    _uiState.value = UiState.Error(e.message ?: "未知错误")
                }
                .collect { workers ->
                    _workers.value = workers
                }
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    fun validateWorker(name: String, phoneNumber: String): ValidationResult {
        // 检查姓名
        if (name.isBlank()) {
            return ValidationResult.Error("请输入工人姓名")
        }

        // 检查电话号码格式
        if (phoneNumber.isBlank()) {
            return ValidationResult.Error("请输入电话号码")
        }
        
        if (!phoneNumber.matches(Regex("^1[3-9]\\d{9}$"))) {
            return ValidationResult.Error("请输入正确的11位手机号码")
        }

        return ValidationResult.Success
    }

    fun addWorker(name: String, phoneNumber: String) {
        viewModelScope.launch {
            try {
                val validationResult = validateWorker(name, phoneNumber)
                when (validationResult) {
                    is ValidationResult.Success -> {
                        val worker = Worker(name = name, phoneNumber = phoneNumber)
                        workerDao.insertWorker(worker)
                        _uiState.value = UiState.Success
                    }
                    is ValidationResult.Error -> {
                        _uiState.value = UiState.Error(validationResult.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "添加工人失败")
            }
        }
    }

    fun updateWorker(worker: Worker) {
        viewModelScope.launch {
            try {
                workerDao.updateWorker(worker)
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                Log.e("WorkerViewModel", "Error updating worker", e)
                _uiState.value = UiState.Error(e.message ?: "更新工人信息失败")
            }
        }
    }

    sealed class UiState {
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
} 