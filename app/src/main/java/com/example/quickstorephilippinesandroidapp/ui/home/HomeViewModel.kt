package com.example.quickstorephilippinesandroidapp.ui.home

<<<<<<< Updated upstream
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
=======
import android.app.Application
import androidx.lifecycle.*
import com.example.quickstorephilippinesandroidapp.data.Locker
import com.example.quickstorephilippinesandroidapp.repository.LockerRepository
import com.example.quickstorephilippinesandroidapp.data.LockerStatus
import com.example.quickstorephilippinesandroidapp.data.AssignedUserInfo
import database.entity.LockerDatabase
import database.entity.LocalLockerDoor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
>>>>>>> Stashed changes

fun LocalLockerDoor.toDomain(): Locker {
    val statusEnum = when (status.lowercase()) {
        "occupied", "in_use", "locked" -> LockerStatus.OCCUPIED
        "overdue" -> LockerStatus.OVERDUE
        else -> LockerStatus.AVAILABLE
    }

<<<<<<< Updated upstream
=======
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
        .distinctUntilChanged() // Prevent identical DB lists
        .map { list -> list.map { it.toDomain() } }
        .distinctUntilChanged() // Prevent identical Locker lists
        .asLiveData(viewModelScope.coroutineContext)

>>>>>>> Stashed changes
    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to the Quick Store locker "
    }
    val text: LiveData<String> = _text
<<<<<<< Updated upstream
=======

    fun loadLockers(clientId: String) {
        val now = System.currentTimeMillis()
        if (now - lastSyncTime < MIN_SYNC_INTERVAL) return
        lastSyncTime = now

        currentClientId = clientId

        viewModelScope.launch {
            try {
                val remoteLockers: List<Locker> = repository.getLockersSuspend(clientId)

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

                if (entities.isNotEmpty()) {
                    dao.insertLockerDoor(entities) // ✅ Pass list directly
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Still show cached data
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
>>>>>>> Stashed changes
}