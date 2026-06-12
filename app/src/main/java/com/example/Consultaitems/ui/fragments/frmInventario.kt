package com.example.Consultaitems.ui.fragments

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cotzul.ConsultaitemsMovil.utils.cls.BarcodeAnalyzer
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.activities.frmLogin
import com.example.Consultaitems.ui.adapters.AdaptadorItemsScan
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.SpinnerItem
import com.example.Consultaitems.utils.parser.XmlConteos
import com.example.Consultaitems.utils.parser.XmlParserConteos
import com.example.Consultaitems.utils.parser.XmlParserItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import androidx.camera.core.Camera
import com.example.Consultaitems.ui.adapters.ItemScan

class FrmInventario : Fragment() {

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1001
    }

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null

    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var solicitudSoap: SolicitudSoap
    private lateinit var dbHelper: SqLiteOpenHelper

    private lateinit var btnEscanearQR: Button
    private lateinit var qr_frame: View
    private lateinit var rvItems: RecyclerView
    private lateinit var adaptadorItems: AdaptadorItemsScan
    private lateinit var spinnerBodega: Spinner

    private val items = mutableListOf<ItemScan>()

    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null

    private var isCameraActive = false
    private var isCameraBound = false
    private var isScanning = false

    private var scanDialog: ProgressDialog? = null
    private var scanTimeout: Runnable? = null
    private var focusRunnable: Runnable? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private val processing = AtomicBoolean(false)
    private val guardandoScan = AtomicBoolean(false)

    private val regexEan13 = Regex("^\\d{13}$")

    private var ultimoCodigoDetectado: String? = null
    private var ultimaLecturaMs: Long = 0L
    private val tiempoCodigoValidoMs = 1500L

    private var toneGenerator: ToneGenerator? = null

    private var bo_codigo: Int = 0
    private var us_login: String = ""
    private lateinit var ClaseXml: XmlConteos

    private val vendedor = frmLogin.CadenaHolder.ep_codigo

    private data class ResultadoConteo(
        val codigo: String,
        val cantidadActual: Int?,
        val valido: Boolean
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        val view = inflater.inflate(R.layout.frm_scan_inventario, container, false)

        if (!::cameraExecutor.isInitialized || cameraExecutor.isShutdown) {
            cameraExecutor = Executors.newSingleThreadExecutor()
        }

        dbHelper = SqLiteOpenHelper(requireContext())
        llenarControles = ClsLLenarControles(requireContext())

        previewView = view.findViewById(R.id.camera_preview)
        btnEscanearQR = view.findViewById(R.id.btnEscanearQR)
        qr_frame = view.findViewById(R.id.qr_frame)
        rvItems = view.findViewById(R.id.rvItems)
        spinnerBodega = view.findViewById(R.id.spinnerBodega)

        llenarControles.fnLLenarSpinnerBodega(spinnerBodega, "iv_ws_bodega")

        adaptadorItems = AdaptadorItemsScan(
            datos = items,
            onDelete = { item, _ ->
                fnRestarOEliminarConteo(
                    boCodigo = bo_codigo,
                    itCodigo = item.codigo,
                    fecha = fnFecha()
                )
            },
            onClick = { _, _ ->
                // Opcional
            }
        )

        val vendedorLocal = llenarControles.fnLLenarVendedor()
        if (vendedorLocal != null) {
            us_login = vendedorLocal.login
        }

        rvItems.layoutManager = LinearLayoutManager(requireContext())
        rvItems.adapter = adaptadorItems
        rvItems.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )

        btnEscanearQR.setOnClickListener {
            if (!isScanning) {
                startScanning()
            } else {
                stopScanning(showNoCode = false)
            }
        }


        spinnerBodega.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = parent.getItemAtPosition(position) as SpinnerItem
                bo_codigo = item.codigo.toInt()
                fnObtenerConteo()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Sin acción
            }
        }

        if (checkCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.frm_menu_inventario, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btnsincronizarEI -> {
                dbHelper = SqLiteOpenHelper(requireContext())
                solicitudSoap = SolicitudSoap(requireContext())
                sincronizarDatos()
                true
            }

            R.id.btnEnviarEI -> {
                fnEnviarInforme()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CAMERA
            )
        ) {
            Toast.makeText(
                requireContext(),
                "La cámara es necesaria para escanear códigos.",
                Toast.LENGTH_LONG
            ).show()
        }

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (
            requestCode == CAMERA_PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            showToast("Permiso de cámara denegado")
        }
    }

    private fun startCamera() {
        if (isCameraBound) return
        if (!isAdded) return

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            if (!isAdded) return@addListener

            try {
                cameraProvider = cameraProviderFuture.get()

                preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->

                        analysis.setAnalyzer(
                            cameraExecutor,
                            BarcodeAnalyzer(
                                previewView = previewView,
                                frameView = qr_frame,
                                shouldAnalyze = {
                                    true
                                }
                            ) { qrCode ->

                                mainHandler.post {
                                    if (!isAdded) return@post
                                    if (qrCode.isNullOrBlank()) return@post

                                    val code = qrCode.trim()

                                    ultimoCodigoDetectado = code
                                    ultimaLecturaMs = SystemClock.elapsedRealtime()

                                    if (!isScanning) return@post

                                    if (!processing.compareAndSet(false, true)) return@post

                                    stopScanning(showNoCode = false)

                                    fnGuardarConteo(code)
                                }
                            }
                        )
                    }

                cameraProvider?.unbindAll()

                camera = cameraProvider?.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )

                isCameraBound = true
                isCameraActive = true

            } catch (exc: Exception) {
                Log.e("FrmInventario", "Error al inicializar la cámara", exc)
                isCameraBound = false
                isCameraActive = false
                camera = null
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun startScanning() {
        if (isScanning) return

        if (!isCameraActive || !isCameraBound) {
            startCamera()
        }

        val ahora = SystemClock.elapsedRealtime()
        val codigoActual = ultimoCodigoDetectado

        if (
            !codigoActual.isNullOrBlank() &&
            ahora - ultimaLecturaMs <= tiempoCodigoValidoMs
        ) {
            fnGuardarConteo(codigoActual.trim())
            return
        }

        isScanning = true
        processing.set(false)

        enfocarFrame()
        iniciarAutoEnfoqueScanner()

        scanDialog?.dismiss()
        scanDialog = ProgressDialog(requireActivity()).apply {
            setMessage("Escaneando...")
            setCancelable(true)
            setOnCancelListener {
                stopScanning(showNoCode = false)
            }
            show()
        }

        scanTimeout?.let(mainHandler::removeCallbacks)
        scanTimeout = Runnable {
            stopScanning(showNoCode = true)
        }

        mainHandler.postDelayed(scanTimeout!!, 3000)
    }

    private fun stopScanning(showNoCode: Boolean) {
        if (!isScanning) {
            if (showNoCode && isAdded) {
                showNoCodeDialog("")
            }

            processing.set(false)
            detenerAutoEnfoqueScanner()
            return
        }

        isScanning = false
        processing.set(false)

        detenerAutoEnfoqueScanner()

        scanTimeout?.let(mainHandler::removeCallbacks)
        scanTimeout = null

        scanDialog?.let { dialog ->
            dialog.setOnCancelListener(null)
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }

        scanDialog = null

        if (showNoCode && isAdded) {
            showNoCodeDialog("")
        }
    }

    private fun enfocarFrame() {
        if (!isAdded) return
        if (previewView.width == 0 || previewView.height == 0) return
        if (qr_frame.width == 0 || qr_frame.height == 0) return

        val cameraControl = camera?.cameraControl ?: return
        val factory = previewView.meteringPointFactory

        val previewLocation = IntArray(2)
        val frameLocation = IntArray(2)

        previewView.getLocationOnScreen(previewLocation)
        qr_frame.getLocationOnScreen(frameLocation)

        val frameCenterX = frameLocation[0] - previewLocation[0] + qr_frame.width / 2f
        val frameCenterY = frameLocation[1] - previewLocation[1] + qr_frame.height / 2f

        val punto = factory.createPoint(frameCenterX, frameCenterY)

        val action = FocusMeteringAction.Builder(
            punto,
            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
        )
            .setAutoCancelDuration(1, TimeUnit.SECONDS)
            .build()

        cameraControl.startFocusAndMetering(action)
    }

    private fun iniciarAutoEnfoqueScanner() {
        detenerAutoEnfoqueScanner()

        focusRunnable = object : Runnable {
            override fun run() {
                if (!isScanning) return

                enfocarFrame()
                mainHandler.postDelayed(this, 800)
            }
        }

        mainHandler.post(focusRunnable!!)
    }

    private fun detenerAutoEnfoqueScanner() {
        focusRunnable?.let {
            mainHandler.removeCallbacks(it)
        }

        focusRunnable = null
    }

    private fun showNoCodeDialog(codigo: String) {
        if (!isAdded) return

        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("No se detectó ningún código válido.\n$codigo")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun fnGuardarConteo(it_codigo: String) {
        if (!guardandoScan.compareAndSet(false, true)) return

        val fecha = fnFecha()
        val usuario = us_login
        val observacion = ""
        val enlace = ""
        val reconteo = ""
        val bodegaCodigo = bo_codigo
        var codigoEscaneado = it_codigo.trim()

        if (codigoEscaneado.contains("EAN-")) {
            codigoEscaneado = codigoEscaneado.replace("EAN-", "")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val datos = withContext(Dispatchers.IO) {
                    var codigo = codigoEscaneado

                    val codigoDesdeEan: String? = if (
                        codigo.matches(regexEan13) &&
                        bodegaCodigo !in listOf(23, 52)
                    ) {
                        llenarControles.fnItemEan(codigo)
                    } else {
                        null
                    }

                    if (!codigoDesdeEan.isNullOrBlank()) {
                        codigo = codigoDesdeEan
                    }

                    val itemValido = llenarControles.fnItemValido(codigo)

                    if (bodegaCodigo !in listOf(23, 52) && itemValido != codigo) {
                        return@withContext ResultadoConteo(
                            codigo = codigo,
                            cantidadActual = null,
                            valido = false
                        )
                    }

                    val cantidadActual = llenarControles.fnObtenerCantidadItem(
                        bodegaCodigo.toString(),
                        codigo,
                        fecha
                    )

                    ResultadoConteo(
                        codigo = codigo,
                        cantidadActual = cantidadActual,
                        valido = true
                    )
                }

                if (!datos.valido) {
                    showNoCodeDialog(datos.codigo)
                    guardandoScan.set(false)
                    return@launch
                }

                val cantidadActual = datos.cantidadActual

                if (cantidadActual == null) {
                    guardarCantidadAsync(
                        codigo = datos.codigo,
                        cantidad = "1",
                        bodegaCodigo = bodegaCodigo,
                        fecha = fecha,
                        usuario = usuario,
                        observacion = observacion,
                        enlace = enlace,
                        reconteo = reconteo
                    )

                    guardandoScan.set(false)
                    return@launch
                }

                val nuevaCantidad = (cantidadActual + 1).toString()
                val requiereConfirmacion = bodegaCodigo == 23 || bodegaCodigo == 52

                if (!requiereConfirmacion) {
                    guardarCantidadAsync(
                        codigo = datos.codigo,
                        cantidad = nuevaCantidad,
                        bodegaCodigo = bodegaCodigo,
                        fecha = fecha,
                        usuario = usuario,
                        observacion = observacion,
                        enlace = enlace,
                        reconteo = reconteo
                    )

                    guardandoScan.set(false)
                    return@launch
                }

                if (!isAdded) {
                    guardandoScan.set(false)
                    return@launch
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("Sistema")
                    .setMessage("El ítem ya fue registrado ${datos.codigo}\n\n¿Deseas agregarlo?")
                    .setCancelable(false)
                    .setPositiveButton("Sí") { dialog, _ ->
                        dialog.dismiss()

                        viewLifecycleOwner.lifecycleScope.launch {
                            try {
                                guardarCantidadAsync(
                                    codigo = datos.codigo,
                                    cantidad = nuevaCantidad,
                                    bodegaCodigo = bodegaCodigo,
                                    fecha = fecha,
                                    usuario = usuario,
                                    observacion = observacion,
                                    enlace = enlace,
                                    reconteo = reconteo
                                )
                            } finally {
                                guardandoScan.set(false)
                            }
                        }
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                        guardandoScan.set(false)
                    }
                    .show()

            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Error al guardar el conteo")
                guardandoScan.set(false)
            }
        }
    }

    private suspend fun guardarCantidadAsync(
        codigo: String,
        cantidad: String,
        bodegaCodigo: Int,
        fecha: String,
        usuario: String,
        observacion: String,
        enlace: String,
        reconteo: String
    ) {
        withContext(Dispatchers.IO) {
            llenarControles.fnInsertarConteo(
                codigo,
                1,
                bodegaCodigo,
                fecha,
                cantidad,
                "A",
                usuario,
                observacion,
                enlace,
                reconteo
            )
        }

        fnObtenerConteoAsync(bodegaCodigo)

        reproducirSonidoEscaneo()
    }

    private fun reproducirSonidoEscaneo() {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            }

            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fnObtenerConteo() {
        val bodegaActual = bo_codigo

        viewLifecycleOwner.lifecycleScope.launch {
            fnObtenerConteoAsync(bodegaActual)
        }
    }

    private suspend fun fnObtenerConteoAsync(bodegaCodigo: Int) {
        val resultados = withContext(Dispatchers.IO) {
            llenarControles.fnObtenerItemsConteo(fnFecha(), bodegaCodigo)
        }

        if (!isAdded) return
        if (bodegaCodigo != bo_codigo) return

        items.clear()
        items.addAll(resultados)
        adaptadorItems.notifyDataSetChanged()

        if (items.isNotEmpty()) {
            rvItems.scrollToPosition(items.size - 1)
        }
    }

    private fun fnRestarOEliminarConteo(
        boCodigo: Int,
        itCodigo: String,
        fecha: String
    ) {
        if (!isAdded) return

        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("¿Deseas eliminar este Item?")
            .setPositiveButton("Sí") { dialog, _ ->
                dialog.dismiss()

                viewLifecycleOwner.lifecycleScope.launch {
                    val ok = withContext(Dispatchers.IO) {
                        runCatching {
                            val cantidadActual = llenarControles.fnObtenerCantidadItem(
                                boCodigo.toString(),
                                itCodigo,
                                fecha
                            ) ?: 0

                            when {
                                cantidadActual > 1 -> {
                                    val nuevaCantidad = cantidadActual - 1

                                    llenarControles.fnInsertarConteo(
                                        itCodigo,
                                        1,
                                        boCodigo,
                                        fecha,
                                        nuevaCantidad.toString(),
                                        "A",
                                        us_login,
                                        "",
                                        "",
                                        ""
                                    )

                                    true
                                }

                                cantidadActual == 1 -> {
                                    llenarControles.fnEliminarConteo(
                                        boCodigo.toString(),
                                        itCodigo,
                                        fecha
                                    )
                                }

                                else -> {
                                    false
                                }
                            }
                        }.getOrDefault(false)
                    }

                    if (!isAdded) return@launch

                    if (ok) {
                        fnObtenerConteoAsync(boCodigo)
                    } else {
                        showToast("No se pudo actualizar el conteo")
                    }
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun fnFecha(formato: String = "dd/MM/yyyy"): String {
        val fechaActual = java.util.Calendar.getInstance().time
        val formatoFecha = java.text.SimpleDateFormat(
            formato,
            java.util.Locale.getDefault()
        )
        return formatoFecha.format(fechaActual)
    }

    private fun stopCamera() {
        detenerAutoEnfoqueScanner()

        scanTimeout?.let(mainHandler::removeCallbacks)
        scanTimeout = null

        scanDialog?.let { dialog ->
            dialog.setOnCancelListener(null)
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }

        scanDialog = null

        isScanning = false
        processing.set(false)

        cameraProvider?.unbindAll()

        camera = null
        isCameraBound = false
        isCameraActive = false
    }

    private fun hideSoftKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = requireActivity().currentFocus

        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        } else {
            Log.d("hideSoftKeyboard", "No hay vista enfocada para ocultar el teclado")
        }

        forceRedrawWindow(requireActivity())
    }

    fun forceRedrawWindow(activity: Activity) {
        val contentView = activity.findViewById<View>(android.R.id.content)
        contentView.requestLayout()
    }

    override fun onStop() {
        super.onStop()
        stopCamera()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        stopCamera()
        mainHandler.removeCallbacksAndMessages(null)

        toneGenerator?.release()
        toneGenerator = null

        if (::cameraExecutor.isInitialized && !cameraExecutor.isShutdown) {
            cameraExecutor.shutdown()
        }
    }

    override fun onResume() {
        super.onResume()

        if (!::cameraExecutor.isInitialized || cameraExecutor.isShutdown) {
            cameraExecutor = Executors.newSingleThreadExecutor()
        }

        if (!isHidden && checkCameraPermission()) {
            startCamera()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (hidden) {
            stopCamera()
        } else {
            if (checkCameraPermission()) {
                startCamera()
            }
        }
    }

    private fun sincronizarDatos() {
        if (isNetworkAvailable(requireContext())) {
            AlertDialog.Builder(requireContext())
                .setMessage("¿Está seguro de que desea sincronizar los datos?")
                .setPositiveButton("Sí") { dialog, _ ->
                    dialog.dismiss()

                    val progressDialog = showProgressDialog()
                    MiAsyncTaskItems(progressDialog).execute()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            showToast("Verifique su conexión a internet")
        }
    }

    private inner class MiAsyncTaskItems(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase

        override fun doInBackground(vararg voids: Void): String? {
            return try {
                val ctx = context?.applicationContext ?: return null

                database = dbHelper.writableDatabase

                database.execSQL("DELETE FROM ve_ws_usuario")
                database.execSQL("DELETE FROM iv_ws_bodega")
                database.execSQL("DELETE FROM iv_ws_itemxbodega")
                database.execSQL("DELETE FROM ve_ws_clienteAsignadoVendedor")

                val cadena = "$vendedor,2"

                solicitudSoap.initializeVariables(
                    getString(R.string.str_tablas).toInt(),
                    cadena
                )

                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.bufferedReader()?.use { it.readText() }

                if (!result.isNullOrBlank()) {
                    XmlParserItems.parseMultiTable(
                        result,
                        database,
                        ctx
                    )
                }

                result
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            try {
                if (!result.isNullOrBlank()) {
                    showToast("Los datos se sincronizaron correctamente")
                    llenarControles.fnLLenarSpinnerBodega(spinnerBodega, "iv_ws_bodega")
                } else {
                    showToast("Error en la sincronización")
                    showToast("Verifique su internet")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (::database.isInitialized && database.isOpen) {
                    database.close()
                }

                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun showToast(message: String) {
        if (!isAdded) return
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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

    fun fnEnviarInforme() {
        if (!isAdded) return

        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("¿Desea enviar los datos?")
            .setPositiveButton("Sí") { dialog, _ ->
                dialog.dismiss()

                if (isNetworkAvailable(requireContext())) {
                    try {
                        val progressDialog = showProgressDialog()

                        ClaseXml = XmlConteos(requireContext())
                        solicitudSoap = SolicitudSoap(requireContext())

                        MiAsyncTaskEnvio(progressDialog).execute()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showToast("Error al enviar los datos")
                    }
                } else {
                    showToast("Verifique su conexión a internet")
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private inner class MiAsyncTaskEnvio(
        private val progressDialog: ProgressDialog
    ) : AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase

        override fun doInBackground(vararg voids: Void): String {
            return try {
                val ctx = context?.applicationContext ?: return "ERROR"

                database = dbHelper.writableDatabase

                val cadena = ClaseXml.obtenerConteo(1)

                solicitudSoap.initializeVariables(
                    getString(R.string.str_conteo).toInt(),
                    cadena
                )

                var recibo = ""

                val inputStream = solicitudSoap.realizarSolicitudSoap()
                val result = inputStream?.bufferedReader()?.use { it.readText() }

                if (!result.isNullOrBlank()) {
                    recibo = XmlParserConteos.parseAndUpdateDocumentCode(
                        result,
                        database,
                        ctx,
                        1
                    )
                }

                recibo
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
                "Falló el envío"
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Mensaje")
                .setMessage(mensaje)
                .setPositiveButton("OK") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()
        }
    }
}