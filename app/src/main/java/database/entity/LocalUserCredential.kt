package database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import database.entity.Converters
import java.util.Date

@Entity(tableName = "local_user_credentials")
@TypeConverters(Converters::class)
data class LocalUserCredential(
    @PrimaryKey val id: String,
    val user_id: String,
    val method_type: String,
    val credential_hash: String,
    val created_at: Date = Date(),
    val updated_at: Date = Date(),
    val is_active: Boolean = true
)
