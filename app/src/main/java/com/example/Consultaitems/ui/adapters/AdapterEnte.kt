package com.example.Consultaitems.utils.cls

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class EnteAdapter(
    private val pedidos: List<EnteDialogFragment.Ente>,
    private val listener: (EnteDialogFragment.Ente) -> Unit
) : RecyclerView.Adapter<EnteAdapter.PedidoViewHolder>() {

    class PedidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumero: TextView = view.findViewById(R.id.txtNumCL)
        val tvCodigo: TextView = view.findViewById(R.id.txtCodigoCL)
        val tvNombre: TextView = view.findViewById(R.id.txtNombreCL)
        val tvEstado: TextView = view.findViewById(R.id.txtEstadoCL)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_consulta_ente, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val ente = pedidos[position]

        holder.tvNumero.text = ente.numero
        holder.tvCodigo.text = ente.codigo
        holder.tvNombre.text = ente.cliente
        holder.tvEstado.text = ente.estado

        if (ente.estado == "Activo") {
            holder.tvEstado.setBackgroundColor(Color.RED)
            holder.tvEstado.setTextColor(Color.WHITE)
        } else {
            holder.tvEstado.setBackgroundColor(Color.TRANSPARENT)
            holder.tvEstado.setTextColor(Color.BLACK)
        }

        holder.itemView.setOnClickListener {
            listener(ente)
        }
    }

    override fun getItemCount(): Int = pedidos.size
}
