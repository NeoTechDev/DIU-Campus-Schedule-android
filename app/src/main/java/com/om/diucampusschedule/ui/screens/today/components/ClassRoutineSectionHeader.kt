package com.om.diucampusschedule.ui.screens.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.om.diucampusschedule.ui.theme.customFontFamily
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun ClassRoutineSectionHeader(
    count: Int,
    title: String,
    countColor: Color,
    filteredRoutines: List<ClassRoutine>,
    formatter12HourUS: DateTimeFormatter,
    selectedDate: LocalDate? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(26.dp))
                .background(Color.LightGray)
                .then(
                    Modifier.layout { measurable, constraints ->
                        val titlePlaceable = measurable.measure(constraints)
                        val totalWidth = titlePlaceable.width + 10.dp.roundToPx() // Adding start padding
                        val totalHeight = titlePlaceable.height
                        layout(totalWidth, totalHeight) {
                            titlePlaceable.placeRelative(0, 0)
                        }
                    }
                ),
            contentAlignment = Alignment.CenterStart
        ){
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(countColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = customFontFamily()
                )
            }
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(start = 30.dp),
                fontFamily = customFontFamily()
            )
        }
        ClassTimeSection(
            filteredRoutines = filteredRoutines,
            formatter12HourUS = formatter12HourUS,
            selectedDate = selectedDate
        )
    }
}

@Composable
fun ClassTimeSection(
    filteredRoutines: List<ClassRoutine>,
    formatter12HourUS: DateTimeFormatter,
    selectedDate: LocalDate? = null
) {
    if (filteredRoutines.isEmpty()) return

    val sortedRoutines = filteredRoutines.sortedBy { it.startTime }
    val firstClass = sortedRoutines.firstOrNull()
    val lastClass = sortedRoutines.lastOrNull()

    if (firstClass?.startTime == null || lastClass?.endTime == null) return

    // Assuming startTime and endTime in ClassRoutine are already LocalTime
    // If they are ZonedDateTime, use .toLocalTime()
    // If they are String, they need to be parsed to LocalTime first
    val dayStartTimeLocal: LocalTime = firstClass.startTime
    val dayEndTimeLocal: LocalTime = lastClass.endTime

    val dayStartTimeFormatted = dayStartTimeLocal.format(formatter12HourUS)
    val dayEndTimeFormatted = dayEndTimeLocal.format(formatter12HourUS)

    var isCurrentTimeInRange by remember { mutableStateOf(false) }
    var waveData by remember { mutableStateOf<FloatArray?>(null) }

    // Check if the selected date is today
    val isToday = selectedDate?.isEqual(LocalDate.now()) ?: false

    // Effect to determine if current time is within the overall class range
    LaunchedEffect(dayStartTimeLocal, dayEndTimeLocal, isToday) {
        val initialNow = LocalTime.now()
        isCurrentTimeInRange = isToday && initialNow.isAfter(dayStartTimeLocal) && initialNow.isBefore(dayEndTimeLocal)

        while (true) {
            delay(5000L) // Check every 5 seconds instead of 3
            val currentTime = LocalTime.now()
            val currentlyInRange = isToday && currentTime.isAfter(dayStartTimeLocal) && currentTime.isBefore(dayEndTimeLocal)
            if (isCurrentTimeInRange != currentlyInRange) {
                isCurrentTimeInRange = currentlyInRange
            }
        }
    }

    // Effect to generate smooth sinusoidal wave animation data when in range
    LaunchedEffect(isCurrentTimeInRange) {
        if (isCurrentTimeInRange) {
            var animationFrame = 0.0

            // Increased number of vertices for ultra-smooth curves
            val numberOfVertices = 120

            // Refined amplitude for elegant wave effect
            val baseAmplitude = 0.3f

            // Optimized wave parameters for smooth, flowing animation
            val waveFrequency = 8 // Number of complete waves across the width
            val animationSpeed = 0.30 // Speed of wave movement

            while (isCurrentTimeInRange) {
                val newWave = FloatArray(numberOfVertices) { vertexIndex ->
                    val normalizedX = vertexIndex.toDouble() / (numberOfVertices - 1)

                    // Create a smooth flowing sine wave (negative animationFrame for left-to-right movement)
                    val phase = (normalizedX * waveFrequency * 2 * PI) - (animationFrame * animationSpeed)
                    val primaryWave = sin(phase)

                    // Add subtle secondary wave for more natural movement
                    val secondaryPhase = (normalizedX * waveFrequency * 1.3 * 2 * PI) - (animationFrame * animationSpeed * 0.7)
                    val secondaryWave = sin(secondaryPhase) * 0.3

                    // Combine waves with smooth envelope to prevent harsh edges
                    val envelope = sin(normalizedX * PI)
                    val combinedWave = (primaryWave + secondaryWave) * envelope

                    // Apply amplitude
                    (combinedWave * baseAmplitude).toFloat()
                }

                waveData = newWave
                animationFrame += 1.0

                // Smooth 30fps animation
                delay(33L)
            }
        } else {
            waveData = null // Clear data to draw a straight line when not in range
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StartTimeIndicator(
            label = "Start",
            time = dayStartTimeFormatted,
            color = Color(0xFF1A56DB)
        )

        AndroidView(
            factory = { context ->
                AnimatedDividerView(context, null).apply {
                    // Set modern styling
                    setStrokeWidth(2.5f)
                }
            },
            update = { view ->
                // This is called when waveData changes
                view.updateData(waveData)
                // Dynamic color based on state
                val lineColor = if (isCurrentTimeInRange) {
                    0xFF4285F4.toInt() // Modern blue when active
                } else {
                    Color.DarkGray.toArgb() // Light gray when inactive // Light gray when inactive
                }
                view.setLineColor(lineColor)
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .height(30.dp) // Slightly increased height for better wave visibility
        )

        EndTimeIndicator(
            label = "End",
            time = dayEndTimeFormatted,
            color = Color(0xFF0EA5E9)
        )
    }
}

@Composable
private fun StartTimeIndicator(
    label: String,
    time: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontFamily = customFontFamily()
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = customFontFamily()
            )
        }
    }
}

@Composable
private fun EndTimeIndicator(
    label: String,
    time: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontFamily = customFontFamily()
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = customFontFamily()
            )
        }
        Spacer(modifier = Modifier.width(4.dp))

        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}
