// database/entity/LocalLockerDoor.kt
package database.entity

import androidx.room.*
import java.util.*

/**
 * Local mirror of Supabase `locker_doors` table.
 * Represents a single locker compartment with assignment and status tracking.
 */
@Entity(tableName = "local_locker_doors")
data class LocalLockerDoor(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "locker_id")
    val lockerId: String,

    @ColumnInfo(name = "door_number")
    val doorNumber: Int,

    @ColumnInfo(name = "status")
    val status: String = "available",

    @ColumnInfo(name = "assigned_user_id")
    val assignedUserId: String? = null,

    // Optional: First/last name can be derived, but stored for offline display
    @ColumnInfo(name = "assigned_user_first_name")
    val assignedUserFirstName: String? = null,

    @ColumnInfo(name = "assigned_user_last_name")
    val assignedUserLastName: String? = null,

    @ColumnInfo(name = "assigned_at")
    val assignedAt: Date? = null,

    @ColumnInfo(name = "client_id")
    val clientId: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),

    @ColumnInfo(name = "last_access_time")
    val lastAccessTime: Long? = null, // optional timestamp (e.g., from device)

    @ColumnInfo(name = "last_opened_at")
    val lastOpenedAt: Date? = null,

    @ColumnInfo(name = "control_metadata")
    val controlMetadata: String? = null, // Stored as JSON string (mirrors jsonb)

    @ColumnInfo(name = "assigned_guest_id")
    val assignedGuestId: String? = null,

    @ColumnInfo(name = "location")
    val location: String? = null, // Optional: useful for multi-location display

    // 🔽 Offline Sync Flags (not in Supabase, but stored locally)
    @ColumnInfo(name = "is_locally_created")
    val isLocallyCreated: Boolean = false,

    @ColumnInfo(name = "is_locally_updated")
    val isLocallyUpdated: Boolean = false,

    @ColumnInfo(name = "is_locally_deleted")
    val isLocallyDeleted: Boolean = false,

    @ColumnInfo(name = "sync_status")
    val syncStatus: Int = 0 // 0=pending, 1=synced, 2=failed
)