package com.example.Consultaitems.utils.parser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.util.Xml
import androidx.appcompat.app.AlertDialog
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.StringReader

class XmlParserAuditoriaPedido {
    companion object {
        @Throws(XmlPullParserException::class, IOException::class)
        fun parseAuditoriaPedido(xmlData: String, database: SQLiteDatabase, cadena: String, context: Context): String {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))

            // Variables para almacenar los valores de las etiquetas
            var pe_coddocumento: String? = null
            var nombreCliente: String? = null
            var te_descripcion: String? = null
            var pe_fechaing: String? = null
            var pe_valorTotal: String? = null
            var fa_coddocumento: String? = null
            var fa_sri: String? = null
            var fa_fechafactura: String? = null
            var fa_guiaremision: String? = null
            var estado: String? = null
            var pe_observacion: String? = null
            var bodega: String? = null
            var totalfact: String? = null

            try {
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    when (parser.eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "pe_coddocumento" -> {
                                    parser.next()
                                    pe_coddocumento = parser.text
                                }
                                "nombreCliente" -> {
                                    parser.next()
                                    nombreCliente = parser.text
                                }
                                "te_descripcion" -> {
                                    parser.next()
                                    te_descripcion = parser.text
                                }
                                "pe_fechaing" -> {
                                    parser.next()
                                    pe_fechaing = parser.text
                                }
                                "pe_valorTotal" -> {
                                    parser.next()
                                    pe_valorTotal = parser.text
                                }
                                "fa_coddocumento" -> {
                                    parser.next()
                                    fa_coddocumento = parser.text
                                }
                                "fa_sri" -> {
                                    parser.next()
                                    fa_sri = parser.text
                                }
                                "fa_fechafactura" -> {
                                    parser.next()
                                    fa_fechafactura = parser.text
                                }
                                "fa_guiaremision" -> {
                                    parser.next()
                                    fa_guiaremision = parser.text
                                }
                                "estado" -> {
                                    parser.next()
                                    estado = parser.text
                                }
                                "pe_observacion" -> {
                                    parser.next()
                                    pe_observacion = parser.text
                                }
                                "bodega" -> {
                                    parser.next()
                                    bodega = parser.text
                                }
                                "totalfact" -> {
                                    parser.next()
                                    totalfact = parser.text
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "Table") {
                                // Insertar valores en la base de datos si todos los campos están presentes
                                if (pe_coddocumento != null && nombreCliente != null ) {

                                    val values = ContentValues().apply {
                                        put("pe_coddocumento", pe_coddocumento)
                                        put("nombreCliente", nombreCliente)
                                        put("te_descripcion", te_descripcion)
                                        put("pe_fechaing", pe_fechaing)
                                        put("pe_valorTotal", pe_valorTotal)
                                        put("fa_coddocumento", fa_coddocumento)
                                        put("fa_sri", fa_sri)
                                        put("fa_fechafactura", fa_fechafactura)
                                        put("fa_guiaremision", fa_guiaremision)
                                        put("estado", estado)
                                        put("pe_observacion", pe_observacion)
                                        put("bodega", bodega)
                                        put("totalfact", totalfact)
                                    }
                                    val newRowId = database.insert("fa_ws_auditoriapedido", null, values)
                                    if (newRowId == -1L) {
                                        Log.e("AuditoriaPedido", "Error inserting into database")
                                    } else {
                                        Log.d("AuditoriaPedido", "Inserted successfully with row id: $newRowId")
                                    }
                                } else {
                                    Log.e("AuditoriaPedido", "Missing required fields in the XML data")
                                }
                                // Restablecer las variables para la siguiente fila
                                pe_coddocumento = null
                                nombreCliente = null
                                te_descripcion = null
                                pe_fechaing = null
                                pe_valorTotal = null
                                fa_coddocumento = null
                                fa_sri = null
                                fa_fechafactura = null
                                fa_guiaremision = null
                                estado = null
                                pe_observacion = null
                                bodega = null
                                totalfact = null
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AuditoriaPedidoXMLParser", "Error parsing XML: ${e.message}")
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Error")
                builder.setMessage("Se produjo un error al procesar el XML: ${e.message}")
                builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                builder.setCancelable(false)
                builder.show()
            }

            return "Parseo completado"
        }
    }
}
