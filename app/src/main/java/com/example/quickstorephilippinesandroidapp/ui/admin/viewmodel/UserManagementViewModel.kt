package com.example.quickstorephilippinesandroidapp.ui.admin.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quickstorephilippinesandroidapp.ui.admin.model.User
import com.example.quickstorephilippinesandroidapp.ui.admin.model.UsersResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserManagementViewModel : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _syncSuccess = MutableLiveData<Boolean>()
    val syncSuccess: LiveData<Boolean> = _syncSuccess

    // Local user cache
    private val localUsers = mutableListOf<User>()

    init {
        // Initialize with sample data matching your API structure
        initializeSampleData()
    }

    private fun initializeSampleData() {
        localUsers.addAll(
            listOf(
                User(
                    id = "ad239e9e-8029-48ae-b5b9-f4cbe5743a3a",
                    fullName = "Chris",
                    email = "chris@gmail.com",
                    phone = "+6321313123213",
                    department = null,
                    isActive = true
                ),
                User(
                    id = "bf1e41ce-deaf-484d-a2f0-ebeee43d9c1d",
                    fullName = "Christian Lim",
                    email = "christianLim@gmail.com",
                    phone = "+6312313213",
                    department = null,
                    isActive = true
                ),
                User(
                    id = "7e325ccb-faf0-4f67-9785-0f36b381c2d5",
                    fullName = "david",
                    email = "david.fajardo26v@gmail.com",
                    phone = "+231231232131",
                    department = null,
                    isActive = true
                ),
                User(
                    id = "4f8cd046-ff6d-4205-9544-d639c5818305",
                    fullName = "Geralt of rivia",
                    email = "geralt@gmail.com",
                    phone = "+2343123213",
                    department = null,
                    isActive = true
                ),
                User(
                    id = "fab65905-7d93-4327-99c9-b90087b86e13",
                    fullName = "kaye",
                    email = "kaye@gmail.com",
                    phone = "+6312321312332",
                    department = null,
                    isActive = true
                )
            )
        )
    }

    fun loadUsers() {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // Simulate loading delay
                delay(500)
                _users.value = localUsers.toList()
            } catch (e: Exception) {
                _error.value = "Failed to load users: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun syncUsersFromCloud() {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // TODO: Replace with actual API call
                // Example: apiService.syncUsers()
                delay(2000)

                // Simulate successful sync - in real implementation,
                // you would push local changes and pull updates from server
                _syncSuccess.value = true

                // Refresh the user list after sync
                loadUsers()

            } catch (e: Exception) {
                _error.value = "Failed to sync users: ${e.message}"
                _syncSuccess.value = false
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchUsersFromCloud() {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // TODO: Replace with actual API call
                // Example: val response = apiService.getUsers()
                delay(1500)

                // Simulate API response matching your backend structure
                val simulatedResponse = UsersResponse(
                    users = listOf(
                        User(
                            id = "new-user-1",
                            fullName = "Alice Wilson",
                            email = "alice.wilson@quickstore.ph",
                            phone = "+639123456789",
                            department = "IT",
                            isActive = true
                        ),
                        User(
                            id = "new-user-2",
                            fullName = "Bob Chen",
                            email = "bob.chen@quickstore.ph",
                            phone = "+639987654321",
                            department = "Sales",
                            isActive = true
                        )
                    ),
                    count = 2
                )

                // Add fetched users to local cache
                localUsers.addAll(simulatedResponse.users)
                _users.value = localUsers.toList()

            } catch (e: Exception) {
                _error.value = "Failed to fetch users: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun refreshUsers() {
        loadUsers()
    }

    fun addUser(user: User) {
        localUsers.add(user)
        _users.value = localUsers.toList()
    }

    fun updateUser(updatedUser: User) {
        val index = localUsers.indexOfFirst { it.id == updatedUser.id }
        if (index != -1) {
            localUsers[index] = updatedUser
            _users.value = localUsers.toList()
        }
    }

    fun deleteUser(userId: String) {
        localUsers.removeAll { it.id == userId }
        _users.value = localUsers.toList()
    }
}