package com.aliashraf.assignment2

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import org.json.JSONArray
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MyProfileActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var circleImageView8: ImageView
    private lateinit var backgroundImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        circleImageView8 = findViewById(R.id.circleImageView8)
        backgroundImageView = findViewById(R.id.background)


        // Your existing code...
        val backBtn: View = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }
        val editBtn: ImageView = findViewById(R.id.editBtn)
        editBtn.setOnClickListener {
            openGallery()
        }
        val searchBtn: ImageView = findViewById(R.id.editBtn3)
        searchBtn.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
        Fetch_Images()
    }
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            uploadImage(imageUri)
        }
    }
    private fun uploadImage(imageUri: Uri?) {
        // Check if the imageUri is not null
        if (imageUri != null) {
            val serverUrl = "http://192.168.18.32/uploadimage.php"

            // Use try-catch to handle exceptions
            try {
                // Convert the imageUri to bitmap
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

                // Convert bitmap to byte array
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()

                // Encode the byte array to Base64 string
                val encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)

                // Create a StringRequest to send the encoded image to the server
                val stringRequest = object : StringRequest(
                    Request.Method.POST, serverUrl,
                    { response ->
                        // Image uploaded successfully, response contains the image filename or identifier
                        Log.d("JSON", "Image uploaded successfully. Response: $response")
                    },
                    { error ->
                        // Handle error
                        Log.e("JSON", "Error uploading image: ${error.message}", error)
                        Toast.makeText(this, "Error uploading image: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    override fun getParams(): MutableMap<String, String> {
                        val params = HashMap<String, String>()
                        params["image"] = encodedImage
                        return params
                    }
                }

                // Add the request to the RequestQueue
                Volley.newRequestQueue(this).add(stringRequest)
            } catch (e: IOException) {
                e.printStackTrace()
                // Handle exception
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Handle null imageUri
            Toast.makeText(this, "Error: Image URI is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun Fetch_Images() {
        val thread = Thread {
            try {
                // URL of the PHP file for retrieving mentor information
                val url = URL("http://192.168.18.32/Fetch_Images.php")
                val urlConnection = url.openConnection() as HttpURLConnection
                try {
                    // Set request method and timeouts
                    urlConnection.requestMethod = "GET"
                    urlConnection.connectTimeout = 5000
                    urlConnection.readTimeout = 5000

                    // Check if the connection is successful
                    if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                        // Read data from the response
                        val bufferedReader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                        val response = StringBuilder()
                        Log.d("JSON", "Response: ${urlConnection.responseMessage}")
                        var line: String?
                        while (bufferedReader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                        bufferedReader.close()
                        Log.d("JSON", "Response: $response")
                        // Parse JSON response
                        val mentorList = JSONArray(response.toString())
                        // Extract the last two image URLs from the mentorList array
                        var lastTwoUrls = mutableListOf<String>()
                        for (i in mentorList.length() - 1 downTo 0) {
                            val mentor = mentorList.getJSONObject(i)
                            val imageUrl = mentor.getString("image")
                            lastTwoUrls.add(imageUrl)
                            if (lastTwoUrls.size == 2) {
                                break
                            }
                        }

                        // Handle the last two URLs as needed
                        runOnUiThread {
                            // Process the last two URLs (e.g., display them, store them in variables, etc.)
                            // Example:
                            lastTwoUrls[0] = "http://192.168.18.32/uploads/" + lastTwoUrls[0]
                            lastTwoUrls[1] = "http://192.168.18.32/uploads/" + lastTwoUrls[1]
                            loadImage(lastTwoUrls[0])
                            loadCoverImage(lastTwoUrls[1])
                            for (url in lastTwoUrls) {
                                Log.d("JSON", "Image URL: $url")
                            }
                        }
                    } else {
                        runOnUiThread {
                            // Handle HTTP error
                            Toast.makeText(this, "Error: ${urlConnection.responseMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } finally {
                    urlConnection.disconnect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    // Handle exception
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        Log.d("JSON", "Thread started")
        thread.start()
    }


    private fun loadImage(imageUrl: String) {
        // Load the image using Picasso or any other image loading library
        Picasso.get().load(imageUrl).into(circleImageView8)
    }

    private fun loadCoverImage(coverImageUrl: String) {
        // Load the cover image using Picasso or any other image loading library
        Picasso.get().load(coverImageUrl).into(backgroundImageView)
    }
}


