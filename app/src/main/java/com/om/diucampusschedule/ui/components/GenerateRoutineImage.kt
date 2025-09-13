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
import com.om.diucampusschedule.R
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

            // --- MODERN LANDSCAPE DESIGN SETUP ---
            val imageWidth = 1920
            val imageHeight = 1080
            val days = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
            val qrCodeSize = 180

            val bitmap = createBitmap(imageWidth, imageHeight)
            val canvas = Canvas(bitmap)

            // --- Modern Gradient Background ---
            val bgPaint = Paint()
            val gradient = android.graphics.LinearGradient(
                0f, 0f, imageWidth.toFloat(), imageHeight.toFloat(),
                Color.parseColor("#1976D2"), // App primary
                Color.parseColor("#42A5F5"), // App secondary
                android.graphics.Shader.TileMode.CLAMP
            )
            bgPaint.shader = gradient
            canvas.drawRect(0f, 0f, imageWidth.toFloat(), imageHeight.toFloat(), bgPaint)

            // --- Card for Table ---
            val cardPaint = Paint().apply {
                color = Color.WHITE
                isAntiAlias = true
                setShadowLayer(24f, 0f, 8f, Color.argb(80, 33, 150, 243))
            }
            // Card position fixed, add extra space above only for heading
            val cardPadding = 40f
            val cardTop = 220f
            val cardLeft = cardPadding
            val cardRight = imageWidth - cardPadding
            val cardBottom = imageHeight - 60f
            val cardRadius = 48f
            val cardRect = android.graphics.RectF(cardLeft, cardTop, cardRight, cardBottom)
            canvas.drawRoundRect(cardRect, cardRadius, cardRadius, cardPaint)
            val headingExtraSpace = 80f
            // Heading area baseline
            val headingBaseY = 60f

            // --- Title and Header ---
            val titlePaint = Paint().apply {
                textSize = 72f
                color = Color.WHITE
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.LEFT
                isAntiAlias = true
                setShadowLayer(12f, 0f, 4f, Color.argb(120, 33, 150, 243))
            }
            val headerPaint = Paint().apply {
                textSize = 36f
                color = Color.WHITE
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.LEFT
                isAntiAlias = true
            }
            val subtitlePaint = Paint().apply {
                textSize = 32f
                color = Color.WHITE
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
                isAntiAlias = true
            }

//            // App Logo (use app icon drawable)
//            val logoRadius = 48f
//            val logoCenterX = cardLeft + logoRadius + 24f
//            val logoCenterY = headingBaseY + logoRadius
//            val logoSize = (logoRadius * 2).toInt()
//            val logoBitmap = try {
//                val drawable = context.getDrawable(R.drawable.dcs_logo)
//                val bmp = if (drawable != null) {
//                    val outBmp = Bitmap.createBitmap(logoSize, logoSize, Bitmap.Config.ARGB_8888)
//                    val canvasLogo = Canvas(outBmp)
//                    drawable.setBounds(0, 0, logoSize, logoSize)
//                    drawable.draw(canvasLogo)
//                    outBmp
//                } else null
//                bmp
//            } catch (e: Exception) { null }
//            logoBitmap?.let {
//                canvas.drawBitmap(
//                    it,
//                    logoCenterX - logoRadius,
//                    logoCenterY - logoRadius,
//                    null
//                )
//            }


            // Title
            val titleY = headingBaseY + 12f
            canvas.drawText("DIU Class Routine (SWE)", cardLeft + 140f, titleY, titlePaint)

            // Filter and effective info
            val defaultUser = defaultFilterText?.uppercase()
            val filterInfo = "${currentFilter?.getDisplayText()?.uppercase() ?: defaultUser}, Effective From: ${effectiveFrom ?: "N/A"}"
            canvas.drawText("Filtered: $filterInfo", cardLeft + 140f, titleY + 48f, headerPaint)
            canvas.drawText("Provided by DIU Campus Schedule", cardLeft + 140f, titleY + 90f, subtitlePaint)


            // --- Modern Table Drawing ---
            // Table now matches card padding
            val tableMargin = 48f
            val tableLeft = cardLeft + tableMargin
            val tableTop = cardTop + tableMargin
            val tableRight = cardRight - tableMargin
            val tableBottom = cardBottom - tableMargin
            val tableWidth = tableRight - tableLeft
            val tableHeight = tableBottom - tableTop

            val cellHeight = 90f
            val dayColumnWidth = 170f
            val timeColumnWidth = ((tableWidth - dayColumnWidth) / startTimes.size.toFloat()).coerceAtLeast(120f)

            // Header row background
            val headerRowPaint = Paint().apply {
                color = Color.parseColor("#1976D2") // App primary
                isAntiAlias = true
            }
            val headerTextPaint = Paint().apply {
                textSize = 32f
                color = Color.WHITE
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val cellBgPaint = Paint().apply {
                color = Color.parseColor("#F5F7FA")
                isAntiAlias = true
            }
            val cellBorderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f
                color = Color.parseColor("#90CAF9")
                isAntiAlias = true
            }
            val dayTextPaint = Paint().apply {
                textSize = 28f
                color = Color.parseColor("#1976D2")
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val cellTextPaint = Paint().apply {
                textSize = 22f
                color = Color.parseColor("#263238")
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
                isAntiAlias = true
            }

            // Draw header row (rounded)
            var currentY = tableTop
            var currentX = tableLeft
            val headerRadius = 24f
            // Day/Time header cell
            val headerCellRect = android.graphics.RectF(currentX, currentY, currentX + dayColumnWidth, currentY + cellHeight)
            canvas.drawRoundRect(headerCellRect, headerRadius, headerRadius, headerRowPaint)
            canvas.drawText("D/T", currentX + dayColumnWidth / 2f, currentY + cellHeight / 2f + 12f, headerTextPaint)
            currentX += dayColumnWidth
            // Time headers
            startTimes.forEach { time ->
                val rect = android.graphics.RectF(currentX, currentY, currentX + timeColumnWidth, currentY + cellHeight)
                canvas.drawRoundRect(rect, headerRadius, headerRadius, headerRowPaint)
                canvas.drawText(time, currentX + timeColumnWidth / 2f, currentY + cellHeight / 2f + 12f, headerTextPaint)
                currentX += timeColumnWidth
            }
            // Table rows
            currentY += cellHeight
            days.forEach { day ->
                currentX = tableLeft
                // Day cell
                val dayCellRect = android.graphics.RectF(currentX, currentY, currentX + dayColumnWidth, currentY + cellHeight)
                canvas.drawRoundRect(dayCellRect, headerRadius, headerRadius, cellBgPaint)
                canvas.drawRoundRect(dayCellRect, headerRadius, headerRadius, cellBorderPaint)
                canvas.drawText(day.take(3).uppercase(), currentX + dayColumnWidth / 2f, currentY + cellHeight / 2f + 10f, dayTextPaint)
                currentX += dayColumnWidth
                val routinesForDay = routineItems.filter { it.day.equals(day, ignoreCase = true) }
                if (routinesForDay.isEmpty()) {
                    // OFF DAY cell
                    val offRect = android.graphics.RectF(currentX, currentY, currentX + (timeColumnWidth * startTimes.size), currentY + cellHeight)
                    canvas.drawRoundRect(offRect, headerRadius, headerRadius, cellBgPaint)
                    canvas.drawRoundRect(offRect, headerRadius, headerRadius, cellBorderPaint)
                    val offTextPaint = Paint(headerTextPaint).apply { color = Color.parseColor("#B0BEC5"); textSize = 28f }
                    canvas.drawText("OFF DAY", currentX + (timeColumnWidth * startTimes.size) / 2f, currentY + cellHeight / 2f + 10f, offTextPaint)
                } else {
                    startTimes.forEach { time ->
                        val cellRect = android.graphics.RectF(currentX, currentY, currentX + timeColumnWidth, currentY + cellHeight)
                        canvas.drawRoundRect(cellRect, headerRadius, headerRadius, cellBgPaint)
                        canvas.drawRoundRect(cellRect, headerRadius, headerRadius, cellBorderPaint)
                        val routinesForCell = routinesForDay.filter { it.time.startsWith(time) }
                        var textY = currentY + 36f
                        if (routinesForCell.isNotEmpty()) {
                            routinesForCell.forEach { routine ->
                                val courseText = "${routine.courseCode}(${routine.teacherInitial}) - ${routine.room}"
                                val batchText = "(${routine.batch}_${routine.section})"
                                // Draw courseText and batchText on the same line, batchText right-aligned
                                val textYCenter = textY
                                val courseX = currentX + 16f
                                val batchPaint = Paint(cellTextPaint).apply { textSize = 18f; color = Color.parseColor("#1976D2"); textAlign = Paint.Align.RIGHT }
                                val batchX = currentX + timeColumnWidth - 16f
                                // Draw courseText left, batchText right, same line
                                canvas.drawText(courseText, courseX, textYCenter, cellTextPaint)
                                canvas.drawText(batchText, batchX, textYCenter, batchPaint)
                                textY += 40f
                            }
                        } else {
                            // Break/counselling
                            val firstClass = routinesForDay.minByOrNull { it.startTime!! }
                            val lastClass = routinesForDay.maxByOrNull { it.endTime!! }
                            val currentTimeSlot = try {
                                java.time.LocalTime.parse(time, java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.US))
                            } catch (e: Exception) { null }
                            if (firstClass != null && lastClass != null && currentTimeSlot != null &&
                                currentTimeSlot.isAfter(firstClass.startTime) && currentTimeSlot.isBefore(lastClass.endTime)) {
                                val breakPaint = Paint(cellTextPaint).apply { color = Color.parseColor("#FFA726"); textSize = 20f }
                                canvas.drawText(getBreakCounsellingText(currentUser, currentFilter), currentX + timeColumnWidth / 2f - 30f, currentY + cellHeight / 2f + 10f, breakPaint)
                            }
                        }
                        currentX += timeColumnWidth
                    }
                }
                currentY += cellHeight
            }



            // --- Modern QR Code and Footer ---
            val qrCodeBitmap = generateQRCode("https://play.google.com/store/apps/details?id=com.om.diucampusschedule", qrCodeSize)
            qrCodeBitmap?.let {
                val qrX = imageWidth - qrCodeSize - 80f
                val qrY = cardTop - qrCodeSize - 20f
                // QR code shadow
                val qrShadowPaint = Paint().apply {
                    color = Color.argb(60, 33, 150, 243)
                    setShadowLayer(24f, 0f, 8f, Color.argb(80, 33, 150, 243))
                }
                canvas.drawRoundRect(
                    android.graphics.RectF(qrX - 12f, qrY - 12f, qrX + qrCodeSize + 12f, qrY + qrCodeSize + 12f),
                    32f, 32f, qrShadowPaint
                )
                canvas.drawBitmap(it, qrX, qrY, null)
                // QR label
                val labelPaint = Paint().apply {
                    textSize = 36f
                    color = Color.parseColor("#1976D2")
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
//                canvas.drawText(
//                    "Download The App",
//                    qrX + qrCodeSize / 2f,
//                    qrY + qrCodeSize + 44f,
//                    labelPaint
//                )
            }

            // --- Modern Footer ---
            val footerPaint = Paint().apply {
                textSize = 28f
                color = Color.parseColor("#90CAF9")
                textAlign = Paint.Align.LEFT
                isAntiAlias = true
            }
            canvas.drawText(
                "© 2025 DIU Campus Schedule | Powered by NeoTechDev",
                cardLeft + 8f,
                imageHeight - 20f,
                footerPaint
            )

            // --- Subtle Watermark ---
            val watermarkPaint = Paint().apply {
                textSize = 120f
                color = Color.argb(18, 33, 150, 243)
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(
                "DIU Campus Schedule",
                imageWidth / 2f,
                imageHeight / 2f + 120f,
                watermarkPaint
            )

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
