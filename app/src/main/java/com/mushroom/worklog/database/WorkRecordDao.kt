package com.mushroom.worklog.database

import androidx.room.*
import com.mushroom.worklog.model.WorkRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkRecordDao {
    @Query("SELECT * FROM work_records WHERE workerId = :workerId")
    fun getWorkerRecords(workerId: Long): Flow<List<WorkRecord>>
    
    @Query("SELECT * FROM work_records WHERE date BETWEEN :startDate AND :endDate")
    fun getRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<WorkRecord>>
    
    @Insert
    suspend fun insertWorkRecord(record: WorkRecord)
    
    @Update
    suspend fun updateWorkRecord(workRecord: WorkRecord)
    
    @Delete
    suspend fun deleteWorkRecord(workRecord: WorkRecord)
} 