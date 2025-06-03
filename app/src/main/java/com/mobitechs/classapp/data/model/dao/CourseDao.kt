package com.mobitechs.classapp.data.model.dao

import androidx.room.*
import com.mobitechs.classapp.data.model.response.Course
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>)

    @Query("SELECT * FROM courses WHERE is_active = 'Active' ORDER BY id DESC")
    fun getAllCourses(): List<Course>

    @Query("SELECT * FROM courses WHERE course_category_id = :categoryId AND is_active = 'Active'")
    fun getCoursesByCategory(categoryId: Int): List<Course>

    @Query("SELECT * FROM courses WHERE course_subcategory_id = :subcategoryId AND is_active = 'Active'")
    fun getCoursesBySubCategory(subcategoryId: Int): List<Course>

    @Query("SELECT * FROM courses WHERE course_subject_id = :subjectId AND is_active = 'Active'")
    fun getCoursesBySubject(subjectId: Int): List<Course>

//combine in one query with filters
@Query("""
    SELECT * FROM courses 
    WHERE is_active = 'Active'
    AND (:categoryId = 0 OR course_category_id = :categoryId)
    AND (:subcategoryId = 0 OR course_subcategory_id = :subcategoryId)
    AND (:subjectId = 0 OR course_subject_id = :subjectId)
""")
fun getCourses(
    categoryId: Int = 0,
    subcategoryId: Int = 0,
    subjectId: Int = 0
): List<Course>



    @Query("SELECT * FROM courses WHERE id = :courseId")
    suspend fun getCourseById(courseId: Int): Course?

    @Query("SELECT * FROM courses WHERE isFavorite = 1")
    fun getFavoriteCourses(): List<Course>




    @Query("UPDATE courses SET isFavorite = :isFavorite WHERE id = :courseId")
    suspend fun updateFavoriteStatus(courseId: Int, isFavorite: Boolean)
}