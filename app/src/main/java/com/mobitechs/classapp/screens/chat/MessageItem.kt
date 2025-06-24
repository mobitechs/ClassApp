package com.mobitechs.classapp.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mobitechs.classapp.data.model.response.ChatMessage
import com.mobitechs.classapp.utils.formatTimestamp
import java.io.File

@Composable
fun MessageItem(
    message: ChatMessage,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        when (message.messageType) {
            "IMAGE" -> {
                PhotoMessageContent(
                    message = message,
                    isCurrentUser = isCurrentUser
                )
            }

            else -> {
                TextMessageContent(
                    message = message,
                    isCurrentUser = isCurrentUser
                )
            }
        }
    }
}

@Composable
private fun TextMessageContent(
    message: ChatMessage,
    isCurrentUser: Boolean
) {
    Column(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .background(
                color = if (isCurrentUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = message.content,
            color = if (isCurrentUser)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatTimestamp(message.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = if (isCurrentUser)
                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun PhotoMessageContent(
    message: ChatMessage,
    isCurrentUser: Boolean
) {
    var showFullScreen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .widthIn(max = 200.dp)
            .background(
                color = if (isCurrentUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(4.dp)
    ) {
        message.mediaUrl?.let { path ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(path))
                    .crossfade(true)
                    .build(),
                contentDescription = "Photo message",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp, max = 300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showFullScreen = true },
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = formatTimestamp(message.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = if (isCurrentUser)
                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }

    // TODO: Add full screen photo viewer
    if (showFullScreen) {
        // Implement full screen photo viewer dialog
    }
}