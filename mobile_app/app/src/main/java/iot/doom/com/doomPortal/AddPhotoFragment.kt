package iot.doom.com.doomPortal

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_add_photo.*
import android.content.Intent
import android.provider.Settings
import android.util.Log
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddPhotoFragment : Fragment() {

    private val PICK_IMAGE = 100
    private val CAMERA_REQUEST = 1888

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

        importButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Choisir une image"), PICK_IMAGE)
        }

        addPhoto.setOnClickListener {
            // TODO send to server

            findNavController().navigateUp()
        }
    }

    /*fun uploadImage() {

        // create RequestBody instance from file
        val requestFile = RequestBody.create(
            MediaType.parse(getContentResolver().getType(fileUri)),
            file
        )

        // MultipartBody.Part is used to send also the actual file name
        val body = MultipartBody.Part.createFormData("picture", file.getName(), requestFile)

        // add another part within the multipart request
        val descriptionString = "hello, this is description speaking"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("picture", "")
            .addFormDataPart("name", photoNameInput.text.toString())
            .build()

        DoomApi.instance.addWhitelist(Settings.Secure.getString(context!!.contentResolver, Settings.Secure.ANDROID_ID), requestBody).enqueue(object : Callback<Unit> {
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE || requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("DataPhoto", data?.data?.toString())
            }
        }
    }
}
