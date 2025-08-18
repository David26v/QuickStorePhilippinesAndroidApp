// File: ui/home/HomeViewModel.kt

package com.example.quickstorephilippinesandroidapp.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import database.entity.LockerDatabase
import database.entity.LocalLockerDoor
import database.entity.LocalLockerRepository
import data.Locker
import data.LockerStatus
import data.AssignedUserInfo
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import repository.LockerRepository
import java.util.*

// Domain → Data Mapper
fun LocalLockerDoor.toDomain(): Locker {
    Log.d("HomeViewModel", "Converting door: $id, status: $status, assignedUser: $assignedUserId")

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
        id = lockerId?.toIntOrNull() ?: -1,
        doorId = id,
        status = statusEnum,
        lastAccessTime = lastAccessTime,
        assignedUser = assignedUser,
        location = location
    )
}

// Data → Domain Mapper
fun Locker.toLocal(clientId: String?): LocalLockerDoor {
    return LocalLockerDoor(
        id = doorId,
        lockerId = id.takeIf { it != -1 }?.toString() ?: "0",
        doorNumber = id.takeIf { it != -1 } ?: 0,
        status = when (status) {
            LockerStatus.OCCUPIED -> "occupied"
            LockerStatus.OVERDUE -> "overdue"
            LockerStatus.AVAILABLE -> "available"
        },
        assignedUserId = assignedUser?.userId,
        assignedUserFirstName = assignedUser?.firstName,
        assignedUserLastName = assignedUser?.lastName,
        assignedAt = lastAccessTime?.let { Date(it) },
        clientId = clientId ?: "unknown_client",
        createdAt = Date(),
        updatedAt = Date(),
        lastAccessTime = lastAccessTime,
        lastOpenedAt = null,
        controlMetadata = null,
        assignedGuestId = null,
        location = location,

        isLocallyCreated = false,
        isLocallyUpdated = false,
        isLocallyDeleted = false,
        syncStatus = 1
    )
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = LockerDatabase.getDatabase(application).lockerDao()
    private val localRepository = LocalLockerRepository(dao)
    private val repository = LockerRepository()

    private var currentClientId: String? = null
    private var lastSyncTime = 0L
    private val MIN_SYNC_INTERVAL = 5_000L // 5 seconds

    // ✅ Observe local data in real-time (Room + Flow)
    val lockers: LiveData<List<Locker>> = dao.getAllDoors()
        .map { list: List<LocalLockerDoor> ->
            list
                .sortedBy { it.doorNumber }
                .map { it.toDomain() }
                .also { domainList ->
                    Log.d("HomeViewModel", "🔁 Mapped ${domainList.size} local doors to UI models")
                }
        }
        .asLiveData(viewModelScope.coroutineContext)

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to the Quick Store locker"
    }
    val text: LiveData<String> = _text

    /**
     * Load lockers: always show local data, sync remote in background
     */
    fun loadLockers(clientId: String) {
        val now = System.currentTimeMillis()
        if (now - lastSyncTime < MIN_SYNC_INTERVAL) return
        lastSyncTime = now

        currentClientId = clientId

        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "🔁 Starting sync with API for client: $clientId")
                val remoteLockers = repository.getLockersSuspend(clientId)
                Log.d("HomeViewModel", "✅ Fetched ${remoteLockers.size} lockers from API")

                // 🔄 Merge: update local only if no pending offline changes
                for (locker in remoteLockers) {
                    val local = localRepository.getLockerDoorById(locker.doorId)

                    if (local == null || shouldUpdateLocal(local, locker)) {
                        val entity = locker.toLocal(clientId)
                        localRepository.saveLockerDoor(entity)
                        Log.d("HomeViewModel", "💾 Saved/updated door: ${entity.id}, status: ${entity.status}")
                    }
                }

                // 🔁 Sync local changes back to server
                syncLocalChanges()

            } catch (e: Exception) {
                Log.e("HomeViewModel", "☁️ API sync failed (offline?) - showing local data only", e)
            }
        }
    }

    /**
     * Decide whether remote data should overwrite local.
     * Prevents losing local offline changes.
     */
    private fun shouldUpdateLocal(local: LocalLockerDoor, remote: Locker): Boolean {
        if (local.isLocallyUpdated || local.isLocallyCreated) {
            Log.d("HomeViewModel", "⏭️ Skipping sync for ${local.id} — has pending local changes")
            return false
        }
        return true
    }

    /**
     * Sync local changes (e.g., assigned doors) back to server
     */
    private suspend fun syncLocalChanges() {
        val unsyncedDoors = localRepository.getUnsyncedDoors()
        Log.d("HomeViewModel", "📤 Found ${unsyncedDoors.size} unsynced doors to upload")

        for (door in unsyncedDoors) {
            try {
                repository.assignLockerToUser(
                    doorId = door.id,
                    userId = door.assignedUserId ?: continue,
                    accessCode = null
                ) { success, response ->
                    viewModelScope.launch {
                        if (success) {
                            localRepository.markDoorSynced(door.id)
                            Log.d("HomeViewModel", "✅ Synced door: ${door.id}")
                        } else {
                            Log.w("HomeViewModel", "❌ Failed to sync door: ${door.id}, will retry later")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ Failed to sync door ${door.id}", e)
            }
        }
    }

    /**
     * Force refresh from API
     */
    fun refreshLockers() {
        currentClientId?.let { loadLockers(it) }
    }

    // --- Business Logic Delegated to Repository ---

    fun controlLockerDoor(
        doorId: String,
        actionType: String,
        userId: String,
        accessCode: String?,
        callback: (Boolean, String?) -> Unit
    ) {
        repository.controlLockerDoor(doorId, actionType, userId, accessCode, callback)
    }

    fun getClientAuthMethods(clientId: String, callback: (List<String>) -> Unit) {
        repository.getClientAuthMethods(clientId, callback)
    }

    fun controlLockerDoorAutoAssign(
        actionType: String,
        userId: String,
        accessCode: String?,
        callback: (Boolean, String?, String?) -> Unit
    ) {
        repository.controlLockerDoorAutoAssign(actionType, userId, accessCode, callback)
    }

    fun assignLockerToUser(
        doorId: String,
        userId: String,
        accessCode: String,
        callback: (Boolean, Any?) -> Unit
    ) {
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