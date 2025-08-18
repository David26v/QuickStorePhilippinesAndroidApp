package com.example.quickstorephilippinesandroidapp.ui.admin

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

import com.example.quickstorephilippinesandroidapp.R
// ML Kit imports
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException

class FaceRegistrationActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_LOCKER_ID = "locker_id"
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

    private var userId: String? = null
    private var lockerId: Int = -1

    private var isFaceDetected = false
    private var isScanning = false

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupFullScreen()
        setContentView(R.layout.activity_face_scanner)

        userId = intent.getStringExtra(EXTRA_USER_ID)
        lockerId = intent.getIntExtra(EXTRA_LOCKER_ID, -1)

        if (userId.isNullOrEmpty() || lockerId == -1) {
            Snackbar.make(findViewById(android.R.id.content), "Invalid user or locker", Snackbar.LENGTH_SHORT).show()
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
        Log.d("FaceRegistration", "onResume: Starting camera")
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
                Snackbar.make(findViewById(android.R.id.content), "Camera access required", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Grant") { requestCameraPermission() }
                    .show()
            }
        }
    }

    private fun startCamera() {
        Log.d("FaceRegistration", "startCamera: Initializing CameraX")

        val previewView = findViewById<PreviewView>(R.id.preview_view)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

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
                                        onFaceRegistered(faces[0])
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("FaceRegistration", "Face detection failed", e)
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

                startRegistration()

                Log.d("FaceRegistration", "CameraX bound successfully")

            } catch (e: InterruptedException) {
                Log.e("FaceRegistration", "CameraX binding interrupted", e)
                onRegistrationFailed()
            } catch (e: ExecutionException) {
                Log.e("FaceRegistration", "CameraX binding failed", e)
                onRegistrationFailed()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun isFaceInsideFrame(faceRect: Rect): Boolean {
        val frameRect = Rect()
        faceDetectionFrame.getGlobalVisibleRect(frameRect)

        val displayFrame = Rect(frameRect)

        val viewFinder = findViewById<PreviewView>(R.id.preview_view)
        val previewWidth = viewFinder.width.takeIf { it > 0 } ?: 640
        val previewHeight = viewFinder.height.takeIf { it > 0 } ?: 480

        // ML Kit typically assumes 640x480 input
        val scaleX = previewWidth.toFloat() / 640f
        val scaleY = previewHeight.toFloat() / 480f

        val mappedLeft = (faceRect.left * scaleX).toInt()
        val mappedTop = (faceRect.top * scaleY).toInt()
        val mappedRight = (faceRect.right * scaleX).toInt()
        val mappedBottom = (faceRect.bottom * scaleY).toInt()

        val faceScreenRect = Rect(mappedLeft, mappedTop, mappedRight, mappedBottom)

        return displayFrame.contains(faceScreenRect)
    }

    private fun startRegistration() {
        if (isScanning) return
        isScanning = true

        faceStatusIndicator.setBackgroundResource(R.drawable.status_indicator_detecting)
        faceStatusText.text = "Registering Face"
        instructionText.text = "Keep your face inside the frame\nStay still and look at the camera"
        retryButton.visibility = View.GONE

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
                if (currentY > maxY) direction = -1f
                if (currentY < -maxY) direction = 1f
                scanningLine.translationY = currentY
                mainHandler.postDelayed(this, 40)
            }
        }
        mainHandler.post(animate)
    }

    private fun onFaceRegistered(face: Face) {
        isScanning = false
        scanningLine.visibility = View.GONE

        faceStatusIndicator.setBackgroundResource(R.drawable.status_indicator_confirmed)
        faceStatusText.text = "Face Captured!"
        instructionText.text = "Saving face data..."



        mainHandler.postDelayed({
            finishWithResult(RESULT_SUCCESS)
        }, 1500)
    }

    private fun onRegistrationFailed() {
        isScanning = false
        scanningLine.visibility = View.GONE

        faceStatusIndicator.setBackgroundResource(R.drawable.status_indicator_error)
        faceStatusText.text = "Registration Failed"
        instructionText.text = "Could not capture face\nEnsure good lighting and try again"
        retryButton.visibility = View.VISIBLE
        retryButton.isEnabled = true
    }

    private fun resetAndRestart() {
        isFaceDetected = false
        isScanning = false
        mainHandler.removeCallbacksAndMessages(null)

        scanningLine.visibility = View.GONE
        retryButton.visibility = View.GONE
        retryButton.isEnabled = false

        startRegistration()
    }

    private fun finishWithResult(result: String) {
        Intent().apply {
            putExtra("result", result)
            putExtra("user_id", userId)
            putExtra("locker_id", lockerId)
        }.also { resultIntent ->
            setResult(if (result == RESULT_SUCCESS) RESULT_OK else RESULT_CANCELED, resultIntent)
            finish()
        }
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