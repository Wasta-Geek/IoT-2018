package iot.doom.com.doomPortal

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras

import iot.doom.com.doomPortal.dummy.DummyContent

class PhotoListFragment : Fragment(), OnListFragmentInteractionListener {

    private var columnCount = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = PhotoRecyclerViewAdapter(DummyContent.ITEMS, this@PhotoListFragment)
            }
        }
        return view
    }

    override fun onListFragmentInteraction(item: DummyContent.DummyItem, sharedImageView: ImageView) {
        val transitionName = ViewCompat.getTransitionName(sharedImageView)!!
        findNavController(view!!).navigate(R.id.photoListtoDetail,
            bundleOf("transition_name" to transitionName), null,
            FragmentNavigatorExtras(sharedImageView to transitionName)
        )
    }
}

interface OnListFragmentInteractionListener {
    fun onListFragmentInteraction(item: DummyContent.DummyItem, sharedImageView: ImageView)
}