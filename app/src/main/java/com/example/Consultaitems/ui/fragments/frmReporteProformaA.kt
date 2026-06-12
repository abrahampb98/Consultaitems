package com.example.Consultaitems.ui.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import com.example.Consultaitems.R
import com.example.Consultaitems.utils.cls.reporteProforma
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class frmReporteProformaA {

    companion object {
        var detalles: List<reporteProforma>? = null

        var pedido: String = ""
        var Fecha: String = ""
        var Transporte: String = ""
        var Observaciones: String = ""
        var PedidoVend: String = ""
        var ciudad: String = ""
        var formPago: String = ""
        var vendedor: String = ""
        var orden: String = ""
        var cliente: String = ""
        var lote: String = ""
        var subtotal: String = ""
        var descuento: String = ""
        var seguro: String = ""
        var iva: String = ""
        var flete: String = ""
        var total: String = ""
        var fileName: String = ""
        var bodega: String = ""
        var ep_codigo: Int = 0

        fun generatePdf(
            context: Context,
            documento: Int,
            bo_codigo: String,
            imagen: Boolean,
            pedidoIn: String,
            fechaIn: String,
            clienteIn: String,
            transporteIn: String,
            observacionesIn: String?,
            pedidoVendIn: String?,
            ciudadIn: String,
            formPagoIn: String,
            vendedorIn: String,
            loteIn: String,
            subtotalIn: String,
            descuentoIn: String,
            seguroIn: String,
            ivaIn: String,
            fleteIn: String,
            totalIn: String,
            bodegaIn: String,
            epCodigoIn: Int,
            detallesIn: List<reporteProforma>
        ): File {
            obtenerPedido(
                pedidoIn = pedidoIn,
                fechaIn = fechaIn,
                clienteIn = clienteIn,
                transporteIn = transporteIn,
                observacionesIn = observacionesIn,
                pedidoVendIn = pedidoVendIn,
                ciudadIn = ciudadIn,
                formPagoIn = formPagoIn,
                vendedorIn = vendedorIn,
                loteIn = loteIn,
                subtotalIn = subtotalIn,
                descuentoIn = descuentoIn,
                seguroIn = seguroIn,
                ivaIn = ivaIn,
                fleteIn = fleteIn,
                totalIn = totalIn,
                bodegaIn = bodegaIn,
                epCodigoIn = epCodigoIn,
                detallesIn = detallesIn
            )

            val fechaActual = SimpleDateFormat("_ddMMyyyy_HHmmss", Locale.getDefault()).format(Date())
            fileName = "${documento}_${limpiarNombreArchivo(cliente)}$fechaActual.pdf"

            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )

            val document = Document(
                PageSize.A4,
                10f,
                10f,
                30f,
                20f
            )

            try {
                val outputStream = FileOutputStream(file)
                val writer = PdfWriter.getInstance(document, outputStream)

                document.open()

                agregarTitulo(context, document, documento, bo_codigo)
                agregarCabecera(document)
                agregarSeparador(document)
                agregarDetalle(document, bo_codigo, imagen)
                addContent(document)
                addFooter(document, writer)

                document.close()
            } catch (e: Exception) {
                try {
                    if (document.isOpen) {
                        document.close()
                    }
                } catch (_: Exception) {
                }

                throw e
            }

            return file
        }

        private fun obtenerPedido(
            pedidoIn: String,
            fechaIn: String,
            clienteIn: String,
            transporteIn: String,
            observacionesIn: String?,
            pedidoVendIn: String?,
            ciudadIn: String,
            formPagoIn: String,
            vendedorIn: String,
            loteIn: String,
            subtotalIn: String,
            descuentoIn: String,
            seguroIn: String,
            ivaIn: String,
            fleteIn: String,
            totalIn: String,
            bodegaIn: String,
            epCodigoIn: Int,
            detallesIn: List<reporteProforma>
        ) {
            pedido = pedidoIn
            Fecha = fechaIn
            cliente = clienteIn
            Transporte = transporteIn
            Observaciones = observacionesIn ?: ""
            PedidoVend = pedidoVendIn ?: "0"
            ciudad = ciudadIn
            formPago = formPagoIn
            vendedor = vendedorIn
            lote = loteIn.replace(".", "")
            subtotal = subtotalIn
            descuento = descuentoIn
            seguro = seguroIn
            iva = ivaIn
            flete = fleteIn
            total = totalIn
            bodega = bodegaIn
            ep_codigo = epCodigoIn
            detalles = detallesIn
        }

        private fun agregarTitulo(
            context: Context,
            document: Document,
            documento: Int,
            boCodigo: String
        ) {
            val fontTitle = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
            val fontRed = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, BaseColor.RED)
            val fontNormal = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)

            val paragraphTitle = Paragraph("PROFORMA N° $documento", fontTitle).apply {
                alignment = Element.ALIGN_CENTER
            }
            document.add(paragraphTitle)

            val tableHeader = PdfPTable(3)
            tableHeader.widthPercentage = 100f

            // Izquierda logo, centro datos, derecha vacío.
            // Así el bloque rojo queda centrado en la hoja.
            tableHeader.setWidths(floatArrayOf(20f, 60f, 20f))

            val drawableRes = when (boCodigo) {
                "1" -> R.drawable.ic_bodega
                "2" -> R.drawable.ic_telerepuestos
                "51" -> R.drawable.ic_pmg
                else -> R.drawable.ic_bodega
            }

            val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, drawableRes)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

            val logo = Image.getInstance(stream.toByteArray()).apply {
                scaleToFit(80f, 80f)
            }

            val cellLogo = PdfPCell(logo, false).apply {
                border = Rectangle.NO_BORDER
                horizontalAlignment = Element.ALIGN_LEFT
                verticalAlignment = Element.ALIGN_TOP
            }
            tableHeader.addCell(cellLogo)

            val cellInfo = PdfPCell().apply {
                border = Rectangle.NO_BORDER
                horizontalAlignment = Element.ALIGN_CENTER
                verticalAlignment = Element.ALIGN_TOP

                addElement(
                    Paragraph("COTZUL S.A.", fontRed).apply {
                        alignment = Element.ALIGN_CENTER
                    }
                )

                when (boCodigo) {
                    "1", "2" -> {
                        addElement(
                            Paragraph("Rumichaca 820 A y 9 de Octubre", fontRed).apply {
                                alignment = Element.ALIGN_CENTER
                            }
                        )
                    }

                    "51" -> {
                        addElement(
                            Paragraph("Victor Manuel Rendon 808B y Riobamba", fontRed).apply {
                                alignment = Element.ALIGN_CENTER
                            }
                        )
                    }
                }

                when (ep_codigo) {
                    17111 -> {
                        addElement(Paragraph("0994558144", fontRed).apply { alignment = Element.ALIGN_CENTER })
                        addElement(Paragraph("abel.calderon@telerepuestos.com", fontRed).apply { alignment = Element.ALIGN_CENTER })
                    }

                    29763 -> {
                        addElement(Paragraph("0994563019", fontRed).apply { alignment = Element.ALIGN_CENTER })
                        addElement(Paragraph("bolivar.quimis@telerepuestos.com", fontRed).apply { alignment = Element.ALIGN_CENTER })
                    }

                    76835 -> {
                        addElement(Paragraph("0984412482", fontRed).apply { alignment = Element.ALIGN_CENTER })
                        addElement(Paragraph("jonathan.campozano@englandsound.com", fontRed).apply { alignment = Element.ALIGN_CENTER })
                    }

                    72599 -> {
                        addElement(Paragraph("0978606606", fontRed).apply { alignment = Element.ALIGN_CENTER })
                        addElement(Paragraph("paul.yepez@englandsound.com", fontRed).apply { alignment = Element.ALIGN_CENTER })
                    }

                    67 -> {
                        addElement(Paragraph("0967932758", fontRed).apply { alignment = Element.ALIGN_CENTER })
                        addElement(Paragraph("ventas@cotzul.com", fontRed).apply { alignment = Element.ALIGN_CENTER })
                    }

                    48303 -> {
                        addElement(Paragraph("0994558136", fontRed).apply { alignment = Element.ALIGN_CENTER })
                        addElement(Paragraph("kevin.garcia@telerepuestos.com", fontRed).apply { alignment = Element.ALIGN_CENTER })
                    }

                    76836 -> {
                        addElement(Paragraph("0992629371", fontRed).apply { alignment = Element.ALIGN_CENTER })
                        addElement(Paragraph("jordy.lino@englandsound.com", fontRed).apply { alignment = Element.ALIGN_CENTER })
                    }

                    81982 -> {
                        addElement(Paragraph("0997299189", fontRed).apply { alignment = Element.ALIGN_CENTER })
                        addElement(Paragraph("vendedor01p@englandsound.com", fontRed).apply { alignment = Element.ALIGN_CENTER })
                    }

                    81736 -> {
                        addElement(Paragraph("0992629371", fontRed).apply { alignment = Element.ALIGN_CENTER })
                        addElement(Paragraph("vendedor02p@englandsound.com", fontRed).apply { alignment = Element.ALIGN_CENTER })
                    }

                    70986 -> {
                        addElement(Paragraph("0997299189", fontRed).apply { alignment = Element.ALIGN_CENTER })
                        addElement(Paragraph("supervision.cliente@englandsound.com", fontRed).apply { alignment = Element.ALIGN_CENTER })
                    }

                    else -> {
                        addElement(
                            Paragraph("Sin información de contacto", fontNormal).apply {
                                alignment = Element.ALIGN_CENTER
                            }
                        )
                    }
                }

                addElement(
                    Paragraph("Ruc: 0992146036001", fontRed).apply {
                        alignment = Element.ALIGN_CENTER
                    }
                )
            }

            tableHeader.addCell(cellInfo)

            val cellEmpty = PdfPCell(Phrase("")).apply {
                border = Rectangle.NO_BORDER
            }
            tableHeader.addCell(cellEmpty)

            document.add(tableHeader)
            document.add(Paragraph("\n"))
        }

        private fun agregarCabecera(document: Document) {
            val table = PdfPTable(4)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(1f, 3.3f, 1.2f, 2.6f))

            val labels = listOf(
                "Proforma:",
                "Proforma General:",
                "Fecha:",
                "Ciudad:",
                "Cliente:"
            )

            val values = listOf(
                pedido,
                PedidoVend,
                Fecha,
                ciudad,
                cliente
            )

            val labelsRight = listOf(
                "Forma de Pago:",
                "Transporte:",
                "Vendedor:",
                "Observaciones:"
            )

            val valuesRight = listOf(
                formPago,
                Transporte,
                vendedor,
                Observaciones
            )

            labels.forEachIndexed { index, label ->
                table.addCell(createLabelCell(label))
                table.addCell(createValueCell(values[index]))
            }

            labelsRight.forEachIndexed { index, label ->
                table.addCell(createLabelCell(label))
                table.addCell(createValueCell(valuesRight[index]))
            }

            document.add(table)
        }

        private fun agregarSeparador(document: Document) {
            val fontSeparador = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
            val separador = Paragraph(
                "_______________________________________________________________________________________________________\n\n",
                fontSeparador
            ).apply {
                alignment = Element.ALIGN_JUSTIFIED
            }

            document.add(separador)
        }

        private fun agregarDetalle(
            document: Document,
            boCodigo: String,
            imagen: Boolean
        ) {
            val fontTableTitulos = Font(Font.FontFamily.HELVETICA, 8f, Font.BOLD)
            val fontTableDetalles = Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL)

            val numCols: Int
            val widths: FloatArray
            val titles: Array<String>

            if (imagen) {
                numCols = 8
                widths = floatArrayOf(0.8f, 3.5f, 0.9f, 0.5f, 0.5f, 0.6f, 0.7f, 0.8f)
                titles = arrayOf(
                    "CÓDIGO",
                    "DESCRIPCION",
                    "IMAGEN",
                    "DSCTO",
                    "CANT.",
                    "PRECIO",
                    "CON DESCT",
                    "SUBTOTAL"
                )
            } else {
                numCols = 7
                widths = floatArrayOf(0.8f, 4.4f, 0.5f, 0.5f, 0.6f, 0.7f, 0.8f)
                titles = arrayOf(
                    "CÓDIGO",
                    "DESCRIPCION",
                    "DSCTO",
                    "CANT.",
                    "PRECIO",
                    "CON DESCT",
                    "SUBTOTAL"
                )
            }

            val tableDetalles = PdfPTable(numCols).apply {
                widthPercentage = 100f
                setWidths(widths)
            }

            titles.forEach { title ->
                tableDetalles.addCell(
                    PdfPCell(Phrase(title, fontTableTitulos)).apply {
                        backgroundColor = BaseColor.YELLOW
                        horizontalAlignment = Element.ALIGN_CENTER
                        verticalAlignment = Element.ALIGN_MIDDLE
                        border = PdfPCell.BOX
                    }
                )
            }

            detalles.orEmpty().forEach { detalle ->
                tableDetalles.addCell(
                    detalleCell(
                        text = detalle.codigo,
                        font = fontTableDetalles,
                        alignment = Element.ALIGN_LEFT
                    )
                )

                tableDetalles.addCell(
                    detalleCell(
                        text = detalle.descripcion,
                        font = fontTableDetalles,
                        alignment = Element.ALIGN_LEFT
                    )
                )

                if (imagen) {
                    tableDetalles.addCell(
                        imageCell(
                            imageUrl = detalle.imageUrl,
                            codigo = detalle.codigo,
                            boCodigo = boCodigo
                        )
                    )
                }

                tableDetalles.addCell(
                    detalleCell(
                        text = fmt2(detalle.descuento),
                        font = fontTableDetalles,
                        alignment = Element.ALIGN_CENTER
                    )
                )

                tableDetalles.addCell(
                    detalleCell(
                        text = fmt2(detalle.cantidad),
                        font = fontTableDetalles,
                        alignment = Element.ALIGN_CENTER
                    )
                )

                tableDetalles.addCell(
                    detalleCell(
                        text = detalle.precio,
                        font = fontTableDetalles,
                        alignment = Element.ALIGN_RIGHT
                    )
                )

                tableDetalles.addCell(
                    detalleCell(
                        text = detalle.ConDescuento,
                        font = fontTableDetalles,
                        alignment = Element.ALIGN_RIGHT
                    )
                )

                tableDetalles.addCell(
                    detalleCell(
                        text = detalle.subtotal,
                        font = fontTableDetalles,
                        alignment = Element.ALIGN_RIGHT
                    )
                )
            }

            document.add(tableDetalles)
        }

        private fun detalleCell(
            text: String?,
            font: Font,
            alignment: Int
        ): PdfPCell {
            return PdfPCell(Phrase(text.orEmpty(), font)).apply {
                border = PdfPCell.BOX
                horizontalAlignment = alignment
                verticalAlignment = Element.ALIGN_MIDDLE
                setMinimumHeight(65f)
            }
        }

        private fun imageCell(
            imageUrl: String?,
            codigo: String,
            boCodigo: String
        ): PdfPCell {
            val urls = mutableListOf<String>()

            val urlPrincipal = imageUrl.orEmpty().trim()

            if (urlPrincipal.isNotBlank() && !urlPrincipal.equals("null", ignoreCase = true)) {
                urls.add(urlPrincipal)
            }

            urls.add("https://app.cotzul.com/sitenet/digital/9/$codigo.png")

            if (boCodigo.isNotBlank() && boCodigo != "9") {
                urls.add("https://app.cotzul.com/sitenet/digital/$boCodigo/$codigo.png")
            }

            for (url in urls.distinct()) {
                try {
                    val bytes = downloadImageCotzul(url)

                    val image = Image.getInstance(bytes).apply {
                        scaleToFit(50f, 50f)
                    }

                    return PdfPCell(image, true).apply {
                        border = PdfPCell.BOX
                        setFixedHeight(65f)
                        horizontalAlignment = Element.ALIGN_CENTER
                        verticalAlignment = Element.ALIGN_MIDDLE
                    }
                } catch (e: Exception) {
                    Log.e("PDF_IMG", "No carga imagen: $url", e)
                }
            }

            return PdfPCell(Phrase("")).apply {
                border = PdfPCell.BOX
                setFixedHeight(65f)
                horizontalAlignment = Element.ALIGN_CENTER
                verticalAlignment = Element.ALIGN_MIDDLE
            }
        }

        private fun downloadImageCotzul(url: String): ByteArray {
            var connection: HttpURLConnection? = null

            return try {
                connection = URL(url).openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
                connection.setRequestProperty("Accept", "image/png,image/jpeg,image/*,*/*")
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val responseCode = connection.responseCode

                if (responseCode !in 200..299) {
                    throw Exception("HTTP $responseCode")
                }

                val inputStream: InputStream = connection.inputStream
                inputStream.use { it.readBytes() }
            } finally {
                connection?.disconnect()
            }
        }

        private fun addContent(document: Document) {
            document.add(Paragraph("\n"))
        }

        private fun addFooter(
            document: Document,
            writer: PdfWriter
        ) {
            val titleFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
            val baseFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)

            val tableDoc = PdfPTable(1)
            tableDoc.widthPercentage = 100f

            val cellDoc = PdfPCell().apply {
                backgroundColor = BaseColor(230, 230, 230)
                border = Rectangle.BOX
                borderWidth = 0.5f

                addElement(
                    Paragraph(
                        "DOCUMENTACIÓN REQUERIDA PARA CONCRETAR COMPRA",
                        titleFont
                    )
                )

                addElement(Paragraph("* PERSONA NATURAL", titleFont))

                addElement(
                    Paragraph(
                        "* Cédula de Identidad a color, en caso de realizar compras a nombre de otra persona, deberá presentar carta de autorización y copia de cédula de quien realiza la compra y también de quien lo autoriza.",
                        baseFont
                    )
                )

                addElement(Paragraph("* COMPAÑÍA O INSTITUCIÓN", titleFont))
                addElement(Paragraph("* Orden de Compra", baseFont))

                addElement(
                    Paragraph(
                        "* Cédula de Identidad a color y RUC del Representante Legal",
                        baseFont
                    )
                )

                addElement(
                    Paragraph(
                        "* Carta de Autorización y Cédula de Identidad a color de quien realiza la compra",
                        baseFont
                    )
                )

                addElement(Paragraph("NOTA", titleFont))

                addElement(
                    Paragraph(
                        "Los precios están sujetos a cambios y estos tendrán validez y serán respetados hasta 15 días después de la fecha que indica la proforma.",
                        baseFont
                    )
                )
            }

            tableDoc.addCell(cellDoc)
            document.add(tableDoc)

            val separator = Paragraph(
                "_______________________________________________________________________________________________________",
                titleFont
            )

            document.add(separator)
            document.add(Paragraph("\n \n"))

            val table = PdfPTable(2)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(3.5f, 0.5f))

            val items = listOf(
                "SUBTOTAL",
                "DESCUENTO:",
                "SEGURO",
                "IVA 15%",
                "FLETE",
                "TOTAL"
            )

            val amounts = listOf(
                subtotal,
                descuento,
                seguro,
                iva,
                flete,
                total
            )

            items.forEachIndexed { index, item ->
                val itemCell = PdfPCell(Phrase(item, titleFont)).apply {
                    border = PdfPCell.NO_BORDER
                    horizontalAlignment = Element.ALIGN_RIGHT
                }

                table.addCell(itemCell)

                val amountCell = PdfPCell(Phrase(amounts[index], baseFont)).apply {
                    border = PdfPCell.NO_BORDER
                    horizontalAlignment = Element.ALIGN_RIGHT
                }

                table.addCell(amountCell)
            }

            document.add(table)
        }

        private fun createLabelCell(text: String): PdfPCell {
            return PdfPCell(
                Phrase(
                    text,
                    Font(Font.FontFamily.HELVETICA, 8f, Font.BOLD)
                )
            ).apply {
                border = PdfPCell.NO_BORDER
                horizontalAlignment = Element.ALIGN_LEFT
                setPaddingLeft(5f)
            }
        }

        private fun createValueCell(text: String): PdfPCell {
            return PdfPCell(
                Phrase(
                    text,
                    Font(Font.FontFamily.HELVETICA, 8f)
                )
            ).apply {
                border = PdfPCell.NO_BORDER
                horizontalAlignment = Element.ALIGN_LEFT
                setPaddingLeft(10f)
            }
        }

        private fun limpiarNombreArchivo(valor: String): String {
            return valor
                .replace("/", "-")
                .replace("\\", "-")
                .replace(":", "-")
                .replace("*", "")
                .replace("?", "")
                .replace("\"", "")
                .replace("<", "")
                .replace(">", "")
                .replace("|", "")
                .trim()
                .ifBlank { "PROFORMA" }
        }

        private fun fmt2(valor: String?): String {
            return valor
                .orEmpty()
                .trim()
                .replace(",", ".")
                .toBigDecimalOrNull()
                ?.setScale(2, RoundingMode.HALF_UP)
                ?.toPlainString()
                ?: "0.00"
        }
    }
}