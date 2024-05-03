package com.aliashraf.assignment2

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var requestQueue: RequestQueue
    private lateinit var recyclerView: RecyclerView
    private lateinit var ImageUri: Uri

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val PICK_VIDEO_REQUEST = 2
        private const val TAG = "JSON"
        private const val SERVER_URL = "http://192.168.18.32/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(mutableListOf())
        recyclerView.adapter = chatAdapter

        requestQueue = Volley.newRequestQueue(this)

        fetchChatMessages()

        val editText: TextView = findViewById(R.id.editText)
        val sendButton: ImageView = findViewById(R.id.iconAfter3)
        val galleryButton: ImageView = findViewById(R.id.iconBefore2)
        val cameraButton: ImageView = findViewById(R.id.cameraBtn)
        val videobutton: ImageView = findViewById(R.id.iconBefore1)

        sendButton.setOnClickListener {
            val message = editText.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message, "text", "")
                editText.text = ""
            }
        }

        galleryButton.setOnClickListener {
            openGallery()
        }
        videobutton.setOnClickListener {
            openGalleryForVideo()
        }

        cameraButton.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    private fun fetchChatMessages() {
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, SERVER_URL + "get_messages.php", null,
            Response.Listener { response ->
                try {
                    Log.d("JSON", "Response received: $response")
                    val messages = mutableListOf<ChatMessage>()
                    for (i in 0 until response.length()) {
                        val messageObj = response.getJSONObject(i)
                        val message = messageObj.getString("message")
                        val sender = messageObj.getString("sender")
                        val timestamp = messageObj.getString("timestamp")
                        val messageTypeString = messageObj.getString("messageType")
                        val messageType = when (messageTypeString) {
                            "TEXT" -> MessageType.TEXT
                            "IMAGE" -> MessageType.IMAGE
                            "VIDEO" -> MessageType.VIDEO
                            else -> MessageType.TEXT // Default to TEXT if messageTypeString is not recognized
                        }
                        val imageUrl = messageObj.optString("imageUrl", "")
                        val videoUrl = messageObj.optString("videoUrl", "")
                        messages.add(ChatMessage(message, sender, timestamp, messageType, imageUrl, videoUrl))
                    }
                    chatAdapter.updateMessages(messages)
                } catch (e: JSONException) {
                    Log.e("JSON", "JSON parsing error: ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                Log.e("JSON", "Error fetching messages: ${error.message}")
            })

        requestQueue.add(jsonArrayRequest)
    }

    private fun sendMessage(message: String, messageType: String, mediaUrl: String) {
        val currentTime = getCurrentTimeString()

        // Construct the URL with parameters directly
        val urlString = "http://192.168.18.32/send_message.php" +
                "?sender=John_Cooper" +
                "&message=$message" +
                "&timestamp=$currentTime" +
                "&messageType=$messageType" +
                if (mediaUrl.isNotEmpty()) "&mediaUrl=$mediaUrl" else ""
        Log.d(TAG, "Sending message: $urlString")
        // Create a StringRequest for GET method
        val stringRequest = StringRequest(
            Request.Method.GET, urlString,
            Response.Listener { response ->
                Log.d(TAG, "Message sent successfully: $response")
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "Error sending message: ${error.message}")
            })

        // Add the request to the RequestQueue
        requestQueue.add(stringRequest)
    }



    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    private fun openGalleryForVideo() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        startActivityForResult(intent, PICK_VIDEO_REQUEST)
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val imageUri = data?.data
                    uploadFile(imageUri, "image")
                }
                PICK_VIDEO_REQUEST -> {
                    val videoUri = data?.data
                    uploadFile(videoUri, "video")
                }
            }
        }
    }

    private fun uploadFile(fileUri: Uri?, fileType: String) {
        if (fileType == "image" && fileUri != null) {
            val serverUrl = "http://192.168.18.32/uploadimage.php"

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
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
                    sendMessage(uploadedImageName, "IMAGE", "uploadedImageName")
                    // Now, you can proceed with uploading mentor data
                },
                Response.ErrorListener { error ->
                    Log.e("JSON", "Error: ${error.message}", error)
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["image"] = encodedImage
                    return params
                }
            }

            // Add the request to the RequestQueue.
            Volley.newRequestQueue(this).add(stringRequest)
        }

        if (fileType == "video" && fileUri != null) {
            val serverUrl = "http://192.168.18.32/uploadvideo.php"

            val inputStream = contentResolver.openInputStream(fileUri)
            val buffer = ByteArray(inputStream!!.available())
            inputStream.read(buffer)
            inputStream.close()
            val encodedVideo = Base64.encodeToString(buffer, Base64.DEFAULT)

            val stringRequest = object : StringRequest(
                Request.Method.POST, serverUrl,
                Response.Listener { response ->
                    // Handle the response from the server
                    Log.d("JSON", "Response received: $response")
                    // Assuming the response contains the name of the uploaded video
                    // You can use this video name as needed
                    val uploadedVideoName = response
                    sendMessage(uploadedVideoName, "VIDEO", "uploadedVideoName")
                    // Now, you can proceed with uploading mentor data
                },
                Response.ErrorListener { error ->
                    Log.e("JSON", "Error: ${error.message}", error)
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["video"] = encodedVideo
                    return params
                }
            }

            // Add the request to the RequestQueue.
            Volley.newRequestQueue(this).add(stringRequest)
        }

    }


    private fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
}
