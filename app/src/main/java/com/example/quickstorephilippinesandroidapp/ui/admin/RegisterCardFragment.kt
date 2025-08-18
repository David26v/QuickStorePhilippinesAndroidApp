package com.example.quickstorephilippinesandroidapp.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.quickstorephilippinesandroidapp.databinding.FragmentRegisterCardBinding
import com.example.quickstorephilippinesandroidapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterCardFragment : Fragment() {

    private var _binding: FragmentRegisterCardBinding? = null
    private val binding get() = _binding!!

    private var isScanning = false
    private var selectedUserId: String? = null
    private var selectedUserName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupClickListeners()
        loadUsers()
    }

    private fun setupUI() {
        // Initialize UI state
        binding.progressScanning.visibility = View.GONE
        binding.layoutScanResult.visibility = View.GONE
        binding.btnStartScan.isEnabled = false
        binding.btnRegisterCard.isEnabled = false
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Select user button
        binding.btnSelectUser.setOnClickListener {
            showUserSelectionDialog()
        }

        // Start scan button
        binding.btnStartScan.setOnClickListener {
            if (!isScanning) {
                startCardScan()
            } else {
                stopCardScan()
            }
        }

        // Register card button
        binding.btnRegisterCard.setOnClickListener {
            registerCard()
        }

        // Try again button
        binding.btnTryAgain.setOnClickListener {
            resetScanState()
        }
    }

    private fun loadUsers() {
        // Simulate loading users - replace with actual data loading
        lifecycleScope.launch {
            delay(500) // Simulate network delay
            // For demo purposes, enable the select user button
            // In real implementation, populate with actual user data
        }
    }

    private fun showUserSelectionDialog() {
        // For demo purposes, simulate user selection
        // In real implementation, show a dialog or navigate to user selection screen
        selectedUserId = "user123"
        selectedUserName = "John Doe"

        binding.tvSelectedUser.text = selectedUserName
        binding.tvSelectedUser.visibility = View.VISIBLE
        binding.btnStartScan.isEnabled = true

        Toast.makeText(requireContext(), "User selected: $selectedUserName", Toast.LENGTH_SHORT).show()
    }

    private fun startCardScan() {
        isScanning = true
        binding.btnStartScan.text = "Stop Scanning"
        binding.btnStartScan.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stop, 0, 0, 0)

        binding.progressScanning.visibility = View.VISIBLE
        binding.tvScanStatus.text = "Place RFID card near the scanner..."
        binding.tvScanStatus.setTextColor(resources.getColor(R.color.blue_primary, null))

        // Simulate card scanning process
        lifecycleScope.launch {
            delay(3000) // Simulate scanning time

            if (isScanning) {
                // Simulate successful card detection
                onCardDetected("CARD123456789")
            }
        }
    }

    private fun stopCardScan() {
        isScanning = false
        binding.btnStartScan.text = "Start Scanning"
        binding.btnStartScan.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_nfc, 0, 0, 0)

        binding.progressScanning.visibility = View.GONE
        binding.tvScanStatus.text = "Scanning stopped"
        binding.tvScanStatus.setTextColor(resources.getColor(R.color.text_secondary, null))
    }

    private fun onCardDetected(cardId: String) {
        isScanning = false
        binding.btnStartScan.text = "Start Scanning"
        binding.btnStartScan.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_nfc, 0, 0, 0)

        binding.progressScanning.visibility = View.GONE
        binding.layoutScanResult.visibility = View.VISIBLE

        binding.tvCardId.text = cardId
        binding.tvScanStatus.text = "Card detected successfully!"
        binding.tvScanStatus.setTextColor(resources.getColor(R.color.green_success, null))

        binding.btnRegisterCard.isEnabled = true
    }

    private fun registerCard() {
        val cardId = binding.tvCardId.text.toString()

        if (selectedUserId.isNullOrEmpty() || cardId.isEmpty()) {
            Toast.makeText(requireContext(), "Missing user or card information", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading state
        binding.btnRegisterCard.isEnabled = false
        binding.btnRegisterCard.text = "Registering..."

        // Simulate card registration
        lifecycleScope.launch {
            delay(2000) // Simulate network request

            // Simulate successful registration
            binding.btnRegisterCard.text = "Register Card"
            binding.btnRegisterCard.isEnabled = true

            Toast.makeText(requireContext(), "Card registered successfully for $selectedUserName", Toast.LENGTH_LONG).show()

            // Navigate back or reset for another registration
            delay(1000)
            findNavController().navigateUp()
        }
    }

    private fun resetScanState() {
        binding.layoutScanResult.visibility = View.GONE
        binding.tvScanStatus.text = "Ready to scan RFID card"
        binding.tvScanStatus.setTextColor(resources.getColor(R.color.text_secondary, null))
        binding.btnRegisterCard.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}