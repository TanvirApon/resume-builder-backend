package com.fullstack.resumebuilder.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;


@Component
public class JwtUtil {

    @Value("${jwt.token.secret}")
    private String jwtSecret;

    @Value("${jwt.token.expiration}")
    private long jwtExpiration;


    public String generateToken(String userId){
         Date now = new Date();
         Date expiryDate = new Date(now.getTime() + jwtExpiration);

         return Jwts.builder()
                 .setSubject(userId)
                 .setIssuedAt(now)
                 .setExpiration(expiryDate)
                 .signWith(getSingingKey())
                 .compact();
    }

    private Key getSingingKey(){
       return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String getUserIdFromToken(String token) {
       Claims claim =  Jwts.parser()
                .setSigningKey(getSingingKey())
                .parseClaimsJws(token)
                .getBody();
       return claim.getSubject();
    }

    public boolean ValidateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSingingKey())
                    .parseClaimsJws(token);
            return true;
        }
        catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claim =  Jwts.parser()
                    .setSigningKey(getSingingKey())
                    .parseClaimsJws(token)
                    .getBody();
            return claim.getExpiration().before(new Date());
        }
        catch (JwtException | IllegalArgumentException e){
            return true;
        }
    }
}
