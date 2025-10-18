package com.example.voltreserve.models
data class UpdateBookingRequest(
    val stationId: String? = null,
    val stationName: String? = null,
    val type: String? = null,
    val selectedSlot: Int? = null,
    val reservationDate: String? = null, // ISO8601 UTC (optional)
    val startTime: String? = null,       // "HH:mm:ss" (optional)
    val endTime: String? = null,         // "HH:mm:ss" (optional)
    val status: String? = null           // e.g., "Completed"
)
