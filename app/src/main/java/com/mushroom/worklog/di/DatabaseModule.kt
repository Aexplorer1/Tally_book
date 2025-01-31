package com.mushroom.worklog.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mushroom.worklog.database.AppDatabase
import com.mushroom.worklog.database.WorkerDao
import com.mushroom.worklog.database.WorkRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建新表
            database.execSQL(
                """
                CREATE TABLE workers_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    phoneNumber TEXT NOT NULL
                )
                """
            )
            // 复制数据
            database.execSQL(
                """
                INSERT INTO workers_new (id, name, phoneNumber)
                SELECT id, name, phoneNumber FROM workers
                """
            )
            // 删除旧表
            database.execSQL("DROP TABLE workers")
            // 重命名新表
            database.execSQL("ALTER TABLE workers_new RENAME TO workers")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "worklog.db"
        )
        .fallbackToDestructiveMigration()  // 在版本更新时重建数据库
        .build()
    }

    @Provides
    fun provideWorkerDao(database: AppDatabase): WorkerDao {
        return database.workerDao()
    }

    @Provides
    fun provideWorkRecordDao(database: AppDatabase): WorkRecordDao {
        return database.workRecordDao()
    }
} 