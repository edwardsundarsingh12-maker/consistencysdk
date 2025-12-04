package com.edapp.habittracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.edapp.habittracker.di.IconMapper
import com.edapp.habittracker.util.IconRepresentation
import com.edapp.habittracker.util.darken
import com.edapp.habittracker.util.isDarkTheme
import com.edapp.habittracker.util.lighten

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedIconTextField(
    label: String,
    placeholder: String,
    value: TextFieldValue,
    showIcon: Boolean = true,
    onValueChange: (TextFieldValue) -> Unit,
    selectedIconName: String?,
    onIconClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    // 🌀 Bounce + Glow animation
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
        finishedListener = { isPressed = false }
    )

    val context = LocalContext.current

    val glowColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else Color.Transparent,
        animationSpec = spring(dampingRatio = 0.5f)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp), // Fixed height for row look
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        val edittextBg = if (context.isDarkTheme()) {
            MaterialTheme.colorScheme.background.lighten(0.05f)
        } else {
            MaterialTheme.colorScheme.background.darken(0.95f)
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(glowColor, edittextBg, glowColor)
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🎨 Icon area
            if (showIcon){
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .fillMaxHeight()
                        .clickable {
                            isPressed = true
                            onIconClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val icon =
                        IconMapper.getIconByName(selectedIconName.toString()) ?: Icons.Default.Edit
                    Surface(
                        shape = CircleShape,
                        color =  MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        if (icon is IconRepresentation.Vector) {
                            Icon(
                                imageVector = icon.icon,
                                contentDescription = "$label icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .scale(scaleAnim)
                            )
                        } else if (icon is IconRepresentation.Emoji) {
                            Text(
                                text = icon.value,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .scale(scaleAnim)
                            )
                        }
                    }
                }

                // Divider line
                Box(
                    Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
            }
            // ✏️ Text field
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
    }
}
