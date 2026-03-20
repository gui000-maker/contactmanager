package com.example.contactmanager.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * Service responsible for JWT token generation, validation and claim extraction.
 *
 * <p>Tokens are signed with HMAC-SHA256 using a secret key loaded from
 * {@code jwt.secret} in application properties. The key must be at least
 * 32 characters long to meet the HMAC-SHA256 minimum key length requirement —
 * shorter keys will cause an exception at startup.</p>
 *
 * <p>Token expiry is configurable via {@code jwt.expiration} (milliseconds).
 * Example: 86400000 = 24 hours.</p>
 *
 * <p>This service does not store tokens — it only issues and validates them.
 * There is no token revocation mechanism; tokens remain valid until expiry.</p>
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final Key key;
    private final long expiration;

    /**
     * Constructs the service and builds the signing key from the secret.
     *
     * <p>The signing key is built once at startup and reused for all
     * operations — it is not rebuilt per request.</p>
     *
     * @param secret     the HMAC-SHA256 signing secret (min 32 characters)
     * @param expiration token validity duration in milliseconds
     */
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes()); // key built once at startup
        this.expiration = expiration;
    }

    /**
     * Generates a signed JWT token for the given username.
     *
     * <p>The username is stored as the subject claim. The token is
     * issued at the current time and expires after the configured
     * duration.</p>
     *
     * @param username the username to embed as the subject claim
     * @return a signed, compact JWT string
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the username (subject claim) from a token.
     *
     * @param token the JWT string to parse
     * @return the username embedded in the subject claim
     * @throws JwtException if the token is malformed, expired or has an invalid signature
     */
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Validates a token against the expected username.
     *
     * <p>Returns {@code false} rather than throwing if the token is
     * null, blank, expired or does not match the username — this lets
     * {@link JwtAuthenticationFilter} handle the unauthenticated case
     * gracefully without a try/catch at every call site.</p>
     *
     * @param token    the JWT string to validate
     * @param username the expected username to match against the subject claim
     * @return {@code true} if the token is valid, matches the username and is not expired
     */
    public boolean isValid(String token, String username) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String extractedUsername = extractUsername(token);

        return extractedUsername.equals(username) && !isExpired(token);
    }

    /**
     * Checks whether the token's expiry date is in the past.
     *
     * @param token the JWT string to check
     * @return {@code true} if the token is expired
     */
    private boolean isExpired(String token) {
        return getClaims(token)
                .getExpiration()
                .before(new Date());
    }

    /**
     * Parses and returns all claims from the token body.
     *
     * <p>Verifies the signature against the signing key as part of parsing.
     * Any tampered, malformed or expired token will throw a
     * {@link JwtException} here.</p>
     *
     * @param token the JWT string to parse
     * @return the verified claims from the token body
     * @throws JwtException if signature verification fails or the token is invalid
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}