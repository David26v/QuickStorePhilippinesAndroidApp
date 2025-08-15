package repository

import api.ApiClient
import com.example.quickstorephilippinesandroidapp.api.*
import data.Locker
import data.LockerApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LockerRepository {

    private val _lockers = MutableStateFlow(generateInitialLockers())
    val lockers: StateFlow<List<Locker>> = _lockers

    private val apiService = ApiClient.instance.create(ApiService::class.java)

    /**
     * Fetch locker statuses via callback
     */
    fun getLockers(clientId: String, callback: (List<Locker>) -> Unit) {
        apiService.getLockerStatuses(clientId).enqueue(object : Callback<List<LockerApiResponse>> {
            override fun onResponse(
                call: Call<List<LockerApiResponse>>,
                response: Response<List<LockerApiResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val lockerList = response.body()!!.map { Locker.fromDto(it) }
                    _lockers.value = lockerList // Update state flow
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

    /**
     * Get available auth methods for a client
     */
    fun getClientAuthMethods(clientId: String, callback: (List<String>) -> Unit) {
        apiService.getClientAuthMethods(clientId).enqueue(object : Callback<ClientAuthMethodsResponse> {
            override fun onResponse(
                call: Call<ClientAuthMethodsResponse>,
                response: Response<ClientAuthMethodsResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    callback(response.body()!!.auth_methods)
                } else {
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<ClientAuthMethodsResponse>, t: Throwable) {
                callback(emptyList())
            }
        })
    }

    /**
     * Control locker door (open/close) with explicit door ID
     */
    fun controlLockerDoor(
        doorId: String,
        actionType: String,
        userId: String,
        accessCode: String?,
        callback: (Boolean, String?) -> Unit
    ) {
        val request = LockerControlRequest(action_type = actionType, user_id = userId, access_code = accessCode)
        apiService.controlLockerDoor(doorId, request).enqueue(object : Callback<LockerControlResponse> {
            override fun onResponse(
                call: Call<LockerControlResponse>,
                response: Response<LockerControlResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.message)
                } else {
                    callback(false, "Failed to control locker door")
                }
            }

            override fun onFailure(call: Call<LockerControlResponse>, t: Throwable) {
                callback(false, t.message ?: "Network error")
            }
        })
    }

    /**
     * Auto-assign and control a locker (e.g., for deposit)
     */
    fun controlLockerDoorAutoAssign(
        actionType: String,
        userId: String,
        accessCode: String?,
        callback: (Boolean, String?, String?) -> Unit
    ) {
        val request = LockerControlRequest(action_type = actionType, user_id = userId, access_code = accessCode)
        apiService.controlLockerDoorAutoAssign(request).enqueue(object : Callback<LockerControlResponse> {
            override fun onResponse(
                call: Call<LockerControlResponse>,
                response: Response<LockerControlResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.message, null)
                } else {
                    callback(false, "Failed to auto-assign locker", null)
                }
            }

            override fun onFailure(call: Call<LockerControlResponse>, t: Throwable) {
                callback(false, null, t.message ?: "Network error")
            }
        })
    }

    /**
     * Assign a specific locker to a user
     */
    fun assignLockerToUser(
        doorId: String,
        userId: String,
        accessCode: String?,
        callback: (Boolean, String?) -> Unit
    ) {
        val request = LockerAssignRequest(user_id = userId, access_code = accessCode)
        apiService.assignLockerToUser(doorId, request).enqueue(object : Callback<LockerAssignResponse> {
            override fun onResponse(
                call: Call<LockerAssignResponse>,
                response: Response<LockerAssignResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.message)
                } else {
                    callback(false, "Failed to assign locker")
                }
            }

            override fun onFailure(call: Call<LockerAssignResponse>, t: Throwable) {
                callback(false, t.message ?: "Network error")
            }
        })
    }

    /**
     * Validate access code
     */
    fun validateAccessCode(
        accessCode: String,
        callback: (Boolean, String?, String?) -> Unit
    ) {
        val request = ValidateAccessCodeRequest(access_code = accessCode)
        apiService.validateAccessCode(request).enqueue(object : Callback<ValidateAccessCodeResponse> {
            override fun onResponse(
                call: Call<ValidateAccessCodeResponse>,
                response: Response<ValidateAccessCodeResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.user_id, response.body()!!.message)
                } else {
                    callback(false, null, "Invalid access code")
                }
            }

            override fun onFailure(call: Call<ValidateAccessCodeResponse>, t: Throwable) {
                callback(false, null, t.message ?: "Network error")
            }
        })
    }

    /**
     * Pick up item from locker
     */
    fun pickupItem(
        doorId: String,
        userId: String,
        accessCode: String?,
        callback: (Boolean, String?) -> Unit
    ) {
        val request = LockerSessionRequest(user_id = userId, access_code = accessCode, source = "apk")
        apiService.pickupItem(doorId, request).enqueue(object : Callback<LockerSessionResponse> {
            override fun onResponse(
                call: Call<LockerSessionResponse>,
                response: Response<LockerSessionResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.message)
                } else {
                    callback(false, "Failed to process pickup")
                }
            }

            override fun onFailure(call: Call<LockerSessionResponse>, t: Throwable) {
                callback(false, t.message ?: "Network error")
            }
        })
    }

    /**
     * End locker session
     */
    fun endLockerSession(
        doorId: String,
        userId: String,
        accessCode: String?,
        callback: (Boolean, String?) -> Unit
    ) {
        val request = LockerSessionRequest(user_id = userId, access_code = accessCode, source = "apk")
        apiService.endLockerSession(doorId, request).enqueue(object : Callback<LockerSessionResponse> {
            override fun onResponse(
                call: Call<LockerSessionResponse>,
                response: Response<LockerSessionResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.message)
                } else {
                    callback(false, "Failed to end session")
                }
            }

            override fun onFailure(call: Call<LockerSessionResponse>, t: Throwable) {
                callback(false, t.message ?: "Network error")
            }
        })
    }

    /**
     * Suspend version to get lockers (for use with coroutines)
     */
    suspend fun getLockersSuspend(clientId: String): List<Locker> = withContext(Dispatchers.IO) {
        try {
            val dtos: List<LockerApiResponse> = apiService.getLockerStatusesSuspend(clientId)
            val lockers = dtos.map { Locker.fromDto(it) }
            _lockers.value = lockers // Update state in suspend context
            lockers
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: LockerRepository? = null

        fun getInstance(): LockerRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LockerRepository().also { INSTANCE = it }
            }
        }
    }

    // Helper function to generate initial dummy lockers (if needed)
    private fun generateInitialLockers(): List<Locker> {
        return emptyList() // Or return some mock data if desired
    }
}