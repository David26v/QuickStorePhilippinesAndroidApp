package com.example.quickstorephilippinesandroidapp

import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import models.DeviceRequest
import models.DeviceResponse
import models.DeviceInfoResponse
import api.ApiClient
import network.DeviceService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    companion object {
        var CLIENT_ID: String? = null
        var DEVICE_ID: String? = null

        // Add this listener for client ID availability
        var onClientIdAvailable: (() -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        setContentView(R.layout.activity_main)
        registerAndFetchDeviceInfo()
    }

    private fun registerAndFetchDeviceInfo() {
        try {
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            DEVICE_ID = deviceId
            Log.d("MainActivity", "Device ID: $deviceId")

            val manufacturer = Build.MANUFACTURER ?: "Unknown"
            val model = Build.MODEL ?: "Unknown"
            val androidVersion = Build.VERSION.RELEASE ?: "Unknown"

            val request = DeviceRequest(
                device_id = deviceId,
                manufacturer = manufacturer,
                model = model,
                android_version = androidVersion,
                locker_id = null
            )

            val service = ApiClient.instance.create(DeviceService::class.java)

            // First register the device
            service.registerDevice(request).enqueue(object : Callback<DeviceResponse> {
                override fun onResponse(call: Call<DeviceResponse>, response: Response<DeviceResponse>) {
                    Log.d("DeviceRegister", "Registration response code: ${response.code()}")
                    if (response.isSuccessful) {
                        Log.d("DeviceRegister", "Registration Success: ${response.body()?.message}")
                        fetchDeviceInfo(deviceId)
                    } else {
                        Log.e("DeviceRegister", "Registration failed: ${response.code()} ${response.message()}")
                        fetchDeviceInfo(deviceId)
                    }
                }

                override fun onFailure(call: Call<DeviceResponse>, t: Throwable) {
                    Log.e("DeviceRegister", "Registration network error: ${t.localizedMessage}", t)
                    fetchDeviceInfo(deviceId)
                }
            })
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in registerAndFetchDeviceInfo", e)
        }
    }

    private fun fetchDeviceInfo(deviceId: String) {
        try {
            val service = ApiClient.instance.create(DeviceService::class.java)

            service.getDeviceInfo(deviceId).enqueue(object : Callback<DeviceInfoResponse> {
                override fun onResponse(call: Call<DeviceInfoResponse>, response: Response<DeviceInfoResponse>) {
                    Log.d("DeviceInfo", "Response code: ${response.code()}")
                    Log.d("DeviceInfo", "Is successful: ${response.isSuccessful}")

                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        Log.d("DeviceInfo", "Full response: $responseBody")

                        if (responseBody != null) {
                            // Extract client ID properly
                            val clientId = responseBody.deviceInfo?.client_id
                            Log.d("DeviceInfo", "Extracted client ID: $clientId")

                            if (clientId != null && clientId.isNotEmpty()) {
                                CLIENT_ID = clientId
                                Log.d("DeviceInfo", "✅ Client ID successfully stored in companion object: $CLIENT_ID")
                                // Notify listeners that client ID is available
                                onClientIdAvailable?.invoke()
                            } else {
                                Log.e("DeviceInfo", "❌ Client ID is null or empty")
                                Log.d("DeviceInfo", "Available data - device_id: ${responseBody.deviceInfo?.device_id}, locker_id: ${responseBody.deviceInfo?.locker_id}")
                            }
                        } else {
                            Log.e("DeviceInfo", "Response body is null")
                        }
                    } else {
                        Log.e("DeviceInfo", "Server error: ${response.code()} ${response.message()}")
                        try {
                            Log.e("DeviceInfo", "Error body: ${response.errorBody()?.string()}")
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }

                override fun onFailure(call: Call<DeviceInfoResponse>, t: Throwable) {
                    Log.e("DeviceInfo", "Network error getting device info: ${t.localizedMessage}", t)
                }
            })
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in fetchDeviceInfo", e)
        }
    }
}