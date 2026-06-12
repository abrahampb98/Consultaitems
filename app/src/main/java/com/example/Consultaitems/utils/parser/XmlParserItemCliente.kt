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

class XmlParserItemxCliente {
    companion object {
        @Throws(XmlPullParserException::class, IOException::class)
        fun parseItemxCliente(xmlData: String, database: SQLiteDatabase, cadena: String, context: Context): String {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))
            var secuenciaValue: String? = null

            // Borra los datos existentes en la tabla
            database.execSQL("DELETE FROM ve_ws_itemCliente")

            // Variables para almacenar los valores de las etiquetas
            var codcliente: String? = null
            var cliente: String? = null
            var coditem: String? = null
            var referencia: String? = null
            var factura: String? = null
            var sri: String? = null
            var fechafactura: String? = null
            var cantidad: String? = null
            var precioventa: String? = null
            var total: String? = null
            var bo_descripcion: String? = null
            var estado: String? = null
            var bo_codigo: String? = null
            var fa_fechafactura: String? = null
            var tipofactura: String? = null
            var it_orden: String? = null
            var it_orden1: String? = null
            var ma_descripcion: String? = null

            try {
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    when (parser.eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "Table" -> Log.d("XmlParserItemxCliente", "Start of Table")
                                "codcliente" -> {
                                    parser.next()
                                    codcliente = parser.text
                                }
                                "cliente" -> {
                                    parser.next()
                                    cliente = parser.text
                                }
                                "coditem" -> {
                                    parser.next()
                                    coditem = parser.text
                                }
                                "referencia" -> {
                                    parser.next()
                                    referencia = parser.text
                                }
                                "factura" -> {
                                    parser.next()
                                    factura = parser.text
                                }
                                "sri" -> {
                                    parser.next()
                                    sri = parser.text
                                }
                                "fechafactura" -> {
                                    parser.next()
                                    fechafactura = parser.text
                                }
                                "cantidad" -> {
                                    parser.next()
                                    cantidad = parser.text
                                }
                                "precioventa" -> {
                                    parser.next()
                                    precioventa = parser.text
                                }
                                "total" -> {
                                    parser.next()
                                    total = parser.text
                                }
                                "bo_descripcion" -> {
                                    parser.next()
                                    bo_descripcion = parser.text
                                }
                                "estado" -> {
                                    parser.next()
                                    estado = parser.text
                                }
                                "bo_codigo" -> {
                                    parser.next()
                                    bo_codigo = parser.text
                                }
                                "fa_fechafactura" -> {
                                    parser.next()
                                    fa_fechafactura = parser.text
                                }
                                "tipofactura" -> {
                                    parser.next()
                                    tipofactura = parser.text
                                }
                                "it_orden" -> {
                                    parser.next()
                                    it_orden = parser.text
                                }
                                "it_orden1" -> {
                                    parser.next()
                                    it_orden1 = parser.text
                                }
                                "ma_descripcion" -> {
                                    parser.next()
                                    ma_descripcion = parser.text
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "Table") {
                                Log.d("XmlParserItemxCliente", "End of Table tag")
                                // Verificar que todos los valores requeridos fueron encontrados
                                if (cliente != null && coditem != null && referencia != null && factura != null &&
                                    sri != null && fechafactura != null && cantidad != null && precioventa != null &&
                                    total != null && bo_descripcion != null && estado != null && bo_codigo != null &&
                                    tipofactura != null && it_orden != null && ma_descripcion != null) {

                                    // Insertar valores en la base de datos
                                    val values = ContentValues().apply {
                                        put("cliente", cliente)
                                        put("coditem", coditem)
                                        put("referencia", referencia)
                                        put("factura", factura)
                                        put("sri", sri)
                                        put("fechafactura", fechafactura)
                                        put("cantidad", cantidad)
                                        put("precioventa", precioventa)
                                        put("total", total)
                                        put("bo_descripcion", bo_descripcion)
                                        put("estado", estado)
                                        put("bo_codigo", bo_codigo)
                                        put("tipofactura", tipofactura)
                                        put("it_orden", it_orden)
                                        put("ma_descripcion", ma_descripcion)
                                    }
                                    val newRowId = database.insert("ve_ws_itemCliente", null, values)
                                    if (newRowId == -1L) {
                                        Log.e("ItemCliente", "Error inserting into database")
                                    } else {
                                        Log.d("ItemCliente", "Inserted successfully with row id: $newRowId")
                                    }
                                } else {
                                    Log.e("ItemCliente", "Missing required fields in the XML data")
                                }

                                // Restablecer las variables para la siguiente fila
                                codcliente = null
                                cliente = null
                                coditem = null
                                referencia = null
                                factura = null
                                sri = null
                                fechafactura = null
                                cantidad = null
                                precioventa = null
                                total = null
                                bo_descripcion = null
                                estado = null
                                bo_codigo = null
                                fa_fechafactura = null
                                tipofactura = null
                                it_orden = null
                                it_orden1 = null
                                ma_descripcion = null
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SecuenciaXMLParser", "Error parsing XML: ${e.message}")

                val builder = AlertDialog.Builder(context)
                builder.setTitle("Error")
                builder.setMessage("Se produjo un error al procesar el XML: ${e.message}")
                builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                builder.setCancelable(false)
                builder.show()
            }

            return secuenciaValue.toString()
        }
    }
}