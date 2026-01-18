package com.fullstack.resumebuilder.util;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class JwtUtil {

    @Value("${jwt.token.secret}")
    private String jwtSecret;

    @Value("${jwt.token.expiration}")
    private long jwtExpiration;


}
