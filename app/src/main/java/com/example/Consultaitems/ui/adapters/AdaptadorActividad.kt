package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class AdaptadorActividad(
    private val datos: MutableList<Actividades>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdaptadorActividad.ViewHolder>() {

    private var selectedPos: Int = -1

    interface OnItemClickListener {
        fun onItemClick(item: Actividades, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_detalle_actividades, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val visibles = datos.filter { it.proceso != -1 }
        val item = visibles[position]
        val realIndex = datos.indexOfFirst { it.ppCodigo == item.ppCodigo }

        holder.txtSecA.text = (position + 1).toString()
        holder.txtActividadA.text = item.gpTarea
        holder.itemView.isSelected = position == selectedPos
        holder.itemView.setOnClickListener {
            val prev = selectedPos
            selectedPos = position
            if (prev != -1) notifyItemChanged(prev)
            notifyItemChanged(position)
            itemClickListener.onItemClick(item, realIndex)
        }
    }

    override fun getItemCount(): Int = datos.count { it.proceso != -1 }

    fun setItems(nuevos: List<Actividades>) {
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
        val txtSecA: TextView = view.findViewById(R.id.txtSecA)
        val txtActividadA: TextView = view.findViewById(R.id.txtActividadA)
    }

    data class Actividades(
        val emCodigo: Int,
        val gpFechaTrn: String,
        val epCodigo: Int,
        val dpCodigo: Int,
        val dpDescripcion: String,
        val gpTarea: String,
        val gpObservacion: String,
        val gpRecurso: String,
        val gpDuracion: Int,
        val gpAvance: Int,
        val gpFechaInicial: String,
        val gpFechaFinal: String,
        val proceso: Int,
        val gpCodigo: Int,
        val gpTareaP: String,
        val ppCodigo: Int,
        val ptDescripcion: String,
        val ptObjetivo: String,
        val ppActividad: String,
        val ppTarea: String
    )
}
