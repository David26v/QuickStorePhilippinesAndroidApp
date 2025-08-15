package data

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat

data class Locker(
    val id: Int,
    val doorId: String,
    val status: LockerStatus,
    val lastAccessTime: Long? = null,
    val assignedUser: AssignedUserInfo? = null,
    val location: String? = null
) {
    fun getStatusColor(): Int {
        return when (status) {
            LockerStatus.AVAILABLE -> Color.parseColor("#4CAF50")  // Rich green
            LockerStatus.OCCUPIED -> Color.parseColor("#FF9800")   // Orange
            LockerStatus.OVERDUE -> Color.parseColor("#F44336")    // Red
        }
    }




    fun getStatusText(): String {
        return when (status) {
            LockerStatus.AVAILABLE -> "Available"
            LockerStatus.OCCUPIED -> "Occupied"
            LockerStatus.OVERDUE -> "Maintenance"
        }
    }

    fun isAccessible(): Boolean {
        return true // Make all lockers accessible for now
    }

    // ✅ Add equals() for structural comparison
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Locker) return false

        return id == other.id &&
                doorId == other.doorId &&
                status == other.status &&
                lastAccessTime == other.lastAccessTime &&
                assignedUser == other.assignedUser &&
                location == other.location
    }

    // ✅ Add hashCode() to match equals()
    override fun hashCode(): Int {
        var result = id
        result = 31 * result + doorId.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + (lastAccessTime?.hashCode() ?: 0)
        result = 31 * result + (assignedUser?.hashCode() ?: 0)
        result = 31 * result + (location?.hashCode() ?: 0)
        return result
    }

    companion object {
        fun fromDto(dto: LockerApiResponse): Locker {
            val status = when (dto.status.lowercase()) {
                "occupied", "in_use", "locked" -> LockerStatus.OCCUPIED
                "overdue" -> LockerStatus.OVERDUE
                else -> LockerStatus.AVAILABLE
            }

            val assignedUser = dto.assignedUser?.let {
                AssignedUserInfo(  // ✅ Now exists
                    userId = it.userId,
                    firstName = it.firstName,
                    lastName = it.lastName
                )
            }

            return Locker(
                id = dto.id,
                doorId = dto.doorId,
                status = status,
                lastAccessTime = dto.lastAccessTime,
                assignedUser = assignedUser,
                location = dto.location
            )
        }
    }
}

// ✅ Fixed: Added equals() and hashCode() to AssignedUserInfo
data class AssignedUserInfo(
    val userId: String,
    val firstName: String,
    val lastName: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AssignedUserInfo) return false
        return userId == other.userId && firstName == other.firstName && lastName == other.lastName
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        return result
    }
}

