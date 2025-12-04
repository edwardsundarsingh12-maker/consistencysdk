package com.edapp.habittracker.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

import androidx.compose.ui.unit.dp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.edapp.habittracker.di.IconMapper
import com.edapp.habittracker.ui.components.IconPickerBottomSheet
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import com.edapp.habittracker.util.darken
import com.edapp.habittracker.util.isDarkTheme
import com.edapp.habittracker.util.lighten
import kotlinx.coroutines.delay
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.edapp.habittracker.ui.components.AnimatedIconTextField
import com.edapp.habittracker.ui.components.*
import com.edapp.habittracker.ui.components.AnimatedToggleIcon
import com.edapp.habittracker.ui.components.FullRoundedHSVColorPicker
import com.edapp.habittracker.ui.consitency.PreviewConsistencyGraph
import com.edapp.habittracker.ui.consitency.SampleConsistencyIcons
import com.edapp.habittracker.util.IconRepresentation


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewHabit(
    viewModel: HabitViewModel,
    navController: NavHostController
) {

    var isBadHabit by remember { mutableStateOf(false) }

    var selectedIcon by remember { mutableStateOf<String?>(null) }
    var selectedConsistencyIcon by remember { mutableStateOf<String?>(null) }
    var selectedColor by remember { mutableStateOf(Color(0xFF3B9CF6)) }
//    var uncheckedColorValue by remember { mutableStateOf(Color.Transparent) }
    var showIconPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showReminderPopup by remember { mutableStateOf(false) }
    val context = LocalContext.current


    var titleValue by remember { mutableStateOf(TextFieldValue("")) }
    var descriptionValue by remember { mutableStateOf(TextFieldValue("")) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New ") },
                navigationIcon = {
                    RotatingAddIcon(Icons.Default.ArrowBack){
                        navController.popBackStack()
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (context.isDarkTheme()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.updateHabitEditOrNewData(
                        selectedHabitConsistencyIcon = selectedConsistencyIcon,
                        selectedHabitIcon = selectedIcon,
                        color = selectedColor,
                        title = titleValue.text,
                        description = descriptionValue.text,
                        uncheckedColorValue = selectedColor.copy(alpha = 0.05f)
                    )
                    viewModel.saveHabit()
                    navController.popBackStack()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            IconAndHSVColorPicker(
                selectedIconName = selectedConsistencyIcon,
                selectedColor = selectedColor,
                onSelectedIconChanged = {
                    // Open your IconPickerBottomSheet
                    selectedConsistencyIcon = it
                },
                onColorSelected = {
                    selectedColor = it
                }
            )

            ReminderType(
                habitViewModel = viewModel,
                isBadHabit = isBadHabit
            ) {
                isBadHabit = it
            }

            AllListedTagChips(viewModel)

            TitleEdittext(
                titleValue = titleValue,
                selectedIconName = selectedIcon ?: "",
                onTitleTextChanged = {
                    titleValue = it
                },
                onSelectedIconChanged = {
                    selectedIcon = it
                }
            )

            DescriptionEdittext(
                descriptionValue = descriptionValue,
                onDescriptionTextChanged = {
                    descriptionValue = it
                }
            )

            AddReminder(habitViewModel = viewModel){
                showReminderPopup = true
            }

        }

        if (showReminderPopup) {
            ReminderPopup(
                viewModel = viewModel,
                onSave = { reminder->
                    viewModel.addReminder(reminder)
                    showReminderPopup = false
                },
                onDismiss = {showReminderPopup = false}
            )
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescriptionEdittext(
    descriptionValue: TextFieldValue,
    onDescriptionTextChanged: (TextFieldValue) -> Unit,
) {

    AnimatedIconTextField(
        label = "Description",
        placeholder = "Add a short description…",
        showIcon = false,
        value = descriptionValue,
        onValueChange = { onDescriptionTextChanged(it) },
        selectedIconName = null,
        onIconClick = {},
    )


    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun ReminderType(
    habitViewModel: HabitViewModel,
    isBadHabit: Boolean,
    toggleToBad:(Boolean) -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = "Enable to track and reduce bad Habit",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 3,
            modifier = Modifier.weight(1f) // THIS makes the Text take remaining space and wrap

        )

        Column(
            modifier = Modifier.width(70.dp),
            verticalArrangement = Arrangement.Center, // center content vertically
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedToggleIcon(
                initialState = isBadHabit,
                activeColor = Color(0xFFBE0000),
                onToggle = { newState -> toggleToBad(isBadHabit) }
            )
        }


    }
}

@Composable
fun AllListedTagChips(
    viewModel: HabitViewModel
) {
    val allHabitTag = viewModel.allTags.collectAsState().value
    val selectedTag = viewModel.editOrAddHabit.collectAsState().value.tagIds

    var showPopup by remember {
        mutableStateOf(false)
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        allHabitTag.forEach { tag ->
            val isTagSelected = selectedTag.contains(tag.tagId)
            TagChip(
                tag.title,
                IconMapper.getIconByName(tag.icon),
                isTagSelected,
                background = if (isTagSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.0f) ,
                onClose = {
                    viewModel.addOrRemoveTag(tag.tagId)
                },
                onTagClick = {
                    viewModel.addOrRemoveTag(tag.tagId)
                }
            )
        }
        // new
        TagChip(
            "Add New",
            IconMapper.getIconByName("Add"),
            false,
            background = MaterialTheme.colorScheme.primary.copy(alpha = 0f) ,
            onClose = {},
            onTagClick = {
                // show add popup
                showPopup = true
            }
        )
    }

    if (showPopup) {
        AddNewTagPopup(
            viewModel = viewModel,
            onSave = { newTag ->
                viewModel.insertNewHabitTag(newTag)
                showPopup = false
            },
            onDismiss = {
                showPopup = false
            }
        )
    }


}


@Composable
fun AddReminder(
    habitViewModel: HabitViewModel,
    onAddReminderClicked : () -> Unit
) {

    var autoRefreshEnabled by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            RotatingAddIcon(
                icon = Icons.Default.Add,
                buttonSize = 45.dp,
                iconSize = 30.dp,
                isError = !autoRefreshEnabled,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = if (autoRefreshEnabled) 1f else 0.3f),
                onClick = {
                    onAddReminderClicked()
                }
            )

            Text(
                text = "Create Reminder",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            AnimatedToggleIcon(
                initialState = autoRefreshEnabled,
                onToggle = { newState ->
                    autoRefreshEnabled = newState
                }
            )
        }
        if (autoRefreshEnabled) {
            NotificationPermissionRequest()
            ReminderListScreen(viewModel = habitViewModel)
        }
    }
}



// ---------------------- MAIN PICKER ROW ----------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconAndHSVColorPicker(
    selectedIconName: String? = null,
    selectedColor: Color = Color.Green,
    onSelectedIconChanged: (String) -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val scope = rememberCoroutineScope()
    var iconPressed by remember { mutableStateOf(false) }
    var colorPressed by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }
    var showColorPickerRow by remember { mutableStateOf(false) }
    var showCustomHSVPicker by remember { mutableStateOf(false) }

    val iconScale by animateFloatAsState(
        targetValue = if (iconPressed) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f)
    )
    val colorScale by animateFloatAsState(
        targetValue = if (colorPressed) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f)
    )

    val glowColor by animateColorAsState(
        targetValue = Color.Transparent,
        animationSpec = spring(dampingRatio = 0.5f)
    )

    val context = LocalContext.current

    val edittextBg = if (context.isDarkTheme()) {
        MaterialTheme.colorScheme.background.lighten(0.05f)
    } else {
        MaterialTheme.colorScheme.background.darken(0.95f)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // ---------------- ICON + COLOR CARD ----------------
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(glowColor, edittextBg, glowColor)
                        )
                    )

                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // ICON BUTTON
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ){
                        Box(
                            modifier = Modifier
                                .size(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier
                                    .scale(iconScale)
                                    .size(50.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        scope.launch {
                                            iconPressed = true
                                            showIconPicker = !showIconPicker
                                            delay(120)
                                            iconPressed = false
                                        }
                                    },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                val icon = IconMapper.getIconByName(selectedIconName ?: "")
                                if (icon is IconRepresentation.Vector) {
                                    Icon(
                                        imageVector = icon.icon,
                                        contentDescription = "Pick Icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                } else if(icon is IconRepresentation.Emoji) {
                                    Text(
                                        text = icon.value,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f))
                        )

                        // COLOR BUTTON
                        Box(
                            modifier = Modifier
                                .size(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier
                                    .scale(colorScale)
                                    .size(50.dp)
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { showColorPickerRow = !showColorPickerRow },
                                shape = CircleShape,
                                color = selectedColor
                            ) {}
                        }
                    }
                    SampleConsistencyIcons(
                        activeColor = selectedColor,
                        cellIcon = IconMapper.getIconByName(selectedIconName ?: "")
                    )
                }
                PreviewConsistencyGraph(
                    activeColor = selectedColor,
                    cellIcon = IconMapper.getIconByName(selectedIconName ?: "")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ---------------- COLOR PICKER ROW ----------------
        if (showColorPickerRow) {
            val defaultColors = listOf(
                Color(0xFFFF0000), Color(0xFFFFA500), Color(0xFFFFFF00), Color(0xFF008000),
                Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFF800080), Color(0xFFFFC0CB),
                Color(0xFFA52A2A), Color(0xFF808080), Color(0xFF00FF00), Color(0xFF000000),
                Color(0xFFADD8E6), Color(0xFFFFD700), Color(0xFF800000), Color(0xFF008080),
                Color(0xFFB0C4DE), Color(0xFFFF69B4), Color(0xFF90EE90), Color(0xFF4B0082)
            )

            ColorGridPicker(
                defaultColors = defaultColors,
                selectedColor = selectedColor,
                onColorSelected = {
                    onColorSelected(it)
                },
                onCustomColorClick = {
                    showCustomHSVPicker = !showCustomHSVPicker
                }
            )
        }

        // ---------------- CUSTOM HSV COLOR PICKER POPUP ----------------
        if (showCustomHSVPicker) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FullRoundedHSVColorPicker(
                        initialColor = selectedColor,
                        onColorSelected = { onColorSelected(it) },
                        onDone = { showCustomHSVPicker = false }
                    )
                }
            }
        }
    }
    if (showIconPicker) {
        IconPickerBottomSheet(
            showSheet = showIconPicker,
            selectedName = selectedIconName,
            icons = IconMapper.allIconsList,
            onDismiss = { showIconPicker = false },
            onSelect = {
                onSelectedIconChanged(it)
                showIconPicker = false
            }
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorGridPicker(
    defaultColors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    onCustomColorClick: () -> Unit
) {
    val gridState = rememberLazyGridState()

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        defaultColors.forEach { color ->
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .size(40.dp)
                    .background(color, CircleShape)
                    .border(
                        2.dp,
                        if (selectedColor == color)
                            MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                        CircleShape
                    )
                    // 🔇 No ripple
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onColorSelected(color) }
            )
        }

        // 🎨 Custom color picker button as last grid item
        Box(
            modifier = Modifier
                .padding(6.dp)
                .size(40.dp)
                .background(Color.Gray, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                // 🔇 No ripple
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onCustomColorClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("🎨", style = MaterialTheme.typography.bodySmall)
        }
    }
}



@Composable
fun ConsistencyIconPicker(
    selectedConsistencyIcon: String,
    onIconSelected : (String) -> Unit
) {

    var showPicker by remember { mutableStateOf(false) }


    IconPickerBottomSheet(
        showSheet = showPicker,
        selectedName = selectedConsistencyIcon,
        icons = IconMapper.allIconsList ,
        onDismiss = { showPicker = false },
        onSelect = { name ->
            onIconSelected(name)
        }
    )

}