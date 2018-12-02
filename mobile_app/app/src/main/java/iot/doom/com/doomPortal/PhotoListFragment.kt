package iot.doom.com.doomPortal

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PhotoListFragment : Fragment(), OnListFragmentInteractionListener {

    lateinit var listView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        listView = inflater.inflate(R.layout.fragment_photo_list, container, false)

        if (listView is RecyclerView) {
            with(listView as RecyclerView) {
                layoutManager = LinearLayoutManager(context)
            }
        }
        return listView
    }

    @SuppressLint("HardwareIds")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DoomApi.instance.getWhitelist(Settings.Secure.getString(context!!.contentResolver, Settings.Secure.ANDROID_ID)).enqueue(
            object : Callback<List<DoomPhoto>> {
                override fun onFailure(call: Call<List<DoomPhoto>>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<List<DoomPhoto>>, response: Response<List<DoomPhoto>>) {
                    Log.d("Connection", response.raw().code().toString())
                    if (response.isSuccessful) {
                        with(listView as RecyclerView) {
                            adapter = PhotoRecyclerViewAdapter(response.body()!!, this@PhotoListFragment)
                        }
                    }
                    else this@PhotoListFragment.findNavController().navigate(R.id.action_disconnect)
                    //else Toast.makeText(context, response.code(), Toast.LENGTH_SHORT).show()// TODO exploit error
                }
            })
    }

    override fun onListFragmentInteraction(item: DoomPhoto, sharedImageView: ImageView) {
        val transitionName = ViewCompat.getTransitionName(sharedImageView)!!
        findNavController(view!!).navigate(R.id.photoListtoDetail,
            bundleOf("transition_name" to transitionName), null,
            FragmentNavigatorExtras(sharedImageView to transitionName)
        )
    }
}

interface OnListFragmentInteractionListener {
    fun onListFragmentInteraction(item: DoomPhoto, sharedImageView: ImageView)
}