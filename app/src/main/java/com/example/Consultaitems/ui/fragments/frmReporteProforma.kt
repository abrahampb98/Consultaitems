package com.example.Consultaitems.ui.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.widget.Toast
import com.example.Consultaitems.R
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.reporte
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Chunk
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class frmReporteProforma {
    private lateinit var llenarControles: ClsLLenarControles


        companion object {
            var detalles: MutableList<reporte>? = null
            var pedido :String = ""
            var Fecha :String = ""
            var Transporte :String = ""
            var Observaciones :String = ""
            var PedidoVend :String = ""
            var ciudad :String = ""
            var formPago:String = ""
            var vendedor:String = ""
            var orden :String = ""
            var cliente :String = ""
            var lote :String = ""
            var subtotal :String = ""
            var descuento :String = ""
            var seguro :String = ""
            var iva :String = ""
            var flete :String = ""
            var total :String = ""
            var fileName = ""
            var bodega :String = ""

        fun generatePdf(context: Context, documento: Int) {

            //llama a mi fun para obtener los datos de cabecera y detalle
            obtenerPedido(context,documento)
            fileName =""
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
                val writer = PdfWriter.getInstance(document, outputStream)
                //val footerEvent = TableFooter()
                //writer.pageEvent = footerEvent
                document.open()

                /*
                // Título
                val fontTitle = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD)
                val paragraphTitle = Paragraph("$bodega", fontTitle).apply {
                    alignment = Element.ALIGN_CENTER
                }
                document.add(paragraphTitle)
                document.add(Paragraph("\n\n"))  // Espaciado después del título

                 */

                val fontTitle = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD)

                // Creamos una tabla de 2 columnas
                val tableTitle = PdfPTable(2)
                tableTitle.widthPercentage = 100f // ocupa todo el ancho
                tableTitle.setWidths(floatArrayOf(45f, 55f)) // ancho relativo columnas (texto más ancho que la imagen)

                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_telerepuestos)
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val image = Image.getInstance(stream.toByteArray())
                image.scaleToFit(60f, 60f)
                val cellImage = PdfPCell(image, false)
                cellImage.border = Rectangle.NO_BORDER
                cellImage.horizontalAlignment = Element.ALIGN_RIGHT
                cellImage.verticalAlignment = Element.ALIGN_MIDDLE
                tableTitle.addCell(cellImage)


                // Celda con el nombre de la bodega
                val cellTitle = PdfPCell(Phrase(bodega, fontTitle))
                cellTitle.border = Rectangle.NO_BORDER
                cellTitle.horizontalAlignment = Element.ALIGN_LEFT
                cellTitle.verticalAlignment = Element.ALIGN_MIDDLE
                tableTitle.addCell(cellTitle)

                // Celda con la imagen




                // Agregamos la tabla al documento
                document.add(tableTitle)
                document.add(Paragraph("\n\n"))




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
                val labels = listOf("Profroma:","Proforma General:" , "Fecha:", "Ciudad:", "Cliente:")
                val values = listOf(
                    "$pedido",
                    "$PedidoVend",
                    "$Fecha",
                    "$ciudad",
                    "$cliente"
                )
                val labelsRight =
                    listOf( "Forma de Pago:", "Transporte:", "Vendedor:", "Observaciones:")
                val valuesRight = listOf(
                    "$formPago",
                    "$Transporte",
                    "$vendedor",
                    "$Observaciones"
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

                val fontSeparador = Font(Font.FontFamily.HELVETICA, 10f,Font.BOLD)
                val chunkContent = Chunk(
                            "_______________________________________________________________________________________________________" +
                            "\n\n",fontSeparador)
                val separador = Paragraph(chunkContent).apply {
                    alignment = Element.ALIGN_JUSTIFIED // Justificar el texto
                }

                //AGREGAMOS LA LINEA
                document.add(separador)


                //tabla de los titulos
                val tableTitulos = PdfPTable(7)  // 7 columnas para los títulos
                tableTitulos.widthPercentage = 100f  // La tabla ocupa el 100% del ancho de la página
                // Definir proporciones de las columnas, por ejemplo, todos iguales aquí
                tableTitulos.setWidths(floatArrayOf(0.8f, 4.4f, 0.5f, 0.5f, 0.6f, 0.7f, 0.8f))

                // Define el color de fondo y la fuente para los títulos
                val backgroundColor = BaseColor.YELLOW  // Color amarillo para el fondo
                val fontTableTitulos = Font(Font.FontFamily.HELVETICA, 8f, Font.BOLD)

                // Lista de títulos
                val titles = arrayOf("CÓDIGO", "REFERENCIA", "DSCTO", "CANT.", "PRECIO", "SUBTOTAL", "CON DESCT")

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
                val tableDetalles = PdfPTable(7)  // 7 columnas para los detalles
                tableDetalles.widthPercentage = 100f  // La tabla ocupa el 100% del ancho de la página
                tableDetalles.setWidths(floatArrayOf(0.8f, 4.4f, 0.5f, 0.5f, 0.6f, 0.7f, 0.8f))

                val fontTableDetalles = Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL)

                // Añadir los detalles de cada producto a la tabla
                detalles?.forEach { detalle ->
                    val cellCodigo = PdfPCell(Phrase(detalle.codigo, fontTableDetalles))
                    cellCodigo.border = PdfPCell.NO_BORDER
                    cellCodigo.horizontalAlignment = Element.ALIGN_LEFT
                    tableDetalles.addCell(cellCodigo)

                    val cellDescripcion = PdfPCell(Phrase(detalle.descripcion, fontTableDetalles))
                    cellDescripcion.border = PdfPCell.NO_BORDER
                    cellCodigo.horizontalAlignment = Element.ALIGN_LEFT
                    tableDetalles.addCell(cellDescripcion)

                    val cellDescuento = PdfPCell(Phrase(detalle.descuento, fontTableDetalles))
                    cellDescuento.border = PdfPCell.NO_BORDER
                    cellDescuento.horizontalAlignment = Element.ALIGN_CENTER
                    tableDetalles.addCell(cellDescuento)

                    val cellCantidad = PdfPCell(Phrase(detalle.cantidad, fontTableDetalles))
                    cellCantidad.border = PdfPCell.NO_BORDER
                    cellCantidad.horizontalAlignment = Element.ALIGN_CENTER
                    tableDetalles.addCell(cellCantidad)

                    val cellPrecio = PdfPCell(Phrase(detalle.precio, fontTableDetalles))
                    cellPrecio.border = PdfPCell.NO_BORDER
                    cellPrecio.horizontalAlignment = Element.ALIGN_RIGHT
                    tableDetalles.addCell(cellPrecio)

                    val cellSubtotal = PdfPCell(Phrase(detalle.subtotal, fontTableDetalles))
                    cellSubtotal.border = PdfPCell.NO_BORDER
                    cellSubtotal.horizontalAlignment = Element.ALIGN_RIGHT
                    tableDetalles.addCell(cellSubtotal)

                    val cellConDescuento = PdfPCell(Phrase(detalle.ConDescuento, fontTableDetalles))
                    cellConDescuento.border = PdfPCell.NO_BORDER
                    cellConDescuento.horizontalAlignment = Element.ALIGN_RIGHT
                    tableDetalles.addCell(cellConDescuento)
                }

                document.add(tableDetalles) //  Añadir la tabla al documento





                // Crear tabla para los totales
                val tableTotales = PdfPTable(2)  // Crear una tabla de 2 columnas
                tableTotales.widthPercentage = 100f
                tableTotales.setWidths(floatArrayOf(1.7f, 0.3f))  // Proporciones iguales para las columnas

                addContent(document)
                addFooter(document, writer)
                document.close()
                showToast(context, "PDF generado correctamente: $fileName")
            } catch (e: IOException) {
                showToast(context, "Error al generar PDF: ${e.message}")
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
                llenarControles.fnProformaReporte(
                    codDocumento = codDocumento,
                    actualizarCabecera = { cursor ->

                         pedido = cursor.getString(cursor.getColumnIndexOrThrow("pr_codigo"))
                         Fecha = cursor.getString(cursor.getColumnIndexOrThrow("pr_fechatrn"))
                         cliente = cursor.getString(cursor.getColumnIndexOrThrow("pr_nombre"))
                         Transporte = cursor.getString(cursor.getColumnIndexOrThrow("tr_nombre"))
                         Observaciones = cursor.getString(cursor.getColumnIndexOrThrow("pr_observacion"))?: ""
                         PedidoVend = cursor.getString(cursor.getColumnIndexOrThrow("pr_codigoA"))?: "0"
                         ciudad = cursor.getString(cursor.getColumnIndexOrThrow("ci_descripcion"))

                         formPago = cursor.getString(cursor.getColumnIndexOrThrow("Descripcion"))
                         vendedor = cursor.getString(cursor.getColumnIndexOrThrow("vn_nombre"))
                         val LoteF = cursor.getString(cursor.getColumnIndexOrThrow("pr_lote"))
                         lote = LoteF.replace(".", "")
                         subtotal = cursor.getString(cursor.getColumnIndexOrThrow("pr_valorbruto"))
                         descuento = cursor.getString(cursor.getColumnIndexOrThrow("pr_valordesc"))
                         seguro = cursor.getString(cursor.getColumnIndexOrThrow("pr_valorseguro"))
                         iva = cursor.getString(cursor.getColumnIndexOrThrow("pr_valoriva"))
                         flete = cursor.getString(cursor.getColumnIndexOrThrow("pr_valorflete"))
                         total = cursor.getString(cursor.getColumnIndexOrThrow("pr_valortotal"))
                         bodega = cursor.getString(cursor.getColumnIndexOrThrow("bodega_nombre"))
                    },
                    actualizarDetalles = { detallesList ->
                        // Almacenar los detalles para uso posterior
                        detalles = detallesList
                    },
                    onDocumentoNoEncontrado = {
                        // Manejar el caso donde el documento no es encontrado
                        Toast.makeText(context, "Documento no encontrado.", Toast.LENGTH_SHORT).show()
                    }
                )
            }




            //clase que agrega al footer
            private fun addFooter(document: Document, writer: PdfWriter) {
                val contentByte = writer.directContent
                val titleFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
                val baseFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)
                val baseY = document.bottomMargin() + 100  // Ajustar según necesidad

                // Línea separadora
                val line = ""
                val linePhrase = Phrase(line, titleFont)
                ColumnText.showTextAligned(contentByte, Element.ALIGN_LEFT, linePhrase, document.leftMargin(), baseY - 15, 0f)

                // Texto del Lote
                val loteText = "_______________________________________________________________________________________________________"
                val lotePhrase = Phrase(loteText, titleFont)
                ColumnText.showTextAligned(contentByte, Element.ALIGN_LEFT, lotePhrase, document.leftMargin(), baseY, 0f)

                // Configurar la tabla de totales
                val table = PdfPTable(2)
                table.totalWidth = document.pageSize.width - document.leftMargin() - document.rightMargin()
                table.isLockedWidth = true
                table.setWidths(floatArrayOf(3.5f, .5f))
                val items = listOf("SUBTOTAL", "DESCUENTO:", "SEGURO", "IVA 15%", "FLETE", "TOTAL")
                val amounts = listOf(subtotal, descuento, seguro, iva, flete, total)

                items.forEachIndexed { index, item ->
                    val itemCell = PdfPCell(Phrase(item, titleFont))
                    itemCell.border = PdfPCell.NO_BORDER
                    itemCell.horizontalAlignment = Element.ALIGN_RIGHT
                    table.addCell(itemCell)

                    val amountCell = PdfPCell(Phrase(amounts[index], baseFont))
                    amountCell.border = PdfPCell.NO_BORDER
                    amountCell.horizontalAlignment = Element.ALIGN_RIGHT
                    table.addCell(amountCell)
                }

                // Añadir la tabla justo debajo de la línea separadora
                table.writeSelectedRows(0, -1, document.leftMargin(), baseY - 5, contentByte)
            }


    }

}