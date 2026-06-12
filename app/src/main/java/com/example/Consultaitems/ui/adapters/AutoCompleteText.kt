package com.example.Consultaitems.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import java.util.Locale

/**
 * Adaptador recuperado desde el APK.
 *
 * Archivo original detectado por JADX: AdaptadorTxt.kt
 * Clase compilada: AutoCompleteText
 *
 * Sirve para llenar un AutoCompleteTextView usando una lista de Adaptador(codigo, descripcion).
 */
class AutoCompleteText(
    context: Context,
    private val todas: List<Adaptador>
) : ArrayAdapter<Adaptador>(context, android.R.layout.simple_dropdown_item_1line, todas), Filterable {

    private var filtros: List<Adaptador> = todas

    override fun getCount(): Int = filtros.size

    override fun getItem(position: Int): Adaptador? = filtros[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_dropdown_item_1line, parent, false)

        val item = getItem(position)
        (view as TextView).text = item?.descripcion.orEmpty()

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()

                val texto = constraint
                    ?.toString()
                    ?.lowercase(Locale.ROOT)
                    ?.trim()
                    .orEmpty()

                val listaFiltrada = if (texto.isEmpty()) {
                    todas
                } else {
                    todas.filter { item ->
                        item.descripcion.lowercase(Locale.ROOT).contains(texto)
                    }
                }

                results.values = listaFiltrada
                results.count = listaFiltrada.size
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filtros = results?.values as? List<Adaptador> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
}

data class Adaptador(
    val codigo: Int,
    val descripcion: String
)
