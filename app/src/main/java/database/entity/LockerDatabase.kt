// File: database/entity/LockerDatabase.kt

package database.entity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import migration.MIGRATION_1_2


@Database(
    entities = [
        LocalLockerDoor::class,
        LocalLockerSession::class,
        LocalLockerDoorEvent::class,
        LocalUserCredential::class
    ],
    version = 3,
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
                )
                    .addMigrations(MIGRATION_1_2)
                    // Wipe and rebuild on schema change — safe because cloud backup
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}