package database.entity

import database.entity.LocalLockerDoor
import database.entity.LocalLockerSession
import database.entity.LocalLockerDoorEvent
import database.entity.LocalUserCredential
import database.entity.LockerDao

class LocalLockerRepository(private val lockerDao: LockerDao) {

    // Locker Doors
    suspend fun saveLockerDoor(door: LocalLockerDoor) = lockerDao.insertLockerDoor(door)
    suspend fun updateLockerDoor(door: LocalLockerDoor) = lockerDao.updateLockerDoor(door)
    suspend fun getLockerDoorById(id: String) = lockerDao.getLockerDoorById(id)
    fun getOccupiedDoors() = lockerDao.getOccupiedDoors()

    // Locker Sessions
    suspend fun saveLockerSession(session: LocalLockerSession) = lockerDao.insertLockerSession(session)
    suspend fun updateLockerSession(session: LocalLockerSession) = lockerDao.updateLockerSession(session)
    suspend fun getLatestSessionByDoorId(doorId: String) = lockerDao.getLatestSessionByDoorId(doorId)

    // Locker Door Events
    suspend fun saveLockerDoorEvent(event: LocalLockerDoorEvent) = lockerDao.insertLockerDoorEvent(event)
    suspend fun getEventsByDoorId(doorId: String) = lockerDao.getEventsByDoorId(doorId)

    // User Credentials
    suspend fun saveUserCredential(credential: LocalUserCredential) = lockerDao.insertUserCredential(credential)
    suspend fun getActiveCredentialByHash(credentialHash: String) = lockerDao.getActiveCredentialByHash(credentialHash)
}