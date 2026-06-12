package com.example.Consultaitems.utils.parser

import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.util.Xml
import androidx.appcompat.app.AlertDialog
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.StringReader

class XmlSincronizacion {
    companion object {
        private const val TAG = "XmlParserPedidosMulti"

        @Throws(XmlPullParserException::class, IOException::class)
        fun parseMultiTable(
            xmlData: String,
            database: SQLiteDatabase,
            context: Context
        ): String {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))

            var currentTable = -1
            var currentValues = ContentValues()

            return try {
                var eventType = parser.eventType

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            val currentTag = parser.name ?: ""

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
                                    val codigo = currentValues.getAsString("it_codigo")
                                    if (!codigo.isNullOrBlank()) {
                                        database.rawQuery(
                                            "SELECT it_codigo FROM ve_ws_item WHERE it_codigo = ?",
                                            arrayOf(codigo)
                                        ).use { cursor ->
                                            val exists = cursor.count > 0
                                            if (exists) {
                                                database.update(
                                                    "ve_ws_item",
                                                    currentValues,
                                                    "it_codigo = ?",
                                                    arrayOf(codigo)
                                                )
                                            } else {
                                                database.insert("ve_ws_item", null, currentValues)
                                            }
                                        }
                                    }
                                    currentTable = -1
                                }

                                "Table1" -> {
                                    database.insert("ve_ws_clienteAsignadoVendedor", null, currentValues)
                                    currentTable = -1
                                }

                                "Table2" -> {
                                    database.insert("iv_ws_itemComboCab", null, currentValues)
                                    currentTable = -1
                                }

                                "Table3" -> {
                                    database.insert("iv_ws_itemComboDet", null, currentValues)
                                    currentTable = -1
                                }
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
                    .setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()

                "Error al parsear XML"
            }
        }
    }
}
