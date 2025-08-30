package com.om.diucampusschedule.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.R
import com.om.diucampusschedule.ui.viewmodel.FilterType
import com.om.diucampusschedule.ui.viewmodel.RoutineFilter
import com.om.diucampusschedule.ui.viewmodel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterRoutinesBottomSheet(
    viewModel: RoutineViewModel,
    onDismissRequest: () -> Unit,
    onFilterApplied: (RoutineFilter) -> Unit
) {
    var selectedFilterType by remember { mutableStateOf(FilterType.STUDENT) }
    var batch by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var teacherInitial by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val hasInputContent by remember {
        derivedStateOf {
            when (selectedFilterType) {
                FilterType.STUDENT -> batch.isNotBlank() || section.isNotBlank()
                FilterType.TEACHER -> teacherInitial.isNotBlank()
                FilterType.ROOM -> room.isNotBlank()
            }
        }
    }

    // Validation and submit function
    fun validateAndSubmit() {
        when(selectedFilterType) {
            FilterType.STUDENT -> {
                when {
                    batch.isBlank() -> errorMessage = "Batch cannot be empty"
                    !batch.all { it.isDigit() } -> errorMessage = "Batch must contain only numbers"
                    section.isBlank() -> errorMessage = "Section cannot be empty"
                    else -> {
                        isLoading = true
                        val filter = RoutineFilter(
                            type = FilterType.STUDENT,
                            batch = batch.trim(),
                            section = section.trim().uppercase()
                        )
                        android.util.Log.d("FilterBottomSheet", "Creating STUDENT filter: batch='${filter.batch}', section='${filter.section}'")
                        onFilterApplied(filter)
                        onDismissRequest()
                    }
                }
            }
            FilterType.TEACHER -> {
                when {
                    teacherInitial.isBlank() -> errorMessage = "Teacher initial cannot be empty"
                    else -> {
                        isLoading = true
                        val filter = RoutineFilter(
                            type = FilterType.TEACHER,
                            teacherInitial = teacherInitial.trim().lowercase()
                        )
                        android.util.Log.d("FilterBottomSheet", "Creating TEACHER filter: initial='${filter.teacherInitial}'")
                        onFilterApplied(filter)
                        onDismissRequest()
                    }
                }
            }
            FilterType.ROOM -> {
                when {
                    room.isBlank() -> errorMessage = "Room number cannot be empty"
                    else -> {
                        isLoading = true
                        val filter = RoutineFilter(
                            type = FilterType.ROOM,
                            room = room.trim().uppercase()
                        )
                        android.util.Log.d("FilterBottomSheet", "Creating ROOM filter: room='${filter.room}'")
                        onFilterApplied(filter)
                        onDismissRequest()
                    }
                }
            }
        }
    }

    // Clear fields function
    fun clearFields() {
        batch = ""
        section = ""
        teacherInitial = ""
        room = ""
        errorMessage = ""
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                .verticalScroll(rememberScrollState())
        ) {
            // Header with icon and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Routines",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { onDismissRequest() }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Filter Type Selector
            FilterTypeSelector(
                selectedType = selectedFilterType,
                onTypeSelected = {
                    selectedFilterType = it
                    clearFields()
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Dynamic Content based on selected filter type
            AnimatedContent(
                targetState = selectedFilterType,
                transitionSpec = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(animationSpec = tween(300)) togetherWith
                            slideOutVertically(
                                targetOffsetY = { -it },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ) + fadeOut(animationSpec = tween(150))
                },
                label = "filter_content_animation"
            ) { filterType ->
                when (filterType) {
                    FilterType.STUDENT -> {
                        StudentFilterContent(
                            batch = batch,
                            section = section,
                            errorMessage = errorMessage,
                            isLoading = isLoading,
                            onBatchChange = {
                                batch = it
                                if (errorMessage.isNotEmpty()) errorMessage = ""
                            },
                            onSectionChange = {
                                section = it.uppercase()
                                if (errorMessage.isNotEmpty()) errorMessage = ""
                            }
                        )
                    }
                    FilterType.TEACHER -> {
                        TeacherFilterContent(
                            teacherInitial = teacherInitial,
                            errorMessage = errorMessage,
                            isLoading = isLoading,
                            onTeacherInitialChange = {
                                teacherInitial = it.lowercase()
                                if (errorMessage.isNotEmpty()) errorMessage = ""
                            }
                        )
                    }
                    FilterType.ROOM -> {
                        RoomFilterContent(
                            room = room,
                            errorMessage = errorMessage,
                            isLoading = isLoading,
                            onRoomChange = {
                                room = it.uppercase()
                                if (errorMessage.isNotEmpty()) errorMessage = ""
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(300)) + scaleIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(150)) + scaleOut(animationSpec = tween(150))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { clearFields() },
                        modifier = Modifier.height(48.dp).weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading && hasInputContent
                    ) {
                        Text(
                            "Clear",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        onClick = { validateAndSubmit() },
                        modifier = Modifier.height(48.dp).weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !isLoading && hasInputContent
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Apply Filter",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTypeSelector(
    selectedType: FilterType,
    onTypeSelected: (FilterType) -> Unit
) {
    val filterOptions = listOf(
        FilterOption(FilterType.STUDENT, "Student", Icons.Default.Person),
        FilterOption(FilterType.TEACHER, "Teacher", Icons.Default.School),
        FilterOption(FilterType.ROOM, "Room", Icons.Default.Place)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            filterOptions.forEach { option ->
                FilterTypeCard(
                    option = option,
                    isSelected = selectedType == option.type,
                    onSelected = { onTypeSelected(option.type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FilterTypeCard(
    option: FilterOption,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_color"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "text_color"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )

    Surface(
        modifier = modifier
            .scale(animatedScale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onSelected() },
        shape = RoundedCornerShape(16.dp),
        color = animatedColor
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.title,
                modifier = Modifier.size(18.dp),
                tint = animatedTextColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = option.title,
                color = animatedTextColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentFilterContent(
    batch: String,
    section: String,
    errorMessage: String,
    isLoading: Boolean,
    onBatchChange: (String) -> Unit,
    onSectionChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Enter Batch",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = batch,
            onValueChange = onBatchChange,
            placeholder = { Text("e.g. \"44\"") },
            isError = errorMessage.contains("Batch", ignoreCase = true),
            supportingText = {
                if (errorMessage.contains("Batch", ignoreCase = true)) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                if (batch.isNotEmpty()) {
                    IconButton(onClick = { onBatchChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear batch"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Text(
            text = "Enter Section",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = section,
            onValueChange = onSectionChange,
            placeholder = { Text("e.g. \"A\"") },
            isError = errorMessage.contains("Section", ignoreCase = true),
            supportingText = {
                if (errorMessage.contains("Section", ignoreCase = true)) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                if (section.isNotEmpty()) {
                    IconButton(onClick = { onSectionChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear section"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeacherFilterContent(
    teacherInitial: String,
    errorMessage: String,
    isLoading: Boolean,
    onTeacherInitialChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        Text(
            text = "Enter Teacher Initial",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = teacherInitial,
            onValueChange = onTeacherInitialChange,
            placeholder = { Text("e.g. \"mbm\"") },
            isError = errorMessage.contains("Teacher", ignoreCase = true) || errorMessage.contains("initial", ignoreCase = true),
            supportingText = {
                if (errorMessage.contains("Teacher", ignoreCase = true) || errorMessage.contains("initial", ignoreCase = true)) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                if (teacherInitial.isNotEmpty()) {
                    IconButton(onClick = { onTeacherInitialChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear teacher initial"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomFilterContent(
    room: String,
    errorMessage: String,
    isLoading: Boolean,
    onRoomChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        Text(
            text = "Enter Room Number",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = room,
            onValueChange = onRoomChange,
            placeholder = { Text("e.g. \"811\"") },
            isError = errorMessage.contains("Room", ignoreCase = true),
            supportingText = {
                if (errorMessage.contains("Room", ignoreCase = true)) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                if (room.isNotEmpty()) {
                    IconButton(onClick = { onRoomChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear room"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
    }
}

private data class FilterOption(
    val type: FilterType,
    val title: String,
    val icon: ImageVector
)