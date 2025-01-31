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

    fun addWorker(name: String, phone: String) {
        viewModelScope.launch {
            try {
                val worker = Worker(
                    name = name,
                    phoneNumber = phone
                )
                val id = workerDao.insertWorker(worker)
                if (id > 0) {
                    _uiState.value = UiState.Success
                    Log.d("WorkerViewModel", "Worker added successfully with id: $id")
                } else {
                    _uiState.value = UiState.Error("添加工人失败")
                    Log.e("WorkerViewModel", "Failed to add worker, returned id: $id")
                }
            } catch (e: Exception) {
                Log.e("WorkerViewModel", "Error adding worker", e)
                _uiState.value = UiState.Error(e.message ?: "添加工人失败")
            }
        }
    }

    fun deleteWorker(worker: Worker) {
        viewModelScope.launch {
            try {
                workerDao.deleteWorker(worker)
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                Log.e("WorkerViewModel", "Error deleting worker", e)
                _uiState.value = UiState.Error(e.message ?: "删除工人失败")
            }
        }
    }

    sealed class UiState {
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
} 