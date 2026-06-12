package com.example.Consultaitems.ui.adapters

import android.content.res.ColorStateList
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.ui.fragments.frmPedidoVendedor

class MiAdapterDetalle(
    val datos: MutableList<datosDet>,
    private val activity: frmPedidoVendedor,
    private val itemClickListener: OnItemClickListener,
    private val doubleClickListener: (datosDet, Int) -> Unit
) : RecyclerView.Adapter<MiAdapterDetalle.ViewHolder>() {

    private var selectedPositions = mutableSetOf<Int>()

    private var total: Double = 0.0
    private var descuento: Double = 0.0
    private var porDesc: Double = 0.0
    private var peso: Double = 0.0
    private var costoProm: Double = 0.0
    private var cantidad: Int = 0
    private var PesoF: Double = 0.0
    private lateinit var recyclerView: RecyclerView
    var callbacks: AdapterCallbacks? = null

    private val colorMap = mutableMapOf<String, Int>()

    private val availableColors = listOf(
        R.color.colorCombo1,
        R.color.colorCombo2,
        R.color.colorCombo3,
        R.color.colorCombo4,
        R.color.colorCombo5,
        R.color.colorCombo6
    )

    interface AdapterCallbacks {
        fun onEmptyState(isEmpty: Boolean)
    }

    interface OnItemClickListener {
        fun onItemClick(item: datosDet, position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int)
    }

    init {
        fnCalcularTotal()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        recyclerView = parent as RecyclerView
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frmdetalle, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        holder.bind(item)

        if (item.proceso == -1) {
            holder.itemView.isSelected = false
            holder.itemView.setOnClickListener(null)
            return
        }

        val isSelected = selectedPositions.contains(position)
        holder.itemView.isSelected = isSelected

        holder.itemView.setOnClickListener(
            DoubleClickListener(
                onDoubleClick = {
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION && pos < datos.size) {
                        val currentItem = datos[pos]
                        if (currentItem.proceso != -1) {
                            doubleClickListener(currentItem, pos)
                        }
                    }
                },
                onSingleClick = {
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION && pos < datos.size) {
                        val currentItem = datos[pos]
                        if (currentItem.proceso != -1) {
                            selectedPositions.clear()
                            selectedPositions.add(pos)
                            notifyDataSetChanged()
                            itemClickListener.onItemClick(currentItem, pos)
                        }
                    }
                }
            )
        )
    }

    fun getItemAtPosition(position: Int): datosDet {
        return datos[position]
    }

    override fun getItemCount(): Int {
        Log.d("MiAdapterDetalle", "Total items: ${datos.size}")
        return datos.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtCodigo: TextView = itemView.findViewById(R.id.txtCodigo)
        private val txtReferencia: TextView = itemView.findViewById(R.id.txtReferenciaDet)
        private val txtCant: TextView = itemView.findViewById(R.id.txtCant)
        private val txtPrec: TextView = itemView.findViewById(R.id.txtPrec)
        private val txtsub: TextView = itemView.findViewById(R.id.txtsub)
        private val eliminarImageView: ImageView = itemView.findViewById(R.id.eliminarImageView)
        private val txtDescItem: TextView = itemView.findViewById(R.id.txtDescItem)
        private val txtConDesc: TextView = itemView.findViewById(R.id.txtConDesc)
        private val txtLoteD: TextView = itemView.findViewById(R.id.txtLoteD)
        private val txtNum: TextView = itemView.findViewById(R.id.txtNum)

        fun bind(item: datosDet) {
            if (item.proceso == -1) {
                itemView.visibility = View.GONE
                itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                return
            }

            itemView.visibility = View.VISIBLE
            itemView.layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            txtCodigo.text = item.Codigo

            txtReferencia.text = if (item.Descripcion.isNotBlank()) {
                "${item.Referencia} - ${item.Descripcion}"
            } else {
                item.Referencia
            }

            txtCant.text = item.Cantidad
            txtPrec.text = item.Precio
            txtsub.text = item.Subtotal
            txtDescItem.text = item.DescItem
            txtConDesc.text = item.ConDesc
            txtLoteD.text = item.lote
            txtNum.text = (bindingAdapterPosition + 1).toString()

            when {
                item.regalo != "0" -> {
                    val textColor = ContextCompat.getColor(itemView.context, R.color.white)
                    itemView.backgroundTintList = null
                    itemView.setBackgroundResource(R.color.red)
                    aplicarColorTexto(textColor)
                }

                item.combo != "0" -> {
                    val color = colorMap.getOrPut(item.combo) {
                        availableColors[colorMap.size % availableColors.size]
                    }

                    val textColor = ContextCompat.getColor(itemView.context, R.color.android_gray)

                    itemView.setBackgroundResource(R.drawable.background)
                    itemView.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.context, color)
                    )
                    aplicarColorTexto(textColor)
                }

                else -> {
                    val textColor = ContextCompat.getColor(itemView.context, R.color.android_gray)
                    itemView.setBackgroundResource(R.drawable.background)
                    itemView.backgroundTintList = null
                    aplicarColorTexto(textColor)
                }
            }
        }

        private fun aplicarColorTexto(textColor: Int) {
            txtNum.setTextColor(textColor)
            txtCodigo.setTextColor(textColor)
            txtReferencia.setTextColor(textColor)
            txtDescItem.setTextColor(textColor)
            txtCant.setTextColor(textColor)
            txtPrec.setTextColor(textColor)
            txtsub.setTextColor(textColor)
            txtConDesc.setTextColor(textColor)
            txtLoteD.setTextColor(textColor)
        }

        init {
            eliminarImageView.setOnClickListener {
                val position = bindingAdapterPosition

                if (position != RecyclerView.NO_POSITION && position < datos.size) {
                    eliminarItem(position)
                    activity.fnCalcularTotales(fnObtenerTotal())
                    notifyDataSetChanged()
                }
            }
        }
    }

    fun clearItems() {
        datos.clear()
        selectedPositions.clear()
        notifyDataSetChanged()
        checkEmptyState()
    }

    fun fnCalcularTotal() {
        var sumatoria = 0.0

        total = 0.0
        descuento = 0.0
        porDesc = 0.0
        peso = 0.0
        costoProm = 0.0
        cantidad = 0
        PesoF = 0.0

        for (item in datos) {
            if (item.proceso != -1) {
                val subtotal = item.Subtotal.toDoubleOrNull() ?: 0.0
                val conDesc = item.ConDesc.toDoubleOrNull() ?: 0.0
                val descItem = item.DescItem.toDoubleOrNull() ?: 0.0
                val unidad = item.unidadCE.toDoubleOrNull() ?: 0.0
                val cant = item.Cantidad.toIntOrNull() ?: 0
                val costo = item.costProm.toDoubleOrNull() ?: 0.0

                descuento += subtotal - conDesc
                total += subtotal
                sumatoria += descItem
                porDesc = descItem
                peso += unidad * cant
                costoProm += costo * cant
                cantidad += cant
            }
        }
    }

    fun fnObtenerTotal(): Double {
        fnCalcularTotal()
        return total
    }

    fun fnObtenerDescuento(): Double {
        fnCalcularTotal()
        return descuento
    }

    fun fnObtenerPorctDesc(): Double {
        fnCalcularTotal()
        return porDesc
    }

    fun fnObtenerPeso(): Double {
        fnCalcularTotal()
        return peso
    }

    fun fnObtenerCostoProm(): Double {
        fnCalcularTotal()
        return costoProm
    }

    fun eliminarItem(position: Int) {
        if (position != RecyclerView.NO_POSITION && position < datos.size) {
            val itemCombo = datos[position].combo

            if (itemCombo != "0") {
                val delCombo = datos.filter {
                    it.combo == itemCombo
                }

                val (aActualizar, aEliminar) = delCombo.partition {
                    it.sugerencia == 1
                }

                if (aEliminar.isNotEmpty()) {
                    datos.removeAll(aEliminar)
                }

                aActualizar.forEach {
                    it.combo = "0"
                    it.sugerencia = 0
                }

                notifyDataSetChanged()
                activity.fnGuardadoautomatico()
            } else {
                datos.removeAt(position)
                notifyItemRemoved(position)
            }

            selectedPositions.remove(position)
            fnCalcularTotal()
            checkEmptyState()
        }
    }

    fun updateData(newData: List<datosDet>) {
        Log.d("RecyclerViewAdapter", "Updating data with ${newData.size} items.")
        datos.clear()
        datos.addAll(newData)
        selectedPositions.clear()
        notifyDataSetChanged()
        checkEmptyState()
    }

    private fun checkEmptyState() {
        val isEmpty = datos.none { it.proceso != -1 }
        callbacks?.onEmptyState(isEmpty)
        Log.d("MiAdapterDetalleEmpty", "Checking if adapter is empty: $isEmpty")
    }

    fun fnRegalo(): Int {
        for (item in datos) {
            val regalo = item.regalo.toIntOrNull() ?: 0
            if (item.proceso != -1 && regalo != 0) {
                return regalo
            }
        }
        return 0
    }

    fun fnSugerencia(): Int {
        for (item in datos) {
            if (item.proceso != -1 && item.sugerencia != 0) {
                return item.sugerencia
            }
        }
        return 0
    }

    private class DoubleClickListener(
        private val onDoubleClick: () -> Unit,
        private val onSingleClick: () -> Unit,
        private val doubleClickTimeout: Long = 250L
    ) : View.OnClickListener {

        private var lastClickTime: Long = 0L
        private var singleClickRunnable: Runnable? = null

        override fun onClick(v: View?) {
            val now = SystemClock.uptimeMillis()

            if (now - lastClickTime < doubleClickTimeout) {
                singleClickRunnable?.let { v?.removeCallbacks(it) }
                singleClickRunnable = null
                lastClickTime = 0L
                onDoubleClick()
            } else {
                lastClickTime = now

                val runnable = Runnable {
                    if (SystemClock.uptimeMillis() - lastClickTime >= doubleClickTimeout) {
                        lastClickTime = 0L
                        onSingleClick()
                    }
                }

                singleClickRunnable = runnable
                v?.postDelayed(runnable, doubleClickTimeout)
            }
        }
    }
}

data class datosDet(
    val Referencia: String,
    val Cantidad: String,
    val Precio: String,
    val Subtotal: String,
    val Codigo: String,
    val Descripcion: String,
    val unidadCE: String,
    var DescItem: String,
    var ConDesc: String,
    val costProm: String,
    var lote: String,
    var combo: String,
    val regalo: String,
    var sugerencia: Int = 0,
    var proceso: Int = 0,
    var secuencia: Int = 0
)
