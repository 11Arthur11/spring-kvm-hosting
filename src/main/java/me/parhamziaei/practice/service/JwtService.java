package me.parhamziaei.practice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.parhamziaei.practice.entity.RefreshToken;
import me.parhamziaei.practice.repository.RefreshTokenRepo;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final static String SECRET = "f633915d212d5159e506d6d69f6d1adcad3aff7396b05545df48183e1331d537";
    private final UserService userService;
    private final RefreshTokenRepo refreshTokenRepo;

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
        Date expiryDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 168);
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
                        userDetails.getUsername(),
                        LocalDateTime.ofInstant(
                                expiryDate.toInstant(),
                                ZoneId.systemDefault()
                        )
                );

        refreshTokenRepo.save(refreshToken);
        return token;
    }

    public void deActivateRefreshToken(String refreshToken) {
        RefreshToken refreshTokenObj = refreshTokenRepo.findByToken(refreshToken);
        refreshTokenRepo.delete(refreshTokenObj);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .signWith(SignatureAlgorithm.HS256, getSignKey())
                .compact();
    }

    public String generateTwoFAToken(
            String userEmail,
            String sessionId
    ) {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("purpose", "2FA");
        claims.put("session_id", sessionId);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userEmail)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 120))
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
        } catch (SignatureException e) {
            return false;
        }
    }

    public boolean isTokenValid(String token) {
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
                    !(extractClaim(token, claims -> claims.get("purpose", String.class).equals("2FA")))
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
                    (extractClaim(token, claims -> claims.get("purpose", String.class).equals("2FA")))
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
                        dbToken.isActive() &&
                        dbToken.getExpiryDate().isAfter(LocalDateTime.now())
                );
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
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
            claims = Jwts.parser()
                    .setSigningKey(getSignKey())
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
