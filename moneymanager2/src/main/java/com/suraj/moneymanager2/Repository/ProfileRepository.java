package com.suraj.moneymanager2.Repository;

import com.suraj.moneymanager2.Entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<ProfileEntity , Long> {
    //select * from tbl_profiles where email = ?
    Optional<ProfileEntity> findByEmail(String email);


    Optional<ProfileEntity> findByActivationToken(String activationToken);


}
