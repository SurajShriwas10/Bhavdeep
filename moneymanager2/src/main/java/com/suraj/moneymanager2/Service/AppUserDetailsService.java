package com.suraj.moneymanager2.Service;

import com.suraj.moneymanager2.Entity.ProfileEntity;
import com.suraj.moneymanager2.Repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    private final ProfileRepository profileRepository;

    // Inside your AppUserDetailsService.java

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ProfileEntity profile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // **THIS IS THE CRITICAL PART:**
        // The User constructor maps its fourth argument (boolean enabled) to the isActive field.
        return new org.springframework.security.core.userdetails.User(
                profile.getEmail(),
                profile.getPassword(), // The stored, encoded password
                profile.getIsActive(), // **Use the isActive status here!**
                true, // account non expired
                true, // credentials non expired
                true, // account non locked
                List.of() // user authorities/roles
        );
    }
}
