package com.example.oauth2demo

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * PKCE (Proof Key for Code Exchange) Helper
 * Implements RFC 7636 for OAuth2 security enhancement
 */
object PKCEHelper {
    
    /**
     * Generates a cryptographically random code verifier
     * Length: 43-128 characters (we use 64)
     */
    fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        val codeVerifier = ByteArray(64)
        secureRandom.nextBytes(codeVerifier)
        
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(codeVerifier)
            .substring(0, 64) // Ensure exact length
    }
    
    /**
     * Generates code challenge from code verifier
     * Method: S256 (SHA-256 hash)
     */
    fun generateCodeChallenge(codeVerifier: String): String {
        val bytes = codeVerifier.toByteArray(Charsets.US_ASCII)
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(bytes)
        
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(digest)
    }
    
    /**
     * Verifies that a code verifier matches a code challenge
     * Used by the authorization server
     */
    fun verifyCodeChallenge(codeVerifier: String, codeChallenge: String): Boolean {
        val computedChallenge = generateCodeChallenge(codeVerifier)
        return computedChallenge == codeChallenge
    }
}
