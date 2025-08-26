package com.om.diucampusschedule.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.om.diucampusschedule.core.validation.DataValidator
import com.om.diucampusschedule.core.validation.DynamicDataValidator
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.model.UserRegistrationForm
import com.om.diucampusschedule.domain.model.UserRole
import com.om.diucampusschedule.ui.navigation.Screen
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme
import com.om.diucampusschedule.ui.viewmodel.AuthViewModel
import com.om.diucampusschedule.ui.viewmodel.ValidationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
    validationViewModel: ValidationViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val validationState by validationViewModel.uiState.collectAsStateWithLifecycle()
    val user = authState.user
    
    // State for editing
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var profilePictureUrl by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Software Engineering") }
    var role by remember { mutableStateOf(UserRole.STUDENT) }
    var batch by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var labSection by remember { mutableStateOf("") }
    var initial by remember { mutableStateOf("") }
    
    // Validation states
    var nameError by remember { mutableStateOf<String?>(null) }
    var batchError by remember { mutableStateOf<String?>(null) }
    var sectionError by remember { mutableStateOf<String?>(null) }
    var labSectionError by remember { mutableStateOf<String?>(null) }
    var initialError by remember { mutableStateOf<String?>(null) }
    
    // Initialize form with current user data
    LaunchedEffect(user) {
        user?.let {
            name = it.name
            profilePictureUrl = it.profilePictureUrl
            department = it.department
            role = it.role
            batch = it.batch
            section = it.section
            labSection = it.labSection
            initial = it.initial
        }
    }
    
    // Auto-dismiss success message after 3 seconds
    LaunchedEffect(authState.successMessage) {
        if (authState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearSuccessMessage()
        }
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            profilePictureUrl = it.toString()
        }
    }
    
    // Show sign out confirmation dialog
    var showSignOutDialog by remember { mutableStateOf(false) }
    
    // Load validation data when user or department changes
    LaunchedEffect(user?.department) {
        user?.department?.let { department ->
            validationViewModel.loadValidationData(department)
        }
    }
    
    // Get current validation data
    val validationData = validationState.validationData
    
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
    
    DIUCampusScheduleTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePictureUrl.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(profilePictureUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(
                                        4.dp,
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .border(
                                        4.dp,
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Default Profile",
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Camera icon for editing
                        if (isEditing) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Name and Role
                    Text(
                        text = if (name.isNotEmpty()) name else "User Name",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${role.name} • $department",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Edit/Save/Cancel buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isEditing) Arrangement.spacedBy(12.dp) else Arrangement.Center
            ) {
                if (isEditing) {
                    // Cancel button
                    OutlinedButton(
                        onClick = {
                            isEditing = false
                            // Reset form to original values
                            user?.let {
                                name = it.name
                                profilePictureUrl = it.profilePictureUrl
                                department = it.department
                                role = it.role
                                batch = it.batch
                                section = it.section
                                labSection = it.labSection
                                initial = it.initial
                            }
                            // Clear validation errors
                            nameError = null
                            batchError = null
                            sectionError = null
                            labSectionError = null
                            initialError = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel")
                    }
                    
                    // Save button
                    Button(
                        onClick = {
                            if (isFormValid()) {
                                val form = UserRegistrationForm(
                                    name = name.trim(),
                                    profilePictureUrl = profilePictureUrl,
                                    department = department,
                                    role = role,
                                    batch = batch.trim().uppercase(),
                                    section = section.trim().uppercase(),
                                    labSection = labSection.trim().uppercase(),
                                    initial = initial.trim().uppercase()
                                )
                                viewModel.updateUserProfile(form)
                                isEditing = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !authState.isLoading && isFormValid()
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save")
                        }
                    }
                } else {
                    // Edit button
                    Button(
                        onClick = { isEditing = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Profile Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Profile Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Name Field
                    ProfileField(
                        label = "Full Name",
                        value = name,
                        onValueChange = { name = it },
                        isEditing = isEditing,
                        icon = Icons.Default.Person,
                        errorMessage = nameError,
                        onValidate = { input ->
                            val error = validateName(input)
                            nameError = error
                            error
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Department Field
                    ProfileField(
                        label = "Department",
                        value = department,
                        onValueChange = { department = it },
                        isEditing = false, // Keep department read-only for now
                        icon = Icons.Default.School
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Role Selection
                    Text(
                        text = "Role",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    if (isEditing) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        role = UserRole.STUDENT
                                        // Clear validation errors when role changes
                                        batchError = null
                                        sectionError = null
                                        labSectionError = null
                                        initialError = null
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                RadioButton(
                                    selected = role == UserRole.STUDENT,
                                    onClick = { role = UserRole.STUDENT }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Student",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        role = UserRole.TEACHER
                                        // Clear validation errors when role changes
                                        batchError = null
                                        sectionError = null
                                        labSectionError = null
                                        initialError = null
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                RadioButton(
                                    selected = role == UserRole.TEACHER,
                                    onClick = { role = UserRole.TEACHER }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Teacher",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        Text(
                            text = role.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Role-specific fields
                    // Show loading indicator if validation data is loading
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
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    if (role == UserRole.STUDENT) {
                        // Student fields
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProfileField(
                                    label = "Batch",
                                    value = batch,
                                    onValueChange = { batch = it },
                                    isEditing = isEditing,
                                    icon = Icons.Default.Groups,
                                    keyboardType = KeyboardType.Number,
                                    placeholder = "59",
                                    errorMessage = batchError,
                                    onValidate = { input ->
                                        val error = validateBatch(input)
                                        batchError = error
                                        error
                                    },
                                    helperText = if (validationData.validBatches.isNotEmpty()) {
                                        "Available batches: ${validationData.validBatches.sorted().joinToString(", ")}"
                                    } else null
                                )
                            }
                            
                            Box(modifier = Modifier.weight(1f)) {
                                ProfileField(
                                    label = "Section",
                                    value = section,
                                    onValueChange = { section = it },
                                    isEditing = isEditing,
                                    icon = Icons.Default.Class,
                                    placeholder = "A",
                                    errorMessage = sectionError,
                                    onValidate = { input ->
                                        val error = validateSection(input)
                                        sectionError = error
                                        error
                                    },
                                    helperText = if (batch.isNotBlank() && validationData.getSectionsForBatch(batch).isNotEmpty()) {
                                        "Available sections for batch $batch: ${validationData.getSectionsForBatch(batch).sorted().joinToString(", ")}"
                                    } else null
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ProfileField(
                            label = "Lab Section",
                            value = labSection,
                            onValueChange = { labSection = it },
                            isEditing = isEditing,
                            icon = Icons.Default.Science,
                            placeholder = "A1",
                            errorMessage = labSectionError,
                            onValidate = { input ->
                                val error = validateLabSection(input)
                                labSectionError = error
                                error
                            },
                            helperText = if (validationData.validLabSections.isNotEmpty()) {
                                "Available lab sections: ${validationData.validLabSections.sorted().joinToString(", ")}"
                            } else null
                        )
                    } else {
                        // Teacher fields
                        ProfileField(
                            label = "Initial",
                            value = initial,
                            onValueChange = { initial = it },
                            isEditing = isEditing,
                            icon = Icons.Default.Badge,
                            placeholder = "ABC",
                            errorMessage = initialError,
                            onValidate = { input ->
                                val error = validateInitial(input)
                                initialError = error
                                error
                            },
                            helperText = if (validationData.validTeacherInitials.isNotEmpty()) {
                                "Available teacher initials: ${validationData.validTeacherInitials.sorted().joinToString(", ")}"
                            } else null
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Actions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Account Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Sign Out Button
                    OutlinedButton(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out")
                    }
                }
            }
            
            // Error Message
            if (authState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = authState.error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Success Message
            if (authState.successMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = authState.successMessage!!,
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Your routine will update automatically",
                            color = Color(0xFF2E7D32).copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Sign Out Confirmation Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text("Sign Out")
            },
            text = {
                Text("Are you sure you want to sign out?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.signOut()
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignOutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = "",
    errorMessage: String? = null,
    helperText: String? = null,
    onValidate: ((String) -> String?)? = null
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    onValueChange(newValue)
                    onValidate?.invoke(newValue)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = errorMessage != null,
                supportingText = when {
                    errorMessage != null -> {
                        {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    helperText != null -> {
                        {
                            Text(
                                text = helperText,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    else -> null
                },
                colors = OutlinedTextFieldDefaults.colors(
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error,
                    errorLeadingIconColor = MaterialTheme.colorScheme.error
                )
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (value.isNotEmpty()) value else "Not set",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isNotEmpty()) MaterialTheme.colorScheme.onSurface 
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
