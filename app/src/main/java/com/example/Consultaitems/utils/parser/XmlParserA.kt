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

class XMlParserA {

    companion object {
        @Throws(XmlPullParserException::class, IOException::class)
        fun parseAndUpdateDocumentCode(xmlData: String, database: SQLiteDatabase, pedido: String, context: Context):String  {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))
            var secuenciaValue: String? = null

            try {
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType == XmlPullParser.START_TAG && parser.name == "secuencia") {
                        parser.next()
                        secuenciaValue = parser.text
                        break
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

            secuenciaValue?.let {
                // Actualiza la base de datos solo si se encontró un valor para 'secuencia'
                val values = ContentValues()
                val estado: String = "C"
                values.put("pe_coddocumentoA", it)
                values.put("pe_estado", estado)
                val rowsAffected = database.update("fa_ws_cabpedidoQueue", values, "pe_coddocumento = ?", arrayOf(pedido))
                if (rowsAffected > 0) {
                    Log.d("SecuenciaXMLParser", "Updated successfully")
                } else {
                    Log.e("SecuenciaXMLParser", "Update failed")
                }
            } ?: Log.e("SecuenciaXMLParser", "No 'secuencia' tag found in the XML data")
            return secuenciaValue.toString()
        }
    }
}
