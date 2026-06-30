package com.example.Consultaitems.utils.cls

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.database.getStringOrNull
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.ui.adapters.Adaptador
import com.example.Consultaitems.ui.adapters.AdaptadorRutaNueva
import com.example.Consultaitems.ui.adapters.AdaptadorRutas
import com.example.Consultaitems.ui.adapters.Categoria
import com.example.Consultaitems.ui.adapters.Cliente
import com.example.Consultaitems.ui.adapters.Documento
import com.example.Consultaitems.ui.adapters.Factura
import com.example.Consultaitems.ui.adapters.Facturas
import com.example.Consultaitems.ui.adapters.ItemDetalle
import com.example.Consultaitems.ui.adapters.ItemScan
import com.example.Consultaitems.ui.adapters.Marca
import com.example.Consultaitems.ui.adapters.PreciosyStock
import com.example.Consultaitems.ui.adapters.Recibo
import com.example.Consultaitems.ui.adapters.Transporte
import com.example.Consultaitems.ui.adapters.clienteD
import com.example.Consultaitems.ui.adapters.datos
import com.example.Consultaitems.ui.adapters.datosDet
import com.example.Consultaitems.ui.adapters.items
import com.example.Consultaitems.ui.adapters.status
import com.example.Consultaitems.ui.fragments.DatosEnte
import com.example.Consultaitems.ui.fragments.SemanaRuta
import com.example.Consultaitems.utils.pdf.RutaVendedorData
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ClsLLenarControles(private val context: Context) {

    fun fnLLenarSpinner(spinner: Spinner, tabla: String) {
        val db = DatabaseManager.openDatabase(context)
        db.rawQuery("SELECT Codigo, Descripcion FROM $tabla", null).use { cursor ->
            val items = mutableListOf<SpinnerItem>()
            while (cursor.moveToNext()) {
                items.add(SpinnerItem(cursor.getString(0), cursor.getString(1)))
            }
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        DatabaseManager.closeDatabase()
    }

    fun fnLLenarSpinnerTransaccion(spinner: Spinner, tabla: String) {
        val db = DatabaseManager.openDatabase(context)
        db.rawQuery("SELECT tr_codigo, tr_descripcion FROM $tabla", null).use { cursor ->
            val items = mutableListOf<SpinnerItem>()
            while (cursor.moveToNext()) {
                items.add(SpinnerItem(cursor.getString(0), cursor.getString(1)?:""))
            }
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        DatabaseManager.closeDatabase()
    }


    fun fnLLenarSpinnerBanco(spinner: Spinner, id: String) {
        val db = DatabaseManager.openDatabase(context)
        val query = """
    SELECT  0 as bc_codigo, '' as ba_descripcion, '' as bc_numcuenta
    UNION ALL
    SELECT bc_codigo, ba_descripcion, bc_numcuenta FROM cc_ws_bancocliente WHERE cl_codigo = ?
    """.trimIndent()

        db.rawQuery(query, arrayOf(id)).use { cursor ->
            val items = mutableListOf<SpinnerItem>()
            while (cursor.moveToNext()) {
                val codigo = cursor.getString(0) ?: ""
                val descripcion = cursor.getString(1) ?: ""
                val numCuenta = cursor.getString(2) ?: ""

                // Concatenar ba_descripcion y bc_numcuenta con un guion
                val descripcionCompleta = "$descripcion - $numCuenta"


                items.add(SpinnerItem(codigo, descripcionCompleta))
            }

            // Configura el adaptador del Spinner
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            spinner.adapter = adapter
        }
        DatabaseManager.closeDatabase()
    }



    fun fnObtenerCodigoBanco(bc_codigo: Int): Int {
        val db = DatabaseManager.openDatabase(context)
        val query = "SELECT ba_codigo FROM cc_ws_bancocliente WHERE bc_codigo = ?"
        return db.rawQuery(query, arrayOf(bc_codigo.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getInt(cursor.getColumnIndexOrThrow("ba_codigo"))
            } else {
                -1 // Retorna -1 o cualquier valor por defecto que elijas si no se encuentra la cuenta
            }
        }.also {
            DatabaseManager.closeDatabase()
        }
    }


    fun fnLLenarVendedor(): Vendedor? {
        val db = DatabaseManager.openDatabase(context)
        return db.rawQuery("SELECT vn_login, vn_codigo FROM ve_ws_usuario LIMIT 1", null)
            .use { cursor ->
                if (cursor.moveToFirst()) {
                    Vendedor(cursor.getString(0), cursor.getString(1))
                } else {
                    null
                }
            }.also {
                DatabaseManager.closeDatabase()
        }
    }

    fun fnCargarClientes(): List<Cliente> {
        val db = DatabaseManager.openDatabase(context)
        val clientes = mutableListOf<Cliente>()
        db.rawQuery("SELECT cl_codigo, cl_nombre FROM ve_ws_clienteAsignadoVendedor", null)
            .use { cursor ->
                while (cursor.moveToNext()) {
                    clientes.add(
                        Cliente(
                            cursor.getString(cursor.getColumnIndexOrThrow("cl_codigo")),
                            cursor.getString(cursor.getColumnIndexOrThrow("cl_nombre"))
                        )
                    )
                }
            }
        DatabaseManager.closeDatabase()
        return clientes
    }

    fun fnCargarDatosTransporte(): List<Transporte> {
        val db = DatabaseManager.openDatabase(context)
        val transporteList = mutableListOf<Transporte>()
        db.rawQuery("SELECT tr_codigo, tr_nombre FROM ve_ws_transporte", null).use { cursor ->
            while (cursor.moveToNext()) {
                transporteList.add(
                    Transporte(
                        cursor.getString(cursor.getColumnIndexOrThrow("tr_codigo")),
                        cursor.getString(cursor.getColumnIndexOrThrow("tr_nombre"))
                    )
                )
            }
        }
        DatabaseManager.closeDatabase()
        return transporteList
    }

    fun fnCargarDatosItem(): List<Transporte> {
        val db = DatabaseManager.openDatabase(context)
        val transporteList = mutableListOf<Transporte>()
        db.rawQuery("SELECT it_codigo, it_referencia FROM ve_ws_item", null).use { cursor ->
            while (cursor.moveToNext()) {
                transporteList.add(
                    Transporte(
                        cursor.getString(cursor.getColumnIndexOrThrow("it_codigo")),
                        cursor.getString(cursor.getColumnIndexOrThrow("it_referencia"))
                    )
                )
            }
        }
        DatabaseManager.closeDatabase()
        return transporteList
    }

    fun fnCargarDatosItemAlmacen(): List<Transporte> {
        val db = DatabaseManager.openDatabase(context)
        val transporteList = mutableListOf<Transporte>()

        db.rawQuery(
            "SELECT Distinct it_codigo, it_referencia FROM iv_ws_itemxbodega",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                transporteList.add(
                    Transporte(
                        cursor.getString(cursor.getColumnIndexOrThrow("it_codigo")),
                        cursor.getString(cursor.getColumnIndexOrThrow("it_referencia"))
                    )
                )
            }
        }

        DatabaseManager.closeDatabase()
        return transporteList
    }


    fun fnCargarDatosItemBodega(): List<Transporte> {
        val db = DatabaseManager.openDatabase(context)
        val transporteList = mutableListOf<Transporte>()

        try {
            db.rawQuery(
                "SELECT DISTINCT it_codigo, it_referencia FROM iv_ws_itemxbodega",
                null
            ).use { cursor ->

                val colCodigo = cursor.getColumnIndexOrThrow("it_codigo")
                val colReferencia = cursor.getColumnIndexOrThrow("it_referencia")

                while (cursor.moveToNext()) {
                    transporteList.add(
                        Transporte(
                            cursor.getString(colCodigo),
                            cursor.getString(colReferencia)
                        )
                    )
                }
            }

            return transporteList

        } finally {
            DatabaseManager.closeDatabase()
        }
    }

    fun fnCargarDatosMarca(): List<Marca> {
        val db = DatabaseManager.openDatabase(context)
        val marcaList = mutableListOf<Marca>()
        db.rawQuery("SELECT ma_codigo, ma_descripcion FROM iv_ws_marca", null).use { cursor ->
            while (cursor.moveToNext()) {
                marcaList.add(
                    Marca(
                        cursor.getString(cursor.getColumnIndexOrThrow("ma_codigo")),
                        cursor.getString(cursor.getColumnIndexOrThrow("ma_descripcion"))
                    )
                )
            }
        }
        DatabaseManager.closeDatabase()
        return marcaList
    }


    fun fnObtenerPolitica(clienteId: String): Politica? {
        val db = DatabaseManager.openDatabase(context)
        return db.rawQuery(
            "SELECT pz_codigo, pz_descripcion FROM ve_ws_clienteAsignadoVendedor WHERE cl_codigo = ?",
            arrayOf(clienteId)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                Politica(
                    cursor.getString(cursor.getColumnIndexOrThrow("pz_codigo")),
                    cursor.getString(cursor.getColumnIndexOrThrow("pz_descripcion"))
                )
            } else {
                null
            }
        }.also {
            DatabaseManager.closeDatabase()
        }
    }

    fun fnObtenerSeguro(): Double {
        val db = DatabaseManager.openDatabase(context)
        return db.rawQuery("SELECT ps_porcentaje FROM fa_ws_parametroseguro", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getDouble(cursor.getColumnIndexOrThrow("ps_porcentaje"))
        }.also {
            DatabaseManager.closeDatabase()
        }
    }

     fun fnObtenerVendedor():String{
         val db = DatabaseManager.openDatabase(context)

         return db.rawQuery("SELECT vn_codigo FROM ve_ws_vendedor", null).use { cursor ->
             cursor.moveToFirst()
             cursor.getString(cursor.getColumnIndexOrThrow("vn_codigo"))
         }.also {
             DatabaseManager.closeDatabase()
         }
     }


    fun fnActualizarPedido(
        peCodDocumento: Int,
        cabValues: ContentValues,
        detalleValuesList: List<ContentValues>
    ): Boolean {
        val db = DatabaseManager.openDatabase(context)
        var success = true

        try {
            db.beginTransaction()
            // Intentar actualizar la cabecera
            val affectedRows = db.update(
                "fa_ws_cabpedidoQueue",
                cabValues,
                "pe_coddocumento = ?",
                arrayOf(peCodDocumento.toString())
            )

            if (affectedRows == 0) {
                // Si no se actualizó ninguna fila, insertar una nueva cabecera
                cabValues.put("pe_coddocumento", peCodDocumento)
                if (db.insert("fa_ws_cabpedidoQueue", null, cabValues) == -1L) {
                    throw Exception("Error al insertar en la tabla de cabecera")
                }
            }

            // Borrar detalles existentes
            db.delete(
                "fa_ws_detpedidoQueue",
                "pe_coddocumento = ?",
                arrayOf(peCodDocumento.toString())
            )

            // Insertar detalles actualizados
            for (detalleValues in detalleValuesList) {
                detalleValues.put("pe_coddocumento", peCodDocumento)
                if (db.insert("fa_ws_detpedidoQueue", null, detalleValues) == -1L) {
                    throw Exception("Error al insertar en la tabla de detalles")
                }
            }

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            success = false

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Error")
            builder.setMessage("Ocurrió un error al guardar: ${e.message}")
            builder.setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss() // Cerrar el diálogo
            }
            val dialog = builder.create()
            dialog.show()
            Log.e("ClsLLenarControles", "Error al actualizar pedido", e)
        } finally {
            db.endTransaction()
            DatabaseManager.closeDatabase()
        }

        return success
    }


    fun fnActualizarCobranzaCabeceraYDetalle(
        rcCodRecibo: String,
        cabValues: ContentValues,
        detallesCobranza: List<ContentValues>
    ): Boolean {
        val db = DatabaseManager.openDatabase(context)
        var success = true

        try {
            db.beginTransaction()

            // Intentar actualizar la cabecera
            val affectedRows = db.update(
                "co_ws_reciboCobranzaConCab",
                cabValues,
                "rc_codrecibo = ?",
                arrayOf(rcCodRecibo)
            )

            if (affectedRows == 0) {
                // Si no se actualizó ninguna fila, insertar una nueva cabecera
                cabValues.put("rc_codrecibo", rcCodRecibo)
                if (db.insert("co_ws_reciboCobranzaConCab", null, cabValues) == -1L) {
                    throw Exception("Error al insertar en la tabla de cabecera")
                }
            }

            // Borrar detalles existentes asociados a la cabecera
            db.delete(
                "co_ws_reciboCobranzaConDet",
                "rc_codrecibo = ?",
                arrayOf(rcCodRecibo)
            )

            // Insertar detalles actualizados
            for (detalleValues in detallesCobranza) {
                detalleValues.put("rc_codrecibo", rcCodRecibo)
                if (db.insert("co_ws_reciboCobranzaConDet", null, detalleValues) == -1L) {
                    throw Exception("Error al insertar en la tabla de detalles")
                }
            }

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            success = false
            Log.e("DBError", "Error al actualizar cobranza: ${e.message}", e)
        } finally {
            db.endTransaction()
            DatabaseManager.closeDatabase()
        }

        return success
    }




    fun fnObtenerMaxioDocumento(): Int {
        val db = DatabaseManager.openDatabase(context)
        var maxCodigo = 1
        db.rawQuery("SELECT MAX(pe_coddocumento) FROM fa_ws_cabpedidoQueue", null).use { cursor ->
            if (cursor.moveToFirst()) {
                val maxCodigoFromDb = cursor.getInt(0)
                if (maxCodigoFromDb != 0) {
                    maxCodigo = maxCodigoFromDb + 1
                }
            }
        }
        DatabaseManager.closeDatabase()
        return maxCodigo
    }

    fun fnObtenerMaxioRecibo(): Int {
        val db = DatabaseManager.openDatabase(context)
        var maxCodigo = 1
        db.rawQuery("SELECT MAX(CAST(rc_codrecibo AS INTEGER)) FROM co_ws_reciboCobranzaConCab", null).use { cursor ->
            if (cursor.moveToFirst()) {
                val maxCodigoFromDb = cursor.getInt(0)
                if (maxCodigoFromDb != 0) {
                    maxCodigo = maxCodigoFromDb + 1
                }
            }
        }
        DatabaseManager.closeDatabase()
        return maxCodigo
    }


    fun fnConsultarPedidos(
        codDocumento: Int,
        actualizarCabecera: (Cursor) -> Unit,
        actualizarDetalles: (MutableList<datosDet>) -> Unit,
        onDocumentoNoEncontrado: () -> Unit  // Añade un callback para manejar la no existencia del documento
    ) {
        val db = DatabaseManager.openDatabase(context)
        var documentoEncontrado = false  // Variable para controlar si se encontró el documento

        try {
            db.rawQuery(
                "SELECT * FROM fa_ws_cabpedidoQueue WHERE pe_coddocumento = ? and pe_estado <>'E' ",
                arrayOf(codDocumento.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    actualizarCabecera(cursor)
                    documentoEncontrado = true  // Indica que se encontró el documento
                }
            }

            if (!documentoEncontrado) {
                // Invoca el callback de documento no encontrado si no se encontró la cabecera
                onDocumentoNoEncontrado()
                return  // Sale temprano para evitar buscar detalles si la cabecera no existe
            }

            val detallesList = mutableListOf<datosDet>()
            db.rawQuery(
                "SELECT * FROM fa_ws_detpedidoQueue WHERE pe_coddocumento = ?",
                arrayOf(codDocumento.toString())
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    detallesList.add(extractDetail(cursor))
                }
                actualizarDetalles(detallesList)
            }
        } finally {
            DatabaseManager.closeDatabase()
        }
    }


    private fun extractDetail(cursor: Cursor): datosDet {
        val codigo = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo"))
        val cantidad = cursor.getString(cursor.getColumnIndexOrThrow("dp_cantidad")).toInt()
        val precio = String.format("%.3f", cursor.getString(cursor.getColumnIndexOrThrow("dp_precio")).toDoubleOrNull() ?: 0.0)
        val referencia = cursor.getString(cursor.getColumnIndexOrThrow("dp_descripcion"))
        val unidadCE = cursor.getString(cursor.getColumnIndexOrThrow("um_pesoCE"))
        val subtotal = BigDecimal(cantidad * precio.toDouble()).setScale(2, BigDecimal.ROUND_HALF_UP).toString()
        val DescItem = String.format("%.2f", cursor.getString(cursor.getColumnIndexOrThrow("dp_porcdescuento")).toDoubleOrNull() ?: 0.0)
        val ConDesc = (subtotal.toBigDecimal() - subtotal.toBigDecimal() * DescItem.toBigDecimal() / BigDecimal(100))
            .setScale(2, RoundingMode.HALF_UP)
        val costProm = cursor.getString(cursor.getColumnIndexOrThrow("dp_costoPromedio"))
        val combo = cursor.getString(cursor.getColumnIndexOrThrow("dp_combo"))?:"0"
        val regalo = cursor.getString(cursor.getColumnIndexOrThrow("it_regalo"))?:"0"
        val sugerencia = cursor.getInt(cursor.getColumnIndexOrThrow("dp_sugerencia"))?:0

        return datosDet(
            referencia,
            cantidad.toString(),
            precio,
            subtotal,
            codigo,
            "",
            unidadCE,
            DescItem,
            ConDesc.toString(),
            costProm.toString(),
            String.format("%.2f", (precio.toDouble() * (1 -DescItem.toDouble()/100)) / costProm.toDouble()),
            combo,
            regalo,
            sugerencia
        )
    }


    fun fnConsultarRecibos(
        codDocumento: Int,
        actualizarCabecera: (Cursor) -> Unit,
        actualizarDetalles: (MutableList<Recibo>) -> Unit,
        onDocumentoNoEncontrado: () -> Unit  // Añade un callback para manejar la no existencia del documento
    ) {
        val db = DatabaseManager.openDatabase(context)
        var documentoEncontrado = false  // Variable para controlar si se encontró el documento

        try {
            db.rawQuery(
                "SELECT rc_codrecibo,ep_codigoRes, ep_codigoBenef, rc_fecharec, rc_total, cl_nombre, rc_enlace, rc_estado " +
                        "FROM co_ws_reciboCobranzaConCab c " +
                        "LEFT JOIN ve_ws_clienteAsignadoVendedor n on c.ep_codigoBenef = n.cl_codigo " +
                        "WHERE rc_codrecibo = ? and rc_estado <>'E' ",
                arrayOf(codDocumento.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    actualizarCabecera(cursor)
                    documentoEncontrado = true  // Indica que se encontró el documento
                }
            }

            if (!documentoEncontrado) {
                // Invoca el callback de documento no encontrado si no se encontró la cabecera
                onDocumentoNoEncontrado()
                return  // Sale temprano para evitar buscar detalles si la cabecera no existe
            }

            val detallesList = mutableListOf<Recibo>()
            db.rawQuery(
                "SELECT t.tr_descripcion, b.ba_descripcion, d.rd_documento, d.rd_ncuenta, " +
                        "d.rd_fecha, d.rd_valor, d.rd_concepto, d.rd_observacion," +
                        "d.ba_codigo, d.tr_codigo, d.bc_codigo " +
                        "FROM co_ws_reciboCobranzaConDet d " +
                        "LEFT JOIN cc_ws_transaccionesA t on t.tr_codigo = d.tr_codigo " +
                        "LEFT JOIN cc_ws_bancoCliente b on b.ba_codigo = d.ba_codigo and  d.bc_codigo = b.bc_codigo " +
                        "WHERE d.rc_codrecibo = ? AND d.rd_estado <>'E'",
                arrayOf(codDocumento.toString())
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    detallesList.add(fnDetallesRecibo(cursor))
                }
                actualizarDetalles(detallesList)
            }
        } finally {
            DatabaseManager.closeDatabase()
        }
    }


    private fun fnDetallesRecibo(cursor: Cursor): Recibo {
        val transaccion = cursor.getString(cursor.getColumnIndexOrThrow("tr_descripcion"))
        val banco = cursor.getString(cursor.getColumnIndexOrThrow("ba_descripcion"))?:""
        val doc = cursor.getString(cursor.getColumnIndexOrThrow("rd_documento"))
        val cuenta = cursor.getString(cursor.getColumnIndexOrThrow("rd_ncuenta"))
        val fecha = cursor.getString(cursor.getColumnIndexOrThrow("rd_fecha"))
        val valor = String.format("%.2f", cursor.getDouble(cursor.getColumnIndexOrThrow("rd_valor")))
        val concepto = cursor.getString(cursor.getColumnIndexOrThrow("rd_concepto"))
        val observacion = cursor.getString(cursor.getColumnIndexOrThrow("rd_observacion"))
        val ba_codigo = cursor.getInt(cursor.getColumnIndexOrThrow("ba_codigo"))
        val tr_codigo = cursor.getInt(cursor.getColumnIndexOrThrow("tr_codigo"))
        val bc_codigo = cursor.getInt(cursor.getColumnIndexOrThrow("bc_codigo"))


        return Recibo(
            transaccion,
            banco,
            doc,
            cuenta,
            fecha,
            valor,
            concepto,
            observacion,
            ba_codigo,
            tr_codigo,
            bc_codigo
        )
    }


    fun fnObtenerNombreCliente(clCodigo: String): String {
        val db = DatabaseManager.openDatabase(context)
        var nombreCliente = ""
        try {
            db.rawQuery("SELECT cl_nombre FROM ve_ws_clienteAsignadoVendedor WHERE cl_codigo = ?", arrayOf(clCodigo)).use { cursor ->
                if (cursor.moveToFirst()) {
                    nombreCliente = cursor.getString(0)  // Asumimos que cl_nombre es la primera columna
                }
            }
        } catch (e: Exception) {
            Log.e("ClsLLenarControles", "Error al obtener nombre del cliente: ${e.localizedMessage}")
        } finally {
            DatabaseManager.closeDatabase()
        }
        return nombreCliente
    }

    // Función para obtener el nombre del transporte dado su código
    fun obtenerNombreTransporte(trCodigo: String): String {
        val db = DatabaseManager.openDatabase(context)
        var nombreTransporte = ""
        try {
            db.rawQuery("SELECT tr_nombre FROM ve_ws_transporte WHERE tr_codigo = ?", arrayOf(trCodigo)).use { cursor ->
                if (cursor.moveToFirst()) {
                    nombreTransporte = cursor.getString(0)  // Asumimos que tr_nombre es la primera columna
                }
            }
        } catch (e: Exception) {
            Log.e("ClsLLenarControles", "Error al obtener nombre del transporte: ${e.localizedMessage}")
        } finally {
            DatabaseManager.closeDatabase()
        }
        return nombreTransporte
    }

    fun selectItemInSpinner(spinner: Spinner, codigo: String) {
        val adapter = spinner.adapter as? ArrayAdapter<SpinnerItem>
        adapter?.let {
            // Buscar la posición basada en el código del ítem
            val position = (0 until it.count).firstOrNull { index ->
                it.getItem(index)?.codigo == codigo
            } ?: -1

            if (position >= 0) {
                spinner.setSelection(position)
            }
        }
    }


    fun fnEliminarDocumentoLogicamente(codDocumento: Int) {
        val db = DatabaseManager.openDatabase(context)
        try {
            db.beginTransaction() // Iniciar transacción para asegurar que ambas operaciones se completan

            // Actualizar la cabecera del pedido
            val valuesCabecera = ContentValues()
            valuesCabecera.put("pe_estado", "E")
            valuesCabecera.put("pe_fechaelim", fnObtenerFecha())// Marcar como eliminado lógicamente
            db.update(
                "fa_ws_cabpedidoQueue",
                valuesCabecera,
                "pe_coddocumento = ?",
                arrayOf(codDocumento.toString())
            )

            // Actualizar los detalles del pedido
            val valuesDetalle = ContentValues()
            valuesDetalle.put("dp_estado", "E")
            valuesDetalle.put("dp_fechaelim", fnObtenerFecha())// Marcar como eliminado lógicamente
            db.update(
                "fa_ws_detpedidoQueue",
                valuesDetalle,
                "pe_coddocumento = ?",
                arrayOf(codDocumento.toString())
            )

            db.setTransactionSuccessful() // Marcar la transacción como exitosa
        } catch (e: Exception) {
            Log.e("Database", "Error al eliminar documento lógicamente: ${e.localizedMessage}")
        } finally {
            db.endTransaction() // Finalizar la transacción
            DatabaseManager.closeDatabase() // Asegurarse de cerrar la base de datos
        }
    }


    fun fnEliminarReciboLogicamente(codDocumento: Int) {
        val db = DatabaseManager.openDatabase(context)
        try {
            db.beginTransaction() // Iniciar transacción para asegurar que ambas operaciones se completan

            // Actualizar la cabecera del recibo
            val valuesCabecera = ContentValues().apply {
                put("rc_estado", "E")
                put("rc_fechaelim", fnObtenerFecha()) // Marcar como eliminado lógicamente
            }
            db.update(
                "co_ws_reciboCobranzaConCab", // Nueva tabla para la cabecera
                valuesCabecera,
                "rc_codrecibo = ?",
                arrayOf(codDocumento.toString())
            )

            // Actualizar los detalles del recibo
            val valuesDetalle = ContentValues().apply {
                put("rd_estado", "E")
                put("rd_fechaelim", fnObtenerFecha()) // Marcar como eliminado lógicamente
            }
            db.update(
                "co_ws_reciboCobranzaConDet", // Nueva tabla para los detalles
                valuesDetalle,
                "rc_codrecibo = ?",
                arrayOf(codDocumento.toString())
            )

            db.setTransactionSuccessful() // Marcar la transacción como exitosa
        } catch (e: Exception) {
            Log.e("Database", "Error al eliminar documento lógicamente: ${e.localizedMessage}")
        } finally {
            db.endTransaction() // Finalizar la transacción
            DatabaseManager.closeDatabase() // Asegurarse de cerrar la base de datos
        }
    }



    fun fnObtenerFecha(): String {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formato.format(Date())
    }

    fun fnTransporteTarifa(trCodigo: Int, clCodigo: Int): List<TransporteTarifa> {
        val tarifas = mutableListOf<TransporteTarifa>()
        val db = DatabaseManager.openDatabase(context)
        // Determinar el sufijo de la columna tt_codigo basado en tr_codigo
        val ttCodigoColumn = if (trCodigo != 90) "tt_codigo" else "tt_codigoA"

        val sql = """
            SELECT tt_peso, tt_tarifa1, tt_tarifa2, tt_descripcion
            FROM fa_ws_transporteTarifa t
            LEFT JOIN ve_ws_clienteAsignadoVendedor v ON t.tt_codigo = v.$ttCodigoColumn
            WHERE t.tr_codigo = ? AND v.cl_codigo = ?
        """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(trCodigo.toString(), clCodigo.toString()))

        with(cursor) {
            while (moveToNext()) {
                val peso = getDouble(getColumnIndexOrThrow("tt_peso"))
                val tarifa1 = getDouble(getColumnIndexOrThrow("tt_tarifa1"))
                val tarifa2 = getDouble(getColumnIndexOrThrow("tt_tarifa2"))
                val descripcion = getString(getColumnIndexOrThrow("tt_descripcion"))

                tarifas.add(TransporteTarifa(peso, tarifa1, tarifa2, descripcion))
            }
            close()
        }

        return tarifas
    }

    fun fnObtenerStock(referencia: String, codigo: String): List<String> {
        val db = DatabaseManager.openDatabase(context)
        val valores = mutableListOf<String>()
        val cursor = db.rawQuery(
            "SELECT i.it_referencia AS Referencia," +
                    "i.it_descripcion AS Descripcion, " +
                    "ROUND(i.it_teler,2) AS StockTelerepuesto, " +
                    "ROUND(i.it_exhTele,2) AS StockExhTelerepuestos, " +
                    "ROUND(i.it_almesa,2) AS StockPortrans, " +
                    "ROUND(i.it_mmg,2) AS StockProMarket, " +
                    "ROUND(i.it_exhVmr,2) AS StockExhProMarket, " +
                    "ROUND(i.it_mmq ,2) AS StockReparadaAlm, " +
                    "ROUND(i.it_dcp,2) AS StockDepComercial, " +
                    "ROUND(i.pv_preciosubdistrib,3) AS SubDistribuidor, " +
                    "ROUND(i.pv_desctosubdistrib,3) AS DsctoSubDistr, " +
                    "ROUND(i.pv_precio5,3) AS Contado, " +
                    "ROUND(i.pv_precio6,3) AS Credito, " +
                    "ROUND(i.um_unidadCM,3) AS CartonMaster, " +
                    "ROUND(i.um_unidadCE,2) AS CartonEstandar, " +
                    "i.um_sku AS Sku, " +
                    "ROUND(i.um_pesoCE,3) AS PesoCE, " +
                    "IFNULL(c.cb_descripcionA, '') AS cb_descripcionA " +
                    "FROM ve_ws_item i " +
                    "left join iv_ws_itemComboCab c on c.cb_codigo = ?" +
                    "WHERE i.it_referencia = ?",
            arrayOf(codigo,referencia)
        )

        val columnNames = cursor.columnNames
        while (cursor.moveToNext()) {
            for (columnName in columnNames) {
                val valor = cursor.getStringOrNull(cursor.getColumnIndex(columnName))
                valores.add(valor ?: "0")
            }
        }
        cursor.close()
        db.close()

        return valores
    }

    fun fnObtenerPedidos(): List<PedidosDialogFragment.Pedido> {
        // Implementación para recuperar los pedidos desde una base de datos o fuente de datos
        val db = DatabaseManager.openDatabase(context)
        val pedidos = mutableListOf<PedidosDialogFragment.Pedido>()
        db.rawQuery("""
            SELECT 
                p.pe_coddocumento, 
                c.cl_nombre, 
                p.pe_valorTotal, 
                p.pe_coddocumentoA, 
                p.pe_estado, 
                p.pe_lote, 
                SUBSTR(p.pe_descripcion, 1, 50) as pe_descripcion,
                p.pe_fechaing,
                t.Descripcion
            FROM fa_ws_cabpedidoQueue p
            LEFT JOIN ve_ws_clienteAsignadoVendedor c ON p.cl_codigo = c.cl_codigo
            LEFT JOIN fa_ws_tipoFactura t ON p.im_codigo = t.Codigo
            WHERE  substr(p.pe_fechaing, 7, 4) || '-' || 
        substr(p.pe_fechaing, 4, 2) || '-' || 
        substr(p.pe_fechaing, 1, 2) >= date('now', '-1 month')
        AND p.pe_estado <> 'E'
        ORDER BY p.pe_coddocumento DESC
            
        """, null).use { cursor ->
            while (cursor.moveToNext()) {
                pedidos.add(
                    PedidosDialogFragment.Pedido(
                        numero = cursor.getString(cursor.getColumnIndexOrThrow("pe_coddocumento")),
                        cliente = cursor.getString(cursor.getColumnIndexOrThrow("cl_nombre"))?:"",
                        total = cursor.getString(cursor.getColumnIndexOrThrow("pe_valorTotal")),
                        numeroInterno = cursor.getString(cursor.getColumnIndexOrThrow("pe_coddocumentoA"))
                            ?: "0",
                        estado = when (cursor.getString(cursor.getColumnIndexOrThrow("pe_estado"))) {
                            "A" -> "Activo"
                            "C" -> "Enviado"
                            else -> cursor.getString(cursor.getColumnIndexOrThrow("pe_estado"))  // Maneja otros casos o estados desconocidos
                        },
                        lote = cursor.getString(cursor.getColumnIndexOrThrow("pe_lote")) ?: "0",
                        observaciones = cursor.getString(cursor.getColumnIndexOrThrow("pe_descripcion"))?:"",
                        fecha = cursor.getString(cursor.getColumnIndexOrThrow("pe_fechaing"))
                    )
                )
            }
        }
        db.close()
        return pedidos
    }



    fun fnObtenerPedidosPorCliente(cliente:String, fechaInc:String, fechaFin:String): List<PedidosDialogFragment.Pedido> {
        // Implementación para recuperar los pedidos desde una base de datos o fuente de datos
        val db = DatabaseManager.openDatabase(context)
        val pedidos = mutableListOf<PedidosDialogFragment.Pedido>()


        db.rawQuery("""
            SELECT 
                p.pe_coddocumento, 
                c.cl_nombre, 
                p.pe_valorTotal, 
                p.pe_coddocumentoA, 
                p.pe_estado, 
                p.pe_lote, 
                SUBSTR(p.pe_descripcion, 1, 50) as pe_descripcion,
                p.pe_fechaing
            FROM fa_ws_cabpedidoQueue p
            LEFT JOIN ve_ws_clienteAsignadoVendedor c ON p.cl_codigo = c.cl_codigo
            WHERE c.cl_nombre LIKE '$cliente%'
            AND date(substr(p.pe_fechaing, 7, 4) || '-' || substr(p.pe_fechaing, 4, 2) || '-' || substr(p.pe_fechaing, 1, 2))
            BETWEEN date('$fechaInc') AND date('$fechaFin')
            AND p.pe_estado <> 'E'
            ORDER BY p.pe_coddocumento DESC
        """, null).use { cursor ->
            while (cursor.moveToNext()) {
                pedidos.add(
                    PedidosDialogFragment.Pedido(
                        numero = cursor.getString(cursor.getColumnIndexOrThrow("pe_coddocumento")),
                        cliente = cursor.getString(cursor.getColumnIndexOrThrow("cl_nombre")),
                        total = cursor.getString(cursor.getColumnIndexOrThrow("pe_valorTotal")),
                        numeroInterno = cursor.getString(cursor.getColumnIndexOrThrow("pe_coddocumentoA"))
                            ?: "0",
                        estado = when (cursor.getString(cursor.getColumnIndexOrThrow("pe_estado"))) {
                            "A" -> "Activo"
                            "C" -> "Enviado"
                            else -> cursor.getString(cursor.getColumnIndexOrThrow("pe_estado"))  // Maneja otros casos o estados desconocidos
                        },
                        lote = cursor.getString(cursor.getColumnIndexOrThrow("pe_lote")) ?: "0",
                        observaciones = cursor.getString(cursor.getColumnIndexOrThrow("pe_descripcion")),
                        fecha = cursor.getString(cursor.getColumnIndexOrThrow("pe_fechaing"))
                    )
                )
            }
        }
        db.close()
        return pedidos
    }


    fun fnObtenerRecibos(context: Context): List<RecibosDialogFragment.Recibo> {
        val db = DatabaseManager.openDatabase(context)
        val pedidos = mutableListOf<RecibosDialogFragment.Recibo>()

        try {
            db.rawQuery(""" 
            SELECT 
                r.rc_codrecibo, 
                c.cl_nombre, 
                r.rc_total,
                r.rc_estado,
                r.rc_fechaing,
                r.rc_codreciboA
            FROM co_ws_reciboCobranzaConCab r
            LEFT JOIN ve_ws_clienteAsignadoVendedor c ON r.ep_codigoBenef = c.cl_codigo
            WHERE  substr(r.rc_fechaing, 7, 4) || '-' || 
        substr(r.rc_fechaing, 4, 2) || '-' || 
        substr(r.rc_fechaing, 1, 2) >= date('now', '-1 month')
        AND r.rc_estado <> 'E'
            ORDER BY CAST(r.rc_codrecibo AS INTEGER) DESC
        """, null).use { cursor ->
                while (cursor.moveToNext()) {
                    pedidos.add(
                        RecibosDialogFragment.Recibo(
                            codigo = cursor.getString(cursor.getColumnIndexOrThrow("rc_codrecibo"))?:"",
                            cliente = cursor.getString(cursor.getColumnIndexOrThrow("cl_nombre"))?:"",
                            total = cursor.getString(cursor.getColumnIndexOrThrow("rc_total"))?:"",
                            estado = when (cursor.getString(cursor.getColumnIndexOrThrow("rc_estado"))) {
                                "A" -> "Activo"
                                "C" -> "Enviado"
                                else -> ""
                            },
                            fecha = cursor.getString(cursor.getColumnIndexOrThrow("rc_fechaing"))?:"",
                            interno = cursor.getString(cursor.getColumnIndexOrThrow("rc_codreciboA"))
                                ?: ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            fnErrorDialog(context, "Error en fnObtenerRecibos:\n${e.message}")
        } finally {
            db.close()
        }

        return pedidos
    }


    fun fnObtenerRecibosPorCliente(cliente:String, fechaInc:String, fechaFin:String): List<RecibosDialogFragment.Recibo> {
        // Implementación para recuperar los pedidos desde una base de datos o fuente de datos
        val db = DatabaseManager.openDatabase(context)
        val pedidos = mutableListOf<RecibosDialogFragment.Recibo>()
        db.rawQuery("""
           SELECT 
                r.rc_codrecibo, 
                c.cl_nombre, 
                r.rc_total,
                r.rc_estado,
                r.rc_fechaing,
                r.rc_codreciboA
            FROM co_ws_reciboCobranzaConCab r
            LEFT JOIN ve_ws_clienteAsignadoVendedor c ON r.ep_codigoBenef = c.cl_codigo
			 where c.cl_nombre like '$cliente%'
            AND date(substr(r.rc_fechaing, 7, 4) || '-' || substr(r.rc_fechaing, 4, 2) || '-' || substr(r.rc_fechaing, 1, 2))
            BETWEEN date('$fechaInc') AND date('$fechaFin')
             AND r.rc_estado <> 'E'
            ORDER BY  r.rc_codrecibo DESC
        """, null).use { cursor ->
            while (cursor.moveToNext()) {
                pedidos.add(
                    RecibosDialogFragment.Recibo(
                        codigo = cursor.getString(cursor.getColumnIndexOrThrow("rc_codrecibo")),
                        cliente = cursor.getString(cursor.getColumnIndexOrThrow("cl_nombre")),
                        total = cursor.getString(cursor.getColumnIndexOrThrow("rc_total")),
                        estado = when (cursor.getString(cursor.getColumnIndexOrThrow("rc_estado"))) {
                            "A" -> "Activo"
                            "C" -> "Enviado"
                            else -> ""
                        },
                        fecha = cursor.getString(cursor.getColumnIndexOrThrow("rc_fechaing")),
                        interno = cursor.getString(cursor.getColumnIndexOrThrow("rc_codreciboA"))
                            ?: ""
                    )
                )
            }
        }
        db.close()
        return pedidos
    }


    fun fnBuscaReferencia(referencia: String): List<datos> {
        val referencias = mutableListOf<datos>()
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery("SELECT it_referencia, it_codigo, " +
                "ROUND(it_almesa + it_teler + it_mmg + it_mmq + it_exhTele + it_exhVmr ,2) AS stock," +
                "ROUND(pv_preciosubdistrib, 2) AS pv_preciosubdistrib," +
                "ROUND(pv_precio5, 2) AS pv_precio5, " +
                "ROUND(pv_precio6, 2) AS pv_precio6, " +
                "it_descripcion, um_pesoCE, it_costoprom FROM ve_ws_item WHERE it_referencia LIKE '%$referencia%'", null)

        if (cursor.moveToFirst()) {
            do {
                val nuevaReferencia = datos(
                    referencia = cursor.getString(cursor.getColumnIndexOrThrow("it_referencia")),
                    codigo = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo")),
                    stock = cursor.getString(cursor.getColumnIndexOrThrow("stock")),
                    precioSub = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_preciosubdistrib")).toString(),
                    precioCont = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio5")).toString(),
                    precioCred = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio6")).toString(),
                    descripcion = cursor.getString(cursor.getColumnIndexOrThrow("it_descripcion"))?:"",
                    unidadCE = cursor.getString(cursor.getColumnIndexOrThrow("um_pesoCE"))?:"0",
                    costoProm = cursor.getDouble(cursor.getColumnIndexOrThrow("it_costoprom")).toString(),
                    combo = "",
                    cd_codigo = "",
                    it_regalo = "0"
                )
                referencias.add(nuevaReferencia)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return referencias
    }


    fun fnBuscaReferenciaRegalo(
        referencia: String,
        monto: String
    ): List<datos> {
        val referencias = mutableListOf<datos>()
        val db = DatabaseManager.openDatabase(context)

        try {
            db.rawQuery(
                """
            SELECT 
                it_referencia, 
                it_codigo, 
                ROUND(it_almesa + it_teler + it_mmg + it_mmq + it_exhTele + it_exhVmr, 2) AS stock,
                ROUND(pv_preciosubdistrib, 2) AS pv_preciosubdistrib,
                ROUND(pv_precio5, 2) AS pv_precio5, 
                ROUND(pv_precio6, 2) AS pv_precio6, 
                it_descripcion, 
                um_pesoCE, 
                it_costoprom, 
                it_regalo 
            FROM ve_ws_item 
            WHERE it_referencia LIKE ?
              AND CAST(it_costoprom AS double) BETWEEN 1 AND CAST(? AS double)
              AND it_regalo IN (3, 2)
            ORDER BY 1
            """.trimIndent(),
                arrayOf("%$referencia%", monto)
            ).use { cursor ->

                if (cursor.moveToFirst()) {
                    do {
                        val nuevaReferencia = datos(
                            referencia = cursor.getString(cursor.getColumnIndexOrThrow("it_referencia")),
                            codigo = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo")),
                            stock = cursor.getString(cursor.getColumnIndexOrThrow("stock")),
                            precioSub = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_preciosubdistrib")).toString(),
                            precioCont = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio5")).toString(),
                            precioCred = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio6")).toString(),
                            descripcion = cursor.getString(cursor.getColumnIndexOrThrow("it_descripcion")) ?: "",
                            unidadCE = cursor.getString(cursor.getColumnIndexOrThrow("um_pesoCE")) ?: "0",
                            costoProm = cursor.getDouble(cursor.getColumnIndexOrThrow("it_costoprom")).toString(),
                            combo = "0",
                            cd_codigo = "0",
                            it_regalo = cursor.getString(cursor.getColumnIndexOrThrow("it_regalo"))
                        )

                        referencias.add(nuevaReferencia)

                    } while (cursor.moveToNext())
                }
            }
        } finally {
            DatabaseManager.closeDatabase()
        }

        return referencias
    }


    fun fnBuscaReferenciaCombos(referencia: String): List<datos> {
        val referencias = mutableListOf<datos>()
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery("select " +
                "i.it_referencia, " +
                "d.it_codigo, " +
                "i.it_almesa + i.it_teler + i.it_mmg + i.it_mmq + i.it_exhTele + i.it_exhVmr  AS stock, " +
                "d.cb_precio AS pv_preciosubdistrib, " +
                "0 AS pv_precio5, " +
                "0 AS pv_precio6, " +
                "c.cb_descripcionA AS it_descripcion, " +
                "i.um_pesoCE, " +
                "d.it_costopromedio AS it_costoprom," +
                "c.cb_codigo " +
                "from iv_ws_itemComboDet d " +
                "left join ve_ws_item i on d.it_codigo = i.it_codigo " +
                "left join iv_ws_itemComboCab c on d.cb_codigo = c.cb_codigo " +
                "where c.cb_descripcionA like'%$referencia%'" +
                "order by c.cb_codigo, d.cb_linea ASC", null)

        if (cursor.moveToFirst()) {
            do {
                val nuevaReferencia = datos(
                    referencia = cursor.getString(cursor.getColumnIndexOrThrow("it_referencia"))?:"",
                    codigo = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo"))?:"0",
                    stock = cursor.getString(cursor.getColumnIndexOrThrow("stock"))?:"0",
                    precioSub = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_preciosubdistrib")).toString(),
                    precioCont = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio5")).toString(),
                    precioCred = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio6")).toString(),
                    descripcion = cursor.getString(cursor.getColumnIndexOrThrow("it_descripcion"))?:"",
                    unidadCE = cursor.getString(cursor.getColumnIndexOrThrow("um_pesoCE"))?:"0",
                    costoProm = cursor.getDouble(cursor.getColumnIndexOrThrow("it_costoprom")).toString(),
                    combo = cursor.getString(cursor.getColumnIndexOrThrow("cb_codigo"))?:"0",
                    cd_codigo= cursor.getString(cursor.getColumnIndexOrThrow("cb_codigo"))?:"",
                    it_regalo = "0"
                )
                referencias.add(nuevaReferencia)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return referencias
    }

    fun fnBuscaReferenciaYcombos(
        textoBuscar: String,
        criterio: String
    ): List<datos> {

        val referencias = mutableListOf<datos>()
        val db = DatabaseManager.openDatabase(context)

        val campoItem = when (criterio) {
            "Referencia" -> "it_referencia"
            "Marca" -> "it_marca"
            "Descripcion" -> "it_descripcion"
            else -> "it_referencia"
        }

        val campoCombo = when (criterio) {
            "Referencia" -> "i.it_referencia"
            "Marca" -> "i.it_marca"
            "Descripcion" -> "c.cb_descripcionA"
            else -> "i.it_referencia"
        }

        val query = """
        SELECT 
            it_referencia, 
            it_codigo,
            ROUND(it_almesa + it_teler + it_mmg + it_mmq + it_exhTele + it_exhVmr, 2) AS stock,
            ROUND(pv_preciosubdistrib, 3) AS pv_preciosubdistrib,
            ROUND(pv_precio5, 3) AS pv_precio5,
            ROUND(pv_precio6, 3) AS pv_precio6,
            it_descripcion,
            um_pesoCE,
            it_costoprom,
            0 AS cb_codigo
        FROM ve_ws_item 
        WHERE $campoItem LIKE ?

        UNION ALL

        SELECT 
            i.it_referencia,
            d.it_codigo,
            ROUND(i.it_almesa + i.it_teler + i.it_mmg + i.it_mmq + i.it_exhTele + i.it_exhVmr, 2) AS stock,
            d.cb_precio AS pv_preciosubdistrib,
            0 AS pv_precio5,
            0 AS pv_precio6,
            c.cb_descripcionA AS it_descripcion,
            i.um_pesoCE,
            d.it_costopromedio AS it_costoprom,
            c.cb_codigo
        FROM iv_ws_itemComboDet d
        INNER JOIN ve_ws_item i ON d.it_codigo = i.it_codigo
        LEFT JOIN iv_ws_itemComboCab c ON d.cb_codigo = c.cb_codigo
        WHERE $campoCombo LIKE ?
          AND d.cb_linea = 1

        ORDER BY 1, 10
    """.trimIndent()

        val parametro = "%$textoBuscar%"

        val cursor = db.rawQuery(
            query,
            arrayOf(parametro, parametro)
        )

        if (cursor.moveToFirst()) {
            do {
                val nuevaReferencia = datos(
                    referencia = cursor.getString(cursor.getColumnIndexOrThrow("it_referencia")) ?: "",
                    codigo = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo")) ?: "",
                    stock = cursor.getString(cursor.getColumnIndexOrThrow("stock")) ?: "0",
                    precioSub = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_preciosubdistrib")).toString(),
                    precioCont = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio5")).toString(),
                    precioCred = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio6")).toString(),
                    descripcion = cursor.getString(cursor.getColumnIndexOrThrow("it_descripcion")) ?: "",
                    unidadCE = cursor.getString(cursor.getColumnIndexOrThrow("um_pesoCE")) ?: "0",
                    costoProm = cursor.getDouble(cursor.getColumnIndexOrThrow("it_costoprom")).toString(),
                    combo = cursor.getString(cursor.getColumnIndexOrThrow("cb_codigo")) ?: "0",
                    cd_codigo = cursor.getString(cursor.getColumnIndexOrThrow("cb_codigo")) ?: "",
                    it_regalo = "0"
                )

                referencias.add(nuevaReferencia)

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return referencias
    }




    fun fnBuscaReferenciaRetroVenta(referencia: String): List<datos> {
        val referencias = mutableListOf<datos>()
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery("SELECT it_referencia, it_codigo, " +
                "ROUND(it_almesa + it_teler + it_mmg + it_mmq + it_exhTele + it_exhVmr ,2) AS stock," +
                "ROUND(pv_preciosubdistrib, 2) AS pv_preciosubdistrib," +
                "ROUND(pv_precio5, 2) AS pv_precio5, " +
                "ROUND(pv_precio6, 2) AS pv_precio6, " +
                "it_descripcion, um_pesoCE, it_costoprom FROM ve_ws_item WHERE it_referencia LIKE '%$referencia%'" +
                "AND it_activaex = 1", null)

        if (cursor.moveToFirst()) {
            do {
                val nuevaReferencia = datos(
                    referencia = cursor.getString(cursor.getColumnIndexOrThrow("it_referencia")),
                    codigo = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo")),
                    stock = cursor.getString(cursor.getColumnIndexOrThrow("stock")),
                    precioSub = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_preciosubdistrib")).toString(),
                    precioCont = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio5")).toString(),
                    precioCred = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio6")).toString(),
                    descripcion = cursor.getString(cursor.getColumnIndexOrThrow("it_descripcion"))?:"",
                    unidadCE = cursor.getString(cursor.getColumnIndexOrThrow("um_pesoCE"))?:"0",
                    costoProm = cursor.getDouble(cursor.getColumnIndexOrThrow("it_costoprom")).toString(),
                    combo = "",
                    cd_codigo = "",
                    it_regalo = "0"
                )
                referencias.add(nuevaReferencia)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return referencias
    }


    fun fnPreciosYStock(criteria: SearchCriteria): List<PreciosyStock> {
        val iva = fnIva()
        val db = DatabaseManager.openDatabase(context)
        val valores = mutableListOf<PreciosyStock>()

        val selectionArgs = mutableListOf<String>()
        val selectionCriteria = mutableListOf<String>()

        criteria.referencia?.let {
            selectionCriteria.add("it_referencia LIKE ?")
            selectionArgs.add("%$it%")
        }
        criteria.marca?.let {
            selectionCriteria.add("it_marca LIKE ?")
            selectionArgs.add("$it%")
        }
        criteria.tipoProducto?.let {
            selectionCriteria.add("it_titulo LIKE ?")
            selectionArgs.add("$it%")
        }
        criteria.familia?.let {
            selectionCriteria.add("it_familia LIKE ?")
            selectionArgs.add("$it%")
        }
        criteria.descripcion?.let {
            selectionCriteria.add("it_descripcion LIKE ?")
            selectionArgs.add("$it%")
        }

        val whereClause = if (selectionCriteria.isNotEmpty()) " WHERE ${selectionCriteria.joinToString(" AND ")}" else ""

        val query = """
        SELECT it_codigo AS Codigo, it_marca AS Marca, it_referencia AS Referencia, um_sku AS Sku,
        it_descripcion AS Descripcion, (it_almesa + it_teler + it_mmg + it_mmq) AS Total, 
        ROUND(pv_preciosubdistrib, 2) AS SubDistribuidor, ROUND(pv_precio5, 2) AS Contado, 
        ROUND(pv_precio6, 2) AS Credito, ROUND(pv_precio7, 2) AS Publico, 
        ROUND(pv_precio7 * 1.15, 2) AS PublicoF, um_pesoCE AS Peso,
        it_titulo AS Titulo, it_costoprom AS costoProm
        
        FROM ve_ws_item$whereClause
    """

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {
            do {
                val preciosyStock = PreciosyStock(
                    marca = cursor.getString(cursor.getColumnIndexOrThrow("Marca")) ?: "",
                    referencia = cursor.getString(cursor.getColumnIndexOrThrow("Referencia")) ?: "",
                    sku = cursor.getString(cursor.getColumnIndexOrThrow("Sku")) ?: "Desconocido",
                    descripcion = cursor.getString(cursor.getColumnIndexOrThrow("Descripcion")) ?: "",
                    total = cursor.getString(cursor.getColumnIndexOrThrow("Total")) ?: "0",
                    contado = cursor.getString(cursor.getColumnIndexOrThrow("Contado")) ?: "0.00",
                    credito = cursor.getString(cursor.getColumnIndexOrThrow("Credito")) ?: "0.00",
                    publico = cursor.getString(cursor.getColumnIndexOrThrow("Publico")) ?: "0.00",
                    final = cursor.getString(cursor.getColumnIndexOrThrow("PublicoF")) ?: "0.00",
                    peso = cursor.getString(cursor.getColumnIndexOrThrow("Peso")) ?: "0.00",
                    titulo =  cursor.getString(cursor.getColumnIndexOrThrow("Titulo")) ?: "",
                    codigo = cursor.getString(cursor.getColumnIndexOrThrow("Codigo")) ?: "",
                    sub = cursor.getString(cursor.getColumnIndexOrThrow("SubDistribuidor")) ?: "",
                    costProm = cursor.getString(cursor.getColumnIndexOrThrow("costoProm")) ?: ""
                )
                valores.add(preciosyStock)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return valores
    }


    fun fnPedidoReporte(
        codDocumento: Int,
        actualizarCabecera: (Cursor) -> Unit,
        actualizarDetalles: (MutableList<reporte>) -> Unit,
        onDocumentoNoEncontrado: () -> Unit  // Añade un callback para manejar la no existencia del documento
    ) {
        val db = DatabaseManager.openDatabase(context)
        var documentoEncontrado = false  // Variable para controlar si se encontró el documento

        try {
            db.rawQuery(
                "select pe_coddocumento," +
                        "pe_fechaing," +
                        "c.cl_nombre, " +
                        "t.tr_nombre, " +
                        "pe_descripcion, " +
                        "pe_coddocumentoA, " +
                        "c.ci_descripcion, " +
                        "c.pz_codigo, " +
                        "fp.Descripcion, " +
                        "c.pz_descripcion, " +
                        "v.vn_nombre," +
                        "pe_orden, " +
                        "pe_lote, " +
                        "pe_valorbruto, " +
                        "pe_valordescuento, " +
                        "pe_seguro, " +
                        "pe_valoriva, " +
                        "pe_flete, " +
                        "pe_valorTotal " +
                        "from fa_ws_cabpedidoQueue p " +
                        "left join ve_ws_clienteAsignadoVendedor c on p.cl_codigo = c.cl_codigo " +
                        "left join ve_ws_transporte t on p.tr_codigo = t.tr_codigo " +
                        "left join cc_ws_transacciones fp on p.tp_codigo = fp.Codigo " +
                        "left join ve_ws_vendedor v on p.ep_codigo = v.vn_codigo " +
                        "WHERE pe_coddocumento = ? " +
                        "and pe_estado <>'E'",

                arrayOf(codDocumento.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    actualizarCabecera(cursor)
                    documentoEncontrado = true  // Indica que se encontró el documento
                }
            }

            if (!documentoEncontrado) {
                // Invoca el callback de documento no encontrado si no se encontró la cabecera
                onDocumentoNoEncontrado()
                return  // Sale temprano para evitar buscar detalles si la cabecera no existe
            }

            val detallesList = mutableListOf<reporte>()
            db.rawQuery(
                "SELECT * FROM fa_ws_detpedidoQueue WHERE pe_coddocumento = ?",
                arrayOf(codDocumento.toString())
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    detallesList.add(detallesPedidoReporte(cursor))
                }
                actualizarDetalles(detallesList)
            }
        } finally {
            DatabaseManager.closeDatabase()
        }
    }


    private fun detallesPedidoReporte(cursor: Cursor): reporte {
        val codigo = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo"))
        val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("dp_descripcion"))
        val descuento = String.format("%.2f", cursor.getString(cursor.getColumnIndexOrThrow("dp_porcdescuento")).toDoubleOrNull() ?: 0.0)
        val cantidad = cursor.getString(cursor.getColumnIndexOrThrow("dp_cantidad"))
        val precio = String.format("%.2f", cursor.getString(cursor.getColumnIndexOrThrow("dp_precio")).toDoubleOrNull() ?: 0.0)
        val subtotal = BigDecimal(cantidad.toInt() * precio.toDouble()).setScale(2, BigDecimal.ROUND_HALF_UP).toString()
        val ConDescuento = (subtotal.toBigDecimal() - (subtotal.toBigDecimal() * descuento.toBigDecimal() / BigDecimal(100)))
            .setScale(2, RoundingMode.HALF_UP).toString()


        return reporte(
            codigo,
            descripcion,
            descuento,
            cantidad,
            precio,
            subtotal,
            ConDescuento

        )
    }


    fun fnReporteRecibo(
        codDocumento: Int,
        actualizarCabecera: (Cursor) -> Unit,
        actualizarDetalles: (MutableList<reporteRecibo>) -> Unit,
        onDocumentoNoEncontrado: () -> Unit
    ) {
        val db = DatabaseManager.openDatabase(context)
        var documentoEncontrado = false

        try {
            db.rawQuery(
                "SELECT " +
                        "rc_codrecibo, " +
                        "ep_codigoBenef || ' - ' || cl_nombre as ente, " +
                        "ep_codigoRes || ' - ' || vn_nombre as vendedor, " +
                        "rc_fecharec, " +
                        "rc_total, " +
                        "cl_nombre " +
                        "FROM co_ws_reciboCobranzaConCab c " +
                        "LEFT JOIN ve_ws_clienteAsignadoVendedor n ON c.ep_codigoBenef = n.cl_codigo " +
                        "LEFT JOIN ve_ws_vendedor v ON c.ep_codigoRes = v.vn_codigo " +
                        "WHERE rc_codrecibo = ? AND rc_estado <> 'E'",
                arrayOf(codDocumento.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    actualizarCabecera(cursor)
                    documentoEncontrado = true
                }
            }

            if (!documentoEncontrado) {
                onDocumentoNoEncontrado()
                return
            }

            val detallesList = mutableListOf<reporteRecibo>()

            db.rawQuery(
                "SELECT " +
                        "t.tr_descripcion, " +
                        "b.ba_descripcion, " +
                        "d.rd_documento, " +
                        "d.rd_ncuenta, " +
                        "d.rd_fecha, " +
                        "d.rd_concepto, " +
                        "d.rd_valor, " +
                        "d.rd_observacion " +
                        "FROM co_ws_reciboCobranzaConDet d " +
                        "LEFT JOIN cc_ws_transaccionesA t ON d.tr_codigo = t.tr_codigo " +
                        "LEFT JOIN cc_ws_bancoCliente b ON d.bc_codigo = b.bc_codigo " +
                        "WHERE d.rc_codrecibo = ? " +
                        "ORDER BY d.rd_documento",
                arrayOf(codDocumento.toString())
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    detallesList.add(fnReporteRecibo(cursor))
                }

                actualizarDetalles(detallesList)
            }
        } finally {
            DatabaseManager.closeDatabase()
        }
    }

    private fun fnReporteRecibo(cursor: Cursor): reporteRecibo {
        val transaccion = cursor.getString(cursor.getColumnIndexOrThrow("tr_descripcion"))
        val banco = cursor.getString(cursor.getColumnIndexOrThrow("ba_descripcion")) ?: ""
        val doc = cursor.getString(cursor.getColumnIndexOrThrow("rd_documento"))
        val cuenta = cursor.getString(cursor.getColumnIndexOrThrow("rd_ncuenta"))
        val fecha = cursor.getString(cursor.getColumnIndexOrThrow("rd_fecha"))
        val concepto = cursor.getString(cursor.getColumnIndexOrThrow("rd_concepto"))

        val valor = String.format(
            "%.2f",
            cursor.getString(cursor.getColumnIndexOrThrow("rd_valor")).toDoubleOrNull() ?: 0.0
        )

        val observacion = cursor.getString(cursor.getColumnIndexOrThrow("rd_observacion")) ?: ""

        return reporteRecibo(
            transaccion,
            banco,
            doc,
            cuenta,
            fecha,
            concepto,
            valor,
            observacion
        )
    }

    fun fnDetallesCombos(combo: String): List<datosDet> {
        val detalles = mutableListOf<datosDet>()
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery("""
        SELECT i.it_referencia,
               i.it_descripcion,
               d.cb_cantidad,
               round(cb_precio,2) as cb_precio,
               d.it_codigo,
               i.um_pesoCE,
               d.it_costopromedio,
               c.cb_codigo
        FROM iv_ws_itemComboDet d
        LEFT JOIN ve_ws_item i ON d.it_codigo = i.it_codigo
        LEFT JOIN iv_ws_itemComboCab c ON d.cb_codigo = c.cb_codigo
        WHERE d.cb_codigo = ?
        ORDER BY d.cb_linea ASC
    """, arrayOf(combo))

        if (cursor.moveToFirst()) {
            do {
                val referencia = cursor.getString(cursor.getColumnIndexOrThrow("it_referencia"))
                val cantidad = cursor.getString(cursor.getColumnIndexOrThrow("cb_cantidad"))
                val precio = cursor.getDouble(cursor.getColumnIndexOrThrow("cb_precio"))
                val codigo = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo"))
                val pesoCE = cursor.getString(cursor.getColumnIndexOrThrow("um_pesoCE"))
                val costProm = cursor.getDouble(cursor.getColumnIndexOrThrow("it_costopromedio"))
                val subtotal = cantidad.toDouble() * precio
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("it_descripcion"))
                val combo = cursor.getString(cursor.getColumnIndexOrThrow("cb_codigo"))

                val nuevoDetalle = datosDet(
                    Referencia = "$referencia - $descripcion" ,
                    Cantidad = cantidad,
                    Precio = precio.toString(),
                    Subtotal = subtotal.toString(),
                    Codigo = codigo,
                    Descripcion = "", // Asumiendo que no hay descripción en tu consulta
                    unidadCE = pesoCE,
                    DescItem = "0.00",  // No se asigna en la consulta
                    ConDesc = precio.toString(),   // No se asigna en la consulta
                    costProm = costProm.toString(),
                    lote = String.format("%.2f",(precio / costProm)),
                    combo,
                    "0"
                )
                detalles.add(nuevoDetalle)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return detalles
    }


     fun fnObtenerItemxCliente(): List<items>{
        val itemC = mutableListOf<items>()
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery("""SELECT * FROM ve_ws_itemCliente""",null )


        if (cursor.moveToFirst()) {
            do {
                val sri = cursor.getString(cursor.getColumnIndexOrThrow("sri"))
                val bodega = cursor.getString(cursor.getColumnIndexOrThrow("bo_descripcion"))
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fechafactura"))
                val tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipofactura"))
                val cantidad = cursor.getString(cursor.getColumnIndexOrThrow("cantidad"))
                val precio = cursor.getString(cursor.getColumnIndexOrThrow("precioventa"))
                val referencia = cursor.getString(cursor.getColumnIndexOrThrow("referencia"))
                val cliente = cursor.getString(cursor.getColumnIndexOrThrow("cliente"))
                val total = cursor.getString(cursor.getColumnIndexOrThrow("total"))


                val nuevoDetalle = items(
                     Sri = sri,
                     Bodega = bodega,
                     FechaF = fecha,
                     TipoF = tipo,
                     Cantidad = cantidad,
                     PrecioV = String.format("%.2f", precio.toBigDecimal()),
                     Total = String.format("%.2f", total.toBigDecimal()),
                     Cliente = cliente,
                     Referencia = referencia,
                )
                itemC.add(nuevoDetalle)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()


        return itemC
    }


    fun fnObtenerDatosCliente(nombre: String): List<clienteD>{
        val ClienteL = mutableListOf<clienteD>()
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery("""SELECT c.cl_nombre, c.cl_direccion, c.cl_fono, c.cl_codigo, r.rv_check, r.rv_latitud
            FROM ve_ws_clienteAsignadoVendedor c
            LEFT JOIN ve_ws_rutavendedor r on c.cl_codigo = r.cl_codigo
            WHERE c.cl_nombre LIKE '%$nombre%'
            AND c.cl_codigo <> 0
            ORDER BY cl_nombre""".trimMargin(),null )


        if (cursor.moveToFirst()) {
            var contador = 1  // Inicializa el contador
            do {
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("cl_nombre")) ?: ""
                val direccion = cursor.getString(cursor.getColumnIndexOrThrow("cl_direccion")) ?: ""
                val telefono = cursor.getString(cursor.getColumnIndexOrThrow("cl_fono")) ?: ""
                val codigo = cursor.getString(cursor.getColumnIndexOrThrow("cl_codigo")) ?: ""
                val check = cursor.getString(cursor.getColumnIndexOrThrow("r.rv_latitud"))
                val isChecked: Boolean = !check.isNullOrEmpty()

                val nuevoDetalle = clienteD(
                    Numero = contador.toString(),
                    Cliente = nombre,
                    Direccion = direccion,
                    Telefono = telefono,
                    Codigo = codigo,
                    isSelected = isChecked
                )
                ClienteL.add(nuevoDetalle)
                contador++  // Incrementa el contador
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()


        return ClienteL
    }


    fun fnOtenerNombreUsuario(userCode: Int): String {
        val db = DatabaseManager.openDatabase(context)
        // Se asume que 'user_code' es el nombre de la columna en la base de datos que almacena el código del usuario.
        val cursor = db.rawQuery("SELECT vn_nombre FROM ve_ws_vendedor WHERE vn_codigo = ?", arrayOf(userCode.toString()))

        var nombreUsuario = "" // Valor predeterminado en caso de que no se encuentre el usuario

        if (cursor.moveToFirst()) {
            val nombreColumnIndex = cursor.getColumnIndex("vn_nombre")
            if (nombreColumnIndex != -1 && !cursor.isNull(nombreColumnIndex)) {
                nombreUsuario = cursor.getString(nombreColumnIndex)
            }
        }

        cursor.close()
        db.close()
        return nombreUsuario
    }


    fun fnObtenerStatus(): MutableList<status> {
        val listaStatus = mutableListOf<status>()

        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery("""SELECT *,
       strftime('%d/%m/%Y', pe_fechaing) AS FechaCorta,
       strftime('%d/%m/%Y', fa_fechafactura) AS FechaFacturaCorta
        FROM fa_ws_auditoriapedido;
        """,null )

        // Verificar si el cursor tiene datos
        if (cursor.moveToFirst()) {
            do {
                // Crear un objeto Status por cada fila en el cursor
                val status = status(
                    Status = cursor.getString(cursor.getColumnIndexOrThrow("estado"))?:"",
                    Pedido = cursor.getString(cursor.getColumnIndexOrThrow("pe_coddocumento"))?:"",
                    Cliente = cursor.getString(cursor.getColumnIndexOrThrow("nombreCliente"))?:"",
                    Dispos = cursor.getString(cursor.getColumnIndexOrThrow("te_descripcion"))?:"",
                    FechaP = cursor.getString(cursor.getColumnIndexOrThrow("FechaCorta"))?:"",
                    Bodega = cursor.getString(cursor.getColumnIndexOrThrow("bodega"))?:"",
                    Sri = cursor.getString(cursor.getColumnIndexOrThrow("fa_sri"))?:"",
                    Gr = cursor.getString(cursor.getColumnIndexOrThrow("fa_guiaremision"))?:"",
                    FechaF = cursor.getString(cursor.getColumnIndexOrThrow("FechaFacturaCorta"))?:"",
                    TotalP = cursor.getString(cursor.getColumnIndexOrThrow("pe_valorTotal"))?:"",
                    TotalF = cursor.getString(cursor.getColumnIndexOrThrow("totalfact"))?:"",
                    Observacion = cursor.getString(cursor.getColumnIndexOrThrow("pe_observacion"))?:""
                )
                listaStatus.add(status)  // Añadir el objeto Status a la lista
            } while (cursor.moveToNext())
        }
        cursor.close()  // Cerrar el cursor para liberar recursos

        return listaStatus
    }


    fun fnObtenerDocumento(): MutableList<Documento> {
        val listaStatus = mutableListOf<Documento>()

        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery("""SELECT bodega,
            factura,
            sri,
            fecha,
            gremision,
            gtransporte,
            ROUND(valortotal,2) AS valortotal,
            de_serie,
            de_claveacceso
        FROM fa_ws_cabfactura;
        """,null )

        // Verificar si el cursor tiene datos
        if (cursor.moveToFirst()) {
            do {
                // Crear un objeto Status por cada fila en el cursor
                val status = Documento(
                    Bodega = cursor.getString(cursor.getColumnIndexOrThrow("bodega"))?:"",
                    Interno = cursor.getString(cursor.getColumnIndexOrThrow("factura"))?:"",
                    Sri = cursor.getString(cursor.getColumnIndexOrThrow("sri"))?:"",
                    Fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))?:"",
                    GuiaR = cursor.getString(cursor.getColumnIndexOrThrow("gremision"))?:"",
                    GuiaT = cursor.getString(cursor.getColumnIndexOrThrow("gtransporte"))?:"",
                    Total = cursor.getString(cursor.getColumnIndexOrThrow("valortotal"))?:"",
                    Serie = cursor.getString(cursor.getColumnIndexOrThrow("de_serie"))?:"",
                    ClaveA = cursor.getString(cursor.getColumnIndexOrThrow("de_claveacceso"))?:"",
                )
                listaStatus.add(status)  // Añadir el objeto Status a la lista
            } while (cursor.moveToNext())
        }
        cursor.close()  // Cerrar el cursor para liberar recursos

        return listaStatus
    }


    fun fnObtenerFactura(factura: String): MutableList<Factura> {
        val listaStatus = mutableListOf<Factura>()

        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery("""SELECT *
                                FROM fa_ws_detfactura
                                WHERE fa_coddocumento = ?;""", arrayOf(factura))

        // Verificar si el cursor tiene datos
        if (cursor.moveToFirst()) {
            do {
                // Crear un objeto Factura por cada fila en el cursor
                val status = Factura(
                    desct = cursor.getString(cursor.getColumnIndexOrThrow("fa_valordescuento")) ?: "",
                    lote = cursor.getString(cursor.getColumnIndexOrThrow("fa_rentab")) ?: "",
                    observacion = cursor.getString(cursor.getColumnIndexOrThrow("fa_descripcion")) ?: "",
                    codigo = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo")) ?: "",
                    referencia = cursor.getString(cursor.getColumnIndexOrThrow("it_referencia")) ?: "",
                    descuento = cursor.getString(cursor.getColumnIndexOrThrow("gt_porcdescuento")) ?: "",
                    cantidad = cursor.getString(cursor.getColumnIndexOrThrow("gt_cant_req")) ?: "",
                    precio = cursor.getDouble(cursor.getColumnIndexOrThrow("gt_costo_prom")).format(3),
                    subtotal = cursor.getDouble(cursor.getColumnIndexOrThrow("gt_preciovta")).format(3)
                )
                listaStatus.add(status)  // Añadir el objeto Factura a la lista
            } while (cursor.moveToNext())
        }
        cursor.close()  // Cerrar el cursor para liberar recursos

        return listaStatus
    }

    // Extensión para formatear Double con un número específico de decimales
    fun Double.format(digits: Int) = "%.${digits}f".format(this)


    fun fnInsertarGpEnlace(rc_codrecibo: Int, rc_enlace: String): Boolean {
        val db = DatabaseManager.openDatabase(context)
        val contentValues = ContentValues().apply {
            put("rc_enlace", rc_enlace)
        }

        val result = db.update("co_ws_reciboCobranzaConCab", contentValues, "rc_codrecibo = ?", arrayOf(rc_codrecibo.toString()))
        DatabaseManager.closeDatabase()

        return if (result == 0) {
            //Log.e("Database", "Error al actualizar el gp_enlace en ve_ws_gestionPromotor")
            false
        } else {
            //Log.d("Database", "gp_enlace actualizado correctamente en ve_ws_gestionPromotor")
            true
        }
    }




    private fun fnIva(): Int {
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery("SELECT pi_porcentaje FROM fa_ws_parametroIva", null)
        var porcentajeIva = 0 // Valor predeterminado en caso de que no se encuentre ningún resultado
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex("pi_porcentaje")
            porcentajeIva = cursor.getInt(columnIndex)
            porcentajeIva = porcentajeIva / 100
        }
        cursor.close()
        db.close()
        return porcentajeIva
    }


    fun fnBuscarFacturas(cliente: String): List<Facturas> {
        val db = DatabaseManager.openDatabase(context)
        val facturas = mutableListOf<Facturas>()
        val query = """
         SELECT fa_sri, bo_descripcion, 
           strftime('%d/%m/%Y', SUBSTR(cc_fechafactura, 1, 10)) AS cc_fechafactura,  -- Convierte a formato dd/mm/yyyy
           ROUND(cc_valorfactura, 2) AS cc_valorfactura, 
           ROUND(cc_valorsaldo, 2) AS cc_valorsaldo
        FROM cc_ws_detalleCobro
        WHERE cl_codigo = ?
        ORDER BY SUBSTR(cc_fechafactura, 1, 10); 
        """.trimIndent()

        val selectionArgs = arrayOf(cliente)  // Aquí pasas el parámetro

        db.rawQuery(query, selectionArgs).use { cursor ->
            while (cursor.moveToNext()) {
                facturas.add(
                    Facturas(
                        Factura = cursor.getString(cursor.getColumnIndexOrThrow("fa_sri")),
                        Bodega = cursor.getString(cursor.getColumnIndexOrThrow("bo_descripcion")),
                        Fecha = cursor.getString(cursor.getColumnIndexOrThrow("cc_fechafactura")),
                        Valor = cursor.getString(cursor.getColumnIndexOrThrow("cc_valorfactura")),
                        Saldo = cursor.getString(cursor.getColumnIndexOrThrow("cc_valorsaldo"))
                    )
                )
            }
        }
        DatabaseManager.closeDatabase()
        return facturas
    }


    fun fnBuscarFacturasCriterios(): List<Facturas> {
        val db = DatabaseManager.openDatabase(context)
        val facturas = mutableListOf<Facturas>()

        val query = """
        SELECT 'Pago cheques protestados' as fa_sri 
        UNION ALL 
        SELECT 'Cambio de cheque' as fa_sri 
        UNION ALL 
        SELECT 'Cancelacion de pagare' as fa_sri 
        UNION ALL 
        SELECT 'Cancelacion de pedido' as fa_sri
    """.trimIndent()

        db.rawQuery(query, null).use { cursor ->
            while (cursor.moveToNext()) {
                facturas.add(
                    Facturas(
                        Factura = cursor.getString(cursor.getColumnIndexOrThrow("fa_sri")),
                        Bodega = "",
                        Fecha = "",
                        Valor = "",
                        Saldo = ""
                    )
                )
            }
        }

        DatabaseManager.closeDatabase()
        return facturas
    }


     fun fnVerificarRecibosPendientes():Int{
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery("SELECT COUNT(*) FROM co_ws_reciboCobranzaConCab WHERE rc_estado = 'A' " +
                "AND  substr(rc_fechaing, 7, 4) || '-' ||  substr(rc_fechaing, 4, 2) || '-' || " +
                "substr(rc_fechaing, 1, 2) >= date('now', '-1 month')", null)
        var totalPendientes = 0
        if (cursor.moveToFirst()) {
            totalPendientes = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return totalPendientes
    }


    fun fnVerificarPedidosPendientes():Int{
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery("SELECT COUNT(*) FROM fa_ws_cabpedidoQueue WHERE pe_estado = 'A'" +
                "AND  substr(pe_fechaing, 7, 4) || '-' ||  substr(pe_fechaing, 4, 2) || '-' || " +
                "substr(pe_fechaing, 1, 2) >= date('now', '-1 month')", null)
        var totalPendientes = 0
        if (cursor.moveToFirst()) {
            totalPendientes = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return totalPendientes
    }


    fun fnVerficarLopd(cl_codigo: Int): Lopd? {
        val db = DatabaseManager.openDatabase(context)
        var result: Lopd? = null

        val query = "SELECT cl_lopdp, cl_campania FROM ve_ws_clienteAsignadoVendedor WHERE cl_codigo = ? "
        val cursor = db.rawQuery(query, arrayOf(cl_codigo.toString()))

        if (cursor.moveToFirst()) {
            val cl_lopdp = cursor.getInt(cursor.getColumnIndexOrThrow("cl_lopdp"))
            val cl_campania = cursor.getInt(cursor.getColumnIndexOrThrow("cl_campania"))

            // Asignar los resultados a la variable result
            result = Lopd(cl_lopdp, cl_campania)
        }

        cursor.close()
        DatabaseManager.closeDatabase()

        return result
    }

    fun fnInsertarPolitica(cl_lopdp: Int, cl_campania: Int, cl_lopdpusuarioing: String, cl_codigo: Int): Boolean {
        val db = DatabaseManager.openDatabase(context)
        val contentValues = ContentValues().apply {
            put("cl_lopdp", cl_lopdp)  // Insertar valor de la política de datos personales
            put("cl_campania", cl_campania)  // Insertar valor de la campaña
            put("cl_lopdpusuarioing", cl_lopdpusuarioing)  // Insertar el usuario que ingresó la información
        }

        // Ejecutar la actualización en la base de datos
        val result = db.update("ve_ws_clienteAsignadoVendedor", contentValues, "cl_codigo = ?", arrayOf(cl_codigo.toString()))
        DatabaseManager.closeDatabase()

        // Verificar el resultado de la actualización
        return result != 0
    }

    fun fnActualizarCoordenada(
        co_latitud: Double,
        co_longitud: Double,
        co_timestamp: String,
        co_estado: String,
        cl_codigo: String,
        check: Int
    ): Boolean {
        val db = DatabaseManager.openDatabase(context)
        val actual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Crear los valores a insertar/actualizar
        val contentValues = ContentValues().apply {
            put("cl_codigo", cl_codigo)          // Código del cliente (clave)
            put("rv_latitud", co_latitud)        // Latitud de la coordenada
            put("rv_longitud", co_longitud)      // Longitud de la coordenada
            put("rv_timestamp", co_timestamp)    // Marca de tiempo
            put("rv_estado", co_estado)          // Estado
            put("rv_check", check)               // check
            put("rv_fecha", actual)              // Fecha actual
        }

        return try {
            // Intentar actualizar el registro
            val rowsAffected = db.update(
                "ve_ws_rutavendedor",    // Nombre de la tabla
                contentValues,          // Valores a actualizar
                "cl_codigo = ?",        // Condición WHERE
                arrayOf(cl_codigo)      // Valor para el placeholder "?"
            )

            // Si no se actualizó ninguna fila, insertar el registro
            if (rowsAffected == 0) {
                val insertResult = db.insert("ve_ws_rutavendedor", null, contentValues)
                insertResult != -1L  // Devuelve true si la inserción fue exitosa
            } else {
                true // Si se actualizó correctamente, retorna true
            }
        } catch (e: Exception) {
            Log.e("DatabaseError", "Error al actualizar/insertar coordenada: ${e.message}")
            false // Retorna false en caso de error
        } finally {
            DatabaseManager.closeDatabase() // Asegura el cierre de la base de datos
        }
    }

    fun fnFechaRutaVigente(): SemanaRuta? {
        val db = DatabaseManager.openDatabase(context)

        val hoyIso = java.text.SimpleDateFormat(
            "yyyy-MM-dd",
            java.util.Locale.US
        ).format(java.util.Date())

        fun leerSemana(
            sql: String,
            params: Array<String>,
            esActual: Boolean
        ): SemanaRuta? {
            db.rawQuery(sql, params).use { cursor ->
                if (cursor.moveToFirst()) {
                    return SemanaRuta(
                        inicial = cursor.getString(cursor.getColumnIndexOrThrow("fecha_ini")),
                        final = cursor.getString(cursor.getColumnIndexOrThrow("fecha_fin")),
                        esActual = esActual
                    )
                }
            }

            return null
        }

        try {
            val sqlSemanaActual = """
            SELECT 
                fecha_ini AS fecha_ini,
                fecha_fin AS fecha_fin
            FROM (
                SELECT DISTINCT
                    substr(rv_fechainicial, 1, 10) AS fecha_ini,
                    substr(rv_fechafinal, 1, 10) AS fecha_fin
                FROM fa_ws_rutaVendedor
                WHERE rv_estado <> 'C'
                  AND rv_fechainicial IS NOT NULL
                  AND rv_fechafinal IS NOT NULL
            )
            WHERE date(?) BETWEEN date(fecha_ini) AND date(fecha_fin)
            ORDER BY date(fecha_ini) DESC
            LIMIT 1
        """.trimIndent()

            val semanaActual = leerSemana(
                sqlSemanaActual,
                arrayOf(hoyIso),
                true
            )

            if (semanaActual != null) {
                return semanaActual
            }

            val sqlUltimaSemana = """
            SELECT 
                fecha_ini AS fecha_ini,
                fecha_fin AS fecha_fin
            FROM (
                SELECT DISTINCT
                    substr(rv_fechainicial, 1, 10) AS fecha_ini,
                    substr(rv_fechafinal, 1, 10) AS fecha_fin
                FROM fa_ws_rutaVendedor
                WHERE rv_estado <> 'C'
                  AND rv_fechainicial IS NOT NULL
                  AND rv_fechafinal IS NOT NULL
            )
            WHERE date(fecha_fin) < date(?)
            ORDER BY date(fecha_fin) DESC
            LIMIT 1
        """.trimIndent()

            return leerSemana(
                sqlUltimaSemana,
                arrayOf(hoyIso),
                false
            )

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            DatabaseManager.closeDatabase()
        }
    }

    fun fnObtenerRutas(
        dia: String,
        fechaInc: String,
        fechaFin: String
    ): List<AdaptadorRutas.Rutas> {

        val clienteL = mutableListOf<AdaptadorRutas.Rutas>()
        val db = DatabaseManager.openDatabase(context)

        try {
            db.rawQuery(
                """
            SELECT 
                r.cl_codigo, 
                c.cl_nombre, 
                ROUND(r.rv_monto, 2) AS rv_monto, 
                c.ci_descripcion,
                r.rv_distancia,
                r.cc_cupoasignado, 
                r.cc_cupodisponible,
                r.rv_visita,
                r.rv_cobro,
                r.rv_venta,
                r.rv_telefono,
                r.rv_observacion,
                r.rv_estado,
                r.rv_fechainicial,
                r.rv_fechafinal,
                r.rv_dia
            FROM fa_ws_rutaVendedor r
            INNER JOIN ve_ws_clienteAsignadoVendedor c 
                ON r.cl_codigo = c.cl_codigo
            WHERE TRIM(r.rv_dia) = ?
              AND r.rv_estado = 'A'
              AND substr(r.rv_fechainicial, 1, 10) <= ?
              AND substr(r.rv_fechafinal, 1, 10) >= ?
            ORDER BY r.rv_linea
            """.trimIndent(),
                arrayOf(
                    dia.trim(),
                    fechaFin,
                    fechaInc
                )
            ).use { cursor ->

                if (cursor.moveToFirst()) {
                    var contador = 1

                    do {
                        val nombre = cursor.getString(cursor.getColumnIndexOrThrow("cl_nombre")) ?: ""
                        val codigo = cursor.getString(cursor.getColumnIndexOrThrow("cl_codigo")) ?: ""
                        val saldo = cursor.getDouble(cursor.getColumnIndexOrThrow("rv_monto"))
                        val ciudad = cursor.getString(cursor.getColumnIndexOrThrow("ci_descripcion")) ?: ""
                        val distancia = cursor.getString(cursor.getColumnIndexOrThrow("rv_distancia")) ?: ""
                        val cupo = cursor.getDouble(cursor.getColumnIndexOrThrow("cc_cupoasignado"))
                        val estado = cursor.getString(cursor.getColumnIndexOrThrow("rv_estado")) ?: ""
                        val observacion = cursor.getString(cursor.getColumnIndexOrThrow("rv_observacion")) ?: ""

                        val isCheckedVisita =
                            cursor.getInt(cursor.getColumnIndexOrThrow("rv_visita")) == 1

                        val isCheckedCobro =
                            cursor.getInt(cursor.getColumnIndexOrThrow("rv_cobro")) == 1

                        val isCheckedVenta =
                            cursor.getInt(cursor.getColumnIndexOrThrow("rv_venta")) == 1

                        val isCheckedTelefono =
                            cursor.getInt(cursor.getColumnIndexOrThrow("rv_telefono")) == 1

                        val nuevoDetalle = AdaptadorRutas.Rutas(
                            codigo = codigo,
                            sec = contador.toString(),
                            cliente = nombre,
                            Saldo = "$" + String.format("%.2f", saldo),
                            Ciudad = ciudad,
                            Cupo = "$" + String.format("%.2f", cupo),
                            estado = estado,
                            distancia = distancia,
                            observacion = observacion,
                            isSelectedVisita = isCheckedVisita,
                            isSelectedVenta = isCheckedVenta,
                            isSelectedCobro = isCheckedCobro,
                            isSelectedTelefono = isCheckedTelefono
                        )

                        clienteL.add(nuevoDetalle)
                        contador++

                    } while (cursor.moveToNext())
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            DatabaseManager.closeDatabase()
        }

        return clienteL
    }

    fun fnObtenerRutasPendientes(
        fechaIni: String,
        fechaFin: String,
        diaActual: String
    ): List<AdaptadorRutas.Rutas> {

        val clienteL = mutableListOf<AdaptadorRutas.Rutas>()

        val diasPendientes = when (diaActual) {
            "Lunes" -> emptyList()
            "Martes" -> listOf("Lunes")
            "Miércoles" -> listOf("Lunes", "Martes")
            "Jueves" -> listOf("Lunes", "Martes", "Miércoles")
            "Viernes" -> listOf("Lunes", "Martes", "Miércoles", "Jueves")
            else -> emptyList()
        }

        if (diasPendientes.isEmpty()) {
            return clienteL
        }

        val db = DatabaseManager.openDatabase(context)
        val placeholders = diasPendientes.joinToString(",") { "?" }

        try {
            db.beginTransaction()

            db.execSQL(
                """
            UPDATE fa_ws_rutaVendedor
            SET rv_estado = 'P'
            WHERE rv_visita = 0
              AND rv_cobro = 0
              AND rv_venta = 0
              AND rv_estado = 'A'
              AND TRIM(rv_dia) IN ($placeholders)
              AND substr(rv_fechainicial, 1, 10) <= ?
              AND substr(rv_fechafinal, 1, 10) >= ?
            """.trimIndent(),
                arrayOf(
                    *diasPendientes.toTypedArray(),
                    fechaFin,
                    fechaIni
                )
            )

            db.rawQuery(
                """
            SELECT 
                r.cl_codigo,
                c.cl_nombre,
                ROUND(r.rv_monto, 2) AS rv_monto,
                c.ci_descripcion,
                r.rv_distancia,
                r.cc_cupoasignado,
                r.cc_cupodisponible,
                r.rv_visita,
                r.rv_cobro,
                r.rv_venta,
                r.rv_telefono,
                r.rv_observacion,
                r.rv_dia,
                r.rv_estado
            FROM fa_ws_rutaVendedor r
            INNER JOIN ve_ws_clienteAsignadoVendedor c
                ON r.cl_codigo = c.cl_codigo
            WHERE r.rv_visita = 0
              AND r.rv_cobro = 0
              AND r.rv_venta = 0
              AND r.rv_estado = 'P'
              AND TRIM(r.rv_dia) IN ($placeholders)
              AND substr(r.rv_fechainicial, 1, 10) <= ?
              AND substr(r.rv_fechafinal, 1, 10) >= ?
            ORDER BY
                CASE TRIM(r.rv_dia)
                    WHEN 'Lunes' THEN 1
                    WHEN 'Martes' THEN 2
                    WHEN 'Miércoles' THEN 3
                    WHEN 'Jueves' THEN 4
                    WHEN 'Viernes' THEN 5
                    ELSE 99
                END,
                r.rv_linea DESC
            """.trimIndent(),
                arrayOf(
                    *diasPendientes.toTypedArray(),
                    fechaFin,
                    fechaIni
                )
            ).use { cursor ->

                if (cursor.moveToFirst()) {
                    var contador = 1

                    do {
                        val nombre = cursor.getString(cursor.getColumnIndexOrThrow("cl_nombre")) ?: ""
                        val codigo = cursor.getString(cursor.getColumnIndexOrThrow("cl_codigo")) ?: ""
                        val saldo = cursor.getDouble(cursor.getColumnIndexOrThrow("rv_monto"))
                        val ciudad = cursor.getString(cursor.getColumnIndexOrThrow("ci_descripcion")) ?: ""
                        val dia = cursor.getString(cursor.getColumnIndexOrThrow("rv_dia")) ?: ""
                        val observacion = cursor.getString(cursor.getColumnIndexOrThrow("rv_observacion")) ?: ""
                        val cupo = cursor.getDouble(cursor.getColumnIndexOrThrow("cc_cupoasignado"))
                        val estado = cursor.getString(cursor.getColumnIndexOrThrow("rv_estado")) ?: ""

                        val isCheckedVisita =
                            cursor.getInt(cursor.getColumnIndexOrThrow("rv_visita")) == 1

                        val isCheckedCobro =
                            cursor.getInt(cursor.getColumnIndexOrThrow("rv_cobro")) == 1

                        val isCheckedVenta =
                            cursor.getInt(cursor.getColumnIndexOrThrow("rv_venta")) == 1

                        val isCheckedTelefono =
                            cursor.getInt(cursor.getColumnIndexOrThrow("rv_telefono")) == 1

                        val nuevoDetalle = AdaptadorRutas.Rutas(
                            codigo = codigo,
                            sec = contador.toString(),
                            cliente = nombre,
                            Saldo = "$" + String.format("%.2f", saldo),
                            Ciudad = ciudad,
                            Cupo = "$" + String.format("%.2f", cupo),
                            estado = estado,
                            distancia = dia,
                            observacion = observacion,
                            isSelectedVisita = isCheckedVisita,
                            isSelectedCobro = isCheckedCobro,
                            isSelectedVenta = isCheckedVenta,
                            isSelectedTelefono = isCheckedTelefono
                        )

                        clienteL.add(nuevoDetalle)
                        contador++

                    } while (cursor.moveToNext())
                }
            }

            db.setTransactionSuccessful()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (db.inTransaction()) {
                db.endTransaction()
            }

            DatabaseManager.closeDatabase()
        }

        return clienteL
    }


    fun fnInsertarNuevaVisita(
        codigo: String,
        fechaInc: String,
        fechaFin: String,
        dia: String
    ) {
        val db = DatabaseManager.openDatabase(context)

        try {
            db.beginTransaction()

            val existeEnSemana = db.rawQuery(
                """
            SELECT COUNT(*)
            FROM fa_ws_rutaVendedor
            WHERE cl_codigo = ?
              AND rv_estado <> 'C'
              AND substr(rv_fechainicial, 1, 10) <= ?
              AND substr(rv_fechafinal, 1, 10) >= ?
            """.trimIndent(),
                arrayOf(
                    codigo,
                    fechaFin,
                    fechaInc
                )
            ).use { cursor ->
                cursor.moveToFirst() && cursor.getInt(0) > 0
            }

            if (!existeEnSemana) {
                db.execSQL(
                    """
                INSERT INTO fa_ws_rutaVendedor (
                    rv_fechainicial,
                    rv_fechafinal,
                    vn_codigo,
                    cl_codigo,
                    rv_linea,
                    rv_zona,
                    rv_monto,
                    rv_tipo,
                    rv_distancia,
                    rv_dia,
                    rv_visita,
                    rv_cobro,
                    rv_venta,
                    cc_cupoasignado,
                    cc_cupodisponible,
                    rv_estado,
                    rv_telefono,
                    rv_observacion
                )
                VALUES (
                    ? || 'T00:00:00-05:00',
                    ? || 'T00:00:00-05:00',

                    (
                        SELECT vn_codigo
                        FROM fa_ws_rutaVendedor
                        WHERE rv_estado <> 'C'
                          AND substr(rv_fechainicial, 1, 10) <= ?
                          AND substr(rv_fechafinal, 1, 10) >= ?
                        ORDER BY rv_linea
                        LIMIT 1
                    ),

                    ?,

                    0,

                    (
                        SELECT pr_codigo
                        FROM ve_ws_clienteAsignadoVendedor
                        WHERE cl_codigo = ?
                        LIMIT 1
                    ),

                    0.00,
                    0,
                    '0 km',
                    ?,
                    0,
                    0,
                    0,
                    0,
                    0,
                    'N',
                    0,
                    ''
                )
                """.trimIndent(),
                    arrayOf(
                        fechaInc,
                        fechaFin,

                        fechaFin,
                        fechaInc,

                        codigo,
                        codigo,
                        dia
                    )
                )
            }

            db.setTransactionSuccessful()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (db.inTransaction()) {
                db.endTransaction()
            }

            DatabaseManager.closeDatabase()
        }
    }

    fun fnObtenerRutasNuevas(
        fechaInc: String,
        fechaFin: String
    ): List<AdaptadorRutaNueva.RutaNueva> {

        val clienteL = mutableListOf<AdaptadorRutaNueva.RutaNueva>()
        val db = DatabaseManager.openDatabase(context)

        try {
            db.rawQuery(
                """
            SELECT 
                r.cl_codigo, 
                c.cl_nombre, 
                c.ci_descripcion,
                r.rv_distancia,
                r.rv_visita,
                r.rv_cobro,
                r.rv_venta,
                r.rv_telefono,
                r.rv_dia,
                r.rv_observacion,
                r.rv_estado
            FROM fa_ws_rutaVendedor r
            LEFT JOIN ve_ws_clienteAsignadoVendedor c 
                ON r.cl_codigo = c.cl_codigo
            WHERE r.rv_estado = 'N'
              AND substr(r.rv_fechainicial, 1, 10) <= ?
              AND substr(r.rv_fechafinal, 1, 10) >= ?
            ORDER BY 
                CASE TRIM(r.rv_dia)
                    WHEN 'Lunes' THEN 1
                    WHEN 'Martes' THEN 2
                    WHEN 'Miércoles' THEN 3
                    WHEN 'Jueves' THEN 4
                    WHEN 'Viernes' THEN 5
                    ELSE 99
                END,
                r.cl_codigo
            """.trimIndent(),
                arrayOf(
                    fechaFin,
                    fechaInc
                )
            ).use { cursor ->

                if (cursor.moveToFirst()) {
                    var contador = 1

                    do {
                        val nombre = cursor.getString(cursor.getColumnIndexOrThrow("cl_nombre")) ?: ""
                        val codigo = cursor.getString(cursor.getColumnIndexOrThrow("cl_codigo")) ?: ""
                        val ciudad = cursor.getString(cursor.getColumnIndexOrThrow("ci_descripcion")) ?: ""
                        val observacion = cursor.getString(cursor.getColumnIndexOrThrow("rv_observacion")) ?: ""
                        val dia = cursor.getString(cursor.getColumnIndexOrThrow("rv_dia")) ?: ""
                        val estado = cursor.getString(cursor.getColumnIndexOrThrow("rv_estado")) ?: ""

                        val checkVisita =
                            cursor.getInt(cursor.getColumnIndexOrThrow("rv_visita")) == 1

                        val checkCobro =
                            cursor.getInt(cursor.getColumnIndexOrThrow("rv_cobro")) == 1

                        val checkVenta =
                            cursor.getInt(cursor.getColumnIndexOrThrow("rv_venta")) == 1

                        val checkTelefono =
                            cursor.getInt(cursor.getColumnIndexOrThrow("rv_telefono")) == 1

                        val nuevoDetalle = AdaptadorRutaNueva.RutaNueva(
                            codigo = codigo,
                            sec = contador.toString(),
                            cliente = nombre,
                            Ciudad = ciudad,
                            dia = dia,
                            observacion = observacion,
                            estado = estado,
                            isSelectedVisita = checkVisita,
                            isSelectedCobro = checkCobro,
                            isSelectedVenta = checkVenta,
                            isSelectedTelefono = checkTelefono
                        )

                        clienteL.add(nuevoDetalle)
                        contador++

                    } while (cursor.moveToNext())
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            DatabaseManager.closeDatabase()
        }

        return clienteL
    }


    fun fnRutaVisita(
        cl_codigo: String,
        check: Int,
        estado: String
    ): Boolean {
        val db = DatabaseManager.openDatabase(context)
        val actual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Crear los valores a actualizar
        val contentValues = ContentValues().apply {
            put("rv_visita", check)  // Estado de la visita
            put("rv_fechavisita", actual)  // Fecha actual
            if (estado == "P"){
                put("rv_estado", "N")
            }
        }

        return try {
            // Intentar actualizar el registro
            val rowsAffected = db.update(
                "fa_ws_rutaVendedor", // Nombre de la tabla
                contentValues,        // Valores a actualizar
                "cl_codigo = ?",      // Condición WHERE
                arrayOf(cl_codigo)    // Parámetro para la condición
            )

            rowsAffected > 0 // Retorna `true` si al menos una fila fue actualizada
        } catch (e: Exception) {
            //Log.e("DatabaseError", "Error al actualizar la base de datos: ${e.message}")
            false
        } finally {
            DatabaseManager.closeDatabase() // Asegura el cierre de la base de datos
        }
    }

    fun fnRutaCobro(
        cl_codigo: String,
        check: Int,
        estado: String
    ): Boolean {
        val db = DatabaseManager.openDatabase(context)
        val actual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Crear los valores a insertar
        val contentValues = ContentValues().apply {
            put("rv_cobro", check)       // Check
            put("rv_fechacobro", actual)      // Fecha actual
            if (estado == "P"){
                put("rv_estado", "N")
            }
        }

        return try {
            // Intentar actualizar el registro
            val rowsAffected = db.update(
                "fa_ws_rutaVendedor", // Nombre de la tabla
                contentValues,        // Valores a actualizar
                "cl_codigo = ?",      // Condición WHERE
                arrayOf(cl_codigo)    // Parámetro para la condición
            )

            rowsAffected > 0 // Retorna `true` si al menos una fila fue actualizada
        } catch (e: Exception) {
            //Log.e("DatabaseError", "Error al actualizar la base de datos: ${e.message}")
            false
        } finally {
            DatabaseManager.closeDatabase() // Asegura el cierre de la base de datos
        }
    }

    fun fnRutaVenta(
        cl_codigo: String,
        check: Int,
        estado: String
    ): Boolean {
        val db = DatabaseManager.openDatabase(context)
        val actual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Crear los valores a insertar
        val contentValues = ContentValues().apply {
            put("rv_venta", check)       // Check
            put("rv_fechaventa", actual)      // Fecha actual
            if (estado == "P"){
                put("rv_estado", "N")
            }

        }

        return try {
            // Intentar actualizar el registro
            val rowsAffected = db.update(
                "fa_ws_rutaVendedor", // Nombre de la tabla
                contentValues,        // Valores a actualizar
                "cl_codigo = ?",      // Condición WHERE
                arrayOf(cl_codigo)    // Parámetro para la condición
            )

            rowsAffected > 0 // Retorna `true` si al menos una fila fue actualizada
        } catch (e: Exception) {
            //Log.e("DatabaseError", "Error al actualizar la base de datos: ${e.message}")
            false
        } finally {
            DatabaseManager.closeDatabase() // Asegura el cierre de la base de datos
        }
    }

    fun fnRutaGT(
        cl_codigo: String,
        check: Int,
        estado: String
    ): Boolean {
        val db = DatabaseManager.openDatabase(context)
        val actual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Crear los valores a insertar
        val contentValues = ContentValues().apply {
            put("rv_telefono", check)       // Check
            put("rv_fechatelefono", actual)      // Fecha actual
            if (estado == "P"){
                put("rv_estado", "N")
            }

        }

        return try {
            // Intentar actualizar el registro
            val rowsAffected = db.update(
                "fa_ws_rutaVendedor", // Nombre de la tabla
                contentValues,        // Valores a actualizar
                "cl_codigo = ?",      // Condición WHERE
                arrayOf(cl_codigo)    // Parámetro para la condición
            )

            rowsAffected > 0 // Retorna `true` si al menos una fila fue actualizada
        } catch (e: Exception) {
            //Log.e("DatabaseError", "Error al actualizar la base de datos: ${e.message}")
            false
        } finally {
            DatabaseManager.closeDatabase() // Asegura el cierre de la base de datos
        }
    }


    fun fnObtenerDia(): List<String> {
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery(
            """
                SELECT DISTINCT rv_dia 
                FROM fa_ws_rutaVendedor
                WHERE rv_dia = 
                    CASE strftime('%w', 'now', 'localtime')
                        WHEN '1' THEN 'Lunes'
                        WHEN '2' THEN 'Martes'
                        WHEN '3' THEN 'Miercoles'
                        WHEN '4' THEN 'Jueves'
                        WHEN '5' THEN 'Viernes'
                        WHEN '6' THEN 'Viernes'
                        WHEN '7' THEN 'Viernes'
                        WHEN '0' THEN 'Lunes'
                    END

                """.trimIndent(), null
        )
        val listaDias = mutableListOf<String>()

        if (cursor.moveToFirst()) {
            do {
                val dia = cursor.getString(cursor.getColumnIndexOrThrow("rv_dia"))
                listaDias.add(dia)
            } while (cursor.moveToNext())
        }

        cursor.close()
        DatabaseManager.closeDatabase()

        return listaDias
    }

    fun fnObtenerDiasDisponibles(
        fechaIni: String,
        fechaFin: String
    ): List<String> {

        val db = DatabaseManager.openDatabase(context)
        val listaDias = mutableListOf<String>()

        try {
            db.rawQuery(
                """
            SELECT DISTINCT TRIM(rv_dia) AS rv_dia
            FROM fa_ws_rutaVendedor
            WHERE rv_estado <> 'C'
              AND TRIM(rv_dia) IN ('Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes')
              AND substr(rv_fechainicial, 1, 10) <= ?
              AND substr(rv_fechafinal, 1, 10) >= ?
            ORDER BY 
                CASE TRIM(rv_dia)
                    WHEN 'Lunes' THEN 1
                    WHEN 'Martes' THEN 2
                    WHEN 'Miércoles' THEN 3
                    WHEN 'Jueves' THEN 4
                    WHEN 'Viernes' THEN 5
                    ELSE 99
                END
            """.trimIndent(),
                arrayOf(
                    fechaFin,
                    fechaIni
                )
            ).use { cursor ->

                if (cursor.moveToFirst()) {
                    do {
                        val dia = cursor.getString(cursor.getColumnIndexOrThrow("rv_dia")) ?: ""

                        if (dia.isNotEmpty()) {
                            listaDias.add(dia)
                        }

                    } while (cursor.moveToNext())
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            DatabaseManager.closeDatabase()
        }

        return listaDias
    }



    fun fnFechaRuta(): Fechas? {
        val db = DatabaseManager.openDatabase(context)
        var result: Fechas? = null
        val cursor = db.rawQuery(   """
       SELECT DISTINCT 
                strftime('%d/%m/%Y', rv_fechainicial) AS rv_fechainicial, 
                 strftime('%d/%m/%Y', rv_fechafinal) AS rv_fechafinal
        FROM  fa_ws_rutaVendedor WHERE rv_estado = 'A' """.trimIndent(), null     )
        if (cursor.moveToFirst()) {
            val fechaInc = cursor.getString(cursor.getColumnIndexOrThrow("rv_fechainicial"))
            val fechaFin = cursor.getString(cursor.getColumnIndexOrThrow("rv_fechafinal"))

            // Asignar los resultados a la variable result
            result = Fechas(fechaInc, fechaFin)
        }

        cursor.close()
        DatabaseManager.closeDatabase()

        return result
    }


    fun fnClienteRutaNueva(context: Context, dia: String, fechaInc: String, fechaFin: String): List<Pair<String, String>> {
        val db = DatabaseManager.openDatabase(context)

        val query = """
        SELECT DISTINCT c.cl_codigo, c.cl_nombre
        FROM ve_ws_clienteAsignadoVendedor c
        LEFT JOIN fa_ws_rutaVendedor r 
            ON r.cl_codigo = c.cl_codigo
            AND date(r.rv_fechainicial) = date(substr(?, 7, 4) || '-' || substr(?, 4, 2) || '-' || substr(?, 1, 2))
            AND date(r.rv_fechafinal) = date(substr(?, 7, 4) || '-' || substr(?, 4, 2) || '-' || substr(?, 1, 2))
        WHERE 
            (
                r.cl_codigo IS NULL  -- nunca agregado
                OR (r.rv_dia <> ? AND r.rv_estado NOT IN ('P', 'N'))  -- agregado en otro día, pero válido
            )
    """.trimIndent()

        // Pasar parámetros: fechaInc, fechaInc, fechaInc (para rv_fechainicial),
        //                  fechaFin, fechaFin, fechaFin (para rv_fechafinal), dia
        val cursor = db.rawQuery(
            query,
            arrayOf(
                fechaInc, fechaInc, fechaInc,  // para rv_fechainicial
                fechaFin, fechaFin, fechaFin,  // para rv_fechafinal
                dia                            // para rv_dia
            )
        )

        val listaClientes = mutableListOf<Pair<String, String>>()

        if (cursor.moveToFirst()) {
            do {
                val codigo = cursor.getString(cursor.getColumnIndexOrThrow("cl_codigo"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("cl_nombre"))
                listaClientes.add(Pair(codigo, nombre))
            } while (cursor.moveToNext())
        }

        cursor.close()
        DatabaseManager.closeDatabase()

        return listaClientes
    }


    fun fnElininarRutasNuevas(
        context: Context,
        codigo: String,
        fechaInc: String,
        fechaFin: String
    ) {
        val db = DatabaseManager.openDatabase(context)

        val fechaIniSql = fechaInc.trim().take(10)
        val fechaFinSql = fechaFin.trim().take(10)

        try {
            db.beginTransaction()

            val cursorLinea = db.rawQuery(
                """
            SELECT 
                rv_linea,
                rv_visita,
                rv_cobro,
                rv_venta,
                rv_telefono,
                rv_estado
            FROM fa_ws_rutaVendedor
            WHERE cl_codigo = ?
              AND rv_estado = 'N'
              AND substr(rv_fechainicial, 1, 10) <= ?
              AND substr(rv_fechafinal, 1, 10) >= ?
            LIMIT 1
            """.trimIndent(),
                arrayOf(
                    codigo,
                    fechaFinSql,
                    fechaIniSql
                )
            )

            var rvLinea = -1
            var hayCheck = false

            if (cursorLinea.moveToFirst()) {
                rvLinea = cursorLinea.getInt(cursorLinea.getColumnIndexOrThrow("rv_linea"))

                val visita = cursorLinea.getInt(cursorLinea.getColumnIndexOrThrow("rv_visita"))
                val cobro = cursorLinea.getInt(cursorLinea.getColumnIndexOrThrow("rv_cobro"))
                val venta = cursorLinea.getInt(cursorLinea.getColumnIndexOrThrow("rv_venta"))
                val telefono = cursorLinea.getInt(cursorLinea.getColumnIndexOrThrow("rv_telefono"))

                hayCheck = visita == 1 || cobro == 1 || venta == 1 || telefono == 1
            }

            cursorLinea.close()

            if (rvLinea == -1) {
                db.setTransactionSuccessful()
                return
            }

            if (rvLinea == 0) {
                // Fue una visita nueva insertada manualmente.
                // Se elimina completamente.
                db.execSQL(
                    """
                DELETE FROM fa_ws_rutaVendedor
                WHERE cl_codigo = ?
                  AND rv_estado = 'N'
                  AND rv_linea = 0
                  AND substr(rv_fechainicial, 1, 10) <= ?
                  AND substr(rv_fechafinal, 1, 10) >= ?
                """.trimIndent(),
                    arrayOf(
                        codigo,
                        fechaFinSql,
                        fechaIniSql
                    )
                )
            } else {
                // Fue una ruta existente que pasó a N.
                // Si tiene gestión, vuelve a A.
                // Si no tiene gestión, vuelve a P.
                if (hayCheck) {
                    db.execSQL(
                        """
                    UPDATE fa_ws_rutaVendedor
                    SET rv_estado = 'A'
                    WHERE cl_codigo = ?
                      AND rv_estado = 'N'
                      AND substr(rv_fechainicial, 1, 10) <= ?
                      AND substr(rv_fechafinal, 1, 10) >= ?
                    """.trimIndent(),
                        arrayOf(
                            codigo,
                            fechaFinSql,
                            fechaIniSql
                        )
                    )
                } else {
                    db.execSQL(
                        """
                    UPDATE fa_ws_rutaVendedor
                    SET rv_estado = 'P',
                        rv_visita = 0,
                        rv_cobro = 0,
                        rv_venta = 0,
                        rv_telefono = 0
                    WHERE cl_codigo = ?
                      AND rv_estado = 'N'
                      AND substr(rv_fechainicial, 1, 10) <= ?
                      AND substr(rv_fechafinal, 1, 10) >= ?
                    """.trimIndent(),
                        arrayOf(
                            codigo,
                            fechaFinSql,
                            fechaIniSql
                        )
                    )
                }
            }

            db.setTransactionSuccessful()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (db.inTransaction()) {
                db.endTransaction()
            }

            DatabaseManager.closeDatabase()
        }
    }



    fun fnRutaObservacion(cl_codigo: String, observacion: String): Boolean {
        val db = DatabaseManager.openDatabase(context)
        return try {
            val contentValues = ContentValues().apply {
                put("rv_observacion", observacion)
            }

            // Actualiza la tabla usando `cl_codigo` como filtro
            val rowsUpdated = db.update(
                "fa_ws_rutaVendedor", // Nombre de la tabla
                contentValues,
                "cl_codigo = ?", // Condición WHERE
                arrayOf(cl_codigo) // Parámetro para evitar SQL Injection
            )

            rowsUpdated > 0 // Devuelve `true` si se actualizó al menos una fila
        } catch (e: Exception) {
            e.printStackTrace()
            false // Devuelve `false` si ocurre un error
        } finally {
            DatabaseManager.closeDatabase()
        }
    }


    fun fnReporteRuta(fechaIni: String, fechaFin: String): List<RutaVendedorData> {
        val db = DatabaseManager.openDatabase(context)
        val listaDatos = mutableListOf<RutaVendedorData>()

        val fechaIniSql = fechaIni.trim().take(10)
        val fechaFinSql = fechaFin.trim().take(10)

        val query = """
        SELECT 
            UPPER(TRIM(rv_dia)) AS Dia, 
            c.cl_nombre, 
            UPPER(c.ci_descripcion) AS Ciudad, 
            r.rv_fechavisita, 
            r.rv_fechaventa, 
            r.rv_fechacobro, 
            UPPER(IFNULL(r.rv_observacion, '')) AS Observacion, 
            r.rv_fechatelefono
        FROM fa_ws_rutaVendedor r
        INNER JOIN ve_ws_clienteAsignadoVendedor c 
            ON r.cl_codigo = c.cl_codigo
        WHERE r.rv_estado <> 'C'
          AND substr(r.rv_fechainicial, 1, 10) <= ?
          AND substr(r.rv_fechafinal, 1, 10) >= ?
        ORDER BY 
            CASE TRIM(r.rv_dia)
                WHEN 'Lunes' THEN 1
                WHEN 'Martes' THEN 2
                WHEN 'Miércoles' THEN 3
                WHEN 'Miercoles' THEN 3
                WHEN 'Jueves' THEN 4
                WHEN 'Viernes' THEN 5
                ELSE 99
            END,
            r.rv_linea
    """.trimIndent()

        val cursor = db.rawQuery(
            query,
            arrayOf(
                fechaFinSql,
                fechaIniSql
            )
        )

        if (cursor.moveToFirst()) {
            do {
                val dia = cursor.getString(0) ?: ""
                val cliente = cursor.getString(1) ?: ""
                val descripcion = cursor.getString(2) ?: ""
                val fechaVisita = cursor.getString(3)
                val fechaVenta = cursor.getString(4)
                val fechaCobro = cursor.getString(5)
                val observacion = cursor.getString(6) ?: ""
                val fechaTelefono = cursor.getString(7)

                val visita = fnObtenerInicialDia(fechaVisita)
                val venta = fnObtenerInicialDia(fechaVenta)
                val cobro = fnObtenerInicialDia(fechaCobro)
                val telefono = fnObtenerInicialDia(fechaTelefono)

                listaDatos.add(
                    RutaVendedorData(
                        dia,
                        cliente,
                        descripcion,
                        visita,
                        venta,
                        cobro,
                        telefono,
                        observacion
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        DatabaseManager.closeDatabase()

        return listaDatos
    }

    private fun fnObtenerInicialDia(fecha: String?): String {
        if (fecha.isNullOrBlank()) return ""

        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = formato.parse(fecha)
            val calendar = Calendar.getInstance().apply { time = date!! }
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            return when (dayOfWeek) {
                Calendar.SUNDAY -> "D"
                Calendar.MONDAY -> "L"
                Calendar.TUESDAY -> "M"
                Calendar.WEDNESDAY -> "X" // Aquí colocas la "X" especial para miércoles
                Calendar.THURSDAY -> "J"
                Calendar.FRIDAY -> "V"
                Calendar.SATURDAY -> "S"
                else -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }



    // Función para formatear el saldo como moneda
    fun fnFormatearSaldo(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        return formatter.format(amount)
    }

    fun fnEliminarCheck(codigo: String) {
        val db = DatabaseManager.openDatabase(context)

        try {
            val whereClause = "rv_estado='E'" // Cláusula WHERE para identificar el registro
            val whereArgs = arrayOf(codigo) // Argumentos para la cláusula WHERE

            // Realiza el DELETE en la tabla
            val rowsDeleted = db.delete("ve_ws_rutavendedor", whereClause, whereArgs)

            if (rowsDeleted > 0) {
               // println("Registro con Código $codigo eliminado exitosamente.")
            } else {
                //println("No se encontró un registro con Código $codigo para eliminar.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //println("Error al eliminar el registro: ${e.message}")
        } finally {
            DatabaseManager.closeDatabase() // Cierra la conexión a la base de datos
        }
    }


    fun fnCambioEstado(codigo: String) {
        val db = DatabaseManager.openDatabase(context)

        try {
            // Crear los valores a actualizar
            val contentValues = ContentValues().apply {
                put("rv_estado", "E") // Cambiar el estado a 'E'
            }

            // Cláusula WHERE para identificar el registro
            val whereClause = "cl_codigo = ?"
            val whereArgs = arrayOf(codigo)

            // Realiza el UPDATE en la tabla
            val rowsUpdated = db.update(
                "ve_ws_rutavendedor", // Nombre de la tabla
                contentValues,        // Valores a actualizar
                whereClause,          // Cláusula WHERE
                whereArgs             // Argumentos para la cláusula WHERE
            )

        } catch (e: Exception) {
            Log.e("fnCambioEstado", "Error al actualizar el estado: ${e.message}")
        } finally {
            DatabaseManager.closeDatabase() // Cierra la conexión a la base de datos
        }
    }

    fun fnObtenerReferenciaPorCodigo(codigo: String): String {
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery(
            "SELECT it_referencia FROM ve_ws_item WHERE it_codigo = ?",
            arrayOf(codigo)
        )
        var referenciaCompleta = ""
        if (cursor.moveToFirst()) {
            referenciaCompleta = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return referenciaCompleta
    }


    fun fnCopiarDatosDesdeRutas(): Boolean {
        val tablaOrigen = "ve_ws_rutas"
        val tablaDestino = "ve_ws_rutavendedor"

        // Columnas específicas de origen y destino
        val columnasOrigen = listOf("cl_codigo", "rv_latitud", "rv_longitud", "'A'", "'0'", "'1'")
        val columnasDestino = listOf("cl_codigo", "rv_latitud", "rv_longitud", "rv_estado", "rv_timestamp", "rv_check")

        val db = DatabaseManager.openDatabase(context)

        return try {
            db.beginTransaction() // Inicia una transacción

            // Construir la consulta para copiar datos con estado diferente de 'C'
            val columnasDestinoSql = columnasDestino.joinToString(", ")
            val columnasOrigenSql = columnasOrigen.joinToString(", ")
            val queryInsertOrUpdate = """
            INSERT OR REPLACE INTO $tablaDestino ($columnasDestinoSql)
            SELECT $columnasOrigenSql 
            FROM $tablaOrigen
            WHERE rv_estado <> 'C'
        """

            // Ejecutar la consulta de inserción/actualización
            db.execSQL(queryInsertOrUpdate)

            // Actualizar los registros en la tabla origen a estado 'C'
            val queryUpdate = """
            UPDATE $tablaOrigen
            SET rv_estado = 'C'
            WHERE rv_estado <> 'C'
        """
            db.execSQL(queryUpdate)

            db.setTransactionSuccessful() // Marca la transacción como exitosa
            true // Retorna true si todo se ejecuta correctamente
        } catch (e: Exception) {
            Log.e("DatabaseError", "Error al copiar y actualizar datos: ${e.message}")
            false // Retorna false en caso de error
        } finally {
            db.endTransaction() // Finaliza la transacción
            DatabaseManager.closeDatabase() // Asegura el cierre de la base de datos
        }
    }


    fun fnObtenerOrden(cliente: String): Int {
        val db = DatabaseManager.openDatabase(context)
        var orden = 0

        db.rawQuery(
            "SELECT cl_orden FROM ve_ws_clienteAsignadoVendedor WHERE cl_codigo = ?",
            arrayOf(cliente) // Se pasa el parámetro correctamente
        ).use { cursor ->
            if (cursor.moveToFirst()) { // Verifica que el cursor tenga datos antes de acceder
                orden = cursor.getInt(cursor.getColumnIndexOrThrow("cl_orden"))
            }
        }

        DatabaseManager.closeDatabase()
        return orden
    }


     fun fnObtenerMargen():Double{
         val db = DatabaseManager.openDatabase(context)
         return db.rawQuery("SELECT mg_margen FROM vn_ws_margen", null).use { cursor ->
             cursor.moveToFirst()
             cursor.getDouble(cursor.getColumnIndexOrThrow("mg_margen"))
         }.also {
             DatabaseManager.closeDatabase()
         }
     }

    fun fnObtenerValorRegalo():Double{
        val db = DatabaseManager.openDatabase(context)
        return db.rawQuery("SELECT mg_regalo FROM vn_ws_margen", null).use { cursor ->
            cursor.moveToFirst()
            cursor.getDouble(cursor.getColumnIndexOrThrow("mg_regalo"))?:200.00
        }.also {
            DatabaseManager.closeDatabase()
        }
    }

    fun fnErrorDialog(context: Context, mensaje: String) {
        AlertDialog.Builder(context)
            .setTitle("Error en la aplicación")
            .setMessage(mensaje)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun obtenerDiaActual(): String {
        val formato = SimpleDateFormat("EEEE", Locale("es", "EC")) // Día en español completo
        val dia = formato.format(Date()) // Ej: "lunes"
        return dia.replaceFirstChar { it.uppercase() } // Resultado: "Lunes"
    }


      fun fnOtenerRutasPorDia(context: Context, dia: String): List<ClientesRuta> {
        val lista = mutableListOf<ClientesRuta>()
        try {
            val db = DatabaseManager.openDatabase(context)
            val cursor = db.rawQuery(
                """
            SELECT cl_nombre, cl_fono, cl_direccion, r.cl_latitud, r.cl_longitud
            FROM fa_ws_rutaVendedor r
            INNER JOIN ve_ws_clienteAsignadoVendedor c ON c.cl_codigo = r.cl_codigo
            WHERE r.cl_latitud IS NOT NULL AND r.cl_longitud IS NOT NULL
              AND rv_dia = ? AND rv_estado = 'A'
            ORDER BY rv_linea
            """.trimIndent(), arrayOf(dia)
            )

            var contador = 1
            if (cursor.moveToFirst()) {
                do {
                    val nombre = cursor.getString(0)
                    val fono = cursor.getString(1)
                    val direccion = cursor.getString(2)
                    val latitud = cursor.getDouble(3)
                    val longitud = cursor.getDouble(4)
                    lista.add(ClientesRuta(nombre, fono, direccion, latitud, longitud, contador++))
                } while (cursor.moveToNext())
            }

            cursor.close()
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return lista
    }


     fun fnObtenerVentasAnuales(cliente: String, item: String): List<VentaAnual> {
        val lista = mutableListOf<VentaAnual>()
        val db = DatabaseManager.openDatabase(context)

        try {
            val query = """
            SELECT 
                it_referencia,
                vn_anio AS anio,
                SUM(ve_cantidad) AS total_cantidad,
                SUM(ROUND(CAST(ve_preciovta AS REAL) * ve_cantidad, 2)) AS total_venta
            FROM fa_ws_ventas v
            inner join ve_ws_item i on i.it_codigo=v.it_codigo
            WHERE cl_codigo = ? AND v.it_codigo = ?
            GROUP BY vn_anio
            ORDER BY anio
        """

            val cursor = db.rawQuery(query, arrayOf(cliente, item))

            if (cursor.moveToFirst()) {
                do {
                    val referencia = cursor.getString(0) // it_referencia
                    val anio = cursor.getInt(1)          // vn_anio
                    val cantidad = cursor.getInt(2)      // total_cantidad
                    val totalVenta = cursor.getString(3) // total_venta
                    lista.add(VentaAnual(referencia, anio, cantidad, totalVenta))

                    // Aquí puedes usar la referencia para el título si lo necesitas:
                    // txtTitulo.text = "Historial de: $referencia"

                } while (cursor.moveToNext())
            }


            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            DatabaseManager.closeDatabase()
        }

        return lista
    }

    fun fnObtenerHistorial(cliente: String?, item: String?): List<HistorialResumen> {
        val lista = mutableListOf<HistorialResumen>()
        val db = DatabaseManager.openDatabase(context)

        try {
            // 🔥 Años dinámicos
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val años = (2019..currentYear)
            val selectAños = años.joinToString(",\n") { año ->
                "SUM(CASE WHEN vn_anio = $año THEN ve_cantidad ELSE 0 END) AS cantidad_$año"
            }

            // 🔥 JOIN dinámico
            val joins = """
            INNER JOIN ve_ws_clienteAsignadoVendedor c ON c.cl_codigo = v.cl_codigo
            INNER JOIN ve_ws_item i ON i.it_codigo = v.it_codigo
        """

            // 🔥 WHERE dinámico
            val whereClauses = mutableListOf<String>()
            val args = mutableListOf<String>()

            if (!cliente.isNullOrEmpty()) {
                whereClauses.add("c.cl_codigo = ?")
                args.add(cliente)
            }
            if (!item.isNullOrEmpty()) {
                whereClauses.add("v.it_codigo = ?")
                args.add(item)
            }

            val whereSQL = if (whereClauses.isNotEmpty()) {
                "WHERE ${whereClauses.joinToString(" AND ")}"
            } else {
                ""
            }

            // 🔥 SELECT dinámico según filtro
            val selectNombre = if (!cliente.isNullOrEmpty() && item.isNullOrEmpty()) {
                "i.it_referencia AS nombre" // Si solo se consulta por cliente, mostrar items
            } else {
                "c.cl_nombre AS nombre" // Caso contrario, mostrar cliente
            }

            // 🔥 GROUP BY dinámico según filtro
            val groupBy = if (!cliente.isNullOrEmpty() && item.isNullOrEmpty()) {
                "GROUP BY i.it_referencia"
            } else {
                "GROUP BY c.cl_nombre"
            }

            // 🔥 Consulta principal
            val query = """
            SELECT 
                $selectNombre,
                $selectAños
            FROM fa_ws_ventas v
            $joins
            $whereSQL
            $groupBy
            ORDER BY nombre
        """

            val cursor = db.rawQuery(query, args.toTypedArray())

            if (cursor.moveToFirst()) {
                do {
                    val nombre = cursor.getString(0)
                    val cantidades = (1..años.count()).map { index -> cursor.getInt(index) }
                    lista.add(HistorialResumen(nombre, cantidades))
                } while (cursor.moveToNext())
            }
            cursor.close()

            // 🔥 Fila TOTAL
            val totalQuery = """
            SELECT 
                'TOTAL',
                $selectAños
            FROM fa_ws_ventas v
            $joins
            $whereSQL
        """
            val totalCursor = db.rawQuery(totalQuery, args.toTypedArray())
            if (totalCursor.moveToFirst()) {
                val totalNombre = totalCursor.getString(0)
                val totalCantidades = (1..años.count()).map { index -> totalCursor.getInt(index) }
                lista.add(HistorialResumen(totalNombre, totalCantidades))
            }
            totalCursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            DatabaseManager.closeDatabase()
        }

        return lista
    }

    fun fnObtenerHistorialWeb(cliente: String?, item: String?): List<HistorialResumen> {
        val lista = mutableListOf<HistorialResumen>()
        val db = DatabaseManager.openDatabase(context)

        try {
            // 🔥 Años dinámicos
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val años = (2019..currentYear)
            val selectAños = años.joinToString(",\n") { año ->
                "SUM(CASE WHEN vn_anio = $año THEN ve_cantidad ELSE 0 END) AS cantidad_$año"
            }

            // 🔥 JOIN dinámico
            val joins = """
            INNER JOIN ve_ws_clienteAsignadoVendedor c ON c.cl_codigo = v.cl_codigo
            INNER JOIN ve_ws_item i ON i.it_codigo = v.it_codigo
        """

            // 🔥 WHERE dinámico
            val whereClauses = mutableListOf<String>()
            val args = mutableListOf<String>()

            if (!cliente.isNullOrEmpty()) {
                whereClauses.add("c.cl_codigo = ?")
                args.add(cliente)
            }
            if (!item.isNullOrEmpty()) {
                whereClauses.add("v.it_codigo = ?")
                args.add(item)
            }

            val whereSQL = if (whereClauses.isNotEmpty()) {
                "WHERE ${whereClauses.joinToString(" AND ")}"
            } else {
                ""
            }

            // 🔥 SELECT dinámico según filtro
            val selectNombre = if (!cliente.isNullOrEmpty() && item.isNullOrEmpty()) {
                "i.it_referencia AS nombre" // Si solo se consulta por cliente, mostrar items
            } else {
                "c.cl_nombre AS nombre" // Caso contrario, mostrar cliente
            }

            // 🔥 GROUP BY dinámico según filtro
            val groupBy = if (!cliente.isNullOrEmpty() && item.isNullOrEmpty()) {
                "GROUP BY i.it_referencia"
            } else {
                "GROUP BY c.cl_nombre"
            }

            // 🔥 Consulta principal
            val query = """
            SELECT 
                $selectNombre,
                $selectAños
            FROM fa_ws_ventasTmp v
            $joins
            $whereSQL
            $groupBy
            ORDER BY nombre
        """

            val cursor = db.rawQuery(query, args.toTypedArray())

            if (cursor.moveToFirst()) {
                do {
                    val nombre = cursor.getString(0)
                    val cantidades = (1..años.count()).map { index -> cursor.getInt(index) }
                    lista.add(HistorialResumen(nombre, cantidades))
                } while (cursor.moveToNext())
            }
            cursor.close()

            // 🔥 Fila TOTAL
            val totalQuery = """
            SELECT 
                'TOTAL',
                $selectAños
            FROM fa_ws_ventasTmp v
            $joins
            $whereSQL
        """
            val totalCursor = db.rawQuery(totalQuery, args.toTypedArray())
            if (totalCursor.moveToFirst()) {
                val totalNombre = totalCursor.getString(0)
                val totalCantidades = (1..años.count()).map { index -> totalCursor.getInt(index) }
                lista.add(HistorialResumen(totalNombre, totalCantidades))
            }
            totalCursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            DatabaseManager.closeDatabase()
        }

        return lista
    }


    fun fnObtenerMaxioDocumentoProforma(): Int {
        val db = DatabaseManager.openDatabase(context)
        var maxCodigo = 1
        db.rawQuery("SELECT MAX(pr_codigo) FROM fa_ws_cabproforma", null).use { cursor ->
            if (cursor.moveToFirst()) {
                val maxCodigoFromDb = cursor.getInt(0)
                if (maxCodigoFromDb != 0) {
                    maxCodigo = maxCodigoFromDb + 1
                }
            }
        }
        DatabaseManager.closeDatabase()
        return maxCodigo
    }


    fun fnLLenarSpinnerBodega(spinner: Spinner, tabla: String) {
        val db = DatabaseManager.openDatabase(context)
        db.rawQuery("SELECT bo_codigo, COALESCE(bo_descripcion,'(Sin descripción)') FROM $tabla", null).use { cursor ->
            val items = mutableListOf<SpinnerItem>()
            while (cursor.moveToNext()) {
                val id = cursor.getString(0) ?: continue
                val label = cursor.getString(1) ?: "(Sin descripción)"
                items.add(SpinnerItem(id, label))
            }

            val adapter = object : ArrayAdapter<SpinnerItem>(
                context,
                android.R.layout.simple_spinner_item,
                items
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getView(position, convertView, parent) as TextView
                    v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)   // tamaño del seleccionado
                    return v
                }
                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getDropDownView(position, convertView, parent) as TextView
                    v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)   // tamaño en el desplegable
                    return v
                }
            }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        DatabaseManager.closeDatabase()
    }

    fun fnLLenarSpinnerFactura(spinner: Spinner, tabla: String) {
        val db = DatabaseManager.openDatabase(context)
        db.rawQuery("SELECT bo_codigo, bo_descripcion $tabla", null).use { cursor ->
            val items = mutableListOf<SpinnerItem>()
            while (cursor.moveToNext()) {
                items.add(SpinnerItem(cursor.getString(0), cursor.getString(1)))
            }
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        DatabaseManager.closeDatabase()
    }


    fun fnCargarDatosTransporteCliente(codigo: String): Transporte? {
        var transporte: Transporte? = null
        val db = DatabaseManager.openDatabase(context)

        try {
            db.rawQuery(
                "SELECT tr_codigo, tr_nombre FROM ve_ws_transporte WHERE tr_codigo = ? LIMIT 1",
                arrayOf(codigo)
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val idxCodigo = cursor.getColumnIndexOrThrow("tr_codigo")
                    val idxNombre = cursor.getColumnIndexOrThrow("tr_nombre")

                    transporte = Transporte(
                        cursor.getString(idxCodigo),
                        cursor.getString(idxNombre)
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("DB", "Error cargando transporte para código $codigo", e)
        } finally {
            DatabaseManager.closeDatabase()
        }

        return transporte
    }


    fun fnBuscaReferenciaYcombosEnlinea(referencia: String, bodega: String): List<datos> {
        val referencias = mutableListOf<datos>()
        val db = DatabaseManager.openDatabase(context)

        // Query con CASE según bodega y con parámetros seguros
        val sql = """
    SELECT 
        it_referencia, 
        it_codigo, 
        CASE 
            WHEN p.bod = '2'  THEN ROUND(it_teler + it_exhTele, 2)
            WHEN p.bod = '51' THEN ROUND(it_mmg + it_exhVmr, 2)
            ELSE ROUND(it_teler + it_mmg + it_mmq + it_exhTele + it_exhVmr, 2)
        END AS stock,
        ROUND(pv_preciosubdistrib, 3) AS pv_preciosubdistrib,
        ROUND(pv_precio5, 3)  AS pv_precio5,
        ROUND(pv_precio6, 3)  AS pv_precio6,
        ROUND(pv_precio7, 3)  AS pv_precio7,
        it_descripcion,
        um_pesoCE,
        it_costoprom,
        0 AS cb_codigo
    FROM ve_ws_itemTmp
    CROSS JOIN (SELECT ? AS bod) p
    WHERE it_referencia LIKE ?
    ORDER BY stock DESC
""".trimIndent()

        val args = arrayOf(bodega, "$referencia%")   // ← solo 2 parámetros


        val cursor = db.rawQuery(sql, args)

        if (cursor.moveToFirst()) {
            do {
                val nuevaReferencia = datos(
                    referencia = cursor.getString(cursor.getColumnIndexOrThrow("it_referencia")),
                    codigo     = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo")),
                    stock      = cursor.getString(cursor.getColumnIndexOrThrow("stock")),
                    precioSub  = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_preciosubdistrib")).toString(),
                    precioCont = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio5")).toString(),
                    precioCred = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio6")).toString(),
                    descripcion= cursor.getString(cursor.getColumnIndexOrThrow("it_descripcion")) ?: "",
                    unidadCE   = cursor.getString(cursor.getColumnIndexOrThrow("um_pesoCE")) ?: "0",
                    costoProm  = cursor.getDouble(cursor.getColumnIndexOrThrow("it_costoprom")).toString(),
                    combo      = cursor.getString(cursor.getColumnIndexOrThrow("cb_codigo")) ?: "0",
                    cd_codigo  = cursor.getString(cursor.getColumnIndexOrThrow("cb_codigo")) ?: "",
                    it_regalo  = "0",
                    pv_precio7 = cursor.getString(cursor.getColumnIndexOrThrow("pv_precio7")) ?: "0.00",
                )
                referencias.add(nuevaReferencia)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return referencias
    }


    fun fnGuardarDatosProforma(
        pr_codigo: Int,
        cabValues: ContentValues,
        detalleValuesList: List<ContentValues>,
        usuario: String
    ): Boolean {
        val db = DatabaseManager.openDatabase(context)
        var success = true

        try {
            db.beginTransaction()
            // Intentar actualizar la cabecera
            val affectedRows = db.update(
                "fa_ws_cabproforma",
                cabValues,
                "pr_codigo = ?",
                arrayOf(pr_codigo.toString())
            )

            if (affectedRows == 0) {
                // Si no se actualizó ninguna fila, insertar una nueva cabecera
                cabValues.put("pr_codigo", pr_codigo)
                if (db.insert("fa_ws_cabproforma", null, cabValues) == -1L) {
                    throw Exception("Error al insertar en la tabla de cabecera")
                }
            } else {
                // Si se actualizó, registrar usuario de modificación
                val updateUser = ContentValues().apply {
                    put("pr_usuariomod", usuario)
                }
                db.update(
                    "fa_ws_cabproforma",
                    updateUser,
                    "pr_codigo = ?",
                    arrayOf(pr_codigo.toString())
                )
            }

            // Borrar detalles existentes
            db.delete(
                "fa_ws_detproforma",
                "pr_codigo = ?",
                arrayOf(pr_codigo.toString())
            )

            // Insertar detalles actualizados
            for (detalleValues in detalleValuesList) {
                detalleValues.put("pr_codigo", pr_codigo)
                if (db.insert("fa_ws_detproforma", null, detalleValues) == -1L) {
                    throw Exception("Error al insertar en la tabla de detalles")
                }
            }

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            success = false

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Error")
            builder.setMessage("Ocurrió un error al guardar: ${e.message}")
            builder.setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss() // Cerrar el diálogo
            }
            val dialog = builder.create()
            dialog.show()
            Log.e("ClsLLenarControles", "Error al actualizar pedido", e)
        } finally {
            db.endTransaction()
            DatabaseManager.closeDatabase()
        }

        return success
    }



    fun fnObtenerProformas(): List<ProformasDialogFragment.Proforma> {
        // Implementación para recuperar los pedidos desde una base de datos o fuente de datos
        val db = DatabaseManager.openDatabase(context)
        val pedidos = mutableListOf<ProformasDialogFragment.Proforma>()
        db.rawQuery("""
            SELECT 
                p.pr_codigo, 
                p.pr_nombre, 
                p.pr_valortotal, 
                p.pr_codigoA, 
                p.pr_estado, 
                pr_lote, 
                SUBSTR(p.pr_observacion, 1, 50) as pr_observacion,
                p.pr_fechaing,
                fa_coddocumento
            FROM fa_ws_cabproforma p
            WHERE  substr(p.pr_fechaing, 7, 4) || '-' || 
        substr(p.pr_fechaing, 4, 2) || '-' || 
        substr(p.pr_fechaing, 1, 2) >= date('now', '-1 month')
        AND p.pr_estado <> 'E'
        ORDER BY p.pr_codigo DESC
            
        """, null).use { cursor ->
            while (cursor.moveToNext()) {
                pedidos.add(
                    ProformasDialogFragment.Proforma(
                        numero = cursor.getString(cursor.getColumnIndexOrThrow("pr_codigo")),
                        cliente = cursor.getString(cursor.getColumnIndexOrThrow("pr_nombre"))?:"",
                        total = cursor.getString(cursor.getColumnIndexOrThrow("pr_valortotal")),
                        numeroInterno = cursor.getString(cursor.getColumnIndexOrThrow("pr_codigoA"))
                            ?: "0",
                        estado = when (cursor.getString(cursor.getColumnIndexOrThrow("pr_estado"))) {
                            "A" -> "Activo"
                            "C" -> "Enviado"
                            else -> cursor.getString(cursor.getColumnIndexOrThrow("pr_estado"))  // Maneja otros casos o estados desconocidos
                        },
                        lote = cursor.getString(cursor.getColumnIndexOrThrow("pr_lote")) ?: "0",
                        observaciones = cursor.getString(cursor.getColumnIndexOrThrow("pr_observacion"))?:"",
                        fecha = cursor.getString(cursor.getColumnIndexOrThrow("pr_fechaing")),
                        factura = cursor.getString(cursor.getColumnIndexOrThrow("fa_coddocumento"))?:"",
                    )
                )
            }
        }
        db.close()
        return pedidos
    }


    fun fnObtenerProformasPorCliente(cliente:String, fechaInc:String, fechaFin:String): List<ProformasDialogFragment.Proforma> {
        // Implementación para recuperar los pedidos desde una base de datos o fuente de datos
        val db = DatabaseManager.openDatabase(context)
        val pedidos = mutableListOf<ProformasDialogFragment.Proforma>()


        db.rawQuery("""
             SELECT 
                p.pr_codigo, 
                p.pr_nombre, 
                p.pr_valortotal, 
                p.pr_codigoA, 
                p.pr_estado, 
                pr_lote, 
                SUBSTR(p.pr_observacion, 1, 50) as pr_observacion,
                p.pr_fechaing
            FROM fa_ws_cabproforma p
            WHERE p.pr_nombre LIKE '$cliente%'
            AND date(substr(p.pr_fechaing, 7, 4) || '-' || substr(p.pr_fechaing, 4, 2) || '-' || substr(p.pr_fechaing, 1, 2))
            BETWEEN date('$fechaInc') AND date('$fechaFin')
            AND p.pr_estado <> 'E'
            ORDER BY p.pr_codigo DESC
        """, null).use { cursor ->
            while (cursor.moveToNext()) {
                pedidos.add(
                    ProformasDialogFragment.Proforma(
                        numero = cursor.getString(cursor.getColumnIndexOrThrow("pr_codigo")),
                        cliente = cursor.getString(cursor.getColumnIndexOrThrow("pr_nombre")),
                        total = cursor.getString(cursor.getColumnIndexOrThrow("pr_valortotal")),
                        numeroInterno = cursor.getString(cursor.getColumnIndexOrThrow("pr_codigoA"))
                            ?: "0",
                        estado = when (cursor.getString(cursor.getColumnIndexOrThrow("pr_estado"))) {
                            "A" -> "Activo"
                            "C" -> "Enviado"
                            else -> cursor.getString(cursor.getColumnIndexOrThrow("pr_estado"))  // Maneja otros casos o estados desconocidos
                        },
                        lote = cursor.getString(cursor.getColumnIndexOrThrow("pr_lote")) ?: "0",
                        observaciones = cursor.getString(cursor.getColumnIndexOrThrow("pr_observacion")),
                        fecha = cursor.getString(cursor.getColumnIndexOrThrow("pr_fechaing")),
                        factura = ""
                    )
                )
            }
        }
        db.close()
        return pedidos
    }


    fun fnConsultarProformas(
        codDocumento: Int,
        actualizarCabecera: (Cursor) -> Unit,
        actualizarDetalles: (MutableList<datosDet>) -> Unit,
        onDocumentoNoEncontrado: () -> Unit  // Añade un callback para manejar la no existencia del documento
    ) {
        val db = DatabaseManager.openDatabase(context)
        var documentoEncontrado = false  // Variable para controlar si se encontró el documento

        try {
            db.rawQuery(
                "SELECT * FROM fa_ws_cabproforma WHERE pr_codigo = ? and pr_estado <>'E' ",
                arrayOf(codDocumento.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    actualizarCabecera(cursor)
                    documentoEncontrado = true  // Indica que se encontró el documento
                }
            }

            if (!documentoEncontrado) {
                // Invoca el callback de documento no encontrado si no se encontró la cabecera
                onDocumentoNoEncontrado()
                return  // Sale temprano para evitar buscar detalles si la cabecera no existe
            }

            val detallesList = mutableListOf<datosDet>()
            db.rawQuery(
                "SELECT * FROM fa_ws_detproforma WHERE pr_codigo = ?",
                arrayOf(codDocumento.toString())
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    detallesList.add(extractDetailProforma(cursor))
                }
                actualizarDetalles(detallesList)
            }
        } finally {
            DatabaseManager.closeDatabase()
        }
    }


    private fun extractDetailProforma(cursor: Cursor): datosDet {
        val codigo = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo"))
        val cantidad = cursor.getString(cursor.getColumnIndexOrThrow("dp_cantidad")).toInt()
        val precio = String.format("%.3f", cursor.getString(cursor.getColumnIndexOrThrow("dp_precio")).toDoubleOrNull() ?: 0.0)
        val referencia = cursor.getString(cursor.getColumnIndexOrThrow("it_referencia"))
        val unidadCE = cursor.getString(cursor.getColumnIndexOrThrow("um_pesoCE"))
        val subtotal = BigDecimal(cantidad * precio.toDouble()).setScale(2, BigDecimal.ROUND_HALF_UP).toString()
        val DescItem = String.format("%.2f", cursor.getString(cursor.getColumnIndexOrThrow("dp_porcdescto")).toDoubleOrNull() ?: 0.0)
        val ConDesc = (subtotal.toBigDecimal() - subtotal.toBigDecimal() * DescItem.toBigDecimal() / BigDecimal(100))
            .setScale(2, RoundingMode.HALF_UP)
        val costProm = cursor.getString(cursor.getColumnIndexOrThrow("dp_costoPromedio"))?:"0"
        val combo = cursor.getString(cursor.getColumnIndexOrThrow("cb_codigo"))?:"0"
        val regalo = cursor.getString(cursor.getColumnIndexOrThrow("it_regalo"))?:"0"

        return datosDet(
            referencia,
            cantidad.toString(),
            precio,
            subtotal,
            codigo,
            "",
            unidadCE,
            DescItem,
            ConDesc.toString(),
            costProm,
            String.format("%.2f", (precio.toDouble() * (1 -DescItem.toDouble()/100)) / costProm.toDouble()),
            combo,
            regalo
        )
    }

    fun fnEliminarProforma(codDocumento: Int) {
        val db = DatabaseManager.openDatabase(context)
        try {
            db.beginTransaction() // Iniciar transacción para asegurar que ambas operaciones se completan

            // Actualizar la cabecera del pedido
            val valuesCabecera = ContentValues()
            valuesCabecera.put("pr_estado", "E")
            valuesCabecera.put("pr_fechaelim", fnObtenerFecha())// Marcar como eliminado lógicamente
            db.update(
                "fa_ws_cabproforma",
                valuesCabecera,
                "pr_codigo = ?",
                arrayOf(codDocumento.toString())
            )

            // Actualizar los detalles del pedido
            val valuesDetalle = ContentValues()
            valuesDetalle.put("dp_estado", "E")
            valuesDetalle.put("dp_fechaelim", fnObtenerFecha())// Marcar como eliminado lógicamente
            db.update(
                "fa_ws_detproforma",
                valuesDetalle,
                "pr_codigo = ?",
                arrayOf(codDocumento.toString())
            )

            db.setTransactionSuccessful() // Marcar la transacción como exitosa
        } catch (e: Exception) {
            Log.e("Database", "Error al eliminar documento lógicamente: ${e.localizedMessage}")
        } finally {
            db.endTransaction() // Finalizar la transacción
            DatabaseManager.closeDatabase() // Asegurarse de cerrar la base de datos
        }
    }


    fun fnFactorComision(): List<TipoTarjetaComision> {
        val resultado = mutableListOf<TipoTarjetaComision>()
        val db = DatabaseManager.openDatabase(context)

        try {
            val sql = """
            SELECT 
                tc.tt_codigo   AS tt_codigo,
                tc.tt_tipo     AS tt_tipo,
                tc.tc_comision AS tc_comision,
                tc.tc_codigo   AS tc_codigo
            FROM fa_ws_tipoTarjetaComision tc
            ORDER BY tc.tt_codigo, tc.tt_tipo
        """.trimIndent()

            db.rawQuery(sql, null).use { c ->
                val ixTtCodigo   = c.getColumnIndexOrThrow("tt_codigo")
                val ixTtTipo     = c.getColumnIndexOrThrow("tt_tipo")
                val ixTcComision = c.getColumnIndexOrThrow("tc_comision")
                val ixTcCodigo   = c.getColumnIndexOrThrow("tc_codigo")

                while (c.moveToNext()) {
                    val ttCodigo   = c.getInt(ixTtCodigo)                   // <- LEE el valor
                    val ttTipo     = c.getString(ixTtTipo) ?: ""            // <- LEE el valor
                    val tcComision = c.getDouble(ixTcComision)              // <- LEE el valor
                    val tcCodigo   = c.getDouble(ixTcCodigo)                   // <- LEE el valor (si es INT)
                    // si tu data class espera Double para tcCodigo, usa: c.getInt(ixTcCodigo).toDouble()

                    resultado.add(
                        TipoTarjetaComision(
                            ttCodigo = ttCodigo,
                            ttTipo = ttTipo,
                            tcComision = tcComision,
                            tcCodigo = tcCodigo
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            DatabaseManager.closeDatabase()
        }

        return resultado
    }


    fun fnBuscaPreciosyStockEnlinea(referencia: String, bodega: String): List<PreciosyStock> {
        val referencias = mutableListOf<PreciosyStock>()
        val db = DatabaseManager.openDatabase(context)

        val query = """
        SELECT
            it_codigo AS Codigo,
            it_marca AS Marca,
            it_referencia AS Referencia,
            um_sku AS Sku,
            it_descripcion AS Descripcion,
            CASE
                WHEN ? = '2'  THEN ROUND(IFNULL(it_teler,0) + IFNULL(it_exhTele,0), 2)
                WHEN ? = '51' THEN ROUND(IFNULL(it_mmg,0)   + IFNULL(it_exhVmr,0), 2)
                ELSE ROUND(
                    IFNULL(it_almesa,0) + IFNULL(it_teler,0) + IFNULL(it_mmg,0) + IFNULL(it_mmq,0) 
                    + IFNULL(it_exhTele,0) + IFNULL(it_exhVmr,0)
                , 2)
            END AS Total,
            ROUND(pv_preciosubdistrib, 2) AS SubDistribuidor,
            ROUND(pv_precio5, 2) AS Contado,
            ROUND(pv_precio6, 2) AS Credito,
            ROUND(pv_precio7, 2) AS Publico,
            ROUND(pv_precio7 * 1.15, 2) AS PublicoF,
            um_pesoCE AS Peso,
            it_titulo AS Titulo,
            it_costoprom AS costoProm
        FROM ve_ws_itemTmp
        WHERE it_referencia LIKE ?
        ORDER BY Total DESC
    """.trimIndent()

        // bodega se usa dos veces en el CASE + referencia en el WHERE
        val args = arrayOf(bodega, bodega, "$referencia%")

        val cursor = db.rawQuery(query, args)

        if (cursor.moveToFirst()) {
            do {
                val item = PreciosyStock(
                    marca       = cursor.getString(cursor.getColumnIndexOrThrow("Marca")) ?: "",
                    referencia  = cursor.getString(cursor.getColumnIndexOrThrow("Referencia")) ?: "",
                    sku         = cursor.getString(cursor.getColumnIndexOrThrow("Sku")) ?: "Desconocido",
                    descripcion = cursor.getString(cursor.getColumnIndexOrThrow("Descripcion")) ?: "",
                    total       = cursor.getString(cursor.getColumnIndexOrThrow("Total")) ?: "0",
                    contado     = cursor.getString(cursor.getColumnIndexOrThrow("Contado")) ?: "0.00",
                    credito     = cursor.getString(cursor.getColumnIndexOrThrow("Credito")) ?: "0.00",
                    publico     = cursor.getString(cursor.getColumnIndexOrThrow("Publico")) ?: "0.00",
                    final       = cursor.getString(cursor.getColumnIndexOrThrow("PublicoF")) ?: "0.00",
                    peso        = cursor.getString(cursor.getColumnIndexOrThrow("Peso")) ?: "0.00",
                    titulo      = cursor.getString(cursor.getColumnIndexOrThrow("Titulo")) ?: "",
                    codigo      = cursor.getString(cursor.getColumnIndexOrThrow("Codigo")) ?: "",
                    sub         = cursor.getString(cursor.getColumnIndexOrThrow("SubDistribuidor")) ?: "",
                    costProm    = cursor.getString(cursor.getColumnIndexOrThrow("costoProm")) ?: ""
                )
                referencias.add(item)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return referencias
    }


    fun fnObtenerTipoVendedor(): List<TipoVendedor> {
        val resultado = mutableListOf<TipoVendedor>()

        // Si tu DatabaseManager puede devolver null, maneja el caso:
        val db = DatabaseManager.openDatabase(context)
            ?: run {
                Log.e("ClsLLenarControles", "openDatabase() devolvió null")
                return emptyList()
            }

        try {
            db.rawQuery(
                "SELECT gv_tipo AS codigo, bo_codigo AS bodega FROM ve_ws_vendedor",
                null
            ).use { c ->
                val idxCodigo = c.getColumnIndexOrThrow("codigo")
                val idxBodega = c.getColumnIndexOrThrow("bodega")

                while (c.moveToNext()) {
                    val cod = c.getString(idxCodigo)
                    val bod = c.getString(idxBodega)
                    resultado.add(TipoVendedor(cod, bod))
                }
            }
        } catch (e: Exception) {
            Log.e("ClsLLenarControles", "fnObtenerTipoVendedor()", e)
            // si hay error, devolvemos lo acumulado (o empty)
            return emptyList()
        } finally {
            DatabaseManager.closeDatabase()
        }

        return resultado
    }


    fun fnObtenerTipoVendedorLogin(ep_codigo: Int): String {
        val db = DatabaseManager.openDatabase(context) ?: return "1" // 🔒 default 1 si DB no abre
        var tipo: String? = null

        try {
            val sql = """
            SELECT gv_tipo AS codigo
            FROM ve_ws_vendedor
            WHERE vn_codigo = ?
            LIMIT 1
        """.trimIndent()

            db.rawQuery(sql, arrayOf(ep_codigo.toString())).use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndexOrThrow("codigo")
                    tipo = if (c.isNull(idx)) null else c.getString(idx)
                }
            }
        } catch (e: Exception) {
            Log.e("ClsLLenarControles", "fnObtenerTipoVendedorLogin()", e)
        } finally {
            DatabaseManager.closeDatabase()
        }

        // 🔒 Normaliza: si null, vacío o no numérico → devuelve "1"
        val gv_tipo = tipo?.trim()
        return if (gv_tipo.isNullOrEmpty()) "1" else gv_tipo
    }


    fun fnProformaReporte(
        codDocumento: Int,
        actualizarCabecera: (Cursor) -> Unit,
        actualizarDetalles: (MutableList<reporte>) -> Unit,
        onDocumentoNoEncontrado: () -> Unit
    ) {
        val db = DatabaseManager.openDatabase(context)
        var documentoEncontrado = false

        try {
            val sqlCab = """
            SELECT 
                p.pr_codigo,
                p.pr_fechatrn,
                p.pr_nombre,
                t.tr_nombre,
                p.pr_observacion,
                p.pr_codigoA,
                p.ci_descripcion,
                fp.Descripcion,
                v.vn_nombre,
                p.pr_lote,
                p.pr_valorbruto,
                p.pr_valordesc,
                p.pr_valorseguro,
                p.pr_valoriva,
                p.pr_valorflete,
                p.pr_valortotal,
                CASE p.bo_codigo
                    WHEN 2  THEN 'Telerepuesto'
                    WHEN 51 THEN 'PromarketGO'
                    ELSE ''
                END AS bodega_nombre
            FROM fa_ws_cabproforma p
            LEFT JOIN ve_ws_transporte t ON p.tr_codigo = t.tr_codigo
            LEFT JOIN cc_ws_parametrostransaccionesxbodega fp ON p.tp_codigo = fp.Codigo
            LEFT JOIN ve_ws_vendedor v ON p.ep_codigo = v.vn_codigo
            WHERE p.pr_codigo = ? 
              AND p.pr_estado <> 'E'
        """.trimIndent()

            db.rawQuery(sqlCab, arrayOf(codDocumento.toString())).use { cursor ->
                if (cursor.moveToFirst()) {
                    actualizarCabecera(cursor)
                    documentoEncontrado = true
                }
            }

            if (!documentoEncontrado) {
                onDocumentoNoEncontrado()
                return
            }

            val detallesList = mutableListOf<reporte>()
            val sqlDet = """
            SELECT * 
            FROM fa_ws_detproforma 
            WHERE pr_codigo = ?
        """.trimIndent()

            db.rawQuery(sqlDet, arrayOf(codDocumento.toString())).use { cursor ->
                while (cursor.moveToNext()) {
                    detallesList.add(detallesProformaReporte(cursor))
                }
            }
            actualizarDetalles(detallesList)
        } finally {
            DatabaseManager.closeDatabase()
        }
    }


    private fun detallesProformaReporte(cursor: Cursor): reporte {
        val codigo = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo"))
        val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("it_referencia"))
        val descuento = String.format("%.2f", cursor.getString(cursor.getColumnIndexOrThrow("dp_porcdescto")).toDoubleOrNull() ?: 0.0)
        val cantidad = cursor.getString(cursor.getColumnIndexOrThrow("dp_cantidad"))
        val precio = String.format("%.2f", cursor.getString(cursor.getColumnIndexOrThrow("dp_precio")).toDoubleOrNull() ?: 0.0)
        val subtotal = BigDecimal(cantidad.toInt() * precio.toDouble()).setScale(2, BigDecimal.ROUND_HALF_UP).toString()
        val ConDescuento = (subtotal.toBigDecimal() - (subtotal.toBigDecimal() * descuento.toBigDecimal() / BigDecimal(100)))
            .setScale(2, RoundingMode.HALF_UP).toString()


        return reporte(
            codigo,
            descripcion,
            descuento,
            cantidad,
            precio,
            subtotal,
            ConDescuento

        )
    }


    fun fnObtenerStockEnLinea(referencia: String, codigo: String): List<String> {
        val db = DatabaseManager.openDatabase(context)
        val valores = mutableListOf<String>()
        val cursor = db.rawQuery(
            "SELECT i.it_referencia AS Referencia," +
                    "i.it_descripcion AS Descripcion, " +
                    "ROUND(i.it_teler,2) AS StockTelerepuesto, " +
                    "ROUND(i.it_exhTele,2) AS StockExhTelerepuestos, " +
                    "ROUND(i.it_almesa,2) AS StockPortrans, " +
                    "ROUND(i.it_mmg,2) AS StockProMarket, " +
                    "ROUND(i.it_exhVmr,2) AS StockExhProMarket, " +
                    "ROUND(i.it_mmq ,2) AS StockReparadaAlm, " +
                    "ROUND(i.it_dcp,2) AS StockDepComercial, " +
                    "ROUND(i.pv_preciosubdistrib,3) AS SubDistribuidor, " +
                    "ROUND(i.pv_desctosubdistrib,3) AS DsctoSubDistr, " +
                    "ROUND(i.pv_precio5,3) AS Contado, " +
                    "ROUND(i.pv_precio6,3) AS Credito, " +
                    "ROUND(i.um_unidadCM,3) AS CartonMaster, " +
                    "ROUND(i.um_unidadCE,2) AS CartonEstandar, " +
                    "i.um_sku AS Sku, " +
                    "ROUND(i.um_pesoCE,3) AS PesoCE, " +
                    "IFNULL(c.cb_descripcionA, '') AS cb_descripcionA " +
                    "FROM ve_ws_itemTmp i " +
                    "left join iv_ws_itemComboCab c on c.cb_codigo = ?" +
                    "WHERE i.it_referencia = ?",
            arrayOf(codigo,referencia)
        )

        val columnNames = cursor.columnNames
        while (cursor.moveToNext()) {
            for (columnName in columnNames) {
                val valor = cursor.getStringOrNull(cursor.getColumnIndex(columnName))
                valores.add(valor ?: "0")
            }
        }
        cursor.close()
        db.close()

        return valores
    }


    fun fnCombosPedidoMasHistorico(
        peCodDocumento: String,
        clCodigo: String,
        incluirLinea: Boolean = true // pon false si tu tabla no tiene d.cb_linea
    ): List<ComboItemRow> {

        val db = DatabaseManager.openDatabase(context)

        val selLinea = if (incluirLinea) "d.cb_linea" else "NULL AS cb_linea"
        val ordLinea = if (incluirLinea) " , d.cb_linea" else ""

        val sql = """
        WITH
        ped AS (
          SELECT DISTINCT it_codigo
          FROM fa_ws_detpedidoQueue
          WHERE TRIM(CAST(pe_coddocumento AS TEXT)) = TRIM(?)
            AND it_codigo IS NOT NULL AND TRIM(it_codigo) <> ''
        ),
        ventas_cli AS (
          SELECT it_codigo,
                 SUM(CAST(ve_cantidad AS REAL)) AS qty,
                 COUNT(DISTINCT vn_codigo)      AS freq
          FROM fa_ws_ventas
          WHERE TRIM(cl_codigo) = TRIM(?)
          GROUP BY it_codigo
        ),
        combos_ped AS (
          SELECT DISTINCT d.cb_codigo
          FROM iv_ws_itemComboDet d
          JOIN ped p ON p.it_codigo = d.it_codigo
        ),
        combos_hist AS (
          SELECT DISTINCT d.cb_codigo
          FROM iv_ws_itemComboDet d
          JOIN ventas_cli v ON v.it_codigo = d.it_codigo
        ),
        combos_union AS (
          SELECT cb_codigo,
                 MAX(ped_flag)  AS ped_flag,
                 MAX(hist_flag) AS hist_flag
          FROM (
            SELECT cp.cb_codigo, 1 AS ped_flag, 0 AS hist_flag FROM combos_ped  cp
            UNION ALL
            SELECT ch.cb_codigo, 0 AS ped_flag, 1 AS hist_flag FROM combos_hist ch
          )
          GROUP BY cb_codigo
        ),
        tot AS (
          SELECT cb_codigo, COUNT(*) AS items_total
          FROM iv_ws_itemComboDet
          GROUP BY cb_codigo
        ),
        en_ped AS (
          SELECT d.cb_codigo, COUNT(*) AS items_en_pedido
          FROM iv_ws_itemComboDet d
          JOIN ped p ON p.it_codigo = d.it_codigo
          GROUP BY d.cb_codigo
        ),
        hist_combo AS (
          SELECT d.cb_codigo, SUM(COALESCE(v.qty,0)) AS qty_hist_combo
          FROM iv_ws_itemComboDet d
          LEFT JOIN ventas_cli v ON v.it_codigo = d.it_codigo
          GROUP BY d.cb_codigo
        )
        SELECT
          cu.cb_codigo                                       AS cb_codigo,
          c.cb_descripcionA                                  AS combo_nombre,
          CASE
            WHEN cu.ped_flag = 1 AND cu.hist_flag = 1 THEN 'Ambos'
            WHEN cu.ped_flag = 1                      THEN 'Pedido'
            ELSE 'Histórico'
          END                                               AS fuente,
          t.items_total                                      AS items_total,
          COALESCE(ep.items_en_pedido,0)                    AS items_en_pedido,
          COALESCE(hc.qty_hist_combo,0)                     AS qty_hist_combo,
          $selLinea,
          d.it_codigo                                        AS it_codigo,
          i.it_referencia                                    AS it_referencia,
          i.it_referencia || ' - ' || i.it_descripcion       AS it_descripcion,
          CASE WHEN p.it_codigo IS NULL THEN 0 ELSE 1 END    AS en_pedido,
          COALESCE(v.qty, 0)                                 AS qty_hist_item,

          -- NUEVO: stock desde ve_ws_item
          CAST(
            COALESCE(i.it_almesa,0) + COALESCE(i.it_teler,0) + COALESCE(i.it_mmg,0) + COALESCE(i.it_mmq,0)
            AS REAL
          )                                                  AS stock,

          -- LOTE excluyendo ítems del combo que ya están en el pedido
          (
            (
              -- subtotal del pedido con descuento
              COALESCE((
                SELECT SUM(CAST(dp.dp_cantidad AS REAL) * CAST(dp.dp_precio AS REAL) * (1 - COALESCE(dp.dp_porcdescuento,0)/100.0))
                FROM fa_ws_detpedidoQueue dp
                WHERE TRIM(CAST(dp.pe_coddocumento AS TEXT)) = TRIM(?)
              ), 0)
              +
              -- subtotal del combo (1 combo) EXCLUYENDO ítems presentes en el pedido
              COALESCE((
                SELECT SUM(CAST(d2.cb_cantidad AS REAL) * CAST(d2.cb_precio AS REAL))
                FROM iv_ws_itemComboDet d2
                WHERE d2.cb_codigo = cu.cb_codigo
                  AND NOT EXISTS (SELECT 1 FROM ped p2 WHERE p2.it_codigo = d2.it_codigo)
              ), 0)
            )
            /
            NULLIF(
              (
                -- costo del pedido
                COALESCE((
                  SELECT SUM(CAST(dp.dp_cantidad AS REAL) * CAST(dp.dp_costoPromedio AS REAL))
                  FROM fa_ws_detpedidoQueue dp
                  WHERE TRIM(CAST(dp.pe_coddocumento AS TEXT)) = TRIM(?)
                ), 0)
                +
                -- costo del combo EXCLUYENDO ítems presentes en el pedido
                COALESCE((
                  SELECT SUM(CAST(d2.cb_cantidad AS REAL) * CAST(COALESCE(d2.it_costopromedio, d2.it_costoPromedio, 0) AS REAL))
                  FROM iv_ws_itemComboDet d2
                  WHERE d2.cb_codigo = cu.cb_codigo
                    AND NOT EXISTS (SELECT 1 FROM ped p2 WHERE p2.it_codigo = d2.it_codigo)
                ), 0)
              ),
              0
            )
          )                                                  AS lote

        FROM combos_union cu
        JOIN iv_ws_itemComboCab  c ON c.cb_codigo  = cu.cb_codigo
        JOIN iv_ws_itemComboDet  d ON d.cb_codigo  = cu.cb_codigo
        JOIN ve_ws_item          i ON i.it_codigo  = d.it_codigo
        LEFT JOIN ped            p  ON p.it_codigo  = d.it_codigo
        LEFT JOIN tot            t  ON t.cb_codigo  = cu.cb_codigo
        LEFT JOIN en_ped         ep ON ep.cb_codigo = cu.cb_codigo
        LEFT JOIN hist_combo     hc ON hc.cb_codigo = cu.cb_codigo
        LEFT JOIN ventas_cli     v  ON v.it_codigo  = d.it_codigo
        ORDER BY
          (cu.ped_flag) DESC,
          COALESCE(ep.items_en_pedido,0) DESC,
          COALESCE(hc.qty_hist_combo,0) DESC,
          c.cb_descripcionA
          $ordLinea,
          i.it_referencia
    """.trimIndent()

        // seguimos pasando dos veces más peCodDocumento para las subconsultas del lote
        val args = arrayOf(peCodDocumento, clCodigo, peCodDocumento, peCodDocumento)

        val out = mutableListOf<ComboItemRow>()
        db.rawQuery(sql, args).use { c ->
            val idxCb        = c.getColumnIndexOrThrow("cb_codigo")
            val idxNombre    = c.getColumnIndexOrThrow("combo_nombre")
            val idxFuente    = c.getColumnIndexOrThrow("fuente")
            val idxTot       = c.getColumnIndexOrThrow("items_total")
            val idxEnPed     = c.getColumnIndexOrThrow("items_en_pedido")
            val idxQtyHistCb = c.getColumnIndexOrThrow("qty_hist_combo")
            val idxLinea     = c.getColumnIndexOrThrow("cb_linea")
            val idxIt        = c.getColumnIndexOrThrow("it_codigo")
            val idxRef       = c.getColumnIndexOrThrow("it_referencia")
            val idxDesc      = c.getColumnIndexOrThrow("it_descripcion")
            val idxEnItem    = c.getColumnIndexOrThrow("en_pedido")
            val idxQtyHistIt = c.getColumnIndexOrThrow("qty_hist_item")
            val idxStock     = c.getColumnIndexOrThrow("stock")   // NUEVO
            val idxLote      = c.getColumnIndexOrThrow("lote")

            while (c.moveToNext()) {
                out += ComboItemRow(
                    cbCodigo      = c.getString(idxCb),
                    comboNombre   = c.getString(idxNombre) ?: "",
                    fuente        = c.getString(idxFuente) ?: "",
                    itemsTotal    = c.getInt(idxTot),
                    itemsEnPedido = c.getInt(idxEnPed),
                    qtyHistCombo  = c.getDouble(idxQtyHistCb),
                    linea         = if (c.isNull(idxLinea)) null else c.getInt(idxLinea),
                    itCodigo      = c.getString(idxIt),
                    itReferencia  = c.getString(idxRef) ?: "",
                    itDescripcion = c.getString(idxDesc) ?: "",
                    enPedido      = c.getInt(idxEnItem) == 1,
                    qtyHistItem   = c.getInt(idxQtyHistIt),

                    // NUEVO: stock
                    stock         = if (c.isNull(idxStock)) 0 else c.getInt(idxStock),

                    // lote con redondeo 2 decimales
                    lote = if (c.isNull(idxLote)) {
                        0.0
                    } else {
                        BigDecimal(c.getDouble(idxLote))
                            .setScale(2, RoundingMode.HALF_UP)
                            .toDouble()
                    }
                )
            }
        }
        return out
    }


    fun fnObtenerAnios(): List<Item> {
        val db = DatabaseManager.openDatabase(context) ?: return emptyList()
        val sql = """
        SELECT DISTINCT substr(trim(rv_fechainicial),1,4) AS anio
        FROM fa_ws_rutaVendedor
        WHERE rv_fechainicial IS NOT NULL AND trim(rv_fechainicial) <> ''
        UNION
        SELECT DISTINCT substr(trim(rv_fechafinal),1,4) AS anio
        FROM fa_ws_rutaVendedor
        WHERE rv_fechafinal IS NOT NULL AND trim(rv_fechafinal) <> ''
        ORDER BY anio
    """.trimIndent()

        val lista = mutableListOf<Item>()
        try {
            db.rawQuery(sql, null).use { c ->
                val idx = c.getColumnIndexOrThrow("anio")
                while (c.moveToNext()) {
                    val valor = c.getString(idx)
                    lista.add(Item(valor, valor)) // código = nombre (ej. "2025")
                }
            }
        } catch (e: Exception) {
            Log.e("ClsLLenarControles", "fnObtenerAnios()", e)
        } finally {
            DatabaseManager.closeDatabase()
        }
        return lista
    }


    fun fnObtenerMeses(anio: String): List<Item> {
        val lista = mutableListOf<Item>()
        val db = DatabaseManager.openDatabase(context) ?: return emptyList()

        val sql = """
        SELECT mm,
               CASE mm
                 WHEN '01' THEN 'Enero'
                 WHEN '02' THEN 'Febrero'
                 WHEN '03' THEN 'Marzo'
                 WHEN '04' THEN 'Abril'
                 WHEN '05' THEN 'Mayo'
                 WHEN '06' THEN 'Junio'
                 WHEN '07' THEN 'Julio'
                 WHEN '08' THEN 'Agosto'
                 WHEN '09' THEN 'Septiembre'
                 WHEN '10' THEN 'Octubre'
                 WHEN '11' THEN 'Noviembre'
                 WHEN '12' THEN 'Diciembre'
               END AS mes_nombre
        FROM (
            SELECT DISTINCT substr(trim(rv_fechainicial),6,2) AS mm
            FROM fa_ws_rutaVendedor
            WHERE rv_fechainicial IS NOT NULL
              AND trim(rv_fechainicial) <> ''
              AND length(trim(rv_fechainicial)) >= 7
              AND substr(trim(rv_fechainicial),1,4) = ?

            UNION

            SELECT DISTINCT substr(trim(rv_fechafinal),6,2) AS mm
            FROM fa_ws_rutaVendedor
            WHERE rv_fechafinal IS NOT NULL
              AND trim(rv_fechafinal) <> ''
              AND length(trim(rv_fechafinal)) >= 7
              AND substr(trim(rv_fechafinal),1,4) = ?
        )
        WHERE mm BETWEEN '01' AND '12'
        ORDER BY mm desc
    """.trimIndent()

        try {
            db.rawQuery(sql, arrayOf(anio, anio)).use { c ->
                val idxCodigo = c.getColumnIndexOrThrow("mm")
                val idxNombre = c.getColumnIndexOrThrow("mes_nombre")
                while (c.moveToNext()) {
                    lista.add(Item(c.getString(idxCodigo), c.getString(idxNombre)))
                }
            }
        } finally {
            DatabaseManager.closeDatabase()
        }
        return lista
    }

    fun fnObtenerSemanas(mes: String, anio: String): List<Semana> {
        val db = DatabaseManager.openDatabase(context) ?: return emptyList()

        val sql = """
        WITH fechas AS (
            SELECT DISTINCT
                date(CASE WHEN instr(trim(rv_fechainicial), 'T') > 0 THEN substr(trim(rv_fechainicial), 1, 10) ELSE trim(rv_fechainicial) END) AS fecha_inicio,
                date(CASE WHEN instr(trim(rv_fechafinal), 'T') > 0 THEN substr(trim(rv_fechafinal), 1, 10) ELSE trim(rv_fechafinal) END) AS fecha_fin
            FROM fa_ws_rutaVendedor
            WHERE
                substr(rv_fechainicial, 1, 7) = ?  -- Filtra por mes y año dinámicamente
                AND (rv_fechainicial IS NOT NULL AND trim(rv_fechainicial) <> '')
                AND (rv_fechafinal IS NOT NULL AND trim(rv_fechafinal) <> '')
        ),
        weeks AS (
            SELECT 
                fecha_inicio,
                fecha_fin,
                (SELECT COUNT(*) FROM fechas f2 WHERE f2.fecha_inicio <= f1.fecha_inicio) AS semana_num  -- Contador de semana
            FROM fechas f1
        )
        SELECT 
            semana_num,
            'Semana ' || semana_num AS semana_nombre,
            fecha_inicio,
            fecha_fin
        FROM weeks
        ORDER BY semana_num desc;
    """.trimIndent()

        val lista = mutableListOf<Semana>()
        try {
            db.rawQuery(sql, arrayOf("$anio-$mes")).use { c ->
                val idxSemana = c.getColumnIndexOrThrow("semana_num")
                val idxSemanaNombre = c.getColumnIndexOrThrow("semana_nombre")
                val idxFechaInicio = c.getColumnIndexOrThrow("fecha_inicio")
                val idxFechaFin = c.getColumnIndexOrThrow("fecha_fin")
                while (c.moveToNext()) {
                    val semanaNum = c.getInt(idxSemana)  // Número de semana
                    val semanaNombre = c.getString(idxSemanaNombre)  // Nombre de la semana
                    val fechaInicio = c.getString(idxFechaInicio)  // Fecha de inicio
                    val fechaFin = c.getString(idxFechaFin)  // Fecha de fin
                    // Añadimos los 4 campos a la lista como un objeto Semana
                    lista.add(Semana(semanaNum, semanaNombre, fechaInicio, fechaFin))
                }
            }
        } catch (e: Exception) {
            Log.e("ClsLLenarControles", "fnObtenerSemanasPorMesYAnio($mes, $anio)", e)
        } finally {
            DatabaseManager.closeDatabase()
        }
        return lista
    }



    fun fnObtenerRutasPorFechas(fechaInicio: String, fechaFin: String): List<ConsultaRutas> {
        val db = DatabaseManager.openDatabase(context) ?: return emptyList()

        val sql = """
        SELECT
            c.cl_codigo,
            r.rv_linea,
            cl_nombre,
            c.ci_descripcion,
            r.rv_dia,
            r.rv_observacion,
            r.rv_visita,
            r.rv_cobro,
            r.rv_venta,
            r.rv_telefono,
            r.rv_fechainicial,
            r.rv_fechafinal,
           CASE
                WHEN r.rv_fechaproceso IS NULL THEN
                    strftime('%d-%m-%Y 12:00', date(r.rv_fechafinal, '+3 days'))  -- Suma 3 días a la fecha final y devuelve el formato deseado
                ELSE
                    strftime('%d-%m-%Y %H:%M', r.rv_fechaproceso)  -- Aplica el formato a rv_fechaproceso
            END AS fecha
        FROM fa_ws_rutaVendedor r
        INNER JOIN ve_ws_clienteAsignadoVendedor c ON r.cl_codigo = c.cl_codigo
        WHERE 
            substr(r.rv_fechainicial, 1, 10) = ?  -- Compara solo la fecha de inicio
            AND substr(r.rv_fechafinal, 1, 10) = ?  -- Compara solo la fecha de fin
        ORDER BY 
            CASE r.rv_dia
                WHEN 'Lunes' THEN 1
                WHEN 'Martes' THEN 2
                WHEN 'Miércoles' THEN 3
                WHEN 'Jueves' THEN 4
                WHEN 'Viernes' THEN 5
                WHEN 'Sábado' THEN 6
                WHEN 'Domingo' THEN 7
                ELSE 8  -- Para otros valores no esperados, los coloca al final
            END, rv_linea;

    """.trimIndent()

        val lista = mutableListOf<ConsultaRutas>()
        try {
            db.rawQuery(sql, arrayOf(fechaInicio, fechaFin)).use { c ->
                val idxClCodigo = c.getColumnIndexOrThrow("cl_codigo")
                val idxRvLinea = c.getColumnIndexOrThrow("rv_linea")
                val idxClNombre = c.getColumnIndexOrThrow("cl_nombre")
                val idxCiDescripcion = c.getColumnIndexOrThrow("ci_descripcion")
                val idxRvDia = c.getColumnIndexOrThrow("rv_dia")
                val idxRvObservacion = c.getColumnIndexOrThrow("rv_observacion")
                val idxRvVisita = c.getColumnIndexOrThrow("rv_visita")
                val idxRvCobro = c.getColumnIndexOrThrow("rv_cobro")
                val idxRvVenta = c.getColumnIndexOrThrow("rv_venta")
                val idxRvTelefono = c.getColumnIndexOrThrow("rv_telefono")
                val idxRvFechaInicial = c.getColumnIndexOrThrow("rv_fechainicial")
                val idxRvFechaFinal = c.getColumnIndexOrThrow("rv_fechafinal")
                val idxFechaProceso = c.getColumnIndexOrThrow("fecha")

                while (c.moveToNext()) {
                    val checkVisita = c.getInt(idxRvVisita)
                    val checkVenta = c.getInt(idxRvVenta)
                    val checkCobro = c.getInt(idxRvCobro)
                    val checkTelefono = c.getInt(idxRvTelefono)

                    val isCheckedVisita = checkVisita == 1
                    val isCheckedCobro = checkCobro == 1
                    val isCheckedVenta = checkVenta == 1
                    val isCheckedTelefono = checkTelefono == 1

                    // Verifica si la columna tiene un valor nulo
                    val clCodigo = c.getString(idxClCodigo) ?: "N/A"  // Valor predeterminado
                    val rvLinea = c.getString(idxRvLinea) ?: "N/A"
                    val clNombre = c.getString(idxClNombre) ?: "N/A"
                    val ciDescripcion = c.getString(idxCiDescripcion) ?: "N/A"
                    val rvDia = c.getString(idxRvDia) ?: "N/A"
                    val rvObservacion = c.getString(idxRvObservacion) ?: "N/A"
                    val rvFechaInicial = c.getString(idxRvFechaInicial) ?: "N/A"
                    val rvFechaFinal = c.getString(idxRvFechaFinal) ?: "N/A"
                    val fechaProceso = c.getString(idxFechaProceso) ?: "N/A"

                    val ruta = ConsultaRutas(
                        clCodigo,   // codigo
                        rvLinea,    // sec
                        clNombre,   // cliente
                        ciDescripcion, // Ciudad
                        rvDia,     // dia
                        rvObservacion, // observacion
                        isCheckedVisita,            // isSelectedVisita
                        isCheckedVenta,             // isSelectedVenta
                        isCheckedCobro,             // isSelectedCobro
                        isCheckedTelefono,                      // isSelectedTelefono, puedes cambiar este valor según sea necesario
                        rvFechaInicial,  // fechaInc
                        rvFechaFinal,     // fechaFin
                        fechaProceso
                    )
                    lista.add(ruta)
                }
            }
        } catch (e: Exception) {
            Log.e("ClsLLenarControles", "fnObtenerRutasPorFechas($fechaInicio, $fechaFin)", e)
        } finally {
            DatabaseManager.closeDatabase()
        }
        return lista
    }



    fun fnObtenerProvincia(cl_codigo: String): Int {
        val db = DatabaseManager.openDatabase(context)
        return db.rawQuery(
            "SELECT pr_codigo FROM ve_ws_clienteAsignadoVendedor WHERE cl_codigo = ?",
            arrayOf(cl_codigo)
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(cursor.getColumnIndexOrThrow("pr_codigo")) else -1
        }.also {
            DatabaseManager.closeDatabase()
        }
    }


    fun fnLLenarSpinnerPlazo(spinner: Spinner, tabla: String) {
        val db = DatabaseManager.openDatabase(context)
        db.rawQuery("SELECT Codigo, Descripcion FROM $tabla WHERE Codigo NOT IN (36,37)", null).use { cursor ->
            val items = mutableListOf<SpinnerItem>()
            while (cursor.moveToNext()) {
                items.add(SpinnerItem(cursor.getString(0), cursor.getString(1)))
            }
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        DatabaseManager.closeDatabase()
    }


    fun fnLLenarSpinnerPlazoParo(spinner: Spinner, tabla: String) {
        val db = DatabaseManager.openDatabase(context)
        db.rawQuery("SELECT Codigo, Descripcion FROM $tabla ", null).use { cursor ->
            val items = mutableListOf<SpinnerItem>()
            while (cursor.moveToNext()) {
                items.add(SpinnerItem(cursor.getString(0), cursor.getString(1)))
            }
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        DatabaseManager.closeDatabase()
    }


    fun obtenerProductosDelPedido(peCodDocumento: String): List<PedidoItem> {
        val db = DatabaseManager.openDatabase(context)
        val itemsPedido = mutableListOf<PedidoItem>()
        val sql = """
            SELECT 
                d.it_codigo, 
                i.it_referencia,
                i.it_descripcion, 
                (dp_precio - (dp_precio * dp_porcdescuento / 100)) * dp_cantidad AS subtotal,
                dp_costoPromedio  * dp_cantidad AS dp_costoPromedio
            FROM fa_ws_detpedidoQueue d
            INNER JOIN fa_ws_cabpedidoQueue c ON d.pe_coddocumento = c.pe_coddocumento
            INNER JOIN ve_ws_item i ON d.it_codigo = i.it_codigo
            WHERE d.pe_coddocumento = ?
        """.trimIndent()

        db.rawQuery(sql, arrayOf(peCodDocumento)).use { c ->
            val idxItCodigo = c.getColumnIndexOrThrow("it_codigo")
            val idxReferencia = c.getColumnIndexOrThrow("it_referencia")
            val idxDescripcion = c.getColumnIndexOrThrow("it_descripcion")
            val idxSubtotal = c.getColumnIndexOrThrow("subtotal")
            val idxCostoPromedio = c.getColumnIndexOrThrow("dp_costoPromedio")

            while (c.moveToNext()) {
                itemsPedido.add(
                    PedidoItem(
                        c.getString(idxItCodigo),
                        c.getString(idxReferencia),
                        c.getString(idxDescripcion),
                        c.getDouble(idxSubtotal),
                        c.getDouble(idxCostoPromedio)
                    )
                )
            }
        }
        return itemsPedido
    }


    fun obtenerHistorialVentas(clCodigo: String): List<VentaHistorial> {
        val db = DatabaseManager.openDatabase(context)
        val historialVentas = mutableListOf<VentaHistorial>()
        val sql = """
            SELECT 
                v.it_codigo, 
                i.it_referencia,
                i.it_descripcion,
                ve_cantidad, 
                ve_preciovta
            FROM fa_ws_ventas v
            INNER JOIN ve_ws_item i on v.it_codigo=i.it_codigo
            WHERE cl_codigo = ?
        """.trimIndent()

        db.rawQuery(sql, arrayOf(clCodigo)).use { c ->
            val idxItCodigo = c.getColumnIndexOrThrow("it_codigo")
            val idxReferencia = c.getColumnIndexOrThrow("it_referencia")
            val idxDescripcion = c.getColumnIndexOrThrow("it_descripcion")
            val idxCantidadVendida = c.getColumnIndexOrThrow("ve_cantidad")
            val idxPrecioVenta = c.getColumnIndexOrThrow("ve_preciovta")

            while (c.moveToNext()) {
                historialVentas.add(
                    VentaHistorial(
                        c.getString(idxItCodigo),
                        c.getString(idxReferencia),
                        c.getString(idxDescripcion),
                        c.getDouble(idxCantidadVendida),
                        c.getDouble(idxPrecioVenta)
                    )
                )
            }
        }
        return historialVentas
    }


    fun obtenerProductosDisponibles(): List<ProductoDisponible> {
        val db = DatabaseManager.openDatabase(context)
        val productosDisponibles = mutableListOf<ProductoDisponible>()
        val sql = """
            SELECT 
                ROUND(it_almesa + it_teler + it_mmg + it_mmq + it_exhTele + it_exhVmr , 2) AS stock,
                d.cb_codigo,
                d.it_codigo,
                it_referencia,
                it_descripcion,
                cb_precio,
                it_costopromedio
            FROM ve_ws_item i
            INNER JOIN iv_ws_itemComboDet d on i.it_codigo=d.it_codigo
            INNER JOIN iv_ws_itemComboCab c on d.cb_codigo=c.cb_codigo
            WHERE ROUND(it_almesa + it_teler + it_mmg + it_mmq + it_exhTele + it_exhVmr , 2) > 5
            ORDER BY d.cb_codigo
        """.trimIndent()

        db.rawQuery(sql, null).use { c ->
            val idxCbCodigo = c.getColumnIndexOrThrow("cb_codigo")
            val idxReferencia = c.getColumnIndexOrThrow("it_referencia")
            val idxDescripcion = c.getColumnIndexOrThrow("it_descripcion")
            val idxStock = c.getColumnIndexOrThrow("stock")
            val idxPrecio = c.getColumnIndexOrThrow("cb_precio")
            val idxCostoProm = c.getColumnIndexOrThrow("it_costopromedio")

            while (c.moveToNext()) {
                productosDisponibles.add(
                    ProductoDisponible(
                        c.getString(idxCbCodigo),
                        c.getString(idxReferencia),
                        c.getString(idxDescripcion),
                        c.getDouble(idxStock),
                        c.getDouble(idxPrecio),
                        c.getDouble(idxCostoProm)
                    )
                )
            }
        }
        return productosDisponibles
    }


    fun obtenerProductosRecomendados(codigosRecomendados: String): List<ProductoDisponible> {
        val db = DatabaseManager.openDatabase(context)
        val codigosFormateados = codigosRecomendados
            .split(",")
            .joinToString(",") { it.trim() }

        val sql = """
            SELECT 
                d.cb_codigo,
                d.it_codigo,
                i.it_referencia,
                i.it_descripcion,
                d.cb_precio,
                d.it_costopromedio
            FROM iv_ws_itemComboDet d
            INNER JOIN ve_ws_item i ON d.it_codigo = i.it_codigo
            WHERE d.cb_codigo IN ($codigosFormateados)
        """.trimIndent()

        val productosRecomendados = mutableListOf<ProductoDisponible>()
        db.rawQuery(sql, null).use { c ->
            val idxCbCodigo = c.getColumnIndexOrThrow("cb_codigo")
            val idxReferencia = c.getColumnIndexOrThrow("it_referencia")
            val idxDescripcion = c.getColumnIndexOrThrow("it_descripcion")
            val idxPrecio = c.getColumnIndexOrThrow("cb_precio")
            val idxCostoProm = c.getColumnIndexOrThrow("it_costopromedio")

            while (c.moveToNext()) {
                productosRecomendados.add(
                    ProductoDisponible(
                        c.getString(idxCbCodigo),
                        c.getString(idxReferencia),
                        c.getString(idxDescripcion),
                        0.0,
                        c.getDouble(idxPrecio),
                        c.getDouble(idxCostoProm)
                    )
                )
            }
        }
        return productosRecomendados
    }


    fun fnBuscaReferenciaItemParo(referencia: String): List<datos> {
        val referencias = mutableListOf<datos>()
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery(
            "SELECT it_referencia, it_codigo, " +
                    "ROUND(it_almesa + it_teler + it_mmg + it_mmq + it_exhTele + it_exhVmr ,2) AS stock," +
                    "ROUND(pv_preciosubdistrib, 2) AS pv_preciosubdistrib," +
                    "ROUND(pv_precio5, 2) AS pv_precio5, " +
                    "ROUND(pv_precio6, 2) AS pv_precio6, " +
                    "it_descripcion, um_pesoCE, it_costoprom, it_regalo " +
                    "FROM ve_ws_item WHERE it_referencia LIKE '%$referencia%'AND it_formaPago = 1",
            null
        )

        if (cursor.moveToFirst()) {
            do {
                referencias.add(
                    datos(
                        referencia = cursor.getString(cursor.getColumnIndexOrThrow("it_referencia")),
                        codigo = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo")),
                        stock = cursor.getString(cursor.getColumnIndexOrThrow("stock")),
                        precioSub = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_preciosubdistrib")).toString(),
                        precioCont = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio5")).toString(),
                        precioCred = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio6")).toString(),
                        descripcion = cursor.getString(cursor.getColumnIndexOrThrow("it_descripcion")) ?: "",
                        unidadCE = cursor.getString(cursor.getColumnIndexOrThrow("um_pesoCE")) ?: "0",
                        costoProm = cursor.getDouble(cursor.getColumnIndexOrThrow("it_costoprom")).toString(),
                        combo = "0",
                        cd_codigo = "0",
                        it_regalo = cursor.getString(cursor.getColumnIndexOrThrow("it_regalo"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return referencias
    }


    fun fnVerficarClienteActualizacion(cl_codigo: String): Boolean {
        val db = DatabaseManager.openDatabase(context)
        var result = false
        val cursor = db.rawQuery(
            """
            SELECT cl_seguimiento  
            FROM ve_ws_clienteAsignadoVendedor 
            WHERE cl_codigo = ?
            """.trimIndent(),
            arrayOf(cl_codigo)
        )
        if (cursor.moveToFirst()) {
            val seguimiento = cursor.getInt(cursor.getColumnIndexOrThrow("cl_seguimiento"))
            result = seguimiento == 0
        }
        cursor.close()
        DatabaseManager.closeDatabase()
        return result
    }


    fun fnDatosClienteActualizacion(cl_codigo: String): ClienteDatos? {
        val db = DatabaseManager.openDatabase(context)
        var cliente: ClienteDatos? = null

        val cursor = db.rawQuery(
            """
        SELECT cl_codigo, cl_nombre, cl_direccion, cl_fono, cl_email
        FROM ve_ws_clienteAsignadoVendedor
        WHERE cl_codigo = ?
        """.trimIndent(),
            arrayOf(cl_codigo)
        )

        if (cursor.moveToFirst()) {
            val codigo = cursor.getString(cursor.getColumnIndexOrThrow("cl_codigo"))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("cl_nombre"))

            val direccion = cursor.getString(cursor.getColumnIndexOrThrow("cl_direccion")) ?: ""
            val fono = cursor.getString(cursor.getColumnIndexOrThrow("cl_fono")) ?: ""
            val email = cursor.getString(cursor.getColumnIndexOrThrow("cl_email")) ?: ""

            cliente = ClienteDatos(
                codigo,
                nombre,
                direccion,
                fono,
                email
            )
        }

        cursor.close()
        DatabaseManager.closeDatabase()

        return cliente
    }


    fun fnUpdateDatosCliente(cl_codigo: String, direccion: String, celular: String, email: String): Boolean {
        val db = DatabaseManager.openDatabase(context)
        return try {
            val values = ContentValues().apply {
                put("cl_direccion", direccion)
                put("cl_fono", celular)
                put("cl_email", email)
                put("cl_estado", "A")
                put("cl_seguimiento", 1)
            }
            db.update("ve_ws_clienteAsignadoVendedor", values, "cl_codigo = ?", arrayOf(cl_codigo)) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            DatabaseManager.closeDatabase()
        }
    }


    fun fnLLenarSpinnerEstadoCivil(spinner: Spinner) {
        val db = DatabaseManager.openDatabase(context)
        val items = mutableListOf<SpinnerItem>()
        db.rawQuery(
            """
            SELECT 0 AS Codigo, 'No Especific.' AS Descripcion
            UNION SELECT 1, 'Soltero'
            UNION SELECT 2, 'Casado'
            UNION SELECT 3, 'Divorciado'
            UNION SELECT 4, 'Unión Libre'
            UNION SELECT 5, 'Viudo'
            ORDER BY Codigo
            """.trimIndent(),
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                items.add(
                    SpinnerItem(
                        cursor.getInt(cursor.getColumnIndexOrThrow("Codigo")).toString(),
                        cursor.getString(cursor.getColumnIndexOrThrow("Descripcion"))
                    )
                )
            }
        }
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        DatabaseManager.closeDatabase()
    }


    fun fnLLenarSpinnerTipoPersona(spinner: Spinner) {
        val db = DatabaseManager.openDatabase(context)
        val items = mutableListOf<SpinnerItem>()
        db.rawQuery(
            """
            SELECT 'N' AS Codigo, 'Natural' AS Descripcion
            UNION SELECT 'J', 'Jurídica'
            ORDER BY 1 DESC
            """.trimIndent(),
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                items.add(
                    SpinnerItem(
                        cursor.getString(cursor.getColumnIndexOrThrow("Codigo")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Descripcion"))
                    )
                )
            }
        }
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        DatabaseManager.closeDatabase()
    }


    fun fnLLenarSpinnerGenero(spinner: Spinner) {
        val db = DatabaseManager.openDatabase(context)
        val items = mutableListOf<SpinnerItem>()
        db.rawQuery(
            """
            SELECT 'E' AS Codigo, 'Empresa' AS Descripcion
            UNION SELECT 'F', 'Femenino'
            UNION SELECT 'M', 'Masculino'
            ORDER BY Codigo
            """.trimIndent(),
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                items.add(
                    SpinnerItem(
                        cursor.getString(cursor.getColumnIndexOrThrow("Codigo")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Descripcion"))
                    )
                )
            }
        }
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        DatabaseManager.closeDatabase()
    }


    fun fnCargarDatosCiudad(): List<Adaptador> {
        val db = DatabaseManager.openDatabase(context)
        val lista = mutableListOf<Adaptador>()
        db.rawQuery("SELECT ci_codigo, ci_descripcion FROM se_ciudad", null).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Adaptador(
                        cursor.getInt(cursor.getColumnIndexOrThrow("ci_codigo")),
                        cursor.getString(cursor.getColumnIndexOrThrow("ci_descripcion"))
                    )
                )
            }
        }
        DatabaseManager.closeDatabase()
        return lista
    }


    fun fnCargarDatosCategoria(): List<Adaptador> {
        val db = DatabaseManager.openDatabase(context)
        val lista = mutableListOf<Adaptador>()
        db.rawQuery(
            """
            SELECT cc_codigo, cc_descripcion
            FROM cc_ws_clienteCategoria
            """.trimIndent(),
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Adaptador(
                        cursor.getInt(cursor.getColumnIndexOrThrow("cc_codigo")),
                        cursor.getString(cursor.getColumnIndexOrThrow("cc_descripcion"))
                    )
                )
            }
        }
        DatabaseManager.closeDatabase()
        return lista
    }


    fun fnCargarDatosParroquia(dp_codigo: Int): List<Adaptador> {
        val db = DatabaseManager.openDatabase(context)
        val lista = mutableListOf<Adaptador>()
        db.rawQuery(
            "SELECT dq_interno, dq_descripcion FROM cc_ws_dinardapParroquia where dp_codigo = ?",
            arrayOf(dp_codigo.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Adaptador(
                        cursor.getInt(cursor.getColumnIndexOrThrow("dq_interno")),
                        cursor.getString(cursor.getColumnIndexOrThrow("dq_descripcion"))
                    )
                )
            }
        }
        DatabaseManager.closeDatabase()
        return lista
    }


    fun fnCargarDatosLugar(dq_interno: Int): List<Transporte> {
        val lista = mutableListOf<Transporte>()
        val db = DatabaseManager.openDatabase(context)
        db.rawQuery(
            """
            SELECT p.dp_descripcion AS provincia, c.dc_descripcion AS canton
            FROM cc_ws_dinardapParroquia a
            INNER JOIN cc_ws_dinardapProvincia p ON a.dp_codigo = p.dp_codigo
            INNER JOIN cc_ws_dinardapCanton c 
                ON a.dp_codigo = c.dp_codigo AND a.dc_codigo = c.dc_codigo
            WHERE dq_interno = ?
            """.trimIndent(),
            arrayOf(dq_interno.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Transporte(
                        cursor.getString(cursor.getColumnIndexOrThrow("provincia")),
                        cursor.getString(cursor.getColumnIndexOrThrow("canton"))
                    )
                )
            }
        }
        DatabaseManager.closeDatabase()
        return lista
    }


    fun fnObtenerSecuenciaente(): Int {
        val db = DatabaseManager.openDatabase(context)
        var maxCodigo = 1
        db.rawQuery("SELECT MAX(en_codigo) FROM cc_ws_ente", null).use { cursor ->
            if (cursor.moveToFirst()) {
                val maxCodigoFromDb = cursor.getInt(0)
                if (maxCodigoFromDb != 0) maxCodigo = maxCodigoFromDb + 1
            }
        }
        DatabaseManager.closeDatabase()
        return maxCodigo
    }


    fun fnObtenerEntes(): List<EnteDialogFragment.Ente> {
        val db = DatabaseManager.openDatabase(context)
        val pedidos = mutableListOf<EnteDialogFragment.Ente>()
        db.rawQuery(
            """
            SELECT 
                en_codigo,
                case when en_codigoA = en_codigo then '' else en_codigoA end as en_codigoA,
                CASE 
                    WHEN en_tipopersona = 'J' THEN
                        CASE 
                            WHEN IFNULL(en_razonsocial, '') <> '' THEN en_razonsocial 
                            WHEN IFNULL(en_razoncomercial, '') <> '' THEN en_razoncomercial 
                            ELSE UPPER(
                                TRIM(IFNULL(en_apellido1, '')) || ' ' ||
                                TRIM(IFNULL(en_apellido2, '')) || ' ' ||
                                TRIM(IFNULL(en_nombre1, '')) || ' ' ||
                                TRIM(IFNULL(en_nombre2, ''))
                            )
                        END
                    ELSE
                        CASE 
                            WHEN LENGTH(
                                TRIM(IFNULL(en_nombre1,'')) ||
                                TRIM(IFNULL(en_nombre2,'')) ||
                                TRIM(IFNULL(en_apellido1,'')) ||
                                TRIM(IFNULL(en_apellido2,'')) 
                            ) <> 0 THEN
                                UPPER(
                                    TRIM(IFNULL(en_apellido1,'')) || ' ' ||
                                    TRIM(IFNULL(en_apellido2,'')) ||
                                    CASE WHEN LENGTH(TRIM(IFNULL(en_apellido2,''))) = 0 THEN '' ELSE ' ' END ||
                                    TRIM(IFNULL(en_nombre1,'')) ||
                                    CASE WHEN LENGTH(TRIM(IFNULL(en_nombre2,''))) = 0 THEN '' ELSE ' ' END ||
                                    TRIM(IFNULL(en_nombre2,''))
                                )
                            WHEN IFNULL(en_razoncomercial,'') <> '' THEN en_razoncomercial
                            WHEN IFNULL(en_razonsocial,'') <> '' THEN en_razonsocial
                        END
                END AS NombreCompleto,
                en_estado
            FROM cc_ws_ente
            WHERE date(us_fechaing) >= date('now', '-1 month')
            ORDER BY 1 DESC
            """.trimIndent(),
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val estadoDb = cursor.getString(cursor.getColumnIndexOrThrow("en_estado"))
                val estado = when (estadoDb) {
                    "A" -> "Activo"
                    "C" -> "Enviado"
                    else -> estadoDb
                } ?: ""
                pedidos.add(
                    EnteDialogFragment.Ente(
                        cursor.getString(cursor.getColumnIndexOrThrow("en_codigo")),
                        cursor.getString(cursor.getColumnIndexOrThrow("en_codigoA")) ?: "",
                        cursor.getString(cursor.getColumnIndexOrThrow("NombreCompleto")),
                        estado
                    )
                )
            }
        }
        db.close()
        return pedidos
    }


    fun fnObtenerEntexNombre(cliente: String, fechaInc: String, fechaFin: String): List<EnteDialogFragment.Ente> {
        val db = DatabaseManager.openDatabase(context)
        val pedidos = mutableListOf<EnteDialogFragment.Ente>()
        db.rawQuery(
            """
            SELECT 
                p.pr_codigo, 
                p.pr_nombre, 
                p.pr_valortotal, 
                p.pr_codigoA, 
                p.pr_estado, 
                pr_lote, 
                SUBSTR(p.pr_observacion, 1, 50) as pr_observacion,
                p.pr_fechaing
            FROM fa_ws_cabproforma p
            WHERE p.pr_nombre LIKE '$cliente%'
            AND date(substr(p.pr_fechaing, 7, 4) || '-' || substr(p.pr_fechaing, 4, 2) || '-' || substr(p.pr_fechaing, 1, 2))
            BETWEEN date('$fechaInc') AND date('$fechaFin')
            AND p.pr_estado <> 'E'
            ORDER BY p.pr_codigo DESC
            """.trimIndent(),
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val estadoDb = cursor.getString(cursor.getColumnIndexOrThrow("pr_estado"))
                val estado = when (estadoDb) {
                    "A" -> "Activo"
                    "C" -> "Enviado"
                    else -> estadoDb
                } ?: ""
                pedidos.add(
                    EnteDialogFragment.Ente(
                        cursor.getString(cursor.getColumnIndexOrThrow("pr_codigo")),
                        cursor.getString(cursor.getColumnIndexOrThrow("pr_valortotal")),
                        cursor.getString(cursor.getColumnIndexOrThrow("pr_nombre")),
                        estado
                    )
                )
            }
        }
        db.close()
        return pedidos
    }


    fun fnObtenerDatosEnte(en_codigo: Int): DatosEnte? {
        val db = DatabaseManager.openDatabase(context)
        var ente: DatosEnte? = null
        val cursor = db.rawQuery(
            """
            SELECT 
                en_codigo, 
                en_codigoA, 
                en_tipoId,
                case when en_tipoId = 36 then en_ci when en_tipoId = 37 then en_rucA end as en_ci, 
                en_nombre1, 
                en_nombre2, 
                en_apellido1, 
                en_apellido2, 
                en_razoncomercial, 
                en_razonsocial,
                en_genero, 
                en_tipopersona, 
                ci_codigo, 
                en_fechanac, 
                cl_politica, 
                cl_campania,
                c.tc_descripcion as direccion,
                d.tc_descripcion as celular,
                e.tc_descripcion as correo,
                e.en_estado,
                e.dq_interno
            FROM cc_ws_ente e
            INNER JOIN cc_ws_conctacto c on e.en_codigo=c.cl_codigo and c.tc_codigo = 626
            INNER JOIN cc_ws_conctacto d on e.en_codigo=d.cl_codigo and d.tc_codigo = 33
            INNER JOIN cc_ws_conctacto e on e.en_codigo=e.cl_codigo and e.tc_codigo = 464
            WHERE en_codigo = ?
            """.trimIndent(),
            arrayOf(en_codigo.toString())
        )
        if (cursor.moveToFirst()) {
            ente = DatosEnte(
                cursor.getInt(cursor.getColumnIndexOrThrow("en_codigo")),
                cursor.getInt(cursor.getColumnIndexOrThrow("en_codigoA")),
                cursor.getInt(cursor.getColumnIndexOrThrow("en_tipoId")),
                cursor.getString(cursor.getColumnIndexOrThrow("en_ci")) ?: "",
                cursor.getString(cursor.getColumnIndexOrThrow("en_nombre1")),
                cursor.getString(cursor.getColumnIndexOrThrow("en_nombre2")),
                cursor.getString(cursor.getColumnIndexOrThrow("en_apellido1")),
                cursor.getString(cursor.getColumnIndexOrThrow("en_apellido2")),
                cursor.getString(cursor.getColumnIndexOrThrow("en_razoncomercial")),
                cursor.getString(cursor.getColumnIndexOrThrow("en_razonsocial")),
                cursor.getString(cursor.getColumnIndexOrThrow("en_genero")),
                cursor.getString(cursor.getColumnIndexOrThrow("en_tipopersona")),
                cursor.getInt(cursor.getColumnIndexOrThrow("ci_codigo")),
                cursor.getString(cursor.getColumnIndexOrThrow("en_fechanac")),
                cursor.getInt(cursor.getColumnIndexOrThrow("cl_politica")),
                cursor.getInt(cursor.getColumnIndexOrThrow("cl_campania")),
                cursor.getString(cursor.getColumnIndexOrThrow("direccion")),
                cursor.getString(cursor.getColumnIndexOrThrow("celular")),
                cursor.getString(cursor.getColumnIndexOrThrow("correo")),
                cursor.getString(cursor.getColumnIndexOrThrow("en_estado")),
                cursor.getInt(cursor.getColumnIndexOrThrow("dq_interno"))
            )
        }
        cursor.close()
        DatabaseManager.closeDatabase()
        return ente
    }


    fun fnObtenerProvincia(ciCodigo: Int): Int {
        val db = DatabaseManager.openDatabase(context)
        var provincia = 0
        db.rawQuery(
            """
            SELECT dp.dp_codigo
            FROM se_ciudad c
            INNER JOIN se_provincia p
                ON c.pr_codigo = p.pr_codigo
            INNER JOIN cc_ws_dinardapProvincia dp
                ON replace(replace(replace(replace(replace(LOWER(p.pr_descripcion),
                    'á','a'),'é','e'),'í','i'),'ó','o'),'ú','u')
                =
                   replace(replace(replace(replace(replace(LOWER(dp.dp_descripcion),
                    'á','a'),'é','e'),'í','i'),'ó','o'),'ú','u')
            WHERE c.ci_codigo = ?
            """.trimIndent(),
            arrayOf(ciCodigo.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                provincia = cursor.getInt(0)
            }
        }
        DatabaseManager.closeDatabase()
        return provincia
    }


    fun fnObtenerCategorias(): List<Categoria> {
        val listaCategorias = mutableListOf<Categoria>()
        val db = DatabaseManager.openDatabase(context)
        db.rawQuery(
            """
            SELECT cc_codigo, cc_descripcion
            FROM cc_ws_clienteCategoria
            ORDER BY cc_codigo
            """.trimIndent(),
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    listaCategorias.add(
                        Categoria(
                            cursor.getInt(cursor.getColumnIndexOrThrow("cc_codigo")),
                            cursor.getString(cursor.getColumnIndexOrThrow("cc_descripcion")),
                            false
                        )
                    )
                } while (cursor.moveToNext())
            }
        }
        DatabaseManager.closeDatabase()
        return listaCategorias
    }


    fun fnObtenerClienteCategoriaDetalle(): List<Pair<Int, Int>> {
        val listaDetalle = mutableListOf<Pair<Int, Int>>()
        val db = DatabaseManager.openDatabase(context)
        db.rawQuery(
            """
            SELECT cl_codigo, cc_codigo
            FROM cc_ws_clienteCategoriaDetalle
            """.trimIndent(),
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    listaDetalle.add(
                        Pair(
                            cursor.getInt(cursor.getColumnIndexOrThrow("cl_codigo")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("cc_codigo"))
                        )
                    )
                } while (cursor.moveToNext())
            }
        }
        DatabaseManager.closeDatabase()
        return listaDetalle
    }


    fun fnObtenerCategoriasPorCliente(cl_odigo: Int): List<Int> {
        val listaCategorias = mutableListOf<Int>()
        val db = DatabaseManager.openDatabase(context)
        db.rawQuery(
            """
            SELECT cc_codigo
            FROM cc_ws_clienteCategoriaDetalle
            WHERE cl_codigo = ?
            """.trimIndent(),
            arrayOf(cl_odigo.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    listaCategorias.add(cursor.getInt(cursor.getColumnIndexOrThrow("cc_codigo")))
                } while (cursor.moveToNext())
            }
        }
        DatabaseManager.closeDatabase()
        return listaCategorias
    }


    fun fnBuscarReferencia(referencia: String): List<Precios> {
        val referencias = mutableListOf<Precios>()
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery(
            """
            SELECT 
                it_referencia, 
                it_codigo, 
                ROUND(it_teler + it_mmg + it_mmq + it_exhTele + it_exhVmr, 2) AS stock,
                ROUND(pv_preciosubdistrib, 3) AS pv_preciosubdistrib,
                ROUND(pv_precio5, 3) AS pv_precio5,
                ROUND(pv_precio6, 3) AS pv_precio6,
                ROUND(pv_precio7, 3) AS pv_precio7,
                it_descripcion,
                um_pesoCE,
                it_costoprom
            FROM ve_ws_item
            WHERE it_referencia LIKE ?
            ORDER BY stock DESC
            """.trimIndent(),
            arrayOf("$referencia%")
        )
        if (cursor.moveToFirst()) {
            do {
                referencias.add(
                    Precios(
                        cursor.getString(cursor.getColumnIndexOrThrow("it_referencia")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("stock")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("pv_preciosubdistrib")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio5")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio6")),
                        cursor.getString(cursor.getColumnIndexOrThrow("it_codigo")),
                        cursor.getString(cursor.getColumnIndexOrThrow("it_descripcion")) ?: "",
                        cursor.getDouble(cursor.getColumnIndexOrThrow("um_pesoCE")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("it_costoprom")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio7"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return referencias
    }


    fun fnConsultarItemXqr(codigo: String): List<ItemDetails> {
        val db = DatabaseManager.openDatabase(context)
        val items = mutableListOf<ItemDetails>()
        db.rawQuery(
            """
            SELECT it_codigo AS Codigo, it_marca AS Marca, it_referencia AS Referencia, um_sku AS Sku,
            it_descripcion AS Descripcion, (it_almesa + it_teler + it_mmg + it_mmq) AS Total, 
            ROUND(pv_preciosubdistrib, 2) AS SubDistribuidor, ROUND(pv_precio5, 2) AS Contado, 
            ROUND(pv_precio6, 2) AS Credito, ROUND(pv_precio7, 2) AS Publico, 
            ROUND(pv_precio7 * 1.15, 2) AS PublicoF, um_pesoCE AS Peso,
            it_titulo AS Titulo, it_costoprom AS costoProm,
            ROUND(it_almesa + it_teler + it_mmg + it_mmq + it_exhTele + it_exhVmr,2) AS stock, 
            it_oferta
            FROM ve_ws_itemTmp 
            WHERE it_codigo = ? OR pc_ean13 = ?
            """.trimIndent(),
            arrayOf(codigo, codigo)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val itCodigo = cursor.getString(cursor.getColumnIndexOrThrow("Codigo")) ?: ""
                items.add(
                    ItemDetails(
                        itCodigo,
                        "https://app.cotzul.com/sitenet/digital/9/$itCodigo.png",
                        cursor.getString(cursor.getColumnIndexOrThrow("Referencia")) ?: "",
                        cursor.getString(cursor.getColumnIndexOrThrow("Descripcion")) ?: "",
                        cursor.getDouble(cursor.getColumnIndexOrThrow("stock")).toInt(),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("SubDistribuidor")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("Contado")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("Credito")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("Publico")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("PublicoF")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("Peso")),
                        cursor.getString(cursor.getColumnIndexOrThrow("costoProm")) ?: "",
                        cursor.getInt(cursor.getColumnIndexOrThrow("it_oferta"))
                    )
                )
            }
        }
        DatabaseManager.closeDatabase()
        return items
    }


    fun fnObtenerCantidadBodega(bo_codigo: Int, it_codigo: String, fecha: String): Triple<String, String, String> {
        val db = DatabaseManager.openDatabase(context)
        val result = db.rawQuery(
            "SELECT co_cantidad, co_observacion, co_reconteo FROM iv_ws_conteo WHERE bo_codigo = ? AND it_codigo = ? AND co_fechatrn = ?AND co_tipo = 2",
            arrayOf(bo_codigo.toString(), it_codigo, fecha)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val cantidad = cursor.getDouble(cursor.getColumnIndexOrThrow("co_cantidad"))
                val cantidadStr = if (cantidad == 0.0) "" else cantidad.toString()
                val observacion = cursor.getString(cursor.getColumnIndexOrThrow("co_observacion")) ?: ""
                val reconteo = cursor.getDouble(cursor.getColumnIndexOrThrow("co_reconteo"))
                val reconteoStr = if (reconteo == 0.0) "" else reconteo.toString()
                Triple(cantidadStr, observacion, reconteoStr)
            } else {
                Triple("", "", "")
            }
        }
        DatabaseManager.closeDatabase()
        return result
    }


    fun fnInsertarConteo(
        it_codigo: String,
        co_tipo: Int,
        bo_codigo: Int,
        co_fechatrn: String,
        co_cantidad: String,
        co_estado: String,
        co_usuarioing: String,
        co_observacion: String,
        co_enlace: String,
        co_reconteo: String
    ): Boolean {
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.query(
            "iv_ws_conteo",
            arrayOf("it_codigo"),
            "it_codigo = ? AND bo_codigo = ? AND co_fechatrn = ?",
            arrayOf(it_codigo, bo_codigo.toString(), co_fechatrn),
            null,
            null,
            null
        )

        val resultado: Boolean
        if (cursor.moveToFirst()) {
            val values = ContentValues().apply {
                put("co_tipo", co_tipo)
                put("bo_codigo", bo_codigo)
                put("co_fechatrn", co_fechatrn)
                put("co_cantidad", co_cantidad)
                put("co_estado", co_estado)
                put("co_usuarioing", co_usuarioing)
                put("co_observacion", co_observacion)
                put("co_enlace", co_enlace)
                put("co_reconteo", co_reconteo)
            }
            val rowsUpdated = db.update(
                "iv_ws_conteo",
                values,
                "it_codigo = ? AND bo_codigo = ? AND co_fechatrn = ?",
                arrayOf(it_codigo, bo_codigo.toString(), co_fechatrn)
            )
            resultado = rowsUpdated > 0
            if (resultado) Log.d("fnInsertarConteo", "Actualización exitosa sin cambiar línea: $values")
            else Log.e("fnInsertarConteo", "Error al actualizar: $values")
        } else {
            var linea = 1
            db.rawQuery(
                """
                SELECT IFNULL(MAX(co_linea), 0) + 1 
                FROM iv_ws_conteo 
                WHERE bo_codigo = ? 
                AND co_fechatrn = ?
                """.trimIndent(),
                arrayOf(bo_codigo.toString(), co_fechatrn)
            ).use { cLinea ->
                if (cLinea.moveToFirst()) linea = cLinea.getInt(0)
            }
            val values = ContentValues().apply {
                put("it_codigo", it_codigo)
                put("co_tipo", co_tipo)
                put("bo_codigo", bo_codigo)
                put("co_fechatrn", co_fechatrn)
                put("co_cantidad", co_cantidad)
                put("co_estado", co_estado)
                put("co_usuarioing", co_usuarioing)
                put("co_observacion", co_observacion)
                put("co_enlace", co_enlace)
                put("co_reconteo", co_reconteo)
                put("co_linea", linea)
            }
            val insertResult = db.insert("iv_ws_conteo", null, values)
            resultado = insertResult != -1L
            if (resultado) Log.d("fnInsertarConteo", "Inserción exitosa: $values")
            else Log.e("fnInsertarConteo", "Error al insertar: $values")
        }
        cursor.close()
        DatabaseManager.closeDatabase()
        return resultado
    }


    fun fnInsertarConteoA(
        it_codigo: String,
        co_tipo: Int,
        bo_codigo: Int,
        co_fechatrn: String,
        co_cantidad: String,
        co_estado: String,
        co_usuarioing: String,
        co_observacion: String,
        co_enlace: String,
        co_reconteo: String
    ): Boolean {
        val db = DatabaseManager.openDatabase(context)
        val values = ContentValues().apply {
            put("it_codigo", it_codigo)
            put("co_tipo", co_tipo)
            put("bo_codigo", bo_codigo)
            put("co_fechatrn", co_fechatrn)
            put("co_cantidad", co_cantidad)
            put("co_estado", co_estado)
            put("co_usuarioing", co_usuarioing)
            put("co_observacion", co_observacion)
            put("co_enlace", co_enlace)
            put("co_reconteo", co_reconteo)
            put("co_linea", 0)
        }

        val cursor = db.query(
            "iv_ws_conteo",
            arrayOf("it_codigo"),
            "it_codigo = ? AND co_tipo = 2",
            arrayOf(it_codigo),
            null,
            null,
            null
        )

        val resultado = if (cursor.moveToFirst()) {
            val rowsUpdated = db.update(
                "iv_ws_conteo",
                values,
                "it_codigo = ? AND bo_codigo = ? AND co_fechatrn =?",
                arrayOf(it_codigo, bo_codigo.toString(), co_fechatrn)
            )
            rowsUpdated > 0
        } else {
            db.insert("iv_ws_conteo", null, values) != -1L
        }
        cursor.close()
        DatabaseManager.closeDatabase()
        return resultado
    }


    fun fnConsultarItemInventario(codigo: String, bodega: String, fecha: String): List<ItemInventario> {
        val db = DatabaseManager.openDatabase(context)
        val items = mutableListOf<ItemInventario>()
        db.rawQuery(
            """
            SELECT DISTINCT
                i.it_codigo,
                i.it_referencia AS Referencia, 
                i.it_descripcion AS Descripcion, 
                c.co_cantidad AS cantidad,
                c.co_observacion AS Observacion,
                c.co_enlace AS Enlace,
                c.co_reconteo AS Reconteo 
            FROM iv_ws_itemxbodega i
            LEFT JOIN iv_ws_conteo c ON c.it_codigo = i.it_codigo or c.it_codigo=i.pc_ean13
            WHERE i.it_codigo = ? OR i.pc_ean13 = ?
            AND c.bo_codigo = ? AND c.co_fechatrn = ?
            """.trimIndent(),
            arrayOf(codigo, codigo, bodega, fecha)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val itCodigo = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo"))
                items.add(
                    ItemInventario(
                        itCodigo,
                        "https://app.cotzul.com/sitenet/digital/9/$itCodigo.png",
                        cursor.getString(cursor.getColumnIndexOrThrow("Referencia")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Descripcion")),
                        0,
                        cursor.getString(cursor.getColumnIndexOrThrow("cantidad")) ?: "",
                        cursor.getString(cursor.getColumnIndexOrThrow("Observacion")) ?: "",
                        cursor.getString(cursor.getColumnIndexOrThrow("Enlace")) ?: "",
                        cursor.getString(cursor.getColumnIndexOrThrow("Reconteo")) ?: ""
                    )
                )
            }
        }
        DatabaseManager.closeDatabase()
        return items
    }


    fun fnInsertarGpEnlaceItem(it_codigo: String, bo_codigo: Int, co_enlace: String, fecha: String): Boolean {
        val db = DatabaseManager.openDatabase(context)
        val values = ContentValues().apply {
            put("co_enlace", co_enlace)
        }
        val result = db.update(
            "iv_ws_conteo",
            values,
            "bo_codigo  = ? AND it_codigo = ? AND co_tipo = 2 AND co_fechatrn = ?",
            arrayOf(bo_codigo.toString(), it_codigo, fecha)
        )
        DatabaseManager.closeDatabase()
        return result > 0
    }


    fun fnObtenerNombreUsuario(userCode: Int): String {
        val db = DatabaseManager.openDatabase(context)
        var nombre = ""
        db.rawQuery("SELECT vn_nombre FROM ve_ws_vendedor WHERE vn_codigo = ?", arrayOf(userCode.toString())).use { cursor ->
            if (cursor.moveToFirst()) nombre = cursor.getString(0)
        }
        DatabaseManager.closeDatabase()
        return nombre
    }


    fun fnObtenerDepartamento(userCode: Int): Int {
        val db = DatabaseManager.openDatabase(context)
        var nombre = 0
        db.rawQuery(
            "SELECT bo_codigo FROM ve_ws_vendedor WHERE vn_codigo = ? AND bo_codigo in (2,51)",
            arrayOf(userCode.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) nombre = cursor.getInt(0)
        }
        DatabaseManager.closeDatabase()
        return nombre
    }


    fun fnObtenerTipoTarjeta(codigo: String): List<String> {
        val db = DatabaseManager.openDatabase(context)
        return try {
            db.rawQuery(
                """
                SELECT
                  t.Descripcion || ' - ' || IFNULL(CAST(c.tc_comision AS TEXT), '0') || '%' AS descripcion,
                  IFNULL(CAST(c.tc_comision AS TEXT), '0') AS tc_comision
                FROM fa_ws_tipoTarjetaComision c
                INNER JOIN fa_ws_tipoTarjeta t ON c.tt_codigo = t.Codigo
                WHERE c.tc_codigo = ?
                LIMIT 1
                """.trimIndent(),
                arrayOf(codigo)
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    listOf(
                        cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                        cursor.getString(cursor.getColumnIndexOrThrow("tc_comision"))
                    )
                } else {
                    emptyList()
                }
            }
        } finally {
            DatabaseManager.closeDatabase()
        }
    }


    fun fnLLenarSpinnerFormaPago(spinner: Spinner, bodega: String) {
        val db = DatabaseManager.openDatabase(context)
        val sql = if (bodega == "1") {
            """
            SELECT Codigo, Descripcion FROM cc_ws_parametrostransaccionesxbodega
            UNION ALL
            SELECT Codigo, Descripcion FROM cc_ws_transacciones
            """.trimIndent()
        } else {
            """
            SELECT Codigo, Descripcion FROM cc_ws_parametrostransaccionesxbodega
            """.trimIndent()
        }

        db.rawQuery(sql, null).use { cursor ->
            val items = mutableListOf<SpinnerItem>()
            while (cursor.moveToNext()) {
                items.add(SpinnerItem(cursor.getString(0), cursor.getString(1)))
            }
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        DatabaseManager.closeDatabase()
    }


    fun fnObtenerItemsConteo(fecha: String, bo_codigo: Int): List<ItemScan> {
        val lista = mutableListOf<ItemScan>()
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery(
            """
            SELECT DISTINCT
                c.it_codigo AS codigo,
                b.it_referencia AS referencia,
                b.it_descripcion AS descripcion,
                c.co_linea AS Linea,
                c.co_cantidad AS cantidad
            FROM iv_ws_conteo c
            INNER JOIN iv_ws_itemxbodega b ON b.it_codigo = c.it_codigo
            WHERE c.bo_codigo= ? AND c.co_fechatrn = ? AND c.co_tipo = 1
            ORDER BY c.co_linea
            """.trimIndent(),
            arrayOf(bo_codigo.toString(), fecha)
        )
        if (cursor.moveToFirst()) {
            do {
                lista.add(
                    ItemScan(
                        cursor.getString(cursor.getColumnIndexOrThrow("codigo")) ?: "",
                        cursor.getString(cursor.getColumnIndexOrThrow("referencia")) ?: "",
                        cursor.getString(cursor.getColumnIndexOrThrow("descripcion")) ?: "",
                        cursor.getString(cursor.getColumnIndexOrThrow("cantidad")) ?: ""
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }


    fun fnObtenerCantidadItem(bo_codigo: String, it_codigo: String, fecha: String): Int? {
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery(
            """
            SELECT co_cantidad
            FROM iv_ws_conteo
            WHERE bo_codigo = ? AND it_codigo = ? AND co_fechatrn = ? AND co_tipo = 1
            """.trimIndent(),
            arrayOf(bo_codigo, it_codigo, fecha)
        )
        val cantidad = if (cursor.moveToFirst()) cursor.getString(0)?.toIntOrNull() else null
        cursor.close()
        DatabaseManager.closeDatabase()
        return cantidad
    }


    fun fnItemEan(codigo: String): String {
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery(
            """
            SELECT it_codigo
            FROM iv_ws_itemxbodega
            WHERE pc_ean13 = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(codigo.trim())
        )
        val itCodigo = if (cursor.moveToFirst()) cursor.getString(0) else ""
        cursor.close()
        db.close()
        return itCodigo
    }


    fun fnItemValido(codigo: String): String {
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery(
            """
            SELECT it_codigo
            FROM iv_ws_itemxbodega
            WHERE it_codigo = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(codigo.trim())
        )
        val itCodigo = if (cursor.moveToFirst()) cursor.getString(0) else ""
        cursor.close()
        db.close()
        return itCodigo
    }


    fun fnObtenerStockAuditoria(codigo: String): List<String> {
        val db = DatabaseManager.openDatabase(context)
        val valores = mutableListOf<String>()
        val cursor = db.rawQuery(
            "SELECT i.it_referencia AS Referencia,i.it_descripcion AS Descripcion, " +
                    "ROUND(i.it_teler,2) AS StockTelerepuesto, " +
                    "ROUND(i.it_exhTele,2) AS StockExhTelerepuestos, " +
                    "ROUND(i.it_almesa,2) AS StockPortrans, " +
                    "ROUND(i.it_mmg,2) AS StockProMarket, " +
                    "ROUND(i.it_exhVmr,2) AS StockExhProMarket, " +
                    "ROUND(i.it_mmq ,2) AS StockReparadaAlm, " +
                    "ROUND(i.it_dcp,2) AS StockDepComercial, " +
                    "ROUND(i.pv_preciosubdistrib,3) AS SubDistribuidor, " +
                    "ROUND(i.pv_desctosubdistrib,3) AS DsctoSubDistr, " +
                    "ROUND(i.pv_precio5,3) AS Contado, " +
                    "ROUND(i.pv_precio6,3) AS Credito, " +
                    "ROUND(i.um_unidadCM,3) AS CartonMaster, " +
                    "ROUND(i.um_unidadCE,2) AS CartonEstandar, " +
                    "i.um_sku AS Sku, " +
                    "ROUND(i.um_pesoCE,3) AS PesoCE " +
                    "FROM ve_ws_itemTmp i WHERE i.it_codigo = ?",
            arrayOf(codigo)
        )
        val columnNames = cursor.columnNames
        while (cursor.moveToNext()) {
            for (columnName in columnNames) {
                val index = cursor.getColumnIndex(columnName)
                val valor = if (cursor.isNull(index)) null else cursor.getString(index)
                valores.add(valor ?: "0")
            }
        }
        cursor.close()
        db.close()
        return valores
    }


    fun fnEliminarConteo(boCodigo: String, itCodigo: String, fecha: String): Boolean {
        val db = DatabaseManager.openDatabase(context)
        return try {
            val rows = db.delete(
                "iv_ws_conteo",
                "bo_codigo = ? AND it_codigo = ? AND co_fechatrn = ? AND co_tipo = 1",
                arrayOf(boCodigo.trim(), itCodigo.trim(), fecha.trim())
            )
            Log.d("DELETE_CONTEO", "rows=$rows bo=$boCodigo it=$itCodigo fecha=$fecha")
            rows > 0
        } catch (e: Exception) {
            Log.e("DELETE_CONTEO", "Error eliminando", e)
            false
        } finally {
            db.close()
        }
    }


    fun fnLLenarSpinnerBodegaAuditoria(spinner: Spinner) {
        val db = DatabaseManager.openDatabase(context)
        db.rawQuery(
            """
            SELECT 0 AS bo_codigo, 'Seleccionar' AS bo_descripcion
            UNION ALL
            SELECT bo_codigo, COALESCE(bo_descripcion,'(Sin descripción)')
            FROM iv_ws_bodega
            WHERE bo_codigo <> 0
            ORDER BY bo_codigo
            """.trimIndent(),
            null
        ).use { cursor ->
            val items = mutableListOf<SpinnerItem>()
            while (cursor.moveToNext()) {
                val id = cursor.getString(0) ?: continue
                val label = cursor.getString(1) ?: "(Sin descripción)"
                items.add(SpinnerItem(id, label))
            }

            val adapter = object : ArrayAdapter<SpinnerItem>(
                context,
                android.R.layout.simple_spinner_item,
                items
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getView(position, convertView, parent) as TextView
                    v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    return v
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getDropDownView(position, convertView, parent) as TextView
                    v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    return v
                }
            }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        DatabaseManager.closeDatabase()
    }


    fun fnItemComboCabTmp(): List<ItemDetalle> {
        val lista = mutableListOf<ItemDetalle>()
        val db = DatabaseManager.openDatabase(context)
        val cursor = db.rawQuery(
            """
            SELECT cb_descripcionA, cb_monto, cb_margen
            FROM iv_ws_itemComboCabTmp
            """.trimIndent(),
            null
        )
        while (cursor.moveToNext()) {
            lista.add(
                ItemDetalle(
                    cursor.getString(0) ?: "",
                    cursor.getString(1) ?: "",
                    cursor.getString(2) ?: ""
                )
            )
        }
        cursor.close()
        db.close()
        return lista
    }

    fun fnCrearTablasApiSiNoExisten() {
        val db = context.openOrCreateDatabase(
            "db_vendedor.db",
            Context.MODE_PRIVATE,
            null
        )

        try {
            db.beginTransaction()

            // 1. Crear tabla de configuración si no existe
            db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS se_ws_apiConfig
            (
                ac_codigo INTEGER PRIMARY KEY AUTOINCREMENT,
                ac_nombre TEXT NOT NULL,
                ac_base_url TEXT NOT NULL,
                ac_client_id TEXT NOT NULL,
                ac_usuario TEXT NOT NULL,
                ac_activo INTEGER NOT NULL DEFAULT 1 CHECK (ac_activo IN (0, 1)),
                ac_fecha_creacion TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
            )
            """.trimIndent()
            )

            // 2. Crear tabla de credencial si no existe
            db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS se_ws_apiCredencial
            (
                cr_codigo INTEGER PRIMARY KEY AUTOINCREMENT,
                cr_client_id TEXT NOT NULL,
                cr_usuario TEXT NOT NULL,
                cr_clave TEXT NOT NULL,
                cr_activo INTEGER NOT NULL DEFAULT 1 CHECK (cr_activo IN (0, 1)),
                cr_fecha_creacion TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
            )
            """.trimIndent()
            )

            // 3. Verificar si ya existe configuración
            val cursorConfig = db.rawQuery(
                """
            SELECT COUNT(*) AS total
            FROM se_ws_apiConfig
            """.trimIndent(),
                null
            )

            var existeConfig = false

            if (cursorConfig.moveToFirst()) {
                existeConfig = cursorConfig.getInt(
                    cursorConfig.getColumnIndexOrThrow("total")
                ) > 0
            }

            cursorConfig.close()

            // 4. Insertar configuración solo si no existe
            if (!existeConfig) {
                db.execSQL(
                    """
                INSERT INTO se_ws_apiConfig
                (
                    ac_nombre,
                    ac_base_url,
                    ac_client_id,
                    ac_usuario,
                    ac_activo
                )
                VALUES
                (
                    'API_COTZUL',
                    'https://api.cotzulapi.com',
                    '2',
                    'Móvil',
                    1
                )
                """.trimIndent()
                )
            }

            // 5. Verificar si ya existe credencial
            val cursorCredencial = db.rawQuery(
                """
            SELECT COUNT(*) AS total
            FROM se_ws_apiCredencial
            """.trimIndent(),
                null
            )

            var existeCredencial = false

            if (cursorCredencial.moveToFirst()) {
                existeCredencial = cursorCredencial.getInt(
                    cursorCredencial.getColumnIndexOrThrow("total")
                ) > 0
            }

            cursorCredencial.close()

            // 6. Insertar credencial solo si no existe
            if (!existeCredencial) {
                db.execSQL(
                    """
                INSERT INTO se_ws_apiCredencial
                (
                    cr_client_id,
                    cr_usuario,
                    cr_clave,
                    cr_activo
                )
                VALUES
                (
                    '2',
                    'Móvil',
                    '64b1d9b86211b83c7ba64f3049685fc96e9e249d846ad9d1b2297ecfc21a6eb7',
                    1
                )
                """.trimIndent()
                )
            }

            db.setTransactionSuccessful()

        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun fnObtenerLoteCliente(clienteId: String): Politica? {
        val db = DatabaseManager.openDatabase(context)

        return db.rawQuery(
            "SELECT cl_lotehistorico, cl_lotegerencia FROM ve_ws_clienteAsignadoVendedor WHERE cl_codigo = ?",
            arrayOf(clienteId)
        ).use { cursor ->
            if (cursor.moveToFirst()) {

                val loteHistorico = cursor
                    .getString(cursor.getColumnIndexOrThrow("cl_lotehistorico"))
                    ?.toDoubleOrNull() ?: 0.0

                val loteGerencia = cursor
                    .getString(cursor.getColumnIndexOrThrow("cl_lotegerencia"))
                    ?.toDoubleOrNull() ?: 0.0

                Politica(
                    String.format("%.2f", loteHistorico),
                    String.format("%.2f", loteGerencia)
                )

            } else {
                null
            }
        }.also {
            DatabaseManager.closeDatabase()
        }
    }


}


data class reporte(
    val codigo: String,
    val descripcion: String,
    val descuento: String,
    val cantidad: String,
    val precio: String,
    val subtotal: String,
    val ConDescuento: String
)

data class reporteRecibo(
    val transaccion: String,
    val banco: String,
    val doc: String,
    val cuenta: String,
    val fecha: String,
    val concepto: String,
    val valor: String,
    val observacion: String
)

data class Vendedor(
    val login: String, val codigo: String
)

data class SpinnerItem(
    val codigo: String, val descripcion: String
)
{

    override fun toString(): String = descripcion  // Para que ArrayAdapter muestre la descripción
}


data class Politica(val codigo: String, val descripcion: String)

data class TransporteTarifa(
    val peso: Double,
    val tarifa1: Double,
    val tarifa2: Double,
    val descripcion: String
)

data class SearchCriteria(
    val referencia: String? = null,
    val marca: String? = null,
    val tipoProducto: String? = null,
    val familia: String? = null,
    val descripcion: String? = null
)


data class Item(
    val codigo: String,
    val descripcion: String) {
    override fun toString(): String {
        return descripcion
    }
}

data class Lopd(
    val cl_lopdp: Int,
    val cl_campania: Int
)

data class Fechas(
    val incial: String,
    val final: String
)

data class ClientesRuta(
    val nombre: String,
    val fono: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val linea: Int
)

data class VentaAnual(
    val referencia: String,
    val anio: Int,
    val cantidad: Int,
    val totalVenta: String
)

data class HistorialResumen(
    val nombre: String,
    val cantidades: List<Int>
)

data class TipoTarjetaComision(
    val ttCodigo: Int,        // tc.tt_codigo
    val ttTipo: String,       // tc.tt_tipo
    val tcComision: Double, // tc.tc_comision
    val tcCodigo: Double         // tc.tc_codigo
)

data class TipoVendedor(
    val codigo: String,
    val bodega: String
)

data class UsuarioSeed(
    val vn_codigo: Int,
    val vn_login: String,
    val vn_password: String
)

data class ComboItemRow(
    val cbCodigo: String,
    val comboNombre: String,
    val fuente: String,
    val itemsTotal: Int,
    val itemsEnPedido: Int,
    val qtyHistCombo: Double,
    val linea: Int?,
    val itCodigo: String,
    val itReferencia: String,
    val itDescripcion: String,
    val enPedido: Boolean,
    val qtyHistItem: Int,
    val stock: Int,
    val lote: Double
)


data class Semana(
    val semanaNum: Int,          // Semana número (1, 2, 3...)
    val semanaNombre: String,    // Semana X (Semana 1, Semana 2, etc.)
    val fechaInicio: String,     // Fecha de inicio
    val fechaFin: String         // Fecha de fin
)


data class ConsultaRutas(
    val codigo: String,
    val sec: String,
    val cliente: String,
    val Ciudad: String,
    val dia: String,
    var observacion: String,
    var isSelectedVisita: Boolean = false,
    var isSelectedVenta: Boolean = false,
    var isSelectedCobro: Boolean = false,
    var isSelectedTelefono: Boolean = false,
    var fechaInc: String,
    var fechaFin: String,
    var fechaProceso: String
)

data class Tarifa(
    val peso: Double?,
    val tarifa1: Double?,
    val tarifa2: Double?,
    val descripcion: String?
)

data class ProductoDisponible(
    val itCodigo: String,
    val referencia: String,
    val descripcion: String,
    val stock: Double,
    val precio: Double,
    val costoPromedio: Double
)


data class HistorialVenta(
    val clienteId: String,
    val ventas: List<ProductoDisponible>
)

data class Pedido(
    val id: String,
    val items: List<ProductoDisponible>
)

data class PedidoItem(
    val itCodigo: String,
    val referencia: String,
    val descripcion: String,
    val subtotal: Double,
    val costoPromedio: Double
)


data class VentaHistorial(
    val itCodigo: String,
    val referencia: String,
    val descripcion: String,
    val cantidadVendida: Double,
    val precioVenta: Double
)

data class ClienteDatos(
    val codigo: String,
    val nombre: String,
    val direccion: String,
    val fono: String,
    val email: String
)


data class ItemInventario(
    val codigo: String,
    val imageUrl: String,
    val referencia: String,
    val description: String,
    val stock: Int,
    val cantidad: String,
    val observacion: String = "",
    val enlace: String = "",
    val reconteo: String = "0"
)

data class ItemDetails(
    val codigo: String,
    val imageUrl: String,
    val referencia: String,
    val description: String,
    val stock: Int,
    val priceSub: Double,
    val priceContado: Double,
    val priceCredito: Double,
    val pricePublico: Double,
    val pricePublicoIva: Double,
    val peso: Double,
    val costProm: String,
    val oferta: Int
)

data class Precios(
    val referencia: String,
    val stock: Double,
    val precioSub: Double,
    val precioCont: Double,
    val precioCred: Double,
    val codigo: String,
    val descripcion: String,
    val unidadCE: Double,
    val costoProm: Double,
    val pv_precio7: Double
)


data class reporteProforma(
    val codigo: String,
    val descripcion: String,
    val imageUrl: String,
    val descuento: String,
    val cantidad: String,
    val precio: String,
    val subtotal: String,
    val ConDescuento: String,
    val proceso: Int = 0
)


object DatabaseManager {
    private var instance: SQLiteDatabase? = null

    fun openDatabase(context: Context): SQLiteDatabase {
        if (instance == null || !instance!!.isOpen) {
            instance = SqLiteOpenHelper(context).readableDatabase
        }
        return instance!!
    }

    fun closeDatabase() {
        instance?.close()
        instance = null
    }
}