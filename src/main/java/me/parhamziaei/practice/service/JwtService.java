package me.parhamziaei.practice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import me.parhamziaei.practice.entity.jpa.RefreshToken;
import me.parhamziaei.practice.repository.jpa.RefreshTokenRepo;
import me.parhamziaei.practice.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt_hs_256_key}")
    private String SECRET;
    private final UserService userService;
    private final RefreshTokenRepo refreshTokenRepo;

    @Autowired
    public JwtService(UserService userService, RefreshTokenRepo refreshTokenRepo) {
        this.userService = userService;
        this.refreshTokenRepo = refreshTokenRepo;
    }

    public String extractJwtFromRequest(HttpServletRequest request) {
        Optional<Cookie> cookie = Arrays.stream(request.getCookies() != null ? request.getCookies() : new Cookie[0])
                    .filter(c -> c.getName().equals("JWT"))
                    .findFirst();
        return cookie.map(Cookie::getValue).orElse(null);
    }

    public String extractRefreshTokenFromRequest(HttpServletRequest request) {
        Optional<Cookie> cookie = Arrays.stream(request.getCookies() != null ? request.getCookies() : new Cookie[0])
                .filter(c -> c.getName().equals("REFRESH"))
                .findFirst();
        return cookie.map(Cookie::getValue).orElse(null);
    }

    public String extractTwoFactorTokenFromRequest(HttpServletRequest request) {
        Optional<Cookie> cookie = Arrays.stream(request.getCookies() != null ? request.getCookies() : new Cookie[0])
                .filter(c -> c.getName().equals("2FA"))
                .findFirst();
        return cookie.map(Cookie::getValue).orElse(null);
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            return null;
        }
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateRefreshToken(new HashMap<>(), userDetails);
    }

    public String generateRefreshToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        Date expiryDate = new Date(System.currentTimeMillis() + SecurityUtil.REFRESH_JWT_TTL.toMillis());
        String token = Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, getSignKey())
                .compact();

        RefreshToken refreshToken =
                new RefreshToken(
                        token,
                        userDetails.getUsername()
                );

        refreshTokenRepo.save(refreshToken);
        return token;
    }

    public void deActivateRefreshToken(String refreshToken) {
        RefreshToken refreshTokenObj = refreshTokenRepo.findByToken(refreshToken);
        refreshTokenRepo.delete(refreshTokenObj);
    }

    public String generateAccessToken(UserDetails userDetails) {
        return generateAccessToken(new HashMap<>(), userDetails);
    }

    public String generateAccessToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + SecurityUtil.ACCESS_JWT_TTL.toMillis()))
                .signWith(SignatureAlgorithm.HS256, getSignKey())
                .compact();
    }

    public String generateTwoFactorToken(
            String userEmail,
            String sessionId,
            boolean rememberMe
    ) {
        return generateTwoFactorToken(
                userEmail,
                sessionId,
                rememberMe,
                new Date(System.currentTimeMillis() + SecurityUtil.TWO_FACTOR_TOKEN_TTL.toMillis())
        );
    }

    public String generateTwoFactorToken(
            String userEmail,
            String sessionId,
            boolean rememberMe,
            Date expiresIn
    ) {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("purpose", "TF");
        claims.put("remember_me", rememberMe);
        claims.put("session_id", sessionId);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userEmail)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiresIn)
                .signWith(SignatureAlgorithm.HS256, getSignKey())
                .compact();
    }

    public String generateEmailVerifyToken(
            String userEmail,
            String sessionId
    ) {
        return generateEmailVerifyToken(
                userEmail,
                sessionId,
                new Date(System.currentTimeMillis() + SecurityUtil.EMAIL_VERIFY_SESSION_TTL.toMillis())
        );
    }

    public String generateEmailVerifyToken(
            String userEmail,
            String sessionId,
            Date expiresIn
    ) {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("purpose", "EV");
        claims.put("session_id", sessionId);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userEmail)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiresIn)
                .signWith(SignatureAlgorithm.HS256, getSignKey())
                .compact();
    }

    public String generateForgotPasswordToken(
            String userEmail,
            String sessionId
    ) {
        return generateForgotPasswordToken(
                userEmail,
                sessionId,
                new Date(System.currentTimeMillis() + SecurityUtil.FORGOT_PASSWORD_JWT_TTL.toMillis())
        );
    }

    public String generateForgotPasswordToken(
            String userEmail,
            String sessionId,
            Date expiresIn
    ) {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("purpose", "FP");
        claims.put("session_id", sessionId);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userEmail)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiresIn)
                .signWith(SignatureAlgorithm.HS256, getSignKey())
                .compact();
    }

    public boolean isSignatureValid(String token) {
        try {
            Key key = getSignKey();
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException ignored) {
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            if (token == null) {
                return false;
            }
            final String userEmail = extractUsername(token);
            if (userEmail == null) {
                return false;
            }
            UserDetails user = userService.loadUserByUsername(userEmail);
            if (user == null) {
                return false;
            }
            boolean forTwoFactorPurpose = Optional.ofNullable(extractClaim(token, claims -> claims.get("purpose", String.class))).isPresent();
            return (
                    isSignatureValid(token) &&
                    !isTokenExpired(token) &&
                    !forTwoFactorPurpose
            );
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isTwoFactorTokenValid(String token) {
        try {
            final String userEmail = extractUsername(token);
            if (userEmail == null) {
                return false;
            }
            UserDetails user = userService.loadUserByUsername(userEmail);
            if (user == null) {
                return false;
            }
            return (
                    isSignatureValid(token) &&
                    !isTokenExpired(token) &&
                    !(extractClaim(token, claims -> claims.get("purpose", String.class).isEmpty()))
            );
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        try {
            if (!isTokenExpired(token) && isSignatureValid(token)) {
                RefreshToken dbToken = refreshTokenRepo.findByToken(token);
                return  (
                        dbToken != null &&
                        dbToken.getToken().equals(token) &&
                        dbToken.getTokenOwner().equals(userDetails.getUsername()) &&
                        dbToken.isActive()
                );
            }
            return false;
        } catch (Exception ex) {
            System.out.println("refresh token failed");
            return false;
        }
    }

    public boolean extractRememberMeFromTwoFactorToken(String token) {
        return extractClaim(token, claims -> claims.get("remember_me", Boolean.class));
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractSessionIdFromTwoFactorToken(String token) {
        return extractClaim(token, claims -> claims.get("session_id", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
