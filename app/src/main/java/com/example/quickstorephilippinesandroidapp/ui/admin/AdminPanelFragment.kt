package com.example.quickstorephilippinesandroidapp.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quickstorephilippinesandroidapp.databinding.FragmentAdminPanelBinding
import com.example.quickstorephilippinesandroidapp.R

class AdminPanelFragment : Fragment() {

    private var _binding: FragmentAdminPanelBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdminPanel()
    }

    private fun setupAdminPanel() {
        // Set up click listeners for admin panel cards
        binding.cardRegisterUser.setOnClickListener {
            // Navigate to Register New User
            findNavController().navigate(R.id.action_to_register_user)
        }

        binding.cardListUsers.setOnClickListener {
            // Navigate to List Users
            findNavController().navigate(R.id.action_to_user_management)
        }

        binding.cardRegisterCard.setOnClickListener {
            // Navigate to Register Card
            findNavController().navigate(R.id.action_to_register_card)
        }

        binding.cardRegisterFace.setOnClickListener {
            // Navigate to Register Face
            findNavController().navigate(R.id.action_to_register_face)
        }

        binding.cardRegisterPalm.setOnClickListener {
            // Navigate to Register Palm
            findNavController().navigate(R.id.action_to_register_palm)
        }

        binding.cardSetAccessCode.setOnClickListener {
            // Navigate to Set Access Code
            findNavController().navigate(R.id.action_to_set_access_code)
        }

        binding.cardViewLogs.setOnClickListener {
            // Navigate to View Logs
            findNavController().navigate(R.id.action_to_view_logs)
        }

        binding.cardSettings.setOnClickListener {
            // Navigate to Settings
            findNavController().navigate(R.id.action_to_settings)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}