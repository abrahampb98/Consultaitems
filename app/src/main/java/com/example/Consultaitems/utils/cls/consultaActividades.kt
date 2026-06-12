package com.example.Consultaitems.utils.cls

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.activities.frmLogin
import com.example.Consultaitems.ui.adapters.AdaptadorReporteActividades
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class consultaActividades : DialogFragment() {

    var XmlDatos: String = ""
    var criterio: Int = 2
    val ep_codigo: Int = frmLogin.CadenaHolder.ep_codigo
    lateinit var solicitudSoap: SolicitudSoap

    private lateinit var adapter: AdaptadorReporteActividades
    private lateinit var bntBuscarGV: Button
    private lateinit var criterios: List<Criterio>
    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var recyclerView: RecyclerView
    private lateinit var spnCriteriosC: Spinner
    private lateinit var txtFechaFinGV: TextView
    private lateinit var txtFechaIncGV: TextView
    private var isDatePickerShown: Boolean = false
    private val listActividades = mutableListOf<AdaptadorReporteActividades.ReporteActividad>()

    interface OnItemSelectedListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.frm_actividades, container, false)
        recyclerView = view.findViewById(R.id.recyclerviewItemsA)
        spnCriteriosC = view.findViewById(R.id.spnCriteriosGV)
        bntBuscarGV = view.findViewById(R.id.bntBuscarGV)
        txtFechaIncGV = view.findViewById(R.id.txtFechaIncGV)
        txtFechaFinGV = view.findViewById(R.id.txtFechaFinGV)

        txtFechaIncGV.text = fnFecha()
        txtFechaFinGV.text = fnFecha()
        llenarControles = ClsLLenarControles(requireContext())
        llenarSpinnerCriterios()

        criterio = criterios.getOrNull(spnCriteriosC.selectedItemPosition)?.codigo ?: 2
        adapter = AdaptadorReporteActividades(listActividades)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        txtFechaIncGV.setOnClickListener {
            if (!isDatePickerShown) showDatePickerDialog(txtFechaIncGV)
        }
        txtFechaFinGV.setOnClickListener {
            if (!isDatePickerShown) showDatePickerDialog(txtFechaFinGV)
        }
        bntBuscarGV.setOnClickListener { fnConsultarItems() }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun llenarSpinnerCriterios() {
        criterios = listOf(
            Criterio(2, "Actividades"),
            Criterio(3, "Pendientes"),
            Criterio(4, "Finalizadas")
        )
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            criterios.map { it.descripcion }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnCriteriosC.adapter = adapter
        spnCriteriosC.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                criterio = criterios.getOrNull(position)?.codigo ?: 2
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun showDatePickerDialog(targetView: TextView) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                targetView.text = formatDate(selectedYear, selectedMonth, selectedDay)
                isDatePickerShown = false
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

    private fun fnFecha(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    fun fnConsultarItems() {
        if (isNetworkAvailable(requireContext())) {
            solicitudSoap = SolicitudSoap(requireContext())
            val progressDialog = showProgressDialog()
            MiAsyncTask(progressDialog).execute()
        } else {
            Toast.makeText(requireContext(), "Verifique su conexion a internet", Toast.LENGTH_LONG).show()
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

    private inner class MiAsyncTask(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                criterio = criterios.getOrNull(spnCriteriosC.selectedItemPosition)?.codigo ?: 2
                val cadena = "2,$ep_codigo,10,1,'${txtFechaIncGV.text}','${txtFechaFinGV.text}'"
                solicitudSoap.initializeVariables(getString(R.string.str_GestionProyecto).toInt(), cadena)
                val result = solicitudSoap.realizarSolicitudSoap()?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText)
                if (!result.isNullOrBlank()) XmlDatos = result
                result
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            val parsed = fnParseXml(XmlDatos)
            listActividades.clear()
            listActividades.addAll(parsed)
            adapter.notifyDataSetChanged()
        }
    }

    fun fnParseXml(xmlString: String): List<AdaptadorReporteActividades.ReporteActividad> {
        return parseRows(xmlString).mapNotNull { row ->
            val actividad = row.str("actividad", "gp_tarea", "GP_TAREA", "pp_actividad", "PP_ACTIVIDAD")
            val observacion = row.str("observacion", "gp_observacion", "GP_OBSERVACION")
            if (actividad.isBlank() && observacion.isBlank()) return@mapNotNull null
            AdaptadorReporteActividades.ReporteActividad(
                actividad = actividad,
                observacion = observacion,
                recurso = row.str("recurso", "gp_recurso", "GP_RECURSO"),
                dias = row.int("dias", "gp_duracion", "GP_DURACION"),
                inicio = formatearFecha(row.str("inicio", "gp_fechainicial", "GP_FECHAINICIAL")),
                fin = formatearFecha(row.str("fin", "gp_fechafinal", "GP_FECHAFINAL")),
                porcentaje = row.int("porcentaje", "gp_avance", "GP_AVANCE"),
                status = row.str("status", "estado", "GP_ESTADO")
            )
        }
    }

    fun formatearFecha(fechaCompleta: String): String {
        return try {
            val fecha = fechaCompleta.substring(0, 10)
            val partes = fecha.split("-")
            "${partes[2]}/${partes[1]}/${partes[0]}"
        } catch (e: Exception) {
            fechaCompleta
        }
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
                    if (tag.equals("Table", true) || tag.equals("Table1", true) || tag.equals("row", true)) {
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
                    if (text.isNotEmpty()) currentRow?.let { row -> currentTag?.let { row[it] = text } }
                }
                XmlPullParser.END_TAG -> {
                    val tag = parser.name
                    if (tag.equals("Table", true) || tag.equals("Table1", true) || tag.equals("row", true)) {
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

