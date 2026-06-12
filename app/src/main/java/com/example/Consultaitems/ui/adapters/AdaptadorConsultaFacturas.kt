package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.SparseBooleanArray
import android.widget.CheckBox
import com.example.Consultaitems.R


class AdaptadorFacturas(
    private val datos: MutableList<Facturas>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdaptadorFacturas.ViewHolder>() {

    private val itemStateArray = SparseBooleanArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frm_detalle_consulta_facturas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        // Mostrar los datos en el TextView
        holder.txtBodegaDT.text = item.Bodega
        holder.txtCodigoDT.text = item.Factura
        holder.txtFechaDT.text = item.Fecha
        holder.txtValorDT.text = item.Valor
        holder.txtSaldoDT.text = item.Saldo

        // Eliminar cualquier listener de cambios anterior para evitar problemas al reciclar vistas
        holder.checkBox.setOnCheckedChangeListener(null)

        // Configurar el estado del CheckBox basado en el modelo de datos
        holder.checkBox.isChecked = item.isSelected

        // Vuelve a asignar el listener de cambios
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            // Actualizar el estado en el SparseBooleanArray
            itemStateArray.put(position, isChecked)
            // Actualizar el estado en el objeto de datos
            item.isSelected = isChecked
        }

        // Manejar el clic en el item (opcional)
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(item)
        }
    }


    override fun getItemCount() = datos.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkBoxSeleccion)
        val txtBodegaDT: TextView = view.findViewById(R.id.txtBodegaDT)
        val txtCodigoDT: TextView = view.findViewById(R.id.txtCodigoDT)
        val txtFechaDT: TextView = view.findViewById(R.id.txtFechaDT)
        val txtValorDT: TextView = view.findViewById(R.id.txtValorDT)
        val txtSaldoDT: TextView = view.findViewById(R.id.txtSaldoDT)
    }

    interface OnItemClickListener {
        fun onItemClick(item: Facturas)
    }

    fun clearItems() {
        datos.clear()
        itemStateArray.clear() // Limpia también los estados
        notifyDataSetChanged()
    }
}


data class Facturas(
    val Factura: String,
    var isSelected: Boolean = false,
    val Bodega: String,
    val Fecha: String,
    val Valor: String,
    val Saldo: String
)




