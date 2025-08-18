package com.example.quickstorephilippinesandroidapp.ui.admin.repository

import com.example.quickstorephilippinesandroidapp.ui.admin.model.User
import com.example.quickstorephilippinesandroidapp.ui.admin.model.UsersResponse
import com.example.quickstorephilippinesandroidapp.ui.admin.service.UserApiService
import retrofit2.Response

class UserRepository(private val apiService: UserApiService) {

    suspend fun getUsers(): Result<UsersResponse> {
        return try {
            val response = apiService.getUsers()
            if (response.isSuccessful) {
                response.body()?.let { usersResponse ->
                    Result.success(usersResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUser(user: User): Result<User> {
        return try {
            val response = apiService.createUser(user)
            if (response.isSuccessful) {
                response.body()?.let { createdUser ->
                    Result.success(createdUser)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(userId: String, user: User): Result<User> {
        return try {
            val response = apiService.updateUser(userId, user)
            if (response.isSuccessful) {
                response.body()?.let { updatedUser ->
                    Result.success(updatedUser)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            val response = apiService.deleteUser(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncUsers(localUsers: List<User>): Result<UsersResponse> {
        return try {
            val response = apiService.syncUsers(localUsers)
            if (response.isSuccessful) {
                response.body()?.let { syncResponse ->
                    Result.success(syncResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}