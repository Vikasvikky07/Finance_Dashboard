package com.Zorvyn.Finance_Dashboard.service;

import com.Zorvyn.Finance_Dashboard.dto.AuthRequest;
import com.Zorvyn.Finance_Dashboard.dto.AuthResponse;
import com.Zorvyn.Finance_Dashboard.dto.UserResponse;
import com.Zorvyn.Finance_Dashboard.exception.ResourceNotFoundException;
import com.Zorvyn.Finance_Dashboard.model.AppUser;
import com.Zorvyn.Finance_Dashboard.security.JwtProperties;
import com.Zorvyn.Finance_Dashboard.security.JwtService;
import java.util.Set;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final LoginRateLimiter loginRateLimiter;
    private final UserService userService;

    public AuthResponse login(AuthRequest request) {
        loginRateLimiter.validate(request.username());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password())
            );
            loginRateLimiter.recordSuccess(request.username());
        } catch (AuthenticationException ex) {
            loginRateLimiter.recordFailure(request.username());
            throw ex;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AppUser user = userService.getUserEntityByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<String> roles = user.getRoles().stream().map(role -> role.getName()).collect(java.util.stream.Collectors.toSet());
        String token = jwtService.generateToken(userDetails, Map.of("roles", roles));

        return new AuthResponse(
                token,
                "Bearer",
                jwtProperties.expirationSeconds(),
                new UserResponse(user.getId(), user.getName(), user.getUsername(), user.getEmail(), roles, user.getStatus())
        );
    }
}
