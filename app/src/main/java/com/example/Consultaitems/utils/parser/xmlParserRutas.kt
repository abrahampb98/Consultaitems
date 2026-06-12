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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class xmlParserRuta {

    companion object {
        @Throws(XmlPullParserException::class, IOException::class)
        fun parseAndUpdateDocumentCode(xmlData: String, database: SQLiteDatabase, context: Context): String {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))

            val clientesList = mutableListOf<String>()

            try {
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType == XmlPullParser.START_TAG && parser.name == "cliente") {
                        parser.next()
                        val clienteCodigo = parser.text?.trim() // Obtener el código del cliente y limpiar espacios
                        if (!clienteCodigo.isNullOrEmpty()) {
                            clientesList.add(clienteCodigo)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("xmlParserRuta", "Error parsing XML: ${e.message}")

                val builder = AlertDialog.Builder(context)
                builder.setTitle("Error")
                builder.setMessage("Se produjo un error al procesar el XML: ${e.message}")
                builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                builder.setCancelable(false)
                builder.show()
            }

            if (clientesList.isNotEmpty()) {
                actualizarClientes(database, clientesList)
            } else {
                Log.e("xmlParserRuta", "No se encontraron clientes en el XML")
            }

            return clientesList.joinToString(", ") // Retorna los códigos de clientes procesados
        }

        /**
         * 🔹 Actualiza el estado de los clientes en fa_ws_rutaVendedor poniendo `rv_estado = 'C'`
         */
        private fun actualizarClientes(database: SQLiteDatabase, clientesList: List<String>) {
            val values = ContentValues()
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            values.put("rv_fechaproceso", currentDate)

            val placeholders = clientesList.joinToString(",") { "?" }
            val whereClause = "cl_codigo IN ($placeholders)"
            val whereArgs = clientesList.toTypedArray()

            val rowsAffected = database.update("fa_ws_rutaVendedor", values, whereClause, whereArgs)

            if (rowsAffected > 0) {
                Log.d("xmlParserRuta", "Se actualizaron $rowsAffected registros en fa_ws_rutaVendedor")
            } else {
                Log.e("xmlParserRuta", "No se encontró ninguno de los clientes en fa_ws_rutaVendedor")
            }
        }
    }
}
