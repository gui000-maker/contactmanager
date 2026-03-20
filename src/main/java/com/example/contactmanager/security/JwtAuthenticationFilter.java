package com.example.contactmanager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter that runs once per request.
 *
 * <p>Intercepts every incoming HTTP request and checks for a JWT token
 * in the Authorization header. If a valid token is found, the user is
 * authenticated and the SecurityContext is populated so downstream
 * filters and controllers can access the current user.</p>
 *
 * <p>Filter flow:</p>
 * <ul>
 *   <li>No Authorization header or not Bearer → skip, continue chain (unauthenticated)</li>
 *   <li>Token present but invalid/expired → log warning, continue chain (unauthenticated)</li>
 *   <li>Token valid → populate SecurityContext, continue chain (authenticated)</li>
 * </ul>
 *
 * <p>This filter never blocks a request directly. If authentication is
 * required for the endpoint, Spring Security's access control handles
 * the 401/403 response after the filter chain completes.</p>
 *
 * <p>Registered as a bean in {@link SecurityConfig} rather than annotated
 * with {@literal @}Component to avoid circular dependency issues.</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * @param jwtService         handles token parsing, validation and claim extraction
     * @param userDetailsService loads user details from the database by username
     */
    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Core filter logic. Extracts and validates the JWT from the request,
     * then populates the SecurityContext if the token is valid.
     *
     * <p>The SecurityContext is only set if:</p>
     * <ul>
     *   <li>A Bearer token is present in the Authorization header</li>
     *   <li>The token contains a valid, non-null username</li>
     *   <li>No authentication already exists in the SecurityContext</li>
     *   <li>The token signature and expiry are valid for that user</li>
     * </ul>
     *
     * <p>Invalid or expired tokens are caught and logged as warnings.
     * The request continues down the chain either way — rejection
     * happens later via the authentication entry point if the
     * endpoint requires authentication.</p>
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the remaining filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token by removing the "Bearer " prefix (7 characters)
        String token = authHeader.substring(7);

        try {
            String username = jwtService.extractUsername(token);

            // Only authenticate if user is not already authenticated in this request
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isValid(token, userDetails.getUsername())) {

                    // Credentials are null — authentication is token-based, not password-based
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            // Token is malformed, expired or has an invalid signature.
            // Do not set authentication — the request continues as unauthenticated.
            logger.warn("JWT authentication failed: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}