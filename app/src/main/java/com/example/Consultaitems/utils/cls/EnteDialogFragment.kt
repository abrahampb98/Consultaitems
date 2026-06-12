package com.example.Consultaitems.utils.cls

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EnteDialogFragment : DialogFragment() {

    var onPedidoSelected: ((String) -> Unit)? = null

    private var isDatePickerShown = false
    private lateinit var llenarControles: ClsLLenarControles

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        llenarControles = ClsLLenarControles(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frmmostrarpedido, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvPedidos)
        val txtCliente = view.findViewById<EditText>(R.id.txtBuscCliente)
        val btnBuscar = view.findViewById<Button>(R.id.btnBuscaXCliente)
        val txtFechaInicial = view.findViewById<TextView>(R.id.txtFechaInicial)
        val txtFechaFinal = view.findViewById<TextView>(R.id.txtFechaFinal)

        txtFechaInicial.setOnClickListener {
            if (!isDatePickerShown) showDatePickerDialog(txtFechaInicial)
        }

        txtFechaFinal.setOnClickListener {
            if (!isDatePickerShown) showDatePickerDialog(txtFechaFinal)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        var entes: List<Ente> = llenarControles.fnObtenerEntes()
        recyclerView.adapter = EnteAdapter(entes) { ente ->
            onPedidoSelected?.invoke(ente.numero)
            dismiss()
        }

        btnBuscar.setOnClickListener {
            fnOcultarTeclado()

            val fechaIni = fnConvertirFecha(txtFechaInicial.text.toString())
            val fechaFin = fnConvertirFecha(txtFechaFinal.text.toString())
            val clienteTexto = txtCliente.text.toString().trim()

            entes = if (clienteTexto.isNotEmpty()) {
                llenarControles.fnObtenerEntexNombre(clienteTexto, fechaIni, fechaFin)
            } else {
                llenarControles.fnObtenerEntes()
            }

            txtCliente.setText("")
            txtFechaInicial.text = ""
            txtFechaFinal.text = ""

            if (entes.isNotEmpty()) {
                recyclerView.adapter = EnteAdapter(entes) { ente ->
                    onPedidoSelected?.invoke(ente.numero)
                    dismiss()
                }
            } else {
                Toast.makeText(requireContext(), "No se encontraron resultados", Toast.LENGTH_SHORT).show()
            }
        }
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
        return String.format(Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year)
    }

    fun fnConvertirFecha(fecha: String): String {
        return try {
            if (fecha.isBlank()) return ""
            val formatoEntrada = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date: Date = formatoEntrada.parse(fecha) ?: return ""
            formatoSalida.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun fnOcultarTeclado() {
        val activity = activity ?: return
        Log.d("hideSoftKeyboard", "Ocultando el teclado")
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val focusedView = activity.currentFocus ?: view ?: View(activity)
        imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
        Log.d("hideSoftKeyboard", "Teclado ocultado")
        forceRedrawWindow(activity)
    }

    fun forceRedrawWindow(activity: Activity) {
        val contentView = activity.findViewById<View>(android.R.id.content)
        contentView?.requestLayout()
    }

    data class Ente(
        val numero: String,
        val codigo: String,
        val cliente: String,
        val estado: String
    )
}
