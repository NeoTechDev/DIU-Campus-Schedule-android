package com.om.diucampusschedule.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.om.diucampusschedule.core.network.rememberConnectivityState
import com.om.diucampusschedule.core.validation.DataValidator
import com.om.diucampusschedule.core.validation.DynamicDataValidator
import com.om.diucampusschedule.domain.model.UserRegistrationForm
import com.om.diucampusschedule.domain.model.UserRole
import com.om.diucampusschedule.ui.components.LabSectionChipDisplay
import com.om.diucampusschedule.ui.components.LabSectionChipGroup
import com.om.diucampusschedule.ui.components.NetworkStatusMessage
import com.om.diucampusschedule.ui.navigation.Screen
import com.om.diucampusschedule.ui.theme.DIUCampusScheduleTheme
import com.om.diucampusschedule.ui.utils.ScreenConfig
import com.om.diucampusschedule.ui.viewmodel.AuthViewModel
import com.om.diucampusschedule.ui.viewmodel.ValidationViewModel
import com.om.diucampusschedule.util.ImageUploadUtil
import kotlinx.coroutines.launch

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
    var initialError by remember { mutableStateOf<String?>(null) }

    // Google Sign-In client for sign out
    val context = LocalContext.current
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.om.diucampusschedule.R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // State for image upload
    var isUploadingImage by remember { mutableStateOf(false) }

    // Coroutine scope for image upload
    val scope = rememberCoroutineScope()

    // Network connectivity state
    val isConnected = rememberConnectivityState()

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
        uri?.let { selectedUri ->
            scope.launch {
                isUploadingImage = true
                try {
                    val uploadResult = ImageUploadUtil.uploadProfilePicture(
                        context = context,
                        imageUri = selectedUri
                    )

                    uploadResult.fold(
                        onSuccess = { downloadUrl ->
                            if (profilePictureUrl.isNotEmpty() && !ImageUploadUtil.isLocalUri(profilePictureUrl)) {
                                ImageUploadUtil.deleteProfilePicture(profilePictureUrl)
                            }
                            profilePictureUrl = downloadUrl
                        },
                        onFailure = { exception ->
                            android.util.Log.e("ProfileScreen", "Image upload failed", exception)
                            profilePictureUrl = selectedUri.toString()
                        }
                    )
                } finally {
                    isUploadingImage = false
                }
            }
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
                (role != UserRole.STUDENT || section.isBlank() || labSection.isNotBlank())
    }

    DIUCampusScheduleTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding() // This adds padding when keyboard appears
                    .navigationBarsPadding() // This handles navigation bar padding
                    .run { ScreenConfig.run { withoutTopAppBar() } }
                    .verticalScroll(rememberScrollState())
            ) {
                // Network Status (only if not connected)
                if (!isConnected) {
                    NetworkStatusMessage(
                        isConnected = isConnected,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                // Profile Header with modern design
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture with gradient border
                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Gradient border
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    val gradient = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF6366F1),
                                            Color(0xFF8B5CF6),
                                            Color(0xFFEC4899),
                                            Color(0xFFF59E0B)
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, size.height)
                                    )
                                    drawCircle(
                                        brush = gradient,
                                        style = Stroke(width = 4.dp.toPx())
                                    )
                                }
                        )

                        // Profile picture content
                        Box(
                            modifier = Modifier.size(80.dp),
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
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.surfaceVariant,
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Default Profile",
                                        modifier = Modifier.size(36.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Edit camera icon with modern design
                            if (isEditing) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                                )
                                            )
                                        )
                                        .clickable {
                                            if (!isUploadingImage) {
                                                imagePickerLauncher.launch("image/*")
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isUploadingImage) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = Color.White,
                                            strokeWidth = 1.5.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Change Photo",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Name with enhanced typography
                    Text(
                        text = name.ifEmpty { "User Name" },
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    user?.email?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    // Role and Department with modern chip design
                    Surface(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${role.name.lowercase().capitalize()} • $department",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Modern Edit/Save/Cancel Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isEditing) Arrangement.spacedBy(12.dp) else Arrangement.Center
                    ) {
                        if (isEditing) {
                            OutlinedButton(
                                onClick = {
                                    isEditing = false
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
                                    nameError = null
                                    batchError = null
                                    sectionError = null
                                    initialError = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Text("Cancel", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium))
                            }

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
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                enabled = !authState.isLoading && isFormValid() && isConnected,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                if (authState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 1.5.dp
                                    )
                                } else {
                                    Text("Save", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium))
                                }
                            }
                        } else {
                            Button(
                                onClick = { isEditing = true },
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Profile", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium))
                            }
                        }
                    }
                }

                // Profile Information with modern card design
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Profile Information",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Name Field
                        ModernProfileInfoItem(
                            label = "Name",
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // Department Field
                        ModernProfileInfoItem(
                            label = "Department",
                            value = department,
                            onValueChange = { },
                            isEditing = false,
                            icon = Icons.Default.School
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Role Selection with modern chips
                        Column {
                            Text(
                                text = "Role",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (isEditing) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        onClick = {
                                            role = UserRole.STUDENT
                                            batchError = null
                                            sectionError = null
                                            initialError = null
                                        },
                                        label = {
                                            Text("Student", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium))
                                        },
                                        selected = role == UserRole.STUDENT,
                                        shape = RoundedCornerShape(16.dp)
                                    )

                                    FilterChip(
                                        onClick = {
                                            role = UserRole.TEACHER
                                            batchError = null
                                            sectionError = null
                                            initialError = null
                                            batch = ""
                                            section = ""
                                            labSection = ""
                                        },
                                        label = {
                                            Text("Teacher", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium))
                                        },
                                        selected = role == UserRole.TEACHER,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ) {
                                    Text(
                                        text = role.name.lowercase().capitalize(),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Loading indicator for validation data
                        if (validationState.isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 1.5.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Loading validation data...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Role-specific fields
                        if (role == UserRole.STUDENT) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    ModernProfileInfoItem(
                                        label = "Batch",
                                        value = batch,
                                        onValueChange = { batch = it },
                                        isEditing = isEditing,
                                        icon = Icons.Default.Groups,
                                        keyboardType = KeyboardType.Number,
                                        placeholder = "e.g. 41",
                                        errorMessage = batchError,
                                        onValidate = { input ->
                                            val error = validateBatch(input)
                                            batchError = error
                                            error
                                        }
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    ModernProfileInfoItem(
                                        label = "Section",
                                        value = section,
                                        onValueChange = { newValue ->
                                            section = newValue
                                            if (labSection.isNotBlank()) {
                                                val expectedLabSections = validationData.getLabSectionsForMainSection(newValue)
                                                if (!expectedLabSections.contains(labSection)) {
                                                    labSection = ""
                                                }
                                            }
                                        },
                                        isEditing = isEditing,
                                        icon = Icons.Default.Class,
                                        placeholder = "e.g. J",
                                        errorMessage = sectionError,
                                        onValidate = { input ->
                                            val error = validateSection(input)
                                            sectionError = error
                                            error
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Lab Section
                            Column {
                                Text(
                                    text = "Lab Section",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                if (isEditing) {
                                    LabSectionChipGroup(
                                        mainSection = section,
                                        selectedLabSection = labSection,
                                        onLabSectionSelected = { selectedLabSection ->
                                            labSection = selectedLabSection
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = section.isNotBlank(),
                                        showLabel = false
                                    )
                                } else {
                                    if (labSection.isNotEmpty()) {
                                        LabSectionChipDisplay(
                                            selectedLabSection = labSection
                                        )
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        ) {
                                            Text(
                                                text = "Not selected",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Teacher fields
                            ModernProfileInfoItem(
                                label = "Initial",
                                value = initial,
                                onValueChange = { initial = it },
                                isEditing = isEditing,
                                icon = Icons.Default.Badge,
                                placeholder = "e.g. MBM",
                                errorMessage = initialError,
                                onValidate = { input ->
                                    val error = validateInitial(input)
                                    initialError = error
                                    error
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Account section with modern design
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Account",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedButton(
                            onClick = { showSignOutDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            enabled = isConnected,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign Out", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Floating Success/Error Messages at the top
            AnimatedVisibility(
                visible = authState.error != null || authState.successMessage != null,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(10f)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(12.dp)
            ) {
                when {
                    authState.error != null -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = authState.error!!,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }

                    authState.successMessage != null -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = authState.successMessage!!,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                    )
                                    Text(
                                        text = "Your routine will update automatically",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modern Sign Out Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    "Sign Out",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            },
            text = {
                Text(
                    "Are you sure you want to sign out?",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.signOutWithGoogle(googleSignInClient)
                        // Note: Navigation will be handled automatically by AppInitializationViewModel
                        // when it detects the user is no longer authenticated
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        "Sign Out",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignOutDialog = false },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernProfileInfoItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = "",
    errorMessage: String? = null,
    onValidate: ((String) -> String?)? = null
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
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
                placeholder = {
                    Text(
                        placeholder,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                isError = errorMessage != null,
                supportingText = if (errorMessage != null) {
                    {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                        )
                    }
                } else null,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = value.ifEmpty { "Not set" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (value.isNotEmpty()) FontWeight.Medium else FontWeight.Normal,
                            fontSize = 13.sp
                        ),
                        color = if (value.isNotEmpty()) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}