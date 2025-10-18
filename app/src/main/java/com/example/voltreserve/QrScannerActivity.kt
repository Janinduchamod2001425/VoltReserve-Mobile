package com.example.voltreserve

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.voltreserve.databinding.ActivityQrScannerBinding
import com.example.voltreserve.models.QrPayload
import com.google.gson.Gson
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory

class QrScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQrScannerBinding
    private val gson = Gson()
    private var handled = false

    private val requestCamera = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startScanner() else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.barcodeScanner.barcodeView.decoderFactory =
            DefaultDecoderFactory(listOf(com.google.zxing.BarcodeFormat.QR_CODE))

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startScanner()
        } else {
            requestCamera.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startScanner() {
        binding.barcodeScanner.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                if (result == null || handled) return
                var text = result.text ?: return
                text = text.trim()  // ⚡️ remove stray newlines/spaces

                try {
                    Log.d("QR_SCAN", "Scanned text: $text")

                    val payload = gson.fromJson(text, QrPayload::class.java)

                    // ✅ accept if have ID or NIC
                    if (payload == null ||
                        (payload.id.isNullOrBlank() && payload.nic.isNullOrBlank())
                    ) {
                        throw IllegalArgumentException("Missing id/nic")
                    }

                    handled = true
                    binding.barcodeScanner.pause()

                    val i = Intent(this@QrScannerActivity, ReservationDetailActivity::class.java)
                    i.putExtra("qr_payload_json", text)
                    startActivity(i)
                    finish()

                } catch (e: Exception) {
                    Log.e("QR_SCAN", "Parse error: ${e.message}")
                    Toast.makeText(
                        this@QrScannerActivity,
                        "Invalid QR format",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
        binding.barcodeScanner.resume()
    }

    override fun onPause() {
        binding.barcodeScanner.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (!handled) binding.barcodeScanner.resume()
    }

    override fun onDestroy() {
        binding.barcodeScanner.pause()
        super.onDestroy()
    }
}
