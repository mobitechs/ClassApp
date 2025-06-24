package com.mobitechs.classapp.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

object AppTheme {
    val cardColors: CardColors
        @Composable
        get() = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )

    val primaryButtonColors: ButtonColors
        @Composable
        get() = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )

    val textFieldColors: TextFieldColors
        @Composable
        get() = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        )

    @OptIn(ExperimentalMaterial3Api::class)
    val topAppBarColors: TopAppBarColors
        @Composable
        get() = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            scrolledContainerColor = MaterialTheme.colorScheme.primary, // When scrolled
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
}