package com.edapp.habittracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edapp.habittracker.util.IconRepresentation

/**
 * A rounded tag chip with optional close icon and full-chip click support.
 *
 * @param text Label text
 * @param trailingIcon Optional icon shown after the text (before close icon)
 * @param showClose Whether to show the close icon
 * @param onClose Callback when the close icon is clicked (if visible)
 * @param onTagClick Callback when the chip itself is clicked
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
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = Color.Gray,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
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
                .clickable(onClick = onTagClick)
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