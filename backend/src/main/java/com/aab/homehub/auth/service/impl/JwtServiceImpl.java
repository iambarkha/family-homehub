package com.aab.homehub.auth.service.impl;


import com.aab.homehub.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Override
    public String extractUsername(String token) {
        if (token == null || token.isBlank()) return null;
        return extractClaim(token, Claims::getSubject);
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateToken(Map.of(), userDetails);
    }




  /*  private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey.trim()); // ← add .trim()
        System.out.println("JWT_SECRET=[" + secretKey + "]");
        System.out.println("Length=" + secretKey.length());
        return Keys.hmacShaKeyFor(keyBytes);
    }*/
  private SecretKey getSigningKey() {
      String normalized = secretKey
              .replaceAll("\\s+", "");

      byte[] keyBytes = Decoders.BASE64.decode(normalized);

      return Keys.hmacShaKeyFor(keyBytes);
  }

    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        // Ensure we call build() before parseClaimsJws
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public long getExpirationMs() {
        return jwtExpiration;
    }
    }
