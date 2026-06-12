package com.example.Consultaitems.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.cotzul.ConsultaitemsMovil.utils.cls.BarcodeAnalyzer
import com.example.Consultaitems.R
import defpackage.FrmItemDetails
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FrmScanQr : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var qrFrame: View
    private lateinit var btnEscanearQR: Button
    private lateinit var cameraExecutor: ExecutorService

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var isCameraActive = false
    private var isReading = false

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                updateCameraAccordingToChildStack()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permiso de cámara denegado.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private val backStackListener = FragmentManager.OnBackStackChangedListener {
        updateCameraAccordingToChildStack()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.frm_scan_qr, container, false)

        previewView = view.findViewById(R.id.camera_preview)
        qrFrame = view.findViewById(R.id.qr_frame)
        btnEscanearQR = view.findViewById(R.id.btnEscanearQR)

        btnEscanearQR.setOnClickListener {
            startReading()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.addOnBackStackChangedListener(backStackListener)

        if (checkCameraPermission()) {
            updateCameraAccordingToChildStack()
        } else {
            requestCameraPermission()
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun updateCameraAccordingToChildStack() {
        val enDetalle = childFragmentManager.backStackEntryCount > 0

        if (enDetalle) {
            stopCamera()
            return
        }

        view?.post {
            startCamera()
        }
    }

    private fun startCamera() {
        if (childFragmentManager.backStackEntryCount > 0 || isCameraActive) return
        if (!checkCameraPermission()) return

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also { preview ->
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                    }

                imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                cameraProvider?.unbindAll()

                cameraProvider?.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )

                isCameraActive = true
                Log.d(TAG, "Cámara iniciada.")

            } catch (e: Exception) {
                Log.e(TAG, "Error al inicializar la cámara", e)
                isCameraActive = false
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun startReading() {
        if (!isCameraActive || isReading) return

        isReading = true

        imageAnalysis?.setAnalyzer(
            cameraExecutor,
            BarcodeAnalyzer(
                previewView = previewView,
                frameView = qrFrame,
                shouldAnalyze = {
                    isReading
                }
            ) { qrCode ->

                val codigo = qrCode?.trim()

                if (codigo.isNullOrBlank()) return@BarcodeAnalyzer

                activity?.runOnUiThread {
                    if (!isAdded) return@runOnUiThread

                    stopReading()
                    navigateToItemDetails(codigo)
                }
            }
        )
    }

    private fun stopReading() {
        if (isReading) {
            imageAnalysis?.clearAnalyzer()
            isReading = false
        }
    }

    fun stopCamera() {
        stopReading()

        if (isCameraActive) {
            cameraProvider?.unbindAll()
            isCameraActive = false
            Log.d(TAG, "Cámara detenida.")
        }
    }

    private fun navigateToItemDetails(qrCode: String) {
        stopCamera()

        val fragment = FrmItemDetails().apply {
            arguments = Bundle().apply {
                putString("qrCode", qrCode)
            }
        }

        childFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.detail_container, fragment, "FrmItemDetails")
            .addToBackStack("FrmItemDetails")
            .commit()
    }

    override fun onResume() {
        super.onResume()

        if (checkCameraPermission()) {
            updateCameraAccordingToChildStack()
        }
    }

    override fun onPause() {
        stopReading()
        super.onPause()
    }

    override fun onDestroyView() {
        childFragmentManager.removeOnBackStackChangedListener(backStackListener)
        stopCamera()
        super.onDestroyView()
    }

    override fun onDestroy() {
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }

        super.onDestroy()
    }

    companion object {
        private const val TAG = "FrmScanQr"
    }
}