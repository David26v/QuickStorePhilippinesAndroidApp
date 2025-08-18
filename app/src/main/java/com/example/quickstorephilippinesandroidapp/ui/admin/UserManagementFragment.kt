package com.example.quickstorephilippinesandroidapp.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quickstorephilippinesandroidapp.databinding.FragmentUserManagementBinding
import com.example.quickstorephilippinesandroidapp.ui.admin.adapter.UserListAdapter
import com.example.quickstorephilippinesandroidapp.ui.admin.viewmodel.UserManagementViewModel

class UserManagementFragment : Fragment() {

    private var _binding: FragmentUserManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: UserManagementViewModel
    private lateinit var userAdapter: UserListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // Load users on start
        viewModel.loadUsers()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[UserManagementViewModel::class.java]
    }

    private fun setupRecyclerView() {
        userAdapter = UserListAdapter { user ->
            // Handle user item click (e.g., edit user)
            Toast.makeText(context, "Edit user: ${user.name}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnSyncUsers.setOnClickListener {
            viewModel.syncUsersFromCloud()
        }

        binding.btnFetchUsers.setOnClickListener {
            viewModel.fetchUsersFromCloud()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshUsers()
        }
    }

    private fun observeViewModel() {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            userAdapter.submitList(users)
            updateUserCount(users.size)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSyncUsers.isEnabled = !isLoading
            binding.btnFetchUsers.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        viewModel.syncSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(context, "Users synced successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserCount(count: Int) {
        binding.textUserCount.text = "Total Users: $count"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}