package repository

import data.Locker
import data.LockerStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LockerRepository {

    private val _lockers = MutableStateFlow(generateInitialLockers())
    val lockers: StateFlow<List<Locker>> = _lockers

    private fun generateInitialLockers(): List<Locker> {
        return (1..24).map { id ->
            val status = when {
                id % 8 == 0 -> LockerStatus.OCCUPIED
                id == 7 || id == 15 || id == 23 -> LockerStatus.OVERDUE
                else -> LockerStatus.AVAILABLE
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
}