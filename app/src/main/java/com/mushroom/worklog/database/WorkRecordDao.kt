package com.mushroom.worklog.database

import androidx.room.*
import com.mushroom.worklog.model.WorkRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkRecordDao {
    @Query("SELECT * FROM work_records WHERE workerId = :workerId")
    fun getWorkerRecords(workerId: Long): Flow<List<WorkRecord>>
    
    @Query("""
        SELECT * FROM work_records 
        WHERE (:workerId IS NULL OR workerId = :workerId)
        AND date >= :startDate AND date <= :endDate
        ORDER BY date DESC
    """)
    fun getRecordsByDateRange(
        workerId: Long?,
        startDate: Long,
        endDate: Long
    ): Flow<List<WorkRecord>>
    
    @Insert
    suspend fun insertWorkRecord(record: WorkRecord)
    
    @Update
    suspend fun updateWorkRecord(workRecord: WorkRecord)
    
    @Delete
    suspend fun deleteWorkRecord(workRecord: WorkRecord)

    @Query("""
        SELECT * FROM work_records 
        WHERE date BETWEEN :startDate AND :endDate 
        AND workerId = :workerId
        ORDER BY date DESC
    """)
    fun getWorkerRecordsByDateRange(
        workerId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<WorkRecord>>

    @Query("""
        SELECT * FROM work_records 
        WHERE (:workerId IS NULL OR workerId = :workerId)
        AND date >= :startDate AND date <= :endDate
        AND isSettled = :isSettled
        ORDER BY date DESC
    """)
    fun getRecordsByDateRangeAndSettleStatus(
        workerId: Long?,
        startDate: Long,
        endDate: Long,
        isSettled: Boolean
    ): Flow<List<WorkRecord>>

    @Query("UPDATE work_records SET isSettled = :isSettled WHERE id IN (:recordIds)")
    suspend fun updateSettleStatus(recordIds: List<Long>, isSettled: Boolean)

    @Query("""
        SELECT workerId, SUM(amount) as total 
        FROM work_records 
        WHERE date >= :startDate AND date <= :endDate 
        AND isSettled = 0
        GROUP BY workerId
    """)
    fun getWorkerTotalsByDateRange(
        startDate: Long,
        endDate: Long
    ): Flow<List<WorkerTotal>>

    @Query("""
        SELECT * FROM work_records 
        WHERE workerId = :workerId 
        AND date BETWEEN :startDate AND :endDate
        AND isSettled = 0
        ORDER BY date DESC
    """)
    fun getUnsettledWorkerRecordsByDateRange(
        workerId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<WorkRecord>>

    data class WorkerTotal(
        val workerId: Long,
        val total: Double
    )
} 