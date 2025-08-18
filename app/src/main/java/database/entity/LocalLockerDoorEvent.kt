package database.entity

import androidx.room.*
import java.util.*


@Entity(tableName = "local_locker_door_events")
@TypeConverters(Converters::class)
data class LocalLockerDoorEvent(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "locker_door_id")
    val lockerDoorId: String?,

    @ColumnInfo(name = "user_id")
    val userId: String?,

    @ColumnInfo(name = "event_type")
    val eventType: String,

    @ColumnInfo(name = "source")
    val source: String,

    @ColumnInfo(name = "metadata")
    val metadata: String? = "{}",

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),

    // 🔽 Offline Sync Flags
    @ColumnInfo(name = "is_locally_created")
    val isLocallyCreated: Boolean = false,

    @ColumnInfo(name = "is_locally_updated")
    val isLocallyUpdated: Boolean = false,

    @ColumnInfo(name = "is_locally_deleted")
    val isLocallyDeleted: Boolean = false,

    @ColumnInfo(name = "sync_status")
    val syncStatus: Int = 0
)