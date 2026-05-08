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

/**
 * Generiert druckfertige PDFs für Arbeitseinträge.
 * Unterstützt drei Modi: aktuelle Woche, aktueller Monat, alle Einträge.
 */
class PdfGenerator(private val context: Context) {

    private val titleFont    = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
    private val subtitleFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, BaseColor.GRAY)
    private val boldFont     = Font(Font.FontFamily.HELVETICA, 9f,  Font.BOLD)
    private val normalFont   = Font(Font.FontFamily.HELVETICA, 9f,  Font.NORMAL)
    private val smallFont    = Font(Font.FontFamily.HELVETICA, 8f,  Font.ITALIC, BaseColor.GRAY)
    private val headerFont   = Font(Font.FontFamily.HELVETICA, 9f,  Font.BOLD, BaseColor.WHITE)
    private val accentColor  = BaseColor(180, 0, 0)

    private val dateFormat     = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    private val fileNameFormat = SimpleDateFormat("dd_MM_yyyy", Locale.GERMAN)

    data class DayEntry(
        val date: String,         // yyyy-MM-dd
        val displayDate: String,  // dd.MM.yyyy
        val dayName: String,
        val zeitVon: String?,
        val zeitBis: String?,
        val pauseMin: Int,
        val baustelleName: String,
        val beschreibung: String
    )

    fun generateWeekPdf(weekStartDate: Date, entries: List<DayEntry>): File {
        val weekEnd = Date(weekStartDate.time + 6 * 24 * 60 * 60 * 1000L)
        return buildPdf(
            title    = "Wochenbericht",
            subtitle = "${dateFormat.format(weekStartDate)} – ${dateFormat.format(weekEnd)}",
            fileName = "Wochenbericht_${fileNameFormat.format(weekStartDate)}.pdf",
            entries  = entries,
            groupByDay = true
        )
    }

    fun generateMonthPdf(year: Int, month: Int, entries: List<DayEntry>): File {
        val cal = Calendar.getInstance(Locale.GERMAN).apply { set(year, month - 1, 1) }
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.GERMAN).format(cal.time)
        return buildPdf(
            title    = "Monatsbericht",
            subtitle = monthName,
            fileName = "Monatsbericht_${String.format("%02d_%04d", month, year)}.pdf",
            entries  = entries,
            groupByDay = true
        )
    }

    fun generateAllPdf(entries: List<DayEntry>): File {
        return buildPdf(
            title    = "Gesamtbericht",
            subtitle = "Alle Einträge (${entries.size} gesamt)",
            fileName = "Gesamtbericht_${fileNameFormat.format(Date())}.pdf",
            entries  = entries,
            groupByDay = false
        )
    }

    // ── Core ────────────────────────────────────────────────────────────────

    private fun buildPdf(
        title: String, subtitle: String, fileName: String,
        entries: List<DayEntry>, groupByDay: Boolean
    ): File {
        val pdfFile = File(context.cacheDir, fileName)
        val document = Document(PageSize.A4, 36f, 36f, 48f, 48f)
        val writer = PdfWriter.getInstance(document, FileOutputStream(pdfFile))

        writer.pageEvent = object : PdfPageEventHelper() {
            override fun onEndPage(writer: PdfWriter, doc: Document) {
                ColumnText.showTextAligned(
                    writer.directContent, Element.ALIGN_RIGHT,
                    Phrase("Seite ${doc.pageNumber}  |  Erstellt ${dateFormat.format(Date())}", smallFont),
                    doc.right(), doc.bottom() - 12f, 0f
                )
            }
        }

        document.open()
        addHeader(document, title, subtitle)

        if (entries.isEmpty()) {
            document.add(Paragraph("Keine Einträge vorhanden.", normalFont))
        } else if (groupByDay) {
            addGroupedTable(document, entries)
        } else {
            addFlatTable(document, entries)
        }

        document.close()
        return saveToDownloads(pdfFile, fileName)
    }

    // ── Header ───────────────────────────────────────────────────────────────

    private fun addHeader(document: Document, title: String, subtitle: String) {
        val headerTable = PdfPTable(1).apply { widthPercentage = 100f }
        val cell = PdfPCell(Phrase(title, Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, BaseColor.WHITE)))
        cell.backgroundColor = accentColor
        cell.paddingTop = 10f; cell.paddingBottom = 10f; cell.paddingLeft = 10f
        cell.border = Rectangle.NO_BORDER
        headerTable.addCell(cell)
        document.add(headerTable)

        document.add(Paragraph(subtitle, subtitleFont).apply { spacingBefore = 4f; spacingAfter = 10f })
        document.add(Paragraph(Chunk(LineSeparator(1f, 100f, accentColor, Element.ALIGN_CENTER, -2f)))
            .apply { spacingAfter = 8f })
    }

    // ── Grouped (Woche / Monat) ──────────────────────────────────────────────

    private fun addGroupedTable(document: Document, entries: List<DayEntry>) {
        val sorted = entries.sortedWith(compareBy({ it.date }, { it.zeitVon ?: "" }))
        val grouped = sorted.groupBy { it.date }  // LinkedHashMap behält Reihenfolge

        val table = PdfPTable(5).apply {
            widthPercentage = 100f
            setWidths(floatArrayOf(15f, 10f, 10f, 20f, 45f))
            spacingBefore = 4f
        }
        listOf("Tag", "Von", "Bis", "Baustelle", "Beschreibung").forEach { addHeaderCell(table, it) }

        var rowIdx = 0
        for ((_, dayEntries) in grouped) {
            val bg = if (rowIdx % 2 == 0) BaseColor.WHITE else BaseColor(245, 245, 245)
            val first = dayEntries.first()
            val netto = calcNetto(first.zeitVon, first.zeitBis, first.pauseMin)
            val span  = dayEntries.size

            // Tag-Zelle mit rowspan
            val tagPhrase = Phrase()
            tagPhrase.add(Chunk("${first.dayName}\n${first.displayDate}", boldFont))
            if (netto != null) tagPhrase.add(Chunk("\n∑ $netto", smallFont))
            val tagCell = PdfPCell().apply {
                addElement(Phrase(tagPhrase)); rowspan = span
                backgroundColor = bg; styleCell(this)
            }
            table.addCell(tagCell)

            dayEntries.forEach { entry ->
                fun cell(text: String) = PdfPCell(Phrase(text, normalFont)).apply {
                    backgroundColor = bg; styleCell(this)
                }
                table.addCell(cell(entry.zeitVon ?: "–"))
                table.addCell(cell(entry.zeitBis ?: "–"))
                table.addCell(cell(entry.baustelleName.ifBlank { "–" }))
                table.addCell(cell(entry.beschreibung.ifBlank { "–" }))
            }
            rowIdx++
        }
        document.add(table)
        addSummary(document, entries)
    }

    // ── Flat (Gesamtbericht, nach Monat gegliedert) ──────────────────────────

    private fun addFlatTable(document: Document, entries: List<DayEntry>) {
        val byMonth = entries
            .sortedWith(compareBy({ it.date }, { it.zeitVon ?: "" }))
            .groupBy { it.date.substring(0, 7) }

        for ((monthKey, monthEntries) in byMonth) {
            val (y, m) = monthKey.split("-").map { it.toInt() }
            val cal = Calendar.getInstance(Locale.GERMAN).apply { set(y, m - 1, 1) }
            val monthName = SimpleDateFormat("MMMM yyyy", Locale.GERMAN).format(cal.time)

            document.add(Paragraph(monthName, Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD))
                .apply { spacingBefore = 12f; spacingAfter = 4f })

            val table = PdfPTable(5).apply {
                widthPercentage = 100f
                setWidths(floatArrayOf(15f, 10f, 10f, 20f, 45f))
            }
            listOf("Tag", "Von", "Bis", "Baustelle", "Beschreibung").forEach { addHeaderCell(table, it) }

            monthEntries.forEachIndexed { idx, entry ->
                val bg = if (idx % 2 == 0) BaseColor.WHITE else BaseColor(245, 245, 245)
                fun cell(text: String) = PdfPCell(Phrase(text, normalFont)).apply {
                    backgroundColor = bg; styleCell(this)
                }
                val tagCell = PdfPCell().apply {
                    addElement(Phrase().apply {
                        add(Chunk("${entry.dayName}\n", boldFont))
                        add(Chunk(entry.displayDate, normalFont))
                    })
                    backgroundColor = bg; styleCell(this)
                }
                table.addCell(tagCell)
                table.addCell(cell(entry.zeitVon ?: "–"))
                table.addCell(cell(entry.zeitBis ?: "–"))
                table.addCell(cell(entry.baustelleName.ifBlank { "–" }))
                table.addCell(cell(entry.beschreibung.ifBlank { "–" }))
            }
            document.add(table)
            addSummary(document, monthEntries)
        }
    }

    // ── Zusammenfassung ──────────────────────────────────────────────────────

    private fun addSummary(document: Document, entries: List<DayEntry>) {
        var total = 0L; var counted = 0
        entries.forEach { e ->
            calcNettoMin(e.zeitVon, e.zeitBis, e.pauseMin)?.let { total += it; counted++ }
        }
        if (counted == 0) return
        document.add(Paragraph(
            "Gesamt: ${String.format("%02d:%02d h", total / 60, total % 60)}" +
                    "  (${entries.size} Einträge, $counted mit Zeitangabe)",
            smallFont
        ).apply { alignment = Element.ALIGN_RIGHT; spacingBefore = 3f })
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun styleCell(cell: PdfPCell) {
        cell.paddingTop = 5f; cell.paddingBottom = 5f
        cell.paddingLeft = 5f; cell.paddingRight = 5f
        cell.border = Rectangle.BOX
        cell.borderColor = BaseColor(210, 210, 210)
    }

    private fun addHeaderCell(table: PdfPTable, text: String) {
        table.addCell(PdfPCell(Phrase(text, headerFont)).apply {
            backgroundColor = accentColor
            paddingTop = 7f; paddingBottom = 7f; paddingLeft = 5f
            border = Rectangle.NO_BORDER
        })
    }

    private fun calcNettoMin(von: String?, bis: String?, pauseMin: Int): Long? {
        if (von.isNullOrBlank() || bis.isNullOrBlank()) return null
        return try {
            val v = von.split(":").map { it.trim().toInt() }
            val b = bis.split(":").map { it.trim().toInt() }
            val gesamt = (b[0] * 60L + b[1]) - (v[0] * 60L + v[1])
            val pause = if (pauseMin > 0) pauseMin.toLong() else when {
                gesamt > 600 -> 60L; gesamt > 540 -> 45L; gesamt > 360 -> 30L; else -> 0L
            }
            maxOf(0L, gesamt - pause)
        } catch (e: Exception) { null }
    }

    private fun calcNetto(von: String?, bis: String?, pauseMin: Int): String? =
        calcNettoMin(von, bis, pauseMin)?.let { String.format("%02d:%02d h", it / 60, it % 60) }

    private fun saveToDownloads(pdfFile: File, fileName: String): File {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cv = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv)
            uri?.let {
                resolver.openOutputStream(it)?.use { out -> pdfFile.inputStream().copyTo(out) }
                cv.clear(); cv.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(it, cv, null, null)
            }
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            pdfFile.copyTo(File(dir, fileName), overwrite = true)
        }
        return pdfFile
    }
}