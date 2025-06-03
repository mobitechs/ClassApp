package com.mobitechs.classapp.data.model.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobitechs.classapp.data.model.response.SubCategoryItem
import com.mobitechs.classapp.data.model.response.SubjectItem


@Dao
interface SubCategoryDao {

//    @Query("SELECT * FROM subCategories WHERE is_active = 'Active'")
//    fun getAllSubCategories(): List<SubCategoryItem>


    @Query("""
    SELECT * FROM subCategories 
    WHERE is_active = 'Active'
    AND (:categoryId = 0 OR category_id = :categoryId)
""")
    fun getSubCategories(
        categoryId: Int = 0,
    ): List<SubCategoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubCategories(subCategories: List<SubCategoryItem>)


}