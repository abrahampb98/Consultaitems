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
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.activities.frmLogin
import com.example.Consultaitems.ui.adapters.AdaptadorLlamada
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class consultaLlamada : DialogFragment(), AdaptadorLlamada.OnItemClickListener {

    object ConstantesReporte {
        const val TIPO_FILA = "_tipo"
        const val TIPO_FECHA = "FECHA"
        const val TIPO_DATA = "DATA"
    }


    var XmlDatos: String = ""
    lateinit var solicitudSoap: SolicitudSoap

    private lateinit var adapter: AdaptadorLlamada
    private lateinit var bntBuscarLl: ImageView
    private lateinit var etCodigoLl: EditText
    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtFechaFinLl: TextView
    private lateinit var txtFechaIncLl: TextView

    private var isDatePickerShown = false
    private var listener: OnItemSelectedListener? = null
    private val listReferencia = mutableListOf<Llamadas>()
    val ep_codigo: Int = frmLogin.CadenaHolder.ep_codigo

    interface OnItemSelectedListener {
        fun onItemsSelected(codigo: Llamadas)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.frm_consulta_llamadas, container, false)

        recyclerView = view.findViewById(R.id.recyclerviewLlamadas)
        etCodigoLl = view.findViewById(R.id.etCodigoLl)
        bntBuscarLl = view.findViewById(R.id.ivBuscarLl)
        txtFechaIncLl = view.findViewById(R.id.txtFechaIncLl)
        txtFechaFinLl = view.findViewById(R.id.txtFechaFinLl)

        adapter = AdaptadorLlamada(listReferencia, this) { item: Llamadas, _: Int ->
            listener?.onItemsSelected(item)
            dismiss()
        }

        llenarControles = ClsLLenarControles(requireContext())
        txtFechaIncLl.text = fnFecha()
        txtFechaFinLl.text = fnFecha()

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        bntBuscarLl.setOnClickListener { fnConsultarItems() }
        txtFechaIncLl.setOnClickListener {
            if (!isDatePickerShown) showDatePickerDialog(txtFechaIncLl)
        }
        txtFechaFinLl.setOnClickListener {
            if (!isDatePickerShown) showDatePickerDialog(txtFechaFinLl)
        }

        return view
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
        )
    }

    private fun fnFecha(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
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

    private inner class MiAsyncTask(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String?>() {

        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                val cadena = "'${etCodigoLl.text}','${txtFechaIncLl.text}','${txtFechaFinLl.text}',$ep_codigo"
                solicitudSoap.initializeVariables(getString(R.string.str_ConsultaLlamadas).toInt(), cadena)
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
            val llamadas = fnParseXml(XmlDatos)
            listReferencia.clear()
            listReferencia.addAll(llamadas)
            adapter.notifyDataSetChanged()
        }
    }

    fun fnParseXml(xmlString: String): List<Llamadas> {
        val resultado = mutableListOf<Llamadas>()
        if (xmlString.isBlank()) return resultado

        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlString))

        val rowTags = setOf("Table", "TABLE", "table")
        var currentField = ""
        var insideRecord = false
        var codigo = ""
        var fecha = ""
        var empleado = ""

        fun flush() {
            val cod = codigo.toIntOrNull() ?: 0
            if (cod != 0 || fecha.isNotBlank() || empleado.isNotBlank()) {
                resultado.add(Llamadas(cod, fecha, empleado))
            }
            codigo = ""
            fecha = ""
            empleado = ""
        }

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name.orEmpty()
                    if (tag in rowTags) {
                        codigo = ""
                        fecha = ""
                        empleado = ""
                        insideRecord = true
                    } else if (insideRecord) {
                        currentField = tag
                    }
                }
                XmlPullParser.END_TAG -> {
                    val tag = parser.name.orEmpty()
                    if (tag in rowTags && insideRecord) {
                        flush()
                        insideRecord = false
                    }
                    currentField = ""
                }
                XmlPullParser.TEXT -> {
                    if (insideRecord && currentField.isNotBlank()) {
                        val text = parser.text?.trim().orEmpty()
                        if (text.isNotBlank()) {
                            when (currentField) {
                                "Codigo", "Código" -> codigo = text
                                "Fecha" -> fecha = try {
                                    val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                    val output = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                                    input.parse(text)?.let { output.format(it) } ?: text
                                } catch (_: Exception) { text }
                                "Empleado" -> empleado = text
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return resultado
    }

    override fun onItemClick(item: Llamadas, position: Int) {
        // Selección simple: la confirmación real se hace con doble click en el adaptador.
    }
}
data class Columna(
    val titulo: String,
    val peso: Float,
    val campo: String
)

data class Avance(
    val fecha: String,
    val linea: String
)

data class Llamadas(
    val codigo: Int,
    val fecha: String,
    val empleado: String
)
