package database.entity

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for interacting with the local Room database.
 * Supports offline-first operations and sync flags for cloud synchronization.
 */
@Dao
interface LockerDao {

    // ======================================================================
    // 🔹 LOCKER DOORS
    // ======================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockerDoor(doors: List<LocalLockerDoor>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleLockerDoor(door: LocalLockerDoor)

    @Update
    suspend fun updateLockerDoor(door: LocalLockerDoor)

    @Query("SELECT * FROM local_locker_doors WHERE id = :id LIMIT 1")
    suspend fun getLockerDoorById(id: String): LocalLockerDoor?

    @Query("SELECT * FROM local_locker_doors ORDER BY door_number")
    fun getAllDoors(): Flow<List<LocalLockerDoor>>

    @Query("SELECT * FROM local_locker_doors WHERE client_id = :clientId ORDER BY door_number")
    fun getDoorsByClient(clientId: String): Flow<List<LocalLockerDoor>>

    @Query("""
        SELECT * FROM local_locker_doors 
        WHERE status IN ('occupied', 'in_use', 'locked', 'overdue') 
        ORDER BY door_number
    """)
    fun getOccupiedDoors(): Flow<List<LocalLockerDoor>>

    @Query("SELECT * FROM local_locker_doors WHERE status IN (:statuses) ORDER BY door_number")
    fun getDoorsByStatuses(statuses: List<String>): Flow<List<LocalLockerDoor>>

    @Query("""
        SELECT * FROM local_locker_doors 
        WHERE is_locally_created = 1 OR is_locally_updated = 1 OR is_locally_deleted = 1
    """)
    suspend fun getUnsyncedDoors(): List<LocalLockerDoor>

    @Query("""
        UPDATE local_locker_doors 
        SET is_locally_created = 0, is_locally_updated = 0, is_locally_deleted = 0, sync_status = 1 
        WHERE id = :id
    """)
    suspend fun markDoorSynced(id: String)

    @Query("UPDATE local_locker_doors SET sync_status = 2 WHERE id = :id")
    suspend fun markDoorSyncFailed(id: String)


    // ======================================================================
    // 🔹 LOCKER SESSIONS
    // ======================================================================

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

    @Query("""
        SELECT * FROM local_locker_sessions 
        WHERE is_locally_created = 1 OR is_locally_updated = 1 OR is_locally_deleted = 1
    """)
    suspend fun getUnsyncedSessions(): List<LocalLockerSession>

    @Query("UPDATE local_locker_sessions SET sync_status = 1 WHERE id = :id")
    suspend fun markSessionSynced(id: String)

    @Query("UPDATE local_locker_sessions SET sync_status = 2 WHERE id = :id")
    suspend fun markSessionSyncFailed(id: String)


    // ======================================================================
    // 🔹 LOCKER DOOR EVENTS
    // ======================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockerDoorEvent(event: LocalLockerDoorEvent)

    @Query("""
        SELECT * FROM local_locker_door_events
        WHERE locker_door_id = :doorId
        ORDER BY created_at DESC
    """)
    suspend fun getEventsByDoorId(doorId: String): List<LocalLockerDoorEvent>

    @Query("""
        SELECT * FROM local_locker_door_events 
        WHERE is_locally_created = 1 OR is_locally_updated = 1 OR is_locally_deleted = 1
    """)
    suspend fun getUnsyncedEvents(): List<LocalLockerDoorEvent>

    @Query("UPDATE local_locker_door_events SET sync_status = 1 WHERE id = :id")
    suspend fun markEventSynced(id: String)

    @Query("UPDATE local_locker_door_events SET sync_status = 2 WHERE id = :id")
    suspend fun markEventSyncFailed(id: String)


    // ======================================================================
    // 🔹 USER CREDENTIALS
    // ======================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCredential(credential: LocalUserCredential)

    @Query("""
        SELECT * FROM local_user_credentials
        WHERE credential_hash = :credentialHash
        AND is_active = 1
        LIMIT 1
    """)
    suspend fun getActiveCredentialByHash(credentialHash: String): LocalUserCredential?

    @Query("""
        SELECT * FROM local_user_credentials 
        WHERE is_locally_created = 1 OR is_locally_updated = 1 OR is_locally_deleted = 1
    """)
    suspend fun getUnsyncedCredentials(): List<LocalUserCredential>

    @Query("UPDATE local_user_credentials SET sync_status = 1 WHERE id = :id")
    suspend fun markCredentialSynced(id: String)

    @Query("UPDATE local_user_credentials SET sync_status = 2 WHERE id = :id")
    suspend fun markCredentialSyncFailed(id: String)

    @Query("""
    UPDATE local_locker_doors
    SET assigned_user_id = :userId,
        assigned_user_first_name = :firstName,
        assigned_user_last_name = :lastName,
        status = 'occupied',
        is_locally_updated = 1,
        sync_status = 0,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = :doorId
""")
    suspend fun assignLockerLocally(
        doorId: String,
        userId: String,
        firstName: String?,
        lastName: String?
    )
}