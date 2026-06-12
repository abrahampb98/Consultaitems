package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class AdaptadorPlantilla(
    private val lista: MutableList<Plantilla>,
    private val listener: OnActividadCheckedListener
) : RecyclerView.Adapter<AdaptadorPlantilla.ViewHolder>() {

    private var habilitado: Boolean = false

    interface OnActividadCheckedListener {
        fun onChecked(actividad: Plantilla)
        fun onUnchecked(actividad: Plantilla)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_detalle_plantilla, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.txtDescripcion.text = item.descripcion
        holder.chkActividad.isEnabled = habilitado
        holder.itemView.isEnabled = habilitado
        holder.chkActividad.setOnCheckedChangeListener(null)
        holder.chkActividad.isChecked = item.isChecked

        if (habilitado) {
            holder.chkActividad.setOnCheckedChangeListener { _, isChecked ->
                item.isChecked = isChecked
                if (isChecked) listener.onChecked(item) else listener.onUnchecked(item)
            }
            holder.itemView.setOnClickListener {
                holder.chkActividad.isChecked = !holder.chkActividad.isChecked
            }
        } else {
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun getSeleccionados(): List<Plantilla> = lista.filter { it.isChecked }

    fun clearItems() {
        lista.clear()
        notifyDataSetChanged()
    }

    fun habilitar() {
        habilitado = true
        notifyDataSetChanged()
    }

    fun deshabilitar() {
        habilitado = false
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chkActividad: CheckBox = view.findViewById(R.id.chkActividad)
        val txtDescripcion: TextView = view.findViewById(R.id.txtDescripcion)
    }

    data class Plantilla(
        val codigo: Int,
        val descripcion: String,
        val proceso: String,
        val objetivo: String,
        val tarea: String,
        var isChecked: Boolean = false
    )
}
