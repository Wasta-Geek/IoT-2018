package iot.doom.com.doomPortal

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface DoomApi {

    @POST("pair")
    fun pairDevice(@Body request: PairingRequest): Call<Unit>

    @POST("unlock")
    fun authorize(@Header("Authorization") deviceId: String, @Body request: AuthorizeRequest): Call<Unit>

    @GET("whitelist")
    fun getWhitelist(@Header("Authorization") deviceId: String): Call<List<DoomPhoto>>

    @POST("whitelist")
    fun addWhitelist(@Header("Authorization") deviceId: String, @Body request: MultipartBody): Call<Unit>

    companion object {
        val instance: DoomApi by lazy {
            val apiUrl = "http://192.168.0.14:5050/"

            val okHttpClient: OkHttpClient = OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build()

            val gson: Gson = GsonBuilder().create()

            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(apiUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build()

            retrofit.create(DoomApi::class.java)
        }
    }
}