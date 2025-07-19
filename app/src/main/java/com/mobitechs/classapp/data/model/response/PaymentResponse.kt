package com.mobitechs.classapp.data.model.response

data class PaymentStatusResponse(
    val details: PaymentStatusDetails,
    val payment_id: String,
    val payment_status: String,
    val status: Boolean,
    val status_code: Int
)

data class PaymentStatusDetails(
    val acquirer_data: AcquirerData,
    val amount: Int,
    val amount_refunded: Int,
    val bank: Any,
    val captured: Boolean,
    val card_id: Any,
    val contact: String,
    val created_at: Int,
    val currency: String,
    val description: String,
    val email: String,
    val entity: String,
    val error_code: Any,
    val error_description: Any,
    val error_reason: Any,
    val error_source: Any,
    val error_step: Any,
    val fee: Any,
    val id: String,
    val international: Boolean,
    val invoice_id: Any,
    val method: String,
    val notes: List<Any>,
    val order_id: Any,
    val refund_status: Any,
    val status: String,
    val tax: Any,
    val upi: Upi,
    val vpa: String,
    val wallet: Any
)

data class AcquirerData(
    val rrn: String,
    val upi_transaction_id: String
)

data class Upi(
    val vpa: String
)


data class PaymentListResponse(
    val payments: List<Payment>,
    val status: Boolean,
    val status_code: Int
)

data class Payment(
    val amount: Int,
    val course_id: Int,
    val id: Int,
    val razorpay_payment_id: String,
    val status: String,
    val student_id: Int,
    val created_at: String?,
    val name: String?, // Fixed: was Any, should be String?
    val course_image: String?, // Added missing field
)