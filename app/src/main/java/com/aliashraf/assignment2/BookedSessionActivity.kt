package com.aliashraf.assignment2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class BookedSessionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booked_session)

        // window.setFlags(android.R.attr.windowFullscreen, android.R.attr.windowFullscreen)

        val backBtn: View = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }
    }
}