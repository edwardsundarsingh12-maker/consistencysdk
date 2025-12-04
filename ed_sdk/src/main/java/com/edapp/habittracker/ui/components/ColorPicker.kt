package com.edapp.habittracker.ui.components


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun FullRoundedHSVColorPicker(
    initialColor: Color = Color.Red,
    onColorSelected: (Color) -> Unit,
    onDone: () -> Unit
) {
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(1f) }
    var value by remember { mutableStateOf(1f) }
    var selectorPos by remember { mutableStateOf(Offset.Zero) }
    var selectedColor by remember { mutableStateOf(initialColor) }

    // Update selected color whenever HSV changes
    LaunchedEffect(hue, saturation, value) {
        selectedColor = Color.hsv(hue, saturation, value)
        onColorSelected(selectedColor)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // -------- Saturation-Value Box --------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Canvas(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val x = change.position.x.coerceIn(0f, size.width.toFloat())
                        val y = change.position.y.coerceIn(0f, size.height.toFloat())
                        selectorPos = Offset(x, y)

                        saturation = (x / size.width).coerceIn(0f, 1f)
                        value = 1f - (y / size.height).coerceIn(0f, 1f)
                    }
                }
            ) {
                val radius = 16.dp.toPx()

                // Saturation gradient
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.White, Color.hsv(hue, 1f, 1f))
                    ),
                    size = size,
                    cornerRadius = CornerRadius(radius, radius)
                )

                // Value gradient
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black)
                    ),
                    size = size,
                    cornerRadius = CornerRadius(radius, radius)
                )

                // Selector circle
                drawCircle(
                    color = Color.White,
                    radius = 10.dp.toPx(),
                    center = selectorPos,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -------- Hue Slider --------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val x = change.position.x.coerceIn(0f, size.width.toFloat())
                        hue = (x / size.width * 360f).coerceIn(0f, 360f)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = 16.dp.toPx()
                val steps = 360
                val widthStep = size.width / steps
                for (i in 0..steps) {
                    drawRoundRect(
                        color = Color.hsv(i.toFloat(), 1f, 1f),
                        topLeft = Offset(i * widthStep, 0f),
                        size = Size(widthStep + 1, size.height),
                        cornerRadius = CornerRadius(radius, radius)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -------- Selected Color & Done Button --------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(selectedColor, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )

            Button(onClick = { onDone() }) {
                Text("Done")
            }
        }
    }
}
