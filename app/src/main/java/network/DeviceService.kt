package network


import models.Device
import models.DeviceInfoResponse
import models.DeviceRequest
import models.DeviceResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

interface DeviceService {
    @POST("api/devices/register-device")
    fun registerDevice(@Body device: DeviceRequest): Call<DeviceResponse>

    @GET("/api/devices/getDeviceInfo")
    fun getDeviceInfo(@Query("device_id") deviceId: String): Call<DeviceInfoResponse>
}