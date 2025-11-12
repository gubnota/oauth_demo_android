const express = require('express');
const fetch = require('node-fetch');
const app = express();

// Your GitHub OAuth credentials
const GITHUB_CLIENT_ID = "Ov23liBjDlU1iHq6w2TK";
const GITHUB_CLIENT_SECRET = "a18fb96c041175cfbb7b0113da462e80a24536ad";
const BACKEND_REDIRECT_URI = "http://localhost:3000/oauth/callback";
const APP_REDIRECT_URI = "oauth2demo://callback";

// Middleware
app.use(express.json());

// OAuth callback endpoint
app.get('/oauth/callback', async (req, res) => {
    const code = req.query.code;
    const state = req.query.state;
    const error = req.query.error;

    console.log("=== GitHub OAuth Callback Received ===");
    console.log("Code:", code?.substring(0, 10) + "...");
    console.log("State:", state);
    console.log("Error:", error);

    // Handle error from GitHub
    if (error) {
        console.log("❌ OAuth Error:", error);
        const errorDesc = req.query.error_description || "";
        return res.redirect(`${APP_REDIRECT_URI}?error=${error}&error_description=${errorDesc}`);
    }

    // Validate code exists
    if (!code) {
        console.log("❌ No authorization code received");
        return res.redirect(`${APP_REDIRECT_URI}?error=no_code`);
    }

    try {
        // Exchange authorization code for access token
        console.log("\n📤 Exchanging authorization code for access token...");
        console.log("Sending POST to: https://github.com/login/oauth/access_token");

        const tokenResponse = await fetch('https://github.com/login/oauth/access_token', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                'User-Agent': 'OAuth2Demo'
            },
            body: JSON.stringify({
                client_id: GITHUB_CLIENT_ID,
                client_secret: GITHUB_CLIENT_SECRET,
                code: code,
                redirect_uri: BACKEND_REDIRECT_URI
            })
        });

        console.log("Response status:", tokenResponse.status);

        const tokenData = await tokenResponse.json();
        console.log("\n📥 Token Response:");
        console.log(JSON.stringify(tokenData, null, 2));

        // Check for errors in response
        if (tokenData.error) {
            console.log("❌ Token exchange error:", tokenData.error);
            console.log("Error description:", tokenData.error_description);
            return res.redirect(
                `${APP_REDIRECT_URI}?error=${tokenData.error}&error_description=${tokenData.error_description}`
            );
        }

        // Extract access token
        const accessToken = tokenData.access_token;
        const tokenType = tokenData.token_type || "Bearer";
        const scope = tokenData.scope || "";

        if (!accessToken) {
            console.log("❌ No access token in response");
            return res.redirect(`${APP_REDIRECT_URI}?error=no_token`);
        }

        console.log("\n✅ Token exchange successful!");
        console.log("Access Token:", accessToken.substring(0, 20) + "...");
        console.log("Token Type:", tokenType);
        console.log("Scope:", scope);

        // Redirect back to Android app with token
        console.log(`\n🔄 Redirecting to Android app: ${APP_REDIRECT_URI}?token=${accessToken.substring(0, 10)}...`);
        
        const redirectUrl = `${APP_REDIRECT_URI}?token=${accessToken}&token_type=${tokenType}`;
        res.redirect(redirectUrl);

    } catch (error) {
        console.error("❌ Error during token exchange:", error.message);
        res.redirect(`${APP_REDIRECT_URI}?error=server_error&error_description=${error.message}`);
    }
});

// Health check endpoint
app.get('/health', (req, res) => {
    console.log("Health check received");
    res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

// Test endpoint to simulate OAuth flow
app.get('/test', (req, res) => {
    console.log("\n=== Test Endpoint ===");
    console.log("Backend is running and ready to handle OAuth callbacks");
    res.json({
        message: "OAuth2 backend is running",
        redirectUri: BACKEND_REDIRECT_URI,
        appRedirectUri: APP_REDIRECT_URI
    });
});

// Start server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`\n🚀 OAuth Backend Server running on http://localhost:${PORT}`);
    console.log(`📍 OAuth Callback URL: http://localhost:${PORT}/oauth/callback`);
    console.log(`📍 Register this in GitHub: ${BACKEND_REDIRECT_URI}`);
    console.log(`📱 Android app will receive token at: ${APP_REDIRECT_URI}`);
    console.log("\nWaiting for OAuth callbacks...\n");
});
