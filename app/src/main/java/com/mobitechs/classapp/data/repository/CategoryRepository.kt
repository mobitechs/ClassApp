package com.mobitechs.classapp.data.repository


import android.util.Log
import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.local.SharedPrefsManager
import com.mobitechs.classapp.data.model.dao.CategoryDao
import com.mobitechs.classapp.data.model.dao.SubCategoryDao
import com.mobitechs.classapp.data.model.dao.SubjectDao
import com.mobitechs.classapp.data.model.response.CategoryResponse
import com.mobitechs.classapp.data.model.response.SubCategoryResponse
import com.mobitechs.classapp.data.model.response.SubjectResponse
import com.mobitechs.classapp.utils.Constants
import com.mobitechs.classapp.utils.updateCategoriesWithUIData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager,
    private val categoryDao: CategoryDao,
    private val subcategoryDao: SubCategoryDao,
    private val subjectDao: SubjectDao
) {

    suspend fun getCategories(): CategoryResponse = withContext(Dispatchers.IO) {
        // Check if already called API today
        if (sharedPrefsManager.isAlreadySyncedToday(Constants.KEY_tbl_categories)) {
            Log.d("Cat API", "Cat Already called API today")
            // Get from Room DB
            return@withContext CategoryResponse(
                categories = categoryDao.getAllCategories(),
                message = "Categories from RoomDb",
                status = true,
                status_code = 200
            )
        } else {
            val response = apiService.getCategories()
            if (response.isSuccessful) {
                val categoryResponse = response.body() ?: throw Exception("Empty response body")
                //add random icon background color
                val updatedCategories = updateCategoriesWithUIData(categoryResponse.categories)

                // Save to Room DB
                categoryDao.insertCategories(updatedCategories)
                sharedPrefsManager.setLastSyncDate(Constants.KEY_tbl_categories)

                return@withContext categoryResponse.copy(categories = updatedCategories)
            } else {
                throw Exception("Failed to get categories: ${response.message()}")
                // you can cache data from room db here and return
            }
        }
    }


//Sub categories ----------------------------------------------------------------------------------

    suspend fun getAllSubCategories(): SubCategoryResponse = withContext(Dispatchers.IO) {
        // Check if already called API today
        if (sharedPrefsManager.isAlreadySyncedToday(Constants.KEY_tbl_sub_categories)) {
            Log.d("subCat API", "subCat Already called API today")
            // Get from Room DB
            return@withContext SubCategoryResponse(
                subCategories = subcategoryDao.getAllSubCategories(),
                message = "SubCategories from RoomDb",
                status = true,
                status_code = 200
            )
        } else {
            // call api
            val response = apiService.getAllSubCategories()
            if (response.isSuccessful) {
                val subCategoryResponse = response.body() ?: throw Exception("Empty response body")

                // Save to Room DB // first delete old subcategories too
                subcategoryDao.insertSubCategories(subCategoryResponse.subCategories)
                sharedPrefsManager.setLastSyncDate(Constants.KEY_tbl_sub_categories)
                return@withContext subCategoryResponse
            } else {
                throw Exception("Failed to get subCategories: ${response.message()}")
            }
        }
    }


    suspend fun getSubCategoryByCategory(categoryId: Int): SubCategoryResponse =
        withContext(Dispatchers.IO) {

            return@withContext SubCategoryResponse(
                subCategories = subcategoryDao.getSubCategoriesByCategory(categoryId),
                message = "SubCategories by category from RoomDb",
                status = true,
                status_code = 200
            )

//          val response = apiService.getSubCategoryByCategory(categoryId)
//            if (response.isSuccessful) {
//                return@withContext response.body() ?: throw Exception("Empty response body")
//            } else {
//                throw Exception("Failed to get categories: ${response.message()}")
//            }
        }


//Subject ----------------------------------------------------------------------------------------


    suspend fun getAllSubject(): SubjectResponse = withContext(Dispatchers.IO) {
        // Check if already called API today
        if (sharedPrefsManager.isAlreadySyncedToday(Constants.KEY_tbl_subjects)) {
            Log.d("subjects API", "Already called API today")
            // Get from Room DB
            return@withContext SubjectResponse(
                subjects = subjectDao.getAllSubjects(),
                message = "Subjects from RoomDb",
                status = true,
                status_code = 200
            )
        } else {
            // call api
            val response = apiService.getAllSubject()
            if (response.isSuccessful) {
                val subjectResponse = response.body() ?: throw Exception("Empty response body")

                // Save to Room DB
                subjectDao.insertSubjects(subjectResponse.subjects)
                sharedPrefsManager.setLastSyncDate(Constants.KEY_tbl_subjects)
                return@withContext subjectResponse
            } else {
                throw Exception("Failed to get subjects: ${response.message()}")
            }
        }
    }


    suspend fun getSubjectByCategory(categoryId: Int): SubjectResponse =
        withContext(Dispatchers.IO) {
            // Get from Room DB
            return@withContext SubjectResponse(
                subjects = subjectDao.getSubjectByCategory(categoryId),
                message = "Subjects ByCategory from RoomDb",
                status = true,
                status_code = 200
            )
        }

    suspend fun getSubjectBySubCategory(subCategoryId: Int): SubjectResponse =
        withContext(Dispatchers.IO) {
            // Get from Room DB
            return@withContext SubjectResponse(
                subjects = subjectDao.getSubjectBySubCategory(subCategoryId),
                message = "Subjects BySubCategory from RoomDb",
                status = true,
                status_code = 200
            )
        }

    suspend fun getSubjectByCategorySubCategory(
        categoryId: Int,
        subCategoryId: Int
    ): SubjectResponse =
        withContext(Dispatchers.IO) {
            // Get from Room DB
            return@withContext SubjectResponse(
                subjects = subjectDao.getSubjectByCategorySubCategory(categoryId, subCategoryId),
                message = "Subjects ByCategorySubCategory from RoomDb",
                status = true,
                status_code = 200
            )
        }


}