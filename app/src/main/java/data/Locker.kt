package data

data class Locker(
    val id: Int,
    val status: LockerStatus,
    val lastAccessTime: Long? = null,
    val assignedUser: String? = null,
    val location: String? = null
) {
    fun getStatusColor(): Int {
        return when (status) {
            LockerStatus.AVAILABLE -> android.R.color.holo_green_light
            LockerStatus.OCCUPIED -> android.R.color.holo_red_light
            LockerStatus.OVERDUE -> android.R.color.holo_orange_light
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
        return status != LockerStatus.OVERDUE
    }
}
