package com.example.Consultaitems.utils.parser

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.ui.adapters.datosDet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class XmlProforma(private val context: Context) {

    private fun abrirBaseDatos(): SQLiteDatabase {
        val dbHelper = SqLiteOpenHelper(context)
        return dbHelper.readableDatabase
    }

    private fun Cursor.text(column: String): String {
        val index = getColumnIndexOrThrow(column)
        return if (isNull(index)) "" else getString(index).orEmpty()
    }

    private fun esc(value: String?): String {
        return value.orEmpty()
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun formatearFechaXml(fecha: String): String {
        val output = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault())
        val patrones = listOf(
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy"
        )

        for (patron in patrones) {
            val date = runCatching {
                SimpleDateFormat(patron, Locale.getDefault()).parse(fecha)
            }.getOrNull()

            if (date != null) {
                return output.format(date)
            }
        }

        return fecha
    }

    private fun String.toIntSafe(): Int = trim().toIntOrNull() ?: 0

    private fun String.toDoubleSafe(): Double {
        return trim().replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    private fun fmt3(value: Double): String {
        return String.format(Locale.US, "%.3f", value)
    }

    private fun getterOpcional(detalle: datosDet, getterName: String, fallback: String): String {
        return runCatching {
            detalle.javaClass.getMethod(getterName).invoke(detalle)?.toString().orEmpty()
        }.getOrDefault(fallback)
    }

    fun obtenerXmlProforma(numeroPedido: Int): String {
        val db = abrirBaseDatos()
        var cursorCabecera: Cursor? = null
        var cursorDetalle: Cursor? = null

        return try {
            cursorCabecera = db.rawQuery(
                "SELECT * FROM fa_ws_cabproforma WHERE pr_codigo = ?",
                arrayOf(numeroPedido.toString())
            )

            if (!cursorCabecera.moveToFirst()) {
                return ""
            }

            val empresa = cursorCabecera.text("em_codigo")
            val bodega = cursorCabecera.text("bo_codigo")
            val secuencia = cursorCabecera.text("pr_codigo")
            val fecha = cursorCabecera.text("pr_fechatrn")
            val usuario = cursorCabecera.text("pr_usuarioing")
            val fechaFormateada = formatearFechaXml(fecha)

            val xml = StringBuilder()
            xml.append("'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><c ")
            xml.append("c0=\"").append(esc(empresa)).append("\" ")
            xml.append("c1=\"").append(esc(bodega)).append("\" ")
            xml.append("c2=\"").append(esc(secuencia)).append("\" ")
            xml.append("c3=\"").append(esc(fechaFormateada)).append("\" ")
            xml.append("c4=\"").append(esc(cursorCabecera.text("ep_codigo"))).append("\" ")
            xml.append("c5=\"").append(esc(cursorCabecera.text("cl_codigo"))).append("\" ")
            xml.append("c6=\"").append(esc(cursorCabecera.text("pr_cedula"))).append("\" ")
            xml.append("c7=\"").append(esc(cursorCabecera.text("pr_nombre"))).append("\" ")
            xml.append("c8=\"").append(esc(cursorCabecera.text("pr_direccion"))).append("\" ")
            xml.append("c9=\"").append(esc(cursorCabecera.text("pr_fono"))).append("\" ")
            xml.append("c10=\"").append(esc(cursorCabecera.text("tr_codigo"))).append("\" ")
            xml.append("c11=\"").append(esc(cursorCabecera.text("tp_codigo"))).append("\" ")
            xml.append("c12=\"").append(esc(cursorCabecera.text("pr_estadopago"))).append("\" ")
            xml.append("c13=\"").append(esc(cursorCabecera.text("pr_observacion"))).append("\" ")
            xml.append("c14=\"").append(esc(cursorCabecera.text("pr_valorbruto"))).append("\" ")
            xml.append("c15=\"").append(esc(cursorCabecera.text("pr_valordesc"))).append("\" ")
            xml.append("c16=\"").append(esc(cursorCabecera.text("pr_porcdesc"))).append("\" ")
            xml.append("c17=\"").append(esc(cursorCabecera.text("pr_valorseguro"))).append("\" ")
            xml.append("c18=\"").append(esc(cursorCabecera.text("pr_porcseguro"))).append("\" ")
            xml.append("c19=\"").append(esc(cursorCabecera.text("pr_valorflete"))).append("\" ")
            xml.append("c20=\"").append(esc(cursorCabecera.text("pr_valoriva"))).append("\" ")
            xml.append("c21=\"").append(esc(cursorCabecera.text("pr_valortotal"))).append("\" ")
            xml.append("c22=\"").append(esc(usuario)).append("\" ")
            xml.append("c23=\"0\" ")
            xml.append("c24=\"").append(esc(secuencia)).append("\" ")
            xml.append("c25=\"").append(esc(cursorCabecera.text("pr_email"))).append("\" ")
            xml.append(">")

            cursorDetalle = db.rawQuery(
                "SELECT * FROM fa_ws_detproforma WHERE pr_codigo = ?",
                arrayOf(numeroPedido.toString())
            )

            while (cursorDetalle.moveToNext()) {
                xml.append("<detalle ")
                xml.append("d0=\"0\" ")
                xml.append("d1=\"").append(esc(cursorDetalle.text("dp_secuencia"))).append("\" ")
                xml.append("d2=\"").append(esc(cursorDetalle.text("it_codigo"))).append("\" ")
                xml.append("d3=\"0\" ")
                xml.append("d4=\"").append(esc(cursorDetalle.text("dp_cantidad"))).append("\" ")
                xml.append("d5=\"").append(esc(cursorDetalle.text("dp_precio"))).append("\" ")
                xml.append("d6=\"").append(esc(cursorDetalle.text("dp_valorbruto"))).append("\" ")
                xml.append("d7=\"").append(esc(cursorDetalle.text("um_pesoCE"))).append("\" ")
                xml.append("d8=\"").append(esc(cursorDetalle.text("dp_porcdescto"))).append("\" ")
                xml.append("d9=\"").append(esc(cursorDetalle.text("cb_codigo"))).append("\" ")
                xml.append("d10=\"").append(esc(cursorDetalle.text("it_regalo"))).append("\" ")
                xml.append("></detalle>")
            }

            xml.append("</c>',1,")
                .append(empresa).append(",")
                .append(bodega).append(",")
                .append(secuencia).append(",'")
                .append(fecha).append("','")
                .append(usuario).append("','I'")

            xml.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        } finally {
            cursorDetalle?.close()
            cursorCabecera?.close()
            db.close()
        }
    }

    fun fnProformaXML(
        empresa: String,
        bodega: String,
        secuencia: String,
        fecha: String,
        usuario: String,
        epCodigo: String,
        clCodigo: String,
        prCedula: String,
        prNombre: String,
        prDireccion: String,
        prFono: String,
        trCodigo: String,
        tpCodigo: String,
        prEstadoPago: String,
        prObservacion: String,
        prValorBruto: String,
        prValorDesc: String,
        prPorcDesc: String,
        prValorSeguro: String,
        prPorcSeguro: String,
        prValorFlete: String,
        prValorIva: String,
        prValorTotal: String,
        prEmail: String,
        opcionenu: String,
        opcion: String,
        detalles: List<datosDet>
    ): String {
        val fechaFormateada = formatearFechaXml(fecha)

        val xml = StringBuilder()
        xml.append("'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><c ")
        xml.append("c0=\"").append(esc(empresa)).append("\" ")
        xml.append("c1=\"").append(esc(bodega)).append("\" ")
        xml.append("c2=\"").append(esc(secuencia)).append("\" ")
        xml.append("c3=\"").append(esc(fechaFormateada)).append("\" ")
        xml.append("c4=\"").append(esc(epCodigo)).append("\" ")
        xml.append("c5=\"").append(esc(clCodigo)).append("\" ")
        xml.append("c6=\"").append(esc(prCedula)).append("\" ")
        xml.append("c7=\"").append(esc(prNombre)).append("\" ")
        xml.append("c8=\"").append(esc(prDireccion)).append("\" ")
        xml.append("c9=\"").append(esc(prFono)).append("\" ")
        xml.append("c10=\"").append(esc(trCodigo)).append("\" ")
        xml.append("c11=\"").append(esc(tpCodigo)).append("\" ")
        xml.append("c12=\"").append(esc(prEstadoPago)).append("\" ")
        xml.append("c13=\"").append(esc(prObservacion)).append("\" ")
        xml.append("c14=\"").append(esc(prValorBruto)).append("\" ")
        xml.append("c15=\"").append(esc(prValorDesc)).append("\" ")
        xml.append("c16=\"").append(esc(prPorcDesc)).append("\" ")
        xml.append("c17=\"").append(esc(prValorSeguro)).append("\" ")
        xml.append("c18=\"").append(esc(prPorcSeguro)).append("\" ")
        xml.append("c19=\"").append(esc(prValorFlete)).append("\" ")
        xml.append("c20=\"").append(esc(prValorIva)).append("\" ")
        xml.append("c21=\"").append(esc(prValorTotal)).append("\" ")
        xml.append("c22=\"").append(esc(usuario)).append("\" ")

        if (opcionenu == "I") {
            xml.append("c23=\"0\" ")
        } else {
            xml.append("c23=\"1\" ")
        }

        xml.append("c24=\"2\" ")
        xml.append("c25=\"").append(esc(prEmail)).append("\" ")
        xml.append(">")

        detalles.forEachIndexed { index, d ->
            val cantidadInt = d.Cantidad.toIntSafe()
            val precioDouble = d.Precio.toDoubleSafe()
            val precio3 = fmt3(precioDouble)
            val valorBruto = if (d.Subtotal.isNotBlank()) {
                d.Subtotal
            } else {
                fmt3(cantidadInt * precioDouble)
            }



            xml.append("<detalle ")
            xml.append("d0=\"").append(esc(d.proceso.toString())).append("\" ")
            xml.append("d1=\"").append(esc(d.secuencia.toString())).append("\" ")
            xml.append("d2=\"").append(esc(d.Codigo)).append("\" ")
            xml.append("d3=\"0\" ")
            xml.append("d4=\"").append(esc(d.Cantidad)).append("\" ")
            xml.append("d5=\"").append(esc(precio3)).append("\" ")
            xml.append("d6=\"").append(esc(valorBruto)).append("\" ")
            xml.append("d7=\"").append(esc(d.unidadCE)).append("\" ")
            xml.append("d8=\"").append(esc(d.DescItem)).append("\" ")
            xml.append("d9=\"").append(esc(d.combo)).append("\" ")
            xml.append("d10=\"").append(esc(d.regalo)).append("\" ")
            xml.append("></detalle>")
        }

        xml.append("</c>',")
            .append(opcion).append(",")
            .append(empresa).append(",")
            .append(bodega).append(",")
            .append(secuencia).append(",'")
            .append(fecha).append("','")
            .append(usuario).append("','")
            .append(opcionenu).append("'")

        return xml.toString()
    }

    fun fnProformaPedidoXML(
        empresa: String,
        bodega: String,
        secuencia: String,
        fecha: String,
        usuario: String,
        epCodigo: String,
        clCodigo: String,
        trCodigo: String,
        tpCodigo: String,
        prValorBruto: String,
        prValorDesc: String,
        prPorcDesc: String,
        prValorSeguro: String,
        prValorFlete: String,
        prValorIva: String,
        prValorTotal: String,
        opcionenu: String,
        detalles: List<datosDet>
    ): String {
        val xml = StringBuilder()

        xml.append("'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><c ")
        xml.append("c0=\"").append(esc(empresa)).append("\" ")
        xml.append("c1=\"").append(esc(bodega)).append("\" ")
        xml.append("c2=\"0\" ")
        xml.append("c3=\"").append(esc(epCodigo)).append("\" ")
        xml.append("c4=\"").append(esc(usuario)).append("\" ")
        xml.append("c5=\"").append(esc(trCodigo)).append("\" ")
        xml.append("c6=\"PROFORMA # ").append(esc(secuencia)).append("\" ")
        xml.append("c7=\"").append(esc(clCodigo)).append("\" ")
        xml.append("c8=\"").append(esc(tpCodigo)).append("\" ")
        xml.append("c9=\"").append(esc(prValorSeguro)).append("\" ")
        xml.append("c10=\"").append(esc(prValorFlete)).append("\" ")
        xml.append("c11=\"").append(esc(prValorDesc)).append("\" ")
        xml.append("c12=\"").append(esc(prPorcDesc)).append("\" ")
        xml.append("c13=\"").append(esc(prValorIva)).append("\" ")
        xml.append("c14=\"").append(esc(prValorTotal)).append("\" ")
        xml.append("c15=\"").append(esc(prValorBruto)).append("\" ")
        xml.append("c16=\"0\" ")
        xml.append(">")

        detalles.forEachIndexed { index, d ->
            val secDet = index + 1

            xml.append("<detalle ")
            xml.append("d0=\"").append(esc(d.Codigo)).append("\" ")
            xml.append("d1=\"").append(esc(d.Precio)).append("\" ")
            xml.append("d2=\"").append(esc(d.Cantidad)).append("\" ")
            xml.append("d3=\"").append(esc(d.Referencia)).append("\" ")
            xml.append("d4=\"").append(esc(secDet.toString())).append("\" ")
            xml.append("d5=\"").append(esc(d.DescItem)).append("\" ")
            xml.append("d6=\"").append(esc(d.unidadCE)).append("\" ")
            xml.append("></detalle>")
        }

        xml.append("</c>',")
            .append(epCodigo)
            .append(",1,'")
            .append(opcionenu)
            .append("',")
            .append(secuencia)

        return xml.toString()
    }
}
