package com.om.diucampusschedule.ui.components


import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
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
import com.om.diucampusschedule.ui.viewmodel.RoutineFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.EnumMap

@RequiresApi(Build.VERSION_CODES.Q)
suspend fun generateRoutinePdf(
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
    onPdfSaved: (Uri, String) -> Unit
) {
    withContext(Dispatchers.IO) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.BLACK
            strokeWidth = 0.3f
        }
        val headerBgPaint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.LTGRAY
        }
        val dayColumnBgPaint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }

        val titlePaint = Paint().apply {
            textAlign = Paint.Align.CENTER
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }

        val textPaint = Paint().apply {
            textSize = 9f
            color = Color.BLACK
            textAlign = Paint.Align.LEFT
        }

        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val boldTextPaint = Paint().apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Generate QR code for Play Store link
        val playStoreLink = "https://play.google.com/store/apps/details?id=com.om.diucampusschedule"
        val qrCodeBitmap = generateQRCode(playStoreLink, 80)

        // Draw QR code in top right corner
        if (qrCodeBitmap != null) {
            // Position in top right with some margin
            val qrX = pageInfo.pageWidth - qrCodeBitmap.width - 20f
            val qrY = 20f
            canvas.drawBitmap(qrCodeBitmap, qrX, qrY, null)

            // Add label below QR code
            val labelPaint = Paint().apply {
                textSize = 8f
                color = Color.BLACK
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "Scan for App",
                qrX + qrCodeBitmap.width / 2f,
                qrY + qrCodeBitmap.height + 10f,
                labelPaint
            )
        }

    val marginHorizontal = 10f
    val marginVertical = 100f
    var yPosition = marginVertical + 40f
    val cellHeight = 30f
    val dayColumnWidth = 90f // Increased from 90f
    val timeColumnWidth = 110f // Increased from 100f
    val tableTotalWidth = dayColumnWidth + (startTimes.size * timeColumnWidth)
    val tableStartX = (pageInfo.pageWidth - tableTotalWidth) / 2f
    var currentX = tableStartX


        // Table Headers
        val daysHeader = "Day/Time"
        canvas.drawText(
            "DIU Class Routine (SWE)",
            pageInfo.pageWidth / 2f,
            marginVertical + 15f,
            titlePaint
        )

        val defaultUser = defaultFilterText?.uppercase()
        val filterInfo = "${currentFilter?.getDisplayText()?.uppercase() ?: defaultUser}, Effective From: ${effectiveFrom ?: "N/A"}"

        canvas.drawText(
            "Filter: $filterInfo",
            pageInfo.pageWidth / 2f,
            marginVertical + 30f,
            headerPaint
        )
        canvas.drawText(
            "Provided by DIU Campus Schedule",
            pageInfo.pageWidth / 2f,
            marginVertical + 45f,
            headerPaint
        )

        yPosition += 30f

        // Day/Time header cell
        canvas.drawRect(
            tableStartX,
            yPosition,
            tableStartX + dayColumnWidth,
            yPosition + cellHeight,
            headerBgPaint
        ) // Use tableStartX
        canvas.drawRect(
            tableStartX,
            yPosition,
            tableStartX + dayColumnWidth,
            yPosition + cellHeight,
            paint
        ) // Use tableStartX
        canvas.drawText(
            daysHeader,
            tableStartX + dayColumnWidth / 2f,
            yPosition + cellHeight / 2f + 3f,
            headerPaint
        ) // Center text in Day/Time cell

        currentX = tableStartX + dayColumnWidth // Update currentX correctly

        startTimes.forEach { time ->
            canvas.drawRect(
                currentX,
                yPosition,
                currentX + timeColumnWidth,
                yPosition + cellHeight,
                headerBgPaint
            )
            canvas.drawRect(
                currentX,
                yPosition,
                currentX + timeColumnWidth,
                yPosition + cellHeight,
                paint
            )
            canvas.drawText(
                time,
                currentX + timeColumnWidth / 2f,
                yPosition + cellHeight / 2f + 3f,
                headerPaint
            )
            currentX += timeColumnWidth
        }

        yPosition += cellHeight
        val daysOfWeek = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

        daysOfWeek.forEach { day ->
            currentX = tableStartX // Reset currentX for each day row
            canvas.drawRect(
                currentX,
                yPosition,
                currentX + dayColumnWidth,
                yPosition + cellHeight,
                dayColumnBgPaint
            )
            canvas.drawRect(
                currentX,
                yPosition,
                currentX + dayColumnWidth,
                yPosition + cellHeight,
                paint
            )
            canvas.drawText(
                day,
                currentX + dayColumnWidth / 2f,
                yPosition + cellHeight / 2f + 3f,
                boldTextPaint
            )
            currentX += dayColumnWidth

            val dayRoutines = routineItems.filter { it.day.equals(day, ignoreCase = true) }

            if (dayRoutines.isEmpty()) {
                // If no routines for this day, print "OFF DAY" across all time columns
                canvas.drawRect(
                    currentX,
                    yPosition,
                    currentX + (timeColumnWidth * startTimes.size),
                    yPosition + cellHeight,
                    paint
                )

                val offDayPaint = Paint().apply {
                    textSize = 12f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                    color = Color.GRAY
                }

                canvas.drawText(
                    "OFF DAY",
                    currentX + (timeColumnWidth * startTimes.size) / 2f,
                    yPosition + cellHeight / 2f + 3f,
                    offDayPaint
                )

                currentX += timeColumnWidth * startTimes.size
            } else {
                startTimes.forEach { time ->
                    canvas.drawRect(
                        currentX,
                        yPosition,
                        currentX + timeColumnWidth,
                        yPosition + cellHeight,
                        paint
                    )
                    val routinesForCell =
                        routineItems.filter { it.day.equals(day, ignoreCase = true) && it.time.startsWith(time) }
                    var cellTextY = yPosition + 6f

                    if (routinesForCell.isNotEmpty()) {
                        routinesForCell.forEach { routine ->
                            val courseText =
                                "${routine.courseCode}(${routine.teacherInitial}) - ${routine.room} (${routine.batch}_${routine.section})"
                            canvas.drawText(courseText, currentX + 2f, cellTextY + 3f, textPaint)
                            cellTextY += 14f
                        }
                    }
                    currentX += timeColumnWidth
                }
            }
            yPosition += cellHeight
        }

        document.finishPage(page)

        val fileName = "DIU_CS_${
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        }.pdf"
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(
                MediaStore.Downloads.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            ) // Optional, but good practice
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    document.writeTo(outputStream)
                }
                document.close()

                CoroutineScope(Dispatchers.Main).launch {
                    onPdfSaved(uri, fileName)
                    snackbarHostState.showSnackbar(
                        message = "$fileName saved to Downloads",
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
                document.close()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Error saving PDF", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            document.close()
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Error saving PDF", Toast.LENGTH_SHORT)
                    .show() // Failed to get Uri
            }
        }
    }
}

// Function to generate QR code bitmap
private fun generateQRCode(content: String, size: Int): Bitmap? {
    try {
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.MARGIN] = 1 // Set narrow margin

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

        return bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}