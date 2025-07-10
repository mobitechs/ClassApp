package com.mobitechs.classapp.screens.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobitechs.classapp.data.model.response.Course
import kotlinx.coroutines.delay

@Composable
fun PaymentFailureScreen(
    course: Course,
    errorMessage: String,
    errorCode: Int,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var showAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showAnimation = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error Animation/Icon
        AnimatedVisibility(
            visible = showAnimation,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(
                animationSpec = tween(500)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                modifier = Modifier.size(120.dp),
                tint = Color(0xFFF44336)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Error Title
        AnimatedVisibility(
            visible = showAnimation,
            enter = slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(
                    durationMillis = 600,
                    delayMillis = 200
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 600,
                    delayMillis = 200
                )
            )
        ) {
            Text(
                text = "Payment Failed",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF44336),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error Message
        AnimatedVisibility(
            visible = showAnimation,
            enter = slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(
                    durationMillis = 600,
                    delayMillis = 400
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 600,
                    delayMillis = 400
                )
            )
        ) {
            Text(
                text = getErrorMessage(errorMessage),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Error Details Card
        AnimatedVisibility(
            visible = showAnimation,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(
                    durationMillis = 600,
                    delayMillis = 600
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 600,
                    delayMillis = 600
                )
            )
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Transaction Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PaymentDetailRow(
                        label = "Course:",
                        value = course.course_name
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PaymentDetailRow(
                        label = "Amount:",
                        value = "₹${course.course_discounted_price}"
                    )

                    if (errorCode != 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        PaymentDetailRow(
                            label = "Error Code:",
                            value = errorCode.toString(),
                            valueColor = Color(0xFFF44336)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Action Buttons
        AnimatedVisibility(
            visible = showAnimation,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(
                    durationMillis = 600,
                    delayMillis = 800
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 600,
                    delayMillis = 800
                )
            )
        ) {
            Column {
                Button(
                    onClick = onRetryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Try Again",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Back to Course",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}