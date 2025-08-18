package com.example.quickstorephilippinesandroidapp.api

import data.LockerApiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body

interface ApiService {
    @GET("api/lockers/locker-status/{client_id}")
    fun getLockerStatuses(@Path("client_id") clientId: String): Call<List<LockerApiResponse>>

    // ✅ Coroutine version
    @GET("api/lockers/locker-status/{client_id}")
    suspend fun getLockerStatusesSuspend(@Path("client_id") clientId: String): List<LockerApiResponse>

    @GET("api/lockers/client-from-locker/{locker_id}")
    fun getClientIdFromLocker(@Path("locker_id") lockerId: String): Call<ClientIdResponse>


    @GET("api/clients/get_client_methods/{client_id}")
    fun getClientAuthMethods(@Path("client_id") clientId: String): Call<ClientAuthMethodsResponse>

    @POST("api/locker-doors/control-door/{door_id}")
    fun controlLockerDoor(@Path("door_id") doorId: String, @Body controlRequest: LockerControlRequest): Call<LockerControlResponse>

    @POST("api/locker-doors/control-door-auto")
    fun controlLockerDoorAutoAssign(@Body controlRequest: LockerControlRequest): Call<LockerControlResponse>

    @POST("api/locker-doors/select-door/{door_id}")
    fun assignLockerToUser(@Path("door_id") doorId: String, @Body assignRequest: LockerAssignRequest): Call<LockerAssignResponse>

    @POST("api/locker-doors/validate-access-code")
    fun validateAccessCode(@Body request: ValidateAccessCodeRequest): Call<ValidateAccessCodeResponse>

    @POST("api/locker-doors/pick-up/{door_id}")
    fun pickupItem(@Path("door_id") doorId: String, @Body pickupRequest: LockerSessionRequest): Call<LockerSessionResponse>

    @POST("api/locker-doors/end-session/{door_id}")
    fun endLockerSession(@Path("door_id") doorId: String, @Body sessionRequest: LockerSessionRequest): Call<LockerSessionResponse>
}

// Keep your existing data classes
data class ClientIdResponse(
    val client_id: String
)

data class LockerControlRequest(
    val action_type: String,
    val user_id: String,
    val source: String = "apk",
    val access_code: String? = null
)

data class LockerControlResponse(
    val message: String,
    val status: String
)

data class ClientAuthMethodsResponse(
    val client_id: String,
    val auth_methods: List<String>
)

data class LockerAssignRequest(
    val user_id: String,
    val access_code: String? = null,
    val source: String = "apk"
)

data class LockerAssignResponse(
    val message: String,
    val door_id: String,
    val status: String
)

data class ValidateAccessCodeRequest(
    val access_code: String
)

data class ValidateAccessCodeResponse(
    val user_id: String,
    val message: String
)

// NEW DATA CLASSES:
data class LockerSessionRequest(
    val user_id: String,
    val access_code: String? = null,
    val source: String = "apk"
)

data class LockerSessionResponse(
    val message: String,
    val door_id: String? = null,
    val status: String? = null,
    val action: String? = null
)