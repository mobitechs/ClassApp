package com.mobitechs.classapp.screens.store


import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.repository.CourseRepository
import com.mobitechs.classapp.utils.BaseViewModel
import kotlinx.coroutines.launch


abstract class CourseActionsViewModel : BaseViewModel() {

    abstract val courseRepository: CourseRepository

    abstract fun updateCourseInState(courseId: Int, transform: (Course) -> Course)


    fun handleFavoriteClick(courseId: Int, is_favourited: Boolean) {
        if (is_favourited) {
            removeFromFavorite(courseId)
        } else {
            addToFavorite(courseId)
        }
    }

    fun handleWishlistClick(courseId: Int, is_in_wishlist: Boolean) {
        if (is_in_wishlist) {
            removeFromWishlist(courseId)
        } else {
            addToWishlist(courseId)
        }
    }

    fun handleLikeClick(courseId: Int, isLike: Boolean) {
        if (isLike) {
            likeCourse(courseId)
        } else {
            dislikeCourse(courseId)
        }
    }

    fun addToFavorite(courseId: Int) {
        viewModelScope.launch {
            try {
                val response = courseRepository.addToFavorite(courseId)
                showToast(response.message)

                if (response.status_code == 200) {
                    updateCourseInState(courseId) { course ->
                        course.copy(is_favourited = true)
                    }
                }
            } catch (e: Exception) {
                showToast(e.message ?: "Failed to add to favorites")
            }
        }
    }

    fun removeFromFavorite(courseId: Int) {
        viewModelScope.launch {
            try {
                val response = courseRepository.removeFromFavorite(courseId)
                showToast(response.message)

                if (response.status_code == 200) {
                    updateCourseInState(courseId) { course ->
                        course.copy(is_favourited = false)
                    }
                }
            } catch (e: Exception) {
                showToast(e.message ?: "Failed to remove from favorites")
            }
        }
    }

    fun addToWishlist(courseId: Int) {
        viewModelScope.launch {
            try {
                val response = courseRepository.addToWishlist(courseId)
                showToast(response.message)

                if (response.status_code == 200) {
                    updateCourseInState(courseId) { course ->
                        course.copy(is_in_wishlist = true)
                    }
                }
            } catch (e: Exception) {
                showToast(e.message ?: "Failed to add to wishlist")
            }
        }
    }

    fun removeFromWishlist(courseId: Int) {
        viewModelScope.launch {
            try {
                val response = courseRepository.removeFromWishlist(courseId)
                showToast(response.message)

                if (response.status_code == 200) {
                    updateCourseInState(courseId) { course ->
                        course.copy(is_in_wishlist = false)
                    }
                }
            } catch (e: Exception) {
                showToast(e.message ?: "Failed to remove from wishlist")
            }
        }
    }

    fun likeCourse(courseId: Int) {
        viewModelScope.launch {
            try {
                val response = courseRepository.likeCourse(courseId)
                showToast(response.message)

                if (response.status_code == 200) {
                    updateCourseInState(courseId) { course ->
                        course.copy(is_liked = true)
                    }
                }
            } catch (e: Exception) {
                showToast(e.message ?: "Failed to like Course")
            }
        }
    }

    fun dislikeCourse(courseId: Int) {
        viewModelScope.launch {
            try {
                val response = courseRepository.dislikeCourse(courseId)
                showToast(response.message)

                if (response.status_code == 200) {
                    updateCourseInState(courseId) { course ->
                        course.copy(is_liked = false)
                    }
                }
            } catch (e: Exception) {
                showToast(e.message ?: "Failed to dislike Course")
            }
        }
    }


}


fun List<Course>.updateCourseList(courseId: Int, transform: (Course) -> Course): List<Course> {
    return this.map { course ->
        if (course.id == courseId) {
            transform(course)
        } else {
            course
        }
    }
}

fun Course.updateSingleCourse(courseId: Int, transform: (Course) -> Course): Course {
    return if (this.id == courseId) {
        transform(this)
    } else {
        this
    }
}
