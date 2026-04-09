package com.lostandfound.app.security;

import com.lostandfound.app.model.AuthProvider;
import com.lostandfound.app.model.RefreshToken;
import com.lostandfound.app.model.User;
import com.lostandfound.app.repository.UserRepository;
import com.lostandfound.app.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Value("${PUBLIC_FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    @Value("${jwt.expiration.access-token}")
    private long accessTokenDurationMs;

    @Value("${jwt.expiration.refresh-token}")
    private long refreshTokenDurationMs;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not provided by Google");
        }

        log.info("OAuth2 login successful for email: {}", email);
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getProvider() != AuthProvider.GOOGLE) {
                log.warn("Account collision detected. Email {} exists as a LOCAL account.", email);
                String errorUrl = frontendUrl + "/login?error=" + URLEncoder.encode("Email already registered with a password. Please login normally.", StandardCharsets.UTF_8);
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
                return;
            }
        } else {
            log.info("Registering new OAuth2 user: {}", email);
            user = User.builder()
                    .email(email)
                    .displayName(name)
                    .provider(AuthProvider.GOOGLE)
                    .providerId(providerId)
                    .emailVerified(true)
                    .isLocked(false)
                    .build();
            userRepository.save(user);
        }

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(user.getId());

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true).secure(true).path("/").maxAge(accessTokenDurationMs / 1000).sameSite("Lax").build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshTokenEntity.getRawToken())
                .httpOnly(true).secure(true).path("/").maxAge(refreshTokenDurationMs / 1000).sameSite("Lax").build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/dashboard");
    }
}