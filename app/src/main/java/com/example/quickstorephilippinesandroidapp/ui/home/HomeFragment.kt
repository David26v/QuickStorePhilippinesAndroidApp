package com.example.quickstorephilippinesandroidapp.ui.home

import java.util.concurrent.TimeUnit
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.quickstorephilippinesandroidapp.MainActivity
import com.example.quickstorephilippinesandroidapp.R
import com.example.quickstorephilippinesandroidapp.data.Locker
import com.example.quickstorephilippinesandroidapp.data.LockerStatus
import com.example.quickstorephilippinesandroidapp.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    private var currentInput = ""
    private var selectedLockerId: Int = -1
    private var lockerList: List<Locker> = emptyList()
    private var availableAuthMethods: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Observe lockers data from ViewModel
        homeViewModel.lockers.observe(viewLifecycleOwner) { lockers ->
            lockerList = lockers
            setupLockerStatusGrid()
        }

        // Listen for client ID availability
        MainActivity.onClientIdAvailable = {
            activity?.runOnUiThread {
                loadLockersWithClientId()
            }
        }

        // Try to load immediately in case client ID is already available
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
            // Retry after a short delay
            view?.postDelayed({
                val retryClientId = MainActivity.CLIENT_ID
                if (retryClientId != null && retryClientId.isNotEmpty()) {
                    homeViewModel.loadLockers(retryClientId)
                } else {
                    try {
                        Snackbar.make(binding.root, "Client ID not available. Please restart the app.", Snackbar.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        // Ignore if Snackbar fails
                    }
                }
            }, 1000)
        }
    }

    private fun setupLockerStatusGrid() {
        val grid = binding.lockerStatusGrid
        grid.removeAllViews()

        // Use the lockerList from ViewModel instead of mock data
        for (locker in lockerList) {
            val lockerButton = Button(requireContext()).apply {
                text = locker.id.toString()
                textSize = 18f
                val sizeInDp = 80
                val density = resources.displayMetrics.density
                val sizeInPixels = (sizeInDp * density).toInt()
                val params = GridLayout.LayoutParams().apply {
                    width = sizeInPixels
                    height = sizeInPixels
                    setMargins(8, 8, 8, 8)
                }
                layoutParams = params
                setBackgroundColor(locker.getStatusColor())
                setTextColor(Color.WHITE)
                isEnabled = locker.isAccessible()
                setOnClickListener {
                    selectLocker(locker.id)
                }
            }
            grid.addView(lockerButton)
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
                val fingerprintButton: Button = dialogView.findViewById(R.id.button_fingerprint)

                lockerInfoText.text = "Locker #${locker.id}\nStatus: ${locker.getStatusText()}\nLocation: ${locker.location ?: "N/A"}"

                // Load client auth methods and update button visibility
                loadAndApplyAuthMethods(accessCodeButton, qrCodeButton, faceRecognitionButton, fingerprintButton)

                val dialog = AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create()

                accessCodeButton.setOnClickListener {
                    dialog.dismiss()
                    showAccessCodeDialog(locker)
                }
                qrCodeButton.setOnClickListener {
                    dialog.dismiss()
                    // TODO: Implement QR code functionality
                    Snackbar.make(binding.root, "QR Code functionality will be implemented", Snackbar.LENGTH_LONG).show()
                }
                faceRecognitionButton.setOnClickListener {
                    dialog.dismiss()
                    // TODO: Implement face recognition functionality
                    Snackbar.make(binding.root, "Face recognition functionality will be implemented", Snackbar.LENGTH_LONG).show()
                }
                fingerprintButton.setOnClickListener {
                    dialog.dismiss()
                    // TODO: Implement fingerprint functionality
                    Snackbar.make(binding.root, "Fingerprint functionality will be implemented", Snackbar.LENGTH_LONG).show()
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
        fingerprintButton: Button
    ) {
        val clientId = MainActivity.CLIENT_ID
        if (clientId != null) {
            homeViewModel.getClientAuthMethods(clientId) { authMethods ->
                availableAuthMethods = authMethods

                // Update UI on main thread
                activity?.runOnUiThread {
                    // Show/hide buttons based on client settings
                    accessCodeButton.visibility = if (authMethods.contains("access_code")) View.VISIBLE else View.GONE
                    qrCodeButton.visibility = if (authMethods.contains("qr_code")) View.VISIBLE else View.GONE
                    faceRecognitionButton.visibility = if (authMethods.contains("face_recognition")) View.VISIBLE else View.GONE
                    fingerprintButton.visibility = if (authMethods.contains("fingerprint")) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun showOccupiedLockerDialog(locker: Locker) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_locker_occupied, null)
        val greetingText: TextView = dialogView.findViewById(R.id.dialog_occupied_greeting)
        val questionText: TextView = dialogView.findViewById(R.id.dialog_occupied_question)
        val lockerInfoText: TextView = dialogView.findViewById(R.id.dialog_occupied_locker_info)
        val userIdText: TextView = dialogView.findViewById(R.id.dialog_occupied_user_id)
        val durationLabel: TextView = dialogView.findViewById(R.id.dialog_occupied_duration_label)
        val durationText: TextView = dialogView.findViewById(R.id.dialog_occupied_duration)
        val endSessionButton: Button = dialogView.findViewById(R.id.button_end_session)
        val pickupButton: Button = dialogView.findViewById(R.id.button_pickup)

        val durationString = locker.lastAccessTime?.let { accessTime ->
            val currentTime = System.currentTimeMillis()
            val durationMillis = currentTime - accessTime
            formatDuration(durationMillis)
        } ?: "Unknown"

        val userName = locker.assignedUser?.let { "${it.firstName} ${it.lastName}" } ?: "User"
        greetingText.text = "Hi $userName,"
        lockerInfoText.text = "Locker #${locker.id}"
        locker.assignedUser?.let { userInfo ->
            userIdText.text = "User ID: ${userInfo.userId}"
        } ?: run {
            userIdText.text = "User ID: Unknown"
        }
        durationText.text = durationString

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        endSessionButton.setOnClickListener {
            dialog.dismiss()
            showAuthMethodsForOccupiedLocker(locker, "end_session")
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
        val messageText: TextView = dialogView.findViewById(R.id.dialog_overdue_message)
        val lockerInfoText: TextView = dialogView.findViewById(R.id.dialog_overdue_locker_info)
        val userIdText: TextView = dialogView.findViewById(R.id.dialog_overdue_user_id)
        val durationLabel: TextView = dialogView.findViewById(R.id.dialog_overdue_duration_label)
        val durationText: TextView = dialogView.findViewById(R.id.dialog_overdue_duration)
        val endSessionButton: Button = dialogView.findViewById(R.id.button_end_session)
        val pickupButton: Button = dialogView.findViewById(R.id.button_pickup)

        val overdueDurationString = locker.lastAccessTime?.let { accessTime ->
            val currentTime = System.currentTimeMillis()
            val overdueMillis = currentTime - accessTime
            formatDuration(overdueMillis)
        } ?: "Unknown"

        val userName = locker.assignedUser?.let { "${it.firstName} ${it.lastName}" } ?: "User"
        greetingText.text = "Hi $userName,"
        lockerInfoText.text = "Locker #${locker.id}"
        locker.assignedUser?.let { userInfo ->
            userIdText.text = "User ID: ${userInfo.userId}"
        } ?: run {
            userIdText.text = "User ID: Unknown"
        }
        durationText.text = overdueDurationString

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        endSessionButton.setOnClickListener {
            dialog.dismiss()
            showAuthMethodsForOccupiedLocker(locker, "end_session")
        }

        pickupButton.setOnClickListener {
            dialog.dismiss()
            showAuthMethodsForOccupiedLocker(locker, "pickup")
        }

        dialog.show()
    }

    private fun showAuthMethodsForOccupiedLocker(locker: Locker, action: String) {
        // Reuse the same dialog as available lockers but hide the end session and pickup buttons
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_locker_selected, null)
        val lockerInfoText: TextView = dialogView.findViewById(R.id.dialog_locker_info)
        val accessCodeButton: Button = dialogView.findViewById(R.id.button_access_code)
        val qrCodeButton: Button = dialogView.findViewById(R.id.button_qr_code)
        val faceRecognitionButton: Button = dialogView.findViewById(R.id.button_face_recognition)
        val fingerprintButton: Button = dialogView.findViewById(R.id.button_fingerprint)

        // Try to find end session and pickup buttons (they might not exist in this layout)
        val endSessionButton = dialogView.findViewById<Button>(R.id.button_end_session)
        val pickupButton = dialogView.findViewById<Button>(R.id.button_pickup)

        // Hide end session and pickup buttons if they exist
        endSessionButton?.visibility = View.GONE
        pickupButton?.visibility = View.GONE

        lockerInfoText.text = "Locker #${locker.id}\nStatus: ${locker.getStatusText()}\nLocation: ${locker.location ?: "N/A"}"

        // Load client auth methods and update button visibility
        loadAndApplyAuthMethods(accessCodeButton, qrCodeButton, faceRecognitionButton, fingerprintButton)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        accessCodeButton.setOnClickListener {
            dialog.dismiss()
            showAccessCodeDialogForAction(locker, action)
        }
        qrCodeButton.setOnClickListener {
            dialog.dismiss()
            // TODO: Implement QR code functionality
            Snackbar.make(binding.root, "QR Code functionality will be implemented", Snackbar.LENGTH_LONG).show()
        }
        faceRecognitionButton.setOnClickListener {
            dialog.dismiss()
            // TODO: Implement face recognition functionality
            Snackbar.make(binding.root, "Face recognition functionality will be implemented", Snackbar.LENGTH_LONG).show()
        }
        fingerprintButton.setOnClickListener {
            dialog.dismiss()
            // TODO: Implement fingerprint functionality
            Snackbar.make(binding.root, "Fingerprint functionality will be implemented", Snackbar.LENGTH_LONG).show()
        }

        dialog.show()
    }

    private fun showAccessCodeDialogForAction(locker: Locker, action: String) {
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

        var localInput = ""
        codeDisplay.text = ""
        codeDisplay.hint = "Enter your code"

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Enter Access Code")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        val appendNumber = { number: String ->
            if (localInput.length < 8) {
                localInput += number
                codeDisplay.text = "*".repeat(localInput.length)
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
            localInput = ""
            codeDisplay.text = ""
            codeDisplay.hint = "Enter your code"
        }

        buttonEnter.setOnClickListener {
            if (localInput.isNotEmpty()) {
                val isValidCode = localInput.length >= 4
                if (isValidCode) {
                    val accessCodeToUse = localInput

                    dialog.setTitle("Validating Access Code...")
                    dialog.setCancelable(false)

                    homeViewModel.validateAccessCode(accessCodeToUse) { validationSuccess, userId, message ->
                        activity?.runOnUiThread {
                            if (validationSuccess && userId != null) {
                                // Code is valid, now proceed with the action
                                when (action) {
                                    "end_session" -> {
                                        dialog.setTitle("Ending Session...")
                                        homeViewModel.endLockerSession(locker.doorId, userId, accessCodeToUse) { success, message ->
                                            activity?.runOnUiThread {
                                                if (success) {
                                                    Snackbar.make(binding.root, "Session ended successfully! Door opened for pickup.", Snackbar.LENGTH_LONG).show()
                                                    homeViewModel.refreshLockers()
                                                    dialog.dismiss()
                                                } else {
                                                    Snackbar.make(binding.root, "Failed to end session: $message", Snackbar.LENGTH_LONG).show()
                                                    dialog.dismiss()
                                                }
                                            }
                                        }
                                    }
                                    "pickup" -> {
                                        dialog.setTitle("Processing Pickup...")
                                        homeViewModel.pickupItem(locker.doorId, userId, accessCodeToUse) { success, message ->
                                            activity?.runOnUiThread {
                                                if (success) {
                                                    Snackbar.make(binding.root, "Door opened for pickup!", Snackbar.LENGTH_LONG).show()
                                                    homeViewModel.refreshLockers()
                                                    dialog.dismiss()
                                                } else {
                                                    Snackbar.make(binding.root, "Failed to open door: $message", Snackbar.LENGTH_LONG).show()
                                                    dialog.dismiss()
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Snackbar.make(binding.root, "Invalid access code: $message", Snackbar.LENGTH_LONG).show()
                                dialog.dismiss()
                            }
                        }
                    }
                } else {
                    Snackbar.make(dialogView, "Invalid code. Please try again.", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(dialogView, "Please enter a code", Snackbar.LENGTH_SHORT).show()
            }
        }

        dialog.setOnDismissListener {
            localInput = ""
        }

        dialog.show()
    }

    private fun formatDuration(durationMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60

        return buildString {
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

        // Create a local copy that won't be modified
        var localInput = ""
        codeDisplay.text = ""
        codeDisplay.hint = "Enter your code"

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Enter Access Code")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        val appendNumber = { number: String ->
            if (localInput.length < 8) {
                localInput += number
                codeDisplay.text = "*".repeat(localInput.length)
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
            localInput = ""
            codeDisplay.text = ""
            codeDisplay.hint = "Enter your code"
        }

        buttonEnter.setOnClickListener {
            if (localInput.isNotEmpty()) {
                val isValidCode = localInput.length >= 4
                if (isValidCode) {
                    val accessCodeToUse = localInput

                    when (locker.status) {
                        LockerStatus.AVAILABLE -> {
                            // For available lockers: Validate code, ASSIGN, then simulate OPEN
                            dialog.setTitle("Validating Access Code...")
                            dialog.setCancelable(false)

                            homeViewModel.validateAccessCode(accessCodeToUse) { validationSuccess, userId, message ->
                                activity?.runOnUiThread {
                                    if (validationSuccess && userId != null) {
                                        // Code is valid, now assign the locker to the user
                                        val doorId = locker.doorId
                                        if (doorId != null) {
                                            dialog.setTitle("Assigning Locker...")

                                            // ASSIGN the locker first (this updates status to "occupied")
                                            homeViewModel.assignLockerToUser(doorId, userId, accessCodeToUse) { assignSuccess, assignMessage ->
                                                activity?.runOnUiThread {
                                                    if (assignSuccess) {
                                                        // Assignment successful - locker is now "occupied"
                                                        // Simulate door opening (you can add actual door control here later)
                                                        dialog.setTitle("Opening Locker...")

                                                        // Add a small delay to simulate door opening
                                                        dialogView.postDelayed({
                                                            Snackbar.make(binding.root, "Locker assigned and opened successfully!", Snackbar.LENGTH_LONG).show()
                                                            // Refresh lockers to show updated status (should be "occupied" now)
                                                            homeViewModel.refreshLockers()
                                                            dialog.dismiss()
                                                        }, 1000)

                                                    } else {
                                                        Snackbar.make(binding.root, "Failed to assign locker: $assignMessage", Snackbar.LENGTH_LONG).show()
                                                        dialog.dismiss()
                                                    }
                                                }
                                            }
                                        } else {
                                            Snackbar.make(binding.root, "Locker door ID not found", Snackbar.LENGTH_LONG).show()
                                            dialog.dismiss()
                                        }
                                    } else {
                                        Snackbar.make(binding.root, "Invalid access code: $message", Snackbar.LENGTH_LONG).show()
                                        dialog.dismiss()
                                    }
                                }
                            }
                        }

                        LockerStatus.OCCUPIED, LockerStatus.OVERDUE -> {
                            // For occupied/overdue lockers, just refresh the UI (simulate pickup)
                            dialog.setTitle("Processing...")
                            dialog.setCancelable(false)

                            // Add a small delay to simulate processing
                            dialogView.postDelayed({
                                Snackbar.make(binding.root, "Locker accessed successfully!", Snackbar.LENGTH_LONG).show()
                                homeViewModel.refreshLockers()
                                dialog.dismiss()
                            }, 1000)
                        }
                    }
                } else {
                    Snackbar.make(dialogView, "Invalid code. Please try again.", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(dialogView, "Please enter a code", Snackbar.LENGTH_SHORT).show()
            }
        }

        dialog.setOnDismissListener {
            localInput = ""
        }

        dialog.show()
    }

    private fun showAccessCodeDialogForPickup(locker: Locker) {
        showAccessCodeDialog(locker)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up listener
        if (MainActivity.onClientIdAvailable != null) {
            MainActivity.onClientIdAvailable = null
        }
        _binding = null
    }
}