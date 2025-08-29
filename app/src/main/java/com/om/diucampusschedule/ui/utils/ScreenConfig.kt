package com.om.diucampusschedule.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Screen configuration utilities for consistent padding and layout handling across different screens.
 * 
 * This helper ensures that screens have proper spacing and window insets handling without
 * conflicting with TopAppBars or other system UI elements.
 * 
 * Usage Examples:
 * 
 * // For screens WITH TopAppBar (like TodayScreen, RoutineScreen):
 * Column(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .background(MaterialTheme.colorScheme.background)
 *         .run { ScreenConfig.run { withTopAppBar() } }
 * ) {
 *     TopAppBar(
 *         // ... your top app bar content
 *         windowInsets = ScreenConfig.getTopAppBarWindowInsets(handleStatusBar = true)
 *     )
 *     // ... rest of your content
 * }
 * 
 * // For screens WITHOUT TopAppBar (like ProfileScreen, auth screens):
 * Column(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .background(MaterialTheme.colorScheme.background)
 *         .run { ScreenConfig.run { withoutTopAppBar() } }
 * ) {
 *     // ... your content
 * }
 */
object ScreenConfig {
    
    /**
     * Modifier for screens that have their own TopAppBar.
     * This removes status bar padding to prevent double padding issues since
     * the TopAppBar will handle the status bar insets.
     */
    fun Modifier.withTopAppBar(): Modifier = this
    
    /**
     * Modifier for screens that don't have a TopAppBar and need to handle status bar insets themselves.
     * This adds proper status bar padding for full-screen layouts.
     */
    @Composable
    fun Modifier.withoutTopAppBar(): Modifier = this.windowInsetsPadding(WindowInsets.statusBars)
    
    /**
     * Get the appropriate WindowInsets for TopAppBar based on screen configuration.
     * 
     * @param handleStatusBar Whether the TopAppBar should handle status bar insets.
     * For most main app screens, this should be true.
     * For modal/dialog screens or custom implementations, this might be false.
     */
    @Composable
    fun getTopAppBarWindowInsets(handleStatusBar: Boolean = true): WindowInsets {
        return if (handleStatusBar) {
            WindowInsets.statusBars
        } else {
            WindowInsets(0)
        }
    }
    
    /**
     * Common modifier patterns for different screen types
     */
    object Modifiers {
        /**
         * Standard modifier for main app screens with TopAppBar
         */
        fun Modifier.mainAppScreen(): Modifier = this.run { ScreenConfig.run { withTopAppBar() } }
        
        /**
         * Standard modifier for full-screen content without TopAppBar
         */
        @Composable
        fun Modifier.fullScreen(): Modifier = this.run { ScreenConfig.run { withoutTopAppBar() } }
    }
}
