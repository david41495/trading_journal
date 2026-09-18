package com.davidserrano.tradejournal.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.davidserrano.tradejournal.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByEmailIgnoreCase(username)
                .map(user -> User.withUsername(user.getEmail())
                        .password(user.getPasswordHash())
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "Account not found."));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfRepository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/login.html",
                                "/css/**",
                                "/js/auth.js",
                                "/api/auth/csrf",
                                "/api/auth/register",
                                "/error")
                        .permitAll()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.csrfTokenRepository(csrfRepository))
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler((request, response, authentication) ->
                                writeJson(response, HttpStatus.OK,
                                        "{\"message\":\"Login successful.\"}"))
                        .failureHandler((request, response, exception) ->
                                writeJson(response, HttpStatus.UNAUTHORIZED,
                                        "{\"message\":\"Invalid email or password.\"}"))
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                writeJson(response, HttpStatus.OK,
                                        "{\"message\":\"Logout successful.\"}"))
                        .deleteCookies("JSESSIONID"))
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                request -> request.getRequestURI()
                                        .startsWith("/api/")));

        return http.build();
    }

    private static void writeJson(HttpServletResponse response,
            HttpStatus status, String body) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(body);
    }
}
