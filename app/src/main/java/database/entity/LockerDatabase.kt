package database.entity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

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
@TypeConverters(Converters::class) // No need for full package path
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
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
