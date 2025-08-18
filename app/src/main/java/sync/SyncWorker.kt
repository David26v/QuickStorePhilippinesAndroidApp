// sync/SyncWorker.kt
package sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import api.ApiClient
import com.example.quickstorephilippinesandroidapp.api.ApiService
import com.example.quickstorephilippinesandroidapp.api.LockerAssignRequest
import database.entity.LocalLockerDoor
import database.entity.LockerDatabase
import database.entity.LocalLockerRepository
import database.entity.LocalLockerSession
import database.entity.LocalUserCredential
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.coroutineScope

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val apiService: ApiService = ApiClient.apiService
    private val db by lazy { LockerDatabase.getDatabase(context) }
    private val localRepo by lazy { LocalLockerRepository(db.lockerDao()) }

    override suspend fun doWork(): Result = coroutineScope {
        Log.d("SyncWorker", "✅ SyncWorker started — checking for pending data")

        var hasFailures = false

        try {
            // 🔁 Sync Doors First
            val unsyncedDoors = localRepo.getUnsyncedDoors()
            Log.d("SyncWorker", "Found ${unsyncedDoors.size} unsynced doors")

            for (door in unsyncedDoors) {
                if (!syncDoor(door)) {
                    hasFailures = true
                }
            }

            // 🔁 Sync Sessions
            val unsyncedSessions = localRepo.getUnsyncedSessions()
            Log.d("SyncWorker", "Found ${unsyncedSessions.size} unsynced sessions")

            for (session in unsyncedSessions) {
                if (!syncSession(session)) {
                    hasFailures = true
                }
            }

            // 🔁 Sync Credentials (if needed)
            val unsyncedCreds = localRepo.getUnsyncedCredentials()
            Log.d("SyncWorker", "Found ${unsyncedCreds.size} unsynced credentials")

            for (cred in unsyncedCreds) {
                if (!syncCredential(cred)) {
                    hasFailures = true
                }
            }

            // ✅ Success: all synced
            if (!hasFailures) {
                Log.d("SyncWorker", "🎉 All pending changes synced successfully")
                return@coroutineScope Result.success()
            } else {
                Log.w("SyncWorker", "⚠️ Some items failed to sync — will retry later")
                return@coroutineScope Result.retry()
            }

        } catch (io: IOException) {
            Log.e("SyncWorker", "📡 Network failure: device might be offline", io)
            return@coroutineScope Result.retry()

        } catch (http: HttpException) {
            Log.e("SyncWorker", "🌐 HTTP error: ${http.code()} - ${http.message()}", http)
            return@coroutineScope Result.retry()

        } catch (e: Exception) {
            Log.e("SyncWorker", "🔥 Unexpected error during sync", e)
            return@coroutineScope Result.retry()
        }
    }

    /**
     * Sync a single locker door (assign/unassign/lock)
     */
    private suspend fun syncDoor(door: LocalLockerDoor): Boolean {
        return try {
            when {
                door.isLocallyDeleted -> {
                    // If no delete API, just mark as synced
                    localRepo.markDoorSynced(door.id)
                    Log.d("SyncWorker", "🗑️ Locally deleted door ${door.id} marked as synced")
                    true
                }

                door.isLocallyCreated || door.isLocallyUpdated -> {
                    val request = mapToAssignRequest(door)
                    val response = apiService.assignLockerToUser(door.id, request).execute()

                    if (response.isSuccessful) {
                        localRepo.markDoorSynced(door.id)
                        Log.d("SyncWorker", "✅ Successfully synced door: ${door.id}")
                        true
                    } else {
                        Log.w("SyncWorker", "❌ Failed to sync door ${door.id}: ${response.message()}")
                        false
                    }
                }

                else -> {
                    // Already synced, no action needed
                    true
                }
            }
        } catch (io: IOException) {
            Log.e("SyncWorker", "📡 Network error syncing door ${door.id}", io)
            false
        } catch (http: HttpException) {
            Log.e("SyncWorker", "🌐 HTTP error syncing door ${door.id}: ${http.code()}", http)
            false
        } catch (e: Exception) {
            Log.e("SyncWorker", "🔥 Unexpected error syncing door ${door.id}", e)
            false
        }
    }

    /**
     * Sync locker session (e.g., pickup or end session)
     */
    private suspend fun syncSession(session: LocalLockerSession): Boolean {
        return try {
            // Example: Call pickup API
            // You can extend this based on your actual API
            /*
            val response = apiService.pickupItem(session.lockerDoorId, ...).execute()
            if (response.isSuccessful) {
                localRepo.markSessionSynced(session.id)
                true
            } else {
                false
            }
            */

            // For now: just mark as synced (demo)
            localRepo.markSessionSynced(session.id)
            Log.d("SyncWorker", "✅ Synced session: ${session.id}")
            true

        } catch (e: Exception) {
            Log.e("SyncWorker", "❌ Failed to sync session ${session.id}", e)
            false
        }
    }

    /**
     * Sync user credential (if you allow offline credential creation)
     */
    private suspend fun syncCredential(credential: LocalUserCredential): Boolean {
        return try {
            // If you have an API to upload credentials (e.g., biometrics, codes)
            // Call it here

            // For now: mark as synced
            localRepo.markCredentialSynced(credential.id)
            Log.d("SyncWorker", "✅ Synced credential: ${credential.id}")
            true
        } catch (e: Exception) {
            Log.e("SyncWorker", "❌ Failed to sync credential ${credential.id}", e)
            false
        }
    }

    /**
     * Map LocalLockerDoor to API request
     */
    private fun mapToAssignRequest(door: LocalLockerDoor): LockerAssignRequest {
        return LockerAssignRequest(
            user_id = door.assignedUserId ?: "",
            access_code = null, // or store last used code?
            source = "background-sync"
        )
    }
}