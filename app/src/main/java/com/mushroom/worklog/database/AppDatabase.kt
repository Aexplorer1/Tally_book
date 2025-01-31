package com.mushroom.worklog.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mushroom.worklog.model.Worker
import com.mushroom.worklog.model.WorkRecord

@Database(
    entities = [Worker::class, WorkRecord::class],
    version = 2,  // 增加版本号
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
    abstract fun workRecordDao(): WorkRecordDao
} 