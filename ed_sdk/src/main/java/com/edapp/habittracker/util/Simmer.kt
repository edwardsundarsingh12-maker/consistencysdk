package com.edapp.habittracker.util
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer

@Composable
fun HabitGridShimmer(
    bgColor: Color,
    padding: PaddingValues = PaddingValues(16.dp)) {
    LazyVerticalGrid(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        columns = GridCells.Adaptive(140.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(12) { // number of shimmer cards
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .padding(8.dp),
            ) {
                // 🟦 Title shimmer
                InstantShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))

                )

                Spacer(Modifier.height(8.dp))

                // 🟨 Description shimmer
                InstantShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))

                )

                Spacer(Modifier.height(16.dp))

                // 🟩 MonthConsistencyCompose grid shimmer
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    repeat(5) { week ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            repeat(7) {
                                InstantShimmerBox(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))

                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // 🟧 Month label shimmer
                    InstantShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))

                    )
                }
            }
        }
    }
}

@Composable
fun HabitGridShimmerFullWidth(
    bgColor: Color,
    padding: PaddingValues = PaddingValues(16.dp)
) {
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(12.dp),
    ) {
        items(12) { // number of shimmer cards
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .padding(8.dp),
            ) {
                // 🟦 Title shimmer
                InstantShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))

                )

                Spacer(Modifier.height(8.dp))

                // 🟨 Description shimmer
                InstantShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))

                )

                Spacer(Modifier.height(16.dp))

                // 🟩 MonthConsistencyCompose grid shimmer
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    repeat(5) { week ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            repeat(15) {
                                InstantShimmerBox(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))

                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // 🟧 Month label shimmer
                    InstantShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))

                    )
                }
            }
        }
    }
}


@Composable
fun InstantShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "shimmerAnim"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.Gray.copy(alpha = 0.3f),
            Color.LightGray.copy(alpha = 0.6f)
        ),
        start = Offset(x = shimmerTranslateAnim - 200f, y = 0f),
        end = Offset(x = shimmerTranslateAnim, y = 0f)
    )

    Box(
        modifier = modifier
            .background(brush, shape)
    )
}


@Composable
fun FullScreenLoader(
    color: Color,
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Circular Loader
            CircularProgressIndicator(
                color = color,
                strokeWidth = 4.dp,
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Loading Text
            Text(
                text = message,
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
