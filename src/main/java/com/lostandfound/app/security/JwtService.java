package com.lostandfound.app.security;

import com.lostandfound.app.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.expiration.access-token}")
  private long accessTokenExpiration;


  public String generateAccessToken(UserDetails userDetails) {
    String token = buildToken(new HashMap<>(), userDetails, accessTokenExpiration);
    log.info("[{}] Access token generated for user: {}", getTraceId(), userDetails.getUsername());
    return token;
  }


  private String buildToken(
          Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    log.debug("[{}] Building access token for user: {}", getTraceId(), userDetails.getUsername());

    if (userDetails instanceof User user) {
      extraClaims.put("userId", user.getId().toString());
      extraClaims.put("tokenType", "access");

      extraClaims.put(
              "roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }

    return Jwts.builder()
            .claims(extraClaims)
            .subject(userDetails.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey())
            .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractClaim(token, Claims::getSubject);
    return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  public <T> T extractClaim(String token, Function<Claims, T> resolver) {
    final Claims claims = extractAllClaims(token);
    return resolver.apply(claims);
  }

  public Claims extractAllClaims(String token) {
    try {
      return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      log.trace("[{}] Token expired", getTraceId());
      throw e;
    } catch (MalformedJwtException | SignatureException e) {
      log.error("[{}] Token integrity error: {}", getTraceId(), e.getMessage());
      throw e;
    } catch (IllegalArgumentException e) {
      log.error("[{}] Token claims string is empty: {}", getTraceId(), e.getMessage());
      throw e;
    }
  }

  private boolean isTokenExpired(String token) {
    return extractClaim(token, Claims::getExpiration).before(new Date());
  }

  private SecretKey getSignInKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
  }

  public String getTraceId() {
    return Objects.toString(MDC.get("traceId"), "SYSTEM");
  }
}