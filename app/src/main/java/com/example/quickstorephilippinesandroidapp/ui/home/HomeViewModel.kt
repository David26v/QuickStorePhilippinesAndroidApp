package com.example.quickstorephilippinesandroidapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quickstorephilippinesandroidapp.data.Locker
import com.example.quickstorephilippinesandroidapp.repository.LockerRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val lockerRepository = LockerRepository()

    private val _lockers = MutableLiveData<List<Locker>>()
    val lockers: LiveData<List<Locker>> = _lockers

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to the Quick Store locker"
    }
    val text: LiveData<String> = _text

    private var currentClientId: String? = null

    fun loadLockers(clientId: String) {
        currentClientId = clientId
        lockerRepository.getLockers(clientId) { lockers ->
            _lockers.postValue(lockers)
        }
    }

    fun refreshLockers() {
        currentClientId?.let { clientId ->
            loadLockers(clientId)
        }
    }

    // Use this for controlling locker doors (instead of updateLockerStatus)
    fun controlLockerDoor(doorId: String, actionType: String, userId: String, accessCode: String?, callback: (Boolean, String?) -> Unit) {
        lockerRepository.controlLockerDoor(doorId, actionType, userId, accessCode, callback)
    }

    // Use this for getting client auth methods
    fun getClientAuthMethods(clientId: String, callback: (List<String>) -> Unit) {
        lockerRepository.getClientAuthMethods(clientId, callback)
    }

    // assign available doors
    fun controlLockerDoorAutoAssign(actionType: String, userId: String, accessCode: String?, callback: (Boolean, String?, String?) -> Unit) {
        lockerRepository.controlLockerDoorAutoAssign(actionType, userId, accessCode, callback)
    }

    // assign doors
    fun assignLockerToUser(doorId: String, userId: String, accessCode: String?, callback: (Boolean, String?) -> Unit) {
        lockerRepository.assignLockerToUser(doorId, userId, accessCode, callback)
    }

    // Add this method to your existing HomeViewModel class
    fun validateAccessCode(accessCode: String, callback: (Boolean, String?, String?) -> Unit) {
        lockerRepository.validateAccessCode(accessCode, callback)
    }

    // NEW METHODS:
    fun pickupItem(doorId: String, userId: String, accessCode: String?, callback: (Boolean, String?) -> Unit) {
        lockerRepository.pickupItem(doorId, userId, accessCode, callback)
    }

    fun endLockerSession(doorId: String, userId: String, accessCode: String?, callback: (Boolean, String?) -> Unit) {
        lockerRepository.endLockerSession(doorId, userId, accessCode, callback)
    }
}