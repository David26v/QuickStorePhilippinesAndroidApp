
package database.entity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalLockerRepository(private val lockerDao: LockerDao) {

    // ======================================================================
    // 🔹 LOCKER DOORS
    // ======================================================================

    /**
     * Insert or update a locker door.
     * If created locally (e.g. during offline assignment), mark it for sync.
     */
    suspend fun saveLockerDoor(door: LocalLockerDoor) = withContext(Dispatchers.IO) {
        lockerDao.insertSingleLockerDoor(door)
    }

    /**
     * Update an existing locker door (e.g., status change).
     * Automatically mark as locally updated if not already synced.
     */
    suspend fun updateLockerDoor(door: LocalLockerDoor) = withContext(Dispatchers.IO) {
        val updatedDoor = if (!door.isLocallyCreated && door.syncStatus == 0) {
            // Not new, but modified offline → mark for sync
            door.copy(isLocallyUpdated = true)  // ✅ works if LocalLockerDoor is a data class
        } else door
        lockerDao.updateLockerDoor(updatedDoor)
    }

    /**
     * Mark a door as deleted locally (soft delete + sync later).
     */
    suspend fun deleteLockerDoor(id: String) = withContext(Dispatchers.IO) {
        val door = lockerDao.getLockerDoorById(id)  // ✅ now correct
        if (door != null) {
            val deleted = door.copy(
                isLocallyDeleted = true,
                isLocallyUpdated = false,
                syncStatus = 0
            )
            lockerDao.updateLockerDoor(deleted)
        }
    }

    /**
     * Get a specific door by ID.
     */
    suspend fun getLockerDoorById(id: String): LocalLockerDoor? = withContext(Dispatchers.IO) {
        lockerDao.getLockerDoorById(id)
    }

    /**
     * Observe all occupied/in-use doors in real-time.
     */
    fun getOccupiedDoors() = lockerDao.getOccupiedDoors()

    /**
     * Get all doors belonging to a client.
     */
    fun getDoorsByClient(clientId: String) = lockerDao.getDoorsByClient(clientId)

    /**
     * Get all doors with specific statuses.
     */
    fun getDoorsByStatuses(statuses: List<String>) = lockerDao.getDoorsByStatuses(statuses)

    /**
     * Get all unsynced doors (created/updated/deleted while offline).
     */
    suspend fun getUnsyncedDoors(): List<LocalLockerDoor> = withContext(Dispatchers.IO) {
        lockerDao.getUnsyncedDoors()
    }

    /**
     * Mark door as successfully synced.
     */
    suspend fun markDoorSynced(id: String) = withContext(Dispatchers.IO) {
        lockerDao.markDoorSynced(id)
    }

    /**
     * Mark sync attempt as failed (retry later).
     */
    suspend fun markDoorSyncFailed(id: String) = withContext(Dispatchers.IO) {
        lockerDao.markDoorSyncFailed(id)
    }


    // ======================================================================
    // 🔹 LOCKER SESSIONS
    // ======================================================================

    /**
     * Save a new locker session.
     * Mark as locally created if we expect to sync it later.
     */
    suspend fun saveLockerSession(session: LocalLockerSession) = withContext(Dispatchers.IO) {
        val sessionToSave = if (session.isLocallyCreated) session else {
            session.copy(isLocallyCreated = true, syncStatus = 0)
        }
        lockerDao.insertLockerSession(sessionToSave)
    }

    /**
     * Update session (e.g., set end time).
     */
    suspend fun updateLockerSession(session: LocalLockerSession) = withContext(Dispatchers.IO) {
        lockerDao.updateLockerSession(session)
    }

    /**
     * Get latest session for a door.
     */
    suspend fun getLatestSessionByDoorId(doorId: String): LocalLockerSession? = withContext(Dispatchers.IO) {
        lockerDao.getLatestSessionByDoorId(doorId)
    }

    /**
     * Get all sessions that need syncing.
     */
    suspend fun getUnsyncedSessions(): List<LocalLockerSession> = withContext(Dispatchers.IO) {
        lockerDao.getUnsyncedSessions()
    }

    /**
     * Mark session as synced.
     */
    suspend fun markSessionSynced(id: String) = withContext(Dispatchers.IO) {
        lockerDao.markSessionSynced(id)
    }


    // ======================================================================
    // 🔹 LOCKER DOOR EVENTS
    // ======================================================================

    /**
     * Save a new event (e.g., opened, closed).
     */
    suspend fun saveLockerDoorEvent(event: LocalLockerDoorEvent) = withContext(Dispatchers.IO) {
        lockerDao.insertLockerDoorEvent(event)
    }

    /**
     * Get all events for a specific door.
     */
    suspend fun getEventsByDoorId(doorId: String): List<LocalLockerDoorEvent> = withContext(Dispatchers.IO) {
        lockerDao.getEventsByDoorId(doorId)
    }


    // ======================================================================
    // 🔹 USER CREDENTIALS
    // ======================================================================

    /**
     * Save user credential (e.g., access code).
     * Mark as locally created if new.
     */
    suspend fun saveUserCredential(credential: LocalUserCredential) = withContext(Dispatchers.IO) {
        val credToSave = if (credential.isLocallyCreated) credential else {
            credential.copy(isLocallyCreated = true, syncStatus = 0)
        }
        lockerDao.insertUserCredential(credToSave)
    }

    // ======================================================================
// 🔹 USER CREDENTIAL VALIDATION
// ======================================================================

    /**
     * Validate an access code against stored credentials.
     * Returns true if valid & active, false otherwise.
     */
    suspend fun validateAccessCode(credentialHash: String): Boolean = withContext(Dispatchers.IO) {
        val credential = lockerDao.getActiveCredentialByHash(credentialHash)
        credential != null && credential.isActive
    }

    /**
     * Find active credential by hash (used for access code validation).
     */
    suspend fun getActiveCredentialByHash(credentialHash: String): LocalUserCredential? = withContext(Dispatchers.IO) {
        lockerDao.getActiveCredentialByHash(credentialHash)
    }

    /**
     * Get all credentials pending sync.
     */
    suspend fun getUnsyncedCredentials(): List<LocalUserCredential> = withContext(Dispatchers.IO) {
        lockerDao.getUnsyncedCredentials()
    }

    /**
     * Mark credential as synced.
     */
    suspend fun markCredentialSynced(id: String) = withContext(Dispatchers.IO) {
        lockerDao.markCredentialSynced(id)
    }
}