package com.example.Consultaitems.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.activities.frmLogin
import com.example.Consultaitems.ui.adapters.AdaptadorPreciosVertical
import com.example.Consultaitems.ui.adapters.PreciosyStock
import com.example.Consultaitems.ui.dialogs.DetallePrecioStockDialog
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.DownloadImageTask
import com.example.Consultaitems.utils.cls.ImageCache
import com.example.Consultaitems.utils.cls.SearchCriteria
import com.example.Consultaitems.utils.parser.XmlParserItemTemp
import com.github.chrisbanes.photoview.PhotoView
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class frmPreciosyStockVertical : Fragment(),
    AdaptadorPreciosVertical.OnItemClickListener,
    AdaptadorPreciosVertical.OnImageClickListener {

    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var adaptadorPrecios: AdaptadorPreciosVertical

    private lateinit var btnBusquedaP: Button
    private lateinit var RefView: EditText
    private lateinit var ProductView: EditText
    private lateinit var MarcaView: EditText
    private lateinit var FamiliaView: EditText
    private lateinit var DescrpView: EditText

    private lateinit var MarcaSelect: EditText
    private lateinit var DescrpSelect: EditText
    private lateinit var ProductSelect: EditText

    private lateinit var recyclerViewPS: RecyclerView
    private lateinit var dbHelper: SqLiteOpenHelper
    private lateinit var solicitudSoap: SolicitudSoap

    private lateinit var downloadButton: Button
    private lateinit var photoView: PhotoView

    private val listaReferencia = mutableListOf<PreciosyStock>()
    private val imageCache = ImageCache()

    private var gvTipo: String = ""
    private var boCodigo: String = ""
    private val epCodigo: Int = frmLogin.CadenaHolder.ep_codigo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.frmpreciosystock_vertical, container, false)

        inicializarVistas(view)
        inicializarRecycler()
        inicializarDatosVendedor()
        configurarBotonBuscar()

        return view
    }

    private fun inicializarVistas(view: View) {
        btnBusquedaP = view.findViewById(R.id.btnBusquedaPS)

        RefView = view.findViewById(R.id.txtRefView)
        ProductView = view.findViewById(R.id.txtProductView)
        MarcaView = view.findViewById(R.id.txtMarcaView)
        FamiliaView = view.findViewById(R.id.txtFamiliaView)
        DescrpView = view.findViewById(R.id.txtDescrpView)

        MarcaSelect = view.findViewById(R.id.txtMarcaSelect)
        DescrpSelect = view.findViewById(R.id.txtDescripcionSelect)
        ProductSelect = view.findViewById(R.id.txtProductSelect)

        recyclerViewPS = view.findViewById(R.id.recyclerViewPS)

        llenarControles = ClsLLenarControles(requireContext())
        dbHelper = SqLiteOpenHelper(requireContext())
    }

    private fun inicializarRecycler() {
        adaptadorPrecios = AdaptadorPreciosVertical(
            datos = listaReferencia,
            itemClickListener = this,
            imageClickListener = this
        )

        recyclerViewPS.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewPS.adapter = adaptadorPrecios
    }

    private fun inicializarDatosVendedor() {
        val tipos = llenarControles.fnObtenerTipoVendedor()

        if (tipos.isNotEmpty()) {
            gvTipo = tipos[0].codigo
            boCodigo = tipos[0].bodega
        }
    }

    private fun configurarBotonBuscar() {
        btnBusquedaP.setOnClickListener {
            buscarPreciosYStock()
        }
    }

    private fun buscarPreciosYStock() {
        adaptadorPrecios.clearItems()
        limpiarProductoSeleccionado()

        val referencia = RefView.text.toString().trim().takeIf { it.isNotBlank() }
        val marca = MarcaView.text.toString().trim().takeIf { it.isNotBlank() }
        val tipoProducto = ProductView.text.toString().trim().takeIf { it.isNotBlank() }
        val familia = FamiliaView.text.toString().trim().takeIf { it.isNotBlank() }
        val descripcion = DescrpView.text.toString().trim().takeIf { it.isNotBlank() }

        val searchCriteria = SearchCriteria(
            referencia = referencia,
            marca = marca,
            tipoProducto = tipoProducto,
            familia = familia,
            descripcion = descripcion
        )

        if (gvTipo == "1") {
            val resultados = llenarControles.fnPreciosYStock(searchCriteria)
            adaptadorPrecios.setItems(resultados)
            funHideSoftKeyboard()
        } else {
            solicitudSoap = SolicitudSoap(requireContext())
            val progressDialog = showProgressDialog()
            MiAsyncTask(progressDialog).execute()
        }
    }

    private fun limpiarProductoSeleccionado() {
        MarcaSelect.setText("")
        DescrpSelect.setText("")
        ProductSelect.setText("")
    }

    override fun onItemClick(item: PreciosyStock) {
        MarcaSelect.setText(item.marca)
        DescrpSelect.setText(item.descripcion)
        ProductSelect.setText(item.titulo)

        DetallePrecioStockDialog(
            context = requireContext(),
            onVerStock = { referencia ->
                fnPreciosStock(referencia)
            },
            onVerImagen = { codigo ->
                fnShowImageDialog(codigo)
            }
        ).mostrar(item)
    }

    override fun onFirstImageClick(referencia: String) {
        fnPreciosStock(referencia)
    }

    override fun onSecondImageClick(codigo: String) {
        fnShowImageDialog(codigo)
    }

    private fun showProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando datos...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    private fun fnShowImageDialog(codigo: String) {
        if (codigo.isBlank()) {
            showToast("No hay código para consultar la imagen")
            return
        }

        val imageDialogView = layoutInflater.inflate(R.layout.imagen, null)
        photoView = imageDialogView.findViewById(R.id.imageViewDialog)

        disableSSLVerification()

        val imageUrl = "https://app.cotzul.com/sitenet/digital/9/$codigo.png"

        val downloadTask = object : DownloadImageTask(
            photoView,
            imageCache,
            R.mipmap.ic_no_disponible_foreground
        ) {
            override fun onPostExecute(result: Bitmap?) {
                super.onPostExecute(result)

                if (::downloadButton.isInitialized) {
                    downloadButton.isEnabled = result != null
                }
            }
        }

        downloadTask.execute(imageUrl)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Imagen del producto")
            .setView(imageDialogView)
            .setPositiveButton("Cerrar") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setNegativeButton("Descargar", null)
            .create()

        dialog.setOnShowListener {
            downloadButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            downloadButton.isEnabled = false

            downloadButton.setOnClickListener {
                saveImageFromImageView(codigo)
            }
        }

        dialog.show()
    }

    private fun saveImageFromImageView(codigo: String) {
        Thread {
            try {
                val drawable = photoView.drawable

                if (drawable != null && drawable is BitmapDrawable) {
                    val bitmap = drawable.bitmap

                    val fecha = SimpleDateFormat(
                        "_ddMMyyyy_HHmmss",
                        Locale.getDefault()
                    ).format(Date())

                    val fileName = "$codigo$fecha.png"

                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Cotzul")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }

                    val resolver = requireActivity().contentResolver

                    val uri = resolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )

                    if (uri != null) {
                        resolver.openOutputStream(uri).use { outputStream ->
                            if (outputStream != null) {
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            }
                        }

                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(uri, values, null, null)

                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Imagen guardada",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "No se pudo guardar la imagen",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "No hay imagen para guardar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()

                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    fun disableSSLVerification() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fnPreciosStock(referencia: String) {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.frmprecios, null)

        if (referencia.isBlank()) {
            showToast("No se ha seleccionado una referencia")
            return
        }

        if (gvTipo == "1") {
            val stock = llenarControles.fnObtenerStock(referencia, "")

            stock.drop(1).forEachIndexed { index, valor ->
                val textViewId = resources.getIdentifier(
                    "valueItem${index + 1}",
                    "id",
                    requireActivity().packageName
                )

                val textView = view.findViewById<TextView>(textViewId)
                textView?.text = valor
            }
        } else {
            val stock = llenarControles.fnObtenerStockEnLinea(referencia, "")

            stock.drop(1).forEachIndexed { index, valor ->
                val textViewId = resources.getIdentifier(
                    "valueItem${index + 1}",
                    "id",
                    requireActivity().packageName
                )

                val textView = view.findViewById<TextView>(textViewId)
                textView?.text = valor
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(referencia)
            .setView(view)
            .setCancelable(true)
            .show()
    }

    private fun funHideSoftKeyboard() {
        Log.d("hideSoftKeyboard", "Ocultando teclado")

        val imm = requireActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val currentFocus = requireActivity().currentFocus

        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }

        forceRedrawWindow(requireActivity())
    }

    private fun forceRedrawWindow(activity: Activity) {
        val contentView = activity.findViewById<View>(android.R.id.content)
        contentView.requestLayout()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    private inner class MiAsyncTask(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase

        override fun doInBackground(vararg voids: Void): String {
            database = dbHelper.writableDatabase
            database.execSQL("DELETE FROM ve_ws_itemTmp")

            val referencia = RefView.text.toString().trim()
            val marca = MarcaView.text.toString().trim()
            val descripcion = DescrpView.text.toString().trim()
            val tipo = ProductView.text.toString().trim()
            val vendedor = epCodigo

            val cadena = "'$referencia','$marca','$descripcion','$tipo', $vendedor"

            return try {
                solicitudSoap.initializeVariables(
                    getString(R.string.str_ListadoPrecios).toInt(),
                    cadena
                )

                val inputStream = solicitudSoap.realizarSolicitudSoap()

                val result = inputStream
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }

                if (result.isNullOrBlank()) {
                    ""
                } else {
                    XmlParserItemTemp.parseItemTemp(
                        result,
                        database,
                        requireContext()
                    )
                }
            } catch (e: Exception) {
                Log.e("SolicitudSOAP", "Error al procesar la solicitud: ${e.message}", e)

                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                ""
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            fnBuscarReferencia()
        }
    }

    private fun fnBuscarReferencia() {
        val referencia = RefView.text.toString().trim()

        val resultados = llenarControles.fnBuscaPreciosyStockEnlinea(
            referencia,
            boCodigo
        )

        adaptadorPrecios.setItems(resultados)
        funHideSoftKeyboard()
    }
}