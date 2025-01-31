package com.mushroom.worklog.database

import androidx.room.*
import com.mushroom.worklog.model.Worker
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers ORDER BY name")
    fun getAllWorkers(): Flow<List<Worker>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker): Long
    
    @Delete
    suspend fun deleteWorker(worker: Worker)
} 