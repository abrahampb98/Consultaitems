package com.example.Consultaitems.utils.parser

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.Consultaitems.data.database.SqLiteOpenHelper

class xmlLocalizacion(private val context: Context) {
    fun obtenerXmlocalizaciones(codigo:String): String {
        var xml: String = ""
        val db = abrirBaseDatos()

        try {
            if (codigo.isEmpty()) {
                val cursorC = db.rawQuery("SELECT * FROM ve_ws_rutavendedor WHERE rv_estado = 'A'",null )
                if (cursorC.moveToFirst()) {

                    xml = "'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
                    xml += "<c "
                    // Asume que conoces los nombres de las columnas y su correspondencia en el XML
                    xml += "c0=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("cl_codigo"))}\" "
                    // Continúa para todas las columnas necesarias
                    xml = xml + ">"

                    // Consulta para obtener los detalles del pedido
                    val cursorD = db.rawQuery("SELECT * FROM ve_ws_rutavendedor WHERE rv_estado = 'A'", null)
                    while (cursorD.moveToNext()) {
                        xml += "<detalle "
                        xml += "d0=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("cl_codigo"))}\" " // Ajusta el nombre de columna
                        xml += "d1=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("rv_latitud"))}\" "
                        xml += "d2=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("rv_longitud"))}\" "
                        xml = xml + "></detalle>"
                    }
                    cursorD.close()

                    xml += "</c>',1"
                }
                cursorC.close()
            }else {

                // Consulta para obtener los datos del pedido
                val cursorC =
                    db.rawQuery("SELECT * FROM ve_ws_rutavendedor WHERE rv_estado ='E' ",null)
                if (cursorC.moveToFirst()) {

                    xml = "'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
                    xml += "<c "
                    // Asume que conoces los nombres de las columnas y su correspondencia en el XML
                    xml += "c0=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("cl_codigo"))}\" "
                    // Continúa para todas las columnas necesarias
                    xml = xml + ">"

                    // Consulta para obtener los detalles del pedido
                    val cursorD =
                        db.rawQuery("SELECT * FROM ve_ws_rutavendedor WHERE rv_estado ='E' ",null)
                    while (cursorD.moveToNext()) {
                        xml += "<detalle "
                        xml += "d0=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("cl_codigo"))}\" " // Ajusta el nombre de columna
                        xml += "d1=\"0\" "
                        xml += "d2=\"0\" "
                        xml = xml + "></detalle>"
                    }
                    cursorD.close()

                    xml += "</c>',1"

                }
                cursorC.close()
            }

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
