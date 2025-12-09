package com.suraj.moneymanager2.Controller;

import com.suraj.moneymanager2.DTO.AuthDTO;
import com.suraj.moneymanager2.DTO.ProfileDTO;
import com.suraj.moneymanager2.Service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.config.RepositoryNameSpaceHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;


    // File: com.suraj.moneymanager2.Controller.ProfileController.java

    @PostMapping("/register")
    public ResponseEntity<?> registerProfile(@RequestBody ProfileDTO profileDTO) {
        try {
            ProfileDTO registeredProfile = profileService.registerProfile(profileDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
        } catch (RuntimeException e) {
            // Handle the error: 409 Conflict if email exists, 500 for others
            if (e.getMessage().contains("Email already registered")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "message", e.getMessage()
                ));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Registration failed: " + e.getMessage()
            ));
        }
    }


    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestParam String token) {
        boolean isActivated = profileService.activateProfile(token);
        if (isActivated) {
            return ResponseEntity.ok("Profile activated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activation token not found or already used");
        }
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthDTO authDTO) {
        try {
            //You can remove this redundant check, as the AuthenticationManager handles it via DisabledException
            if (!profileService.isAccountActive(authDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "message", "Account is not active. Please activate your account first."
                ));
            }

            Map<String, Object> response = profileService.authenticateAndGenerateToken(authDTO);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            // **If you see this error, the password hash comparison failed.**
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Invalid email or password (Bad Credentials)" // Use this specific message for now
            ));
        } catch (DisabledException e) {
            // **If you see this error, the account's 'isActive' status is false.**
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "Account is not active. Please activate your account first."
            ));
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "An unexpected error occurred during login."
            ));
        }
    }





}
