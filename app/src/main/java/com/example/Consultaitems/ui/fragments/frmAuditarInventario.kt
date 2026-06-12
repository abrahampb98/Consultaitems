package com.example.Consultaitems.ui.fragments

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.cotzul.ConsultaitemsMovil.utils.cls.BarcodeAnalyzer
import com.example.Consultaitems.R
import com.example.Consultaitems.data.database.SqLiteOpenHelper
import com.example.Consultaitems.network.SolicitudSoap
import com.example.Consultaitems.ui.activities.frmLogin
import com.example.Consultaitems.ui.adapters.TransporteAdapter
import com.example.Consultaitems.utils.cls.ClsLLenarControles
import com.example.Consultaitems.utils.cls.SpinnerItem
import com.example.Consultaitems.utils.parser.XmlParserItems
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class FrmAuditarInventario : Fragment() {

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1001
    }

    private var backStackListener: FragmentManager.OnBackStackChangedListener? = null

    private var bo_codigo: Int = 0
    private var it_codigo: String = ""

    private lateinit var btnEscanearQR: Button
    lateinit var butBuscarRF: Button

    lateinit var editTextReferencia: AutoCompleteTextView
    private lateinit var previewView: PreviewView
    private lateinit var qr_frame: View
    private lateinit var spBodega: Spinner

    private lateinit var llenarControles: ClsLLenarControles
    lateinit var solicitudSoap: SolicitudSoap
    lateinit var dbHelper: SqLiteOpenHelper

    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null

    private var isCameraActive: Boolean = false
    private var isCameraBound: Boolean = false
    private var isScanning: Boolean = false

    private var scanDialog: ProgressDialog? = null
    private var scanTimeout: Runnable? = null

    val vendedor: Int = frmLogin.CadenaHolder.ep_codigo

    private val mainHandler = Handler(Looper.getMainLooper())
    private val processing = AtomicBoolean(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        val view = inflater.inflate(R.layout.frm_auditar_inventario, container, false)

        backStackListener = FragmentManager.OnBackStackChangedListener {
            val enDetalle = childFragmentManager.backStackEntryCount > 0

            if (enDetalle) {
                stopCamera()
            } else {
                getView()?.post {
                    startCamera()
                }
            }
        }

        previewView = view.findViewById(R.id.camera_preview)
        editTextReferencia = view.findViewById(R.id.editTextReferencia)
        btnEscanearQR = view.findViewById(R.id.btnEscanearQR)
        qr_frame = view.findViewById(R.id.qr_frame)
        spBodega = view.findViewById(R.id.spBodega)

        llenarControles = ClsLLenarControles(requireContext())
        llenarControles.fnLLenarSpinnerBodegaAuditoria(spBodega)

        if (checkCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        fnllenarAdapter()

        btnEscanearQR.setOnClickListener {
            when {
                bo_codigo == 0 -> {
                    fnAlertaSeleccionBodega(requireContext())
                }

                !isScanning -> {
                    startScanning()
                }

                else -> {
                    stopScanning(showNoCode = false)
                }
            }
        }

        backStackListener?.let {
            childFragmentManager.addOnBackStackChangedListener(it)
        }

        spBodega.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = parent.getItemAtPosition(position) as SpinnerItem
                bo_codigo = item.codigo.toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Sin acción
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!::cameraExecutor.isInitialized || cameraExecutor.isShutdown) {
            cameraExecutor = Executors.newSingleThreadExecutor()
        }
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

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun fnllenarAdapter() {
        val adaptadorItem = TransporteAdapter(
            requireContext(),
            llenarControles.fnCargarDatosItemBodega()
        )

        editTextReferencia.setAdapter(adaptadorItem)

        editTextReferencia.setOnItemClickListener { _, _, position, _ ->
            val item = adaptadorItem.getItem(position)

            if (item != null) {
                editTextReferencia.setText(item.nombre, false)
                it_codigo = item.codigo
                navigateToItemDetails(it_codigo, bo_codigo)
            }
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
                "La cámara es necesaria para escanear códigos QR.",
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

                val localPreview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                val localImageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                preview = localPreview
                imageAnalysis = localImageAnalysis

                cameraProvider?.unbindAll()

                cameraProvider?.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    localPreview,
                    localImageAnalysis
                )

                isCameraBound = true
                isCameraActive = true
            } catch (exc: Exception) {
                Log.e("FrmScanQr", "Error al inicializar la cámara", exc)
                isCameraBound = false
                isCameraActive = false
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun startScanning() {
        if (isScanning) return

        isScanning = true
        processing.set(false)

        scanDialog?.dismiss()

        scanDialog = ProgressDialog(requireActivity()).apply {
            setMessage("Cargando...")
            setCancelable(true)
            setOnCancelListener {
                stopScanning(showNoCode = false)
            }
            setOnDismissListener {
                stopScanning(showNoCode = false)
            }
            show()
        }

        if (!isCameraActive) {
            startCamera()
        }

        scanTimeout?.let {
            mainHandler.removeCallbacks(it)
        }

        scanTimeout = Runnable {
            stopScanning(showNoCode = true)
        }

        mainHandler.postDelayed(scanTimeout!!, 5000L)

        imageAnalysis?.setAnalyzer(
            cameraExecutor,
            BarcodeAnalyzer(
                previewView = previewView,
                frameView = qr_frame,
                shouldAnalyze = {
                    isScanning
                }
            ) { qrCode ->

                mainHandler.post {
                    if (!isAdded) return@post
                    if (qrCode.isNullOrBlank()) return@post

                    if (!processing.compareAndSet(false, true)) return@post

                    val code = qrCode.trim()

                    stopScanning(showNoCode = false)
                    navigateToItemDetails(code, bo_codigo)
                }
            }
        )
    }

    private fun stopScanning(showNoCode: Boolean) {
        if (!isScanning) {
            if (showNoCode && isAdded) {
                showNoCodeDialog()
            }

            return
        }

        isScanning = false

        scanTimeout?.let {
            mainHandler.removeCallbacks(it)
        }
        scanTimeout = null

        imageAnalysis?.clearAnalyzer()

        scanDialog?.let { dialog ->
            dialog.setOnCancelListener(null)
            dialog.setOnDismissListener(null)

            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }

        scanDialog = null

        if (showNoCode && isAdded) {
            showNoCodeDialog()
        }
    }

    private fun showNoCodeDialog() {
        if (!isAdded) return

        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("No se detectó ningún código.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun stopCamera() {
        stopScanning(showNoCode = false)

        cameraProvider?.unbindAll()

        isCameraBound = false
        isCameraActive = false
    }

    private fun navigateToItemDetails(qrCode: String, boCodigo: Int) {
        stopScanning(showNoCode = false)
        stopCamera()
        hideSoftKeyboard()

        val detalle = FrmDetalleAuditoria()

        detalle.arguments = Bundle().apply {
            putString("qrCode", qrCode)
            putInt("bo_codigo", boCodigo)
        }

        childFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.detail_container, detalle, "FrmItemDetails")
            .addToBackStack("FrmItemDetails")
            .commit()
    }

    private fun hideSoftKeyboard() {
        val imm = requireActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

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

        backStackListener?.let {
            childFragmentManager.removeOnBackStackChangedListener(it)
        }
        backStackListener = null

        stopCamera()
        mainHandler.removeCallbacksAndMessages(null)

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
    ) : AsyncTask<Void, Void, String?>() {

        private lateinit var database: SQLiteDatabase

        override fun onPreExecute() {
            super.onPreExecute()
        }

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
                val result = inputStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }

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
                } else {
                    showToast("Error en la sincronizacion ")
                    showToast("verfique su internet")
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

                if (isAdded) {
                    fnllenarAdapter()
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

    fun fnAlertaSeleccionBodega(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Sistema")
            .setMessage("Debe seleccionar una bodega")
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}
