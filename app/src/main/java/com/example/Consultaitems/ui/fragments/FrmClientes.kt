package com.example.Consultaitems.ui.fragments

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.adapters.AdaptadorDatosDelCliente
import com.example.Consultaitems.ui.adapters.clienteD
import com.example.Consultaitems.utils.cls.ClienteDatos
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.RegistrarCoordenadas
import com.example.Consultaitems.utils.parser.XmlClienteAct
import com.example.Consultaitems.utils.parser.xmlClientesAct
import com.example.Consultaitems.utils.parser.xmlLocalizacion
import com.example.Consultaitems.utils.parser.xmlParserRuta
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val URL_PDF = "https://app.cotzul.com/sitenet/ec/"

class frmClientes : Fragment(),
    AdaptadorDatosDelCliente.OnItemClickListener,
    AdaptadorDatosDelCliente.OnImageClickListener {

    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var txtClienteCL: EditText
    private lateinit var recyclerViewCL: RecyclerView
    private lateinit var adaptadorDatosCliente: AdaptadorDatosDelCliente
    private lateinit var ClaseXml: xmlLocalizacion
    private lateinit var ClaseXmlCliente: XmlClienteAct

    private val datosList = mutableListOf<clienteD>()

    lateinit var dbHelper: SqLiteOpenHelper
    lateinit var solicitudSoap: SolicitudSoap

    var archivo: String = ""
    var cl_codigo: String = ""

    private var downloadedFile: File? = null
    private var errorMessage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        val view = inflater.inflate(R.layout.frmcliente, container, false)

        llenarControles = ClsLLenarControles(requireContext())
        txtClienteCL = view.findViewById(R.id.txtClienteCL)
        recyclerViewCL = view.findViewById(R.id.recyclerViewCL)
        adaptadorDatosCliente = AdaptadorDatosDelCliente(datosList, this, this)
        dbHelper = SqLiteOpenHelper(requireContext())

        fnLlenarAdaptador()

        txtClienteCL.addTextChangedListener(object : TextWatcher {
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
                fnLlenarAdaptador()
            }
        })

        return view
    }

    private fun fnLlenarAdaptador() {
        datosList.clear()
        adaptadorDatosCliente.clearItems()

        val resultados = llenarControles.fnObtenerDatosCliente(txtClienteCL.text.toString())
        for (dato in resultados) {
            datosList.add(dato)
        }

        recyclerViewCL.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewCL.adapter = adaptadorDatosCliente
    }

    fun disableSSLVerification() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
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
                }
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onItemClick(item: clienteD) {
        // Sin acción por ahora
    }

    override fun onCheckBoxClick(codigo: String) {
        val registrarCoordenadas = RegistrarCoordenadas(requireContext(), requireActivity())

        if (
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            registrarCoordenadas.fnRegistrarCoordenadas(codigo) { success ->
                if (success) {
                    cl_codigo = ""
                    fnEnviarInforme()
                }
            }
        } else {
            registrarCoordenadas.solicitarPermisoUbicacion()
        }
    }

    override fun onCheckBoxUncheck(codigo: String) {
        cl_codigo = codigo
        llenarControles.fnCambioEstado(codigo)
        fnEnviarInforme()
    }

    override fun onImageClick(cliente: String, codigo: String) {
        archivo = "${codigo}_${
            cliente
                .replace(" ", "_")
                .replace(".", "_")
                .replace("ñ", "n")
                .replace("Ñ", "N")
        }"

        DownloadPdfTask().execute("$URL_PDF$archivo.pdf")
    }

    override fun onAdicionalClick(Codigo: String) {
        fnActualizarDatosCliente(Codigo)
    }

    fun fnEnviarInforme() {
        if (isNetworkAvailable(requireContext())) {
            try {
                ClaseXml = xmlLocalizacion(requireContext())
                solicitudSoap = SolicitudSoap(requireContext())
                MiAsyncTask().execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(requireContext(), "Sin conexión a internet", Toast.LENGTH_SHORT).show()
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    inner class DownloadPdfTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg urls: String?): Boolean {
            val url = urls.firstOrNull() ?: return false

            disableSSLVerification()

            return try {
                val connection = URL(url).openConnection()
                val outputDirectory = File("/storage/emulated/0/Download/")
                outputDirectory.mkdirs()

                val fechaActual = SimpleDateFormat("ddMMyyyy_HHmmss").format(Date())
                val outputFile = File(outputDirectory, "${archivo}_$fechaActual.pdf")

                connection.getInputStream().use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                downloadedFile = outputFile
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        override fun onPostExecute(result: Boolean) {
            val message = if (result) {
                downloadedFile?.let { fnOpenPdf(it) }
                "Archivo descargado exitosamente"
            } else {
                "¡Error al descargar el archivo!"
            }

            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun fnOpenPdf(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
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
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "No hay una aplicación para abrir PDFs",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private inner class MiAsyncTask : AsyncTask<Void, Void, String?>() {
        private lateinit var database: SQLiteDatabase

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                database = dbHelper.writableDatabase

                val cadena = ClaseXml.obtenerXmlocalizaciones(cl_codigo)

                solicitudSoap.initializeVariables(
                    getString(R.string.str_ruta).toInt(),
                    cadena
                )

                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.bufferedReader()?.use { it.readText() }

                if (result.isNullOrBlank()) {
                    ""
                } else {
                    val recibo = xmlParserRuta.parseAndUpdateDocumentCode(
                        result,
                        database,
                        requireContext()
                    )

                    if (cl_codigo.isNotBlank()) {
                        llenarControles.fnEliminarCheck(cl_codigo)
                    }

                    recibo
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "ERROR"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
        }
    }

    fun fnActualizarDatosCliente(codigo: String) {
        val cliente: ClienteDatos? = if (llenarControles.fnVerficarClienteActualizacion(codigo)) {
            llenarControles.fnDatosClienteActualizacion(codigo)
        } else {
            llenarControles.fnDatosClienteActualizacion(codigo)
        }

        cliente?.let {
            mostrarDialogoCliente(it, codigo)
        }
    }

    fun mostrarDialogoCliente(cliente: ClienteDatos, codigo: String) {
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
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cerrar") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setCancelable(true)
            .create()

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
                    Toast.makeText(
                        requireContext(),
                        "Todos los campos son obligatorios",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (!fnValidarTelefonos(celularNuevo)) {
                    Toast.makeText(
                        requireContext(),
                        "Número telefónico no tiene el formato permitido",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                if (!fnValidarCorreo(emailNuevo)) {
                    Toast.makeText(
                        requireContext(),
                        "Email no tiene el formato permitido",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                val actualizado = llenarControles.fnUpdateDatosCliente(
                    codigo,
                    direccionNueva,
                    celularNuevo,
                    emailNuevo
                )

                if (actualizado) {
                    Toast.makeText(
                        requireContext(),
                        "Cliente actualizado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                    fnLlenarAdaptador()
                } else {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    fun fnValidarTelefonos(input: String): Boolean {
        if (input.isBlank()) return false

        val telefonos = input
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (telefonos.isEmpty()) return false

        for (phone in telefonos) {
            val numero = Regex("[\\s\\-()]").replace(phone, "")
            val esCelular = Regex("^09\\d{8}$").matches(numero)

            if (
                !esCelular ||
                fnTodosIguales(numero) ||
                fnMaxRunIguales(numero) >= 6 ||
                fnEsSecuenciaAscendente(numero) ||
                fnEsSecuenciaDescendente(numero)
            ) {
                return false
            }
        }

        return true
    }

    private fun fnTodosIguales(s: String): Boolean {
        return s.all { it == s[0] }
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
            "^[A-Za-z0-9](?:[A-Za-z0-9._%+\\-]{1,63})@" +
                    "[A-Za-z0-9](?:[A-Za-z0-9\\-]{0,61}[A-Za-z0-9])" +
                    "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9\\-]{0,61}[A-Za-z0-9]))+$"
        )

        val desechables = listOf(
            "mailinator.com",
            "yopmail.com",
            "10minutemail.com",
            "guerrillamail.com",
            "tempmail.com"
        )

        for (correo in correos) {
            if (!pattern.matches(correo)) return false
            if (correo.contains("..")) return false

            val at = correo.lastIndexOf('@')
            if (at < 2) return false

            val domain = correo.substring(at + 1).lowercase()
            val parts = domain.split(".")

            if (parts.size < 2) return false
            if (parts.last().length < 2) return false

            val esDesechable = desechables.any { d ->
                domain == d || domain.endsWith(".$d")
            }

            if (esDesechable) return false
        }

        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.frm_menu_cliente, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btnEnviarCL -> {
                ClaseXmlCliente = XmlClienteAct(requireContext())
                solicitudSoap = SolicitudSoap(requireContext())

                val progressDialog = showProgressDialog()
                MiAsyncTaskActualizacionCliente(progressDialog).execute()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Enviando Datos...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    private inner class MiAsyncTaskActualizacionCliente(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String?>() {

        private lateinit var database: SQLiteDatabase

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                database = dbHelper.writableDatabase

                val cadena = ClaseXmlCliente.obtenerXmlInforme()

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
                Toast.makeText(
                    requireContext(),
                    errorMessage.ifBlank { "Error al enviar datos" },
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        private fun showResultDialog(pedido: String) {
            AlertDialog.Builder(requireContext())
                .setTitle("Sistema")
                .setMessage("Datos enviados correctamente")
                .setPositiveButton("OK") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()
        }
    }
}