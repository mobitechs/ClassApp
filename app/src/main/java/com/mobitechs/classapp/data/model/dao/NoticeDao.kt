package com.mobitechs.classapp.data.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobitechs.classapp.data.model.response.Notice


@Dao
interface NoticeDao {
    @Query("SELECT * FROM noticeBoard WHERE is_active = 'Active'")
    fun getAllNotice(): List<Notice>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: List<Notice>)


}