# OAuth2 Android Educational Example

A complete educational example of OAuth2 authentication in Android using Kotlin and XML layouts.

## Features

- ✅ OAuth2 Authorization Code Flow with PKCE
- ✅ WebView-based authentication
- ✅ Deep linking for callback handling
- ✅ Custom URL scheme (oauth2demo://)
- ✅ Test scenarios for success and failure
- ✅ GitHub OAuth2 integration (configurable)
- ✅ State parameter for CSRF protection
- ✅ Clean Kotlin code with comments

## Quick Start

### 1. Configure OAuth2 Credentials

#### For GitHub:
1. Go to https://github.com/settings/developers
2. Click "New OAuth App"
3. Fill in the details:
   - Application name: OAuth2 Demo
   - Homepage URL: http://localhost
   - Authorization callback URL: **oauth2demo://callback**
4. Copy the "Client ID"
5. Open `app/src/main/java/com/example/oauth2demo/OAuthConfig.kt`
6. Replace `YOUR_GITHUB_CLIENT_ID` with your actual Client ID

### 2. Open in Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the `OAuth2AndroidExample` folder
4. Wait for Gradle sync to complete

### 3. Run the App

1. Connect an Android device or start an emulator
2. Click the "Run" button (or press Shift+F10)
3. The app will build and install

## How It Works

### OAuth2 Authorization Code Flow with PKCE

```
User Clicks Login
      ↓
App Generates PKCE Codes
(code_verifier → SHA-256 → code_challenge)
      ↓
WebView Loads Authorization Endpoint
      ↓
User Authenticates on OAuth Provider
      ↓
OAuth Provider Redirects to oauth2demo://callback
      ↓
Android Intercepts Deep Link
      ↓
App Receives Authorization Code
      ↓
App Verifies State Parameter (CSRF Protection)
      ↓
Success! Authorization Code Extracted
      ↓
Success Screen Displayed
```

## Project Structure

```
OAuth2AndroidExample/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/oauth2demo/
│   │   │   ├── MainActivity.kt
│   │   │   ├── LoginActivity.kt
│   │   │   ├── SuccessActivity.kt
│   │   │   ├── OAuthConfig.kt
│   │   │   └── PKCEHelper.kt
│   │   └── res/
│   │       ├── layout/
│   │       ├── values/
│   │       └── drawable/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Key Files

### MainActivity.kt
- Entry point of the application
- Handles deep link callbacks from OAuth2 provider
- Implements test scenarios (success/failure)
- Verifies state parameter to prevent CSRF attacks

### LoginActivity.kt
- Displays OAuth2 login page in WebView
- Intercepts the redirect URI
- Generates PKCE code_verifier and code_challenge
- Handles authentication errors

### SuccessActivity.kt
- Shows successful authentication confirmation
- Displays authorization code and simulated access token
- Demonstrates complete OAuth2 flow

### OAuthConfig.kt
- Centralized OAuth2 configuration
- Manages state and code_verifier storage
- Builds authorization URL with PKCE parameters
- Supports multiple providers (GitHub, Google, Yandex)

### PKCEHelper.kt
- Generates cryptographically secure code_verifier
- Creates SHA-256 code_challenge
- Implements RFC 7636 specification

## Test Scenarios

### ✓ Test Successful Login
- Simulates a complete OAuth2 flow
- Demonstrates authorization code reception
- Shows success screen

### ✗ Test Failed Login
- Simulates authentication failure
- Shows error handling
- Demonstrates error messages

## Security Features

1. **PKCE (Proof Key for Code Exchange)**
   - Protects against authorization code interception
   - Implements RFC 7636

2. **State Parameter**
   - Prevents CSRF attacks
   - Validated on callback

3. **URL Scheme Verification**
   - Custom deep link scheme
   - Intent filter ensures app receives callback

## Configuration

### Supported OAuth2 Providers

The app comes configured for GitHub but can be adapted for:

- **Google OAuth2**: Uncomment Google configuration in OAuthConfig.kt
- **Yandex OAuth2**: Uncomment Yandex configuration in OAuthConfig.kt
- **Any OAuth2 provider**: Update endpoints and scopes

### Update Provider Configuration

Edit `OAuthConfig.kt`:

```kotlin
const val CLIENT_ID = "YOUR_CLIENT_ID"
const val AUTHORIZATION_ENDPOINT = "https://provider.com/oauth/authorize"
const val TOKEN_ENDPOINT = "https://provider.com/oauth/token"
const val REDIRECT_URI = "oauth2demo://callback"
const val SCOPE = "user read:user"
```

## Requirements

- Android SDK 24 (API Level 24) or higher
- Kotlin 1.9.0+
- Android Studio Giraffe or later
- Java 8+

## Troubleshooting

### Deep link not working

Verify the manifest intent filter:
```xml
<data
    android:scheme="oauth2demo"
    android:host="callback" />
```

### "Authorization callback URL mismatch"

Ensure the redirect URI in your OAuth app settings exactly matches:
```
oauth2demo://callback
```

### WebView not loading

Check permissions in AndroidManifest.xml:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## References

- [RFC 6749 - OAuth 2.0 Authorization Framework](https://tools.ietf.org/html/rfc6749)
- [RFC 7636 - Proof Key for Code Exchange (PKCE)](https://tools.ietf.org/html/rfc7636)
- [RFC 8252 - OAuth 2.0 for Native Apps](https://tools.ietf.org/html/rfc8252)
- [GitHub OAuth Documentation](https://docs.github.com/en/developers/apps/building-oauth-apps)
- [Android Deep Linking](https://developer.android.com/training/app-links/deep-linking)

## Security Considerations

⚠️ **For Production Use:**

- Never embed client secrets in mobile apps
- Always use PKCE for public clients
- Validate state parameter on every callback
- Use Android Keystore for token storage
- Implement proper SSL certificate validation
- Consider using AppAuth library for production apps
- Store sensitive data securely
- Implement token refresh mechanism

## License

MIT License - Free to use for educational purposes

## Author

Generated as an educational example for OAuth2 implementation in Android.

