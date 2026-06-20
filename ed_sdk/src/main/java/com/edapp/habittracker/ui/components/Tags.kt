package com.edapp.habittracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edapp.habittracker.util.IconRepresentation

/**
 * A rounded tag chip with optional close icon and full-chip click support.
 * Long-press to trigger a delete confirmation dialog.
 *
 * @param text Label text
 * @param trailingIcon Optional icon shown after the text (before close icon)
 * @param showClose Whether to show the close icon
 * @param onClose Callback when the close icon is clicked (if visible)
 * @param onTagClick Callback when the chip itself is clicked
 * @param onLongClickDelete Callback when user confirms deletion via long-press dialog
 * @param modifier Modifier for customization
 * @param background Background color
 * @param borderColor 1.dp border color
 * @param contentColor Text and icon color
 */
@Composable
fun TagChip(
    text: String,
    trailingIcon: IconRepresentation? = null,
    showClose: Boolean = true,
    onClose: () -> Unit = {},
    onTagClick: () -> Unit = {},
    onLongClickDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = Color.Gray,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    useCustomDeleteDialog: Boolean = false
) {
    val showDeleteDialog = remember { mutableStateOf(false) }

    if (showDeleteDialog.value) {
        DeleteTagWithImpactDialogFromChip(
            tagName = text,
            onConfirm = {
                showDeleteDialog.value = false
                onLongClickDelete()
            },
            onDismiss = {
                showDeleteDialog.value = false
            }
        )
    }

    Surface(
        modifier = modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        contentColor = contentColor,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = onTagClick,
                    onLongClick = {
                        showDeleteDialog.value = true
                    }
                )
                .background(background)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Optional trailing icon (after text)
            if (trailingIcon != null) {
                if (trailingIcon is IconRepresentation.Vector){
                    Icon(
                        imageVector = trailingIcon.icon,
                        contentDescription = "tag icon",
                        modifier = Modifier.size(18.dp)
                    )
                } else if (trailingIcon is IconRepresentation.Emoji) {
                    Text(
                        text = trailingIcon.value,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Text label
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp
            )

            // Optional close button (on the far right)
            if (showClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "close icon",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onClose)
                )
            }
        }
    }
}

@Composable
fun DeleteTagWithImpactDialogFromChip(
    tagName: String,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(
                        modifier = Modifier.padding(8.dp),
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                Text(
                    text = "Delete Tag",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Are you sure you want to delete \"$tagName\"?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                    )
                ) {

                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {

                            Text(
                                text = "Impact of deletion",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error
                            )

                            Text(
                                text = "• This tag will be removed from all associated habits.",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = "• Those habits will remain active as regular habits.",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = "• This action cannot be undone.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },

        confirmButton = {
            FilledTonalButton(
                onClick = onConfirm,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {

                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}
