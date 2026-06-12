package com.example.Consultaitems.ui.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import java.text.NumberFormat
import java.util.Locale


// Clase DataModel para representar los datos de cada factura
data class DataModel(
    val sri: String,
    val descripcion: String,
    val fechaFactura: String,
    val valorTotalFactura: String,
    var isChecked: Boolean = false  // Indica si la factura está seleccionada
)

// Adaptador personalizado para el ListView
 class FacturaListAdapter(
    private val context: Context,
    private val data: List<DataModel>,
    private val txtTotal: TextView,
    private val txtPorcentaje: TextView,
    private val txtPorcentaje2: TextView
) : RecyclerView.Adapter<FacturaListAdapter.FacturaViewHolder>() {


    init {
        // Agregar un TextWatcher al txtPorcentaje para llamar a actualizarTotal() cada vez que cambie el texto
        txtPorcentaje.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No es necesario hacer nada antes de que cambie el texto
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No es necesario hacer nada mientras cambia el texto
            }

            override fun afterTextChanged(s: Editable?) {
                actualizarTotal() // Llamar a actualizarTotal() después de que el texto haya cambiado en txtPorcentaje
            }
        })


    // Agregar un TextWatcher a txtPorcentaje2 para llamar a actualizarTotal() cada vez que cambie el texto
    txtPorcentaje2.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // No es necesario hacer nada antes de que cambie el texto
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // No es necesario hacer nada mientras cambie el texto
        }

        override fun afterTextChanged(s: Editable?) {
            actualizarTotal() // Llamar a actualizarTotal() después de que el texto haya cambiado en txtPorcentaje2
        }
    })
}


    inner class FacturaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBoxFactura: CheckBox = itemView.findViewById(R.id.checkBox)
        val textViewInfoFactura: TextView = itemView.findViewById(R.id.textViewInfoFactura)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.frmfacturas, parent, false)
        return FacturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacturaViewHolder, position: Int) {
        val factura = data[position]

        holder.checkBoxFactura.isChecked = factura.isChecked
        holder.checkBoxFactura.setOnCheckedChangeListener { _, isChecked ->
            factura.isChecked = isChecked
            actualizarTotal()
        }

        holder.textViewInfoFactura.text = "${factura.sri} - ${factura.descripcion} - ${factura.fechaFactura} - ${factura.valorTotalFactura}"
    }

    override fun getItemCount(): Int {
        return data.size
    }
    // Método para actualizar el total cuando se cambia el estado de un CheckBox
    fun actualizarTotal() {
        var total = 0.0
        for (factura in data) {
            if (factura.isChecked) {
                val valorFactura = factura.valorTotalFactura.replace("$", "").trim().toDoubleOrNull() ?: 0.0 // Obtener el valor de la factura como Double
                val porcentaje1 = txtPorcentaje.text.toString().toDoubleOrNull() ?: 0.0 // Obtener el porcentaje1 como Double, si está vacío se considera como 0.0
                val porcentaje2 = txtPorcentaje2.text.toString().toDoubleOrNull() ?: 0.0 // Obtener el porcentaje2 como Double, si está vacío se considera como 0.0
                val porcentaje = if (porcentaje1 != 0.0) porcentaje1 else porcentaje2 // Usar porcentaje1 si tiene un valor, de lo contrario, usar porcentaje2
                val nuevoTotal = valorFactura + valorFactura * (porcentaje / 100)
                total += nuevoTotal // Sumar al total
            }
        }
        txtTotal.text = NumberFormat.getCurrencyInstance(Locale.US).format(total) // Formatear y establecer el nuevo total en el TextView
    }


    // Método para desmarcar todos los CheckBox en el adaptador
    fun desmarcarChecks() {
        for (factura in data) {
            factura.isChecked = false
        }
        notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado
    }


}
