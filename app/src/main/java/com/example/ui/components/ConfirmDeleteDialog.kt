package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.ui.util.LocalAppStrings

/**
 * Step 3 Reusable Confirmation Dialog before every deletion or destructive operation.
 * Theme-aware colors, red confirm button, Cancel button, all text from AppStrings.
 */
@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    confirmText: String = LocalAppStrings.current.delete,
    cancelText: String = LocalAppStrings.current.cancel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text(
                    text = confirmText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_button")
            ) {
                Text(
                    text = cancelText,
                    color = TextSecondary
                )
            }
        },
        containerColor = EmeraldSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.testTag("confirm_delete_dialog")
    )
}
