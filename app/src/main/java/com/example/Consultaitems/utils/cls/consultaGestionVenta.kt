package com.example.Consultaitems.utils.cls

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.activities.frmLogin
import com.example.Consultaitems.ui.adapters.AdaptadorProspectos
import com.example.Consultaitems.ui.adapters.AdapterReporteCriterio2
import com.example.Consultaitems.ui.adapters.AdapterReporteCriterio5
import com.example.Consultaitems.ui.adapters.TIPO_CLIENTE
import com.example.Consultaitems.ui.adapters.TIPO_DETALLE
import com.example.Consultaitems.ui.adapters.TIPO_FECHA_CREACION
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class consultaGestionVenta : DialogFragment() {

    var XmlDatos: String = ""
    var criterio: Int = 2
    lateinit var solicitudSoap: SolicitudSoap

    private var adapterCriterio2: AdapterReporteCriterio2? = null
    private var adapterCriterio5: AdapterReporteCriterio5? = null
    private lateinit var bntBuscarGV: Button
    private lateinit var criterios: List<Criterio>
    private var isDatePickerShown = false
    private var listener: OnItemSelectedListener? = null
    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var lyCabecera: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var spnCriteriosC: Spinner
    private lateinit var txtFechaFinGV: TextView
    private lateinit var txtFechaIncGV: TextView
    private lateinit var rootView: View

    private val listReferencia = mutableListOf<Map<String, String>>()
    private val listReferencia5 = mutableListOf<Map<String, Any>>()
    val ep_codigo: Int = frmLogin.CadenaHolder.ep_codigo

    interface OnItemSelectedListener {
        fun onItemsSelected(clientes: AdaptadorProspectos.Prospectos)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.frm_gestion_venta, container, false)

        recyclerView = rootView.findViewById(R.id.recyclerviewItemsGV)
        spnCriteriosC = rootView.findViewById(R.id.spnCriteriosGV)
        bntBuscarGV = rootView.findViewById(R.id.bntBuscarGV)
        txtFechaIncGV = rootView.findViewById(R.id.txtFechaIncGV)
        txtFechaFinGV = rootView.findViewById(R.id.txtFechaFinGV)
        lyCabecera = rootView.findViewById(R.id.lyCabecera)

        val cabecera = fnObtenerCabecera(criterio)
        adapterCriterio2 = AdapterReporteCriterio2(cabecera, listReferencia)
        adapterCriterio5 = AdapterReporteCriterio5(cabecera, listReferencia5)

        txtFechaIncGV.text = fnFecha()
        txtFechaFinGV.text = fnFecha()
        llenarControles = ClsLLenarControles(requireContext())

        llenarSpinnerCriterios()
        criterio = criterios.getOrNull(spnCriteriosC.selectedItemPosition)?.codigo ?: 2
        fnConstruirCabecera(fnObtenerCabecera(criterio), rootView)

        recyclerView.layoutManager = LinearLayoutManager(context)

        bntBuscarGV.setOnClickListener { fnConsultarItems() }
        txtFechaIncGV.setOnClickListener {
            if (!isDatePickerShown) showDatePickerDialog(txtFechaIncGV)
        }
        txtFechaFinGV.setOnClickListener {
            if (!isDatePickerShown) showDatePickerDialog(txtFechaFinGV)
        }
        spnCriteriosC.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                recyclerView.adapter = null
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = targetFragment as? OnItemSelectedListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )    }

    fun fnObtenerCabecera(idCriterio: Int): List<Columna> {
        return when (idCriterio) {
            2 -> listOf(
                Columna("Nombre", 2.5f, "gc_cliente"),
                Columna("Razón Social", 2.7f, "gc_razonsocial"),
                Columna("Actividad", 1.8f, "oc_descripcion"),
                Columna("Provincia", 1.4f, "pr_descripcion"),
                Columna("Ciudad", 1.3f, "ci_descripcion"),
                Columna("Email", 2.0f, "gc_email"),
                Columna("Teléfono", 1.4f, "gc_fono")
            )
            5 -> listOf(
                Columna("Nombre", 2.0f, "gc_cliente"),
                Columna("Razón Social", 2.8f, "gc_razonsocial"),
                Columna("Actividad", 1.8f, "oc_descripcion"),
                Columna("Provincia", 1.2f, "pr_descripcion"),
                Columna("Ciudad", 1.2f, "ci_descripcion"),
                Columna("Email", 2.5f, "gc_email"),
                Columna("Teléfono", 1.5f, "gc_fono")
            )
            7 -> listOf(
                Columna("Fecha", 1.2f, "gc_fechaing"),
                Columna("Razón Social", 3.5f, "gc_razonsocial"),
                Columna("Actividad", 1.6f, "oc_descripcion"),
                Columna("Provincia", 1.3f, "pr_descripcion"),
                Columna("Ciudad", 1.3f, "ci_descripcion"),
                Columna("Teléfono", 1.4f, "gc_fono")
            )
            else -> emptyList()
        }
    }

    fun fnConstruirCabecera(columnas: List<Columna>, rootView: View) {
        val contenedor = rootView.findViewById<LinearLayout>(R.id.lyCabecera)
        contenedor.removeAllViews()
        columnas.forEach { col ->
            contenedor.addView(TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, col.peso).apply {
                    marginEnd = (6 * resources.displayMetrics.density).toInt()
                }
                text = col.titulo
                setTypeface(null, Typeface.BOLD)
                setTextColor(android.graphics.Color.BLACK)
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_Facturas))
                isSingleLine = true
                ellipsize = TextUtils.TruncateAt.END
            })
        }
    }

    private fun llenarSpinnerCriterios() {
        criterios = listOf(
            Criterio(2, "Cliente Prospecto"),
            Criterio(5, "Seguimiento C.Prospecto"),
            Criterio(7, "Seguimiento C.Base")
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, criterios.map { it.descripcion })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnCriteriosC.adapter = adapter
    }

    private fun showDatePickerDialog(targetView: TextView) {
        val calendar = Calendar.getInstance()
        val picker = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                targetView.text = formatDate(selectedYear, selectedMonth, selectedDay)
                isDatePickerShown = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        picker.setOnDismissListener { isDatePickerShown = false }
        picker.setOnCancelListener { isDatePickerShown = false }
        picker.show()
        isDatePickerShown = true
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)
    }

    private fun fnFecha(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    fun fnConsultarItems() {
        if (isNetworkAvailable(requireContext())) {
            solicitudSoap = SolicitudSoap(requireContext())
            MiAsyncTask(showProgressDialog()).execute()
        } else {
            Toast.makeText(requireContext(), "Verifique su conexión a internet", Toast.LENGTH_LONG).show()
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        @Suppress("DEPRECATION")
        return connectivityManager.activeNetworkInfo?.isConnected == true
    }

    private fun showProgressDialog(): ProgressDialog {
        return ProgressDialog(requireContext()).apply {
            setMessage("Cargando Datos...")
            setCancelable(false)
            show()
        }
    }

    private inner class MiAsyncTask(private val progressDialog: ProgressDialog) : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                criterio = criterios.getOrNull(spnCriteriosC.selectedItemPosition)?.codigo ?: 2
                val cadena = "'${txtFechaIncGV.text}','${txtFechaFinGV.text}',$ep_codigo,'',$criterio"
                solicitudSoap.initializeVariables(getString(R.string.str_Avances).toInt(), cadena)
                val result = solicitudSoap.realizarSolicitudSoap()?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                if (!result.isNullOrBlank()) XmlDatos = result
                null
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            val parsed = fnParseXml(XmlDatos)

            when (criterio) {
                2 -> {
                    val columnas = fnObtenerCabecera(2)
                    listReferencia.clear()
                    listReferencia.addAll(fnAgruparPorFechaCriterio2(parsed))
                    fnConstruirCabecera(columnas, rootView)
                    val adapter = adapterCriterio2 ?: AdapterReporteCriterio2(columnas, listReferencia).also { adapterCriterio2 = it }
                    recyclerView.adapter = adapter
                    adapter.actualizarDatos(columnas, listReferencia)
                }
                5, 7 -> {
                    val columnas = fnObtenerCabecera(5)
                    val resultado = mutableListOf<Map<String, Any>>()
                    fnAgruparPorClienteCriterio5(parsed).forEach { cliente ->
                        resultado.add(mapOf(consultaLlamada.ConstantesReporte.TIPO_FILA to TIPO_FECHA_CREACION, "gc_fechaing" to cliente["gc_fechaing"]?.toString().orEmpty()))
                        resultado.add(
                            mapOf(
                                consultaLlamada.ConstantesReporte.TIPO_FILA to TIPO_CLIENTE,
                                "gc_cliente" to cliente["gc_cliente"]?.toString().orEmpty(),
                                "gc_razonsocial" to cliente["gc_razonsocial"]?.toString().orEmpty(),
                                "oc_descripcion" to cliente["oc_descripcion"]?.toString().orEmpty(),
                                "pr_descripcion" to cliente["pr_descripcion"]?.toString().orEmpty(),
                                "ci_descripcion" to cliente["ci_descripcion"]?.toString().orEmpty(),
                                "gc_email" to cliente["gc_email"]?.toString().orEmpty(),
                                "gc_fono" to cliente["gc_fono"]?.toString().orEmpty()
                            )
                        )
                        @Suppress("UNCHECKED_CAST")
                        val detalles = cliente["detalles"] as? List<Map<String, String>> ?: emptyList()
                        detalles.forEach { det ->
                            resultado.add(
                                mapOf(
                                    consultaLlamada.ConstantesReporte.TIPO_FILA to TIPO_DETALLE,
                                    "gv_fechatrn" to det["gv_fechatrn"].orEmpty(),
                                    "gv_observacion" to det["gv_observacion"].orEmpty(),
                                    "gv_lineaofrecida" to det["gv_lineaofrecida"].orEmpty()
                                )
                            )
                        }
                    }
                    listReferencia5.clear()
                    listReferencia5.addAll(resultado)
                    fnConstruirCabecera(columnas, rootView)
                    val adapter = adapterCriterio5 ?: AdapterReporteCriterio5(columnas, listReferencia5).also { adapterCriterio5 = it }
                    recyclerView.adapter = adapter
                    adapter.actualizarDatos(columnas, listReferencia5)
                }
            }
        }
    }

    fun fnParseXml(xmlString: String): List<Map<String, String>> {
        val resultado = mutableListOf<Map<String, String>>()
        if (xmlString.isBlank()) return resultado

        val filaActual = linkedMapOf<String, String>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlString))

        var currentField = ""
        var insideRecord = false
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name.orEmpty()
                    if (tag.equals("Table", ignoreCase = true)) {
                        filaActual.clear()
                        insideRecord = true
                    } else if (insideRecord) {
                        currentField = tag
                    }
                }
                XmlPullParser.END_TAG -> {
                    val tag = parser.name.orEmpty()
                    if (tag.equals("Table", ignoreCase = true) && insideRecord) {
                        resultado.add(filaActual.toMap())
                        filaActual.clear()
                        insideRecord = false
                    }
                    currentField = ""
                }
                XmlPullParser.TEXT -> {
                    if (insideRecord && currentField.isNotBlank()) {
                        val text = parser.text?.trim().orEmpty()
                        if (text.isNotBlank()) filaActual[currentField] = text
                    }
                }
            }
            eventType = parser.next()
        }
        return resultado
    }

    fun formatearFecha(fechaCompleta: String): String {
        return try {
            val fecha = fechaCompleta.substring(0, 10)
            val partes = fecha.split("-")
            "${partes[2]}/${partes[1]}/${partes[0]}"
        } catch (_: Exception) {
            fechaCompleta
        }
    }

    fun fnAgruparPorFechaCriterio2(data: List<Map<String, String>>): List<Map<String, String>> {
        val resultado = mutableListOf<Map<String, String>>()
        data.groupBy { formatearFecha(it["gc_fechaing"].orEmpty()) }.forEach { (fecha, items) ->
            resultado.add(mapOf(consultaLlamada.ConstantesReporte.TIPO_FILA to consultaLlamada.ConstantesReporte.TIPO_FECHA, "gc_fechaing" to fecha))
            items.forEach { fila ->
                resultado.add(fila.toMutableMap().apply { put(consultaLlamada.ConstantesReporte.TIPO_FILA, consultaLlamada.ConstantesReporte.TIPO_DATA) })
            }
        }
        return resultado
    }

    fun fnAgruparPorClienteCriterio5(clientes: List<Map<String, String>>): List<Map<String, Any>> {
        return clientes.groupBy { it["gc_codigo"].orEmpty() }
            .map { (_, registros) ->
                val primero = registros.first()
                mapOf<String, Any>(
                    "gc_cliente" to primero["cl_cliente"].orEmpty(),
                    "gc_razonsocial" to primero["gc_razonsocial"].orEmpty(),
                    "oc_descripcion" to primero["oc_descripcion"].orEmpty(),
                    "pr_descripcion" to primero["pr_descripcion"].orEmpty(),
                    "ci_descripcion" to primero["ci_descripcion"].orEmpty(),
                    "gc_email" to primero["gc_email"].orEmpty(),
                    "gc_fono" to primero["gc_fono"].orEmpty(),
                    "gc_fechaing" to formatearFecha(primero["gc_fechaing"].orEmpty()),
                    "detalles" to registros.sortedBy { it["gv_fechatrn"] }.map { r ->
                        mapOf(
                            "gv_fechatrn" to formatearFecha(r["gv_fechatrn"].orEmpty()),
                            "gv_observacion" to r["gv_observacion"].orEmpty(),
                            "gv_lineaofrecida" to r["gv_lineaofrecida"].orEmpty()
                        )
                    }
                )
            }
            .sortedBy { it["gc_cliente"].toString() }
    }
}
