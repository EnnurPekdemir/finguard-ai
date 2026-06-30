package com.sentinelbank.finguard.controller;

import com.sentinelbank.finguard.dto.AuthRequest;
import com.sentinelbank.finguard.dto.AuthResponse;
import com.sentinelbank.finguard.dto.RegisterRequest;
import com.sentinelbank.finguard.model.User;
import com.sentinelbank.finguard.repository.UserRepository;
import com.sentinelbank.finguard.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller handling user registration and login/authentication.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Registers a new user.
     *
     * @param registerRequest DTO containing registration credentials
     * @return Response containing the operation status
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Username is already taken!"));
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Email address is already in use!"));
        }

        String role = registerRequest.getRole() != null ? registerRequest.getRole().toUpperCase() : "USER";
        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            role = "USER";
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .role(role)
                .build();

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully."));
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param authRequest DTO containing credentials
     * @return Response containing JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);

            User user = (User) authentication.getPrincipal();

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwt)
                    .username(user.getUsername())
                    .role(user.getRole())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password!"));
        }
    }
}
