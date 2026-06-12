package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class AdaptadorStatusP(
    private val datos: MutableList<status>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdaptadorStatusP.ViewHolder>() {

    private var onItemLongClickListener: OnItemLongClickListener? = null
    private var selectedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frmdetallestatupedido, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        holder.bind(item)
        println("Elemento en la posición $position seleccionado: ${selectedPositions.contains(position)}")

        // Determinar si el elemento actual está seleccionado
        val isSelected = selectedPositions.contains(position)

        // Actualiza el estado de selección de la vista
        holder.itemView.isSelected = isSelected

        // Configura el OnClickListener para manejar los clics en los elementos
        holder.itemView.setOnClickListener {
            // Eliminar todas las selecciones anteriores
            selectedPositions.clear()
            // Marcar el elemento actual como seleccionado
            selectedPositions.add(position)
            // Notificar al adaptador de los cambios en la selección
            notifyDataSetChanged()
            itemClickListener.onItemClick(item) // Llama al método onItemClick del itemClickListener
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClickListener?.onItemLongClick(position)
            true
        }
    }

    override fun getItemCount() = datos.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtStatusSP: TextView = view.findViewById(R.id.txtStatusSP)
        val txtPedidoSP : TextView = view.findViewById(R.id.txtPedidoSP)
        val txtClienteSP : TextView = view.findViewById(R.id.txtClienteSP)
        val txtDispSP : TextView = view.findViewById(R.id.txtDispSP)
        val txtFechaSP: TextView = view.findViewById(R.id.txtFechaSP)
        val txtBodegaSP: TextView = view.findViewById(R.id.txtBodegaSP)
        val txtSriSP: TextView = view.findViewById(R.id.txtSriSP)
        val txtGrSP: TextView = view.findViewById(R.id.txtGrSP)
        val txtFechafSP: TextView = view.findViewById(R.id.txtFechafSP)
        val txtTotalSP: TextView = view.findViewById(R.id.txtTotalSP)
        val txtTotalfSP: TextView = view.findViewById(R.id.txtTotalfSP)
        val txtObservacionSP: TextView = view.findViewById(R.id.txtObservacionSP)

        fun bind(item: status) {
            txtStatusSP.text = item.Status
            txtPedidoSP.text = item.Pedido
            txtClienteSP.text = item.Cliente
            txtDispSP.text = item.Dispos
            txtFechaSP.text = item.FechaP
            txtBodegaSP.text = item.Bodega
            txtSriSP.text = item.Sri
            txtGrSP.text = item.Gr
            txtFechafSP.text = item.FechaF
            txtTotalSP.text = item.TotalP
            txtTotalfSP.text = item.TotalF
            txtObservacionSP.text = item.Observacion
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: status)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int)
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        onItemLongClickListener = listener
    }

    fun clearItems() {
        datos.clear()  // Limpia la lista de datos
        selectedPositions.clear()  // Limpia las selecciones
        notifyDataSetChanged()  // Notifica al adaptador del cambio
    }

}



data class status(
    val Status: String,
    val Pedido: String,
    val Cliente: String,
    val Dispos: String,
    val FechaP: String,
    val Bodega: String,
    val Sri: String,
    val Gr: String,
    val FechaF: String,
    val TotalP: String,
    val TotalF: String,
    val Observacion: String
)