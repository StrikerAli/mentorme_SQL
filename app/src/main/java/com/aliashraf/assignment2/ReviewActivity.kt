package com.aliashraf.assignment2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class ReviewActivity : AppCompatActivity() {
    private lateinit var mentorName: String
    private lateinit var mentorImageUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        // Get mentor name and image URL from intent extras
        mentorName = intent.getStringExtra("mentor_name") ?: ""
        mentorImageUrl = intent.getStringExtra("mentor_image_url") ?: ""

        // Set mentor name and image
        val mentorNameTextView: TextView = findViewById(R.id.Name)
        val mentorProfileImageView: ImageView = findViewById(R.id.circleImageView)
        mentorNameTextView.text = mentorName
        Picasso.get().load(mentorImageUrl).into(mentorProfileImageView)

        // Handle click on the back button
        val backBtn: View = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }

        // Handle click on the submit button
        val submitBtn: View = findViewById(R.id.submitBtn)
        submitBtn.setOnClickListener {
            // Get user email and then submit the review
            getUserEmail { userEmail ->
                // Pass the user email to the submitReview function
                submitReview(userEmail)
            }
        }
    }

    private fun getUserEmail(callback: (String) -> Unit) {
        val urlString = "http://192.168.18.32/logged_in_email_fetch.php"

        val request = Request.Builder()
            .url(urlString)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@ReviewActivity, "Failed to fetch user email", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle response
                val responseData = response.body()?.string()
                if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                    // Pass the user email to the callback function
                    callback(responseData.trim())
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ReviewActivity, "Failed to fetch user email", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun submitReview(userEmail: String) {
        val ratingBar: RatingBar = findViewById(R.id.ratingBar)
        val rating = ratingBar.rating
        val experienceText: TextView = findViewById(R.id.reviewTxt)
        val experience = experienceText.text.toString().trim()

        // Ensure a rating is selected
        if (rating == 0f) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
            return
        }

        // Ensure experience text is not empty
        if (experience.isEmpty()) {
            Toast.makeText(this, "Please provide your experience", Toast.LENGTH_SHORT).show()
            return
        }

        // Construct the URL for submitting the review
        val urlString = "http://192.168.18.32/submit_review.php?user_email=$userEmail&mentor_name=$mentorName&experience=$experience&rating=$rating"

        // Make an HTTP request to the PHP script
        val request = Request.Builder()
            .url(urlString)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                e.printStackTrace()
                runOnUiThread {
                    Log.d("JSON", "Failed to submit review")
                    Toast.makeText(this@ReviewActivity, "Failed to submit review", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle response
                val responseData = response.body()?.string()
                if (response.isSuccessful && responseData == "success") {
                    runOnUiThread {
                        Log.d("JSON", response.toString())
                        Toast.makeText(this@ReviewActivity, "Review submitted successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Log.d("JSON", response.toString())
                        Toast.makeText(this@ReviewActivity, "Review submitted successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
