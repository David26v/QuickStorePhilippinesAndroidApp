package com.example.quickstorephilippinesandroidapp.data

import android.graphics.Color
import data.LockerApiResponse

data class Locker(
    val id: Int,
    val doorId: String,  // Add this field to store the actual database ID
    val status: LockerStatus,
    val lastAccessTime: Long? = null,
    val assignedUser: AssignedUserInfo? = null,
    val location: String? = null
) {
    fun getStatusColor(): Int {
        return when (status) {
            LockerStatus.AVAILABLE -> Color.parseColor("#4CAF50") // Green
            LockerStatus.OCCUPIED -> Color.parseColor("#F44336") // Red
            LockerStatus.OVERDUE -> Color.parseColor("#FF9800") // Orange
        }
    }

    fun getStatusText(): String {
        return when (status) {
            LockerStatus.AVAILABLE -> "Available"
            LockerStatus.OCCUPIED -> "Occupied"
            LockerStatus.OVERDUE -> "Overdue"
        }
    }

    fun isAccessible(): Boolean {
        return true // Make all lockers accessible for now
    }

    companion object {
        fun fromDto(dto: LockerApiResponse): Locker {
            // Convert API status string to enum
            val status = when (dto.status.lowercase()) {
                "occupied", "in_use", "locked" -> LockerStatus.OCCUPIED
                "overdue" -> LockerStatus.OVERDUE
                else -> LockerStatus.AVAILABLE
            }

            // Convert assigned user DTO to your model
            val assignedUser = dto.assignedUser?.let {
                AssignedUserInfo(
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

data class AssignedUserInfo(
    val userId: String,
    val firstName: String,
    val lastName: String
)

enum class LockerStatus {
    AVAILABLE,
    OCCUPIED,
    OVERDUE
}