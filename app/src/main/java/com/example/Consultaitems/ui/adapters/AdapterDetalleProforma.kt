package com.example.Consultaitems.ui.adapters

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
import com.example.Consultaitems.ui.fragments.frmProformaA
import java.util.Locale

class MiAdapterDetalleProforma(
    val datos: MutableList<datosDet>,
    private val activity: frmProformaA,
    private val itemClickListener: OnItemClickListener,
    private val doubleClickListener: (datosDet, Int) -> Unit
) : RecyclerView.Adapter<MiAdapterDetalleProforma.ViewHolder>() {

    private var selectedPositions = mutableSetOf<Int>()

    private var total: Double = 0.0
    private var descuento: Double = 0.0
    private var porDesc: Double = 0.0
    private var peso: Double = 0.0
    private var costoProm: Double = 0.0
    private var cantidad: Int = 0
    private var PesoF: Double = 0.0
    private var secuencia: Int = 0
    private var habilitado: Boolean = true

    private lateinit var recyclerView: RecyclerView
    var callbacks: AdapterCallbacks? = null

    private val colorMap = mutableMapOf<String, Int>()

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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        recyclerView = parent as RecyclerView

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_detalle_proforma, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
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

    override fun getItemCount(): Int {
        Log.d("MiAdapterDetalle", "Total items: ${datos.size}")
        return datos.size
    }

    fun getItemAtPosition(position: Int): datosDet {
        return datos[position]
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val txtReferencia: TextView = itemView.findViewById(R.id.txtReferenciaDetPF)
        private val txtCant: TextView = itemView.findViewById(R.id.txtCantPF)
        private val txtPrec: TextView = itemView.findViewById(R.id.txtPrecPF)
        private val txtsub: TextView = itemView.findViewById(R.id.txtsubPF)
        private val eliminarImageView: ImageView = itemView.findViewById(R.id.eliminarImageViewPF)
        private val txtDescItem: TextView = itemView.findViewById(R.id.txtDescItemPF)
        private val txtConDesc: TextView = itemView.findViewById(R.id.txtConDescPF)
        private val txtLoteD: TextView = itemView.findViewById(R.id.txtLoteDPF)
        private val txtNum: TextView = itemView.findViewById(R.id.txtNumPF)
        private val txtPeso: TextView = itemView.findViewById(R.id.txtPesoPF)
        private val txtSubPeso: TextView = itemView.findViewById(R.id.txtSubPesoPF)

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

            txtReferencia.text = item.Referencia
            txtCant.text = fmt2(toDouble(item.Cantidad))
            txtPrec.text = fmt2(toDouble(item.Precio))
            txtsub.text = fmt2(toDouble(item.Subtotal))
            txtDescItem.text = fmt2(toDouble(item.DescItem))
            txtConDesc.text = fmt2(toDouble(item.ConDesc))
            txtLoteD.text = fmt2(toDouble(item.lote))
            txtNum.text = (bindingAdapterPosition + 1).toString()
            txtPeso.text = fmt2(toDouble(item.unidadCE))

            val subPeso = toDouble(item.unidadCE) * toDouble(item.Cantidad)
            txtSubPeso.text = fmt2(subPeso)

            when {
                item.combo != "0" -> {
                    itemView.setBackgroundResource(R.color.colorCombo1)
                    restaurarColorTextoNormal()
                }

                item.regalo != "0" -> {
                    val textColor = ContextCompat.getColor(
                        itemView.context,
                        R.color.white
                    )

                    itemView.setBackgroundResource(R.color.red)
                    aplicarColorTexto(textColor)
                }

                else -> {
                    val textColor = ContextCompat.getColor(
                        itemView.context,
                        R.color.android_gray
                    )

                    itemView.setBackgroundResource(R.drawable.background)
                    aplicarColorTexto(textColor)
                }
            }
        }

        private fun restaurarColorTextoNormal() {
            val textColor = ContextCompat.getColor(
                itemView.context,
                R.color.android_gray
            )

            aplicarColorTexto(textColor)
        }

        private fun aplicarColorTexto(textColor: Int) {
            txtNum.setTextColor(textColor)
            txtReferencia.setTextColor(textColor)
            txtDescItem.setTextColor(textColor)
            txtCant.setTextColor(textColor)
            txtPrec.setTextColor(textColor)
            txtsub.setTextColor(textColor)
            txtConDesc.setTextColor(textColor)
            txtLoteD.setTextColor(textColor)
            txtPeso.setTextColor(textColor)
            txtSubPeso.setTextColor(textColor)
        }

        init {
            eliminarImageView.setOnClickListener {
                val position = bindingAdapterPosition

                if (
                    habilitado &&
                    position != RecyclerView.NO_POSITION &&
                    position < datos.size
                ) {
                    val item = datos[position]

                    item.proceso = -1

                    selectedPositions.remove(position)
                    notifyItemChanged(position)

                    activity.fnCalcularTotales(fnObtenerTotal())
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
        total = 0.0
        descuento = 0.0
        porDesc = 0.0
        peso = 0.0
        costoProm = 0.0
        cantidad = 0
        PesoF = 0.0
        secuencia = 0

        for (item in datos) {
            secuencia = item.secuencia

            if (item.proceso != -1) {
                val subtotalItem = toDouble(item.Subtotal)
                val conDescItem = toDouble(item.ConDesc)
                val descPorc = toDouble(item.DescItem)
                val unidad = toDouble(item.unidadCE)
                val cant = item.Cantidad
                    .trim()
                    .replace(",", ".")
                    .toDoubleOrNull()
                    ?: 0.0

                val costo = toDouble(item.costProm)
                val precio = toDouble(item.Precio)
                val regalo = item.regalo.toIntOrNull() ?: 0

                if (regalo > 0) {
                    item.ConDesc = "0.00"
                } else if (descPorc > 0.0) {
                    val valorFinal = if (conDescItem > 0.0) {
                        conDescItem
                    } else {
                        subtotalItem - (subtotalItem * descPorc / 100.0)
                    }

                    item.ConDesc = String.format(Locale.US, "%.2f", valorFinal)
                } else {
                    item.ConDesc = "0.00"
                }

                item.lote = if (costo > 0.0) {
                    val loteItem = precio * (1.0 - descPorc / 100.0) / costo
                    String.format(Locale.US, "%.2f", loteItem)
                } else {
                    "0.00"
                }

                total += subtotalItem

                descuento += if (descPorc > 0.0 && regalo == 0) {
                    val valorFinal = toDouble(item.ConDesc)
                    subtotalItem - valorFinal
                } else {
                    0.0
                }

                porDesc = descPorc
                peso += unidad * cant
                costoProm += costo * cant
                cantidad += cant.toInt()
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

    fun fnObtenerSecuencia(): Int {
        fnCalcularTotal()
        return secuencia
    }

    fun eliminarItem(position: Int) {
        if (position != RecyclerView.NO_POSITION && position < datos.size) {
            val itemCombo = datos[position].combo

            if (itemCombo != "0") {
                val itemsToRemove = datos.filter { it.combo == itemCombo }
                datos.removeAll(itemsToRemove)
                notifyDataSetChanged()
            } else {
                datos.removeAt(position)
                notifyItemRemoved(position)
            }

            selectedPositions.clear()
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
        callbacks?.onEmptyState(datos.isEmpty())

        Log.d(
            "MiAdapterDetalleEmpty",
            "Checking if adapter is empty: ${datos.isEmpty()}"
        )
    }

    fun fnRegalo(): Int {
        var regalo = 0

        for (item in datos) {
            if (item.proceso != -1) {
                regalo = item.regalo.toIntOrNull() ?: 0
            }
        }

        return regalo
    }

    fun fnsetHabilitado(valor: Boolean) {
        habilitado = valor
        notifyDataSetChanged()
    }

    private fun toDouble(value: String): Double {
        return value
            .trim()
            .replace(",", ".")
            .toDoubleOrNull()
            ?: 0.0
    }

    private fun fmt2(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
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