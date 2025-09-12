package com.om.diucampusschedule.ui.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.om.diucampusschedule.domain.model.RoutineItem
import com.om.diucampusschedule.ui.screens.routine.getBreakCounsellingText
import com.om.diucampusschedule.ui.viewmodel.RoutineFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.EnumMap
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.Q)
suspend fun generateRoutineImage(
    context: Context,
    routineItems: List<RoutineItem>,
    role: String,
    batch: String,
    section: String,
    teacherInitial: String,
    room: String,
    startTimes: List<String>,
    effectiveFrom: String?,
    defaultFilterText: String?,
    currentFilter: RoutineFilter?,
    snackbarHostState: SnackbarHostState,
    onImageSaved: (Uri, String) -> Unit,
    currentUser: com.om.diucampusschedule.domain.model.User?,
) {
    withContext(Dispatchers.IO) {
        try {
            // Image dimensions
            val imageWidth = 1200
            val days = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
            val qrCodeSize = 800 // Much bigger QR code size to fill empty space

            // Calculate precise height based on actual content
            val headerHeight = 180f // Title + filter info + margin to table
            val tableHeight = 80f + (days.size * 80f) // Header row + day rows
            val qrSectionHeight = 40f + qrCodeSize + 30f + 60f // Margin + QR + spacing + text
            val dynamicHeight = (headerHeight + tableHeight + qrSectionHeight).toInt()

            val bitmap = createBitmap(imageWidth, dynamicHeight)
            val canvas = Canvas(bitmap)

            // Background
            canvas.drawColor(Color.WHITE)

            // Paint configurations
            val titlePaint = Paint().apply {
                textSize = 48f
                color = Color.BLACK
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            val headerPaint = Paint().apply {
                textSize = 32f
                color = Color.BLACK
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            val textPaint = Paint().apply {
                textSize = 20f
                color = Color.BLACK
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val cellPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = Color.BLACK
                isAntiAlias = true
            }

            val dayPaint = Paint().apply {
                textSize = 24f
                color = Color.rgb(33, 150, 243) // Blue color
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }



            // Title
            val yPosition = 80f
            canvas.drawText(
                "DIU Class Routine (SWE)",
                imageWidth / 2f,
                yPosition,
                titlePaint
            )

            // Filter information
            val defaultUser = defaultFilterText?.uppercase()
            val filterInfo = "${currentFilter?.getDisplayText()?.uppercase() ?: defaultUser}, Effective From: ${effectiveFrom ?: "N/A"}"

            canvas.drawText(
                "Filter: $filterInfo",
                imageWidth / 2f,
                yPosition + 40f,
                headerPaint
            )
            canvas.drawText(
                "Provided by DIU Campus Schedule",
                imageWidth / 2f,
                yPosition + 75f,
                headerPaint
            )

            // Table setup
            val marginHorizontal = 40f
            val marginVertical = 180f
            var currentY = marginVertical
            val cellHeight = 80f
            val dayColumnWidth = 120f
            val timeColumnWidth = max(140f, (imageWidth - dayColumnWidth - marginHorizontal * 2) / startTimes.size.toFloat())

            val tableStartX = marginHorizontal

            // Draw table headers
            var currentX = tableStartX

            // Day/Time header
            canvas.drawRect(
                currentX,
                currentY,
                currentX + dayColumnWidth,
                currentY + cellHeight,
                cellPaint
            )
            canvas.drawText(
                "D/T",
                currentX + dayColumnWidth / 2f,
                currentY + cellHeight / 2f + 8f,
                headerPaint
            )

            currentX += dayColumnWidth

            // Time headers
            startTimes.forEach { time ->
                canvas.drawRect(
                    currentX,
                    currentY,
                    currentX + timeColumnWidth,
                    currentY + cellHeight,
                    cellPaint
                )
                canvas.drawText(
                    time,
                    currentX + timeColumnWidth / 2f,
                    currentY + cellHeight / 2f + 8f,
                    headerPaint
                )
                currentX += timeColumnWidth
            }

            currentY += cellHeight

            // Draw table rows for each day
            days.forEach { day ->
                val routinesForDay = routineItems.filter { it.day.equals(day, ignoreCase = true) }
                currentX = tableStartX

                // Day column
                canvas.drawRect(
                    currentX,
                    currentY,
                    currentX + dayColumnWidth,
                    currentY + cellHeight,
                    cellPaint
                )
                canvas.drawText(
                    day.take(3).uppercase(),
                    currentX + dayColumnWidth / 2f,
                    currentY + cellHeight / 2f + 8f,
                    dayPaint
                )

                currentX += dayColumnWidth

                if (routinesForDay.isEmpty()) {
                    // Draw "OFF DAY" spanning all time columns
                    canvas.drawRect(
                        currentX,
                        currentY,
                        currentX + (timeColumnWidth * startTimes.size),
                        currentY + cellHeight,
                        cellPaint
                    )
                    canvas.drawText(
                        "OFF DAY",
                        currentX + (timeColumnWidth * startTimes.size) / 2f,
                        currentY + cellHeight / 2f + 8f,
                        headerPaint
                    )
                } else {
                    // Draw time slot cells
                    startTimes.forEach { time ->
                        canvas.drawRect(
                            currentX,
                            currentY,
                            currentX + timeColumnWidth,
                            currentY + cellHeight,
                            cellPaint
                        )

                        val routinesForCell = routinesForDay.filter { it.time.startsWith(time) }
                        var textY = currentY + 25f

                        if (routinesForCell.isNotEmpty()) {
                            routinesForCell.forEach { routine ->
                                val courseText = "${routine.courseCode} - ${routine.room}"
                                val batchText = "(${routine.batch}_${routine.section})"

                                // Draw course code and room
                                canvas.drawText(
                                    courseText,
                                    currentX + 5f,
                                    textY,
                                    textPaint
                                )

                                // Draw batch and section
                                textPaint.textSize = 16f
                                canvas.drawText(
                                    batchText,
                                    currentX + 5f,
                                    textY + 20f,
                                    textPaint
                                )
                                textPaint.textSize = 20f

                                textY += 40f
                            }
                        } else {
                            // Check if this is a break time
                            val firstClass = routinesForDay.minByOrNull { it.startTime!! }
                            val lastClass = routinesForDay.maxByOrNull { it.endTime!! }

                            if (firstClass != null && lastClass != null) {
                                val currentTimeSlot = try {
                                    java.time.LocalTime.parse(time, java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.US))
                                } catch (e: Exception) { null }

                                if (currentTimeSlot != null &&
                                    currentTimeSlot.isAfter(firstClass.startTime) &&
                                    currentTimeSlot.isBefore(lastClass.endTime)) {
                                    canvas.drawText(
                                        getBreakCounsellingText(currentUser, currentFilter),
                                        currentX + timeColumnWidth / 2f - 30f,
                                        currentY + cellHeight / 2f + 8f,
                                        textPaint
                                    )
                                }
                            }
                        }

                        currentX += timeColumnWidth
                    }
                }

                currentY += cellHeight
            }

            // Generate and draw QR code below the table
            val qrCodeBitmap = generateQRCode("https://play.google.com/store/apps/details?id=com.om.diucampusschedule", qrCodeSize)
            qrCodeBitmap?.let {
                val qrX = (imageWidth - it.width) / 2f // Center horizontally
                val qrY = currentY + 40f // Position below table with some margin
                canvas.drawBitmap(it, qrX, qrY, null)

                // Add label below QR code
                val labelPaint = Paint().apply {
                    textSize = 60f
                    color = Color.BLACK
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                }
                canvas.drawText(
                    "Download The App",
                    imageWidth / 2f,
                    qrY + it.height + 30f,
                    labelPaint
                )
            }

            // Save the image
            val fileName = "DIU_CS_Routine_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.jpg"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            if (uri != null) {
                try {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        onImageSaved(uri, fileName)
                        snackbarHostState.showSnackbar(
                            message = "$fileName saved to Pictures",
                            withDismissAction = true,
                            duration = SnackbarDuration.Long
                        )
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Error saving image", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Error saving image", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Error generating image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// Function to generate QR code bitmap (reused from PDF generation)
private fun generateQRCode(content: String, size: Int): Bitmap? {
    return try {
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.MARGIN] = 1

        val writer = QRCodeWriter()
        val bitMatrix: BitMatrix = writer.encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )

        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = createBitmap(width, height)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
