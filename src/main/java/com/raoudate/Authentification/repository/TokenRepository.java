package com.raoudate.Authentification.repository;

import com.raoudate.Authentification.user.Token;
import com.raoudate.Authentification.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.authentication.jaas.JaasAuthenticationCallbackHandler;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    Optional<Token> findByToken(String token);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Token t WHERE t.user = :user")
    void deleteAllByUser(User user);
}
