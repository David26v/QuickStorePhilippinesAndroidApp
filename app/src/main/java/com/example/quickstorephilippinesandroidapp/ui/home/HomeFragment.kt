package com.example.quickstorephilippinesandroidapp.ui.home

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.core.content.ContextCompat
import com.example.quickstorephilippinesandroidapp.*
import com.example.quickstorephilippinesandroidapp.databinding.FragmentHomeBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import data.Locker
import data.LockerStatus
import java.text.SimpleDateFormat
import java.util.*
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    private var currentInput = ""
    private var selectedLockerId: Int = -1
    private var lockerList: List<Locker> = emptyList()

    private val dateTimeHandler = Handler(Looper.getMainLooper())
    private lateinit var dateTimeRunnable: Runnable

    companion object {
        private const val REQUEST_FACE_SCAN = 1001
        private const val REQUEST_PALM_SCAN = 1002
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Observe lockers from ViewModel
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up button listeners
        binding.buttonSave.setOnClickListener {
            Snackbar.make(binding.root, "💾 Saving configuration...", Snackbar.LENGTH_SHORT).show()
            // Extend later: save to local DB or settings
        }

        binding.buttonTake.setOnClickListener {
            Snackbar.make(binding.root, "🔄 Syncing lockers...", Snackbar.LENGTH_SHORT).show()
            homeViewModel.refreshLockers() // Already implemented
        }

        // Start auto-updating date/time
        startDateTimeUpdates()
    }

    private fun startDateTimeUpdates() {
        dateTimeRunnable = object : Runnable {
            override fun run() {
                updateDateTime()
                dateTimeHandler.postDelayed(this, 60000)
            }
        }
        dateTimeHandler.post(dateTimeRunnable)
    }

    private fun updateDateTime() {
        val formatter = SimpleDateFormat("EEE, yyyy-MM-dd HH:mm", Locale.getDefault())
        binding.textDateTime.text = formatter.format(Date())
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

        lockerList.forEach { locker ->
            val lockerView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_locker, grid, false)

            val textView = lockerView.findViewById<TextView>(R.id.locker_number)
            val cardView = lockerView.findViewById<CardView>(R.id.locker_card)

            textView.text = locker.id.toString()

            // Set background color based on status
            val color = when (locker.status) {
                LockerStatus.AVAILABLE -> R.color.success_green
                LockerStatus.OCCUPIED -> R.color.warning_orange
                LockerStatus.OVERDUE -> R.color.error_red
            }.let { ContextCompat.getColor(requireContext(), it) }

            cardView.setCardBackgroundColor(color)
            textView.setTextColor(Color.WHITE)

            // Click listener
            lockerView.setOnClickListener { selectLocker(locker.id) }

            // Add to grid
            grid.addView(lockerView)
        }

        updateStatusCounts()
        animateOverdueIfAny()
    }
    private fun updateStatusCounts() {
        val available = lockerList.count { it.status == LockerStatus.AVAILABLE }
        val occupied = lockerList.count { it.status == LockerStatus.OCCUPIED }
        val overdue = lockerList.count { it.status == LockerStatus.OVERDUE }

        binding.availableCount.text = available.toString()
        binding.occupiedCount.text = occupied.toString()
        binding.overdueCount.text = overdue.toString()

        binding.textUsedLockers.text = (occupied + overdue).toString()
        binding.textUnusedLockers.text = available.toString()
    }

    private fun animateOverdueIfAny() {
        if (lockerList.any { it.status == LockerStatus.OVERDUE }) {
            val pulse = ObjectAnimator.ofFloat(binding.overdueCount, "scaleX", 1f, 1.1f, 1f)
            pulse.duration = 800
            pulse.repeatCount = 1
            pulse.interpolator = AccelerateDecelerateInterpolator()
            pulse.start()
        }
    }

    private fun selectLocker(lockerId: Int) {
        val locker = lockerList.find { it.id == lockerId }
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

                loadAndApplyAuthMethods(accessCodeButton, qrCodeButton, faceRecognitionButton, palmRecognitionButton, cardReaderButton)

                val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create()

                accessCodeButton.setOnClickListener {
                    dialog.dismiss()
                    showAccessCodeDialog(locker, "assign")
                }
                qrCodeButton.setOnClickListener {
                    dialog.dismiss()
                    showQRCodeDialog(locker, "assign")
                }
                faceRecognitionButton.setOnClickListener {
                    dialog.dismiss()
                    startFaceScan(locker, "assign")
                }
                palmRecognitionButton.setOnClickListener {
                    dialog.dismiss()
                    startPalmScan(locker, "assign")
                }
                cardReaderButton.setOnClickListener {
                    dialog.dismiss()
                    Snackbar.make(binding.root, "💳 Waiting for card swipe...", Snackbar.LENGTH_LONG).show()
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

    private fun showOccupiedLockerDialog(locker: Locker) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_locker_occupied, null)

        val greetingText: TextView = dialogView.findViewById(R.id.dialog_occupied_greeting)
        val lockerInfoText: TextView = dialogView.findViewById(R.id.dialog_occupied_locker_info)
        val userIdText: TextView = dialogView.findViewById(R.id.dialog_occupied_user_id)
        val durationText: TextView = dialogView.findViewById(R.id.dialog_occupied_duration)
        val endSessionButton: Button = dialogView.findViewById(R.id.button_end_session)
        val pickupButton: Button = dialogView.findViewById(R.id.button_pickup)

        val durationString = locker.lastAccessTime?.let { formatDuration(System.currentTimeMillis() - it) } ?: "Unknown"
        val userName = locker.assignedUser?.let { "${it.firstName} ${it.lastName}" } ?: "User"

        greetingText.text = "Hi $userName,"
        lockerInfoText.text = "Locker #${locker.id}"
        userIdText.text = "User ID: ${locker.assignedUser?.userId ?: "Unknown"}"
        durationText.text = durationString

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        endSessionButton.setOnClickListener {
            dialog.dismiss()
            showAccessCodeDialog(locker, "end_session")
        }
        pickupButton.setOnClickListener {
            dialog.dismiss()
            showAuthMethodsForOccupiedLocker(locker, "pickup")
        }

        dialog.show()
    }

    private fun showOverdueLockerDialog(locker: Locker) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_locker_overdue, null)

        val greetingText: TextView = dialogView.findViewById(R.id.dialog_overdue_greeting)
        val lockerInfoText: TextView = dialogView.findViewById(R.id.dialog_overdue_locker_info)
        val userIdText: TextView = dialogView.findViewById(R.id.dialog_overdue_user_id)
        val durationText: TextView = dialogView.findViewById(R.id.dialog_overdue_duration)
        val endSessionButton: MaterialButton = dialogView.findViewById(R.id.button_end_session)
        val pickupButton: MaterialButton = dialogView.findViewById(R.id.button_pickup)
        val messageText: TextView = dialogView.findViewById(R.id.dialog_overdue_message)
        val alertIcon: ImageView = dialogView.findViewById(R.id.alert_animation_icon)

        val overdueDuration = locker.lastAccessTime?.let {
            formatDuration(System.currentTimeMillis() - it)
        } ?: "Unknown"
        val userName = locker.assignedUser?.let { "${it.firstName} ${it.lastName}" } ?: "User"

        greetingText.text = "Hi $userName,"
        lockerInfoText.text = "Locker #${locker.id}"
        userIdText.text = "User ID: ${locker.assignedUser?.userId ?: "Unknown"}"
        durationText.text = overdueDuration
        messageText.text = "Your locker is overdue!"

        startPulseAnimation(alertIcon)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        endSessionButton.setOnClickListener {
            dialog.dismiss()
            showAccessCodeDialog(locker, "end_session")
        }

        pickupButton.setOnClickListener {
            dialog.dismiss()
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

        dialogView.findViewById<Button>(R.id.button_end_session)?.visibility = View.GONE
        dialogView.findViewById<Button>(R.id.button_pickup)?.visibility = View.GONE

        lockerInfoText.text = "Locker #${locker.id}\nStatus: ${locker.getStatusText()}\nLocation: ${locker.location ?: "N/A"}"

        loadAndApplyAuthMethods(accessCodeButton, qrCodeButton, faceRecognitionButton, palmRecognitionButton, cardReaderButton)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

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
        }

        dialog.show()
    }

    private fun startFaceScan(locker: Locker, action: String?) {
        selectedLockerId = locker.id
        currentInput = action ?: "assign"

        val intent = Intent(requireContext(), FaceScannerActivity::class.java).apply {
            putExtra(FaceScannerActivity.EXTRA_LOCKER_ID, locker.id)
            putExtra(FaceScannerActivity.EXTRA_ACTION, action)
        }
        startActivityForResult(intent, REQUEST_FACE_SCAN)
    }

    private fun startPalmScan(locker: Locker, action: String?) {
        selectedLockerId = locker.id
        currentInput = action ?: "assign"

        val intent = Intent(requireContext(), PalmScannerActivity::class.java).apply {
            putExtra(PalmScannerActivity.EXTRA_LOCKER_ID, locker.id)
            putExtra(PalmScannerActivity.EXTRA_ACTION, action)
        }
        startActivityForResult(intent, REQUEST_PALM_SCAN)
    }

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

    private fun processAuthentication(lockerId: Int, action: String?, method: String) {
        val locker = lockerList.find { it.id == lockerId }
        if (locker == null) {
            Snackbar.make(binding.root, "Locker not found", Snackbar.LENGTH_LONG).show()
            return
        }

        when (action) {
            "assign" -> Snackbar.make(binding.root, "✅ Locker assigned via $method!", Snackbar.LENGTH_LONG).show()
            "pickup" -> Snackbar.make(binding.root, "🚪 Door opened for pickup via $method!", Snackbar.LENGTH_LONG).show()
            "end_session" -> Snackbar.make(binding.root, "🔚 Session ended via $method!", Snackbar.LENGTH_LONG).show()
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

        var isScanningQR = true
        startScanningLineAnimation(scanningLine)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

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
            stopScanningLineAnimation(scanningLine)
            dialog.dismiss()
        }

        manualEntryButton.setOnClickListener {
            isScanningQR = false
            stopScanningLineAnimation(scanningLine)
            dialog.dismiss()
            showAccessCodeDialog(locker, action ?: "manual")
        }

        dialog.show()
    }

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

    private fun stopScanningLineAnimation(scanningLine: View) {
        scanningLine.removeCallbacks(null)
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
            "assign" -> Snackbar.make(binding.root, "Locker assigned via QR code!", Snackbar.LENGTH_LONG).show()
            "pickup" -> Snackbar.make(binding.root, "Door opened for pickup via QR!", Snackbar.LENGTH_LONG).show()
            "end_session" -> Snackbar.make(binding.root, "Session ended via QR code!", Snackbar.LENGTH_LONG).show()
        }
        homeViewModel.refreshLockers()
    }

    private fun showAccessCodeDialog(locker: Locker, action: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_access_code, null)

        val dots = listOf(
            dialogView.findViewById<View>(R.id.dot1),
            dialogView.findViewById<View>(R.id.dot2),
            dialogView.findViewById<View>(R.id.dot3),
            dialogView.findViewById<View>(R.id.dot4),
            dialogView.findViewById<View>(R.id.dot5),
            dialogView.findViewById<View>(R.id.dot6)
        )

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

        fun updateDots() {
            dots.forEachIndexed { index, dot ->
                dot.background = if (index < input.length) {
                    ContextCompat.getDrawable(requireContext(), R.drawable.premium_code_dot_filled)
                } else {
                    ContextCompat.getDrawable(requireContext(), R.drawable.premium_code_dot_empty)
                }
            }
        }

        fun showError() {
            val animation = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.shake_animation)
            animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}

                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    updateDots()
                }

                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })
            dialogView.findViewById<LinearLayout>(R.id.dots_container).startAnimation(animation)
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

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

        clearButton.setOnClickListener {
            input = ""
            updateDots()
        }

        backspaceButton.setOnClickListener {
            if (input.isNotEmpty()) {
                input = input.dropLast(1)
                updateDots()
            }
        }

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

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

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

        dialogView.findViewById<CardView>(R.id.btn_thanks_ok).setOnClickListener {
            dialog.dismiss()
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
            dialog.dismiss()
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
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showErrorDialog(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Oops!")
            .setMessage(message)
            .setPositiveButton("OK") { d, _ -> d.dismiss() }
            .show()
    }

    private fun formatDuration(durationMillis: Long): String {
        val h = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(durationMillis)
        val m = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
        val s = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60
        return buildString {
            if (h > 0) append("$h hr${if (h != 1L) "s" else ""} ")
            if (m > 0) append("$m min${if (m != 1L) "s" else ""} ")
            if (h == 0L && m == 0L) append("$s sec${if (s != 1L) "s" else ""}")
        }.trim().ifEmpty { "Just now" }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        MainActivity.onClientIdAvailable = null
        dateTimeHandler.removeCallbacks(dateTimeRunnable) // Stop time updates
    }
}