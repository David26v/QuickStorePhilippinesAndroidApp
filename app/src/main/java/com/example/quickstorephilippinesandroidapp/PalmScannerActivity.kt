package com.example.quickstorephilippinesandroidapp

import android.content.Intent
import android.graphics.Camera
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.TextureView
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import camera.PreviewCamera
import com.google.android.material.snackbar.Snackbar
import com.tendcent.palm.JXPalmSdk
import com.veinauthen.palm.JXImage
import com.veinauthen.palm.JXPalmConfig
import com.veinauthen.tool.JXPalmCaptureListener
import com.veinauthen.tool.JXPalmMatchListener
import com.veinauthen.tool.JXPalmTool
import android.view.ViewGroup

class PalmScannerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LOCKER_ID = "locker_id"
        const val EXTRA_ACTION = "action"
        const val RESULT_SUCCESS = "success"
        const val RESULT_CANCELLED = "cancelled"
        const val RESULT_FAILED = "failed"

        const val DEFAULT_GROUP = "PALM_VEIN"
    }

    private lateinit var palmStatusIndicator: View
    private lateinit var palmStatusText: TextView
    private lateinit var instructionText: TextView
    private lateinit var cancelButton: Button
    private lateinit var retryButton: Button
    private lateinit var backButton: View
    private lateinit var previewTextureView: TextureView

    private val mainHandler = Handler(Looper.getMainLooper())
    private val previewCamera = PreviewCamera()

    private var lockerId: Int = -1
    private var action: String = ""
    private var isScanning = false
    private var isMatchSuccessful = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        setContentView(R.layout.activity_palm_scanner)

        lockerId = intent.getIntExtra(EXTRA_LOCKER_ID, -1)
        action = intent.getStringExtra(EXTRA_ACTION) ?: ""

        if (lockerId == -1) {
            showError("Invalid locker ID")
            return
        }

        initViews()
        setupListeners()
        initPalmSdk()
    }

    private fun setupFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun initViews() {
        palmStatusIndicator = findViewById(R.id.palm_status_indicator)
        palmStatusText = findViewById(R.id.palm_status_text)
        instructionText = findViewById(R.id.palm_instruction_text)
        cancelButton = findViewById(R.id.button_cancel)
        retryButton = findViewById(R.id.button_retry)
        backButton = findViewById(R.id.button_back)

        previewTextureView = TextureView(this)

        // Fix: Ensure camera_container is a ViewGroup
        val container = findViewById<ViewGroup>(R.id.camera_container)
        container.addView(previewTextureView, 0)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { finishWithResult(RESULT_CANCELLED) }
        cancelButton.setOnClickListener { finishWithResult(RESULT_CANCELLED) }
        retryButton.setOnClickListener {
            retryButton.isEnabled = false
            startPalmDetection()
        }
    }

    private fun initPalmSdk() {
        mainHandler.post {
            JXPalmConfig.setMinEnrollSize(150)
            JXPalmConfig.setIrLivenessEnable(true)
            JXPalmConfig.setVerticalFlip(true)
            JXPalmConfig.setHorizonFlip(false)

            Thread {
                try {
                    val result = JXPalmSdk.getInstance().initializeSDK(this)
                    runOnUiThread {
                        if (result == 0) {
                            instructionText.text = "Starting scanner..."
                            JXPalmTool.getPalmInstance().initGroup(DEFAULT_GROUP)
                            JXPalmTool.getPalmInstance().startWork()
                            JXPalmTool.getPalmInstance().setMatchListener(matchListener)
                            startCameraAndScan()
                        } else if (result == 10006) {
                            showError("SDK init failed: $result (Check device compatibility or other apps using camera)")
                        } else {
                            showError("SDK init failed: $result")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread { showError("SDK init error: ${e.message}") }
                }
            }.start()
        }
    }


    private fun startCameraAndScan() {
        previewCamera.setMirror(false)
        previewCamera.setRotate(90)
        previewCamera.startCamera(previewTextureView, 1, 640, 480, object : PreviewCamera.ICallback {
            override fun onSucc(camera: android.hardware.Camera) {
                // Start scanning only when camera is ready
                startPalmDetection()
            }

            override fun onData(data: ByteArray, camera: android.hardware.Camera) {
                val jxImage = JXPalmTool.getPalmInstance().yuvToJXImage(data, 640, 480)
                JXPalmTool.getPalmInstance().addDetectImage(jxImage)
                jxImage.recycle()
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                runOnUiThread { showError("Camera error: ${e.message}") }
            }
        })
    }

    private fun startPalmDetection() {
        if (isScanning || isMatchSuccessful) return
        isScanning = true

        palmStatusIndicator.setBackgroundResource(R.drawable.status_indicator_detecting)
        palmStatusText.text = "Scanning..."
        instructionText.text = "Place your palm steadily"

        JXPalmTool.getPalmInstance().startCapture(captureListener)
    }

    private val captureListener = object : JXPalmCaptureListener {
        override fun onDistance(distance: Int) {}
        override fun onCapture(code: Int, jxImage: JXImage?) {
            jxImage?.recycle()
        }
    }

    private val matchListener = object : JXPalmMatchListener {
        override fun onMatch(code: Int, feature: ByteArray?, jxImage: JXImage?) {
            if (code == 0 && feature != null && !isMatchSuccessful) {
                runOnUiThread {
                    val idRet = arrayOfNulls<String>(1)
                    val score = JXPalmTool.getPalmInstance().palmMatch(DEFAULT_GROUP, feature, idRet)

                    if (score >= 65) {
                        isMatchSuccessful = true
                        onPalmVerified(idRet[0] ?: "Unknown", score)
                    }
                }
            }
            jxImage?.recycle()
        }

        override fun onDetected(code: Int, rect: IntArray?, jxImage: JXImage?) {}
        override fun onLiveness(code: Int, value: Float) {}
    }

    private fun onPalmVerified(userId: String, score: Int) {
        isScanning = false
        JXPalmTool.getPalmInstance().stopCapture()

        palmStatusIndicator.setBackgroundResource(R.drawable.status_indicator_confirmed)
        palmStatusText.text = "Verified!"
        instructionText.text = "Access granted\nID: $userId\nScore: $score"

        mainHandler.postDelayed({ finishWithResult(RESULT_SUCCESS) }, 1500)
    }

    private fun showError(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
            .setAction("OK") { finish() }
            .show()
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

    override fun onPause() {
        super.onPause()
        previewCamera.stopCamera()
        JXPalmTool.getPalmInstance().stopCapture()
    }

    override fun onResume() {
        super.onResume()
        if (!isMatchSuccessful) startCameraAndScan()
    }

    override fun onDestroy() {
        super.onDestroy()
        JXPalmTool.getPalmInstance().stopWork()
        JXPalmTool.getPalmInstance().stopCapture()
        JXPalmTool.getPalmInstance().setMatchListener(null)
        previewCamera.stopCamera()
        mainHandler.removeCallbacksAndMessages(null)
    }

    override fun onBackPressed() {
        finishWithResult(RESULT_CANCELLED)
    }
}
