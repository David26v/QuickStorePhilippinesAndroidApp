package data

data class LockerApiResponse(
    val id: Int,
    val doorId: String,
    val status: String,
    val lastAccessTime: Long?,
    val assignedUser: AssignedUserDto?,
    val location: String?
)

