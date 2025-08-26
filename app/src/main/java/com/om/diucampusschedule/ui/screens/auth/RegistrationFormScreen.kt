package com.om.diucampusschedule.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.om.diucampusschedule.core.validation.DataValidator
import com.om.diucampusschedule.core.validation.DynamicDataValidator
import com.om.diucampusschedule.domain.model.UserRegistrationForm
import com.om.diucampusschedule.domain.model.UserRole
import com.om.diucampusschedule.core.network.rememberConnectivityState
import com.om.diucampusschedule.ui.components.NetworkStatusMessage
import com.om.diucampusschedule.ui.navigation.Screen
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme
import com.om.diucampusschedule.ui.viewmodel.AuthViewModel
import com.om.diucampusschedule.ui.viewmodel.ValidationViewModel

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
    var labSectionError by remember { mutableStateOf<String?>(null) }
    var initialError by remember { mutableStateOf<String?>(null) }
    
    // Network connectivity state
    val isConnected = rememberConnectivityState()

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
    
    fun validateLabSection(value: String): String? {
        val result = DynamicDataValidator.validateLabSection(value, validationData)
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
        val currentLabSectionError = if (role == UserRole.STUDENT) validateLabSection(labSection) else null
        val currentInitialError = if (role == UserRole.TEACHER) validateInitial(initial) else null
        
        nameError = currentNameError
        batchError = currentBatchError
        sectionError = currentSectionError
        labSectionError = currentLabSectionError
        initialError = currentInitialError
        
        return currentNameError == null && 
               currentBatchError == null && 
               currentSectionError == null && 
               currentLabSectionError == null && 
               currentInitialError == null
    }

    // Note: Navigation logic is handled by AppNavigation.kt
    // No need to handle auth state navigation here

    DIUCampusScheduleTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background Image with Gradient Overlay
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.welcome_screen_bg),
                    contentDescription = "DIU Campus Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.3f
                )

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Network Status
                NetworkStatusMessage(
                    isConnected = isConnected,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Title
                Text(
                    text = "Complete Your Profile",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Help us customize your experience",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Registration Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Profile Picture Section
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(
                                        3.dp,
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    )
                                    .clickable {
                                        // TODO: Implement image picker
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
                                            .fillMaxSize() // since parent Box is already clipped to circle and sized
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(R.drawable.ic_person_placeholder), // add a drawable placeholder
                                        error = painterResource(R.drawable.ic_person_placeholder)        // fallback if load fails
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Add Profile Picture",
                                        modifier = Modifier.size(60.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            Text(
                                text = "Tap to add photo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Name Field
                        OutlinedTextField(
                            value = name,
                            onValueChange = { newValue ->
                                name = newValue
                                nameError = validateName(newValue)
                            },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = nameError != null,
                            supportingText = if (nameError != null) {
                                { Text(text = nameError!!, color = MaterialTheme.colorScheme.error) }
                            } else null
                        )

                        // Department Field (Read-only for now)
                        OutlinedTextField(
                            value = department,
                            onValueChange = { department = it },
                            label = { Text("Department") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            readOnly = true
                        )

                        // Role Selection
                        Column {
                            Text(
                                text = "I am a:",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Student Radio Button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { 
                                        role = UserRole.STUDENT
                                        // Clear teacher-specific validation errors
                                        initialError = null
                                    }
                                ) {
                                    RadioButton(
                                        selected = role == UserRole.STUDENT,
                                        onClick = { 
                                            role = UserRole.STUDENT
                                            // Clear teacher-specific validation errors
                                            initialError = null
                                        }
                                    )
                                    Text(
                                        text = "Student",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }

                                // Teacher Radio Button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { 
                                        role = UserRole.TEACHER
                                        // Clear student-specific validation errors
                                        batchError = null
                                        sectionError = null
                                        labSectionError = null
                                    }
                                ) {
                                    RadioButton(
                                        selected = role == UserRole.TEACHER,
                                        onClick = { 
                                            role = UserRole.TEACHER
                                            // Clear student-specific validation errors
                                            batchError = null
                                            sectionError = null
                                            labSectionError = null
                                        }
                                    )
                                    Text(
                                        text = "Teacher",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }

                        // Student-specific fields
                        if (role == UserRole.STUDENT) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = batch,
                                    onValueChange = { newValue ->
                                        batch = newValue
                                        batchError = validateBatch(newValue)
                                        // Re-validate section when batch changes
                                        if (section.isNotBlank()) {
                                            sectionError = validateSection(section)
                                        }
                                    },
                                    label = { Text("Batch") },
                                    placeholder = { Text("59") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    isError = batchError != null,
                                    supportingText = when {
                                        batchError != null -> {
                                            { Text(text = batchError!!, color = MaterialTheme.colorScheme.error) }
                                        }
                                        validationData.validBatches.isNotEmpty() -> {
                                            { Text(text = "Available: ${validationData.validBatches.sorted().joinToString(", ")}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                        }
                                        else -> null
                                    }
                                )

                                OutlinedTextField(
                                    value = section,
                                    onValueChange = { newValue ->
                                        section = newValue
                                        sectionError = validateSection(newValue)
                                    },
                                    label = { Text("Section") },
                                    placeholder = { Text("A") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    isError = sectionError != null,
                                    supportingText = when {
                                        sectionError != null -> {
                                            { Text(text = sectionError!!, color = MaterialTheme.colorScheme.error) }
                                        }
                                        batch.isNotBlank() && validationData.getSectionsForBatch(batch).isNotEmpty() -> {
                                            { Text(text = "For batch $batch: ${validationData.getSectionsForBatch(batch).sorted().joinToString(", ")}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                        }
                                        else -> null
                                    }
                                )
                            }

                            OutlinedTextField(
                                value = labSection,
                                onValueChange = { newValue ->
                                    labSection = newValue
                                    labSectionError = validateLabSection(newValue)
                                },
                                label = { Text("Lab Section (Optional)") },
                                placeholder = { Text("A1") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = labSectionError != null,
                                supportingText = when {
                                    labSectionError != null -> {
                                        { Text(text = labSectionError!!, color = MaterialTheme.colorScheme.error) }
                                    }
                                    validationData.validLabSections.isNotEmpty() -> {
                                        { Text(text = "Available: ${validationData.validLabSections.sorted().joinToString(", ")}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    }
                                    else -> null
                                }
                            )
                        }

                        // Teacher-specific fields
                        if (role == UserRole.TEACHER) {
                            OutlinedTextField(
                                value = initial,
                                onValueChange = { newValue ->
                                    initial = newValue
                                    initialError = validateInitial(newValue)
                                },
                                label = { Text("Initial") },
                                placeholder = { Text("ABC") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = initialError != null,
                                supportingText = when {
                                    initialError != null -> {
                                        { Text(text = initialError!!, color = MaterialTheme.colorScheme.error) }
                                    }
                                    validationData.validTeacherInitials.isNotEmpty() -> {
                                        { Text(text = "Available: ${validationData.validTeacherInitials.sorted().joinToString(", ")}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    }
                                    else -> null
                                }
                            )
                        }
                        
                        // Loading indicator if validation data is loading
                        if (validationState.isLoading) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Loading validation data...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        // Error Message
                        if (authState.error != null) {
                            Text(
                                text = authState.error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Complete Registration Button
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
                            enabled = !authState.isLoading && !validationState.isLoading && isFormValid() && isConnected,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (authState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    text = "Complete Registration",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
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
