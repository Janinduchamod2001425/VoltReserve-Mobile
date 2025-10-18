// ReservationDetailActivity.kt
package com.example.voltreserve

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.voltreserve.client.RetrofitClient
import com.example.voltreserve.databinding.ActivityReservationDetailBinding
import com.example.voltreserve.models.QrPayload
import com.example.voltreserve.models.ReservationDto
import com.example.voltreserve.services.UpdateReservationRequest
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class ReservationDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReservationDetailBinding
    private val gson = Gson()

    private var bookingId: String? = null
    private var qr: QrPayload? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val raw = intent.getStringExtra("qr_payload_json").orEmpty()
        val okJson = try { JsonParser.parseString(raw).asJsonObject; true } catch (_: Exception) { false }
        if (!okJson) {
            Toast.makeText(this, "No QR data", Toast.LENGTH_LONG).show()
            finish(); return
        }
        qr = gson.fromJson(raw, QrPayload::class.java)

        val nic = qr?.nic
        if (nic.isNullOrBlank()) {
            Toast.makeText(this, "QR missing NIC/email", Toast.LENGTH_LONG).show()
            finish(); return
        }

        val id = qr?.id
        if (!id.isNullOrBlank()) {
            loadById(id)
        } else {
            resolveByNicComposite(nic)
        }

        binding.btnMarkCompleted.setOnClickListener {
            bookingId?.let { markCompleted(it) }
        }
    }

    private fun loadById(id: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.bookingAuthed(this@ReservationDetailActivity)
                val res = api.getById(id)
                if (res.isSuccessful && res.body() != null) {
                    bookingId = id
                    bind(res.body()!!)
                } else {
                    Toast.makeText(this@ReservationDetailActivity, "Reservation not found", Toast.LENGTH_LONG).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ReservationDetailActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun resolveByNicComposite(nic: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.bookingAuthed(this@ReservationDetailActivity)
                val listRes = api.getByNic(nic)
                val items = listRes.body().orEmpty()

                val stationIdOrName = qr?.stationId?.ifBlank { null } ?: qr?.station
                val slot = qr?.slot
                val dayString = qr?.date

                val match = items.firstOrNull { r ->
                    val stationOk = when {
                        !stationIdOrName.isNullOrBlank() ->
                            r.stationId.equals(stationIdOrName, true) ||
                                    r.stationName.equals(stationIdOrName, true)
                        else -> true
                    }
                    val slotOk = slot?.let { r.selectedSlot == it } ?: true

                    val dayOk = dayString?.let {
                        val qrDay = safeDay(it)
                        val rDay = safeDay(r.reservationDate)
                        qrDay == null || rDay == null || qrDay == rDay
                    } ?: true

                    val statusOk = r.status.equals("Approved", true) ||
                            r.status.equals(qr?.status ?: "", true)

                    stationOk && slotOk && dayOk && statusOk
                }

                if (match != null) {
                    bookingId = match.id
                    bind(match)
                } else {
                    Toast.makeText(this@ReservationDetailActivity, "No matching reservation for this QR", Toast.LENGTH_LONG).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ReservationDetailActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun bind(r: ReservationDto) {
        binding.tvTitle.text = "Reservation #${r.id}"
        binding.tvStatus.text = "Status: ${r.status}"
        binding.tvStation.text = "Station: ${r.stationName} (${r.stationId})"
        binding.tvSlot.text = "Slot: ${r.selectedSlot}  Type: ${r.type}"
        binding.tvWhen.text = "Date: ${r.reservationDate}  Window: ${r.startTime}-${r.endTime}"

        binding.btnMarkCompleted.isEnabled =
            !bookingId.isNullOrBlank() && r.status.equals("Approved", true)
    }

    private fun markCompleted(id: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.bookingAuthed(this@ReservationDetailActivity)
                val res = api.update(id, UpdateReservationRequest(status = "Completed"))
                if (res.isSuccessful && res.body() != null) {
                    Toast.makeText(this@ReservationDetailActivity, "Marked as Completed", Toast.LENGTH_SHORT).show()
                    bind(res.body()!!)
                } else {
                    Toast.makeText(this@ReservationDetailActivity, "Failed to update status", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ReservationDetailActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Return yyyy-MM-dd if we can extract it from a variety of strings, else null. */
    private fun safeDay(x: String?): String? {
        if (x.isNullOrBlank()) return null

        // 1) Quick regex pull of yyyy-MM-dd inside any ISO string
        val m = Regex("""\d{4}-\d{2}-\d{2}""").find(x)
        if (m != null) return m.value

        // 2) Try a couple of common non-ISO formats (adjust if your QR uses others)
        val formats = listOf(
            "dd/MM/yyyy",
            "MM/dd/yyyy",
            "dd-MM-yyyy",
            "MM-dd-yyyy"
        )
        for (f in formats) {
            try {
                val sdf = SimpleDateFormat(f, Locale.US)
                sdf.isLenient = false
                val d = sdf.parse(x)
                if (d != null) {
                    val out = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    out.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    return out.format(d)
                }
            } catch (_: ParseException) { /* try next */ }
        }
        return null
    }
}
