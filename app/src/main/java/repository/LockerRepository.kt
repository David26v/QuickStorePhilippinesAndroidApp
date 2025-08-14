package repository

<<<<<<< Updated upstream
import data.Locker
import data.LockerStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
=======
import api.ApiClient
import com.example.quickstorephilippinesandroidapp.api.*
import com.example.quickstorephilippinesandroidapp.data.Locker
import data.LockerApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
>>>>>>> Stashed changes

class LockerRepository {

    private val _lockers = MutableStateFlow(generateInitialLockers())
    val lockers: StateFlow<List<Locker>> = _lockers

<<<<<<< Updated upstream
    private fun generateInitialLockers(): List<Locker> {
        return (1..24).map { id ->
            val status = when {
                id % 8 == 0 -> LockerStatus.OCCUPIED
                id == 7 || id == 15 || id == 23 -> LockerStatus.OVERDUE
                else -> LockerStatus.AVAILABLE
=======
    fun getLockers(clientId: String, callback: (List<Locker>) -> Unit) {
        apiService.getLockerStatuses(clientId).enqueue(object : Callback<List<LockerApiResponse>> {
            override fun onResponse(call: Call<List<LockerApiResponse>>, response: Response<List<LockerApiResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val lockerList = response.body()!!.map { Locker.fromDto(it) }
                    callback(lockerList)
                } else {
                    callback(emptyList())
                }
>>>>>>> Stashed changes
            }

            Locker(
                id = id,
                status = status,
                lastAccessTime = if (status == LockerStatus.OCCUPIED) System.currentTimeMillis() else null,
                assignedUser = if (status == LockerStatus.OCCUPIED) "User${id}" else null,
                location = "Row ${(id - 1) / 6 + 1}, Column ${(id - 1) % 6 + 1}"
            )
        }
    }

<<<<<<< Updated upstream
    fun getLocker(id: Int): Locker? {
        return _lockers.value.find { it.id == id }
    }

    fun updateLockerStatus(lockerId: Int, newStatus: LockerStatus, assignedUser: String? = null): Boolean {
        val currentLockers = _lockers.value.toMutableList()
        val lockerIndex = currentLockers.indexOfFirst { it.id == lockerId }

        if (lockerIndex != -1) {
            val currentLocker = currentLockers[lockerIndex]
            val updatedLocker = currentLocker.copy(
                status = newStatus,
                lastAccessTime = System.currentTimeMillis(),
                assignedUser = if (newStatus == LockerStatus.OCCUPIED) assignedUser else null
            )

            currentLockers[lockerIndex] = updatedLocker
            _lockers.value = currentLockers
            return true
        }
        return false
    }

    fun getAvailableLockers(): List<Locker> {
        return _lockers.value.filter { it.status == LockerStatus.AVAILABLE }
    }

    fun getOccupiedLockers(): List<Locker> {
        return _lockers.value.filter { it.status == LockerStatus.OCCUPIED }
    }

    fun getMaintenanceLockers(): List<Locker> {
        return _lockers.value.filter { it.status == LockerStatus.OVERDUE }
    }

    fun getStatusCounts(): Triple<Int, Int, Int> {
        val lockers = _lockers.value
        val available = lockers.count { it.status == LockerStatus.AVAILABLE }
        val occupied = lockers.count { it.status == LockerStatus.OCCUPIED }
        val maintenance = lockers.count { it.status == LockerStatus.OVERDUE }
        return Triple(available, occupied, maintenance)
    }


    suspend fun refreshFromServer(): Boolean {
        // TODO: Implement actual API call
        // For now, just simulate some random changes
        val currentLockers = _lockers.value.toMutableList()

        // Randomly change some statuses (simulation)
        currentLockers.indices.random().let { index ->
            val locker = currentLockers[index]
            if (locker.status == LockerStatus.AVAILABLE && Math.random() < 0.1) {
                currentLockers[index] = locker.copy(
                    status = LockerStatus.OCCUPIED,
                    assignedUser = "SimUser${System.currentTimeMillis() % 1000}",
                    lastAccessTime = System.currentTimeMillis()
                )
=======
    fun getClientAuthMethods(clientId: String, callback: (List<String>) -> Unit) {
        apiService.getClientAuthMethods(clientId).enqueue(object : Callback<ClientAuthMethodsResponse> {
            override fun onResponse(call: Call<ClientAuthMethodsResponse>, response: Response<ClientAuthMethodsResponse>) {
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

    fun controlLockerDoor(doorId: String, actionType: String, userId: String, accessCode: String?, callback: (Boolean, String?) -> Unit) {
        val request = LockerControlRequest(action_type = actionType, user_id = userId, access_code = accessCode)
        apiService.controlLockerDoor(doorId, request).enqueue(object : Callback<LockerControlResponse> {
            override fun onResponse(call: Call<LockerControlResponse>, response: Response<LockerControlResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.message)
                } else {
                    callback(false, "Failed to control locker door")
                }
            }

            override fun onFailure(call: Call<LockerControlResponse>, t: Throwable) {
                callback(false, t.message)
            }
        })
    }

    fun controlLockerDoorAutoAssign(actionType: String, userId: String, accessCode: String?, callback: (Boolean, String?, String?) -> Unit) {
        val request = LockerControlRequest(action_type = actionType, user_id = userId, access_code = accessCode)
        apiService.controlLockerDoorAutoAssign(request).enqueue(object : Callback<LockerControlResponse> {
            override fun onResponse(call: Call<LockerControlResponse>, response: Response<LockerControlResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.message, null)
                } else {
                    callback(false, "Failed to auto-assign locker", null)
                }
            }

            override fun onFailure(call: Call<LockerControlResponse>, t: Throwable) {
                callback(false, null, t.message)
            }
        })
    }

    fun assignLockerToUser(doorId: String, userId: String, accessCode: String?, callback: (Boolean, String?) -> Unit) {
        val request = LockerAssignRequest(user_id = userId, access_code = accessCode)
        apiService.assignLockerToUser(doorId, request).enqueue(object : Callback<LockerAssignResponse> {
            override fun onResponse(call: Call<LockerAssignResponse>, response: Response<LockerAssignResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.message)
                } else {
                    callback(false, "Failed to assign locker")
                }
            }

            override fun onFailure(call: Call<LockerAssignResponse>, t: Throwable) {
                callback(false, t.message)
            }
        })
    }

    fun validateAccessCode(accessCode: String, callback: (Boolean, String?, String?) -> Unit) {
        val request = ValidateAccessCodeRequest(access_code = accessCode)
        apiService.validateAccessCode(request).enqueue(object : Callback<ValidateAccessCodeResponse> {
            override fun onResponse(call: Call<ValidateAccessCodeResponse>, response: Response<ValidateAccessCodeResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.user_id, response.body()!!.message)
                } else {
                    callback(false, null, "Invalid access code")
                }
            }

            override fun onFailure(call: Call<ValidateAccessCodeResponse>, t: Throwable) {
                callback(false, null, t.message)
            }
        })
    }

    fun pickupItem(doorId: String, userId: String, accessCode: String?, callback: (Boolean, String?) -> Unit) {
        val request = LockerSessionRequest(user_id = userId, access_code = accessCode, source = "apk")
        apiService.pickupItem(doorId, request).enqueue(object : Callback<LockerSessionResponse> {
            override fun onResponse(call: Call<LockerSessionResponse>, response: Response<LockerSessionResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.message)
                } else {
                    callback(false, "Failed to process pickup")
                }
            }

            override fun onFailure(call: Call<LockerSessionResponse>, t: Throwable) {
                callback(false, t.message)
            }
        })
    }

    fun endLockerSession(doorId: String, userId: String, accessCode: String?, callback: (Boolean, String?) -> Unit) {
        val request = LockerSessionRequest(user_id = userId, access_code = accessCode, source = "apk")
        apiService.endLockerSession(doorId, request).enqueue(object : Callback<LockerSessionResponse> {
            override fun onResponse(call: Call<LockerSessionResponse>, response: Response<LockerSessionResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    callback(true, response.body()!!.message)
                } else {
                    callback(false, "Failed to end session")
                }
            }

            override fun onFailure(call: Call<LockerSessionResponse>, t: Throwable) {
                callback(false, t.message)
>>>>>>> Stashed changes
            }
        }

        _lockers.value = currentLockers
        return true
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

    // SUSPEND VERSION for use with coroutines
    suspend fun getLockersSuspend(clientId: String): List<Locker> = withContext(Dispatchers.IO) {
        val dtos: List<LockerApiResponse> = apiService.getLockerStatusesSuspend(clientId)
        dtos.map { Locker.fromDto(it) }
    }
}
