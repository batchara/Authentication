package com.raoudate.Authentification.auth;

import com.raoudate.Authentification.email.EmailTemplateName;
import com.raoudate.Authentification.email.EmailsService;
import com.raoudate.Authentification.repository.RoleRepository;
import com.raoudate.Authentification.repository.TokenRepository;
import com.raoudate.Authentification.repository.UserRepository;
import com.raoudate.Authentification.security.JwtService;
import com.raoudate.Authentification.user.Token;
import com.raoudate.Authentification.user.User;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
@Service
@RequiredArgsConstructor

public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private  final TokenRepository tokenRepository;
    private final EmailsService emailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    public void register( RegistrationRequest request) throws MessagingException {
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

    public AuthenticationResponse authenticate( AuthenticationRequest request) {

        var auth= authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var claims = new HashMap<String, Object>();
        var user = ((User) auth.getPrincipal());
        claims.put("fulName", user.getEmail());
        var jwtToken = jwtService.generateToken(claims, user);
        return AuthenticationResponse.builder()
                .token(jwtToken).build();
    }

    @Transactional
    public void activateAcount(String token) throws MessagingException {
        Token saveToken = tokenRepository.findByToken(token)
                //todo exception has to be defined

                .orElseThrow(() -> new RuntimeException("Invalid Token"));
        if(LocalDateTime.now().isAfter(saveToken.getExpiresAt())){
            sendValidationEmail(saveToken.getUser());
            throw new RuntimeException("Token expired . A new token has been sent to the same email");

        }

        var user = userRepository.findById(saveToken.getUser().getId())
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
        saveToken.setValidateAt(LocalDateTime.now());
        tokenRepository.save(saveToken);

    }


}
