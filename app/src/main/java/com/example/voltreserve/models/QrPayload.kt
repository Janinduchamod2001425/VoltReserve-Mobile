package com.example.voltreserve.models

data class QrPayload(
    val id: String? = null,
    val nic: String? = null,
    val stationId: String? = null,
    val station: String? = null,
    val slot: Int? = null,
    val date: String? = null,
    val start: String? = null,
    val end: String? = null,
    val status: String? = null
)
