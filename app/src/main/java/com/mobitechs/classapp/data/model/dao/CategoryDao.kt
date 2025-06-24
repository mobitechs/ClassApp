package com.mobitechs.classapp.data.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobitechs.classapp.data.model.response.CategoryItem

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE is_active = 'Active'")
    fun getAllCategories(): List<CategoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryItem>)

    @Query("DELETE FROM categories WHERE lastSyncedAt < :timestamp")
    suspend fun deleteOldCategories(timestamp: Long)
}