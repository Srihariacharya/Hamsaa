package com.contactpro.contactpro.repository;

import com.contactpro.contactpro.model.GoogleOAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoogleOAuthTokenRepository extends JpaRepository<GoogleOAuthToken, Long> {

    Optional<GoogleOAuthToken> findByUserId(Long userId);
}
