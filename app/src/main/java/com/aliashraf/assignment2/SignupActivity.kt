package com.aliashraf.assignment2

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val signupBtn: Button = findViewById(R.id.signupBtn)
        val emailEditText: EditText = findViewById(R.id.editText2)
        val passwordEditText: EditText = findViewById(R.id.editText6)
        val nameEditText: EditText = findViewById(R.id.editText)
        val cityEditText: EditText = findViewById(R.id.editText5)
        val countryEditText: EditText = findViewById(R.id.editText4)
        val phoneNumberEditText: EditText = findViewById(R.id.editText3)

        signupBtn.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val name = nameEditText.text.toString()
            val city = cityEditText.text.toString()
            val country = countryEditText.text.toString()
            val phoneNumber = phoneNumberEditText.text.toString()

            SignupAsyncTask().execute(name, email, password, city, country, phoneNumber)
        }

        val loginBtn: TextView = findViewById(R.id.loginBtn)
        loginBtn.setOnClickListener {
            finish()
        }
    }

    private inner class SignupAsyncTask : AsyncTask<String, Void, JSONObject>() {

        override fun doInBackground(vararg params: String): JSONObject {
            val name = params[0]
            val email = params[1]
            val password = params[2]
            val city = params[3]
            val country = params[4]
            val phoneNumber = params[5]

            val urlString = "http://192.168.18.32/signup.php?email=${URLEncoder.encode(email, "UTF-8")}&password=${URLEncoder.encode(password, "UTF-8")}&name=${URLEncoder.encode(name, "UTF-8")}&city=${URLEncoder.encode(city, "UTF-8")}&country=${URLEncoder.encode(country, "UTF-8")}&phoneNumber=${URLEncoder.encode(phoneNumber, "UTF-8")}"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.doOutput = true

            val responseCode = connection.responseCode
            val response = StringBuilder()

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
            }

            return JSONObject(response.toString())
        }

        override fun onPostExecute(result: JSONObject) {
            handleResponse(result)
        }
    }

    private fun handleResponse(response: JSONObject) {
        if (response.getBoolean("success")) {
            Log.d("JSON", "Signup to SQL: Success")
            Toast.makeText(this@SignupActivity, "Successfully Signed Up", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@SignupActivity, VerifyPhoneActivity::class.java)
            startActivity(intent)
        } else {
            Log.w("JSON", "Signup to SQL: Failed")
            Toast.makeText(this@SignupActivity, "Signup Failed", Toast.LENGTH_SHORT).show()
        }
    }
}
