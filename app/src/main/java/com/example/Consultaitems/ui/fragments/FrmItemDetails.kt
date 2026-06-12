package defpackage

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.internal.view.SupportMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.adapters.AdaptadorItemsQr
import com.example.Consultaitems.ui.adapters.ItemDetalle
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.parser.XmlParserItemTemporal
import com.github.chrisbanes.photoview.PhotoView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

class FrmItemDetails : Fragment() {

    private lateinit var adapter: AdaptadorItemsQr
    lateinit var dbHelper: SqLiteOpenHelper
    lateinit var solicitudSoap: SolicitudSoap

    private lateinit var itemImage: ImageView
    private lateinit var recyclerDetalleItems: RecyclerView
    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var photoView: PhotoView

    lateinit var itemReferencia: TextView
    lateinit var itemDescription: TextView
    lateinit var itemStock: TextView
    lateinit var itemPriceSub: TextView
    lateinit var itemContado: TextView
    lateinit var itemCredito: TextView
    lateinit var itemPublico: TextView
    lateinit var itemPublicoIva: TextView
    lateinit var itemPeso: TextView
    lateinit var item_codigo: TextView
    lateinit var item_lsub: TextView
    lateinit var item_lcont: TextView
    lateinit var item_lcred: TextView
    lateinit var item_lpub: TextView

    var it_codigo: String = ""
    private val listaItems: MutableList<ItemDetalle> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_item_details, container, false)

        llenarControles = ClsLLenarControles(requireContext())
        dbHelper = SqLiteOpenHelper(requireContext())
        solicitudSoap = SolicitudSoap(requireContext())

        itemImage = view.findViewById(R.id.item_image)
        itemReferencia = view.findViewById(R.id.item_referencia)
        itemDescription = view.findViewById(R.id.item_description)
        itemStock = view.findViewById(R.id.item_stock)
        itemPriceSub = view.findViewById(R.id.item_price_sub)
        itemContado = view.findViewById(R.id.item_contado)
        itemCredito = view.findViewById(R.id.item_credito)
        itemPublico = view.findViewById(R.id.item_publico)
        itemPublicoIva = view.findViewById(R.id.item_publico_iva)
        itemPeso = view.findViewById(R.id.item_peso)
        item_codigo = view.findViewById(R.id.item_codigo)
        item_lsub = view.findViewById(R.id.item_lsub)
        item_lcont = view.findViewById(R.id.item_lcont)
        item_lcred = view.findViewById(R.id.item_lcred)
        item_lpub = view.findViewById(R.id.item_lpub)
        recyclerDetalleItems = view.findViewById(R.id.recyclerDetalleItems)

        adapter = AdaptadorItemsQr(listaItems)

        it_codigo = arguments?.getString("qrCode").orEmpty()

        if (isNetworkAvailable(requireContext())) {
            val progressDialog = showProgressDialog()
            MiAsyncTask(progressDialog).execute()
        } else {
            fnDetallesItem()
        }

        val backButton: Button = view.findViewById(R.id.btn_back_to_scan)
        backButton.setOnClickListener {
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
            } else {
                parentFragmentManager.popBackStack()
            }
        }

        itemImage.setOnClickListener { fnShowImageDialog() }

        recyclerDetalleItems.layoutManager = LinearLayoutManager(requireContext())
        recyclerDetalleItems.adapter = adapter
        recyclerDetalleItems.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        return view
    }

    fun calculateAndFormat(value: Double, costProm: Double): String {
        if (costProm == 0.0) return "0.00"
        return String.format(Locale.US, "%.2f", value / costProm)
    }

    private class DownloadImageTask(private val imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {
        override fun doInBackground(vararg urls: String): Bitmap? {
            val urlDisplay = urls.firstOrNull() ?: return null
            return try {
                val connection = URL(urlDisplay).openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
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

    fun disableSSLVerification() {
        try {
            val trustAllCerts = arrayOf<X509TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
                    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                }
            )
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetworkInfo?.isConnected == true
    }

    private fun showProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando Datos...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    private inner class MiAsyncTask(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void?): String {
            database = dbHelper.writableDatabase
            database.execSQL("DELETE FROM ve_ws_itemTmp")
            database.execSQL("DELETE FROM iv_ws_itemComboCabTmp")

            val cadena = "0,3,'$it_codigo'"

            return try {
                solicitudSoap.initializeVariables(getString(R.string.str_usuarios).toInt(), cadena)
                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.use { input ->
                    BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { it.readText() }
                }

                if (result.isNullOrBlank()) {
                    ""
                } else {
                    XmlParserItemTemporal.parseToTable(result, database, requireContext())
                }
            } catch (e: Exception) {
                Log.e("SolicitudSOAP", "Error al procesar la solicitud: ${e.message}", e)
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                ""
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            fnDetallesItem()
        }
    }

    fun fnDetallesItem() {
        val item = llenarControles.fnConsultarItemXqr(it_codigo).firstOrNull()

        if (item != null) {
            disableSSLVerification()

            if (isNetworkAvailable(requireContext())) {
                DownloadImageTask(itemImage).execute(item.imageUrl)
            } else {
                itemImage.setImageResource(R.drawable.no_disponible)
            }

            it_codigo = item.codigo
            item_codigo.text = item.codigo
            itemReferencia.text = item.referencia
            itemDescription.text = item.description
            itemStock.text = item.stock.toString()
            itemPriceSub.text = item.priceSub.toString()
            itemContado.text = item.priceContado.toString()
            itemCredito.text = item.priceCredito.toString()
            itemPublico.text = item.pricePublico.toString()
            itemPublicoIva.text = item.pricePublicoIva.toString()
            itemPeso.text = item.peso.toString()

            val costoPromedio = item.costProm.toDoubleOrNull() ?: 0.0
            item_lsub.text = calculateAndFormat(item.priceSub, costoPromedio)
            item_lcont.text = calculateAndFormat(item.priceContado, costoPromedio)
            item_lcred.text = calculateAndFormat(item.priceCredito, costoPromedio)
            item_lpub.text = calculateAndFormat(item.pricePublico, costoPromedio)

            if (item.oferta == 1) {
                itemPriceSub.setBackgroundColor(Color.RED)
                itemPriceSub.setTextColor(Color.WHITE)
            }
        } else {
            item_codigo.text = it_codigo
        }

        listaItems.clear()
        adapter.clearItems()
        listaItems.addAll(llenarControles.fnItemComboCabTmp())
        adapter.notifyDataSetChanged()
    }

    private fun fnShowImageDialog() {
        val imageDialogView = layoutInflater.inflate(R.layout.imagen, null)
        photoView = imageDialogView.findViewById(R.id.imageViewDialog)

        val drawable = itemImage.drawable
        if (drawable == null || drawable !is BitmapDrawable) {
            showToast("No hay imagen cargada")
            return
        }

        val bitmap = drawable.bitmap
        photoView.setImageBitmap(bitmap)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(imageDialogView)
            .setPositiveButton("Cerrar") { dialogInterface, _ -> dialogInterface.dismiss() }
            .setNegativeButton("Descargar", null)
            .create()

        dialog.setOnShowListener {
            val btn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            btn.setOnClickListener {
                saveImageFromImageView(it_codigo)
            }
        }

        dialog.show()
    }

    private fun saveImageFromImageView(codigo: String) {
        Thread {
            try {
                val drawable = photoView.drawable
                if (drawable !is BitmapDrawable) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "No hay imagen para guardar", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val bitmap = drawable.bitmap
                val fecha = SimpleDateFormat("_ddMMyyyy_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "$codigo$fecha.png"

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Cotzul")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = requireActivity().contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Imagen guardada", Toast.LENGTH_LONG).show()
                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "No se pudo guardar la imagen", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
