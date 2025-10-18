// services/BookingApiService.kt
package com.example.voltreserve.services

import com.example.voltreserve.models.ReservationDto
import retrofit2.Response
import retrofit2.http.*

data class UpdateReservationRequest(
    val stationId: String? = null,
    val stationName: String? = null,
    val type: String? = null,
    val selectedSlot: Int? = null,
    val reservationDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val status: String? = null
)

interface BookingApiService {
    @GET("api/Booking/{id}")
    suspend fun getById(@Path("id") id: String): Response<ReservationDto>

    @GET("api/Booking/by-nic/{nic}")
    suspend fun getByNic(@Path("nic") nic: String): Response<List<ReservationDto>>

    @PUT("api/Booking/{id}")
    suspend fun update(@Path("id") id: String, @Body req: UpdateReservationRequest): Response<ReservationDto>
}
