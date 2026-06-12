package com.example.Consultaitems.utils.parser

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.Consultaitems.data.database.SqLiteOpenHelper

class XmlConteos(private val context: Context) {

    fun obtenerConteo(tipo: Int): String {
        var xml = ""
        val db = abrirBaseDatos()

        try {
            val cursorC = db.rawQuery("SELECT * FROM iv_ws_conteo WHERE co_estado = 'A'", null)
            cursorC.use { cabecera ->
                if (cabecera.moveToFirst()) {
                    var xmlDetalle = "'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><c " +
                        "c0=\"${cabecera.getString(cabecera.getColumnIndexOrThrow("co_usuarioing"))}\" >"

                    val cursorD = db.rawQuery(
                        "SELECT * FROM iv_ws_conteo c WHERE c.co_estado = 'A' AND co_tipo = ?",
                        arrayOf(tipo.toString())
                    )

                    cursorD.use { detalle ->
                        while (detalle.moveToNext()) {
                            val cantidad = detalle.getString(detalle.getColumnIndexOrThrow("co_cantidad"))
                                ?.takeIf { it.isNotBlank() } ?: "0.00"
                            val reconteo = detalle.getString(detalle.getColumnIndexOrThrow("co_reconteo"))
                                ?.takeIf { it.isNotBlank() } ?: "0"

                            xmlDetalle += "<detalle " +
                                "d0=\"${detalle.getString(detalle.getColumnIndexOrThrow("it_codigo"))}\" " +
                                "d1=\"${detalle.getString(detalle.getColumnIndexOrThrow("co_tipo"))}\" " +
                                "d2=\"${detalle.getString(detalle.getColumnIndexOrThrow("bo_codigo"))}\" " +
                                "d3=\"${detalle.getString(detalle.getColumnIndexOrThrow("co_fechatrn"))}\" " +
                                "d4=\"$cantidad\" " +
                                "d5=\"${detalle.getString(detalle.getColumnIndexOrThrow("co_observacion"))}\" " +
                                "d6=\"${detalle.getString(detalle.getColumnIndexOrThrow("co_enlace"))}\" " +
                                "d7=\"$reconteo\" " +
                                "d8=\"${detalle.getString(detalle.getColumnIndexOrThrow("co_linea"))}\" " +
                                "></detalle>"
                        }
                    }

                    xml = "$xmlDetalle</c>',1"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return xml
    }

    private fun abrirBaseDatos(): SQLiteDatabase {
        val dbHelper = SqLiteOpenHelper(context)
        return dbHelper.readableDatabase
    }
}
