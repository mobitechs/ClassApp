package com.mobitechs.classapp.screens.payment

import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.mobitechs.classapp.ClassConnectApp
import com.mobitechs.classapp.ViewModelFactory
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.Student
import com.mobitechs.classapp.ui.theme.ClassConnectTheme
import com.mobitechs.classapp.utils.Constants
import com.mobitechs.classapp.utils.showToast
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Payment States
sealed class PaymentState {
    object Processing : PaymentState()
    data class Success(val paymentId: String) : PaymentState()
    data class Failed(val errorMessage: String, val errorCode: Int = 0) : PaymentState()
}

class PaymentActivity : ComponentActivity(), PaymentResultListener {

    private lateinit var course: Course
    private lateinit var user: Student
    private lateinit var viewModel: PaymentViewModel

    // State management
    private var paymentState by mutableStateOf<PaymentState>(PaymentState.Processing)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val courseJson = intent.getStringExtra("COURSE_DATA")
        val userJson = intent.getStringExtra("USER_DATA")

        if (courseJson == null || userJson == null) {
            Toast.makeText(this, "Error: Missing data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val gson = Gson()
        course = gson.fromJson(courseJson, Course::class.java)
        user = gson.fromJson(userJson, Student::class.java)

        initializeViewModel()

        setContent {
            ClassConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (paymentState) {
                        is PaymentState.Processing -> {
                            PaymentProcessingScreen()
                        }
                        is PaymentState.Success -> {
                            PaymentSuccessScreen(
                                course = course,
                                paymentId = (paymentState as PaymentState.Success).paymentId,
                                onContinueClick = { navigateToCourseDetails() },
                                onHomeClick = { navigateToHome() }
                            )
                        }
                        is PaymentState.Failed -> {
                            PaymentFailureScreen(
                                course = course,
                                errorMessage = (paymentState as PaymentState.Failed).errorMessage,
                                errorCode = (paymentState as PaymentState.Failed).errorCode,
                                onRetryClick = { retryPayment() },
                                onBackClick = { navigateBack() }
                            )
                        }
                    }
                }
            }
        }

        // Initialize Razorpay and start payment
        initializePayment()
    }

    private fun initializeViewModel() {
        val app = application as ClassConnectApp
        val viewModelFactory = ViewModelFactory(
            applicationContext,
            app.authRepository,
            app.userRepository,
            app.courseRepository,
            app.batchRepository,
            app.categoryRepository,
            app.notificationRepository,
            app.paymentRepository,
            app.freeContentRepository,
            app.myDownloadsRepository,
            app.searchRepository,
            app.policyTermConditionRepository,
            app.chatUserRepository,
            app.chatRepository,
            app.messageRepository,
            app.themeRepository,
            app.profileRepository
        )

        viewModel = ViewModelProvider(this, viewModelFactory)[PaymentViewModel::class.java]
    }

    private fun initializePayment() {
        val checkout = Checkout()
        checkout.setKeyID(Constants.RAZORPAY_KEY_ID)

        try {
            val options = JSONObject()
            val amountInPaise = (course.course_discounted_price.toFloat() * 100).toInt()

            val orderId = createOrderId()

            options.put("name", "ClassConnect")
            options.put("description", course.course_name)
            options.put("send_sms_hash", true)
            options.put("allow_rotation", true)
            options.put("image", course.image)
            options.put("currency", "INR")
            options.put("amount", amountInPaise.toString())

            val preFill = JSONObject()
            preFill.put("email", user.email)
            preFill.put("contact", user.phone)
            options.put("prefill", preFill)

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 3)
            options.put("retry", retryObj)
            options.put("timeout", 300)

            checkout.open(this, options)

        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            paymentState = PaymentState.Failed(
                errorMessage = e.message ?: "Payment initialization failed"
            )
        }
    }

    private fun createOrderId(): String {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val currentDate = sdf.format(Date())
        return "ORDER_$currentDate"
    }

    override fun onPaymentSuccess(razorpayPaymentId: String) {
        // Update state to success
        paymentState = PaymentState.Success(razorpayPaymentId)

        // Update course status
        updateCourseStatus(razorpayPaymentId)

    }

    override fun onPaymentError(code: Int, description: String) {
        var errorMessage = description

        try {
            val jsonResponse = JSONObject(description)
            val error = jsonResponse.getJSONObject("error")
            val reason = error.getString("reason")

            errorMessage = when (reason) {
                "payment_cancelled" -> "Payment was cancelled by user"
                "payment_timeout" -> "Payment session timed out"
                "insufficient_balance" -> "Insufficient balance in account"
                "network_error" -> "Network connection error"
                else -> reason
            }
        } catch (e: Exception) {
            // Use original description if JSON parsing fails
        }

        // Update state to failed
        paymentState = PaymentState.Failed(
            errorMessage = errorMessage,
            errorCode = code
        )


    }

    private fun retryPayment() {
        paymentState = PaymentState.Processing
        initializePayment()
    }

    private fun navigateToCourseDetails() {
        val intent = Intent().apply {
            putExtra("PAYMENT_SUCCESS", true)
            putExtra("COURSE_ID", course.id)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent().apply {
            putExtra("NAVIGATE_TO_HOME", true)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun navigateBack() {
        finish()
    }

    private fun updateCourseStatus(paymentId: String) {

        viewModel.updatePurchaseStatus(
            courseId = course.id.toString(),
            paymentId = paymentId,
            onSuccess = {
                // Handle success if needed
                println("✅ Payment status updated successfully")
                showToast(this, "Course purchased successfully!")
            },
            onError = { errorMessage ->
                // Handle error if needed
                println("❌ Failed to update payment status: $errorMessage")
                showToast(this, "Failed to update payment status: $errorMessage")
                // Note: Don't show error UI here as payment was successful
                // Maybe just log it for debugging
            }
        )
    }
}






@Composable
fun PaymentDetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = valueStyle,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

 fun getErrorMessage(errorMessage: String): String {
    return when {
        errorMessage.contains("cancelled", ignoreCase = true) ->
            "Payment was cancelled. Don't worry, no amount has been deducted from your account."

        errorMessage.contains("timeout", ignoreCase = true) ->
            "Payment session timed out. Please try again with a stable internet connection."

        errorMessage.contains("insufficient", ignoreCase = true) ->
            "Transaction failed due to insufficient balance. Please check your account balance and try again."

        errorMessage.contains("network", ignoreCase = true) ->
            "Network error occurred. Please check your internet connection and try again."

        else ->
            "We encountered an issue while processing your payment. Please try again or contact support if the issue persists."
    }
}