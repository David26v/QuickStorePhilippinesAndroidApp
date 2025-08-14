package com.example.quickstorephilippinesandroidapp.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quickstorephilippinesandroidapp.R
import com.example.quickstorephilippinesandroidapp.databinding.FragmentAdminBinding

class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    // You can change this to your desired access code
    private val adminAccessCode = "12345"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showAccessCodeDialog()
    }

    private fun showAccessCodeDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_access, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false) // Prevent outside touch dismiss
            .create()

        // Remove default title if needed (optional)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Dots for code input
        val dots = listOf(
            dialogView.findViewById<View>(R.id.dot1),
            dialogView.findViewById(R.id.dot2),
            dialogView.findViewById(R.id.dot3),
            dialogView.findViewById(R.id.dot4),
            dialogView.findViewById(R.id.dot5)
        )

        val cancelButton = dialogView.findViewById<TextView>(R.id.btnCancel)
        val enterButton = dialogView.findViewById<CardView>(R.id.btnEnter)
        val numberButtons = mapOf(
            0 to dialogView.findViewById<CardView>(R.id.btn0),
            1 to dialogView.findViewById<CardView>(R.id.btn1),
            2 to dialogView.findViewById<CardView>(R.id.btn2),
            3 to dialogView.findViewById<CardView>(R.id.btn3),
            4 to dialogView.findViewById<CardView>(R.id.btn4),
            5 to dialogView.findViewById<CardView>(R.id.btn5),
            6 to dialogView.findViewById<CardView>(R.id.btn6),
            7 to dialogView.findViewById<CardView>(R.id.btn7),
            8 to dialogView.findViewById<CardView>(R.id.btn8),
            9 to dialogView.findViewById<CardView>(R.id.btn9)
        )
        val clearButton = dialogView.findViewById<CardView>(R.id.btnClear)
        val backspaceButton = dialogView.findViewById<CardView>(R.id.btnBackspace)

        var enteredCode = ""

        // Helper to update dots
        fun updateDots() {
            dots.forEachIndexed { index, dot ->
                if (index < enteredCode.length) {
                    dot.setBackgroundResource(R.drawable.code_dot_filled) // filled
                } else {
                    dot.setBackgroundResource(R.drawable.code_dot_empty) // empty
                }
            }
        }

        // Number click listener
        numberButtons.forEach { (number, button) ->
            button.setOnClickListener {
                if (enteredCode.length < 5) {
                    enteredCode += number
                    updateDots()
                }
            }
        }

        // Clear button
        clearButton.setOnClickListener {
            enteredCode = ""
            updateDots()
        }

        // Backspace button
        backspaceButton.setOnClickListener {
            if (enteredCode.isNotEmpty()) {
                enteredCode = enteredCode.dropLast(1)
                updateDots()
            }
        }

        // Cancel button
        cancelButton.setOnClickListener {
            dialog.dismiss()
            findNavController().popBackStack()
        }

        // Enter button
        enterButton.setOnClickListener {
            if (enteredCode == adminAccessCode) {
                dialog.dismiss()
                findNavController().navigate(R.id.action_admin_to_panel)
            } else {
                // Shake animation for wrong code
                dialogView.startAnimation(
                    AnimationUtils.loadAnimation(requireContext(), R.anim.shake_animation)
                )

                // Reset code and show error
                Toast.makeText(requireContext(), "Invalid access code", Toast.LENGTH_SHORT).show()
                enteredCode = ""
                updateDots()
            }
        }

        dialog.show()
        updateDots() // Initialize dots
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}