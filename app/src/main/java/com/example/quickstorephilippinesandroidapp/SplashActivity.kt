// File: SplashActivity.kt
package com.example.quickstorephilippinesandroidapp

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import models.DeviceRequest
import models.DeviceResponse
import models.DeviceInfoResponse
import api.ApiClient
import network.DeviceService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.provider.Settings
import android.os.Build
import android.view.View

class SplashActivity : AppCompatActivity() {

    private val MAX_WAIT_TIME: Long = 12000 // 12 seconds max
    private var isDataLoaded = false

    // Beautiful UI Views
    private lateinit var statusTextView: TextView
    private lateinit var appNameTextView: TextView
    private lateinit var subtitleTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var logoImageView: ImageView
    private lateinit var logoCard: CardView
    private lateinit var progressContainer: LinearLayout
    private lateinit var backgroundCircle1: View
    private lateinit var backgroundCircle2: View
    private lateinit var progressDots: Array<View>

    private var currentDot = 0
    private var progressAnimationHandler = Handler(Looper.getMainLooper())
    private var progressAnimationRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide status bar for immersive experience
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Initialize views
        initViews()

        // Start beautiful animations
        startEntranceAnimations()

        // Start device setup with delay for smooth visuals
        Handler(Looper.getMainLooper()).postDelayed({
            setupDevice()
        }, 1000) // Allow entrance animations to start

        // Safety fallback
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isDataLoaded) {
                Log.w("SplashActivity", "⏰ Timeout after $MAX_WAIT_TIME ms")
                showError("App took too long to initialize.")
            }
        }, MAX_WAIT_TIME)
    }

    private fun initViews() {
        statusTextView = findViewById(R.id.statusTextView)
        appNameTextView = findViewById(R.id.appNameTextView)
        subtitleTextView = findViewById(R.id.subtitleTextView)
        progressBar = findViewById(R.id.progressBar)
        logoImageView = findViewById(R.id.logoImageView)
        logoCard = findViewById(R.id.logoCard)
        progressContainer = findViewById(R.id.progressContainer)
        backgroundCircle1 = findViewById(R.id.backgroundCircle1)
        backgroundCircle2 = findViewById(R.id.backgroundCircle2)

        progressDots = arrayOf(
            findViewById(R.id.progressDot1),
            findViewById(R.id.progressDot2),
            findViewById(R.id.progressDot3),
            findViewById(R.id.progressDot4)
        )
    }

    private fun startEntranceAnimations() {
        // Logo entrance animation
        logoCard.apply {
            scaleX = 0f
            scaleY = 0f
            alpha = 0f
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(800)
                .setInterpolator(DecelerateInterpolator())
                .setStartDelay(300)
                .start()
        }

        // App name animation
        appNameTextView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(DecelerateInterpolator())
            .setStartDelay(800)
            .start()

        // Subtitle animation
        subtitleTextView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(DecelerateInterpolator())
            .setStartDelay(1000)
            .start()

        // Progress container animation
        progressContainer.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(1400)
            .withEndAction { startProgressAnimation() }
            .start()

        // Status text animation
        statusTextView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(1600)
            .start()

        // Background circles animation
        startBackgroundAnimation()
    }

    private fun startProgressAnimation() {
        progressAnimationRunnable = object : Runnable {
            override fun run() {
                // Reset all dots
                progressDots.forEach { dot ->
                    dot.animate()
                        .alpha(0.3f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }

                // Animate current dot
                progressDots[currentDot].animate()
                    .alpha(1f)
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(200)
                    .start()

                currentDot = (currentDot + 1) % progressDots.size

                // Continue animation
                progressAnimationHandler.postDelayed(this, 500)
            }
        }
        progressAnimationRunnable?.let { progressAnimationHandler.post(it) }
    }

    private fun startBackgroundAnimation() {
        // Rotate background circles
        val rotation1 = ObjectAnimator.ofFloat(backgroundCircle1, "rotation", 0f, 360f)
        rotation1.duration = 20000
        rotation1.repeatCount = ObjectAnimator.INFINITE
        rotation1.interpolator = LinearInterpolator()
        rotation1.start()

        val rotation2 = ObjectAnimator.ofFloat(backgroundCircle2, "rotation", 0f, -360f)
        rotation2.duration = 15000
        rotation2.repeatCount = ObjectAnimator.INFINITE
        rotation2.interpolator = LinearInterpolator()
        rotation2.start()

        // Subtle scale animation for circles using ObjectAnimator
        val scaleX1 = ObjectAnimator.ofFloat(backgroundCircle1, "scaleX", 1f, 1.1f)
        scaleX1.duration = 3000
        scaleX1.repeatCount = ObjectAnimator.INFINITE
        scaleX1.repeatMode = ObjectAnimator.REVERSE
        scaleX1.start()

        val scaleY1 = ObjectAnimator.ofFloat(backgroundCircle1, "scaleY", 1f, 1.1f)
        scaleY1.duration = 3000
        scaleY1.repeatCount = ObjectAnimator.INFINITE
        scaleY1.repeatMode = ObjectAnimator.REVERSE
        scaleY1.start()

        val scaleX2 = ObjectAnimator.ofFloat(backgroundCircle2, "scaleX", 1f, 0.9f)
        scaleX2.duration = 2500
        scaleX2.repeatCount = ObjectAnimator.INFINITE
        scaleX2.repeatMode = ObjectAnimator.REVERSE
        scaleX2.start()

        val scaleY2 = ObjectAnimator.ofFloat(backgroundCircle2, "scaleY", 1f, 0.9f)
        scaleY2.duration = 2500
        scaleY2.repeatCount = ObjectAnimator.INFINITE
        scaleY2.repeatMode = ObjectAnimator.REVERSE
        scaleY2.start()
    }

    private fun updateStatus(message: String) {
        runOnUiThread {
            statusTextView.text = message
            // Add subtle animation when status changes
            statusTextView.animate()
                .alpha(0.7f)
                .setDuration(100)
                .withEndAction {
                    statusTextView.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                }
                .start()
        }
    }

    private fun setupDevice() {
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        // Save globally
        MainActivity.DEVICE_ID = deviceId
        Log.d("SplashActivity", "📱 Device ID: $deviceId")

        val request = DeviceRequest(
            device_id = deviceId,
            manufacturer = Build.MANUFACTURER ?: "Unknown",
            model = Build.MODEL ?: "Unknown",
            android_version = Build.VERSION.RELEASE ?: "Unknown",
            locker_id = null
        )

        val service = ApiClient.instance.create(DeviceService::class.java)

        // Step 1: Register Device
        updateStatus("Registering device...")
        Log.d("SplashActivity", "📡 Registering device: $deviceId")

        service.registerDevice(request).enqueue(object : Callback<DeviceResponse> {
            override fun onResponse(call: Call<DeviceResponse>, response: Response<DeviceResponse>) {
                if (response.isSuccessful) {
                    Log.d("SplashActivity", "✅ Registration success: ${response.body()?.message}")
                    updateStatus("Device registered successfully")
                } else {
                    Log.w("SplashActivity", "⚠️ Registration failed: ${response.code()}")
                    updateStatus("Registration skipped, continuing...")
                }
                // Proceed to fetch device info
                Handler(Looper.getMainLooper()).postDelayed({
                    fetchDeviceInfo(deviceId)
                }, 800)
            }

            override fun onFailure(call: Call<DeviceResponse>, t: Throwable) {
                Log.e("SplashActivity", "❌ Register failed: ${t.message}", t)
                updateStatus("Connection issue, retrying...")
                Handler(Looper.getMainLooper()).postDelayed({
                    fetchDeviceInfo(deviceId)
                }, 1000)
            }
        })
    }

    private fun fetchDeviceInfo(deviceId: String) {
        updateStatus("Loading device configuration...")
        Log.d("SplashActivity", "🔍 Fetching device info for $deviceId")

        val service = ApiClient.instance.create(DeviceService::class.java)

        service.getDeviceInfo(deviceId).enqueue(object : Callback<DeviceInfoResponse> {
            override fun onResponse(call: Call<DeviceInfoResponse>, response: Response<DeviceInfoResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val clientId = body?.deviceInfo?.client_id

                    if (!clientId.isNullOrEmpty()) {
                        MainActivity.CLIENT_ID = clientId
                        Log.d("SplashActivity", "🔑 CLIENT_ID fetched: $clientId")
                        updateStatus("Configuration complete!")

                        // Add slight delay for user to see success message
                        Handler(Looper.getMainLooper()).postDelayed({
                            updateStatus("Launching QuickStore...")
                            Handler(Looper.getMainLooper()).postDelayed({
                                proceedToMainActivity()
                            }, 500)
                        }, 800)
                    } else {
                        Log.e("SplashActivity", "❌ client_id is null or empty")
                        showError("Device not configured properly.\nPlease contact administrator.")
                    }
                } else {
                    val errorBody = try {
                        response.errorBody()?.string()
                    } catch (e: Exception) {
                        "Unknown error"
                    }
                    Log.e("SplashActivity", "❌ getDeviceInfo failed: ${response.code()} | $errorBody")
                    showError("Failed to load device configuration.\nServer returned error ${response.code()}.")
                }
            }

            override fun onFailure(call: Call<DeviceInfoResponse>, t: Throwable) {
                Log.e("SplashActivity", "🌐 Network error: ${t.message}", t)
                showError("No internet connection detected.\nPlease check your network and try again.")
            }
        })
    }

    private fun proceedToMainActivity() {
        if (isDataLoaded) return
        isDataLoaded = true

        runOnUiThread {
            Log.d("SplashActivity", "🚀 Launching MainActivity")

            // Stop progress animation
            progressAnimationRunnable?.let { progressAnimationHandler.removeCallbacks(it) }

            // Fade out animation before transitioning
            logoCard.animate()
                .alpha(0.8f)
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(300)
                .withEndAction {
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
                .start()
        }
    }

    private fun showError(message: String) {
        if (isDataLoaded) return
        isDataLoaded = true

        runOnUiThread {
            // Stop progress animation
            progressAnimationRunnable?.let { progressAnimationHandler.removeCallbacks(it) }

            // Hide progress, show error state
            progressContainer.animate()
                .alpha(0f)
                .setDuration(300)
                .start()

            progressBar.visibility = ProgressBar.VISIBLE
            progressBar.animate()
                .alpha(0f)
                .setDuration(300)
                .start()

            updateStatus("Connection failed")

            AlertDialog.Builder(this)
                .setTitle("QuickStore Setup Failed")
                .setMessage(message)
                .setPositiveButton("Retry") { _, _ ->
                    recreate() // restart splash
                }
                .setNegativeButton("Exit") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progressAnimationRunnable?.let { progressAnimationHandler.removeCallbacks(it) }
    }
}