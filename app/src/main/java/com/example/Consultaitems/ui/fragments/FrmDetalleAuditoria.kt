package com.example.Consultaitems.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.ClsRegImagen
import com.example.Consultaitems.utils.parser.XmlConteos
import com.example.Consultaitems.utils.parser.XmlParserConteos
import com.example.Consultaitems.utils.parser.XmlParserItemTemporal
import com.example.Consultaitems.utils.parser.XmlParserItemTemporales
import com.github.chrisbanes.photoview.PhotoView
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.HorizontalAlignment
import com.itextpdf.layout.property.UnitValue
import com.itextpdf.layout.property.VerticalAlignment
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class FrmDetalleAuditoria : Fragment() {

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val CAMERA_REQUEST = 2
    }

    private lateinit var ClaseXml: XmlConteos

    var bo_codigo: Int = 0
    var it_codigo: String = ""
    var it_enlace: String = ""

    private lateinit var btnEnlaceRD: Button
    lateinit var btnEnviarEI: Button
    lateinit var dbHelper: SqLiteOpenHelper
    lateinit var solicitudSoap: SolicitudSoap

    lateinit var itemDescription: TextView
    private lateinit var itemImage: ImageView
    lateinit var itemReferencia: TextView
    lateinit var item_codigo: TextView
    lateinit var item_stockBo: TextView
    lateinit var item_stockAl: TextView
    lateinit var item_stockEt: TextView
    lateinit var item_stockPm: TextView
    lateinit var item_stockEp: TextView
    lateinit var txtCantidadEI: EditText
    private lateinit var txtObservacionEI: TextView

    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var photoUri: Uri
    private lateinit var photoView: PhotoView
    private var progressDialog: ProgressDialog? = null

    private var ep_codigo: Int = 0
    private var selectedImageViewIndex: Int = -1
    private var enlace: String = ""
    private var response: String = ""

    private val imageViews = mutableListOf<ImageView>()
    private val imageChanged = mutableListOf<Boolean>()
    private val originalBitmaps = mutableListOf<Bitmap?>()
    private val imageUris = mutableListOf<Uri?>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.frm_escanear_inventario, container, false)

        llenarControles = ClsLLenarControles(requireContext())
        dbHelper = SqLiteOpenHelper(requireContext())
        solicitudSoap = SolicitudSoap(requireContext())

        itemImage = view.findViewById(R.id.item_imageEI)
        itemReferencia = view.findViewById(R.id.item_referenciaEI)
        itemDescription = view.findViewById(R.id.item_descriptionEI)
        item_codigo = view.findViewById(R.id.item_codigoEI)
        item_stockBo = view.findViewById(R.id.item_stockBo)
        item_stockAl = view.findViewById(R.id.item_stockAl)
        item_stockEt = view.findViewById(R.id.item_stockEt)
        item_stockPm = view.findViewById(R.id.item_stockPm)
        item_stockEp = view.findViewById(R.id.item_stockEp)
        btnEnviarEI = view.findViewById(R.id.btnEnviarEI)
        txtCantidadEI = view.findViewById(R.id.txtCantidadEI)
        txtObservacionEI = view.findViewById(R.id.txtObservacionEI)
        btnEnlaceRD = view.findViewById(R.id.btnEnlaceRD)

        ep_codigo = llenarControles.fnObtenerVendedor().toIntOrNull() ?: 0

        val qrCode = arguments?.getString("qrCode").orEmpty()
        bo_codigo = arguments?.getInt("bo_codigo", 0) ?: 0
        it_codigo = qrCode.removePrefix("EAN-")

        view.findViewById<Button>(R.id.btn_back_to_scan).setOnClickListener {
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
            } else {
                parentFragmentManager.popBackStack()
            }
        }

        configurarImageViews(view)

        if (bo_codigo != 0) {
            txtCantidadEI.isEnabled = true
            txtObservacionEI.isEnabled = true

            val datos = llenarControles.fnObtenerCantidadBodega(
                bo_codigo,
                it_codigo,
                fnFecha()
            )

            txtCantidadEI.setText(datos.first)
            txtObservacionEI.text = datos.second
            fnActivarImageViews()
        } else {
            txtCantidadEI.isEnabled = false
            txtObservacionEI.isEnabled = false
            fnDesactivarImageViews()
        }

        fnDetallesItem()

        if (it_enlace.isNotBlank()) {
            btnEnlaceRD.isEnabled = true
            btnEnlaceRD.text = "si"
        }

        itemImage.setOnClickListener {
            fnShowImageDialog()
        }

        btnEnviarEI.setOnClickListener {
            fnEnviarDatos()
        }

        btnEnlaceRD.setOnClickListener {
            fnAbrirEnlace()
        }

        MiAsyncTaskStock(showProgressDialog()).execute()

        return view
    }

    private fun configurarImageViews(view: View) {
        imageViews.clear()
        imageChanged.clear()
        originalBitmaps.clear()
        imageUris.clear()

        imageViews.add(view.findViewById(R.id.imageViewPreview1))
        imageViews.add(view.findViewById(R.id.imageViewPreview2))
        imageViews.add(view.findViewById(R.id.imageViewPreview3))
        imageViews.add(view.findViewById(R.id.imageViewPreview4))
        imageViews.add(view.findViewById(R.id.imageViewPreview5))
        imageViews.add(view.findViewById(R.id.imageViewPreview6))

        repeat(imageViews.size) {
            imageChanged.add(false)
            originalBitmaps.add(null)
            imageUris.add(null)
        }

        imageViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedImageViewIndex = index
                fnOpenImagePicker()
            }
        }
    }

    fun fnFecha(formato: String = "dd/MM/yyyy"): String {
        val fechaActual = Calendar.getInstance().time
        val formatoFecha = SimpleDateFormat(formato, Locale.getDefault())
        return formatoFecha.format(fechaActual)
    }

    fun obtenerFechaActual(formato: String = "dd/MM/yyyy"): String {
        val fechaActual = Calendar.getInstance().time
        val formatoFecha = SimpleDateFormat(formato, Locale.getDefault())
        return formatoFecha.format(fechaActual)
    }

    private fun fnDesactivarImageViews() {
        imageViews.forEach { imageView ->
            imageView.isEnabled = false
        }
    }

    private fun fnActivarImageViews() {
        imageViews.forEach { imageView ->
            imageView.isEnabled = true
        }
    }

    fun fnGuardar() {
        val cantidad = txtCantidadEI.text.toString().trim()

        if (cantidad.isBlank()) {
            showToast("Ingrese la cantidad")
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("¿Deseas guardar los datos?")
            .setPositiveButton("Sí") { dialog, _ ->
                dialog.dismiss()
                fnGuardarConteo()
                fnDesactivarImageViews()
                showToast("Datos guardados correctamente")
                txtCantidadEI.isEnabled = false
                btnEnviarEI.isEnabled = true
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    fun fnGuardarConteo() {
        val fecha = obtenerFechaActual()
        val cantidad = txtCantidadEI.text.toString()
        val usuario = llenarControles.fnLLenarVendedor()
        val codigo = item_codigo.text.toString()
        val observacion = txtObservacionEI.text.toString()
        val enlaceActual = it_enlace

        if (usuario != null) {
            llenarControles.fnInsertarConteoA(
                codigo,
                2,
                bo_codigo,
                fecha,
                cantidad,
                "A",
                usuario.login,
                observacion,
                enlaceActual,
                ""
            )
        }
    }

    fun fnEnviarDatos() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("¿Desea enviar los datos y las imágenes?")
            .setPositiveButton("Sí") { dialog, _ ->
                dialog.dismiss()

                fnGuardarConteo()
                fnDesactivarImageViews()
                txtCantidadEI.isEnabled = false
                txtObservacionEI.isEnabled = false
                btnEnviarEI.isEnabled = true

                val pdfBase64 = fnCrearPdf(originalBitmaps, imageChanged, imageUris)

                if (pdfBase64 != null) {
                    fnEnviarImagenes(pdfBase64, requireContext())
                } else {
                    val progressDialog = showProgressDialog()
                    fnEnviarInforme(progressDialog)
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun showToast(message: String) {
        if (!isAdded) return
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun fnEnviarInforme(progressDialog: ProgressDialog) {
        if (!isAdded) {
            if (progressDialog.isShowing) progressDialog.dismiss()
            return
        }

        if (isNetworkAvailable(requireContext())) {
            try {
                ClaseXml = XmlConteos(requireContext())
                solicitudSoap = SolicitudSoap(requireContext())
                MiAsyncTaskEnvio(progressDialog).execute()
            } catch (e: Exception) {
                e.printStackTrace()
                if (progressDialog.isShowing) progressDialog.dismiss()
                showToast("Error al enviar los datos")
            }
        } else {
            if (progressDialog.isShowing) progressDialog.dismiss()
            showToast("Verifique su conexión a internet")
        }
    }

    private inner class MiAsyncTaskEnvio(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase

        override fun doInBackground(vararg voids: Void): String? {
            return try {
                val ctx = context?.applicationContext ?: return "ERROR"

                database = dbHelper.writableDatabase

                val cadena = ClaseXml.obtenerConteo(2)

                solicitudSoap.initializeVariables(
                    getString(R.string.str_conteo).toInt(),
                    cadena
                )

                val result = solicitudSoap.realizarSolicitudSoap()
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }

                if (result.isNullOrBlank()) {
                    ""
                } else {
                    XmlParserConteos.parseAndUpdateDocumentCode(
                        result,
                        database,
                        ctx,
                        2
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "ERROR"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            try {
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }

                showResultDialog(result)

                if (btnEnlaceRD.text.toString() == "No") {
                    fnActivarImageViews()
                }
            } finally {
                if (::database.isInitialized && database.isOpen) {
                    database.close()
                }
            }
        }

        private fun showResultDialog(recibo: String?) {
            if (!isAdded) return

            val mensaje = if (
                !recibo.isNullOrBlank() &&
                !recibo.equals("null", ignoreCase = true) &&
                recibo != "ERROR"
            ) {
                "Datos enviados correctamente"
            } else {
                "Fallo el envio"
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Mensaje")
                .setMessage(mensaje)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private class DownloadImageTask(
        private val imageView: ImageView
    ) : AsyncTask<String, Void, Bitmap>() {

        override fun doInBackground(vararg urls: String): Bitmap? {
            val urlDisplay = urls.firstOrNull() ?: return null

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

    fun disableSSLVerification() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
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
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
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

        override fun doInBackground(vararg voids: Void): String? {
            return try {
                val ctx = context?.applicationContext ?: return ""

                database = dbHelper.writableDatabase
                database.execSQL("DELETE FROM ve_ws_itemTemp")

                val cadena = "2,0,2,'$it_codigo'"

                solicitudSoap.initializeVariables(
                    getString(R.string.str_id).toInt(),
                    cadena
                )

                val result = solicitudSoap.realizarSolicitudSoap()
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }

                if (result.isNullOrBlank()) {
                    ""
                } else {
                    XmlParserItemTemporales.parseToTable(
                        result,
                        database,
                        ctx
                    )
                }
            } catch (e: Exception) {
                Log.e("SolicitudSOAP", "Error al procesar la solicitud: ${e.message}", e)

                activity?.runOnUiThread {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                ""
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            try {
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }

                fnDetallesItem()
            } finally {
                if (::database.isInitialized && database.isOpen) {
                    database.close()
                }
            }
        }
    }

    fun fnDetallesItem() {
        val lista = llenarControles.fnConsultarItemInventario(
            it_codigo,
            bo_codigo.toString(),
            obtenerFechaActual()
        )

        if (lista.isNotEmpty()) {
            val item = lista.first()

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
            txtObservacionEI.text = item.observacion
            txtCantidadEI.setText(item.cantidad)
            it_enlace = item.enlace

            if (it_enlace.isNotBlank()) {
                btnEnlaceRD.isEnabled = true
                btnEnlaceRD.text = "si"
            }
        } else {
            item_codigo.text = it_codigo
            Toast.makeText(
                requireContext(),
                "No se encontraron datos para el item escaneado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun fnShowImageDialog() {
        val imageDialogView = layoutInflater.inflate(R.layout.imagen, null)
        photoView = imageDialogView.findViewById(R.id.imageViewDialog)

        val drawable = itemImage.drawable

        if (drawable !is BitmapDrawable) {
            showToast("No hay imagen cargada")
            return
        }

        photoView.setImageBitmap(drawable.bitmap)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(imageDialogView)
            .setPositiveButton("Cerrar") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setNegativeButton("Descargar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
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
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "No hay imagen para guardar", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val bitmap = drawable.bitmap
                val fecha = SimpleDateFormat("_ddMMyyyy_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "$codigo$fecha.png"

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Cotzul")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = requireActivity().contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                if (uri == null) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "No se pudo guardar la imagen", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)

                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Imagen guardada", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()

                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun fnOpenImagePicker() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageFile = createImageFile()

        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireActivity().packageName}.provider",
            imageFile
        )

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

        val cameraAvailable = cameraIntent.resolveActivity(requireActivity().packageManager) != null
        val intentArray = if (cameraAvailable) {
            arrayOf(cameraIntent)
        } else {
            emptyArray()
        }

        val chooserIntent = Intent.createChooser(galleryIntent, "Seleccionar una opción")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
        startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK || requestCode != PICK_IMAGE_REQUEST) return
        if (selectedImageViewIndex !in imageViews.indices) return

        val selectedImage = data?.data
        val uri = selectedImage ?: if (::photoUri.isInitialized) photoUri else null

        if (uri != null) {
            loadImage(uri, imageViews[selectedImageViewIndex])
            originalBitmaps[selectedImageViewIndex] = getBitmapFromUri(uri)
            imageUris[selectedImageViewIndex] = uri
            imageChanged[selectedImageViewIndex] = true
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun loadImage(imageUri: Uri, imageView: ImageView) {
        Glide.with(this)
            .load(imageUri)
            .into(imageView)
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return requireActivity().contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }

    fun fnCrearPdf(
        originalBitmaps: List<Bitmap?>,
        imageChanged: List<Boolean>,
        imageUris: List<Uri?>
    ): String? {
        val imageCount = originalBitmaps.indices.count { index ->
            imageChanged.getOrNull(index) == true && originalBitmaps[index] != null
        }

        if (imageCount == 0) return null

        val outputStream = ByteArrayOutputStream()
        val pdfWriter = PdfWriter(outputStream)
        pdfWriter.setCompressionLevel(9)

        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)

        var imagesInCurrentPage = 0
        var table = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
            .useAllAvailableWidth()

        for (index in originalBitmaps.indices) {
            if (imageChanged.getOrNull(index) != true) continue

            val bitmap = originalBitmaps[index] ?: continue
            val orientedBitmap = getCorrectlyOrientedBitmap(bitmap, imageUris.getOrNull(index))

            val imageData = ByteArrayOutputStream()
            orientedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, imageData)

            val image = Image(ImageDataFactory.create(imageData.toByteArray())).apply {
                scaleToFit(250f, 300f)
            }

            val cell = Cell()
                .add(image)
                .setPadding(10f)
                .setBorder(Border.NO_BORDER)
                .setHeight(300f)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)

            table.addCell(cell)
            imagesInCurrentPage++

            if (imagesInCurrentPage == 2) {
                document.add(table)

                table = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
                    .useAllAvailableWidth()

                val hasMoreImages = (index + 1 until originalBitmaps.size).any { nextIndex ->
                    imageChanged.getOrNull(nextIndex) == true && originalBitmaps[nextIndex] != null
                }

                if (hasMoreImages) {
                    pdfDocument.addNewPage()
                }

                imagesInCurrentPage = 0
            }
        }

        if (imagesInCurrentPage > 0) {
            while (imagesInCurrentPage < 2) {
                table.addCell(
                    Cell()
                        .setPadding(10f)
                        .setBorder(Border.NO_BORDER)
                )
                imagesInCurrentPage++
            }

            document.add(table)
        } else if (pdfDocument.numberOfPages > 1) {
            pdfDocument.removePage(pdfDocument.numberOfPages)
        }

        document.close()

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun getCorrectlyOrientedBitmap(bitmap: Bitmap, uri: Uri?): Bitmap {
        if (uri == null) return bitmap

        return try {
            requireActivity().contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

                val rotation = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }

                if (rotation == 0f) {
                    bitmap
                } else {
                    val matrix = Matrix().apply {
                        postRotate(rotation)
                    }

                    Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.width,
                        bitmap.height,
                        matrix,
                        true
                    )
                }
            } ?: bitmap
        } catch (e: Exception) {
            bitmap
        }
    }

    fun fnEnviarImagenes(pdfBase64: String, context: Context) {
        val nombrePdf = "${bo_codigo}_${it_codigo}_${ep_codigo}"
        enlace = "https://app.cotzul.com/sitenet/37/img/$nombrePdf.pdf"

        val clsRegImagen = ClsRegImagen(context)
        val fecha = obtenerFechaActual()

        progressDialog = ProgressDialog(context).apply {
            setTitle("Sistema")
            setMessage("Enviando...")
            setCancelable(false)
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            show()
        }

        try {
            clsRegImagen.enviarDatosInventario(
                nombrePdf,
                pdfBase64,
                { mensaje ->
                    response = mensaje
                    insertarEnlaceYEnviarInforme(fecha)
                    it_enlace = enlace
                    btnEnlaceRD.isEnabled = true
                    btnEnlaceRD.text = "si"
                },
                { error ->
                    response = error
                    insertarEnlaceYEnviarInforme(fecha)
                },
                {
                    insertarEnlaceYEnviarInforme(fecha)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al intentar enviar la imagen", Toast.LENGTH_SHORT).show()
            fnCerrarProgressDialog()
        }
    }

    private fun insertarEnlaceYEnviarInforme(fecha: String) {
        llenarControles.fnInsertarGpEnlaceItem(
            it_codigo,
            bo_codigo,
            enlace,
            fecha
        )

        val dialog = progressDialog
        if (dialog != null) {
            fnEnviarInforme(dialog)
        }
    }

    fun fnCerrarProgressDialog() {
        progressDialog?.let { dialog ->
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        progressDialog = null
    }

    private fun fnAbrirEnlace() {
        val url = it_enlace
        if (url.isBlank()) return

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }

        startActivity(intent)
    }

    private inner class MiAsyncTaskStock(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase

        override fun doInBackground(vararg voids: Void): String? {
            return try {
                val ctx = context?.applicationContext ?: return ""

                database = dbHelper.writableDatabase
                database.execSQL("DELETE FROM ve_ws_itemTmp")

                val cadena = "0,3,'$it_codigo'"

                solicitudSoap.initializeVariables(
                    getString(R.string.str_usuarios).toInt(),
                    cadena
                )

                val result = solicitudSoap.realizarSolicitudSoap()
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }

                if (result.isNullOrBlank()) {
                    ""
                } else {
                    XmlParserItemTemporal.parseToTable(
                        result,
                        database,
                        ctx
                    )
                }
            } catch (e: Exception) {
                Log.e("SolicitudSOAP", "Error al procesar la solicitud: ${e.message}", e)

                activity?.runOnUiThread {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                ""
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            try {
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }

                fnConsultarStock()
            } finally {
                if (::database.isInitialized && database.isOpen) {
                    database.close()
                }
            }
        }
    }

    fun fnConsultarStock() {
        val stock = llenarControles.fnObtenerStockAuditoria(it_codigo)

        if (stock.size > 6) {
            item_stockBo.text = stock[4]
            item_stockAl.text = stock[2]
            item_stockEt.text = stock[3]
            item_stockPm.text = stock[5]
            item_stockEp.text = stock[6]
        }
    }
}
