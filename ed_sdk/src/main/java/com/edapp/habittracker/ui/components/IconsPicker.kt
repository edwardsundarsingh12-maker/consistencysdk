package com.edapp.habittracker.ui.components

import com.edapp.habittracker.di.IconMapper
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.edapp.habittracker.util.IconRepresentation

import com.edapp.habittracker.util.keyboardAsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun IconPickerBottomSheet(
    showSheet: Boolean,
    selectedName: String?,
    icons:  List<Pair<String, IconRepresentation>>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    // Observe IME (keyboard) visibility
    val keyboardVisible = keyboardAsState()

    LaunchedEffect(keyboardVisible) {
        if (keyboardVisible) {
            scope.launch { sheetState.expand() }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = sheetState,
            shape = RectangleShape, // sharp top corners
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(50)
                        )
                )
            }
        ) {
            IconPickerSheetContent(
                selectedName = selectedName,
                icons =  icons ,
                onSelect = {
                    onSelect(it)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconPickerSheetContent(
    selectedName: String?,
    icons:  List<Pair<String, IconRepresentation>>,
    onSelect: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredIcons = remember(searchQuery) {
        if (searchQuery.isBlank()) icons
        else icons.filter { (name, _) -> name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Text(
            "Select an Icon",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search icons…") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(64.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // important: allows sheet to expand naturally
                .padding(horizontal = 8.dp)
        ) {
            items(filteredIcons) { (name, icon) ->
                val isSelected = name == selectedName

                Box(
                    modifier = Modifier
                        .padding(5.dp)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF4CAF50) else Color.Transparent)
                        .clickable { onSelect(name) },
                    contentAlignment = Alignment.Center
                ) {
                    if (icon is IconRepresentation.Vector) {
                        Icon(
                            imageVector = icon.icon,
                            contentDescription = name,
                            tint = if (isSelected) Color.White else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    else if (icon is IconRepresentation.Emoji) {
                        Text(
                            text = icon.value,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
