package com.example.Consultaitems.ui.fragments

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.activities.frmLogin
import com.example.Consultaitems.ui.adapters.AdaptadorActividad
import com.example.Consultaitems.ui.adapters.AdaptadorPlantilla
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.consultaActividades
import com.example.Consultaitems.utils.parser.XmlGestionProyecto
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ActividadesFragment : Fragment(), AdaptadorActividad.OnItemClickListener {

    private lateinit var adapterActividades: AdaptadorActividad
    private lateinit var adapterPlantilla: AdaptadorPlantilla
    private lateinit var xmlGestionProyecto: XmlGestionProyecto

    private var bo_codigo: Int = 0
    private var dp_codigo: Int = 0
    private var posicion: Int = -1
    private var vgsOpcionMenu: String = ""
    private var usuario: String = ""
    private var isDatePickerShown: Boolean = false

    lateinit var lLenarControles: ClsLLenarControles
    lateinit var solicitudSoap: SolicitudSoap
    lateinit var recyclerActividades: RecyclerView
    lateinit var recyclerPlantilla: RecyclerView
    lateinit var btnEliminarDetalle: Button
    lateinit var btnGuardarA: Button
    lateinit var etEmpleadoA: EditText
    lateinit var etDepartamentoA: EditText
    lateinit var etActividadA: EditText
    lateinit var etObservacionA: EditText
    lateinit var etProcesoA: EditText
    lateinit var etTareaA: EditText
    lateinit var etObjetivoA: EditText
    lateinit var txtFechaA: TextView

    val ep_codigo: Int = frmLogin.CadenaHolder.ep_codigo
    private val listaActividades = mutableListOf<AdaptadorActividad.Actividades>()
    private val listaPlantilla = mutableListOf<AdaptadorPlantilla.Plantilla>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_actividades, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        lLenarControles = ClsLLenarControles(requireContext())
        solicitudSoap = SolicitudSoap(requireContext())

        recyclerActividades = view.findViewById(R.id.recyclerActividades)
        recyclerPlantilla = view.findViewById(R.id.recyclerPlantilla)
        btnEliminarDetalle = view.findViewById(R.id.btnEliminarDetalle)
        etEmpleadoA = view.findViewById(R.id.etEmpleadoA)
        txtFechaA = view.findViewById(R.id.txtFechaA)
        etDepartamentoA = view.findViewById(R.id.etDepartamentoA)
        etActividadA = view.findViewById(R.id.etActividadA)
        etObservacionA = view.findViewById(R.id.etObservacionA)
        etProcesoA = view.findViewById(R.id.etProcesoA)
        etTareaA = view.findViewById(R.id.etTareaA)
        etObjetivoA = view.findViewById(R.id.etObjetivoA)
        btnGuardarA = view.findViewById(R.id.btnGuardarA)

        etEmpleadoA.setText(lLenarControles.fnObtenerNombreUsuario(ep_codigo))
        lLenarControles.fnLLenarVendedor()?.let { vendedor ->
            usuario = vendedor.login
        }

        txtFechaA.text = fnFecha()
        bo_codigo = lLenarControles.fnObtenerDepartamento(ep_codigo)
        when (bo_codigo) {
            2 -> {
                etDepartamentoA.setText("ALMACEN 820")
                dp_codigo = 6
            }
            51 -> {
                etDepartamentoA.setText("ALMACEN PROMARKET")
                dp_codigo = 39
            }
        }

        adapterActividades = AdaptadorActividad(listaActividades, object : AdaptadorActividad.OnItemClickListener {
            override fun onItemClick(item: AdaptadorActividad.Actividades, position: Int) {
                fnSeleccionarGestion(item, position)
            }
        })
        recyclerActividades.layoutManager = LinearLayoutManager(context)
        recyclerActividades.adapter = adapterActividades
        recyclerActividades.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        adapterPlantilla = AdaptadorPlantilla(listaPlantilla, object : AdaptadorPlantilla.OnActividadCheckedListener {
            override fun onChecked(actividad: AdaptadorPlantilla.Plantilla) {
                fnAgregarActividad(plantillaToActividad(actividad))
            }

            override fun onUnchecked(actividad: AdaptadorPlantilla.Plantilla) {
                fnQuitarActividad(plantillaToActividad(actividad))
            }
        })
        recyclerPlantilla.layoutManager = LinearLayoutManager(context)
        recyclerPlantilla.adapter = adapterPlantilla
        recyclerPlantilla.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        fnControles(false)

        txtFechaA.setOnClickListener {
            if (!isDatePickerShown) showDatePickerDialog(txtFechaA)
        }
        btnEliminarDetalle.setOnClickListener { fnEliminar() }
        btnGuardarA.setOnClickListener {
            fnAlertDialog(
                requireContext(),
                "Deseas guardar los datos?",
                mostrarCancelar = true
            ) {
                fnGuardarDatos()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.frm_menu_actividad, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bntNuevoA -> {
                fnAccionesPulsarNuevo()
                true
            }
            R.id.btnConsultarA -> {
                fnAccionesPulsarConsultar()
                true
            }
            R.id.btnReporteA -> {
                fnAcionesPulsarReporte()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fnControles(enabled: Boolean) {
        btnEliminarDetalle.isEnabled = enabled
        etActividadA.isEnabled = enabled
        etObservacionA.isEnabled = enabled
        etTareaA.isEnabled = enabled
        etProcesoA.isEnabled = enabled
        etObjetivoA.isEnabled = enabled
        btnGuardarA.isEnabled = enabled
        txtFechaA.isEnabled = enabled

        if (enabled) adapterPlantilla.habilitar() else adapterPlantilla.deshabilitar()

        val alphaValue = if (enabled) 1.0f else 0.5f
        btnEliminarDetalle.alpha = alphaValue
        etActividadA.alpha = alphaValue
        etObservacionA.alpha = alphaValue
        btnGuardarA.alpha = alphaValue
        etTareaA.alpha = alphaValue
        etObjetivoA.alpha = alphaValue
        etProcesoA.alpha = alphaValue
    }

    fun fnAccionesPulsarConsultar() {
        fnLimpiarControles()
        fnControles(false)
        txtFechaA.isEnabled = true
        vgsOpcionMenu = "M"
    }

    fun fnAccionesPulsarNuevo() {
        fnLimpiarControles()
        fnControles(true)
        vgsOpcionMenu = "I"
        fnPlantilla()
    }

    private fun fnLimpiarControles() {
        etActividadA.text?.clear()
        etObservacionA.text?.clear()
        etProcesoA.text?.clear()
        etTareaA.text?.clear()
        etObjetivoA.text?.clear()
        adapterActividades.clearItems()
        adapterPlantilla.clearItems()
    }

    private fun showDatePickerDialog(targetView: TextView) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val formattedDate = formatDate(selectedYear, selectedMonth, selectedDay)
                targetView.text = formattedDate
                isDatePickerShown = false
                if (vgsOpcionMenu == "M") {
                    fnBuscarGestion()
                    fnControles(true)
                    txtFechaA.isEnabled = false
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.setOnDismissListener { isDatePickerShown = false }
        datePicker.setOnCancelListener { isDatePickerShown = false }
        datePicker.show()
        isDatePickerShown = true
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)
    }

    override fun onItemClick(item: AdaptadorActividad.Actividades, position: Int) = Unit

    private fun fnFecha(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    private fun fnFechaxml(): String {
        val fechaTexto = txtFechaA.text.toString().trim()
        val entrada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val salida = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return salida.format(entrada.parse(fechaTexto) ?: Date())
    }

    private fun fnTransaccion(): String {
        return SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date())
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

    fun fnBuscarGestion() {
        solicitudSoap = SolicitudSoap(requireContext())
        val progressDialog = showProgressDialog()
        MiAsyncTaskActividades(progressDialog).execute()
    }

    private fun showProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    private inner class MiAsyncTaskActividades(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                val fecha = txtFechaA.text.toString()
                val cadena = "2,$ep_codigo,8,2,'$fecha','$fecha'"
                solicitudSoap.initializeVariables(getString(R.string.str_GestionProyecto).toInt(), cadena)
                solicitudSoap.realizarSolicitudSoap()?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText)
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val actividades = result?.let { fnParseXmlActividades(it) }.orEmpty()
            progressDialog.dismiss()
            listaActividades.clear()
            listaActividades.addAll(actividades)
            adapterActividades.notifyDataSetChanged()
            fnPlantilla()
        }
    }

    fun fnParseXmlActividades(xmlString: String): List<AdaptadorActividad.Actividades> {
        val rows = parseRows(xmlString)
        return rows.mapNotNull { row ->
            val ppCodigo = row.int("pp_codigo", "ppCodigo", "PP_CODIGO", "d13", default = 0)
            AdaptadorActividad.Actividades(
                emCodigo = row.int("em_codigo", "emCodigo", "EM_CODIGO", "c0", default = 0),
                gpFechaTrn = row.str("gp_fechatrn", "gpFechaTrn", "GP_FECHATRN", "d11"),
                epCodigo = row.int("ep_codigo", "epCodigo", "EP_CODIGO", default = ep_codigo),
                dpCodigo = row.int("dp_codigo", "dpCodigo", "DP_CODIGO", "d0", default = dp_codigo),
                dpDescripcion = row.str("dp_descripcion", "dpDescripcion", "DP_DESCRIPCION", default = etDepartamentoA.text.toString()),
                gpTarea = row.str("gp_tarea", "gpTarea", "GP_TAREA", "d1"),
                gpObservacion = row.str("gp_observacion", "gpObservacion", "GP_OBSERVACION", "d2", "d12"),
                gpRecurso = row.str("gp_recurso", "gpRecurso", "GP_RECURSO", "d3", default = usuario),
                gpDuracion = row.int("gp_duracion", "gpDuracion", "GP_DURACION", "d4", default = 0),
                gpAvance = row.int("gp_avance", "gpAvance", "GP_AVANCE", "d5", default = 0),
                gpFechaInicial = row.str("gp_fechainicial", "gpFechaInicial", "GP_FECHAINICIAL", "d6"),
                gpFechaFinal = row.str("gp_fechafinal", "gpFechaFinal", "GP_FECHAFINAL", "d7"),
                proceso = row.int("proceso", "PROCESO", "d8", default = 0),
                gpCodigo = row.int("gp_codigo", "gpCodigo", "GP_CODIGO", "d9", default = 0),
                gpTareaP = row.str("gp_tareap", "gpTareaP", "GP_TAREAP", "d10"),
                ppCodigo = ppCodigo,
                ptDescripcion = row.str("pt_descripcion", "ptDescripcion", "PT_DESCRIPCION"),
                ptObjetivo = row.str("pt_objetivo", "ptObjetivo", "PT_OBJETIVO"),
                ppActividad = row.str("pp_actividad", "ppActividad", "PP_ACTIVIDAD"),
                ppTarea = row.str("pp_tarea", "ppTarea", "PP_TAREA")
            )
        }
    }

    private fun resetPlantillasChecked() {
        listaPlantilla.forEach { plantilla ->
            plantilla.isChecked = listaActividades.any { it.ppCodigo == plantilla.codigo && it.proceso != -1 }
        }
        adapterPlantilla.notifyDataSetChanged()
    }

    fun fnSeleccionarGestion(gestion: AdaptadorActividad.Actividades, indexReal: Int) {
        fnActualizarItemSeleccionado()
        posicion = indexReal
        etProcesoA.setText(gestion.ptDescripcion)
        etObjetivoA.setText(gestion.ptObjetivo)
        etActividadA.setText(gestion.ppActividad)
        etTareaA.setText(gestion.ppTarea)
        etObservacionA.setText(gestion.gpObservacion)
    }

    fun fnPlantilla() {
        solicitudSoap = SolicitudSoap(requireContext())
        val progressDialog = showProgressDialog()
        MiAsyncTaskPlantilla(progressDialog).execute()
    }

    private inner class MiAsyncTaskPlantilla(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                val cadena = "2,0,14,$dp_codigo,'',''"
                solicitudSoap.initializeVariables(getString(R.string.str_GestionProyecto).toInt(), cadena)
                solicitudSoap.realizarSolicitudSoap()?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText)
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val plantilla = result?.let { fnParseXmlPlantilla(it) }.orEmpty()
            progressDialog.dismiss()
            listaPlantilla.clear()
            listaPlantilla.addAll(plantilla)
            resetPlantillasChecked()
            adapterPlantilla.notifyDataSetChanged()
        }
    }

    fun fnParseXmlPlantilla(xmlString: String): List<AdaptadorPlantilla.Plantilla> {
        val rows = parseRows(xmlString)
        return rows.mapNotNull { row ->
            val codigo = row.int("pp_codigo", "codigo", "PP_CODIGO", "Codigo", default = 0)
            val descripcion = row.str("pp_actividad", "descripcion", "PP_ACTIVIDAD", "pt_descripcion", "PT_DESCRIPCION")
            if (codigo == 0 && descripcion.isBlank()) return@mapNotNull null
            AdaptadorPlantilla.Plantilla(
                codigo = codigo,
                descripcion = descripcion,
                proceso = row.str("pt_descripcion", "proceso", "PT_DESCRIPCION"),
                objetivo = row.str("pt_objetivo", "objetivo", "PT_OBJETIVO"),
                tarea = row.str("pp_tarea", "tarea", "PP_TAREA"),
                isChecked = listaActividades.any { it.ppCodigo == codigo && it.proceso != -1 }
            )
        }
    }

    fun fnAgregarActividad(actividadSeleccionada: AdaptadorActividad.Actividades) {
        fnLimpiarEdicion()
        val index = listaActividades.indexOfFirst { it.ppCodigo == actividadSeleccionada.ppCodigo }
        if (index >= 0) {
            listaActividades[index] = listaActividades[index].copy(proceso = 0)
        } else {
            listaActividades.add(actividadSeleccionada)
        }
        adapterActividades.notifyDataSetChanged()
        recyclerActividades.scrollToPosition(listaActividades.size - 1)
    }

    fun fnQuitarActividad(actividad: AdaptadorActividad.Actividades) {
        fnLimpiarEdicion()
        val index = listaActividades.indexOfFirst { it.ppCodigo == actividad.ppCodigo }
        if (index == -1 || listaActividades[index].proceso != 0) return
        listaActividades.removeAt(index)
        adapterActividades.notifyDataSetChanged()
    }

    fun plantillaToActividad(plantilla: AdaptadorPlantilla.Plantilla): AdaptadorActividad.Actividades {
        return AdaptadorActividad.Actividades(
            emCodigo = 2,
            gpFechaTrn = fnFechaxml(),
            epCodigo = 0,
            dpCodigo = dp_codigo,
            dpDescripcion = etDepartamentoA.text.toString(),
            gpTarea = plantilla.descripcion,
            gpObservacion = "",
            gpRecurso = usuario,
            gpDuracion = 0,
            gpAvance = 100,
            gpFechaInicial = fnFechaxml(),
            gpFechaFinal = fnFechaxml(),
            proceso = 0,
            gpCodigo = 0,
            gpTareaP = "",
            ppCodigo = plantilla.codigo,
            ptDescripcion = plantilla.proceso,
            ptObjetivo = plantilla.objetivo,
            ppActividad = plantilla.descripcion,
            ppTarea = plantilla.tarea
        )
    }

    fun fnLimpiarEdicion() {
        etActividadA.text?.clear()
        etObservacionA.text?.clear()
        etProcesoA.text?.clear()
        etTareaA.text?.clear()
        etObjetivoA.text?.clear()
    }

    fun fnEliminar() {
        if (posicion == -1) {
            fnAlertDialog(requireContext(), "Seleccione una fila para eliminar", textoPositivo = "Aceptar")
        } else {
            fnAlertDialog(requireContext(), "Desea Eliminar la Fila", mostrarCancelar = true) {
                fnEliminarActividad()
            }
        }
    }

    fun fnEliminarActividad() {
        val visibles = listaActividades.filter { it.proceso != -1 }
        if (posicion !in visibles.indices) return
        val actividadVisible = visibles[posicion]
        val indexReal = listaActividades.indexOf(actividadVisible)
        if (indexReal == -1) return

        val actual = listaActividades[indexReal]
        listaActividades[indexReal] = actual.copy(proceso = -1)
        val nuevosVisibles = listaActividades.filter { it.proceso != -1 }
        posicion = if (nuevosVisibles.isNotEmpty()) {
            if (posicion >= nuevosVisibles.size) nuevosVisibles.size - 1 else posicion
        } else {
            -1
        }
        adapterActividades.notifyDataSetChanged()
        fnLimpiarEdicion()
    }

    fun fnActualizarItemSeleccionado() {
        if (posicion in listaActividades.indices) {
            val anterior = listaActividades[posicion]
            listaActividades[posicion] = anterior.copy(
                gpObservacion = etObservacionA.text.toString(),
                proceso = 0
            )
        }
    }

    fun fnGuardarDatos() {
        if (listaActividades.isEmpty()) {
            fnAlertDialog(requireContext(), "Ingrese lineas de detalle", textoPositivo = "Aceptar")
            return
        }
        fnActualizarItemSeleccionado()
        xmlGestionProyecto = XmlGestionProyecto(requireContext())
        solicitudSoap = SolicitudSoap(requireContext())
        val progressDialog = showProgressDialog()
        MiAsyncTaskGuardar(progressDialog).execute()
    }

    private inner class MiAsyncTaskGuardar(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                val cadena = xmlGestionProyecto.fnObtenerXmlActividades(
                    1,
                    usuario,
                    ep_codigo.toString(),
                    listaActividades
                )
                solicitudSoap.initializeVariables(getString(R.string.str_GestionProyectoXML).toInt(), cadena)
                solicitudSoap.realizarSolicitudSoap()?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText)
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            showResultDialog("")
            btnGuardarA.isEnabled = false
            fnControles(false)
        }
    }

    private fun showResultDialog(pedido: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("Datos Guardados Correctamente")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun fnAcionesPulsarReporte() {
        val dialog = consultaActividades()
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "consultaCliente")
    }

    private fun parseRows(xmlString: String): List<Map<String, String>> {
        val rows = mutableListOf<Map<String, String>>()
        val factory = XmlPullParserFactory.newInstance().apply { isNamespaceAware = false }
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlString))

        var currentRow: MutableMap<String, String>? = null
        var currentTag: String? = null
        var event = parser.eventType

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name
                    if (tag.equals("Table", true) || tag.equals("Table1", true) || tag.equals("detalle", true) || tag.equals("row", true)) {
                        currentRow = mutableMapOf()
                        for (i in 0 until parser.attributeCount) {
                            currentRow[parser.getAttributeName(i)] = parser.getAttributeValue(i).orEmpty()
                        }
                    } else {
                        currentTag = tag
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text?.trim().orEmpty()
                    if (text.isNotEmpty()) {
                        currentRow?.let { row -> currentTag?.let { row[it] = text } }
                    }
                }
                XmlPullParser.END_TAG -> {
                    val tag = parser.name
                    if (tag.equals("Table", true) || tag.equals("Table1", true) || tag.equals("detalle", true) || tag.equals("row", true)) {
                        currentRow?.let { rows.add(it) }
                        currentRow = null
                    }
                    currentTag = null
                }
            }
            event = parser.next()
        }
        return rows
    }

    private fun Map<String, String>.str(vararg keys: String, default: String = ""): String {
        for (key in keys) {
            this[key]?.let { return it }
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value?.let { return it }
        }
        return default
    }

    private fun Map<String, String>.int(vararg keys: String, default: Int = 0): Int {
        return str(*keys).toIntOrNull() ?: default
    }
}
