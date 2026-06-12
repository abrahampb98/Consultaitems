package com.example.Consultaitems.utils.parser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.util.Xml
import androidx.appcompat.app.AlertDialog
import com.itextpdf.kernel.pdf.tagging.StandardRoles
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.StringReader

class XmlParserClientItems {

    companion object {

        @Throws(XmlPullParserException::class, IOException::class)
        fun parseTable(
            xmlData: String,
            database: SQLiteDatabase,
            context: Context
        ): String {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))

            var currentTable = -1
            var currentValues = ContentValues()

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                try {
                    when (parser.eventType) {
                        XmlPullParser.START_TAG -> {
                            val currentTag = parser.name

                            if (currentTag == StandardRoles.TABLE) {
                                currentTable = 0
                                currentValues = ContentValues()
                            } else {
                                parser.next()
                                val text = parser.text ?: ""

                                if (currentTable == 0) {
                                    currentValues.put(currentTag, text)
                                }
                            }
                        }

                        XmlPullParser.END_TAG -> {
                            if (parser.name == StandardRoles.TABLE) {
                                database.insert(
                                    "fa_ws_ventasTmp",
                                    null,
                                    currentValues
                                )
                                currentTable = -1
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        "XmlParserPedidosMulti",
                        "Error parsing XML: ${e.message}"
                    )

                    AlertDialog.Builder(context)
                        .setTitle("Error")
                        .setMessage("Error al procesar XML: ${e.message}")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setCancelable(false)
                        .show()

                    return "Error al parsear XML"
                }
            }

            return "Parseo completado correctamente"
        }
    }
}