package com.example.Consultaitems.utils.parser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.util.Xml
import androidx.appcompat.app.AlertDialog
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

class XmlParserItems {
    companion object {
        fun parseMultiTable(
            xmlData: String,
            database: SQLiteDatabase,
            context: Context
        ): String {
            return try {
                val parser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(StringReader(xmlData))

                var currentTable = -1
                var currentValues = ContentValues()
                var currentTag = ""

                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    when (parser.eventType) {
                        XmlPullParser.START_TAG -> {
                            currentTag = parser.name ?: ""
                            when (currentTag) {
                                "Table" -> {
                                    currentTable = 0
                                    currentValues = ContentValues()
                                }
                                "Table1" -> {
                                    currentTable = 1
                                    currentValues = ContentValues()
                                }
                                "Table2" -> {
                                    currentTable = 2
                                    currentValues = ContentValues()
                                }
                                "Table3" -> {
                                    currentTable = 3
                                    currentValues = ContentValues()
                                }
                                "Table4" -> {
                                    currentTable = 4
                                    currentValues = ContentValues()
                                }
                                else -> {
                                    if (currentTable >= 0) {
                                        val text = parser.nextText() ?: ""
                                        currentValues.put(currentTag, text)
                                    }
                                }
                            }
                        }

                        XmlPullParser.END_TAG -> {
                            when (parser.name) {
                                "Table" -> {
                                    database.insert("ve_ws_usuario", null, currentValues)
                                    currentTable = -1
                                }
                                "Table2" -> {
                                    database.insert("iv_ws_bodega", null, currentValues)
                                    currentTable = -1
                                }
                                "Table3" -> {
                                    database.insert("iv_ws_itemxbodega", null, currentValues)
                                    currentTable = -1
                                }
                                "Table4" -> {
                                    database.insert("ve_ws_clienteAsignadoVendedor", null, currentValues)
                                    currentTable = -1
                                }
                            }
                        }
                    }
                }

                "Parseo completado correctamente"
            } catch (e: Exception) {
                Log.e("XmlParserItems", "Error parsing XML: ${e.message}", e)
                AlertDialog.Builder(context)
                    .setTitle("Error")
                    .setMessage("Error al procesar XML: ${e.message}")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .setCancelable(false)
                    .show()
                "Error al parsear XML"
            }
        }
    }
}
