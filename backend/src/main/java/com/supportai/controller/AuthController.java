package com.supportai.controller;

import com.supportai.dto.AuthRequest;
import com.supportai.dto.AuthResponse;
import com.supportai.dto.RegisterRequest;
import com.supportai.model.Role;
import com.supportai.model.User;
import com.supportai.repository.UserRepository;
import com.supportai.security.JwtUtils;
import com.supportai.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        return ResponseEntity.ok(new AuthResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getFullName(),
                role
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Email is already registered!"));
        }

        Role role = registerRequest.getRole() != null ? registerRequest.getRole() : Role.ROLE_CUSTOMER;

        User user = new User(
                registerRequest.getEmail(),
                encoder.encode(registerRequest.getPassword()),
                registerRequest.getFullName(),
                role
        );

        userRepository.save(user);

        String jwt = jwtUtils.generateTokenFromEmail(user.getEmail(), user.getId(), user.getFullName(), role.name());

        return ResponseEntity.ok(new AuthResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                role.name()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        return ResponseEntity.ok(Map.of(
                "id", userDetails.getId(),
                "email", userDetails.getUsername(),
                "fullName", userDetails.getFullName(),
                "role", role
        ));
    }
}
