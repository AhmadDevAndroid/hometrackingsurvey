package com.app.householdtracing.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.app.householdtracing.util.AppUtil.copyToClipboard
import com.app.householdtracing.util.AppUtil.showLogError
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

class CameraProcessor(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val onBarcodeResult: (String) -> Unit,
    private val onReceiptResult: (String) -> Unit
) {

    private val executor = Executors.newSingleThreadExecutor()
    private val barcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()
    )
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var imageAnalysis: ImageAnalysis? = null
    private var isScanning = false

    fun bindCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            imageAnalysis = ImageAnalysis.Builder()
                .setTargetRotation(previewView.display.rotation)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        if (!isScanning) return

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(
                mediaImage, imageProxy.imageInfo.rotationDegrees
            )

            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val barcodeData = barcodes.first().rawValue ?: "No data"
                        onBarcodeResult(barcodeData)
                        copyToClipboard(context, "Barcode", barcodeData)
                        stopScanning()
                    } else {
                        processTextRecognition(inputImage)
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure("Barcode scanning failed", exception)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            onFailure("Media image is null", null)
            imageProxy.close()
        }
    }

    private fun processTextRecognition(inputImage: InputImage) {
        textRecognizer.process(inputImage)
            .addOnSuccessListener { text ->
                val receiptData = extractReceiptData(text.text)
                if (receiptData.isNotEmpty()) {
                    onReceiptResult(receiptData)
                    copyToClipboard(context, "Receipt Data", receiptData)
                    stopScanning()
                } else {
                    onFailure("Invalid receipt data", null)
                }
            }
            .addOnFailureListener { exception ->
                onFailure("Text recognition failed", exception)
            }
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun startScanning() {
        if (isScanning) return
        isScanning = true

        imageAnalysis?.setAnalyzer(executor) { imageProxy ->
            processImage(imageProxy)
        }
    }

    private fun stopScanning() {
        isScanning = false
        imageAnalysis?.clearAnalyzer()
    }

    private fun onFailure(message: String, exception: Exception?) {
        val errorMessage = exception?.localizedMessage ?: "Unknown error"
        showLogError("CameraProcessor", "$message: $errorMessage")
        showToast(message)
        stopScanning()
    }

    //Extract Qty,Product and Price
    private fun extractReceiptData(text: String): String {
        val totalRegex = Regex("(Total|TOTAL|total)\\s*[:\\-]?\\s*\\d+(\\.\\d{2})?")
        val productLineRegex = Regex("([a-zA-Z]+(?:\\s[a-zA-Z]+)*)\\s+\\d+\\s+\\d+(\\.\\d{2})?")

        val total = totalRegex.find(text)?.value ?: "Total: Not Found"
        val products = productLineRegex.findAll(text).joinToString("\n") { it.value }

        return if (products.isNotEmpty()) "Products:\n$products\n$total" else ""
    }
}
