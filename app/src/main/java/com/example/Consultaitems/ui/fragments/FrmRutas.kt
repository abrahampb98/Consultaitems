package com.example.Consultaitems.ui.fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.adapters.AdaptadorRutaNueva
import com.example.Consultaitems.ui.adapters.AdaptadorRutas
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.parser.XmlPedido
import com.example.Consultaitems.utils.parser.xmlParserRuta
import com.example.Consultaitems.utils.parser.xmlRutas
import com.example.Consultaitems.utils.pdf.frmPdfRutaV
import java.io.File
import java.util.Calendar

class FrmRuta: Fragment(), AdaptadorRutas.OnItemClickListener, AdaptadorRutaNueva.OnItemClickListener, AdaptadorRutaNueva.OnImageClickListenerN  {

    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var recyclerViewR: RecyclerView
    private lateinit var recyclerViewR2: RecyclerView
    private lateinit var recyclerViewR3: RecyclerView
    private lateinit var adaptadorDatosCliente: AdaptadorRutas
    private lateinit var adaptadorDatosCliente2: AdaptadorRutas
    private lateinit var adaptadorDatosCliente3: AdaptadorRutaNueva
    private val datosList = mutableListOf<AdaptadorRutas.Rutas>()
    private val datosList2 = mutableListOf<AdaptadorRutas.Rutas>()
    private val datosList3 = mutableListOf<AdaptadorRutaNueva.RutaNueva>()
    private lateinit var txtFechaInicialRV: EditText
    private lateinit var txtFechaFinalRV: EditText
    private lateinit var spinnerDias: Spinner
    lateinit var dbHelper: SqLiteOpenHelper
    private lateinit var btnAgregarRN: ImageButton
    private lateinit var ClaseXml: xmlRutas
    lateinit var solicitudSoap: SolicitudSoap
    var dia: String = ""
    var ruta: String = ""
    var fechaIni = ""
    var fechaFin = ""
    private var semanaEsActual = false

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
        val view = inflater.inflate(R.layout.frm_visita_clientes, container, false)

        spinnerDias = view.findViewById(R.id.spinnerDias)
        llenarControles = ClsLLenarControles(requireContext())
        recyclerViewR = view.findViewById(R.id.recyclerViewR)
        recyclerViewR2 = view.findViewById(R.id.recyclerViewR2)
        recyclerViewR3 = view.findViewById(R.id.recyclerViewR3)
        txtFechaInicialRV = view.findViewById(R.id.txtFechaInicialRV)
        txtFechaFinalRV = view.findViewById(R.id.txtFechaFinalRV)
        btnAgregarRN = view.findViewById(R.id.btnAgregarRN)
        adaptadorDatosCliente = AdaptadorRutas(datosList, this)
        adaptadorDatosCliente2 = AdaptadorRutas(datosList2, this)
        adaptadorDatosCliente3 = AdaptadorRutaNueva(datosList3,this, this)
        dbHelper = SqLiteOpenHelper(requireContext())

        spinnerDias.isEnabled = false

        fnObtenerFechas()

        fnLlenarDias()
        fnLlenarAdaptadorRutas(dia)
        fnLlenarAdaptadorPendientes()
        fnLllenarAdaptadorNuevo()


        btnAgregarRN.setOnClickListener {
            fnRutaNueva(
                requireContext() // Contexto en una Activity
            ) { codigo, cliente ->
                fnRegistrarNuevaVisita(codigo)

            }
        }


        spinnerDias.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                dia = parent.getItemAtPosition(position).toString()

                fnLlenarAdaptadorRutas(dia)
                fnLlenarAdaptadorPendientes()
                fnLllenarAdaptadorNuevo()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        return view
    }


    // Inflar el menú en el fragmento
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.frm_menu_ruta, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bntReporteRV -> {
                fnGenerarReporte()
                true
            }
            R.id.btnEnviarRV -> {
                fnEnviarInforme()
                true
            }
            R.id.btnActualizarRV -> {
                fnObtenerFechas()

                fnLlenarDias()
                fnLlenarAdaptadorRutas(dia)
                fnLlenarAdaptadorPendientes()
                fnLllenarAdaptadorNuevo()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun observacion(codigo: String, cliente: String, observacion: String) {
        fnObservacion(codigo, cliente, observacion)
    }

    override fun onItemClick(item: AdaptadorRutas.Rutas) {
       val item = item
        //showToast("$item")
    }

    override fun onCheckBoxClickVisita(codigo: String, check: Int, estado: String) {
       llenarControles.fnRutaVisita(codigo,check,estado)

        //if (estado=="P"){
            fnLlenarAdaptadorPendientes()
            fnLllenarAdaptadorNuevo()
        //}
    }

    override fun onCheckBoxClickVenta(codigo: String, check: Int, estado: String) {
        llenarControles.fnRutaVenta(codigo,check,estado)

        //if (estado=="P"){
            fnLlenarAdaptadorPendientes()
            fnLllenarAdaptadorNuevo()
        //}
    }

    override fun onCheckBoxClickCobro(codigo: String, check: Int, estado: String) {
        llenarControles.fnRutaCobro(codigo,check,estado)

        //if (estado=="P"){
            fnLlenarAdaptadorPendientes()
            fnLllenarAdaptadorNuevo()
        //}
    }

    override fun onCheckBoxClickGT(codigo: String, check: Int, estado: String) {
        llenarControles.fnRutaGT(codigo, check, estado)

        recyclerViewR2.post {
            fnLlenarAdaptadorPendientes()
            fnLllenarAdaptadorNuevo()
        }
    }


    override fun observacionN(codigo: String, cliente: String, observacion: String) {
        fnObservacion(codigo, cliente, observacion)
    }


    override fun onItemClick(item: AdaptadorRutaNueva.RutaNueva) {
        val item = item
        //showToast("$item")
    }

    override fun onCheckBoxClickVisitaN(codigo: String, check: Int, estado: String) {
        llenarControles.fnRutaVisita(codigo,check,estado)
    }

    override fun onCheckBoxClickVentaN(codigo: String, check: Int, estado: String) {
        llenarControles.fnRutaVenta(codigo,check,estado)
    }

    override fun onCheckBoxClickCobroN(codigo: String, check: Int, estado: String) {
        llenarControles.fnRutaCobro(codigo,check,estado)
    }

    override fun onCheckBoxClickGTN(codigo: String, check: Int, estado: String) {
        llenarControles.fnRutaGT(codigo,check,estado)
    }

    fun fnObtenerFechas() {
        val semana = llenarControles.fnFechaRutaVigente()

        if (semana != null) {
            fechaIni = semana.inicial
            fechaFin = semana.final
            semanaEsActual = semana.esActual

            txtFechaInicialRV.setText(convertirFechaSqlAPantalla(fechaIni))
            txtFechaFinalRV.setText(convertirFechaSqlAPantalla(fechaFin))
        } else {
            fechaIni = ""
            fechaFin = ""
            semanaEsActual = false

            txtFechaInicialRV.setText("")
            txtFechaFinalRV.setText("")
        }
    }

    fun fnLlenarDias() {
        if (fechaIni.isEmpty() || fechaFin.isEmpty()) {
            dia = ""
            return
        }

        val opcionesListado = llenarControles.fnObtenerDiasDisponibles(fechaIni, fechaFin)
            .map { it.trim() }
            .filter { it.isNotEmpty() }


        val adapterListado = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            opcionesListado
        )

        adapterListado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDias.adapter = adapterListado

        if (opcionesListado.isEmpty()) {
            dia = ""
            return
        }

        val calendar = Calendar.getInstance()

        val diaSistema = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Lunes"
            Calendar.TUESDAY -> "Martes"
            Calendar.WEDNESDAY -> "Miércoles"
            Calendar.THURSDAY -> "Jueves"
            Calendar.FRIDAY -> "Viernes"
            Calendar.SATURDAY -> "Viernes"
            Calendar.SUNDAY -> "Viernes"
            else -> "Viernes"
        }

        val diaSeleccionado = if (!semanaEsActual) {
            "Viernes"
        } else if (opcionesListado.any { it.equals(diaSistema, ignoreCase = true) }) {
            diaSistema
        } else {
            opcionesListado.lastOrNull() ?: "Viernes"
        }

        dia = diaSeleccionado

        val indexSeleccion = opcionesListado.indexOfFirst {
            it.equals(diaSeleccionado, ignoreCase = true)
        }

        if (indexSeleccion >= 0) {
            spinnerDias.setSelection(indexSeleccion)
        }
    }


    fun fnObservacion(
        codigo: String,
        cliente: String,
        observacion: String // Se recibe la observación actual
    ) {
        val editText = EditText(context).apply {
            setText(if (observacion.isNotEmpty()) observacion else "") // Si hay observación, la coloca en el EditText
            hint = if (observacion.isEmpty()) "Ingrese su observación" else "" // Si no hay, muestra el hint
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 10)
            addView(editText)
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(cliente)
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevaObservacion = editText.text.toString().trim()
                if (nuevaObservacion.isNotEmpty()) {
                    // Guardar la nueva observación en la base de datos
                    llenarControles.fnRutaObservacion(codigo, nuevaObservacion)

                    Toast.makeText(context, "Datos guardados", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Ingrese una observación válida", Toast.LENGTH_SHORT).show()
                }

                fnLlenarAdaptadorRutas(dia)
            }
            .setNegativeButton("Cancelar", null)
            .create()


        dialog.show()
    }


    fun fnRutaNueva(
        context: Context,
        onClienteSelected: (String, String) -> Unit
    ) {
        val clientes: List<Pair<String, String>> =
            llenarControles.fnClienteRutaNueva(context, dia, fechaIni, fechaFin)

        if (clientes.isEmpty()) {
            Toast.makeText(context, "No hay clientes disponibles para agregar", Toast.LENGTH_SHORT).show()
            return
        }

        val autoCompleteTextView = AutoCompleteTextView(context).apply {
            hint = "Buscar cliente..."
            threshold = 1
        }

        val clienteMap = clientes.associateBy({ it.second }, { it.first })
        val nombresClientes = clientes.map { it.second }

        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_dropdown_item_1line,
            nombresClientes
        )

        autoCompleteTextView.setAdapter(adapter)

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 10)
            addView(autoCompleteTextView)
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle("Seleccionar Cliente")
            .setView(layout)
            .setNegativeButton("Cancelar", null)
            .create()

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val nombreCliente = adapter.getItem(position)
            val codigoCliente = clienteMap[nombreCliente]

            if (codigoCliente != null && nombreCliente != null) {
                dialog.dismiss()
                onClienteSelected(codigoCliente, nombreCliente)
            }
        }

        autoCompleteTextView.setOnDismissListener {
            if (
                autoCompleteTextView.text.isNotEmpty() &&
                !clienteMap.containsKey(autoCompleteTextView.text.toString())
            ) {
                Toast.makeText(context, "Cliente no encontrado", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }


    private fun fnLlenarAdaptadorRutas(dia: String) {
        datosList.clear()
        adaptadorDatosCliente.clearItems()

        val resultados = llenarControles.fnObtenerRutas(dia, fechaIni, fechaFin)

        for (dato in resultados) {
            datosList.add(dato)
        }

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerViewR.layoutManager = layoutManager
        recyclerViewR.adapter = adaptadorDatosCliente
    }


    private fun fnLlenarAdaptadorPendientes() {
        datosList2.clear()
        adaptadorDatosCliente2.clearItems()

        val resultados = llenarControles.fnObtenerRutasPendientes(
            fechaIni,
            fechaFin,
            dia
        )

        for (dato in resultados) {
            datosList2.add(dato)
        }

        // Configurar el RecyclerView y asignar el adaptador
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerViewR2.layoutManager = layoutManager
        recyclerViewR2.adapter = adaptadorDatosCliente2
    }

    private fun fnLllenarAdaptadorNuevo(){
        datosList3.clear()
        adaptadorDatosCliente3.clearItems()

        val resultados = llenarControles.fnObtenerRutasNuevas(
            fechaIni,
            fechaFin
        )

        for (dato in resultados) {
            datosList3.add(dato)
        }

        // Configurar el RecyclerView y asignar el adaptador
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerViewR3.layoutManager = layoutManager
        recyclerViewR3.adapter = adaptadorDatosCliente3
    }

    private fun fnRegistrarNuevaVisita(codigo: String){

        llenarControles.fnInsertarNuevaVisita(codigo, fechaIni, fechaFin, dia)
        fnLllenarAdaptadorNuevo()
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDeleteItem(codigo: String) {
        llenarControles.fnElininarRutasNuevas(requireContext(), codigo, fechaIni, fechaFin)

        fnLlenarAdaptadorPendientes()
        fnLllenarAdaptadorNuevo()
    }

    fun fnGenerarReporte() {
        val pdfGenerator = frmPdfRutaV(requireContext())

        val file = pdfGenerator.generatePdfRutaVendedor(fechaIni, fechaFin)

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

    fun fnEnviarInforme(){
        if (isNetworkAvailable(requireContext())) {
            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Confirmar envío")
            builder.setMessage("¿Desea enviar los datos?")
            builder.setPositiveButton("Sí") { dialog, which ->
                try {
                    ClaseXml = xmlRutas(requireContext())
                    solicitudSoap = SolicitudSoap(requireContext())
                    val progressDialog = showProgressDialog()
                    MiAsyncTask(progressDialog).execute()
                } catch (e: Exception) {
                    //Log.e("UpdateError", "Error during update: ${e.message}")
                    e.printStackTrace()
                }
            }
            builder.show()
        }
    }

    private fun showProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Enviando...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }


    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

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
                val cadena = ClaseXml.fnObtenerRutas(fechaIni, fechaFin)
                solicitudSoap.initializeVariables(
                    getString(R.string.str_rutaxml).toInt(),
                    cadena
                )

                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.bufferedReader()?.use { it.readText() }
                if (!result.isNullOrBlank()) {
                    ruta = xmlParserRuta.parseAndUpdateDocumentCode(
                        result,
                        database,
                        requireContext()
                    )
                }
                ruta
            } catch (e: Exception) {
                //Log.e("AsyncTask", "Error en doInBackground: ${e.message}", e)
                "ERROR"  // Devuelve un valor distintivo en caso de error
            }
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result != null) {
                progressDialog.dismiss()
                showResultDialog(ruta)
            }
        }

        private fun showResultDialog(recibo: String) {
            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            dialog.setTitle("Mensaje")
            if (recibo != null && recibo != "null" && recibo != "ERROR") {
                dialog.setMessage("Ruta enviada Correctamente")
            } else {
                dialog.setMessage("Fallo el envio")
            }

            dialog.setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.dismiss() }
            dialog.show()
        }

    }

    private fun convertirFechaSqlAPantalla(fechaSql: String): String {
        return try {
            val entrada = java.text.SimpleDateFormat(
                "yyyy-MM-dd",
                java.util.Locale.getDefault()
            )

            val salida = java.text.SimpleDateFormat(
                "dd/MM/yyyy",
                java.util.Locale.getDefault()
            )

            entrada.isLenient = false

            val fechaDate = entrada.parse(fechaSql.substring(0, 10))

            if (fechaDate != null) {
                salida.format(fechaDate)
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}

data class SemanaRuta(
    val inicial: String,
    val final: String,
    val esActual: Boolean
)