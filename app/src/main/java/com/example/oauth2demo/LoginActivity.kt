package com.example.oauth2demo

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity that displays OAuth2 login page in WebView
 * Intercepts the redirect URI to capture authorization code
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var codeVerifier: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        webView = findViewById(R.id.webView)
        
        // Generate PKCE code verifier and challenge
        codeVerifier = PKCEHelper.generateCodeVerifier()
        val codeChallenge = PKCEHelper.generateCodeChallenge(codeVerifier)
        
        // Store code verifier for later use when exchanging code for token
        OAuthConfig.storeCodeVerifier(codeVerifier)

        setupWebView()
        loadOAuthUrl(codeChallenge)
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportMultipleWindows(false)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                
                // Intercept the redirect URI
                if (url.startsWith(OAuthConfig.REDIRECT_URI)) {
                    handleRedirect(url)
                    return true
                }
                
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Page loaded successfully
            }
        }
    }

    /**
     * Loads the OAuth2 authorization URL with PKCE parameters
     */
    private fun loadOAuthUrl(codeChallenge: String) {
        val authUrl = OAuthConfig.buildAuthorizationUrl(codeChallenge)
        
        Toast.makeText(
            this,
            "Loading GitHub OAuth2...",
            Toast.LENGTH_SHORT
        ).show()
        
        webView.loadUrl(authUrl)
    }

    /**
     * Handles the OAuth2 redirect callback
     */
    private fun handleRedirect(url: String) {
        val uri = Uri.parse(url)
        
        // Check for errors
        val error = uri.getQueryParameter("error")
        if (error != null) {
            Toast.makeText(
                this,
                "Authentication failed: $error",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        // Get authorization code
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        
        if (code != null && state != null) {
            // Verify state
            if (OAuthConfig.verifyState(state)) {
                // Success! Return to MainActivity
                // The deep link will be handled by MainActivity
                finish()
            } else {
                Toast.makeText(
                    this,
                    "State verification failed",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
