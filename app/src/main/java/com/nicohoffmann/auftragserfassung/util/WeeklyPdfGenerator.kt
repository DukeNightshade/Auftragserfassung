package com.nicohoffmann.auftragserfassung.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.draw.LineSeparator
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class WeeklyPdfGenerator(private val context: Context) {

    private val titleFont = Font(Font.FontFamily.HELVETICA, 24f, Font.BOLD)
    private val subTitleFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL, BaseColor.GRAY)
    private val boldFont = Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD)
    private val normalFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL)
    private val headerFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor.WHITE)

    fun generateWeeklyPdf(
        weekStartDate: Date,
        entries: Map<String, List<String>>
    ): File {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
        val fileNameFormat = SimpleDateFormat("dd_MM_yyyy", Locale.GERMAN)
        val weekEnd = Date(weekStartDate.time + 6 * 24 * 60 * 60 * 1000L)
        val fileName = "Wochenbericht_${fileNameFormat.format(weekStartDate)}.pdf"

        // Temporär in cacheDir schreiben (keine Permission nötig)
        val pdfFile = File(context.cacheDir, fileName)

        val document = Document(PageSize.A4, 40f, 40f, 60f, 60f)
        PdfWriter.getInstance(document, FileOutputStream(pdfFile))
        document.open()

        // ── Titel ──
        val title = Paragraph("Wochenbericht", titleFont)
        title.alignment = Element.ALIGN_CENTER
        title.spacingAfter = 4f
        document.add(title)

        // ── Datumsbereich ──
        val dateRange = Paragraph(
            "${dateFormat.format(weekStartDate)} – ${dateFormat.format(weekEnd)}",
            subTitleFont
        )
        dateRange.alignment = Element.ALIGN_CENTER
        dateRange.spacingAfter = 20f
        document.add(dateRange)

        // ── Trennlinie ──
        val separator = Paragraph(Chunk(LineSeparator()))
        separator.spacingAfter = 16f
        document.add(separator)

        // ── Tabelle ──
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(22f, 78f))
        table.spacingBefore = 8f

        // Header
        addHeaderCell(table, "Tag")
        addHeaderCell(table, "Einträge")

        // Wochentage
        val daysOfWeek = listOf(
            "Montag", "Dienstag", "Mittwoch",
            "Donnerstag", "Freitag", "Samstag", "Sonntag"
        )

        daysOfWeek.forEachIndexed { index, day ->
            val bgColor = if (index % 2 == 0) BaseColor.WHITE else BaseColor(245, 245, 245)

            // Tag-Zelle
            val dayCell = PdfPCell(Phrase(day, boldFont))
            dayCell.backgroundColor = bgColor
            dayCell.paddingTop = 8f
            dayCell.paddingBottom = 8f
            dayCell.paddingLeft = 6f
            dayCell.verticalAlignment = Element.ALIGN_MIDDLE
            dayCell.border = Rectangle.BOX
            dayCell.borderColor = BaseColor(200, 200, 200)
            table.addCell(dayCell)

            // Eintrags-Zelle
            val dayEntries = entries[day] ?: emptyList()
            val entriesText = if (dayEntries.isEmpty()) "–" else dayEntries.joinToString("\n")
            val entryCell = PdfPCell(Phrase(entriesText, normalFont))
            entryCell.backgroundColor = bgColor
            entryCell.paddingTop = 8f
            entryCell.paddingBottom = 8f
            entryCell.paddingLeft = 6f
            entryCell.border = Rectangle.BOX
            entryCell.borderColor = BaseColor(200, 200, 200)
            table.addCell(entryCell)
        }

        document.add(table)

        // ── Footer ──
        document.add(Paragraph(" "))
        val footerFont = Font(Font.FontFamily.HELVETICA, 9f, Font.ITALIC, BaseColor.GRAY)
        val footer = Paragraph("Erstellt am ${dateFormat.format(Date())}", footerFont)
        footer.alignment = Element.ALIGN_RIGHT
        document.add(footer)

        document.close()

        // ── In Downloads speichern via MediaStore (Android 10+) ──
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    pdfFile.inputStream().copyTo(outputStream)
                }
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
        } else {
            // Android 9 und älter: direkt in Downloads kopieren
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            pdfFile.copyTo(File(downloadsDir, fileName), overwrite = true)
        }

        return pdfFile
    }

    private fun addHeaderCell(table: PdfPTable, text: String) {
        val cell = PdfPCell(Phrase(text, headerFont))
        cell.backgroundColor = BaseColor(50, 50, 50)
        cell.paddingTop = 10f
        cell.paddingBottom = 10f
        cell.paddingLeft = 6f
        cell.horizontalAlignment = Element.ALIGN_CENTER
        table.addCell(cell)
    }
}
