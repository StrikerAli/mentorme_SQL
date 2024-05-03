package com.aliashraf.assignment2

import android.content.Intent
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
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginBtn: Button = findViewById(R.id.loginBtn)
        val emailEditText: EditText = findViewById(R.id.editText)
        val passwordEditText: EditText = findViewById(R.id.editText2)

        loginBtn.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            Log.d("JSON", "Email: $email, Password: $password")
            val jsonRequest = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            Thread {
                val response = login(email, password)
                runOnUiThread {
                    if (response.getBoolean("success")) {
                        Log.d("TAG", "Login: Success")
                        Toast.makeText(this@LoginActivity, "Successfully LoggedIn", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.w("TAG", "Login: failed")
                        Toast.makeText(this@LoginActivity, "Authentication Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }

        val signupBtn: TextView = findViewById(R.id.signupBtn)
        signupBtn.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        val forgotPassBtn: TextView = findViewById(R.id.forgotPassBtn)
        forgotPassBtn.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(email: String, password: String): JSONObject {
        val urlString = "http://192.168.18.32/login.php?email=${email}&password=${password}"
        val url = URL(urlString) // Construct URL with parameters
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Content-Type", "application/json")
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
        Log.d("JSON", response.toString())
        return JSONObject(response.toString())
    }

}
