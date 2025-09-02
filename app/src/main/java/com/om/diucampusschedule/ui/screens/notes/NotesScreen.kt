package com.om.diucampusschedule.ui.screens.notes

import android.annotation.SuppressLint
import android.os.Build
import android.text.Html
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichText
import com.om.diucampusschedule.R
import com.om.diucampusschedule.domain.model.Note
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme
import com.om.diucampusschedule.ui.theme.InterFontFamily
import com.om.diucampusschedule.ui.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NotesScreen(navController: NavController) {
    DIUCampusScheduleTheme {
        val noteViewModel: NoteViewModel = hiltViewModel()
        val uiState by noteViewModel.uiState.collectAsStateWithLifecycle()

        var searchQuery by remember { mutableStateOf("") }
        var showDeleteDialog by remember { mutableStateOf(false) }

        val notes = uiState.notes
        val selectedNotes = uiState.selectedNoteIds
        val isSelectionMode = uiState.isSelectionMode

        val sortedNotes = remember(notes, searchQuery) {
            if (searchQuery.isEmpty()) {
                notes.sortedByDescending { parseTimestamp(it.lastEditedTime) }
            } else {
                notes.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                            it.content.contains(searchQuery, ignoreCase = true)
                }.sortedByDescending { parseTimestamp(it.lastEditedTime) }
            }
        }

        BackHandler(enabled = isSelectionMode) {
            noteViewModel.clearSelection()
        }

        Scaffold(
            floatingActionButton = {
                AnimatedVisibility(
                    visible = !isSelectionMode,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    FloatingActionButton(
                        onClick = { navController.navigate("note_editor") },
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(painter = painterResource(R.drawable.edit_24px), "Add Note", Modifier.size(24.dp))
                    }
                }
            },
            topBar = {
                if (isSelectionMode) {
                    TopAppBar(
                        title = {
                            Text(
                                "${selectedNotes.size} selected",
                                fontFamily = InterFontFamily,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                noteViewModel.clearSelection()
                            }) {
                                Icon(painterResource(R.drawable.ic_close), "Close", tint = Color.White)
                            }
                        },
                        actions = {
                            AnimatedVisibility(
                                visible = selectedNotes.size < notes.size,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                IconButton(onClick = {
                                    noteViewModel.selectAllNotes()
                                }) {
                                    Icon(Icons.Default.SelectAll, "Select All", tint = Color.White)
                                }
                            }
                            AnimatedVisibility(
                                visible = selectedNotes.isNotEmpty(),
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                IconButton(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                } else {
                    TopAppBar(
                        title = {
                            Text(
                                "Notes",
                                fontFamily = InterFontFamily,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium)
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(paddingValues)
                ) {
                    if (!isSelectionMode) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            placeholder = { Text("Search notes...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = InterFontFamily) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, "Search Icon", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingIcon = {
                                AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(painterResource(R.drawable.ic_close), "Clear search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            textStyle = TextStyle(fontFamily = InterFontFamily),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AnimatedVisibility(visible = notes.isEmpty(), enter = fadeIn(), exit = fadeOut()) {
                            EmptyNotesState()
                        }
                        AnimatedVisibility(visible = notes.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                            NotesGrid(
                                notes = sortedNotes,
                                selectedNotes = selectedNotes,
                                isSelectionMode = isSelectionMode,
                                onNoteClick = { noteId ->
                                    if (isSelectionMode) {
                                        noteViewModel.toggleNoteSelection(noteId)
                                    } else {
                                        navController.navigate("note_editor?noteId=$noteId")
                                    }
                                },
                                onLongPress = { noteId ->
                                    if (!isSelectionMode) {
                                        noteViewModel.toggleNoteSelection(noteId)
                                    }
                                }
                            )
                        }
                    }
                }

                // Delete Confirmation Dialog
                if (showDeleteDialog) {
                    DeleteConfirmationDialog(
                        count = selectedNotes.size,
                        onDismiss = { showDeleteDialog = false },
                        onConfirm = {
                            noteViewModel.deleteSelectedNotes()
                            showDeleteDialog = false
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    count: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .width(IntrinsicSize.Min)
                    .widthIn(min = 280.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Delete Notes",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = InterFontFamily
                    )
                }

                val message = if (count == 1) {
                    "This note will be permanently deleted."
                } else {
                    "These $count notes will be permanently deleted."
                }

                Text(
                    message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontFamily = InterFontFamily
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("CANCEL", fontFamily = InterFontFamily)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("DELETE", fontFamily = InterFontFamily)
                    }
                }
            }
        }
    }
}


@Composable
fun NotesGrid(
    notes: List<Note>,
    selectedNotes: Set<Int>,
    isSelectionMode: Boolean,
    onNoteClick: (Int) -> Unit,
    onLongPress: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(notes) { note ->
            // Use the note's color instead of random color generation
            val noteColor = Color(note.color.toColorInt())
            NoteCard(
                note = note,
                backgroundColor = noteColor,
                isSelected = note.id in selectedNotes,
                isSelectionMode = isSelectionMode,
                onClick = { onNoteClick(note.id) },
                onLongPress = { onLongPress(note.id) }
            )
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    backgroundColor: Color,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val elevation = animateDpAsState(
        targetValue = when {
            isSelected -> 4.dp
            backgroundColor == Color.White -> 0.dp
            else -> 1.dp
        },
        animationSpec = tween(200)
    )

    val cardBorderColor = animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            backgroundColor == Color.White -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            else -> Color.Transparent
        },
        animationSpec = tween(200)
    )

    // Create a rich text state for preview
    val richTextState = rememberRichTextState()

    // Set HTML content if available
    LaunchedEffect(note.richTextHtml) {
        if (note.richTextHtml?.isNotEmpty() == true) {
            richTextState.setHtml(note.richTextHtml)
        }
    }

    // Get plain text from HTML for fallback display
    val contentPreview = if (note.richTextHtml?.isNotEmpty() == true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(note.richTextHtml, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(note.richTextHtml).toString()
        }
    } else {
        note.content ?: ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            }
            .border(
                width = when{
                    isSelected -> 2.dp
                    backgroundColor == Color.White -> 1.dp
                    else -> 0.dp
                },
                color = cardBorderColor.value,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.value)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 2,
                        fontFamily = InterFontFamily,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Display rich text if available, otherwise fallback to plain text
                    if (note.richTextHtml?.isNotEmpty() == true) {
                        BasicRichText(
                            state = richTextState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        )
                    } else {
                        Text(
                            text = contentPreview,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            maxLines = 5,
                            fontFamily = InterFontFamily,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                note.lastEditedTime?.let {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        color = Color.Gray.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDisplayTime(it),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = InterFontFamily,
                            color = Color.DarkGray.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.End
            ){
                // Selection indicator
                AnimatedVisibility(
                    visible = isSelectionMode || isSelected,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200)),
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            )
                            .border(1.dp,
                                if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(0.5f),
                                CircleShape
                            )
                    ) {
                        Column{
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = scaleIn(animationSpec = tween(150)),
                                exit = scaleOut(animationSpec = tween(150))
                            ) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


private fun parseTimestamp(timestamp: String?): Long {
    if (timestamp == null) return 0L
    return try {
        val format = SimpleDateFormat("MMM d yyyy, h:mm a", Locale.US)
        format.parse(timestamp)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}

@Composable
fun EmptyNotesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(R.drawable.notebook), "Note Icon", Modifier.size(200.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No notes yet",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            fontFamily = InterFontFamily
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Create your first note now!",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            fontFamily = InterFontFamily
        )
    }
}

private fun formatDisplayTime(timestamp: String): String {
    try {
        val inputFormat = SimpleDateFormat("MMM d yyyy, h:mm a", Locale.US)
        val noteDate: Date? = inputFormat.parse(timestamp)
        if (noteDate == null) return timestamp

        val noteCalendar = Calendar.getInstance().apply { time = noteDate }
        val todayCalendar = Calendar.getInstance()
        val yesterdayCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return when {
            noteCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                    noteCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR) ->
                "Today at ${SimpleDateFormat("h:mm a", Locale.US).format(noteDate)}"
            noteCalendar.get(Calendar.YEAR) == yesterdayCalendar.get(Calendar.YEAR) &&
                    noteCalendar.get(Calendar.DAY_OF_YEAR) == yesterdayCalendar.get(Calendar.DAY_OF_YEAR) ->
                "Yesterday at ${SimpleDateFormat("h:mm a", Locale.US).format(noteDate)}"
            (todayCalendar.timeInMillis - noteCalendar.timeInMillis) <= 7 * 24 * 60 * 60 * 1000 ->
                SimpleDateFormat("EEEE at h:mm a", Locale.US).format(noteDate)
            else -> SimpleDateFormat("MMM d", Locale.US).format(noteDate)
        }
    } catch (e: Exception) {
        return timestamp
    }
}
