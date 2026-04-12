package com.lostandfound.app.config;

import com.lostandfound.app.exception.RestAccessDeniedHandler;
import com.lostandfound.app.security.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

//  private final JwtAuthenticationFilter jwtAuthFilter;
//  private final TraceIdFilter traceIdFilter;
//  private final CustomAuthenticationEntryPoint authEntryPoint;
  private final RestAccessDeniedHandler accessDeniedHandler;
  private final SecurityProperties securityProperties;

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    log.info("Applying Security Filter Chain configurations...");

    String[] publicRoutes = securityProperties.publicRoutes().toArray(new String[0]);

    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            ex -> {
//              ex.authenticationEntryPoint(authEntryPoint);
              ex.accessDeniedHandler(accessDeniedHandler);
            })
        .authorizeHttpRequests(
            auth ->
                auth
                    // 1. Allow the routes defined in application.yml
                    .requestMatchers(publicRoutes)
                    .permitAll()

                    // 2. Allow ALL public GET requests for articles (Search List & Raw Images)
                    .requestMatchers(HttpMethod.GET, "/api/v1/articles/**")
                    .permitAll()

                    // 3. Everything else (including POST, PUT, DELETE to articles) requires a token
                    .anyRequest()
                    .authenticated());

//    http.addFilterBefore(traceIdFilter, UsernamePasswordAuthenticationFilter.class);
//
//    http.addFilterAfter(jwtAuthFilter, TraceIdFilter.class);

    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    var corsProps = securityProperties.cors();

    config.setAllowedOrigins(corsProps.allowedOrigins());
    config.setAllowedMethods(corsProps.allowedMethods());
    config.setAllowedHeaders(corsProps.allowedHeaders());
    config.setExposedHeaders(corsProps.exposedHeaders());

    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
