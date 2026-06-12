package com.example.Consultaitems.utils.parser

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.Consultaitems.data.database.SqLiteOpenHelper

class XmlClienteAct(
    private val context: Context
) {

    fun obtenerXmlInforme(): String {
        var xml = ""
        val db = abrirBaseDatos()

        try {
            val cursorUsuario = db.rawQuery(
                "SELECT vn_login FROM ve_ws_usuario",
                null
            )

            cursorUsuario.use { cursorC ->
                if (cursorC.moveToFirst()) {
                    val login = cursorC.getString(
                        cursorC.getColumnIndexOrThrow("vn_login")
                    )

                    val builder = StringBuilder()
                    builder.append("'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><c ")
                    builder.append("c0=\"")
                    builder.append(escapeXml(login))
                    builder.append("\" >")

                    val queryDetalle = """
                        SELECT *
                        FROM (
                            SELECT 
                                cl_codigo,
                                33 AS tc_codigo,
                                cl_fono AS valor,
                                cl_estado
                            FROM ve_ws_clienteAsignadoVendedor
                            WHERE cl_seguimiento = 1
                              AND cl_estado = 'A'

                            UNION ALL

                            SELECT 
                                cl_codigo,
                                464 AS tc_codigo,
                                cl_email AS valor,
                                cl_estado
                            FROM ve_ws_clienteAsignadoVendedor
                            WHERE cl_seguimiento = 1
                              AND cl_estado = 'A'
                        )
                        ORDER BY cl_codigo
                    """.trimIndent()

                    val cursorDetalle = db.rawQuery(queryDetalle, null)

                    cursorDetalle.use { cursorD ->
                        while (cursorD.moveToNext()) {
                            builder.append("<detalle ")
                            builder.append("d0=\"")
                            builder.append(
                                escapeXml(
                                    cursorD.getString(
                                        cursorD.getColumnIndexOrThrow("cl_codigo")
                                    )
                                )
                            )
                            builder.append("\" ")

                            builder.append("d1=\"")
                            builder.append(
                                escapeXml(
                                    cursorD.getString(
                                        cursorD.getColumnIndexOrThrow("tc_codigo")
                                    )
                                )
                            )
                            builder.append("\" ")

                            builder.append("d2=\"")
                            builder.append(
                                escapeXml(
                                    cursorD.getString(
                                        cursorD.getColumnIndexOrThrow("valor")
                                    )
                                )
                            )
                            builder.append("\" ")

                            builder.append("d3=\"")
                            builder.append(
                                escapeXml(
                                    cursorD.getString(
                                        cursorD.getColumnIndexOrThrow("cl_estado")
                                    )
                                )
                            )
                            builder.append("\" ")

                            builder.append("></detalle>")
                        }
                    }

                    builder.append("</c>',1")
                    xml = builder.toString()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return xml
    }

    fun obtenerXmlInformeIndividual(cl_codigo: String): String {
        var xml = ""
        val db = abrirBaseDatos()

        try {
            val cursorUsuario = db.rawQuery(
                "SELECT vn_login FROM ve_ws_usuario",
                null
            )

            cursorUsuario.use { cursorC ->
                if (cursorC.moveToFirst()) {
                    val login = cursorC.getString(
                        cursorC.getColumnIndexOrThrow("vn_login")
                    )

                    val builder = StringBuilder()
                    builder.append("'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><c ")
                    builder.append("c0=\"")
                    builder.append(escapeXml(login))
                    builder.append("\">")

                    val queryDetalle = """
                        SELECT *
                        FROM (
                            SELECT 
                                cl_codigo,
                                33 AS tc_codigo,
                                cl_fono AS valor,
                                cl_estado
                            FROM ve_ws_clienteAsignadoVendedor
                            WHERE cl_seguimiento = 1
                              AND cl_estado = 'A'
                              AND cl_codigo = ?

                            UNION ALL

                            SELECT 
                                cl_codigo,
                                464 AS tc_codigo,
                                cl_email AS valor,
                                cl_estado
                            FROM ve_ws_clienteAsignadoVendedor
                            WHERE cl_seguimiento = 1
                              AND cl_estado = 'A'
                              AND cl_codigo = ?
                        )
                        ORDER BY cl_codigo
                    """.trimIndent()

                    val cursorDetalle = db.rawQuery(
                        queryDetalle,
                        arrayOf(cl_codigo, cl_codigo)
                    )

                    cursorDetalle.use { cursorD ->
                        while (cursorD.moveToNext()) {
                            builder.append("<detalle ")
                            builder.append("d0=\"")
                            builder.append(
                                escapeXml(
                                    cursorD.getString(
                                        cursorD.getColumnIndexOrThrow("cl_codigo")
                                    )
                                )
                            )
                            builder.append("\" ")

                            builder.append("d1=\"")
                            builder.append(
                                escapeXml(
                                    cursorD.getString(
                                        cursorD.getColumnIndexOrThrow("tc_codigo")
                                    )
                                )
                            )
                            builder.append("\" ")

                            builder.append("d2=\"")
                            builder.append(
                                escapeXml(
                                    cursorD.getString(
                                        cursorD.getColumnIndexOrThrow("valor")
                                    )
                                )
                            )
                            builder.append("\" ")

                            builder.append("d3=\"")
                            builder.append(
                                escapeXml(
                                    cursorD.getString(
                                        cursorD.getColumnIndexOrThrow("cl_estado")
                                    )
                                )
                            )
                            builder.append("\" ")

                            builder.append("/>")
                        }
                    }

                    builder.append("</c>',1")
                    xml = builder.toString()
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

    private fun escapeXml(value: String?): String {
        return value
            ?.replace("&", "&amp;")
            ?.replace("\"", "&quot;")
            ?.replace("'", "&apos;")
            ?.replace("<", "&lt;")
            ?.replace(">", "&gt;")
            ?: ""
    }
}