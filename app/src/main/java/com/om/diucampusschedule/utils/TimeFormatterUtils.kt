package com.om.diucampusschedule.utils

import android.util.Log
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility object for robust time formatting across different Android devices and locales.
 * Addresses compatibility issues with devices like Xiaomi that may have different locale settings.
 */
object TimeFormatterUtils {
    private const val TAG = "TimeFormatterUtils"
    
    /**
     * Creates a robust DateTimeFormatter that works across different devices and locales.
     * Falls back through multiple formatting options to ensure compatibility.
     */
    fun createRobustTimeFormatter(): DateTimeFormatter {
        return try {
            // Try with US locale first (most common format expected by users)
            DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create US locale formatter, falling back to device locale")
            try {
                // Fall back to device locale
                DateTimeFormatter.ofPattern("hh:mm a")
            } catch (e2: Exception) {
                Log.w(TAG, "Failed to create device locale formatter, falling back to 24-hour format")
                // Final fallback to 24-hour format
                DateTimeFormatter.ofPattern("HH:mm")
            }
        }
    }
    
    /**
     * Formats a LocalTime using the robust formatter with fallback options.
     * Returns a formatted time string or "N/A" if formatting fails completely.
     */
    fun formatTime(time: LocalTime?): String {
        if (time == null) return ""
        
        return try {
            // First try with US locale 12-hour format
            time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
        } catch (e: Exception) {
            Log.w(TAG, "US locale formatting failed, trying device locale")
            try {
                // Try with device locale
                time.format(DateTimeFormatter.ofPattern("hh:mm a"))
            } catch (e2: Exception) {
                Log.w(TAG, "Device locale formatting failed, trying 24-hour format")
                try {
                    // Try 24-hour format
                    time.format(DateTimeFormatter.ofPattern("HH:mm"))
                } catch (e3: Exception) {
                    Log.e(TAG, "All time formatting attempts failed", e3)
                    // Final fallback - manual formatting
                    val hour = if (time.hour == 0) 12 else if (time.hour > 12) time.hour - 12 else time.hour
                    val amPm = if (time.hour < 12) "AM" else "PM"
                    String.format("%02d:%02d %s", hour, time.minute, amPm)
                }
            }
        }
    }
}