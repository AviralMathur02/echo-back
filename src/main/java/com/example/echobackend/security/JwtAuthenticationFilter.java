package com.example.echobackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays; // Import Arrays for stream operations

// Import specific JWT exceptions if your JwtService throws them
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException; // Added for more specific catches
import io.jsonwebtoken.UnsupportedJwtException; // Added for more specific catches
import io.jsonwebtoken.security.SignatureException; // Or JwtException for a general catch

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = null; // Initialize jwt to null
        String userEmail = null; // Initialize userEmail to null

        // --- Debugging Logs Start ---
        System.out.println("\n--- JwtAuthenticationFilter: Processing request to " + request.getRequestURI() + " ---");
        System.out.println("JwtAuthenticationFilter: Request Method: " + request.getMethod());
        System.out.println("JwtAuthenticationFilter: Authorization Header: " + request.getHeader("Authorization"));
        // --- Debugging Logs End ---

        // 1. Try to extract JWT from 'accessToken' cookie first
        if (request.getCookies() != null) {
            System.out.println("JwtAuthenticationFilter: Checking cookies for 'accessToken'...");
            for (Cookie cookie : request.getCookies()) {
                System.out.println("  Found cookie: " + cookie.getName() + " = " + (cookie.getValue() != null && cookie.getValue().length() > 20 ? cookie.getValue().substring(0, 20) + "..." : cookie.getValue()));
                if ("accessToken".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    System.out.println("JwtAuthenticationFilter: 'accessToken' cookie found.");
                    break; // Found the cookie, no need to check other cookies
                }
            }
            if (jwt == null) {
                System.out.println("JwtAuthenticationFilter: 'accessToken' cookie not present among cookies.");
            }
        } else {
            System.out.println("JwtAuthenticationFilter: No cookies found in request.");
        }


        // 2. If no JWT from cookie, try Authorization header (Bearer token)
        if (jwt == null) {
            System.out.println("JwtAuthenticationFilter: 'accessToken' cookie not found, checking Authorization header...");
            final String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                System.out.println("JwtAuthenticationFilter: JWT extracted from Authorization header.");
            } else {
                System.out.println("JwtAuthenticationFilter: No Bearer token found in Authorization header.");
            }
        }

        // Now, process the found JWT (if any)
        if (jwt == null) {
            System.out.println("JwtAuthenticationFilter: No valid JWT found from any source. Continuing filter chain.");
            filterChain.doFilter(request, response);
            return; // Exit early if no JWT
        }

        // JWT found, now attempt to extract username and authenticate
        try {
            userEmail = jwtService.extractUsername(jwt);
            System.out.println("JwtAuthenticationFilter: Extracted username from JWT: " + userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                System.out.println("JwtAuthenticationFilter: UserDetails loaded for: " + userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("JwtAuthenticationFilter: Authentication SUCCESS for: " + userEmail);
                } else {
                    System.out.println("JwtAuthenticationFilter: Token is INVALID for user: " + userEmail + " (e.g., expired, bad signature).");
                }
            } else if (userEmail == null) {
                System.out.println("JwtAuthenticationFilter: Extracted username was null. Not authenticating.");
            } else { // SecurityContextHolder.getContext().getAuthentication() != null
                System.out.println("JwtAuthenticationFilter: User already authenticated (" + SecurityContextHolder.getContext().getAuthentication().getName() + "). Skipping JWT processing for this request.");
            }
        } catch (ExpiredJwtException e) {
            System.err.println("JWT authentication failed: Token expired: " + e.getMessage());
            // Optionally, you can send an HTTP 401 response directly here
            // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // response.getWriter().write("Token expired");
            // return; // Stop filter chain
        } catch (SignatureException e) {
            System.err.println("JWT authentication failed: Invalid signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("JWT authentication failed: Malformed JWT: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("JWT authentication failed: Unsupported JWT: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("JWT authentication failed: JWT claims string is empty: " + e.getMessage());
        } catch (Exception e) { // Catch any other general exceptions during JWT processing
            System.err.println("JWT authentication failed: General error during JWT processing: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for unexpected errors
        }

        System.out.println("--- JwtAuthenticationFilter: Ending doFilterInternal for " + request.getRequestURI() + " ---\n");
        filterChain.doFilter(request, response);
    }
}