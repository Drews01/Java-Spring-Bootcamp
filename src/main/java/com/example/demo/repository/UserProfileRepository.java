package com.example.demo.repository;

import com.example.demo.entity.UserProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

  Optional<UserProfile> findByNik(String nik);

  Optional<UserProfile> findByPhoneNumber(String phoneNumber);
}
