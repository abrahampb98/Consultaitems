package com.example.Consultaitems.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class SqLiteOpenHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "Creando la base de datos")
        crearTablas(db)
        asegurarMigracionesCriticas(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)

        // Seguridad extra:
        // Si la base ya existe pero quedó con una versión vieja o incompleta,
        // aquí se agregan columnas faltantes sin depender solo de onUpgrade().
        if (!db.isReadOnly) {
            asegurarMigracionesCriticas(db)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.e(TAG, "Actualizando la base de datos de $oldVersion a $newVersion")

        crearTablas(db)
        asegurarMigracionesCriticas(db)
    }

    private fun crearTablas(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ve_ws_vendedor (
                vn_codigo TEXT,
                vn_nombre TEXT,
                gv_tipo INTEGER,
                bo_codigo TEXT,
                bo_descripcion TEXT,
                vn_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ve_ws_clienteAsignadoVendedor (
                em_codigo INTEGER,
                en_identificacion TEXT,
                cl_codigo INTEGER,
                cl_nombre TEXT,
                vn_codigo INTEGER,
                pz_descripcion TEXT,
                pz_codigo TEXT,
                ci_codigo TEXT,
                ci_descripcion TEXT,
                tt_codigo TEXT,
                tt_codigoA TEXT,
                cl_fono TEXT,
                cl_direccion TEXT,
                cv_fechaing TEXT,
                cl_lopdp INTEGER,
                cl_lopdpusuarioing TEXT,
                cl_campania INTEGER,
                pr_codigo TEXT,
                cl_orden TEXT,
                cl_latitud TEXT,
                cl_estado TEXT,
                cl_email TEXT,
                cl_seguimiento TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ve_ws_usuario (
                vn_codigo INTEGER,
                vn_login TEXT,
                vn_password TEXT,
                vn_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cc_ws_clienteFacturaVendedor (
                cl_codigo INTEGER,
                fa_coddocumento TEXT,
                fa_sri TEXT,
                bo_descripcion TEXT,
                fa_fechafactura DATE,
                fa_valortotfact TEXT,
                bo_codigo TEXT,
                fa_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ve_ws_item (
                em_codigo INTEGER,
                bo_codigo INTEGER,
                it_codigo TEXT,
                it_referencia TEXT,
                it_descripcion TEXT,
                it_titulo TEXT,
                it_familia TEXT,
                it_marca TEXT,
                it_almesa TEXT,
                it_teler TEXT,
                it_mmg TEXT,
                it_mmq TEXT,
                pv_precio5 TEXT,
                pv_precio6 TEXT,
                pv_precio7 TEXT,
                it_fechaing TEXT,
                um_unidadCM TEXT,
                um_unidadCE TEXT,
                um_sku TEXT,
                um_pesoCE TEXT,
                pv_preciosubdistrib TEXT,
                pv_desctosubdistrib TEXT,
                pv_costoN TEXT,
                it_exhVmr TEXT,
                it_dcp TEXT,
                it_exhTele TEXT,
                it_costoprom TEXT,
                it_activaex TEXT,
                it_regalo TEXT,
                it_formaPago INTEGER DEFAULT 0,
                rTele DOUBLE DEFAULT 0,
                rPmg DOUBLE DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_tipoFactura (
                Codigo TEXT,
                Descripcion TEXT,
                tf_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cc_ws_transacciones (
                Codigo TEXT,
                Descripcion TEXT,
                tr_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ve_ws_prioridad (
                Codigo TEXT,
                Descripcion TEXT,
                pr_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_plazo (
                Codigo TEXT,
                Descripcion TEXT,
                pz_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_parametroIva (
                pi_porcentaje INTEGER,
                pi_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_parametroseguro (
                ps_porcentaje TEXT,
                ps_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ve_ws_transporte (
                tr_codigo INTEGER,
                tr_nombre TEXT,
                tr_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_transporteTarifa (
                tr_codigo INTEGER,
                tt_codigo TEXT,
                tt_descripcion TEXT,
                tt_peso TEXT,
                tt_tarifa1 TEXT,
                tt_tarifa2 TEXT,
                tt_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_cabpedidoQueue (
                pe_coddocumento INTEGER PRIMARY KEY,
                ep_codigo INTEGER,
                tr_codigo INTEGER,
                cl_codigo INTEGER,
                tp_codigo INTEGER,
                pz_codigo INTEGER,
                pe_descripcion TEXT,
                pe_valorbruto TEXT,
                pe_porcentdescuento TEXT,
                pe_valordescuento TEXT,
                pe_seguro TEXT,
                pe_flete TEXT,
                pe_valoriva TEXT,
                pe_valorTotal TEXT,
                pe_estado TEXT,
                pr_codigo TEXT,
                te_codigo TEXT,
                pe_orden TEXT,
                td_codigo TEXT,
                im_codigo TEXT,
                pe_usuarioing TEXT,
                pe_fechaing TEXT,
                pe_fechamod TEXT,
                pe_fechaelim TEXT,
                pe_coddocumentoA TEXT,
                pe_lote TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_detpedidoQueue (
                em_codigo INTEGER,
                bo_codigo INTEGER,
                pe_coddocumento INTEGER,
                ep_codigo INTEGER,
                dp_secuencia INTEGER,
                it_codigo TEXT,
                dp_cantidad INTEGER,
                dp_precio TEXT,
                dp_descripcion TEXT,
                dp_estado TEXT,
                te_codigo INTEGER,
                um_pesoCE TEXT,
                dp_porcdescuento TEXT,
                it_activaex TEXT,
                cb_codigo TEXT,
                dp_usuarioing TEXT,
                dp_fechaing TEXT,
                dp_fechamod TEXT,
                dp_fechaelim TEXT,
                dp_costoPromedio TEXT,
                dp_combo TEXT,
                it_regalo TEXT,
                dp_sugerencia INTEGER,
                PRIMARY KEY (pe_coddocumento, dp_secuencia)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_tipoDescuentoPedido (
                Codigo INTEGER,
                Descripcion TEXT,
                td_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS iv_ws_itemComboCab (
                cb_codigo INTEGER,
                cb_descripcionA TEXT,
                cb_monto TEXT,
                cb_montocp TEXT,
                cb_margen TEXT,
                cb_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS iv_ws_itemComboDet (
                cb_codigo INTEGER,
                it_codigo TEXT,
                cb_tipo TEXT,
                cb_linea TEXT,
                cb_cantidad TEXT,
                cb_precio TEXT,
                it_costopromedio TEXT,
                cb_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS iv_ws_marca (
                ma_codigo INTEGER,
                ma_descripcion TEXT,
                ma_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ve_ws_itemCliente (
                cliente TEXT,
                coditem TEXT,
                referencia TEXT,
                factura TEXT,
                sri TEXT,
                fechafactura TEXT,
                cantidad TEXT,
                precioventa TEXT,
                total TEXT,
                bo_descripcion TEXT,
                estado TEXT,
                bo_codigo TEXT,
                tipofactura TEXT,
                it_orden TEXT,
                ma_descripcion TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_auditoriapedido (
                pe_coddocumento TEXT,
                nombreCliente TEXT,
                te_descripcion TEXT,
                pe_fechaing TEXT,
                pe_valorTotal TEXT,
                fa_coddocumento TEXT,
                fa_sri TEXT,
                fa_fechafactura TEXT,
                fa_guiaremision TEXT,
                estado TEXT,
                pe_observacion TEXT,
                bodega TEXT,
                totalfact TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_cabfactura (
                bodega TEXT,
                factura TEXT,
                sri TEXT,
                fecha TEXT,
                gremision TEXT,
                gtransporte TEXT,
                valortotal TEXT,
                de_serie TEXT,
                de_claveacceso TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_detfactura (
                fa_coddocumento TEXT,
                fa_valordescuento TEXT,
                fa_rentab TEXT,
                fa_descripcion TEXT,
                it_codigo TEXT,
                it_referencia TEXT,
                gt_porcdescuento TEXT,
                gt_cant_req TEXT,
                gt_costo_prom TEXT,
                gt_preciovta TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cc_ws_transaccionesA (
                tr_codigo INTEGER,
                tr_descripcion TEXT,
                tr_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cc_ws_bancoCliente (
                cl_codigo INTEGER,
                bc_codigo TEXT,
                ba_codigo TEXT,
                ba_descripcion TEXT,
                bc_numcuenta TEXT,
                ba_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS co_ws_reciboCobranzaConCab (
                em_codigo INTEGER,
                rc_codrecibo TEXT,
                ae_codigo INTEGER,
                ep_codigoBenef INTEGER,
                ep_codigoRes INTEGER,
                rc_fecharec TEXT,
                rc_total REAL,
                rc_tipo INTEGER,
                mo_codigo INTEGER,
                rc_estado TEXT,
                rc_enlace TEXT,
                rc_codreciboA TEXT,
                rc_usuarioing TEXT,
                rc_fechaing TEXT,
                rc_usuariomod TEXT,
                rc_fechamod TEXT,
                rc_usuarioelim TEXT,
                rc_fechaelim TEXT,
                PRIMARY KEY (em_codigo, rc_codrecibo, ae_codigo)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS co_ws_reciboCobranzaConDet (
                rd_id INTEGER PRIMARY KEY AUTOINCREMENT,
                em_codigo INTEGER,
                rc_codrecibo TEXT,
                ae_codigo INTEGER,
                tr_codigo INTEGER,
                ba_codigo INTEGER,
                bc_codigo INTEGER,
                rd_documento TEXT,
                rd_ncuenta TEXT,
                rd_fecha TEXT,
                rd_concepto TEXT,
                rd_observacion TEXT,
                rd_valor REAL,
                rd_chequeprot TEXT,
                rd_estado TEXT,
                rd_usuarioing TEXT,
                rd_fechaing TEXT,
                rd_usuariomod TEXT,
                rd_fechamod TEXT,
                rd_usuarioelim TEXT,
                rd_fechaelim TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cc_ws_detalleCobro (
                bo_descripcion TEXT,
                cl_codigo INTEGER,
                fa_sri TEXT,
                cc_fechafactura TEXT,
                cc_valorfactura TEXT,
                cc_valorsaldo TEXT,
                dc_fechaing TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ve_ws_rutavendedor (
                cl_codigo INTEGER PRIMARY KEY,
                pr_saldo TEXT,
                rv_tipo TEXT,
                rv_check INTEGER,
                rv_latitud REAL,
                rv_longitud REAL,
                rv_timestamp TEXT,
                rv_fecha TEXT,
                rv_estado TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ve_ws_rutas (
                cl_codigo INTEGER PRIMARY KEY,
                pr_codigo TEXT,
                ci_codigo TEXT,
                pr_saldo TEXT,
                rv_tipo TEXT,
                rv_check INTEGER,
                rv_latitud REAL,
                rv_longitud REAL,
                rv_fecha TEXT,
                rv_estado TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS vn_ws_ventas (
                cl_codigo INTEGER,
                vn_subtotal TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS vn_ws_margen (
                mg_codigo INTEGER,
                mg_margen TEXT,
                mg_regalo REAL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_rutaVendedor (
                rv_fechainicial INTEGER,
                rv_fechafinal TEXT,
                vn_codigo INTEGER,
                cl_codigo INTEGER,
                rv_linea INTEGER,
                rv_zona INTEGER,
                rv_monto TEXT,
                rv_tipo INTEGER,
                rv_distancia TEXT,
                rv_dia TEXT,
                rv_visita INTEGER,
                rv_cobro INTEGER,
                rv_venta INTEGER,
                rv_telefono INTEGER,
                rv_fechavisita TEXT,
                rv_fechacobro TEXT,
                rv_fechaventa TEXT,
                rv_fechatelefono TEXT,
                rv_observacion TEXT,
                cc_cupoasignado TEXT,
                cc_cupodisponible TEXT,
                rv_estado TEXT,
                cl_latitud TEXT,
                cl_longitud TEXT,
                rv_fechaproceso TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ve_ws_appSincronizar (
                si_app INTEGER,
                si_contador TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_ventas (
                vn_codigo INTEGER,
                cl_codigo TEXT,
                vn_anio INTEGER,
                ve_mes INTEGER,
                it_codigo TEXT,
                ve_cantidad INTEGER,
                ve_preciovta TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_cabproforma (
                em_codigo INTEGER NOT NULL,
                bo_codigo INTEGER NOT NULL,
                pr_codigo REAL NOT NULL,
                pr_fechatrn TEXT NOT NULL,
                ep_codigo INTEGER,
                cl_codigo INTEGER,
                pr_cedula TEXT,
                pr_nombre TEXT,
                pr_direccion TEXT,
                ci_descripcion TEXT,
                pr_fono TEXT,
                pr_email TEXT,
                pr_contacto TEXT,
                tr_codigo INTEGER,
                tp_codigo INTEGER,
                pr_estadopago TEXT,
                pr_observacion TEXT,
                pr_valorbruto REAL,
                pr_valordesc REAL,
                pr_porcseguro REAL,
                pr_valorseguro REAL,
                pr_valorflete REAL,
                pr_porcdesc REAL,
                pr_valoriva REAL,
                pr_valortotal REAL,
                pr_estado TEXT,
                tc_codigo INTEGER DEFAULT 0,
                pr_usuarioing TEXT,
                pr_fechaing TEXT,
                pr_usuariomod TEXT,
                pr_fechamod TEXT,
                pr_usuarioelim TEXT,
                pr_fechaelim TEXT,
                pr_usuariogenerafac TEXT,
                pr_fechagenerafac TEXT,
                pr_estadotransporte TEXT DEFAULT 'N',
                pr_usuariotransporte TEXT,
                pr_fechatransporte TEXT,
                gc_codigo INTEGER,
                pr_codigoA REAL,
                pr_nivel INTEGER,
                pr_lote TEXT,
                fa_coddocumento TEXT,
                peso DOUBLE,
                tarifa1 DOUBLE,
                tarifa2 DOUBLE,
                descripcion TEXT,
                PRIMARY KEY (em_codigo, bo_codigo, pr_codigo, pr_fechatrn)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_detproforma (
                em_codigo INTEGER NOT NULL,
                bo_codigo INTEGER NOT NULL,
                pr_codigo REAL NOT NULL,
                dp_fechatrn TEXT NOT NULL,
                dp_secuencia INTEGER NOT NULL,
                it_codigo TEXT NOT NULL,
                it_referencia TEXT NOT NULL,
                dp_cantidad REAL,
                dp_cantidadfac REAL DEFAULT 0,
                dp_precio REAL,
                dp_preciofac REAL DEFAULT 0,
                dp_valorbruto REAL,
                um_pesoCE REAL DEFAULT 0,
                dp_porcdescto REAL DEFAULT 0,
                dp_costoPromedio REAL,
                cb_codigo REAL DEFAULT 0,
                it_regalo INTEGER DEFAULT 0,
                dp_estado TEXT,
                dp_usuarioing TEXT,
                dp_fechaing TEXT,
                dp_usuariomod TEXT,
                dp_fechamod TEXT,
                dp_usuarioelim TEXT,
                dp_fechaelim TEXT,
                dp_usuariogenerafac TEXT,
                dp_fechagenerafac TEXT,
                dp_descripcion TEXT,
                PRIMARY KEY (em_codigo, bo_codigo, pr_codigo, dp_fechatrn, dp_secuencia, it_codigo)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cc_ws_parametrostransaccionesxbodega (
                Codigo INTEGER NOT NULL,
                Descripcion TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_tipoTarjeta (
                Codigo INTEGER NOT NULL,
                Descripcion TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ve_ws_itemTmp (
                it_codigo TEXT,
                it_referencia TEXT,
                it_descripcion TEXT,
                it_titulo TEXT,
                it_familia TEXT,
                it_marca TEXT,
                it_almesa TEXT,
                it_teler TEXT,
                it_mmg TEXT,
                it_mmq TEXT,
                pv_precio5 TEXT,
                pv_precio6 TEXT,
                pv_precio7 TEXT,
                um_unidadCM TEXT,
                um_unidadCE TEXT,
                um_sku TEXT,
                um_pesoCE TEXT,
                pv_preciosubdistrib TEXT,
                pv_desctosubdistrib TEXT,
                pv_costoN TEXT,
                it_exhVmr TEXT,
                it_dcp TEXT,
                it_exhTele TEXT,
                it_costoprom TEXT,
                it_activaex TEXT,
                it_regalo TEXT,
                pc_ean13 INTEGER,
                it_oferta INTEGER DEFAULT 0,
                rTele DOUBLE DEFAULT 0,
                rPmg DOUBLE DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_tipoTarjetaComision (
                tt_codigo INTEGER NOT NULL,
                tt_tipo TEXT NOT NULL,
                tc_comision REAL NOT NULL,
                tc_codigo REAL NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS se_ciudad (
                pr_codigo INTEGER NOT NULL,
                ci_codigo INTEGER NOT NULL,
                ci_descripcion TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS se_provincia (
                pr_codigo INTEGER NOT NULL,
                pr_descripcion TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cc_ws_dinardapParroquia (
                dq_interno INTEGER NOT NULL,
                dp_codigo INTEGER NOT NULL,
                dc_codigo INTEGER NOT NULL,
                dq_descripcion TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cc_ws_dinardapProvincia (
                dp_codigo INTEGER NOT NULL,
                dp_descripcion TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cc_ws_dinardapCanton (
                dp_codigo INTEGER NOT NULL,
                dc_codigo INTEGER NOT NULL,
                dc_descripcion TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cc_ws_ente (
                en_codigo INTEGER PRIMARY KEY,
                en_codigoA INTEGER,
                en_tipoId TEXT,
                en_ci TEXT,
                en_ruc INTEGER,
                en_nombre1 TEXT,
                en_nombre2 TEXT,
                en_apellido1 TEXT,
                en_apellido2 TEXT,
                en_razoncomercial TEXT,
                en_razonsocial TEXT,
                en_genero TEXT,
                de_codigo INTEGER,
                en_agenteretencion TEXT,
                en_pagaiva TEXT,
                en_contribuyente TEXT,
                en_tipopersona TEXT,
                ci_codigo INTEGER,
                en_fechanac TEXT,
                us_fechaing TEXT,
                us_usuarioing TEXT,
                en_estado TEXT,
                cl_politica INTEGER,
                cl_campania INTEGER,
                dq_interno INTEGER,
                en_rucA TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cc_ws_conctacto (
                cl_codigo INTEGER,
                cl_codigoA INTEGER,
                co_secuencia INTEGER,
                tc_codigo INTEGER,
                tc_descripcion TEXT,
                tc_estado TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cc_ws_clienteCategoria (
                cc_codigo INTEGER NOT NULL,
                cc_descripcion TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cc_ws_clienteCategoriaDetalle (
                cl_codigo INTEGER,
                cl_codigoA INTEGER,
                cc_codigo INTEGER,
                te_codigo INTEGER,
                tc_estado TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fa_ws_ventasTmp (
                vn_codigo INTEGER,
                cl_nombre TEXT,
                cl_codigo TEXT,
                vn_anio INTEGER,
                ve_mes INTEGER,
                it_codigo TEXT,
                it_referencia TEXT,
                ve_cantidad INTEGER,
                ve_preciovta TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS iv_ws_conteo (
                it_codigo TEXT,
                co_tipo INTEGER,
                bo_codigo INTEGER,
                co_fechatrn TEXT,
                co_cantidad TEXT,
                co_estado TEXT,
                co_usuarioing TEXT,
                co_fechaing TEXT,
                co_observacion TEXT,
                co_enlace TEXT,
                co_reconteo TEXT,
                co_linea INTEGER,
                PRIMARY KEY (it_codigo, co_tipo)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS iv_ws_bodega (
                bo_codigo INTEGER,
                bo_descripcion TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS iv_ws_itemxbodega (
                bo_codigo INTEGER,
                it_codigo TEXT,
                it_referencia TEXT,
                it_descripcion TEXT,
                pc_ean13 INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS iv_ws_itemComboCabTmp (
                cb_codigo INTEGER,
                cb_descripcionA TEXT,
                cb_monto DOUBLE,
                cb_montocp DOUBLE,
                cb_margen DOUBLE
            )
            """.trimIndent()
        )
    }

    private fun asegurarMigracionesCriticas(db: SQLiteDatabase) {
        agregarColumnaSiNoExiste(db, "ve_ws_vendedor", "bo_descripcion", "TEXT")

        agregarColumnaSiNoExiste(db, "ve_ws_clienteAsignadoVendedor", "cl_estado", "TEXT")
        agregarColumnaSiNoExiste(db, "ve_ws_clienteAsignadoVendedor", "cl_email", "TEXT")
        agregarColumnaSiNoExiste(db, "ve_ws_clienteAsignadoVendedor", "cl_seguimiento", "TEXT")

        agregarColumnaSiNoExiste(db, "fa_ws_detpedidoQueue", "dp_sugerencia", "INTEGER")

        agregarColumnaSiNoExiste(db, "vn_ws_margen", "mg_regalo", "REAL")

        agregarColumnaSiNoExiste(db, "fa_ws_rutaVendedor", "rv_fechaproceso", "TEXT")

        agregarColumnaSiNoExiste(db, "ve_ws_item", "it_formaPago", "INTEGER DEFAULT 0")
        agregarColumnaSiNoExiste(db, "ve_ws_item", "rTele", "DOUBLE DEFAULT 0")
        agregarColumnaSiNoExiste(db, "ve_ws_item", "rPmg", "DOUBLE DEFAULT 0")

        agregarColumnaSiNoExiste(db, "fa_ws_cabproforma", "peso", "DOUBLE")
        agregarColumnaSiNoExiste(db, "fa_ws_cabproforma", "tarifa1", "DOUBLE")
        agregarColumnaSiNoExiste(db, "fa_ws_cabproforma", "tarifa2", "DOUBLE")
        agregarColumnaSiNoExiste(db, "fa_ws_cabproforma", "descripcion", "TEXT")

        agregarColumnaSiNoExiste(db, "fa_ws_detproforma", "dp_descripcion", "TEXT")

        agregarColumnaSiNoExiste(db, "ve_ws_itemTmp", "pc_ean13", "INTEGER")
        agregarColumnaSiNoExiste(db, "ve_ws_itemTmp", "it_oferta", "INTEGER DEFAULT 0")
        agregarColumnaSiNoExiste(db, "ve_ws_itemTmp", "rTele", "DOUBLE DEFAULT 0")
        agregarColumnaSiNoExiste(db, "ve_ws_itemTmp", "rPmg", "DOUBLE DEFAULT 0")

        agregarColumnaSiNoExiste(db, "cc_ws_clienteCategoriaDetalle", "tc_estado", "TEXT")
        agregarColumnaSiNoExiste(db, "cc_ws_ente", "en_rucA", "TEXT")

        agregarColumnaSiNoExiste(db, "iv_ws_conteo", "co_reconteo", "TEXT")
        agregarColumnaSiNoExiste(db, "iv_ws_conteo", "co_linea", "INTEGER")

        agregarColumnaSiNoExiste(db, "ve_ws_clienteAsignadoVendedor", "cl_lotehistorico", "DOUBLE DEFAULT 0")
        agregarColumnaSiNoExiste(db, "ve_ws_clienteAsignadoVendedor", "cl_lotegerencia", "DOUBLE DEFAULT 0")
    }

    private fun agregarColumnaSiNoExiste(
        db: SQLiteDatabase,
        tabla: String,
        columna: String,
        tipo: String
    ) {
        if (!existeTabla(db, tabla)) {
            Log.w(TAG, "No existe la tabla $tabla. No se puede agregar la columna $columna.")
            return
        }

        var columnaExiste = false

        db.rawQuery("PRAGMA table_info($tabla)", null).use { cursor ->
            val indexName = cursor.getColumnIndex("name")

            if (indexName == -1) {
                Log.e(TAG, "No se encontró la columna 'name' en PRAGMA table_info para la tabla $tabla")
                return
            }

            while (cursor.moveToNext()) {
                val nombreColumna = cursor.getString(indexName)
                if (columna.equals(nombreColumna, ignoreCase = true)) {
                    columnaExiste = true
                    break
                }
            }
        }

        if (!columnaExiste) {
            Log.d(TAG, "Agregando columna $columna a la tabla $tabla")
            db.execSQL("ALTER TABLE $tabla ADD COLUMN $columna $tipo")
        } else {
            //Log.d(TAG, "La columna $columna ya existe en la tabla $tabla")
        }
    }

    private fun existeTabla(db: SQLiteDatabase, tabla: String): Boolean {
        db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(tabla)
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    companion object {
        private const val TAG = "SqLiteOpenHelper"
        private const val DATABASE_NAME = "db_vendedor.db"

        // La recuperada por JADX estaba en versión 39.
        // Se deja 40 para forzar migración en instalaciones donde ya exista una base incompleta.
        private const val DATABASE_VERSION = 40
    }
}
