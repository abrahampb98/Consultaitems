package com.example.Consultaitems.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R

class CategoriaAdapter(
    val categorias: MutableList<Categoria>,
    private val onCheckedChanged: (Categoria) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    private var habilitado: Boolean = true

    inner class CategoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.chkCategoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_item_categoria, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = categorias[position]

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.text = categoria.descripcion
        holder.checkBox.isChecked = categoria.seleccionada
        holder.checkBox.isEnabled = habilitado

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (habilitado) {
                categoria.seleccionada = isChecked
                onCheckedChanged(categoria)
            }
        }
    }

    override fun getItemCount(): Int = categorias.size

    fun fnLimpiarSeleccion() {
        categorias.forEach { it.seleccionada = false }
        notifyDataSetChanged()
    }

    fun fnHabilitar() {
        habilitado = true
        notifyDataSetChanged()
    }

    fun fnDeshabilitar() {
        habilitado = false
        notifyDataSetChanged()
    }
}


data class Categoria(
    val codigo: Int,
    val descripcion: String,
    var seleccionada: Boolean = false
)
