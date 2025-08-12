package database.entity

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LockerDao {

    // LocalLockerDoor Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockerDoor(door: LocalLockerDoor): Long

    @Update
    suspend fun updateLockerDoor(door: LocalLockerDoor): Int

    @Query("SELECT * FROM local_locker_doors WHERE id = :id")
    suspend fun getLockerDoorById(id: String): LocalLockerDoor?

    @Query("SELECT * FROM local_locker_doors WHERE status = 'occupied' OR status = 'overdue'")
    fun getOccupiedDoors(): Flow<List<LocalLockerDoor>> // ✅ Flow is now imported

    // LocalLockerSession Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockerSession(session: LocalLockerSession): Long

    @Update
    suspend fun updateLockerSession(session: LocalLockerSession): Int

    @Query("SELECT * FROM local_locker_sessions WHERE locker_door_id = :doorId ORDER BY start_time DESC LIMIT 1")
    suspend fun getLatestSessionByDoorId(doorId: String): LocalLockerSession?

    // LocalLockerDoorEvent Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockerDoorEvent(event: LocalLockerDoorEvent): Long

    @Query("SELECT * FROM local_locker_door_events WHERE locker_door_id = :doorId ORDER BY created_at DESC")
    suspend fun getEventsByDoorId(doorId: String): List<LocalLockerDoorEvent>

    // LocalUserCredential Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCredential(credential: LocalUserCredential): Long

    @Query("SELECT * FROM local_user_credentials WHERE credential_hash = :credentialHash AND is_active = true")
    suspend fun getActiveCredentialByHash(credentialHash: String): LocalUserCredential?
}
