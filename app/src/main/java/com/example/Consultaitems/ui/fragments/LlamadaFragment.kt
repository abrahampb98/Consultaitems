package com.example.Consultaitems.ui.fragments

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.util.Xml
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.activities.frmLogin
import com.example.Consultaitems.ui.adapters.AdaptadorProspectos
import com.example.Consultaitems.ui.adapters.AdapterAvance
import com.example.Consultaitems.ui.adapters.AdapterClienteProspecto
import com.example.Consultaitems.utils.cls.Avance
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.Criterio
import com.example.Consultaitems.utils.cls.Llamadas
import com.example.Consultaitems.utils.cls.consultaClienteProspecto
import com.example.Consultaitems.utils.cls.consultaGestionVenta
import com.example.Consultaitems.utils.cls.consultaLlamada
import com.example.Consultaitems.utils.parser.ClienteProspecto
import com.example.Consultaitems.utils.parser.XmlProspecto
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class LlamadaFragment : Fragment(),
    consultaLlamada.OnItemSelectedListener,
    AdapterClienteProspecto.OnItemClickListener,
    AdapterAvance.OnItemClickListener,
    consultaClienteProspecto.OnItemSelectedListener {

    private lateinit var adapterAvance: AdapterAvance
    private lateinit var adapterProspecto: AdapterClienteProspecto
    private lateinit var xmlProspecto: XmlProspecto

    lateinit var btnGuardarLl: Button
    lateinit var etCatalogoLl: EditText
    lateinit var etDireccionLl: EditText
    lateinit var etLineaLl: EditText
    lateinit var etNombreLl: EditText
    lateinit var etNumeroL: EditText
    lateinit var etObservacionLl: EditText
    lateinit var etTipoLl: EditText
    lateinit var etVendedorLl: TextView
    lateinit var ivBusquedaLl: ImageView
    lateinit var ivClienteLl: ImageView
    lateinit var lLenarControles: ClsLLenarControles
    lateinit var rvAvances: RecyclerView
    lateinit var rvClientes: RecyclerView
    lateinit var solicitudSoap: SolicitudSoap
    lateinit var spinnerRedLl: Spinner
    lateinit var txtAvancesLl: TextView
    lateinit var txtFechaLl: TextView

    private var clienteSeleccionado: AdaptadorProspectos.Prospectos? = null
    private lateinit var criterios: List<Criterio>
    private var isDatePickerShown = false
    private var opcion = 0
    private var posicion = -1
    private var rs_codigo = 0
    private val ep_codigo: Int = frmLogin.CadenaHolder.ep_codigo
    private val listaCliente = mutableListOf<ClienteProspecto>()
    private val listaAvance = mutableListOf<Avance>()
    private var usuario = ""
    private var vgsOpcionMenu = ""
    private var xmlDatos = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_llamada, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        etNumeroL = view.findViewById(R.id.etNumeroL)
        ivBusquedaLl = view.findViewById(R.id.ivBusquedaLl)
        txtFechaLl = view.findViewById(R.id.txtFechaLl)
        etVendedorLl = view.findViewById(R.id.etVendedorLl)
        ivClienteLl = view.findViewById(R.id.ivClienteLl)
        etNombreLl = view.findViewById(R.id.etNombreLl)
        etTipoLl = view.findViewById(R.id.etTipoLl)
        etDireccionLl = view.findViewById(R.id.etDireccionLl)
        rvClientes = view.findViewById(R.id.rvClientes)
        etLineaLl = view.findViewById(R.id.etLineaLl)
        etObservacionLl = view.findViewById(R.id.etObservacionLl)
        spinnerRedLl = view.findViewById(R.id.spinnerRedLl)
        etCatalogoLl = view.findViewById(R.id.etCatalogoLl)
        rvAvances = view.findViewById(R.id.rvAvances)
        txtAvancesLl = view.findViewById(R.id.etAvancesLl)
        btnGuardarLl = view.findViewById(R.id.btnGuardarLl)

        lLenarControles = ClsLLenarControles(requireContext())
        lLenarControles.fnLLenarVendedor()?.let { vendedor ->
            usuario = vendedor.login
        }

        fnControles(false)
        txtFechaLl.text = fnFecha()
        etVendedorLl.text = lLenarControles.fnObtenerNombreUsuario(ep_codigo)

        adapterProspecto = AdapterClienteProspecto(listaCliente) { item, position ->
            onItemClick(item, position)
        }
        adapterAvance = AdapterAvance(listaAvance) { item: Avance, position: Int ->
            onItemClick(item, position)
        }

        rvClientes.layoutManager = LinearLayoutManager(context)
        rvClientes.adapter = adapterProspecto
        rvClientes.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        rvAvances.layoutManager = LinearLayoutManager(context)
        rvAvances.adapter = adapterAvance
        rvAvances.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        fnLlenarSpinnerCriterios()

        ivBusquedaLl.setOnClickListener { fnLlamadas() }
        ivClienteLl.setOnClickListener {
            if (listaCliente.isEmpty() || fnVerificarControles()) {
                fnActualizarItemSeleccionado()
                fnCliente()
            }
        }
        txtFechaLl.setOnClickListener {
            if (!isDatePickerShown) showDatePickerDialog(txtFechaLl)
        }

        etNumeroL.setOnEditorActionListener { _, actionId, event ->
            if (actionId == 6 || actionId == 2 || actionId == 3 ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                etNumeroL.isEnabled = false
                fnControles(true)
                solicitudSoap = SolicitudSoap(requireContext())
                val codigo = etNumeroL.text.toString().toIntOrNull() ?: 0
                MiAsyncTaskGestionVenta(showProgressDialog(), codigo).execute()
                true
            } else {
                false
            }
        }

        etNombreLl.setOnEditorActionListener { _, actionId, event ->
            if (actionId == 6 || actionId == 2 || actionId == 3 ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                clienteSeleccionado?.let { seleccionado ->
                    fnAgregarProspecto(seleccionado)
                    clienteSeleccionado = null
                }
                true
            } else {
                false
            }
        }

        btnGuardarLl.setOnClickListener {
            if (fnVerificarControles()) {
                fnAlertDialog(
                    requireContext(),
                    "¿Deseas guardar los datos?",
                    mostrarCancelar = true
                ) {
                    fnGuardarDatos()
                }
            }
        }

        spinnerRedLl.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                rs_codigo = criterios.getOrNull(position)?.codigo ?: 0
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.frm_menu_llamada, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bntNuevoLl -> {
                fnAccionesPulsarNuevo()
                true
            }
            R.id.btnConsultarLl -> {
                fnAccionesPulsarConsultar()
                true
            }
            R.id.btnEliminarLl -> {
                fnAccionesPulsarEliminar()
                true
            }
            R.id.btnReporteLl -> {
                fnAcionesPulsarReporte()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fnLlenarSpinnerCriterios() {
        criterios = listOf(
            Criterio(1, "Ninguno"),
            Criterio(2, "Meta"),
            Criterio(3, "Escala"),
            Criterio(4, "Google"),
            Criterio(5, "Volante"),
            Criterio(6, "Whatsapp")
        )

        val adapterCriterios = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            criterios.map { it.descripcion }
        )
        adapterCriterios.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRedLl.adapter = adapterCriterios
    }

    private fun showDatePickerDialog(targetView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                targetView.text = formatDate(selectedYear, selectedMonth, selectedDay)
                isDatePickerShown = false
            },
            year,
            month,
            day
        )
        datePicker.setOnDismissListener { isDatePickerShown = false }
        datePicker.setOnCancelListener { isDatePickerShown = false }
        datePicker.show()
        isDatePickerShown = true
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)
    }

    private fun fnControles(enabled: Boolean) {
        etNombreLl.isEnabled = enabled
        etTipoLl.isEnabled = enabled
        etDireccionLl.isEnabled = enabled
        ivClienteLl.isEnabled = enabled
        etLineaLl.isEnabled = enabled
        etObservacionLl.isEnabled = enabled
        spinnerRedLl.isEnabled = enabled
        etCatalogoLl.isEnabled = enabled
        txtAvancesLl.isEnabled = enabled
        rvAvances.isEnabled = enabled
        rvClientes.isEnabled = enabled
        btnGuardarLl.isEnabled = enabled

        val alpha = if (enabled) 1.0f else 0.5f
        etNombreLl.alpha = alpha
        etTipoLl.alpha = alpha
        etDireccionLl.alpha = alpha
        etLineaLl.alpha = alpha
        etObservacionLl.alpha = alpha
        spinnerRedLl.alpha = alpha
        etCatalogoLl.alpha = alpha
        txtAvancesLl.alpha = alpha
        btnGuardarLl.alpha = alpha
    }

    private fun fnLimpiarControles() {
        etNumeroL.text?.clear()
        etNombreLl.text?.clear()
        etTipoLl.text?.clear()
        etDireccionLl.text?.clear()
        etLineaLl.text?.clear()
        etObservacionLl.text?.clear()
        etCatalogoLl.text?.clear()
        spinnerRedLl.setSelection(0)
        txtAvancesLl.text = ""
        rvClientes.scrollToPosition(0)
        rvAvances.scrollToPosition(0)
        adapterProspecto.clearItems()
        adapterAvance.clearItems()
        txtFechaLl.text = fnFecha()
    }

    private fun fnFecha(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    fun fnAccionesPulsarConsultar() {
        fnLimpiarControles()
        fnControles(false)
        etNumeroL.isEnabled = true
        ivBusquedaLl.isEnabled = true
        etNumeroL.requestFocus()
        txtFechaLl.isEnabled = false
        vgsOpcionMenu = "M"
        opcion = 1
    }

    fun fnAccionesPulsarNuevo() {
        fnLimpiarControles()
        fnControles(true)
        txtFechaLl.isEnabled = true
        etNumeroL.clearFocus()
        vgsOpcionMenu = "I"
        opcion = 1
        fnVerificarDocumento()
    }

    fun fnAcionesPulsarReporte() {
        val dialog = consultaGestionVenta()
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "consultaCliente")
    }

    fun fnAccionesPulsarEliminar() {
        if (etNumeroL.text.toString().isNotEmpty()) {
            opcion = 2
            fnControles(false)
            fnAlertDialog(
                requireContext(),
                "¿Esta seguro de eliminar los datos?",
                mostrarCancelar = true
            ) {
                xmlProspecto = XmlProspecto(requireContext())
                solicitudSoap = SolicitudSoap(requireContext())
                MiAsyncTaskProspecto(showProgressDialog()).execute()
            }
        }
    }

    fun fnVerificarDocumento() {
        solicitudSoap = SolicitudSoap(requireContext())
        MiAsyncTaskVerificaDoc(showProgressDialog()).execute()
    }

    private inner class MiAsyncTaskVerificaDoc(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                val fecha = fnFecha()
                val cadena = "2,$ep_codigo,0,'','$fecha','',22,0"
                solicitudSoap.initializeVariables(
                    getString(R.string.str_ConsultaGestionVenta).toInt(),
                    cadena
                )
                solicitudSoap.realizarSolicitudSoap()?.readTextUtf8()
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val resultado = fnObtenerValorXml(result)
            if (resultado == "1") {
                progressDialog.dismiss()
                fnAlertDialog(
                    requireContext(),
                    "Documento de Gestion de venta ya existe. Consulte.",
                    textoPositivo = "Aceptar"
                )
                return
            }
            MiAsyncTaskSecuencia(progressDialog).execute()
        }
    }

    fun fnObtenerValorXml(xmlResult: String?): String? {
        if (xmlResult.isNullOrBlank()) return null
        return try {
            val parser = Xml.newPullParser()
            parser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false)
            parser.setInput(StringReader(xmlResult))
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.TEXT) {
                    val value = parser.text?.trim()
                    if (!value.isNullOrEmpty()) return value
                }
                eventType = parser.next()
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private inner class MiAsyncTaskSecuencia(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                val cadena = "2,$ep_codigo,0,0,'','',1"
                solicitudSoap.initializeVariables(
                    getString(R.string.str_ConsultaGestionVenta).toInt(),
                    cadena
                )
                solicitudSoap.realizarSolicitudSoap()?.readTextUtf8()
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            etNumeroL.setText(fnObtenerValorXml(result))
            progressDialog.dismiss()
        }
    }

    private fun fnLlamadas() {
        val dialog = consultaLlamada()
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "consultaCliente")
    }

    override fun onItemsSelected(codigo: Llamadas) {
        etNumeroL.setText(codigo.codigo.toString())
        etNumeroL.isEnabled = false
        ivBusquedaLl.isEnabled = false
        fnControles(true)
        solicitudSoap = SolicitudSoap(requireContext())
        MiAsyncTaskGestionVenta(showProgressDialog(), codigo.codigo).execute()
    }

    private fun showProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    private inner class MiAsyncTaskGestionVenta(
        private val progressDialog: ProgressDialog,
        private val codigo: Int
    ) : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                val cadena = "0,$ep_codigo,$codigo,0,'','',8"
                solicitudSoap.initializeVariables(
                    getString(R.string.str_ConsultaGestionVenta).toInt(),
                    cadena
                )
                val result = solicitudSoap.realizarSolicitudSoap()?.readTextUtf8()
                if (!result.isNullOrBlank()) xmlDatos = result
                null
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val clientes = fnParseXmlClienteProspecto(xmlDatos)
            listaCliente.clear()
            listaCliente.addAll(clientes)
            adapterProspecto.notifyDataSetChanged()

            if (clientes.isNotEmpty()) {
                txtFechaLl.text = fnFormatearFecha(listaCliente[0].fecha)
                fnCargarCliente(clientes[0])
                posicion = 0
                adapterProspecto.selectItem(0)
            } else {
                fnAccionesPulsarConsultar()
                fnAlertDialog(requireContext(), "Gestion de venta no existe", textoPositivo = "Aceptar")
            }
            progressDialog.dismiss()
        }
    }

    fun fnParseXmlClienteProspecto(xmlString: String): List<ClienteProspecto> {
        val resultado = mutableListOf<ClienteProspecto>()
        if (xmlString.isBlank()) return resultado

        val factory = XmlPullParserFactory.newInstance().apply {
            isNamespaceAware = false
        }

        val parser = factory.newPullParser().apply {
            setInput(StringReader(xmlString))
        }

        val recordTags = setOf("table")

        var eventType = parser.eventType
        var currentField = ""
        var insideRecord = false

        val row = mutableMapOf<String, String>()

        fun resetRow() {
            row.clear()
        }

        fun text(vararg names: String): String {
            for (name in names) {
                val value = row[name.lowercase(Locale.ROOT)]
                if (!value.isNullOrBlank()) return value
            }
            return ""
        }

        fun int(vararg names: String): Int {
            return text(*names).toIntOrNull() ?: 0
        }

        fun flushRow() {
            if (row.values.none { it.isNotBlank() }) return

            resultado.add(
                ClienteProspecto(
                    codigoP = int("codprospecto"),
                    cl_codigo = int("cl_codigo"),
                    secuencia = int("gv_secuencia"),
                    bodega = int("bo_codigob1"),
                    identificacion = text("identificacion"),
                    nombre = text("nombre"),
                    linea = text("gv_lineaofrecida"),
                    observacion = text("gv_observacion"),
                    redSocial = int("rs_codigo"),
                    catalogo = text("gv_catalogo"),
                    gv_interno = int("gv_interno"),
                    proceso = int("pr_codigo"),
                    fecha = text("gv_fechatrn")
                )
            )
        }

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {

                XmlPullParser.START_TAG -> {
                    val tag = parser.name.orEmpty().lowercase(Locale.ROOT)

                    if (tag in recordTags) {
                        insideRecord = true
                        resetRow()
                    } else if (insideRecord) {
                        currentField = tag
                    }
                }

                XmlPullParser.TEXT -> {
                    if (insideRecord && currentField.isNotBlank()) {
                        val value = parser.text?.trim().orEmpty()
                        if (value.isNotBlank()) {
                            row[currentField] = value
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    val tag = parser.name.orEmpty().lowercase(Locale.ROOT)

                    if (tag in recordTags && insideRecord) {
                        flushRow()
                        resetRow()
                        insideRecord = false
                        currentField = ""
                    } else if (insideRecord) {
                        currentField = ""
                    }
                }
            }

            eventType = parser.next()
        }

        return resultado
    }

    fun fnFormatearFecha(gvFecha: String?): String {
        if (gvFecha.isNullOrBlank()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(gvFecha)
            if (date != null) outputFormat.format(date) else gvFecha
        } catch (e: Exception) {
            gvFecha
        }
    }

    fun fnCargarCliente(item: ClienteProspecto) {
        etLineaLl.setText(item.linea)
        etObservacionLl.setText(item.observacion)
        etCatalogoLl.setText(item.catalogo)
        val posSpinner = criterios.indexOfFirst { it.codigo == item.redSocial }
        if (posSpinner != -1) spinnerRedLl.setSelection(posSpinner, false)
    }

    override fun onItemClick(item: ClienteProspecto, position: Int) {
        if (posicion != -1) fnActualizarItemSeleccionado()
        posicion = position
        fnCargarCliente(item)

        val codigo: Int
        val opcionAvance: Int
        if (item.cl_codigo != 1774) {
            codigo = item.cl_codigo
            opcionAvance = 6
        } else {
            codigo = item.codigoP
            opcionAvance = 3
        }

        adapterAvance.clearItems()
        txtAvancesLl.text = ""
        if (item.bodega != 0) {
            solicitudSoap = SolicitudSoap(requireContext())
            MiAsyncTaskAvances(showProgressDialog(), codigo, opcionAvance).execute()
        }
    }

    private inner class MiAsyncTaskAvances(
        private val progressDialog: ProgressDialog,
        private val codigo: Int,
        private val opcionAvance: Int
    ) : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                val cadena = "'','', $ep_codigo,'$codigo',$opcionAvance"
                solicitudSoap.initializeVariables(getString(R.string.str_Avances).toInt(), cadena)
                val result = solicitudSoap.realizarSolicitudSoap()?.readTextUtf8()
                if (!result.isNullOrBlank()) xmlDatos = result
                null
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val avances = fnParseXmlGestionSeguimiento(xmlDatos)
            listaAvance.clear()
            listaAvance.addAll(avances)
            adapterAvance.notifyDataSetChanged()
            if (avances.isNotEmpty()) {
                adapterAvance.selectItem(0)
                onItemClick(avances[0], 0)
            }
            progressDialog.dismiss()
        }
    }

    fun fnParseXmlGestionSeguimiento(xmlString: String): List<Avance> {
        val fechas = mutableListOf<String>()
        val lineas = mutableListOf<String>()
        if (xmlString.isBlank()) return emptyList()

        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlString))

        var currentField = ""
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentField = parser.name.orEmpty().lowercase(Locale.ROOT)
                }
                XmlPullParser.END_TAG -> currentField = ""
                XmlPullParser.TEXT -> {
                    val text = parser.text?.trim().orEmpty()
                    if (text.isNotEmpty()) {
                        when (currentField) {
                            "gv_fechatrn" -> {
                                val fecha = try {
                                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    inputFormat.parse(text)?.let { outputFormat.format(it) } ?: text
                                } catch (e: Exception) {
                                    text
                                }
                                fechas.add(fecha)
                            }
                            "gv_lineaofrecida" -> lineas.add(text)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        val total = maxOf(fechas.size, lineas.size)
        return (0 until total).map { index ->
            Avance(
                fecha = fechas.getOrElse(index) { "" },
                linea = lineas.getOrElse(index) { "" }
            )
        }
    }

    override fun onItemClick(item: Avance, position: Int) {
        txtAvancesLl.text = item.linea
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun fnAlertDialog(
        context: Context,
        mensaje: String,
        mostrarCancelar: Boolean = false,
        textoPositivo: String = "Si",
        textoNegativo: String = "No",
        accionPositiva: (() -> Unit)? = null
    ) {
        val builder = AlertDialog.Builder(context)
            .setTitle("Sistema")
            .setMessage(mensaje)
            .setCancelable(false)
            .setPositiveButton(textoPositivo) { dialog, _ ->
                dialog.dismiss()
                accionPositiva?.invoke()
            }

        if (mostrarCancelar) {
            builder.setNegativeButton(textoNegativo) { dialog, _ -> dialog.dismiss() }
        }
        builder.show()
    }

    private fun fnCliente() {
        val dialog = consultaClienteProspecto()
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "consultaCliente")
    }

    override fun onItemsSelected(clientes: AdaptadorProspectos.Prospectos) {
        clienteSeleccionado = clientes
        val nombre = listOf(clientes.Nombre1, clientes.Nombre2, clientes.Apellido1, clientes.Apellido2)
            .filter { it.isNotBlank() }
            .joinToString(" ")
        etNombreLl.setText(nombre)
        etDireccionLl.setText(clientes.Direccion)
        etTipoLl.setText(clientes.Tipo)
    }

    fun fnAgregarProspecto(clienteSeleccionado: AdaptadorProspectos.Prospectos) {
        val nombreCompleto = listOf(
            clienteSeleccionado.Nombre1,
            clienteSeleccionado.Nombre2,
            clienteSeleccionado.Apellido1,
            clienteSeleccionado.Apellido2
        ).filter { it.isNotBlank() }.joinToString(" ")

        val nuevaSecuencia = listaCliente.size + 1
        val clCodigo = if (clienteSeleccionado.gt_codigo == "1") {
            1774
        } else {
            clienteSeleccionado.Codigo.toIntOrNull() ?: 0
        }

        val clienteProspecto = ClienteProspecto(
            codigoP = clienteSeleccionado.Codigo.toIntOrNull() ?: 0,
            cl_codigo = clCodigo,
            secuencia = nuevaSecuencia,
            bodega = 0,
            identificacion = clienteSeleccionado.Codigo,
            nombre = nombreCompleto,
            linea = "",
            observacion = "",
            redSocial = 0,
            catalogo = "",
            gv_interno = 0,
            proceso = 0,
            fecha = ""
        )

        listaCliente.add(clienteProspecto)
        adapterProspecto.notifyDataSetChanged()

        etNombreLl.text?.clear()
        etTipoLl.text?.clear()
        etDireccionLl.text?.clear()
        etLineaLl.text?.clear()
        etObservacionLl.text?.clear()
        spinnerRedLl.setSelection(0)
        etCatalogoLl.text?.clear()
        etLineaLl.requestFocus()

        val position = listaCliente.size - 1
        posicion = position
        adapterProspecto.selectItem(position)
        rvClientes.scrollToPosition(position)
        adapterAvance.clearItems()
        txtAvancesLl.text = ""
    }

    fun fnVerificarControles(): Boolean {
        if (listaCliente.isEmpty()) {
            fnAlertDialog(requireContext(), "Debe agregar un cliente", textoPositivo = "Aceptar")
            etNombreLl.requestFocus()
            return false
        }

        if (etLineaLl.text.isNullOrEmpty()) {
            etLineaLl.requestFocus()
            fnAlertDialog(requireContext(), "Ingrese Linea Ofrecida", textoPositivo = "Aceptar")
            return false
        }

        if (etObservacionLl.text.isNullOrEmpty()) {
            etObservacionLl.requestFocus()
            fnAlertDialog(requireContext(), "Ingrese Observacion", textoPositivo = "Aceptar")
            return false
        }

        val url = etCatalogoLl.text.toString().trim()
        if (Patterns.WEB_URL.matcher(url).matches()) return true

        etCatalogoLl.requestFocus()
        etCatalogoLl.error = "Ingrese un enlace web válido"
        fnAlertDialog(requireContext(), "Debe ingresar un link válido del Catálogo Digital", textoPositivo = "Aceptar")
        return false
    }

    fun fnActualizarItemSeleccionado() {
        if (posicion in listaCliente.indices) {
            val clienteAntes = listaCliente[posicion]
            val clienteActualizado = clienteAntes.copy(
                linea = etLineaLl.text.toString().trim(),
                observacion = etObservacionLl.text.toString().trim(),
                redSocial = rs_codigo,
                catalogo = etCatalogoLl.text.toString().trim()
            )
            listaCliente[posicion] = clienteActualizado
            adapterProspecto.notifyItemChanged(posicion)
        }
    }

    fun fnGuardarDatos() {
        fnActualizarItemSeleccionado()
        if (!fnVerificarControles()) return

        val existeClientePublico = listaCliente.any { it.cl_codigo == 1774 }
        xmlProspecto = XmlProspecto(requireContext())
        solicitudSoap = SolicitudSoap(requireContext())
        val progressDialog = showProgressDialog()

        if (existeClientePublico) {
            MiAsyncTaskLClientePublico(progressDialog).execute()
        } else {
            MiAsyncTaskProspecto(progressDialog).execute()
        }
    }

    private inner class MiAsyncTaskLClientePublico(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                val documento = etNumeroL.text.toString()
                val cadena = xmlProspecto.fnObtenerGestionVentaPublico(
                    listaCliente,
                    documento,
                    usuario,
                    ep_codigo.toString(),
                    vgsOpcionMenu
                )
                if (cadena != null) {
                    solicitudSoap.initializeVariables(getString(R.string.str_LlamadaXML).toInt(), cadena)
                }
                val result = solicitudSoap.realizarSolicitudSoap()?.readTextUtf8()
                if (!result.isNullOrBlank()) {
                    xmlDatos = result
                    result
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            xmlProspecto = XmlProspecto(requireContext())
            solicitudSoap = SolicitudSoap(requireContext())
            MiAsyncTaskProspecto(progressDialog).execute()
        }
    }

    private inner class MiAsyncTaskProspecto(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                val fecha = txtFechaLl.text.toString()
                val formatoEntrada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formatoSalida = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val fechaConvertida = formatoEntrada.parse(fecha)?.let { formatoSalida.format(it) } ?: fecha
                val horaActual = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                val fechaHora = "$fechaConvertida $horaActual"
                val documento = etNumeroL.text.toString()

                val cadena = xmlProspecto.fnObtenerXmGestionVenta(
                    opcion,
                    fechaHora,
                    usuario,
                    documento,
                    ep_codigo.toString(),
                    listaCliente,
                    vgsOpcionMenu
                )

                if (cadena != null) {
                    solicitudSoap.initializeVariables(getString(R.string.str_LlamadaXML).toInt(), cadena)
                }

                val result = solicitudSoap.realizarSolicitudSoap()?.readTextUtf8()
                if (!result.isNullOrBlank()) {
                    xmlDatos = result
                    result
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            showResultDialog("")
            btnGuardarLl.isEnabled = false
            fnControles(false)
        }
    }

    private fun showResultDialog(pedido: String) {
        val titulo = if (opcion == 1) "Guardados" else "Eliminados"
        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("Datos $titulo Correctamente")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun InputStream.readTextUtf8(): String {
        return BufferedReader(InputStreamReader(this, Charsets.UTF_8)).use { it.readText() }
    }
}
