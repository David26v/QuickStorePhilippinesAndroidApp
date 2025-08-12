package database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import database.entity.Converters
import java.util.Date

@Entity(tableName = "local_locker_sessions")
@TypeConverters(Converters::class)
data class LocalLockerSession(
    @PrimaryKey val id: String, // uuid from Supabase
    val locker_door_id: String, // uuid reference
    val user_id: String, // uuid reference
    val start_time: Date,
    val end_time: Date? = null,
    val actual_end_time: Date? = null,
    val duration: String? = null, // interval as string
    val status: String = "active", // active, completed
    val created_at: Date = Date(),
    val updated_at: Date = Date()
)