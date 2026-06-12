package com.example.Consultaitems.ui.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.github.chrisbanes.photoview.PhotoView
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import java.net.HttpURLConnection
import java.net.URL

class AdaptadorItemsScan(
    private val datos: MutableList<ItemScan>,
    private val onDelete: (item: ItemScan, position: Int) -> Unit,
    private val onClick: ((item: ItemScan, position: Int) -> Unit)? = null
) : RecyclerView.Adapter<AdaptadorItemsScan.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.frm_item_scan_detalle, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        holder.txtCantidad.text = "Cantidad: ${item.cantidad}"
        holder.txtReferencia.text = item.referencia
        holder.txtDescripcion.text = item.descripcion
        holder.txtSec.text = (position + 1).toString()

        val url = "https://app.cotzul.com/sitenet/digital/9/${item.codigo}.png"
        DownloadImageTask(holder.itemImage).execute(url)

        holder.btnEliminar.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onDelete(datos[pos], pos)
            }
        }

        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onClick?.invoke(datos[pos], pos)
            }
        }

        holder.itemImage.setOnClickListener {
            fnShowImageDialog(holder.itemImage, item.codigo, item.referencia)
        }
    }

    override fun getItemCount(): Int = datos.size

    fun setItems(nuevos: List<ItemScan>) {
        datos.clear()
        datos.addAll(nuevos)
        notifyDataSetChanged()
    }

    fun addItem(item: ItemScan) {
        datos.add(item)
        notifyItemInserted(datos.size - 1)
    }

    fun removeAt(position: Int) {
        if (position in datos.indices) {
            datos.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, datos.size - position)
        }
    }

    fun clearItems() {
        datos.clear()
        notifyDataSetChanged()
    }

    private fun fnShowImageDialog(imageView: ImageView, itCodigo: String, referencia: String) {
        val ctx = imageView.context
        val imageDialogView = LayoutInflater.from(ctx).inflate(R.layout.imagen, null)
        val photoView = imageDialogView.findViewById<PhotoView>(R.id.imageViewDialog)
        val drawable = imageView.drawable ?: return

        photoView.setImageBitmap(drawable.toBitmap())

        AlertDialog.Builder(ctx)
            .setTitle("$itCodigo - $referencia")
            .setView(imageDialogView)
            .setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtCantidad: TextView = view.findViewById(R.id.txtCantidad)
        val txtReferencia: TextView = view.findViewById(R.id.txtReferencia)
        val txtDescripcion: TextView = view.findViewById(R.id.txtDescripcion)
        val txtSec: TextView = view.findViewById(R.id.txtSec)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
        val itemImage: ImageView = view.findViewById(R.id.item_image)
    }
}

internal class DownloadImageTask(
    private val imageView: ImageView
) : AsyncTask<String, Void, Bitmap?>() {

    override fun doInBackground(vararg urls: String?): Bitmap? {
        val urlDisplay = urls.firstOrNull().orEmpty()
        return try {
            val connection = URL(urlDisplay).openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            connection.inputStream.use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onPostExecute(result: Bitmap?) {
        if (result != null) {
            imageView.setImageBitmap(result)
        } else {
            imageView.setImageResource(R.drawable.no_disponible)
        }
    }
}

data class ItemScan(
    val codigo: String,
    val referencia: String,
    val descripcion: String,
    val cantidad: String
)
