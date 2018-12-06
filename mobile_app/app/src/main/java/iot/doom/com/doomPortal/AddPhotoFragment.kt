package iot.doom.com.doomPortal

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_add_photo.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddPhotoFragment : Fragment() {

    private val PICK_IMAGE = 100
    private val CAMERA_REQUEST = 1888

    private var photo: Bitmap? = null
    private var file: File? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        takePictureButton.setOnClickListener {
            val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST)
        }

        Log.d("Token",  PreferenceManager.getDefaultSharedPreferences(context).getString("fcm_token", null))

        importButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Choisir une image"), PICK_IMAGE)
        }

        ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

        addPhoto.setOnClickListener {
            if (file != null && !photoNameInput.text.isNullOrEmpty()) uploadImage()
        }
    }

    @SuppressLint("HardwareIds")
    fun uploadImage() {
        val filePart = MultipartBody.Part.createFormData(
            "picture",
            file?.name,
            RequestBody.create(MediaType.parse("image/*"), file!!)
        )

        val namePart = RequestBody.create(MediaType.parse("text/plain"), photoNameInput.text.toString())

        DoomApi.instance.addWhitelist(DoomApi.getDeviceId(context!!), filePart, namePart).enqueue(object : Callback<Unit> {
            override fun onFailure(call: Call<Unit>, t: Throwable) {

            }

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) findNavController().navigateUp()
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST -> {
                    photo = data?.extras?.get("data") as Bitmap
                }
                PICK_IMAGE -> {
                    val stream = context?.contentResolver?.openInputStream(data?.data!!)
                    photo = BitmapFactory.decodeStream(stream)
                    stream?.close()
                }
                else -> return
            }
            Glide.with(context!!).asBitmap().load(photo).into(pictureView)
            val tempUri = getImageUri(context!!, photo!!)

            // CALL THIS METHOD TO GET THE ACTUAL PATH
            file = File(getRealPathFromURI(tempUri))
            Log.d("DataPhoto", photo.toString())
        }
    }

    private fun getImageUri(inContext : Context, inImage: Bitmap) : Uri {
        val outImage = Bitmap.createScaledBitmap(inImage, 1000, 1000,true)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, outImage, "Title", null)
        return Uri.parse(path)
    }

    private fun getRealPathFromURI(uri: Uri): String {
        var path = ""
        if (context?.contentResolver != null) {
            val cursor = context?.contentResolver?.query(uri, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }
}
