package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.NutritionCheckEntity
import com.example.data.model.UserEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateAndOpenReport(
        context: Context,
        madrasahName: String,
        officerName: String,
        students: List<UserEntity>,
        nutritionChecks: List<NutritionCheckEntity>,
        lowRiskCount: Int,
        attentionCount: Int,
        highRiskCount: Int,
        unmonitoredCount: Int
    ): File? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 standard width in points
        val pageHeight = 842 // A4 standard height in points
        var pageNumber = 1

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        fun drawHeader(canvas: Canvas) {
            // Header Top Bar
            paint.color = Color.rgb(6, 95, 70) // Emerald Dark (#065F46)
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 64f, paint)

            // App Title
            textPaint.color = Color.WHITE
            textPaint.textSize = 15f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("NUTRIMIND AI — PEMANTAUAN GIZI UKS MADRASAH", 25f, 28f, textPaint)

            textPaint.textSize = 10f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Sistem Deteksi Dini & Rekomendasi Gizi Seimbang Siswa Madrasah", 25f, 48f, textPaint)
        }

        fun drawFooter(canvas: Canvas, currentPg: Int) {
            paint.color = Color.rgb(226, 232, 240)
            paint.strokeWidth = 1f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(25f, 805f, (pageWidth - 25).toFloat(), 805f, paint)

            textPaint.color = Color.rgb(100, 116, 139)
            textPaint.textSize = 8f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText(
                "NutriMind AI adalah sarana pemantauan awal & edukasi UKS madrasah, bukan pengganti diagnosis medis dokter.",
                25f,
                818f,
                textPaint
            )

            val pageStr = "Hal. $currentPg"
            val textWidth = textPaint.measureText(pageStr)
            canvas.drawText(pageStr, pageWidth - 25f - textWidth, 818f, textPaint)
        }

        drawHeader(canvas)
        drawFooter(canvas, pageNumber)

        var currentY = 82f

        // --- Metadata Box ---
        paint.color = Color.rgb(248, 250, 252)
        paint.style = Paint.Style.FILL
        val metaRect = RectF(25f, currentY, (pageWidth - 25).toFloat(), currentY + 54f)
        canvas.drawRoundRect(metaRect, 6f, 6f, paint)

        paint.color = Color.rgb(203, 213, 225)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(metaRect, 6f, 6f, paint)

        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.textSize = 10f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Madrasah: $madrasahName", 35f, currentY + 18f, textPaint)
        canvas.drawText("Petugas UKS: $officerName", 35f, currentY + 34f, textPaint)

        val dateStr = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Waktu Unduh: $dateStr WIB", 35f, currentY + 48f, textPaint)

        currentY += 66f

        // --- Summary Statistics Cards ---
        val statCardWidth = (pageWidth - 50f - 24f) / 4f
        val statCardHeight = 44f

        val stats = listOf(
            Triple("Risiko Rendah", "$lowRiskCount", Color.rgb(4, 120, 87)), // Emerald/Green
            Triple("Perhatian", "$attentionCount", Color.rgb(180, 83, 9)),   // Amber
            Triple("Risiko Tinggi", "$highRiskCount", Color.rgb(190, 18, 60)), // Coral/Red
            Triple("Belum Cek", "$unmonitoredCount", Color.rgb(30, 41, 59))  // Slate/Black
        )

        for (i in stats.indices) {
            val (label, count, textColor) = stats[i]
            val startX = 25f + i * (statCardWidth + 8f)
            val statRect = RectF(startX, currentY, startX + statCardWidth, currentY + statCardHeight)

            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRoundRect(statRect, 6f, 6f, paint)

            textPaint.textSize = 14f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.color = textColor
            val countWidth = textPaint.measureText(count)
            canvas.drawText(count, startX + (statCardWidth - countWidth) / 2f, currentY + 20f, textPaint)

            textPaint.textSize = 8.5f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.color = Color.rgb(51, 65, 85)
            val labelWidth = textPaint.measureText(label)
            canvas.drawText(label, startX + (statCardWidth - labelWidth) / 2f, currentY + 36f, textPaint)
        }

        currentY += statCardHeight + 14f

        // --- Table Header ---
        fun drawTableHeader(canvas: Canvas, y: Float) {
            paint.color = Color.rgb(15, 23, 42) // Dark Navy
            paint.style = Paint.Style.FILL
            canvas.drawRect(25f, y, (pageWidth - 25).toFloat(), y + 20f, paint)

            textPaint.color = Color.WHITE
            textPaint.textSize = 8.5f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            canvas.drawText("No", 29f, y + 13f, textPaint)
            canvas.drawText("NIS", 50f, y + 13f, textPaint)
            canvas.drawText("Nama Siswa", 110f, y + 13f, textPaint)
            canvas.drawText("Kelas", 230f, y + 13f, textPaint)
            canvas.drawText("L/P", 265f, y + 13f, textPaint)
            canvas.drawText("IMT", 295f, y + 13f, textPaint)
            canvas.drawText("Status Gizi", 335f, y + 13f, textPaint)
            canvas.drawText("Risiko Gizi", 425f, y + 13f, textPaint)
            canvas.drawText("Tgl Cek", 515f, y + 13f, textPaint)
        }

        drawTableHeader(canvas, currentY)
        currentY += 20f

        // --- Table Rows ---
        val rowHeight = 22f

        for (index in students.indices) {
            val student = students[index]
            val check = nutritionChecks.filter { it.studentId == student.id }.maxByOrNull { it.timestamp }

            // Check if we need a new page
            if (currentY + rowHeight > 790f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas

                drawHeader(canvas)
                drawFooter(canvas, pageNumber)

                currentY = 80f
                drawTableHeader(canvas, currentY)
                currentY += 20f
            }

            // Alternating Row Background
            paint.color = if (index % 2 == 0) Color.rgb(255, 255, 255) else Color.rgb(248, 250, 252)
            paint.style = Paint.Style.FILL
            canvas.drawRect(25f, currentY, (pageWidth - 25).toFloat(), currentY + rowHeight, paint)

            // Row Bottom Border
            paint.color = Color.rgb(226, 232, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawLine(25f, currentY + rowHeight, (pageWidth - 25).toFloat(), currentY + rowHeight, paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 8.5f
            textPaint.color = Color.rgb(15, 23, 42)

            val textY = currentY + 14f

            // No
            canvas.drawText("${index + 1}", 29f, textY, textPaint)
            // NIS
            canvas.drawText(student.studentIdNumber.take(10), 50f, textY, textPaint)
            // Nama
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val nameDisplay = if (student.nameOrInitial.length > 20) student.nameOrInitial.take(19) + "…" else student.nameOrInitial
            canvas.drawText(nameDisplay, 110f, textY, textPaint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            // Kelas
            canvas.drawText(student.className, 230f, textY, textPaint)
            // Gender
            val genderShort = if (student.gender.startsWith("L", ignoreCase = true)) "L" else "P"
            canvas.drawText(genderShort, 268f, textY, textPaint)

            if (check != null) {
                // IMT
                canvas.drawText("${check.bmi}", 295f, textY, textPaint)

                // Status Gizi
                val catShort = check.bmiCategory.replace("Gizi ", "").take(14)
                canvas.drawText(catShort, 335f, textY, textPaint)

                // Risiko Gizi
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                when {
                    check.riskCategory.contains("Tinggi", ignoreCase = true) -> textPaint.color = Color.rgb(190, 18, 60)
                    check.riskCategory.contains("Perhatian", ignoreCase = true) -> textPaint.color = Color.rgb(180, 83, 9)
                    else -> textPaint.color = Color.rgb(4, 120, 87)
                }
                canvas.drawText(check.riskCategory, 425f, textY, textPaint)

                textPaint.color = Color.rgb(71, 85, 105)
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(check.dateString, 515f, textY, textPaint)
            } else {
                textPaint.color = Color.rgb(100, 116, 139)
                canvas.drawText("-", 295f, textY, textPaint)
                canvas.drawText("-", 335f, textY, textPaint)
                textPaint.color = Color.rgb(180, 83, 9)
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Belum Cek", 425f, textY, textPaint)
                textPaint.color = Color.rgb(100, 116, 139)
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("-", 515f, textY, textPaint)
            }

            currentY += rowHeight
        }

        pdfDocument.finishPage(page)

        // Save PDF to documents/downloads directory
        return try {
            val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.filesDir

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val pdfFile = File(docsDir, "Laporan_Gizi_Madrasah_$timeStamp.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            // Open or share the PDF
            openOrSharePdf(context, pdfFile)
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            Toast.makeText(context, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun openOrSharePdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(viewIntent, "Buka Laporan PDF UKS")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback to SEND intent if no specific PDF viewer application is registered
            try {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(shareIntent, "Bagikan Laporan PDF UKS")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (ex: Exception) {
                Toast.makeText(context, "Laporan PDF berhasil disimpan di: ${file.name}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
