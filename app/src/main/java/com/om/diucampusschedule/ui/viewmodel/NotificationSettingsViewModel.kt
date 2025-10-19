package com.om.diucampusschedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.core.permission.NotificationPermissionManager
import com.om.diucampusschedule.domain.usecase.notification.ManageNotificationPreferencesUseCase
import com.om.diucampusschedule.domain.usecase.exam.GetExamModeInfoUseCase
import com.om.diucampusschedule.domain.usecase.routine.GetMaintenanceInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * ViewModel for managing notification preferences
 */
@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val manageNotificationPreferencesUseCase: ManageNotificationPreferencesUseCase,
    private val notificationPermissionManager: NotificationPermissionManager,
    private val getExamModeInfoUseCase: GetExamModeInfoUseCase,
    private val getMaintenanceInfoUseCase: GetMaintenanceInfoUseCase
) : ViewModel() {
    
    /**
     * Current state of class reminders
     */
    val isClassRemindersEnabled: StateFlow<Boolean> = 
        manageNotificationPreferencesUseCase.isClassRemindersEnabled()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = true
            )
    
    /**
     * Whether notification permission has been requested before
     */
    val hasNotificationPermissionBeenRequested: StateFlow<Boolean> = 
        manageNotificationPreferencesUseCase.hasNotificationPermissionBeenRequested()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )
    
    /**
     * Current notification permission state (real-time)
     */
    val hasNotificationPermission: StateFlow<Boolean> = 
        notificationPermissionManager.permissionState
            .map { it.hasNotificationPermission }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )
    
    /**
     * Current exam mode state (polled every 30 seconds)
     */
    val isExamMode: StateFlow<Boolean> = flow {
        while (true) {
            try {
                val examModeInfo = getExamModeInfoUseCase().getOrNull()
                emit(examModeInfo?.isExamMode ?: false)
            } catch (e: Exception) {
                emit(false)
            }
            delay(30_000) // Check every 30 seconds
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    /**
     * Current maintenance mode state (polled every 30 seconds)
     */
    val isMaintenanceMode: StateFlow<Boolean> = flow {
        while (true) {
            try {
                val maintenanceInfo = getMaintenanceInfoUseCase().getOrNull()
                emit(maintenanceInfo?.isMaintenanceMode ?: false)
                emit(maintenanceInfo?.isSemesterBreak ?: false)
            } catch (e: Exception) {
                emit(false)
            }
            delay(30_000) // Check every 30 seconds
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    /**
     * Current semester break state (polled every 30 seconds)
     */
    val isSemesterBreak: StateFlow<Boolean> = flow {
        while (true) {
            try {
                val maintenanceInfo = getMaintenanceInfoUseCase().getOrNull()
                emit(maintenanceInfo?.isSemesterBreak ?: false)
            } catch (e: Exception) {
                emit(false)
            }
            delay(30_000) // Check every 30 seconds
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    
    /**
     * Toggle class reminders on/off
     */
    fun toggleClassReminders(enabled: Boolean) {
        viewModelScope.launch {
            manageNotificationPreferencesUseCase.toggleClassReminders(enabled)
        }
    }
    
    /**
     * Mark notification permission as requested
     */
    fun markNotificationPermissionRequested() {
        viewModelScope.launch {
            manageNotificationPreferencesUseCase.markNotificationPermissionRequested()
        }
    }
}
