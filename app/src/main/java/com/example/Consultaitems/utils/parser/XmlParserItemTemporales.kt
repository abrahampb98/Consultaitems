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

class XmlParserItemTemporales {

    companion object {

        fun parseToTable(
            xmlData: String,
            database: SQLiteDatabase,
            context: Context
        ): String {
            return try {
                val parser = Xml.newPullParser().apply {
                    setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                    setInput(StringReader(xmlData))
                }

                var currentTable = -1
                var currentValues = ContentValues()

                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    when (parser.eventType) {

                        XmlPullParser.START_TAG -> {
                            val currentTag = parser.name ?: ""

                            if (currentTag == "Table") {
                                currentTable = 0
                                currentValues = ContentValues()
                            } else if (currentTable == 0) {
                                val text = parser.nextText() ?: ""
                                currentValues.put(currentTag, text)
                            }
                        }

                        XmlPullParser.END_TAG -> {
                            if (parser.name == "Table" && currentTable == 0) {
                                database.insert(
                                    "ve_ws_itemTmp",
                                    null,
                                    currentValues
                                )

                                currentTable = -1
                                currentValues = ContentValues()
                            }
                        }
                    }
                }

                "Parseo completado correctamente"

            } catch (e: Exception) {
                Log.e("ve_ws_itemTmp", "Error parsing XML: ${e.message}", e)

                Handler(Looper.getMainLooper()).post {
                    AlertDialog.Builder(context)
                        .setTitle("Error")
                        .setMessage("Error al procesar XML: ${e.message}")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setCancelable(false)
                        .show()
                }

                "Error al parsear XML"
            }
        }
    }
}
