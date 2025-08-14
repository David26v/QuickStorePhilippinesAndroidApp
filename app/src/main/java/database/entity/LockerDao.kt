package database.entity

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LockerDao {

    // ✅ Insert list of doors
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockerDoor(doors: List<LocalLockerDoor>)

    // ✅ Optional: Insert single door
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleLockerDoor(door: LocalLockerDoor)

    @Update
    suspend fun updateLockerDoor(door: LocalLockerDoor)

    @Query("SELECT * FROM local_locker_doors WHERE id = :id LIMIT 1")
    suspend fun getLockerDoorById(id: String): LocalLockerDoor?

    // Flow: All doors
    @Query("SELECT * FROM local_locker_doors")
    fun getAllDoors(): Flow<List<LocalLockerDoor>>

    // Flow: Occupied doors
    @Query("SELECT * FROM local_locker_doors WHERE status IN ('occupied','in_use','locked','overdue')")
    fun getOccupiedDoors(): Flow<List<LocalLockerDoor>>

    // Filter by statuses
    @Query("SELECT * FROM local_locker_doors WHERE status IN (:statuses)")
    fun getDoorsByStatuses(statuses: List<String>): Flow<List<LocalLockerDoor>>

    // --- Sessions ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockerSession(session: LocalLockerSession)

    @Update
    suspend fun updateLockerSession(session: LocalLockerSession)

    @Query("""
        SELECT * FROM local_locker_sessions
        WHERE locker_door_id = :doorId
        ORDER BY start_time DESC
        LIMIT 1
    """)
    suspend fun getLatestSessionByDoorId(doorId: String): LocalLockerSession?

    // --- Events ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockerDoorEvent(event: LocalLockerDoorEvent)

    @Query("""
        SELECT * FROM local_locker_door_events
        WHERE locker_door_id = :doorId
        ORDER BY created_at DESC
    """)
    suspend fun getEventsByDoorId(doorId: String): List<LocalLockerDoorEvent>

    // --- Credentials ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCredential(credential: LocalUserCredential)

    @Query("""
        SELECT * FROM local_user_credentials
        WHERE credential_hash = :credentialHash
        AND is_active = 1
        LIMIT 1
    """)


    suspend fun getActiveCredentialByHash(credentialHash: String): LocalUserCredential?
}