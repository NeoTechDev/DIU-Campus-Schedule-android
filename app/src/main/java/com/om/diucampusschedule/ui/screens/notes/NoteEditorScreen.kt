package com.om.diucampusschedule.ui.screens.notes


import android.annotation.SuppressLint
import android.os.Build
import android.text.Html
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import com.om.diucampusschedule.R
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme
import com.om.diucampusschedule.ui.theme.md_theme_light_primary
import com.om.diucampusschedule.ui.utils.ScreenConfig
import com.om.diucampusschedule.ui.utils.TopAppBarIconSize.topbarIconSize
import com.om.diucampusschedule.ui.viewmodel.NoteViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(navController: NavController, noteId: Int?) {
    
    // Data class to track note state
    @Stable
    data class NoteEditorState(
        val title: String = "",
        val richTextHtml: String = "",
        val color: String = "#FFFFFF",
        val isModified: Boolean = false
    ) {
        fun toPlainText(html: String): String {
            return if (html.isBlank()) "" else {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT).toString().trim()
                    } else {
                        @Suppress("DEPRECATION")
                        Html.fromHtml(html).toString().trim()
                    }
                } catch (e: Exception) {
                    html.replace(Regex("<[^>]*>"), "").trim()
                }
            }
        }
    }
    DIUCampusScheduleTheme {
        val noteViewModel: NoteViewModel = hiltViewModel()
        val uiState by noteViewModel.uiState.collectAsStateWithLifecycle()

        // Get the specific note if noteId is provided
        val existingNote = uiState.notes.find { it.id == noteId }
        
        // Initialize editor state based on existing note or create new
        var editorState by remember(existingNote) {
            mutableStateOf(
                NoteEditorState(
                    title = existingNote?.title.orEmpty(),
                    richTextHtml = existingNote?.richTextHtml.orEmpty(),
                    color = existingNote?.color ?: "#FFFFFF",
                    isModified = false
                )
            )
        }

        // Function to update editor state and mark as modified
        fun updateEditorState(
            title: String = editorState.title,
            richTextHtml: String = editorState.richTextHtml,
            color: String = editorState.color
        ) {
            val newState = NoteEditorState(
                title = title,
                richTextHtml = richTextHtml,
                color = color,
                isModified = if (existingNote == null) {
                    // For new notes, any content is considered modified
                    title.isNotEmpty() || richTextHtml.isNotEmpty()
                } else {
                    // For existing notes, compare with original values
                    title != existingNote.title ||
                    richTextHtml != existingNote.richTextHtml ||
                    color != existingNote.color
                }
            )
            editorState = newState
        }

        // Convert the selected color string to Color object with error handling
        val backgroundColor = remember(editorState.color) {
            try {
                Color(editorState.color.toColorInt())
            } catch (e: IllegalArgumentException) {
                // Fallback to white if color parsing fails
                Color.White
            }
        }

        // Calculate contrasting colors for text and icons
        val contentColor = remember(backgroundColor) {
            getContrastingColor(backgroundColor)
        }

        val iconTint = remember(backgroundColor) {
            if (getContrastingColor(backgroundColor) == Color.Black) {
                Color.Black.copy(alpha = 0.87f) // Better contrast for dark icons
            } else {
                Color.White.copy(alpha = 0.87f) // Better contrast for light icons
            }
        }

        // Rich text state
        val richTextState = rememberRichTextState()
        var showLinkDialog by remember { mutableStateOf(false) }
        var linkUrl by remember { mutableStateOf("") }
        var showTextColorDialog by remember { mutableStateOf(false) }
        var showHighlightColorDialog by remember { mutableStateOf(false) }

        // Text color options
        val textColorOptions = remember {
            listOf(
                Color.Black,
                Color.DarkGray,
                Color.Gray,
                Color.Red,
                Color.Blue,
                Color.Green,
                Color.Magenta,
                Color.Cyan,
                Color(0xFF9C27B0), // Purple
                Color(0xFFFF9800), // Orange
                Color(0xFF795548), // Brown
                Color(0xFF607D8B)  // Blue Gray
            )
        }

        // Highlight color options (lighter colors for highlights)
        val highlightColorOptions = remember {
            listOf(
                Color(0xFFFFFF00), // Yellow
                Color(0xFFFFE0B2), // Light Orange
                Color(0xFFFFCDD2), // Light Red
                Color(0xFFC8E6C9), // Light Green
                Color(0xFFBBDEFB), // Light Blue
                Color(0xFFE1BEE7), // Light Purple
                Color(0xFFF5F5F5), // Light Gray
                Color(0xFFB2DFDB), // Light Teal
                Color(0xFFD7CCC8), // Light Brown
                Color(0xFFDCEDC8), // Light Lime
                Color(0xFFFFF9C4), // Light Yellow
                Color(0xFFFCE4EC)  // Light Pink
            )
        }

        // Configure rich text editor appearance
        LaunchedEffect(Unit) {
            // Customize code span appearance
            richTextState.config.codeSpanBackgroundColor = Color(0xFFF5F5F5)
            richTextState.config.codeSpanColor = Color(0xFFD32F2F) // Reddish color for code
            richTextState.config.codeSpanStrokeColor = Color(0xFFE0E0E0)
            richTextState.config.linkColor = md_theme_light_primary
            richTextState.config.linkTextDecoration = TextDecoration.Underline
        }

        // Load the existing rich text content if available
        LaunchedEffect(existingNote?.richTextHtml) {
            if (!existingNote?.richTextHtml.isNullOrEmpty()) {
                richTextState.setHtml(existingNote?.richTextHtml ?: "")
            }
        }

        // Track changes to rich text content
        LaunchedEffect(richTextState.annotatedString.text) {
            updateEditorState(richTextHtml = richTextState.toHtml())
        }

        // Define a list of available colors
        val colorOptions = remember {
            listOf(
                "#FFFFFF",  // White
                "#FAE3E3",  // Light Red
                "#E3FAE3",  // Light Green
                "#E3E3FA",  // Light Blue
                "#FAFAE3",  // Light Yellow
                "#E3FAFA",  // Light Cyan
                "#FAE3FA",  // Light Magenta
                "#F5F5F5",  // Light Gray
                "#FFE0B2",  // Light Orange
                "#D7CCC8",  // Light Brown
                "#BBDEFB",  // Pale Blue
                "#C8E6C9",  // Pale Green
                "#F8BBD0",  // Pale Pink
                "#D1C4E9",  // Pale Purple
                "#B2EBF2",  // Pale Cyan
                "#FFECB3",  // Pale Amber
                "#E6EE9C",  // Pale Lime
                "#CFD8DC",  // Blue Gray
                "#FFD8B0",  // Peach
                "#E1BEE7"   // Lavender
            )
        }

        // State for LazyRow
        val lazyListState = rememberLazyListState()

        // Scroll to the selected color if it's not the default white
        LaunchedEffect(editorState.color) {
            if (editorState.color != "#FFFFFF") {
                val selectedIndex = colorOptions.indexOf(editorState.color)
                if (selectedIndex != -1) {
                    lazyListState.scrollToItem(selectedIndex)
                }
            }
        }

        // Link Dialog
        if (showLinkDialog) {
            Dialog(onDismissRequest = { showLinkDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Dialog header with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Add Link",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // URL input field with visual label
                        OutlinedTextField(
                            value = linkUrl,
                            onValueChange = { linkUrl = it },
                            label = { Text("URL") },
                            placeholder = { Text("e.g. https://example.com") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                        )

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showLinkDialog = false },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    if (linkUrl.isNotEmpty()) {
                                        richTextState.addLink(text = linkUrl, url = linkUrl)
                                    }
                                    showLinkDialog = false
                                    linkUrl = ""
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Add")
                            }
                        }
                    }
                }
            }
        }

        // Text Color Dialog
        if (showTextColorDialog) {
            Dialog(onDismissRequest = { showTextColorDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Dialog header with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatColorText,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Select Text Color",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Color grid display (4x3 grid)
                        val rows = 3
                        val columns = 4
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (row in 0 until rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    for (col in 0 until columns) {
                                        val index = row * columns + col
                                        if (index < textColorOptions.size) {
                                            val color = textColorOptions[index]

                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(color)
                                                    .border(
                                                        width = 2.dp,
                                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable {
                                                        richTextState.toggleSpanStyle(SpanStyle(color = color))
                                                        showTextColorDialog = false
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                // Add a checkmark to show current color
                                                if (richTextState.currentSpanStyle.color == color) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = getContrastingColor(color),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showTextColorDialog = false },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }

        // Highlight Color Dialog
        if (showHighlightColorDialog) {
            Dialog(onDismissRequest = { showHighlightColorDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Dialog header with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatColorFill,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Select Highlight Color",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Color grid display (4x3 grid)
                        val rows = 3
                        val columns = 4
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (row in 0 until rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    for (col in 0 until columns) {
                                        val index = row * columns + col
                                        if (index < highlightColorOptions.size) {
                                            val color = highlightColorOptions[index]

                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(color)
                                                    .border(
                                                        width = 2.dp,
                                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable {
                                                        richTextState.toggleSpanStyle(SpanStyle(background = color))
                                                        showHighlightColorDialog = false
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                // Add a checkmark to show current color
                                                if (richTextState.currentSpanStyle.background == color) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = getContrastingColor(color),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showHighlightColorDialog = false },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }

        Scaffold(
            modifier = Modifier.navigationBarsPadding(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (existingNote == null) "New Note" else "Edit Note",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            // Only save if there are actual changes
                            if (editorState.isModified && (editorState.title.isNotEmpty() || richTextState.annotatedString.text.isNotEmpty())) {
                                val plainTextContent = editorState.toPlainText(richTextState.toHtml())
                                
                                if (existingNote != null) {
                                    noteViewModel.updateNote(
                                        noteId = existingNote.id,
                                        title = editorState.title,
                                        content = plainTextContent,
                                        richTextHtml = richTextState.toHtml(),
                                        color = editorState.color
                                    )
                                } else {
                                    noteViewModel.createNote(
                                        title = editorState.title,
                                        content = plainTextContent,
                                        richTextHtml = richTextState.toHtml(),
                                        color = editorState.color
                                    )
                                }
                            }
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Back",
                                tint = iconTint,
                                modifier = Modifier.size(topbarIconSize)
                            )
                        }
                    },
                    actions = {
                        if (existingNote != null) {
                            IconButton(onClick = {
                                noteViewModel.deleteNote(existingNote.id)
                                navController.popBackStack()
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.delete),
                                    contentDescription = "Delete",
                                    tint = if (backgroundColor.luminance() > 0.5f) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                                    },
                                    modifier = Modifier.size(topbarIconSize)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundColor,
                        titleContentColor = contentColor,
                        navigationIconContentColor = iconTint,
                        actionIconContentColor = iconTint
                    ),
                    windowInsets = ScreenConfig.getTopAppBarWindowInsets(handleStatusBar = true),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            bottomBar = {
                Column(modifier = Modifier.windowInsetsPadding(WindowInsets.ime)) { 
                    // Rich text formatting toolbar - single compact row with all tools
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Define all formatting tools in one row
                        item {
                            // Bold button
                            FormatToolButton(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.FormatBold,
                                        contentDescription = null,
                                        tint = if (richTextState.currentSpanStyle.fontWeight == FontWeight.Bold)
                                            Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = { richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
                                isSelected = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold,
                                contentDescription = "Bold"
                            )
                        }

                        item {
                            // Italic button
                            FormatToolButton(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.FormatItalic,
                                        contentDescription = null,
                                        tint = if (richTextState.currentSpanStyle.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic)
                                            Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = { richTextState.toggleSpanStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) },
                                isSelected = richTextState.currentSpanStyle.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic,
                                contentDescription = "Italic"
                            )
                        }

                        item {
                            // Underline button
                            FormatToolButton(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.FormatUnderlined,
                                        contentDescription = null,
                                        tint = if (richTextState.currentSpanStyle.textDecoration == TextDecoration.Underline)
                                            Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) },
                                isSelected = richTextState.currentSpanStyle.textDecoration == TextDecoration.Underline,
                                contentDescription = "Underline"
                            )
                        }

                        item {
                            // Heading button
                            FormatToolButton(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Title,
                                        contentDescription = null,
                                        tint = if (richTextState.currentSpanStyle.fontSize?.value ?: 16f > 16f)
                                            Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    // Toggle between normal text and heading
                                    if (richTextState.currentSpanStyle.fontSize?.value ?: 16f > 16f) {
                                        richTextState.toggleSpanStyle(SpanStyle(fontSize = 16.sp))
                                    } else {
                                        richTextState.toggleSpanStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold))
                                    }
                                },
                                isSelected = richTextState.currentSpanStyle.fontSize?.value ?: 16f > 16f,
                                contentDescription = "Heading"
                            )
                        }

                        item {
                            // Text Color button
                            FormatToolButton(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.FormatColorText,
                                        contentDescription = null,
                                        tint = richTextState.currentSpanStyle.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = { showTextColorDialog = true },
                                isSelected = richTextState.currentSpanStyle.color != null &&
                                        richTextState.currentSpanStyle.color != Color.Transparent &&
                                        richTextState.currentSpanStyle.color != Color.Black &&
                                        textColorOptions.contains(richTextState.currentSpanStyle.color),
                                contentDescription = "Text Color"
                            )
                        }

                        item {
                            // Highlight Color button (new)
                            FormatToolButton(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.FormatColorFill,
                                        contentDescription = null,
                                        tint = richTextState.currentSpanStyle.background ?: Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = { showHighlightColorDialog = true },
                                isSelected = richTextState.currentSpanStyle.background != null &&
                                        richTextState.currentSpanStyle.background != Color.Transparent &&
                                        highlightColorOptions.contains(richTextState.currentSpanStyle.background),
                                contentDescription = "Highlight Color"
                            )
                        }

                        item {
                            // Code button
                            FormatToolButton(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = null,
                                        tint = if (richTextState.isCodeSpan)
                                            Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    // Apply monospace font when toggling code span
                                    richTextState.toggleCodeSpan()
                                    if (richTextState.isCodeSpan) {
                                        richTextState.toggleSpanStyle(SpanStyle(fontFamily = FontFamily.Monospace))
                                    }
                                },
                                isSelected = richTextState.isCodeSpan,
                                contentDescription = "Code"
                            )
                        }

                        item {
                            // Bullet list button
                            FormatToolButton(
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                        contentDescription = null,
                                        tint = if (richTextState.isUnorderedList)
                                            Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = { richTextState.toggleUnorderedList() },
                                isSelected = richTextState.isUnorderedList,
                                contentDescription = "Bullet List"
                            )
                        }

                        item {
                            // Numbered list button
                            FormatToolButton(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.FormatListNumbered,
                                        contentDescription = null,
                                        tint = if (richTextState.isOrderedList)
                                            Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = { richTextState.toggleOrderedList() },
                                isSelected = richTextState.isOrderedList,
                                contentDescription = "Numbered List"
                            )
                        }

                        item {
                            // Align Left button
                            FormatToolButton(
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.FormatAlignLeft,
                                        contentDescription = null,
                                        tint = if (richTextState.currentParagraphStyle.textAlign == TextAlign.Start)
                                            Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = { richTextState.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Start)) },
                                isSelected = richTextState.currentParagraphStyle.textAlign == TextAlign.Start,
                                contentDescription = "Align Left"
                            )
                        }

                        item {
                            // Align Center button
                            FormatToolButton(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.FormatAlignCenter,
                                        contentDescription = null,
                                        tint = if (richTextState.currentParagraphStyle.textAlign == TextAlign.Center)
                                            Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = { richTextState.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center)) },
                                isSelected = richTextState.currentParagraphStyle.textAlign == TextAlign.Center,
                                contentDescription = "Align Center"
                            )
                        }

                        item {
                            // Align Right button
                            FormatToolButton(
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.FormatAlignRight,
                                        contentDescription = null,
                                        tint = if (richTextState.currentParagraphStyle.textAlign == TextAlign.End)
                                            Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = { richTextState.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.End)) },
                                isSelected = richTextState.currentParagraphStyle.textAlign == TextAlign.End,
                                contentDescription = "Align Right"
                            )
                        }

                        item {
                            // Link button
                            FormatToolButton(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = if (richTextState.isLink)
                                            Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = { showLinkDialog = true },
                                isSelected = richTextState.isLink,
                                contentDescription = "Add Link"
                            )
                        }
                    }

                    // Color selection bar
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        state = lazyListState // Add the state here
                    ) {
                        items(colorOptions) { color ->
                            val isSelected = color == editorState.color
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(color)))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable { updateEditorState(color = color) }
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.Black,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Last edited info
                    BottomAppBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        containerColor = Color.Transparent,
                        contentColor = Color.Gray
                    ){
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text = existingNote?.lastEditedTime?.let { "Last edited: $it" } ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            contentWindowInsets = WindowInsets.ime, // Added for IME (keyboard) awareness
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .padding(innerPadding)
                ) {

                    TextField(
                        value = editorState.title,
                        onValueChange = { updateEditorState(title = it) },
                        placeholder = {
                            Text(
                                text = "Title",
                                color = Color.Gray,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedLabelColor = Color.Transparent,
                            unfocusedLabelColor = Color.Gray,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp),
                    )

                    // Rich Text Editor
                    RichTextEditor(
                        state = richTextState,
                        placeholder = {
                            Text(
                                text = "Content",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors =  RichTextEditorDefaults.richTextEditorColors(
                            textColor = Color.Black,
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        )

        BackHandler {
            // Only save if there are actual changes
            if (editorState.isModified && (editorState.title.isNotEmpty() || richTextState.annotatedString.text.isNotEmpty())) {
                val plainTextContent = editorState.toPlainText(richTextState.toHtml())
                
                if (existingNote != null) {
                    noteViewModel.updateNote(
                        noteId = existingNote.id,
                        title = editorState.title,
                        content = plainTextContent,
                        richTextHtml = richTextState.toHtml(),
                        color = editorState.color
                    )
                } else {
                    noteViewModel.createNote(
                        title = editorState.title,
                        content = plainTextContent,
                        richTextHtml = richTextState.toHtml(),
                        color = editorState.color
                    )
                }
            }
            navController.popBackStack()
        }
    }
}

@Composable
private fun FormatToolButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    isSelected: Boolean,
    contentDescription: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate scale when pressed
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(34.dp)
            .scale(scale)
            .clip(RoundedCornerShape(percent = 15))
            .background(
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    Color.Transparent
            )
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else Color.Black,
                shape = RoundedCornerShape(percent = 15)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

// Helper function to determine contrasting color (for text on color backgrounds)
private fun getContrastingColor(backgroundColor: Color): Color {
    val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return if (luminance > 0.5f) Color.Black else Color.White
}
