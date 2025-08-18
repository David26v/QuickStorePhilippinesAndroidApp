package com.example.quickstorephilippinesandroidapp.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quickstorephilippinesandroidapp.R
import com.example.quickstorephilippinesandroidapp.databinding.FragmentConfirmFaceBinding

class ConfirmFaceFragment : Fragment() {

    private var _binding: FragmentConfirmFaceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmFaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userName = arguments?.getString("userName") ?: "User"
        binding.tvUserName.text = "Ready to register $userName’s face?"

        binding.btnTryAgain.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnRegisterFace.setOnClickListener {
            Toast.makeText(context, "✅ Face registered successfully!", Toast.LENGTH_LONG).show()
            findNavController().popBackStack(R.id.nav_admin_panel, false)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}