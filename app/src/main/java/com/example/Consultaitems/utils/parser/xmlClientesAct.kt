package com.example.Consultaitems.utils.parser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Xml
import androidx.appcompat.app.AlertDialog
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

class xmlClientesAct {

    companion object {

        fun parseAndUpdateDocumentCode(
            xmlData: String,
            database: SQLiteDatabase,
            context: Context
        ): String {
            val clientesList = mutableListOf<String>()

            try {
                val parser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(StringReader(xmlData))

                var eventType = parser.eventType

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (
                        eventType == XmlPullParser.START_TAG &&
                        parser.name == "cliente"
                    ) {
                        val clienteCodigo = parser.nextText()?.trim()

                        if (!clienteCodigo.isNullOrEmpty()) {
                            clientesList.add(clienteCodigo)
                        }
                    }

                    eventType = parser.next()
                }

                if (clientesList.isNotEmpty()) {
                    actualizarClientes(database, clientesList)
                } else {
                    Log.e("xmlParserRuta", "No se encontraron clientes en el XML")
                }

            } catch (e: Exception) {
                Log.e("xmlParserRuta", "Error parsing XML: ${e.message}", e)

                Handler(Looper.getMainLooper()).post {
                    AlertDialog.Builder(context)
                        .setTitle("Error")
                        .setMessage("Se produjo un error al procesar el XML: ${e.message}")
                        .setPositiveButton("OK") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }
                        .setCancelable(false)
                        .show()
                }
            }

            return clientesList.joinToString(", ")
        }

        private fun actualizarClientes(
            database: SQLiteDatabase,
            clientesList: List<String>
        ) {
            val values = ContentValues().apply {
                put("cl_estado", "C")
            }

            val placeholders = clientesList.joinToString(",") { "?" }
            val whereClause = "cl_codigo IN ($placeholders)"
            val whereArgs = clientesList.toTypedArray()

            val rowsAffected = database.update(
                "ve_ws_clienteAsignadoVendedor",
                values,
                whereClause,
                whereArgs
            )

            if (rowsAffected > 0) {
                Log.d(
                    "ClientesActualizados",
                    "Se actualizaron $rowsAffected registros en ve_ws_clienteAsignadoVendedor"
                )
            } else {
                Log.e(
                    "ClientesActualizados",
                    "No se encontró ninguno de los clientes en ve_ws_clienteAsignadoVendedor"
                )
            }
        }
    }
}