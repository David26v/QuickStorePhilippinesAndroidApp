package database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "local_locker_doors")
data class LocalLockerDoor(
    @PrimaryKey val id: String,
    val lockerId: String,
    val doorNumber: Int,
    val status: String = "available",
    val assignedUserId: String? = null,
    val assignedAt: Date? = null,
    val clientId: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val lastOpenedAt: Date? = null,
    val controlMetadata: String? = null,
    val assignedGuestId: String? = null
)