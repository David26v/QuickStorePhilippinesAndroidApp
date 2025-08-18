package migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {

        // --- Modify local_locker_doors ---
        database.execSQL("ALTER TABLE local_locker_doors ADD COLUMN door_number INTEGER")
        database.execSQL("ALTER TABLE local_locker_doors ADD COLUMN client_id TEXT")
        database.execSQL("ALTER TABLE local_locker_doors ADD COLUMN location TEXT")
        database.execSQL("ALTER TABLE local_locker_doors ADD COLUMN control_metadata TEXT")
        database.execSQL("ALTER TABLE local_locker_doors ADD COLUMN assigned_guest_id TEXT")
        database.execSQL("ALTER TABLE local_locker_doors ADD COLUMN last_access_time INTEGER")

        // --- Sync columns (add to all tables) ---
        listOf(
            "local_user_credentials",
            "local_locker_doors",
            "local_locker_sessions",
            "local_locker_door_events"
        ).forEach { table ->
            database.execSQL("ALTER TABLE $table ADD COLUMN is_locally_created INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE $table ADD COLUMN is_locally_updated INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE $table ADD COLUMN is_locally_deleted INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE $table ADD COLUMN sync_status INTEGER NOT NULL DEFAULT 0")
        }

        // --- Create indices ---
        database.execSQL("CREATE INDEX IF NOT EXISTS index_local_locker_doors_sync_status ON local_locker_doors(sync_status)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_local_locker_door_events_sync_status ON local_locker_door_events(sync_status)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_local_locker_sessions_sync_status ON local_locker_sessions(sync_status)")
    }
}