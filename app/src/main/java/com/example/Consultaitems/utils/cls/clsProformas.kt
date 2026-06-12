package com.example.Consultaitems.utils.cls

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
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
import java.util.Locale

class ProformaAdapter(
    private val pedidos: List<ProformasDialogFragment.Proforma>,
    private val listener: OnProformaClickListener
) : RecyclerView.Adapter<ProformaAdapter.PedidoViewHolder>() {

    interface OnProformaClickListener {
        fun onPedidoClick(Proforma: ProformasDialogFragment.Proforma)
    }

    class PedidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumeroPedido: TextView = view.findViewById(R.id.txtPedidoView)
        val tvCliente: TextView = view.findViewById(R.id.txtClienteView)
        val tvTotal: TextView = view.findViewById(R.id.txtTotalView)
        val tvNumeroInterno: TextView = view.findViewById(R.id.txtPedIntView)
        val tvEstado: TextView = view.findViewById(R.id.txtEstadoView)
        val tvLote: TextView = view.findViewById(R.id.txtLoteView)
        val tvObservaciones: TextView = view.findViewById(R.id.txtObservacionView)
        val tvFecha: TextView = view.findViewById(R.id.txtFechaView)
        val tvFactura: TextView = view.findViewById(R.id.txtFacturaView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frm_consulta_proformas, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        holder.tvNumeroPedido.text = pedido.numero
        holder.tvCliente.text = pedido.cliente
        holder.tvTotal.text = pedido.total
        holder.tvNumeroInterno.text = pedido.numeroInterno
        holder.tvEstado.text = pedido.estado
        holder.tvLote.text = pedido.lote
        holder.tvObservaciones.text = pedido.observaciones
        holder.tvFecha.text = pedido.fecha
        holder.tvFactura.text = pedido.factura

        if (pedido.estado == "Activo")
        {
            holder.tvEstado.setBackgroundColor(Color.RED)
            holder.tvEstado.setTextColor(Color.WHITE)
        }

        // Configura el click listener para cada elemento
        holder.itemView.setOnClickListener {
            listener.onPedidoClick(pedido)
        }
    }

    override fun getItemCount() = pedidos.size
}

class ProformasDialogFragment : DialogFragment() {
    var onPedidoSelected: ((String) -> Unit)? = null
    private lateinit var llenarControles: ClsLLenarControles
    private var isDatePickerShown = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Usar requireContext() para asegurar que el contexto está disponible
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
        val recyclerView: RecyclerView = view.findViewById(R.id.rvPedidos)
        val txtCliente: EditText = view.findViewById(R.id.txtBuscCliente)
        val btnBuscar: Button = view.findViewById(R.id.btnBuscaXCliente)
        val txtFechaInicial: TextView = view.findViewById(R.id.txtFechaInicial)
        val txtFechaFinal: TextView = view.findViewById(R.id.txtFechaFinal)

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


        recyclerView.layoutManager = LinearLayoutManager(context)

        // Cargar todos los pedidos inicialmente
        var pedidos = llenarControles.fnObtenerProformas()
        recyclerView.adapter =
            ProformaAdapter(pedidos, object : ProformaAdapter.OnProformaClickListener  {
                override fun onPedidoClick(pedido: Proforma) {
                    onPedidoSelected?.invoke(pedido.numero)
                    dismiss()  // Cerrar el diálogo tras seleccionar
                }
            })

        // Manejar el evento de clic del botón de búsqueda
        btnBuscar.setOnClickListener {
            fnOcultarTeclado(this)

            val fechaIni = fnConvertirFecha(txtFechaInicial.text.toString())
            val fechaFin = fnConvertirFecha(txtFechaFinal.text.toString())

            val clienteTexto = txtCliente.text.toString()
            if (clienteTexto.isNotEmpty()) {
                pedidos =
                    llenarControles.fnObtenerProformasPorCliente(clienteTexto, fechaIni, fechaFin)
            } else {
                pedidos = llenarControles.fnObtenerProformas()
            }
            txtCliente.setText("")
            txtFechaFinal.setText("")
            txtFechaInicial.setText("")

            if (pedidos.isNotEmpty()) {
                recyclerView.adapter =
                    ProformaAdapter(pedidos, object : ProformaAdapter.OnProformaClickListener {
                        override fun onPedidoClick(pedido: ProformasDialogFragment.Proforma) {
                            onPedidoSelected?.invoke(pedido.numero)
                            dismiss()  // Cerrar el diálogo tras seleccionar
                        }
                    })
            } else {
                Toast.makeText(context, "No se encontraron resultados", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showDatePickerDialog(targetView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker =
            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
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
        return String.format(Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year)
    }

    fun fnConvertirFecha(fecha: String): String {
        if (fecha.isNotEmpty()) {
            val formatoEntrada = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return formatoSalida.format(formatoEntrada.parse(fecha)!!)
        } else {
            return ""
        }
    }


    fun fnOcultarTeclado(fragment: DialogFragment) {
        val activity = fragment.activity
        if (activity != null) {
            Log.d("hideSoftKeyboard", "Ocultando el teclado")
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val view = activity.currentFocus ?: fragment.view
            ?: View(activity) // Usar la vista del Fragment o una vista de respaldo
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            Log.d("hideSoftKeyboard", "Teclado ocultado")
            forceRedrawWindow(activity)
        }
    }

    fun forceRedrawWindow(activity: Activity) {
        val contentView = activity.findViewById<View>(android.R.id.content)
        contentView.requestLayout()
    }

    data class Proforma(
        val numero: String,
        val cliente: String,
        val total: String,
        val numeroInterno: String,
        val estado: String,
        val lote: String,
        val observaciones: String,
        val fecha: String,
        val factura: String
    )
}



