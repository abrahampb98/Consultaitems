package com.example.Consultaitems.utils.parser

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Locale

class XmlFactura(private val context: Context) {

    /**
     * Versión actual del APK.
     *
     * Se usa desde frmProformaA para verificar stock o procesar factura.
     *
     * Formato:
     * '',empresa,bodega,proforma,'usuario',opcion
     */
    fun obtenerFactura(
        empresa: Int,
        bodega: Int,
        proforma: Int,
        usuario: String,
        opcion: Int
    ): String {
        return try {
            "'',$empresa,$bodega,$proforma,'$usuario',$opcion"
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Versión compatible con tu respaldo anterior.
     *
     * La dejo para no romper llamadas antiguas que todavía usen:
     * obtenerFactura(numeroPedido)
     */
    fun obtenerFactura(numeroPedido: Int): String {
        val db = abrirBaseDatos()
        var cursorC: Cursor? = null
        var cursorD: Cursor? = null

        return try {
            cursorC = db.rawQuery(
                "SELECT * FROM fa_ws_cabproforma WHERE pr_codigo = ?",
                arrayOf(numeroPedido.toString())
            )

            if (!cursorC.moveToFirst()) {
                return ""
            }

            val empresa = cursorC.valor("em_codigo")
            val bodega = cursorC.valor("bo_codigo")
            val proforma = cursorC.valor("pr_codigoA")
            val fecha = cursorC.valor("pr_fechatrn")
            val usuario = cursorC.valor("pr_usuarioing")
            val proceso = 0

            val fechaFormateada = formatearFecha(fecha)

            val xml = StringBuilder()
            xml.append("'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>")
            xml.append("<c ")
            xml.append("c0=\"").append(esc(cursorC.valor("em_codigo"))).append("\" ")
            xml.append("c1=\"").append(esc(cursorC.valor("bo_codigo"))).append("\" ")
            xml.append("c2=\"").append(esc(cursorC.valor("pr_codigo"))).append("\" ")
            xml.append("c3=\"").append(esc(fechaFormateada)).append("\" ")
            xml.append("c4=\"").append(esc(cursorC.valor("ep_codigo"))).append("\" ")
            xml.append("c5=\"").append(esc(cursorC.valor("cl_codigo"))).append("\" ")
            xml.append("c6=\"").append(esc(cursorC.valor("pr_cedula"))).append("\" ")
            xml.append("c7=\"").append(esc(cursorC.valor("pr_nombre"))).append("\" ")
            xml.append("c8=\"").append(esc(cursorC.valor("pr_direccion"))).append("\" ")
            xml.append("c9=\"").append(esc(cursorC.valor("pr_fono"))).append("\" ")
            xml.append("c10=\"").append(esc(cursorC.valor("tr_codigo"))).append("\" ")
            xml.append("c11=\"").append(esc(cursorC.valor("tp_codigo"))).append("\" ")
            xml.append("c12=\"").append(esc(cursorC.valor("pr_estadopago"))).append("\" ")
            xml.append("c13=\"").append(esc(cursorC.valor("pr_observacion"))).append("\" ")
            xml.append("c14=\"").append(esc(cursorC.valor("pr_valorbruto"))).append("\" ")
            xml.append("c15=\"").append(esc(cursorC.valor("pr_valordesc"))).append("\" ")
            xml.append("c16=\"").append(esc(cursorC.valor("pr_porcdesc"))).append("\" ")
            xml.append("c17=\"").append(esc(cursorC.valor("pr_valorseguro"))).append("\" ")
            xml.append("c18=\"").append(esc(cursorC.valor("pr_porcseguro"))).append("\" ")
            xml.append("c19=\"").append(esc(cursorC.valor("pr_valorflete"))).append("\" ")
            xml.append("c20=\"").append(esc(cursorC.valor("pr_valoriva"))).append("\" ")
            xml.append("c21=\"").append(esc(cursorC.valor("pr_valortotal"))).append("\" ")
            xml.append("c22=\"").append(esc(cursorC.valor("pr_usuarioing"))).append("\" ")
            xml.append("c23=\"").append(proceso).append("\" ")
            xml.append("c24=\"").append(esc(cursorC.valor("pr_codigo"))).append("\" ")
            xml.append(">")

            cursorD = db.rawQuery(
                "SELECT * FROM fa_ws_detproforma WHERE pr_codigo = ?",
                arrayOf(numeroPedido.toString())
            )

            while (cursorD.moveToNext()) {
                xml.append("<detalle ")
                xml.append("d0=\"").append(proceso).append("\" ")
                xml.append("d1=\"").append(esc(cursorD.valor("dp_secuencia"))).append("\" ")
                xml.append("d2=\"").append(esc(cursorD.valor("it_codigo"))).append("\" ")
                xml.append("d3=\"0\" ")
                xml.append("d4=\"").append(esc(cursorD.valor("dp_cantidad"))).append("\" ")
                xml.append("d5=\"").append(esc(cursorD.valor("dp_precio"))).append("\" ")
                xml.append("d6=\"").append(esc(cursorD.valor("dp_valorbruto"))).append("\" ")
                xml.append("d7=\"").append(esc(cursorD.valor("um_pesoCE"))).append("\" ")
                xml.append("d8=\"").append(esc(cursorD.valor("dp_porcdescto"))).append("\" ")
                xml.append("d9=\"").append(esc(cursorD.valor("cb_codigo"))).append("\" ")
                xml.append("d10=\"").append(esc(cursorD.valor("it_regalo"))).append("\" ")
                xml.append("></detalle>")
            }

            xml.append("</c>',")
                .append(empresa).append(",")
                .append(bodega).append(",")
                .append(proforma).append(",'")
                .append(usuario).append("',1")

            xml.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        } finally {
            cursorD?.close()
            cursorC?.close()
            db.close()
        }
    }

    /**
     * Recuperado del APK actual.
     *
     * Formato:
     * '',"em_codigo","bo_codigo","pr_codigoA","pr_usuarioing",2
     */
    fun fnVerificarStock(numeroProform: Int): String {
        val db = abrirBaseDatos()
        var cursorC: Cursor? = null

        return try {
            cursorC = db.rawQuery(
                "SELECT * FROM fa_ws_cabproforma WHERE pr_codigo = ?",
                arrayOf(numeroProform.toString())
            )

            if (cursorC.moveToFirst()) {
                "''," +
                        "\"${cursorC.valor("em_codigo")}\"," +
                        "\"${cursorC.valor("bo_codigo")}\"," +
                        "\"${cursorC.valor("pr_codigoA")}\"," +
                        "\"${cursorC.valor("pr_usuarioing")}\"," +
                        "2"
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        } finally {
            cursorC?.close()
            db.close()
        }
    }

    private fun abrirBaseDatos(): SQLiteDatabase {
        val dbHelper = SqLiteOpenHelper(context)
        return dbHelper.readableDatabase
    }

    private fun Cursor.valor(columna: String): String {
        val index = getColumnIndexOrThrow(columna)
        return if (isNull(index)) "" else getString(index).orEmpty()
    }

    private fun formatearFecha(fecha: String): String {
        val salida = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault())
        val formatos = listOf(
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy"
        )

        for (formato in formatos) {
            val date = runCatching {
                SimpleDateFormat(formato, Locale.getDefault()).parse(fecha)
            }.getOrNull()

            if (date != null) {
                return salida.format(date)
            }
        }

        return fecha
    }

    private fun esc(valor: String?): String {
        return valor.orEmpty()
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
