package com.example.quickstorephilippinesandroidapp.ui.admin

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.quickstorephilippinesandroidapp.R
import com.example.quickstorephilippinesandroidapp.databinding.FragmentUserRegistrationBinding
import database.entity.LocalUserCredential
import database.entity.LockerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.*

class UserRegistrationFragment : Fragment() {

    private var _binding: FragmentUserRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var lockerDao: database.entity.LockerDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize DAO
        lockerDao = LockerDatabase.getDatabase(requireContext()).lockerDao()

        setupUI()
        generateAccessCode() // Set initial access code
    }

    private fun setupUI() {
        binding.btnGenerateCode.setOnClickListener {
            generateAccessCode()
        }

        binding.tilAccessCode.setEndIconOnClickListener {
            generateAccessCode()
        }

        binding.etAccessCode.setOnLongClickListener {
            copyAccessCodeToClipboard()
            true
        }

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun generateAccessCode() {
        // Generate 7-digit code
        val code = "%07d".format((1..9999999).random())
        binding.etAccessCode.setText(code)

        // Visual feedback: highlight border
        val blue = ContextCompat.getColor(requireContext(), R.color.blue_500)
        binding.tilAccessCode.boxStrokeColor = blue
        Toast.makeText(context, R.string.new_access_code_generated, Toast.LENGTH_SHORT).show()

        // Reset color after 1 second, only if fragment is still active
        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.delay(1000)
            if (_binding != null && isAdded) {
                val gray = ContextCompat.getColor(requireContext(), R.color.gray_400)
                binding.tilAccessCode.boxStrokeColor = gray
            }
        }
    }

    private fun copyAccessCodeToClipboard() {
        val code = binding.etAccessCode.text?.toString()?.trim() ?: return
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Access Code", code))
        Toast.makeText(context, R.string.access_code_copied, Toast.LENGTH_SHORT).show()
    }

    private fun registerUser() {
        val fullName = binding.etFullName.text?.toString()?.trim()
        val email = binding.etEmail.text?.toString()?.trim()
        val accessCode = binding.etAccessCode.text?.toString()?.trim()

        if (!validateInputs(fullName, email, accessCode)) return

        // Disable button to prevent double tap
        binding.btnRegister.isEnabled = false
        binding.btnRegister.setText(R.string.registering)

        val hashedCode = hashString(accessCode!!)

        val credential = LocalUserCredential(
            id = UUID.randomUUID().toString(),
            userId = UUID.randomUUID().toString(),
            methodType = "access_code",
            credentialHash = hashedCode,
            userFullName = fullName,
            userEmail = email,
            isActive = true,
            isLocallyCreated = true,
            isLocallyUpdated = false,
            isLocallyDeleted = false,
            syncStatus = 0
        )

        // Insert in background
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    lockerDao.insertUserCredential(credential)
                }
                Toast.makeText(requireContext(), R.string.user_registered_success, Toast.LENGTH_LONG).show()
                clearForm()
                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                if (_binding != null) {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.setText(R.string.register_user)
                }
            }
        }
    }

    private fun validateInputs(fullName: String?, email: String?, accessCode: String?): Boolean {
        var isValid = true

        if (fullName.isNullOrEmpty()) {
            binding.tilFullName.error = getString(R.string.full_name_required)
            isValid = false
        } else {
            binding.tilFullName.error = null
        }

        if (email.isNullOrEmpty()) {
            binding.tilEmail.error = getString(R.string.email_required)
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.valid_email_required)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (accessCode.isNullOrEmpty()) {
            binding.tilAccessCode.error = getString(R.string.access_code_required)
            isValid = false
        } else if (accessCode.length != 7 || !accessCode.all { it.isDigit() }) {
            binding.tilAccessCode.error = getString(R.string.seven_digit_code_required)
            isValid = false
        } else {
            binding.tilAccessCode.error = null
        }

        return isValid
    }

    private fun hashString(input: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            md.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            throw RuntimeException("Hashing failed", e)
        }
    }

    private fun clearForm() {
        binding.etFullName.text?.clear()
        binding.etEmail.text?.clear()
        // Only generate new code if view is still attached
        if (_binding != null) {
            generateAccessCode()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Critical: avoid memory leaks and stale references
    }
}