package com.example.Consultaitems.utils.parser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.util.Xml
import androidx.appcompat.app.AlertDialog
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

class XmlParserProvinciaCiudad {

    companion object INSTANCE {

        fun parseMultiTable(
            xmlData: String,
            database: SQLiteDatabase?,
            context: Context
        ): String {
            if (database == null) return "Error: base de datos no disponible"

            return try {
                val parser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(StringReader(xmlData))

                var currentTable = -1
                var currentValues = ContentValues()
                var currentTag: String? = null

                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            val tag = parser.name.orEmpty()
                            when (tag) {
                                "Table" -> {
                                    currentTable = 0
                                    currentValues = ContentValues()
                                    currentTag = null
                                }
                                "Table1" -> {
                                    currentTable = 1
                                    currentValues = ContentValues()
                                    currentTag = null
                                }
                                "Table2" -> {
                                    currentTable = 2
                                    currentValues = ContentValues()
                                    currentTag = null
                                }
                                "Table3" -> {
                                    currentTable = 3
                                    currentValues = ContentValues()
                                    currentTag = null
                                }
                                "Table4" -> {
                                    currentTable = 4
                                    currentValues = ContentValues()
                                    currentTag = null
                                }
                                "Table5" -> {
                                    currentTable = 5
                                    currentValues = ContentValues()
                                    currentTag = null
                                }
                                else -> {
                                    if (currentTable >= 0) currentTag = tag
                                }
                            }
                        }

                        XmlPullParser.TEXT -> {
                            val tag = currentTag
                            if (currentTable >= 0 && !tag.isNullOrBlank()) {
                                currentValues.put(tag, parser.text?.trim().orEmpty())
                            }
                        }

                        XmlPullParser.END_TAG -> {
                            when (parser.name.orEmpty()) {
                                "Table" -> {
                                    database.insert("se_ciudad", null, currentValues)
                                    currentTable = -1
                                    currentTag = null
                                }
                                "Table1" -> {
                                    database.insert("se_provincia", null, currentValues)
                                    currentTable = -1
                                    currentTag = null
                                }
                                "Table2" -> {
                                    database.insert("cc_ws_dinardapParroquia", null, currentValues)
                                    currentTable = -1
                                    currentTag = null
                                }
                                "Table3" -> {
                                    database.insert("cc_ws_dinardapProvincia", null, currentValues)
                                    currentTable = -1
                                    currentTag = null
                                }
                                "Table4" -> {
                                    database.insert("cc_ws_dinardapCanton", null, currentValues)
                                    currentTable = -1
                                    currentTag = null
                                }
                                "Table5" -> {
                                    database.insert("cc_ws_clienteCategoria", null, currentValues)
                                    currentTable = -1
                                    currentTag = null
                                }
                                else -> {
                                    if (parser.name == currentTag) currentTag = null
                                }
                            }
                        }
                    }

                    eventType = parser.next()
                }

                "Parseo completado correctamente"
            } catch (e: Exception) {
                Log.e("XmlParserProvinciaCiudad", "Error parsing XML: ${e.message}", e)
                runCatching {
                    AlertDialog.Builder(context)
                        .setTitle("Error")
                        .setMessage("Error al procesar XML: ${e.message}")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .setCancelable(false)
                        .show()
                }
                "Error al parsear XML"
            }
        }
    }
}
