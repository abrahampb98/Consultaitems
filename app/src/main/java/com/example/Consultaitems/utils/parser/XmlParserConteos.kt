package com.example.Consultaitems.utils.parser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.util.Xml
import androidx.appcompat.app.AlertDialog
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

class XmlParserConteos {
    companion object {

        fun parseAndUpdateDocumentCode(
            xmlData: String,
            database: SQLiteDatabase,
            context: Context,
            tipo: Int
        ): String {
            return try {
                val respuesta = obtenerRespuesta(xmlData)

                if (respuesta.isNotBlank() && respuesta != "ERROR") {
                    val values = ContentValues().apply {
                        put("co_estado", "C")
                    }

                    try {
                        database.update(
                            "iv_ws_conteo",
                            values,
                            "co_estado = 'A' AND co_tipo = ?",
                            arrayOf(tipo.toString())
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "XmlParserConteos",
                            "No se pudo actualizar iv_ws_conteo: ${e.message}",
                            e
                        )
                    }
                }

                respuesta.ifBlank { "OK" }

            } catch (e: Exception) {
                Log.e("XmlParserConteos", "Error parsing XML: ${e.message}", e)

                runCatching {
                    AlertDialog.Builder(context)
                        .setTitle("Error")
                        .setMessage("Error al procesar XML de conteos: ${e.message}")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .setCancelable(false)
                        .show()
                }

                "ERROR"
            }
        }

        private fun obtenerRespuesta(xmlData: String): String {
            val parsed = parseXmlToMaps(xmlData)
            val firstRow = parsed.firstOrNull().orEmpty()

            return firstRow["Column1"]
                ?: firstRow["codigo"]
                ?: firstRow["documento"]
                ?: firstRow["respuesta"]
                ?: firstRow.values.firstOrNull()
                ?: ""
        }

        private fun parseXmlToMaps(xmlData: String): List<Map<String, String>> {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))

            val rows = mutableListOf<MutableMap<String, String>>()
            var currentRow: MutableMap<String, String>? = null
            var currentTag = ""

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name ?: ""

                        if (currentTag.startsWith("Table", ignoreCase = true)) {
                            currentRow = mutableMapOf()
                        } else if (currentRow != null) {
                            val value = runCatching {
                                parser.nextText()
                            }.getOrDefault("")

                            currentRow[currentTag] = value
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        val name = parser.name ?: ""

                        if (name.startsWith("Table", ignoreCase = true)) {
                            currentRow?.let { rows.add(it) }
                            currentRow = null
                        }
                    }
                }
            }

            return rows
        }
    }
}