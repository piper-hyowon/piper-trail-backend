package com.piper_trail.blog.infrastructure.security;

import com.piper_trail.blog.shared.domain.Admin;
import com.piper_trail.blog.shared.domain.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserDetailsService implements UserDetailsService {

  private final AdminRepository adminRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Admin admin =
        adminRepository
            .findByUsername(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("Admin NotFound" + username));

    return User.builder()
        .username(admin.getUsername())
        .password(admin.getPassword())
        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .disabled(false)
        .build();
  }
}
