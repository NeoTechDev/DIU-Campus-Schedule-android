package com.om.diucampusschedule.ui.screens.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.om.diucampusschedule.R
import com.om.diucampusschedule.core.network.rememberConnectivityState
import com.om.diucampusschedule.core.validation.DataValidator
import com.om.diucampusschedule.core.validation.DynamicDataValidator
import com.om.diucampusschedule.domain.model.UserRegistrationForm
import com.om.diucampusschedule.domain.model.UserRole
import com.om.diucampusschedule.ui.components.LabSectionChipGroup
import com.om.diucampusschedule.ui.components.NetworkStatusMessage
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme
import com.om.diucampusschedule.ui.viewmodel.AuthViewModel
import com.om.diucampusschedule.ui.viewmodel.ValidationViewModel
import com.om.diucampusschedule.util.ImageUploadUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationFormScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
    validationViewModel: ValidationViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val validationState by validationViewModel.uiState.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var profilePictureUrl by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Software Engineering") }
    var role by remember { mutableStateOf(UserRole.STUDENT) }
    var batch by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var labSection by remember { mutableStateOf("") }
    var initial by remember { mutableStateOf("") }
    
    // Validation error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var batchError by remember { mutableStateOf<String?>(null) }
    var sectionError by remember { mutableStateOf<String?>(null) }
    var initialError by remember { mutableStateOf<String?>(null) }
    
    // Network connectivity state
    val isConnected = rememberConnectivityState()
    
    // State for image upload
    var isUploadingImage by remember { mutableStateOf(false) }
    
    // Coroutine scope for image upload
    val scope = rememberCoroutineScope()
    
    // Context for image upload
    val context = LocalContext.current
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            scope.launch {
                isUploadingImage = true
                try {
                    // Upload image to Firebase Storage
                    val uploadResult = ImageUploadUtil.uploadProfilePicture(
                        context = context,
                        imageUri = selectedUri
                    )
                    
                    uploadResult.fold(
                        onSuccess = { downloadUrl ->
                            // Delete previous image if it was from Firebase Storage
                            if (profilePictureUrl.isNotEmpty() && !ImageUploadUtil.isLocalUri(profilePictureUrl)) {
                                ImageUploadUtil.deleteProfilePicture(profilePictureUrl)
                            }
                            
                            // Update profile picture URL with the new Firebase Storage URL
                            profilePictureUrl = downloadUrl
                        },
                        onFailure = { exception ->
                            // Handle upload error - could show a snackbar or toast
                            android.util.Log.e("RegistrationFormScreen", "Image upload failed", exception)
                            // For now, fallback to local URI
                            profilePictureUrl = selectedUri.toString()
                        }
                    )
                } finally {
                    isUploadingImage = false
                }
            }
        }
    }

    // Load validation data when department changes
    LaunchedEffect(department) {
        validationViewModel.loadValidationData(department)
    }
    
    // Get current validation data
    val validationData = validationState.validationData

    // Pre-fill data if user already has some info
    LaunchedEffect(authState.user) {
        authState.user?.let { user ->
            if (user.name.isNotBlank()) name = user.name
            if (user.profilePictureUrl.isNotBlank()) profilePictureUrl = user.profilePictureUrl
            department = user.department
            role = user.role
            if (user.batch.isNotBlank()) batch = user.batch
            if (user.section.isNotBlank()) section = user.section
            if (user.labSection.isNotBlank()) labSection = user.labSection
            if (user.initial.isNotBlank()) initial = user.initial
        }
    }
    
    // Validation functions using DynamicDataValidator
    fun validateName(value: String): String? {
        val result = DataValidator.validateName(value)
        return if (result.isValid) null else result.getErrorMessage()
    }
    
    fun validateBatch(value: String): String? {
        val result = DynamicDataValidator.validateBatch(value, validationData)
        return if (result.isValid) null else result.getErrorMessage()
    }
    
    fun validateSection(value: String): String? {
        val result = DynamicDataValidator.validateSection(value, batch, validationData)
        return if (result.isValid) null else result.getErrorMessage()
    }
    

    
    fun validateInitial(value: String): String? {
        val result = DynamicDataValidator.validateTeacherInitial(value, validationData)
        return if (result.isValid) null else result.getErrorMessage()
    }
    
    // Check if all validations pass
    fun isFormValid(): Boolean {
        val currentNameError = validateName(name)
        val currentBatchError = if (role == UserRole.STUDENT) validateBatch(batch) else null
        val currentSectionError = if (role == UserRole.STUDENT) validateSection(section) else null
        val currentInitialError = if (role == UserRole.TEACHER) validateInitial(initial) else null
        
        nameError = currentNameError
        batchError = currentBatchError
        sectionError = currentSectionError
        initialError = currentInitialError
        
        return currentNameError == null && 
               currentBatchError == null && 
               currentSectionError == null && 
               currentInitialError == null &&
               // For students, lab section must be selected if section is provided
               (role != UserRole.STUDENT || section.isBlank() || labSection.isNotBlank()) &&
               // Ensure validation data is available for dynamic validation
               (validationState.validationData.validBatches.isNotEmpty() || validationState.error != null)
    }

    // Note: Navigation logic is handled by AppNavigation.kt
    // No need to handle auth state navigation here

    DIUCampusScheduleTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .imePadding() // This adds padding when keyboard appears
                    .navigationBarsPadding() // This handles navigation bar padding
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(50.dp))
                
                // Network Status
                NetworkStatusMessage(
                    isConnected = isConnected,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Modern Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Complete Your Profile",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Help us customize your experience",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                // Modern Form Layout
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Profile Picture Section with modern styling
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                            )
                                        )
                                    )
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                                    .clickable {
                                        imagePickerLauncher.launch("image/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (profilePictureUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(profilePictureUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(R.drawable.ic_person_placeholder),
                                        error = painterResource(R.drawable.ic_person_placeholder)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Add Profile Picture",
                                        modifier = Modifier.size(60.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            
                            // Modern camera icon overlay
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { 
                                        if (!isUploadingImage) {
                                            imagePickerLauncher.launch("image/*")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUploadingImage) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Change Photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = "Tap to add photo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Name Field with modern styling
                    Column {
                        Text(
                            text = "Full Name",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { newValue ->
                                name = newValue
                                nameError = validateName(newValue)
                            },
                            placeholder = { 
                                Text(
                                    "Enter your full name",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = nameError != null,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                errorBorderColor = MaterialTheme.colorScheme.error
                            ),
                            supportingText = if (nameError != null) {
                                { Text(text = nameError!!, color = MaterialTheme.colorScheme.error) }
                            } else null
                        )
                    }

                    // Department Field with modern styling
                    Column {
                        Text(
                            text = "Department",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = department,
                            onValueChange = { department = it },
                            placeholder = { 
                                Text(
                                    "Your department",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            readOnly = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                            )
                        )
                    }

                    // Modern Role Selection
                    Column {
                        Text(
                            text = "I am a:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Student Radio Button
                            Row(
                                modifier = Modifier
                                    .clickable { role = UserRole.STUDENT }
                                    .background(
                                        color = if (role == UserRole.STUDENT) 
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = role == UserRole.STUDENT,
                                    onClick = { role = UserRole.STUDENT },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Student",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (role == UserRole.STUDENT) FontWeight.Medium else FontWeight.Normal
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            
                            // Teacher Radio Button
                            Row(
                                modifier = Modifier
                                    .clickable { role = UserRole.TEACHER }
                                    .background(
                                        color = if (role == UserRole.TEACHER) 
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = role == UserRole.TEACHER,
                                    onClick = { role = UserRole.TEACHER },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Teacher",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (role == UserRole.TEACHER) FontWeight.Medium else FontWeight.Normal
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }

                    // Student-specific fields
                    if (role == UserRole.STUDENT) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Student Information",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Batch Field
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Batch",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    
                                    OutlinedTextField(
                                        value = batch,
                                        onValueChange = { newValue ->
                                            // Only allow digits
                                            val filteredValue = newValue.filter { it.isDigit() }
                                            batch = filteredValue
                                            batchError = validateBatch(filteredValue)
                                            // Re-validate section if batch changes
                                            if (section.isNotBlank()) {
                                                sectionError = validateSection(section)
                                            }
                                        },
                                        placeholder = { Text("e.g. 41", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = batchError != null,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            errorBorderColor = MaterialTheme.colorScheme.error
                                        )
                                    )
                                    
                                    if (batchError != null) {
                                        Text(
                                            text = batchError!!,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    } else if (validationData.validBatches.isNotEmpty()) {
                                        val sortedBatches = validationData.validBatches.map { it.toIntOrNull() ?: 0 }.sorted()
                                        val batchHint = if (sortedBatches.size > 1 && 
                                            sortedBatches.last() - sortedBatches.first() + 1 == sortedBatches.size) {
                                            // Continuous range
                                            "Available: ${sortedBatches.first()} to ${sortedBatches.last()}"
                                        } else {
                                            // Non-continuous
                                            "Available: ${validationData.validBatches.sorted().joinToString(", ")}"
                                        }
                                        Text(
                                            text = batchHint,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }

                                // Section Field
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Section",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    
                                    OutlinedTextField(
                                        value = section,
                                        onValueChange = { newValue ->
                                            // Auto-uppercase and limit to single letter
                                            val filteredValue = newValue.filter { it.isLetter() }.uppercase().take(1)
                                            section = filteredValue
                                            sectionError = validateSection(filteredValue)
                                            // Reset lab section if section changes and current lab section is invalid
                                            if (labSection.isNotBlank()) {
                                                val expectedLabSections = validationData.getLabSectionsForMainSection(filteredValue)
                                                if (!expectedLabSections.contains(labSection)) {
                                                    labSection = ""
                                                }
                                            }
                                        },
                                        placeholder = { Text("e.g. J", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        isError = sectionError != null,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            errorBorderColor = MaterialTheme.colorScheme.error
                                        )
                                    )
                                    
                                    if (sectionError != null) {
                                        Text(
                                            text = sectionError!!,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    } else if (batch.isNotBlank() && validationData.getSectionsForBatch(batch).isNotEmpty()) {
                                        val availableSections = validationData.getSectionsForBatch(batch).sorted()
                                        val sectionHint = if (availableSections.size > 2) {
                                            "For batch $batch: ${availableSections.first()} to ${availableSections.last()}"
                                        } else {
                                            "For batch $batch: ${availableSections.joinToString(", ")}"
                                        }
                                        Text(
                                            text = sectionHint,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Lab Section Chip Selection
                            LabSectionChipGroup(
                                mainSection = section,
                                selectedLabSection = labSection,
                                onLabSectionSelected = { selectedLabSection ->
                                    labSection = selectedLabSection
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = section.isNotBlank()
                            )
                        }
                    }

                    // Teacher-specific fields
                    if (role == UserRole.TEACHER) {
                        Column {
                            Text(
                                text = "Teacher Information",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            OutlinedTextField(
                                value = initial,
                                onValueChange = { newValue ->
                                    // Auto-uppercase and filter letters and numbers only
                                    val filteredValue = newValue.filter { it.isLetterOrDigit() }.uppercase().take(6)
                                    initial = filteredValue
                                    initialError = validateInitial(filteredValue)
                                },
                                placeholder = { Text("e.g. MBM", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = initialError != null,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                ),
                                supportingText = when {
                                    initialError != null -> {
                                        { Text(text = initialError!!, color = MaterialTheme.colorScheme.error) }
                                    }
                                    validationData.validTeacherInitials.isNotEmpty() -> {
                                        val sortedInitials = validationData.validTeacherInitials.sorted()
                                        val displayText = if (sortedInitials.size > 10) {
                                            "Available initials: ${sortedInitials.take(8).joinToString(", ")} and ${sortedInitials.size - 8} more"
                                        } else {
                                            "Available: ${sortedInitials.joinToString(", ")}"
                                        }
                                        { Text(text = displayText, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    }
                                    else -> null
                                }
                            )
                        }
                    }
                    
                    // Loading indicator if validation data is loading
                    if (validationState.isLoading) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Loading validation data for $department...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Validation data error
                    if (validationState.error != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Validation Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = validationState.error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Error Message with clean styling
                    if (authState.error != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = authState.error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Modern Complete Registration Button
                    Button(
                        onClick = {
                            if (isFormValid()) {
                                viewModel.clearError()
                                val form = UserRegistrationForm(
                                    name = name.trim(),
                                    profilePictureUrl = profilePictureUrl.trim(),
                                    department = department.trim(),
                                    role = role,
                                    batch = batch.trim().uppercase(),
                                    section = section.trim().uppercase(),
                                    labSection = labSection.trim().uppercase().takeIf { it.isNotBlank() } ?: "",
                                    initial = initial.trim().uppercase()
                                )
                                viewModel.updateUserProfile(form)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !authState.isLoading && !validationState.isLoading && isFormValid() && isConnected &&
                                // Ensure validation data is loaded if needed
                                (validationState.validationData.validBatches.isNotEmpty() || validationState.error != null),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    ) {
                        if (authState.isLoading) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Completing...",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        } else {
                            Text(
                                text = "Complete Registration",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp)) // Increased bottom padding for keyboard
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegistrationFormScreenPreview() {
    val navController = rememberNavController()
    RegistrationFormScreen(navController = navController)
}
