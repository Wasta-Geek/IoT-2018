package iot.doom.com.doomPortal

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface DoomApi {

    @POST("pair")
    fun pairDevice(@Body request: PairingRequest): Call<Unit>

    @POST("unlock")
    fun authorize(@Header("Authorization") deviceId: String, @Body request: AuthorizeRequest): Call<Unit>

    @GET("whitelist")
    fun getWhitelist(@Header("Authorization") deviceId: String): Call<List<DoomPhoto>>

    @Multipart
    @POST("whitelist")
    fun addWhitelist(@Header("Authorization") deviceId: String, @Part filePart: MultipartBody.Part,
                     @Part("name") name: RequestBody): Call<Unit>

    companion object {

        val DEVICE_ID = "fcm_token"

        fun getDeviceId(context: Context) = PreferenceManager.getDefaultSharedPreferences(context).getString(DEVICE_ID, "")
        fun setDeviceId(context: Context, token: String) =
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(DEVICE_ID, token).apply()

        val instance: DoomApi by lazy {
            val apiUrl = "http://192.168.0.11:5050/"

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