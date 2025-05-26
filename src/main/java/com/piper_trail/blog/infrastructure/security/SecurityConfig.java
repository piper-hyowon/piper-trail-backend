package com.piper_trail.blog.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.CrossOriginEmbedderPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Value("${REQUIRE_HTTPS:false}")
  private boolean requireHttps;

  @Value("${ENABLE_SECURITY_HEADERS:false}")
  private boolean enableSecurityHeaders;

  @Value("${CORS_ALLOWED_ORIGINS}")
  private String[] allowedOrigins;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/auth/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/posts/**")
                    .permitAll()
                    .requestMatchers("/chat/**")
                    .permitAll()
                    .requestMatchers("/error")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/comments")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/posts/*/comments")
                    .permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/posts/*/comments/*")
                    .permitAll()
                    .requestMatchers(HttpMethod.PATCH, "/posts/*/comments/*/review")
                    .hasRole("ADMIN")
                    .requestMatchers("/dashboard/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/statistics/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/posts/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/posts/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/posts/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            exceptions ->
                exceptions
                    // 인증 실패 (토큰 없음, 만료 등)
                    .authenticationEntryPoint(
                    (request, response, authException) -> {
                      response.setStatus(401);
                      response.setContentType("application/json;charset=UTF-8");
                      response
                          .getWriter()
                          .write(
                              """
                        {
                            "error": "Unauthorized",
                            "message": "인증이 필요합니다"
                        }
                        """);
                    }));

    configureEnvironmentSpecificSecurity(http);

    return http.build();
  }

  private void configureEnvironmentSpecificSecurity(HttpSecurity http) throws Exception {
    // HTTPS 강제
    if (requireHttps) {
      http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
    }

    if (enableSecurityHeaders) {
      http.headers(
          headers ->
              headers
                  .contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::disable)
                  .httpStrictTransportSecurity(
                      hstsConfig ->
                          hstsConfig
                              .maxAgeInSeconds(31536000) // 1년
                              .includeSubDomains(true)
                              .preload(true))
                  .crossOriginEmbedderPolicy(
                      coep ->
                          coep.policy(
                              CrossOriginEmbedderPolicyHeaderWriter.CrossOriginEmbedderPolicy
                                  .REQUIRE_CORP))
                  .addHeaderWriter(
                      new StaticHeadersWriter(
                          "Content-Security-Policy",
                          "default-src 'self'; "
                              + "script-src 'self'; "
                              + "style-src 'self' 'unsafe-inline'; "
                              + "img-src 'self' data: https:; "
                              + "object-src 'none'; "
                              + "frame-ancestors 'none';"))
                  .addHeaderWriter(new StaticHeadersWriter("X-XSS-Protection", "1; mode=block"))
                  .addHeaderWriter(
                      new StaticHeadersWriter("Referrer-Policy", "strict-origin-when-cross-origin"))
                  .addHeaderWriter(
                      new StaticHeadersWriter(
                          "Permissions-Policy", "camera=(), microphone=(), geolocation=()")));
    }
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));

    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    configuration.setAllowedHeaders(
        Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Cache-Control"));

    configuration.setAllowCredentials(true);

    configuration.setMaxAge(3600L);

    configuration.setExposedHeaders(Arrays.asList("Authorization", "X-Total-Count"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}
