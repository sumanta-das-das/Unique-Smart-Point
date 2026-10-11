package com.example.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

object QrUtils {

    /**
     * Generates a 2D QR Code style matrix bitmap from content string.
     * Uses deterministic pseudo-random module distribution based on content hash
     * with authentic QR finder patterns in the corners.
     */
    fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val darkPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = false
        }

        val grid = 29 // 29x29 QR version 3 grid
        val moduleSize = size.toFloat() / grid.toFloat()

        // Draw 3 Corner Finder Patterns (Top-Left, Top-Right, Bottom-Left)
        drawFinderPattern(canvas, 0f, 0f, moduleSize, darkPaint)
        drawFinderPattern(canvas, (grid - 7) * moduleSize, 0f, moduleSize, darkPaint)
        drawFinderPattern(canvas, 0f, (grid - 7) * moduleSize, moduleSize, darkPaint)

        // Deterministic bit generator from content hash
        val bytes = content.toByteArray(Charsets.UTF_8)
        var hash = 5381L
        for (b in bytes) {
            hash = ((hash shl 5) + hash) + b
        }

        for (row in 0 until grid) {
            for (col in 0 until grid) {
                // Skip finder pattern zones
                if ((row < 8 && col < 8) || (row < 8 && col >= grid - 8) || (row >= grid - 8 && col < 8)) {
                    continue
                }
                // Timing lines (row 6 and col 6 alternating)
                if (row == 6 || col == 6) {
                    if ((row + col) % 2 == 0) {
                        canvas.drawRect(
                            col * moduleSize,
                            row * moduleSize,
                            (col + 1) * moduleSize,
                            (row + 1) * moduleSize,
                            darkPaint
                        )
                    }
                    continue
                }

                // Data module distribution
                val bitSeed = (row * 37 + col * 19 + hash).hashCode()
                val isDark = (bitSeed and 0x01) == 1
                if (isDark) {
                    canvas.drawRect(
                        col * moduleSize,
                        row * moduleSize,
                        (col + 1) * moduleSize,
                        (row + 1) * moduleSize,
                        darkPaint
                    )
                }
            }
        }

        return bitmap
    }

    private fun drawFinderPattern(canvas: Canvas, left: Float, top: Float, moduleSize: Float, darkPaint: Paint) {
        val lightPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        // 7x7 outer dark square
        canvas.drawRect(left, top, left + 7 * moduleSize, top + 7 * moduleSize, darkPaint)
        // 5x5 light square
        canvas.drawRect(left + moduleSize, top + moduleSize, left + 6 * moduleSize, top + 6 * moduleSize, lightPaint)
        // 3x3 inner dark square
        canvas.drawRect(left + 2 * moduleSize, top + 2 * moduleSize, left + 5 * moduleSize, top + 5 * moduleSize, darkPaint)
    }

    /**
     * Generates a 1D Barcode bitmap (Code 128 style).
     */
    fun generateBarcodeBitmap(content: String, width: Int = 600, height: Int = 200): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val darkPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = false
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val margin = 30f
        val barcodeHeight = height - 50f
        val availableWidth = width - 2 * margin

        val barsCount = 65
        val barUnit = availableWidth / barsCount

        var x = margin
        val hash = content.hashCode().toLong()

        for (i in 0 until barsCount) {
            val bit = ((hash shr (i % 31)) and 1L) == 1L || (i % 3 == 0)
            val barWidth = if (bit) barUnit * 0.8f else barUnit * 0.3f
            if (bit || i % 2 == 0) {
                canvas.drawRect(x, 20f, x + barWidth, barcodeHeight, darkPaint)
            }
            x += barUnit
        }

        canvas.drawText(content.take(20), width / 2f, height - 10f, textPaint)
        return bitmap
    }
}
