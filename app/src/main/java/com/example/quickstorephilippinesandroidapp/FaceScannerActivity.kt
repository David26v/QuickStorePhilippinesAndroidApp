package com.example.quickstorephilippinesandroidapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// ML Kit imports
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.common.util.concurrent.ListenableFuture

class FaceScannerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LOCKER_ID = "locker_id"
        const val EXTRA_ACTION = "action"
        const val RESULT_SUCCESS = "success"
        const val RESULT_CANCELLED = "cancelled"
        const val RESULT_FAILED = "failed"
    }

    private lateinit var faceStatusIndicator: View
    private lateinit var faceStatusText: TextView
    private lateinit var instructionText: TextView
    private lateinit var cancelButton: Button
    private lateinit var retryButton: Button
    private lateinit var backButton: View
    private lateinit var scanningLine: View
    private lateinit var faceDetectionFrame: View

    private var lockerId: Int = -1
    private var action: String = ""

    private var isFaceDetected = false
    private var isScanning = false

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupFullScreen()
        setContentView(R.layout.activity_face_scanner)

        // Get intent extras
        lockerId = intent.getIntExtra(EXTRA_LOCKER_ID, -1)
        action = intent.getStringExtra(EXTRA_ACTION) ?: ""

        if (lockerId == -1) {
            Snackbar.make(findViewById(android.R.id.content), "Invalid locker ID", Snackbar.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupClickListeners()

        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
    }

    override fun onResume() {
        super.onResume()
        Log.d("FaceScanner", "onResume: Attempting to start camera")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun setupFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun initializeViews() {
        faceStatusIndicator = findViewById(R.id.face_status_indicator)
        faceStatusText = findViewById(R.id.face_status_text)
        instructionText = findViewById(R.id.face_instruction_text)
        cancelButton = findViewById(R.id.button_cancel)
        retryButton = findViewById(R.id.button_retry)
        backButton = findViewById(R.id.button_back)
        scanningLine = findViewById(R.id.scanning_line)
        faceDetectionFrame = findViewById(R.id.face_detection_frame)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener { finishWithResult(RESULT_CANCELLED) }
        cancelButton.setOnClickListener { finishWithResult(RESULT_CANCELLED) }
        retryButton.setOnClickListener { resetAndRestart() }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                1001
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Camera access is required", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Grant") { requestCameraPermission() }
                    .show()
            }
        }
    }

    private fun startCamera() {
        Log.d("FaceScanner", "startCamera: Initializing CameraX")

        val previewView = findViewById<PreviewView>(R.id.preview_view)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                    previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                // Image Analysis for Face Detection
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val rotation = imageProxy.imageInfo.rotationDegrees
                        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

                        val faceDetector = FaceDetection.getClient()

                        faceDetector.process(inputImage)
                            .addOnSuccessListener { faces: List<Face> ->
                                if (faces.isNotEmpty() && isScanning && !isFaceDetected) {
                                    val face = faces[0]
                                    if (isFaceInsideFrame(face.boundingBox)) {
                                        isFaceDetected = true
                                        onFaceDetected()
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("FaceScanner", "Face detection failed", e)
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

                startFaceDetection()

                Log.d("FaceScanner", "CameraX use cases bound successfully")

            } catch (e: Exception) {
                Log.e("FaceScanner", "Use case binding failed", e)
                onFaceDetectionFailed()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun isFaceInsideFrame(faceRect: Rect): Boolean {
        val frameRect = Rect()
        faceDetectionFrame.getGlobalVisibleRect(frameRect)

        val displayFrame = Rect(frameRect)

        val viewFinder = findViewById<View>(R.id.preview_view)
        val previewWidth = viewFinder.width.takeIf { it > 0 } ?: 640
        val previewHeight = viewFinder.height.takeIf { it > 0 } ?: 480

        val scaleX = previewWidth.toFloat() / 640f
        val scaleY = previewHeight.toFloat() / 480f

        val mappedLeft = (faceRect.left * scaleX).toInt()
        val mappedTop = (faceRect.top * scaleY).toInt()
        val mappedRight = (faceRect.right * scaleX).toInt()
        val mappedBottom = (faceRect.bottom * scaleY).toInt()

        val faceScreenRect = Rect(mappedLeft, mappedTop, mappedRight, mappedBottom)

        return displayFrame.contains(faceScreenRect)
    }

    private fun startFaceDetection() {
        if (isScanning) return
        isScanning = true

        faceStatusIndicator.setBackgroundResource(R.drawable.status_indicator_detecting)
        faceStatusText.text = "Detecting Face..."
        instructionText.text = "Position your face within the frame\nKeep your head steady and look at the camera"
        retryButton.isEnabled = false

        scanningLine.visibility = View.VISIBLE
        startScanningAnimation()
    }

    private fun startScanningAnimation() {
        val animate = object : Runnable {
            var direction = 1f
            var currentY = 0f
            val maxY = 150f
            val speed = 6f

            override fun run() {
                if (!isScanning) {
                    scanningLine.visibility = View.GONE
                    return
                }

                currentY += direction * speed
                if (currentY > maxY) {
                    currentY = maxY
                    direction = -1f
                } else if (currentY < -maxY) {
                    currentY = -maxY
                    direction = 1f
                }

                scanningLine.translationY = currentY
                mainHandler.postDelayed(this, 40)
            }
        }
        mainHandler.post(animate)
    }

    private fun onFaceDetected() {
        isScanning = false
        scanningLine.visibility = View.GONE

        faceStatusIndicator.setBackgroundResource(R.drawable.status_indicator_confirmed)
        faceStatusText.text = "Face Recognized!"
        instructionText.text = "Authentication successful!\nProcessing..."

        mainHandler.postDelayed({
            finishWithResult(RESULT_SUCCESS)
        }, 1500)
    }

    private fun onFaceDetectionFailed() {
        isScanning = false
        scanningLine.visibility = View.GONE

        faceStatusIndicator.setBackgroundResource(R.drawable.status_indicator_error)
        faceStatusText.text = "Face Not Recognized"
        instructionText.text = "Unable to detect face clearly\nPlease ensure good lighting and try again"
        retryButton.isEnabled = true

        Snackbar.make(findViewById(android.R.id.content), "Face detection failed. Please try again.", Snackbar.LENGTH_LONG).show()
    }

    private fun resetAndRestart() {
        isFaceDetected = false
        isScanning = false
        mainHandler.removeCallbacksAndMessages(null)

        scanningLine.visibility = View.GONE
        retryButton.isEnabled = false

        startFaceDetection()
    }

    private fun finishWithResult(result: String) {
        val resultIntent = Intent().apply {
            putExtra("result", result)
            putExtra("locker_id", lockerId)
            putExtra("action", action)
        }

        setResult(
            if (result == RESULT_SUCCESS) RESULT_OK else RESULT_CANCELED,
            resultIntent
        )
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        mainHandler.removeCallbacksAndMessages(null)
    }

    override fun onBackPressed() {
        finishWithResult(RESULT_CANCELLED)
    }
}