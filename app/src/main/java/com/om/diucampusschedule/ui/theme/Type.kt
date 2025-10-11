package com.om.diucampusschedule.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.R

// Custom Font Families using your Roboto Condensed fonts
val RobotoCondensedFontFamily = FontFamily(
    Font(R.font.roboto_condensed_thin, FontWeight.Thin),
    Font(R.font.roboto_condensed_thin_italic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.roboto_condensed_extralight, FontWeight.ExtraLight),
    Font(R.font.roboto_condensed_extralight_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.roboto_condensed_light, FontWeight.Light),
    Font(R.font.roboto_condensed_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.roboto_condensed_regular, FontWeight.Normal),
    Font(R.font.roboto_condensed_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.roboto_condensed_medium, FontWeight.Medium),
    Font(R.font.roboto_condensed_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.roboto_condensed_semibold, FontWeight.SemiBold),
    Font(R.font.roboto_condensed_semibold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.roboto_condensed_bold, FontWeight.Bold),
    Font(R.font.roboto_condensed_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.roboto_condensed_extrabold, FontWeight.ExtraBold),
    Font(R.font.roboto_condensed_extrabold_italic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.roboto_condensed_black, FontWeight.Black),
    Font(R.font.roboto_condensed_black_italic, FontWeight.Black, FontStyle.Italic)
)

// Professional font families for different use cases
val HeadingFontFamily = RobotoCondensedFontFamily // Condensed for headlines
val BodyFontFamily = RobotoCondensedFontFamily // Condensed for body text too
val DisplayFontFamily = RobotoCondensedFontFamily // Condensed for display text
val MonospaceFontFamily = FontFamily.Monospace // For code/time displays

// Academic Typography Scale - Optimized for readability and professionalism with Roboto Condensed
val Typography = Typography(
// Display Styles - For major headings and hero text
    displayLarge = TextStyle(
        fontFamily = DisplayFontFamily, // Clean, professional display
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

// Headline Styles - For section headers and important titles
    headlineLarge = TextStyle(
        fontFamily = HeadingFontFamily, // Professional headers with Roboto Condensed
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

// Title Styles - For card headers and subsection titles
    titleLarge = TextStyle(
        fontFamily = BodyFontFamily, // Readable, modern titles with Roboto Condensed
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

// Body Styles - For main content and reading text
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily, // Highly readable for body text with Roboto Condensed
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

// Label Styles - For buttons, badges, and UI elements
    labelLarge = TextStyle(
        fontFamily = BodyFontFamily, // Clear labels and buttons with Roboto Condensed
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Additional Custom Text Styles for Academic App with Roboto Condensed
val AcademicTextStyles = object {
    // For time displays (class schedules, deadlines)
    val timeDisplay = TextStyle(
        fontFamily = MonospaceFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    // For course codes (CS101, MATH201, etc.)
    val courseCode = TextStyle(
        fontFamily = RobotoCondensedFontFamily, // Use condensed for compact course codes
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 1.sp
    )

    // For GPA and grade displays
    val gradeDisplay = TextStyle(
        fontFamily = RobotoCondensedFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    )

    // For academic calendar dates
    val calendarDate = TextStyle(
        fontFamily = RobotoCondensedFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )

    // For class names and subject titles
    val classTitle = TextStyle(
        fontFamily = RobotoCondensedFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )

    // For teacher names and instructor info
    val instructorName = TextStyle(
        fontFamily = RobotoCondensedFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    )

    // For room numbers and location info
    val locationInfo = TextStyle(
        fontFamily = RobotoCondensedFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    )
}

// Custom font family function for consistent usage across the app
@Composable
fun customFontFamily(): FontFamily {
    return RobotoCondensedFontFamily
}

// Helper functions to access specific font families
object DIUFonts {
    val heading = HeadingFontFamily
    val body = BodyFontFamily
    val display = DisplayFontFamily
    val monospace = MonospaceFontFamily
    val robotoCondensed = RobotoCondensedFontFamily
}