package com.mushroom.worklog.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mushroom.worklog.model.Worker
import com.mushroom.worklog.model.WorkRecord

@Database(
    entities = [Worker::class, WorkRecord::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
    abstract fun workRecordDao(): WorkRecordDao

    companion object {
        const val DATABASE_NAME = "worklog.db"
    }
} 