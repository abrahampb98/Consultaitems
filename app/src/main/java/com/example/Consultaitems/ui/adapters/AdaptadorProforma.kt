package com.example.Consultaitems.ui.adapters

import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class AdaptadorProforma(
    private val datos: MutableList<Proformas>,
    private val itemClickListener: OnItemClickListener,
    private val doubleClickListener: (Proformas, Int) -> Unit
) : RecyclerView.Adapter<AdaptadorProforma.ViewHolder>() {

    private var selectedPos: Int = -1

    interface OnItemClickListener {
        fun onItemClick(item: Proformas, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_detalle_proformas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        holder.txtnumeroP.text = item.numero.toString()
        holder.txtVendedorP.text = item.vendedor
        holder.txtClienteP.text = item.cliente
        holder.txtFechaP.text = item.fecha
        holder.txtTotalP.text = item.total

        holder.itemView.isSelected = position == selectedPos
        holder.itemView.setOnClickListener(
            DoubleClickListener(
                onDoubleClick = {
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        doubleClickListener(datos[pos], pos)
                    }
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

    fun setItems(nuevos: List<Proformas>) {
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
        val txtnumeroP: TextView = view.findViewById(R.id.txtnumeroP)
        val txtVendedorP: TextView = view.findViewById(R.id.txtVendedorP)
        val txtClienteP: TextView = view.findViewById(R.id.txtClienteP)
        val txtFechaP: TextView = view.findViewById(R.id.txtFechaP)
        val txtTotalP: TextView = view.findViewById(R.id.txtTotalP)
    }

    private class DoubleClickListener(
        private val onDoubleClick: () -> Unit,
        private val onSingleClick: () -> Unit,
        private val doubleClickTimeout: Long = 250L
    ) : View.OnClickListener {
        private var lastClickTime: Long = 0L

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


data class Proformas(
    val numero: Int,
    val vendedor: String,
    val cliente: String,
    val fecha: String,
    val total: String
)
