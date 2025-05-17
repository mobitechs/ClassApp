package com.mobitechs.classapp.screens.common


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun OtpTimer(
    initialTimeInSeconds: Int,
    onResendClick: () -> Unit,
    modifier: Modifier = Modifier,
    isResendEnabled: Boolean = true
) {
    var timeRemaining by remember { mutableStateOf(initialTimeInSeconds) }
    var canResend by remember { mutableStateOf(false) }

    LaunchedEffect(initialTimeInSeconds) {
        timeRemaining = initialTimeInSeconds
        canResend = false

        while (timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        }

        canResend = true
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (timeRemaining > 0) {
            Text(
                text = "Resend code in ${formatTime(timeRemaining)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        } else {
            Text(
                text = "Didn't receive code?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.width(4.dp))

            TextButton(
                onClick = onResendClick,
                enabled = canResend && isResendEnabled
            ) {
                Text(text = "Resend")
            }
        }
    }
}

// Helper function to format time (MM:SS)
private fun formatTime(timeInSeconds: Int): String {
    val minutes = timeInSeconds / 60
    val seconds = timeInSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}