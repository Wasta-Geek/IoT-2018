package iot.doom.com.doomPortal

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_photo_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PhotoDetailActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_detail)
    }

    override fun onStart() {
        super.onStart()
        val photo = intent.getStringExtra("url")
        Glide.with(this)
            .load(photo)
            .into(main_backdrop)

        allowAuthButton.setOnClickListener { authorize(true) }

        refuseAuthButton.setOnClickListener { authorize(false) }
    }

    private fun authorize(boolean: Boolean) {
        DoomApi.instance.authorize(DoomApi.getDeviceId(this), AuthorizeRequest(boolean)).enqueue(
            object : Callback<Unit> {
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.d("Fail", t.message)
                }

                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    Log.d("Connection", response.raw().code().toString())
                    if (response.isSuccessful) {
                        val nMgr = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        nMgr.cancel(0)
                        finish()
                    }
                }

            })
    }

}
