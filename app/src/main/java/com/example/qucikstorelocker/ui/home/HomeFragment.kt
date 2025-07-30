package com.example.qucikstorelocker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.qucikstorelocker.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel (if you still want the welcome text)
    private lateinit var homeViewModel: HomeViewModel

    // For managing the dial pad input
    private var currentInput = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize ViewModel
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Observe ViewModel text (optional, if you want it)
        homeViewModel.text.observe(viewLifecycleOwner) { welcomeText ->
            binding.textHome.text = welcomeText
        }

        // Set up click listeners for dial pad buttons
        setupDialPadListeners()

        // Set up click listeners for function buttons
        setupFunctionButtonListeners()

        return root
    }

    private fun setupDialPadListeners() {
        val display: TextView = binding.lockerDisplay

        // Number buttons (0-9)
        for (i in 0..9) {
            val buttonId = resources.getIdentifier("button_$i", "id", requireContext().packageName)
            val button: Button = binding.root.findViewById(buttonId)
            button.setOnClickListener {
                if (currentInput.length < 10) { // Limit input length
                    currentInput += i.toString()
                    display.text = currentInput
                } else {
                    // Optional: Show message if max length reached
                    Snackbar.make(binding.root, "Maximum code length reached", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        // Clear button
        binding.buttonClear.setOnClickListener {
            currentInput = ""
            display.text = "" // Or reset to hint
            display.hint = getString(R.string.locker_code_hint)
        }

        // Enter button
        binding.buttonEnter.setOnClickListener {
            if (currentInput.isNotEmpty()) {
                // Simulate processing the code
                Snackbar.make(binding.root, "Code Entered: $currentInput", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                // Reset input after "entering"
                currentInput = ""
                display.text = ""
                display.hint = getString(R.string.locker_code_hint)
            } else {
                Snackbar.make(binding.root, "Please enter a code", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFunctionButtonListeners() {
        binding.buttonOpen.setOnClickListener {
            // UI Feedback for Open
            Snackbar.make(binding.root, "Opening locker...", Snackbar.LENGTH_SHORT).show()
            // TODO: Add actual locker opening logic here (e.g., API call)
        }

        binding.buttonClose.setOnClickListener {
            // UI Feedback for Close
            Snackbar.make(binding.root, "Closing locker...", Snackbar.LENGTH_SHORT).show()
            // TODO: Add actual locker closing logic here
        }

        binding.buttonStatus.setOnClickListener {
            // UI Feedback for Status
            // This could fetch status from a ViewModel or API
            Snackbar.make(binding.root, "Locker Status: Online", Snackbar.LENGTH_LONG)
                .setAction("Refresh") {
                    // Simulate refresh action
                    Snackbar.make(binding.root, "Status refreshed", Snackbar.LENGTH_SHORT).show()
                }.show()
            // TODO: Add actual status fetching logic here
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}