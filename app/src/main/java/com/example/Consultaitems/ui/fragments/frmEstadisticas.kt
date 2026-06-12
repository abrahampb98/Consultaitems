package com.example.Consultaitems.ui.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.parser.XmlDoc
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class frmEstadistica: Fragment() {
    private lateinit var llenarControles: ClsLLenarControles
    lateinit var solicitudSoap: SolicitudSoap
    lateinit var dbHelper: SqLiteOpenHelper
    lateinit var txtCantidadPedidos: TextView
    lateinit var txtMontoPedidos: TextView
    lateinit var txtCantidadVentas: TextView
    lateinit var txtMontoVentas: TextView
    lateinit var txtMargenVentas: TextView
    var XmlDatos: String = ""
    private var isDatePickerShown = false
    private lateinit var txtFechaInicial: TextView
    private lateinit var txtFechaFinal: TextView
    private lateinit var btnBusquedaSP: Button
    private var vendedor: String = ""

    private lateinit var Clientes: HorizontalBarChart
    private lateinit var Items: HorizontalBarChart
    private lateinit var Marcas: HorizontalBarChart

    private lateinit var txtSubototalMeta: TextView
    private lateinit var txtMeta: TextView
    private lateinit var txtDiferenciaMeta: TextView
    private lateinit var txtProcedimientoMeta: TextView
    private lateinit var txtDescripcionMeta: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.frm_estadistica, container, false)

        //asignacion de variabales
        llenarControles = ClsLLenarControles(requireContext())
        dbHelper = SqLiteOpenHelper(requireContext())


        txtFechaInicial = view.findViewById(R.id.txtFechaInicialSP)
        txtFechaFinal = view.findViewById(R.id.txtFechaFinalSP)


        // 🔹 1. Obtener referencias a los TextView de cada CardView
        txtCantidadPedidos = view.findViewById(R.id.txtCantidadPedidos)
        txtMontoPedidos = view.findViewById(R.id.txtMontoPedidos)

        txtCantidadVentas = view.findViewById(R.id.txtCantidadVentas)
        txtMontoVentas = view.findViewById(R.id.txtMontoVentas)
        txtMargenVentas = view.findViewById(R.id.txtMargenVentas)

        Clientes = view.findViewById(R.id.charClientes)
        Items = view.findViewById(R.id.charItems)
        Marcas = view.findViewById(R.id.charMarcas)

        txtSubototalMeta = view.findViewById(R.id.txtSubototalMeta)
        txtMeta = view.findViewById(R.id.txtMeta)
        txtDiferenciaMeta = view.findViewById(R.id.txtDiferenciaMeta)
        txtProcedimientoMeta = view.findViewById(R.id.txtProcedimientoMeta)
        txtDescripcionMeta = view.findViewById(R.id.txtDescripcionMeta)


        btnBusquedaSP = view.findViewById(R.id.btnBusquedaSP)

        vendedor = llenarControles.fnObtenerVendedor()

        txtFechaInicial.setText(fnFecha())
        txtFechaFinal.setText(fnFecha())

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

        btnBusquedaSP.setOnClickListener {

            hideSoftKeyboard()
            fnConsultarItems()
        }

        return view

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

    fun fnConsultarItems(){

        if (isNetworkAvailable(requireContext())) {

                solicitudSoap = SolicitudSoap(requireContext())
                val progressDialog = showProgressDialog()
                MiAsyncTask(progressDialog).execute()

        } else {
                Toast.makeText(
                    requireContext(),
                    "Verifique su conexión a internet",
                    Toast.LENGTH_LONG
                ).show()
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

                val cadena = "2,1,'${txtFechaInicial.text}','${txtFechaFinal.text}','3','9',$vendedor,37"
                //val cadena = "2,1,'01/03/2025','12/03/2025','3','9',33,37"
                //Log.e("Cadena", cadena)

                solicitudSoap.initializeVariables(getString(R.string.str_rentabilidad).toInt(), cadena)

                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.bufferedReader()?.use { it.readText() }

                if (!result.isNullOrBlank()) {
                    XmlDatos = result
                }
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
            }
            return pedido
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()

            fnParseXml(XmlDatos)
        }
    }


    fun fnParseXml(xmlString: String) {
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlString))

            var eventType = parser.eventType
            var currentTag = ""
            var currentTable = ""

            // Variables para almacenar datos de cada tabla
            var pedidosCantidad = 0
            var pedidosMonto = 0.0

            var ventasCantidad = 0
            var ventasSubtotal = 0.0
            var margen = 0.0

            val clientes = mutableListOf<String>()
            val subtotalesClientes = mutableListOf<String>()

            val referenciasItems = mutableListOf<String>()
            val montosItems = mutableListOf<String>()

            val referenciasMarcas = mutableListOf<String>()
            val montosMarcas = mutableListOf<String>()

            var metaSubotal = 0.0
            var meta = 0.0
            var metadiferencia = 0.0
            var metaprocedimiento = 0.0
            var metaDescripcion = ""


            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name

                        // Identificar en qué tabla estamos
                        when (currentTag) {
                            "Table" -> currentTable = "pedidos"
                            "Table1" -> currentTable = "ventas"
                            "Table2" -> currentTable = "clientes"
                            "Table3" -> currentTable = "items"
                            "Table4" -> currentTable = "marcas"
                            "Table5" -> currentTable = "metas"
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim()
                        if (!text.isNullOrEmpty()) {
                            when (currentTable) {
                                "pedidos" -> {
                                    when (currentTag) {
                                        "cantidad" -> pedidosCantidad = text.toInt()
                                        "monto" -> pedidosMonto = text.toDouble()
                                    }
                                }
                                "ventas" -> {
                                    when (currentTag) {
                                        "cantidad" -> ventasCantidad = text.toInt()
                                        "subtotal" -> ventasSubtotal = text.toDouble()
                                        "margen" -> margen = text.toDouble()
                                    }
                                }
                                "clientes" -> {
                                    when (currentTag) {
                                        "nombrecliente" -> clientes.add(text)
                                        "subtotal" -> subtotalesClientes.add("$${String.format("%.2f", text.toDouble())}")
                                    }
                                }
                                "items" -> {
                                    when (currentTag) {
                                        "referencia" -> referenciasItems.add(text)
                                        "subtotal" -> montosItems.add("$${String.format("%.2f", text.toDouble())}")
                                    }
                                }
                                "marcas" -> {
                                    when (currentTag) {
                                        "marca" -> referenciasMarcas.add(text)
                                        "subtotal" -> montosMarcas.add("$${String.format("%.2f", text.toDouble())}")
                                    }
                                }
                                "metas" -> {
                                    when (currentTag) {
                                        "subtotal" -> metaSubotal = text.toDouble()
                                        "meta01" -> meta = text.toDouble()
                                        "diferencia" -> metadiferencia = text.toDouble()
                                        "porcrendimiento" -> metaprocedimiento = text.toDouble()
                                        "descripcion" -> metaDescripcion = text
                                    }
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }


                txtCantidadPedidos.text = "Cantidad: $pedidosCantidad"
                txtMontoPedidos.text = "Monto: $${String.format("%.2f", pedidosMonto)}"

                txtCantidadVentas.text = "Cantidad: $ventasCantidad"
                txtMontoVentas.text = "Monto: $${String.format("%.2f", ventasSubtotal)}"
                txtMargenVentas.text = "Lote: ${String.format("%.2f", margen)}"

                txtSubototalMeta.text = "Subtotal: $${String.format("%.2f",metaSubotal)}"
                txtMeta.text = "Meta01: $${String.format("%.2f",meta)}"
                txtDiferenciaMeta.text = "Diferencia: $${String.format("%.2f",metadiferencia)}"
                txtProcedimientoMeta.text = "Rend: ${String.format("%.2f",metaprocedimiento)}%"
                txtDescripcionMeta.text = "$metaDescripcion"


            // Llamar a la función para llenar los gráficos
            fnLlenarGraficos(Clientes, clientes, subtotalesClientes, "Clientes", Color.BLUE, 25f)
            fnLlenarGraficos(Items, referenciasItems, montosItems, "Items", Color.RED, 10f)
            fnLlenarGraficos(Marcas, referenciasMarcas, montosMarcas, "Marcas", Color.rgb(89, 185, 59), 10f)


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun fnLlenarGraficos(
        grafico: HorizontalBarChart,
        titulos: List<String>,
        valores: List<String>,
        tituloGrafico: String,
        color: Int,
        extends: Float
    ) {
        // ** LIMPIAR COMPLETAMENTE EL GRÁFICO PARA EVITAR DESPLAZAMIENTO **
        grafico.clear()
        grafico.data = null
        grafico.notifyDataSetChanged()
        grafico.invalidate()

        // ** REINICIAR OFFSET PARA EVITAR MOVIMIENTOS INESPERADOS **
        grafico.setExtraOffsets(0f, 0f, 0f, 0f)

        // ** FIJAR TAMAÑO PARA EVITAR REDUCCIÓN O MOVIMIENTOS **
        grafico.layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = 400
        }
        grafico.requestLayout()

        val entries = ArrayList<BarEntry>()

        // ** ORDENAR de mayor a menor **
        val datosOrdenados = titulos.indices.map { i ->
            Pair(titulos[i], valores[i].replace("$", "").toFloatOrNull() ?: 0f)
        }.sortedByDescending { it.second }

        val titulosOrdenados = datosOrdenados.map { it.first }
        val valoresOrdenados = datosOrdenados.map { it.second }

        if (titulosOrdenados.isNotEmpty()) {
            for (i in titulosOrdenados.indices) {
                entries.add(BarEntry(i.toFloat(), valoresOrdenados[i]))
            }
        } else {
            entries.add(BarEntry(0f, 0f))
        }

        val dataSet = BarDataSet(entries, tituloGrafico)
        dataSet.color = color
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.BLACK

        // ** Formatear valores a 2 decimales **
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.2f", value)
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f
        barData.setValueTextSize(12f)
        barData.setDrawValues(true)

        grafico.data = barData
        grafico.description.isEnabled = false

        // ** Configuración del eje Y (Etiquetas de Categorías) **
        val yAxis = grafico.xAxis
        yAxis.position = XAxis.XAxisPosition.BOTTOM
        yAxis.setDrawGridLines(false)
        yAxis.textSize = 12f
        yAxis.labelRotationAngle = 0f
        yAxis.granularity = 1f
        yAxis.setAvoidFirstLastClipping(false)
        yAxis.yOffset = 5f
        yAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return titulosOrdenados.getOrNull(value.toInt()) ?: ""
            }
        }

        // ** Configuración del eje X (Valores) **
        val xAxis = grafico.axisLeft
        xAxis.setDrawGridLines(true)
        xAxis.granularity = 1f
        xAxis.axisMinimum = 0f

        grafico.axisRight.isEnabled = false

        // ** Ajustar espacio adicional si es necesario **
        grafico.setExtraOffsets(extends, 10f, 10f, 10f)

        // ** Configuración del título (leyenda) **
        val title = grafico.legend
        title.isEnabled = true
        title.textSize = 16f
        title.textColor = color
        title.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        title.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        title.orientation = Legend.LegendOrientation.HORIZONTAL
        title.setDrawInside(false)

        // ** Animación de entrada **
        grafico.animateX(2000)

        // ** Forzar actualización visual **
        grafico.requestLayout()
        grafico.invalidate()
    }


    private fun showDatePickerDialog(targetView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = formatDate(selectedYear, selectedMonth, selectedDay)
            targetView.text = formattedDate
            isDatePickerShown = false
        }, year, month, day)

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

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun fnFecha(): String {
        val fechaActual = Date()
        // Formatear la fecha como string
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaFormateada = dateFormat.format(fechaActual)
        return fechaFormateada

    }
}