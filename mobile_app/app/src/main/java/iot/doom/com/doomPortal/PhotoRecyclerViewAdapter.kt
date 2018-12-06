package iot.doom.com.doomPortal

import android.content.Context
import android.util.Base64
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide

class PhotoRecyclerViewAdapter(
    private val mValues: List<DoomPhoto>,
    private val mListener: OnListFragmentInteractionListener
) : RecyclerView.Adapter<PhotoRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_photo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        if (item.photoUrl != null) {
            Glide.with(holder.context)
                .load(item.photoUrl)
                .into(holder.mImageView)
        } else {
            val photo = Base64.decode(item.photoBase64, Base64.DEFAULT)
            if (photo != null) {
                Glide.with(holder.context)
                    .asBitmap()
                    .load(photo)
                    .into(holder.mImageView)
            }
        }
        holder.mTextView.text = item.name
        ViewCompat.setTransitionName(holder.mImageView, "photo_" + item.name)

        with(holder.mView) {
            tag = item
            setOnClickListener {
                mListener.onListFragmentInteraction(item, holder.mImageView)
            }
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val context: Context = mView.context
        val mImageView: ImageView = mView.findViewById(R.id.photo)
        val mTextView: TextView = mView.findViewById(R.id.photoName)
    }
}
