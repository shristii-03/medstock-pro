package com.medstockpro.service;

import com.medstockpro.config.JwtUtil;
import com.medstockpro.dto.LoginRequest;
import com.medstockpro.dto.LoginResponse;
import com.medstockpro.entity.User;
import com.medstockpro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil               jwtUtil;
    private final UserRepository        userRepository;

    public LoginResponse login(LoginRequest request) {

        // Authenticate credentials
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String role = userDetails.getAuthorities()
                .iterator().next().getAuthority();

        // Fetch full user for name
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return LoginResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(
                        userDetails.getUsername(), role))
                .refreshToken(jwtUtil.generateRefreshToken(
                        userDetails.getUsername()))
                .email(user.getEmail())
                .name(user.getName())
                .role(role)
                .build();
    }
}