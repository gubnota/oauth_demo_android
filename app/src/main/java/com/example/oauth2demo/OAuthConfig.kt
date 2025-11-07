package com.example.oauth2demo

import java.security.SecureRandom
import java.util.Base64

/**
 * OAuth2 configuration object
 * Contains all OAuth2 endpoints and parameters
 */
object OAuthConfig {
    
    // ========================================
    // GITHUB OAuth2 Configuration
    // ========================================
    // To use this app with real GitHub OAuth2:
    // 1. Go to https://github.com/settings/developers
    // 2. Create a new OAuth App
    // 3. Set Authorization callback URL to: oauth2demo://callback
    // 4. Copy your Client ID and Client Secret below
    
    const val CLIENT_ID = "YOUR_GITHUB_CLIENT_ID"  // Replace with your GitHub Client ID
    const val CLIENT_SECRET = ""  // Not needed for PKCE flow (public clients)
    
    const val AUTHORIZATION_ENDPOINT = "https://github.com/login/oauth/authorize"
    const val TOKEN_ENDPOINT = "https://github.com/login/oauth/access_token"
    
    const val REDIRECT_URI = "oauth2demo://callback"
    const val SCOPE = "user read:user"
    
    // ========================================
    // Alternative Providers (Uncomment to use)
    // ========================================
    
    // GOOGLE OAuth2
    // const val CLIENT_ID = "YOUR_GOOGLE_CLIENT_ID"
    // const val AUTHORIZATION_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
    // const val TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"
    // const val SCOPE = "openid email profile"
    
    // YANDEX OAuth2
    // const val CLIENT_ID = "YOUR_YANDEX_CLIENT_ID"
    // const val AUTHORIZATION_ENDPOINT = "https://oauth.yandex.com/authorize"
    // const val TOKEN_ENDPOINT = "https://oauth.yandex.com/token"
    // const val SCOPE = "login:email login:info"
    
    // ========================================
    // State Management (CSRF Protection)
    // ========================================
    private var currentState: String? = null
    private var currentCodeVerifier: String? = null
    
    /**
     * Generates a random state parameter for CSRF protection
     */
    fun generateState(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        currentState = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        return currentState!!
    }
    
    /**
     * Verifies that the state parameter matches
     */
    fun verifyState(state: String): Boolean {
        return state == currentState
    }
    
    /**
     * Stores the PKCE code verifier
     */
    fun storeCodeVerifier(codeVerifier: String) {
        currentCodeVerifier = codeVerifier
    }
    
    /**
     * Retrieves the stored code verifier
     */
    fun getCodeVerifier(): String? {
        return currentCodeVerifier
    }
    
    /**
     * Builds the complete OAuth2 authorization URL
     */
    fun buildAuthorizationUrl(codeChallenge: String): String {
        val state = generateState()
        
        return "\$AUTHORIZATION_ENDPOINT?" +
                "client_id=\$CLIENT_ID&" +
                "redirect_uri=\$REDIRECT_URI&" +
                "response_type=code&" +
                "scope=\$SCOPE&" +
                "state=\$state&" +
                "code_challenge=\$codeChallenge&" +
                "code_challenge_method=S256"
    }
}
