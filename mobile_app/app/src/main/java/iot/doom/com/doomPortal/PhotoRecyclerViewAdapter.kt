package iot.doom.com.doomPortal

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat


import iot.doom.com.doomPortal.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class PhotoRecyclerViewAdapter(
    private val mValues: List<DummyItem>,
    private val mListener: OnListFragmentInteractionListener
) : RecyclerView.Adapter<PhotoRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_photo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        // holder.mIdView.text = item.id
        // holder.mContentView.text = item.content
        ViewCompat.setTransitionName(holder.mImageView, "photo_" + item.id)

        with(holder.mView) {
            tag = item
            setOnClickListener {
                mListener.onListFragmentInteraction(item, holder.mImageView)
            }
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        // val mIdView: TextView = mView.item_number
        // val mContentView: TextView = mView.content
        val mImageView: ImageView = mView.findViewById(R.id.photo)

        override fun toString(): String {
            return super.toString()// + " '" + mContentView.text + "'"
        }
    }
}
