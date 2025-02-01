package com.mushroom.worklog.di

import android.content.Context
import androidx.room.Room
import com.mushroom.worklog.database.AppDatabase
import com.mushroom.worklog.database.WorkerDao
import com.mushroom.worklog.database.WorkRecordDao
import com.mushroom.worklog.database.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .addMigrations(DatabaseMigrations.MIGRATION_1_2)
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