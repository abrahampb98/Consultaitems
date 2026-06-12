package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class AdaptadorReporteActividades(
    private val lista: List<ReporteActividad>
) : RecyclerView.Adapter<AdaptadorReporteActividades.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_detalle_reporte_actividades, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.txtActividad.text = item.actividad
        holder.txtObservacion.text = item.observacion
        holder.txtRecurso.text = item.recurso
        holder.txtDias.text = item.dias.toString()
        holder.txtInicio.text = item.inicio
        holder.txtFin.text = item.fin
        holder.txtPorcentaje.text = item.porcentaje.toString()
        holder.txtStatus.text = item.status
    }

    override fun getItemCount(): Int = lista.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtActividad: TextView = view.findViewById(R.id.txtActividad)
        val txtObservacion: TextView = view.findViewById(R.id.txtObservacion)
        val txtRecurso: TextView = view.findViewById(R.id.txtRecurso)
        val txtDias: TextView = view.findViewById(R.id.txtDias)
        val txtInicio: TextView = view.findViewById(R.id.txtInicio)
        val txtFin: TextView = view.findViewById(R.id.txtFin)
        val txtPorcentaje: TextView = view.findViewById(R.id.txtPorcentaje)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
    }

    data class ReporteActividad(
        val actividad: String,
        val observacion: String,
        val recurso: String,
        val dias: Int,
        val inicio: String,
        val fin: String,
        val porcentaje: Int,
        val status: String
    )
}
