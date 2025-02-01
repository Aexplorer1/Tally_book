package com.mushroom.worklog.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 先备份原表
            database.execSQL(
                "ALTER TABLE work_records RENAME TO work_records_old"
            )
            
            // 创建新表
            database.execSQL("""
                CREATE TABLE work_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    workerId INTEGER NOT NULL,
                    date INTEGER NOT NULL,
                    workType TEXT NOT NULL,
                    hours REAL NOT NULL,
                    pieces INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    notes TEXT NOT NULL,
                    isSettled INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY(workerId) REFERENCES workers(id) ON DELETE CASCADE
                )
            """)
            
            // 复制数据
            database.execSQL("""
                INSERT INTO work_records (
                    id, workerId, date, workType, hours, pieces, amount, notes, isSettled
                )
                SELECT 
                    id, workerId, date, workType, hours, pieces, amount, notes, 0
                FROM work_records_old
            """)
            
            // 删除旧表
            database.execSQL("DROP TABLE work_records_old")
            
            // 创建索引
            database.execSQL(
                "CREATE INDEX index_work_records_workerId ON work_records(workerId)"
            )
        }
    }
} 