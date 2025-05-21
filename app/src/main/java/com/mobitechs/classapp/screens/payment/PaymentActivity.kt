package com.mobitechs.classapp.screens.payment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.data.model.response.Student
import com.mobitechs.classapp.screens.store.CourseDetailViewModel
import com.mobitechs.classapp.ui.theme.ClassConnectTheme
import com.mobitechs.classapp.utils.Constants
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentActivity : ComponentActivity(), PaymentResultListener {

    private lateinit var course: Course
    private lateinit var user: Student
    private lateinit var viewModel: CourseDetailViewModel

    private var paymentResultReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_payment)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

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



        setContent {
            // Use your app theme
            ClassConnectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Processing Payment...",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }

        // Initialize Razorpay and start payment
        initializePayment()
    }




    private fun initializePayment() {
        val currency = "INR"
        val orderId = createOrderId()

        try {
            val checkout = Checkout()
            checkout.setKeyID(Constants.RAZORPAY_KEY_ID)

            val options = JSONObject()
            options.put("name", "Class Connect")
            options.put("description", "Payment for ${course.course_name}")
            options.put("order_id", orderId)
            options.put("currency", currency)

            // Calculate amount based on whether discounted price exists
            val price = if (course.course_discounted_price != null) {
                course.course_discounted_price.toDouble()
            } else {
                course.course_price.toDouble()
            }

            options.put("amount", (50 * 100).toInt()) // Convert to smallest currency unit

            val prefill = JSONObject()
            prefill.put("email", user.email)
            prefill.put("contact", user.phone)
            options.put("prefill", prefill)

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Payment Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun createOrderId(): String {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val currentDate = sdf.format(Date())
        return "ORDER_$currentDate"
    }

    override fun onPaymentSuccess(razorpayPaymentId: String) {
        // Handle successful payment
        Toast.makeText(this, "Payment Successful: $razorpayPaymentId", Toast.LENGTH_LONG).show()

        // You could send a broadcast or set a result to communicate back to your app
        val intent = Intent("PAYMENT_SUCCESS")
        intent.putExtra("PAYMENT_ID", razorpayPaymentId)
        intent.putExtra("COURSE_ID", course.id)
        sendBroadcast(intent)

        // Update the course status on your server
        updateCourseStatus(razorpayPaymentId)

        // Return to previous screen
        finish()
    }

    override fun onPaymentError(code: Int, description: String) {
        // Handle payment failure
        Toast.makeText(this, "Payment Failed: $description", Toast.LENGTH_LONG).show()

        // You could send a broadcast for error handling
        val intent = Intent("PAYMENT_FAILURE")
        intent.putExtra("ERROR_CODE", code)
        intent.putExtra("ERROR_DESC", description)
        sendBroadcast(intent)

        // Return to previous screen
        finish()
    }

    private fun updateCourseStatus(paymentId: String) {
        // Implement API call to update course status on your server
        // This will depend on your backend implementation
        // Example using a ViewModel:

        viewModel.updatePurchaseStatus(
            courseId = course.id.toString(),
            paymentId = paymentId,
            onSuccess = {
                // Handle success
            },
            onError = { errorMessage ->
                // Handle error
            }
        )

    }


}