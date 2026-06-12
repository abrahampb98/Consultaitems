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

class AdaptadorRutas(
    private val datos: MutableList<Rutas>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdaptadorRutas.ViewHolder>() {

    private val itemStateArray = SparseBooleanArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frm_detalles_visita_cliente, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        holder.tvClienteNombre.text = item.cliente
        holder.tvSec.text = item.sec
        holder.tvSaldo.text = item.Saldo
        holder.tvCiudad.text = item.Ciudad
        holder.tvCupo.text = item.Cupo
        holder.tvDistancia.text = item.distancia

        // Quitar listeners antes de cambiar estados
        holder.chkVenta.setOnCheckedChangeListener(null)
        holder.chkCobro.setOnCheckedChangeListener(null)
        holder.chkVisita.setOnCheckedChangeListener(null)
        holder.chkGT.setOnCheckedChangeListener(null)

        // Pintar estados sin disparar eventos
        holder.chkVenta.isChecked = item.isSelectedVenta
        holder.chkCobro.isChecked = item.isSelectedCobro
        holder.chkVisita.isChecked = item.isSelectedVisita
        holder.chkGT.isChecked = item.isSelectedTelefono

        holder.chkVenta.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener

            item.isSelectedVenta = isChecked
            itemClickListener.onCheckBoxClickVenta(
                item.codigo,
                if (isChecked) 1 else 0,
                item.estado
            )
        }

        holder.chkCobro.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener

            item.isSelectedCobro = isChecked
            itemClickListener.onCheckBoxClickCobro(
                item.codigo,
                if (isChecked) 1 else 0,
                item.estado
            )
        }

        holder.chkVisita.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener

            item.isSelectedVisita = isChecked
            itemClickListener.onCheckBoxClickVisita(
                item.codigo,
                if (isChecked) 1 else 0,
                item.estado
            )
        }

        holder.chkGT.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener

            item.isSelectedTelefono = isChecked
            itemClickListener.onCheckBoxClickGT(
                item.codigo,
                if (isChecked) 1 else 0,
                item.estado
            )
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(item)
        }

        holder.btn.setOnClickListener {
            itemClickListener.observacion(item.codigo, item.cliente, item.observacion)
        }
    }

    override fun getItemCount() = datos.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSec: TextView = view.findViewById(R.id.tvSec)
        val tvSaldo: TextView = view.findViewById(R.id.tvSaldo)
        val tvCiudad: TextView = view.findViewById(R.id.tvCiudad)
        val chkVisita: CheckBox = view.findViewById(R.id.chkVisita)
        val chkCobro: CheckBox = view.findViewById(R.id.chkCobro)
        val chkVenta: CheckBox = view.findViewById(R.id.chkVenta)
        val tvClienteNombre: TextView = view.findViewById(R.id.tvClienteNombre)
        val tvCupo: TextView = view.findViewById(R.id.tvCupo)
        val tvDistancia: TextView = view.findViewById(R.id.tvDistancia)
        val btn: Button = view.findViewById(R.id.bntObservacionR)
        val chkGT: CheckBox = view.findViewById(R.id.chkGT)
    }

    interface OnItemClickListener {
        fun onItemClick(item: Rutas)
        fun onCheckBoxClickVisita(codigo: String, check: Int, estado: String) // Cuando se interactúa con el CheckBox

        fun onCheckBoxClickCobro(codigo: String, check: Int, estado: String)

        fun onCheckBoxClickVenta(codigo: String, check: Int, estado: String)
        fun onCheckBoxClickGT(codigo: String, check: Int, estado: String)

        fun observacion(codigo: String, cliente: String, observacion: String)
    }

    fun clearItems() {
        if (datos.isEmpty()) return

        datos.clear()
        itemStateArray.clear()
        notifyDataSetChanged()
    }

    data class Rutas(
        val codigo: String,
        val sec: String,
        val cliente: String,
        val Saldo: String,
        val Ciudad: String,
        val Cupo: String,
        val estado: String,
        var isSelectedVisita: Boolean = false,
        var isSelectedVenta: Boolean = false,
        var isSelectedCobro: Boolean = false,
        var isSelectedTelefono: Boolean = false,
        var distancia: String,
        var observacion: String
    )
}
