package rs.ac.bg.fon.ebanking.security.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.bg.fon.ebanking.security.token.TokenRepository;
import rs.ac.bg.fon.ebanking.security.token.TokenType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {


    private final TokenRepository tokenRepository;
    private final JwtService jwtService;

    @Value("${jwt.refresh-cookie-name:refresh_token}")
    private String refreshCookieName;

    @Value("${jwt.refresh-pepper:pepper-change-me}")
    private String refreshPepper;

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            String rawRefresh = pickLatestValidRefresh(request);
            if (rawRefresh != null) {
                String hash = sha256(refreshPepper + rawRefresh);
                tokenRepository.findByToken(hash).ifPresent(t -> {
                    if (t.getTokenType() == null || t.getTokenType() == TokenType.REFRESH) {
                        t.setRevoked(true);
                        t.setExpired(true);
                        tokenRepository.save(t);
                    }
                });
            }
        } catch (Exception ignore) {
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private String pickLatestValidRefresh(HttpServletRequest req) {
        Cookie[] cs = Optional.ofNullable(req.getCookies()).orElse(new Cookie[0]);
        return Stream.of(cs)
                .filter(c -> refreshCookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(Objects::nonNull)
                .map(val -> {
                    try {
                        String typ = jwtService.extractTyp(val);
                        if (!"refresh".equals(typ) || jwtService.isTokenExpired(val)) return null;
                        Date iat = jwtService.extractIssuedAt(val);
                        return new AbstractMap.SimpleEntry<>(val, iat);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .max(Comparator.comparing(e -> Optional.ofNullable(e.getValue()).orElse(new Date(0))))
                .map(AbstractMap.SimpleEntry::getKey)
                .orElse(null);
    }

    private String sha256(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
    }
}
