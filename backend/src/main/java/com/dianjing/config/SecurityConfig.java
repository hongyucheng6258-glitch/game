package com.dianjing.config;

import com.dianjing.filter.RateLimitFilter;
import com.dianjing.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, RateLimitFilter rateLimitFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 放行路径
                .requestMatchers("/api/v1/users/register", "/api/v1/users/login", "/api/v1/users/send-sms", "/api/v1/users/phone-login", "/api/v1/users/reset-password").permitAll()
                .requestMatchers("/api/v1/settings").permitAll()
                .requestMatchers("/api/v1/public-settings").permitAll()
                .requestMatchers("/api/v1/services").permitAll()
                .requestMatchers("/api/v1/services/{id}").permitAll()
                .requestMatchers("/api/v1/services/recommend").permitAll()
                .requestMatchers("/api/v1/service-tags").permitAll()
                .requestMatchers("/api/v1/level/list").permitAll()
                .requestMatchers("/api/v1/public/settings").permitAll()
                .requestMatchers("/api/v1/announcements").permitAll()
                .requestMatchers("/api/v1/announcements/{id}").permitAll()
                .requestMatchers("/api/v1/activities").permitAll()
                .requestMatchers("/api/v1/activities/{id}").permitAll()
                .requestMatchers("/api/v1/statistics/ranking").permitAll()
                .requestMatchers("/api/v1/statistics/platform").permitAll()
                .requestMatchers("/api/v1/rankings/**").permitAll()
                .requestMatchers("/api/v1/reviews/service/{serviceId}").permitAll()
                .requestMatchers("/api/v1/reviews/stats/{providerId}").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**", "/v3/api-docs").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                // 管理员路径
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // 其他路径需要认证
                .anyRequest().authenticated()
            )
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
