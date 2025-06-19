package com.mobitechs.classapp.screens.chat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun ChatInput(
    onSendMessage: (String) -> Unit,
    enabled: Boolean = true
) {
    var message by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                singleLine = true,
                enabled = enabled,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (message.isNotBlank() && enabled) {
                            onSendMessage(message)
                            message = ""
                        }
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
            IconButton(
                onClick = {
                    if (message.isNotBlank() && enabled) {
                        onSendMessage(message)
                        message = ""
                    }
                },
                enabled = enabled && message.isNotBlank()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (enabled && message.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Send
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun ChatInput(
//    onSendMessage: (String) -> Unit
//) {
//    var message by remember { mutableStateOf("") }
//
//    Surface(
//        modifier = Modifier.fillMaxWidth(),
//        shadowElevation = 8.dp
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            TextField(
//                value = message,
//                onValueChange = { message = it },
//                modifier = Modifier.weight(1f),
//                placeholder = { Text("Type a message...") },
//                singleLine = true,
//                colors = TextFieldDefaults.colors(
//                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
//                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
//                )
//            )
//            IconButton(
//                onClick = {
//                    if (message.isNotBlank()) {
//                        onSendMessage(message)
//                        message = ""
//                    }
//                }
//            ) {
//                Icon(
//                    Icons.Default.Send,
//                    contentDescription = "Send",
//                    tint = MaterialTheme.colorScheme.primary
//                )
//            }
//        }
//    }
//}