package com.example.Consultaitems.utils.parser

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.Consultaitems.data.database.SqLiteOpenHelper

class XmlPedido(private val context: Context) {



    fun obtenerXmlPedido(numeroPedido: Int): String {
         var xml: String = ""
        var xmlFinal: String = ""
        val db = abrirBaseDatos()

        try {
            // Consulta para obtener los datos del pedido
            val cursorC = db.rawQuery("SELECT a.* , b.cl_lopdp, b.cl_campania FROM fa_ws_cabpedidoQueue a " +
                    "LEFT JOIN ve_ws_clienteAsignadoVendedor b on a.cl_codigo=b.cl_codigo " +
                    "WHERE pe_coddocumento = ?", arrayOf(numeroPedido.toString()))
            if (cursorC.moveToFirst()) {

                val secuencia = cursorC.getString(cursorC.getColumnIndexOrThrow("pe_coddocumento"))
                val vendedor = cursorC.getString(cursorC.getColumnIndexOrThrow("ep_codigo"))

                xml = "'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
                xml += "<c "
                xml += "c0=\"2\" " //
                xml += "c1=\"1\" "
                xml += "c2=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pe_coddocumento"))}\" "
                xml += "c3=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("ep_codigo"))}\" "
                xml += "c4=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("tr_codigo"))}\" "
                xml += "c5=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("cl_codigo"))}\" "
                xml += "c6=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("tp_codigo"))}\" "
                xml += "c7=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pz_codigo"))}\" "
                xml += "c8=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pe_descripcion"))}\" "
                xml += "c9=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pe_valorbruto"))}\" "
                xml += "c10=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pe_porcentdescuento"))}\" "
                xml += "c11=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pe_valordescuento"))}\" "
                xml += "c12=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pe_seguro"))}\" "
                xml += "c13=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pe_flete"))}\" "
                xml += "c14=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pe_valoriva"))}\" "
                xml += "c15=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pe_valorTotal"))}\" "
                xml += "c16=\"0\" "
                xml += "c17=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pe_orden"))}\" "
                xml += "c18=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pe_usuarioing"))}\" "
                xml += "c19=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("im_codigo"))}\" "
                xml += "c20=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("cl_lopdp"))}\" "
                xml += "c21=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("cl_campania"))}\" "
                // Continúa para todas las columnas necesarias
                xml = xml + ">"

                // Consulta para obtener los detalles del pedido
                val cursorD = db.rawQuery("SELECT * FROM fa_ws_detpedidoQueue WHERE pe_coddocumento = ?", arrayOf(numeroPedido.toString()))
                while (cursorD.moveToNext()) {
                    xml += "<detalle "
                    xml += "d0=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("dp_secuencia"))}\" " // Ajusta el nombre de columna
                    xml += "d1=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("it_codigo"))}\" "
                    xml += "d2=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("dp_cantidad"))}\" "
                    xml += "d3=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("dp_precio"))}\" "
                    xml += "d4=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("dp_descripcion")).replace("\"", " pul").replace("&","AND")}\" "
                    xml += "d5=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("um_pesoCE"))}\" "
                    xml += "d6=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("dp_porcdescuento"))}\" "
                    xml += "d7=\"0\" "
                    xml += "d8=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("dp_combo"))}\" "
                    xml += "d9=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("it_regalo"))}\" "
                    // Continúa para todas las columnas de detalles
                    xml = xml + "></detalle>"
                }
                cursorD.close()

                xml += "</c>',$secuencia,$vendedor,$vendedor,'I',1,5"
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
