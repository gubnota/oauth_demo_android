package com.example.oauth2demo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Main entry point of the application
 * Handles deep link callbacks from OAuth2 provider
 */
class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var btnLoginGithub: Button
    private lateinit var btnTestSuccess: Button
    private lateinit var btnTestFailure: Button
    private lateinit var userInfoTextView: TextView
    private lateinit var btnLogout: Button

    // Backend URL - change this to your actual backend
    private val BACKEND_BASE_URL = "http://10.0.2.2:3000"  // For emulator (localhost)
    // For real device: "http://YOUR_BACKEND_IP:3000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupClickListeners()
        checkExistingToken()
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.let { handleDeepLink(it) }
    }

    private fun initializeViews() {
        statusTextView = findViewById(R.id.statusTextView)
        btnLoginGithub = findViewById(R.id.btnLoginGithub)
        btnTestSuccess = findViewById(R.id.btnTestSuccess)
        btnTestFailure = findViewById(R.id.btnTestFailure)
        userInfoTextView = findViewById(R.id.infoTextView)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupClickListeners() {
        btnLoginGithub.setOnClickListener {
            startGitHubLogin()
        }

        btnTestSuccess.setOnClickListener {
            simulateSuccessfulLogin()
        }

        btnTestFailure.setOnClickListener {
            simulateFailedLogin()
        }

        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun checkExistingToken() {
        val token = getAccessToken()
        if (token != null) {
            Log.d("OAuth", "Existing token found, fetching user info...")
            fetchGitHubUserInfo(token)
        } else {
            updateUILoggedOut()
        }
    }

    private fun startGitHubLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun simulateSuccessfulLogin() {
        statusTextView.text = "Simulating successful login..."
        val state = OAuthConfig.generateState()
        val testUri = Uri.parse("oauth2demo://callback?code=test_auth_code_123&state=$state")
        val testIntent = Intent(Intent.ACTION_VIEW, testUri)
        handleDeepLink(testIntent)
    }

    private fun simulateFailedLogin() {
        statusTextView.text = "Simulating failed login..."
        val testUri = Uri.parse("oauth2demo://callback?error=access_denied&error_description=User%20denied%20access")
        val testIntent = Intent(Intent.ACTION_VIEW, testUri)
        handleDeepLink(testIntent)
    }

    /**
     * Handles deep link callbacks from OAuth2 provider
     */
    private fun handleDeepLink(intent: Intent) {
        val data: Uri? = intent.data

        if (data != null && data.scheme == "oauth2demo" && data.host == "callback") {
            val error = data.getQueryParameter("error")
            val errorDescription = data.getQueryParameter("error_description")

            if (error != null) {
                handleAuthError(error, errorDescription)
                return
            }

            // Check if token is directly provided (from backend redirect)
            val token = data.getQueryParameter("token")
            if (token != null) {
                Log.d("OAuth", "Token received from backend: ${token.take(20)}...")
                saveTokenSecurely(token)
                fetchGitHubUserInfo(token)
                return
            }

            // Get authorization code
            val authCode = data.getQueryParameter("code")
            val state = data.getQueryParameter("state")

            if (authCode != null && state != null) {
                if (OAuthConfig.verifyState(state)) {
                    handleAuthSuccess(authCode)
                } else {
                    handleAuthError("invalid_state", "State verification failed")
                }
            }
        }
    }

    private fun handleAuthSuccess(authCode: String) {
        statusTextView.text = "✓ Authentication successful!\nExchanging code via backend..."

        Toast.makeText(this, "Exchanging auth code...", Toast.LENGTH_SHORT).show()

        // Use backend to exchange code
        exchangeCodeViaBackend(authCode)
    }

    private fun handleAuthError(error: String, description: String?) {
        val errorMessage = "✗ Authentication failed\nError: $error\n${description ?: ""}"
        statusTextView.text = errorMessage

        Toast.makeText(this, "Login failed: $error", Toast.LENGTH_LONG).show()

        updateUILoggedOut()
    }

    /**
     * Exchange authorization code via your backend (RECOMMENDED)
     * Backend handles CLIENT_SECRET securely
     */
    private fun exchangeCodeViaBackend(authCode: String) {
        statusTextView.text = "📤 Sending authorization code to backend...\nPlease wait..."

        Thread {
            try {
                val backendUrl = "$BACKEND_BASE_URL/oauth/callback?code=$authCode"
                val url = URL(backendUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                Log.d("OAuth", "Backend exchange URL: $backendUrl")

                val responseCode = connection.responseCode
                Log.d("OAuth", "Backend response code: $responseCode")

                if (responseCode == 200 || responseCode == 302) {
                    val responseBody = if (responseCode == 200) {
                        connection.inputStream.bufferedReader().readText()
                    } else {
                        // Redirect response
                        val location = connection.getHeaderField("Location")
                        Log.d("OAuth", "Redirect location: $location")
                        location ?: ""
                    }

                    Log.d("OAuth", "Backend response: $responseBody")

                    // Parse response for token
                    if (responseBody.contains("token=")) {
                        val tokenMatch = Regex("token=([^&]+)").find(responseBody)
                        val token = tokenMatch?.groupValues?.get(1)

                        if (token != null) {
                            Log.d("OAuth", "Token extracted from backend: ${token.take(20)}...")
                            saveTokenSecurely(token)

                            runOnUiThread {
                                fetchGitHubUserInfo(token)
                            }
                        } else {
                            runOnUiThread {
                                handleAuthError("token_parse_failed", "Could not extract token from backend")
                            }
                        }
                    } else {
                        runOnUiThread {
                            handleAuthError("backend_error", "Backend did not return token")
                        }
                    }
                } else {
                    val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                    Log.e("OAuth", "Backend error response: $errorBody")
                    runOnUiThread {
                        handleAuthError("backend_error", "HTTP $responseCode")
                    }
                }

            } catch (e: Exception) {
                Log.e("OAuth", "Error contacting backend", e)
                runOnUiThread {
                    handleAuthError("network_error", e.message ?: "Backend unreachable")
                }
            }
        }.start()
    }

    /**
     * Fetches GitHub user information using the access token
     */
    private fun fetchGitHubUserInfo(accessToken: String) {
        statusTextView.text = "✓ Token obtained!\nFetching your GitHub profile..."

        Thread {
            try {
                val url = URL("https://api.github.com/user")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.setRequestProperty("User-Agent", "OAuth2Demo")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                Log.d("OAuth", "User API response code: $responseCode")

                if (responseCode == 200) {
                    val responseBody = connection.inputStream.bufferedReader().readText()
                    Log.d("OAuth", "User info response: $responseBody")

                    val userInfo = JSONObject(responseBody)
                    val username = userInfo.optString("login", "Unknown")
                    val name = userInfo.optString("name", "N/A")
                    val bio = userInfo.optString("bio", "No bio")
                    val publicRepos = userInfo.optInt("public_repos", 0)
                    val followers = userInfo.optInt("followers", 0)
                    val following = userInfo.optInt("following", 0)

                    runOnUiThread {
                        updateUILoggedIn(username, name, bio, publicRepos, followers, following)
                    }
                } else {
                    val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                    Log.e("OAuth", "User API error: $errorBody")
                    runOnUiThread {
                        handleAuthError("user_fetch_failed", "HTTP $responseCode")
                    }
                }

            } catch (e: Exception) {
                Log.e("OAuth", "Error fetching user info", e)
                runOnUiThread {
                    handleAuthError("network_error", e.message ?: "Failed to fetch user info")
                }
            }
        }.start()
    }

    private fun updateUILoggedIn(
        username: String,
        name: String,
        bio: String,
        publicRepos: Int,
        followers: Int,
        following: Int
    ) {
        statusTextView.text = "✓ Successfully Logged In!"

        val userInfoText = """
            👤 GitHub Profile
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Username: @$username
            Name: $name
            Bio: $bio
            
            📊 Statistics
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Public Repos: $publicRepos
            Followers: $followers
            Following: $following
            
            Token: ${getAccessToken()?.take(20)}...
        """.trimIndent()

        userInfoTextView.text = userInfoText
        userInfoTextView.visibility = android.view.View.VISIBLE

        btnLoginGithub.visibility = android.view.View.GONE
        btnTestSuccess.visibility = android.view.View.GONE
        btnTestFailure.visibility = android.view.View.GONE
        btnLogout.visibility = android.view.View.VISIBLE

        Toast.makeText(this, "Welcome, $username!", Toast.LENGTH_LONG).show()
    }

    private fun updateUILoggedOut() {
        statusTextView.text = "📱 GitHub OAuth2 Demo\n\nClick 'Login with GitHub' to continue"
        userInfoTextView.visibility = android.view.View.GONE

        btnLoginGithub.visibility = android.view.View.VISIBLE
        btnTestSuccess.visibility = android.view.View.VISIBLE
        btnTestFailure.visibility = android.view.View.VISIBLE
        btnLogout.visibility = android.view.View.GONE
    }

    private fun logout() {
        val sharedPref = getSharedPreferences("oauth_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        updateUILoggedOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    private fun saveTokenSecurely(accessToken: String) {
        try {
            val sharedPref = getSharedPreferences("oauth_prefs", Context.MODE_PRIVATE)
            sharedPref.edit().apply {
                putString("access_token", accessToken)
                putLong("token_time", System.currentTimeMillis())
            }.apply()
            Log.d("OAuth", "Token saved securely")
        } catch (e: Exception) {
            Log.e("OAuth", "Error saving token", e)
        }
    }

    private fun getAccessToken(): String? {
        val sharedPref = getSharedPreferences("oauth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("access_token", null)
    }
}
