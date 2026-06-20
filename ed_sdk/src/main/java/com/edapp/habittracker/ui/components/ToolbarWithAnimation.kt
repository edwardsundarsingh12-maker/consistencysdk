package com.edapp.habittracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edapp.habittracker.di.IconMapper
import com.edapp.habittracker.domain.HabitTag
import com.edapp.habittracker.util.CommonUtil
import com.edapp.habittracker.util.IconRepresentation
import com.edapp.habittracker.util.isDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarWithAnimation(
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit
) {

    val context = LocalContext.current
    TopAppBar(

        title = {
            Text(
                text = "My Toolbar",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            RotatingSettingsIcon(onClick = onSettingsClick)
        },
        actions = {
            RotatingAddIcon(Icons.Default.Add,onClick = onAddClick)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun HabitTagRow(
    tags: List<HabitTag>,
    selectedTags: List<Long>,
    onTagClick: (HabitTag?) -> Unit,
    onTagDelete: (HabitTag) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // State for delete confirmation


    val allOption = HabitTag(
        tagId = -1,
        title = "All",
        icon = "✨",
        colorValue = 0xFF888888
    )

    val selectedItems = tags.filter {
        selectedTags.contains(it.tagId)
    }

    val unSelectedItems = tags.filterNot {
        selectedTags.contains(it.tagId)
    }.sortedBy {
        it.title
    }

    val primary =
        MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
    ) {

        // SELECTED TAGS

        if (selectedItems.isNotEmpty()) {

            LazyRow(

                horizontalArrangement =
                    Arrangement.spacedBy(10.dp),

                contentPadding =
                    PaddingValues(horizontal = 12.dp)
            ) {

                items(
                    selectedItems,
                    key = { it.tagId }
                ) { tag ->

                    TagChip(

                        text = tag.title,

                        trailingIcon =
                            IconMapper.getIconByName(
                                tag.icon
                            ),

                        showClose = false,

                        onTagClick = {
                            onTagClick(tag)
                        },

                        onLongClickDelete = {
                            onTagDelete(tag)
                        },

                        background =
                            primary.copy(alpha = 0.14f),

                        borderColor =
                            primary.copy(alpha = 0.45f),

                        contentColor = primary,

                        useCustomDeleteDialog = false,

                        modifier = Modifier
                            .scale(1.08f)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }

        // UNSELECTED + ALL

        LazyRow(

            horizontalArrangement =
                Arrangement.spacedBy(10.dp),

            contentPadding =
                PaddingValues(horizontal = 12.dp)
        ) {

            // SHOW "ALL" ONLY WHEN TAGS SELECTED

            if (selectedItems.isNotEmpty()) {

                item {

                    TagChip(

                        text = allOption.title,

                        trailingIcon =
                            IconMapper.getIconByName(
                                allOption.icon
                            ),

                        showClose = false,

                        onTagClick = {
//                            onTagClick(null)
                        },

                        background =
                            Color(allOption.colorValue)
                                .copy(alpha = 0.12f),

                        borderColor =
                            Color(allOption.colorValue),

                        contentColor =
                            Color(allOption.colorValue)
                    )
                }
            }

            items(
                unSelectedItems,
                key = { it.tagId }
            ) { tag ->
                val baseColor = Color(tag.colorValue)
                TagChip(
                    text = tag.title,
                    trailingIcon = IconMapper.getIconByName(tag.icon),
                    showClose = false,
                    onTagClick = {
                        onTagClick(tag)
                    },
                    onLongClickDelete = {
                        onTagDelete(tag)
                    },
                    useCustomDeleteDialog = false,
                    background = baseColor.copy(alpha = 0.14f),
                    borderColor = baseColor,
                    contentColor = baseColor
                )
            }
        }
    }
}
@Composable
fun RotatingSettingsIcon(onClick: () -> Unit) {
    var rotated by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 360f else 0f,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "rotateAnim"
    )

    IconButton(
        onClick = {
            rotated = !rotated
            onClick()
        }
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            modifier = Modifier.rotate(rotation),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}


@Composable
fun RotatingAddIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.primary,
    buttonSize: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    onClick: () -> Unit
) {
    var clicked by remember { mutableStateOf(false) }
    var rotationTarget by remember { mutableStateOf(0f) }
    var shakeDirection by remember { mutableStateOf(0f) }

    // Animate rotation
    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
    )

    // Animate tint color
    val currentTint by animateColorAsState(
        targetValue = if (isError && clicked) Color.Red else tint,
        animationSpec = tween(200)
    )

    val scope = rememberCoroutineScope()

    IconButton(
        onClick = {
            clicked = true
            rotationTarget += 360f
            if (isError) {
                CommonUtil.haptic()
            } else {
                onClick()
            }
            scope.launch {
                delay(500L)
                clicked = false
            }
        },
        modifier = modifier.size(buttonSize)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Add",
            tint = currentTint,
            modifier = Modifier
                .size(iconSize)
                .rotate(rotation)
        )
    }
}

/**
 * Delete tag dialog with impact information
 * Shows that this tag will be removed from all associated habits
 */
@Composable
fun DeleteTagWithImpactDialog(
    tagName: String,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Tag",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Are you sure you want to delete the tag \"$tagName\"?",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Impact information box
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Impact of deletion:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "This tag will be removed from all habits associated with it.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "The removed habits will be considered as normal habits without any tag.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "This action cannot be undone.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = "Delete Tag",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}
