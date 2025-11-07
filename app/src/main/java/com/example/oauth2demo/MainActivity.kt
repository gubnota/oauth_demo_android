package com.example.oauth2demo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Main entry point of the application
 * Handles deep link callbacks from OAuth2 provider
 */
class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var btnLoginGithub: Button
    private lateinit var btnTestSuccess: Button
    private lateinit var btnTestFailure: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupClickListeners()
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleDeepLink(it) }
    }

    private fun initializeViews() {
        statusTextView = findViewById(R.id.statusTextView)
        btnLoginGithub = findViewById(R.id.btnLoginGithub)
        btnTestSuccess = findViewById(R.id.btnTestSuccess)
        btnTestFailure = findViewById(R.id.btnTestFailure)
    }

    private fun setupClickListeners() {
        // Real GitHub OAuth2 login
        btnLoginGithub.setOnClickListener {
            startGitHubLogin()
        }

        // Test successful login scenario
        btnTestSuccess.setOnClickListener {
            simulateSuccessfulLogin()
        }

        // Test failed login scenario
        btnTestFailure.setOnClickListener {
            simulateFailedLogin()
        }
    }

    private fun startGitHubLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    /**
     * Simulates a successful OAuth2 flow for testing
     */
    private fun simulateSuccessfulLogin() {
        statusTextView.text = "Simulating successful login..."
        
        // Simulate the callback with a test authorization code
        val testUri = Uri.parse("oauth2demo://callback?code=test_auth_code_123&state=\${OAuthConfig.generateState()}")
        val testIntent = Intent(Intent.ACTION_VIEW, testUri)
        handleDeepLink(testIntent)
    }

    /**
     * Simulates a failed OAuth2 flow for testing
     */
    private fun simulateFailedLogin() {
        statusTextView.text = "Simulating failed login..."
        
        // Simulate the callback with an error
        val testUri = Uri.parse("oauth2demo://callback?error=access_denied&error_description=User%20denied%20access")
        val testIntent = Intent(Intent.ACTION_VIEW, testUri)
        handleDeepLink(testIntent)
    }

    /**
     * Handles deep link callbacks from OAuth2 provider
     * This is called when the user completes authentication
     */
    private fun handleDeepLink(intent: Intent) {
        val data: Uri? = intent.data

        if (data != null && data.scheme == "oauth2demo" && data.host == "callback") {
            // Check for error first
            val error = data.getQueryParameter("error")
            val errorDescription = data.getQueryParameter("error_description")

            if (error != null) {
                handleAuthError(error, errorDescription)
                return
            }

            // Get authorization code
            val authCode = data.getQueryParameter("code")
            val state = data.getQueryParameter("state")

            if (authCode != null && state != null) {
                // Verify state to prevent CSRF attacks
                if (OAuthConfig.verifyState(state)) {
                    handleAuthSuccess(authCode)
                } else {
                    handleAuthError("invalid_state", "State verification failed - potential CSRF attack")
                }
            } else {
                statusTextView.text = "Waiting for authentication..."
            }
        }
    }

    /**
     * Handles successful authentication
     */
    private fun handleAuthSuccess(authCode: String) {
        statusTextView.text = "✓ Authentication successful!\nAuthorization code received"
        
        Toast.makeText(
            this,
            "Success! Auth Code: \${authCode.take(10)}...",
            Toast.LENGTH_LONG
        ).show()

        // Exchange authorization code for access token
        // In a real app, you would call your backend or token endpoint here
        exchangeCodeForToken(authCode)
    }

    /**
     * Handles authentication errors
     */
    private fun handleAuthError(error: String, description: String?) {
        val errorMessage = "✗ Authentication failed\nError: $error\n${description ?: ""}"
        statusTextView.text = errorMessage
        
        Toast.makeText(
            this,
            "Login failed: $error",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Exchanges authorization code for access token
     * This demonstrates the second part of OAuth2 flow
     */
    private fun exchangeCodeForToken(authCode: String) {
        // In a real implementation, you would:
        // 1. Send authCode + code_verifier to token endpoint
        // 2. Receive access_token and refresh_token
        // 3. Store tokens securely
        // 4. Navigate to authenticated screen

        // For this educational example, we will simulate success
        val intent = Intent(this, SuccessActivity::class.java).apply {
            putExtra("auth_code", authCode)
            putExtra("access_token", "simulated_access_token_\${System.currentTimeMillis()}")
        }
        startActivity(intent)
    }
}
