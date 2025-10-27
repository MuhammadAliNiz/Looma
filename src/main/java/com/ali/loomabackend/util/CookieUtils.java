package com.ali.loomabackend.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {
    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    public ResponseCookie createHttpOnlyCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true) // Should be true in production
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    public String extractCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Creates a ResponseCookie that instructs the browser to delete the specified cookie.
     * It does this by setting the cookie's max age to 0.
     *
     * @param name The name of the cookie to delete.
     * @return A ResponseCookie object configured to clear the cookie.
     */
    public ResponseCookie deleteHttpOnlyCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true) // Must match the properties of the cookie being deleted
                .sameSite("Strict")
                .path("/")
                .maxAge(0) // The key to deleting the cookie
                .build();
    }
}
