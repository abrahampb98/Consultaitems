 package com.example.Consultaitems.ui.fragments


import android.Manifest
import com.example.Consultaitems.utils.cls.ClsRegImagen
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.ConnectivityManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.UnitValue
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import android.content.res.Configuration
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.Consultaitems.R
import com.example.Consultaitems.utils.parser.XMlParserRecibo
import com.example.Consultaitems.utils.parser.XmlInforme
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.adapters.AdaptadorRecibo
import com.example.Consultaitems.ui.adapters.ClienteAdapter
import com.example.Consultaitems.ui.adapters.Recibo
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.RecibosDialogFragment
import com.example.Consultaitems.utils.cls.RegistrarCoordenadas
import com.example.Consultaitems.utils.cls.SpinnerItem
import com.example.Consultaitems.utils.cls.consultaItems
import java.math.BigDecimal


 class frmReciboDigital: Fragment(), AdaptadorRecibo.OnItemClickListener, consultaItems.OnItemSelectedListener {
    companion object {
        const val PICK_IMAGE_REQUEST = 1
        private val CAMERA_REQUEST = 2

    }

    private var ep_codigo: String = ""
    private var ba_codigo: Int = 0
    private var vgsOpcionMenu = ""
    lateinit var solicitudSoap: SolicitudSoap
    lateinit var dbHelper: SqLiteOpenHelper
    private val imageViews = mutableListOf<ImageView>()
    private var selectedImageViewIndex: Int = -1
    private var imageChanged = mutableListOf<Boolean>()
    private val originalBitmaps = mutableListOf<Bitmap?>()
    private var enlace: String = ""
    private val imageUris = mutableListOf<Uri?>()
    private var response: String = ""
    var isDatePickerShown = false
    private lateinit var txtFechaFactura: EditText
    private lateinit var txtNumeroRD: EditText
    private lateinit var txtFechaRD: EditText
    private lateinit var txtClienteRD: AutoCompleteTextView
    private lateinit var spTransaccion: Spinner
    private lateinit var spBanco: Spinner
    private lateinit var txtDocRD: EditText
    private lateinit var txtCtaRD: EditText
    private lateinit var txtValorRD: EditText
    private lateinit var txtConceptoRD: EditText
    private lateinit var txtObservacionRD: EditText
    private lateinit var txtTotalGenRD: TextView
    private lateinit var btnEliminar: Button
    private lateinit var btnEnviar: Button
    private lateinit var btnGuardar: Button
    private lateinit var btnAgregar: Button
    private lateinit var btnBuscar: Button
    private lateinit var btnImprimir: Button
    private lateinit var btnEnlaceRD: Button
    private lateinit var btnLimpiarRD: Button
    private lateinit var btnCriteriosRD: Button
    private lateinit var recyclerRD: RecyclerView
    val todosLosItemsDet = mutableListOf<Recibo>()
    private lateinit var adaptadorDetalle: AdaptadorRecibo
    private lateinit var llenarControles: ClsLLenarControles
    private var cl_codigo: String = ""
    private var posicion: Int? = null
    private var itemEnEdicion: Int? = null
    private var tr_codigo: String = ""
    private var codRecibo: String = ""
    private var bc_codigo : Int? = null
    private var vliGuardar: Int = 0
    private lateinit var ClaseXml: XmlInforme
    private var usuario: String = ""
    private var estado: String = ""
     private  var cuenta: String = ""

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
         val view = inflater.inflate(R.layout.frm_recibo_digital, container, false)

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


        //declaracion de variables
        llenarControles = ClsLLenarControles(requireContext())
        txtFechaFactura = view.findViewById(R.id.txtFechaTransaccionRD)
        txtNumeroRD = view.findViewById(R.id.txtNumeroRD)
        txtFechaRD = view.findViewById(R.id.txtFechaRD)
        txtClienteRD = view.findViewById(R.id.txtClienteRD)
        spTransaccion = view.findViewById(R.id.spTransaccion)
        spBanco = view.findViewById(R.id.spBanco)
        txtDocRD = view.findViewById(R.id.txtDocRD)
        txtCtaRD = view.findViewById(R.id.txtCtaRD)
        txtValorRD = view.findViewById(R.id.txtValorRD)
        txtConceptoRD = view.findViewById(R.id.txtConceptoRD)
        txtObservacionRD = view.findViewById(R.id.txtObservacionRD)
        txtTotalGenRD = view.findViewById(R.id.txtTotalGenRD)
        btnEliminar = view.findViewById(R.id.btnEliminarDocRD)
        btnGuardar = view.findViewById(R.id.btnGuardarRD)
        btnEnviar = view.findViewById(R.id.btnEnviarRD)
        btnBuscar = view.findViewById(R.id.btnBuscarRD)
        btnImprimir = view.findViewById(R.id.btnImprimirRD)
        btnAgregar = view.findViewById(R.id.btnAgregarRD)
        btnEnlaceRD = view.findViewById(R.id.btnEnlaceRD)
        btnCriteriosRD = view.findViewById(R.id.btnCriteriosRD)
        btnLimpiarRD = view.findViewById(R.id.btnLimpiarRD)
        recyclerRD = view.findViewById(R.id.recyclerRD)
        dbHelper = SqLiteOpenHelper(requireContext())

        adaptadorDetalle = AdaptadorRecibo(
            activity = this,
            datos = todosLosItemsDet,
            itemClickListener = this,  // Asumiendo que `this` implementa la interfaz `OnItemClickListener`
            doubleClickListener = { item, position ->

                // Llena los campos de edición con los datos del ítem seleccionado


                txtDocRD.setText(item.Doc)
                txtCtaRD.setText(item.Cuenta)
                txtFechaFactura.setText(item.Fecha)
                txtValorRD.setText(item.Valor)
                txtConceptoRD.setText(item.Concepto)
                txtObservacionRD.setText(item.Observacion)
                fnSelectItemInSpinner(spTransaccion,item.tr_codigo.toString())
                fnSelectItemInSpinner(spBanco,item.bc_codigo.toString())
                // Configurar para actualizar el ítem
                itemEnEdicion = position
            }
        )



        // Inicializa la lista de ImageView
        imageViews.add(view.findViewById(R.id.imageViewPreview1))
        imageViews.add(view.findViewById(R.id.imageViewPreview2))
        imageViews.add(view.findViewById(R.id.imageViewPreview3))
        imageViews.add(view.findViewById(R.id.imageViewPreview4))


        for (imageView in imageViews) {
            imageChanged.add(false) // Inicialmente, ninguna imagen ha sido cambiada
            originalBitmaps.add(null)
            imageUris.add(null)
        }


        for ((index, imageView) in imageViews.withIndex()) {
            imageView.setOnClickListener {
                selectedImageViewIndex = index
                fnOpenImagePicker()
            }
        }


        txtFechaFactura.setOnClickListener {
            if (!isDatePickerShown) {
                fnOcultarTeclado()
                showDatePickerDialog(txtFechaFactura)
            }
        }

        btnAgregar.setOnClickListener {
            fnAgregarDetalles()
        }

        btnGuardar.setOnClickListener {
            fnMostrarDialogoDeConfirmacion()
        }

        btnEnviar.setOnClickListener{
            fnEnviarGestion(codRecibo,ep_codigo)
        }

        btnEliminar.setOnClickListener {
            vgsOpcionMenu = "E"
            fnMostrarDialogoDeConfirmacion()
        }

        btnBuscar.setOnClickListener {
          fnConsultar()
        }
        btnImprimir.setOnClickListener {
            fnImprimir()
        }

        btnEnlaceRD.setOnClickListener {
            fnAbrirEnlace()
        }

        btnCriteriosRD.setOnClickListener {
            fnBuscarFacturas()
        }

        btnLimpiarRD.setOnClickListener {
            txtConceptoRD.text.clear()
        }

        //cargar funciones por defecto
        fnDeshabilitarControles()
        fnAdapterCliente()
        fnLLenarSpiner()
        fnObtenerVendedor()

        // Configurar el TextWatcher para txtClienteRD
        txtClienteRD.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No necesitas hacer nada aquí
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No necesitas hacer nada aquí
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    // Limpiar el campo txtClienteRD
                    txtCtaRD.text.clear()
                    spTransaccion.setSelection(0)
                                       // Vaciar el Spinner spBanco llamando a fnLLenarSpinnerBanco con un id inválido
                    llenarControles.fnLLenarSpinnerBanco(spBanco, "-1") //
                }
            }
        })


        spBanco.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position) as SpinnerItem
                if (tr_codigo == "12" || tr_codigo == "14" || tr_codigo == "48") { //valida el codigo de transaccion se cheques
                    bc_codigo = item.codigo.toInt()
                    ba_codigo = llenarControles.fnObtenerCodigoBanco(item.codigo.toInt())

                    val descripcionCompleta = item.descripcion


                    // Extraer solo el número de cuenta de la descripción completa
                    val partes = descripcionCompleta.split(" - ")
                    if (partes.size > 1) {
                        val numeroCuenta = partes[1]

                        txtCtaRD.setText(numeroCuenta)
                    } else {
                        txtCtaRD.setText("") // Si no hay número de cuenta, limpia el campo
                    }

                }else{
                    bc_codigo = 0
                    ba_codigo = 0
                    txtCtaRD.setText("0")
                }




            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Opcionalmente manejar la no selección
            }
        }


        spTransaccion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position) as SpinnerItem
                tr_codigo = item.codigo


                if (spTransaccion.selectedItemPosition == 4) {
                    txtDocRD.setText("0")
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Opcionalmente manejar la no selección
            }
        }
         requireActivity().window.decorView.clearFocus()

         return view
     }

     override fun onItemClick(item: Recibo, position: Int) {
         posicion = position
     }


     // Inflar el menú en el fragmento
     override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
         inflater.inflate(R.menu.frm_menu_pedido, menu)
         super.onCreateOptionsMenu(menu, inflater)
     }


     override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bntNuevoPv -> {
                fnAccionesAlPulsarNuevo()
                estado = "A"
                true
            }
            R.id.btnModificarPv -> {
                fnAccionesAlPulsarConsultar()
                true
            }

            R.id.btnPedidoPv ->{
                fnAccionesAlPulsarModificar()
                fnMostrarRecibos()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

     override fun onItemsSelected(facturas: List<String>, saldos: List<BigDecimal>) {
         val textoActual = txtConceptoRD.text.toString()

         val facturasExistentes = textoActual.split(", ").toSet() // Convierte las facturas ya agregadas en un conjunto

         val nuevasFacturas = mutableListOf<String>()

         // Si la lista de saldos está vacía, significa que el criterio es "Criterios"
         if (saldos.isEmpty()) {
             facturas.forEach { factura ->
                 if (!facturasExistentes.contains(factura)) {
                     nuevasFacturas.add(factura)
                 }
             }
         } else {
             // Modo "Facturas" con saldo
             facturas.zip(saldos).forEach { (factura) ->
                 if (!facturasExistentes.contains(factura)) {
                     nuevasFacturas.add(factura)

                 }
             }
         }

         // Si no hay nuevas facturas, no actualizar nada
         if (nuevasFacturas.isEmpty()) {
             //println("No se agregaron nuevas facturas.")
             return
         }

         // Actualiza `txtConceptoRD` con las nuevas facturas
         val nuevoTexto = if (textoActual.isNotEmpty()) "$textoActual, ${nuevasFacturas.joinToString(", ")}"
         else nuevasFacturas.joinToString(", ")

         txtConceptoRD.setText(nuevoTexto)

         fnOcultarTeclado()
     }




     fun fnAccionesAlPulsarNuevo(){
        //txtNumero
        fnLimpiarControles()
        fnHabilitarControles()
        txtClienteRD.requestFocus()
        vgsOpcionMenu = "I"
        btnGuardar.text = "Guardar"
        codRecibo = llenarControles.fnObtenerMaxioRecibo().toString()
        txtNumeroRD.setText(codRecibo)
        txtNumeroRD.isEnabled = false
        btnBuscar.isEnabled = false
        btnEnviar.isEnabled = false
        txtFechaRD.setText(fnFecha())
        itemEnEdicion = null  // Resetear la variable de edición
        enlace = "A"

    }

     fun fnAccionesAlPulsarModificar(){
         fnLimpiarControles()
         fnDeshabilitarControles()
         btnEnviar.isEnabled = true
         vgsOpcionMenu = "M"
         btnGuardar.text = "Modificar"
         fnLimpiarImageViews()
         itemEnEdicion = null
     }

    fun fnAccionesAlPulsarConsultar(){
        fnLimpiarControles()
        vgsOpcionMenu = "M"
        btnGuardar.text = "Modificar"
        fnDeshabilitarControles()
        txtNumeroRD.isEnabled = true
        txtNumeroRD.requestFocus()
        btnBuscar.isEnabled = true
    }

    private fun fnLimpiarControles(){
         txtFechaFactura.text.clear()
         txtNumeroRD.text.clear()
         txtFechaRD.text.clear()
         txtClienteRD.text.clear()
         spTransaccion.setSelection(0)
         spBanco.setSelection(0)
         txtDocRD.text.clear()
         txtCtaRD.text.clear()
         txtValorRD.text.clear()
         txtConceptoRD.text.clear()
         txtObservacionRD.text.clear()
        txtTotalGenRD.setText("")
        btnEnlaceRD.text = "No"
        adaptadorDetalle.clearItems()
        fnLimpiarImageViews()
    }

     private fun fnConsultar(){
         val textoIngresado = txtNumeroRD.text.toString()
         if (textoIngresado.isNotEmpty()) {
             fnConsultarRecibos(txtNumeroRD.text.toString().toInt())
         } else {
             showToast("Ingrese un documento para buscar")
         }
     }

     private fun fnImprimir(){
         val file = frmReporteRecibo.generatePdf(
             requireContext(),
             txtNumeroRD.text.toString().toInt()
         )
         fnOpenPdf(file)
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


     private fun fnAbrirEnlace(){
         val url = enlace  // Reemplaza con la URL que desees abrir
         val intent = Intent(Intent.ACTION_VIEW)
         intent.data = Uri.parse(url)
         startActivity(intent)
     }


    private fun fnHabilitarControles(){
        txtFechaFactura.isEnabled = true
        txtClienteRD.isEnabled = true
        spTransaccion.isEnabled = true
        spBanco.isEnabled = true
        txtDocRD.isEnabled = true
        txtCtaRD.isEnabled = true
        txtValorRD.isEnabled = true
        txtConceptoRD.isEnabled = true
        txtObservacionRD.isEnabled = true
        btnGuardar.isEnabled = true
        btnAgregar.isEnabled = true
        txtTotalGenRD.isEnabled = true
        btnCriteriosRD.isEnabled = true
        btnLimpiarRD.isEnabled = true
        fnActivarImageViews()
    }

    private fun fnDeshabilitarControles(){
        txtFechaFactura.isEnabled = false
        txtNumeroRD.isEnabled = false
        txtFechaRD.isEnabled = false
        txtClienteRD.isEnabled = false
        spTransaccion.isEnabled = false
        spBanco.isEnabled = false
        txtDocRD.isEnabled = false
        txtCtaRD.isEnabled = false
        txtValorRD.isEnabled = false
        txtConceptoRD.isEnabled = false
        txtObservacionRD.isEnabled = false
        btnEliminar.isEnabled = false
        btnEnviar.isEnabled = false
        btnGuardar.isEnabled = false
        btnAgregar.isEnabled = false
        btnBuscar.isEnabled = false
        btnImprimir.isEnabled = false
        txtTotalGenRD.isEnabled = false
        btnEnlaceRD.isEnabled = false
        btnCriteriosRD.isEnabled = false
        btnLimpiarRD.isEnabled = false
        fnDesactivarImageViews()
    }

    private fun fnDesactivarImageViews() {
        for (imageView in imageViews) {
            imageView.isEnabled = false
        }
    }

    private fun fnActivarImageViews() {
        for (imageView in imageViews) {
            imageView.isEnabled = true
        }
    }

    private fun fnFecha(): String {
        val fechaActual = Date()
        // Formatear la fecha como string
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaFormateada = dateFormat.format(fechaActual)
        return fechaFormateada

    }

    private fun fnLimpiarImageViews() {
        val placeholderImage = requireActivity().getDrawable(R.mipmap.ic_subir_imagen)
        for (i in imageViews.indices) {
            imageViews[i].setImageDrawable(placeholderImage)
            imageChanged[i] = false
            originalBitmaps[i] = null
        }
    }


     private fun fnAgregarDetalles() {
         if (adaptadorDetalle.itemCount >= 10) {
             showToast("Genere otro recibo")
         } else {
             if (fnVerificarControles()) {
                 val nuevoItem = Recibo(
                     spTransaccion.selectedItem.toString(),
                     spBanco.selectedItem.toString().split(" - ")[0].trimEnd(),
                     txtDocRD.text.toString(),
                     txtCtaRD.text.toString().ifEmpty { "0" },
                     txtFechaFactura.text.toString(),
                     txtValorRD.text.toString(),
                     txtConceptoRD.text.toString(),
                     txtObservacionRD.text.toString(),
                     ba_codigo,
                     tr_codigo.toInt(),
                     bc_codigo.toString().toInt()
                 )

                 itemEnEdicion?.let { indiceEdicion ->
                     // Si estamos en modo de edición
                     if (!fnVerificarDuplicados(nuevoItem, indiceEdicion)) {
                         todosLosItemsDet[indiceEdicion] = nuevoItem
                         itemEnEdicion = null  // Resetear la variable de edición
                     } else {
                         showToast("El documento ya ha sido agregado")
                     }
                 } ?: run {
                     // Si no estamos en modo de edición (es decir, estamos agregando un nuevo ítem)
                     if (!fnVerificarDuplicados(nuevoItem)) {
                         todosLosItemsDet.add(nuevoItem)
                     } else {
                         showToast("El documento ya ha sido agregado")
                     }
                 }

                 recyclerRD.adapter = adaptadorDetalle
                 recyclerRD.layoutManager = LinearLayoutManager(requireContext())

                 // Notificar al adaptador que los datos han cambiado
                 adaptadorDetalle.notifyDataSetChanged()

                 // Limpieza de campos
                 txtDocRD.text.clear()
                 txtFechaFactura.text.clear()
                 txtValorRD.text.clear()
                 txtObservacionRD.text.clear()

                 fnCalcularTotales()
                 fnOcultarTeclado()
                 fnGuardadoAutomaticoCobranza()
             }
         }
     }

     fun fnVerificarDuplicados(nuevoItem: Recibo, indiceExcluido: Int? = null): Boolean {
         return todosLosItemsDet.withIndex().any { (index, existingItem) ->
             // Si estamos editando, excluimos el ítem en la posición de edición
             if (index == indiceExcluido) {
                 false
             } else {
                 existingItem.Doc == nuevoItem.Doc
             }
         }
     }

     fun fnCalcularTotales(){
         txtTotalGenRD.text = String.format("%.2f", adaptadorDetalle.fnSumatoriaValor())
     }

     private fun fnAdapterCliente(){
        //txtcliente
        val adaptercliente = ClienteAdapter(requireContext(), llenarControles.fnCargarClientes())
        txtClienteRD.setAdapter(adaptercliente)
        txtClienteRD.setOnItemClickListener { _, _, position, _ ->
            val cliente = adaptercliente.getItem(position)
            if (cliente != null) {
                txtClienteRD.setText(cliente.nombre, false)  // Configura el nombre del cliente seleccionado en el AutoCompleteTextView
                cl_codigo = cliente.id  // Guarda el ID del cliente en cl_codigo

                llenarControles.fnLLenarSpinnerBanco(spBanco, cl_codigo)
            }
        }
    }



    private fun fnLLenarSpiner(){
        llenarControles.fnLLenarSpinnerTransaccion(spTransaccion, "cc_ws_transaccionesA")
    }


     fun fnVerificarControles(): Boolean {
         val campoInvalido = when {
             txtClienteRD.text.isNullOrEmpty() -> {
                 txtClienteRD.requestFocus()
                 "Ingrese el cliente"
             }
             spTransaccion.selectedItemPosition == 0 -> {
                 "Seleccione la transacción"
             }
             (tr_codigo == "12" || tr_codigo == "14" || tr_codigo == "48") && spBanco.selectedItemPosition == 0 -> {
                 "Seleccione el banco"
             }
             txtDocRD.text.isNullOrEmpty() -> {
                 txtDocRD.requestFocus()
                 "Ingrese el documento"
             }
             (tr_codigo == "12" || tr_codigo == "14") && txtCtaRD.text.isNullOrEmpty() -> {
                 txtCtaRD.requestFocus()
                 "Ingrese la cuenta"
             }
             txtFechaFactura.text.isNullOrEmpty() -> {
                 txtFechaFactura.requestFocus()
                 "Ingrese la fecha de la transacción"
             }
             txtValorRD.text.isNullOrEmpty() -> {
                 txtValorRD.requestFocus()
                 "Ingrese el valor"
             }
             txtConceptoRD.text.isNullOrEmpty() -> {
                 txtConceptoRD.requestFocus()
                 "Ingrese el concepto"
             }
             else -> null
         }

         return if (campoInvalido != null) {
             showToast(campoInvalido)
             false
         } else {
             true
         }
     }



     fun fnMostrarRecibos() {
        val dialog = RecibosDialogFragment()
        var recibo: Int = 0
        dialog.onPedidoSelected = { numeroRecibo ->
            requireActivity().runOnUiThread {
                recibo = numeroRecibo.toInt()
            }

            if (recibo != 0 ){
                fnConsultarRecibos(recibo)
            }
        }
        dialog.show(requireActivity().supportFragmentManager, "RecibosDialogFragment")

    }


    fun fnGuardadoAutomaticoCobranza() {
        if (adaptadorDetalle.itemCount > 0) {

            vliGuardar = 1

            // Datos para la tabla co_ws_reciboCobranzaConCab
            val cabValues = ContentValues().apply {
                put("em_codigo", 2)
                put("rc_codrecibo", txtNumeroRD.text.toString())
                put("ae_codigo", 11)
                put("ep_codigoBenef", cl_codigo)
                put("ep_codigoRes", ep_codigo)
                put("rc_fecharec", txtFechaRD.text.toString())
                put("rc_total", txtTotalGenRD.text.toString().toDouble())
                put("rc_tipo", 2)
                put("mo_codigo", 1)
                put("rc_estado", estado)
                put("rc_usuarioing", usuario)
                put("rc_fechaing", fnFecha())
                put("rc_usuariomod", ep_codigo)
                put("rc_fechamod", fnFecha())
            }

            // Obtén la instancia del Adapter para el detalle
             adaptadorDetalle = recyclerRD.adapter as AdaptadorRecibo

            // Datos para la tabla co_ws_reciboCobranzaConDet
            val detallesCobranza = adaptadorDetalle.datos.mapIndexed { index, detalle ->
                ContentValues().apply {
                    put("em_codigo", 2)
                    put("rc_codrecibo", txtNumeroRD.text.toString())
                    put("ae_codigo", 7)
                    put("tr_codigo", detalle.tr_codigo)
                    put("ba_codigo", detalle.ba_codigo)
                    put("bc_codigo", detalle.bc_codigo)
                    put("rd_documento", detalle.Doc)
                    put("rd_ncuenta", detalle.Cuenta)
                    put("rd_fecha", detalle.Fecha)
                    put("rd_concepto", detalle.Concepto)
                    put("rd_observacion", detalle.Observacion)
                    put("rd_valor", detalle.Valor.toDouble())
                    put("rd_chequeprot", "")
                    put("rd_estado", estado)
                    put("rd_usuarioing", ep_codigo)
                    put("rd_fechaing", fnFecha())
                    put("rd_fechamod", fnFecha())
                }
            }

           if (llenarControles.fnActualizarCobranzaCabeceraYDetalle(txtNumeroRD.text.toString(),cabValues, detallesCobranza)) {
                //Log.d("GuardadoAutomático", "Cobranza guardada con éxito")
            } else {
               //Log.e("GuardadoAutomático", "Fallo al guardar la cobranza")
            }

        } else {
            showToast("Ingrese líneas de detalle")
            vliGuardar = 0
        }

    }

    fun fnSelectItemInSpinner(spinner: Spinner, codigo: String) {
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


    private fun fnConsultarRecibos(codDocumento: Int){

            btnGuardar.isEnabled = true
            llenarControles.fnConsultarRecibos(codDocumento,
                actualizarCabecera = { cursor ->
                    requireActivity().runOnUiThread {
                        try {
                            txtNumeroRD.isEnabled = false
                            ep_codigo = cursor.getString(cursor.getColumnIndexOrThrow("ep_codigoRes"))
                            cl_codigo = cursor.getString(cursor.getColumnIndexOrThrow("ep_codigoBenef"))
                            txtFechaRD.text = Editable.Factory.getInstance().newEditable(cursor.getString(cursor.getColumnIndexOrThrow("rc_fecharec")))
                            txtTotalGenRD.text = String.format("%.2f", cursor.getString(cursor.getColumnIndexOrThrow("rc_total")).toDouble())
                            txtClienteRD.text = Editable.Factory.getInstance().newEditable(cursor.getString(cursor.getColumnIndexOrThrow("cl_nombre")))
                            txtNumeroRD.text = Editable.Factory.getInstance().newEditable(cursor.getString(cursor.getColumnIndexOrThrow("rc_codrecibo")))
                            codRecibo = cursor.getString(cursor.getColumnIndexOrThrow("rc_codrecibo"))
                            enlace = cursor.getString(cursor.getColumnIndexOrThrow("rc_enlace"))?:""
                            estado = cursor.getString(cursor.getColumnIndexOrThrow("rc_estado"))
                        } catch (e: Exception) {
                            //Log.e("ConsultarRecibos", "Error en la cabecera: ${e.localizedMessage}")
                        }
                    }
                },
                actualizarDetalles = { detalles ->
                    requireActivity().runOnUiThread {
                        //Log.d("ConsultarPedido", "Detalles recibidos: ${detalles.size}")
                        if (detalles.isNotEmpty()) {

                            adaptadorDetalle = AdaptadorRecibo(
                                activity = this,
                                datos = todosLosItemsDet,
                                itemClickListener = this,
                                doubleClickListener = { item, position ->

                                    txtDocRD.setText(item.Doc)
                                    txtCtaRD.setText(item.Cuenta)
                                    txtFechaFactura.setText(item.Fecha)
                                    txtValorRD.setText(item.Valor)
                                    txtConceptoRD.setText(item.Concepto)
                                    txtObservacionRD.setText(item.Observacion)
                                    fnSelectItemInSpinner(spTransaccion,item.tr_codigo.toString())
                                    fnSelectItemInSpinner(spBanco,item.bc_codigo.toString())
                                    itemEnEdicion = position
                                }
                            )
                            recyclerRD.adapter = adaptadorDetalle
                            recyclerRD.layoutManager = LinearLayoutManager(requireContext())
                            adaptadorDetalle.updateData(detalles)

                            fnHabilitarControles()

                        } else {
                            //Log.d("ConsultarRecibo", "No se recibieron detalles para el documento: $codDocumento")
                        }
                    }
                    llenarControles.fnLLenarSpinnerBanco(spBanco, cl_codigo)

                    if (enlace != "") {
                        btnEnlaceRD.isEnabled = true
                        btnEnlaceRD.text = "si"
                    }

                },
                onDocumentoNoEncontrado = {
                    txtNumeroRD.isEnabled = true
                    requireActivity().runOnUiThread {
                        showToast("Documento no encontrado")
                    }
                }
            )
        if (estado == "C"){
            fnDeshabilitarControles()
            btnImprimir.isEnabled = true
        }
    }




    private fun fnMostrarDialogoDeConfirmacion() {
        when (vgsOpcionMenu) {
            "I" -> {
                mostrarDialogo("¿Deseas guardar los datos?", ::fnGuardadoAutomaticoCobranza)
            }
            "M" -> {
                mostrarDialogo("¿Deseas actualizar los datos?", ::fnGuardadoAutomaticoCobranza)
            }
            "E" -> {
                val documentoId = txtNumeroRD.text.toString().toIntOrNull()
                if (documentoId != null) {
                    mostrarDialogo("¿Desea eliminar los datos?", { llenarControles.fnEliminarReciboLogicamente(documentoId) })
                    vgsOpcionMenu = "M"
                } else {
                    showToast("Número de documento no válido.")
                }
            }
        }
    }


    private fun mostrarDialogo(mensaje: String, accion: () -> Unit) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Sistema")
            setMessage(mensaje)
            setPositiveButton("Sí") { dialog, which ->
                accion()
                if (vliGuardar == 1 ){
                    fnDeshabilitarControles()

                    if (estado == "C")
                    {
                        btnEliminar.isEnabled = true
                        btnImprimir.isEnabled = true
                    }else{
                        btnEliminar.isEnabled = true
                        btnEnviar.isEnabled = true
                        btnImprimir.isEnabled = true
                    }

                    showToast("Recibo guardado correctamente")
                }else{

                }

            }
            setNegativeButton("No") { dialog, which -> dialog.dismiss() }
            create().show()
        }
    }


     @SuppressLint("SuspiciousIndentation")
     private fun fnEnviarGestion (gp_codigo: String, ep_codigo: String) {
         val builder = AlertDialog.Builder(requireContext())
         builder.setTitle("Confirmar Envío")
         builder.setMessage("¿Desea enviar el informe?")

         builder.setPositiveButton("Sí") { dialog, _ ->
             var pdfBase64 = fnCrearPdf(originalBitmaps, imageChanged, imageUris)

             if (pdfBase64 != null) {
                 btnEnviar.isEnabled = false
                     fnEnviarImagenes(gp_codigo, ep_codigo, pdfBase64, requireContext())
                     //fnEnviarInforme()
             } else {
                 // Mostrar `AlertDialog` para confirmar envío sin imágenes
                 fnValiadarEnvioimagenes {
                     fnEnviarInforme()
                 }
             }
             dialog.dismiss() // Cierra el diálogo después de la acción positiva
         }

         builder.setNegativeButton("No") { dialog, _ ->
             dialog.dismiss() // Cierra el diálogo sin hacer nada
         }

         builder.show() // Muestra el diálogo

     }



     private fun fnValiadarEnvioimagenes(onConfirm: () -> Unit) {
         val builder = AlertDialog.Builder(requireContext())
         builder.setMessage("No ha seleccionado imágenes. ¿Desea enviar sin imágenes?")
             .setPositiveButton("Sí") { dialog, _ ->
                 dialog.dismiss()
                 onConfirm()
             }
             .setNegativeButton("No") { dialog, _ ->
                 fnActivarImageViews()
                 dialog.dismiss()
             }
         val alert = builder.create()
         alert.show()
     }


     private var progressDialog: ProgressDialog? = null

     fun fnEnviarImagenes(rc_codrecibo: String, ep_codigo: String, pdfBase64: String, context: Context) {
         val nombrePdf = "${rc_codrecibo}_${ep_codigo}"
         enlace = "https://app.cotzul.com/sitenet/18/img/$nombrePdf.pdf"
         val clsRegImagen = ClsRegImagen(context)

         progressDialog = ProgressDialog(context).apply {
             setTitle("Sistema")
             setMessage("Enviando...")
             setCancelable(false)
             setProgressStyle(ProgressDialog.STYLE_SPINNER)
             show()
         }

         try {
             clsRegImagen.enviarDatos(
                 nombrePdf,
                 pdfBase64,
                 onSuccess = { mensaje ->
                     response = mensaje
                     llenarControles.fnInsertarGpEnlace(rc_codrecibo.toInt(), enlace)
                     fnCerrarProgressDialog()
                     fnEnviarInforme()
                 },
                 onError = { error ->
                     response = error
                     fnCerrarProgressDialog()
                     fnEnviarInforme()
                 },
                 onNoResponse = {
                     fnCerrarProgressDialog()
                     fnEnviarInforme()
                 }
             )
         } catch (e: Exception) {
             Toast.makeText(context, "Error al intentar enviar la imagen", Toast.LENGTH_SHORT).show()
             fnCerrarProgressDialog()
         }

     }


     fun fnCerrarProgressDialog() {
         progressDialog?.let {
             if (it.isShowing) {
                 it.dismiss()
             }
         }
         progressDialog = null // Liberar el ProgressDialog
     }


     fun fnEnviarInforme(){

         if (isNetworkAvailable(requireContext())) {
             try {
                 ClaseXml = XmlInforme(requireContext())
                 solicitudSoap = SolicitudSoap(requireContext())
                 MiAsyncTask().execute()
             } catch (e: Exception) {
                // Log.e("UpdateError", "Error during update: ${e.message}")
                 e.printStackTrace()
             }

         } else {
             showToast("Verifique su conexión a internet")
         }
     }

     fun isNetworkAvailable(context: Context): Boolean {
         val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
         val activeNetworkInfo = connectivityManager.activeNetworkInfo
         return activeNetworkInfo != null && activeNetworkInfo.isConnected
     }


     private inner class MiAsyncTask() :
         AsyncTask<Void, Void, String>() {

         private lateinit var database: SQLiteDatabase
         private var datosInsertados: String = ""

         override fun onPreExecute() {
             super.onPreExecute()
         }

         override fun doInBackground(vararg voids: Void): String? {
             return try {
                 database = dbHelper.writableDatabase
                 val cadena = ClaseXml.obtenerXmlInforme(codRecibo.toInt())
                 solicitudSoap.initializeVariables(getString(R.string.str_informe).toInt(), cadena)
                 var recibo: String = ""
                 val inputStream = solicitudSoap.realizarSolicitudSoap()
                 val result = inputStream?.bufferedReader()?.use { it.readText() }
                 if (!result.isNullOrBlank()) {
                     recibo = XMlParserRecibo.parseAndUpdateDocumentCode(
                         result,
                         database,
                         codRecibo,
                         requireContext()
                     )
                 }
                 recibo
             } catch (e: Exception) {
                 //Log.e("AsyncTask", "Error en doInBackground: ${e.message}", e)
                 "ERROR"  // Devuelve un valor distintivo en caso de error
             }
         }


         override fun onPostExecute(result: String?) {
             super.onPostExecute(result)
             if (result != null) {
                 showResultDialog(result)
             }
         }


         private fun showResultDialog(recibo: String) {
             val dialog = AlertDialog.Builder(requireContext())
             dialog.setTitle("Mensaje")
             if (recibo != null && recibo != "null" && recibo != "ERROR") {
                 dialog.setMessage("Recibo enviado correctamente: $recibo \n$response")
             } else {
                 dialog.setMessage("Fallo el envio")
             }

             dialog.setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.dismiss() }
             dialog.show()
             btnEnviar.isEnabled = false
             btnGuardar.isEnabled = false
         }
     }



     fun fnCrearPdf(originalBitmaps: List<Bitmap?>, imageChanged: List<Boolean>, imageUris: List<Uri?>): String? {
        // Validar que haya imágenes en los Bitmaps
        val imageCount = originalBitmaps.indices.count { imageChanged[it] && originalBitmaps[it] != null }
        if (imageCount == 0) {
            return null
        }

        val outputStream = ByteArrayOutputStream()
        val pdfWriter = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)

        pdfWriter.setCompressionLevel(9) // Nivel de compresión máximo

        var imagesInCurrentPage = 0
        var table = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f))).useAllAvailableWidth()

        for (i in originalBitmaps.indices) {
            if (imageChanged[i]) {
                var bitmap = originalBitmaps[i]
                if (bitmap != null) {
                    // Verificar y ajustar la orientación del Bitmap
                    bitmap = getCorrectlyOrientedBitmap(bitmap, imageUris[i])

                    val imageData = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 25, imageData)
                    val image = Image(com.itextpdf.io.image.ImageDataFactory.create(imageData.toByteArray()))

                    // Ajustar la imagen para que mantenga su relación de aspecto
                    image.setWidth(UnitValue.createPercentValue(100f))
                    image.setAutoScaleHeight(true)

                    table.addCell(Cell().add(image).setPadding(10f).setBorder(Border.NO_BORDER))
                    imagesInCurrentPage++

                    if (imagesInCurrentPage == 2) {
                        document.add(table)
                        table = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f))).useAllAvailableWidth()
                        imagesInCurrentPage = 0

                        // Verificar si hay más imágenes válidas después de esta
                        val hasMoreImages = (i + 1 until originalBitmaps.size).any { imageChanged[it] && originalBitmaps[it] != null }
                        if (hasMoreImages) {
                            pdfDocument.addNewPage()
                        }
                    }
                }
            }
        }

        // Agregar la última tabla si contiene una o dos imágenes.
        if (imagesInCurrentPage > 0) {
            // Añadir celdas vacías si solo hay una imagen en la última página
            while (imagesInCurrentPage < 2) {
                table.addCell(Cell().setPadding(10f).setBorder(Border.NO_BORDER))
                imagesInCurrentPage++
            }
            document.add(table)
        } else {
            // Si no hay imágenes en la página actual, eliminar la última página en blanco si existe
            if (pdfDocument.numberOfPages > 1) {
                pdfDocument.removePage(pdfDocument.numberOfPages)
            }
        }

        document.close()

        val pdfBytes = outputStream.toByteArray()
        return Base64.encodeToString(pdfBytes, Base64.DEFAULT)
    }

    private fun getCorrectlyOrientedBitmap(bitmap: Bitmap, uri: Uri?): Bitmap {
        if (uri == null) return bitmap

        val inputStream: InputStream = requireContext().contentResolver.openInputStream(uri) ?: return bitmap
        val exif = ExifInterface(inputStream)  // Usar ExifInterface de androidx
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


     private fun fnObtenerVendedor(){
         val vendedor = llenarControles.fnLLenarVendedor()
         if (vendedor != null) {
             usuario = vendedor.login
             ep_codigo = vendedor.codigo
         }
     }


     // Método para abrir la galería
     private lateinit var photoUri: Uri

     private fun fnOpenImagePicker() {
         // Crear un intent para la galería
         val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
         galleryIntent.type = "image/*"

         // Crear un intent para la cámara
         val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

         // Crear un archivo temporal para guardar la imagen capturada
         val imageFile = createImageFile()
         photoUri = FileProvider.getUriForFile(requireContext(), "${requireActivity().packageName}.provider", imageFile)
         cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

         // Verificar si la cámara está disponible
         val cameraAvailable = cameraIntent.resolveActivity(requireActivity().packageManager) != null

         // Crear un array con los intents
         val intentArray: Array<Intent?> = if (cameraAvailable) arrayOf(cameraIntent) else arrayOfNulls(0)

         // Crear un intent de selección
         val chooserIntent = Intent.createChooser(galleryIntent, "Seleccionar una opción")
         chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)

         // Lanzar el intent
         startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST)
     }


     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         if (resultCode == Activity.RESULT_OK) {
             when (requestCode) {
                 PICK_IMAGE_REQUEST -> {
                     // Verificar si la imagen proviene de la galería
                     val selectedImage: Uri? = data?.data
                     if (selectedImage != null && selectedImageViewIndex != null) {
                         // Cargar la imagen desde la galería
                         loadImage(selectedImage, imageViews[selectedImageViewIndex!!])
                         originalBitmaps[selectedImageViewIndex!!] = getBitmapFromUri(selectedImage)
                         imageUris[selectedImageViewIndex!!] = selectedImage
                         imageChanged[selectedImageViewIndex!!] = true // Marcar la imagen como cambiada
                     }

                     // Si se toma una foto con la cámara
                     else if (photoUri != null && selectedImageViewIndex != null) {
                         // Cargar la imagen capturada desde la URI
                         loadImage(photoUri, imageViews[selectedImageViewIndex!!])
                         originalBitmaps[selectedImageViewIndex!!] = getBitmapFromUri(photoUri)
                         imageUris[selectedImageViewIndex!!] = photoUri
                         imageChanged[selectedImageViewIndex!!] = true // Marcar la imagen como cambiada
                     }
                 }
             }
         }
     }

     private fun createImageFile(): File {
         val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
         val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
         return File.createTempFile(
             "JPEG_${timeStamp}_", /* prefix */
             ".jpg", /* suffix */
             storageDir /* directory */
         )
     }



     // Método para cargar la imagen en el ImageView
     private fun loadImage(imageUri: Uri, imageView: ImageView) {
         Glide.with(this)
             .load(imageUri)
             .into(imageView)
     }

     // Método para convertir Uri a Bitmap
     private fun getBitmapFromUri(uri: Uri): Bitmap? {
         return requireActivity().contentResolver.openInputStream(uri)?.use { inputStream ->
             BitmapFactory.decodeStream(inputStream)
         }
     }

     // Método para guardar la imagen capturada en el almacenamiento externo
     private fun saveImageToExternalStorage(bitmap: Bitmap): Uri {
         val imagesDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
         val imageFile = File(imagesDir, "captured_image_${System.currentTimeMillis()}.jpg")
         val outputStream = FileOutputStream(imageFile)
         bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
         outputStream.flush()
         outputStream.close()
         return Uri.fromFile(imageFile)
     }


     private fun fnBuscarFacturas() {
         val dialog = consultaItems(cl_codigo)
         dialog.setTargetFragment(this, 0) // Configura el Fragment receptor directamente
         dialog.show(parentFragmentManager, "consultaItemsTag")
     }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showDatePickerDialog(targetView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = formatDate(selectedYear, selectedMonth, selectedDay)
            targetView.text = formattedDate
            isDatePickerShown = false
        }, year, month, day)

        datePicker.setOnDismissListener {
            isDatePickerShown = false
        }

        datePicker.setOnCancelListener {
            isDatePickerShown = false
        }

        datePicker.show()
        isDatePickerShown = true
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year)
    }


    private fun fnOcultarTeclado() {
        //Log.d("hideSoftKeyboard", "Ocultando el teclado")
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = requireActivity().currentFocus
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
            //Log.d("hideSoftKeyboard", "Teclado ocultado")
        }
        forceRedrawWindow(requireActivity())
    }

    fun forceRedrawWindow(activity: Activity) {
        val contentView = activity.findViewById<View>(android.R.id.content)
        contentView.requestLayout()
    }

}