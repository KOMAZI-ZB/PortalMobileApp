package com.example.portalapp.util

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream
import kotlin.math.max

object PdfUtils {

    /**
     * Builds a simple multi-page PDF table.
     *
     * @param title   Document title
     * @param headers Column headers
     * @param rows    Table rows (each cell is plain text; long text is clipped)
     */
    fun buildSimpleTablePdf(
        title: String,
        headers: List<String>,
        rows: List<List<String>>
    ): ByteArray {
        val pageWidth = 595  // A4 @ 72 dpi: 595x842
        val pageHeight = 842
        val left = 32
        val right = 32
        val top = 40
        val bottom = 40

        val titlePaint = Paint().apply {
            textSize = 18f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerPaint = Paint().apply {
            textSize = 12f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val cellPaint = Paint().apply {
            textSize = 12f
            isAntiAlias = true
        }

        val colCount = headers.size
        val usableWidth = pageWidth - left - right
        val colWidth = usableWidth / max(1, colCount)
        val lineHeight = 20

        val out = ByteArrayOutputStream()
        val doc = PdfDocument()

        var y = top
        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas

        fun newPage() {
            doc.finishPage(page)
            pageNumber += 1
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            y = top
        }

        // Title
        canvas.drawText(title, left.toFloat(), y.toFloat(), titlePaint)
        y += lineHeight + 6

        // Header row
        headers.forEachIndexed { i, h ->
            val x = left + i * colWidth
            canvas.drawText(h, x.toFloat(), y.toFloat(), headerPaint)
        }
        y += lineHeight

        // Divider
        canvas.drawLine(left.toFloat(), y.toFloat(), (pageWidth - right).toFloat(), y.toFloat(), headerPaint)
        y += 10

        rows.forEach { row ->
            // New page if needed
            if (y + lineHeight > pageHeight - bottom) newPage()

            row.forEachIndexed { i, cell ->
                val x = left + i * colWidth
                val text = cell.take(64) // clip long text (simple & safe)
                canvas.drawText(text, x.toFloat(), y.toFloat(), cellPaint)
            }
            y += lineHeight
        }

        doc.finishPage(page)
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }
}
