package com.example.Consultaitems.utils.parser

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.StringReader
import java.util.Calendar

class XMLParser {

    companion object {
        @Throws(XmlPullParserException::class, IOException::class)
        fun parseAndInsertData(xmlData: String, database: SQLiteDatabase, callback: (String) -> Unit) {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))

            val insertedData = StringBuilder()

            if (!xmlTieneDatos(xmlData)) {
                Log.w("XMLParser", "El XML no contiene datos. No se eliminarán las tablas.")
                callback("Error en Sincronizacion")
                return
            }

            // Borrar los datos anteriores de todas las tablas
            database.execSQL("DELETE FROM ve_ws_vendedor")
            database.execSQL("DELETE FROM ve_ws_clienteAsignadoVendedor")
            database.execSQL("DELETE FROM ve_ws_usuario")
            database.execSQL("DELETE FROM cc_ws_clienteFacturaVendedor")
            database.execSQL("DELETE FROM ve_ws_item")
            database.execSQL("DELETE FROM fa_ws_tipoFactura")
            database.execSQL("DELETE FROM cc_ws_transacciones")
            database.execSQL("DELETE FROM ve_ws_prioridad")
            database.execSQL("DELETE FROM fa_ws_plazo")
            database.execSQL("DELETE FROM fa_ws_parametroIva")
            database.execSQL("DELETE FROM fa_ws_parametroseguro")
            database.execSQL("DELETE FROM ve_ws_transporte")
            database.execSQL("DELETE FROM fa_ws_tipoDescuentoPedido")
            database.execSQL("DELETE FROM fa_ws_transporteTarifa")
            database.execSQL("DELETE FROM iv_ws_itemComboCab")
            database.execSQL("DELETE FROM iv_ws_itemComboDet")
            database.execSQL("DELETE FROM iv_ws_marca")
            database.execSQL("DELETE FROM cc_ws_transaccionesA")
            database.execSQL("DELETE FROM cc_ws_bancoCliente")
            database.execSQL("DELETE FROM cc_ws_detalleCobro")
            database.execSQL("DELETE FROM ve_ws_appSincronizar")
            database.execSQL("DELETE FROM vn_ws_margen")
            database.execSQL("DELETE FROM cc_ws_parametrostransaccionesxbodega")
            database.execSQL("DELETE FROM fa_ws_tipoTarjeta")
            database.execSQL("DELETE FROM fa_ws_tipoTarjetaComision")

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG) {
                    val tableName = parser.name
                    if (tableName != "Table22" && tableName != "Table23") { // ⛔ Excluir Table22 de la carga normal
                        parseAndInsertTableData(parser, database, tableName, insertedData)
                    }
                }
            }

            // 🔹 Ejecutar la función después de llenar todas las tablas
            verificarYActualizarRutaVendedor(xmlData, database)

            fnActualizarVentas(xmlData, database)

            // Llama al callback con los datos insertados
            callback(insertedData.toString())

        }

        @Throws(XmlPullParserException::class, IOException::class)
        private fun parseAndInsertTableData(parser: XmlPullParser, database: SQLiteDatabase, tableName: String, insertedData: StringBuilder) {
            val values = ContentValues()
            val actualTableName = when (tableName) {
                "Table" -> "ve_ws_vendedor"
                "Table1" -> "ve_ws_clienteAsignadoVendedor"
                "Table2" -> "ve_ws_usuario"
                "Table3" ->"cc_ws_clienteFacturaVendedor"
                "Table4" ->"ve_ws_item"
                "Table5" -> "fa_ws_tipoFactura"
                "Table6" -> "cc_ws_transacciones"
                "Table7" -> "ve_ws_prioridad"
                "Table8" -> "fa_ws_plazo"
                "Table9" -> "fa_ws_parametroIva"
                "Table10" -> "fa_ws_parametroseguro"
                "Table11" -> "ve_ws_transporte"
                "Table12" -> "fa_ws_transporteTarifa"
                "Table13" -> "fa_ws_tipoDescuentoPedido"
                "Table14" -> "iv_ws_itemComboCab"
                "Table15" -> "iv_ws_itemComboDet"
                "Table16" -> "iv_ws_marca"
                "Table17" -> "cc_ws_transaccionesA"
                "Table18" -> "cc_ws_bancoCliente"
                "Table19" -> "cc_ws_detalleCobro"
                "Table20" -> "ve_ws_appSincronizar"
                "Table21" -> "vn_ws_margen"
                "Table22" -> "fa_ws_rutaVendedor"
                "Table23" -> "fa_ws_ventas"
                "Table24" -> "cc_ws_parametrostransaccionesxbodega"
                "Table25" -> "fa_ws_tipoTarjeta"
                "Table26" -> "fa_ws_tipoTarjetaComision"
                else -> {
                    Log.e("XMLParser", "Unknown table name: $tableName")
                    return
                }
            }

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.END_TAG && parser.name == tableName) {
                    break
                }

                if (parser.eventType == XmlPullParser.START_TAG) {
                    val tagName = parser.name
                    parser.next()

                    val value = parser.text ?: continue

                    // Registro de depuración para verificar los valores obtenidos
                    Log.d("XMLParser", "Inserting value into $tagName: $value")

                    // Verificar si el valor es nulo antes de insertarlo en la base de datos
                    if (value.isNotBlank()) {
                        // Ajusta la lógica según la estructura de tu XML y los tipos de datos de tus columnas
                        when (tagName) {
                            "vn_codigo","cl_codigo","pi_porcentaje", "tr_codigo", "cb_codigo", "vn_anio","ve_mes", "ve_cantidad", "gv_tipo",
                            "bo_codigo","tt_codigo"-> values.put(tagName, value.toInt())
                            "vn_nombre", "cl_nombre", "vn_login", "vn_password" ,"en_identificacion"-> values.put(tagName, value)
                            "vn_codigo", "Codigo", "Descripcion", "ps_porcentaje", "tr_nombre"  -> values.put(tagName, value)
                            "pz_descripcion", "cv_fechaing", "vn_fechaing", "em_codigo", "fa_fechaing" -> values.put(tagName, value)
                            "fa_coddocumento","fa_sri","bo_descripcion","fa_valortotfact","bo_codigo"-> values.put(tagName, value)
                            "it_codigo", "it_referencia", "it_descripcion", "it_titulo", "it_familia"  -> values.put(tagName,value)
                            "it_marca", "it_almesa", "it_teler", "it_mmg", "it_mmq", "pv_precio5" -> values.put(tagName,value)
                            "pv_precio6", "it_fechaing", "um_unidadCM", "um_unidadCE", "um_sku"-> values.put(tagName,value)
                            "um_pesoCE", "pv_preciosubdistrib", "pv_desctosubdistrib", "pv_costoN"-> values.put(tagName,value)
                            "it_exhVmr", "it_dcp", "it_exhTele", "it_costoprom", "it_activaex", "tt_tipo"-> values.put(tagName,value)
                            "tc_comision", "tc_codigo", "mg_regalo"-> values.put(tagName,value.toDouble())
                            "tf_fechaing","tr_fechaing", "pr_fechaing", "pz_fechaing", "pi_fechaing",
                            "ps_fechaing", "tt_fechaing","td_fechaing", "pz_codigo", "tt_codigo","tt_descripcion","tt_peso","tt_tarifa1","tt_tarifa2",
                            "ci_codigo", "ci_descripcion", "tt_codigoA", "cb_descripcionA", "cb_monto","cl_lopdp","cl_lopdpusuarioing","cl_campania",
                            "cb_montocp","cb_margen","cb_fechaing", "cb_tipo", "cb_linea", "cb_cantidad","pr_codigo","pr_descripcion", "pr_saldo",
                            "cb_precio", "it_costopromedio", "pv_precio7","ma_fechaing", "ma_codigo", "ma_descripcion", "cl_orden", "it_regalo","cl_seguimiento",
                            "cl_fono", "cl_direccion", "tr_descripcion", "ba_codigo", "ba_descripcion","bc_numcuenta", "mg_codigo", "mg_margen",
                            "rv_fechainicial", "rv_fechafinal", "rv_linea", "rv_zona", "rv_monto", "rv_tipo", "rv_distancia", "rv_dia", "rv_telefono",
                            "cc_cupoasignado", "cc_cupodisponible", "rv_visita", "rv_cobro", "rv_venta","rv_estado", "cl_latitud","cl_longitud","ve_preciovta",
                            "bc_codigo", "cc_fechafactura", "cc_valorfactura", "dc_fechaing", "cc_valorsaldo", "si_contador", "si_app" -> values.put(tagName,value)
                            "fa_fechafactura" ->

                                                          {
                                val formattedDate = formatDate(value)
                                values.put(tagName, formattedDate)
                            }

                        }
                    } else {
                        // Registro de depuración para valores nulos
                        //Log.d("XMLParser", "Null value found for element $tagName")
                    }

                    // Agregar los datos insertados al StringBuilder
                    insertedData.append("$tagName: $value\n")
                }
            }

            // Inserta los datos en la base de datos SQLite
            if (values.size() > 0) {
                val result = database.insert(actualTableName, null, values)
                if (result == -1L) {
                    // La inserción falló
                    //Log.e("XMLParser", "Data insertion failed for table $actualTableName")
                } else {
                    // La inserción fue exitosa
                    //Log.d("XMLParser", "Data inserted successfully for table $actualTableName")
                }
            } else {
                // No se insertaron datos debido a que todos los valores eran nulos o vacíos
                //Log.d("XMLParser", "No data inserted for table $actualTableName")
            }
        }

        fun formatDate(dateString: String): String {
            // Suponiendo que 'dateString' está en formato dd-mm-yyyy
            val parts = dateString.split("-")
            val day = parts[0]
            val month = parts[1]
            val year = parts[2]
            // Formatear la fecha como dd-MM-yyyy para SQLite
            return "$day-$month-$year"
        }


        private fun verificarYActualizarRutaVendedor(xmlData: String, database: SQLiteDatabase) {
            var debeActualizar = false

            val cursor = database.rawQuery("SELECT si_contador FROM ve_ws_appSincronizar LIMIT 1", null)
            if (cursor.moveToFirst()) {
                val siContador = cursor.getInt(0) // Obtiene el valor de si_contador
                debeActualizar = siContador == 0
                Log.d("XMLParser", "Valor de si_contador: $siContador, debeActualizar: $debeActualizar")
            }
            cursor.close()

            if (debeActualizar) {
                Log.d("XMLParser", "Eliminando y llenando fa_ws_rutaVendedor porque si_contador = 0")

                // 🔹 Eliminar fa_ws_rutaVendedor
                database.execSQL("UPDATE  fa_ws_rutaVendedor set rv_estado='C'")

                // 🔹 Volver a llenar fa_ws_rutaVendedor con los datos procesados del XML
                val parser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(StringReader(xmlData)) // Volver a leer el XML desde el inicio

                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType == XmlPullParser.START_TAG && parser.name == "Table22") {
                        val insertedData = StringBuilder()
                        parseAndInsertTableData(parser, database, "Table22", insertedData)
                    }
                }
            } else {
                Log.d("XMLParser", "No se toca fa_ws_rutaVendedor porque si_contador ≠ 0")
            }
        }


        private fun fnActualizarVentas(xmlData: String, database: SQLiteDatabase) {

                // 🔹 Obtener el año actual
                val añoActual = Calendar.getInstance().get(Calendar.YEAR)

                // 🔹 Eliminar solo registros del año actual
                database.execSQL("DELETE FROM fa_ws_ventas WHERE vn_anio = $añoActual")

                // 🔹 Volver a llenar fa_ws_ventas con los datos procesados del XML
                val parser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(StringReader(xmlData)) // Volver a leer el XML desde el inicio

                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType == XmlPullParser.START_TAG && parser.name == "Table23") {
                        val insertedData = StringBuilder()
                        parseAndInsertTableData(parser, database, "Table23", insertedData)
                    }
                }

            Log.d("XMLParser", "Actualizando en fa_ws_ventas")

        }


        private fun xmlTieneDatos(xmlData: String): Boolean {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG) {
                    val name = parser.name
                    if (name.startsWith("Table")) {
                        return true
                    }
                }
            }

            return false
        }


    }
}
