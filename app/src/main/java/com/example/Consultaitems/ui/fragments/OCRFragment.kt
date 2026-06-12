package com.example.Consultaitems.ui.fragments

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Xml
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.activities.frmLogin
import com.example.Consultaitems.ui.adapters.Adaptador
import com.example.Consultaitems.ui.adapters.AutoCompleteText
import com.example.Consultaitems.ui.adapters.Categoria
import com.example.Consultaitems.ui.adapters.CategoriaAdapter
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.clsObtenerDatos
import com.example.Consultaitems.utils.parser.XMlParserEnte
import com.example.Consultaitems.utils.parser.XmlMantEnte
import com.example.Consultaitems.utils.parser.XmlParserProvinciaCiudad
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.xmlpull.v1.XmlPullParserException
import com.google.android.flexbox.FlexboxLayoutManager
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader


class OCRFragment : Fragment() {

    private lateinit var xmlEnte: XmlMantEnte
    private var adaptadorCiudad: AutoCompleteText? = null
    private var adaptadorParroquia: AutoCompleteText? = null

    private lateinit var btnBuscarCodigo2: ImageButton
    private lateinit var btnEnvioCL: Button
    private lateinit var btnGuardarCL: Button
    private lateinit var btnOcrCamara: ImageButton
    private lateinit var categoriaAdapter: CategoriaAdapter

    private lateinit var chkNovedadesNo: CheckBox
    private lateinit var chkNovedadesSi: CheckBox
    private lateinit var chkPasaporte: CheckBox
    private lateinit var chkPoliticaNo: CheckBox
    private lateinit var chkPoliticaSi: CheckBox
    private lateinit var chkRuc: CheckBox

    private var ci_codigo: Int = 0
    private var dq_interno: Int = 0
    var ep_codigo: Int = frmLogin.CadenaHolder.ep_codigo
    var usuario: String = ""
    var vgsOpcionMenu: String = ""

    lateinit var dbHelper: SqLiteOpenHelper
    lateinit var solicitudSoap: SolicitudSoap

    private lateinit var etApellido1: EditText
    private lateinit var etApellido2: EditText
    private lateinit var etCantonNacimiento: EditText
    private lateinit var etCedula: EditText
    private lateinit var etCiudadNacimiento: AutoCompleteTextView
    private lateinit var etCodigo: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etEmail: EditText
    private lateinit var etFechaNacimiento: EditText
    private lateinit var etNombre1: EditText
    private lateinit var etNombre2: EditText
    private lateinit var etNombreComercial: EditText
    private lateinit var etParroquiaNacimiento: AutoCompleteTextView
    private lateinit var etProvinciaNacimiento: EditText
    private lateinit var etRazonSocial: EditText
    private lateinit var etTelefono: EditText
    private lateinit var llenarControles: ClsLLenarControles
    private var photoUri: Uri? = null
    private lateinit var rvCategorias: RecyclerView
    private lateinit var spnEstadoCivil: Spinner
    private lateinit var spnGenero: Spinner
    private lateinit var spnTipoPersona: Spinner
    private lateinit var tvTitulo: TextView

    private var isDatePickerShown = false
    private var validando = false
    private var errorMessage = ""
    private var errorMessageC = ""
    private var codigosSeleccionados: List<Int> = emptyList()
    private val listaCategorias: MutableList<Categoria> = mutableListOf()
    private val PICK_IMAGE_REQUEST = 100

    private val pickImage: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { processImage(it) }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.frm_mant_cliente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llenarControles = ClsLLenarControles(requireContext())
        dbHelper = SqLiteOpenHelper(requireContext())
        solicitudSoap = SolicitudSoap(requireContext())
        xmlEnte = XmlMantEnte(requireContext())

        llenarControles.fnLLenarVendedor()?.let { vendedor ->
            ep_codigo = vendedor.codigo.toIntOrNull() ?: ep_codigo
            usuario = vendedor.login
        }

        setHasOptionsMenu(true)

        tvTitulo = view.findViewById(R.id.tvTitulo)
        btnOcrCamara = view.findViewById(R.id.btnOcrCamara)
        etCodigo = view.findViewById(R.id.etCodigo)
        btnBuscarCodigo2 = view.findViewById(R.id.btnBuscarCodigo2)
        etCedula = view.findViewById(R.id.etCedula)
        chkPasaporte = view.findViewById(R.id.chkPasaporte)
        chkRuc = view.findViewById(R.id.chkRuc)
        etNombre1 = view.findViewById(R.id.etNombre1)
        etNombre2 = view.findViewById(R.id.etNombre2)
        etApellido1 = view.findViewById(R.id.etApellido1)
        etApellido2 = view.findViewById(R.id.etApellido2)
        etNombreComercial = view.findViewById(R.id.etNombreComercial)
        etRazonSocial = view.findViewById(R.id.etRazonSocial)
        spnGenero = view.findViewById(R.id.spnGenero)
        etFechaNacimiento = view.findViewById(R.id.etFechaNacimiento)
        etProvinciaNacimiento = view.findViewById(R.id.etProvinciaNacimiento)
        etCiudadNacimiento = view.findViewById(R.id.etCiudadNacimiento)
        etParroquiaNacimiento = view.findViewById(R.id.etParroquiaNacimiento)
        etCantonNacimiento = view.findViewById(R.id.etCantonNacimiento)
        spnEstadoCivil = view.findViewById(R.id.spnEstadoCivil)
        spnTipoPersona = view.findViewById(R.id.spnTipoPersona)
        etDireccion = view.findViewById(R.id.etDireccion)
        etTelefono = view.findViewById(R.id.etTelefono)
        etEmail = view.findViewById(R.id.etEmail)
        chkPoliticaSi = view.findViewById(R.id.chkPoliticaSi)
        chkPoliticaNo = view.findViewById(R.id.chkPoliticaNo)
        chkNovedadesSi = view.findViewById(R.id.chkNovedadesSi)
        chkNovedadesNo = view.findViewById(R.id.chkNovedadesNo)
        btnGuardarCL = view.findViewById(R.id.btnGuardarCL)
        btnEnvioCL = view.findViewById(R.id.btnEnvioCL)
        rvCategorias = view.findViewById(R.id.rvCategorias)

        fnInicializarCategorias()
        fnDesactivarControles()
        fnLLenarAdaptadores()

        runCatching { llenarControles.fnLLenarSpinnerEstadoCivil(spnEstadoCivil) }
        runCatching { llenarControles.fnLLenarSpinnerTipoPersona(spnTipoPersona) }
        runCatching { llenarControles.fnLLenarSpinnerGenero(spnGenero) }

        btnOcrCamara.setOnClickListener { fnOpenImagePicker() }
        etFechaNacimiento.setOnClickListener {
            if (!isDatePickerShown) showDatePickerDialog(etFechaNacimiento)
        }
        btnGuardarCL.setOnClickListener {
            if (fnValidarControles()) {
                fnGuardar()
                if (vgsOpcionMenu == "I") {
                    btnEnvioCL.isEnabled = true
                } else {
                    fnDesactivarControles()
                }
            }
        }
        btnEnvioCL.setOnClickListener {
            fnEnviar()
        }
        btnBuscarCodigo2.setOnClickListener { fnMostrarDialogoDePedidos() }

        chkRuc.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                chkPasaporte.isChecked = false
                etCedula.filters = arrayOf(android.text.InputFilter.LengthFilter(13))
            }
        }
        chkPasaporte.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                chkRuc.isChecked = false
                etCedula.filters = emptyArray()
            }
        }
        chkPoliticaSi.setOnCheckedChangeListener { _, isChecked -> if (isChecked) chkPoliticaNo.isChecked = false }
        chkPoliticaNo.setOnCheckedChangeListener { _, isChecked -> if (isChecked) chkPoliticaSi.isChecked = false }
        chkNovedadesSi.setOnCheckedChangeListener { _, isChecked -> if (isChecked) chkNovedadesNo.isChecked = false }
        chkNovedadesNo.setOnCheckedChangeListener { _, isChecked -> if (isChecked) chkNovedadesSi.isChecked = false }

        etCedula.setOnFocusChangeListener { _, hasFocus ->
            validarAlPerderFoco(
                hasFocus = hasFocus,
                campo = etCedula,
                mensajeError = "Número de Cédula incorrecto, verifique"
            ) {
                fnDigitoVerificador(etCedula.text.toString())
            }

            if (!hasFocus) {
                fnVerificarIdentificacion()
            }
        }

        etTelefono.setOnFocusChangeListener { _, hasFocus ->
            validarAlPerderFoco(
                hasFocus = hasFocus,
                campo = etTelefono,
                mensajeError = "Ingrese el número de teléfono"
            ) {
                !etTelefono.text.isNullOrBlank()
            }
        }

        etEmail.setOnFocusChangeListener { _, hasFocus ->
            validarAlPerderFoco(
                hasFocus = hasFocus,
                campo = etEmail,
                mensajeError = "Ingrese el correo electrónico"
            ) {
                !etEmail.text.isNullOrBlank()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.frm_menu_mant_ente, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bntNuevoCL -> {
                fnAccionesAlPulsarNuevo(); true
            }
            R.id.btnModificarCL -> {
                fnAcionesAlPulsarModificar(); true
            }
            R.id.btnsincronizarCL -> {
                fnSincronizarCiudadesProvinciasparroquias(); true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun fnAccionesAlPulsarNuevo() {
        vgsOpcionMenu = "I"
        tvTitulo.text = "Crear cliente"
        fnLimpiarControles()
        fnActivarControles()
        fnObtenerSecuencia()
        btnEnvioCL.isEnabled = false
    }

    fun fnAcionesAlPulsarModificar() {
        vgsOpcionMenu = "M"
        tvTitulo.text = "Modificar cliente"
        fnActivarControles()
        etCodigo.isEnabled = true
        btnBuscarCodigo2.isEnabled = true
        btnEnvioCL.isEnabled = false
    }

    fun fnObtenerSecuencia() {
        runCatching {
            val secuencia = llenarControles.fnObtenerSecuenciaente()
            etCodigo.setText(secuencia.toString())
            dq_interno = secuencia
        }.onFailure {
            dq_interno = 0
        }
    }

    fun fnActivarControles() {
        listOf(
            etCedula, etNombre1, etNombre2, etApellido1, etApellido2, etNombreComercial,
            etRazonSocial, etFechaNacimiento, etProvinciaNacimiento, etCiudadNacimiento,
            etParroquiaNacimiento, etCantonNacimiento, etDireccion, etTelefono, etEmail
        ).forEach { it.isEnabled = true }
        listOf(chkRuc, chkPasaporte, chkPoliticaSi, chkPoliticaNo, chkNovedadesSi, chkNovedadesNo)
            .forEach { it.isEnabled = true }
        listOf(spnGenero, spnEstadoCivil, spnTipoPersona).forEach { it.isEnabled = true }
        btnGuardarCL.isEnabled = true
        btnOcrCamara.isEnabled = true
        categoriaAdapter.fnHabilitar()
    }

    fun fnDesactivarControles() {
        listOf(
            etCodigo, etCedula, etNombre1, etNombre2, etApellido1, etApellido2,
            etNombreComercial, etRazonSocial, etFechaNacimiento, etProvinciaNacimiento,
            etCiudadNacimiento, etParroquiaNacimiento, etCantonNacimiento, etDireccion,
            etTelefono, etEmail
        ).forEach { it.isEnabled = false }
        listOf(chkRuc, chkPasaporte, chkPoliticaSi, chkPoliticaNo, chkNovedadesSi, chkNovedadesNo)
            .forEach { it.isEnabled = false }
        listOf(spnGenero, spnEstadoCivil, spnTipoPersona).forEach { it.isEnabled = false }
        btnBuscarCodigo2.isEnabled = false
        btnGuardarCL.isEnabled = false
        btnEnvioCL.isEnabled = false
        btnOcrCamara.isEnabled = false
        if (::categoriaAdapter.isInitialized) categoriaAdapter.fnDeshabilitar()
    }

    fun fnLimpiarControles() {
        listOf(
            etCodigo, etCedula, etNombre1, etNombre2, etApellido1, etApellido2,
            etNombreComercial, etRazonSocial, etFechaNacimiento, etProvinciaNacimiento,
            etCiudadNacimiento, etParroquiaNacimiento, etCantonNacimiento, etDireccion,
            etTelefono, etEmail
        ).forEach { it.setText("") }
        listOf(chkRuc, chkPasaporte, chkPoliticaSi, chkPoliticaNo, chkNovedadesSi, chkNovedadesNo)
            .forEach { it.isChecked = false }
        spnGenero.setSelection(0)
        spnEstadoCivil.setSelection(0)
        spnTipoPersona.setSelection(0)
        ci_codigo = 0
        dq_interno = 0
        codigosSeleccionados = emptyList()
        fnLimpiarSeleccionCategorias()
        fnlimpiarValidaciones()
    }

    fun fnlimpiarValidaciones() {
        errorMessage = ""
        errorMessageC = ""
        listOf(etCedula, etTelefono, etEmail).forEach { it.error = null }
    }

    fun fnLlenarControles(datos: DatosEnte) {
        dq_interno = datos.en_codigo
        etCodigo.setText(datos.en_codigo.toString())
        etCedula.setText(datos.en_identificacion)
        etNombre1.setText(datos.en_nombre1)
        etNombre2.setText(datos.en_nombre2)
        etApellido1.setText(datos.en_apellido1)
        etApellido2.setText(datos.en_apellido2)
        etNombreComercial.setText(datos.en_razoncomercial)
        etRazonSocial.setText(datos.en_razonsocial)
        etFechaNacimiento.setText(datos.en_fechanac)
        etDireccion.setText(datos.direccion)
        etTelefono.setText(datos.celular)
        etEmail.setText(datos.correo)
        ci_codigo = datos.ci_codigo
        chkPoliticaSi.isChecked = datos.cl_politica == 1
        chkPoliticaNo.isChecked = datos.cl_politica != 1
        chkNovedadesSi.isChecked = datos.cl_campania == 1
        chkNovedadesNo.isChecked = datos.cl_campania != 1
        seleccionarSpinner(spnGenero, datos.en_genero)
        seleccionarSpinner(spnTipoPersona, datos.en_tipopersona)
        fnMarcarCategoriasCliente(datos.en_codigo)
    }

    fun fnLLenarAdaptadores() {
        adaptadorCiudad = AutoCompleteText(requireContext(), llenarControles.fnCargarDatosCiudad())
        etCiudadNacimiento.setAdapter(adaptadorCiudad)
        etCiudadNacimiento.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val ciudad = parent.getItemAtPosition(position) as? Adaptador ?: return@OnItemClickListener
            etCiudadNacimiento.setText(ciudad.descripcion, false)
            ci_codigo = ciudad.codigo
            fnObtenerProvinciaCanton()
        }

        adaptadorParroquia = AutoCompleteText(requireContext(), llenarControles.fnCargarDatosParroquia(0))
        etParroquiaNacimiento.setAdapter(adaptadorParroquia)
        etParroquiaNacimiento.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val parroquia = parent.getItemAtPosition(position) as? Adaptador ?: return@OnItemClickListener
            etParroquiaNacimiento.setText(parroquia.descripcion, false)
        }
    }

    fun fnMostrarDialogoDePedidos() {
        val input = EditText(requireContext()).apply {
            hint = "Codigo de ente"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Buscar ente")
            .setView(input)
            .setPositiveButton("Buscar") { _, _ ->
                val codigo = input.text.toString().trim()
                if (codigo.isBlank()) return@setPositiveButton
                val datos = llenarControles.fnObtenerDatosEnte(codigo.toInt())
                if (datos != null) {
                    fnLlenarControles(datos)
                    fnActivarControles()
                } else {
                    Toast.makeText(requireContext(), "No se encontro el ente", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    fun fnObtenerProvinciaCanton() {
        runCatching {
            val provincia = llenarControles.fnObtenerProvincia(ci_codigo)
            etProvinciaNacimiento.setText(provincia.toString())
        }
    }

    private fun fnFecha(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    private fun showDatePickerDialog(targetView: TextView) {
        val calendar = Calendar.getInstance()
        isDatePickerShown = true
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                targetView.text = formatDate(year, month, day)
                isDatePickerShown = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { isDatePickerShown = false }
            setOnCancelListener { isDatePickerShown = false }
        }.show()
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)
    }

    fun fnDigitoVerificador(identificacion: String): Boolean {
        val numero = identificacion.filter { it.isDigit() }
        if (numero.length != 10) return false
        val provincia = numero.substring(0, 2).toIntOrNull() ?: return false
        if (provincia !in 1..24 && provincia != 30) return false
        val coeficientes = intArrayOf(2, 1, 2, 1, 2, 1, 2, 1, 2)
        val suma = coeficientes.indices.sumOf { i ->
            val producto = numero[i].digitToInt() * coeficientes[i]
            if (producto >= 10) producto - 9 else producto
        }
        val digito = if (suma % 10 == 0) 0 else 10 - (suma % 10)
        return digito == numero[9].digitToInt()
    }

    private fun fnValidarControles(): Boolean {
        fnlimpiarValidaciones()
        var valido = true

        val identificacion = etCedula.text.toString().trim()
        if (identificacion.isBlank()) {
            etCedula.error = "Ingrese identificacion"
            valido = false
        } else if (!chkPasaporte.isChecked) {
            val ok = if (chkRuc.isChecked) {
                identificacion.length == 13 && fnDigitoVerificador(identificacion.take(10))
            } else {
                fnDigitoVerificador(identificacion)
            }
            if (!ok) {
                etCedula.error = "Identificacion no valida"
                valido = false
            }
        }

        if (etNombre1.text.toString().isBlank() && etRazonSocial.text.toString().isBlank()) {
            etNombre1.error = "Ingrese nombre o razon social"
            valido = false
        }
        if (etDireccion.text.toString().isBlank()) {
            etDireccion.error = "Ingrese direccion"
            valido = false
        }
        if (!fnValidarTelefonos(etTelefono.text.toString())) {
            etTelefono.error = "Telefono no valido"
            valido = false
        }
        if (!fnValidarCorreo(etEmail.text.toString())) {
            etEmail.error = "Correo no valido"
            valido = false
        }
        if (!chkPoliticaSi.isChecked && !chkPoliticaNo.isChecked) {
            Toast.makeText(requireContext(), "Seleccione politica LOPDP", Toast.LENGTH_SHORT).show()
            valido = false
        }
        return valido
    }

    fun fnValidarTelefonos(input: String?): Boolean {
        val value = input.orEmpty().trim()
        if (value.isBlank()) return false
        val digits = value.filter { it.isDigit() }
        if (digits.length < 7) return false
        if (fnTodosIguales(digits)) return false
        if (fnEsSecuenciaAscendente(digits) || fnEsSecuenciaDescendente(digits)) return false
        return true
    }

    private fun fnTodosIguales(s: String): Boolean = s.isNotEmpty() && s.all { it == s[0] }

    private fun fnMaxRunIguales(s: String): Int {
        if (s.isEmpty()) return 0
        var max = 1
        var current = 1
        for (i in 1 until s.length) {
            if (s[i] == s[i - 1]) current++ else current = 1
            if (current > max) max = current
        }
        return max
    }

    private fun fnEsSecuenciaAscendente(s: String): Boolean {
        if (s.length < 4) return false
        return s.windowed(2).all { it[1].digitToIntOrNull() == (it[0].digitToIntOrNull() ?: -10) + 1 }
    }

    private fun fnEsSecuenciaDescendente(s: String): Boolean {
        if (s.length < 4) return false
        return s.windowed(2).all { it[1].digitToIntOrNull() == (it[0].digitToIntOrNull() ?: 10) - 1 }
    }

    fun fnValidarCorreo(email: String?): Boolean {
        val value = email.orEmpty().trim()
        if (value.isBlank()) return true
        return android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()
    }

    private fun fnGuardar() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar")
            .setMessage("Desea guardar los datos?")
            .setPositiveButton("Si") { _, _ -> fnGuardarDatos() }
            .setNegativeButton("No", null)
            .show()
    }

    fun fnGuardarDatos() {
        val db = dbHelper.writableDatabase
        try {
            db.beginTransaction()
            val codigo = etCodigo.text.toString().toIntOrNull() ?: dq_interno
            dq_interno = codigo

            val values = android.content.ContentValues().apply {
                put("en_codigo", codigo)
                put("en_ci", if (chkRuc.isChecked) "" else etCedula.text.toString())
                put("en_rucA", if (chkRuc.isChecked) etCedula.text.toString() else "")
                put("en_tipoId", when {
                    chkRuc.isChecked -> 37
                    chkPasaporte.isChecked -> 38
                    else -> 36
                })
                put("en_nombre1", etNombre1.text.toString())
                put("en_nombre2", etNombre2.text.toString())
                put("en_apellido1", etApellido1.text.toString())
                put("en_apellido2", etApellido2.text.toString())
                put("en_razoncomercial", etNombreComercial.text.toString())
                put("en_razonsocial", etRazonSocial.text.toString())
                put("ci_codigo", ci_codigo)
                put("en_fechanac", etFechaNacimiento.text.toString())
                put("us_usuarioing", usuario)
            }

            val updated = db.update("cc_ws_ente", values, "en_codigo = ?", arrayOf(codigo.toString()))
            if (updated == 0) db.insert("cc_ws_ente", null, values)
            db.setTransactionSuccessful()
            Toast.makeText(requireContext(), "Datos guardados", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage("No se pudo guardar: ${e.message}")
                .setPositiveButton("Aceptar", null)
                .show()
        } finally {
            runCatching { db.endTransaction() }
        }
    }

    private fun fnOpenImagePicker() {
        pickImage.launch("image/*")
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST) {
            data?.data?.let { processImage(it) }
        }
    }

    private fun processImage(uri: Uri) {
        val image = runCatching { InputImage.fromFilePath(requireContext(), uri) }.getOrElse {
            Toast.makeText(requireContext(), "No se pudo leer la imagen", Toast.LENGTH_SHORT).show()
            return
        }
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            .process(image)
            .addOnSuccessListener { result ->
                val texto = result.text.orEmpty()
                val identificacion = Regex("\\b\\d{10,13}\\b").find(texto)?.value.orEmpty()
                if (identificacion.isNotBlank()) etCedula.setText(identificacion)

                val lineas = texto.lines().map { it.trim() }.filter { it.isNotBlank() }
                if (etNombre1.text.isNullOrBlank()) etNombre1.setText(lineas.getOrNull(0).orEmpty())
                if (etApellido1.text.isNullOrBlank()) etApellido1.setText(lineas.getOrNull(1).orEmpty())
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error OCR: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun seleccionarSpinner(spinner: Spinner, valor: String) {
        val buscado = valor.trim()
        for (i in 0 until spinner.adapter.count) {
            val item = spinner.adapter.getItem(i)
            val texto = item?.toString()?.trim().orEmpty()
            val codigo = runCatching {
                item?.javaClass?.getDeclaredField("codigo")?.apply { isAccessible = true }?.get(item)?.toString()
            }.getOrNull().orEmpty()
            if (texto.equals(buscado, true) || codigo.equals(buscado, true)) {
                spinner.setSelection(i)
                return
            }
        }
    }

    private fun fnLimpiarSeleccionCategorias() {
        if (::categoriaAdapter.isInitialized) categoriaAdapter.fnLimpiarSeleccion()
        codigosSeleccionados = emptyList()
    }

    private fun fnInicializarCategorias() {
        listaCategorias.clear()
        runCatching { listaCategorias.addAll(llenarControles.fnObtenerCategorias()) }
        categoriaAdapter = CategoriaAdapter(listaCategorias) { categoria ->
            codigosSeleccionados = listaCategorias.filter { it.seleccionada }.map { it.codigo }
        }
        rvCategorias.layoutManager = FlexboxLayoutManager(requireContext())
        rvCategorias.adapter = categoriaAdapter
    }

    private fun fnMarcarCategoriasCliente(cl_codigo: Int) {
        val seleccionadas: Set<Int> = runCatching {
            llenarControles.fnObtenerCategoriasPorCliente(cl_codigo)
        }
            .getOrDefault(emptyList<Int>())
            .toSet()

        listaCategorias.forEach { categoria ->
            categoria.seleccionada = categoria.codigo in seleccionadas
        }

        categoriaAdapter.notifyDataSetChanged()
    }

    fun fnSincronizarCiudadesProvinciasparroquias() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sincronizar")
            .setMessage("Desea sincronizar ciudades, provincias y parroquias?")
            .setPositiveButton("Si") { _, _ -> fnSincronizar() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showProgressDialog(): ProgressDialog {
        return ProgressDialog(requireContext()).apply {
            setMessage("Sincronizando...")
            setCancelable(false)
            show()
        }
    }

    fun fnSincronizar() {
        val id = getString(R.string.str_id).toInt()
        val cadena = "2,0,5"
        var database: SQLiteDatabase? = null
        database = dbHelper.writableDatabase
        val progressDialog = showProgressDialog()

        clsObtenerDatos(
            context = requireContext(),
            solicitudSoap = solicitudSoap,
            id = id,
            cadena = cadena,
            onSuccess = { xml ->
                database?.execSQL("DELETE FROM se_ciudad")
                database?.execSQL("DELETE FROM se_provincia")
                database?.execSQL("DELETE FROM cc_ws_dinardapParroquia")
                database?.execSQL("DELETE FROM cc_ws_dinardapProvincia")
                database?.execSQL("DELETE FROM cc_ws_dinardapCanton")
                database?.execSQL("DELETE FROM cc_ws_clienteCategoria")
                XmlParserProvinciaCiudad.parseMultiTable(xml, database, requireContext())

                Toast.makeText(requireContext(), "Sincronizacion completa", Toast.LENGTH_SHORT).show()
                fnLLenarAdaptadores()
                progressDialog.dismiss()
            },
            onError = { ex ->
                progressDialog.dismiss()
                Toast.makeText(requireContext(), errorMessage ?: "Error al sincronizar", Toast.LENGTH_LONG).show()
            }
        ).execute()
    }


    fun fnEnviar() {
        AlertDialog.Builder(requireContext())
            .setTitle("Enviar")
            .setMessage("Desea enviar los datos del ente?")
            .setPositiveButton("Si") { _, _ -> fnEnviarDatosEnte()}
            .setNegativeButton("No", null)
            .show()
    }


    fun fnEnviarDatosEnte() {
        val progressDialog = showProgressDialogEnviar()

        val enCodigo = etCodigo.text.toString().toIntOrNull() ?: dq_interno
        val id = getString(R.string.str_ente).toInt()
        val cadena = xmlEnte.obtenerXmlInforme(enCodigo, dq_interno)

        clsObtenerDatos(
            context = requireContext(),
            solicitudSoap = solicitudSoap,
            id = 2647,//id,
            cadena = cadena,
            onSuccess = { xml ->
                val database = dbHelper.writableDatabase

                val result = XMlParserEnte.parserEnte(
                    xml.orEmpty(),
                    database,
                    enCodigo,
                    requireContext()
                )

                if (!result.isNullOrBlank()) {
                    fnEnviarDatosCliente(progressDialog, result.toInt())
                } else {
                    progressDialog.dismiss()
                    showToast("No se pudo enviar el ente")
                }
            },
            onError = { ex ->
                progressDialog.dismiss()
                Log.e("OCRFragment", "Error enviando ente: ${ex.message}", ex)
                showToast("Error enviando ente")
            }
        ).execute()
    }

    fun fnEnviarDatosCliente(
        progressDialog: ProgressDialog,
        ente: Int
    ) {
        solicitudSoap = SolicitudSoap(requireContext())

        val enCodigo = etCodigo.text.toString().toIntOrNull() ?: dq_interno
        val id = 2648//getString(R.string.str_cliente).toInt()
        val cadena = xmlEnte.fnXmlCupoCliente(
            ente,
            usuario,
            enCodigo
        )

        clsObtenerDatos(
            context = requireContext(),
            solicitudSoap = solicitudSoap,
            id = id,
            cadena = cadena,
            onSuccess = { xml ->
                val database = dbHelper.writableDatabase

                val result = XMlParserEnte.parserCliente(
                    xml.orEmpty(),
                    database,
                    enCodigo,
                    ente,
                    requireContext()
                )

                if (!result.isNullOrBlank()) {
                    fnEnviarDatosPoliticaCampania(progressDialog, result.toInt())
                } else {
                    progressDialog.dismiss()
                    showToast("No se pudo enviar el cliente")
                }
            },
            onError = { ex ->
                progressDialog.dismiss()
                Log.e("OCRFragment", "Error enviando cliente: ${ex.message}", ex)
                showToast("Error enviando cliente")
            }
        ).execute()
    }

    fun fnEnviarDatosPoliticaCampania(
        progressDialog: ProgressDialog,
        ente: Int
    ) {
        solicitudSoap = SolicitudSoap(requireContext())

        val lopdp = fnCheckValue(chkPoliticaSi, chkPoliticaNo)
        val campania = fnCheckValue(chkNovedadesSi, chkNovedadesNo)

        val id = 2649//getString(R.string.str_Politica).toInt()
        val cadena = "7,$ente,$lopdp,$campania,'$usuario',0"

        clsObtenerDatos(
            context = requireContext(),
            solicitudSoap = solicitudSoap,
            id = id,
            cadena = cadena,
            onSuccess = { xml ->
                progressDialog.dismiss()

                if (!xml.isNullOrBlank()) {
                    showResultDialog(ente.toString())
                } else {
                    showToast("No se pudo enviar política/campaña")
                }
            },
            onError = { ex ->
                progressDialog.dismiss()
                Log.e("OCRFragment", "Error enviando política/campaña: ${ex.message}", ex)
                showToast("Error enviando política/campaña")
            }
        ).execute()
    }

    private fun showProgressDialogEnviar(): ProgressDialog {
        return ProgressDialog(requireContext()).apply {
            setMessage("Enviando...")
            setCancelable(false)
            show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun fnCheckValue(chkSi: CheckBox, chkNo: CheckBox): Int {
        return when {
            chkSi.isChecked -> 1
            chkNo.isChecked -> 0
            else -> 0
        }
    }

    private fun showResultDialog(pedido: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("# Cliente: $pedido")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

        btnEnvioCL.isEnabled = false
    }


    private fun fnVerificarIdentificacion() {
        val tipo = if (chkPasaporte.isChecked) "S" else "N"

        val id = getString(R.string.str_CcValidaIdentificacion).toInt()
        val identificacion = etCedula.text.toString().trim()

        if (identificacion.isBlank()) return

        val cadena = "'$identificacion','0','$tipo'"

        clsObtenerDatos(
            context = requireContext(),
            solicitudSoap = solicitudSoap,
            id = id,
            cadena = cadena,
            onSuccess = { xml ->
                val valores = fnObtenerValoresXml(xml.orEmpty())

                if (valores.isNotEmpty() && valores[0] == "-1") {
                    val mensaje = valores.getOrNull(1).orEmpty()
                    showResultDialogId(mensaje)
                }
            },
            onError = { ex ->
                Log.e("Tarifa", "Falló: ${ex.message}", ex)
            }
        ).execute()
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun fnObtenerValoresXml(xmlResult: String?): List<String> {
        val resultados = mutableListOf<String>()

        if (xmlResult.isNullOrBlank()) {
            return resultados
        }

        try {
            val parser = Xml.newPullParser()
            parser.setFeature(
                "http://xmlpull.org/v1/doc/features.html#process-namespaces",
                false
            )
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

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return resultados
    }

    private fun showResultDialogId(vlpResultado: String) {
        if (chkRuc.isChecked) {
            chkRuc.isChecked = false
        } else if (chkPasaporte.isChecked) {
            chkPasaporte.isChecked = false
        }

        etCedula.setText("")
        etCedula.requestFocus()

        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage(vlpResultado)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun validarAlPerderFoco(
        hasFocus: Boolean,
        campo: EditText,
        mensajeError: String,
        esValido: () -> Boolean
    ) {
        if (!hasFocus && campo.text.toString().isNotBlank()) {
            campo.post {
                if (!esValido()) campo.error = mensajeError else campo.error = null
            }
        }
    }
}


data class DatosEnte(
    val en_codigo: Int,
    val en_codigoA: Int,
    val en_tipoId: Int,
    val en_identificacion: String,
    val en_nombre1: String,
    val en_nombre2: String,
    val en_apellido1: String,
    val en_apellido2: String,
    val en_razoncomercial: String,
    val en_razonsocial: String,
    val en_genero: String,
    val en_tipopersona: String,
    val ci_codigo: Int,
    val en_fechanac: String,
    val cl_politica: Int,
    val cl_campania: Int,
    val direccion: String,
    val celular: String,
    val correo: String,
    val estado: String,
    val parroquia: Int
)
