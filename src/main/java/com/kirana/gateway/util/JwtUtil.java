package com.kirana.gateway.util;

import com.kirana.gateway.constants.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Map;

@Component
public class JwtUtil {

    public static final String SECRET = "6b7723a105e111ec96c7a0aca2295e474846b05c760a222f126ef012a8ddf9b7";

    public Jws<Claims> validateToken(final String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUserId(String accessToken) {
        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(accessToken);
        return claims.getBody().get(Constants.USER_ID, String.class);
    }

}
