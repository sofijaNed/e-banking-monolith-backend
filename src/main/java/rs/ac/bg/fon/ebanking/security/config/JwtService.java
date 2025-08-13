package rs.ac.bg.fon.ebanking.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import rs.ac.bg.fon.ebanking.user.User;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
//    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access-token-ms:900000}")
    private long accessTokenMs;

    @Value("${jwt.refresh-token-ms:604800000}")
    private long refreshTokenMs;

    public String extractUsername(String token){
        return extractClaim(token,Claims::getSubject);
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(),userDetails);
    }



    public String generateToken(Map<String,Object> extractClaims, UserDetails userDetails){
        return buildToken(extractClaims,userDetails,accessTokenMs);
    }


    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshTokenMs);
    }


    private String buildToken(Map<String,Object> extraClaims,UserDetails userDetails,long expiration){
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }


    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    private Date extractExpiration(String token) {
        return extractClaim(token,Claims::getExpiration);
    }


    private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    private Key getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generatePreAuthToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("2fa", true); // flag da je ovo 2FA token
        claims.put("purpose", "LOGIN_2FA");

        long expirationMillis = 3 * 60 * 1000;

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsernameFromPreAuth(String token) {
        Claims claims = extractAllClaims(token);
        if (!"LOGIN_2FA".equals(claims.get("purpose"))) {
            throw new BadCredentialsException("Invalid preAuth token");
        }
        return claims.getSubject();
    }
}
