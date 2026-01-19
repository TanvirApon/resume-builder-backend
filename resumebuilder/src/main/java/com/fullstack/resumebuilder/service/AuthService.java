package com.fullstack.resumebuilder.service;

import com.fullstack.resumebuilder.document.User;
import com.fullstack.resumebuilder.dto.AuthResponse;
import com.fullstack.resumebuilder.dto.LoginRequest;
import com.fullstack.resumebuilder.dto.RegisterRequest;
import com.fullstack.resumebuilder.exception.ResourceExitsException;
import com.fullstack.resumebuilder.repository.UserRepository;
import com.fullstack.resumebuilder.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.NameNotFoundException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil  jwtUtil;

    @Value("${app.base.url}")
    private String appBaseUrl;

    public AuthResponse register(RegisterRequest request) {
        log.info("Inside Auth Service: register() {}",request);

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceExitsException("Email already exists with this email");
        }

        User newUser = toDocument(request);

        userRepository.save(newUser);

        sendVerificationEmail(newUser);

        return toResponse(newUser);
    }


    private void sendVerificationEmail(User newUser) {
        log.info("Inside Auth Service: sendVerificationEmail() {}",newUser);

        try{
            String link = appBaseUrl + "/api/auth/verify-email?token="+newUser.getVarificationToken();
            String html ="<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "  <meta charset='UTF-8'>" +
                    "  <title>Email Verification</title>" +
                    "</head>" +
                    "<body style='font-family: Arial, sans-serif;'>" +
                    "  <h2>Hello " + newUser.getName() + ",</h2>" +
                    "  <p>Thank you for registering! Please verify your email address by clicking the link below:</p>" +
                    "  <p style='margin: 20px 0;'>" +
                    "    <a href='" + link + "' " +
                    "       style='background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>" +
                    "       Verify Email" +
                    "    </a>" +
                    "  </p>" +
                    "  <p>If you did not create an account, please ignore this email.</p>" +
                    "  <br>" +
                    "  <p>Best regards,<br/>Resume Builder Team</p>" +
                    "</body>" +
                    "</html>";
            emailService.sendHtmlEmail(newUser.getEmail(),"Verify Email",html);
        }
        catch (Exception e){
            log.error("Email sending failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email");
        }
    }

    public void verifyEmail(String token){
        log.info("Inside Auth Service: verifyEmail() {}",token);
        User user = userRepository.findByVarificationToken(token)
                .orElseThrow(()-> new RuntimeException("Invalid or Expired Verification Token"));

        if(user.getVarificationExpires()!=null && user.getVarificationExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or Expired Verification Token");
        }

        user.setEmailVarified(true);
        user.setVarificationToken(null);
        user.setVarificationExpires(null);
        userRepository.save(user);
    }

    private AuthResponse toResponse(User newUser){
            return AuthResponse.builder()
                    .id(newUser.getId())
                    .name(newUser.getName())
                    .email(newUser.getEmail())
                    .profileImageUrl(newUser.getProfileImageUrl())
                    .emailVarified(newUser.getEmailVarified())
                    .subscription(newUser.getSubscriptionPlan())
                    .createdAt(newUser.getCreatedAt())
                    .updatedAt(newUser.getUpdatedAt())
                    .build();
    }

    private User toDocument(RegisterRequest request){
            return User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .profileImageUrl(request.getProfileImageUrl())
                    .subscriptionPlan("basic")
                    .emailVarified(false)
                    .varificationToken(UUID.randomUUID().toString())
                    .varificationExpires(LocalDateTime.now().plusHours(24))
                    .build();
    }

    public AuthResponse login(LoginRequest request){

       User existingUser =  userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new UsernameNotFoundException("Email not found"));

       if(!passwordEncoder.matches(request.getPassword(),existingUser.getPassword())){
           throw new UsernameNotFoundException("Please verify email before loggin in");
       }

       String token = jwtUtil.generateToken(existingUser.getId());

       AuthResponse response = toResponse(existingUser);
       response.setToken(token);
       return response;
    }

}
