package com.example.quickstorephilippinesandroidapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import api.ApiClient
import com.example.quickstorephilippinesandroidapp.api.LockerSessionRequest
import com.example.quickstorephilippinesandroidapp.data.Locker
import data.LockerApiResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LockerRepository {

    private val apiService = ApiClient.apiService

    fun getLockers(clientId: String, callback: (List<Locker>) -> Unit) {
        apiService.getLockerStatuses(clientId).enqueue(object : Callback<List<LockerApiResponse>> {
            override fun onResponse(call: Call<List<LockerApiResponse>>, response: Response<List<LockerApiResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val lockerList = response.body()!!.map { lockerApiResponse ->
                        Locker.fromDto(lockerApiResponse)
                    }
                    callback(lockerList)
                } else {
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<List<LockerApiResponse>>, t: Throwable) {
                callback(emptyList())
            }
        })
    }

    fun getClientAuthMethods(clientId: String, callback: (List<String>) -> Unit) {
        apiService.getClientAuthMethods(clientId).enqueue(object : Callback<com.example.quickstorephilippinesandroidapp.api.ClientAuthMethodsResponse> {
            override fun onResponse(
                call: Call<com.example.quickstorephilippinesandroidapp.api.ClientAuthMethodsResponse>,
                response: Response<com.example.quickstorephilippinesandroidapp.api.ClientAuthMethodsResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    callback(response.body()!!.auth_methods)
                } else {
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<com.example.quickstorephilippinesandroidapp.api.ClientAuthMethodsResponse>, t: Throwable) {
                callback(emptyList())
            }
        })
    }

    fun controlLockerDoor(doorId: String, actionType: String, userId: String, accessCode: String?, callback: (Boolean, String?) -> Unit) {
        val request = com.example.quickstorephilippinesandroidapp.api.LockerControlRequest(
            action_type = actionType,
            user_id = userId,
            access_code = accessCode
        )

        apiService.controlLockerDoor(doorId, request).enqueue(object : Callback<com.example.quickstorephilippinesandroidapp.api.LockerControlResponse> {
            override fun onResponse(
                call: Call<com.example.quickstorephilippinesandroidapp.api.LockerControlResponse>,
                response: Response<com.example.quickstorephilippinesandroidapp.api.LockerControlResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.message)
                } else {
                    callback(false, "Failed to control locker door")
                }
            }

            override fun onFailure(call: Call<com.example.quickstorephilippinesandroidapp.api.LockerControlResponse>, t: Throwable) {
                callback(false, t.message)
            }
        })
    }

    fun controlLockerDoorAutoAssign(actionType: String, userId: String, accessCode: String?, callback: (Boolean, String?, String?) -> Unit) {
        val request = com.example.quickstorephilippinesandroidapp.api.LockerControlRequest(
            action_type = actionType,
            user_id = userId,
            access_code = accessCode
        )

        apiService.controlLockerDoorAutoAssign(request).enqueue(object : Callback<com.example.quickstorephilippinesandroidapp.api.LockerControlResponse> {
            override fun onResponse(
                call: Call<com.example.quickstorephilippinesandroidapp.api.LockerControlResponse>,
                response: Response<com.example.quickstorephilippinesandroidapp.api.LockerControlResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.message, null)
                } else {
                    callback(false, "Failed to auto-assign locker", null)
                }
            }

            override fun onFailure(call: Call<com.example.quickstorephilippinesandroidapp.api.LockerControlResponse>, t: Throwable) {
                callback(false, null, t.message)
            }
        })
    }

    fun assignLockerToUser(doorId: String, userId: String, accessCode: String?, callback: (Boolean, String?) -> Unit) {
        val request = com.example.quickstorephilippinesandroidapp.api.LockerAssignRequest(
            user_id = userId,
            access_code = accessCode
        )

        apiService.assignLockerToUser(doorId, request).enqueue(object : Callback<com.example.quickstorephilippinesandroidapp.api.LockerAssignResponse> {
            override fun onResponse(
                call: Call<com.example.quickstorephilippinesandroidapp.api.LockerAssignResponse>,
                response: Response<com.example.quickstorephilippinesandroidapp.api.LockerAssignResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.message)
                } else {
                    callback(false, "Failed to assign locker")
                }
            }

            override fun onFailure(call: Call<com.example.quickstorephilippinesandroidapp.api.LockerAssignResponse>, t: Throwable) {
                callback(false, t.message)
            }
        })
    }

    fun validateAccessCode(accessCode: String, callback: (Boolean, String?, String?) -> Unit) {
        val request = com.example.quickstorephilippinesandroidapp.api.ValidateAccessCodeRequest(
            access_code = accessCode
        )

        apiService.validateAccessCode(request).enqueue(object : Callback<com.example.quickstorephilippinesandroidapp.api.ValidateAccessCodeResponse> {
            override fun onResponse(
                call: Call<com.example.quickstorephilippinesandroidapp.api.ValidateAccessCodeResponse>,
                response: Response<com.example.quickstorephilippinesandroidapp.api.ValidateAccessCodeResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.user_id, response.body()!!.message)
                } else {
                    callback(false, null, "Invalid access code")
                }
            }

            override fun onFailure(call: Call<com.example.quickstorephilippinesandroidapp.api.ValidateAccessCodeResponse>, t: Throwable) {
                callback(false, null, t.message)
            }
        })
    }

    // NEW METHODS:
    fun pickupItem(doorId: String, userId: String, accessCode: String?, callback: (Boolean, String?) -> Unit) {
        val request = LockerSessionRequest(
            user_id = userId,
            access_code = accessCode,
            source = "apk"
        )

        apiService.pickupItem(doorId, request).enqueue(object : Callback<com.example.quickstorephilippinesandroidapp.api.LockerSessionResponse> {
            override fun onResponse(
                call: Call<com.example.quickstorephilippinesandroidapp.api.LockerSessionResponse>,
                response: Response<com.example.quickstorephilippinesandroidapp.api.LockerSessionResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.message)
                } else {
                    callback(false, "Failed to process pickup")
                }
            }

            override fun onFailure(call: Call<com.example.quickstorephilippinesandroidapp.api.LockerSessionResponse>, t: Throwable) {
                callback(false, t.message)
            }
        })
    }

    fun endLockerSession(doorId: String, userId: String, accessCode: String?, callback: (Boolean, String?) -> Unit) {
        val request = LockerSessionRequest(
            user_id = userId,
            access_code = accessCode,
            source = "apk"
        )

        apiService.endLockerSession(doorId, request).enqueue(object : Callback<com.example.quickstorephilippinesandroidapp.api.LockerSessionResponse> {
            override fun onResponse(
                call: Call<com.example.quickstorephilippinesandroidapp.api.LockerSessionResponse>,
                response: Response<com.example.quickstorephilippinesandroidapp.api.LockerSessionResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.message)
                } else {
                    callback(false, "Failed to end session")
                }
            }

            override fun onFailure(call: Call<com.example.quickstorephilippinesandroidapp.api.LockerSessionResponse>, t: Throwable) {
                callback(false, t.message)
            }
        })
    }
}