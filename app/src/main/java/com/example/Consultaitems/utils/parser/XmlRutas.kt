package com.example.Consultaitems.utils.parser

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Locale

class xmlRutas(private val context: Context) {

    fun fnObtenerRutas(
        fechaIni: String,
        fechaFin: String
    ): String {

        var xml = ""
        val db = abrirBaseDatos()

        val fechaIniSql = normalizarFechaSql(fechaIni)
        val fechaFinSql = normalizarFechaSql(fechaFin)

        Log.d("RUTAS_DEBUG", "XML fechaIniSql: $fechaIniSql")
        Log.d("RUTAS_DEBUG", "XML fechaFinSql: $fechaFinSql")

        try {
            val cursorC = db.rawQuery(
                """
                SELECT DISTINCT
                    substr(rv_fechainicial, 1, 10) AS rv_fechainicial,
                    substr(rv_fechafinal, 1, 10) AS rv_fechafinal,
                    vn_codigo
                FROM fa_ws_rutaVendedor
                WHERE rv_estado <> 'C'
                  AND substr(rv_fechainicial, 1, 10) <= ?
                  AND substr(rv_fechafinal, 1, 10) >= ?
                ORDER BY substr(rv_fechainicial, 1, 10) DESC
                LIMIT 1
                """.trimIndent(),
                arrayOf(
                    fechaFinSql,
                    fechaIniSql
                )
            )

            if (cursorC.moveToFirst()) {
                xml = "'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
                xml += "<c "
                xml += "c0=\"${formatDate(getString(cursorC, "rv_fechainicial"))}\" "
                xml += "c1=\"${formatDate(getString(cursorC, "rv_fechafinal"))}\" "
                xml += "c2=\"${escapeXml(getString(cursorC, "vn_codigo"))}\" "
                xml += ">"

                val cursorD = db.rawQuery(
                    """
                    SELECT 
                        r.*,
                        (
                            SELECT vn_login
                            FROM ve_ws_usuario
                            LIMIT 1
                        ) AS vn_login
                    FROM fa_ws_rutaVendedor r
                    WHERE r.rv_estado IN ('A', 'N')
                      AND substr(r.rv_fechainicial, 1, 10) <= ?
                      AND substr(r.rv_fechafinal, 1, 10) >= ?
                      AND (
                            IFNULL(r.rv_visita, 0) <> 0
                         OR IFNULL(r.rv_venta, 0) <> 0
                         OR IFNULL(r.rv_cobro, 0) <> 0
                         OR IFNULL(r.rv_telefono, 0) <> 0
                         OR TRIM(IFNULL(r.rv_observacion, '')) <> ''
                      )
                    ORDER BY
                        CASE TRIM(r.rv_dia)
                            WHEN 'Lunes' THEN 1
                            WHEN 'Martes' THEN 2
                            WHEN 'Miércoles' THEN 3
                            WHEN 'Jueves' THEN 4
                            WHEN 'Viernes' THEN 5
                            ELSE 99
                        END,
                        r.rv_linea
                    """.trimIndent(),
                    arrayOf(
                        fechaFinSql,
                        fechaIniSql
                    )
                )

                Log.d("RUTAS_DEBUG", "XML detalles encontrados: ${cursorD.count}")

                while (cursorD.moveToNext()) {
                    val rvLinea = getString(cursorD, "rv_linea", "0").toIntOrNull() ?: 0
                    val d15Value = if (rvLinea == 0) "0" else "1"

                    xml += "<detalle "
                    xml += "d0=\"${escapeXml(getString(cursorD, "cl_codigo"))}\" "
                    xml += "d1=\"${escapeXml(getString(cursorD, "rv_linea", "0"))}\" "
                    xml += "d2=\"${escapeXml(getString(cursorD, "rv_monto", "0"))}\" "
                    xml += "d3=\"${escapeXml(getString(cursorD, "rv_distancia", "0 km"))}\" "
                    xml += "d4=\"${escapeXml(getString(cursorD, "rv_dia"))}\" "
                    xml += "d5=\"${escapeXml(getString(cursorD, "rv_tipo", "0"))}\" "
                    xml += "d6=\"${escapeXml(getString(cursorD, "rv_zona", "0"))}\" "
                    xml += "d7=\"${escapeXml(getString(cursorD, "vn_login"))}\" "
                    xml += "d8=\"${escapeXml(getString(cursorD, "rv_visita", "0"))}\" "
                    xml += "d9=\"${escapeXml(getString(cursorD, "rv_cobro", "0"))}\" "
                    xml += "d10=\"${escapeXml(getString(cursorD, "rv_venta", "0"))}\" "
                    xml += "d11=\"${escapeXml(getString(cursorD, "rv_observacion"))}\" "
                    xml += "d12=\"${escapeXml(getString(cursorD, "rv_fechavisita"))}\" "
                    xml += "d13=\"${escapeXml(getString(cursorD, "rv_fechacobro"))}\" "
                    xml += "d14=\"${escapeXml(getString(cursorD, "rv_fechaventa"))}\" "
                    xml += "d15=\"$d15Value\" "
                    xml += "d16=\"${escapeXml(getString(cursorD, "rv_telefono", "0"))}\" "
                    xml += "d17=\"${escapeXml(getString(cursorD, "rv_fechatelefono"))}\" "
                    xml += "></detalle>"
                }

                cursorD.close()

                xml += "</c>',2"
            } else {
                Log.d("RUTAS_DEBUG", "XML no encontró cabecera para semana $fechaIniSql - $fechaFinSql")
            }

            cursorC.close()

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RUTAS_DEBUG", "Error generando XML rutas: ${e.message}")
        } finally {
            db.close()
        }

        Log.d("RUTAS_DEBUG", "XML generado: $xml")

        return xml
    }

    private fun abrirBaseDatos(): SQLiteDatabase {
        val dbHelper = SqLiteOpenHelper(context)
        return dbHelper.readableDatabase
    }

    private fun normalizarFechaSql(fecha: String): String {
        return try {
            val valor = fecha.trim()

            when {
                valor.length >= 10 && valor.substring(4, 5) == "-" -> {
                    valor.substring(0, 10)
                }

                valor.length >= 10 && valor.substring(2, 3) == "/" -> {
                    val entrada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val salida = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    entrada.isLenient = false

                    val date = entrada.parse(valor.substring(0, 10))
                    if (date != null) salida.format(date) else ""
                }

                else -> valor
            }

        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun formatDate(inputDate: String?): String {
        if (inputDate.isNullOrBlank()) return ""

        return try {
            val fechaSql = inputDate.trim().take(10)

            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            inputFormat.isLenient = false

            val date = inputFormat.parse(fechaSql)
            date?.let { outputFormat.format(it) } ?: ""

        } catch (e: Exception) {
            e.printStackTrace()
            inputDate ?: ""
        }
    }

    private fun getString(
        cursor: Cursor,
        columnName: String,
        defaultValue: String = ""
    ): String {
        val index = cursor.getColumnIndex(columnName)

        return if (index >= 0 && !cursor.isNull(index)) {
            cursor.getString(index) ?: defaultValue
        } else {
            defaultValue
        }
    }

    private fun escapeXml(value: String?): String {
        if (value.isNullOrEmpty()) return ""

        return value
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }
}