package com.example.Consultaitems.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.activities.frmLogin
import com.example.Consultaitems.ui.adapters.AdaptadorClienteItems
import com.example.Consultaitems.ui.adapters.AdaptadorClientes
import com.example.Consultaitems.ui.adapters.ClienteAdapter
import com.example.Consultaitems.ui.adapters.TransporteAdapter
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.HistorialResumen
import com.example.Consultaitems.utils.cls.consultaCliente
import com.example.Consultaitems.utils.parser.XmlParserClientItems

class frmClienteItems : Fragment(),
    AdaptadorClienteItems.OnItemClickListener,
    consultaCliente.OnItemSelectedListener {

    lateinit var ReciclerviewCI: RecyclerView
    lateinit var solicitudSoap: SolicitudSoap
    lateinit var dbHelper: SqLiteOpenHelper

    private lateinit var txtClienteCI: AutoCompleteTextView
    private lateinit var txtItemCI: AutoCompleteTextView
    private lateinit var btnBusquedaCI: Button
    private lateinit var btnClienteCI: ImageButton
    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var adaptadorItemCliente: AdaptadorClienteItems

    private var cl_codigo: String = ""
    private var it_codigo: String = ""
    private var gv_tipo: String = ""

    private val datosList = mutableListOf<HistorialResumen>()
    private val vendedor: Int = frmLogin.CadenaHolder.ep_codigo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.frm_cliente_item, container, false)

        llenarControles = ClsLLenarControles(requireContext())
        adaptadorItemCliente = AdaptadorClienteItems(datosList, this)

        txtClienteCI = view.findViewById(R.id.txtClienteCI)
        txtItemCI = view.findViewById(R.id.txtItemCI)
        btnBusquedaCI = view.findViewById(R.id.btnBusquedaCI)
        btnClienteCI = view.findViewById(R.id.btnClienteCI)
        ReciclerviewCI = view.findViewById(R.id.recyclerViewCI)
        dbHelper = SqLiteOpenHelper(requireContext())

        val anios = (2019..2026).toList()
        val layoutCabecera = view.findViewById<LinearLayout>(R.id.layout3)
        generarCabeceras(layoutCabecera, anios)

        gv_tipo = llenarControles.fnObtenerTipoVendedorLogin(vendedor)
        if (gv_tipo == "2") {
            btnClienteCI.visibility = View.VISIBLE
            txtClienteCI.isEnabled = false
        }

        fnCargarAdapters()

        btnBusquedaCI.setOnClickListener {
            hideSoftKeyboard()
            fnConsultarItems(gv_tipo)
        }

        btnClienteCI.setOnClickListener {
            txtClienteCI.setText("")
            fnCliente()
        }

        txtClienteCI.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    cl_codigo = ""
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        txtItemCI.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    it_codigo = ""
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        return view
    }

    private fun fnCargarAdapters() {
        val adapterCliente = ClienteAdapter(requireContext(), llenarControles.fnCargarClientes())
        txtClienteCI.setAdapter(adapterCliente)
        txtClienteCI.setOnItemClickListener { _, _, position, _ ->
            val cliente = adapterCliente.getItem(position)
            if (cliente != null) {
                txtClienteCI.setText(cliente.nombre, false)
                cl_codigo = cliente.id
            }
        }

        val datosItems = if (gv_tipo == "1") {
            llenarControles.fnCargarDatosItem()
        } else {
            llenarControles.fnCargarDatosItemAlmacen()
        }

        Log.e("tipo", datosItems.toString())

        val adaptadorItem = TransporteAdapter(requireContext(), datosItems)
        txtItemCI.setAdapter(adaptadorItem)
        txtItemCI.setOnItemClickListener { _, _, position, _ ->
            val item = adaptadorItem.getItem(position)
            if (item != null) {
                txtItemCI.setText(item.nombre, false)
                it_codigo = item.codigo
            }
        }
    }

    private fun generarCabeceras(layout: LinearLayout, anios: List<Int>) {
        layout.removeAllViews()

        val txtCliente = TextView(requireContext()).apply {
            text = "Cliente"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                2.8f
            )
        }
        layout.addView(txtCliente)

        anios.forEach { anio ->
            val txtAnio = TextView(requireContext()).apply {
                text = anio.toString()
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setTextColor(Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.5f
                )
            }
            layout.addView(txtAnio)
        }
    }

    private fun fnConsultarItems(gvTipo: String) {
        if (gvTipo != "2") {
            fnLlenarAdaptador()
        } else if (it_codigo == "" && cl_codigo == "") {
            showToast("Debe seleccionar un criterio")
        } else {
            fnConsultaItemsWeb()
        }
    }

    private fun fnCliente() {
        val dialog = consultaCliente()
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "consultaCliente")
    }

    override fun onItemsSelected(clientes: AdaptadorClientes.Clientes) {
        cl_codigo = clientes.codigo

        val nombreCliente = when {
            !clientes.nombre.isNullOrBlank() -> clientes.nombre
            !clientes.razonComercial.isNullOrBlank() -> clientes.razonComercial
            else -> clientes.en_razonsocial
        }

        txtClienteCI.setText(nombreCliente ?: "", false)
    }

    fun fnConsultaItemsWeb() {
        if (isNetworkAvailable(requireContext())) {
            solicitudSoap = SolicitudSoap(requireContext())
            val progressDialog = showProgressDialog()
            MiAsyncTask(progressDialog).execute()
        } else {
            Toast.makeText(requireContext(), "Verifique su conexión a internet", Toast.LENGTH_LONG).show()
        }
    }

    private fun showProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando Datos...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
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

    override fun onItemClick(item: HistorialResumen) {
        // Sin acción por ahora.
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private inner class MiAsyncTask(private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void?): String? {
            return try {
                database = dbHelper.writableDatabase
                database.execSQL("DELETE FROM fa_ws_ventasTmp")

                val cliente = if (cl_codigo != "") cl_codigo else "0"
                val cadena = "$vendedor,5,'$it_codigo',$cliente"

                solicitudSoap.initializeVariables(getString(R.string.str_ClienteItem).toInt(), cadena)

                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.bufferedReader()?.use { it.readText() }

                if (!result.isNullOrBlank()) {
                    XmlParserClientItems.parseTable(result, database, requireContext())
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("Error", "Exception in doInBackground: ${e.message}", e)
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_SHORT).show()
                }
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            fnLlenarAdaptador()
        }
    }

    fun fnLlenarAdaptador() {
        datosList.clear()
        adaptadorItemCliente.clearItems()

        val resultados = if (gv_tipo != "2") {
            llenarControles.fnObtenerHistorial(cl_codigo, it_codigo)
        } else {
            llenarControles.fnObtenerHistorialWeb(cl_codigo, it_codigo)
        }

        for (dato in resultados) {
            datosList.add(dato)
        }

        ReciclerviewCI.layoutManager = LinearLayoutManager(requireContext())
        ReciclerviewCI.adapter = adaptadorItemCliente
    }
}
