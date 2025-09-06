package com.om.diucampusschedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.core.service.CourseNameService
import com.om.diucampusschedule.domain.model.CourseInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for course search functionality
 */
@HiltViewModel
class CourseSearchViewModel @Inject constructor(
    private val courseNameService: CourseNameService
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<CourseInfo>>(emptyList())
    val searchResults: StateFlow<List<CourseInfo>> = _searchResults.asStateFlow()

    private var searchJob: Job? = null
    private var allCourses: List<CourseInfo> = emptyList()

    init {
        loadAllCourses()
    }

    private fun loadAllCourses() {
        viewModelScope.launch {
            val result = courseNameService.loadCourseNames()
            result.onSuccess { courseMap ->
                allCourses = courseMap.values.toList()
            }
        }
    }

    fun searchCourses(query: String) {
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            // No artificial delay - search immediately
            val filteredCourses = allCourses.filter { course ->
                course.courseCode.contains(query, ignoreCase = true) ||
                course.courseName.contains(query, ignoreCase = true)
            }.take(50) // Limit results for performance
            
            _searchResults.value = filteredCourses
        }
    }

    fun clearResults() {
        searchJob?.cancel()
        _searchResults.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}
