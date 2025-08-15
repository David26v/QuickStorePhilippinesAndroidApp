package com.example.quickstorephilippinesandroidapp.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import data.Locker
import repository.LockerRepository
import data.LockerStatus
import data.AssignedUserInfo
import database.entity.LockerDatabase
import database.entity.LocalLockerDoor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

fun LocalLockerDoor.toDomain(): Locker {
    Log.d("HomeViewModel", "Converting door: $id, status: $status, assignedUser: $assignedUserId")  // 🔥 Add this

    val statusEnum = when (status.lowercase()) {
        "occupied", "in_use", "locked" -> LockerStatus.OCCUPIED
        "overdue" -> LockerStatus.OVERDUE
        else -> LockerStatus.AVAILABLE
    }

    val assignedUser = assignedUserId?.let {
        AssignedUserInfo(
            userId = it,
            firstName = assignedUserFirstName ?: "",
            lastName = assignedUserLastName ?: ""
        )
    }

    return Locker(
        id = lockerId.toIntOrNull() ?: -1,
        doorId = id,
        status = statusEnum,
        lastAccessTime = lastAccessTime,
        assignedUser = assignedUser,
        location = location
    )
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LockerRepository()
    private val dao = LockerDatabase.getDatabase(application).lockerDao()

    private var currentClientId: String? = null
    private var lastSyncTime = 0L
    private val MIN_SYNC_INTERVAL = 5_000L // 5 seconds

    // ✅ Live data from Room with deduplication
    val lockers: LiveData<List<Locker>> = dao.getAllDoors()
        .map { list ->
            list.map { it.toDomain() }.also {
                Log.d("HomeViewModel", "🔁 Mapped ${list.size} doors to domain models")
            }
        }
        .asLiveData(viewModelScope.coroutineContext)

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to the Quick Store locker "
    }
    val text: LiveData<String> = _text

    fun loadLockers(clientId: String) {
        val now = System.currentTimeMillis()
        if (now - lastSyncTime < MIN_SYNC_INTERVAL) return
        lastSyncTime = now

        currentClientId = clientId

        viewModelScope.launch {
            try {
                val remoteLockers: List<Locker> = repository.getLockersSuspend(clientId)
                Log.d("HomeViewModel", "✅ Fetched ${remoteLockers.size} lockers from API")  // 🔥 Add this

                val entities = remoteLockers.map { locker ->
                    LocalLockerDoor(
                        id = locker.doorId,
                        lockerId = locker.id.toString(),
                        doorNumber = locker.id,
                        status = when (locker.status) {
                            LockerStatus.OCCUPIED -> "occupied"
                            LockerStatus.OVERDUE -> "overdue"
                            else -> "available"
                        },
                        assignedUserId = locker.assignedUser?.userId,
                        assignedUserFirstName = locker.assignedUser?.firstName,
                        assignedUserLastName = locker.assignedUser?.lastName,
                        assignedAt = locker.lastAccessTime?.let { Date(it) },
                        clientId = clientId,
                        createdAt = Date(),
                        updatedAt = Date(),
                        lastAccessTime = locker.lastAccessTime,
                        lastOpenedAt = null,
                        controlMetadata = null,
                        assignedGuestId = null,
                        location = locker.location
                    )
                }

                Log.d("HomeViewModel", "💾 Inserting ${entities.size} lockers into Room")  // 🔥 Add this
                if (entities.isNotEmpty()) {
                    dao.insertLockerDoor(entities)
                } else {
                    Log.w("HomeViewModel", "⚠️ No lockers to insert — remote list is empty")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ Error fetching lockers", e)  // 🔥 Add full error
                e.printStackTrace()
            }
        }
    }

    fun refreshLockers() {
        currentClientId?.let { loadLockers(it) }
    }

    // --- Other business logic ---

    fun controlLockerDoor(doorId: String, actionType: String, userId: String, accessCode: String?, callback: (Boolean, String?) -> Unit) {
        repository.controlLockerDoor(doorId, actionType, userId, accessCode, callback)
    }

    fun getClientAuthMethods(clientId: String, callback: (List<String>) -> Unit) {
        repository.getClientAuthMethods(clientId, callback)
    }

    fun controlLockerDoorAutoAssign(actionType: String, userId: String, accessCode: String?, callback: (Boolean, String?, String?) -> Unit) {
        repository.controlLockerDoorAutoAssign(actionType, userId, accessCode, callback)
    }

    fun assignLockerToUser(doorId: String, userId: String, accessCode: String, callback: (Boolean, Any?) -> Unit) {
        repository.assignLockerToUser(doorId, userId, accessCode, callback)
    }

    fun validateAccessCode(accessCode: String, callback: (Boolean, String?, String?) -> Unit) {
        repository.validateAccessCode(accessCode, callback)
    }

    fun pickupItem(doorId: String, userId: String, accessCode: String?, callback: (Boolean, String?) -> Unit) {
        repository.pickupItem(doorId, userId, accessCode, callback)
    }

    fun endLockerSession(doorId: String, userId: String, accessCode: String?, callback: (Boolean, String?) -> Unit) {
        repository.endLockerSession(doorId, userId, accessCode, callback)
    }
}