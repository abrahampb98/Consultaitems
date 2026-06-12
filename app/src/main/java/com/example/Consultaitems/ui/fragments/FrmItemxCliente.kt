package com.example.Consultaitems.ui.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.adapters.AdaptadorClientes
import com.example.Consultaitems.ui.adapters.AdaptadorItemxCliente
import com.example.Consultaitems.ui.adapters.ClienteAdapter
import com.example.Consultaitems.ui.adapters.MarcaAdapter
import com.example.Consultaitems.ui.adapters.TransporteAdapter
import com.example.Consultaitems.ui.adapters.items
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.consultaCliente
import com.example.Consultaitems.utils.parser.XmlParserItemxCliente
import java.util.Calendar
import java.util.Locale

class frmItemxCliente : Fragment(),
    AdaptadorItemxCliente.OnItemClickListener,
    consultaCliente.OnItemSelectedListener {

    private var isDatePickerShown = false

    private lateinit var txtFechaInicial: TextView
    private lateinit var txtFechaFinal: TextView
    private lateinit var txtClienteIC: AutoCompleteTextView
    private lateinit var txtItemIC: AutoCompleteTextView
    private lateinit var txtMarcaIC: AutoCompleteTextView
    private lateinit var btnBusquedaIC: Button
    private lateinit var btnClienteIC: ImageButton

    private var cl_codigo: String = ""
    private var it_codigo: String = ""
    private var marca: String = ""
    private var vendedor: String = ""
    private var gv_tipo: String = ""

    private lateinit var llenarControles: ClsLLenarControles
    lateinit var solicitudSoap: SolicitudSoap
    lateinit var dbHelper: SqLiteOpenHelper

    private lateinit var adaptadorCliente: AdaptadorItemxCliente
    private val datosList = mutableListOf<items>()
    lateinit var ReciclerviewIC: RecyclerView

    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.frmitemxcliente, container, false)

        llenarControles = ClsLLenarControles(requireContext())

        txtFechaInicial = view.findViewById(R.id.txtFechaInicialIC)
        txtFechaFinal = view.findViewById(R.id.txtFechaFinalIC)
        txtClienteIC = view.findViewById(R.id.txtClienteIC)
        txtItemIC = view.findViewById(R.id.txtItemIC)
        txtMarcaIC = view.findViewById(R.id.txtMarcaIC)
        btnBusquedaIC = view.findViewById(R.id.btnBusquedaIC)
        btnClienteIC = view.findViewById(R.id.btnClienteIC)
        ReciclerviewIC = view.findViewById(R.id.recyclerViewIC)

        dbHelper = SqLiteOpenHelper(requireContext())
        adaptadorCliente = AdaptadorItemxCliente(datosList, this)

        vendedor = llenarControles.fnObtenerVendedor()
        gv_tipo = llenarControles.fnObtenerTipoVendedorLogin(vendedor.toInt())

        if (gv_tipo == ExifInterface.GPS_MEASUREMENT_2D) {
            btnClienteIC.visibility = View.VISIBLE
            txtClienteIC.isEnabled = false
        }

        fnCargarAdapters()

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

        btnBusquedaIC.setOnClickListener {
            hideSoftKeyboard()
            fnConsultarItems()
        }

        btnClienteIC.setOnClickListener {
            fnCliente()
        }

        txtClienteIC.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank()) {
                    cl_codigo = "0"
                }
            }

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
        })

        txtItemIC.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank()) {
                    it_codigo = ""
                }
            }

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
        })

        txtMarcaIC.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank()) {
                    marca = "0"
                }
            }

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
        })

        return view
    }

    override fun onItemClick(item: items) {
        // Sin acción por ahora
    }

    private fun fnConsultarItems() {
        if (!isNetworkAvailable(requireContext())) {
            Toast.makeText(
                requireContext(),
                "Verifique su conexión a internet",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        var errorEncontrado = false

        if (txtFechaInicial.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Ingrese fecha inicial.", Toast.LENGTH_SHORT).show()
            errorEncontrado = true
        }

        if (!errorEncontrado && txtFechaFinal.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Ingrese fecha final.", Toast.LENGTH_SHORT).show()
            errorEncontrado = true
        }

        if (!errorEncontrado && txtClienteIC.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Ingreseel cliente.", Toast.LENGTH_SHORT).show()
            errorEncontrado = true
        }

        if (
            !errorEncontrado &&
            txtItemIC.text.isNullOrEmpty() &&
            txtMarcaIC.text.isNullOrEmpty()
        ) {
            Toast.makeText(
                requireContext(),
                "Debe ingresar el item o la marca.",
                Toast.LENGTH_SHORT
            ).show()
            errorEncontrado = true
        }

        if (!errorEncontrado) {
            solicitudSoap = SolicitudSoap(requireContext())
            val progressDialog = showProgressDialog()
            MiAsyncTask(progressDialog).execute()
        }
    }

    private fun fnLlenarAdaptador() {
        datosList.clear()
        adaptadorCliente.clearItems()

        val resultados = llenarControles.fnObtenerItemxCliente()
        for (dato in resultados) {
            datosList.add(dato)
        }

        ReciclerviewIC.layoutManager = LinearLayoutManager(requireContext())
        ReciclerviewIC.adapter = adaptadorCliente
    }

    private inner class MiAsyncTask(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String?>() {

        private lateinit var database: SQLiteDatabase

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                database = dbHelper.writableDatabase
                database.execSQL("DELETE FROM ve_ws_itemCliente")

                val marcaEnvio = if (marca.isEmpty()) "0" else marca

                val cadena =
                    "2,0,'$it_codigo',$cl_codigo,'A','${txtFechaInicial.text}','${txtFechaFinal.text}',1,-1,$marcaEnvio"

                solicitudSoap.initializeVariables(
                    getString(R.string.str_itemCliente).toInt(),
                    cadena
                )

                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }

                if (result.isNullOrBlank()) {
                    ""
                } else {
                    XmlParserItemxCliente.parseItemxCliente(
                        result,
                        database,
                        cadena,
                        requireContext()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            fnLlenarAdaptador()
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun fnCargarAdapters() {
        val adaptercliente = ClienteAdapter(
            requireContext(),
            llenarControles.fnCargarClientes()
        )

        txtClienteIC.setAdapter(adaptercliente)
        txtClienteIC.setOnItemClickListener { _, _, position, _ ->
            val cliente = adaptercliente.getItem(position)

            if (cliente != null) {
                txtClienteIC.setText(cliente.nombre, false)
                cl_codigo = cliente.id
            }
        }

        val listaItems = if (gv_tipo == ExifInterface.GPS_MEASUREMENT_2D) {
            llenarControles.fnCargarDatosItemBodega()
        } else {
            llenarControles.fnCargarDatosItem()
        }

        val adaptadorItem = TransporteAdapter(requireContext(), listaItems)

        txtItemIC.setAdapter(adaptadorItem)
        txtItemIC.setOnItemClickListener { _, _, position, _ ->
            val item = adaptadorItem.getItem(position)

            if (item != null) {
                txtItemIC.setText(item.nombre, false)
                it_codigo = item.codigo
            }
        }

        val adaptadorMarca = MarcaAdapter(
            requireContext(),
            llenarControles.fnCargarDatosMarca()
        )

        txtMarcaIC.setAdapter(adaptadorMarca)
        txtMarcaIC.setOnItemClickListener { _, _, position, _ ->
            val item = adaptadorMarca.getItem(position)

            if (item != null) {
                txtMarcaIC.setText(item.nombre, false)
                marca = item.codigo
            }
        }
    }

    private fun hideSoftKeyboard() {
        Log.d("hideSoftKeyboard", "Ocultando el teclado")

        val imm = requireActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

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
                val formattedDate = formatDate(
                    selectedYear,
                    selectedMonth,
                    selectedDay
                )

                targetView.text = formattedDate
                isDatePickerShown = false
            },
            year,
            month,
            day
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
        return String.format(
            Locale.getDefault(),
            "%02d/%02d/%04d",
            day,
            month + 1,
            year
        )
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

        when {
            !nombre.isNullOrBlank() -> {
                txtClienteIC.setText(nombre)
            }

            !razonComercial.isNullOrBlank() -> {
                txtClienteIC.setText(razonComercial)
            }

            else -> {
                txtClienteIC.setText(clientes.en_razonsocial)
            }
        }
    }
}