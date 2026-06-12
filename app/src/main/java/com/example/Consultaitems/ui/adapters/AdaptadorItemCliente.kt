package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class AdaptadorItemxCliente(
    private val datos: MutableList<items>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdaptadorItemxCliente.ViewHolder>() {

    private var onItemLongClickListener: OnItemLongClickListener? = null
    private var selectedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frmdetalleitemcliente, parent, false)
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
        val txtSriIC: TextView = view.findViewById(R.id.txtSriIC)
        val txtBodegaIC: TextView = view.findViewById(R.id.txtBodegaIC)
        val txtFechaFacIC: TextView = view.findViewById(R.id.txtFechaFacIC)
        val txtTipoFacIC: TextView = view.findViewById(R.id.txtTipoFacIC)
        val txtCantidadIC: TextView = view.findViewById(R.id.txtCantidadIC)
        val txtPrecioVenIC: TextView = view.findViewById(R.id.txtPrecioVenIC)
        val txtTotalIC: TextView = view.findViewById(R.id.txtTotalIC)
        val txtReferenciaIC: TextView = view.findViewById(R.id.txtReferenciaIC)
        val txtClienteIC: TextView = view.findViewById(R.id.txtClienteIC)

        fun bind(item: items) {
            txtSriIC.text = item.Sri
            txtBodegaIC.text = item.Bodega
            txtFechaFacIC.text = item.FechaF
            txtTipoFacIC.text = item.TipoF
            txtCantidadIC.text = item.Cantidad
            txtPrecioVenIC.text = item.PrecioV
            txtTotalIC.text = item.Total
            txtReferenciaIC.text = item.Referencia
            txtClienteIC.text = item.Cliente
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: items)
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

data class items(
    val Sri: String,
    val Bodega: String,
    val FechaF: String,
    val TipoF: String,
    val Cantidad: String,
    val PrecioV: String,
    val Total: String,
    val Cliente: String,
    val Referencia: String
)
