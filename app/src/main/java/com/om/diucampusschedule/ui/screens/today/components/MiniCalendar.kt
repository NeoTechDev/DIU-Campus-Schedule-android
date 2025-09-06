package com.om.diucampusschedule.ui.screens.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.ui.theme.customFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MiniCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    // Get current date as a reference point
    val today = LocalDate.now()
    
    // Use mutable state for the center date of our range
    var centerDate by remember { mutableStateOf(selectedDate) }
    
    // Generate a range of 61 days (30 before + current + 30 after)
    val dates = remember(centerDate) {
        (-30..30).map { offset ->
            centerDate.plusDays(offset.toLong())
        }
    }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Update center date and scroll position whenever selected date changes
    LaunchedEffect(selectedDate) {
        // If selected date is outside our current range, update the center
        if (!dates.contains(selectedDate)) {
            centerDate = selectedDate
            // After centerDate changes and dates are recalculated, we'll scroll to index 30 (middle)
            coroutineScope.launch {
                // Small delay to ensure the list is recomposed with new dates
                delay(50)
                listState.animateScrollToItem(30)
            }
        } else {
            // If the date is within our current range, just scroll to it
            val index = dates.indexOf(selectedDate)
            if (index != -1) {
                coroutineScope.launch {
                    listState.animateScrollToItem(index)
                }
            }
        }
    }
    
    // Initial scroll to center (selected date or today)
    LaunchedEffect(Unit) {
        // Find index of today or selected date
        val initialIndex = dates.indexOf(selectedDate).takeIf { it != -1 } ?: dates.indexOf(today).takeIf { it != -1 } ?: 30
        listState.scrollToItem(initialIndex)
    }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        state = listState
    ) {
        items(dates.size) { index ->
            val date = dates[index]
            val isSelected = selectedDate == date
            DateItem(
                date = date,
                isSelected = isSelected,
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
fun DateItem(date: LocalDate, isSelected: Boolean, onClick: () -> Unit) {
    val today = LocalDate.now()
    val isToday = date == today

    val dayOfWeekName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val dayOfMonth = date.dayOfMonth

    // Detect if we're in dark mode
    val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
    // Clean, consistent color scheme that adapts to dark/light mode
    val selectedHeaderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    val selectedDateColor = MaterialTheme.colorScheme.primary
    
    // Different background colors for light/dark themes
    // In dark mode, use colors that match the Material dark theme surface colors
    val unselectedHeaderColor = if (isDarkTheme) 
        Color(0xFF2D2D2D) // Darker surface color for dark mode
    else 
        Color(0xFFE0E0E0) // Light gray for light mode
        
    val unselectedDateColor = if (isDarkTheme) 
        Color(0xFF383838) // Slightly lighter than header for dark mode
    else 
        Color(0xFFBDBDBD) // Medium gray for light mode
    
    // Different text colors for light/dark themes
    val selectedTextColor = Color.White
    val unselectedTextColor = if (isDarkTheme) 
        Color(0xFFE0E0E0) // Light gray text for dark mode
    else 
        Color(0xFF424242) // Dark gray text for light mode

    // Improved rounded corners
    val cornerRadius = 15.dp

    Column(
        modifier = Modifier
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(cornerRadius)
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(44.dp)
                // Slightly increased height
                .height(52.dp)
                .border(
                    width = 1.5.dp,
                    color = when {
                        isSelected -> Color.Transparent
                        isToday -> MaterialTheme.colorScheme.primary
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(cornerRadius)
                )
                .background(
                    color = if (isSelected) selectedHeaderColor else unselectedHeaderColor,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .clip(RoundedCornerShape(cornerRadius))
                .clickable { onClick() },
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = dayOfWeekName.uppercase(Locale.ROOT),
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) selectedTextColor else unselectedTextColor,
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontFamily = customFontFamily(),
                modifier = Modifier.padding(top = 3.dp)
            )

            Box(
                modifier = Modifier
                    .width(44.dp)
                    // Increased height of the date section
                    .height(34.dp)
                    .background(
                        color = if (isSelected) selectedDateColor else unselectedDateColor,
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .align(Alignment.BottomCenter),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = selectedTextColor,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = customFontFamily()
                )
            }
        }
    }
}
