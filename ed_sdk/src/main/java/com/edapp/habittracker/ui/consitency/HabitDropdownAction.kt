package com.edapp.habittracker.ui.consitency

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

sealed class HabitDropdownAction(
    val title: String,
    val icon: ImageVector,
    val tint: Color? = null
) {

    data object Edit : HabitDropdownAction(
        title = "Edit",
        icon = Icons.Outlined.Edit
    )

    data object Delete : HabitDropdownAction(
        title = "Delete",
        icon = Icons.Outlined.Delete,
        tint = Color.Red
    )

    data object Archive : HabitDropdownAction(
        title = "Archive",
        icon = Icons.Outlined.Archive
    )

    data object Unarchive : HabitDropdownAction(
        title = "Unarchive",
        icon = Icons.Outlined.Unarchive
    )

    data object Lock : HabitDropdownAction(
        title = "Lock",
        icon = Icons.Outlined.Lock
    )

    data object Unlock : HabitDropdownAction(
        title = "Unlock",
        icon = Icons.Outlined.LockOpen
    )

    data object AddAsWidget : HabitDropdownAction(
        title = "Add as Widget",
        icon = Icons.Outlined.Widgets
    )
}
@Composable
fun HabitDropdownMenu(
    expanded: Boolean,
    actions: List<HabitDropdownAction>,
    offset: DpOffset,
    onDismiss: () -> Unit,
    onActionClick: (HabitDropdownAction) -> Unit
) {

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var pendingDeleteAction by remember {
        mutableStateOf<HabitDropdownAction?>(null)
    }

    DropdownMenu(
        offset = offset,
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {

        actions.forEachIndexed { index, action ->

            val tint =
                action.tint
                    ?: MaterialTheme.colorScheme.onSurface

            DropdownMenuItem(

                text = {

                    Row {

                        Text(
                            text = action.title,
                            color = tint
                        )
                    }
                },

                leadingIcon = {

                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.title,
                        tint = tint
                    )
                },

                onClick = {

                    if (action is HabitDropdownAction.Delete) {

                        pendingDeleteAction = action
                        showDeleteDialog = true

                    } else {

                        onActionClick(action)
                    }

                    onDismiss()
                }
            )

            if (index != actions.lastIndex) {

                HorizontalDivider(
                    modifier = Modifier.padding(
                        horizontal = 12.dp
                    ),

                    color = MaterialTheme
                        .colorScheme
                        .outline
                        .copy(alpha = 0.08f)
                )
            }
        }
    }

    // DELETE CONFIRMATION DIALOG

    if (showDeleteDialog) {

        AlertDialog(

            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {

                Text(
                    text = "Delete Habit"
                )
            },

            text = {

                Text(
                    text =
                        "This will be deleted permanently. Are you sure?"
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        pendingDeleteAction?.let {
                            onActionClick(it)
                        }

                        showDeleteDialog = false
                    }

                ) {

                    Text(
                        text = "Confirm",
                        color = Color.Red
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {
                        showDeleteDialog = false
                    }

                ) {

                    Text("Cancel")
                }
            }
        )
    }
}
