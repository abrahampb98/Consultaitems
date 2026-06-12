package com.example.Consultaitems.ui.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
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
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.utils.parser.XmlParserAuditoriaPedido
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.adapters.AdaptadorStatusP
import com.example.Consultaitems.ui.adapters.status
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import java.util.Calendar
import java.util.Locale

class frmStatusPedido: Fragment() , AdaptadorStatusP.OnItemClickListener {
    private var isDatePickerShown = false
    private lateinit var txtFechaInicial: TextView
    private lateinit var txtFechaFinal: TextView
    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var btnBusquedaSP: Button
    private var vendedor: String = ""
    private val datosList = mutableListOf<status>()
    lateinit var ReciclerviewSP: RecyclerView
    private lateinit var adaptadorStatus: AdaptadorStatusP
    lateinit var solicitudSoap: SolicitudSoap
    lateinit var dbHelper: SqLiteOpenHelper
    private lateinit var txtVendedor: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento
        val view = inflater.inflate(R.layout.frmstatuspedido, container, false)



        llenarControles = ClsLLenarControles(requireContext())
        txtFechaInicial = view.findViewById(R.id.txtFechaInicialSP)
        txtFechaFinal = view.findViewById(R.id.txtFechaFinalSP)
        btnBusquedaSP = view.findViewById(R.id.btnBusquedaSP)
        txtVendedor = view.findViewById(R.id.txtVendedorSP)
        vendedor = llenarControles.fnObtenerVendedor()
        dbHelper = SqLiteOpenHelper(requireContext())
        adaptadorStatus = AdaptadorStatusP(datosList, this)
        ReciclerviewSP = view.findViewById(R.id.recyclerViewSP)

        txtVendedor.text = llenarControles.fnOtenerNombreUsuario(vendedor.toInt())


        btnBusquedaSP.setOnClickListener {
            if (txtFechaInicial.text.isNullOrEmpty() || txtFechaFinal.text.isNullOrEmpty()) {

                Toast.makeText(requireContext(), "Debe ingresar fecha inicial y final", Toast.LENGTH_SHORT).show()
            }else{
                //verifica la conexion a interntet
                if (isNetworkAvailable(requireContext())) {
                    hideSoftKeyboard()
                    fnConsultarItems()
                }else{
                    Toast.makeText(requireContext(), "Verifique su conexion a internet", Toast.LENGTH_SHORT).show()
                }

            }


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
        return view
    }


    override fun onItemClick(item: status) {

    }




    private fun fnConsultarItems(){

        solicitudSoap = SolicitudSoap(requireContext())
        val progressDialog = showProgressDialog()
        MiAsyncTask(progressDialog).execute()
    }

    private fun fnLlenarAdaptador(){

        datosList.clear()
        adaptadorStatus.clearItems()

        val resultados = llenarControles.fnObtenerStatus()
        for (dato in resultados) {
            datosList.add(dato)
        }
        // Configura el RecyclerView y asigna el adaptador
        val layoutManager = LinearLayoutManager(requireContext())
        ReciclerviewSP.layoutManager = layoutManager
        ReciclerviewSP.adapter = adaptadorStatus
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


    private fun formatDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)
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




    private inner class MiAsyncTask(private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase
        private var datosInsertados: String = ""

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {
            database = dbHelper.writableDatabase

           /* database.execSQL("""
            CREATE TABLE IF NOT EXISTS fa_ws_auditoriapedido (
                pe_coddocumento TEXT,
                nombreCliente TEXT,
                te_descripcion TEXT,
                pe_fechaing TEXT,
                pe_valorTotal TEXT,
                fa_coddocumento TEXT,
                fa_sri TEXT,
                fa_fechafactura TEXT,
                fa_guiaremision TEXT,
                estado TEXT,
                pe_observacion TEXT,
                bodega TEXT,
                totalfact TEXT
            )
        """)*/

            database.execSQL("DELETE FROM fa_ws_auditoriapedido")


            val cadena = "$vendedor,'${txtFechaInicial.text}','${txtFechaFinal.text}',1"

            //val cadena = "2,0,'873205423',68532,'A','01/01/2022','29/05/2024',1,-1,0"
            solicitudSoap.initializeVariables(getString(R.string.str_Status).toInt(), cadena)
            var pedido: String = ""
            val inputStream = solicitudSoap.realizarSolicitudSoap()
            val result = inputStream?.bufferedReader()?.use { it.readText() }
            if (!result.isNullOrBlank()) {
                pedido = XmlParserAuditoriaPedido.parseAuditoriaPedido(
                    result,
                    database,
                    cadena,
                    requireContext()
                )
            }
            return pedido
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            fnLlenarAdaptador()

        }

        }

}
