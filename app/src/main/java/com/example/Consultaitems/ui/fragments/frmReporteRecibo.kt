package com.example.Consultaitems.ui.fragments

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.clsConvertirDolares
import com.example.Consultaitems.utils.cls.consultaLlamada
import com.example.Consultaitems.utils.cls.reporteRecibo
import com.example.Consultaitems.utils.pdf.frmPdfPagare1
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Chunk
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

class frmReporteRecibo {
    private lateinit var llenarControles: ClsLLenarControles


    companion object {
        var detalles: MutableList<reporteRecibo>? = null
        var doc: String = ""
        var ente: String = ""
        var fecha: String = ""
        var vendedor: String = ""
        var cliente: String = ""
        var total: String = ""
        var fileName = ""
        var valor: Double = 0.0

        fun generatePdf(context: Context, documento: Int): File {

            //llama a mi fun para obtener los datos de cabecera y detalle
            obtenerPedido(context, documento)
            fileName = ""
            val fechaActual = SimpleDateFormat("_ddMMyyyy_HHmmss").format(Date())
            fileName = "$documento" + "_" + "$cliente" + "${fechaActual}" + ".pdf"
            val filePath =
                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$fileName"


            // Ajustar márgenes: izquierdo, derecho, superior, inferior
            val document = Document(
                PageSize.A4,
                10f,
                10f,
                30f,
                20f
            ) // Reducir los márgenes izquierdo y derecho

            try {
                val outputStream = FileOutputStream(filePath)
                PdfWriter.getInstance(document, outputStream)
                //val footerEvent = TableFooter()
                //writer.pageEvent = footerEvent
                document.open()

                // Título
                val fontTitle = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
                val paragraphTitle =
                    Paragraph("RECIBO DE COBRO N°  $doc\n COTZUL S.A.", fontTitle).apply {
                        alignment = Element.ALIGN_CENTER
                    }
                document.add(paragraphTitle)
                document.add(Paragraph("\n\n"))  // Espaciado después del título

                // Configurar tabla
                val table = PdfPTable(4)  // Crear una tabla de 4 columnas
                table.widthPercentage = 100f
                table.setWidths(
                    floatArrayOf(
                        1f,
                        3.3f,
                        1.2f,
                        2.6f
                    )
                )  // Ajuste de las proporciones de las columnas

                // Datos para la tabla
                val labels = listOf("Recibido de:", "Fecha:")
                val values = listOf(
                    "$ente",
                    "$fecha",
                )
                val labelsRight =
                    listOf("Ente Resp:", "Area:")
                val valuesRight = listOf(
                    "$vendedor",
                    "CTAS X COBRAR",
                )

                // Añadir datos a la tabla
                labels.forEachIndexed { index, label ->
                    table.addCell(createLabelCell(label))
                    table.addCell(createValueCell(values[index]))
                }
                labelsRight.forEachIndexed { index, label ->
                    table.addCell(createLabelCell(label))
                    table.addCell(createValueCell(valuesRight[index]))
                }
                //agregamos la tala de cabecera
                document.add(table)

                val fontSeparador = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
                val chunkContent = Chunk(
                    "_______________________________________________________________________________________________________" +
                            "\n\n", fontSeparador
                )
                val separador = Paragraph(chunkContent).apply {
                    alignment = Element.ALIGN_JUSTIFIED // Justificar el texto
                }

                //AGREGAMOS LA LINEA
                document.add(separador)


                //tabla de los titulos
                val tableTitulos = PdfPTable(8)  // 8 columnas para los títulos
                tableTitulos.widthPercentage =
                    100f  // La tabla ocupa el 100% del ancho de la página
                // Definir proporciones de las columnas, por ejemplo, todos iguales aquí
                tableTitulos.setWidths(floatArrayOf(0.8f, 1.5f, 0.7f, 0.8f, 0.9f, 2.2f, 0.8f, 2.0f))

                // Define el color de fondo y la fuente para los títulos
                val backgroundColor = BaseColor.YELLOW  // Color amarillo para el fondo
                val fontTableTitulos = Font(Font.FontFamily.HELVETICA, 8f, Font.BOLD)

                // Lista de títulos
                val titles =
                    arrayOf("TRANSAC", "BANCO", "# DOC.", "#CTA.", consultaLlamada.ConstantesReporte.TIPO_FECHA, "CONCEPTO", "VALOR", "OBSERVACION")

                // Añadir los títulos a la tabla
                titles.forEach { title ->
                    val cell = PdfPCell(Phrase(title, fontTableTitulos))
                    cell.backgroundColor = backgroundColor  // Establecer el color de fondo
                    cell.horizontalAlignment = Element.ALIGN_CENTER
                    cell.border = PdfPCell.BOX    // Puedes cambiar a BORDER si deseas bordes
                    tableTitulos.addCell(cell)
                }
                document.add(tableTitulos)  // Añadir la tabla al documento


                // Configurar la tabla de detalles
                val tableDetalles = PdfPTable(8)  // 8 columnas para los detalles
                tableDetalles.widthPercentage =
                    100f  // La tabla ocupa el 100% del ancho de la página
                tableDetalles.setWidths(floatArrayOf(0.8f, 1.5f, 0.7f, 0.8f, 0.9f, 2.2f, 0.8f, 2.0f))

                val fontTableDetalles = Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL)

                // Añadir los detalles de cada producto a la tabla
                detalles?.forEach { detalle ->
                    val TRANSAC = PdfPCell(Phrase(detalle.transaccion, fontTableDetalles))
                    TRANSAC.border = PdfPCell.NO_BORDER
                    TRANSAC.horizontalAlignment = Element.ALIGN_LEFT
                    tableDetalles.addCell(TRANSAC)

                    val BANCO = PdfPCell(Phrase(detalle.banco, fontTableDetalles))
                    BANCO.border = PdfPCell.NO_BORDER
                    BANCO.horizontalAlignment = Element.ALIGN_LEFT
                    tableDetalles.addCell(BANCO)

                    val DOC = PdfPCell(Phrase(detalle.doc, fontTableDetalles))
                    DOC.border = PdfPCell.NO_BORDER
                    DOC.horizontalAlignment = Element.ALIGN_CENTER
                    tableDetalles.addCell(DOC)

                    val CTA = PdfPCell(Phrase(detalle.cuenta, fontTableDetalles))
                    CTA.border = PdfPCell.NO_BORDER
                    CTA.horizontalAlignment = Element.ALIGN_CENTER
                    tableDetalles.addCell(CTA)

                    val FECHA = PdfPCell(Phrase(detalle.fecha, fontTableDetalles))
                    FECHA.border = PdfPCell.NO_BORDER
                    FECHA.horizontalAlignment = Element.ALIGN_CENTER
                    tableDetalles.addCell(FECHA)

                    val CONCEPTO = PdfPCell(Phrase(detalle.concepto, fontTableDetalles))
                    CONCEPTO.border = PdfPCell.NO_BORDER
                    CONCEPTO.horizontalAlignment = Element.ALIGN_LEFT
                    CONCEPTO.setNoWrap(false) // Permite que el texto se envuelva y crezca verticalmente
                    tableDetalles.addCell(CONCEPTO)

                    val VALOR = PdfPCell(Phrase(detalle.valor, fontTableDetalles))
                    VALOR.border = PdfPCell.NO_BORDER
                    VALOR.horizontalAlignment = Element.ALIGN_RIGHT
                    tableDetalles.addCell(VALOR)

                    val observacion = PdfPCell(Phrase(detalle.observacion, fontTableDetalles))
                    observacion.border = PdfPCell.NO_BORDER
                    observacion.horizontalAlignment = Element.ALIGN_LEFT
                    observacion.setNoWrap(false)
                    tableDetalles.addCell(observacion)
                }

                document.add(tableDetalles) //  Añadir la tabla de detalles al documento

                // Añadir línea separadora
                val separator = Paragraph(
                    "_______________________________________________________________________________________________________",
                    Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
                )
                document.add(separator)


                // Configurar la tabla de totales
                val tableTotales = PdfPTable(2)
                tableTotales.totalWidth =
                    document.pageSize.width - document.leftMargin() - document.rightMargin()
                tableTotales.isLockedWidth = true
                tableTotales.setWidths(
                    floatArrayOf(
                        4f,
                        1f
                    )
                ) // Ajusta los anchos de las columnas según sea necesario

                val letras = clsConvertirDolares.convertirNumeroALetras(valor) + " DOLARES"
                val total = "$ $total"

                // Fuente en negrita
                val boldFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)

                // Celda con el texto en letras (alineado a la izquierda)
                val letrasCell = PdfPCell(Phrase(letras, boldFont))
                letrasCell.border = PdfPCell.NO_BORDER
                letrasCell.horizontalAlignment = Element.ALIGN_LEFT
                tableTotales.addCell(letrasCell)

                // Crear un Phrase para "TOTAL:" y el valor total, ambos en negrita
                val totalPhrase = Phrase()
                totalPhrase.add(Chunk("TOTAL: ", boldFont))
                totalPhrase.add(Chunk(total, boldFont))

                // Celda con el Phrase combinado (alineado a la derecha)
                val totalCell = PdfPCell(totalPhrase)
                totalCell.border = PdfPCell.NO_BORDER
                totalCell.horizontalAlignment = Element.ALIGN_RIGHT
                tableTotales.addCell(totalCell)

                // Añadir la tabla de totales al documento
                document.add(tableTotales)

                // Finalizar y cerrar el documento
                document.close()
                return  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName
                )
            } catch (e: Exception) {
                if (document.isOpen) {
                    document.close()
                }
                throw e
            }
        }


        private fun addContent(document: Document) {
            val fontTitle = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD)
            val paragraphTitle = Paragraph("", fontTitle).apply {
                alignment = Element.ALIGN_CENTER
            }
            document.add(Paragraph("\n\n"))
        }


        private fun createLabelCell(text: String): PdfPCell {
            val cell = PdfPCell(Phrase(text, Font(Font.FontFamily.HELVETICA, 8f, Font.BOLD)))
            cell.border = PdfPCell.NO_BORDER
            cell.horizontalAlignment = Element.ALIGN_LEFT
            cell.paddingLeft = 5f
            return cell
        }

        private fun createValueCell(text: String): PdfPCell {
            val cell = PdfPCell(Phrase(text, Font(Font.FontFamily.HELVETICA, 8f)))
            cell.border = PdfPCell.NO_BORDER
            cell.horizontalAlignment = Element.ALIGN_LEFT
            cell.paddingLeft = 10f
            return cell
        }

        private fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        fun obtenerPedido(context: Context, codDocumento: Int) {
            val llenarControles = ClsLLenarControles(context)
            llenarControles.fnReporteRecibo(
                codDocumento = codDocumento,
                actualizarCabecera = { cursor ->
                    doc = cursor.getString(cursor.getColumnIndexOrThrow("rc_codrecibo"))
                    ente = cursor.getString(cursor.getColumnIndexOrThrow("ente"))
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow("rc_fecharec"))
                    cliente = cursor.getString(cursor.getColumnIndexOrThrow("cl_nombre"))
                    vendedor = cursor.getString(cursor.getColumnIndexOrThrow("vendedor"))
                    total = String.format("%.2f", cursor.getString(cursor.getColumnIndexOrThrow("rc_total")).toDouble())
                    valor = cursor.getDouble(cursor.getColumnIndexOrThrow("rc_total"))

                },
                actualizarDetalles = { detallesList ->
                    // Almacenar los detalles para uso posterior
                    detalles = detallesList
                },
                onDocumentoNoEncontrado = {
                    // Manejar el caso donde el documento no es encontrado
                    Toast.makeText(context, "Documento no encontrado.", Toast.LENGTH_SHORT)
                        .show()
                }
            )
        }

    }

}