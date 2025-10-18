package com.example.voltreserve.client

import android.content.Context
import com.example.voltreserve.helpers.SessionDbHelper
import com.example.voltreserve.services.AuthApiService
import com.example.voltreserve.services.OwnerApiService
import com.example.voltreserve.services.BookingApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.43.170:5029/"

    // ---------- PUBLIC RETROFIT ----------
    private val publicRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val ownerPublic: OwnerApiService by lazy {
        publicRetrofit.create(OwnerApiService::class.java)
    }

    val staffPublic: AuthApiService by lazy {
        publicRetrofit.create(AuthApiService::class.java)
    }

    // ---------- AUTHENTICATED OWNER ----------
    fun ownerAuthed(context: Context): OwnerApiService {
        val db = SessionDbHelper(context)
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                db.getSession()?.let { builder.addHeader("Authorization", "Bearer $it") }
                chain.proceed(builder.build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OwnerApiService::class.java)
    }

    // ---------- AUTHENTICATED STAFF ----------
    fun staffAuthed(context: Context): AuthApiService {
        val db = SessionDbHelper(context)
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val b = chain.request().newBuilder()
                db.getSession()?.let { b.addHeader("Authorization", "Bearer $it") }
                chain.proceed(b.build())
            }.build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    // ---------- AUTHENTICATED BOOKING (for QR scan + mark completed) ----------
    fun bookingAuthed(context: Context): BookingApiService {
        val db = SessionDbHelper(context)
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                db.getSession()?.let { builder.addHeader("Authorization", "Bearer $it") }
                chain.proceed(builder.build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BookingApiService::class.java)
    }
}
