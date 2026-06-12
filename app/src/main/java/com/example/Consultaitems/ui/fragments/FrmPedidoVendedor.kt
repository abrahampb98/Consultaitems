package com.example.Consultaitems.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
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
import android.provider.MediaStore
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
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.adapters.ClienteAdapter
import com.example.Consultaitems.ui.adapters.MiAdaptadorRef
import com.example.Consultaitems.ui.adapters.MiAdapterDetalle
import com.example.Consultaitems.ui.adapters.TransporteAdapter
import com.example.Consultaitems.ui.adapters.datos
import com.example.Consultaitems.ui.adapters.datosDet
import com.example.Consultaitems.utils.cls.ClienteDatos
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.ClsRegImagenOrden
import com.example.Consultaitems.utils.cls.CombosDialogFragment
import com.example.Consultaitems.utils.cls.DownloadImageTask
import com.example.Consultaitems.utils.cls.HistorialVenta
import com.example.Consultaitems.utils.cls.ImageCache
import com.example.Consultaitems.utils.cls.Pedido
import com.example.Consultaitems.utils.cls.PedidosDialogFragment
import com.example.Consultaitems.utils.cls.ProductoDisponible
import com.example.Consultaitems.utils.cls.RecommendationsDialog
import com.example.Consultaitems.utils.cls.SpinnerItem
import com.example.Consultaitems.utils.cls.VentaHistorial
import com.example.Consultaitems.utils.cls.hisotrial
import com.example.Consultaitems.utils.parser.XMlParserA
import com.example.Consultaitems.utils.parser.XmlClienteAct
import com.example.Consultaitems.utils.parser.XmlPedido
import com.example.Consultaitems.utils.parser.xmlClientesAct
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.property.HorizontalAlignment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class frmPedidoVendedor : Fragment(), MiAdaptadorRef.OnItemClickListener, MiAdaptadorRef.OnImageClickListener, MiAdapterDetalle.AdapterCallbacks, MiAdapterDetalle.OnItemClickListener {
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
    lateinit var txtTotal: TextView
    lateinit var txtSub: TextView
    lateinit var txtIva: TextView
    lateinit var txtDesc: TextView
    private lateinit var spinnerPrioridad: Spinner
    private lateinit var spinnerTipoPed: Spinner
    private lateinit var spinnerFormaPag: Spinner
    private lateinit var spinnerPlazo: Spinner
    private lateinit var spinnerSeguro: Spinner
    private lateinit var spinnerItem: Spinner
    private lateinit var llenarControles: ClsLLenarControles  // Declaración
    lateinit var solicitudSoap: SolicitudSoap
    private lateinit var database: SQLiteDatabase
    private var datosInsertados: String = ""
    lateinit var txtVendedor: TextView
    private lateinit var txtFecha: TextView
    private lateinit var txtCliente: AutoCompleteTextView
    private lateinit var txtTransporte: AutoCompleteTextView
    private lateinit var txtPolitica: TextView
    private lateinit var txtDescuentoT: TextView
    private lateinit var txtSeguro: TextView
    private lateinit var btnGuardar: Button
    private lateinit var btnOrden: Button
    private lateinit var ep_codigo: String
    private lateinit var usuario: String
    private lateinit var tr_codigo: String
    private lateinit var cl_codigo: String
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
    private lateinit var adaptadorDetalle: MiAdapterDetalle
    private lateinit var adaptadorRef: MiAdaptadorRef
    private var valInser: Int = 0
    private var vgsOpcionMenu: String = ""
    private lateinit var btnEliminar: Button
    private lateinit var btnEnviar: Button
    private var porcdescuento: Double = 0.0
    private var unidadCE: Double = 0.0
    private lateinit var ClaseXml: XmlPedido
    private var vliExito: Int = 0
    private var vliGuardar: Int = 0
    private var vgsEstado: String = ""
    private lateinit var rootLayout: View
    private var Tarifa: String = ""
    private var Cobertura: String = ""
    private lateinit var Kilo: TextView
    private lateinit var tarifa: TextView
    private lateinit var cobertura: TextView
    private lateinit var btnImprimir: Button
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
    private var errorMessage: String = ""
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
    private var margen: Double = 0.0
    private var mg_regalo: Double = 0.0

    private lateinit var btnSugerencia: Button
    private lateinit var ClaseXmlCliente: XmlClienteAct
    private var vgsActualiza: Int = 0
    private var provincia: Int = 0
    private var plazo: String = ""


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
        val view = inflater.inflate(R.layout.frmprueba, container, false)

        // Lógica para determinar el tamaño de la pantalla
        val screenSize =
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK

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

        adaptadorDetalle = MiAdapterDetalle(todosLosItemsDet, this,
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
                    it_regalo = item.regalo
                )


            })
        adaptadorDetalle.callbacks = this
        adaptadorRef = MiAdaptadorRef(datosList, this, this)
        llenarControles = ClsLLenarControles(requireContext())

        fnInicializarVariables(view)
        fnLLenarControles()
        fnDesactivarControles()


        btnGuardar.setOnClickListener {
            if(adaptadorDetalle.fnSugerencia()==0){
                fnSugerencias()
            }else{
                fnMostrarDialogoDeConfirmacion()
            }
        }

        btnBuscar.setOnClickListener {
            val codDocumento = txtNumero.text.toString().toIntOrNull()
            if (codDocumento != null) {
                fnConsultarPedido(codDocumento)
                //fnReferenciaCombos()
                if (vgsEstado == "C") {
                    fnDesactivarControles()
                    btnImprimir.isEnabled = true
                }
            } else {
                showToast("Ingrese un numero de documento valido")
            }
        }

        btnOk.setOnClickListener {
            fnBusqueda()
        }

        btnAgregar.setOnClickListener {
            fnValidarRegalo()
            hideSoftKeyboard()
        }

        btnEliminar.setOnClickListener {
            vgsOpcionMenu = "E"
            fnMostrarDialogoDeConfirmacion()
            //btnGuardar.isEnabled = false
            //btnEnviar.isEnabled = false
        }

        btnEnviar.setOnClickListener {
            if (fnIsNetworkAvailable(requireContext())) {
                if (cl_orden == 0) {
                    val valor =
                        txtSub.text.toString().toDouble() - txtDescuentoT.text.toString().toDouble()
                    if (adaptadorDetalle.fnRegalo() == 0 && txtLote.text.toString() >= margen.toString() && valor >= mg_regalo) {
                        fnAgregarRegalo()
                    } else {
                        fnConfirmarEnvio()
                    }
                } else if (pdfBase64 != null) {
                    fnConfirmarEnvio()
                } else {
                    showToast("Debe asociar una imagen a la orden")
                    fnActivarBotonesOrden()

                }
            } else {
                showToast("Verifique su conexión a internet")
            }
        }


        btnImprimir.setOnClickListener {
            val file = frmReportePedido.generatePdf(
                requireContext(),
                txtNumero.text.toString().toInt()
            )
            fnOpenPdf(file)
        }

        btnOrden.setOnClickListener {
            fnOpenImagePicker()

        }


        /*btnSugerencia.setOnClickListener {
            fnSugerencias()
        }*/

        txtCliente.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No necesitamos usar este método ahora
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No necesitamos usar este método ahora
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) {
                    // Cuando el campo del cliente se borre, también borra el campo de política
                    txtPolitica.text = ""
                }
            }
        })

        txtDesc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (vliExito == 1) {
                    if (td_codigo.toInt() == 1) {
                        fnactualizarDescuento(txtDesc.text.toString())
                        adaptadorDetalle.fnObtenerDescuento()
                        fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())
                        if (adaptadorDetalle.itemCount > 0){
                            fnGuardadoautomatico()
                        }
                    }

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

        spinnerFormaPag.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val item = parent.getItemAtPosition(position) as SpinnerItem
                tp_codigo = item.codigo  // Asigna el código del ítem seleccionado a tp_codigo
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Opcionalmente manejar la no selección
            }
        }

        spinnerTipoPed.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val item = parent.getItemAtPosition(position) as SpinnerItem
                im_codigo = item.codigo
                txtReferencia.setText("")
                adaptadorRef.clearItems()

                if (vgsOpcionMenu == "I") {
                    when (spinnerTipoPed.selectedItemPosition) {
                        0 -> {
                            fnReferenciaCombos(txtReferencia.text.toString().trim())
                        }

                        1 -> {
                            datosList.clear()
                            adaptadorRef.clearItems()

                            val resultados = llenarControles.fnBuscaReferenciaRetroVenta("")
                            for (dato in resultados) {
                                datosList.add(dato)
                            }

                            ReciclerviewRef.layoutManager = LinearLayoutManager(requireContext())
                            ReciclerviewRef.adapter = adaptadorRef
                        }
                    }
                }

                if (im_codigo.toInt() == 14) {
                    txtDesc.isEnabled = false
                    txtCantidad.isEnabled = false
                    txtPrecio.isEnabled = false
                    spinnerItem.isEnabled = false
                } else if (vgsOpcionMenu == "I" || vgsOpcionMenu == "M") {
                    txtReferencia.isEnabled = true
                    txtDesc.isEnabled = true
                    txtCantidad.isEnabled = true
                    txtPrecio.isEnabled = true
                    spinnerItem.isEnabled = true
                    txtReferencia.requestFocus()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val item = parent.getItemAtPosition(position) as SpinnerItem
                td_codigo = item.codigo
                //showToast(td_codigo)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Opcionalmente manejar la no selección
            }
        }

        spinnerPrioridad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val item = parent.getItemAtPosition(position) as SpinnerItem
                pr_codigo = item.codigo
                //showToast(pr_codigo)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Opcionalmente manejar la no selección
            }
        }

        // Configurar el listener para cuando se seleccione una opción
        spinnerSeguro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Obtener la opción seleccionada
                val seleccion = parent.getItemAtPosition(position).toString()

                // Realizar la acción según la opción seleccionada
                when (seleccion) {
                    "Sí" -> {
                        fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())
                    }

                    "No" -> {
                        fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Acción cuando no se selecciona nada (opcional)
            }
        }


        spinnerListado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Obtener la opción seleccionada
                val seleccion = parent.getItemAtPosition(position).toString()

                // Realizar la acción según la opción seleccionada
                when (seleccion) {

                    "Item" -> {
                        datosList.clear()
                        adaptadorRef.clearItems()
                        fnLimpiarBusqueda(position)
                    }

                    "Combo" -> {
                        fnLimpiarBusqueda(position)
                        fnReferenciaCombos(txtReferencia.text.toString().trim())
                    }

                    "Regalo" -> {
                        fnLimpiarBusqueda(position)
                        fnReferenciaRegalos(txtReferencia.text.toString().trim())
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Acción cuando no se selecciona nada (opcional)
            }
        }


        spinnerPlazo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val item = parent.getItemAtPosition(position) as SpinnerItem
                plazo = item.codigo
                pz_codigo = plazo

                spinnerListado.isEnabled = plazo != "35"
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        return view
    }

    private fun fnLimpiarBusqueda(position: Int) {
        adaptadorRef.fnObtenerSpinner(position)
        txtCantidad.setText("")
        txtPrecio.setText("")
        txtReferencia.setText("")
    }


    private fun fnBusqueda() {
        hideSoftKeyboard()

        val referencia = txtReferencia.text.toString().trim()
        itemEnEdicion = null
        nuevoItemSeleccionado = null

        if (plazo != "35") {
            when (spinnerListado.selectedItemPosition) {
                0 -> fnBuscarReferencia(referencia)
                1 -> fnReferenciaCombos(referencia)
                2 -> fnReferenciaRegalos(referencia)
            }

            txtPrecio.text = ""
            txtCantidad.text = ""
        } else {
            fnReferenciaItemParo(referencia)
        }
    }

    private fun showProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Enviando pedido...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    // Enviar datos
    private inner class MiAsyncTask(private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase
        private var datosInsertados: String = ""

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {
            return try {
                database = dbHelper.writableDatabase
                val cadena = ClaseXml.obtenerXmlPedido(txtNumero.text.toString().toInt())
                solicitudSoap.initializeVariables(getString(R.string.str_pedido).toInt(), cadena)
                var pedido: String = ""
                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.bufferedReader()?.use { it.readText() }
                if (!result.isNullOrBlank()) {
                    pedido = XMlParserA.parseAndUpdateDocumentCode(
                        result,
                        database,
                        txtNumero.text.toString(),
                        requireContext()
                    )
                }
                pedido
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
                showResultDialog(result)
            } else {
                // Mostrar el mensaje de error
                Toast.makeText(requireContext(), errorMessage ?: "", Toast.LENGTH_LONG).show()
            }
        }

        private fun showResultDialog(pedido: String) {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle("Pedido Enviado")
            dialog.setMessage("Pedido del sistema #:$pedido")
            dialog.setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.dismiss() }
            dialog.show()
            btnEnviar.isEnabled = false

            //envio de imagen asociada a la orden
            if (cl_orden == 1) {
                fnEnviarImagenes(cl_codigo, txtOrden.text.toString(), pdfBase64!!, requireContext())
            }

            if (vgsActualiza == 1) {
                ClaseXmlCliente = XmlClienteAct(requireContext())
                solicitudSoap = SolicitudSoap(requireContext())

                val progressDialog = showProgressDialog()
                MiAsyncTaskActualizacionCliente(progressDialog).execute()
            }
        }
    }

    //valida si mi adapatador esta vacio
    override fun onEmptyState(isEmpty: Boolean) {

        spinnerTipoPed.isEnabled = isEmpty
        spinnerItem.isEnabled = isEmpty
        Log.d("MainActivity", "Adapter is empty: $isEmpty")
    }

    //manejo del menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.frm_menu_pedido, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    //opciones del menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bntNuevoPv -> {
                vgsOpcionMenu = "I"
                btnGuardar.text = "Guardar"
                fnAccionesAlPulsarNuevo()
                fnActivarControles()
                btnEliminar.isEnabled = false
                btnEnviar.isEnabled = false
                btnImprimir.isEnabled = false
                vgsEstado = "A"
                constraintLayout.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.background)

                true
            }

            R.id.btnModificarPv -> {
                vgsOpcionMenu = "M"
                btnGuardar.text = "Modificar"
                fnAcionesAlPulsarModificar()
                constraintLayout.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.background)
                true
            }

            R.id.btnPedidoPv -> {
                vgsOpcionMenu = "M"
                btnGuardar.text = "Modificar"
                fnDesactivarControles()
                fnMostrarDialogoDePedidos()
                fnAccionesAlPulsarTodos()
                constraintLayout.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.background)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fnConfirmarEnvio() {

        // Crear un AlertDialog para confirmar el envío
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmar envío")
        builder.setMessage("¿Desea enviar el pedido?")
        builder.setPositiveButton("Sí") { dialog, which ->
            try {
                //Log.d("Opcion si ", "Ingreso")
                ClaseXml = XmlPedido(requireContext())
                solicitudSoap = SolicitudSoap(requireContext())
                val progressDialog = showProgressDialog()
                MiAsyncTask(progressDialog).execute()
            } catch (e: Exception) {
                //Log.e("UpdateError", "Error during update: ${e.message}")
                e.printStackTrace()
            }
        }

        builder.setNegativeButton("No") { dialog, which ->
            // Si el usuario no desea enviar, simplemente cerrar el diálogo
            dialog.dismiss()
        }
        builder.show()
    }

    private fun fnActivarBotonesOrden() {
        fnActivarControles()
        btnEnviar.isEnabled = false
        btnEliminar.isEnabled = false
        btnImprimir.isEnabled = false

    }

    private fun fnAgregarRegalo() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Sistema")
        builder.setMessage("Debe agregar Obsequio(Regalo)")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()

        fnActivarControles()
        btnEnviar.isEnabled = false
        btnEliminar.isEnabled = false
        btnImprimir.isEnabled = false
    }


    private fun fnControlSpinners() {

        if (vgsOpcionMenu == "M") {
            if (adaptadorDetalle.itemCount > 0) {
                spinnerTipoPed.isEnabled = false
                spinnerItem.isEnabled = false
            }
        }
    }

    private fun hideSoftKeyboard() {
        Log.d("hideSoftKeyboard", "Ocultando el teclado")
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun fnMostrarDialogoDePedidos() {
        val dialog = PedidosDialogFragment()
        var pedido: Int = 0
        dialog.onPedidoSelected = { numeroPedido ->
            // Aquí puedes manejar el número de pedido seleccionado
            requireActivity().runOnUiThread {
                //Toast.makeText(this, "Pedido seleccionado: $numeroPedido", Toast.LENGTH_LONG).show()
                pedido = numeroPedido.toInt()
            }

            if (pedido != 0) {
                txtReferencia.setText("")
                adaptadorRef.clearItems()
                txtNumero.text = pedido.toString()
                fnConsultarPedido(pedido)
                hideSoftKeyboard()
                fnActivarControles()
                fnControlSpinners()
                if (vgsEstado == "C") {
                    fnDesactivarControles()
                    btnImprimir.isEnabled = true
                }
                //fnReferenciaCombos()
                cl_orden = llenarControles.fnObtenerOrden(cl_codigo)
            }
        }
        dialog.show(requireActivity().supportFragmentManager, "PedidosDialogFragment")

    }

    private fun fnInicializarVariables(view: View) {
        txtReferencia = view.findViewById(R.id.txtReferencia)
        btnOk = view.findViewById(R.id.btnOK)
        dbHelper = SqLiteOpenHelper(requireContext())
        ReciclerviewRef = view.findViewById(R.id.recyclerViewRef)
        btnAgregar = view.findViewById(R.id.btnAgregar)
        txtCantidad = view.findViewById(R.id.txtCantidad)
        ReciclerviewDet = view.findViewById(R.id.recyclerViewDetalle)
        txtPrecio = view.findViewById(R.id.txtPre)
        txtTotal = view.findViewById<TextView>(R.id.txtTotalPv)
        txtSub = view.findViewById<TextView>(R.id.txtSubotal)
        txtIva = view.findViewById<TextView>(R.id.txtIva)
        txtDesc = view.findViewById<TextView>(R.id.txtDescuento)
        txtVendedor = view.findViewById<TextView>(R.id.txtVendedor)
        txtFecha = view.findViewById<TextView>(R.id.txtFecha)
        txtFecha.text = fnFecha()
        txtTransporte = view.findViewById(R.id.txtTransporte)
        txtCliente = view.findViewById<AutoCompleteTextView>(R.id.txtCliente)
        txtPolitica = view.findViewById<AutoCompleteTextView>(R.id.txtPolitica)
        txtDescuentoT = view.findViewById<TextView>(R.id.txtDescuentoT)
        txtSeguro = view.findViewById<TextView>(R.id.txtSeguro)
        spinnerPrioridad = view.findViewById(R.id.spinnerPrioridad)
        spinnerTipoPed = view.findViewById(R.id.spinnerTipoPed)
        spinnerFormaPag = view.findViewById(R.id.spinnerFormaPag)
        spinnerPlazo = view.findViewById(R.id.spinnerPlazo)
        spinnerSeguro = view.findViewById(R.id.spinnerSeguro)
        spinnerItem = view.findViewById(R.id.spinnerItem)
        btnGuardar = view.findViewById(R.id.btnGuardar)
        txtObservacion = view.findViewById(R.id.txtObservacion)
        txtFlete = view.findViewById(R.id.txtFlete)
        txtOrden = view.findViewById(R.id.txtOrden)
        txtNumero = view.findViewById(R.id.txtNumero)
        txtLote = view.findViewById(R.id.txtLote)
        btnBuscar = view.findViewById(R.id.btnBuscarPv)
        btnEnviar = view.findViewById(R.id.btnEnviar)
        btnEliminar = view.findViewById(R.id.btnEliminarDoc)
        btnImprimir = view.findViewById(R.id.btnImprimir)

        Kilo = view.findViewById(R.id.txtKiloView)
        tarifa = view.findViewById(R.id.txtTarifaView)
        cobertura = view.findViewById(R.id.txtCoberturaView)

        btnOrden = view.findViewById(R.id.btnOrden)

        constraintLayout = view.findViewById(R.id.constraint)

        spinnerListado = view.findViewById(R.id.spinnerListado)

        //btnSugerencia = view.findViewById(R.id.btnSugerencia)

        hideSoftKeyboard()
    }

    private fun fnBuscarReferencia(BusReferencia: String) {

        if (BusReferencia.isNotEmpty()) {
            datosList.clear()
            adaptadorRef.clearItems()

            if (im_codigo.toInt() == 9) {//busca items de retroventa
                val resultados = llenarControles.fnBuscaReferenciaRetroVenta(BusReferencia)
                for (dato in resultados) {
                    datosList.add(dato)
                }
                // Configura el RecyclerView y asigna el adaptador
                val layoutManager = LinearLayoutManager(requireContext())
                ReciclerviewRef.layoutManager = layoutManager
                ReciclerviewRef.adapter = adaptadorRef

            } else if (im_codigo.toInt() == 1) { //todos los items
                val resultados = llenarControles.fnBuscaReferenciaYcombos(BusReferencia)

                for (dato in resultados) {
                    datosList.add(dato)
                }
                // Configura el RecyclerView y asigna el adaptador
                val layoutManager = LinearLayoutManager(requireContext())
                ReciclerviewRef.layoutManager = layoutManager
                ReciclerviewRef.adapter = adaptadorRef
            } else if (im_codigo.toInt() == 14) {
                val resultados = llenarControles.fnBuscaReferenciaCombos(BusReferencia)
                for (dato in resultados) {
                    datosList.add(dato)
                }
                // Configura el RecyclerView y asigna el adaptador
                val layoutManager = LinearLayoutManager(requireContext())
                ReciclerviewRef.layoutManager = layoutManager
                ReciclerviewRef.adapter = adaptadorRef
            }

        } else {
            // Si el campo de referencia está vacío, muestra un mensaje o realiza alguna otra acción
            //showToast("Ingrese una referencia")
            //fnReferenciaCombos()
        }

    }

    private fun fnReferenciaCombos(combo: String) {
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

    private fun fnReferenciaRegalos(regalo: String) {
        datosList.clear()
        adaptadorRef.clearItems()

        val subt = txtSub.text.toString().toDoubleOrNull() ?: 0.0
        val desc = txtDescuentoT.text.toString().toDoubleOrNull() ?: 0.0

        if (subt == 0.0) {
            return
        }

        val monto = (subt - desc) * 0.05

        val resultados = llenarControles.fnBuscaReferenciaRegalo(regalo,monto.toString()
        )

        for (dato in resultados) {
            datosList.add(dato)
        }

        ReciclerviewRef.layoutManager = LinearLayoutManager(requireContext())
        ReciclerviewRef.adapter = adaptadorRef
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(item: datos) {
        txtCantidad.requestFocus()
        txtPrecio.text = item.precioSub
    }

    override fun onFirstImageClick(referencia: String, codigo: String) {

        fnPreciosStock(referencia, codigo)
    }

    override fun onSecondImageClick(codigo: String) {
        fnShowImageDialog(codigo)
    }

    override fun onThirdImageClick(codigo: String) {
        if (txtCliente.text.toString().isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Debe seleccionar un cliente primero.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (cl_codigo.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Seleccione un cliente ", Toast.LENGTH_SHORT).show()
            return
        }

        fnHistorial(cl_codigo!!, codigo)
    }


    fun fnCalcularTotales(subt: Double) {
        // Obtiene el valor de descuento acumulado de los detalles

        var subtotalDescontado: Double = 0.0
        var descuento = adaptadorDetalle.fnObtenerDescuento()
        unidadCE = adaptadorDetalle.fnObtenerPeso()


        var costoPromedio = adaptadorDetalle.fnObtenerCostoProm()

        // Subtotal inicial convertido a BigDecimal
        val subtotal = subt

        val descTxt = txtDesc.text.toString()

        val descuentoTXT = if (descTxt.isEmpty()) {
            BigDecimal.ZERO
        } else {
            descTxt.toBigDecimalOrNull() ?: BigDecimal.ZERO
        }

        if (descuentoTXT > BigDecimal("0.00")) { //descuento es mayor a 0
            subtotalDescontado = subtotal - descuento

            //descuento = subtotalDescontado
        } else {
            subtotalDescontado = subtotal
            descuento = 0.0
        }

        // Determina si aplicar seguro
        val aplicarSeguro = spinnerSeguro.selectedItemPosition == 0
        val seguroPercent =
            if (aplicarSeguro) llenarControles.fnObtenerSeguro().toDouble() / 100.0 else 0.0

        // Calcula el monto del seguro sobre el subtotal después de aplicar el descuento
        val montoSeguro = subtotalDescontado * seguroPercent

        // Calcula el subtotal final sumando el monto del seguro al subtotal descontado
        val subtotalFinal = subtotalDescontado + montoSeguro

        // Tasa de IVA como BigDecimal
        val ivaInc = fnIva()

        // Calcula el IVA sobre el subtotal final
        val iva = subtotalFinal * ivaInc / 100

        //calcula el lote subtotal / costo promedio
        var lote: Double = 0.0
        if (costoPromedio <= 0.0) {
            lote = 0.0
        } else {
            lote = subtotalDescontado / costoPromedio
        }

        // Calcula el total sumando el IVA al subtotal final

        val flete = fnTransporte().toString().toDoubleOrNull() ?: 0.0
        val total = subtotalFinal + iva + flete

        // Actualiza los TextViews con los valores calculados
        txtSub.text = String.format("%.2f", subtotal)
        txtDescuentoT.text = String.format("%.2f", descuento)
        txtSeguro.text = String.format("%.2f", montoSeguro)
        txtIva.text = String.format("%.2f", iva)
        txtTotal.text = String.format("%.2f", total)
        txtLote.text = String.format("%.2f", lote)
        txtFlete.text = String.format("%.2f", flete)
        porcdescuento = adaptadorDetalle.fnObtenerPorctDesc()
        //showToast(porcdescuento.toString())

        fnPesos()
        if (spinnerListado.selectedItemPosition == 2) {
            fnReferenciaRegalos("")
        }
    }

    private fun fnIva(): Int {
        val database = dbHelper.readableDatabase
        val cursor = database.rawQuery("SELECT pi_porcentaje FROM fa_ws_parametroIva", null)
        var porcentajeIva =
            0 // Valor predeterminado en caso de que no se encuentre ningún resultado
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex("pi_porcentaje")
            porcentajeIva = cursor.getInt(columnIndex).toInt()
        }
        cursor.close()
        database.close()
        return porcentajeIva
    }

    private fun fnFecha(): String {
        val fechaActual = Date()
        // Formatear la fecha como string
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaFormateada = dateFormat.format(fechaActual)
        return fechaFormateada

    }

    private fun fnLLenarControles() {
        llenarControles.fnLLenarSpinner(spinnerPrioridad, "ve_ws_prioridad")
        llenarControles.fnLLenarSpinner(spinnerTipoPed, "fa_ws_tipoFactura")
        llenarControles.fnLLenarSpinner(spinnerFormaPag, "cc_ws_transacciones")
        llenarControles.fnLLenarSpinner(spinnerPlazo, "fa_ws_plazo")
        llenarControles.fnLLenarSpinner(spinnerItem, "fa_ws_tipoDescuentoPedido")
        spinnerPlazo.setSelection(6)
        spinnerListado.setSelection(0)

        margen = llenarControles.fnObtenerMargen()
        mg_regalo = llenarControles.fnObtenerValorRegalo()


        // Opciones para el spinner
        val opcionesSeguro = listOf("Sí", "No")

        // Creación del ArrayAdapter usando un layout simple
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, opcionesSeguro)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Asignación del adapter al spinner
        spinnerSeguro.adapter = adapter

        //llenar el spinner listado
        val opcionesListado = listOf("Item", "Combo", "Regalo")

        val adapterListado =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, opcionesListado)
        adapterListado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Asignación del adapter al spinner
        spinnerListado.adapter = adapterListado


        //obtener vendedor y codigo
        val vendedor = llenarControles.fnLLenarVendedor()
        if (vendedor != null) {
            usuario = vendedor.login
            txtVendedor.text = usuario
        }

        if (vendedor != null) {
            ep_codigo = vendedor.codigo
        }

        //txtcliente
        val adaptercliente = ClienteAdapter(requireContext(), llenarControles.fnCargarClientes())
        txtCliente.setAdapter(adaptercliente)
        txtCliente.setOnItemClickListener { _, _, position, _ ->
            val cliente = adaptercliente.getItem(position)
            if (cliente != null) {
                txtCliente.setText(
                    cliente.nombre,
                    false
                )  // Configura el nombre del cliente seleccionado en el AutoCompleteTextView
                cl_codigo = cliente.id  // Guarda el ID del cliente en cl_codigo


                val politica = llenarControles.fnObtenerPolitica(cliente.id)
                if (politica != null) {
                    txtPolitica.text = politica.descripcion
                    pz_codigo = politica.codigo

                    fnBuscarProvinciaCliente()

                    cl_orden = llenarControles.fnObtenerOrden(cl_codigo)

                    fnPolitica(cl_codigo.toInt())
                    fnActualizarDatosCliente()

                    if (txtSub.text.toString().toDouble() != 0.00) {
                        fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())
                    }
                }

                cl_orden = llenarControles.fnObtenerOrden(cl_codigo)
                //showToast("$cl_orden")

                fnPolitica(cl_codigo.toInt())
            }
        }

        //txtTransporte
        val adaptadorTransporte =
            TransporteAdapter(requireContext(), llenarControles.fnCargarDatosTransporte())
        txtTransporte.setAdapter(adaptadorTransporte)
        txtTransporte.setOnItemClickListener { _, _, position, _ ->
            val transporte = adaptadorTransporte.getItem(position)
            if (transporte != null) {
                txtTransporte.setText(transporte.nombre, false)
                tr_codigo = transporte.codigo
                if (txtSub.text.toString().toDouble() != 0.00) {
                    fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())
                }
            }
        }
    }

    fun fnDesactivarControles() {
        txtNumero.isEnabled = false
        txtVendedor.isEnabled = false
        txtFecha.isEnabled = false
        txtPolitica.isEnabled = false
        spinnerPrioridad.isEnabled = false
        spinnerSeguro.isEnabled = false
        spinnerPlazo.isEnabled = false
        spinnerTipoPed.isEnabled = false
        spinnerFormaPag.isEnabled = false
        spinnerItem.isEnabled = false
        txtCliente.isEnabled = false
        txtTransporte.isEnabled = false
        txtObservacion.isEnabled = false
        txtOrden.isEnabled = false
        txtPrecio.isEnabled = false
        txtDesc.isEnabled = false
        txtCantidad.isEnabled = false
        txtReferencia.isEnabled = false
        btnAgregar.isEnabled = false
        btnOk.isEnabled = false
        btnGuardar.isEnabled = false
        btnEliminar.isEnabled = false
        btnEnviar.isEnabled = false
        btnBuscar.isEnabled = false
        btnImprimir.isEnabled = false
        btnOrden.isEnabled = false
        spinnerListado.isEnabled = false

    }

    fun fnActivarControles() {
        spinnerPrioridad.isEnabled = true
        spinnerSeguro.isEnabled = true
        spinnerPlazo.isEnabled = true
        spinnerTipoPed.isEnabled = true
        spinnerFormaPag.isEnabled = true
        spinnerItem.isEnabled = true
        txtCliente.isEnabled = true
        txtTransporte.isEnabled = true
        txtObservacion.isEnabled = true
        txtOrden.isEnabled = true
        txtPrecio.isEnabled = true
        txtDesc.isEnabled = true
        txtCantidad.isEnabled = true
        btnAgregar.isEnabled = true
        txtReferencia.isEnabled = true
        btnOk.isEnabled = true
        btnGuardar.isEnabled = true
        btnBuscar.isEnabled = true
        btnOrden.isEnabled = true
        spinnerListado.isEnabled = true
    }

    fun fnAcionesAlPulsarModificar() {
        fnDesactivarControles()
        txtNumero.isEnabled = true
        txtNumero.requestFocus()
        txtNumero.setText("")
        btnBuscar.isEnabled = true
        adaptadorDetalle.clearItems()
        adaptadorRef.clearItems()
        spinnerTipoPed.isEnabled = false
        fnLimpiarControles()
        itemEnEdicion = null
    }

    fun fnAccionesAlPulsarNuevo() {
        //txtNumero
        val nuevoCodigoDocumento = llenarControles.fnObtenerMaxioDocumento()
        pe_coddocumento = nuevoCodigoDocumento.toString()
        txtNumero.text = pe_coddocumento
        txtNumero.isEnabled = false
        btnBuscar.isEnabled = false
        // Limpia el contenido de los adaptadores existentes
        adaptadorDetalle.clearItems()
        adaptadorRef.clearItems()
        fnLimpiarControles()
        fnLLenarControles()
        itemEnEdicion = null
    }

    fun fnAccionesAlPulsarTodos() {
        adaptadorDetalle.clearItems()
        adaptadorRef.clearItems()
        fnLimpiarControles()
    }

    fun fnLimpiarControles() {
        txtCliente.setText("")
        txtObservacion.setText("")
        txtReferencia.setText("")
        txtCliente.setText("")
        txtDesc.setText("0.00")
        txtOrden.setText("")
        txtTransporte.setText("")
        txtFlete.setText("0.00")
        txtCantidad.setText("")
        txtPrecio.setText("")
    }

    fun fnGuardadoautomatico() {
        if (adaptadorDetalle.itemCount > 0) {

            vliGuardar = 1
            //funcion donde guarda los datos de la cabecera y el detalle
            val cabValues = ContentValues().apply {
                put("pe_coddocumento", txtNumero.text.toString())
                put("ep_codigo", ep_codigo)
                put("tr_codigo", tr_codigo)
                put("cl_codigo", cl_codigo)
                put("tp_codigo", tp_codigo)
                put("pz_codigo", pz_codigo)
                put("pe_descripcion", txtObservacion.text.toString())
                put("pe_valorbruto", txtSub.text.toString())
                put("pe_porcentdescuento", porcdescuento)
                put("pe_valordescuento", txtDescuentoT.text.toString())
                put("pe_seguro", txtSeguro.text.toString())
                put("pe_flete", txtFlete.text.toString())
                put("pe_valoriva", txtIva.text.toString())
                put("pe_valorTotal", txtTotal.text.toString())

                put("pe_estado", vgsEstado)
                put("pr_codigo", pr_codigo)
                put("te_codigo", 5)//dispositivo
                put(
                    "pe_orden",
                    if (txtOrden.text.toString().isEmpty()) "0" else txtOrden.text.toString()
                )
                put("td_codigo", td_codigo)
                put("im_codigo", im_codigo)
                put("pe_usuarioing", usuario)
                put("pe_fechaing", txtFecha.text.toString())
                put("pe_fechamod", fnFecha())
                put("pe_lote", txtLote.text.toString())

            }
            // Obtén la instancia del Adapter
            val adapterDetalle = ReciclerviewDet.adapter as MiAdapterDetalle

            // Obtén los datos del Adapter
            val detallesDelPedido = adapterDetalle.datos.mapIndexed { index, detalle ->
                ContentValues().apply {
                    put("em_codigo", 2)
                    put("bo_codigo", 1)
                    put("pe_coddocumento", txtNumero.text.toString())
                    put("ep_codigo", ep_codigo)
                    put("dp_secuencia", index + 1)
                    put("it_codigo", detalle.Codigo)
                    put("dp_cantidad", detalle.Cantidad.toInt())
                    put("dp_precio", String.format("%.3f", detalle.Precio.toDouble()).toDouble())
                    if (!detalle.Descripcion.isNullOrEmpty()) {
                        put("dp_descripcion", "${detalle.Referencia} - ${detalle.Descripcion}")
                    } else {
                        put("dp_descripcion", detalle.Referencia)
                    }

                    put("dp_estado", vgsEstado)
                    put("te_codigo", 5)
                    if (!detalle.unidadCE.isNullOrEmpty()) {
                        put("um_pesoCE", detalle.unidadCE)
                    } else {
                        put("um_pesoCE", "0")
                    }
                    put("dp_porcdescuento", detalle.DescItem)
                    put("it_activaex", "0")
                    put("cb_codigo", "0")
                    put("dp_usuarioing", usuario)
                    put("dp_fechaing", txtFecha.text.toString())
                    put("dp_fechamod", fnFecha())
                    put("dp_costoPromedio", detalle.costProm)
                    put("dp_combo", detalle.combo)
                    put("it_regalo", detalle.regalo)
                    put("dp_sugerencia", detalle.sugerencia)
                }
            }

            if (llenarControles.fnActualizarPedido(
                    txtNumero.text.toString().toInt(),
                    cabValues,
                    detallesDelPedido
                )
            ) {
                Log.d("ClsLLenarControles", "Pedido insertado con éxito")

            } else {
                Log.e("ClsLLenarControles", "Fallo al insertar pedido")
            }

        } else {
            showToast("Ingrese lineas de detalle")
            vliGuardar = 0
        }

    }


    fun fnAgregarDetalles() {
        if (txtCantidad.text.toString().isNotEmpty()) {

            spinnerItem.isEnabled = false
            spinnerTipoPed.isEnabled = false

            var selectedItems = (ReciclerviewRef.adapter as? MiAdaptadorRef)?.getSelectedItems()

            if (selectedItems.isNullOrEmpty() || itemEnEdicion != null) {
                selectedItems = nuevoItemSeleccionado?.let { listOf(it) } ?: emptyList()
            }


            if (selectedItems.isNullOrEmpty()) {
                showToast("No se han seleccionado elementos para agregar")
            } else {
                selectedItems.forEach { item ->
                    val cantidad = txtCantidad.text.toString().toIntOrNull() ?: 0
                    val precio =
                        String.format("%.3f", txtPrecio.text.toString().toBigDecimal().setScale(3))
                    val subtotal =
                        String.format("%.2f", cantidad.toBigDecimal() * precio.toBigDecimal())
                    val descuento = String.format(
                        "%.2f",
                        txtDesc.text.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
                    )
                    val conDescto = if (descuento.toBigDecimal() > BigDecimal.ZERO) {
                        subtotal.toBigDecimal() - (subtotal.toBigDecimal() * descuento.toBigDecimal() / BigDecimal(
                            "100"
                        )).setScale(2, RoundingMode.HALF_UP)
                    } else {
                        BigDecimal("0.00")
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
                        "0",
                        item.it_regalo

                    )


                    itemEnEdicion?.let { indiceEdicion ->
                        if (todosLosItemsDet.isNotEmpty() && indiceEdicion >= 0 && indiceEdicion < todosLosItemsDet.size) {
                            todosLosItemsDet[indiceEdicion] = newItem
                            itemEnEdicion = null // Restablecer después de editar
                        }
                    } ?: run {
                        // Modo agregado: Verificar duplicados antes de agregar
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

                fnGuardadoautomatico()

                txtCantidad.setText("")
                txtPrecio.setText("")
                txtReferencia.setText("")
            }
        }
    }


    fun fnAgregarDetallesCombo() {
        var selectedItems = (ReciclerviewRef.adapter as? MiAdaptadorRef)?.getSelectedItems()

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
                subtotal.toBigDecimal() - (subtotal.toBigDecimal() * descuento.toBigDecimal() / BigDecimal(
                    "100"
                )).setScale(2, RoundingMode.HALF_UP)
            } else {
                BigDecimal("0.00")
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


    private fun fnConsultarPedido(codDocumento: Int) {

        var clCodigo: String = ""
        var trCodigo: String = ""
        var descuento: String = ""
        var plazo: String = ""

        btnGuardar.isEnabled = true
        llenarControles.fnConsultarPedidos(codDocumento,
            actualizarCabecera = { cursor ->
                requireActivity().runOnUiThread {
                    try {
                        txtNumero.isEnabled = false
                        clCodigo = cursor.getString(cursor.getColumnIndexOrThrow("cl_codigo"))
                        trCodigo = cursor.getString(cursor.getColumnIndexOrThrow("tr_codigo"))
                        //txtNumero.text = cursor.getString(cursor.getColumnIndexOrThrow("pe_coddocumento"))
                        plazo = cursor.getString(cursor.getColumnIndexOrThrow("pz_codigo"))
                        txtFecha.text =
                            cursor.getString(cursor.getColumnIndexOrThrow("pe_fechaing"))
                        txtObservacion.text =
                            cursor.getString(cursor.getColumnIndexOrThrow("pe_descripcion"))
                        descuento =
                            cursor.getString(cursor.getColumnIndexOrThrow("pe_porcentdescuento"))
                        vgsEstado = cursor.getString(cursor.getColumnIndexOrThrow("pe_estado"))

                        llenarControles.selectItemInSpinner(
                            spinnerPrioridad,
                            cursor.getString(cursor.getColumnIndexOrThrow("pr_codigo"))
                        )
                        llenarControles.selectItemInSpinner(
                            spinnerTipoPed,
                            cursor.getString(cursor.getColumnIndexOrThrow("im_codigo"))
                        )
                        llenarControles.selectItemInSpinner(
                            spinnerFormaPag,
                            cursor.getString(cursor.getColumnIndexOrThrow("tp_codigo"))
                        )
                        llenarControles.selectItemInSpinner(
                            spinnerItem,
                            cursor.getString(cursor.getColumnIndexOrThrow("td_codigo"))
                        )

                    } catch (e: Exception) {
                        Log.e("ConsultarPedido", "Error en la cabecera: ${e.localizedMessage}")
                    }
                }
            },
            actualizarDetalles = { detalles ->
                requireActivity().runOnUiThread {
                    //Log.d("ConsultarPedido", "Detalles recibidos: ${detalles.size}")
                    if (detalles.isNotEmpty()) {

                        adaptadorDetalle = MiAdapterDetalle(todosLosItemsDet, this,
                            itemClickListener = this,
                            doubleClickListener = { item, position ->
                                // Cargar los valores del ítem seleccionado en los campos de entrada
                                txtCantidad.setText(item.Cantidad)
                                txtPrecio.setText(item.Precio)
                                txtDesc.setText(item.DescItem)
                                txtReferencia.setText(
                                    llenarControles.fnObtenerReferenciaPorCodigo(
                                        item.Codigo
                                    )
                                )
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
                        Log.d(
                            "ConsultarPedido",
                            "No se recibieron detalles para el documento: $codDocumento"
                        )
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

        if (valInser == 0) {
            //obtener el cliente - transporte y politica
            val clienteNombre = llenarControles.fnObtenerNombreCliente(clCodigo)
            val transporteNombre = llenarControles.obtenerNombreTransporte(trCodigo)
            tr_codigo = trCodigo
            cl_codigo = clCodigo
            txtCliente.setText(clienteNombre)
            txtTransporte.setText(transporteNombre)
            txtDesc.text = descuento

            val politica = llenarControles.fnObtenerPolitica(clCodigo)
            if (politica != null) {
                txtPolitica.text = politica.descripcion
                pz_codigo = politica.codigo
                pz_codigo = plazo
                fnBuscarProvinciaCliente()
            }
            fnActivarControles()
            spinnerItem.isEnabled = false
            fnPolitica(cl_codigo.toInt())
        }
        fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())
        vliExito = 1

        if (vgsEstado == "C") {
            fnDesactivarControles()

        }

    }

    fun fnValidarRegalo() {
        if (spinnerListado.selectedItemPosition == 2) {
            if (itemEnEdicion == null) {

                if (txtLote.text.toString().toDouble() >= margen) {
                    fnValidarYAgregarDetalles()
                } else {
                    showToast("El margen debe ser mayor o igual a $margen")
                }
            } else {
                fnValidarYAgregarDetalles()
            }
        } else {
            fnValidarYAgregarDetalles()
        }
    }

    fun fnValidarYAgregarDetalles() {


        // Acciones específicas si se pasan todas las validaciones
        /*if (im_codigo.toInt() == 14) {
            fnAgregarDetallesCombo()
        } else {
            fnAgregarDetalles()
            if (td_codigo == "2"){
                txtDesc.setText("")
            }
        }*/

        var combo = "0"
        var selectedItems = (ReciclerviewRef.adapter as? MiAdaptadorRef)?.getSelectedItems()

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
                if (combo.toInt() == 0) listOf(
                    "cantidad" to txtCantidad,
                    "precio" to txtPrecio
                ) else emptyList()

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
        }

    }

    private fun fnMostrarDialogoDeConfirmacion() {
        when (vgsOpcionMenu) {
            "I" -> {
                mostrarDialogo("¿Deseas guardar los datos?", ::fnGuardadoautomatico)
                fnGuardarPolitica()
            }

            "M" -> {
                mostrarDialogo("¿Deseas actualizar los datos?", ::fnGuardadoautomatico)
                fnGuardarPolitica()
            }

            "E" -> {
                val documentoId = txtNumero.text.toString().toIntOrNull()
                if (documentoId != null) {
                    mostrarDialogo(
                        "¿Desea eliminar los datos?",
                        { llenarControles.fnEliminarDocumentoLogicamente(documentoId) })
                    vgsOpcionMenu = "M"
                } else {
                    showToast("Número de documento no válido.")
                }
            }
        }
    }

    private fun fnGuardarPolitica() {
        val nonNullLopd = lopd
        val nonNullCampania = campania
        if (nonNullLopd != null && nonNullCampania != null) {
            llenarControles.fnInsertarPolitica(
                nonNullLopd,
                nonNullCampania,
                usuario,
                cl_codigo.toInt()
            )
        }
    }

    private fun mostrarDialogo(mensaje: String, accion: () -> Unit) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Sistema")
            setMessage(mensaje)
            setPositiveButton("Sí") { dialog, which ->
                accion()
                if (vliGuardar == 1) {
                    fnDesactivarControles()

                    if (estado == "C") {
                        // btnEliminar.isEnabled = true
                        btnImprimir.isEnabled = true
                    } else {
                        btnEliminar.isEnabled = true
                        btnEnviar.isEnabled = true
                        btnImprimir.isEnabled = true
                    }

                    showToast("pedido guardado correctamente")
                } else {

                }
            }
            setNegativeButton("No") { dialog, which -> dialog.dismiss() }
            create().show()
        }
    }

    private fun fnTransporte(): String? {
        var finalTarifa: String? = null
        var tarifaP: Double = 0.00
        try {
            val resultados =
                llenarControles.fnTransporteTarifa(tr_codigo.toInt(), cl_codigo.toInt())
            for (tarifa in resultados) {
                finalTarifa = if (unidadCE >= 0 && unidadCE < tarifa.peso) {
                    tarifaP = tarifa.tarifa1
                    tarifa.tarifa1.toString()

                } else {
                    tarifaP = tarifa.tarifa2
                    (unidadCE * tarifa.tarifa2).toString()
                }

                Tarifa = String.format("$%.2f", tarifaP)
                Cobertura = tarifa.descripcion

                //txtFlete.post { txtFlete.text = finalTarifa }

            }
        } catch (e: Exception) {
            // Manejar la excepción
            e.printStackTrace()
            // Retorna un valor por defecto o maneja el caso de error adecuadamente
            finalTarifa = "0"
        }
        return finalTarifa
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
            val tituloDialogo = referencia ?: "Detalles del Ítem"


            // Asigna valores de stock comenzando desde el segundo valor en el array de stock
            primerItem?.let {
                val stock = llenarControles.fnObtenerStock(referencia, codigo)

                // Empieza a asignar desde el segundo valor (índice 1 en la lista)
                stock.drop(1).forEachIndexed { index, valor ->
                    val textViewId = resources.getIdentifier(
                        "valueItem${index + 1}",
                        "id",
                        requireActivity().packageName
                    )
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

    fun fnactualizarDescuento(descuentoTexto: String) {
        // Convierte el texto a BigDecimal, o utiliza 0 si el texto está vacío o no es válido
        val descuento = descuentoTexto.toBigDecimalOrNull() ?: BigDecimal.ZERO

        for (item in todosLosItemsDet) {
            item.DescItem = descuento.toString()
            item.lote = String.format(
                "%.2f", (
                        item.Precio.toBigDecimal() *
                                (BigDecimal.ONE - item.DescItem.toBigDecimal()
                                    .divide(BigDecimal(100)))
                                / item.costProm.toBigDecimal())
            )

            if (item.DescItem.toBigDecimal() > BigDecimal.ZERO) {
                if (item.regalo > "0") {
                    item.ConDesc = "0.00"
                } else {
                    item.ConDesc =
                        (item.Subtotal.toDouble() - (item.Subtotal.toDouble() * item.DescItem.toDouble() / 100))
                            .toBigDecimal()
                            .setScale(2, RoundingMode.HALF_UP)
                            .toString()
                }
            } else {
                item.ConDesc = "0.00"

            }
        }

        // Notifica al adaptador que los datos han cambiado
        adaptadorDetalle.notifyDataSetChanged()
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
                    val aceptaPolitica =
                        if (rgPolitica.checkedRadioButtonId == R.id.rbSiPolitica) 1 else 0
                    val aceptaOfertas =
                        if (rgOfertas.checkedRadioButtonId == R.id.rbSiOfertas) 1 else 0

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
        val downloadTask = object : DownloadImageTask(
            imageView, imageCache,
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

                if (drawable == null || drawable !is BitmapDrawable) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "No hay imagen para guardar", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val bitmap = drawable.bitmap
                val fechaActual = SimpleDateFormat("_ddMMyyyy_HHmmss", Locale.getDefault()).format(Date())

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "${codigo}${fechaActual}.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = requireActivity().contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                if (uri != null) {
                    resolver.openOutputStream(uri).use { out ->
                        if (out != null) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                    }

                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)

                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Imagen guardada", Toast.LENGTH_LONG).show()
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "No se pudo guardar la imagen", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }


    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            val scaleFactor = scaleGestureDetector.scaleFactor
            val newScale = scaleFactor * currentMatrix.scaleX()
            if (newScale in minScale..maxScale) {
                matrix.postScale(
                    scaleFactor,
                    scaleFactor,
                    scaleGestureDetector.focusX,
                    scaleGestureDetector.focusY
                )
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
                override fun checkClientTrusted(
                    chain: Array<java.security.cert.X509Certificate>,
                    authType: String
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<java.security.cert.X509Certificate>,
                    authType: String
                ) {
                }

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


    // Método para abrir la galería
    private fun fnOpenImagePicker() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
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
    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream) // Calidad al 80%
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

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
        val orientation =
            exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun fnEnviarImagenes(codigo: String, order: String, pdfBase64: String, context: Context) {
        val nombrePdf = "${codigo}_${order}"
        val ClsRegImagenOrden = ClsRegImagenOrden(context)


        try {
            ClsRegImagenOrden.enviarDatos(
                nombrePdf,
                pdfBase64,
                onSuccess = { mensaje ->

                },
                onError = { error ->


                },
                onNoResponse = {

                }
            )
        } catch (e: Exception) {
            //Toast.makeText(context, "Error al intentar enviar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fnHistorial(cl_codigo: String, item: String) {
        val dialog = hisotrial(cl_codigo, item)
        dialog.setTargetFragment(this, 0) // Configura el Fragment receptor directamente
        dialog.show(parentFragmentManager, "consultaItemsTag")
    }


    private fun showProgressDialogSugerenciaPedido(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }


    private fun fnSugerenciasPedidos() {
        val pedido = txtNumero.text.toString()
        if (pedido.isBlank()) {
            showToast("Debe tener un pedido seleccionado")
            return
        }
        if (cl_codigo.isBlank()) {
            showToast("Debe seleccionar un cliente")
            return
        }

        val progressDialog = showProgressDialogSugerenciaPedido()

        try {
            val productosDelPedido: List<ProductoDisponible> = llenarControles
                .obtenerProductosDelPedido(pedido)
                .map { item ->
                    ProductoDisponible(
                        itCodigo = item.itCodigo,
                        referencia = item.referencia,
                        descripcion = item.descripcion,
                        stock = item.subtotal,
                        precio = item.subtotal,
                        costoPromedio = item.costoPromedio
                    )
                }

            val historial: List<VentaHistorial> = llenarControles.obtenerHistorialVentas(cl_codigo)
            val clienteIdHistorial = historial.firstOrNull()?.itCodigo.orEmpty()
            val historialVentasCliente = HistorialVenta(
                clienteId = clienteIdHistorial,
                ventas = historial.map { venta ->
                    ProductoDisponible(
                        itCodigo = venta.itCodigo,
                        referencia = venta.referencia,
                        descripcion = venta.descripcion,
                        stock = venta.cantidadVendida,
                        precio = venta.precioVenta,
                        costoPromedio = 100.0
                    )
                }
            )

            val productosDisponibles = llenarControles.obtenerProductosDisponibles()
            val recommendationsDialog = RecommendationsDialog(requireContext(), viewLifecycleOwner)

            recommendationsDialog.showRecommendationsDialog(
                Pedido(pedido, productosDelPedido),
                historialVentasCliente,
                productosDisponibles,
                onFinished = { progressDialog.dismiss() }
            )
        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.e("fnSugerenciasPedidos", "Error cargando recomendaciones", e)
            showToast("No se pudieron cargar las sugerencias")
        }
    }

    fun fnSugerencias() {
        val peDoc = txtNumero.text.toString()

        val dlg = CombosDialogFragment.newInstance(peDoc, cl_codigo)
        dlg.onComboSelected = { cb_codigo, cbNombre ->
            fnAgregarDetallesComboSugerencia(cb_codigo)
        }
        dlg.onCerrar = {
            fnMostrarDialogoDeConfirmacion()
        }
        dlg.show(requireActivity().supportFragmentManager, "CombosDialogFragment")
    }



    fun fnAgregarDetallesComboSugerencia(cb_codigo: String) {
        // 1) Traer ítems del combo y evitar duplicados por Código
        val nuevos: List<datosDet> = llenarControles.fnDetallesCombos(cb_codigo)
            .distinctBy { it.Codigo }

        // 2) Índice rápido de lo que ya tienes en el detalle: CODIGO -> índice
        val indexByCode: Map<String, Int> = todosLosItemsDet
            .withIndex()
            .associate { it.value.Codigo to it.index }

        val indicesActualizados = mutableListOf<Int>()
        val porInsertar = mutableListOf<datosDet>()

        // 3) Actualizar existentes / acumular faltantes
        for (n in nuevos) {
            val idx = indexByCode[n.Codigo]
            if (idx != null) {
                // Ya existe -> actualiza los campos
                val e = todosLosItemsDet[idx]
                e.combo = cb_codigo
                e.sugerencia = 1
                indicesActualizados += idx
            } else {
                // No existe -> preparar para insertar
                n.combo = cb_codigo
                n.sugerencia = 0
                porInsertar += n
            }
        }

        // 4) Insertar de una sola vez y notificar al adapter
        if (porInsertar.isNotEmpty()) {
            val start = todosLosItemsDet.size
            todosLosItemsDet.addAll(porInsertar)
            adaptadorDetalle.notifyItemRangeInserted(start, porInsertar.size)
        }

        // 5) Notificar los ítems actualizados
        if (indicesActualizados.isNotEmpty()) {
            indicesActualizados.forEach { adaptadorDetalle.notifyItemChanged(it) }
        } else if (porInsertar.isEmpty()) {
            // Si no hubo cambios visibles, fuerza un refresco
            adaptadorDetalle.notifyDataSetChanged()
        }

        // 6) UI y totales
        ReciclerviewDet.layoutManager = LinearLayoutManager(requireContext())
        ReciclerviewDet.adapter = adaptadorDetalle
        fnCalcularTotales(adaptadorDetalle.fnObtenerTotal())

        vliExito = 1
        fnGuardadoautomatico()
    }

    private fun fnOpenPdf(file: File) {
        if (!file.exists()) {
            Toast.makeText(requireContext(), "El archivo no existe", Toast.LENGTH_LONG).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No hay una aplicación para abrir PDFs", Toast.LENGTH_LONG).show()
        }
    }

    private fun fnReferenciaItemParo(paro: String) {
        datosList.clear()
        adaptadorRef.clearItems()

        val resultados = llenarControles.fnBuscaReferenciaItemParo(paro)
        for (dato in resultados) {
            datosList.add(dato)
        }

        ReciclerviewRef.layoutManager = LinearLayoutManager(requireContext())
        ReciclerviewRef.adapter = adaptadorRef
    }

    fun fnBuscarProvinciaCliente() {
        provincia = llenarControles.fnObtenerProvincia(cl_codigo)

        val provinciasParo = listOf(6, 7, 8, 9, 10, 11, 12, 14, 15)

        if (provinciasParo.contains(provincia)) {
            llenarControles.fnLLenarSpinnerPlazoParo(spinnerPlazo, "fa_ws_plazo")
            llenarControles.selectItemInSpinner(spinnerPlazo, pz_codigo)
        } else {
            llenarControles.fnLLenarSpinnerPlazo(spinnerPlazo, "fa_ws_plazo")
            llenarControles.selectItemInSpinner(spinnerPlazo, pz_codigo)
        }
    }

    fun fnActualizarDatosCliente() {
        if (llenarControles.fnVerficarClienteActualizacion(cl_codigo)) {
            val cliente = llenarControles.fnDatosClienteActualizacion(cl_codigo)
            if (cliente != null) {
                mostrarDialogoCliente(cliente)
            }
        }
    }

    fun mostrarDialogoCliente(cliente: ClienteDatos) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.frm_actualizacion_cliente, null)

        val txtNombres = view.findViewById<EditText>(R.id.txtNombresAC)
        val txtDireccion = view.findViewById<EditText>(R.id.txtDireccionAC)
        val txtCelular = view.findViewById<EditText>(R.id.txtCelularAC)
        val txtEmail = view.findViewById<EditText>(R.id.txtEmailDA)

        txtNombres.setText(cliente.nombre)
        txtDireccion.setText(cliente.direccion)
        txtCelular.setText(cliente.fono)
        txtEmail.setText(cliente.email)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Datos del Cliente")
            .setView(view)
            .setCancelable(false)
            .create()

        dialog.setCanceledOnTouchOutside(false)

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Guardar") { _, _ -> }

        dialog.setOnShowListener {
            val btnGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            btnGuardar.setOnClickListener {
                val nombreNuevo = txtNombres.text.toString().trim()
                val direccionNueva = txtDireccion.text.toString().trim()
                val celularNuevo = txtCelular.text.toString().trim()
                val emailNuevo = txtEmail.text.toString().trim()

                if (
                    nombreNuevo.isEmpty() ||
                    direccionNueva.isEmpty() ||
                    celularNuevo.isEmpty() ||
                    emailNuevo.isEmpty()
                ) {
                    Toast.makeText(requireContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!fnValidarTelefonos(celularNuevo)) {
                    Toast.makeText(
                        requireContext(),
                        "Numero telefonico del Cliente no tiene el formato permitido",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                if (!fnValidarCorreo(emailNuevo)) {
                    Toast.makeText(
                        requireContext(),
                        "Email del Cliente no tiene el formato permitido",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                if (llenarControles.fnUpdateDatosCliente(cl_codigo, direccionNueva, celularNuevo, emailNuevo)) {
                    Toast.makeText(requireContext(), "Cliente actualizado correctamente", Toast.LENGTH_SHORT).show()
                    vgsActualiza = 1
                }

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    fun fnValidarTelefonos(input: String): Boolean {
        val telefonos = input
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (telefonos.isEmpty()) return false

        return telefonos.all { phone ->
            val numero = Regex("[\\s\\-\\(\\)]").replace(phone, "")
            val esCelular = Regex("^09\\d{8}$").matches(numero)

            esCelular &&
                    !fnTodosIguales(numero) &&
                    fnMaxRunIguales(numero) < 6 &&
                    !fnEsSecuenciaAscendente(numero) &&
                    !fnEsSecuenciaDescendente(numero)
        }
    }

    private fun fnTodosIguales(s: String): Boolean {
        return s.all { it == s.first() }
    }

    private fun fnMaxRunIguales(s: String): Int {
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
        var seq = 1

        for (i in 1 until s.length) {
            val prev = s[i - 1].digitToInt()
            val cur = s[i].digitToInt()
            val esperado = (prev + 1) % 10

            if (cur == esperado) {
                seq++
                if (seq >= 6) return true
            } else {
                seq = 1
            }
        }

        return false
    }

    private fun fnEsSecuenciaDescendente(s: String): Boolean {
        var seq = 1

        for (i in 1 until s.length) {
            val prev = s[i - 1].digitToInt()
            val cur = s[i].digitToInt()
            val esperado = (prev + 9) % 10

            if (cur == esperado) {
                seq++
                if (seq >= 6) return true
            } else {
                seq = 1
            }
        }

        return false
    }

    fun fnValidarCorreo(email: String): Boolean {
        if (email.isBlank()) return false

        val correos = email
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (correos.isEmpty()) return false

        val pattern = Regex(
            "^[A-Za-z0-9](?:[A-Za-z0-9._%+\\-]{1,63})@[A-Za-z0-9](?:[A-Za-z0-9\\-]{0,61}[A-Za-z0-9])(?:\\.[A-Za-z0-9](?:[A-Za-z0-9\\-]{0,61}[A-Za-z0-9]))+$"
        )

        val desechables = listOf(
            "mailinator.com",
            "yopmail.com",
            "10minutemail.com",
            "guerrillamail.com",
            "tempmail.com"
        )

        return correos.all { correo ->
            if (!pattern.matches(correo)) return@all false
            if (correo.contains("..")) return@all false

            val at = correo.lastIndexOf('@')
            if (at < 2) return@all false

            val domain = correo.substring(at + 1)
            val parts = domain.split(".")

            if (parts.size < 2) return@all false
            if (parts.last().length < 2) return@all false

            val esDesechable = desechables.any { d ->
                domain.equals(d, ignoreCase = true) ||
                        domain.endsWith(".$d", ignoreCase = true)
            }

            !esDesechable
        }
    }

    private inner class MiAsyncTaskActualizacionCliente(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {
            return try {
                database = dbHelper.writableDatabase

                val cadena = ClaseXmlCliente.obtenerXmlInformeIndividual(cl_codigo)

                solicitudSoap.initializeVariables(
                    getString(R.string.str_ActualizaCliente).toInt(),
                    cadena
                )

                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.bufferedReader()?.use { it.readText() }

                if (result.isNullOrBlank()) {
                    ""
                } else {
                    xmlClientesAct.parseAndUpdateDocumentCode(
                        result,
                        database,
                        requireContext()
                    )
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

            if (result != null) {
                showResultDialog(result)
            } else {
                Toast.makeText(requireContext(), errorMessage ?: "", Toast.LENGTH_LONG).show()
            }
        }

        private fun showResultDialog(pedido: String) {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle("Sistema")
            dialog.setMessage("Datos enviados correctamente")
            dialog.setPositiveButton("OK") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            dialog.show()
        }
    }




}

