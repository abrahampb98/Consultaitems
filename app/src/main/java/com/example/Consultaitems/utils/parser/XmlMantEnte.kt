package com.example.Consultaitems.utils.parser

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.Consultaitems.data.database.SqLiteOpenHelper

class XmlMantEnte(private val context: Context) {

    fun obtenerXmlInforme(en_codigo: Int, dq_codigo: Int): String {
        var xml = ""
        val db = abrirBaseDatos()

        try {
            try {
                val cursorEnte = db.rawQuery(
                    """
                    SELECT en_codigo, en_ci, en_rucA, en_tipopersona, en_nombre1, en_nombre2,
                           en_apellido1, en_apellido2, en_razoncomercial, en_razonsocial,
                           en_genero, ci_codigo, en_fechanac, en_agenteretencion, en_pagaiva,
                           en_contribuyente, us_usuarioing, (case en_tipoId
                            when 36 then 'C'
                            when 37 then 'R'
                            when 38 then 'P'
                           end) as en_tipoId
                    FROM cc_ws_ente
                    WHERE en_codigo = ?
                    """.trimIndent(),
                    arrayOf(en_codigo.toString())
                )

                cursorEnte.use { ente ->
                    if (ente.moveToFirst()) {
                        val usuario = ente.getString(ente.getColumnIndexOrThrow("us_usuarioing")) ?: ""

                        var xmlCliente = "" + en_codigo + ",'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" +
                                "<Cliente>" +
                                "<DatosEnte opcion=\"I\" ente=\"${ente.getInt(ente.getColumnIndexOrThrow("en_codigo"))}\"" +
                                " cedula=\"${ente.getString(ente.getColumnIndexOrThrow("en_ci")) ?: ""}\"" +
                                " ruc=\"${ente.getString(ente.getColumnIndexOrThrow("en_rucA")) ?: ""}\"" +
                                " tipopersona=\"${ente.getString(ente.getColumnIndexOrThrow("en_tipopersona")) ?: ""}\"" +
                                " nombre1=\"${ente.getString(ente.getColumnIndexOrThrow("en_nombre1")) ?: ""}\"" +
                                " nombre2=\"${ente.getString(ente.getColumnIndexOrThrow("en_nombre2")) ?: ""}\"" +
                                " apellido1=\"${ente.getString(ente.getColumnIndexOrThrow("en_apellido1")) ?: ""}\"" +
                                " apellido2=\"${ente.getString(ente.getColumnIndexOrThrow("en_apellido2")) ?: ""}\"" +
                                " razoncomercial=\"${ente.getString(ente.getColumnIndexOrThrow("en_razoncomercial")) ?: ""}\"" +
                                " razonsocial=\"${ente.getString(ente.getColumnIndexOrThrow("en_razonsocial")) ?: ""}\"" +
                                " genero=\"${ente.getString(ente.getColumnIndexOrThrow("en_genero")) ?: ""}\"" +
                                " ciudad=\"${ente.getInt(ente.getColumnIndexOrThrow("ci_codigo"))}\"" +
                                " fechanacimiento=\"${ente.getString(ente.getColumnIndexOrThrow("en_fechanac")) ?: ""}\"" +
                                " agenteretencion=\"${ente.getString(ente.getColumnIndexOrThrow("en_agenteretencion")) ?: "N"}\"" +
                                " pagaiva=\"${ente.getString(ente.getColumnIndexOrThrow("en_pagaiva")) ?: "S"}\"" +
                                " contribuyente=\"${ente.getString(ente.getColumnIndexOrThrow("en_contribuyente")) ?: "N"}\"" +
                                " tipoId=\"${ente.getString(ente.getColumnIndexOrThrow("en_tipoId")) ?: ""}\"" +
                                " enteA=\"${ente.getString(ente.getColumnIndexOrThrow("en_codigo")) ?: ""}\"" +
                                "></DatosEnte>"

                        val cursorC = db.rawQuery(
                            """
                            SELECT co_secuencia, tc_codigo, tc_descripcion
                            FROM cc_ws_conctacto
                            WHERE cl_codigo = ?
                            """.trimIndent(),
                            arrayOf(en_codigo.toString())
                        )

                        cursorC.use { contactos ->
                            while (contactos.moveToNext()) {
                                xmlCliente += "<Contacto secuencia=\"${contactos.getInt(contactos.getColumnIndexOrThrow("co_secuencia"))}\"" +
                                        " tipocontacto=\"${contactos.getInt(contactos.getColumnIndexOrThrow("tc_codigo"))}\"" +
                                        " informacion=\"${contactos.getString(contactos.getColumnIndexOrThrow("tc_descripcion")) ?: ""}\"" +
                                        " opcion=\"I\"></Contacto>"
                            }
                        }

                        xml = "$xmlCliente</Cliente>','$usuario','N',$dq_codigo,0,0"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } finally {
            db.close()
        }

        return xml
    }

    fun fnXmlCupoCliente(clienteId: Int, usuario: String, cl_codigo: Int): String {
        val dbHelper = SqLiteOpenHelper(context)
        val db = dbHelper.readableDatabase
        val xmlOcupaciones = StringBuilder()
        var secuencia = 1

        try {
            val cursor = db.rawQuery(
                """
                SELECT cc_codigo
                FROM cc_ws_clienteCategoriaDetalle
                WHERE cl_codigo = ?
                """.trimIndent(),
                arrayOf(cl_codigo.toString())
            )

            cursor.use {
                while (it.moveToNext()) {
                    val codOcupacion = it.getInt(it.getColumnIndexOrThrow("cc_codigo"))
                    xmlOcupaciones.append(
                        """
                        <Ocupacion
                            codOcupacion="$codOcupacion"
                            secuencia="$secuencia"
                            cliente="$clienteId"
                            nuevaOcupacion="2"/>
                        """.trimIndent()
                    )
                    secuencia++
                }
            }
        } finally {
            db.close()
        }

        return """
        $clienteId,'<?xml version="1.0" encoding="iso-8859-1"?>
        <Cliente>
            <CupoCliente
                nuevo="-1" empresa="2" cliente="$clienteId"
                cupo="0.00" cupoasignadoO="0"
                cupodisponiblenuevo="0" nivel="7"
                cupodisponibleO="0" numvendedores="0"
                vendedor="-1" observacion=""
                secuenciahistorico="1"></CupoCliente>

            <CupoCliente
                nuevo="-1" empresa="7" cliente="$clienteId"
                cupo="0" cupoasignadoO="0"
                cupodisponiblenuevo="0" nivel="7"
                cupodisponibleO="0" numvendedores="0"
                vendedor="-1" observacion=""
                secuenciahistorico="1"></CupoCliente>

            <CupoCliente
                nuevo="-1" empresa="28" cliente="$clienteId"
                cupo="0.00" cupoasignadoO="0"
                cupodisponiblenuevo="0" nivel="7"
                cupodisponibleO="0" numvendedores="0"
                vendedor="-1" observacion=""
                secuenciahistorico="1"></CupoCliente>

            <CupoCliente
                nuevo="-1" empresa="31" cliente="$clienteId"
                cupo="0.00" cupoasignadoO="0"
                cupodisponiblenuevo="0" nivel="7"
                cupodisponibleO="0" numvendedores="0"
                vendedor="-1" observacion=""
                secuenciahistorico="1"></CupoCliente>

            <Datos
                opcion="I" cliente="$clienteId" politicapago="1"
                numempleados="0" personaencargada=""
                dimensionlocal="" numtiendas="0"
                aniosmercado="0" vtaspromedio="0.00"
                gastospromedio="0.00" porcvtascontado="0.00"
                porcvtascredito="0.00" cupodeseado="0.00"
                orden="0" activocorriente="0.00"
                tgactivofijo="0.00" tgactivo="0.00"
                pasivocorriente="0.00" tgpasivo="0.00"
                patrimonio="0.00" tgpatrimonio="0.00"
                deudabancaria="0.00" ventaneta="0.00"
                utilidadbruta="0.00" utilidadoperativa="0.00"
                utilidadpart="0.00" utilidadneta="0.00"
                compras="0.00" costoventa="0.00"
                totalcostos="0.00" totalgastos="0.00"
                ventapatrimonio="0.00" ventapatrimoniopasivo="0.00"
                inventario="0.00" CuotaEstimada="0.00"
                Score="0.00" Morosidad="0.00"
                CapacidadPago="0.00">
            </Datos>
        </Cliente>',
        '<?xml version="1.0" encoding="iso-8859-1"?>
        <Caracteristicas>
            $xmlOcupaciones
        </Caracteristicas>',
        '$usuario'
        """.trimIndent()
    }

    fun fnMantenimientoLOPDPCampanias(
        clienteId: Int,
        usuario: String,
        LOPDP: Int,
        Campania: Int
    ): String {
        return "7,$clienteId,$LOPDP,$Campania,'$usuario',0"
    }

    private fun abrirBaseDatos(): SQLiteDatabase {
        val dbHelper = SqLiteOpenHelper(context)
        return dbHelper.readableDatabase
    }
}
