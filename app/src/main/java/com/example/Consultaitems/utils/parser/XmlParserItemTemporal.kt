package com.example.Consultaitems.utils.parser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.util.Xml
import androidx.appcompat.app.AlertDialog
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

object XmlParserItemTemporal {

    fun parseToTable(xmlData: String, database: SQLiteDatabase, context: Context): String {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xmlData))

        var currentTableName: String? = null
        var currentValues = ContentValues()

        return try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tagName = parser.name
                        when (tagName) {
                            "Table" -> {
                                currentTableName = "ve_ws_itemTmp"
                                currentValues = ContentValues()
                            }
                            "Table1" -> {
                                currentTableName = "iv_ws_itemComboCabTmp"
                                currentValues = ContentValues()
                            }
                            else -> {
                                val table = currentTableName
                                if (table != null) {
                                    val value = parser.nextText().trim()
                                    if (value.isNotEmpty()) {
                                        if (table == "iv_ws_itemComboCabTmp") {
                                            when (tagName) {
                                                "cb_codigo" -> currentValues.put("cb_codigo", value.toIntOrNull() ?: 0)
                                                "cb_descripcionA" -> currentValues.put("cb_descripcionA", value)
                                                "cb_monto" -> currentValues.put("cb_monto", value.toDoubleOrNull() ?: 0.0)
                                                "cb_montocp" -> currentValues.put("cb_montocp", value.toDoubleOrNull() ?: 0.0)
                                                "cb_margen" -> currentValues.put("cb_margen", value.toDoubleOrNull() ?: 0.0)
                                            }
                                        } else if (table == "ve_ws_itemTmp") {
                                            currentValues.put(tagName, value)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val tagName = parser.name
                        if (tagName == "Table" || tagName == "Table1") {
                            val table = currentTableName
                            if (table != null && currentValues.size() > 0) {
                                val rowId = database.insert(table, null, currentValues)
                                Log.d(TAG, "Insertando en $table -> $currentValues")
                                Log.d(TAG, "Resultado insert: $rowId")
                                if (rowId == -1L) {
                                    Log.e(TAG, "No se pudo insertar en $table")
                                }
                            } else {
                                Log.w(TAG, "No hay datos para insertar en $table")
                            }
                            currentTableName = null
                            currentValues = ContentValues()
                        }
                    }
                }
                eventType = parser.next()
            }
            "Parseo completado correctamente"
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing XML: ${e.message}", e)
            AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage("Error al procesar XML: ${e.message}")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .setCancelable(false)
                .show()
            "Error al parsear XML"
        }
    }

    private const val TAG = "XmlParserItemTemporal"
}
