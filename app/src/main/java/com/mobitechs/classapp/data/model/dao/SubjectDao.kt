package com.mobitechs.classapp.data.model.dao



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.SubjectItem


@Dao
interface SubjectDao {

//    @Query("SELECT * FROM subjects WHERE is_active = 'Active' AND ")
//    fun getAllSubjects(): List<SubjectItem>


    @Query("""
    SELECT * FROM subjects 
    WHERE is_active = 'Active'
    AND (:categoryId = 0 OR category_id = :categoryId)
    AND (:subcategoryId = 0 OR subcategory_id = :subcategoryId)
""")
    fun getSubject(
        categoryId: Int = 0,
        subcategoryId: Int = 0
    ): List<SubjectItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectItem>)


}