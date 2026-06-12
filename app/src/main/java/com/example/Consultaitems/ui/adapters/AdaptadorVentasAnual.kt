package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.utils.cls.VentaAnual

class AdaptadorVentasAnuales(
    private val lista: List<VentaAnual>
) : RecyclerView.Adapter<AdaptadorVentasAnuales.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtAnio = view.findViewById<TextView>(R.id.txtAnio)
        val txtCantidad = view.findViewById<TextView>(R.id.txtCantidad)
        val txtTotal = view.findViewById<TextView>(R.id.txtTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_item_anual, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val venta = lista[position]
        holder.txtAnio.text = venta.anio.toString()
        holder.txtCantidad.text = "Cant: ${venta.cantidad}"
        //holder.txtTotal.text = "Total: \$${venta.totalVenta}"
    }

    override fun getItemCount(): Int = lista.size
}
