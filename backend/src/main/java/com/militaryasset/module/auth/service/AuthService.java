package com.militaryasset.module.auth.service;

import com.militaryasset.module.auth.dto.JwtResponseDTO;
import com.militaryasset.module.auth.dto.LoginRequestDTO;
import com.militaryasset.module.auth.entity.User;
import com.militaryasset.module.auth.repository.UserRepository;
import com.militaryasset.security.CustomUserDetails;
import com.militaryasset.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    public JwtResponseDTO login(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));

        Long baseId = null;
        String baseName = null;
        String baseCode = null;

        if (user.getBase() != null) {
            baseId = user.getBase().getId();
            baseName = user.getBase().getName();
            baseCode = user.getBase().getCode();
        }

        return new JwtResponseDTO(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                baseId,
                baseName,
                baseCode
        );
    }
}
