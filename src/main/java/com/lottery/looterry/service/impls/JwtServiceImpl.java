package com.lottery.looterry.service.impls;

import com.lottery.looterry.entity.RefreshToken;
import com.lottery.looterry.entity.Users;
import com.lottery.looterry.payload.response.JwtResponse;
import com.lottery.looterry.repository.RefreshTokenRepository;
import com.lottery.looterry.service.JwtService;
import com.lottery.looterry.service.RefreshTokenService;
import com.lottery.looterry.utils.Constant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    @Autowired
    RefreshTokenService refreshTokenService;
    private final int accessTokenDuration = 48 * 60 * 60 * 1000;
    @Value("${token.secrect.keys}")
    private String JWT_SECRET;

    private Key getSigningKey() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
        return key;
    }

    @Override
    public String generateToken(String email, Map<String, Object> extraClaims) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenDuration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    private Date extractExpiration(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getExpiration();
    }

    @Override
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    @Override
    public Boolean isTokenValid(String token, String email) {
        final String emailInsideToken = extractEmail(token);
        if (emailInsideToken.equals(email) && !extractExpiration(token).before(new Date())) {
            return true;
        }
        return false;
    }

    @Override
    public JwtResponse getNewJwtToken(String token) {
        RefreshToken refreshToken = refreshTokenService.handleGetNewRefreshToken(token);
        Users user = refreshToken.getUsers();
        List<String> listRoles = new ArrayList<>();
        listRoles.add(user.getRole().getRoleName());
        return generateJwtResponse(user.getEmail(), listRoles);
    }

    @Override
    public JwtResponse generateJwtResponse(String email, List<String> roles) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(Constant.LIST_ROLE_KEY, roles);
        String accessToken = generateToken(email, extraClaims);
        // RefreshToken refreshToken = refreshTokenService.processRefreshToken(email);
        JwtResponse jwtResponse = JwtResponse.builder()
                .accessToken(accessToken)
                // .refreshToken(refreshToken.getRefreshToken())
                .build();
        return jwtResponse;
    }
}