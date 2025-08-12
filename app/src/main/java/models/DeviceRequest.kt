package models

data class DeviceRequest(
    val device_id: String,
    val manufacturer: String,
    val model: String,
    val android_version: String,
    val locker_id: Int? = null,
)
