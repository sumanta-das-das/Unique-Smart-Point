package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageProcessor {

    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun resizeByPixels(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val w = if (targetWidth > 0) targetWidth else 100
        val h = if (targetHeight > 0) targetHeight else 100
        return Bitmap.createScaledBitmap(bitmap, w, h, true)
    }

    fun resizeByPercentage(bitmap: Bitmap, percentage: Float): Bitmap {
        val factor = (percentage / 100f).coerceIn(0.05f, 5.0f)
        val w = (bitmap.width * factor).toInt().coerceAtLeast(10)
        val h = (bitmap.height * factor).toInt().coerceAtLeast(10)
        return Bitmap.createScaledBitmap(bitmap, w, h, true)
    }

    fun compressBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 80): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(format, quality.coerceIn(1, 100), stream)
        return stream.toByteArray()
    }

    fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun flip(bitmap: Bitmap, horizontal: Boolean): Bitmap {
        val matrix = Matrix().apply {
            if (horizontal) postScale(-1f, 1f) else postScale(1f, -1f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun cropToRatio(bitmap: Bitmap, targetAspectWidth: Float, targetAspectHeight: Float): Bitmap {
        val targetRatio = targetAspectWidth / targetAspectHeight
        val currentRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        val cropWidth: Int
        val cropHeight: Int
        val startX: Int
        val startY: Int

        if (currentRatio > targetRatio) {
            cropHeight = bitmap.height
            cropWidth = (cropHeight * targetRatio).toInt()
            startX = (bitmap.width - cropWidth) / 2
            startY = 0
        } else {
            cropWidth = bitmap.width
            cropHeight = (cropWidth / targetRatio).toInt()
            startX = 0
            startY = (bitmap.height - cropHeight) / 2
        }

        return Bitmap.createBitmap(bitmap, startX, startY, cropWidth, cropHeight)
    }

    fun addWatermark(bitmap: Bitmap, text: String, colorArgb: Int = Color.WHITE, alpha: Int = 180, position: String = "Center"): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            color = colorArgb
            this.alpha = alpha
            textSize = (bitmap.width * 0.05f).coerceAtLeast(24f)
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val x: Float = result.width / 2f
        val y: Float = when (position) {
            "Top" -> result.height * 0.15f
            "Bottom" -> result.height * 0.90f
            else -> result.height / 2f
        }

        canvas.drawText(text, x, y, paint)
        return result
    }

    /**
     * Passport Photo generator:
     * Crops image to 3.5 x 4.5 ratio, replaces/tints background if requested, and adds clean border.
     */
    fun createPassportPhoto(
        source: Bitmap,
        targetWidthPx: Int = 413, // ~3.5cm @ 300dpi
        targetHeightPx: Int = 531, // ~4.5cm @ 300dpi
        backgroundColor: Int = Color.WHITE,
        addBorder: Boolean = true
    ): Bitmap {
        // Crop source to 3.5 : 4.5 aspect ratio
        val cropped = cropToRatio(source, 3.5f, 4.5f)
        val scaled = Bitmap.createScaledBitmap(cropped, targetWidthPx, targetHeightPx, true)

        val output = Bitmap.createBitmap(targetWidthPx, targetHeightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Draw solid background
        canvas.drawColor(backgroundColor)
        canvas.drawBitmap(scaled, 0f, 0f, null)

        // Add 1px thin border for clean cutting guide
        if (addBorder) {
            val borderPaint = Paint().apply {
                color = Color.LTGRAY
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRect(0f, 0f, targetWidthPx.toFloat(), targetHeightPx.toFloat(), borderPaint)
        }
        return output
    }

    /**
     * Generates a 4x6 inch print sheet with a grid of 8 passport photos (2 rows x 4 columns).
     */
    fun generate4x6PrintSheet(passportPhoto: Bitmap): Bitmap {
        val sheetWidth = 1800 // 6 inches @ 300 dpi
        val sheetHeight = 1200 // 4 inches @ 300 dpi

        val sheet = Bitmap.createBitmap(sheetWidth, sheetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sheet)
        canvas.drawColor(Color.WHITE)

        val photoWidth = 350
        val photoHeight = 450
        val scaledPhoto = Bitmap.createScaledBitmap(passportPhoto, photoWidth, photoHeight, true)

        val cols = 4
        val rows = 2
        val gapX = (sheetWidth - (cols * photoWidth)) / (cols + 1)
        val gapY = (sheetHeight - (rows * photoHeight)) / (rows + 1)

        val guidePaint = Paint().apply {
            color = Color.rgb(220, 220, 220)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = gapX + c * (photoWidth + gapX)
                val top = gapY + r * (photoHeight + gapY)
                canvas.drawBitmap(scaledPhoto, left.toFloat(), top.toFloat(), null)
                canvas.drawRect(left.toFloat(), top.toFloat(), (left + photoWidth).toFloat(), (top + photoHeight).toFloat(), guidePaint)
            }
        }

        // Header watermark
        val titlePaint = Paint().apply {
            color = Color.GRAY
            textSize = 24f
            isAntiAlias = true
        }
        canvas.drawText("UNIQUE SMART POINT - 4x6 Photo Sheet (8 Photos)", 40f, 50f, titlePaint)

        return sheet
    }

    /**
     * Applies studio filters: Sharpen / Brightness / Contrast / B&W / Pencil Sketch.
     */
    fun applyFilter(bitmap: Bitmap, filterType: String): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint().apply { isAntiAlias = true }

        when (filterType) {
            "Grayscale", "B&W" -> {
                val cm = ColorMatrix().apply { setSaturation(0f) }
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
            }
            "High Contrast" -> {
                val cm = ColorMatrix(floatArrayOf(
                    1.4f, 0f, 0f, 0f, -40f,
                    0f, 1.4f, 0f, 0f, -40f,
                    0f, 0f, 1.4f, 0f, -40f,
                    0f, 0f, 0f, 1f, 0f
                ))
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
            }
            "Vibrant" -> {
                val cm = ColorMatrix().apply { setSaturation(1.5f) }
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
            }
            "Pencil Sketch" -> {
                // High contrast inverted/grayscale simulation
                val cm = ColorMatrix(floatArrayOf(
                    -0.8f, -0.8f, -0.8f, 0f, 255f,
                    -0.8f, -0.8f, -0.8f, 0f, 255f,
                    -0.8f, -0.8f, -0.8f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                ))
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
            }
            else -> {
                canvas.drawBitmap(bitmap, 0f, 0f, null)
            }
        }
        return result
    }

    fun extractPalette(bitmap: Bitmap): List<Int> {
        val colors = mutableListOf<Int>()
        val scaled = Bitmap.createScaledBitmap(bitmap, 16, 16, false)
        for (y in 0 until 16 step 4) {
            for (x in 0 until 16 step 4) {
                val pixel = scaled.getPixel(x, y)
                if (!colors.contains(pixel)) colors.add(pixel)
            }
        }
        return colors.take(6)
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap, filename: String): File {
        val dir = File(context.cacheDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        return file
    }

    fun createCollage(
        bitmaps: List<Bitmap>,
        layout: String = "4_grid",
        spacingPx: Int = 16,
        backgroundColor: Int = Color.WHITE,
        canvasWidth: Int = 1080,
        canvasHeight: Int = 1080
    ): Bitmap {
        val output = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(backgroundColor)
        if (bitmaps.isEmpty()) return output

        val s = spacingPx.toFloat()
        when (layout) {
            "2_split_v" -> {
                val b1 = bitmaps[0]
                val b2 = bitmaps.getOrElse(1) { bitmaps[0] }
                val halfW = (canvasWidth - 3 * s) / 2f
                val h = canvasHeight - 2 * s

                val r1 = RectF(s, s, s + halfW, s + h)
                val r2 = RectF(s + halfW + s, s, s + 2 * halfW + s, s + h)
                drawFittedBitmap(canvas, b1, r1)
                drawFittedBitmap(canvas, b2, r2)
            }
            "2_split_h" -> {
                val b1 = bitmaps[0]
                val b2 = bitmaps.getOrElse(1) { bitmaps[0] }
                val w = canvasWidth - 2 * s
                val halfH = (canvasHeight - 3 * s) / 2f

                val r1 = RectF(s, s, s + w, s + halfH)
                val r2 = RectF(s, s + halfH + s, s + w, s + 2 * halfH + s)
                drawFittedBitmap(canvas, b1, r1)
                drawFittedBitmap(canvas, b2, r2)
            }
            "3_grid" -> {
                val b1 = bitmaps[0]
                val b2 = bitmaps.getOrElse(1) { bitmaps[0] }
                val b3 = bitmaps.getOrElse(2) { bitmaps[0] }
                val w = canvasWidth - 2 * s
                val halfH = (canvasHeight - 3 * s) / 2f
                val halfW = (canvasWidth - 3 * s) / 2f

                val rTop = RectF(s, s, s + w, s + halfH)
                val rBottom1 = RectF(s, s + halfH + s, s + halfW, s + 2 * halfH + s)
                val rBottom2 = RectF(s + halfW + s, s + halfH + s, s + 2 * halfW + s, s + 2 * halfH + s)

                drawFittedBitmap(canvas, b1, rTop)
                drawFittedBitmap(canvas, b2, rBottom1)
                drawFittedBitmap(canvas, b3, rBottom2)
            }
            else -> { // "4_grid"
                val halfW = (canvasWidth - 3 * s) / 2f
                val halfH = (canvasHeight - 3 * s) / 2f
                val b1 = bitmaps[0]
                val b2 = bitmaps.getOrElse(1) { bitmaps[0] }
                val b3 = bitmaps.getOrElse(2) { bitmaps[0] }
                val b4 = bitmaps.getOrElse(3) { bitmaps[0] }

                val r1 = RectF(s, s, s + halfW, s + halfH)
                val r2 = RectF(s + halfW + s, s, s + 2 * halfW + s, s + halfH)
                val r3 = RectF(s, s + halfH + s, s + halfW, s + 2 * halfH + s)
                val r4 = RectF(s + halfW + s, s + halfH + s, s + 2 * halfW + s, s + 2 * halfH + s)

                drawFittedBitmap(canvas, b1, r1)
                drawFittedBitmap(canvas, b2, r2)
                drawFittedBitmap(canvas, b3, r3)
                drawFittedBitmap(canvas, b4, r4)
            }
        }
        return output
    }

    private fun drawFittedBitmap(canvas: Canvas, bitmap: Bitmap, rect: RectF) {
        val srcRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val destRatio = rect.width() / rect.height()

        val srcCrop: Rect
        if (srcRatio > destRatio) {
            val newWidth = (bitmap.height * destRatio).toInt()
            val xOff = (bitmap.width - newWidth) / 2
            srcCrop = Rect(xOff, 0, xOff + newWidth, bitmap.height)
        } else {
            val newHeight = (bitmap.width / destRatio).toInt()
            val yOff = (bitmap.height - newHeight) / 2
            srcCrop = Rect(0, yOff, bitmap.width, yOff + newHeight)
        }

        canvas.save()
        canvas.clipRect(rect)
        canvas.drawBitmap(bitmap, srcCrop, rect, null)
        canvas.restore()
    }
}
