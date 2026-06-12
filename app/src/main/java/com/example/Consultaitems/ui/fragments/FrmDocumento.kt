package com.example.Consultaitems.ui.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.adapters.AdaptadorClientes
import com.example.Consultaitems.ui.adapters.AdaptadorDoc
import com.example.Consultaitems.ui.adapters.AdaptadorFact
import com.example.Consultaitems.ui.adapters.ClienteAdapter
import com.example.Consultaitems.ui.adapters.Documento
import com.example.Consultaitems.ui.adapters.Factura
import com.example.Consultaitems.ui.adapters.ItemSpinner
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.consultaCliente
import com.example.Consultaitems.utils.parser.XmlDoc
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val URL_DOC = "http://181.39.14.142/siteWeb02/de/"

class frmDocumento :
    Fragment(),
    AdaptadorDoc.OnItemClickListener,
    AdaptadorDoc.OnImageClickListener,
    consultaCliente.OnItemSelectedListener {

    private var isDatePickerShown = false

    private lateinit var adaptadorFactura: AdaptadorFact
    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var txtClienteDC: AutoCompleteTextView
    private lateinit var txtFechaInicial: TextView
    private lateinit var txtFechaFinal: TextView
    private lateinit var btnBusquedaDC: Button
    private lateinit var btnClienteDC: ImageButton
    private lateinit var RecyclerViewDC: RecyclerView
    private lateinit var recyclerViewDF: RecyclerView
    private lateinit var adaptadorDocumento: AdaptadorDoc
    private lateinit var spnTipoDC: Spinner

    private val datosList = mutableListOf<Documento>()
    private val datosFact = mutableListOf<Factura>()

    lateinit var solicitudSoap: SolicitudSoap
    lateinit var dbHelper: SqLiteOpenHelper
    lateinit var downloadedFile: File

    var archivo: String = ""

    private var vendedor: String = ""
    private var cl_codigo: String = ""
    private var tipo: String = ""
    private var anio: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.frmdocumento, container, false)

        llenarControles = ClsLLenarControles(requireContext())
        txtClienteDC = view.findViewById(R.id.txtClienteDC)
        txtFechaInicial = view.findViewById(R.id.txtFechaInicialDC)
        txtFechaFinal = view.findViewById(R.id.txtFechaFinalDC)
        btnBusquedaDC = view.findViewById(R.id.btnBusquedaDC)
        btnClienteDC = view.findViewById(R.id.btnClienteDC)
        RecyclerViewDC = view.findViewById(R.id.recyclerViewDC)
        spnTipoDC = view.findViewById(R.id.spnTipoDC)

        adaptadorDocumento = AdaptadorDoc(datosList, this, this)
        adaptadorFactura = AdaptadorFact(datosFact)
        dbHelper = SqLiteOpenHelper(requireContext())
        vendedor = llenarControles.fnObtenerVendedor()

        val textView1: TextView = view.findViewById(R.id.textView1)
        val textView2: TextView = view.findViewById(R.id.textView2)

        fnLLenarSpiner(view)
        fnCargarAdapters()

        val gvTipo = vendedor.toIntOrNull()?.let {
            llenarControles.fnObtenerTipoVendedorLogin(it)
        }

        if (gvTipo == "2") {
            btnClienteDC.visibility = View.VISIBLE
            txtClienteDC.isEnabled = false
        }

        txtFechaInicial.setOnClickListener {
            if (!isDatePickerShown) {
                showDatePickerDialog(txtFechaInicial)
            }
        }

        txtFechaFinal.setOnClickListener {
            if (!isDatePickerShown) {
                showDatePickerDialog(txtFechaFinal)
            }
        }

        btnBusquedaDC.setOnClickListener {
            hideSoftKeyboard()
            fnConsultarItems()
        }

        btnClienteDC.setOnClickListener {
            fnCliente()
        }

        spnTipoDC.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, itemView: View, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position) as ItemSpinner
                adaptadorDocumento.clearItems()
                tipo = item.codigo

                when (position) {
                    0 -> {
                        textView1.text = "Guia Remisión"
                        textView2.text = "Guia Transpt."
                    }
                    1 -> {
                        textView1.text = "Tipo"
                        textView2.text = "Factura"
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }

        return view
    }

    override fun onItemClick(item: Documento) {
        // Sin acción por ahora.
    }

    override fun onImageClick(serie: String, clave: String, fecha: String) {
        val downloadTask = DownloadPdfTask()
        archivo = serie
        anio = fecha.substringAfterLast("/")
        downloadTask.execute("$URL_DOC$tipo/$anio/$serie/$clave.pdf")
    }

    override fun onFirstImageClick(Interno: String, Sri: String) {
        fnDetallFactura(Interno, Sri)
    }

    fun fnDetallFactura(Interno: String, Sri: String) {
        val datosFact = fnLlenarAdaptadorFacturas(Interno)

        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.frm_mostrar_facturas, null)

        val txtFacturaF: TextView = view.findViewById(R.id.txtFacturaF)
        val txtSriF: TextView = view.findViewById(R.id.txtSriF)
        val txtDescuentoF: TextView = view.findViewById(R.id.txtDescuentoF)
        val txtLoteF: TextView = view.findViewById(R.id.txtLoteF)
        val txtObservacionF: TextView = view.findViewById(R.id.txtObservacionF)

        txtFacturaF.text = Interno
        txtSriF.text = Sri

        if (datosFact.isNotEmpty()) {
            val primerDato = datosFact[0]
            txtDescuentoF.text = primerDato.descuento
            txtLoteF.text = primerDato.lote
            txtObservacionF.text = primerDato.observacion
        } else {
            txtDescuentoF.text = ""
            txtLoteF.text = ""
            txtObservacionF.text = ""
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewDF)
        val adapter = AdaptadorFact(datosFact)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        AlertDialog.Builder(requireContext())
            .setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
            .setView(view)
            .setCancelable(true)
            .show()
    }

    private fun fnLlenarAdaptadorFacturas(Interno: String): MutableList<Factura> {
        val datosFact = mutableListOf<Factura>()
        val resultados = llenarControles.fnObtenerFactura(Interno)

        for (dato in resultados) {
            datosFact.add(dato)
        }

        return datosFact
    }

    fun fnLLenarSpiner(view: View) {
        val listaItems = listOf(
            ItemSpinner("1", "Factura"),
            ItemSpinner("2", "Nota de credito"),
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinner: Spinner = view.findViewById(R.id.spnTipoDC)
        spinner.adapter = adapter
    }

    fun fnConsultarItems() {
        if (isNetworkAvailable(requireContext())) {
            solicitudSoap = SolicitudSoap(requireContext())
            val progressDialog = showProgressDialog()
            MiAsyncTask(progressDialog).execute()
        } else {
            Toast.makeText(requireContext(), "Verifique su conexión a internet", Toast.LENGTH_LONG).show()
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun showProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando Datos...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    private fun fnCargarAdapters() {
        val adaptercliente = ClienteAdapter(requireContext(), llenarControles.fnCargarClientes())

        txtClienteDC.setAdapter(adaptercliente)
        txtClienteDC.setOnItemClickListener { _, _, position, _ ->
            val cliente = adaptercliente.getItem(position)

            if (cliente != null) {
                txtClienteDC.setText(cliente.nombre, false)
                cl_codigo = cliente.id
            }
        }
    }

    fun disableSSLVerification() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<java.security.cert.X509Certificate>,
                        authType: String,
                    ) = Unit

                    override fun checkServerTrusted(
                        chain: Array<java.security.cert.X509Certificate>,
                        authType: String,
                    ) = Unit

                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                        return arrayOf()
                    }
                },
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class DownloadPdfTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg urls: String?): Boolean {
            val url = urls[0] ?: return false

            disableSSLVerification()

            return try {
                val connection = URL(url).openConnection()
                val inputStream = connection.getInputStream()

                val outputDirectory = File("/storage/emulated/0/Download/")
                outputDirectory.mkdirs()

                val fechaActual = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(Date())
                val outputFile = File(outputDirectory, "${archivo}_$fechaActual.pdf")

                inputStream.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
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
                fnOpenPdf(downloadedFile)
                "Archivo descargado exitosamente"
            } else {
                "¡Error al descargar el archivo!"
            }

            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun fnOpenPdf(file: File) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file,
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No hay una aplicación para abrir PDFs", Toast.LENGTH_LONG).show()
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

    private fun showDatePickerDialog(targetView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = formatDate(selectedYear, selectedMonth, selectedDay)
                targetView.text = formattedDate
                isDatePickerShown = false
            },
            year,
            month,
            day,
        )

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
        return String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)
    }

    private inner class MiAsyncTask(private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase
        private var datosInsertados: String = ""

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {
            var pedido: String? = null

            try {
                database = dbHelper.writableDatabase

                database.execSQL("DELETE FROM fa_ws_cabfactura")
                database.execSQL("DELETE FROM fa_ws_detfactura")

                val cadena = "2,'${txtFechaInicial.text}','${txtFechaFinal.text}',$cl_codigo,$tipo,$vendedor,1,''"
                Log.e("Cadena", cadena)

                solicitudSoap.initializeVariables(getString(R.string.str_Documento).toInt(), cadena)

                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.bufferedReader()?.use { it.readText() }

                if (!result.isNullOrBlank()) {
                    pedido = XmlDoc.parseCabFactura(result, database, cadena, requireContext())
                }
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
            }

            return pedido
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            fnLlenarAdaptador()
        }
    }

    private fun fnLlenarAdaptador() {
        datosList.clear()
        adaptadorDocumento.clearItems()

        val resultados = llenarControles.fnObtenerDocumento()

        for (dato in resultados) {
            datosList.add(dato)
        }

        val layoutManager = LinearLayoutManager(requireContext())
        RecyclerViewDC.layoutManager = layoutManager
        RecyclerViewDC.adapter = adaptadorDocumento
    }

    private fun fnCliente() {
        val dialog = consultaCliente()
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "consultaCliente")
    }

    override fun onItemsSelected(clientes: AdaptadorClientes.Clientes) {
        cl_codigo = clientes.codigo

        val nombre = clientes.nombre
        val razonComercial = clientes.razonComercial

        txtClienteDC.setText(
            when {
                !nombre.isNullOrBlank() -> nombre
                !razonComercial.isNullOrBlank() -> razonComercial
                else -> clientes.en_razonsocial
            },
        )
    }
}
