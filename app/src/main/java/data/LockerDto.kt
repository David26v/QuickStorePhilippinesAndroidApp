package data

data class LockerDto(
    val id: Int,
    val doorId: String,
    val status: String,
    val lastAccessTime: Long?,
    val assignedUser: AssignedUserDto?,
    val location: String?
)

data class AssignedUserDto(
    val userId: String,
    val firstName: String,
    val lastName: String
)