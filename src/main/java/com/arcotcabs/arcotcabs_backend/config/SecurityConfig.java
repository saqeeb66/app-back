package com.arcotcabs.arcotcabs_backend.config;

import com.arcotcabs.arcotcabs_backend.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            SecretKey jwtKey
    ) throws Exception {

        JwtFilter jwtFilter = new JwtFilter(jwtKey);

        http
                // 🔐 JWT → NO SESSIONS
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ❌ CSRF NOT NEEDED FOR APIs
                .csrf(csrf -> csrf.disable())

                // 🌍 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 🔐 AUTH RULES
                .authorizeHttpRequests(auth -> auth

                        // ✅ PUBLIC
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/test/**",
                                "/error",
                                "/actuator/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🔥 ROLE BASED (FIXED)
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/driver/**").hasAuthority("ROLE_DRIVER")
                        .requestMatchers("/api/user/**").hasAuthority("ROLE_USER")


                        .anyRequest().authenticated()
                )

                // 🔑 JWT FILTER
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /* ================= CORS ================= */

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // 🔥 ALLOW ALL (DEV)
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /* ================= PASSWORD ================= */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
