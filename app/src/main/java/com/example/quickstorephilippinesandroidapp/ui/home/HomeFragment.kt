package com.example.quickstorephilippinesandroidapp.ui.home

import java.util.concurrent.TimeUnit
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.quickstorephilippinesandroidapp.R
import com.example.quickstorephilippinesandroidapp.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
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

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    private var currentInput = ""
    private var selectedLockerId: Int = -1


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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setupLockerStatusGrid()
        return root
    }

    private fun setupLockerStatusGrid() {
        val grid = binding.lockerStatusGrid
        grid.removeAllViews()
        for (lockerNumber in 1..24) {
            val locker = lockerMap[lockerNumber] ?: continue
            val lockerButton = Button(requireContext()).apply {
                text = lockerNumber.toString()
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
                val fingerprintButton: Button = dialogView.findViewById(R.id.button_fingerprint)

                lockerInfoText.text = "Locker #${locker.id}\nStatus: ${locker.getStatusText()}\nLocation: ${locker.location ?: "N/A"}"

                val dialog = AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create()

                accessCodeButton.setOnClickListener {
                    dialog.dismiss()
                    showAccessCodeDialog(locker)
                }
                qrCodeButton.setOnClickListener {
                    dialog.dismiss()
                    // TODO: Implement QR Code logic
                    Snackbar.make(binding.root, "QR Code access selected for Locker #${locker.id}", Snackbar.LENGTH_LONG).show()
                }
                faceRecognitionButton.setOnClickListener {
                    dialog.dismiss()
                    // TODO: Implement Face Recognition logic
                    Snackbar.make(binding.root, "Face Recognition selected for Locker #${locker.id}", Snackbar.LENGTH_LONG).show()
                }
                fingerprintButton.setOnClickListener {
                    dialog.dismiss()
                    // TODO: Implement Fingerprint logic
                    Snackbar.make(binding.root, "Fingerprint Scanner selected for Locker #${locker.id}", Snackbar.LENGTH_LONG).show()
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

    // --- New Function: Show Occupied Locker Dialog ---
    private fun showOccupiedLockerDialog(locker: Locker) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_locker_occupied, null)

        // Find views in the occupied layout
        val greetingText: TextView = dialogView.findViewById(R.id.dialog_occupied_greeting)
        val questionText: TextView = dialogView.findViewById(R.id.dialog_occupied_question)
        val lockerInfoText: TextView = dialogView.findViewById(R.id.dialog_occupied_locker_info)
        val userIdText: TextView = dialogView.findViewById(R.id.dialog_occupied_user_id)
        val durationLabel: TextView = dialogView.findViewById(R.id.dialog_occupied_duration_label)
        val durationText: TextView = dialogView.findViewById(R.id.dialog_occupied_duration)
        val endSessionButton: Button = dialogView.findViewById(R.id.button_end_session)
        val pickupButton: Button = dialogView.findViewById(R.id.button_pickup)

        // --- Calculate Duration ---
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
            showAccessCodeDialog(locker)
        }

        pickupButton.setOnClickListener {
            dialog.dismiss()
            showAccessCodeDialog(locker)
        }

        dialog.show()
    }

    // --- New Function: Show Overdue Locker Dialog ---
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
            showAccessCodeDialog(locker)
        }

        pickupButton.setOnClickListener {
            dialog.dismiss()
            showAccessCodeDialog(locker)
        }

        dialog.show()
    }

    // --- Ensure formatDuration is present ---
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}