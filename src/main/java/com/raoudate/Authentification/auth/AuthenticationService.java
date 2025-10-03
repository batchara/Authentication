package com.raoudate.Authentification.auth;

import com.raoudate.Authentification.email.EmailTemplateName;
import com.raoudate.Authentification.email.EmailsService;
import com.raoudate.Authentification.repository.RoleRepository;
import com.raoudate.Authentification.repository.TokenRepository;
import com.raoudate.Authentification.repository.UserRepository;
import com.raoudate.Authentification.user.Token;
import com.raoudate.Authentification.user.User;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor

public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private  final TokenRepository tokenRepository;
    private final EmailsService emailsService;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    public void register( ResgistrationRequest request) throws MessagingException {
        var userRole = roleRepository.findByName("ROLE_USER")
        //todo - better exception handling
        .orElseThrow(() -> new IllegalStateException("ROLE_USER was not initialized "));

        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();

        userRepository.save(user);

        sendValidationEmail(user);


    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndeSaveActivationToken(user) ;
                //envoin des mails

        emailsService.sendEmail(
                user.getEmail(),
                user.fullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation"

        );


    }

    private String generateAndeSaveActivationToken(User user) {
        //generation de Token
        String generateToken = generateActivationToken(6);
        var token = Token.builder()
                .token(generateToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);

        return generateToken;
    }

    private String generateActivationToken(int length) {
        String Characters = "0123456789";

        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(Characters.length()); //de 0 à 9
            codeBuilder.append(Characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }
}
