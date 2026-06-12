package com.example.Consultaitems.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView

// ClienteAdapter.kt
class ClienteAdapter(context: Context, private val todosLosClientes: List<Cliente>) :
    ArrayAdapter<Cliente>(context, android.R.layout.simple_dropdown_item_1line, todosLosClientes), Filterable {

    private var clientesFiltrados: List<Cliente> = todosLosClientes

    override fun getCount(): Int = clientesFiltrados.size

    override fun getItem(position: Int): Cliente? = clientesFiltrados[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        val cliente = getItem(position)
        (view as TextView).text = cliente?.nombre
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint == null || constraint.isEmpty()) {
                    filterResults.values = todosLosClientes
                    filterResults.count = todosLosClientes.size
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    val filteredList = todosLosClientes.filter {
                        it.nombre.lowercase().contains(filterPattern)
                    }
                    filterResults.values = filteredList
                    filterResults.count = filteredList.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                clientesFiltrados = if (results?.values == null) {
                    emptyList()
                } else {
                    results.values as List<Cliente>
                }
                notifyDataSetChanged()
            }
        }
    }
}

// Cliente.kt
data class Cliente(
    val id: String,
    val nombre: String
)

data class Transporte(
    val codigo: String,
    val nombre: String
)

data class Marca(
    val codigo: String,
    val nombre: String
)


//busqueda de transportes
class TransporteAdapter(context: Context, private val todosLosTransportes: List<Transporte>) :
    ArrayAdapter<Transporte>(context, android.R.layout.simple_dropdown_item_1line, todosLosTransportes), Filterable {

    private var transportesFiltrados: List<Transporte> = todosLosTransportes

    override fun getCount(): Int = transportesFiltrados.size

    override fun getItem(position: Int): Transporte? = transportesFiltrados[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        val transporte = getItem(position)
        (view as TextView).text = transporte?.nombre
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint == null || constraint.isEmpty()) {
                    filterResults.values = todosLosTransportes
                    filterResults.count = todosLosTransportes.size
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    val filteredList = todosLosTransportes.filter {
                        it.nombre.lowercase().contains(filterPattern)
                    }
                    filterResults.values = filteredList
                    filterResults.count = filteredList.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                transportesFiltrados = if (results?.values == null) {
                    emptyList()
                } else {
                    results.values as List<Transporte>
                }
                notifyDataSetChanged()
            }
        }
    }
}


//todas las marcas
class MarcaAdapter(context: Context, private val todasLasMarcas: List<Marca>) :
    ArrayAdapter<Marca>(context, android.R.layout.simple_dropdown_item_1line, todasLasMarcas), Filterable {

    private var marcasFiltradas: List<Marca> = todasLasMarcas

    override fun getCount(): Int = marcasFiltradas.size

    override fun getItem(position: Int): Marca? = marcasFiltradas[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        val marca = getItem(position)
        (view as TextView).text = marca?.nombre
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint == null || constraint.isEmpty()) {
                    filterResults.values = todasLasMarcas
                    filterResults.count = todasLasMarcas.size
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    val filteredList = todasLasMarcas.filter {
                        it.nombre.lowercase().contains(filterPattern)
                    }
                    filterResults.values = filteredList
                    filterResults.count = filteredList.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                marcasFiltradas = if (results?.values == null) {
                    emptyList()
                } else {
                    results.values as List<Marca>
                }
                notifyDataSetChanged()
            }
        }
    }
}
