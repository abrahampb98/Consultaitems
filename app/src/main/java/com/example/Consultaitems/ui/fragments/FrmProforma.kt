package com.example.Consultaitems.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.lifecycleScope
import android.view.KeyEvent
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.example.Consultaitems.utils.cls.consultaProforma
import com.example.Consultaitems.ui.adapters.Proformas
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.adapters.AdaptadorClientes
import com.example.Consultaitems.ui.adapters.MiAdaptadorRef
import com.example.Consultaitems.ui.adapters.MiAdaptadorRefProf
import com.example.Consultaitems.ui.adapters.MiAdapterDetalleProforma
import com.example.Consultaitems.ui.adapters.TransporteAdapter
import com.example.Consultaitems.ui.adapters.datos
import com.example.Consultaitems.ui.adapters.datosDet
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.ClsRegImagenOrden
import com.example.Consultaitems.utils.cls.DownloadImageTask
import com.example.Consultaitems.utils.cls.ImageCache
import com.example.Consultaitems.utils.cls.ProformasDialogFragment
import com.example.Consultaitems.utils.cls.SpinnerItem
import com.example.Consultaitems.utils.cls.Tarifa
import com.example.Consultaitems.utils.cls.consultaCliente
import com.example.Consultaitems.utils.cls.clsObtenerDatos
import com.example.Consultaitems.utils.cls.reporteProforma
import com.example.Consultaitems.utils.parser.XMlParserA
import com.example.Consultaitems.utils.parser.XMlParserFactura
import com.example.Consultaitems.utils.parser.XMlParserProforma
import com.example.Consultaitems.utils.parser.XmlFactura
import com.example.Consultaitems.utils.parser.XmlParserItemTemp
import com.example.Consultaitems.utils.parser.XmlParserStock
import com.example.Consultaitems.utils.parser.XmlProforma
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.property.HorizontalAlignment
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.StringReader
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class frmProformaA : Fragment(), MiAdaptadorRefProf.OnItemClickListener, MiAdaptadorRefProf.OnImageClickListener, MiAdapterDetalleProforma.AdapterCallbacks, MiAdapterDetalleProforma.OnItemClickListener, consultaCliente.OnItemSelectedListener, consultaProforma.OnItemSelectedListener {
    private lateinit var txtReferencia: TextView
    private lateinit var btnOk: Button
    lateinit var dbHelper: SqLiteOpenHelper
    lateinit var ReciclerviewRef: RecyclerView
    lateinit var btnAgregar: ImageView
    lateinit var btnPrecios: Button
    lateinit var txtCantidad: TextView
    private val datosList = mutableListOf<datos>()
    lateinit var ReciclerviewDet: RecyclerView
    val todosLosItemsDet = mutableListOf<datosDet>()
    private val itemsSeleccionados = mutableListOf<datos>()
    lateinit var txtPrecio: TextView
    lateinit var txtTotal : TextView
    lateinit var txtSub : TextView
    lateinit var txtIva : TextView
    lateinit var txtDesc : TextView
    private lateinit var spinnerFormaPag: Spinner
    private lateinit var spinnerItem: Spinner
    private lateinit var spinnerBodega: Spinner
    private lateinit var llenarControles: ClsLLenarControles  // Declaración
    lateinit var solicitudSoap: SolicitudSoap
    lateinit var txtVendedor : TextView
    private lateinit var txtFecha:TextView
    private lateinit var txtCliente: TextView
    private lateinit var txtTransporte: AutoCompleteTextView
    private lateinit var txtPolitica: TextView
    private lateinit var txtDescuentoT: TextView
    private lateinit var txtSeguro: TextView
    private lateinit var btnGuardar: Button
    private lateinit var btnOrden: Button
    private lateinit var ep_codigo: String
    private lateinit var usuario: String
    private lateinit var tr_codigo: String
    private  var cl_codigo: String = ""
    private lateinit var txtCIdentificacionPF: TextView
    private lateinit var txtObservacion: TextView
    private lateinit var txtFlete: TextView
    private lateinit var txtOrden: TextView
    private lateinit var tp_codigo: String
    private lateinit var pz_codigo: String
    private lateinit var im_codigo: String
    private lateinit var td_codigo: String
    private lateinit var pr_codigo: String
    private lateinit var pe_coddocumento: String
    private lateinit var txtNumero: TextView
    private lateinit var txtLote: TextView
    private lateinit var btnBuscar: ImageButton
    private lateinit var adaptadorDetalle: MiAdapterDetalleProforma
    private lateinit var adaptadorRef: MiAdaptadorRefProf
    private  var valInser: Int = 0
    private var vgsOpcionMenu: String = ""
    private lateinit var btnEliminar: Button
    private  var porcdescuento: Double = 0.0
    private var unidadCE: Double = 0.0
    private lateinit var XmlProforma: XmlProforma
    private lateinit var XmlFactura: XmlFactura
    private var vliExito: Int = 0
    private var vliGuardar: Int = 0
    private var vgsEstado: String = ""
    private lateinit var rootLayout: View
    private  var Tarifa: String = ""
    private  var Cobertura: String = ""
    private lateinit var Kilo: TextView
    private lateinit var tarifa: TextView
    private lateinit var cobertura: TextView
    private lateinit var btnImprimir:Button
    private val imageCache = ImageCache()
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var imageView: ImageView
    private val matrix = Matrix()
    private var scaleFactor = 1.0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private val currentMatrix = Matrix()
    private val matrixValues = FloatArray(9)
    private val minScale = 1.0f
    private val maxScale = 3.0f
    private val last = PointF()
    private val start = PointF()
    private var errorMessage: String =""
    private var estado: String = ""
    private var lopd: Int? = null
    private var campania: Int? = null
    private var posicion: Int? = null
    private var itemEnEdicion: Int? = null
    private var nuevoItemSeleccionado: datos? = null // Inicializado como nulo

    private val PICK_IMAGE_REQUEST = 1
    private var pdfBase64: String? = null
    private var cl_orden: Int = 0
    private lateinit var spinnerListado: Spinner

    private lateinit var constraintLayout: ConstraintLayout
    private  var margen: Double = 0.0

    private lateinit var btnClientePF: ImageButton
    private lateinit var btnAdicionalesPF: ImageButton
    private lateinit var lblseguroPF: TextView
    private lateinit var txtFormaPagoPF: TextView
    private lateinit var txtCiudadViewPF: TextView
    private lateinit var btnProcesarPF: Button
    private lateinit var btnTransportePF: ImageButton
    private lateinit var txtFleteMontoPF: EditText
    private lateinit var  txtNivelPF: TextView


    private var clienteSeleccionado: AdaptadorClientes.Clientes? = null

    private var calculandoPesoTarifa = false
    private var cargandoProforma = false

    var DireccionDA: String =""
    var TelefonoDA: String =""
    var CiudadDA: String =""
    var EmailDA: String =""
    var ContactoDA: String =""
    var XmlSeguro: String = ""
    var nivel: String = ""
    var factor: Double = 0.00
    var codFactor: Double = 0.00
    var factura: String = ""
    private var bodega: String = ""
    private var XmlTarifa: String = ""
    private var DatosTarifa: List<Tarifa> = emptyList()
    private var tp_descripcion: String = ""
    private var pr_estadotransporte: String = "N"
    private var vgDseguro: Double = 0.0
    private var Nombre: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)
        // Inflar el layout del fragmento
        val view = inflater.inflate(R.layout.frm_proforma, container, false)

        // Lógica para determinar el tamaño de la pantalla
        val screenSize = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK

        // Acceder a la actividad contenedora para cambiar la orientación
        val activity = requireActivity()

        // Si la pantalla es de tamaño "normal"
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            // Cambia la orientación a landscape para pantallas normales
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        } else {
            // Mantén la orientación en portrait para otros tamaños de pantalla
            //activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        adaptadorDetalle = MiAdapterDetalleProforma(todosLosItemsDet, this,
            itemClickListener = this,
            doubleClickListener = { item, position ->
                // Cargar los valores del ítem seleccionado en los campos de entrada
                item.Referencia
                item.Codigo
                txtCantidad.setText(item.Cantidad)
                txtPrecio.setText(item.Precio)
                txtDesc.setText(item.DescItem)
                txtReferencia.setText(llenarControles.fnObtenerReferenciaPorCodigo(item.Codigo))
                itemEnEdicion = position


                nuevoItemSeleccionado = null

                nuevoItemSeleccionado = datos(
                    referencia = item.Referencia,
                    stock = "0",
                    precioSub = item.Precio,
                    precioCont = item.Precio,
                    precioCred = item.Precio,
                    codigo = item.Codigo,
                    descripcion = item.Descripcion,
                    unidadCE = item.unidadCE,
                    costoProm = item.costProm,
                    combo = item.combo,
                    cd_codigo = item.combo,
                    it_regalo= item.regalo
                )
            })
        adaptadorDetalle.callbacks = this
        adaptadorRef = MiAdaptadorRefProf(datosList, this, this)
        llenarControles = ClsLLenarControles(requireContext())
        solicitudSoap = SolicitudSoap(requireContext())

        fnInicializarVariables(view)
        fnLLenarControles()
        fnDesactivarControles()


        btnGuardar.setOnClickListener {
            fnMostrarDialogoDeConfirmacion()
        }

        btnBuscar.setOnClickListener {
            fnProformas()
        }

        btnOk.setOnClickListener {
            if (cl_codigo != ""){
                fnBusqueda()
            }else{
                Toast.makeText(requireContext(), "Debe seleccionar un cliente", Toast.LENGTH_SHORT).show()
            }

        }

        btnAgregar.setOnClickListener {
            fnValidarYAgregarDetalles()
            hideSoftKeyboard()
        }

        btnEliminar.setOnClickListener {
            vgsOpcionMenu = "E"
            fnMostrarDialogoDeConfirmacion()
            //btnGuardar.isEnabled = false
        }

        btnImprimir.setOnClickListener {
            fnImprimir()
        }

        btnProcesarPF.setOnClickListener {
            if (bodega != "1") {
                fnVerificastock()
            } else {
                val progressDialog = showProgressDialogItems()
                MiAsyncTaskRestringirFacturaCliente(progressDialog).execute()
            }
        }


        btnClientePF.setOnClickListener {
            fnCliente()
        }

        btnAdicionalesPF.setOnClickListener{
            fnDatosAdicionales()
        }

        btnTransportePF.setOnClickListener {
            fnCambioTransporte()
        }


        txtNumero.setOnEditorActionListener { _, actionId, event ->
            if (fnEsEnter(actionId, event)) {
                txtNumero.isEnabled = false
                fnActivarControles()

                val codigo = txtNumero.text.toString().toIntOrNull() ?: 0
                fnConsultarProformaEnLinea(codigo)

                true
            } else {
                false
            }
        }

        txtReferencia.setOnEditorActionListener { _, actionId, event ->
            if (fnEsEnter(actionId, event)) {
                if (cl_codigo.isNotBlank()) {
                    fnBusqueda()
                } else {
                    Toast.makeText(requireContext(), "Debe seleccionar un cliente", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        txtCantidad.setOnEditorActionListener { _, actionId, event ->
            if (fnEsEnter(actionId, event)) {
                fnValidarYAgregarDetalles()
                hideSoftKeyboard()
                true
            } else {
                false
            }
        }

        txtCliente.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No necesitamos usar este método ahora
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No necesitamos usar este método ahora
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        txtDesc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (cargandoProforma) return
                if (!::adaptadorDetalle.isInitialized) return
                if (!::td_codigo.isInitialized) return
                if (!::tr_codigo.isInitialized) return
                if (adaptadorDetalle.datos.isEmpty()) return

                val tipoDesc = td_codigo.toIntOrNull() ?: 0

                if (tipoDesc == 1) {
                    fnactualizarDescuento(
                        descuentoTexto = s?.toString().orEmpty().ifBlank { "0" },
                        recalcularTotales = true
                    )
                }
            }
        })


        txtReferencia.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (vgsOpcionMenu == "I" || vgsOpcionMenu == "M") {
                    /* if (txtReferencia.text.isEmpty()){
                         if(itemEnEdicion!=null){
                             fnReferenciaCombos()
                         }
                     }
                     */
                }

            }
        })

        txtFleteMontoPF.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (::adaptadorDetalle.isInitialized) {
                    fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())
                }
            }
        })

        spinnerFormaPag.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position) as SpinnerItem
                tp_codigo = item.codigo
                tp_descripcion = item.descripcion
                txtFormaPagoPF.text = "Forma Pago: ${item.descripcion}"

                if (tp_codigo == "3" && vgsOpcionMenu == "I") {
                    fnFormaPago()
                } else if (vgsOpcionMenu == "M" && codFactor != 0.0) {
                    val datosTarjeta = runCatching {
                        llenarControles.fnObtenerTipoTarjeta(codFactor.toString())
                    }.getOrDefault(emptyList())

                    if (datosTarjeta.isNotEmpty()) {
                        txtFormaPagoPF.text = "${txtFormaPagoPF.text} - ${datosTarjeta.getOrNull(0).orEmpty()}"
                        factor = datosTarjeta.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                    }
                } else {
                    factor = 0.0
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        spinnerItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position) as SpinnerItem
                td_codigo = item.codigo
                //showToast(td_codigo)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Opcionalmente manejar la no selección
            }
        }

        return view
    }

    private fun fnLimpiarBusqueda(position: Int){
        adaptadorRef.fnObtenerSpinner(position)
        txtCantidad.setText("")
        txtPrecio.setText("")
        txtReferencia.setText("")
    }


    private fun fnBusqueda(){
        hideSoftKeyboard()
        // Obtener el texto de txtReferencia
        val referencia = txtReferencia.text.toString().trim()
        itemEnEdicion = null
        nuevoItemSeleccionado = null

        fnBuscarReferencia(referencia.trim())

        txtPrecio.setText("")
        txtCantidad.setText("")
    }

    private fun showProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Guardando Proforma...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    private fun showProgressDialogItems(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    private fun showProgressDialogStock(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Verificando Stock...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    private fun showProgressDialogFactura(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Guardando Factura...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    // Enviar datos
    private inner class MiAsyncTaskFactura(private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {
            return try {
                database = dbHelper.writableDatabase
                val cadena = XmlFactura.obtenerFactura(
                    2,
                    bodega.toIntOrNull() ?: 0,
                    txtNumero.text.toString().toInt(),
                    usuario,
                    1
                )
                solicitudSoap.initializeVariables(getString(R.string.str_verificaStock).toInt(), cadena)

                val result = solicitudSoap.realizarSolicitudSoap()
                    ?.bufferedReader()
                    ?.use { it.readText() }

                if (result.isNullOrBlank()) {
                    ""
                } else {
                    XMlParserFactura.parseAndUpdateDocumentCode(
                        result,
                        database,
                        txtNumero.text.toString(),
                        requireContext()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error al enviar Factura: ${e.message}"
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()

            if (result != null) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Sistema")
                    .setMessage("# Factura: $result")
                    .setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.dismiss() }
                    .show()
                btnProcesarPF.isEnabled = false
            } else {
                Toast.makeText(requireContext(), errorMessage.ifBlank { "Error al enviar Factura" }, Toast.LENGTH_LONG).show()
            }
        }
    }

    //valida si mi adapatador esta vacio
    override fun onEmptyState(isEmpty: Boolean) {
        spinnerItem.isEnabled = isEmpty
        fnActivarCabecera()
    }

    //manejo del menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.frm_menu_proforma, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    //opciones del menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bntNuevoPF -> {
                fnAccionesAlPulsarNuevo()
                true
            }

            R.id.btnModificarPF -> {
                fnAccionesAlPulsarModificar()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fnControlSpinners(){

        if (vgsOpcionMenu == "M"){
            if (adaptadorDetalle.itemCount > 0){
                spinnerItem.isEnabled = false
            }
        }
    }

    private fun hideSoftKeyboard() {
        Log.d("hideSoftKeyboard", "Ocultando el teclado")
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = requireActivity().currentFocus
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
            Log.d("hideSoftKeyboard", "Teclado ocultado")
        } else {
            Log.d("hideSoftKeyboard", "No hay vista enfocada para ocultar el teclado")
        }
        forceRedrawWindow(requireActivity())
    }

    fun forceRedrawWindow(activity: Activity) {
        val contentView = activity.findViewById<View>(android.R.id.content)
        contentView.requestLayout()
    }

    fun fnIsNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun fnMostrarDialogoDePedidos() {
        val dialog = ProformasDialogFragment()
        var pedido: Int = 0
        dialog.onPedidoSelected = { numeroPedido ->
            // Aquí puedes manejar el número de pedido seleccionado
            requireActivity().runOnUiThread {
                //Toast.makeText(this, "Pedido seleccionado: $numeroPedido", Toast.LENGTH_LONG).show()
                pedido = numeroPedido.toInt()
            }

            if (pedido != 0 ){
                txtReferencia.setText("")
                adaptadorRef.clearItems()
                txtNumero.text = pedido.toString()
                fnConsultarProforma(pedido)
                hideSoftKeyboard()
                fnActivarControles()
                fnControlSpinners()
                if (vgsEstado == "C"){
                    fnDesactivarControles()
                    btnImprimir.isEnabled  = true
                    btnProcesarPF.isEnabled = true
                }
                if (factura != ""){
                    btnProcesarPF.isEnabled = false
                }
                //fnReferenciaCombos()
            }
        }
        dialog.show(requireActivity().supportFragmentManager, "PedidosDialogFragment")

    }

    private fun fnInicializarVariables(view: View) {
        txtReferencia = view.findViewById(R.id.txtReferenciaPF)
        btnOk = view.findViewById(R.id.btnBusquedaPF)
        dbHelper = SqLiteOpenHelper(requireContext())
        ReciclerviewRef = view.findViewById(R.id.recyclerViewRefPF)
        btnAgregar = view.findViewById(R.id.btnAgregarPF)
        txtCantidad = view.findViewById(R.id.txtCantidadPF)
        ReciclerviewDet = view.findViewById(R.id.recyclerViewDetallePF)
        txtPrecio = view.findViewById(R.id.txtPrecioPF)
        txtTotal = view.findViewById<TextView>(R.id.txtTotalPvPF)
        txtSub = view.findViewById<TextView>(R.id.txtSubotalPF)
        txtIva = view.findViewById<TextView>(R.id.txtIvaPF)
        txtDesc = view.findViewById<TextView>(R.id.txtDescuentoPF)
        txtVendedor = view.findViewById<TextView>(R.id.txtVendedorPF)
        txtFecha = view.findViewById<TextView>(R.id.txtFechaPF)
        txtFecha.text = fnFechaCorta()
        txtTransporte = view.findViewById(R.id.txtTransportePF)
        txtCliente = view.findViewById<TextView>(R.id.txtClientePF)
        txtDescuentoT = view.findViewById<TextView>(R.id.txtDescuentoTPF)
        txtSeguro = view.findViewById<TextView>(R.id.txtSeguroPF)
        spinnerFormaPag= view.findViewById(R.id.spinnerFormaPagPF)
        spinnerItem = view.findViewById(R.id.spinnerItemPF)
        spinnerBodega = view.findViewById(R.id.spinnerBodegaPF)
        btnGuardar = view.findViewById(R.id.btnGuardarPF)
        txtObservacion = view.findViewById(R.id.txtObservacionPF)
        txtFlete = view.findViewById(R.id.txtFletePF)
        txtNumero = view.findViewById(R.id.txtNumeroPF)
        txtLote = view.findViewById(R.id.txtLotePF)
        btnBuscar = view.findViewById(R.id.btnBuscarPF)
        btnEliminar = view.findViewById(R.id.btnEliminarDocPF)
        btnImprimir = view.findViewById(R.id.btnImprimirPF)
        btnClientePF = view.findViewById(R.id.btnClientePF)
        txtCIdentificacionPF = view.findViewById(R.id.txtCIdentificacionPF)
        btnAdicionalesPF = view.findViewById(R.id.btnAdicionalesPF)
        btnTransportePF = view.findViewById(R.id.btnTransportePF)
        txtFleteMontoPF = view.findViewById(R.id.txtFleteMontoPF)
        lblseguroPF = view.findViewById(R.id.lblseguroPF)
        txtFormaPagoPF = view.findViewById(R.id.txtFormaPagoPF)
        txtCiudadViewPF = view.findViewById(R.id.txtCiudadViewPF)
        btnProcesarPF = view.findViewById(R.id.btnProcesarPF)
        txtNivelPF = view.findViewById(R.id.txtNivelPF)

        Kilo = view.findViewById(R.id.txtKiloViewPF)
        tarifa = view.findViewById(R.id.txtTarifaViewPF)
        cobertura = view.findViewById(R.id.txtCoberturaViewPF)


        constraintLayout = view.findViewById(R.id.constraintPF)

        //spinnerListado = view.findViewById(R.id.spinnerListadoPF)

        hideSoftKeyboard()
    }

    private fun fnBuscarReferencia(BusReferencia: String) {
        if (BusReferencia.isNotEmpty()) {
            datosList.clear()
            adaptadorRef.clearItems()

            val progressDialog = showProgressDialogItems()
            MiAsyncTaskItems(progressDialog ).execute()
        }
    }

    private fun fnReferenciaCombos(combo: String){
        datosList.clear()
        adaptadorRef.clearItems()

        val resultados = llenarControles.fnBuscaReferenciaCombos(combo)
        for (dato in resultados) {
            datosList.add(dato)
        }
        // Configura el RecyclerView y asigna el adaptador
        val layoutManager = LinearLayoutManager(requireContext())
        ReciclerviewRef.layoutManager = layoutManager
        ReciclerviewRef.adapter = adaptadorRef
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(item: datos) {
        txtCantidad.requestFocus()

        when (nivel) {
            "7", "8" -> {
                val base = item.pv_precio7.toBigDecimal()
                val comision = base.multiply(factor.toBigDecimal()).divide(BigDecimal(100))
                val precio = base.add(comision).setScale(3, RoundingMode.HALF_UP)
                txtPrecio.text = precio.toPlainString()
                //Log.e("","$factor")
            }
            "6" -> {
                val base = item.precioCred.toBigDecimal()
                val comision = base.multiply(factor.toBigDecimal()).divide(BigDecimal(100))
                val precio = base.add(comision).setScale(3, RoundingMode.HALF_UP)
                txtPrecio.text = precio.toPlainString()
                //Log.e("","$factor")
            }
            "5" -> {
                val base = item.precioCont.toBigDecimal()
                val comision = base.multiply(factor.toBigDecimal()).divide(BigDecimal(100))
                val precio = base.add(comision).setScale(3, RoundingMode.HALF_UP)
                txtPrecio.text = precio.toPlainString()
            }
        }

    }

    override fun onFirstImageClick(referencia: String, codigo: String) {
        fnPreciosStock(referencia, codigo)
    }

    override fun onSecondImageClick(codigo: String) {
        fnShowImageDialog(codigo)
    }

    fun fnCalcularTotales(subt: Double) {
        var descuento = adaptadorDetalle.fnObtenerDescuento()
        unidadCE = adaptadorDetalle.fnObtenerPeso()

        val costoPromedio = adaptadorDetalle.fnObtenerCostoProm()

        val descTxt = txtDesc.text.toString()
        val descuentoTXT = if (descTxt.isBlank()) {
            BigDecimal.ZERO
        } else {
            descTxt.toBigDecimalOrNull() ?: BigDecimal.ZERO
        }

        val subtotalDescontado: Double = if (descuentoTXT > BigDecimal("0.00")) {
            subt - descuento
        } else {
            descuento = 0.0
            subt
        }

        val aplicarSeguro = vgDseguro > 0.0

        val seguroPercent = if (aplicarSeguro) {
            llenarControles.fnObtenerSeguro().toDouble() / 100.0
        } else {
            0.0
        }

        val montoSeguro = subtotalDescontado * seguroPercent
        val subtotalFinal = subtotalDescontado + montoSeguro

        val ivaInc = fnIva()
        val iva = ivaInc * subtotalFinal / 100.0

        val lote = if (costoPromedio <= 0.0) {
            0.0
        } else {
            subtotalDescontado / costoPromedio
        }

        val flete: Double
        val total: Double

        if (tr_codigo == "12") {
            flete = fnTransporte().toString().toDoubleOrNull() ?: 0.0
            total = subtotalFinal + iva + flete
            fnPesos()
        } else {
            flete = txtFleteMontoPF.text.toString().toDoubleOrNull() ?: 0.0
            total = subtotalFinal + iva + flete
        }

        txtSub.text = String.format("%.2f", subt)
        txtDescuentoT.text = String.format("%.2f", descuento)
        txtSeguro.text = String.format("%.2f", montoSeguro)
        txtIva.text = String.format("%.2f", iva)
        txtTotal.text = String.format("%.2f", total)
        txtLote.text = String.format("%.2f", lote)
        txtFlete.text = String.format("%.2f", flete)

        porcdescuento = adaptadorDetalle.fnObtenerPorctDesc()
    }

    private fun fnIva(): Int {
        val database = dbHelper.readableDatabase
        val cursor = database.rawQuery("SELECT pi_porcentaje FROM fa_ws_parametroIva", null)
        var porcentajeIva = 0 // Valor predeterminado en caso de que no se encuentre ningún resultado
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex("pi_porcentaje")
            porcentajeIva = cursor.getInt(columnIndex).toInt()
        }
        cursor.close()
        database.close()
        return porcentajeIva
    }

    private fun fnFecha(): String {
        val tz = TimeZone.getTimeZone("America/Guayaquil")
        val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("es", "EC"))
        fmt.timeZone = tz
        return fmt.format(Date())
    }


    private fun fnFechaCorta(): String {
        val fechaActual = Date()
        // Formatear la fecha como string
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaFormateada = dateFormat.format(fechaActual)
        return fechaFormateada

    }

    private fun fnLLenarControles(){
        llenarControles.fnLLenarSpinner(spinnerItem, "fa_ws_tipoDescuentoPedido")
        llenarControles.fnLLenarSpinnerBodega(spinnerBodega, "ve_ws_vendedor")

        spinnerBodega.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position) as? SpinnerItem
                bodega = item?.codigo ?: "0"

                if (vgsOpcionMenu == "I") {
                    fnObtenerSecuencia()
                    runCatching {
                        llenarControles.fnLLenarSpinnerFormaPago(spinnerFormaPag, bodega)
                    }
                    if (bodega == "1") {
                        spinnerFormaPag.setSelection(3)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        margen = llenarControles.fnObtenerMargen()

        val vendedor = llenarControles.fnLLenarVendedor()
        if (vendedor != null) {
            usuario = vendedor.login
            txtVendedor.text = usuario
            ep_codigo = vendedor.codigo
        }

        val itemBodega = spinnerBodega.selectedItem as? SpinnerItem
        bodega = itemBodega?.codigo ?: bodega.ifBlank { "0" }

        runCatching {
            llenarControles.fnLLenarSpinnerFormaPago(spinnerFormaPag, bodega)
        }.onFailure {
            llenarControles.fnLLenarSpinner(spinnerFormaPag, "cc_ws_parametrostransaccionesxbodega")
        }

        val adaptadorTransporte = TransporteAdapter(requireContext(), llenarControles.fnCargarDatosTransporte())
        txtTransporte.setAdapter(adaptadorTransporte)

        txtTransporte.setOnItemClickListener { _, _, position, _ ->
            if (cargandoProforma) return@setOnItemClickListener

            val transporte = adaptadorTransporte.getItem(position)

            if (transporte != null) {
                txtTransporte.setText(transporte.nombre, false)
                tr_codigo = transporte.codigo

                if (txtSub.text.toString().toDoubleOrNull() != 0.0) {
                    fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())

                    if (tr_codigo == "12") {
                        ejecutarPesoTarifaSeguro()
                    }
                }

                if (tr_codigo != "12" && tr_codigo != "0") {
                    txtFleteMontoPF.isEnabled = true
                    txtFleteMontoPF.requestFocus()
                } else {
                    txtFleteMontoPF.isEnabled = false
                }
            }
        }
    }

    private fun ejecutarPesoTarifaSeguro() {
        if (cargandoProforma) return
        if (calculandoPesoTarifa) return

        calculandoPesoTarifa = true

        try {
            fnPesoTarifaA()
        } catch (e: Exception) {
            Log.e("frmProformaA", "Error ejecutando fnPesoTarifaA: ${e.message}", e)
        } finally {
            calculandoPesoTarifa = false
        }
    }

    fun fnDesactivarControles() {
        txtNumero.isEnabled = false
        txtVendedor.isEnabled = false
        txtFecha.isEnabled = false

        spinnerFormaPag.isEnabled = false
        spinnerItem.isEnabled = false
        spinnerBodega.isEnabled = false

        txtCliente.isEnabled = false
        txtTransporte.isEnabled = false
        txtObservacion.isEnabled = false
        txtPrecio.isEnabled = false
        txtDesc.isEnabled = false
        txtCantidad.isEnabled = false
        txtReferencia.isEnabled = false
        txtCIdentificacionPF.isEnabled = false

        btnAgregar.isEnabled = false
        btnOk.isEnabled = false
        btnGuardar.isEnabled = false
        btnEliminar.isEnabled = false
        btnBuscar.isEnabled = false
        btnImprimir.isEnabled = false
        btnClientePF.isEnabled = false
        btnAdicionalesPF.isEnabled = false
        btnProcesarPF.isEnabled = false
        btnTransportePF.isEnabled = false

        btnClientePF.isClickable = false

        txtFlete.isClickable = false
        txtFlete.isEnabled = false

        txtFleteMontoPF.isEnabled = false

        setEnabledAll(ReciclerviewRef, false)
        setEnabledAll(ReciclerviewDet, false)

        if (::adaptadorDetalle.isInitialized) {
            adaptadorDetalle.fnsetHabilitado(false)
        }
    }

    fun fnActivarControles() {
        spinnerFormaPag.isEnabled = true
        spinnerItem.isEnabled = true
        spinnerBodega.isEnabled = true

        txtCliente.isEnabled = true
        txtTransporte.isEnabled = true
        txtObservacion.isEnabled = true
        txtPrecio.isEnabled = true
        txtCantidad.isEnabled = true
        txtReferencia.isEnabled = true
        txtDesc.isEnabled = true

        btnAgregar.isEnabled = true
        btnOk.isEnabled = true
        btnGuardar.isEnabled = true
        btnBuscar.isEnabled = true
        btnClientePF.isEnabled = true
        btnAdicionalesPF.isEnabled = true

        btnClientePF.isClickable = true

        txtFlete.isClickable = true
        txtFlete.isEnabled = true

        txtFleteMontoPF.isEnabled = true

        setEnabledAll(ReciclerviewRef, true)
        setEnabledAll(ReciclerviewDet, true)

        if (::adaptadorDetalle.isInitialized) {
            adaptadorDetalle.fnsetHabilitado(true)
        }
    }

    private fun setEnabledAll(view: View, enabled: Boolean) {
        view.isEnabled = enabled

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setEnabledAll(view.getChildAt(i), enabled)
            }
        }
    }

    fun fnAccionesAlPulsarModificar() {
        fnDesactivarControles()
        fnLimpiarControles()

        txtNumero.isEnabled = true
        txtNumero.requestFocus()

        btnBuscar.isEnabled = true

        vgsOpcionMenu = "M"
        btnGuardar.text = "Modificar"

        constraintLayout.background = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.background
        )
    }

    fun fnAccionesAlPulsarNuevo() {
        txtNumero.isEnabled = false
        btnBuscar.isEnabled = false
        btnProcesarPF.isEnabled = false

        if (::adaptadorDetalle.isInitialized) {
            adaptadorDetalle.clearItems()
        }

        if (::adaptadorRef.isInitialized) {
            adaptadorRef.clearItems()
        }

        fnLimpiarControles()

        itemEnEdicion = null
        vgsOpcionMenu = "I"
        btnGuardar.text = "Guardar"

        val itemBodega = spinnerBodega.selectedItem as? SpinnerItem
        bodega = itemBodega?.codigo ?: bodega.ifBlank { "0" }

        fnObtenerSecuencia()

        fnActivarControles()

        btnEliminar.isEnabled = false
        btnImprimir.isEnabled = false

        vgsEstado = "A"

        constraintLayout.background = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.background
        )

        if (bodega == "1") {
            spinnerFormaPag.setSelection(3)
        }
    }

    fun fnAccionesAlPulsarTodos(){
        adaptadorDetalle.clearItems()
        adaptadorRef.clearItems()
        fnLimpiarControles()
    }

    fun fnLimpiarControles() {
        val estadoAnteriorCargando = cargandoProforma
        cargandoProforma = true

        try {
            txtNumero.setText("")
            txtCliente.setText("")
            txtObservacion.setText("")
            txtReferencia.setText("")
            txtCliente.setText("")

            txtDesc.setText("0.00")
            txtTransporte.setText("")
            txtFlete.setText("0.00")
            txtCantidad.setText("")
            txtPrecio.setText("")

            txtFormaPagoPF.setText("")
            txtCiudadViewPF.setText("Ciudad")
            cobertura.setText("Cobertura")
            Kilo.setText("0.00")
            tarifa.setText("0.00")
            txtCIdentificacionPF.setText("")

            ContactoDA = ""

            spinnerItem.setSelection(0)
            spinnerFormaPag.setSelection(0)

            if (::adaptadorDetalle.isInitialized) {
                adaptadorDetalle.clearItems()
            }

            if (::adaptadorRef.isInitialized) {
                adaptadorRef.clearItems()
            }

            itemEnEdicion = null

            if (::adaptadorDetalle.isInitialized && ::tr_codigo.isInitialized) {
                fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())
            }

            if (::llenarControles.isInitialized) {
                val vendedor = llenarControles.fnLLenarVendedor()

                if (vendedor != null) {
                    usuario = vendedor.login
                    txtVendedor.setText(usuario)

                    ep_codigo = vendedor.codigo
                }
            }

        } finally {
            cargandoProforma = estadoAnteriorCargando
        }
    }

    fun fnGuardadoautomatico(){
        if (adaptadorDetalle.itemCount > 0 ){

            val item = spinnerBodega.selectedItem as? SpinnerItem
            val bodega = item?.codigo

            vliGuardar = 1
            //funcion donde guarda los datos de la cabecera y el detalle
            val cabValues = ContentValues().apply {
                put("em_codigo", 2)
                put("bo_codigo", 2)
                put("pr_codigo", txtNumero.text.toString())
                put("pr_fechatrn", fnFecha())
                put("ep_codigo", ep_codigo)
                put("cl_codigo", cl_codigo)
                put("pr_cedula", txtCIdentificacionPF.text.toString())
                put("pr_nombre", txtCliente.text.toString())
                put("pr_direccion", clienteSeleccionado?.direccion ?:"" )
                put("pr_fono", clienteSeleccionado?.fono ?:"")
                put("pr_email", clienteSeleccionado?.en_correo ?:"")
                put("pr_contacto", ContactoDA)
                put("tr_codigo", tr_codigo)
                put("tp_codigo", tp_codigo)
                put("pr_estadopago", "N")
                put("pr_observacion", txtObservacion.text.toString())
                put("pr_valorbruto", txtSub.text.toString())
                put("pr_valordesc",  txtDescuentoT.text.toString())
                put("pr_porcseguro", lblseguroPF.text.toString().replace("%", ""))
                put("pr_valorseguro", txtSeguro.text.toString())
                put("pr_valorflete", txtFlete.text.toString())
                put("pr_porcdesc", porcdescuento)
                put("pr_valoriva", txtIva.text.toString())
                put("pr_valortotal", txtTotal.text.toString())
                put("pr_estado ","A")
                put("tc_codigo", 0)
                put("pr_usuarioing", usuario)
                put("pr_fechaing", txtFecha.text.toString())
                put("pr_estadotransporte", "N")
                put("gc_codigo", 0)
                put("pr_nivel", nivel)
                put("pr_lote",txtLote.text.toString())
                put("ci_descripcion", txtCiudadViewPF.text.toString())
            }

            // Obtén la instancia del Adapter
            val adapterDetalle = ReciclerviewDet.adapter as MiAdapterDetalleProforma

            // Obtén los datos del Adapter
            val detallesDelPedido = adapterDetalle.datos.mapIndexed { index, detalle ->
                ContentValues().apply {
                    val precio = String.format("%.3f", detalle.Precio.toDouble()).toDouble()

                    put("em_codigo", 2)
                    put("bo_codigo", bodega)
                    put("pr_codigo", txtNumero.text.toString())
                    put("dp_fechatrn", fnFecha())
                    put("dp_secuencia", index + 1)
                    put("it_codigo", detalle.Codigo)
                    put("it_referencia", detalle.Referencia)
                    put("dp_cantidad", detalle.Cantidad.toInt())
                    put("dp_cantidadfac", detalle.Cantidad.toInt())
                    put("dp_precio", precio)
                    put("dp_preciofac", precio)
                    put("dp_valorbruto", precio * detalle.Cantidad.toInt())
                    put("um_pesoCE", detalle.unidadCE)
                    put("dp_porcdescto", detalle.DescItem)
                    put("cb_codigo", detalle.combo)
                    put("dp_costoPromedio", detalle.costProm)
                    put("dp_estado", "A")
                    put("dp_usuarioing", usuario)
                    put("dp_fechaing", txtFecha.text.toString())
                }
            }

            if (llenarControles.fnGuardarDatosProforma(txtNumero.text.toString().toInt(),cabValues, detallesDelPedido, usuario)) {
                Log.d("ClsLLenarControles", "Pedido insertado con éxito")

            } else {
                Log.e("ClsLLenarControles", "Fallo al insertar pedido")
            }

        }else{
            showToast("Ingrese lineas de detalle")
            vliGuardar = 0
        }

    }

    fun fnAgregarDetalles() {
        if (txtCantidad.text.toString().isNotEmpty()) {

            var selectedItems =
                (ReciclerviewRef.adapter as? MiAdaptadorRefProf)?.getSelectedItems()

            if (selectedItems.isNullOrEmpty() || itemEnEdicion != null) {
                selectedItems = nuevoItemSeleccionado?.let { listOf(it) } ?: emptyList()
            }

            if (selectedItems.isNullOrEmpty()) {
                showToast("No se han seleccionado elementos para agregar")
                return
            }

            selectedItems.forEach { item ->
                val cantidad = txtCantidad.text.toString().toIntOrNull() ?: 0

                val precio = String.format(
                    "%.3f",
                    txtPrecio.text.toString().toBigDecimal().setScale(3)
                )

                val subtotal = String.format(
                    "%.2f",
                    cantidad.toBigDecimal().multiply(precio.toBigDecimal())
                )

                val descuento = String.format(
                    "%.2f",
                    txtDesc.text.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
                )

                val conDescto = if (descuento.toBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
                    val valorDescuento = subtotal.toBigDecimal()
                        .multiply(descuento.toBigDecimal())
                        .divide(BigDecimal("100"), RoundingMode.HALF_EVEN)
                        .setScale(2, RoundingMode.HALF_UP)

                    subtotal.toBigDecimal().subtract(valorDescuento)
                } else {
                    BigDecimal("0.00")
                }

                val unidadFormateada = String.format(
                    "%.2f",
                    item.unidadCE.toDouble()
                )

                val sugerencia = String.format(
                    "%.2f",
                    (
                            precio.toDouble() *
                                    (1.0 - (descuento.toDouble() / 100.0))
                            ) / item.costoProm.toDouble()
                )

                val secuenciaDetalle = todosLosItemsDet.size + 1

                val newItem = datosDet(
                    item.referencia,
                    cantidad.toString(),
                    precio,
                    subtotal,
                    item.codigo,
                    item.descripcion,
                    unidadFormateada,
                    descuento,
                    conDescto.toString(),
                    item.costoProm,
                    sugerencia,
                    "0",
                    item.it_regalo,
                    0,
                    0,
                    secuenciaDetalle
                )

                itemEnEdicion?.let { indiceEdicion ->
                    if (
                        todosLosItemsDet.isNotEmpty() &&
                        indiceEdicion >= 0 &&
                        indiceEdicion < todosLosItemsDet.size
                    ) {
                        todosLosItemsDet[indiceEdicion] = newItem
                        itemEnEdicion = null
                    }
                } ?: run {
                    if (!fnVerificarDuplicados(newItem)) {
                        todosLosItemsDet.add(newItem)
                    } else {
                        showToast("El ítem ya existe")
                    }
                }

                ReciclerviewDet.adapter = adaptadorDetalle
                ReciclerviewDet.layoutManager = LinearLayoutManager(requireContext())
                adaptadorDetalle.notifyDataSetChanged()
            }

            fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())

            txtCantidad.setText("")
            vliExito = 1

            txtCantidad.setText("")
            txtPrecio.setText("")
            txtReferencia.setText("")
        }
    }

    fun fnAgregarDetallesCombo() {
        var selectedItems = (ReciclerviewRef.adapter as? MiAdaptadorRefProf)?.getSelectedItems()

        if (selectedItems.isNullOrEmpty() || itemEnEdicion != null) {
            selectedItems = nuevoItemSeleccionado?.let { listOf(it) } ?: emptyList()
        }

        if (selectedItems.isNullOrEmpty()) {
            showToast("No se han seleccionado elementos para agregar")
            return
        }


        selectedItems.forEach { item ->
            val cantidad = txtCantidad.text.toString().toIntOrNull() ?: 0
            val precio = String.format("%.3f", txtPrecio.text.toString().toBigDecimal().setScale(3))
            val subtotal = String.format("%.2f", cantidad.toBigDecimal() * precio.toBigDecimal())
            val descuento = String.format(
                "%.2f",
                txtDesc.text.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
            )
            val conDescto = if (descuento.toBigDecimal() > BigDecimal.ZERO) {
                subtotal.toBigDecimal().subtract(
                    subtotal.toBigDecimal()
                        .multiply(descuento.toBigDecimal())
                        .divide(BigDecimal("100"), 6, RoundingMode.HALF_UP)
                ).setScale(2, RoundingMode.HALF_UP)
            } else {
                subtotal.toBigDecimal().setScale(2, RoundingMode.HALF_UP)
            }

            val newItem = datosDet(
                item.referencia,
                cantidad.toString(),
                precio,
                subtotal,
                item.codigo,
                item.descripcion,
                item.unidadCE,
                descuento,
                conDescto.toString(),
                item.costoProm,
                String.format(
                    "%.2f",
                    (precio.toDouble() * (1 - descuento.toDouble() / 100)) / item.costoProm.toDouble()
                ),
                item.combo,
                item.it_regalo
            )

            val newItems = mutableListOf<datosDet>()

            itemEnEdicion?.let { indiceEdicion ->
                if (todosLosItemsDet.isNotEmpty() && indiceEdicion >= 0 && indiceEdicion < todosLosItemsDet.size) {
                    todosLosItemsDet[indiceEdicion] = newItem
                    itemEnEdicion = null // Restablecer después de editar
                }
            } ?: run {
                // Recolecta todos los nuevos ítems que podrían agregarse
                selectedItems.forEach { item ->
                    newItems.addAll(llenarControles.fnDetallesCombos(item.combo))
                }

                // Verifica si alguno de los nuevos ítems ya está en la lista
                if (newItems.any { fnVerificarDuplicadosCombo(it) }) {
                    showToast("El item ya ha sido agregado")
                    return
                }
                // Si no hay duplicados, procede a agregar todos los nuevos ítems
                todosLosItemsDet.addAll(newItems)
            }

        }

        // Actualizar el RecyclerView una vez después de agregar todos los ítems
        ReciclerviewDet.adapter = adaptadorDetalle
        ReciclerviewDet.layoutManager = LinearLayoutManager(requireContext())
        fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())
        txtCantidad.setText("")
        txtPrecio.setText("")
        txtReferencia.setText("")
        vliExito = 1

        fnGuardadoautomatico()
    }


    fun fnVerificarDuplicadosCombo(nuevoItem: datosDet, indiceExcluido: Int? = null): Boolean {
        return todosLosItemsDet.withIndex().any { (index, existingItem) ->
            // Excluir el índice actual en modo de edición y verificar duplicados
            index != indiceExcluido && existingItem.Codigo == nuevoItem.Codigo
        }
    }


    fun fnVerificarDuplicados(nuevoItem: datosDet, indiceExcluido: Int? = null): Boolean {
        return todosLosItemsDet.withIndex().any { (index, existingItem) ->
            // Si estamos editando, excluimos el ítem en la posición de edición
            if (index == indiceExcluido) {
                false
            } else {
                existingItem.Codigo == nuevoItem.Codigo
            }
        }
    }


    private fun fnConsultarProforma(codDocumento: Int) {

        var clCodigo: String =""
        var trCodigo: String = ""
        var descuento: String = ""
        var clienteNombre: String = ""

        btnGuardar.isEnabled = true
        llenarControles.fnConsultarProformas(codDocumento,
            actualizarCabecera = { cursor ->
                requireActivity().runOnUiThread {
                    try {
                        txtNumero.isEnabled = false
                        clCodigo = cursor.getString(cursor.getColumnIndexOrThrow("cl_codigo"))
                        trCodigo = cursor.getString(cursor.getColumnIndexOrThrow("tr_codigo"))
                        //txtNumero.text = cursor.getString(cursor.getColumnIndexOrThrow("pe_coddocumento"))
                        txtFecha.text = cursor.getString(cursor.getColumnIndexOrThrow("pr_fechaing"))
                        txtObservacion.text = cursor.getString(cursor.getColumnIndexOrThrow("pr_observacion"))
                        descuento = cursor.getString(cursor.getColumnIndexOrThrow("pr_porcdesc"))
                        vgsEstado = cursor.getString(cursor.getColumnIndexOrThrow("pr_estado"))
                        txtCIdentificacionPF.text = cursor.getString(cursor.getColumnIndexOrThrow("pr_cedula"))
                        lblseguroPF.text = "${cursor.getString(cursor.getColumnIndexOrThrow("pr_porcseguro"))}%"
                        clienteNombre = cursor.getString(cursor.getColumnIndexOrThrow("pr_nombre"))
                        cl_codigo = clCodigo
                        txtCliente.setText(clienteNombre)
                        txtCiudadViewPF.text = cursor.getString(cursor.getColumnIndexOrThrow("ci_descripcion"))
                        nivel = cursor.getString(cursor.getColumnIndexOrThrow("pr_nivel"))
                        factura = cursor.getString(cursor.getColumnIndexOrThrow("fa_coddocumento"))?:""


                        llenarControles.selectItemInSpinner(spinnerFormaPag, cursor.getString(cursor.getColumnIndexOrThrow("tp_codigo")))
                        //llenarControles.selectItemInSpinner(spinnerItem, cursor.getString(cursor.getColumnIndexOrThrow("td_codigo")))

                    } catch (e: Exception) {
                        Log.e("ConsultarPedido", "Error en la cabecera: ${e.localizedMessage}")
                    }
                }
            },
            actualizarDetalles = { detalles ->
                requireActivity().runOnUiThread {
                    //Log.d("ConsultarPedido", "Detalles recibidos: ${detalles.size}")
                    if (detalles.isNotEmpty()) {

                        adaptadorDetalle = MiAdapterDetalleProforma(todosLosItemsDet, this,
                            itemClickListener = this,
                            doubleClickListener = { item, position ->
                                // Cargar los valores del ítem seleccionado en los campos de entrada
                                txtCantidad.setText(item.Cantidad)
                                txtPrecio.setText(item.Precio)
                                txtDesc.setText(item.DescItem)
                                txtReferencia.setText(llenarControles.fnObtenerReferenciaPorCodigo(item.Codigo))
                                itemEnEdicion = position

                                nuevoItemSeleccionado = null

                                nuevoItemSeleccionado = datos(
                                    referencia = item.Referencia,
                                    stock = "0",
                                    precioSub = item.Precio,
                                    precioCont = item.Precio,
                                    precioCred = item.Precio,
                                    codigo = item.Codigo,
                                    descripcion = item.Descripcion,
                                    unidadCE = item.unidadCE,
                                    costoProm = item.costProm,
                                    combo = item.combo,
                                    cd_codigo = item.combo,
                                    it_regalo = item.regalo
                                )
                            })
                        adaptadorDetalle.callbacks = this
                        ReciclerviewDet.adapter = adaptadorDetalle
                        ReciclerviewDet.layoutManager = LinearLayoutManager(requireContext())
                        adaptadorDetalle.updateData(detalles)


                    } else {
                        Log.d("ConsultarPedido", "No se recibieron detalles para el documento: $codDocumento")
                    }
                }
                valInser = 0
            },
            onDocumentoNoEncontrado = {
                txtNumero.isEnabled = true
                requireActivity().runOnUiThread {
                    showToast("Documento no encontrado")
                    valInser = 1
                }
            }
        )

        if (valInser == 0){
            //obtener el cliente - transporte y politica
            val transporteNombre = llenarControles.obtenerNombreTransporte(trCodigo)
            tr_codigo = trCodigo
            txtTransporte.setText(transporteNombre)
            txtDesc.text = descuento

            val politica = llenarControles.fnObtenerPolitica(clCodigo)
            if (politica != null){
                pz_codigo = politica.codigo
            }
            fnActivarControles()
            spinnerItem.isEnabled = false
            //fnPolitica(cl_codigo.toInt())
        }
        fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())
        vliExito = 1
    }

    fun fnValidarYAgregarDetalles() {

        var combo ="0"
        var selectedItems = (ReciclerviewRef.adapter as? MiAdaptadorRefProf)?.getSelectedItems()

        if (selectedItems.isNullOrEmpty() || itemEnEdicion != null) {
            selectedItems = nuevoItemSeleccionado?.let { listOf(it) } ?: emptyList()
        }

        if (selectedItems != null) {

            selectedItems.forEach { item ->
                combo = item.cd_codigo
            }
        }


        // Validación común para todostodos los casos
        val camposNecesarios = listOf("cliente" to txtCliente, "transporte" to txtTransporte) +
                if (combo.toInt() == 0) listOf("cantidad" to txtCantidad, "precio" to txtPrecio) else emptyList()

        for ((nombre, campo) in camposNecesarios) {
            if (campo.text.toString().isBlank()) {
                showToast("Por favor, ingresa el $nombre.")
                return
            }
        }


        if (combo.toInt() != 0) {
            fnAgregarDetallesCombo()
        } else {
            fnAgregarDetalles()
            fnDesactivarCabecera()
        }

    }

    private fun fnMostrarDialogoDeConfirmacion() {
        when (vgsOpcionMenu) {
            "E" -> {
                mostrarDialogo("¿Desea eliminar los datos?") {
                    fnEnviarProforma()
                }
            }

            "I" -> {
                mostrarDialogo("¿Deseas guardar los datos?") {
                    fnEnviarProforma()
                }
            }

            "M" -> {
                mostrarDialogo("¿Deseas actualizar los datos?") {
                    fnEnviarProforma()
                }
            }
        }
    }

    private fun fnGuardarPolitica(){
        val nonNullLopd = lopd
        val nonNullCampania = campania
        if (nonNullLopd != null && nonNullCampania != null) {
            llenarControles.fnInsertarPolitica(nonNullLopd, nonNullCampania, usuario, cl_codigo.toInt())
        }
    }

    private fun mostrarDialogo(mensaje: String, accion: () -> Unit) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Sistema")
            setMessage(mensaje)
            setPositiveButton("Sí") { dialog, which ->
                accion()
                if (vliGuardar == 1 ){
                    fnDesactivarControles()

                    if (estado == "C")
                    {
                        // btnEliminar.isEnabled = true
                        btnImprimir.isEnabled = true
                    }else{
                        btnEliminar.isEnabled = true
                        btnImprimir.isEnabled = true
                    }


                }else{

                }
            }
            setNegativeButton("No") { dialog, which -> dialog.dismiss() }
            create().show()
        }
    }

    private fun fnTransporte(): String {
        var finalTarifa = "0"

        try {
            for (tarifa in DatosTarifa) {
                val peso = tarifa.peso ?: 0.0
                val t1 = tarifa.tarifa1 ?: 0.0
                val t2 = tarifa.tarifa2 ?: 0.0

                val tarifaP: Double

                finalTarifa = if (unidadCE >= 0.0 && unidadCE < peso) {
                    tarifaP = t1
                    t1.toString()
                } else {
                    tarifaP = t2
                    (unidadCE * t2).toString()
                }

                Tarifa = String.format("$%.2f", tarifaP)
                Cobertura = tarifa.descripcion.orEmpty()
            }

            return finalTarifa
        } catch (e: Exception) {
            e.printStackTrace()
            return "0"
        }
    }

    fun fnPesos() {
        Kilo.text = String.format("%.2f", unidadCE)
        tarifa.text = if (Tarifa.isNullOrEmpty()) "$0.00" else Tarifa
        cobertura.text = if (Cobertura.isNullOrEmpty()) "Cobertura" else Cobertura

    }

    fun fnPreciosStock(referencia: String, codigo: String) {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.frmprecios, null) // Usa el nombre de tu archivo XML

        val selectedItems = referencia
        if (selectedItems.isNullOrEmpty()) {
            showToast("No se han seleccionado elementos para agregar")
        } else {
            // Obtén el primer ítem seleccionado
            val primerItem = selectedItems.firstOrNull()
            val tituloDialogo = referencia ?:  "Detalles del Ítem"


            // Asigna valores de stock comenzando desde el segundo valor en el array de stock
            primerItem?.let {
                val stock = llenarControles.fnObtenerStockEnLinea(referencia, codigo)

                // Empieza a asignar desde el segundo valor (índice 1 en la lista)
                stock.drop(1).forEachIndexed { index, valor ->
                    val textViewId = resources.getIdentifier("valueItem${index + 1}", "id", requireActivity().packageName)
                    val textView = view.findViewById<TextView>(textViewId)
                    textView?.text = valor
                }
            }

            // Crear y mostrar el diálogo con el título modificado
            AlertDialog.Builder(requireContext())
                .setTitle(tituloDialogo)
                .setView(view)
                .setCancelable(true)
                .show()
        }
    }

    fun fnactualizarDescuento(
        descuentoTexto: String,
        recalcularTotales: Boolean = false
    ) {
        if (!::adaptadorDetalle.isInitialized) return

        val listaDetalle = adaptadorDetalle.datos

        if (listaDetalle.isEmpty()) return

        val descuento = descuentoTexto
            .trim()
            .replace(",", ".")
            .toBigDecimalOrNull()
            ?: BigDecimal.ZERO

        for (item in listaDetalle) {
            if (item.proceso == -1) continue

            val subtotal = item.Subtotal
                .replace(",", ".")
                .toBigDecimalOrNull()
                ?: BigDecimal.ZERO

            val precio = item.Precio
                .replace(",", ".")
                .toBigDecimalOrNull()
                ?: BigDecimal.ZERO

            val costoProm = item.costProm
                .replace(",", ".")
                .toBigDecimalOrNull()
                ?: BigDecimal.ZERO

            val regalo = item.regalo.toIntOrNull() ?: 0

            val descuentoFormateado = descuento
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString()

            item.DescItem = descuentoFormateado

            val valorDescuento = subtotal
                .multiply(descuento)
                .divide(BigDecimal("100"), 6, RoundingMode.HALF_UP)

            item.ConDesc = if (regalo > 0) {
                "0.00"
            } else if (descuento > BigDecimal.ZERO) {
                subtotal
                    .subtract(valorDescuento)
                    .setScale(2, RoundingMode.HALF_UP)
                    .toPlainString()
            } else {
                "0.00"
            }

            item.lote = if (costoProm > BigDecimal.ZERO) {
                val factor = BigDecimal.ONE.subtract(
                    descuento.divide(BigDecimal("100"), 6, RoundingMode.HALF_EVEN)
                )

                precio.multiply(factor)
                    .divide(costoProm, 6, RoundingMode.HALF_EVEN)
                    .setScale(2, RoundingMode.HALF_EVEN)
                    .toPlainString()
            } else {
                "0.00"
            }
        }

        adaptadorDetalle.notifyDataSetChanged()

        if (recalcularTotales && ::tr_codigo.isInitialized) {
            fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())
        }
    }

    private fun fnPolitica(cl_codigo: Int) {
        val lopdData = llenarControles.fnVerficarLopd(cl_codigo)

        if (lopdData == null) {
            // Crear el AlertDialog
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Confirmación de Políticas")

            // Inflar el layout personalizado
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.frm_politica, null)
            builder.setView(dialogView)

            // Referencias a los elementos del layout
            val rgPolitica = dialogView.findViewById<RadioGroup>(R.id.rgPolitica)
            val rgOfertas = dialogView.findViewById<RadioGroup>(R.id.rgOfertas)

            // Configurar el botón "Aceptar" antes de mostrar el diálogo
            builder.setPositiveButton("Aceptar", null)

            // Crear el diálogo
            val dialog = builder.create()

            // Deshabilitar la posibilidad de cerrar el diálogo con el botón de retroceso o tocando fuera del diálogo
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)

            // Mostrar el diálogo y trabajar con el botón "Aceptar"
            dialog.setOnShowListener {
                // Obtener el botón "Aceptar" y deshabilitarlo inicialmente
                val aceptarButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                aceptarButton.isEnabled = false // Deshabilitar el botón "Aceptar" al inicio

                // Función para habilitar el botón solo cuando ambos RadioGroups tengan una selección
                val enableAcceptButton = {
                    val politicaSelected = rgPolitica.checkedRadioButtonId != -1
                    val ofertasSelected = rgOfertas.checkedRadioButtonId != -1

                    // Habilitar el botón solo si ambas opciones están seleccionadas
                    aceptarButton.isEnabled = politicaSelected && ofertasSelected
                }

                // Listener para habilitar el botón al seleccionar una opción en rgPolitica
                rgPolitica.setOnCheckedChangeListener { _, _ ->
                    enableAcceptButton()
                }

                // Listener para habilitar el botón al seleccionar una opción en rgOfertas
                rgOfertas.setOnCheckedChangeListener { _, _ ->
                    enableAcceptButton()
                }

                // Configurar el comportamiento del botón "Aceptar" una vez esté habilitado
                aceptarButton.setOnClickListener {
                    val aceptaPolitica = if (rgPolitica.checkedRadioButtonId == R.id.rbSiPolitica) 1 else 0
                    val aceptaOfertas = if (rgOfertas.checkedRadioButtonId == R.id.rbSiOfertas) 1 else 0

                    // Guardar los valores seleccionados
                    lopd = aceptaPolitica
                    campania = aceptaOfertas

                    // Cerrar el diálogo
                    dialog.dismiss()
                }
            }

            // Mostrar el diálogo
            dialog.show()
        }
    }

    private lateinit var downloadButton: Button

    private fun fnShowImageDialog(codigo: String) {
        val imageDialogView = layoutInflater.inflate(R.layout.imagen, null)
        imageView = imageDialogView.findViewById(R.id.imageViewDialog)

        disableSSLVerification()
        val imageUrl = "https://app.cotzul.com/sitenet/digital/9/$codigo.png"

        val imageCache = ImageCache()
        val downloadTask = object : DownloadImageTask(imageView, imageCache,
            R.mipmap.ic_no_disponible_foreground
        ) {
            override fun onPostExecute(result: Bitmap?) {
                super.onPostExecute(result)
                downloadButton.isEnabled = result != null
            }
        }
        downloadTask.execute(imageUrl)

        // Initialize the scale gesture detector
        scaleGestureDetector = ScaleGestureDetector(requireContext(), ScaleListener())

        imageView.setOnTouchListener { v, event ->
            scaleGestureDetector.onTouchEvent(event)

            val action = event.actionMasked
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    last.set(event.x, event.y)
                    start.set(last)
                    activePointerId = event.getPointerId(0)
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - last.x
                    val dy = event.y - last.y
                    matrix.postTranslate(dx, dy)
                    imageView.imageMatrix = matrix
                    last.set(event.x, event.y)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    activePointerId = MotionEvent.INVALID_POINTER_ID
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    val pointerIndex = event.actionIndex
                    val pointerId = event.getPointerId(pointerIndex)
                    if (pointerId == activePointerId) {
                        val newPointerIndex = if (pointerIndex == 0) 1 else 0
                        lastTouchX = event.getX(newPointerIndex)
                        lastTouchY = event.getY(newPointerIndex)
                        activePointerId = event.getPointerId(newPointerIndex)
                    }
                }
            }

            true
        }

        val dialog = AlertDialog.Builder(requireContext()).apply {
            setView(imageDialogView)
            setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
            setNegativeButton("Descargar", null)
        }.create()

        dialog.setOnShowListener {
            downloadButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            downloadButton.isEnabled = false
            downloadButton.setOnClickListener {
                saveImageFromImageView(codigo)
            }
        }

        dialog.show()
    }

    private fun saveImageFromImageView(codigo: String) {
        Thread {
            try {
                val drawable = imageView.drawable
                if (drawable != null && drawable is BitmapDrawable) {
                    val bitmap = drawable.bitmap
                    val fechaActual = SimpleDateFormat("_ddMMyyyy_HHmmss").format(Date())
                    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "$codigo" + "$fechaActual" +".png")
                    val outputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Imagen guardada correctamente", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Fallo al guardar la imagen", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Log.e("ImageDowlonad", "$e")
                    Toast.makeText(requireContext(), "Fallo al guardar la imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            val scaleFactor = scaleGestureDetector.scaleFactor
            val newScale = scaleFactor * currentMatrix.scaleX()
            if (newScale in minScale..maxScale) {
                matrix.postScale(scaleFactor, scaleFactor, scaleGestureDetector.focusX, scaleGestureDetector.focusY)
                currentMatrix.set(matrix)
                imageView.imageMatrix = matrix
            }
            return true
        }
    }

    private fun Matrix.scaleX(): Float {
        getValues(matrixValues)
        return matrixValues[Matrix.MSCALE_X]
    }

    private fun Matrix.scaleY(): Float {
        getValues(matrixValues)
        return matrixValues[Matrix.MSCALE_Y]
    }

    fun disableSSLVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return arrayOf()
                }
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onItemClick(item: datosDet, position: Int) {
        posicion = position
    }


    // Manejo del resultado de la selección
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            val selectedImage: Uri? = data?.data
            if (selectedImage != null) {
                val bitmap = getBitmapFromUri(selectedImage)
                pdfBase64 = createPdfFromImageAndEncodeBase64(bitmap, selectedImage)

                if (pdfBase64 != null) {
                    //Log.d("Base64PDF", "PDF en Base64: $pdfBase64")

                    // Cambiar el fondo de txtOrden cuando la conversión sea exitosa
                    txtOrden.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.orden
                        )
                    )
                }
            }
        }
    }

    // Método para obtener Bitmap desde un Uri
    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return requireActivity().contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }

    // Método para convertir un Bitmap a Base64
    fun createPdfFromImageAndEncodeBase64(imageBitmap: Bitmap?, imageUri: Uri?): String? {
        if (imageBitmap == null) {
            return null // Si no hay imagen, no hacemos nada
        }

        val outputStream = ByteArrayOutputStream()
        val pdfWriter = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)

        pdfWriter.setCompressionLevel(9) // Nivel de compresión máximo

        // Tamaño máximo de la celda para la imagen
        val maxWidth = 500f
        val maxHeight = 700f

        // Ajustar la orientación del Bitmap
        val bitmap = getCorrectlyOrientedBitmap(imageBitmap, imageUri)

        // Convertir el Bitmap en una imagen para el PDF
        val imageData = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, imageData) // Comprimir a calidad 80%
        val image = Image(ImageDataFactory.create(imageData.toByteArray()))

        // Escalar la imagen al tamaño máximo permitido
        val aspectRatio = image.imageWidth / image.imageHeight.toFloat()
        if (aspectRatio > 1) { // Imagen horizontal
            image.scaleToFit(maxWidth, maxHeight)
        } else { // Imagen vertical
            image.scaleToFit(maxWidth, maxHeight)
        }

        // Centrar la imagen
        image.setHorizontalAlignment(HorizontalAlignment.CENTER)

        // Agregar la imagen al documento
        document.add(image)

        document.close()

        // Convertir el PDF a Base64
        val pdfBytes = outputStream.toByteArray()
        return Base64.encodeToString(pdfBytes, Base64.DEFAULT)
    }


    fun getCorrectlyOrientedBitmap(bitmap: Bitmap, imageUri: Uri?): Bitmap {
        val inputStream = imageUri?.let { requireActivity().contentResolver.openInputStream(it) }
        val exif = inputStream?.let { ExifInterface(it) }
        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun fnCliente() {
        val dialog = consultaCliente()
        dialog.setTargetFragment(this, 0) // Configura el Fragment receptor directamente
        dialog.show(parentFragmentManager, "consultaCliente")
    }

    override fun onItemsSelected(clientes: AdaptadorClientes.Clientes) {
        clienteSeleccionado = clientes

        cl_codigo = clientes.codigo

        val nombreCliente = when {
            clientes.nombre.isNotBlank() -> clientes.nombre
            clientes.razonComercial.isNotBlank() -> clientes.razonComercial
            else -> clientes.en_razonsocial
        }

        txtCliente.text = nombreCliente
        Nombre = txtCliente.text.toString()

        txtCIdentificacionPF.text = clientes.en_identificacion

        DireccionDA = clientes.direccion
        TelefonoDA = clientes.fono
        CiudadDA = clientes.ciudad
        EmailDA = clientes.en_correo

        txtCiudadViewPF.text = clientes.ciudad

        nivel = clientes.cc_nivelprecio

        if (::txtNivelPF.isInitialized) {
            txtNivelPF.text = nivel
        }

        val codigoTransporte = if (
            clientes.pr_descripcion.uppercase(Locale.ROOT) != "GUAYAS"
        ) {
            "12"
        } else {
            "0"
        }

        val transporte = llenarControles.fnCargarDatosTransporteCliente(codigoTransporte)

        if (transporte != null) {
            txtTransporte.setText(transporte.nombre, false)
            tr_codigo = transporte.codigo
        } else {
            tr_codigo = codigoTransporte
            txtTransporte.setText("", false)
        }

        if (tr_codigo == "12") {
            ejecutarPesoTarifaSeguro()
        } else {
            DatosTarifa = emptyList()
            Tarifa = ""
            Cobertura = ""
            txtFlete.text = "0.00"
            Kilo.text = String.format(Locale.US, "%.2f", unidadCE)
            tarifa.text = "$0.00"
            cobertura.text = "Cobertura"

            if (::adaptadorDetalle.isInitialized) {
                fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())
            }
        }

        fnDatosAdicionales()
    }

    private fun fnDatosAdicionales() {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.frm_datos_adicionales, null)

        val txtNombreDA = view.findViewById<EditText>(R.id.txtNombreDA)
        val txtDireccion = view.findViewById<EditText>(R.id.txtDireccionDA)
        val txtTelefono = view.findViewById<EditText>(R.id.txtTelefonoDA)
        val txtCiudad = view.findViewById<EditText>(R.id.txtCiudadDA)
        val txtEmail = view.findViewById<EditText>(R.id.txtEmailDA)
        val txtContacto = view.findViewById<EditText>(R.id.txtContactoDA)

        if (cl_codigo == "1774") {
            if (Nombre == "SR .") {
                txtNombreDA.setText("")
            } else {
                txtNombreDA.setText(Nombre)
            }

            txtNombreDA.isEnabled = true
        } else {
            txtNombreDA.setText(Nombre)
            txtNombreDA.isEnabled = false
        }

        txtDireccion.setText(DireccionDA)
        txtTelefono.setText(TelefonoDA)
        txtCiudad.setText(CiudadDA)
        txtEmail.setText(EmailDA)
        txtContacto.setText(ContactoDA)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Datos adicionales")
            .setView(view)
            .setPositiveButton("Aceptar", null)
            .setCancelable(false)
            .create()

        dialog.setCanceledOnTouchOutside(false)

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nombre = txtNombreDA.text.toString()
                val telefono = txtTelefono.text.toString()
                val email = txtEmail.text.toString()

                if (nombre.isBlank()) {
                    txtNombreDA.error = "Ingrese el nombre del cliente"
                    txtNombreDA.requestFocus()
                    return@setOnClickListener
                }

                if (!fnValidarTelefonos(telefono)) {
                    txtTelefono.error = "Ingrese un telefono valido"
                    txtTelefono.requestFocus()
                    return@setOnClickListener
                }

                if (!fnValidarCorreo(email)) {
                    txtEmail.error = "Ingrese el correo electrónico valido"
                    txtEmail.requestFocus()
                    return@setOnClickListener
                }

                DireccionDA = txtDireccion.text.toString()
                TelefonoDA = telefono
                CiudadDA = txtCiudad.text.toString()
                EmailDA = email
                ContactoDA = txtContacto.text.toString()
                Nombre = nombre

                txtCliente.text = Nombre

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun fnPesoTarifaA(){
        MiAsyncTaskTarifa().execute()
    }

    private inner class MiAsyncTaskTarifa : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg voids: Void): String? {
            return try {
                val cadena = "$cl_codigo,$tr_codigo"
                solicitudSoap.initializeVariables(getString(R.string.str_Tarifa).toInt(), cadena)
                val result = solicitudSoap.realizarSolicitudSoap()
                    ?.bufferedReader()
                    ?.use { it.readText() }

                if (!result.isNullOrBlank()) {
                    XmlTarifa = result
                }

                null
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            DatosTarifa = fnParseTarifas(XmlTarifa)
            fnTransporte()
            fnPesos()
            fnSeguroCliente()
        }
    }

    private inner class MiAsyncTaskSeguro() :
        AsyncTask<Void, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {
            var pedido: String? = null
            try {

                val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                val cadena = "$cl_codigo,$tr_codigo,'$fechaActual'"

                solicitudSoap.initializeVariables(getString(R.string.str_Seguros).toInt(), cadena)

                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.bufferedReader()?.use { it.readText() }

                if (!result.isNullOrBlank()) {
                    XmlSeguro = result
                }
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
            }
            return pedido
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val seguro = fnParseSeguro(XmlSeguro)
            vgDseguro = seguro ?: 0.0
            lblseguroPF.text = if (seguro != null) {
                "${seguro}%"
            } else {
                "0%"
            }
        }
    }

    fun fnParseSeguro(xml: String): Double? {
        return try {
            val factory = XmlPullParserFactory.newInstance().apply { isNamespaceAware = true }
            val parser = factory.newPullParser().apply { setInput(StringReader(xml)) }

            var event = parser.eventType
            var resultado: Double? = null

            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "Column1") {
                    val text = parser.nextText().trim()
                    resultado = text.toDoubleOrNull()
                    break // Solo el primero
                }
                event = parser.next()
            }
            resultado
        } catch (e: Exception) {
            Log.e("XML", "Error parseando Column1", e)
            null
        }
    }

    private fun fnFormaPago() {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.frm_forma_pago, null)
        val spnNombreFP = view.findViewById<Spinner>(R.id.spnNombreFP)
        val spnTipoFP   = view.findViewById<Spinner>(R.id.spnTipoFP)

        //  Llenar spnNombreFP desde base de datos
        llenarControles.fnLLenarSpinner(spnNombreFP, "fa_ws_tipoTarjeta")

        //  Llenar spnTipoFP manualmente
        val listaTipos = listOf("Ninguno", "Corriente", "Diferido")
        val adapterTipo = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaTipos).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spnTipoFP.adapter = adapterTipo

        //  Crear AlertDialog sin botones
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Forma de Pago")
            .setView(view)
            .create()

        // Flags y estado
        var tipoInicial = true
        var ttCodigoSeleccionado: Int = 0


        spnNombreFP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = parent?.getItemAtPosition(position) as SpinnerItem
                ttCodigoSeleccionado = item.codigo.toInt()
                if (ttCodigoSeleccionado != 0)
                {
                    txtFormaPagoPF.setText("${txtFormaPagoPF.text} - ${item.descripcion}")
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 🔹 Segundo spinner: usa el tt_codigo guardado para obtener comisión
        spnTipoFP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (tipoInicial) { tipoInicial = false; return }

                val tipoLargo = parent?.getItemAtPosition(position)?.toString().orEmpty()

                val tipoCorto = when (tipoLargo) {
                    "Corriente" -> "C"
                    "Diferido"  -> "D"
                    else        -> null
                }

                // No cierres si escogieron "Ninguno"
                if (tipoCorto == null) return

                if (ttCodigoSeleccionado != 0) {
                    val lista = llenarControles.fnFactorComision()
                    val fila = lista.firstOrNull {
                        it.ttCodigo == ttCodigoSeleccionado && it.ttTipo.equals(tipoCorto, ignoreCase = true)
                    }

                    if (fila != null) {
                        // Usa los tipos que tengas en tu modelo
                        factor = fila.tcComision
                        codFactor = fila.tcCodigo

                        txtFormaPagoPF.setText("${txtFormaPagoPF.text} - ${factor}%")
                    } else {
                        factor = 0.00
                        codFactor = 0.00
                    }
                }

                dialog.dismiss() // Cierra solo en selección válida
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        dialog.show()
    }


    private inner class MiAsyncTaskItems(private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase

        override fun onPreExecute() {
            super.onPreExecute()
        }
        override fun doInBackground(vararg voids: Void): String? {
            database = dbHelper.writableDatabase

            database.execSQL("DELETE FROM ve_ws_itemTmp")

            val item = spinnerBodega.selectedItem as? SpinnerItem
            val bodega = item?.codigo
            val descripcionSel = item?.descripcion
            val referencia = txtReferencia.text.toString().trim()


            val cadena = "2,$bodega,3,'$referencia'"
            var pedido: String = ""

            try {
                solicitudSoap.initializeVariables(getString(R.string.str_id).toInt(), cadena)

                // Realizar la solicitud SOAP
                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.bufferedReader()?.use { it.readText() }

                if (!result.isNullOrBlank()) {
                    // Procesar el resultado si no está en blanco
                    pedido = XmlParserItemTemp.parseItemTemp(
                        result,
                        database,
                        requireContext()
                    )
                }
            } catch (e: Exception) {
                // Manejo de errores
                Log.e("SolicitudSOAP", "Error al procesar la solicitud: ${e.message}", e)
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            return pedido
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()

            val referencia = txtReferencia.text.toString().trim()
            val item = spinnerBodega.selectedItem as? SpinnerItem

            val bodega = item?.codigo ?: "0"
            val resultados = llenarControles.fnBuscaReferenciaYcombosEnlinea(referencia, bodega)

            for (dato in resultados) {
                datosList.add(dato)
            }
            // Configura el RecyclerView y asigna el adaptador
            val layoutManager = LinearLayoutManager(requireContext())
            ReciclerviewRef.layoutManager = layoutManager
            ReciclerviewRef.adapter = adaptadorRef

        }
    }


    private inner class MiAsyncTaskEnviarProforma(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String?>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                val cadena = XmlProforma.fnProformaXML(
                    "2",
                    bodega.orEmpty(),
                    txtNumero.text.toString(),
                    fnFecha(),
                    usuario,
                    ep_codigo.toString(),
                    cl_codigo.toString(),
                    txtCIdentificacionPF.text.toString(),
                    txtCliente.text.toString(),
                    DireccionDA,
                    TelefonoDA,
                    tr_codigo.toString(),
                    tp_codigo.toString(),
                    "N",
                    txtObservacion.text.toString(),
                    txtSub.text.toString(),
                    txtDescuentoT.text.toString(),
                    porcdescuento.toString(),
                    txtSeguro.text.toString(),
                    lblseguroPF.text.toString().replace("%", ""),
                    txtFlete.text.toString(),
                    txtIva.text.toString(),
                    txtTotal.text.toString(),
                    EmailDA,
                    vgsOpcionMenu,
                    if (vgsOpcionMenu == "E") "2" else "1",
                    adaptadorDetalle.datos
                )

                solicitudSoap.initializeVariables(
                    getString(R.string.str_Proforma).toInt(),
                    cadena
                )

                solicitudSoap.realizarSolicitudSoap()
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }

            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error al enviar pedido: ${e.message}"
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            progressDialog.dismiss()

            if (result != null) {
                if (codFactor != 0.0 && vgsOpcionMenu == "I") {
                    MiAsyncTaskTipoTarjeta(
                        progressDialog,
                        result
                    ).execute()

                    return
                }

                showResultDialog(result)

                if (
                    txtCiudadViewPF.text.toString() == "GUAYAQUIL" &&
                    pr_estadotransporte == "N"
                ) {
                    btnTransportePF.isEnabled = true
                }

                return
            }

            Toast.makeText(
                requireContext(),
                errorMessage,
                Toast.LENGTH_LONG
            ).show()
        }

        private fun showResultDialog(result: String) {
            val accion = when (vgsOpcionMenu) {
                "E" -> "Eliminados"
                "I" -> "Guardados"
                "M" -> "Actualizados"
                else -> "SIN ACCIÓN"
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Sistema")
                .setMessage("Datos $accion Correctamente")
                .setPositiveButton("OK") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()

            fnDesactivarControles()

            if (vgsOpcionMenu != "E") {
                btnImprimir.isEnabled = true
                btnProcesarPF.isEnabled = true
            }

            fnLimpiarFoco()
        }
    }


    private inner class MiAsyncTaskTipoTarjeta(
        private val progressDialog: ProgressDialog,
        private val result: String?
    ) : AsyncTask<Void, Void, String?>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                val cadena = "2,$bodega,${txtNumero.text},$codFactor,1"

                solicitudSoap.initializeVariables(
                    getString(R.string.str_TipoTarjeta).toInt(),
                    cadena
                )

                solicitudSoap.realizarSolicitudSoap()
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }

            } catch (e: Exception) {
                Log.e("frmProformaA", "Error tipo tarjeta: ${e.message}", e)
                errorMessage = "Error al enviar pedido: ${e.message}"
                null
            }
        }

        override fun onPostExecute(respuesta: String?) {
            super.onPostExecute(respuesta)

            try {
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
            } catch (_: Exception) {
            }

            if (!isAdded) return

            if (result != null) {
                showResultDialog(result)
            } else {
                Toast.makeText(
                    requireContext(),
                    errorMessage.ifBlank { "Error al enviar pedido" },
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        private fun showResultDialog(pedido: String) {
            AlertDialog.Builder(requireContext())
                .setTitle("Sistema")
                .setMessage("Datos Guardados Correctamente")
                .setPositiveButton("OK") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()

            fnDesactivarControles()

            btnImprimir.isEnabled = true

            if (bodega != "1") {
                btnProcesarPF.isEnabled = true
            }
        }
    }


    private fun fnVerificastock(){
        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("¿Desea Enviar Factura?")
            .setPositiveButton("Sí") { dialog, _ ->
                try {
                    XmlFactura = XmlFactura(requireContext())
                    val progressDialog = showProgressDialogStock()
                    MiAsyncTaskVerificaStock(progressDialog).execute()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }


    private inner class MiAsyncTaskVerificaStock(private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {
            return try {
                val cadena = XmlFactura.obtenerFactura(
                    2,
                    bodega.toIntOrNull() ?: 0,
                    txtNumero.text.toString().toInt(),
                    usuario,
                    2
                )

                solicitudSoap.initializeVariables(getString(R.string.str_verificaStock).toInt(), cadena)

                val result = solicitudSoap.realizarSolicitudSoap()
                    ?.bufferedReader()
                    ?.use { it.readText() }

                if (result.isNullOrBlank()) {
                    ""
                } else {
                    XmlParserStock.buildAlertTextFromResponse(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error al enviar pedido: ${e.message}"
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()

            if (result != null && result != "null" && result.isNotBlank()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Item con stock faltante")
                    .setMessage("$result\n\n\n\n¿Desea procesar la factura?")
                    .setPositiveButton("Sí") { dialogInterface, _ ->
                        fnEnviarFactura()
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
                    .setCancelable(false)
                    .show()
            } else {
                fnEnviarFactura()
            }
        }
    }


    private inner class MiAsyncTaskRestringirFacturaCliente(private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg voids: Void): String? {
            return try {
                val cadena = "2,$cl_codigo,'','','',4"
                solicitudSoap.initializeVariables(getString(R.string.str_ClienteRestringido).toInt(), cadena)
                solicitudSoap.realizarSolicitudSoap()
                    ?.bufferedReader()
                    ?.use { it.readText() }
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val restringido = fnObtenerValorXml(result,1)

            if (restringido == "1") {
                progressDialog.dismiss()
                AlertDialog.Builder(requireContext())
                    .setTitle("Sistema")
                    .setMessage("Cliente restringido en la facturacion por Gerencia")
                    .setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.dismiss() }
                    .show()
                return
            }

            MiAsyncTaskProcesarPedido(progressDialog).execute()
        }
    }

    private inner class MiAsyncTaskProcesarPedido(private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg voids: Void): String? {
            return try {
                val cadena = XmlProforma.fnProformaPedidoXML(
                    "2",
                    bodega.ifBlank { "0" },
                    txtNumero.text.toString(),
                    fnFecha(),
                    usuario,
                    ep_codigo,
                    cl_codigo,
                    tr_codigo,
                    tp_codigo,
                    txtSub.text.toString(),
                    txtDescuentoT.text.toString(),
                    porcdescuento.toString(),
                    txtSeguro.text.toString(),
                    txtFlete.text.toString(),
                    txtIva.text.toString(),
                    txtTotal.text.toString(),
                    vgsOpcionMenu,
                    adaptadorDetalle.datos
                )

                solicitudSoap.initializeVariables(getString(R.string.str_FaProformaPedido).toInt(), cadena)
                solicitudSoap.realizarSolicitudSoap()
                    ?.bufferedReader()
                    ?.use { it.readText() }
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()

            val proforma = fnObtenerValorXml(result,1).orEmpty()
            AlertDialog.Builder(requireContext())
                .setTitle("Sistema")
                .setMessage("Peddido # $proforma")
                .setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.dismiss() }
                .show()
        }
    }

    fun fnEnviarFactura(){
        XmlFactura = XmlFactura(requireContext())
        val progressDialog = showProgressDialogFactura()
        MiAsyncTaskFactura(progressDialog).execute()
    }

    fun fnActivarCabecera(){
        spinnerBodega.isEnabled = true
        btnClientePF.isEnabled = true
        spinnerFormaPag.isEnabled = true
        btnBuscar.isEnabled = true
    }

    fun fnDesactivarCabecera(){
        spinnerBodega.isEnabled = false
        btnClientePF.isEnabled = false
        spinnerFormaPag.isEnabled = false
        btnBuscar.isEnabled = false
    }


    private fun fnConsultarProformaEnLinea(codigo: Int) {
        val context = requireContext()

        if (!fnIsNetworkAvailable(context)) {
            Toast.makeText(context, "Verifique su conexión a internet", Toast.LENGTH_LONG).show()
            return
        }

        if (codigo <= 0) {
            showToast("Número de proforma inválido")
            return
        }

        solicitudSoap = SolicitudSoap(context)

        val fecha = fnFecha()
        val cadena = "2,$bodega,$codigo,1,0,0,0,0,0,0,0,0,0,'$fecha',0,'$usuario'"
        val id = getString(R.string.str_FaProforma).toInt()
        val progressDialog = showProgressDialogItems()

        clsObtenerDatos(
            context = context,
            solicitudSoap = solicitudSoap,
            id = id,
            cadena = cadena,
            onSuccess = { xml ->
                dismissProgress(progressDialog)

                if (isAdded) {
                    procesarProformaRecibida(xml.orEmpty())
                }
            },
            onError = { ex ->
                dismissProgress(progressDialog)

                Log.e("frmProformaA", "Error consultando proforma: ${ex.message}", ex)

                if (isAdded) {
                    showToast("No se recibió información de la proforma")
                }
            }
        ).execute()
    }

    private fun procesarProformaRecibida(xml: String) {
        if (xml.isBlank() || xml == "null") {
            return
        }

        cargandoProforma = true

        try {
            vgsOpcionMenu = "M"

            fnCargarProforma(xml)

            if (::adaptadorDetalle.isInitialized) {
                fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())
            }

            fnControlSpinners()

            if (vgsEstado == "C") {
                fnDesactivarControles()
                btnImprimir.isEnabled = true
                btnProcesarPF.isEnabled = true
            }

            if (factura.isNotBlank()) {
                btnProcesarPF.isEnabled = false
            }

            txtReferencia.requestFocus()
        } finally {
            cargandoProforma = false

            txtTransporte.clearFocus()
            txtTransporte.dismissDropDown()
            view?.requestFocus()
        }
    }

    private fun dismissProgress(progressDialog: ProgressDialog) {
        try {
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        } catch (e: Exception) {
            Log.w("frmProformaA", "No se pudo cerrar el ProgressDialog: ${e.message}", e)
        }
    }

    private fun fnCargarProforma(xml: String) {
        if (xml.isBlank() || xml == "null") {
            showToast("No se recibió información de la proforma")
            return
        }

        val cabecera = fnParseXmlCabeceraProforma(xml)
        val detalles = fnParseXmlDetalleProforma(xml)

        val clCodigo = cabecera["cl_codigo"].orEmpty()
        val trCodigo = cabecera["tr_codigo"].orEmpty()
        val descuento = cabecera["pr_porcdesc"].orEmpty()

        cl_codigo = clCodigo
        tr_codigo = trCodigo

        txtNumero.isEnabled = false

        txtFecha.text = fnFechaNormalizada(cabecera["pr_fechaing"])
        txtObservacion.text = cabecera["observacion"].orEmpty()

        vgsEstado = cabecera["pr_estado"].orEmpty()

        txtCIdentificacionPF.text = cabecera["pr_cedula"].orEmpty()
        lblseguroPF.text = "${cabecera["pr_porcseguro"].orEmpty()}%"

        val clienteNombre = cabecera["pr_nombre"].orEmpty()
        txtCliente.text = clienteNombre
        cl_codigo = clCodigo

        txtVendedor.text = cabecera["pr_usuarioing"].orEmpty()
        usuario = cabecera["pr_usuarioing"].orEmpty()
        ep_codigo = cabecera["ep_codigo"].orEmpty()

        txtCiudadViewPF.text = cabecera["ci_descripcion"].orEmpty()

        nivel = cabecera["pr_nivel"].orEmpty()
        factura = cabecera["fa_coddocumento"].orEmpty()
        pr_estadotransporte = cabecera["pr_estadotransporte"].orEmpty()

        txtNivelPF.text = nivel

        cabecera["bo_codigo"]?.let { boCodigo ->
            runCatching {
                llenarControles.selectItemInSpinner(spinnerBodega, boCodigo)
            }
        }

        tp_codigo = cabecera["tp_codigo"].orEmpty()

        cabecera["tp_codigo"]?.let { tpCodigo ->
            runCatching {
                llenarControles.selectItemInSpinner(spinnerFormaPag, tpCodigo)
            }
        }

        codFactor = cabecera["tc_codigo"]?.toDoubleOrNull() ?: 0.0
        vgDseguro = cabecera["pr_porcseguro"]?.toDoubleOrNull() ?: 0.0

        DireccionDA = cabecera["direccion"].orEmpty()
        TelefonoDA = cabecera["pr_fono"].orEmpty()
        CiudadDA = cabecera["ci_descripcion"].orEmpty()
        EmailDA = cabecera["pr_email"].orEmpty()
        ContactoDA = cabecera["pr_contacto"].orEmpty()
        Nombre = cabecera["pr_nombre"].orEmpty()

        txtTransporte.setText(
            llenarControles.obtenerNombreTransporte(trCodigo),
            false
        )

        txtDesc.setText(descuento)

        adaptadorDetalle.callbacks = this
        ReciclerviewDet.adapter = adaptadorDetalle
        ReciclerviewDet.layoutManager = LinearLayoutManager(requireContext())
        adaptadorDetalle.updateData(detalles)

        fnActivarControles()

        spinnerBodega.isEnabled = false
        btnEliminar.isEnabled = true

        if (pr_estadotransporte == "S") {
            txtTransporte.isEnabled = true
        }
    }

    // ===== Recuperado/actualizado desde APK actual =====

    fun fnProformas() {
        val dialog = consultaProforma(bodega.ifBlank { "0" })
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "consultaProforma")
    }

    override fun onItemsSelected(codigo: Proformas) {
        val numero = codigo.numero

        txtNumero.text = numero.toString()
        btnBuscar.isEnabled = false

        fnConsultarProformaEnLinea(numero)
    }

    private fun fnEnviarProforma() {
        try {
            XmlProforma = XmlProforma(requireContext())
            val progressDialog = showProgressDialog()
            MiAsyncTaskEnviarProforma(progressDialog).execute()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("frmProformaA", "Error en fnEnviarProforma: ${e.message}", e)
        }
    }

    private fun fnObtenerSecuencia() {
        val context = requireContext()
        solicitudSoap = SolicitudSoap(context)

        val progressDialog = showProgressDialogItems()

        val id = getString(R.string.str_SecProforma).toInt()
        val cadena = "2,$bodega,$ep_codigo,1,2"

        clsObtenerDatos(
            context = context,
            solicitudSoap = solicitudSoap,
            id = id,
            cadena = cadena,
            onSuccess = { xml ->
                dismissProgress(progressDialog)

                if (!isAdded) return@clsObtenerDatos

                pe_coddocumento = fnObtenerValorXml(xml,1).orEmpty()

                if (pe_coddocumento.isNotBlank() && pe_coddocumento != "null") {
                    txtNumero.text = pe_coddocumento
                } else {
                    fnDesactivarControles()
                    showToast("No se pudo obtener la secuencia")
                }
            },
            onError = { ex ->
                dismissProgress(progressDialog)

                Log.e("SecProforma", "Error obteniendo secuencia: ${ex.message}", ex)

                if (isAdded) {
                    fnDesactivarControles()
                }
            }
        ).execute()
    }


    fun fnSeguroCliente() {
        fnPesoTarifaA()
    }

    fun fnCambioTransporte() {
        fnPesoTarifaA()
        if (adaptadorDetalle.itemCount > 0) {
            fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())
        }
    }

    private fun fnFechaNormalizada(raw: String?): String {
        val value = raw.orEmpty().trim()

        if (value.isBlank()) return fnFechaCorta()

        val outFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val formatosEntrada = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy",
            "MM/dd/yyyy HH:mm:ss",
            "MM/dd/yyyy"
        )

        for (formato in formatosEntrada) {
            val fecha = runCatching {
                SimpleDateFormat(formato, Locale.getDefault()).parse(value)
            }.getOrNull()

            if (fecha != null) {
                return outFormat.format(fecha)
            }
        }

        return value
    }

    fun fnValidarTelefonos(input: String): Boolean {
        val limpio = input.filter { it.isDigit() }
        if (limpio.length < 7) return false
        if (fnTodosIguales(limpio)) return false
        if (fnEsSecuenciaAscendente(limpio)) return false
        if (fnEsSecuenciaDescendente(limpio)) return false
        if (fnMaxRunIguales(limpio) >= 5) return false
        return true
    }

    private fun fnTodosIguales(s: String): Boolean = s.isNotEmpty() && s.all { it == s.first() }

    private fun fnMaxRunIguales(s: String): Int {
        if (s.isEmpty()) return 0
        var max = 1
        var run = 1
        for (i in 1 until s.length) {
            if (s[i] == s[i - 1]) run++ else run = 1
            if (run > max) max = run
        }
        return max
    }

    private fun fnEsSecuenciaAscendente(s: String): Boolean {
        if (s.length < 4) return false
        return s.zipWithNext().all { (a, b) -> b == a + 1 }
    }

    private fun fnEsSecuenciaDescendente(s: String): Boolean {
        if (s.length < 4) return false
        return s.zipWithNext().all { (a, b) -> b == a - 1 }
    }

    fun fnValidarCorreo(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun fnObtenerValorXml(xmlResult: String?, posicion: Int): String? {
        if (xmlResult.isNullOrBlank()) return null
        if (posicion <= 0) return null

        return try {
            val factory = XmlPullParserFactory.newInstance().apply {
                isNamespaceAware = true
            }

            val parser = factory.newPullParser().apply {
                setInput(StringReader(xmlResult))
            }

            var event = parser.eventType
            var contador = 0

            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.TEXT) {
                    val value = parser.text?.trim()

                    if (!value.isNullOrBlank()) {
                        contador++

                        if (contador == posicion) {
                            return value
                        }
                    }
                }

                event = parser.next()
            }

            null
        } catch (e: Exception) {
            Log.e("XML", "Error leyendo valor XML posición $posicion", e)
            null
        }
    }

    fun fnParseTarifas(xml: String): List<Tarifa> {
        val lista = mutableListOf<Tarifa>()
        if (xml.isBlank()) return lista
        try {
            val factory = XmlPullParserFactory.newInstance().apply { isNamespaceAware = true }
            val parser = factory.newPullParser().apply { setInput(StringReader(xml)) }
            var event = parser.eventType
            var peso = 0.0
            var tarifa1 = 0.0
            var tarifa2 = 0.0
            var descripcion = ""
            var current = ""
            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> current = parser.name ?: ""
                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim().orEmpty()
                        when (current.lowercase()) {
                            "peso", "tt_peso" -> peso = text.replace(",", ".").toDoubleOrNull() ?: peso
                            "tarifa1", "tt_tarifa1" -> tarifa1 = text.replace(",", ".").toDoubleOrNull() ?: tarifa1
                            "tarifa2", "tt_tarifa2" -> tarifa2 = text.replace(",", ".").toDoubleOrNull() ?: tarifa2
                            "descripcion", "tt_descripcion" -> descripcion = text
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name.equals("Table", ignoreCase = true)) {
                            lista.add(Tarifa(peso, tarifa1, tarifa2, descripcion))
                            peso = 0.0; tarifa1 = 0.0; tarifa2 = 0.0; descripcion = ""
                        }
                        current = ""
                    }
                }
                event = parser.next()
            }
        } catch (e: Exception) {
            Log.e("XML", "Error parseando tarifas", e)
        }
        return lista
    }

    fun fnParseXmlCabeceraProforma(xmlString: String): Map<String, String> {
        val salida = linkedMapOf<String, String>()
        if (xmlString.isBlank()) return salida

        fun putIfMissing(key: String, value: String?) {
            if (!salida.containsKey(key) && !value.isNullOrBlank()) {
                salida[key] = value
            }
        }

        return try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false

            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlString))

            val recordTags = setOf("Table")
            var eventType = parser.eventType
            var currentField = ""
            var insideRecord = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tag = parser.name.orEmpty()

                        if (recordTags.contains(tag)) {
                            insideRecord = true
                            salida.clear()
                        } else if (insideRecord) {
                            currentField = tag
                        }
                    }

                    XmlPullParser.TEXT -> {
                        if (insideRecord && currentField.isNotBlank()) {
                            val text = parser.text?.trim().orEmpty()
                            if (text.isNotBlank()) {
                                salida[currentField] = text
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        val tag = parser.name.orEmpty()

                        if (recordTags.contains(tag) && insideRecord) {
                            putIfMissing("pr_nombre", salida["cliente"])
                            putIfMissing("ci_descripcion", salida["ciudad"])
                            putIfMissing("pr_email", salida["correo"])
                            putIfMissing("direccion", salida["direccion"])
                            putIfMissing("pr_usuarioing", salida["pr_usuarioing"])
                            putIfMissing("ep_codigo", salida["ep_codigo"])
                            putIfMissing("tc_codigo", salida["tc_codigo"])
                            putIfMissing("pr_estadotransporte", salida["pr_estadotransporte"])
                            putIfMissing("pr_observacion", "")
                            putIfMissing("pr_porcseguro", "0")
                            putIfMissing("pr_nivel", salida["cc_nivelprecio"] ?: "0")

                            return salida
                        }

                        if (insideRecord) {
                            currentField = ""
                        }
                    }
                }

                eventType = parser.next()
            }

            salida
        } catch (e: Exception) {
            Log.e("XML", "Error parseando cabecera proforma", e)
            salida
        }
    }

    fun fnParseXmlDetalleProforma(xmlString: String): List<datosDet> {
        val lista = mutableListOf<datosDet>()
        if (xmlString.isBlank()) return lista

        fun String.toDoubleSafeLocal(): Double {
            return trim().replace(",", ".").toDoubleOrNull() ?: 0.0
        }

        fun fmt2(value: Double): String {
            return String.format(Locale.US, "%.2f", value)
        }

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false

            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlString))

            val recordTags = setOf("Table1", "Detalle", "detalle")
            val item = linkedMapOf<String, String>()

            var event = parser.eventType
            var currentField = ""
            var insideRecord = false

            fun value(vararg keys: String): String {
                for (key in keys) {
                    item[key]?.let {
                        if (it.isNotBlank()) return it
                    }

                    item.entries.firstOrNull {
                        it.key.equals(key, ignoreCase = true)
                    }?.value?.let {
                        if (it.isNotBlank()) return it
                    }
                }

                return ""
            }

            fun agregarDetalle() {
                val codigo = value(
                    "it_codigo",
                    "codigo",
                    "Codigo",
                    "d2"
                )

                val referencia = value(
                    "it_referencia",
                    "referencia",
                    "Referencia",
                    "dp_descripcion"
                )

                val cantidad = value(
                    "dp_cantidad",
                    "cantidad",
                    "Cantidad",
                    "d4"
                ).ifBlank { "0" }

                val precio = value(
                    "dp_precio",
                    "precio",
                    "Precio",
                    "d5"
                ).ifBlank { "0" }

                val subtotal = value(
                    "subtotal",
                    "dp_valorbruto",
                    "valorbruto",
                    "valor_bruto",
                    "d6"
                ).ifBlank {
                    fmt2(cantidad.toDoubleSafeLocal() * precio.toDoubleSafeLocal())
                }

                val descuento = value(
                    "dp_porcdescto",
                    "dp_porcdescuento",
                    "descuento",
                    "DescItem",
                    "d8"
                ).ifBlank { "0" }

                val conDesc = value(
                    "ConDesc",
                    "condesc",
                    "dp_valordescuento"
                ).ifBlank { "0" }

                val unidad = value(
                    "um_pesoCE",
                    "unidadCE",
                    "UnidadCE",
                    "peso",
                    "d7"
                ).ifBlank { "0" }

                val costo = value(
                    "dp_costoPromedio",
                    "it_costoprom",
                    "costoProm",
                    "costProm"
                ).ifBlank { "0" }

                val sugerencia = value(
                    "sugerencia",
                    "Sugerencia"
                ).ifBlank { "0" }

                val combo = value(
                    "cb_codigo",
                    "combo",
                    "Combo",
                    "d9"
                ).ifBlank { "0" }

                val regalo = value(
                    "it_regalo",
                    "regalo",
                    "Regalo",
                    "d10"
                ).ifBlank { "0" }

                val descripcion = value(
                    "descripcion",
                    "it_descripcion",
                    "Descripcion"
                )

                val proceso = value(
                    "proceso",
                    "Proceso",
                    "dp_proceso",
                    "d0"
                ).ifBlank { "0" }

                val secuencia = value(
                    "dp_secuencia"
                ).ifBlank {
                    (lista.size + 1).toString()
                }

                lista.add(
                    datosDet(
                        referencia,
                        cantidad,
                        precio,
                        subtotal,
                        codigo,
                        descripcion,
                        unidad,
                        descuento,
                        conDesc,
                        costo,
                        sugerencia,
                        combo,
                        regalo,
                        0,
                        1,
                        secuencia.toInt()
                    )
                )
            }

            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        val tag = parser.name.orEmpty()

                        if (recordTags.any { it.equals(tag, ignoreCase = true) }) {
                            insideRecord = true
                            currentField = ""
                            item.clear()
                        } else if (insideRecord) {
                            currentField = tag
                        }
                    }

                    XmlPullParser.TEXT -> {
                        if (insideRecord && currentField.isNotBlank()) {
                            val text = parser.text?.trim().orEmpty()
                            if (text.isNotBlank()) {
                                item[currentField] = text
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        val tag = parser.name.orEmpty()

                        if (insideRecord && recordTags.any { it.equals(tag, ignoreCase = true) }) {
                            agregarDetalle()
                            insideRecord = false
                            currentField = ""
                            item.clear()
                        } else if (insideRecord) {
                            currentField = ""
                        }
                    }
                }

                event = parser.next()
            }
        } catch (e: Exception) {
            Log.e("XML", "Error parseando detalle proforma", e)
        }

        return lista
    }

    private fun openPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    fun fnImprimir() {
        AlertDialog.Builder(requireContext())
            .setTitle("Imprimir PDF")
            .setMessage("¿Desea imprimir el PDF con imágenes?")
            .setPositiveButton("Con imagen") { _, _ -> fnGenerarPdf(true) }
            .setNegativeButton("Sin imagen") { _, _ -> fnGenerarPdf(false) }
            .show()
    }

    private fun fnGenerarPdf(imagen: Boolean) {
        lifecycleScope.launch {
            val progressDialog = showProgressDialogItems()

            try {
                val detallesReporte = adaptadorDetalle.datos.map { d ->
                    reporteProforma(
                        codigo = d.Codigo,
                        descripcion = "${d.Referencia} - ${d.Descripcion}",
                        imageUrl = "https://app.cotzul.com/sitenet/digital/9/${d.Codigo}.png",
                        descuento = d.DescItem,
                        cantidad = d.Cantidad,
                        precio = d.Precio,
                        subtotal = d.Subtotal,
                        ConDescuento = d.ConDesc,
                        proceso = d.proceso
                    )
                }.toMutableList()

                val file = withContext(Dispatchers.IO) {
                    frmReporteProformaA.generatePdf(
                        context = requireContext(),
                        documento = txtNumero.text.toString().toIntOrNull() ?: 0,
                        bo_codigo = bodega,
                        imagen = imagen,
                        pedidoIn = txtNumero.text.toString(),
                        fechaIn = txtFecha.text.toString(),
                        clienteIn = txtCliente.text.toString(),
                        transporteIn = txtTransporte.text.toString(),
                        observacionesIn = txtObservacion.text.toString(),
                        pedidoVendIn = "0",
                        ciudadIn = txtCiudadViewPF.text.toString(),
                        formPagoIn = spinnerFormaPag.selectedItem?.toString().orEmpty(),
                        vendedorIn = txtVendedor.text.toString(),
                        loteIn = "",
                        subtotalIn = txtSub.text.toString(),
                        descuentoIn = txtDescuentoT.text.toString(),
                        seguroIn = txtSeguro.text.toString(),
                        ivaIn = txtIva.text.toString(),
                        fleteIn = txtFlete.text.toString(),
                        totalIn = txtTotal.text.toString(),
                        bodegaIn = bodega,
                        epCodigoIn = ep_codigo.toIntOrNull() ?: 0,
                        detallesIn = detallesReporte
                    )
                }

                progressDialog.dismiss()

                showToast("PDF generado correctamente")

                openPdf(requireContext(), file)

            } catch (e: Exception) {
                try {
                    progressDialog.dismiss()
                } catch (_: Exception) {
                }

                Log.e("PDF_ERROR", "Error al generar PDF", e)
                showToast("Error al generar PDF: ${e.message}")
            }
        }
    }



    private fun fnEsEnter(actionId: Int, event: KeyEvent?): Boolean {
        return actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
    }

    private fun toDoubleSafe(value: String): Double {
        return value.replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    private fun fnLimpiarFoco() {
        val rootView = view
        if (rootView != null) {
            rootView.clearFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(rootView.windowToken, 0)
        }
    }


}