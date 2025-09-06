package com.om.diucampusschedule.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.core.error.AppError
import com.om.diucampusschedule.core.service.CourseNameService
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.domain.model.User
import com.om.diucampusschedule.domain.usecase.auth.GetCurrentUserUseCase
import com.om.diucampusschedule.domain.usecase.routine.GetUserRoutineForDayUseCase
import com.om.diucampusschedule.ui.screens.today.components.CourseUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class TodayUiState(
    val isLoading: Boolean = false,
    val routineItems: List<RoutineItem> = emptyList(),
    val currentUser: User? = null,
    val error: AppError? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val courseNames: Map<String, String> = emptyMap() // Cache for course names
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserRoutineForDayUseCase: GetUserRoutineForDayUseCase,
    private val courseNameService: CourseNameService
) : ViewModel() {
    
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    
    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()
    
    init {
        observeUserAndRoutineData()
    }
    
    private fun observeUserAndRoutineData() {
        combine(
            getCurrentUserUseCase.observeCurrentUser(),
            _selectedDate
        ) { user, date ->
            Pair(user, date)
        }.onEach { (user, date) ->
            if (user != null) {
                _uiState.value = _uiState.value.copy(
                    currentUser = user,
                    selectedDate = date,
                    isLoading = true,
                    error = null
                )
                loadRoutineForDay(user, date)
            } else {
                _uiState.value = _uiState.value.copy(
                    currentUser = null,
                    routineItems = emptyList(),
                    isLoading = false,
                    selectedDate = date
                )
            }
        }.catch { throwable ->
            val error = AppError.fromThrowable(throwable)
            _uiState.value = _uiState.value.copy(
                error = error,
                isLoading = false
            )
        }.launchIn(viewModelScope)
    }
    
    private fun loadRoutineForDay(user: User, date: LocalDate) {
        val dayName = date.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH))
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            android.util.Log.d("TodayViewModel", "Loading routine for user: ${user.name}")
            android.util.Log.d("TodayViewModel", "User details - Batch: '${user.batch}', Section: '${user.section}', LabSection: '${user.labSection}'")
            android.util.Log.d("TodayViewModel", "Filtering for day: $dayName")
            
            getUserRoutineForDayUseCase(user, dayName).fold(
                onSuccess = { routineItems ->
                    android.util.Log.d("TodayViewModel", "Received ${routineItems.size} routine items for $dayName")
                    routineItems.forEach { item ->
                        android.util.Log.d("TodayViewModel", "  Item: ${item.courseCode} | Section: ${item.section} | Time: ${item.time}")
                    }
                    
                    // Load course names for all routine items
                    loadCourseNames(routineItems)
                    
                    _uiState.value = _uiState.value.copy(
                        routineItems = routineItems.sortedBy { it.startTime },
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { throwable ->
                    android.util.Log.e("TodayViewModel", "Error loading routine", throwable)
                    val error = AppError.fromThrowable(throwable)
                    _uiState.value = _uiState.value.copy(
                        error = error,
                        isLoading = false,
                        routineItems = emptyList()
                    )
                }
            )
        }
    }
    
    private suspend fun loadCourseNames(routineItems: List<RoutineItem>) {
        val currentNames = _uiState.value.courseNames.toMutableMap()
        
        routineItems.forEach { item ->
            if (!currentNames.containsKey(item.courseCode)) {
                val courseName = courseNameService.getCourseName(item.courseCode)
                if (courseName != null) {
                    currentNames[item.courseCode] = courseName
                    android.util.Log.d("TodayViewModel", "Loaded course name: ${item.courseCode} -> $courseName")
                } else {
                    // If no course name found, keep the course code as fallback
                    currentNames[item.courseCode] = item.courseCode
                    android.util.Log.d("TodayViewModel", "No course name found for: ${item.courseCode}, using code as fallback")
                }
            }
        }
        
        _uiState.value = _uiState.value.copy(courseNames = currentNames)
        
        // Also update the CourseUtils cache for ClassRoutineCard compatibility
        CourseUtils.setCourseNames(currentNames)
    }
    
    fun getCourseName(courseCode: String): String {
        return _uiState.value.courseNames[courseCode] ?: courseCode
    }
    
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }
    
    fun resetToToday() {
        _selectedDate.value = LocalDate.now()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun retryLastAction() {
        val currentState = _uiState.value
        if (currentState.currentUser != null) {
            viewModelScope.launch {
                loadRoutineForDay(currentState.currentUser, currentState.selectedDate)
            }
        }
    }
}
