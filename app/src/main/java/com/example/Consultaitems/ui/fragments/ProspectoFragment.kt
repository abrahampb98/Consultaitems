package com.example.Consultaitems.ui.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.util.Xml
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.example.Consultaitems.R
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.adapters.Adaptador
import com.example.Consultaitems.ui.adapters.AdaptadorProspectos
import com.example.Consultaitems.ui.adapters.AutoCompleteText
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.Criterio
import com.example.Consultaitems.utils.cls.GestionSeguimiento
import com.example.Consultaitems.utils.cls.SpinnerItem
import com.example.Consultaitems.utils.cls.consultaClienteProspecto
import com.example.Consultaitems.utils.parser.XmlProspecto
import com.google.android.material.checkbox.MaterialCheckBox
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.StringReader
import java.nio.charset.StandardCharsets.UTF_8
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
class ProspectoFragment : Fragment(), consultaClienteProspecto.OnItemSelectedListener {

    private var opcion = 0
    private var xmlProspecto: XmlProspecto? = null
    private var solicitudSoap: SolicitudSoap? = null
    private var adaptadorCategoria: AutoCompleteText? = null
    private var adaptadorCiudad: AutoCompleteText? = null
    private var llenarControles: ClsLLenarControles? = null
    private var clienteSeleccionado: AdaptadorProspectos.Prospectos? = null
    private var criterios: List<Criterio> = emptyList()

    private lateinit var btnBuscarCodigoCP: ImageButton
    private lateinit var btnGuardarLlP: Button
    private lateinit var btnGuardarP: Button
    private lateinit var btnProcesarP: Button
    private lateinit var cbRazonSocialCP: MaterialCheckBox
    private lateinit var etApellido1CP: EditText
    private lateinit var etApellido2CP: EditText
    private lateinit var etCatalogoCP: EditText
    private lateinit var etCategoriaCP: AutoCompleteTextView
    private lateinit var etCiudadCP: AutoCompleteTextView
    private lateinit var etDetalleCP: TextView
    private lateinit var etDireccionCP: EditText
    private lateinit var etEmailCP: EditText
    private lateinit var etLineaCP: EditText
    private lateinit var etMapsCP: EditText
    private lateinit var etNombre1CP: EditText
    private lateinit var etNombre2CP: EditText
    private lateinit var etObservacionCP: EditText
    private lateinit var etPostalCP: EditText
    private lateinit var etProvinciaCP: EditText
    private lateinit var etRazonSocialCP: EditText
    private lateinit var etReferenciaCP: EditText
    private lateinit var etSectorCP: EditText
    private lateinit var etTelefonoCP: EditText
    private lateinit var rbCotzul: RadioButton
    private lateinit var rbPotencial: RadioButton
    private lateinit var rbProspecto: RadioButton
    private lateinit var rgTipoCliente: RadioGroup
    private lateinit var spinnerAvancesP: Spinner
    private lateinit var spinnerDescripcionP: Spinner

    private var dialog: AlertDialog? = null
    private var ciCodigo = 0
    private var clCodigo = 0
    private var coCodigo = 0
    private var rsCodigo = 0
    private var epCodigo = ""
    private var usuario = ""
    private var xmlDatos = ""
    private var tipoId = "36"
    private var tipoPersona = "N"
    private var estadoCivil = ""
    private var genero = "M"
    private var identificacion = "9999999999"
    private var existe = "0"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_prospecto, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        bindViews(view)

        llenarControles = ClsLLenarControles(requireContext())
        fnLlenarSpinnerCriterios()
        fnDesactivarControles()
        fnDesactivarLlamada()
        fnLLenarAdaptadores()

        llenarControles?.fnLLenarVendedor()?.let { vendedor ->
            usuario = vendedor.login
            epCodigo = vendedor.codigo
        }

        btnBuscarCodigoCP.setOnClickListener { fnCliente() }

        btnGuardarP.setOnClickListener {
            if (fnValidarControles()) {
                fnGuardar()
                fnDesactivarControles()
                btnProcesarP.isEnabled = true
            }
        }

        btnProcesarP.setOnClickListener { fnProcesar() }

        btnGuardarLlP.setOnClickListener {
            if (fnValidarControlesLlamada()) {
                fnGuardarLlamada()
            }
        }

        spinnerDescripcionP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                rsCodigo = criterios.getOrNull(position)?.codigo ?: 0
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun bindViews(view: View) {
        btnBuscarCodigoCP = view.findViewById(R.id.btnBuscarCodigoCP)
        rgTipoCliente = view.findViewById(R.id.rgTipoCliente)
        rbProspecto = view.findViewById(R.id.rbProspecto)
        rbPotencial = view.findViewById(R.id.rbPotencial)
        rbCotzul = view.findViewById(R.id.rbCotzul)
        etNombre1CP = view.findViewById(R.id.etNombre1CP)
        etNombre2CP = view.findViewById(R.id.etNombre2CP)
        etApellido1CP = view.findViewById(R.id.etApellido1CP)
        etApellido2CP = view.findViewById(R.id.etApellido2CP)
        etRazonSocialCP = view.findViewById(R.id.etRazonSocialCP)
        cbRazonSocialCP = view.findViewById(R.id.cbRazonSocialCP)
        etDireccionCP = view.findViewById(R.id.etDireccionCP)
        etTelefonoCP = view.findViewById(R.id.etTelefonoCP)
        etCategoriaCP = view.findViewById(R.id.etCategoriaCP)
        etCiudadCP = view.findViewById(R.id.etCiudadCP)
        etProvinciaCP = view.findViewById(R.id.etProvinciaCP)
        etEmailCP = view.findViewById(R.id.etEmailCP)
        etPostalCP = view.findViewById(R.id.etPostalCP)
        etReferenciaCP = view.findViewById(R.id.etReferenciaCP)
        etSectorCP = view.findViewById(R.id.etSectorCP)
        etMapsCP = view.findViewById(R.id.etMapsCP)
        etLineaCP = view.findViewById(R.id.etLineaCP)
        etObservacionCP = view.findViewById(R.id.etObservacionCP)
        etCatalogoCP = view.findViewById(R.id.etCatalogoCP)
        spinnerDescripcionP = view.findViewById(R.id.spinnerDescripcionP)
        spinnerAvancesP = view.findViewById(R.id.spinnerAvancesP)
        etDetalleCP = view.findViewById(R.id.etDetalleCP)
        btnGuardarP = view.findViewById(R.id.btnGuardarP)
        btnProcesarP = view.findViewById(R.id.btnProcesarP)
        btnGuardarLlP = view.findViewById(R.id.btnGuardarLlP)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.frm_menu_pedido, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bntNuevoPv -> {
                fnLimpiarControles()
                fnActivarControles()
                btnBuscarCodigoCP.isEnabled = false
                rbCotzul.isEnabled = false
                fnDesactivarLlamada()
                opcion = 1
                true
            }

            R.id.btnModificarPv -> {
                fnLimpiarControles()
                fnDesactivarLlamada()
                fnDesactivarControles()
                btnBuscarCodigoCP.isEnabled = true
                opcion = 2
                true
            }

            R.id.btnPedidoPv -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun fnLLenarAdaptadores() {
        val controles = llenarControles ?: return
        val context = requireContext()

        adaptadorCiudad = AutoCompleteText(context, controles.fnCargarDatosCiudad())
        etCiudadCP.setAdapter(adaptadorCiudad)
        etCiudadCP.setOnItemClickListener { parent, _, position, _ ->
            val ciudad = parent.getItemAtPosition(position) as? Adaptador

            if (ciudad != null && etCiudadCP.text?.isNotBlank() == true) {
                etCiudadCP.setText(ciudad.descripcion, false)
                ciCodigo = ciudad.codigo
            } else {
                ciCodigo = 0
            }
        }

        adaptadorCategoria = AutoCompleteText(context, controles.fnCargarDatosCategoria())
        etCategoriaCP.setAdapter(adaptadorCategoria)
        etCategoriaCP.setOnItemClickListener { parent, _, position, _ ->
            val categoria = parent.getItemAtPosition(position) as? Adaptador

            if (categoria != null && etCategoriaCP.text?.isNotBlank() == true) {
                etCategoriaCP.setText(categoria.descripcion, false)
                coCodigo = categoria.codigo
            } else {
                coCodigo = 0
            }
        }
    }

    private fun fnLlenarSpinnerCriterios() {
        criterios = listOf(
            Criterio(1, "Ninguno"),
            Criterio(2, "Meta"),
            Criterio(3, "Escala"),
            Criterio(4, "Google"),
            Criterio(5, "Volante"),
            Criterio(6, "Whatsapp"),
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            criterios.map { it.descripcion },
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDescripcionP.adapter = adapter
    }

    private fun fnCliente() {
        val dialogCliente = consultaClienteProspecto()
        dialogCliente.setTargetFragment(this, 0)
        dialogCliente.show(parentFragmentManager, "consultaCliente")
    }

    override fun onItemsSelected(clientes: AdaptadorProspectos.Prospectos) {
        clienteSeleccionado = clientes
        fnActivarLlamada()

        when (clientes.gt_codigo) {
            "1" -> rbProspecto.isChecked = true
            "2" -> rbPotencial.isChecked = true
            else -> rbCotzul.isChecked = true
        }

        clCodigo = clientes.Codigo.toIntOrNull() ?: 0

        etNombre1CP.setText(clientes.Nombre1)
        etNombre2CP.setText(clientes.Nombre2)
        etApellido1CP.setText(clientes.Apellido1)
        etApellido2CP.setText(clientes.Apellido2)
        etRazonSocialCP.setText(clientes.RazonSocial)
        etDireccionCP.setText(clientes.Direccion)
        etTelefonoCP.setText(clientes.Fono)
        etCiudadCP.setText(clientes.Ciudad)
        etProvinciaCP.setText(clientes.Provincia)
        etEmailCP.setText(clientes.Correo)
        etPostalCP.setText(clientes.gc_codigopostal)
        etReferenciaCP.setText(clientes.gc_puntoreferencia)
        etSectorCP.setText(clientes.gc_sector)
        etMapsCP.setText(clientes.gc_googlemap)

        fnConsultarGestion()
        fnActivarControles()

        if (clientes.Tipo != "COTZUL") {
            btnGuardarP.isEnabled = true
        }

        val codigoCiudad = clientes.CodCiudad.toIntOrNull()
        val ciudad = (0 until (adaptadorCiudad?.count ?: 0))
            .mapNotNull { adaptadorCiudad?.getItem(it) }
            .firstOrNull { it.codigo == codigoCiudad }

        if (ciudad != null) {
            etCiudadCP.setText(ciudad.descripcion, false)
            ciCodigo = ciudad.codigo
        }

        val codigoCategoria = clientes.oc_codigo.toIntOrNull()
        val categoria = (0 until (adaptadorCategoria?.count ?: 0))
            .mapNotNull { adaptadorCategoria?.getItem(it) }
            .firstOrNull { it.codigo == codigoCategoria }

        if (categoria != null) {
            etCategoriaCP.setText(categoria.descripcion, false)
            coCodigo = categoria.codigo
        }

        btnGuardarLlP.isEnabled = true
    }

    fun fnActivarControles() {
        listOf<View>(
            etDetalleCP,
            rbProspecto,
            rbPotencial,
            rbCotzul,
            btnBuscarCodigoCP,
            etNombre1CP,
            etNombre2CP,
            etApellido1CP,
            etApellido2CP,
            etRazonSocialCP,
            cbRazonSocialCP,
            etDireccionCP,
            etTelefonoCP,
            etCategoriaCP,
            etCiudadCP,
            etProvinciaCP,
            etEmailCP,
            etPostalCP,
            etReferenciaCP,
            etSectorCP,
            etMapsCP,
        ).forEach { it.isEnabled = true }

        btnGuardarP.isEnabled = true
    }

    fun fnDesactivarControles() {
        listOf<View>(
            etDetalleCP,
            rbProspecto,
            rbPotencial,
            rbCotzul,
            btnBuscarCodigoCP,
            etNombre1CP,
            etNombre2CP,
            etApellido1CP,
            etApellido2CP,
            etRazonSocialCP,
            cbRazonSocialCP,
            etDireccionCP,
            etTelefonoCP,
            etCategoriaCP,
            etCiudadCP,
            etProvinciaCP,
            etEmailCP,
            etPostalCP,
            etReferenciaCP,
            etSectorCP,
            etMapsCP,
            btnGuardarP,
            btnProcesarP,
            btnGuardarLlP,
        ).forEach { it.isEnabled = false }
    }

    fun fnLimpiarControles() {
        etDetalleCP.text = ""
        rgTipoCliente.clearCheck()

        listOf(
            etNombre1CP,
            etNombre2CP,
            etApellido1CP,
            etApellido2CP,
            etRazonSocialCP,
            etDireccionCP,
            etTelefonoCP,
            etProvinciaCP,
            etEmailCP,
            etPostalCP,
            etReferenciaCP,
            etSectorCP,
            etMapsCP,
            etLineaCP,
            etObservacionCP,
            etCatalogoCP,
        ).forEach {
            it.setText("")
            it.error = null
        }

        etCategoriaCP.setText("")
        etCategoriaCP.error = null
        etCiudadCP.setText("")
        etCiudadCP.error = null
        cbRazonSocialCP.isChecked = false
        spinnerDescripcionP.setSelection(0)

        tipoId = "36"
        tipoPersona = "N"
        estadoCivil = ""
        genero = "M"
        identificacion = "9999999999"
        ciCodigo = 0
        coCodigo = 0
    }

    fun fnActivarLlamada() {
        listOf<View>(
            etLineaCP,
            etObservacionCP,
            etCatalogoCP,
            spinnerDescripcionP,
            spinnerAvancesP,
        ).forEach { it.isEnabled = true }
    }

    fun fnDesactivarLlamada() {
        listOf<View>(
            etLineaCP,
            etObservacionCP,
            etCatalogoCP,
            spinnerDescripcionP,
            spinnerAvancesP,
        ).forEach { it.isEnabled = false }
    }

    private fun fnValidarControles(): Boolean {
        if (!rbProspecto.isChecked && !rbPotencial.isChecked) {
            Toast.makeText(
                requireContext(),
                "Debe seleccionar un tipo de cliente: Prospecto o Potencial",
                Toast.LENGTH_SHORT,
            ).show()
            return false
        }

        return validarCampo(etNombre1CP, "Ingrese el nombre", "Debe ingresar el nombre del cliente") &&
                validarCampo(etApellido1CP, "Ingrese el apellido", "Debe ingresar el apellido del cliente") &&
                validarCampo(etTelefonoCP, "Ingrese el teléfono del cliente", "Debe ingresar el teléfono del cliente") &&
                validarAutoComplete(
                    etCategoriaCP,
                    coCodigo,
                    "Seleccione la categoría del cliente",
                    "Debe seleccionar la categoría",
                ) &&
                validarAutoComplete(
                    etCiudadCP,
                    ciCodigo,
                    "Seleccione la ciudad",
                    "Debe seleccionar la ciudad",
                )
    }

    private fun fnValidarControlesLlamada(): Boolean {
        return validarCampo(etLineaCP, "Ingrese la línea ofrecida", "Debe ingresar la línea ofrecida") &&
                validarCampo(etObservacionCP, "Ingrese la observación", "Debe ingresar la observación") &&
                validarCampo(etCatalogoCP, "Ingrese el catálogo", "Debe ingresar el catálogo")
    }

    private fun validarCampo(campo: EditText, error: String, mensaje: String): Boolean {
        if (campo.text?.isBlank() == true) {
            campo.requestFocus()
            campo.error = error
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun validarAutoComplete(
        campo: AutoCompleteTextView,
        codigo: Int,
        error: String,
        mensaje: String,
    ): Boolean {
        if (campo.text?.isBlank() == true || codigo == 0) {
            campo.requestFocus()
            campo.error = error
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun fnConsultarGestion() {
        val context = requireContext()
        if (isNetworkAvailable(context)) {
            solicitudSoap = SolicitudSoap(context)
            MiAsyncTaskAvances(showProgressDialog()).execute()
        } else {
            Toast.makeText(requireContext(), "Verifique su conexión a internet", Toast.LENGTH_LONG).show()
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return manager.activeNetworkInfo?.isConnected == true
    }

    private fun showProgressDialog(): ProgressDialog {
        return ProgressDialog(requireContext()).apply {
            setMessage("Cargando Datos...")
            setCancelable(false)
            show()
        }
    }

    private fun readSoapResponse(): String? {
        return solicitudSoap
            ?.realizarSolicitudSoap()
            ?.bufferedReader(UTF_8)
            ?.use { it.readText() }
            ?.takeIf { it.isNotBlank() }
    }

    private inner class MiAsyncTaskAvances(
        private val progressDialog: ProgressDialog,
    ) : AsyncTask<Void, Void, String?>() {

        override fun doInBackground(vararg params: Void?): String? {
            return try {
                val clienteCodigo = clienteSeleccionado?.Codigo.orEmpty()
                val cadena = "'','', $epCodigo,'$clienteCodigo',3"
                solicitudSoap?.initializeVariables(getString(R.string.str_Avances).toInt(), cadena)
                val result = readSoapResponse()
                if (!result.isNullOrBlank()) xmlDatos = result
                result
            } catch (e: Exception) {
                Log.e("ProspectoFragment", "Error consultando avances", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            progressDialog.dismiss()

            val avances = fnParseXmlGestionSeguimiento(result ?: xmlDatos)

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                avances.map { it.observacion }
            )

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerAvancesP.adapter = adapter

            spinnerAvancesP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val seleccionado = avances.getOrNull(position) ?: return

                    val fechaFormateada = formatearFechaHora(seleccionado.fecha)
                    etDetalleCP.setText("$fechaFormateada - ${seleccionado.lineaOfrecida}")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    etDetalleCP.setText("")
                }
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun fnParseXmlGestionSeguimiento(xmlString: String?): List<GestionSeguimiento> {
        if (xmlString.isNullOrBlank()) return emptyList()

        val observaciones = mutableListOf<String>()
        val fechas = mutableListOf<String>()
        val lineas = mutableListOf<String>()

        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlString))

        var eventType = parser.eventType
        var currentField = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> currentField = parser.name.orEmpty().lowercase(Locale.ROOT)
                XmlPullParser.END_TAG -> currentField = ""
                XmlPullParser.TEXT -> {
                    val text = parser.text?.trim().orEmpty()
                    if (text.isNotEmpty()) {
                        when (currentField) {
                            "gv_observacion" -> observaciones.add(text)
                            "gv_fechatrn" -> fechas.add(text)
                            "gv_lineaofrecida" -> lineas.add(text)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        val total = maxOf(observaciones.size, fechas.size, lineas.size)
        return (0 until total).map { index ->
            GestionSeguimiento(
                "${index + 1} - ${observaciones.getOrElse(index) { "" }}",
                fechas.getOrElse(index) { "" },
                lineas.getOrElse(index) { "" },
            )
        }
    }

    fun formatearFechaHora(fechaOriginal: String): String {
        if (fechaOriginal.isBlank()) return ""

        val formatosEntrada = listOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSS",
        )
        val formatoSalida = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        formatosEntrada.forEach { formato ->
            try {
                val date = SimpleDateFormat(formato, Locale.getDefault()).parse(fechaOriginal)
                if (date != null) return formatoSalida.format(date)
            } catch (_: Exception) {
                // Se prueba el siguiente formato.
            }
        }

        return fechaOriginal.replace("T", " ")
    }

    fun fnGuardar() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("¿Desea enviar los datos?")
            .setPositiveButton("Sí") { _, _ ->
                try {
                    xmlProspecto = XmlProspecto(requireContext())
                    solicitudSoap = SolicitudSoap(requireContext())
                    MiAsyncTaskProspecto(showProgressDialog()).execute()
                } catch (e: Exception) {
                    Log.e("ProspectoFragment", "Error guardando prospecto", e)
                }
            }
            .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
    }

    fun fnProcesar() {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.frm_prospecto_procesar, null as ViewGroup?)

        val spTipoDoc = view.findViewById<Spinner>(R.id.spTipoDoc)
        val spTipoPersona = view.findViewById<Spinner>(R.id.spTipoPersona)
        val spEstadoCivil = view.findViewById<Spinner>(R.id.spEstadoCivil)
        val spGenero = view.findViewById<Spinner>(R.id.spGenero)
        val etId = view.findViewById<EditText>(R.id.etIdentificacion)

        val listaTipoDoc = listOf(
            SpinnerItem("36", "Cédula"),
            SpinnerItem("37", "RUC"),
            SpinnerItem("38", "Pasaporte"),
        )
        val listaTipoPersona = listOf(
            SpinnerItem("N", "Natural"),
            SpinnerItem("J", "Jurídica"),
        )
        val listaEstadoCivil = listOf(
            SpinnerItem("0", "No Especifica"),
            SpinnerItem("1", "Soltero"),
            SpinnerItem(ExifInterface.GPS_MEASUREMENT_2D, "Casado"),
            SpinnerItem(ExifInterface.GPS_MEASUREMENT_3D, "Divorciado"),
            SpinnerItem("4", "Unión Libre"),
            SpinnerItem("5", "Viudo"),
        )
        val listaGenero = listOf(
            SpinnerItem("M", "Masculino"),
            SpinnerItem("F", "Femenino"),
        )

        spTipoDoc.adapter = crearAdapterSpinner(listaTipoDoc)
        spTipoPersona.adapter = crearAdapterSpinner(listaTipoPersona)
        spEstadoCivil.adapter = crearAdapterSpinner(listaEstadoCivil)
        spGenero.adapter = crearAdapterSpinner(listaGenero)

        dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)
            .setPositiveButton("Aceptar", null)
            .setNegativeButton("Cancelar") { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()

        dialog?.show()
        dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
            aceptarProcesar(spTipoDoc, spTipoPersona, spEstadoCivil, spGenero, etId)
        }
    }

    private fun crearAdapterSpinner(list: List<SpinnerItem>): ArrayAdapter<SpinnerItem> {
        return ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, list).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun aceptarProcesar(
        spTipoDoc: Spinner,
        spTipoPersona: Spinner,
        spEstadoCivil: Spinner,
        spGenero: Spinner,
        etId: EditText,
    ) {
        val tipoDoc = spTipoDoc.selectedItem as SpinnerItem
        val tipoPersonaSeleccionada = spTipoPersona.selectedItem as SpinnerItem
        val estadoCivilSeleccionado = spEstadoCivil.selectedItem as SpinnerItem
        val generoSeleccionado = spGenero.selectedItem as SpinnerItem
        val identificacionIngresada = etId.text.toString().trim()

        if (tipoDoc.codigo.isBlank() || tipoDoc.codigo == "0") {
            Toast.makeText(requireContext(), "Seleccione Tipo de Documento", Toast.LENGTH_SHORT).show()
            return
        }
        if (identificacionIngresada.isBlank()) {
            Toast.makeText(requireContext(), "Ingrese Identificación", Toast.LENGTH_SHORT).show()
            return
        }
        if (tipoPersonaSeleccionada.codigo.isBlank()) {
            Toast.makeText(requireContext(), "Seleccione Tipo de Persona", Toast.LENGTH_SHORT).show()
            return
        }
        if (generoSeleccionado.codigo.isBlank()) {
            Toast.makeText(requireContext(), "Seleccione Género", Toast.LENGTH_SHORT).show()
            return
        }

        tipoId = tipoDoc.codigo.ifBlank { "0" }
        tipoPersona = tipoPersonaSeleccionada.codigo.ifBlank { "N" }
        estadoCivil = estadoCivilSeleccionado.codigo.ifBlank { "0" }
        genero = generoSeleccionado.codigo.ifBlank { "M" }
        identificacion = identificacionIngresada.ifBlank { "9999999999" }
        opcion = 4

        solicitudSoap = SolicitudSoap(requireContext())
        MiAsyncTaskValidarIdentificacion().execute()
    }

    fun fnLlenarDatosClienteNuevo() {
        val nombre1 = etNombre1CP.text.toString().trim().uppercase(Locale.ROOT)
        val nombre2 = etNombre2CP.text.toString().trim().uppercase(Locale.ROOT)
        val apellido1 = etApellido1CP.text.toString().trim().uppercase(Locale.ROOT)
        val apellido2 = etApellido2CP.text.toString().trim().uppercase(Locale.ROOT)
        val razonSocial = etRazonSocialCP.text.toString().trim().uppercase(Locale.ROOT)
        val direccion = etDireccionCP.text.toString().trim().uppercase(Locale.ROOT)
        val telefono = etTelefonoCP.text.toString().trim()
        val correo = etEmailCP.text.toString().trim()
        val postal = etPostalCP.text.toString().trim()
        val sector = etSectorCP.text.toString().trim().uppercase(Locale.ROOT)
        val referencia = etReferenciaCP.text.toString().trim().uppercase(Locale.ROOT)
        val maps = etMapsCP.text.toString().trim()

        clienteSeleccionado = AdaptadorProspectos.Prospectos(
            "PROSPECTO",
            "0",
            nombre1,
            nombre2,
            apellido1,
            apellido2,
            "",
            razonSocial,
            direccion,
            telefono,
            ciCodigo.toString(),
            "",
            "",
            correo,
            coCodigo.toString(),
            "",
            "1",
            postal,
            sector,
            referencia,
            maps,
        )
    }

    private inner class MiAsyncTaskProspecto(
        private val progressDialog: ProgressDialog,
    ) : AsyncTask<Void, Void, String?>() {

        override fun doInBackground(vararg params: Void?): String? {
            return try {
                if (opcion == 1) fnLlenarDatosClienteNuevo()

                val cliente = clienteSeleccionado ?: return null
                val cadena = xmlProspecto?.fnObtenerXmlProspecto(
                    cliente,
                    usuario,
                    tipoId,
                    identificacion,
                    tipoPersona,
                    genero,
                    epCodigo,
                    coCodigo,
                    opcion,
                ) ?: return null

                solicitudSoap?.initializeVariables(getString(R.string.str_ProspectosXML).toInt(), cadena)
                val result = readSoapResponse()
                if (!result.isNullOrBlank()) xmlDatos = result
                result
            } catch (e: Exception) {
                Log.e("ProspectoFragment", "Error enviando prospecto", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            progressDialog.dismiss()

            if (opcion == 4) {
                dialog?.dismiss()
                val secuencia = fnObtenerSecuencia(result)
                showResultDialog(if (!secuencia.isNullOrBlank()) secuencia else "No se pudo obtener la secuencia")
            } else {
                val secuencia = fnObtenerSecuencia(result)
                if (!secuencia.isNullOrBlank()) {
                    clCodigo = secuencia.toIntOrNull() ?: clCodigo
                }
                showResultDialogOk(clCodigo.toString())
            }

            fnActivarLlamada()
            btnProcesarP.isEnabled = true
            btnGuardarLlP.isEnabled = true
        }
    }

    private fun showResultDialog(pedido: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("# Cliente: $pedido")
            .setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()

        btnGuardarP.isEnabled = false
        btnProcesarP.isEnabled = false
    }

    private fun showResultDialogOk(pedido: String?) {
        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("Datos Guardados Correctamente")
            .setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()

        btnGuardarP.isEnabled = false
        btnProcesarP.isEnabled = false
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun fnObtenerSecuencia(xmlResult: String?): String? {
        if (xmlResult.isNullOrBlank()) return null

        return try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlResult))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name.equals("secuencia", ignoreCase = true)) {
                    parser.next()
                    return parser.text?.trim()
                }
                eventType = parser.next()
            }
            null
        } catch (e: Exception) {
            Log.e("ProspectoFragment", "Error obteniendo secuencia", e)
            null
        }
    }

    internal inner class MiAsyncTaskValidarIdentificacion : AsyncTask<Void, Void, String?>() {

        override fun doInBackground(vararg params: Void?): String? {
            return try {
                val cadena = "'',0,'$usuario','$identificacion',5"
                solicitudSoap?.initializeVariables(getString(R.string.str_ProspectosXML).toInt(), cadena)
                readSoapResponse()
            } catch (e: Exception) {
                Log.e("ProspectoFragment", "Error validando identificación", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            existe = fnExisteId(result) ?: "0"

            if ((existe.toIntOrNull() ?: 0) >= 1) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Sistema")
                    .setMessage("Número de cédula ya existe")
                    .setPositiveButton("Aceptar") { dialogInterface, _ -> dialogInterface.dismiss() }
                    .setCancelable(false)
                    .show()
            } else {
                fnGuardar()
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun fnExisteId(xml: String?): String? {
        if (xml.isNullOrBlank()) return null

        return try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xml))

            var dentroExiste = false
            var eventType = parser.eventType

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name.equals("existe", ignoreCase = true)) dentroExiste = true
                    }

                    XmlPullParser.END_TAG -> {
                        if (parser.name.equals("existe", ignoreCase = true)) dentroExiste = false
                    }

                    XmlPullParser.TEXT -> {
                        if (dentroExiste) return parser.text?.trim()
                    }
                }
                eventType = parser.next()
            }
            null
        } catch (e: Exception) {
            Log.e("ProspectoFragment", "Error leyendo existe", e)
            null
        }
    }

    fun fnGuardarLlamada() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("¿Desea guardar los datos?")
            .setPositiveButton("Sí") { _, _ ->
                try {
                    fnEnviarGestionLlamada()
                } catch (e: Exception) {
                    Log.e("ProspectoFragment", "Error guardando llamada", e)
                }
            }
            .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
    }

    fun fnEnviarGestionLlamada() {
        xmlProspecto = XmlProspecto(requireContext())
        solicitudSoap = SolicitudSoap(requireContext())

        val progressDialog = showProgressDialog()
        if (clienteSeleccionado?.gt_codigo== "1") {
            MiAsyncTaskLClientePublico(progressDialog).execute()
        } else {
            MiAsyncTaskLlamada(progressDialog).execute()
        }

        fnDesactivarLlamada()
        fnDesactivarControles()
    }

    private inner class MiAsyncTaskLlamada(
        private val progressDialog: ProgressDialog,
    ) : AsyncTask<Void, Void, String?>() {

        override fun doInBackground(vararg params: Void?): String? {
            return try {
                val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                val linea = etLineaCP.text.toString()
                val observacion = etObservacionCP.text.toString()
                val catalogo = etCatalogoCP.text.toString()

                var cliente = clienteSeleccionado?.Codigo?.toIntOrNull() ?: 0
                if (clienteSeleccionado?.gt_codigo == "1") {
                    cliente = 1774
                }

                val cadena = xmlProspecto?.fnObtenerXmLLamada(
                    fecha,
                    usuario,
                    linea,
                    observacion,
                    catalogo,
                    epCodigo,
                    cliente,
                    rsCodigo,
                ) ?: return null

                solicitudSoap?.initializeVariables(getString(R.string.str_LlamadaXML).toInt(), cadena)
                val result = readSoapResponse()
                if (!result.isNullOrBlank()) xmlDatos = result
                result
            } catch (e: Exception) {
                Log.e("ProspectoFragment", "Error enviando llamada", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            progressDialog.dismiss()
            showResultDialogOk("")
        }
    }

    private inner class MiAsyncTaskLClientePublico(
        private val progressDialog: ProgressDialog,
    ) : AsyncTask<Void, Void, String?>() {

        override fun doInBackground(vararg params: Void?): String? {
            return try {
                val cliente = if (clienteSeleccionado?.gt_codigo == "1") {
                    1774
                } else {
                    clienteSeleccionado?.Codigo?.toIntOrNull() ?: 0
                }

                val prospecto = clienteSeleccionado ?: return null
                val cadena = xmlProspecto?.fnObtenerXmPublico(
                    prospecto,
                    usuario,
                    epCodigo,
                    cliente,
                    "9999999999999",
                    clCodigo,
                ) ?: return null

                solicitudSoap?.initializeVariables(getString(R.string.str_LlamadaXML).toInt(), cadena)
                val result = readSoapResponse()
                if (!result.isNullOrBlank()) xmlDatos = result
                result
            } catch (e: Exception) {
                Log.e("ProspectoFragment", "Error enviando cliente público", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            xmlProspecto = XmlProspecto(requireContext())
            solicitudSoap = SolicitudSoap(requireContext())
            MiAsyncTaskLlamada(progressDialog).execute()
            btnGuardarLlP.isEnabled = false
        }
    }
}
