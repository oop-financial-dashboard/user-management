package com.iams.Auth;

import com.iams.Config.JwtService;
import com.iams.Email.EmailService;
import com.iams.Token.*;
import com.iams.User.Role;
import com.iams.User.User;
import com.iams.User.UserRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final EmailService emailService;

    // create user, save details in the database and return the generated token
    public AuthenticationResponse register(RegisterRequest request) {
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new EntityExistsException("This email has already been registered!");
        } else {
            var user = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.PROFESSIONAL)
                    .isActivated(false)
                    .build();
            var savedUser = repository.save(user);
            var jwtToken = jwtService.generateToken(user);
            saveUserToken(savedUser, jwtToken);
            var confirmationToken = saveConfirmationToken(user);
            var email = createMailMessage(request.getEmail(), confirmationToken);
            emailService.sendEmail(email);
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // if authentication fails here, an exception will be thrown
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // if this code runs, it means user has successfully authenticated
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        // revoke any prior valid tokens for this user
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public Boolean confirmAccount(String token) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByConfirmationToken(token);

        if (confirmationToken != null) {
            // user .orElseThrow to catch situations where there is no user with the email & since user is optional
            var user = repository.findByEmail(confirmationToken.getUser().getEmail())
                    .orElseThrow();
            user.setIsActivated(true);
            repository.save(user);
            return user.isEnabled();
        }
        return false;
    }

    public Boolean validateToken(TokenValidationRequest request) {
        return tokenRepository.findByToken(request.getToken())
                .map(token -> !token.isExpired() && !token.isRevoked())
                .orElse(false);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserTokens.isEmpty()) return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
    }

    private ConfirmationToken saveConfirmationToken(User user) {
        ConfirmationToken token = new ConfirmationToken(user);
        confirmationTokenRepository.save(token);
        return token;
    }

    private SimpleMailMessage createMailMessage(String userEmail, ConfirmationToken confirmationToken) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(userEmail);
        mailMessage.setSubject("Complete Registration!");
        mailMessage.setFrom("srikar.primary@gmail.com");
        mailMessage.setText("Hi " + confirmationToken.getUser().getFirstName() + " " + confirmationToken.getUser().getLastName() + "\n"
                +"Welcome to the GS Team! To activate your account, please click the link below. \n\n"
                +"http://localhost:8080/auth/confirm-account?token="+confirmationToken.getConfirmationToken()
                +"\n\nWarm Regards,\nGS Development Team");
        return mailMessage;
    }
}
