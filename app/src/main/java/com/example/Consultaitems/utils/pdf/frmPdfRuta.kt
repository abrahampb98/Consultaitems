package com.example.Consultaitems.utils.pdf

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.example.Consultaitems.ui.fragments.frmReportePedido
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import java.io.File
import java.io.FileOutputStream

class frmPdfRutaV(private val context: Context) {

    private val llenarControles = ClsLLenarControles(context)

    fun generatePdfRutaVendedor(incial: String, final:String): File {
        val fileName = "Reporte_RutaVendedor_${System.currentTimeMillis()}.pdf"
        val filePath =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$fileName"

        val document = Document(PageSize.A4, 20f, 20f, 30f, 20f) // Márgenes reducidos

        try {
            val outputStream = FileOutputStream(File(filePath))
            PdfWriter.getInstance(document, outputStream)
            document.open()

            // Título del reporte
            val fontTitle = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD)
            val paragraphTitle = Paragraph("REPORTE RUTA VENDEDOR", fontTitle).apply {
                alignment = Element.ALIGN_CENTER
            }
            document.add(paragraphTitle)


            val fontSubtitle = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
            val paragraphSubTitle = Paragraph("Periodo: $incial al $final ", fontSubtitle).apply {
                alignment = Element.ALIGN_CENTER
            }
            document.add(paragraphSubTitle)


            document.add(Paragraph("\n\n")) // Espaciado

            // Obtener datos de la consulta SQL
            val listaDatos = llenarControles.fnReporteRuta(incial, final)

            // Definir la tabla con 8 columnas
            val table = PdfPTable(8)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(1.1f, 3f, 1.5f, 0.6f, 0.6f, 0.7f , 0.5f, 2.3f)) // Ajuste de ancho de columnas

            // Encabezados de la tabla
            val headers = arrayOf("Día", "Cliente", "Ciudad", "Visita", "Venta", "Cobro", "GT", "Observacion")
            val fontHeader = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)

            headers.forEach { header ->
                val cell = PdfPCell(Phrase(header, fontHeader)).apply {
                    backgroundColor = BaseColor.LIGHT_GRAY
                    horizontalAlignment = Element.ALIGN_CENTER // Centrar horizontalmente
                    verticalAlignment = Element.ALIGN_MIDDLE  // Centrar verticalmente
                    paddingTop = 5f  // Ajustar espacio interno superior
                    paddingBottom = 5f  // Ajustar espacio interno inferior
                }
                table.addCell(cell)
            }


            val fontRow = Font(Font.FontFamily.HELVETICA, 9f, Font.NORMAL)

            listaDatos.forEach { item ->

                val cellDia = PdfPCell(Phrase(item.dia, fontRow))
                cellDia.verticalAlignment = Element.ALIGN_MIDDLE
                table.addCell(cellDia)

                table.addCell(Phrase(item.cliente, fontRow))

                val cellCiudad = PdfPCell(Phrase(item.ciudad, fontRow))
                cellCiudad.horizontalAlignment = Element.ALIGN_CENTER
                cellCiudad.verticalAlignment = Element.ALIGN_MIDDLE
                table.addCell(cellCiudad)

                val cellVisita = PdfPCell(Phrase(item.visita, fontRow))
                cellVisita.horizontalAlignment = Element.ALIGN_CENTER
                cellCiudad.verticalAlignment = Element.ALIGN_MIDDLE
                table.addCell(cellVisita)

                val cellVenta = PdfPCell(Phrase(item.venta, fontRow))
                cellVenta.horizontalAlignment = Element.ALIGN_CENTER
                cellCiudad.verticalAlignment = Element.ALIGN_MIDDLE
                table.addCell(cellVenta)

                val cellCobro = PdfPCell(Phrase(item.cobro, fontRow))
                cellCobro.horizontalAlignment = Element.ALIGN_CENTER
                cellCiudad.verticalAlignment = Element.ALIGN_MIDDLE
                table.addCell(cellCobro)

                val cellTelefono = PdfPCell(Phrase(item.telefono, fontRow))
                cellCobro.horizontalAlignment = Element.ALIGN_CENTER
                cellCiudad.verticalAlignment = Element.ALIGN_MIDDLE
                table.addCell(cellTelefono)

                table.addCell(Phrase(item.observacion, fontRow))
            }

            document.add(table) // Agregar la tabla al documento
            document.close()

            return File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )

        } catch (e: Exception) {
            if (document.isOpen) {
                document.close()
            }
            throw e
        }
    }
}


data class RutaVendedorData(
    val dia: String,
    val cliente: String,
    val ciudad: String,
    val visita: String,
    val venta: String,
    val cobro: String,
    val telefono: String,
    val observacion: String
)