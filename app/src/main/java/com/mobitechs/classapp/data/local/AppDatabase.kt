package com.mobitechs.classapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mobitechs.classapp.data.model.dao.CategoryDao
import com.mobitechs.classapp.data.model.dao.CourseDao
import com.mobitechs.classapp.data.model.dao.DownloadContentDao
import com.mobitechs.classapp.data.model.dao.NoticeDao
import com.mobitechs.classapp.data.model.dao.NotificationDao
import com.mobitechs.classapp.data.model.dao.OfferBannerDao
import com.mobitechs.classapp.data.model.dao.SubCategoryDao
import com.mobitechs.classapp.data.model.dao.SubjectDao
import com.mobitechs.classapp.data.model.dao.chat.ChatDao
import com.mobitechs.classapp.data.model.dao.chat.ChatUserDao
import com.mobitechs.classapp.data.model.dao.chat.MessageDao
import com.mobitechs.classapp.data.model.response.CategoryItem
import com.mobitechs.classapp.data.model.response.Chat
import com.mobitechs.classapp.data.model.response.ChatMessage
import com.mobitechs.classapp.data.model.response.ChatParticipantEntity
import com.mobitechs.classapp.data.model.response.ChatUser
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.DownloadContent
import com.mobitechs.classapp.data.model.response.Notice
import com.mobitechs.classapp.data.model.response.NotificationItem
import com.mobitechs.classapp.data.model.response.OfferBanner
import com.mobitechs.classapp.data.model.response.SubCategoryItem
import com.mobitechs.classapp.data.model.response.SubjectItem

@Database(
    entities = [
        Course::class,
        CategoryItem::class,
        SubCategoryItem::class,
        SubjectItem::class,
        NotificationItem::class,
        Notice::class,
        OfferBanner::class,
        DownloadContent::class,
        ChatUser::class,
        Chat::class,
        ChatMessage::class,
        ChatParticipantEntity::class
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
    abstract fun contentDao(): DownloadContentDao
    abstract fun chatUserDao(): ChatUserDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao

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