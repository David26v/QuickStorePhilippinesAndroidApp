package com.example.quickstorephilippinesandroidapp.ui.admin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import com.example.quickstorephilippinesandroidapp.databinding.FragmentRegisterFaceBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RegisterFaceFragment : Fragment() {

    private var _binding: FragmentRegisterFaceBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var faceScanLauncher: ActivityResultLauncher<Intent>

    private var selectedUserId: String? = null
    private var currentStep = 1 // 1 = select user, 2 = capture face, 3 = confirmation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterFaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Register activity result launcher
        faceScanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val data = result.data
                val scanResult = data?.getStringExtra("result")

                if (scanResult == "success") {
                    showStep(3) // Move to success/confirmation
                } else {
                    Toast.makeText(context, "Face scan failed or cancelled", Toast.LENGTH_SHORT).show()
                    showStep(2)
                }
            } else {
                Toast.makeText(context, "Scan cancelled", Toast.LENGTH_SHORT).show()
                showStep(2)
            }
        }

        // Initially show Step 1
        showStep(1)

        // Back Button Logic
        binding.btnBack.setOnClickListener {
            when (currentStep) {
                2 -> showStep(1)
                3 -> showStep(2)
                else -> findNavController().navigateUp()
            }
        }

        // Step 1: Select User
        binding.btnSelectUser.setOnClickListener {
            selectedUserId = "USER123"
            binding.tvSelectedUser.text = "User: John Doe"
            binding.layoutSelectedUser.visibility = View.VISIBLE
            showStep(2)
        }

        // Step 2: Capture Face → Launch FaceScannerActivity
        binding.btnCaptureFace.setOnClickListener {
            Log.d("FaceRegistration", "Button clicked! Current step: $currentStep")
            if (currentStep == 2) {
                launchFaceScanner()
            } else {
                Log.d("FaceRegistration", "Not on step 2, ignoring click")
            }
        }

        // Step 3: Try Again
        binding.btnTryAgain.setOnClickListener {
            showStep(2)
        }

        // Step 3: Register Face
        binding.btnRegisterFace.setOnClickListener {
            Toast.makeText(context, "Face registered successfully!", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
        }
    }

    /**
     * Launches FaceScannerActivity with required extras
     */
    private fun launchFaceScanner() {
        Log.d("FaceRegistration", "Launching face registration for User ID: $selectedUserId, Locker ID: 1")

        val intent = Intent(requireContext(), FaceRegistrationActivity::class.java).apply {
            putExtra(FaceRegistrationActivity.EXTRA_USER_ID, selectedUserId)
            putExtra(FaceRegistrationActivity.EXTRA_LOCKER_ID, 1)
        }
        faceScanLauncher.launch(intent)
    }

    /**
     * Requests camera permission
     */
    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Toast.makeText(context, "Camera permission is required to scan your face.", Toast.LENGTH_LONG).show()
        }
        requestPermissions(arrayOf(Manifest.permission.CAMERA), 1001)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchFaceScanner()
            } else {
                Toast.makeText(context, "Camera permission is required.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Show the appropriate step UI
     */
    private fun showStep(step: Int) {
        currentStep = step

        binding.cardUserSelection.visibility = View.GONE
        binding.cardFaceCapture.visibility = View.GONE
        binding.successCard.visibility = View.GONE

        when (step) {
            1 -> {
                binding.cardUserSelection.visibility = View.VISIBLE
                binding.btnCaptureFace.isEnabled = false  // Disable until user selected
            }
            2 -> {
                binding.cardFaceCapture.visibility = View.VISIBLE
                binding.btnCaptureFace.text = "Scan Face"
                binding.btnCaptureFace.isEnabled = true
            }
            3 -> {
                binding.successCard.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}