package com.mobitechs.classapp.data.local

import com.mobitechs.classapp.data.model.response.CategoryItem
import com.mobitechs.classapp.data.model.response.SubCategoryItem
import com.mobitechs.classapp.data.model.response.SubjectItem

// Static data for Mathematics category



object StaticData {
    // Main category
    val mathCategory = CategoryItem(
        id = 1,
        name = "Mathematics",
        is_active = "Active",
        created_at = "2025-05-01T00:00:00.000000Z",
        updated_at = "2025-05-01T00:00:00.000000Z",
        deleted_at = "2025-05-01T00:00:00.000000Z",
        added_by =""
    )

    // Subcategories
    val mathSubcategories = listOf(
        SubCategoryItem(
            id = 1,
            name = "Algebra",
            category_id = 1,
            is_active = "Active",
            category = mathCategory,
            added_by = "",
            created_at = "2025-05-01T00:00:00.000000Z",
            updated_at = "2025-05-01T00:00:00.000000Z",
            deleted_at = ""
        ),
        SubCategoryItem(
            id = 2,
            name = "Geometry",
            category_id = 1,
            is_active = "Active",
            category = mathCategory,
            added_by = "",
            created_at = "2025-05-01T00:00:00.000000Z",
            updated_at = "2025-05-01T00:00:00.000000Z",
            deleted_at = ""
        ),
        SubCategoryItem(
            id = 3,
            name = "Calculus",
            category_id = 1,
            is_active = "Active",
            category = mathCategory,
            added_by = "",
            created_at = "2025-05-01T00:00:00.000000Z",
            updated_at = "2025-05-01T00:00:00.000000Z",
            deleted_at = ""
        ),
        SubCategoryItem(
            id = 4,
            name = "Statistics",
            category_id = 1,
            is_active = "Active",
            category = mathCategory,
            added_by = "",
            created_at = "2025-05-01T00:00:00.000000Z",
            updated_at = "2025-05-01T00:00:00.000000Z",
            deleted_at = ""
        ),
        SubCategoryItem(
            id = 5,
            name = "Trigonometry",
            category_id = 1,
            is_active = "Active",
            category = mathCategory,
            added_by = "",
            created_at = "2025-05-01T00:00:00.000000Z",
            updated_at = "2025-05-01T00:00:00.000000Z",
            deleted_at = ""
        ),
        SubCategoryItem(
            id = 6,
            name = "Logic",
            category_id = 1,
            is_active = "Active",
            category = mathCategory,
            added_by = "",
            created_at = "2025-05-01T00:00:00.000000Z",
            updated_at = "2025-05-01T00:00:00.000000Z",
            deleted_at = ""
        )
    )

    // Subjects for Trigonometry subcategory
    val trigSubjects = listOf(
        SubjectItem(
            id = 1,
            name = "Basics of Trigonometry",
            is_active = "Active",
            added_by = "",
            created_at = "2025-05-01T00:00:00.000000Z",
            updated_at = "2025-05-01T00:00:00.000000Z",
            deleted_at = ""
        ),
        SubjectItem(
            id = 2,
            name = "Advanced Trigonometry",
            is_active = "Active",
            added_by = "",
            created_at = "2025-05-01T00:00:00.000000Z",
            updated_at = "2025-05-01T00:00:00.000000Z",
            deleted_at = ""
        ),
        SubjectItem(
            id = 3,
            name = "Trigonometric Formulas",
            is_active = "Active",
            added_by = "",
            created_at = "2025-05-01T00:00:00.000000Z",
            updated_at = "2025-05-01T00:00:00.000000Z",
            deleted_at = ""
        ),
        SubjectItem(
            id = 4,
            name = "Trigonometric Functions",
            is_active = "Active",
            added_by = "",
            created_at = "2025-05-01T00:00:00.000000Z",
            updated_at = "2025-05-01T00:00:00.000000Z",
            deleted_at = ""
        ),
        SubjectItem(
            id = 5,
            name = "Trigonometric Identities",
            is_active = "Active",
            added_by = "",
            created_at = "2025-05-01T00:00:00.000000Z",
            updated_at = "2025-05-01T00:00:00.000000Z",
            deleted_at = ""
        ),
        SubjectItem(
            id = 6,
            name = "Applications of Trigonometry",
            is_active = "Active",
            added_by = "",
            created_at = "2025-05-01T00:00:00.000000Z",
            updated_at = "2025-05-01T00:00:00.000000Z",
            deleted_at = ""
        )
    )

    // Course count by subject (for display purposes)
    val subjectCounts = mapOf(
        1 to 5,  // Basics of Trigonometry: 5 courses
        2 to 3,  // Advanced Trigonometry: 3 courses
        3 to 8,  // Trigonometric Formulas: 8 courses
        4 to 4,  // Trigonometric Functions: 4 courses
        5 to 6,  // Trigonometric Identities: 6 courses
        6 to 2   // Applications of Trigonometry: 2 courses
    )

    // Sample course data
    data class CourseData(
        val id: String,
        val title: String,
        val instructor: String,
        val rating: Float,
        val price: String,
        val subjectId: Int
    )

    val sampleCourses = listOf(
        CourseData("1", "Complete Trigonometry Course", "Dr. Smith", 4.8f, "₹1299", 1),
        CourseData("2", "Trigonometry for Engineering", "Prof. Johnson", 4.5f, "₹999", 2),
        CourseData("3", "Mastering Trigonometric Formulas", "Dr. Williams", 4.7f, "₹1499", 3),
        CourseData("4", "Trigonometry Applications in Physics", "Dr. Miller", 4.2f, "₹899", 6)
    )
}