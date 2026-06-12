package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.utils.cls.Avance

class AdapterAvance(
    private val clientes: MutableList<Avance>,
    private val onClienteClick: (Avance, Int) -> Unit
) : RecyclerView.Adapter<AdapterAvance.ViewHolder>() {

    private var selectedPos = -1

    interface OnItemClickListener {
        fun onItemClick(item: Avance, position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtSecGv: TextView = itemView.findViewById(R.id.txtSecGv)
        val txtnombreGv: TextView = itemView.findViewById(R.id.txtnombreGv)

        init {
            (txtSecGv.layoutParams as? LinearLayout.LayoutParams)?.let { params ->
                params.width = 0
                params.weight = 0.2f
                txtSecGv.layoutParams = params
            }
            (txtnombreGv.layoutParams as? LinearLayout.LayoutParams)?.let { params ->
                params.width = 0
                params.weight = 1.3f
                txtnombreGv.layoutParams = params
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_detalle_gestion_venta, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val avance = clientes[position]
        holder.txtSecGv.text = avance.fecha
        holder.txtnombreGv.text = avance.linea
        holder.itemView.isSelected = position == selectedPos
        holder.itemView.setOnClickListener {
            val prev = selectedPos
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            selectedPos = pos
            if (prev != -1) notifyItemChanged(prev)
            notifyItemChanged(pos)
            onClienteClick(avance, position)
        }
    }

    fun clearItems() {
        clientes.clear()
        selectedPos = -1
        notifyDataSetChanged()
    }

    fun selectItem(position: Int) {
        if (position in clientes.indices) {
            val prev = selectedPos
            selectedPos = position
            if (prev != -1) notifyItemChanged(prev)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = clientes.size
}
