package com.mobitechs.classapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mobitechs.classapp.data.model.response.OfferBanner
import java.util.Date


data class MyBatchResponse(
    val message: String,
    val batches: List<BatchItem>,
    val status: Boolean,
    val status_code: Int
)


data class BatchItem(
    val batche_id: String,
    val batche_name: String,
    val batche_code: String,
    val subject: String,
    val start_date: String,
    val enrollment_status: String,
    val coverImage: String?,
    val totalStudents: String?

)

