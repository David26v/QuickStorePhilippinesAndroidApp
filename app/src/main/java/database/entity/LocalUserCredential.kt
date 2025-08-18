package database.entity

import androidx.room.*
import java.util.*

@Entity(tableName = "local_user_credentials")
@TypeConverters(Converters::class)
data class LocalUserCredential(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "method_type")
    val methodType: String,

    @ColumnInfo(name = "credential_hash")
    val credentialHash: String,

    @ColumnInfo(name = "user_full_name")
    val userFullName: String?,

    @ColumnInfo(name = "user_email")
    val userEmail: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "is_locally_created")
    val isLocallyCreated: Boolean = false,

    @ColumnInfo(name = "is_locally_updated")
    val isLocallyUpdated: Boolean = false,

    @ColumnInfo(name = "is_locally_deleted")
    val isLocallyDeleted: Boolean = false,

    @ColumnInfo(name = "sync_status")
    val syncStatus: Int = 0
)