package com.example.Consultaitems.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import com.example.Consultaitems.ui.adapters.AdaptadorPrecios
import com.example.Consultaitems.ui.adapters.PreciosyStock
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

class frmPreciosyStock : Fragment(),
    AdaptadorPrecios.OnItemClickListener,
    AdaptadorPrecios.OnImageClickListener {

    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var adaptadorPrecios: AdaptadorPrecios

    private lateinit var btnBusquedaP: Button
    private lateinit var RefView: EditText
    private lateinit var ProductView: EditText
    private lateinit var MarcaView: EditText
    private lateinit var FamiliaView: EditText
    private lateinit var DescrpView: EditText
    private lateinit var MarcaSelect: EditText
    private lateinit var DescrpSelect: EditText
    private lateinit var ProductSelect: EditText

    val Listreferencia = mutableListOf<PreciosyStock>()

    lateinit var recyclerViewPS: RecyclerView
    lateinit var dbHelper: SqLiteOpenHelper
    lateinit var solicitudSoap: SolicitudSoap

    private lateinit var downloadButton: Button
    private lateinit var imageView: ImageView
    private lateinit var photoView: PhotoView
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    private val imageCache = ImageCache()
    private val matrix = Matrix()
    private var scaleFactor = 1.0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = -1

    private val currentMatrix = Matrix()
    private val matrixValues = FloatArray(9)
    private val minScale = 1.0f
    private val maxScale = 3.0f
    private val last = PointF()
    private val start = PointF()

    private var gv_tipo: String = ""
    private var bo_codigo: String = ""
    private val ep_codigo: Int = frmLogin.CadenaHolder.ep_codigo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.frmpreciosystock, container, false)

        btnBusquedaP = view.findViewById(R.id.btnBusquedaPS)
        RefView = view.findViewById(R.id.txtRefView)
        ProductView = view.findViewById(R.id.txtProductView)
        MarcaView = view.findViewById(R.id.txtMarcaView)
        FamiliaView = view.findViewById(R.id.txtFamiliaView)
        DescrpView = view.findViewById(R.id.txtDescrpView)
        recyclerViewPS = view.findViewById(R.id.recyclerViewPS)

        adaptadorPrecios = AdaptadorPrecios(Listreferencia, this, this)
        llenarControles = ClsLLenarControles(requireContext())

        MarcaSelect = view.findViewById(R.id.txtMarcaSelect)
        DescrpSelect = view.findViewById(R.id.txtDescripcionSelect)
        ProductSelect = view.findViewById(R.id.txtProductSelect)

        dbHelper = SqLiteOpenHelper(requireContext())

        val tipos = llenarControles.fnObtenerTipoVendedor()
        if (tipos.isNotEmpty()) {
            gv_tipo = tipos[0].codigo
            bo_codigo = tipos[0].bodega
        }

        btnBusquedaP.setOnClickListener {
            val layoutManager = LinearLayoutManager(requireContext())

            adaptadorPrecios.clearItems()

            val referencia = RefView.text.toString().takeIf { it.isNotBlank() }
            val marca = MarcaView.text.toString().takeIf { it.isNotBlank() }
            val tipoProducto = ProductView.text.toString().takeIf { it.isNotBlank() }
            val familia = FamiliaView.text.toString().takeIf { it.isNotBlank() }
            val descripcion = DescrpView.text.toString().takeIf { it.isNotBlank() }

            val searchCriteria = SearchCriteria(
                referencia = referencia,
                marca = marca,
                tipoProducto = tipoProducto,
                familia = familia,
                descripcion = descripcion
            )

            if (gv_tipo == "1") {
                val resultados = llenarControles.fnPreciosYStock(searchCriteria)
                for (dato in resultados) {
                    Listreferencia.add(dato)
                }

                recyclerViewPS.layoutManager = layoutManager
                recyclerViewPS.adapter = adaptadorPrecios

                funHideSoftKeyboard()
            } else {
                solicitudSoap = SolicitudSoap(requireContext())
                val progressDialog = showProgressDialog()
                MiAsyncTask(progressDialog).execute()
            }

            MarcaSelect.setText("")
            DescrpSelect.setText("")
            ProductSelect.setText("")
        }

        return view
    }

    override fun onItemClick(item: PreciosyStock) {
        MarcaSelect.setText(item.marca)
        DescrpSelect.setText(item.descripcion)
        ProductSelect.setText(item.titulo)
    }

    override fun onFirstImageClick(referencia: String) {
        fnPreciosStock(referencia)
    }

    override fun onSecondImageClick(codigo: String) {
        fnShowImageDialog(codigo)
    }

    private fun showProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando Datos...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    private fun fnShowImageDialog(codigo: String) {
        val imageDialogView = layoutInflater.inflate(R.layout.imagen, null)
        photoView = imageDialogView.findViewById(R.id.imageViewDialog)

        disableSSLVerification()

        val imageUrl = "https://app.cotzul.com/sitenet/digital/9/$codigo.png"
        val imageCache = ImageCache()

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
            .setView(imageDialogView)
            .setPositiveButton("Cerrar") { dialog, _ ->
                dialog.dismiss()
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
                        resolver.openOutputStream(uri).use { out ->
                            if (out != null) {
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
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

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            val scaleFactor = scaleGestureDetector.scaleFactor
            val newScale = scaleFactor * currentMatrix.scaleX()

            if (newScale in minScale..maxScale) {
                matrix.postScale(
                    scaleFactor,
                    scaleFactor,
                    scaleGestureDetector.focusX,
                    scaleGestureDetector.focusY
                )

                currentMatrix.set(matrix)
                imageView.imageMatrix = matrix
            }

            return true
        }
    }

    private fun Matrix.scaleX(): Float {
        getValues(matrixValues)
        return matrixValues[Matrix.MSCALE_X]
    }

    private fun Matrix.scaleY(): Float {
        getValues(matrixValues)
        return matrixValues[Matrix.MSCALE_Y]
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

    private fun funHideSoftKeyboard() {
        Log.d("hideSoftKeyboard", "Ocultando el teclado")

        val imm = requireActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val currentFocus = requireActivity().currentFocus

        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
            Log.d("hideSoftKeyboard", "Teclado ocultado")
        } else {
            Log.d("hideSoftKeyboard", "No hay vista enfocada para ocultar el teclado")
        }

        forceRedrawWindow(requireActivity())
    }

    fun forceRedrawWindow(activity: Activity) {
        val contentView = activity.findViewById<View>(android.R.id.content)
        contentView.requestLayout()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    fun fnPreciosStock(referencia: String) {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.frmprecios, null)

        if (referencia.isEmpty()) {
            showToast("No se han seleccionado elementos para agregar")
            return
        }

        referencia.firstOrNull()?.let {
            if (gv_tipo == "1") {
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
        }

        AlertDialog.Builder(requireContext())
            .setTitle(referencia)
            .setView(view)
            .setCancelable(true)
            .show()
    }

    private inner class MiAsyncTask(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase
        private var datosInsertados: String = ""

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String {
            database = dbHelper.writableDatabase
            database.execSQL("DELETE FROM ve_ws_itemTmp")

            val referencia = RefView.text.toString()
            val marca = MarcaView.text.toString()
            val descripcion = DescrpView.text.toString()
            val tipo = ProductView.text.toString()
            val vendedor = ep_codigo

            val cadena = "'$referencia','$marca','$descripcion','$tipo', $vendedor"

            return try {
                solicitudSoap.initializeVariables(
                    getString(R.string.str_ListadoPrecios).toInt(),
                    cadena
                )

                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }

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
        val layoutManager = LinearLayoutManager(requireContext())

        val resultados = llenarControles.fnBuscaPreciosyStockEnlinea(referencia, bo_codigo)
        for (dato in resultados) {
            Listreferencia.add(dato)
        }

        recyclerViewPS.layoutManager = layoutManager
        recyclerViewPS.adapter = adaptadorPrecios

        funHideSoftKeyboard()
    }
}
