package com.example.Consultaitems.ui.adapters

import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.utils.cls.ConsultaRutas

class AdaptadorConsultaRutas(
    private val datos: MutableList<ConsultaRutas>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdaptadorConsultaRutas.ViewHolder>() {

    private val itemStateArray = SparseBooleanArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frm_detalle_consulta_rutas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        // Mostrar los datos en el TextView
        holder.tvSec.text = item.sec
        holder.tvClienteNombre.text = item.cliente
        holder.tvCiudad.text = item.Ciudad
        holder.tvDia.text = item.dia
        // Eliminar cualquier listener anterior
        //holder.chkVenta.setOnCheckedChangeListener(null)
        // Configurar el estado del CheckBox
        holder.chkVenta.isChecked = item.isSelectedVenta
        holder.chkCobro.isChecked = item.isSelectedCobro
        holder.chkVisita.isChecked = item.isSelectedVisita
        holder.chkGT.isChecked = item.isSelectedTelefono
        holder.tvObservacion.text = item.observacion
        holder.tvEnvio.text = item.fechaProceso

        holder.chkVenta.isEnabled = false
        holder.chkVisita.isEnabled = false
        holder.chkCobro.isEnabled = false
        holder.chkGT.isEnabled = false


        // Manejar el clic en el item (opcional)
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(item)
        }




    }

    override fun getItemCount() = datos.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSec: TextView = view.findViewById(R.id.tvSecCR)
        val tvClienteNombre: TextView = view.findViewById(R.id.tvClienteNombreCR)
        val tvCiudad: TextView = view.findViewById(R.id.tvCiudadCR)
        val tvDia: TextView = view.findViewById(R.id.tvDiaCR)
        val chkVisita: CheckBox = view.findViewById(R.id.chkVisitaCR)
        val chkCobro: CheckBox = view.findViewById(R.id.chkCobroCR)
        val chkVenta: CheckBox = view.findViewById(R.id.chkVentaCR)
        val chkGT: CheckBox = view.findViewById(R.id.chkGTCR)
        val tvObservacion: TextView = view.findViewById(R.id.tvObservacionCR)
        val tvEnvio: TextView = view.findViewById(R.id.tvEnvioCR)
    }

    interface OnItemClickListener {
        fun onItemClick(item: ConsultaRutas)

        fun observacion(codigo: String, cliente: String, observacion: String)
    }

    fun clearItems() {
        datos.clear()
        itemStateArray.clear() // Limpia también los estados
        notifyDataSetChanged()
    }

}
