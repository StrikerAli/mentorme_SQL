package com.aliashraf.assignment2

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class EditProfileActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var countryEditText: EditText
    private lateinit var updateProfileBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        nameEditText = findViewById(R.id.editText)
        emailEditText = findViewById(R.id.editText2)
        phoneNumberEditText = findViewById(R.id.editText3)
        cityEditText = findViewById(R.id.editText4)
        countryEditText = findViewById(R.id.editText5)
        updateProfileBtn = findViewById(R.id.updateProfileBtn)

        // Perform HTTP GET request to the PHP script to get the user's email
        getEmailFromPHP()
        updateProfileBtn.setOnClickListener {
            updateUserProfile()
        }
    }

    private fun getEmailFromPHP() {
        val thread = Thread {
            try {
                // URL of the PHP file for retrieving user's email
                val url = URL("http://192.168.18.32/logged_in_email_fetch.php")
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
                        var line: String?
                        while (bufferedReader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                        bufferedReader.close()

                        // Parse JSON response and update UI with user's email
                        updateEmailUI(response.toString())
                    } else {
                        runOnUiThread {
                            // Handle HTTP error
                            Log.d("JSON", "Error: ${urlConnection.responseMessage}")
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
                    Log.d("JSON", "Error: ${e.message}")
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        thread.start()
    }

    private fun updateEmailUI(emailJson: String) {
        try {
            Log.d("JSON", emailJson)
            // Parse JSON string to JSONObject

            // Extract email from JSON object
            Log.d("JSON", emailJson)
            // Update EditText field with user's email
            runOnUiThread {
                emailEditText.setText(emailJson)
            }

            // Now that we have the email, fetch the user's data using it
            getUserDataFromPHP(emailJson)
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                // Handle exception
                Toast.makeText(this, "Error parsing user's email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUserDataFromPHP(email: String) {
        val thread = Thread {
            try {
                // URL of the PHP file for retrieving user's data based on email
                val url = URL("http://192.168.18.32/get_user_data.php?email=$email")
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
                        var line: String?
                        while (bufferedReader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                        bufferedReader.close()

                        // Parse JSON response and update UI with user's data
                        updateUserUI(response.toString())
                    } else {
                        runOnUiThread {
                            // Handle HTTP error
                            Log.d("JSON", "Error: ${urlConnection.responseMessage}")
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
                    Log.d("JSON", "Error: ${e.message}")
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        thread.start()
    }

    private fun updateUserProfile() {
        val email = emailEditText.text.toString()
        val name = nameEditText.text.toString()
        val phoneNumber = phoneNumberEditText.text.toString()
        val city = cityEditText.text.toString()
        val country = countryEditText.text.toString()

        // Prepare the data to be sent in the POST request
        val postData = "email=$email&name=$name&phoneNumber=$phoneNumber&city=$city&country=$country"

        val thread = Thread {
            try {
                val url = URL("http://192.168.18.32/update_user.php")
                val urlConnection = url.openConnection() as HttpURLConnection

                urlConnection.apply {
                    requestMethod = "POST"
                    connectTimeout = 5000
                    readTimeout = 5000
                    doOutput = true
                }

                // Write data to the server
                val outputStream = OutputStreamWriter(urlConnection.outputStream)
                outputStream.write(postData)
                outputStream.flush()

                if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    // Handle the response if needed
                    // For example, display a toast message indicating success
                    runOnUiThread {
                        Log.d("JSON", "Profile updated successfully")
                        Toast.makeText(this@EditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle the HTTP error
                    // For example, display a toast message indicating failure
                    runOnUiThread {
                        Log.d("JSON", "Error: ${urlConnection.responseMessage}")
                        Toast.makeText(this@EditProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }

                urlConnection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    // Handle the exception
                    Log.d("JSON", "Error: ${e.message}")
                    Toast.makeText(this@EditProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        thread.start()
    }
    private fun updateUserUI(userDataJson: String) {
        try {
            Log.d("JSON", userDataJson)
            // Parse JSON string to JSONObject
            val userDataObject = JSONObject(userDataJson)

            // Extract user data from JSON object
            val name = userDataObject.getString("name")
            val phoneNumber = userDataObject.getString("phoneNumber")
            val city = userDataObject.getString("city")
            val country = userDataObject.getString("country")

            // Update EditText fields with user's data
            runOnUiThread {
                nameEditText.setText(name)
                phoneNumberEditText.setText(phoneNumber)
                cityEditText.setText(city)
                countryEditText.setText(country)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                // Handle exception
                Log.d("JSON", "Error parsing user's data")
                Toast.makeText(this, "Error parsing user's data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
