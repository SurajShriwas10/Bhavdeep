package com.suraj.moneymanager2.Service;
import com.suraj.moneymanager2.DTO.AuthDTO;
import com.suraj.moneymanager2.DTO.ProfileDTO;
import com.suraj.moneymanager2.Entity.ProfileEntity;
import com.suraj.moneymanager2.Repository.ProfileRepository;
import com.suraj.moneymanager2.Util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;


    public ProfileDTO registerProfile(ProfileDTO profileDTO){

        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);
        //send activaation email

        String activationLink = " http://localhost:8080/api/v1.0/activate?token=" + newProfile.getActivationToken();

        String subject = "Activate your Money Manager account";
        String body = "Click on the following link to activate your account ->>> "+activationLink;
        emailService.sendEmail(newProfile.getEmail(), subject, body);

        return toDTO(newProfile);
    }


    public ProfileEntity toEntity(ProfileDTO profileDTO){
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }


    public ProfileDTO toDTO(ProfileEntity profileEntity){
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();

    }

    public boolean activateProfile(String activationtoken){
        return profileRepository.findByActivationToken(activationtoken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);

    }

    public Boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive).orElse(false);
    }

    public ProfileEntity getCurrentProfile(){
     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           return
             profileRepository.findByEmail(authentication.getName())
               .orElseThrow(()->new UsernameNotFoundException("Profile not found with email:"+authentication.getName()));
    }

    public ProfileDTO getpublicProfile(String email){
        ProfileEntity currentUser = null;
        if(email == null){
           currentUser = getCurrentProfile();
        }else{
           currentUser =  profileRepository.findByEmail(email)
                    .orElseThrow(()->new UsernameNotFoundException("profile not found with email"+email));
        }
        return ProfileDTO.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();

    }

    // In ProfileService.java

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword()));

            // Generate JWT token
            String token = jwtUtil.generateToken(authDTO.getEmail());

            return Map.of(
                    "token", token,
                    "user", getpublicProfile(authDTO.getEmail())
            );
        } catch (Exception e) {
            // **<<-- CRITICAL: This is the only way to see the error in the console -->>**
            e.printStackTrace();

            // Re-throw so the controller catches it and sends the generic message
            throw new RuntimeException("Login failed internally.");
        }
    }
}
