package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class AdaptadorFact(private val datos: MutableList<Factura>) : RecyclerView.Adapter<AdaptadorFact.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frm_detalle_facturas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]
        holder.txtCodigo.text = item.codigo
        holder.txtReferencia.text = item.referencia
        holder.txtDescuento.text = item.descuento
        holder.txtCantidad.text = item.cantidad
        holder.txtPrecio.text = item.precio
        holder.txtSubtotal.text = item.subtotal
    }

    override fun getItemCount() = datos.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtCodigo: TextView = view.findViewById(R.id.txtCodigoF)
        val txtReferencia: TextView = view.findViewById(R.id.txtReferenciaF)
        val txtDescuento: TextView = view.findViewById(R.id.txtDescuentoF)
        val txtCantidad: TextView = view.findViewById(R.id.txtCantidadF)
        val txtPrecio: TextView = view.findViewById(R.id.txtPrecioF)
        val txtSubtotal: TextView = view.findViewById(R.id.txtSubtotalF)
    }

    fun clearItems() {
        datos.clear()  // Limpia la lista de datos
        notifyDataSetChanged()  // Notifica al adaptador del cambio
    }

}

data class Factura(
    val desct: String,
    val lote: String,
    val observacion: String,
    val codigo: String,
    val referencia: String,
    val descuento: String,
    val cantidad: String,
    val precio: String,
    val subtotal: String
)
