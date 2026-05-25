package io.wahid.publication.security;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenVerifier {

    private static final Logger LOGGER = Logger.getLogger(TokenVerifier.class.getName());
    private static FirebaseAuth auth;

    private TokenVerifier() {
    }

    public static synchronized FirebaseToken verify(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return null;

        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }

        String idToken = authHeader.substring("Bearer ".length());

        try {
            // Verify Firebase ID token
            return auth.verifyIdToken(idToken);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Exception occurred with token -> {0}", idToken);
            LOGGER.log(Level.SEVERE, "Exception occurred with token -> ", e);
            return null;
        }
    }
}
