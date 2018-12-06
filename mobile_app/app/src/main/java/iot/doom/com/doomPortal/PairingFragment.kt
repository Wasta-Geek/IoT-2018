package iot.doom.com.doomPortal

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_auth.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PairingFragment : Fragment() {

    private lateinit var layoutView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layoutView = inflater.inflate(R.layout.fragment_auth, container, false)
        return layoutView
    }

    @SuppressLint("HardwareIds")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spannable = SpannableString(pairingButton.text.toString())

        val fontLeft = ResourcesCompat.getFont(context!!, R.font.amaz_doom_left)
        val fontRight = ResourcesCompat.getFont(context!!, R.font.amaz_doom_right)

        spannable.setSpan(CustomTypefaceSpan(fontLeft), 0,   (pairingButton.text.length / 2F).toInt(), Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        spannable.setSpan(CustomTypefaceSpan(fontRight),  (pairingButton.text.length / 2F).toInt(), pairingButton.text.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        pairingButton.text = spannable

        val myAnim = AnimationUtils.loadAnimation(context!!, R.anim.bounce)
        pairingButton.startAnimation(myAnim)

        pairingButton.setOnClickListener {
            //it.visibility = View.GONE
            //progressBar.visibility = View.VISIBLE
            DoomApi.instance.pairDevice(PairingRequest(DoomApi.getDeviceId(context!!))).enqueue(
                object : Callback<Unit> {
                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        Log.d("Connection", response.raw().code().toString())
                        if (response.isSuccessful) findNavController().navigate(R.id.action_successful_pairing)
                        //else Toast.makeText(context, response.code(), Toast.LENGTH_SHORT).show()// TODO exploit error
                    }

                })
        }
    }
}