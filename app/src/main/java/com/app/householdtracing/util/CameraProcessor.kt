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
        BarcodeScannerOptions.Builder().build()
    )
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())
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
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
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
        if (!isScanning) {
            imageProxy.close()
            return
        }

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
                        // If barcode not found, try text recognition
                        processTextRecognition(inputImage, imageProxy)
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure("Barcode scanning failed", exception, imageProxy)
                    processTextRecognition(inputImage, imageProxy)
                }
                .addOnCompleteListener {
                    //imageProxy.close()
                }
        } else {
            onFailure("Media image is null", null, imageProxy)
        }
    }

//    private fun processTextRecognition(inputImage: InputImage, imageProxy: ImageProxy) {
//        textRecognizer.process(inputImage)
//            .addOnSuccessListener { text ->
//                val receiptData = text.textBlocks.toString()//extractReceiptData(text.text)
//                if (receiptData.isNotEmpty()) {
//                    onReceiptResult(receiptData)
//                    copyToClipboard(context, "Receipt Data", receiptData)
//                    stopScanning()
//                } else {
//                    onFailure("Invalid receipt data", null, imageProxy)
//                }
//            }
//            .addOnFailureListener { exception ->
//                onFailure("Text recognition failed", exception, imageProxy)
//            }
//            .addOnCompleteListener {
//                imageProxy.close()
//            }
//    }

    /*private fun processTextRecognition(inputImage: InputImage, imageProxy: ImageProxy) {
        textRecognizer.process(inputImage)
            .addOnSuccessListener { text ->
                val allLines = text.textBlocks.flatMap { it.lines }.map { it.text.trim() }

                // Process lines and extract products
                val receipt = processReceipt(allLines)

                if (receipt.products.isNotEmpty()) {
                    val receiptData = formatReceiptData(receipt)
                    onReceiptResult(receiptData)
                    copyToClipboard(context, "Receipt Data", receiptData)
                    stopScanning()
                } else {
                    onFailure("No valid receipt data detected.", null, imageProxy)
                }
            }
            .addOnFailureListener { exception ->
                onFailure("Text recognition failed.", exception, imageProxy)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }*/

    private fun processTextRecognition(inputImage: InputImage, imageProxy: ImageProxy) {
        textRecognizer.process(inputImage)
            .addOnSuccessListener { text ->
                val allLines = text.textBlocks.flatMap { it.lines }.map { it.text.trim() }

                // Process lines and extract products
                val receipt = processReceipt(allLines)

                if (receipt.items.isNotEmpty()) {
                    val receiptData =
                        formatReceiptData(receipt) // Format as needed for UI or display
                    onReceiptResult(receiptData) // Pass receipt data to UI or further processing
                    copyToClipboard(
                        context,
                        "Receipt Data",
                        receiptData
                    ) // Optional: Copy to clipboard
                    stopScanning() // Stop scanning after success
                } else {
                    onFailure("No valid receipt data detected.", null, imageProxy) // Handle failure
                }
            }
            .addOnFailureListener { exception ->
                onFailure(
                    "Text recognition failed.",
                    exception,
                    imageProxy
                ) // Handle text recognition failure
            }
            .addOnCompleteListener {
                imageProxy.close() // Ensure image proxy is closed
            }
    }

    data class ReceiptItem(val name: String, val quantity: Int)
    data class ReceiptResult(val items: List<ReceiptItem>, val totalAmount: Double)

    private fun processReceipt(lines: List<String>): ReceiptResult {
        val items = mutableListOf<ReceiptItem>()
        var paidAmount: Double? = null

        // Patterns for matching quantity and paid amount
        val quantityPattern = "\\b\\d+\\b".toRegex()
        val paidAmountPattern = "(?i)(paid amount|total amount|amount)".toRegex()
        val numberPattern = "\\d+(,\\d{3})*(\\.\\d{2})?".toRegex()

        for (line in lines) {
            if (paidAmountPattern.containsMatchIn(line)) {
                // Extract the paid amount
                paidAmount = numberPattern.find(line)?.value?.replace(",", "")?.toDoubleOrNull()
            } else {
                // Extract item data (name and quantity)
                val words = line.split("\\s+".toRegex())
                val quantityMatch = words.findLast { it.matches(quantityPattern) }
                val quantity = quantityMatch?.toIntOrNull()

                if (quantity != null) {
                    // Extract item name by taking everything before the quantity
                    val quantityIndex = words.indexOf(quantityMatch)
                    val name = words.subList(0, quantityIndex).joinToString(" ").trim()
                    items.add(ReceiptItem(name = name, quantity = quantity))
                }
            }
        }

        return ReceiptResult(
            items = items,
            totalAmount = paidAmount ?: 0.0 // Store paid amount in totalPrice for consistency
        )
    }

    private fun formatReceiptData(receipt: ReceiptResult): String {
        val productsFormatted = receipt.items.joinToString("\n") { product ->
            "Item: ${product.name}, Qty: ${product.quantity}"
        }
        val totalFormatted =
            "Total Price: ${String.format("%.2f", receipt.totalAmount)}"

        return """
        $productsFormatted
        
        $totalFormatted
    """.trimIndent()
    }


    ////////////////////////////////////////////////////////

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

    private fun onFailure(message: String, exception: Exception?, imageProxy: ImageProxy) {
        val errorMessage = exception?.localizedMessage ?: "Unknown error"
        showLogError("CameraProcessor", "$message: $errorMessage")
        showToast(message)
        imageProxy.close()
        stopScanning()
    }

    //Extract Qty,Product and Price
//    private fun extractReceiptData(receiptText: String): String {
//        // Normalize the text for better matching
//        val normalizedText = receiptText
//            .replace("l", "1")
//            .replace("O", "0")
//            .replace(",", "")
//            .lowercase()
//            .trim()
//
//        // Regex pattern to match product entries: name, quantity, and price
//        val productPattern = Regex("(.+?)\\s+(\\d+(?:\\.\\d+)?)\\s+(\\d+(?:\\.\\d+)?)")
//        // Regex pattern to match the total price
//        val totalPattern = Regex("(?i)(total|grand total|net amount)[:\\s]*([0-9]+\\.?[0-9]*)")
//
//        // Lists to store extracted products and values
//        val products = mutableListOf<String>()
//        var totalItems = 0
//        var totalQuantity = 0.0
//        var totalPrice = 0.0
//
//        // Extract products using the product regex pattern
//        productPattern.findAll(normalizedText).forEach { match ->
//            val (product, quantity, price) = match.destructured
//            products.add("Product: ${product.capitalize()}, Qty: $quantity, Price: $price")
//            totalItems += 1
//            totalQuantity += quantity.toDoubleOrNull() ?: 0.0
//            totalPrice += price.toDoubleOrNull() ?: 0.0
//        }
//
//        // Extract the grand total if available
//        val grandTotal =
//            totalPattern.find(normalizedText)?.groupValues?.get(2)?.toDoubleOrNull() ?: totalPrice
//
//        // Build the final result string
//        val result = StringBuilder()
//        if (products.isNotEmpty()) {
//            result.append(products.joinToString("\n"))
//            result.append("\nTotal Items: $totalItems")
//            result.append("\nTotal Quantity: $totalQuantity")
//            result.append("\nTotal Price: $grandTotal")
//        } else {
//            result.append("No valid products found.\n")
//            result.append("Total Price: $grandTotal")
//        }
//
//        return result.toString()
//    }

}
