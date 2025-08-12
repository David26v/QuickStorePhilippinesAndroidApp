package database.entity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import database.entity.LocalLockerDoor
import database.entity.LocalLockerSession
import database.entity.LocalLockerDoorEvent
import database.entity.LocalUserCredential
import database.entity.Converters


@Database(
    entities = [
        LocalLockerDoor::class,
        LocalLockerSession::class,
        LocalLockerDoorEvent::class,
        LocalUserCredential::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LockerDatabase : RoomDatabase() {

    abstract fun lockerDao(): LockerDao

    companion object {
        @Volatile
        private var INSTANCE: LockerDatabase? = null

        fun getDatabase(context: Context): LockerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LockerDatabase::class.java,
                    "locker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}