package com.aliashraf.assignment2

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class AddMentorActivity : AppCompatActivity() {
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImageUri: Uri
    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var availableEditText: EditText
    private lateinit var PostionEditText: EditText
    private var notificationId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_mentor)

        val uploadBtn: Button = findViewById(R.id.submitBtn)
        val openGallery : ImageView = findViewById(R.id.iconCam)

        openGallery.setOnClickListener {
            openGalleryForImage()
        }

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        selectedImageUri = uri

                    }
                }
            }
        uploadBtn.setOnClickListener {
            nameEditText = findViewById(R.id.editText)
            descriptionEditText = findViewById(R.id.editText2)
            availableEditText = findViewById(R.id.editText56)
            PostionEditText = findViewById(R.id.editText5)
            uploadDataToServer()

        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun uploadDataToServer() {
        if (!::selectedImageUri.isInitialized) {
            Toast.makeText(this, "Please select an image from the gallery", Toast.LENGTH_SHORT).show()
            return
        }

        val serverUrl = "http://192.168.18.32/uploadimage.php"

        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)

        val stringRequest = object : StringRequest(
            Request.Method.POST, serverUrl,
            Response.Listener { response ->
                // Handle the response from the server
                Log.d("JSON", "Response received: $response")
                // Assuming the response contains the name of the uploaded image
                // You can use this image name as needed
                val uploadedImageName = response
                // Now, you can proceed with uploading mentor data
                uploadMentorData(
                    nameEditText.text.toString(),
                    descriptionEditText.text.toString(),
                    availableEditText.text.toString(),
                    PostionEditText.text.toString(),
                    uploadedImageName
                )
            },
            Response.ErrorListener { error ->
                Log.e("JSON", "Error: ${error.message}", error)
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["image"] = encodedImage
                return params
            }
        }
        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(stringRequest)
    }


    private fun uploadMentorData(name: String, description: String, available: String, position: String, imageUrl: String) {
        val url = "http://192.168.18.32/upload_mentor_data.php"
        val postData = "name=$name&description=$description&available=$available&position=$position&imageUrl=$imageUrl"
        Thread {
            val response = sendData(url, postData)
            Log.d("JSON", response.toString())
            if (response != null && response == "Data Uploaded") {
                runOnUiThread {
                    Toast.makeText(this, "Mentor data uploaded successfully", Toast.LENGTH_SHORT).show()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Mentor data uploaded successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun sendData(url: String, postData: String): String? {
        val myUrl = URL(url)
        val httpURLConnection = myUrl.openConnection() as HttpURLConnection
        httpURLConnection.readTimeout = 15000
        httpURLConnection.connectTimeout = 15000
        httpURLConnection.requestMethod = "POST"
        httpURLConnection.doInput = true
        httpURLConnection.doOutput = true

        val outputStream = httpURLConnection.outputStream
        val bufferedWriter = OutputStreamWriter(outputStream, "UTF-8")
        bufferedWriter.write(postData)
        bufferedWriter.flush()
        bufferedWriter.close()
        outputStream.close()

        val responseCode = httpURLConnection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = httpURLConnection.inputStream
            val bufferedReader = inputStream.bufferedReader()
            val result = bufferedReader.use { it.readText() }
            inputStream.close()
            return result
        }
        return null
    }
}
