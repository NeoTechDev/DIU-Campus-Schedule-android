package com.om.diucampusschedule.ui.theme

import androidx.compose.animation.core.CubicBezierEasing

/**
 * Custom easing curves for animations
 */

// Fixed version of EaseInOutCubic that uses parameters that don't cause crashes
// Using parameters (0.4, 0.0, 0.2, 1.0) which is the same as FastOutSlowInEasing
val EaseInOutCubic = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)