package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.utils.parser.ClienteProspecto

class AdapterClienteProspecto(
    private val clientes: MutableList<ClienteProspecto>,
    private val onClienteClick: (ClienteProspecto, Int) -> Unit
) : RecyclerView.Adapter<AdapterClienteProspecto.ViewHolder>() {

    private var selectedPos = -1

    interface OnItemClickListener {
        fun onItemClick(item: ClienteProspecto, position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtSecGv: TextView = itemView.findViewById(R.id.txtSecGv)
        val txtnombreGv: TextView = itemView.findViewById(R.id.txtnombreGv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_detalle_gestion_venta, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cliente = clientes[position]
        holder.txtSecGv.text = cliente.secuencia.toString()
        holder.txtnombreGv.text = cliente.nombre
        holder.itemView.isSelected = position == selectedPos
        holder.itemView.setOnClickListener {
            val prev = selectedPos
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            selectedPos = pos
            if (prev != -1) notifyItemChanged(prev)
            notifyItemChanged(pos)
            onClienteClick(cliente, position)
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
