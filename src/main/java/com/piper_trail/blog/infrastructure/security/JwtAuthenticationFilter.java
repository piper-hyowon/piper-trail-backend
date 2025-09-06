package com.piper_trail.blog.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final AdminUserDetailsService userDetailsService;

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader(AUTHORIZATION_HEADER);

    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      log.debug("No Authorization header: {}", request.getRequestURI());
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String token = authHeader.substring(BEARER_PREFIX.length());

      if (SecurityContextHolder.getContext().getAuthentication() != null) {
        log.debug("SecurityContext already contains authentication, skipping JWT process");
        filterChain.doFilter(request, response);
        return;
      }

      String username = jwtService.extractUsername(token);

      if (username == null) {
        log.debug("Username could not be extracted from token");
        filterChain.doFilter(request, response);
        return;
      }

      if (jwtService.isRefreshToken(token)) {
        log.debug("Token is not an access token");
        filterChain.doFilter(request, response);
        return;
      }

      if (!jwtService.validateToken(token, username)) {
        log.debug("Token validation failed: {}", username);
        filterChain.doFilter(request, response);
        return;
      }

      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);

    } catch (UsernameNotFoundException e) {
      log.warn("User not found", e.getMessage());
    } catch (Exception e) {
      log.warn("JWT authentication failed: {}", e.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();

    return path.startsWith("/auth/")
        || path.startsWith("/posts/") && "GET".equals(request.getMethod())
        || path.startsWith("/series/")
        || path.startsWith("/comments/")
        || path.startsWith("/monitoring/")
        || path.startsWith("/error");
  }
}
