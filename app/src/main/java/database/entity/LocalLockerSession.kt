package database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "local_locker_sessions")
data class LocalLockerSession(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "locker_door_id")
    val lockerDoorId: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "start_time")
    val startTime: Date,

    @ColumnInfo(name = "end_time")
    val endTime: Date?,

    @ColumnInfo(name = "actual_end_time")
    val actualEndTime: Date?,

    @ColumnInfo(name = "duration")
    val duration: String?,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),

    // Sync flags
    @ColumnInfo(name = "is_locally_created")
    val isLocallyCreated: Boolean = false,

    @ColumnInfo(name = "is_locally_updated")
    val isLocallyUpdated: Boolean = false,

    @ColumnInfo(name = "is_locally_deleted")
    val isLocallyDeleted: Boolean = false,

    @ColumnInfo(name = "sync_status")
    val syncStatus: Int = 0
)