package database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "local_locker_sessions")
@TypeConverters(Converters::class)
data class LocalLockerSession(
    @PrimaryKey val id: String,
    val locker_door_id: String,
    val user_id: String,
    val start_time: Date,
    val end_time: Date? = null,
    val actual_end_time: Date? = null,
    val duration: String? = null,
    val status: String = "active",
    val created_at: Date = Date(),
    val updated_at: Date = Date()
)
