package com.aliashraf.assignment2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import android.util.Log
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.squareup.picasso.Picasso
import okhttp3.*
import java.io.IOException

class BookSessionActivity : AppCompatActivity() {
    private lateinit var buttonContainer: LinearLayout
    private lateinit var selectedDate: String
    private lateinit var selectedHour: String
    private var messageId = 0
    private var notificationId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_session)

        val mentorNameTextView: TextView = findViewById(R.id.Name)
        val mentorProfileImageView: ImageView = findViewById(R.id.circleImageView)

        // Retrieve data from Intent extras
        val mentorName = intent.getStringExtra("mentor_name")
        val mentorImageUrl = intent.getStringExtra("mentor_image_url")

        // Set data to TextViews and ImageView
        mentorNameTextView.text = mentorName
        // Load mentor image using Picasso
        Picasso.get().load(mentorImageUrl).into(mentorProfileImageView)

        buttonContainer = findViewById(R.id.buttonContainer)

        // Generate buttons for all 24 time slots
        for (i in 0 until 24) {
            val hour = String.format("%02d", i) // Format hour with leading zero if needed
            val time = "$hour:00" // Format time as HH:00 AM/PM

            val button = AppCompatButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = time
                setOnClickListener { onButtonClicked(this) }
                setBackgroundResource(R.drawable.appointment_time_btn)
                setTextColor(ContextCompat.getColor(this@BookSessionActivity, R.color.black))
            }

            buttonContainer.addView(button)
        }

        val backBtn: View = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // month is 0-based, so add 1 for the actual month
            selectedDate = "$dayOfMonth/${month + 1}/$year"
            Log.d("JSON", "Date $selectedDate is selected")

        }

        val bookAppointmentButton: View = findViewById(R.id.bookAppointment)
        bookAppointmentButton.setOnClickListener {
            bookAppointment()
        }
    }

    fun onButtonClicked(view: View) {
        val buttonText = (view as AppCompatButton).text.toString()
        selectedHour = buttonText
        Log.d("JSON", "Hour button $selectedHour is pressed")
    }

    private fun bookAppointment() {
        if (::selectedDate.isInitialized && ::selectedHour.isInitialized) {
            // Fetch the logged-in user's email using the PHP script
            val request = Request.Builder()
                .url("http://192.168.18.32/logged_in_email_fetch.php")
                .build()

            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle failure
                    e.printStackTrace()
                    runOnUiThread {
                        Log.d("JSON", "Failed to fetch email!")
                        Toast.makeText(this@BookSessionActivity, "Failed to fetch email!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    // Handle response
                    if (response.isSuccessful) {
                        val email = response.body()?.string() ?: ""
                        // Once email is fetched, proceed to book appointment
                        sendBookingRequest(email)
                    } else {
                        runOnUiThread {
                            Log.d("JSON", "Failed to fetch email!")
                            Toast.makeText(this@BookSessionActivity, "Failed to fetch email!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } else {
            Log.d("JSON", "Please select both date and time.")
            Toast.makeText(this, "Please select both date and time.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendBookingRequest(bookerEmail: String) {
        // Construct the URL with booking details
        val mentorNameTextView: TextView = findViewById(R.id.Name)
        val mentorName = mentorNameTextView.text
        val urlString = "http://192.168.18.32/book_session.php?date=$selectedDate&time=$selectedHour&booker_email=$bookerEmail&mentor_name=$mentorName"

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
                    Log.d("JSON", "Failed to book appointment!")
                    Toast.makeText(this@BookSessionActivity, "Failed to book appointment!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle response
                if (response.isSuccessful) {
                    runOnUiThread {
                        Log.d("JSON", response.body()?.string() ?: "Appointment booked successfully!")
                        Toast.makeText(this@BookSessionActivity, "Appointment booked successfully!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Log.d("JSON", response.body()?.string() ?: "Failed to book appointment!")
                        Toast.makeText(this@BookSessionActivity, "Failed to book appointment!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
