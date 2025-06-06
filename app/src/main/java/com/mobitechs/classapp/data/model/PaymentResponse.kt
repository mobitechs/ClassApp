package com.mobitechs.classapp.data.model


data class PaymentResponse(
    val statusCode: Int = 0,
    val status: String = "",
    val message: String = "",
    val orderId: String = "",
    val currency: String = "",
    val amount: String = "",
    val data: List<PaymentItem>?
)

data class PaymentItem(
    val id: String,
    val name: String,
    val isRead: Boolean = false,
    val icon: String? = null,
    val parentId: String? = null
)


data class PaymentData(
    val orderId: String,
    val amount: String,
    val currency: String
)