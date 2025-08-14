package database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "local_locker_door_events")
@TypeConverters(Converters::class)
data class LocalLockerDoorEvent(
    @PrimaryKey val id: String,
    val locker_door_id: String?,
    val user_id: String?,
    val event_type: String,
    val source: String,
    val metadata: String? = "{}",
    val created_at: Date = Date()
)
