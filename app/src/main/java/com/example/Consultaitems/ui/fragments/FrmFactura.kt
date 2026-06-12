package com.example.Consultaitems.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.Xml
import android.view.Gravity
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.adapters.AdaptadorClientes
import com.example.Consultaitems.ui.adapters.AdaptadorItemsFacturas
import com.example.Consultaitems.ui.adapters.ConsultaFacturas
import com.example.Consultaitems.ui.adapters.Transporte
import com.example.Consultaitems.ui.adapters.TransporteAdapter
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.Item
import com.example.Consultaitems.utils.cls.SpinnerItem
import com.example.Consultaitems.utils.cls.Tarifa
import com.example.Consultaitems.utils.cls.Vendedor
import com.example.Consultaitems.utils.cls.clsObtenerDatos
import com.example.Consultaitems.utils.cls.consultaCliente
import com.example.Consultaitems.utils.cls.consultaFactura
import com.example.Consultaitems.utils.cls.consultaItemsF
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource
import java.io.StringReader
import org.xmlpull.v1.XmlPullParser

class FrmFactura : Fragment(),
    consultaCliente.OnItemSelectedListener,
    consultaItemsF.OnItemSelectedListener,
    consultaFactura.OnItemSelectedListener {

    private val COL_NUM = 0
    private val COL_REF = 1
    private val COL_DESCTO = 2
    private val COL_CANT = 3
    private val COL_PRECIO = 4
    private val COL_SUBT = 5
    private val COL_CONDESC = 6
    private val COL_PESO = 7
    private val COL_SUBPESO = 8
    private val COL_PRECIOSITEM = 9
    private val COL_CODIGO_OCULTO = 10
    private val COL_COSTOPROM_OCULTO = 11
    private val COL_PROCESO_OCULTO = 12
    private val COL_NUM_OCULTO = 13
    private val COL_COMBO_OCULTO = 14
    private val COL_REGALO_OCULTO = 15

    lateinit var dbHelper: SqLiteOpenHelper
    lateinit var solicitudSoap: SolicitudSoap
    private lateinit var llenarControles: ClsLLenarControles

    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var spinnerBodega: Spinner
    private lateinit var spinnerFormaPag: Spinner
    private lateinit var spinnerItem: Spinner
    private lateinit var txtNumero: TextView
    private lateinit var txtFecha: TextView
    lateinit var txtVendedor: TextView
    private lateinit var txtCodClientePF: TextView
    private lateinit var txtCliente: EditText
    private lateinit var txtCIdentificacionPF: EditText
    private lateinit var txtNivelPF: EditText
    private lateinit var lblseguroPF: TextView
    private lateinit var txtTransporte: AutoCompleteTextView
    private lateinit var txtFleteMontoPF: EditText
    private lateinit var txtFormaPagoPF: TextView
    private lateinit var txtObservacion: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var txtDesc: TextView
    private lateinit var tableDetalleF: TableLayout
    lateinit var btnAgregarF: ImageButton
    lateinit var btnQuitarF: ImageButton
    private lateinit var btnRegaloF: ImageButton
    private lateinit var Kilo: TextView
    private lateinit var tarifa: TextView
    private lateinit var cobertura: TextView
    private lateinit var txtCiudadViewPF: TextView
    private lateinit var txtLote: TextView
    private lateinit var btnLotePF: ImageButton
    lateinit var txtSub: TextView
    private lateinit var txtSeguro: TextView
    private lateinit var txtFlete: TextView
    private lateinit var txtRegaloPF: TextView
    private lateinit var txtDescuentoT: TextView
    lateinit var txtIva: TextView
    lateinit var txtTotal: TextView
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminar: Button
    private lateinit var btnBuscar: ImageButton
    private lateinit var btnClientePF: ImageButton
    private lateinit var btnTransportePF: ImageButton
    private lateinit var btnAdicionalesPF: ImageButton

    private var clienteSeleccionado: AdaptadorClientes.Clientes? = null
    private var filaSeleccionada: TableRow? = null
    private var filaPendiente: TableRow? = null
    private var dialogDatosAdicionales: AlertDialog? = null
    private var dialogoListaRegalo: AlertDialog? = null

    private var cl_codigo: String = ""
    private var vgsOpcionMenu: String = ""
    private var vgsEstado: String = ""
    private var Tarifa: String = ""
    private var Cobertura: String = ""
    private var bodega: String = ""
    private var tr_codigo: String = ""
    private var td_codigo: String = ""
    private var tp_codigo: String = ""
    private var tp_descripcion: String = ""
    private var ep_codigo: String = ""
    private var usuario: String = ""
    private var Nombre: String = ""
    private var DireccionDA: String = ""
    private var TelefonoDA: String = ""
    private var CiudadDA: String = ""
    private var EmailDA: String = ""
    private var ContactoDA: String = ""
    private var nivel: String = ""
    private var factura: String = ""

    private var vgiContadorDetalle: Int = 1
    private var contador: Int = 1
    private var vgiSecuencia: Int = 1
    private var vgiSupervisor: Int = 0
    private var cargandoListadoRegalo: Boolean = false
    private var vgbAutorizaDescuento: Boolean = false
    private var vgbValidoRegalo: Boolean = false
    private var unidadCE: Double = 0.0
    private var vgDseguro: Double = 0.0
    private var codFactor: Double = 0.0
    private var factor: Double = 0.0
    private var margen: Double = 0.0
    private var vgdMargenM: Double = 0.0
    private var vgdMargenP: Double = 0.0
    private var vgdMontoAceptado: Double = 0.0
    private var vgdMontoRegaloA: Double = 0.0
    private var vgdPorcEfectivo: Double = 0.0
    private var vgdPorcTarjeta: Double = 0.0

    private var DatosTarifa: List<Tarifa> = emptyList()
    private var listaItemsFacturas: MutableList<AdaptadorItemsFacturas.itemsFactura> = mutableListOf()
    private var listaSecuencia: List<String> = emptyList()

    private data class ControlesFilaDetalle(
        val tvNum: TextView,
        val etRef: EditText,
        val etDescto: EditText,
        val etCant: EditText,
        val tvPrecio: TextView,
        val tvSubtotal: TextView,
        val tvConDesc: TextView,
        val tvPeso: TextView,
        val tvSubPeso: TextView,
        val tvPreciosItem: TextView,
        val tvCodigo: TextView,
        val tvCostoProm: TextView,
        val tvProceso: TextView,
        val tvNumOculto: TextView,
        val tvCombo: TextView,
        val tvRegalo: TextView
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.frm_factura, container, false)
        dbHelper = SqLiteOpenHelper(requireContext())
        solicitudSoap = SolicitudSoap(requireContext())
        llenarControles = ClsLLenarControles(requireContext())
        fnInicializarVariables(view)
        fnLLenarControles()
        fnDesactivarControles()
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.frm_menu_proforma, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bntNuevoPF -> { fnAccionesAlPulsarNuevo(); true }
            R.id.btnModificarPF -> { fnAccionesAlPulsarModificar(); true }
            //R.id.btnConsultarPF -> { fnFacturas(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun fnInicializarVariables(view: View) {
        constraintLayout = view.findViewById(R.id.constraintPF)
        spinnerBodega = view.findViewById(R.id.spinnerBodegaPF)
        spinnerFormaPag = view.findViewById(R.id.spinnerFormaPagPF)
        spinnerItem = view.findViewById(R.id.spinnerItemPF)
        txtNumero = view.findViewById(R.id.txtNumeroPF)
        txtFecha = view.findViewById(R.id.txtFechaPF)
        txtVendedor = view.findViewById(R.id.txtVendedorPF)
        txtCodClientePF = view.findViewById(R.id.txtCodClientePF)
        txtCliente = view.findViewById(R.id.txtClientePF)
        txtCIdentificacionPF = view.findViewById(R.id.txtCIdentificacionPF)
        txtNivelPF = view.findViewById(R.id.txtNivelPF)
        lblseguroPF = view.findViewById(R.id.lblseguroPF)
        txtTransporte = view.findViewById(R.id.txtTransportePF)
        txtFleteMontoPF = view.findViewById(R.id.txtFleteMontoPF)
        txtFormaPagoPF = view.findViewById(R.id.txtFormaPagoPF)
        txtObservacion = view.findViewById(R.id.txtObservacionPF)
        radioGroup = view.findViewById(R.id.radioGroupEncuesta)
        txtDesc = view.findViewById(R.id.txtDescuentoF)
        tableDetalleF = view.findViewById(R.id.tableDetalleF)
        btnAgregarF = view.findViewById(R.id.btnAgregarF)
        btnQuitarF = view.findViewById(R.id.btnRemoverF)
        btnRegaloF = view.findViewById(R.id.btnRegaloF)
        Kilo = view.findViewById(R.id.txtKiloViewPF)
        tarifa = view.findViewById(R.id.txtTarifaViewPF)
        cobertura = view.findViewById(R.id.txtCoberturaViewPF)
        txtCiudadViewPF = view.findViewById(R.id.txtCiudadViewPF)
        txtLote = view.findViewById(R.id.txtLotePF)
        btnLotePF = view.findViewById(R.id.btnLotePF)
        txtSub = view.findViewById(R.id.txtSubotalPF)
        txtSeguro = view.findViewById(R.id.txtSeguroPF)
        txtFlete = view.findViewById(R.id.txtFletePF)
        txtRegaloPF = view.findViewById(R.id.txtRegaloPF)
        txtDescuentoT = view.findViewById(R.id.txtDescuentoTPF)
        txtIva = view.findViewById(R.id.txtIvaPF)
        txtTotal = view.findViewById(R.id.txtTotalPvPF)
        btnGuardar = view.findViewById(R.id.btnGuardarPF)
        btnEliminar = view.findViewById(R.id.btnEliminarDocPF)
        btnBuscar = view.findViewById(R.id.btnBuscarPF)
        btnClientePF = view.findViewById(R.id.btnClientePF)
        btnTransportePF = view.findViewById(R.id.btnTransportePF)
        btnAdicionalesPF = view.findViewById(R.id.btnAdicionalesPF)

        txtFecha.text = fnFecha()

        btnAgregarF.setOnClickListener {
            fnAgregarDetalle()
            hideSoftKeyboard()
        }
        btnQuitarF.setOnClickListener { fnAccionesBotonQuitar() }
        btnGuardar.setOnClickListener {
            when {
                tableDetalleF.childCount <= 0 -> fnMensajeSistema("Ingrese lineas de detalle")
                vgbValidoRegalo -> fnGuardarDatos()
                else -> fnListadoRegalo(6)
            }
        }
        btnEliminar.setOnClickListener { fnMostrarDialogoEliminar() }
        btnBuscar.setOnClickListener { fnFacturas() }
        btnClientePF.setOnClickListener { fnCliente() }
        btnTransportePF.setOnClickListener { fnCambioTransporte() }
        btnAdicionalesPF.setOnClickListener {
            if (cl_codigo.isNotBlank() && dialogDatosAdicionales?.isShowing != true) fnDatosAdicionales()
        }
        btnLotePF.setOnClickListener { fnAutorizaRentabilidad() }
        btnRegaloF.setOnClickListener { fnAccionesBotonListadoRegalo() }

        txtDesc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                val descuento = s?.toString()?.toDoubleOrNull() ?: 0.0
                if (!vgbAutorizaDescuento) {
                    val maximo = if (tp_codigo == "1") vgdPorcEfectivo else vgdPorcTarjeta
                    if (descuento > maximo) {
                        fnMostrarAlertaAutorizacion()
                        return
                    }
                }
                if (td_codigo.toIntOrNull() == 1) fnActualizarDescuento()
            }
        })

        txtFleteMontoPF.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) { fnCalcularTotales() }
        })

        spinnerFormaPag.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = parent?.getItemAtPosition(position) as? SpinnerItem ?: return
                tp_codigo = item.codigo
                tp_descripcion = item.descripcion
                txtFormaPagoPF.text = "Forma Pago: ${item.descripcion}"

                if (tp_codigo == "3" && vgsOpcionMenu == "I") {
                    fnFormaPago()
                } else if (vgsOpcionMenu == "M" && codFactor != 0.0) {
                    runCatching {
                        val datos = llenarControles.fnObtenerTipoTarjeta(codFactor.toString())
                        if (datos.isNotEmpty()) {
                            txtFormaPagoPF.text = "${txtFormaPagoPF.text} - ${datos[0]}"
                            factor = datos.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                        }
                    }
                } else {
                    factor = 0.0
                }
                fnCalcularTotales()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        spinnerItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = parent?.getItemAtPosition(position) as? SpinnerItem ?: return
                td_codigo = item.codigo
                fnCalcularTotales()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        spinnerBodega.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = parent?.getItemAtPosition(position) as? SpinnerItem ?: return
                bodega = item.codigo
                if (vgsOpcionMenu == "I") {
                    fnObtenerSecuencia()
                    runCatching { llenarControles.fnLLenarSpinnerFormaPago(spinnerFormaPag, bodega) }
                    if (bodega == "1") spinnerFormaPag.setSelection(3)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }


    private fun fnLLenarControles() {
        runCatching { llenarControles.fnLLenarSpinner(spinnerItem, "fa_ws_tipoDescuentoPedido") }
            .onFailure { llenarControles.fnLLenarSpinner(spinnerItem, "fa_ws_tipoFactura") }

        runCatching { llenarControles.fnLLenarSpinnerFormaPago(spinnerFormaPag, bodega) }
            .onFailure { llenarControles.fnLLenarSpinner(spinnerFormaPag, "cc_ws_transacciones") }

        runCatching { llenarControles.fnLLenarSpinnerFactura(spinnerBodega, "ve_ws_vendedor") }
            .onFailure { llenarControles.fnLLenarSpinner(spinnerBodega, "fa_ws_bodega") }

        runCatching { margen = llenarControles.fnObtenerMargen() }

        runCatching {
            val vendedor: Vendedor? = llenarControles.fnLLenarVendedor()
            if (vendedor != null) {
                usuario = vendedor.login
                ep_codigo = vendedor.codigo
                txtVendedor.text = usuario
            } else {
                txtVendedor.text = llenarControles.fnObtenerVendedor()
            }
        }.onFailure {
            txtVendedor.text = runCatching { llenarControles.fnObtenerVendedor() }.getOrDefault("")
        }

        runCatching {
            val adaptadorTransporte = TransporteAdapter(requireContext(), llenarControles.fnCargarDatosTransporte())
            txtTransporte.setAdapter(adaptadorTransporte)
            txtTransporte.setOnItemClickListener { _, _, position, _ ->
                val transporte: Transporte? = adaptadorTransporte.getItem(position)
                if (transporte != null) {
                    txtTransporte.setText(transporte.nombre, false)
                    tr_codigo = transporte.codigo
                    fnPesoTarifaA()
                    if ((txtSub.text.toString().toDoubleOrNull() ?: 0.0) != 0.0) {
                        fnCalcularTotales()
                    }
                }
            }
        }
    }


    fun fnAccionesAlPulsarNuevo() {
        if (vgsOpcionMenu == "M" && txtNumero.text.toString().isNotBlank()) {
            txtNumero.text.toString().toIntOrNull()?.let { fnAccesoUsuario(it, 2) }
            fnLimpiarControles()
        }

        fnLimpiarControles()
        vgsOpcionMenu = "I"
        btnGuardar.text = "Guardar"
        fnObtenerSecuencia()
        fnActivarControles()

        txtCodClientePF.isEnabled = true
        txtCodClientePF.requestFocus()
        txtNumero.isEnabled = false
        btnBuscar.isEnabled = false
        btnEliminar.isEnabled = false
        btnTransportePF.isEnabled = false
        vgsEstado = "A"
        constraintLayout.background = ContextCompat.getDrawable(requireContext(), R.drawable.background)

        if (bodega == "1") spinnerFormaPag.setSelection(3)
    }


    fun fnAccionesAlPulsarModificar() {
        if (vgsOpcionMenu == "M" && txtNumero.text.toString().isNotBlank()) {
            txtNumero.text.toString().toIntOrNull()?.let { fnAccesoUsuario(it, 2) }
            fnLimpiarControles()
        }

        vgsOpcionMenu = "M"
        fnObtenerSecuencia()
        fnDesactivarControles()
        fnLimpiarControles()

        txtNumero.isEnabled = true
        txtNumero.requestFocus()
        btnBuscar.isEnabled = true
        btnGuardar.text = "Modificar"
        constraintLayout.background = ContextCompat.getDrawable(requireContext(), R.drawable.background)
    }


    fun fnGuardarFactura() {
        if (!fnVerificarControles()) return

        if (fnValidarMargenTipoCliente() == 1.toShort() && vgiSupervisor == 0) {
            val mensaje = if (nivel == "7") {
                "El Margen de Rentabilidad de Clientes Publicos no puede ser menor a 1.80. Solicitar Autorizacion."
            } else {
                "El Margen de Rentabilidad de Clientes Mayoristas no puede ser menor a 1.65. Solicitar Autorizacion."
            }
            fnMensajeSistema(mensaje)
            btnGuardar.isEnabled = true
            btnLotePF.isEnabled = true
            return
        }

        if (tableDetalleF.childCount <= 0) {
            fnMensajeSistema("Ingrese lineas de detalle")
            return
        }

        val fila = tableDetalleF.getChildAt(tableDetalleF.childCount - 1) as? TableRow ?: return
        val c = fnObtenerControlesFila(fila)

        val codigo = c.tvCodigo.text.toString()
        val costoProm = c.tvCostoProm.text.toString().toDoubleOrNull() ?: 0.0
        val proceso = c.tvProceso.text.toString().toIntOrNull() ?: 0
        val cantidad = c.etCant.text.toString().toDoubleOrNull() ?: 0.0
        val precio = c.tvPrecio.text.toString().toDoubleOrNull() ?: 0.0
        val descuento = c.etDescto.text.toString().toDoubleOrNull() ?: 0.0
        val secuencia = c.tvNumOculto.text.toString().toIntOrNull() ?: vgiSecuencia

        val onOk: (String) -> Unit = { respuesta ->
            val valores = fnObtenerValoresXml(respuesta)
            val estado = valores.firstOrNull()?.trim().orEmpty()
            when (estado) {
                "-1" -> fnAlertStock(valores) { acepto, _ ->
                    if (acepto) {
                        btnGuardar.isEnabled = false
                        fnGuardarFactura()
                    } else {
                        btnGuardar.isEnabled = true
                    }
                }
                "-3" -> {
                    fnAlertItemSinStock()
                    btnGuardar.isEnabled = true
                }
                else -> {
                    fnBloquearFila(fila)
                    val documento = txtNumero.text.toString().toIntOrNull() ?: 0
                    if (cl_codigo == "1774" && vgsOpcionMenu == "I") {
                        fnProcesarClientePublico(documento)
                    } else {
                        fnFacturaPorcDescto(documento)
                    }
                    if (txtCiudadViewPF.text.toString() != "GUAYAQUIL") {
                        btnTransportePF.isEnabled = true
                    }
                }
            }
        }

        if (proceso == 1) {
            fnTransaccionFactura(
                sec = 0,
                codigo = "0",
                cantidad = 0.0,
                costProm = 0.0,
                precio = 0.0,
                descuento = 0.0,
                opcionItem = "I",
                proceso = 1,
                accion = 1,
                secGrid = vgiContadorDetalle,
                onResult = onOk
            )
        } else {
            fnTransaccionFactura(
                sec = secuencia,
                codigo = codigo,
                cantidad = cantidad,
                costProm = costoProm,
                precio = precio,
                descuento = descuento,
                opcionItem = "I",
                proceso = proceso,
                accion = 1,
                secGrid = vgiContadorDetalle,
                onResult = onOk
            )
        }
    }


    fun fnGuardarDatos() {
        if (!fnVerificarControles()) return

        if (tableDetalleF.childCount <= 0) {
            fnMensajeSistema("Ingrese lineas de detalle")
            return
        }

        val ultimaFila = tableDetalleF.getChildAt(tableDetalleF.childCount - 1) as? TableRow
        if (ultimaFila != null) {
            val codigo = (ultimaFila.getChildAt(COL_CODIGO_OCULTO) as? TextView)?.text?.toString().orEmpty()
            if (codigo.isBlank()) fnEliminarFila(ultimaFila)
        }

        if (tableDetalleF.childCount <= 0) {
            fnMensajeSistema("Ingrese lineas de detalle")
            return
        }

        fnMostrarDialogoDeConfirmacion()
    }

    fun showResultDialog() {
        val accion = when (vgsOpcionMenu) {
            "I" -> "Guardados"
            "M" -> "Actualizados"
            "E" -> "Eliminados"
            else -> "Procesados"
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("Datos $accion Correctamente")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
        fnDesactivarControles()
        if (vgsOpcionMenu == "I" || vgsOpcionMenu == "M") {
            txtNumero.text?.toString()?.toIntOrNull()?.let { fnAccesoUsuario(it, 2) }
        }
    }

    fun fnDesactivarControles() {
        listOf<View>(
            spinnerBodega, spinnerFormaPag, spinnerItem, txtCliente, txtCIdentificacionPF,
            txtNivelPF, txtTransporte, txtFleteMontoPF, txtObservacion,
            btnAgregarF, btnQuitarF, btnRegaloF, btnGuardar, btnEliminar,
            btnClientePF, btnTransportePF, btnAdicionalesPF, btnLotePF
        ).forEach { it.isEnabled = false }
    }

    fun fnActivarControles() {
        listOf<View>(
            spinnerBodega, spinnerFormaPag, spinnerItem, txtCliente, txtCIdentificacionPF,
            txtNivelPF, txtTransporte, txtFleteMontoPF, txtObservacion,
            btnAgregarF, btnQuitarF, btnRegaloF, btnGuardar, btnEliminar,
            btnClientePF, btnTransportePF, btnAdicionalesPF, btnLotePF
        ).forEach { it.isEnabled = true }
    }

    fun fnActivarCabecera() { fnActivarControles() }
    fun fnDesactivarCabecera() { fnDesactivarControles() }

    fun fnLimpiarControles() {
        txtCodClientePF.text = ""
        txtCliente.setText("")
        txtCIdentificacionPF.setText("")
        txtNivelPF.setText("")
        txtTransporte.setText("", false)
        txtFleteMontoPF.setText("0.00")
        txtFormaPagoPF.text = ""
        txtObservacion.text = ""
        txtSub.text = "0.00"
        txtSeguro.text = "0.00"
        txtFlete.text = "0.00"
        txtRegaloPF.text = "0.00"
        txtDescuentoT.text = "0.00"
        txtIva.text = "0.00"
        txtTotal.text = "0.00"
        fnLimpiarTabla()
    }

    private fun fnLimpiarTabla() { tableDetalleF.removeAllViews(); contador = 1; vgiSecuencia = 1 }


    fun fnVerificarControles(): Boolean {
        if (cl_codigo.isBlank() && txtCodClientePF.text.isNullOrBlank()) {
            fnMensajeSistema("Debe seleccionar un cliente")
            return false
        }

        if (radioGroup.checkedRadioButtonId == -1) {
            fnMensajeSistema("Debe seleccionar una opción de la encuesta")
            return false
        }

        return true
    }

    private fun fnAgregarDetalle() { fnCrearFilanueva() }

    fun fnCrearFilanueva() {
        val row = TableRow(requireContext()).apply {
            tag = DetalleFila(
                codigo = "",
                costoProm = 0.0,
                proceso = 0,
                cantidad = 1.0,
                precio = 0.0,
                descuento = 0.0,
                secuencia = vgiSecuencia,
                combo = 0,
                num = contador
            )

            setOnClickListener {
                filaSeleccionada = this
                fnBloquearFila(this)
            }
        }

        val tvNum = fnCreateCellW(contador.toString(), 0.7f, Gravity.CENTER)

        val etRef = fnCreateRefEditTextW(
            weight = 2.3f,
            onTyping = { ref, editText ->
                val fila = editText.parent as? TableRow ?: return@fnCreateRefEditTextW
                filaPendiente = fila

                fnLimpiarValoresFila(fila)

                if (ref.isBlank()) {
                    return@fnCreateRefEditTextW
                }
            },
            onDone = { ref, editText ->
                hideKeyboard(editText)

                val fila = editText.parent as? TableRow ?: return@fnCreateRefEditTextW
                filaPendiente = fila

                if (ref.isBlank()) {
                    fnLimpiarValoresFila(fila)
                    return@fnCreateRefEditTextW
                }

                fnItems(ref)
            }
        )

        val etDescto = fnCreateDesctoEditTextW(0.8f)
        val etCant = fnCreateCantEditTextW(0.9f) { fila ->
            val c = fnObtenerControlesFila(fila)
            val codigo = c.tvCodigo.text.toString().trim()
            if (codigo.isNotEmpty()) {
                fnCalcularFilaActual(
                    c.etCant,
                    c.etDescto,
                    c.tvPrecio,
                    c.tvSubtotal,
                    c.tvConDesc,
                    c.tvPeso,
                    c.tvSubPeso,
                    codigo
                )
            }
        }

        val tvPrecio = fnCreateCellW("0.00", 1f, Gravity.END)
        val tvSubtotal = fnCreateCellW("0.00", 1f, Gravity.END)
        val tvConDesc = fnCreateCellW("0.00", 1f, Gravity.END)
        val tvPeso = fnCreateCellW("0.00", 0.9f, Gravity.END)
        val tvSubPeso = fnCreateCellW("0.00", 0.9f, Gravity.END)

        val tvPreciosItem = fnCreateHiddenCell("0.00")
        val tvCodigo = fnCreateHiddenCell("")
        val tvCostoProm = fnCreateHiddenCell("0")
        val tvProceso = fnCreateHiddenCell("0")
        val tvNumOculto = fnCreateHiddenCell(vgiSecuencia.toString())
        val tvCombo = fnCreateHiddenCell("0")
        val tvRegalo = fnCreateHiddenCell("0")

        row.addView(tvNum)
        row.addView(etRef)
        row.addView(etDescto)
        row.addView(etCant)
        row.addView(tvPrecio)
        row.addView(tvSubtotal)
        row.addView(tvConDesc)
        row.addView(tvPeso)
        row.addView(tvSubPeso)
        row.addView(tvPreciosItem)
        row.addView(tvCodigo)
        row.addView(tvCostoProm)
        row.addView(tvProceso)
        row.addView(tvNumOculto)
        row.addView(tvCombo)
        row.addView(tvRegalo)

        tableDetalleF.addView(row)
        filaSeleccionada = row
        fnBloquearFila(row)

        contador++
        vgiSecuencia++
    }

    private fun fnCreateCellW(text: String, weight: Float, gravity: Int): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            this.gravity = gravity
            setPadding(6, 4, 6, 4)
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight)
        }
    }

    private fun fnCreateHiddenCell(text: String): TextView =
        fnCreateCellW(text, 0f, Gravity.CENTER).apply { visibility = View.GONE }

    private fun fnCreateButton(weight: Float): ImageButton =
        ImageButton(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight)
        }

    private fun fnCreateCantEditTextW(weight: Float, onDone: (TableRow) -> Unit): EditText =
        EditText(requireContext()).apply {
            setText("1")
            gravity = Gravity.CENTER
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_DONE
            setPadding(8, 8, 8, 8)
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.black))
            layoutParams = fnCellParams(weight)

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                override fun afterTextChanged(s: Editable?) {
                    val fila = parent as? TableRow ?: return
                    val c = fnObtenerControlesFila(fila)
                    val codigo = c.tvCodigo.text.toString().trim()

                    if (codigo.isNotEmpty()) {
                        fnCalcularFilaActual(
                            c.etCant,
                            c.etDescto,
                            c.tvPrecio,
                            c.tvSubtotal,
                            c.tvConDesc,
                            c.tvPeso,
                            c.tvSubPeso,
                            codigo
                        )
                    }
                }
            })

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val fila = parent as? TableRow
                    if (fila != null) {
                        hideKeyboard(this)
                        onDone(fila)
                    }
                    true
                } else {
                    false
                }
            }
        }

    private fun fnCreateDesctoEditTextW(weight: Float): EditText =
        EditText(requireContext()).apply {
            setText("0")
            gravity = Gravity.CENTER
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_DONE
            setPadding(8, 8, 8, 8)
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.black))
            layoutParams = fnCellParams(weight)

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                override fun afterTextChanged(s: Editable?) {
                    val fila = parent as? TableRow ?: return
                    val c = fnObtenerControlesFila(fila)
                    val codigo = c.tvCodigo.text.toString().trim()

                    if (codigo.isNotEmpty()) {
                        fnCalcularFilaActual(
                            c.etCant,
                            c.etDescto,
                            c.tvPrecio,
                            c.tvSubtotal,
                            c.tvConDesc,
                            c.tvPeso,
                            c.tvSubPeso,
                            codigo
                        )
                    }
                }
            })
        }

    private fun fnCreateRefEditTextW(
        weight: Float,
        onTyping: (String, EditText) -> Unit,
        onDone: (String, EditText) -> Unit
    ): EditText =
        EditText(requireContext()).apply {
            hint = "Referencia..."
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_DONE
            setPadding(12, 8, 12, 8)
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.black))
            layoutParams = fnCellParams(weight)

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    onTyping(s?.toString()?.trim().orEmpty(), this@apply)
                }

                override fun afterTextChanged(s: Editable?) = Unit
            })

            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val ref = v.text.toString().trim()
                    if (ref.isNotEmpty()) {
                        onDone(ref, this)
                    }
                    true
                } else {
                    false
                }
            }
        }

    private fun fnCellParams(weight: Float): TableRow.LayoutParams =
        TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight)

    private fun fnObtenerControlesFila(fila: TableRow): ControlesFilaDetalle {
        return ControlesFilaDetalle(
            tvNum = fila.getChildAt(COL_NUM) as TextView,
            etRef = fila.getChildAt(COL_REF) as EditText,
            etDescto = fila.getChildAt(COL_DESCTO) as EditText,
            etCant = fila.getChildAt(COL_CANT) as EditText,
            tvPrecio = fila.getChildAt(COL_PRECIO) as TextView,
            tvSubtotal = fila.getChildAt(COL_SUBT) as TextView,
            tvConDesc = fila.getChildAt(COL_CONDESC) as TextView,
            tvPeso = fila.getChildAt(COL_PESO) as TextView,
            tvSubPeso = fila.getChildAt(COL_SUBPESO) as TextView,
            tvPreciosItem = fila.getChildAt(COL_PRECIOSITEM) as TextView,
            tvCodigo = fila.getChildAt(COL_CODIGO_OCULTO) as TextView,
            tvCostoProm = fila.getChildAt(COL_COSTOPROM_OCULTO) as TextView,
            tvProceso = fila.getChildAt(COL_PROCESO_OCULTO) as TextView,
            tvNumOculto = fila.getChildAt(COL_NUM_OCULTO) as TextView,
            tvCombo = fila.getChildAt(COL_COMBO_OCULTO) as TextView,
            tvRegalo = fila.getChildAt(COL_REGALO_OCULTO) as TextView
        )
    }


    fun fnCalcularTotales() {
        var subtotal = 0.0
        var costoPromedioTotal = 0.0
        var descuentoTotal = 0.0
        unidadCE = 0.0
        vgdMontoRegaloA = 0.0

        for (i in 0 until tableDetalleF.childCount) {
            val fila = tableDetalleF.getChildAt(i) as? TableRow ?: continue
            val c = runCatching { fnObtenerControlesFila(fila) }.getOrNull() ?: continue

            val cantidad = c.etCant.text.toString().toDoubleOrNull() ?: 0.0
            val subtotalFila = c.tvSubtotal.text.toString().toDoubleOrNull() ?: 0.0
            val costoProm = c.tvCostoProm.text.toString().toDoubleOrNull() ?: 0.0
            val descuento = c.etDescto.text.toString().toDoubleOrNull() ?: 0.0
            val peso = c.tvPeso.text.toString().toDoubleOrNull() ?: 0.0

            subtotal += subtotalFila
            costoPromedioTotal += costoProm * cantidad
            descuentoTotal += descuento
            unidadCE += cantidad * peso
            vgdMontoRegaloA += cantidad * costoProm
        }

        fnTransporte()

        val descuentoPercent = if (td_codigo == "1") {
            txtDesc.text.toString().toDoubleOrNull() ?: 0.0
        } else {
            descuentoTotal
        }

        val subtotalDescontado = if (descuentoPercent > 0.0) {
            subtotal - ((subtotal * descuentoPercent) / 100.0)
        } else {
            subtotal
        }

        val seguroPercent = if (vgDseguro > 0.0) {
            runCatching { llenarControles.fnObtenerSeguro() / 100.0 }.getOrDefault(vgDseguro / 100.0)
        } else {
            0.0
        }

        val montoSeguro = subtotalDescontado * seguroPercent
        val subtotalFinal = subtotalDescontado + montoSeguro
        val iva = (fnIva() * subtotalFinal) / 100.0
        fnPesos()

        val tarifaValor = Tarifa.replace("$", "").toDoubleOrNull() ?: 0.0
        val flete = unidadCE * tarifaValor
        val total = subtotalFinal + iva + flete
        val lote = if (costoPromedioTotal > 0.0) subtotalDescontado / costoPromedioTotal else 0.0

        txtDescuentoT.text = fnDecimal(subtotal - subtotalDescontado)
        txtSub.text = fnDecimal(subtotal)
        txtSeguro.text = fnDecimal(montoSeguro)
        txtIva.text = fnDecimal(iva)
        txtTotal.text = fnDecimal(total)
        txtLote.text = fnDecimal(lote)
        txtFlete.text = fnDecimal(flete)

        if (tableDetalleF.childCount > 0) fnCalcularMontoRegalo()
    }

    private fun fnIva(): Int = 15
    private fun fnFecha(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    private fun fnFechaCorta(): String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    private fun fnFechaHora(): String = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    private fun fnFechaxml(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
    private fun fnFechaDescuentoxml(): String = fnFechaxml()
    private fun fnDecimal(valor: Double): String = String.format(Locale.US, "%.2f", valor)

    fun fnMensajeSistema(mensaje: String) {
        AlertDialog.Builder(requireContext()).setTitle("Sistema").setMessage(mensaje)
            .setPositiveButton("Aceptar", null).show()
    }

    private fun fnMensajeSistemaPregunta(mensaje: String, mostrarCancelar: Boolean, onResult: (Int) -> Unit) {
        val builder = AlertDialog.Builder(requireContext()).setTitle("Sistema").setMessage(mensaje)
        if (mostrarCancelar) builder.setNegativeButton("Cancelar") { _, _ -> onResult(0) }
        builder.setPositiveButton("Aceptar") { _, _ -> onResult(1) }.show()
    }

    private fun showToast(message: String) = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    fun hideKeyboard(view: View) { (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(view.windowToken, 0) }
    private fun hideSoftKeyboard() { view?.let { hideKeyboard(it) } }
    fun forceRedrawWindow(activity: Activity) { activity.window?.decorView?.invalidate() }

    fun fnIsNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun openPdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }

    fun showProgressDialogItems(): ProgressDialog = ProgressDialog(requireContext()).apply { setMessage("Consultando items..."); setCancelable(false); show() }
    private fun showProgressDialogFactura(): ProgressDialog = ProgressDialog(requireContext()).apply { setMessage("Procesando factura..."); setCancelable(false); show() }

    fun fnActualizarDescuento() { fnCalcularTotales() }
    private fun disableEdit(editText: EditText) { editText.isEnabled = false }
    private fun enableEdit(editText: EditText) { editText.isEnabled = true }
    private fun focusAndShowKeyboard(et: EditText) { et.requestFocus(); (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.showSoftInput(et, 0) }

    fun fnCalcularFilaActual(
        etCant: EditText,
        etDescto: EditText,
        tvPrecio: TextView,
        tvSubtotal: TextView,
        tvConDesc: TextView,
        tvPeso: TextView,
        tvSubPeso: TextView,
        codigo: String
    ) {
        val fila = etCant.parent as? TableRow

        val cant = etCant.text.toString().toDoubleOrNull() ?: 0.0
        val desc = etDescto.text.toString().toDoubleOrNull() ?: 0.0
        val precio = tvPrecio.text.toString().toDoubleOrNull() ?: 0.0
        val pesoUnitario = tvPeso.text.toString().toDoubleOrNull() ?: 0.0

        val subtotal = cant * precio
        val conDesc = subtotal - (subtotal * desc / 100.0)
        val subPeso = cant * pesoUnitario

        tvSubtotal.text = fnDecimal(subtotal)
        tvConDesc.text = fnDecimal(conDesc)
        tvSubPeso.text = fnDecimal(subPeso)

        if (fila != null) {
            val actual = fnDetalleFila(fila)
            fila.tag = actual.copy(
                codigo = codigo,
                cantidad = cant,
                precio = precio,
                descuento = desc,
                peso = pesoUnitario,
                subPeso = subPeso,
                conDesc = conDesc
            )
        }

        fnCalcularTotales()
    }

    fun fnBloquearFila(fila: TableRow) {
        for (i in 0 until tableDetalleF.childCount) tableDetalleF.getChildAt(i).setBackgroundColor(Color.TRANSPARENT)
        fila.setBackgroundColor(Color.parseColor("#E3F2FD"))
    }

    private fun fnVerificarDuplicados(codigo: String): Boolean {
        for (i in 0 until tableDetalleF.childCount) {
            val det = (tableDetalleF.getChildAt(i) as? TableRow)?.tag as? DetalleFila ?: continue
            if (det.codigo == codigo) return true
        }
        return false
    }

    private fun fnAccionesBotonQuitar() { filaSeleccionada?.let { fnEliminarFila(it) } }

    private fun fnEliminarFila(fila: TableRow) {
        tableDetalleF.removeView(fila)
        filaSeleccionada = null
        fnReordenarFilas()
        fnCalcularTotales()
    }


    private fun fnEliminarItemsCombo(comboId: Int) {
        val filasAEliminar = mutableListOf<TableRow>()

        for (i in 0 until tableDetalleF.childCount) {
            val fila = tableDetalleF.getChildAt(i) as? TableRow ?: continue
            val detalle = fnDetalleFila(fila)
            if (detalle.combo == comboId) filasAEliminar.add(fila)
        }

        filasAEliminar.forEach { fila ->
            val detalle = fnDetalleFila(fila)
            if (detalle.codigo.isNotBlank()) {
                Log.e("Transaccion", "Elimina combo=$comboId secuencia=${detalle.secuencia}")
                fnTransaccionFactura(
                    sec = detalle.secuencia,
                    codigo = detalle.codigo,
                    cantidad = detalle.cantidad,
                    costProm = detalle.costoProm,
                    precio = detalle.precio,
                    descuento = detalle.descuento,
                    opcionItem = "E",
                    proceso = detalle.proceso,
                    accion = 1,
                    secGrid = vgiContadorDetalle,
                    onResult = { Log.e("Eliminar transaccion combo", it) }
                )
            }
            tableDetalleF.removeView(fila)
            if (vgiContadorDetalle > 0) vgiContadorDetalle--
        }

        fnReordenarFilas()
        fnCalcularTotales()
    }

    fun fnLimpiarValoresFila(fila: TableRow) {
        val c = fnObtenerControlesFila(fila)

        c.etDescto.setText("0")
        c.etCant.setText("1")

        c.tvPrecio.text = "0.00"
        c.tvSubtotal.text = "0.00"
        c.tvConDesc.text = "0.00"
        c.tvPeso.text = "0.00"
        c.tvSubPeso.text = "0.00"
        c.tvPreciosItem.text = "0.00"

        c.tvCodigo.text = ""
        c.tvCostoProm.text = "0"
        c.tvProceso.text = "0"
        c.tvCombo.text = "0"
        c.tvRegalo.text = "0"

        fila.tag = DetalleFila(
            codigo = "",
            costoProm = 0.0,
            proceso = 0,
            cantidad = 1.0,
            precio = 0.0,
            descuento = 0.0,
            secuencia = c.tvNumOculto.text.toString().toIntOrNull() ?: 0,
            combo = 0,
            num = c.tvNum.text.toString().toIntOrNull() ?: 0
        )

        fnCalcularTotales()
    }

    private fun fnDetalleFila(fila: TableRow): DetalleFila = fila.tag as? DetalleFila ?: DetalleFila()

    private fun fnReordenarFilas() {
        for (i in 0 until tableDetalleF.childCount) {
            val fila = tableDetalleF.getChildAt(i) as? TableRow ?: continue
            val c = fnObtenerControlesFila(fila)
            val numero = i + 1
            c.tvNum.text = numero.toString()
            fila.tag = fnDetalleFila(fila).copy(num = numero)
        }
        contador = tableDetalleF.childCount + 1
    }

    private fun fnMostrarMensaje(titulo: String, mensaje: String) { AlertDialog.Builder(requireContext()).setTitle(titulo).setMessage(mensaje).setPositiveButton("OK", null).show() }

    override fun onItemsSelected(item: AdaptadorItemsFacturas.itemsFactura) {
        val codigo = runCatching { item.codigo }.getOrDefault("")
        val referencia = runCatching { item.referencia }.getOrDefault("")

        val fila = filaPendiente
            ?: (tableDetalleF.getChildAt(tableDetalleF.childCount - 1) as? TableRow)
            ?: run {
                fnCrearFilanueva()
                tableDetalleF.getChildAt(tableDetalleF.childCount - 1) as? TableRow
            }
            ?: return

        val actualCodigo = fnDetalleFila(fila).codigo
        if (codigo.isNotBlank() && actualCodigo != codigo && fnVerificarDuplicados(codigo)) {
            fnMensajeSistema("El item ya fue agregado")
            return
        }

        val c = fnObtenerControlesFila(fila)
        val costoProm = runCatching { item.costoProm }.getOrDefault(0.0)
        val peso = runCatching { item.peso }.getOrDefault(0.0)

        c.etRef.setText(referencia)
        c.etDescto.setText("0")
        c.etCant.setText("1")
        c.tvCodigo.text = codigo
        c.tvCostoProm.text = costoProm.toString()
        c.tvProceso.text = "0"
        c.tvPeso.text = fnDecimal(peso)
        c.tvSubPeso.text = fnDecimal(peso)
        c.tvNumOculto.text = fnDetalleFila(fila).secuencia.toString()

        fila.tag = fnDetalleFila(fila).copy(
            codigo = codigo,
            costoProm = costoProm,
            proceso = 0,
            cantidad = 1.0,
            descuento = 0.0,
            peso = peso,
            subPeso = peso,
            referencia = referencia
        )

        fnPreciosxItem(codigo) { precio ->
            c.tvPrecio.text = precio.ifBlank { "0.00" }
            fnCalcularFilaActual(
                c.etCant,
                c.etDescto,
                c.tvPrecio,
                c.tvSubtotal,
                c.tvConDesc,
                c.tvPeso,
                c.tvSubPeso,
                codigo
            )
        }

        filaPendiente = null
        filaSeleccionada = fila
        fnBloquearFila(fila)
    }

    fun fnItems(vlpReferencia: String) {
        val dialog = consultaItemsF(vlpReferencia, bodega)
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "consultaItemsF")
    }

    private fun fnPreciosItem(codigo: String, referencia: String) {
        val id = getString(R.string.str_PreciosxItem).toInt()
        val cadena = "2,'$codigo',$ep_codigo,1"

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = { xml ->
            val mensaje = fnObtenerValoresXml(xml).joinToString("\n")
            AlertDialog.Builder(requireContext())
                .setTitle(referencia.ifBlank { "Precios" })
                .setMessage(mensaje.ifBlank { "No hay datos" })
                .setPositiveButton("Aceptar", null)
                .show()
        }, onError = { ex ->
            Log.e("fnPreciosItem", "Falló: ${ex.message}", ex)
        }).execute()
    }

    fun fnPreciosxItem(item: String, onResult: (String) -> Unit) {
        val id = getString(R.string.str_FacturaPrecios).toInt()
        val cadena = "2,'$item',1,$nivel,'N'"

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = { xml ->
            onResult(fnObtenerValorXml(xml).orEmpty().ifBlank { "0.00" })
        }, onError = { ex ->
            Log.e("Precios", "Falló: ${ex.message}", ex)
            onResult("0.00")
        }).execute()
    }


    fun fnObtenerValorXml(xmlResult: String?): String? {
        if (xmlResult.isNullOrBlank()) return null

        return try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlResult))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.TEXT) {
                    val value = parser.text?.trim()
                    if (!value.isNullOrEmpty()) {
                        return value
                    }
                }
                eventType = parser.next()
            }
            null
        } catch (e: Exception) {
            Log.e("fnObtenerValorXml", "Error: ${e.message}", e)
            null
        }
    }

    fun fnObtenerValoresXml(xmlResult: String?): List<String> {
        val resultados = mutableListOf<String>()
        if (xmlResult.isNullOrBlank()) return resultados

        return try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlResult))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.TEXT) {
                    val value = parser.text?.trim()
                    if (!value.isNullOrEmpty()) {
                        resultados.add(value)
                    }
                }
                eventType = parser.next()
            }
            resultados
        } catch (e: Exception) {
            Log.e("fnObtenerValoresXml", "Error: ${e.message}", e)
            resultados
        }
    }


    private fun fnMostrarDialogoDeConfirmacion() {
        when (vgsOpcionMenu) {
            "I" -> fnMostrarDialogo("¿Deseas guardar los datos?") { fnGuardarFactura() }
            "M" -> fnMostrarDialogo("¿Deseas actualizar los datos?") { fnGuardarFactura() }
        }
    }

    private fun fnMostrarDialogo(mensaje: String, accion: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage(mensaje)
            .setPositiveButton("Sí") { dialog, _ ->
                dialog.dismiss()
                accion()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
    private fun fnMostrarDialogoEliminar() { fnMensajeSistemaPregunta("¿Eliminar factura?", true) { if (it == 1) fnEliminarFactura() } }

    private fun fnEliminarFactura() {
        if (tableDetalleF.childCount <= 0) return

        for (i in 0 until tableDetalleF.childCount) {
            val fila = tableDetalleF.getChildAt(i) as? TableRow ?: continue
            val c = runCatching { fnObtenerControlesFila(fila) }.getOrNull() ?: continue
            val codigo = c.tvCodigo.text.toString().trim()
            if (codigo.isBlank()) continue

            fnTransaccionFactura(
                sec = c.tvNumOculto.text.toString().toIntOrNull() ?: 0,
                codigo = codigo,
                cantidad = c.etCant.text.toString().toDoubleOrNull() ?: 0.0,
                costProm = c.tvCostoProm.text.toString().toDoubleOrNull() ?: 0.0,
                precio = c.tvPrecio.text.toString().toDoubleOrNull() ?: 0.0,
                descuento = c.etDescto.text.toString().toDoubleOrNull() ?: 0.0,
                opcionItem = "E",
                proceso = c.tvProceso.text.toString().toIntOrNull() ?: 0,
                accion = 2,
                secGrid = vgiContadorDetalle,
                onResult = { fnObtenerValoresXml(it).isEmpty() }
            )
        }

        vgsOpcionMenu = "E"
        showResultDialog()
    }


    fun fnTransporte(): String {
        if (DatosTarifa.isEmpty()) {
            Tarifa = "$0.00"
            Cobertura = ""
            return "0"
        }

        var finalTarifa = "0"
        runCatching {
            DatosTarifa.forEach { tarifaItem ->
                val peso = tarifaItem.peso ?: 0.0
                val tarifa1 = tarifaItem.tarifa1 ?: 0.0
                val tarifa2 = tarifaItem.tarifa2 ?: 0.0

                val tarifaAplicada: Double
                finalTarifa = if (unidadCE >= 0.0 && unidadCE < peso) {
                    tarifaAplicada = tarifa1
                    tarifa1.toString()
                } else {
                    tarifaAplicada = tarifa2
                    (unidadCE * tarifa2).toString()
                }

                Tarifa = String.format(Locale.US, "$%.2f", tarifaAplicada)
                Cobertura = tarifaItem.descripcion.orEmpty()
                Log.e("XML", "unidadCE=$unidadCE, peso=$peso, t1=$tarifa1, t2=$tarifa2, desc=${tarifaItem.descripcion}")
            }
        }.onFailure {
            Log.e("fnTransporte", "Error calculando transporte: ${it.message}", it)
            Tarifa = "$0.00"
            Cobertura = ""
            finalTarifa = "0"
        }

        return finalTarifa
    }


    fun fnPesos() {
        Kilo.text = String.format(Locale.US, "%.2f", unidadCE)
        tarifa.text = Tarifa.ifBlank { "$0.00" }
        cobertura.text = Cobertura.ifBlank { "" }
    }

    private fun fnCliente() {
        val dialog = consultaCliente()
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "consultaCliente")
    }

    override fun onItemsSelected(clientes: AdaptadorClientes.Clientes) {
        clienteSeleccionado = clientes
        fnAsignarDatosCliente(clientes)
    }

    fun fnAsignarDatosCliente(clientes: AdaptadorClientes.Clientes) {
        cl_codigo = runCatching { clientes.codigo }.getOrDefault("")
        txtCodClientePF.text = cl_codigo
        txtCliente.setText(runCatching { clientes.nombre }.getOrDefault(""))
        txtCIdentificacionPF.setText(runCatching { clientes.en_identificacion }.getOrDefault(""))
        nivel = runCatching { clientes.cc_nivelprecio }.getOrDefault("")
        txtNivelPF.setText(nivel)
    }

    fun fnCargarClienteSeleccionado(clientes: AdaptadorClientes.Clientes) { fnAsignarDatosCliente(clientes) }
    private fun fnDatosAdicionales() { fnMensajeSistema("Datos adicionales pendientes de completar") }

    private fun fnPesoTarifaA() {
        val id = getString(R.string.str_Tarifa).toInt()
        val cadena = "$cl_codigo,$tr_codigo"

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = { xml ->
            DatosTarifa = fnParseTarifas(xml)
            fnPesos()
            fnTransporte()
            fnSeguroCliente()
        }, onError = { ex ->
            Log.e("Tarifa", "Falló: ${ex.message}", ex)
        }).execute()
    }

    fun fnSeguroCliente() {
        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val cadena = "$cl_codigo,$tr_codigo,'$fechaActual'"
        val id = getString(R.string.str_Seguros).toInt()

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = { xml ->
            val seguro = fnParseSeguro(xml)
            if (seguro != null) vgDseguro = seguro
            lblseguroPF.text = seguro?.let { "$it%" }.orEmpty()
            fnCalcularTotales()
        }, onError = { ex ->
            Log.e("Seguro", "Falló: ${ex.message}", ex)
        }).execute()
    }

    fun fnFormaPago() {
        val items = mutableListOf<SpinnerItem>()
        runCatching {
            val adapter = spinnerFormaPag.adapter
            for (i in 0 until adapter.count) (adapter.getItem(i) as? SpinnerItem)?.let { items.add(it) }
        }
        if (items.isEmpty()) return
        val nombres = items.map { it.descripcion }.toTypedArray()
        AlertDialog.Builder(requireContext()).setTitle("Forma de Pago").setItems(nombres) { _, which ->
            val item = items[which]
            tp_codigo = item.codigo
            tp_descripcion = item.descripcion
            txtFormaPagoPF.text = item.descripcion
        }.show()
    }

    fun fnValidarTelefonos(input: String): Boolean = input.length >= 7 && !fnTodosIguales(input) && !fnEsSecuenciaAscendente(input) && !fnEsSecuenciaDescendente(input)

    private fun fnTodosIguales(s: String): Boolean {
        if (s.isEmpty()) return false
        return s.all { it == s.first() }
    }

    private fun fnMaxRunIguales(s: String): Int {
        if (s.isEmpty()) return 0
        var maxRun = 1
        var run = 1
        for (i in 1 until s.length) {
            if (s[i] == s[i - 1]) {
                run++
                if (run > maxRun) maxRun = run
            } else {
                run = 1
            }
        }
        return maxRun
    }

    private fun fnEsSecuenciaAscendente(s: String): Boolean {
        var secuencia = 1
        for (i in 1 until s.length) {
            val prev = s[i - 1].digitToIntOrNull() ?: return false
            val cur = s[i].digitToIntOrNull() ?: return false
            val esperado = (prev + 1) % 10
            if (cur == esperado) {
                secuencia++
                if (secuencia >= 6) return true
            } else {
                secuencia = 1
            }
        }
        return false
    }

    private fun fnEsSecuenciaDescendente(s: String): Boolean {
        var secuencia = 1
        for (i in 1 until s.length) {
            val prev = s[i - 1].digitToIntOrNull() ?: return false
            val cur = s[i].digitToIntOrNull() ?: return false
            val esperado = (prev + 9) % 10
            if (cur == esperado) {
                secuencia++
                if (secuencia >= 6) return true
            } else {
                secuencia = 1
            }
        }
        return false
    }
    fun fnValidarCorreo(email: String): Boolean = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun fnObtenerSecuencia() { vgiSecuencia++ }

    fun fnTransaccionFactura(
        sec: Int,
        codigo: String,
        cantidad: Double,
        costProm: Double,
        precio: Double,
        descuento: Double,
        opcionItem: String,
        proceso: Int,
        accion: Int,
        secGrid: Int,
        onResult: (String) -> Unit
    ) {
        val fechaTrn = fnFechaxml()
        val soloCabecera = if (accion == 2 || proceso != 1 || vgsOpcionMenu == "E") "N" else "S"
        val id = getString(R.string.str_TransaccionFactura).toInt()

        val cadena = listOf(
            "2",
            bodega.ifBlank { "0" },
            "'${txtNumero.text}'",
            "'0'",
            ep_codigo.ifBlank { "0" },
            cl_codigo.ifBlank { "0" },
            "'0'",
            "'0'",
            tr_codigo.ifBlank { "0" },
            "'N'",
            tp_codigo.ifBlank { "0" },
            "1",
            "'${txtObservacion.text}'",
            lblseguroPF.text.toString().replace("%", ""),
            txtDesc.text.toString(),
            txtFlete.text.toString(),
            txtSeguro.text.toString(),
            txtDescuentoT.text.toString(),
            txtIva.text.toString(),
            txtSub.text.toString(),
            txtTotal.text.toString(),
            "'0'",
            "0",
            "'$codigo'",
            sec.toString(),
            "'$fechaTrn'",
            "'0'",
            cantidad.toString(),
            costProm.toString(),
            precio.toString(),
            descuento.toString(),
            "''",
            "'${txtObservacion.text}'",
            "'${usuario.ifBlank { "0" }}'",
            "'$opcionItem'",
            "'$vgsOpcionMenu'",
            "0",
            "428",
            "0",
            "'$soloCabecera'",
            "1",
            secGrid.toString(),
            "1"
        ).joinToString(",")

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = { xml ->
            onResult(xml)
        }, onError = { ex ->
            Log.e("TransaccionFactura", "Falló: ${ex.message}", ex)
            onResult("")
        }).execute()
    }

    fun fnAlertStock(lista: List<String>, onResult: (Boolean, String) -> Unit) {
        if (lista.size < 5) {
            fnMensajeSistema("No se pudo mostrar la información completa.")
            onResult(false, "")
            return
        }

        val stock = lista[1].trim()
        val codigo = lista[3].trim()
        val mensaje = lista[4].trim()

        val editText = EditText(requireContext()).apply {
            setText(stock)
            setSelection(text.length)
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val contenido = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 10)

            addView(TextView(requireContext()).apply {
                text = "$codigo - $mensaje : $stock"
                textSize = 16f
            })

            addView(editText)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("SiteNet")
            .setView(contenido)
            .setPositiveButton("Aceptar") { _, _ ->
                onResult(true, editText.text.toString().trim())
            }
            .setNegativeButton("Cancelar") { _, _ ->
                onResult(false, editText.text.toString().trim())
            }
            .setCancelable(false)
            .show()
    }

    fun fnAlertItemSinStock() { fnMensajeSistema("Item no tiene stock") }

    fun fnFacturas() {
        val dialog = consultaFactura(bodega)
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "consultaFactura")
    }

    override fun onItemsSelected(codigo: ConsultaFacturas) {
        val numero = runCatching { codigo.factura }.getOrDefault(0)
        txtNumero.text = numero.toString()
        fnObtenerFactura(numero)
    }


    private fun fnObtenerFactura(fa_documento: Int) {
        val fecha = fnFechaHora()
        val id = getString(R.string.str_FaFactura).toInt()
        val cadena = "2,$bodega,'$fa_documento','$fa_documento','$fecha','','','','','','','$usuario',1"
        val progressDialog = showProgressDialogItems()

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = { xml ->
            progressDialog.dismiss()
            factura = fa_documento.toString()

            runCatching {
                val parse = fnParseFacturaXml(xml)
                parse.cabecera?.let { fnLlenarCabeceraFactura(it) }

                val detalle = parse.detalle.mapIndexed { index, item ->
                    ItemDetalleFactura(
                        num = index + 1,
                        referencia = item.referencia,
                        descuento = item.descuento.toDoubleOrNull() ?: 0.0,
                        cantidad = item.cantidad.toDoubleOrNull() ?: 0.0,
                        precio = item.precio.toDoubleOrNull() ?: 0.0,
                        subtotal = item.subtotal.toDoubleOrNull() ?: 0.0,
                        conDesc = item.conDesc.toDoubleOrNull() ?: 0.0,
                        peso = item.peso.toDoubleOrNull() ?: 0.0,
                        subPeso = item.subPeso.toDoubleOrNull() ?: 0.0,
                        codigo = item.codigo,
                        costoProm = item.costoProm.toDoubleOrNull() ?: 0.0,
                        proceso = item.proceso.toIntOrNull() ?: 1,
                        secuencia = item.secuencia.toIntOrNull() ?: (index + 1)
                    )
                }

                fnLlenarDetalleFactura(tableDetalleF, detalle, true)
                fnActivarControles()
                fnCalcularTotales()
            }.onFailure {
                Log.e("fnObtenerFactura", "No se pudo parsear factura: ${it.message}", it)
                fnMensajeSistema("No se pudo cargar el detalle de la factura.")
            }
        }, onError = { ex ->
            progressDialog.dismiss()
            Log.e("fnObtenerFactura", "Falló: ${ex.message}", ex)
        }).execute()
    }

    fun fnParseFacturaXml(xml: String): FacturaParseResult {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(xml)))
        doc.documentElement.normalize()
        return FacturaParseResult(fnParseCabecera(doc), fnParseDetalle(doc))
    }

    private fun fnParseCabecera(document: Document): FacturaCabecera? {
        val nodes = document.getElementsByTagName("Table")
        if (nodes.length == 0) return null
        val e = nodes.item(0) as? Element ?: return null
        return FacturaCabecera(
            faCodDocumento = fnGetTagValueMulti(e, listOf("fa_coddocumento", "fa_documento")),
            faFechaFactura = fnGetTagValueMulti(e, listOf("fa_fechafactura", "fa_fecha")),
            vendedor = fnGetTagValueMulti(e, listOf("vendedor", "vn_nombre")),
            tr_codigo = fnGetTagValueMulti(e, listOf("tr_codigo")),
            transporte = fnGetTagValueMulti(e, listOf("transporte", "tr_nombre")),
            cliente = fnGetTagValueMulti(e, listOf("cliente", "cl_nombre")),
            cedulaRuc = fnGetTagValueMulti(e, listOf("cedulaRuc", "cl_identificacion")),
            direccion = fnGetTagValueMulti(e, listOf("direccion", "cl_direccion")),
            fono = fnGetTagValueMulti(e, listOf("fono", "telefono")),
            ciudad = fnGetTagValueMulti(e, listOf("ciudad", "ci_descripcion")),
            formaPago = fnGetTagValueMulti(e, listOf("formaPago", "fp_descripcion")),
            observacion = fnGetTagValueMulti(e, listOf("observacion", "fa_observacion"))
        )
    }

    private fun fnParseDetalle(document: Document): List<FacturaDetalle> {
        val result = mutableListOf<FacturaDetalle>()
        val candidates = listOf("Table1", "Detalle", "Items")
        for (tag in candidates) {
            val nodes = document.getElementsByTagName(tag)
            for (i in 0 until nodes.length) {
                val e = nodes.item(i) as? Element ?: continue
                result.add(
                    FacturaDetalle(
                        codigo = fnGetTagValueMulti(e, listOf("it_codigo", "codigo")),
                        referencia = fnGetTagValueMulti(e, listOf("it_referencia", "referencia")),
                        descripcion = fnGetTagValueMulti(e, listOf("descripcion", "it_descripcion")),
                        cantidad = fnGetTagValueMulti(e, listOf("cantidad", "df_cantidad")),
                        descuento = fnGetTagValueMulti(e, listOf("descuento", "df_descuento")),
                        precio = fnGetTagValueMulti(e, listOf("precio", "df_precio")),
                        subtotal = fnGetTagValueMulti(e, listOf("subtotal")),
                        conDesc = fnGetTagValueMulti(e, listOf("conDesc", "condesc")),
                        peso = fnGetTagValueMulti(e, listOf("peso")),
                        subPeso = fnGetTagValueMulti(e, listOf("subPeso", "subpeso")),
                        costoProm = fnGetTagValueMulti(e, listOf("costoProm", "it_costoprom")),
                        proceso = fnGetTagValueMulti(e, listOf("proceso")),
                        secuencia = fnGetTagValueMulti(e, listOf("secuencia"))
                    )
                )
            }
            if (result.isNotEmpty()) break
        }
        return result
    }

    private fun fnGetTagValue(element: Element, tag: String): String = runCatching {
        element.getElementsByTagName(tag).item(0)?.textContent ?: ""
    }.getOrDefault("")

    private fun fnGetTagValueMulti(element: Element, tags: List<String>): String {
        for (tag in tags) {
            val value = fnGetTagValue(element, tag)
            if (value.isNotBlank()) return value
        }
        return ""
    }

    fun fnLlenarCabeceraFactura(cab: FacturaCabecera) {
        txtNumero.text = cab.faCodDocumento
        txtFecha.text = fnFormatearFechaXml(cab.faFechaFactura)
        txtCliente.setText(cab.cliente)
        txtCodClientePF.text = cab.cl_codigo
        txtCIdentificacionPF.setText(cab.cedulaRuc)
        txtTransporte.setText(cab.transporte, false)
        txtFormaPagoPF.text = cab.formaPago
        txtObservacion.text = cab.observacion
        txtSub.text = cab.faValorBruto
        txtIva.text = cab.faValorIva
        txtFlete.text = cab.faFlete
        txtSeguro.text = cab.faSeguro
        txtTotal.text = cab.faValorTotFact
    }

    private fun fnFormatearFechaXml(fechaXml: String): String = fechaXml.ifBlank { fnFecha() }
    fun fnFormatearFechaIso(fechaIso: String): String = fechaIso.take(10)


    fun fnLlenarDetalleFactura(table: TableLayout, lista: List<ItemDetalleFactura>, vpbRegalo: Boolean) {
        if (vpbRegalo) table.removeAllViews()

        for (item in lista) {
            if (item.codigo.isBlank()) return
            fnLlenarFilaDetalle(
                table = table,
                num = item.num,
                referencia = item.referencia,
                descuento = item.descuento,
                cantidad = item.cantidad,
                precio = item.precio,
                subtotal = item.subtotal,
                conDesc = item.conDesc,
                peso = item.peso,
                subPeso = item.subPeso,
                codigo = item.codigo,
                costoProm = item.costoProm,
                proceso = item.proceso,
                secuencia = item.secuencia,
                vpbRegalo = vpbRegalo
            )
        }

        fnReordenarFilas()
        fnCalcularTotales()
    }


    private fun fnLlenarFilaDetalle(
        table: TableLayout,
        num: Int,
        referencia: String,
        descuento: Double,
        cantidad: Double,
        precio: Double,
        subtotal: Double,
        conDesc: Double,
        peso: Double,
        subPeso: Double,
        codigo: String,
        costoProm: Double,
        proceso: Int,
        secuencia: Int,
        vpbRegalo: Boolean
    ) {
        val row = TableRow(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
            setPadding(0, 4, 0, 4)
            tag = DetalleFila(
                codigo = codigo,
                costoProm = costoProm,
                proceso = proceso,
                cantidad = cantidad,
                precio = precio,
                descuento = descuento,
                secuencia = secuencia,
                combo = 0,
                num = num,
                referencia = referencia,
                peso = peso,
                subPeso = subPeso,
                conDesc = conDesc,
                regalo = if (vpbRegalo) 1 else 0
            )
            setOnClickListener {
                filaSeleccionada = this
                fnBloquearFila(this)
            }
        }

        val tvNum = fnCreateCellW(num.toString(), 0.7f, Gravity.CENTER)

        val etRef = fnCreateRefEditTextW(
            weight = 2.3f,
            onTyping = { ref, editText ->
                val fila = editText.parent as? TableRow ?: return@fnCreateRefEditTextW
                filaPendiente = fila
                if (ref.isBlank()) fnLimpiarValoresFila(fila)
            },
            onDone = { ref, editText ->
                hideKeyboard(editText)
                val fila = editText.parent as? TableRow ?: return@fnCreateRefEditTextW
                filaPendiente = fila
                if (ref.isBlank()) {
                    fnLimpiarValoresFila(fila)
                } else {
                    fnItems(ref)
                }
            }
        ).apply { setText(referencia) }

        val etDescto = fnCreateDesctoEditTextW(0.8f).apply { setText(fnDecimal(descuento)) }
        val etCant = fnCreateCantEditTextW(0.9f) { fila ->
            val c = fnObtenerControlesFila(fila)
            val cod = c.tvCodigo.text.toString().trim()
            if (cod.isNotEmpty()) {
                fnCalcularFilaActual(c.etCant, c.etDescto, c.tvPrecio, c.tvSubtotal, c.tvConDesc, c.tvPeso, c.tvSubPeso, cod)
            }
        }.apply { setText(fnDecimal(cantidad)) }

        val tvPrecio = fnCreateCellW(fnDecimal(precio), 1f, Gravity.END)
        val tvSubtotal = fnCreateCellW(fnDecimal(subtotal), 1f, Gravity.END)
        val tvConDesc = fnCreateCellW(fnDecimal(conDesc), 1f, Gravity.END)
        val tvPeso = fnCreateCellW(fnDecimal(peso), 0.9f, Gravity.END)
        val tvSubPeso = fnCreateCellW(fnDecimal(subPeso), 0.9f, Gravity.END)

        row.addView(tvNum)
        row.addView(etRef)
        row.addView(etDescto)
        row.addView(etCant)
        row.addView(tvPrecio)
        row.addView(tvSubtotal)
        row.addView(tvConDesc)
        row.addView(tvPeso)
        row.addView(tvSubPeso)
        row.addView(fnCreateHiddenCell("0.00"))
        row.addView(fnCreateHiddenCell(codigo))
        row.addView(fnCreateHiddenCell(costoProm.toString()))
        row.addView(fnCreateHiddenCell(proceso.toString()))
        row.addView(fnCreateHiddenCell(secuencia.toString()))
        row.addView(fnCreateHiddenCell("0"))
        row.addView(fnCreateHiddenCell(if (vpbRegalo) "1" else "0"))

        table.addView(row)
        filaSeleccionada = row
        vgiSecuencia = maxOf(vgiSecuencia, secuencia + 1)
        contador = table.childCount + 1
    }


    fun fnAccesoUsuario(faCodDocumento: Int, tipo: Int) {
        val id = getString(R.string.str_FaAccesoUsuario).toInt()
        val cadena = "2,$bodega,'$faCodDocumento',$usuario,$tipo"

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = {
            // Registro realizado.
        }, onError = { ex ->
            Log.e("accesousuario", "Falló: ${ex.message}", ex)
        }).execute()
    }

    fun fnProcesarClientePublico(faCodDocumento: Int) {
        val fecha = fnFechaHora()
        val id = getString(R.string.str_FaFactura).toInt()
        val cadena = "2,$bodega,'$faCodDocumento','$faCodDocumento','$fecha','$cl_codigo','${txtCliente.text}','${txtCIdentificacionPF.text}','GUAYAQUIL','0','GUAYAQUIL','$usuario',2"

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = { xml ->
            Log.e("clientePublico", xml)
            fnFacturaPorcDescto(faCodDocumento)
        }, onError = { ex ->
            Log.e("ClientePublico", "Falló: ${ex.message}", ex)
        }).execute()
    }

    fun fnFacturaPorcDescto(faCodDocumento: Int) {
        val id = getString(R.string.str_FaFacturaPorcDescto).toInt()
        val cadena = fnGenerarXmlDescuento(
            table = tableDetalleF,
            boCodigo = bodega.toIntOrNull() ?: 0,
            mvCodigo = 1,
            ctCodDocumento = faCodDocumento.toDouble(),
            gtFechaTrn = fnFechaDescuentoxml()
        )

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = { xml ->
            Log.e("descuento", xml)
            fnFacturaPesos(faCodDocumento)
        }, onError = { ex ->
            Log.e("Descuento", "Falló: ${ex.message}", ex)
        }).execute()
    }

    fun fnFacturaPesos(faCodDocumento: Int) {
        val id = getString(R.string.str_FaFacturaPesos).toInt()
        val cadena = fnGenerarXmlPesos(
            table = tableDetalleF,
            boCodigo = bodega.toIntOrNull() ?: 0,
            mvCodigo = 1,
            ctCodDocumento = faCodDocumento
        )

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = {
            fnFacturaEncuesta(faCodDocumento)
        }, onError = { ex ->
            Log.e("Pesos", "Falló: ${ex.message}", ex)
        }).execute()
    }

    fun fnFacturaEncuesta(faCodDocumento: Int) {
        val encuesta = fnObtenerEncuesta()
        val id = getString(R.string.str_FaFacturaEncuesta).toInt()
        val cadena = "2,$bodega,'$faCodDocumento',$encuesta,3"

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = {
            fnMargenRentabilidadFactura(faCodDocumento)
        }, onError = { ex ->
            Log.e("Encuesta", "Falló: ${ex.message}", ex)
        }).execute()
    }

    private fun fnGenerarXmlDescuento(
        table: TableLayout,
        boCodigo: Int,
        mvCodigo: Int,
        ctCodDocumento: Double,
        gtFechaTrn: String
    ): String {
        val sb = StringBuilder()
        sb.append("'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>")
        sb.append("<c c0=\"2\" c1=\"$boCodigo\" c2=\"$mvCodigo\" c3=\"$ctCodDocumento\" c4=\"$gtFechaTrn\">")

        for (i in 0 until table.childCount) {
            val row = table.getChildAt(i) as? TableRow ?: continue
            val etDescto = row.getChildAt(COL_DESCTO) as? EditText ?: continue
            val tvCodigo = row.getChildAt(COL_CODIGO_OCULTO) as? TextView ?: continue
            val tvNum = row.getChildAt(COL_NUM_OCULTO) as? TextView ?: continue

            val itCodigo = tvCodigo.text.toString().trim()
            if (itCodigo.isBlank()) continue

            val gtSecuenciaDoc = tvNum.text.toString().trim().toIntOrNull() ?: (i + 1)
            val gtPorcDescuento = etDescto.text.toString().trim().toDoubleOrNull() ?: 0.0
            sb.append("<detalle d0=\"$gtSecuenciaDoc\" d1=\"$itCodigo\" d2=\"$gtPorcDescuento\" d3=\"\" ></detalle>")
        }

        sb.append("</c>'")
        return sb.toString()
    }

    private fun fnGenerarXmlPesos(
        table: TableLayout,
        boCodigo: Int,
        mvCodigo: Int,
        ctCodDocumento: Int
    ): String {
        val sb = StringBuilder()
        sb.append("'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>")
        sb.append("<c c0=\"2\" c1=\"$boCodigo\" c2=\"$mvCodigo\" c3=\"$ctCodDocumento\" c4=\"$usuario\">")

        for (i in 0 until table.childCount) {
            val row = table.getChildAt(i) as? TableRow ?: continue
            val txtPeso = row.getChildAt(COL_PESO) as? TextView ?: continue
            val tvCodigo = row.getChildAt(COL_CODIGO_OCULTO) as? TextView ?: continue
            val tvNum = row.getChildAt(COL_NUM_OCULTO) as? TextView ?: continue

            val itCodigo = tvCodigo.text.toString().trim()
            if (itCodigo.isBlank()) continue

            val gtSecuenciaDoc = tvNum.text.toString().trim().toIntOrNull() ?: (i + 1)
            val gtPeso = txtPeso.text.toString().trim().toDoubleOrNull() ?: 0.0
            sb.append("<detalle d0=\"$gtSecuenciaDoc\" d1=\"$itCodigo\" d2=\"$gtPeso\" ></detalle>")
        }

        sb.append("</c>'")
        return sb.toString()
    }


    private fun fnObtenerEncuesta(): Int = when (radioGroup.checkedRadioButtonId) {
        R.id.rbNinguno -> 1
        R.id.rbMeta -> 2
        R.id.rbEscala -> 3
        R.id.rbGoogle -> 4
        R.id.rbVolante -> 5
        R.id.rbWhatsapp -> 6
        else -> 0
    }
    private fun fnAsignarEncuesta(valor: Int) {
        when (valor) {
            1 -> radioGroup.check(R.id.rbNinguno)
            2 -> radioGroup.check(R.id.rbMeta)
            3 -> radioGroup.check(R.id.rbEscala)
            4 -> radioGroup.check(R.id.rbGoogle)
            5 -> radioGroup.check(R.id.rbVolante)
            6 -> radioGroup.check(R.id.rbWhatsapp)
            else -> radioGroup.clearCheck()
        }
    }

    private fun fnCalcularMontoRegalo() {
        if (listaSecuencia.size < 35) {
            txtRegaloPF.text = "0.00"
            btnRegaloF.isEnabled = false
            return
        }

        val rentabilidad = txtLote.text.toString().toDoubleOrNull() ?: 0.0
        val subtotal = txtSub.text.toString().toDoubleOrNull() ?: 0.0
        val descuento = txtDescuentoT.text.toString().toDoubleOrNull() ?: 0.0
        val base = subtotal - descuento

        val limite1 = listaSecuencia[29].toDoubleOrNull() ?: 0.0
        val porcentaje1 = listaSecuencia[30].toDoubleOrNull() ?: 0.0
        val limite2 = listaSecuencia[31].toDoubleOrNull() ?: 0.0
        val porcentaje2 = listaSecuencia[32].toDoubleOrNull() ?: 0.0
        val limite3 = listaSecuencia[33].toDoubleOrNull() ?: 0.0
        val porcentaje3 = listaSecuencia[34].toDoubleOrNull() ?: 0.0

        val montoRegalo = when {
            rentabilidad <= limite1 -> base * porcentaje1
            rentabilidad > limite2 && rentabilidad <= limite3 -> base * porcentaje2
            rentabilidad > limite3 -> base * porcentaje3
            else -> 0.0
        }

        txtRegaloPF.text = fnDecimal(montoRegalo)
        btnRegaloF.isEnabled = montoRegalo > 0.0
    }
    fun fnAccionesBotonListadoRegalo() { fnListadoRegalo(0) }

    fun fnListadoRegalo(vlpOpcion: Int) {
        if (cargandoListadoRegalo || dialogoListaRegalo?.isShowing == true) return

        val showRegalo = showProgressDialogItems()
        btnGuardar.isEnabled = false
        cargandoListadoRegalo = true

        val id = getString(R.string.str_FaFacturaRegalo).toInt()
        val rentabilidad = txtLote.text.toString().toDoubleOrNull() ?: 0.0
        val cadena = "2,$bodega,$vgdMontoRegaloA,$rentabilidad,'',$vlpOpcion,$nivel"

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = { xml ->
            cargandoListadoRegalo = false
            btnGuardar.isEnabled = true

            fnMostrarDialogoListaRegalo(xml, showRegalo) { item ->
                val detalle = ItemDetalleFactura(
                    num = tableDetalleF.childCount + 1,
                    referencia = item.referencia,
                    descuento = item.descuento.toDoubleOrNull() ?: 0.0,
                    cantidad = item.cantidad.toDoubleOrNull() ?: 0.0,
                    precio = item.precio.toDoubleOrNull() ?: 0.0,
                    subtotal = item.subtotal.toDoubleOrNull() ?: 0.0,
                    conDesc = item.conDesc.toDoubleOrNull() ?: 0.0,
                    peso = item.peso.toDoubleOrNull() ?: 0.0,
                    subPeso = item.subPeso.toDoubleOrNull() ?: 0.0,
                    codigo = item.codigo,
                    costoProm = item.costoProm.toDoubleOrNull() ?: 0.0,
                    proceso = item.proceso.toIntOrNull() ?: 0,
                    secuencia = vgiSecuencia
                )
                fnLlenarDetalleFactura(tableDetalleF, listOf(detalle), false)
                vgbValidoRegalo = true
                fnCalcularTotales()
            }
        }, onError = { ex ->
            showRegalo.dismiss()
            cargandoListadoRegalo = false
            btnGuardar.isEnabled = true
            Log.e("Regalos", "Falló: ${ex.message}", ex)
        }).execute()
    }

    private fun fnValidarMargenTipoCliente(): Short {
        val lote = txtLote.text.toString().toDoubleOrNull() ?: 0.0
        Log.e("Respuesta", "$lote - $vgdMargenP")
        val resultado = if (nivel == "7") {
            if (lote >= vgdMargenP) 0 else 1
        } else {
            if (lote >= vgdMargenM) 0 else 1
        }
        return resultado.toShort()
    }

    private fun fnAutorizaRentabilidad() {
        val lote = txtLote.text.toString().toDoubleOrNull() ?: 0.0
        if (lote == 0.0 || fnValidarMargenTipoCliente() == 0.toShort()) return

        val items = listOf(
            Item("0", "<<Ninguno>>"),
            Item("11", "Jorge Guillen"),
            Item("82", "Ab. Ledesma"),
            Item("16", "Ing. Ledesma"),
            Item("17111", "Abel Calderón"),
            Item("67", "Marlon Villacres")
        )

        val contexto = requireContext()
        val contenedor = LinearLayout(contexto).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 20)
            background = ContextCompat.getDrawable(contexto, R.drawable.background)
        }

        val titulo = TextView(contexto).apply {
            text = "Autoriza Supervisor"
            setTextColor(ContextCompat.getColor(contexto, android.R.color.black))
            textSize = 18f
            setPadding(0, 0, 0, 20)
        }

        val spinner = Spinner(contexto).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100)
            background = ContextCompat.getDrawable(contexto, R.drawable.spinner_background)
            adapter = ArrayAdapter(contexto, android.R.layout.simple_spinner_item, items).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(0)
        }

        contenedor.addView(titulo)
        contenedor.addView(spinner)

        val dialog = AlertDialog.Builder(contexto)
            .setView(contenedor)
            .setCancelable(false)
            .setPositiveButton("Aceptar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val item = spinner.selectedItem as? Item
                val codigo = item?.codigo?.toIntOrNull() ?: 0
                if (codigo <= 0) {
                    Toast.makeText(contexto, "Seleccione supervisor", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                vgiSupervisor = codigo
                btnLotePF.isEnabled = false
                dialog.dismiss()
                fnMostrarDialogoClave()
            }
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.background)
    }
    fun fnMostrarAlertaAutorizacion() { fnMensajeSistema("Autorización requerida") }

    private fun fnMostrarDialogoClave() {
        val contexto = requireContext()

        val contenedor = LinearLayout(contexto).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 20)
            background = ContextCompat.getDrawable(contexto, R.drawable.background)
        }

        val titulo = TextView(contexto).apply {
            text = "Administrador"
            setTextColor(ContextCompat.getColor(contexto, android.R.color.holo_red_dark))
            textSize = 18f
            setPadding(0, 0, 0, 20)
        }

        val txtUsuario = EditText(contexto).apply {
            setText(if (bodega == "2") "acalderon" else "mvillacres")
            isEnabled = false
            setTextColor(ContextCompat.getColor(contexto, android.R.color.black))
        }

        val txtPassword = EditText(contexto).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(ContextCompat.getColor(contexto, android.R.color.black))
            hint = "Password"
        }

        contenedor.addView(titulo)
        contenedor.addView(TextView(contexto).apply { text = "Usuario:" })
        contenedor.addView(txtUsuario)
        contenedor.addView(TextView(contexto).apply { text = "Password:" })
        contenedor.addView(txtPassword)

        val dialog = AlertDialog.Builder(contexto)
            .setView(contenedor)
            .setCancelable(false)
            .setPositiveButton("Aceptar", null)
            .setNegativeButton("Cerrar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val pass = txtPassword.text.toString().trim()
                if (pass.isBlank()) {
                    Toast.makeText(contexto, "Ingrese clave", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                fnValidarClaveAdministrador(txtUsuario.text.toString().trim(), pass) { valido ->
                    if (valido) {
                        vgbAutorizaDescuento = true
                        btnGuardar.isEnabled = true
                        dialog.dismiss()
                    } else {
                        Toast.makeText(contexto, "Clave incorrecta", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.background)
    }

    fun fnValidarClaveAdministrador(vlpUsuario: String, vlpContrasena: String, resultado: (Boolean) -> Unit) {
        val id = getString(R.string.str_FaClaveJefeAlmacen).toInt()
        val cadena = fnValidarUsuarioClaveXML(vlpUsuario, vlpContrasena)

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = { xml ->
            resultado(fnClaveXml(xml).toBoolean())
        }, onError = { ex ->
            Log.e("clave", "Falló: ${ex.message}", ex)
            resultado(false)
        }).execute()
    }

    private fun fnValidarUsuarioClaveXML(vlpUsuario: String, vlpContrasena: String): String {
        val vlsJefeAlmacen = if (vlpUsuario == "acalderon") 17111 else 67
        return buildString {
            append("'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>")
            append("<c c0=\"0\" c1=\"$usuario\" c2=\"$vlsJefeAlmacen\" c3=\"2\" c4=\"$ep_codigo\" c5=\"$cl_codigo\" c6=\"${txtCliente.text}\" c7=\"$vlpContrasena\" >")
            append("</c>'")
        }
    }

    fun fnMargenRentabilidadFactura(faCodDocumento: Int) {
        val lote = txtLote.text.toString()
        val id = getString(R.string.str_FaFacturaMargenRentabilidad).toInt()
        val cadena = "2,$bodega,'$faCodDocumento',$lote,1"

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = {
            if (vgiSupervisor == 0) {
                showResultDialog()
            } else {
                fnSupervisorFactura(faCodDocumento)
            }
        }, onError = { ex ->
            Log.e("Encuesta", "Falló: ${ex.message}", ex)
        }).execute()
    }

    fun fnSupervisorFactura(faCodDocumento: Int) {
        val id = getString(R.string.str_FaFacturaEncuesta).toInt()
        val cadena = "2,$bodega,'$faCodDocumento',$vgiSupervisor,2"

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = {
            showResultDialog()
        }, onError = { ex ->
            Log.e("Encuesta", "Falló: ${ex.message}", ex)
        }).execute()
    }
    fun fnConsultaCliente() { fnCliente() }

    fun fnCambioTransporte() {
        fnMensajeSistemaPregunta("Esta seguro de solicitar Cambio de Transporte?", true) { resultado ->
            if (resultado != 1) return@fnMensajeSistemaPregunta

            val id = getString(R.string.str_FaTransporteCliente).toInt()
            val cadena = "2,$bodega,'${txtNumero.text}',$usuario,1"
            val progressDialog = showProgressDialogItems()

            clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = {
                progressDialog.dismiss()
                fnMensajeSistema("Cambio de Transporte solicitado, consulte con los responsables.")
            }, onError = { ex ->
                progressDialog.dismiss()
                Log.e("CambioTransporte", "Falló: ${ex.message}", ex)
            }).execute()
        }
    }

    private fun fnPermisoVendedorMayorista(cliente: String, nivelCliente: String, resultado: (Boolean) -> Unit) {
        val id = getString(R.string.str_FaPermisoVendedorClienteMayorista).toInt()
        val cadena = "2,$ep_codigo,$cliente,$usuario,$nivelCliente,1,$bodega"

        clsObtenerDatos(requireContext(), solicitudSoap, id, cadena, onSuccess = { xml ->
            val respuesta = fnObtenerValoresXml(xml).firstOrNull()?.toIntOrNull() == 1
            resultado(respuesta)
        }, onError = { ex ->
            Log.e("PermisoMayorista", "Falló: ${ex.message}", ex)
            resultado(false)
        }).execute()
    }

    private fun fnProcesarTablaPrecios(xmlResult: String): List<Map<String, Any>> {
        val lista = mutableListOf<Map<String, Any>>()
        if (xmlResult.isBlank()) return lista

        return try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlResult))

            var eventType = parser.eventType
            var currentTag = ""
            var descripcion = ""
            var precio = 0.0
            var margen = 0.0

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> currentTag = parser.name.lowercase(Locale.ROOT)
                    XmlPullParser.TEXT -> {
                        val value = parser.text?.trim().orEmpty()
                        if (value.isNotBlank()) {
                            when {
                                currentTag.endsWith("descripcion") -> descripcion = value
                                currentTag.endsWith("precio") -> precio = value.toDoubleOrNull() ?: 0.0
                                currentTag.endsWith("margen") -> margen = value.toDoubleOrNull() ?: 0.0
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name.equals("Table", ignoreCase = true)) {
                            lista.add(mapOf("descripcion" to descripcion, "precio" to precio, "margen" to margen))
                            descripcion = ""
                            precio = 0.0
                            margen = 0.0
                        }
                    }
                }
                eventType = parser.next()
            }

            lista
        } catch (e: Exception) {
            Log.e("fnProcesarTablaPrecios", "Error: ${e.message}", e)
            lista
        }
    }

    private fun fnParseSeguro(xml: String): Double? {
        return try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xml))

            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "Column1") {
                    return parser.nextText().trim().toDoubleOrNull()
                }
                event = parser.next()
            }

            null
        } catch (e: Exception) {
            Log.e("XML", "Error parseando Column1", e)
            null
        }
    }


    private fun fnParseTarifas(xml: String): List<Tarifa> {
        val listaTarifas = mutableListOf<Tarifa>()

        return try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xml))

            var event = parser.eventType
            var peso: Double? = null
            var tarifa1: Double? = null
            var tarifa2: Double? = null
            var descripcion: String? = null

            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        val tag = parser.name?.lowercase(Locale.getDefault()).orEmpty()
                        when {
                            tag.endsWith("peso") -> peso = parser.nextText().trim().toDoubleOrNull()
                            tag.endsWith("tarifa1") -> tarifa1 = parser.nextText().trim().toDoubleOrNull()
                            tag.endsWith("tarifa2") -> tarifa2 = parser.nextText().trim().toDoubleOrNull()
                            tag.endsWith("descripcion") -> descripcion = parser.nextText().trim()
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name?.lowercase(Locale.getDefault())?.endsWith("table") == true) {
                            listaTarifas.add(Tarifa(peso, tarifa1, tarifa2, descripcion))
                            peso = null
                            tarifa1 = null
                            tarifa2 = null
                            descripcion = null
                        }
                    }
                }
                event = parser.next()
            }

            listaTarifas
        } catch (e: Exception) {
            Log.e("XML", "Error parseando tarifas", e)
            listaTarifas
        }
    }

    private fun fnObtenerListaRegaloXML(xml: String): List<ItemRegalo> {
        val lista = mutableListOf<ItemRegalo>()
        if (xml.isBlank()) return lista

        return try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var tagActual = ""
            var dentroDeTable = false

            var codigo = ""
            var referencia = ""
            var descripcion = ""
            var existenciaA = ""
            var existenciaB = ""
            var existenciaC = ""
            var existenciaD = ""
            var cantidad = ""
            var precio = ""
            var costoProm = ""
            var peso = ""

            fun limpiar() {
                codigo = ""
                referencia = ""
                descripcion = ""
                existenciaA = ""
                existenciaB = ""
                existenciaC = ""
                existenciaD = ""
                cantidad = ""
                precio = ""
                costoProm = ""
                peso = ""
            }

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        tagActual = parser.name?.lowercase(Locale.ROOT).orEmpty()
                        if (tagActual == "table") {
                            dentroDeTable = true
                            limpiar()
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (dentroDeTable) {
                            val valor = parser.text?.trim().orEmpty()
                            if (valor.isNotBlank()) {
                                when {
                                    tagActual.endsWith("codigo") -> codigo = valor
                                    tagActual.endsWith("referencia") -> referencia = valor
                                    tagActual.endsWith("descripcion") -> descripcion = valor
                                    tagActual.endsWith("existenciaa") || tagActual.endsWith("existencia_a") -> existenciaA = valor
                                    tagActual.endsWith("existenciab") || tagActual.endsWith("existencia_b") -> existenciaB = valor
                                    tagActual.endsWith("existenciac") || tagActual.endsWith("existencia_c") -> existenciaC = valor
                                    tagActual.endsWith("existenciad") || tagActual.endsWith("existencia_d") -> existenciaD = valor
                                    tagActual.endsWith("cantidad") -> cantidad = valor
                                    tagActual.endsWith("precio") -> precio = valor
                                    tagActual.endsWith("costoprom") || tagActual.endsWith("costo_prom") -> costoProm = valor
                                    tagActual.endsWith("peso") -> peso = valor
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name.equals("Table", ignoreCase = true)) {
                            val cant = cantidad.toDoubleOrNull() ?: 1.0
                            val pre = precio.toDoubleOrNull() ?: 0.0
                            val pes = peso.toDoubleOrNull() ?: 0.0
                            val subtotal = cant * pre
                            val subPeso = cant * pes

                            lista.add(
                                ItemRegalo(
                                    codigo = codigo,
                                    referencia = referencia,
                                    descripcion = descripcion,
                                    existenciaA = existenciaA,
                                    existenciaB = existenciaB,
                                    existenciaC = existenciaC,
                                    existenciaD = existenciaD,
                                    cantidad = cantidad.ifBlank { "1" },
                                    precio = precio.ifBlank { "0" },
                                    descuento = "0",
                                    subtotal = fnDecimal(subtotal),
                                    conDesc = fnDecimal(subtotal),
                                    peso = peso.ifBlank { "0" },
                                    subPeso = fnDecimal(subPeso),
                                    costoProm = costoProm.ifBlank { "0" },
                                    proceso = "0",
                                    secuencia = vgiSecuencia.toString()
                                )
                            )
                            dentroDeTable = false
                        }
                    }
                }
                eventType = parser.next()
            }

            lista
        } catch (e: Exception) {
            Log.e("Regalos", "Error parseando XML", e)
            lista
        }
    }

    private fun fnMostrarDialogoListaRegalo(
        data: String,
        showRegalo: ProgressDialog,
        onSeleccionado: (ItemRegalo) -> Unit
    ) {
        if (dialogoListaRegalo?.isShowing == true) return

        showRegalo.dismiss()
        val lista = fnObtenerListaRegaloXML(data)

        if (lista.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("Listado de Regalo")
                .setMessage("No hay datos para mostrar")
                .setPositiveButton("Aceptar", null)
                .show()
            return
        }

        val etiquetas = lista.map {
            "${it.referencia} - ${it.descripcion}\nCant: ${it.cantidad}  Precio: ${it.precio}"
        }.toTypedArray()

        dialogoListaRegalo = AlertDialog.Builder(requireContext())
            .setTitle("Listado de Regalo")
            .setItems(etiquetas) { dialog, which ->
                onSeleccionado(lista[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cerrar", null)
            .create()

        dialogoListaRegalo?.show()
    }

    private fun fnClaveXml(xmlResult: String): String? {
        if (xmlResult.isBlank()) return null

        return try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlResult))

            var contador = 0
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.TEXT) {
                    val value = parser.text?.trim()
                    if (!value.isNullOrBlank()) {
                        contador++
                        if (contador == 1) return value
                    }
                }
                eventType = parser.next()
            }

            null
        } catch (e: Exception) {
            Log.e("fnClaveXml", "Error: ${e.message}", e)
            null
        }
    }

    data class DetalleFila(
        val codigo: String = "",
        val costoProm: Double = 0.0,
        val proceso: Int = 0,
        val cantidad: Double = 0.0,
        val precio: Double = 0.0,
        val descuento: Double = 0.0,
        val secuencia: Int = 0,
        val combo: Int = 0,
        val num: Int = 0,
        val referencia: String = "",
        val peso: Double = 0.0,
        val subPeso: Double = 0.0,
        val conDesc: Double = 0.0,
        val regalo: Int = 0
    )

    data class FacturaCabecera(
        var faCodDocumento: String = "",
        var faFechaFactura: String = "",
        var vendedor: String = "",
        var tr_codigo: String = "",
        var transporte: String = "",
        var cliente: String = "",
        var cedulaRuc: String = "",
        var direccion: String = "",
        var fono: String = "",
        var ciudad: String = "",
        var formaPago: String = "",
        var estadoPago: String = "",
        var plazo: String = "",
        var faValorTotFact: String = "",
        var faValorBruto: String = "",
        var faValorIva: String = "",
        var faFlete: String = "",
        var faSeguro: String = "",
        var faEstado: String = "",
        var usuarioAudita: String = "",
        var fechaaud: String = "",
        var descuento: String = "",
        var cl_codigo: String = "",
        var nivel: String = "",
        var encuesta: Int = 0,
        var usuarioelim: String = "",
        var fechaElim: String = "",
        var fa_supervisor: Int = 0,
        var observacion: String = ""
    )

    data class FacturaDetalle(
        var codigo: String = "",
        var referencia: String = "",
        var descripcion: String = "",
        var cantidad: String = "",
        var descuento: String = "",
        var precio: String = "",
        var subtotal: String = "",
        var conDesc: String = "",
        var peso: String = "",
        var subPeso: String = "",
        var costoProm: String = "",
        var proceso: String = "",
        var secuencia: String = ""
    )

    data class FacturaParseResult(
        val cabecera: FacturaCabecera?,
        val detalle: List<FacturaDetalle>
    )

    data class ItemDetalleFactura(
        val num: Int,
        val referencia: String,
        val descuento: Double,
        val cantidad: Double,
        val precio: Double,
        val subtotal: Double,
        val conDesc: Double,
        val peso: Double,
        val subPeso: Double,
        val codigo: String,
        val costoProm: Double,
        val proceso: Int,
        val secuencia: Int
    )

    data class ItemRegalo(
        val codigo: String,
        val referencia: String,
        val descripcion: String,
        val existenciaA: String,
        val existenciaB: String,
        val existenciaC: String,
        val existenciaD: String,
        val cantidad: String,
        val precio: String,
        var descuento: String,
        var subtotal: String,
        var conDesc: String,
        var peso: String,
        var subPeso: String,
        var costoProm: String,
        var proceso: String,
        var secuencia: String
    )
}

