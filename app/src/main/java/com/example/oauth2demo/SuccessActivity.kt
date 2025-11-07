package com.example.oauth2demo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Success screen shown after successful OAuth2 authentication
 */
class SuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        val authCode = intent.getStringExtra("auth_code") ?: "N/A"
        val accessToken = intent.getStringExtra("access_token") ?: "N/A"

        findViewById<TextView>(R.id.tvAuthCode).text = "Auth Code:\n\${authCode.take(20)}..."
        findViewById<TextView>(R.id.tvAccessToken).text = "Access Token:\n\${accessToken.take(30)}..."
        findViewById<TextView>(R.id.tvMessage).text = 
            "✓ Successfully authenticated!\n\nYou can now use the access token to make API calls."

        findViewById<Button>(R.id.btnDone).setOnClickListener {
            finish()
        }
    }
}
