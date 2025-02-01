package com.mushroom.worklog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushroom.worklog.database.WorkerDao
import com.mushroom.worklog.model.Worker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val workerDao: WorkerDao
) : ViewModel() {
    private val _workers = MutableStateFlow<List<Worker>>(emptyList())
    val workers: StateFlow<List<Worker>> = _workers

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

    fun deleteWorker(worker: Worker) {
        viewModelScope.launch {
            workerDao.deleteWorker(worker)
        }
    }
} 