// DeviceInfoResponse.kt
package models

data class DeviceInfoResponse(
    val deviceInfo: DeviceInfo?
)

data class DeviceInfo(
    val device_id: String,
    val client_id: String?,
    val locker_id: String?,
    val lockers: LockerInfo?
)

data class LockerInfo(
    val id: String,
    val client_id: String
)