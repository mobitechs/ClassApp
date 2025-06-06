package com.mobitechs.classapp.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.mobitechs.classapp.data.model.dao.*
import com.mobitechs.classapp.data.model.response.*

@Database(
    entities = [
        Course::class,
        CategoryItem::class,
        SubCategoryItem::class,
        SubjectItem::class,
        NotificationItem::class,
        Notice::class,
        OfferBanner::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subCategoryDao(): SubCategoryDao
    abstract fun subjectDao(): SubjectDao
    abstract fun notificationDao(): NotificationDao
    abstract fun noticeDao(): NoticeDao
    abstract fun offerBannerDao(): OfferBannerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "classconnect_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun destroyInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}