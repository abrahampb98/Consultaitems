package com.example.Consultaitems.utils.parser

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.Consultaitems.data.database.SqLiteOpenHelper

class XmlInforme(private val context: Context) {



    fun obtenerXmlInforme(numeRecibo: Int): String {
        var xml: String = ""
        val db = abrirBaseDatos()

        try {
            // Consulta para obtener los datos del pedido
            val cursorC = db.rawQuery("SELECT * FROM co_ws_reciboCobranzaConCab WHERE rc_codrecibo = ?", arrayOf(numeRecibo.toString()))
            if (cursorC.moveToFirst()) {

                val secuencia = cursorC.getString(cursorC.getColumnIndexOrThrow("rc_codrecibo"))
                val usuario = cursorC.getString(cursorC.getColumnIndexOrThrow("ep_codigoRes"))

                xml = "'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
                xml += "<c "
                // Asume que conoces los nombres de las columnas y su correspondencia en el XML
                xml += "c0=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("rc_codrecibo"))}\" "
                xml += "c1=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("ep_codigoBenef"))}\" "
                xml += "c2=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("em_codigo"))}\" "
                xml += "c3=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("ep_codigoRes"))}\" "
                xml += "c4=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("rc_fecharec"))}\" "
                xml += "c5=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("rc_total"))}\" "
                xml += "c6=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("rc_usuarioing"))}\" "
                xml += "c7=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("ae_codigo"))}\" "
                xml += "c8=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("rc_tipo"))}\" "
                xml += "c9=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("mo_codigo"))}\" "
                xml += "c10=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("rc_enlace"))}\" "
                // Continúa para todas las columnas necesarias
                xml = xml + ">"

                // Consulta para obtener los detalles del pedido
                val cursorD = db.rawQuery("SELECT * FROM co_ws_reciboCobranzaConDet WHERE rc_codrecibo = ?", arrayOf(numeRecibo.toString()))
                while (cursorD.moveToNext()) {
                    xml += "<detalle "
                    xml += "d0=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("tr_codigo"))}\" " // Ajusta el nombre de columna
                    xml += "d1=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("ba_codigo"))}\" "
                    xml += "d2=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("rd_documento"))}\" "
                    xml += "d3=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("rd_ncuenta"))}\" "
                    xml += "d4=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("rd_fecha"))}\" "
                    xml += "d5=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("rd_concepto")).replace("\"", " pul").replace("&","AND")}\" "
                    xml += "d6=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("rd_valor"))}\" "
                    xml += "d7=\"0\" "
                    xml += "d8=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("rd_observacion")).replace("\"", " pul").replace("&","AND")}\" "
                    xml += "d9=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("bc_codigo"))}\" "
                    xml += "d10=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("rd_chequeprot"))}\" "
                    xml = xml + "></detalle>"
                }
                cursorD.close()

                xml += "</c>',1,$usuario,$secuencia,2,7"
            }
            cursorC.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        return xml
    }

    private fun abrirBaseDatos(): SQLiteDatabase {
        val dbHelper = SqLiteOpenHelper(context) // Asegúrate de que esta clase esté bien implementada para manejar la base de datos
        return dbHelper.readableDatabase
    }

}
