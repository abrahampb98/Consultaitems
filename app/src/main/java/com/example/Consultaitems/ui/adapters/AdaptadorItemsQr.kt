package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class AdaptadorItemsQr(
    private val lista: MutableList<ItemDetalle>
) : RecyclerView.Adapter<AdaptadorItemsQr.DetalleViewHolder>() {

    class DetalleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecio)
        val txtLote: TextView = itemView.findViewById(R.id.txtLote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetalleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_detalle_items_qr, parent, false)
        return DetalleViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetalleViewHolder, position: Int) {
        val item = lista[position]
        holder.txtNombre.text = item.nombre
        holder.txtPrecio.text = item.precio
        holder.txtLote.text = item.lote
    }

    override fun getItemCount(): Int = lista.size

    fun clearItems() {
        lista.clear()
        notifyDataSetChanged()
    }
}

data class ItemDetalle(
    val nombre: String,
    val precio: String,
    val lote: String
)
