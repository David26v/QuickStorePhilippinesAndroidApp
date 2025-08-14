package com.example.quickstorephilippinesandroidapp.ui.home

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
<<<<<<< Updated upstream
=======
import android.os.Handler
import android.os.Looper
import android.util.Log
>>>>>>> Stashed changes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.quickstorephilippinesandroidapp.R
import com.example.quickstorephilippinesandroidapp.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
<<<<<<< Updated upstream
import com.ys.rkapi.MyManager


data class Locker(
    val id: Int,
    val status: LockerStatus,
    val lastAccessTime: Long? = null,
    val assignedUser: AssignedUserInfo? = null,
    val location: String? = null
) {
    fun getStatusColor(): Int {
        return when (status) {
            LockerStatus.AVAILABLE -> Color.parseColor("#4CAF50") // Green
            LockerStatus.OCCUPIED -> Color.parseColor("#F44336") // Red
            LockerStatus.OVERDUE -> Color.parseColor("#FF9800") // Orange
        }
    }

    fun getStatusText(): String {
        return when (status) {
            LockerStatus.AVAILABLE -> "Available"
            LockerStatus.OCCUPIED -> "Occupied"
            LockerStatus.OVERDUE -> "Overdue"
        }
    }

    fun isAccessible(): Boolean {
        return true
    }
}


data class AssignedUserInfo(
    val userId: String,
    val firstName: String,
    val lastName: String
)


enum class LockerStatus {
    AVAILABLE,
    OCCUPIED,
    OVERDUE
}
=======
import java.util.concurrent.TimeUnit
import com.example.quickstorephilippinesandroidapp.FaceScannerActivity
import com.example.quickstorephilippinesandroidapp.PalmScannerActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.AlertDialog
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
>>>>>>> Stashed changes

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    private var currentInput = ""
    private var selectedLockerId: Int = -1
<<<<<<< Updated upstream


    private val lockerMap = mutableMapOf<Int, Locker>().apply {
        for (i in 1..24) {

            val status = when {
                i % 8 == 0 -> LockerStatus.OCCUPIED
                i == 7 || i == 15 || i == 23 -> LockerStatus.OVERDUE
                else -> LockerStatus.AVAILABLE
            }

            this[i] = Locker(
                id = i,
                status = status,
                lastAccessTime = if (status == LockerStatus.OCCUPIED || status == LockerStatus.OVERDUE) System.currentTimeMillis() - (10 + i % 5) * 60 * 1000
                else null,
                assignedUser = if (status == LockerStatus.OCCUPIED || status == LockerStatus.OVERDUE) AssignedUserInfo(
                    userId = "user_$i",
                    firstName = "First$i",
                    lastName = "Last$i"
                ) else null,
                location = "Row ${(i - 1) / 6 + 1}, Column ${(i - 1) % 6 + 1}"
            )
        }
=======
    private var lockerList: List<Locker> = emptyList()

    // Request codes for scanner activities
    companion object {
        private const val REQUEST_FACE_SCAN = 1001
        private const val REQUEST_PALM_SCAN = 1002
>>>>>>> Stashed changes
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
<<<<<<< Updated upstream
        setupLockerStatusGrid()
        return root
    }

    private fun setupLockerStatusGrid() {
        val grid = binding.lockerStatusGrid
        grid.removeAllViews()
        for (lockerNumber in 1..24) {
            val locker = lockerMap[lockerNumber] ?: continue
=======

        // Observe lockers
        homeViewModel.lockers.observe(viewLifecycleOwner) { lockers ->
            lockerList = lockers
            setupLockerStatusGrid()
        }

        // Listen for client ID
        MainActivity.onClientIdAvailable = {
            activity?.runOnUiThread {
                loadLockersWithClientId()
            }
        }

        // Try immediate load
        binding.root.post {
            loadLockersWithClientId()
        }

        return root
    }

    private fun loadLockersWithClientId() {
        val clientId = MainActivity.CLIENT_ID
        if (clientId != null && clientId.isNotEmpty()) {
            homeViewModel.loadLockers(clientId)
        } else {
            view?.postDelayed({
                val retryClientId = MainActivity.CLIENT_ID
                if (retryClientId != null && retryClientId.isNotEmpty()) {
                    homeViewModel.loadLockers(retryClientId)
                } else {
                    try {
                        Snackbar.make(binding.root, "Client ID not available. Please restart the app.", Snackbar.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Snackbar failed", e)
                    }
                }
            }, 1000)
        }
    }

    private fun setupLockerStatusGrid() {
        val grid = binding.lockerStatusGrid
        grid.removeAllViews()

        for (locker in lockerList) {
>>>>>>> Stashed changes
            val lockerButton = Button(requireContext()).apply {
                text = lockerNumber.toString()
                textSize = 18f

                val sizeInDp = 80
                val density = resources.displayMetrics.density
                val sizeInPixels = (sizeInDp * density).toInt()

                layoutParams = GridLayout.LayoutParams().apply {
                    this.width = sizeInPixels
                    this.height = sizeInPixels
                    (this as ViewGroup.MarginLayoutParams).setMargins(8, 8, 8, 8)
                }

                setBackgroundColor(locker.getStatusColor())
                setTextColor(Color.WHITE)
                isEnabled = locker.isAccessible()
                setOnClickListener {
                    selectLocker(lockerNumber)
                }
            }
            grid.addView(lockerButton)
        }
    }

    private fun selectLocker(lockerId: Int) {
        val locker = lockerMap[lockerId]
        if (locker == null) {
            Snackbar.make(binding.root, "Invalid locker selected", Snackbar.LENGTH_SHORT).show()
            return
        }
        selectedLockerId = lockerId
        showLockerSelectedDialog(locker)
    }


    private fun showLockerSelectedDialog(locker: Locker) {
        when (locker.status) {
            LockerStatus.AVAILABLE -> {
                val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_locker_selected, null)
                val lockerInfoText: TextView = dialogView.findViewById(R.id.dialog_locker_info)
                val accessCodeButton: Button = dialogView.findViewById(R.id.button_access_code)
                val qrCodeButton: Button = dialogView.findViewById(R.id.button_qr_code)
                val faceRecognitionButton: Button = dialogView.findViewById(R.id.button_face_recognition)
                val palmRecognitionButton: Button = dialogView.findViewById(R.id.button_palm_recognition)
                val cardReaderButton: Button = dialogView.findViewById(R.id.button_card_reader)

                lockerInfoText.text = "Locker #${locker.id}\nStatus: ${locker.getStatusText()}\nLocation: ${locker.location ?: "N/A"}"

<<<<<<< Updated upstream
                val dialog = AlertDialog.Builder(requireContext())
=======
                loadAndApplyAuthMethods(
                    accessCodeButton,
                    qrCodeButton,
                    faceRecognitionButton,
                    palmRecognitionButton,
                    cardReaderButton
                )

                val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
>>>>>>> Stashed changes
                    .setView(dialogView)
                    .create()

                accessCodeButton.setOnClickListener {
                    dialog.dismiss()
                    showAccessCodeDialog(locker, "assign")
                }
                qrCodeButton.setOnClickListener {
                    dialog.dismiss()
<<<<<<< Updated upstream
                    // TODO: Implement QR Code logic
                    Snackbar.make(binding.root, "QR Code access selected for Locker #${locker.id}", Snackbar.LENGTH_LONG).show()
                }
                faceRecognitionButton.setOnClickListener {
                    dialog.dismiss()
                    // TODO: Implement Face Recognition logic
                    Snackbar.make(binding.root, "Face Recognition selected for Locker #${locker.id}", Snackbar.LENGTH_LONG).show()
=======
                    showQRCodeDialog(locker, "assign")
                }
                faceRecognitionButton.setOnClickListener {
                    dialog.dismiss()
                    startFaceScan(locker, "assign")
>>>>>>> Stashed changes
                }
                palmRecognitionButton.setOnClickListener {
                    dialog.dismiss()
<<<<<<< Updated upstream
                    // TODO: Implement Fingerprint logic
                    Snackbar.make(binding.root, "Fingerprint Scanner selected for Locker #${locker.id}", Snackbar.LENGTH_LONG).show()
=======
                    startPalmScan(locker, "assign")
>>>>>>> Stashed changes
                }
                cardReaderButton.setOnClickListener {
                    dialog.dismiss()
                    Snackbar.make(binding.root, "💳 Waiting for card swipe...", Snackbar.LENGTH_LONG).show()
                    // TODO: Integrate with physical card reader later
                }

                dialog.show()
            }

            LockerStatus.OCCUPIED -> {
                showOccupiedLockerDialog(locker)
            }

            LockerStatus.OVERDUE -> {
                showOverdueLockerDialog(locker)
            }
        }
    }

<<<<<<< Updated upstream
    // --- New Function: Show Occupied Locker Dialog ---
=======
    private fun loadAndApplyAuthMethods(
        accessCodeButton: Button,
        qrCodeButton: Button,
        faceRecognitionButton: Button,
        palmRecognitionButton: Button,
        cardReaderButton: Button
    ) {
        val clientId = MainActivity.CLIENT_ID
        if (clientId != null) {
            homeViewModel.getClientAuthMethods(clientId) { authMethods ->
                activity?.runOnUiThread {
                    accessCodeButton.visibility = if (authMethods.contains("access_code")) View.VISIBLE else View.GONE
                    qrCodeButton.visibility = if (authMethods.contains("qr_code")) View.VISIBLE else View.GONE
                    faceRecognitionButton.visibility = if (authMethods.contains("face_recognition")) View.VISIBLE else View.GONE
                    palmRecognitionButton.visibility = if (authMethods.contains("palm_recognition")) View.VISIBLE else View.GONE
                    cardReaderButton.visibility = if (authMethods.contains("card_reader")) View.VISIBLE else View.GONE
                }
            }
        }
    }

>>>>>>> Stashed changes
    private fun showOccupiedLockerDialog(locker: Locker) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_locker_occupied, null)

        // Find views in the occupied layout
        val greetingText: TextView = dialogView.findViewById(R.id.dialog_occupied_greeting)
        val lockerInfoText: TextView = dialogView.findViewById(R.id.dialog_occupied_locker_info)
        val userIdText: TextView = dialogView.findViewById(R.id.dialog_occupied_user_id)
        val durationText: TextView = dialogView.findViewById(R.id.dialog_occupied_duration)
        val endSessionButton: Button = dialogView.findViewById(R.id.button_end_session)
        val pickupButton: Button = dialogView.findViewById(R.id.button_pickup)

<<<<<<< Updated upstream
        // --- Calculate Duration ---
        val durationString = locker.lastAccessTime?.let { accessTime ->
            val currentTime = System.currentTimeMillis()
            val durationMillis = currentTime - accessTime
            formatDuration(durationMillis)
        } ?: "Unknown"


=======
        val durationString = locker.lastAccessTime?.let { formatDuration(System.currentTimeMillis() - it) } ?: "Unknown"
>>>>>>> Stashed changes
        val userName = locker.assignedUser?.let { "${it.firstName} ${it.lastName}" } ?: "User"

        greetingText.text = "Hi $userName,"

        lockerInfoText.text = "Locker #${locker.id}"
        userIdText.text = "User ID: ${locker.assignedUser?.userId ?: "Unknown"}"
        durationText.text = durationString

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext()).setView(dialogView).create()

        endSessionButton.setOnClickListener {
            dialog.dismiss()
            showAccessCodeDialog(locker)
        }
        pickupButton.setOnClickListener {
            dialog.dismiss()
            showAccessCodeDialog(locker)
        }

        dialog.show()
    }

<<<<<<< Updated upstream
    // --- New Function: Show Overdue Locker Dialog ---
=======
    private fun startIconPulseAnimation(imageView: ImageView) {
        val pulseAnimation = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0.7f, 1f)
        pulseAnimation.duration = 2000
        pulseAnimation.repeatCount = ObjectAnimator.INFINITE
        pulseAnimation.start()
    }

>>>>>>> Stashed changes
    private fun showOverdueLockerDialog(locker: Locker) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_locker_overdue, null)

        // Bind Views
        val greetingText: TextView = dialogView.findViewById(R.id.dialog_overdue_greeting)
        val lockerInfoText: TextView = dialogView.findViewById(R.id.dialog_overdue_locker_info)
        val userIdText: TextView = dialogView.findViewById(R.id.dialog_overdue_user_id)
        val durationText: TextView = dialogView.findViewById(R.id.dialog_overdue_duration)
        val endSessionButton: MaterialButton = dialogView.findViewById(R.id.button_end_session)
        val pickupButton: MaterialButton = dialogView.findViewById(R.id.button_pickup)
        val messageText: TextView = dialogView.findViewById(R.id.dialog_overdue_message)
        val alertIcon: ImageView = dialogView.findViewById(R.id.alert_animation_icon)

<<<<<<< Updated upstream

        val overdueDurationString = locker.lastAccessTime?.let { accessTime ->
            val currentTime = System.currentTimeMillis()
            val overdueMillis = currentTime - accessTime
            formatDuration(overdueMillis)
        } ?: "Unknown"


=======
        // Format overdue duration
        val overdueDuration = locker.lastAccessTime?.let {
            formatDuration(System.currentTimeMillis() - it)
        } ?: "Unknown"

        // Get user name
>>>>>>> Stashed changes
        val userName = locker.assignedUser?.let { "${it.firstName} ${it.lastName}" } ?: "User"

        // Populate UI
        greetingText.text = "Hi $userName,"

        lockerInfoText.text = "Locker #${locker.id}"
        userIdText.text = "User ID: ${locker.assignedUser?.userId ?: "Unknown"}"
        durationText.text = overdueDuration
        messageText.text = "Your locker is overdue!"

        // Optional: Add pulsing animation to alert icon
        startPulseAnimation(alertIcon)

        // Create dialog
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Set button actions
        endSessionButton.setOnClickListener {
            dialog.dismiss()
            showAccessCodeDialog(locker)
        }

        pickupButton.setOnClickListener {
            dialog.dismiss()
<<<<<<< Updated upstream
            showAccessCodeDialog(locker)
=======
            showAuthMethodsForOccupiedLocker(locker, "pickup")
        }

        dialog.show()
    }

    private fun startPulseAnimation(view: View) {
        val pulse = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.1f)
        ).apply {
            duration = 600
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }
        pulse.start()
    }

    private fun showAuthMethodsForOccupiedLocker(locker: Locker, action: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_locker_selected, null)
        val lockerInfoText: TextView = dialogView.findViewById(R.id.dialog_locker_info)
        val accessCodeButton: Button = dialogView.findViewById(R.id.button_access_code)
        val qrCodeButton: Button = dialogView.findViewById(R.id.button_qr_code)
        val faceRecognitionButton: Button = dialogView.findViewById(R.id.button_face_recognition)
        val palmRecognitionButton: Button = dialogView.findViewById(R.id.button_palm_recognition)
        val cardReaderButton: Button = dialogView.findViewById(R.id.button_card_reader)

        // Hide action buttons not needed
        dialogView.findViewById<Button>(R.id.button_end_session)?.visibility = View.GONE
        dialogView.findViewById<Button>(R.id.button_pickup)?.visibility = View.GONE

        lockerInfoText.text = "Locker #${locker.id}\nStatus: ${locker.getStatusText()}\nLocation: ${locker.location ?: "N/A"}"

        loadAndApplyAuthMethods(
            accessCodeButton,
            qrCodeButton,
            faceRecognitionButton,
            palmRecognitionButton,
            cardReaderButton
        )

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext()).setView(dialogView).create()

        accessCodeButton.setOnClickListener {
            dialog.dismiss()
            showAccessCodeDialog(locker, action)
        }
        qrCodeButton.setOnClickListener {
            dialog.dismiss()
            showQRCodeDialog(locker, action)
        }
        faceRecognitionButton.setOnClickListener {
            dialog.dismiss()
            startFaceScan(locker, action)
        }
        palmRecognitionButton.setOnClickListener {
            dialog.dismiss()
            startPalmScan(locker, action)
        }
        cardReaderButton.setOnClickListener {
            dialog.dismiss()
            Snackbar.make(binding.root, "💳 Waiting for card swipe...", Snackbar.LENGTH_LONG).show()
            // Future: Trigger card reader service
        }

        dialog.show()
    }

    // 🔹 Start Face Scanner Only
    private fun startFaceScan(locker: Locker, action: String?) {
        selectedLockerId = locker.id
        currentInput = action ?: "assign"

        val intent = Intent(requireContext(), FaceScannerActivity::class.java).apply {
            putExtra(FaceScannerActivity.EXTRA_LOCKER_ID, locker.id)
            putExtra(FaceScannerActivity.EXTRA_ACTION, action)
        }
        startActivityForResult(intent, REQUEST_FACE_SCAN)
    }

    // 🔹 Start Palm Scanner Only
    private fun startPalmScan(locker: Locker, action: String?) {
        selectedLockerId = locker.id
        currentInput = action ?: "assign"

        val intent = Intent(requireContext(), PalmScannerActivity::class.java).apply {
            putExtra(PalmScannerActivity.EXTRA_LOCKER_ID, locker.id)
            putExtra(PalmScannerActivity.EXTRA_ACTION, action)
        }
        startActivityForResult(intent, REQUEST_PALM_SCAN)
    }

    // 🔁 Handle results independently
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_FACE_SCAN -> {
                if (resultCode == Activity.RESULT_OK) {
                    val lockerId = data?.getIntExtra("locker_id", selectedLockerId) ?: selectedLockerId
                    val action = data?.getStringExtra("action") ?: currentInput
                    Snackbar.make(binding.root, "✅ Face scan successful!", Snackbar.LENGTH_LONG).show()
                    processAuthentication(lockerId, action, "face")
                } else {
                    Snackbar.make(binding.root, "❌ Face scan failed or canceled", Snackbar.LENGTH_LONG).show()
                }
            }

            REQUEST_PALM_SCAN -> {
                if (resultCode == Activity.RESULT_OK) {
                    val lockerId = data?.getIntExtra("locker_id", selectedLockerId) ?: selectedLockerId
                    val action = data?.getStringExtra("action") ?: currentInput
                    Snackbar.make(binding.root, "✅ Palm scan successful!", Snackbar.LENGTH_LONG).show()
                    processAuthentication(lockerId, action, "palm")
                } else {
                    Snackbar.make(binding.root, "❌ Palm scan failed or canceled", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    // 🔹 Process authentication based on modality
    private fun processAuthentication(lockerId: Int, action: String?, method: String) {
        val locker = lockerList.find { it.id == lockerId }
        if (locker == null) {
            Snackbar.make(binding.root, "Locker not found", Snackbar.LENGTH_LONG).show()
            return
        }

        when (action) {
            "assign" -> {
                Snackbar.make(binding.root, "✅ Locker assigned via $method!", Snackbar.LENGTH_LONG).show()
            }
            "pickup" -> {
                Snackbar.make(binding.root, "🚪 Door opened for pickup via $method!", Snackbar.LENGTH_LONG).show()
            }
            "end_session" -> {
                Snackbar.make(binding.root, "🔚 Session ended via $method!", Snackbar.LENGTH_LONG).show()
            }
        }
        homeViewModel.refreshLockers()
    }

    private fun showQRCodeDialog(locker: Locker, action: String? = null) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_qr_code_scanner, null)
        val statusText: TextView = dialogView.findViewById(R.id.qr_scanning_status)
        val indicator: View = dialogView.findViewById(R.id.qr_status_indicator)
        val statusLabel: TextView = dialogView.findViewById(R.id.qr_status_text)
        val scanningLine: View = dialogView.findViewById(R.id.scanning_line)
        val flashButton: Button = dialogView.findViewById(R.id.button_toggle_flash)
        val cancelButton: Button = dialogView.findViewById(R.id.button_cancel_qr_scan)
        val manualEntryButton: Button = dialogView.findViewById(R.id.button_manual_entry)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        var isScanningQR = true
        startScanningLineAnimation(scanningLine)

        startQRCodeDetectionSimulation(statusText, indicator, statusLabel) { qrCode ->
            dialog.dismiss()
            processQRCodeAuthentication(locker, qrCode, action)
        }

        flashButton.setOnClickListener {
            val isFlashOn = flashButton.text == "Flash: ON"
            flashButton.text = if (isFlashOn) "Flash" else "Flash: ON"
            // TODO: Toggle flashlight
        }

        cancelButton.setOnClickListener {
            isScanningQR = false
            stopScanningLineAnimation()
            dialog.dismiss()
        }

        manualEntryButton.setOnClickListener {
            isScanningQR = false
            stopScanningLineAnimation()
            dialog.dismiss()
            showAccessCodeDialog(locker, action ?: "manual")
>>>>>>> Stashed changes
        }

        dialog.show()
    }

<<<<<<< Updated upstream
    // --- Ensure formatDuration is present ---
=======
    private fun startScanningLineAnimation(scanningLine: View) {
        val animate = object : Runnable {
            var direction = 1f
            var currentY = 0f
            val maxY = 80f

            override fun run() {
                currentY += direction * 5
                if (currentY >= maxY || currentY <= -maxY) direction *= -1
                scanningLine.translationY = currentY
                scanningLine.postDelayed(this, 50)
            }
        }
        scanningLine.post(animate)
    }

    private fun stopScanningLineAnimation() {
        binding.root.handler?.removeCallbacksAndMessages(null)
    }

    private fun startQRCodeDetectionSimulation(
        statusText: TextView,
        indicator: View,
        statusLabel: TextView,
        onSuccess: (String) -> Unit
    ) {
        val delay = (3000..5000).random().toLong()
        Handler(Looper.getMainLooper()).postDelayed({
            statusText.text = "QR Code detected!"
            statusLabel.text = "Processing..."
            indicator.setBackgroundResource(R.drawable.status_indicator_confirmed)
            Handler(Looper.getMainLooper()).postDelayed({ onSuccess("MOCK_QR_123") }, 1000)
        }, delay)

        if (Math.random() < 0.15) {
            Handler(Looper.getMainLooper()).postDelayed({
                statusText.text = "Unable to read QR code"
                statusLabel.text = "Try better lighting"
                indicator.setBackgroundResource(R.drawable.status_indicator_error)
            }, 7000)
        }
    }

    private fun processQRCodeAuthentication(locker: Locker, qrCode: String, action: String?) {
        when (action) {
            "assign" -> {
                Snackbar.make(binding.root, "Locker assigned via QR code!", Snackbar.LENGTH_LONG).show()
            }
            "pickup" -> {
                Snackbar.make(binding.root, "Door opened for pickup via QR!", Snackbar.LENGTH_LONG).show()
            }
            "end_session" -> {
                Snackbar.make(binding.root, "Session ended via QR code!", Snackbar.LENGTH_LONG).show()
            }
        }
        homeViewModel.refreshLockers()
    }

    private fun showAccessCodeDialog(locker: Locker, action: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_access_code, null)

        // Dot indicators
        val dots = listOf(
            dialogView.findViewById<View>(R.id.dot1),
            dialogView.findViewById<View>(R.id.dot2),
            dialogView.findViewById<View>(R.id.dot3),
            dialogView.findViewById<View>(R.id.dot4),
            dialogView.findViewById<View>(R.id.dot5),
            dialogView.findViewById<View>(R.id.dot6)
        )

        // Number buttons
        val numButtons = mapOf(
            dialogView.findViewById<CardView>(R.id.btn1) to "1",
            dialogView.findViewById<CardView>(R.id.btn2) to "2",
            dialogView.findViewById<CardView>(R.id.btn3) to "3",
            dialogView.findViewById<CardView>(R.id.btn4) to "4",
            dialogView.findViewById<CardView>(R.id.btn5) to "5",
            dialogView.findViewById<CardView>(R.id.btn6) to "6",
            dialogView.findViewById<CardView>(R.id.btn7) to "7",
            dialogView.findViewById<CardView>(R.id.btn8) to "8",
            dialogView.findViewById<CardView>(R.id.btn9) to "9",
            dialogView.findViewById<CardView>(R.id.btn0) to "0"
        )

        val clearButton: CardView = dialogView.findViewById(R.id.btnClear)
        val backspaceButton: CardView = dialogView.findViewById(R.id.btnBackspace)
        val unlockButton: CardView = dialogView.findViewById(R.id.btnUnlock)
        val cancelButton: TextView = dialogView.findViewById(R.id.btnCancel)

        var input = ""
        val maxDigits = 6

        // Update dots based on input
        fun updateDots() {
            dots.forEachIndexed { index, dot ->
                dot.background = if (index < input.length) {
                    ContextCompat.getDrawable(requireContext(), R.drawable.premium_code_dot_filled)
                } else {
                    ContextCompat.getDrawable(requireContext(), R.drawable.premium_code_dot_empty)
                }
            }
        }

        // Shake animation on error
        fun showError() {
            val animation = AnimationUtils.loadAnimation(context, R.anim.shake_animation)
            dialogView.findViewById<LinearLayout>(R.id.dots_container).startAnimation(animation)
            Handler(Looper.getMainLooper()).postDelayed({ updateDots() }, 500)
        }

        // Create the access code dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Set up number buttons
        numButtons.forEach { (button, number) ->
            button.setOnClickListener {
                if (input.length < maxDigits) {
                    input += number
                    updateDots()
                }
                if (input.length == maxDigits) {
                    unlockButton.performClick()
                }
            }
        }

        // Clear button
        clearButton.setOnClickListener {
            input = ""
            updateDots()
        }

        // Backspace button
        backspaceButton.setOnClickListener {
            if (input.isNotEmpty()) {
                input = input.dropLast(1)
                updateDots()
            }
        }

        // Unlock button
        unlockButton.setOnClickListener {
            if (input.length != maxDigits) {
                Snackbar.make(dialogView, "Please enter a 6-digit code", Snackbar.LENGTH_SHORT).show()
                showError()
                return@setOnClickListener
            }

            dialog.setTitle("Validating...")
            dialog.setCancelable(false)
            unlockButton.isEnabled = false

            homeViewModel.validateAccessCode(input) { valid, userId, msg ->
                activity?.runOnUiThread {
                    unlockButton.isEnabled = true
                    if (valid && userId != null) {
                        when (action) {
                            "assign" -> homeViewModel.assignLockerToUser(locker.doorId!!, userId, input) { success, _ ->
                                activity?.runOnUiThread {
                                    dialog.dismiss()
                                    if (success) {
                                        showAssignmentSuccessDialog()
                                        homeViewModel.refreshLockers()
                                    } else {
                                        showErrorDialog("Failed to assign locker.")
                                    }
                                }
                            }

                            "pickup" -> homeViewModel.pickupItem(locker.doorId!!, userId, input) { success, _ ->
                                activity?.runOnUiThread {
                                    dialog.dismiss()
                                    if (success) {
                                        showPickupSuccessDialog()
                                        homeViewModel.refreshLockers()
                                    } else {
                                        showErrorDialog("Pickup failed. Try again.")
                                    }
                                }
                            }

                            "end_session" -> homeViewModel.endLockerSession(locker.doorId!!, userId, input) { success, sessionDuration ->
                                activity?.runOnUiThread {
                                    dialog.dismiss()
                                    if (success) {
                                        val duration = sessionDuration ?: "2h 15m"
                                        showThanksDialog(duration)
                                        homeViewModel.refreshLockers()
                                    } else {
                                        showErrorDialog("Failed to end session: $msg")
                                    }
                                }
                            }
                        }
                    } else {
                        showError()
                        Snackbar.make(dialogView, "Invalid code: $msg", Snackbar.LENGTH_LONG).show()
                        input = ""
                        updateDots()
                        unlockButton.isEnabled = true
                    }
                }
            }
        }

        // Cancel button
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // Reset input when dialog is dismissed
        dialog.setOnDismissListener {
            input = ""
            updateDots()
        }

        dialog.show()
    }

    private fun showThanksDialog(duration: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_thanks_for_using, null)

        dialogView.findViewById<TextView>(R.id.duration_info).text = "Session Duration: $duration"

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // ✅ Set onClickListener after dialog is created
        dialogView.findViewById<CardView>(R.id.btn_thanks_ok).setOnClickListener {
            dialog.dismiss() // ✅ Now it's valid
        }

        dialog.show()
    }

    private fun showPickupSuccessDialog(remainingTime: String = "4h 30m") {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_session_continued, null)

        dialogView.findViewById<TextView>(R.id.remaining_time).text = "Remaining Time: $remainingTime"
        dialogView.findViewById<TextView>(R.id.continue_message).text = "Door opened! Please pick up your item safely."

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<CardView>(R.id.btn_continue_ok).setOnClickListener {
            dialog.dismiss() // ✅ Fixed
        }

        dialog.show()
    }

    private fun showAssignmentSuccessDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_thanks_for_using, null)

        dialogView.findViewById<TextView>(R.id.thanks_message).text = "Locker assigned successfully.\nReady for use!"
        dialogView.findViewById<TextView>(R.id.duration_info).visibility = View.GONE

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogView.findViewById<CardView>(R.id.btn_thanks_ok).setOnClickListener {
            dialog.dismiss() // ✅ Fixed
        }

        dialog.show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Oops!")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }



>>>>>>> Stashed changes
    private fun formatDuration(durationMillis: Long): String {
        val h = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val m = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
        val s = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60
        return buildString {
<<<<<<< Updated upstream
            if (hours > 0) {
                append("$hours hour")
                if (hours > 1) append("s")
            }
            if (minutes > 0) {
                if (isNotEmpty()) append(" ")
                append("$minutes minute")
                if (minutes > 1) append("s")
            }

            if (isNotEmpty() && seconds > 0) {
                append(" $seconds second")
                if (seconds > 1) append("s")
            }
            else if (hours == 0L && minutes == 0L) {
                append("$seconds second")
                if (seconds != 1L) append("s")
            }
        }.ifEmpty { "Just now" }
    }

    private fun showAccessCodeDialog(locker: Locker) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_access_code, null)
        val codeDisplay: TextView = dialogView.findViewById(R.id.dialog_locker_display)
        val button0: Button = dialogView.findViewById(R.id.dialog_button_0)
        val button1: Button = dialogView.findViewById(R.id.dialog_button_1)
        val button2: Button = dialogView.findViewById(R.id.dialog_button_2)
        val button3: Button = dialogView.findViewById(R.id.dialog_button_3)
        val button4: Button = dialogView.findViewById(R.id.dialog_button_4)
        val button5: Button = dialogView.findViewById(R.id.dialog_button_5)
        val button6: Button = dialogView.findViewById(R.id.dialog_button_6)
        val button7: Button = dialogView.findViewById(R.id.dialog_button_7)
        val button8: Button = dialogView.findViewById(R.id.dialog_button_8)
        val button9: Button = dialogView.findViewById(R.id.dialog_button_9)
        val buttonClear: Button = dialogView.findViewById(R.id.dialog_button_clear)
        val buttonEnter: Button = dialogView.findViewById(R.id.dialog_button_enter)

        // Reset input for new dialog
        currentInput = ""
        codeDisplay.text = ""
        codeDisplay.hint = "Enter your code"

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Enter Access Code for Locker #${locker.id}")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        val appendNumber = { number: String ->
            if (currentInput.length < 8) {
                currentInput += number
                codeDisplay.text = "*".repeat(currentInput.length)
            } else {
                Snackbar.make(dialogView, "Maximum code length reached", Snackbar.LENGTH_SHORT).show()
            }
        }

        button0.setOnClickListener { appendNumber("0") }
        button1.setOnClickListener { appendNumber("1") }
        button2.setOnClickListener { appendNumber("2") }
        button3.setOnClickListener { appendNumber("3") }
        button4.setOnClickListener { appendNumber("4") }
        button5.setOnClickListener { appendNumber("5") }
        button6.setOnClickListener { appendNumber("6") }
        button7.setOnClickListener { appendNumber("7") }
        button8.setOnClickListener { appendNumber("8") }
        button9.setOnClickListener { appendNumber("9") }

        buttonClear.setOnClickListener {
            currentInput = ""
            codeDisplay.text = ""
            codeDisplay.hint = "Enter your code"
        }

        buttonEnter.setOnClickListener {
            if (currentInput.isNotEmpty()) {
                val isValidCode = currentInput.length >= 4
                if (isValidCode) {
                    when (locker.status) {
                        LockerStatus.AVAILABLE -> {
                            val updatedLocker = locker.copy(
                                status = LockerStatus.OCCUPIED,
                                lastAccessTime = System.currentTimeMillis(),
                                assignedUser = AssignedUserInfo(
                                    userId = "CurrentUser",
                                    firstName = "Current",
                                    lastName = "User"
                                )
                            )
                            lockerMap[selectedLockerId] = updatedLocker
                            refreshLockerGrid()
                            dialog.dismiss()
                            Snackbar.make(binding.root, "Locker #${selectedLockerId} assigned successfully!", Snackbar.LENGTH_LONG).show()
                        }
                        LockerStatus.OCCUPIED -> {
                            val updatedLocker = locker.copy(
                                status = LockerStatus.AVAILABLE,
                                lastAccessTime = System.currentTimeMillis(),
                                assignedUser = null
                            )
                            lockerMap[selectedLockerId] = updatedLocker
                            refreshLockerGrid()
                            dialog.dismiss()
                            Snackbar.make(binding.root, "Locker #${selectedLockerId} opened for pickup!", Snackbar.LENGTH_LONG).show()
                        }
                        LockerStatus.OVERDUE -> {

                            val updatedLocker = locker.copy(
                                status = LockerStatus.AVAILABLE,
                                lastAccessTime = System.currentTimeMillis(),
                                assignedUser = null
                            )
                            lockerMap[selectedLockerId] = updatedLocker
                            refreshLockerGrid()
                            dialog.dismiss()
                            Snackbar.make(binding.root, "Locker #${selectedLockerId} opened for pickup (was overdue)!", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
                else {
                    Snackbar.make(dialogView, "Invalid code. Please try again.", Snackbar.LENGTH_SHORT).show()
                }
                // Clear the input
                currentInput = ""
                codeDisplay.text = ""
                codeDisplay.hint = "Enter your code"
            } else {
                Snackbar.make(dialogView, "Please enter a code", Snackbar.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun refreshLockerGrid() {
        val grid = binding.lockerStatusGrid
        for (i in 0 until grid.childCount) {
            val button = grid.getChildAt(i) as Button
            val lockerId = i + 1
            val locker = lockerMap[lockerId]
            if (locker != null) {
                button.setBackgroundColor(locker.getStatusColor())
                button.isEnabled = locker.isAccessible()
                button.setTextColor(Color.WHITE)
            }
        }
=======
            if (h > 0) append("$h hr${if (h != 1L) "s" else ""} ")
            if (m > 0) append("$m min${if (m != 1L) "s" else ""} ")
            if (h == 0L && m == 0L) append("$s sec${if (s != 1L) "s" else ""}")
        }.trim().ifEmpty { "Just now" }
>>>>>>> Stashed changes
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        MainActivity.onClientIdAvailable = null
    }
}