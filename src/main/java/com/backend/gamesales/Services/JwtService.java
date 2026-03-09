package com.backend.gamesales.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expirationMs}")
    private long jwtExpirationMs;

    private Key getSignInKey(){
        byte [] keyBytes= Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public Claims extractAllClaims(String token){

        return Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public<T>T extractClaim(String token, Function<Claims,T> classResolver){
        final  Claims claims =extractAllClaims(token);
        return classResolver.apply(claims);
    }
    public String extractUsername(String token){
        return extractClaim(token,Claims::getSubject);
    }
    private Date extractExpiration(String token){
        return extractClaim(token,Claims::getExpiration);
    }
    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username=extractUsername(token);
        return (username.equals(userDetails.getUsername()))&& !isTokenExpired(token);
    }
    public String generateToken(Map<String,Object> extractClaims, UserDetails  userDetails){
        return Jwts.builder()
                .setClaims(extractClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+jwtExpirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
         public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(),userDetails);
    }

    private long getJwtExpirationMs(){
        return jwtExpirationMs;
    }
    private String renewTokenFromOldToken(String oldToken,UserDetails userDetails) {
        try {
            Claims claims = extractAllClaims(oldToken);
            Map<String,Object> extraClaims = new HashMap<>();

            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                if (!entry.getKey().equals(Claims.EXPIRATION) &&
                        !entry.getKey().equals(Claims.ISSUED_AT) &&
                        !entry.getKey().equals(Claims.SUBJECT)) {
                    extraClaims.put(entry.getKey(), entry.getValue());
                }
            }
            return generateToken(extraClaims, userDetails);
        } catch (Exception e) {
            return generateToken(userDetails);
        }
    }
    public String generateForgotPasswordToken(UserDetails userDetails){
        Map<String,Object>extraClaims=new HashMap<>();
        extraClaims.put("type","FORGOT_PASSWORD");
         return Jwts.builder()
                 .setClaims(extraClaims)
                 .setSubject(userDetails.getUsername())
                 .setIssuedAt(new Date(System.currentTimeMillis()))
                 .setExpiration(new Date(System.currentTimeMillis()+ (10 * 60 * 1000)))
                 .signWith(getSignInKey(),SignatureAlgorithm.HS256)
                 .compact();
    }
}
