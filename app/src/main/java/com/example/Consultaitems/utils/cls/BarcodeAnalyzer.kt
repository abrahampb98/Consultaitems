package com.cotzul.ConsultaitemsMovil.utils.cls

import android.graphics.Rect
import android.view.View
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

class BarcodeAnalyzer(
    private val previewView: PreviewView,
    private val frameView: View,
    private val shouldAnalyze: () -> Boolean,
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    private val procesando = AtomicBoolean(false)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!shouldAnalyze()) {
            imageProxy.close()
            return
        }

        if (!procesando.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image

        if (mediaImage == null) {
            procesando.set(false)
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotation)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val barcodeDentroFrame = barcodes.firstOrNull { barcode ->
                    val valor = barcode.rawValue
                    val box = barcode.boundingBox

                    !valor.isNullOrBlank() &&
                            box != null &&
                            estaDentroDelFrame(box, imageProxy, rotation)
                }

                val valor = barcodeDentroFrame?.rawValue

                if (!valor.isNullOrBlank()) {
                    onBarcodeDetected(valor)
                }
            }
            .addOnFailureListener {
                // Ignorar error de lectura
            }
            .addOnCompleteListener {
                procesando.set(false)
                imageProxy.close()
            }
    }

    private fun estaDentroDelFrame(
        barcodeBox: Rect,
        imageProxy: ImageProxy,
        rotation: Int
    ): Boolean {
        if (previewView.width == 0 || previewView.height == 0) return false
        if (frameView.width == 0 || frameView.height == 0) return false

        val imageWidth: Int
        val imageHeight: Int

        if (rotation == 90 || rotation == 270) {
            imageWidth = imageProxy.height
            imageHeight = imageProxy.width
        } else {
            imageWidth = imageProxy.width
            imageHeight = imageProxy.height
        }

        val scale = max(
            previewView.width.toFloat() / imageWidth.toFloat(),
            previewView.height.toFloat() / imageHeight.toFloat()
        )

        val offsetX = (previewView.width - imageWidth * scale) / 2f
        val offsetY = (previewView.height - imageHeight * scale) / 2f

        val barcodeCenterX = barcodeBox.centerX() * scale + offsetX
        val barcodeCenterY = barcodeBox.centerY() * scale + offsetY

        val previewLocation = IntArray(2)
        val frameLocation = IntArray(2)

        previewView.getLocationOnScreen(previewLocation)
        frameView.getLocationOnScreen(frameLocation)

        val frameLeft = frameLocation[0] - previewLocation[0]
        val frameTop = frameLocation[1] - previewLocation[1]
        val frameRight = frameLeft + frameView.width
        val frameBottom = frameTop + frameView.height

        return barcodeCenterX >= frameLeft &&
                barcodeCenterX <= frameRight &&
                barcodeCenterY >= frameTop &&
                barcodeCenterY <= frameBottom
    }
}