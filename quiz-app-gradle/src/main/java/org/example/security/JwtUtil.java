package org.example.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.example.exception.AuthException;
import org.example.model.Role;
import org.example.model.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class JwtUtil {
    private static final String JWT_SECRET = System.getenv("JWT_SECRET");

    private static final long ONE_DAY_IN_MILLISECONDS = 24L * 60L * 60L * 1000L; // 86,400,000 milliseconds
    private static final long EXPIRATION_TIME = 10L * ONE_DAY_IN_MILLISECONDS; // 10 days
    //private static final long REFRESH_EXPIRATION_TIME = 70L * ONE_DAY_IN_MILLISECONDS; // 70 days

    private final Key key;

    public JwtUtil() {
        if (JWT_SECRET == null || JWT_SECRET.trim().isEmpty()) {
            throw new IllegalStateException("JWT_SECRET environment variable is not set");
        }
        if (JWT_SECRET.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", roles)
                .claim("userId", user.getId())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /*public String generateRefreshToken(User user) {
        return generateToken(user, REFRESH_EXPIRATION_TIME);
    }
    */



    public boolean validateToken(String token) throws AuthException {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            throw new AuthException.InvalidTokenException();
        } catch (ExpiredJwtException e) {
            throw new AuthException.TokenExpiredException();
        } catch (Exception e) {
            throw new AuthException("Invalid token: " + e.getMessage());
        }

    }

    /*
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (AuthException e) {
            return false;
        }
    }
    */

    public Claims getAllClaimsFromToken(String token) throws AuthException {
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new AuthException("Could not parse token claims");
        }
    }

    public String getUsernameFromToken(String token) throws AuthException {
        return getAllClaimsFromToken(token).getSubject();
    }

    /*
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) throws AuthException {
        Claims claims = getAllClaimsFromToken(token);
        List<String> roles = claims.get("roles", List.class);
        if (roles == null) {
            return new ArrayList<>();
        }
        return roles;
    }

    public Long getUserIdFromToken(String token) throws AuthException {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException | AuthException e) {
            return true;
        }
    }

    public Date getExpirationDateFromToken(String token) throws AuthException {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getExpiration();
    }

    public String refreshToken(String token) throws AuthException {
        if (validateToken(token)) {
            throw new AuthException("Invalid token for refresh");
        }

        Claims claims = getAllClaimsFromToken(token);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }
    */
}
