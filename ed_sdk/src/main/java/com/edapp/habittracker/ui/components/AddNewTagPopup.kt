package com.edapp.habittracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.edapp.habittracker.di.IconMapper
import com.edapp.habittracker.domain.HabitTag
import com.edapp.habittracker.ui.HabitViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AddNewTagPopup(
    viewModel: HabitViewModel,
    cardTitle: String = "",
    onDismiss: () -> Unit,
    onSave: (newHabitTag: HabitTag) -> Unit
) {
    var titleValue by remember { mutableStateOf(TextFieldValue("")) }
    var selectedIcon by remember { mutableStateOf("FontAwesome.Readme") }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false // allows custom width
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.85f),
                exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.9f)
            ) {
                Surface (
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .widthIn(min = 300.dp, max = 400.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TitleEdittext(
                            titleValue = titleValue,
                            selectedIconName = selectedIcon,
                            onTitleTextChanged = { titleValue = it },
                            onSelectedIconChanged = { selectedIcon = it }
                        )

                        Spacer(Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    val newHabitTag = HabitTag(
                                        tagId = System.currentTimeMillis(),
                                        title = titleValue.text,
                                        icon = selectedIcon,
                                        colorValue = -1
                                    )
                                    onSave(newHabitTag)
                                },
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun TitleEdittext(
    titleValue: TextFieldValue,
    selectedIconName: String,
    onTitleTextChanged: (TextFieldValue) -> Unit,
    onSelectedIconChanged: (String) -> Unit
) {

    var showPicker by remember { mutableStateOf(false) }

    // 🔹 Title field with icon picker
    AnimatedIconTextField(
        label = "Title",
        placeholder = "Enter habit name",
        value = titleValue,
        onValueChange = { onTitleTextChanged(it) },
        selectedIconName = selectedIconName,
        onIconClick = { showPicker = true }
    )

    if (showPicker) {
        IconPickerBottomSheet(
            showSheet = showPicker,
            selectedName = selectedIconName,
            icons = IconMapper.allIconsList,
            onDismiss = { showPicker = false },
            onSelect = {
                onSelectedIconChanged(it)
                showPicker = false
            }
        )
    }

}
