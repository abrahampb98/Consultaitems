package com.example.Consultaitems.ui.adapters

import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class AdaptadorConsultaFactura(
    private val datos: MutableList<ConsultaFacturas>,
    private val itemClickListener: OnItemClickListener,
    private val doubleClickListener: (ConsultaFacturas, Int) -> Unit
) : RecyclerView.Adapter<AdaptadorConsultaFactura.ViewHolder>() {

    private var selectedPos = -1

    interface OnItemClickListener {
        fun onItemClick(item: ConsultaFacturas, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_detalle_consulta_factura, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]
        holder.txtFactutaF.text = item.factura.toString()
        holder.txtSriF.text = item.sri
        holder.txtFechaF.text = item.fecha
        holder.txtClienteF.text = item.cliente
        holder.txtTotalF.text = item.total
        holder.txtVendedorF.text = item.vendedor
        holder.itemView.isSelected = position == selectedPos

        holder.itemView.setOnClickListener(
            DoubleClickListener(
                onDoubleClick = {
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) doubleClickListener(datos[pos], pos)
                },
                onSingleClick = {
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val prev = selectedPos
                        selectedPos = pos
                        if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev)
                        notifyItemChanged(pos)
                        itemClickListener.onItemClick(datos[pos], pos)
                    }
                }
            )
        )
    }

    override fun getItemCount(): Int = datos.size

    fun setItems(nuevos: List<ConsultaFacturas>) {
        datos.clear()
        datos.addAll(nuevos)
        selectedPos = -1
        notifyDataSetChanged()
    }

    fun clearItems() {
        datos.clear()
        selectedPos = -1
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtFactutaF: TextView = view.findViewById(R.id.txtFactutaF)
        val txtSriF: TextView = view.findViewById(R.id.txtSriF)
        val txtFechaF: TextView = view.findViewById(R.id.txtFechaF)
        val txtClienteF: TextView = view.findViewById(R.id.txtClienteF)
        val txtTotalF: TextView = view.findViewById(R.id.txtTotalF)
        val txtVendedorF: TextView = view.findViewById(R.id.txtVendedorF)
    }

    private class DoubleClickListener(
        private val onDoubleClick: () -> Unit,
        private val onSingleClick: () -> Unit,
        private val doubleClickTimeout: Long = 250L
    ) : View.OnClickListener {
        private var lastClickTime = 0L

        override fun onClick(v: View?) {
            val now = SystemClock.uptimeMillis()
            if (now - lastClickTime < doubleClickTimeout) {
                lastClickTime = 0L
                onDoubleClick()
            } else {
                lastClickTime = now
                v?.postDelayed({
                    if (SystemClock.uptimeMillis() - lastClickTime >= doubleClickTimeout) {
                        onSingleClick()
                    }
                }, doubleClickTimeout)
            }
        }
    }
}

data class ConsultaFacturas(
    val factura: Int,
    val sri: String,
    val fecha: String,
    val cliente: String,
    val total: String,
    val vendedor: String,
    val ep_codigo: Int
)
