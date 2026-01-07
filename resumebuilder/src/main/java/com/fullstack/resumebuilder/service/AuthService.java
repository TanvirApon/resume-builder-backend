package com.fullstack.resumebuilder.service;

import com.fullstack.resumebuilder.document.User;
import com.fullstack.resumebuilder.dto.AuthResponse;
import com.fullstack.resumebuilder.dto.RegisterRequest;
import com.fullstack.resumebuilder.exception.ResourceExitsException;
import com.fullstack.resumebuilder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

    public AuthResponse register(RegisterRequest request) {
        log.info("Inside Auth Service: register() {}",request);

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceExitsException("Email already exists with this email");
        }

        User newUser = toDocument(request);

        userRepository.save(newUser);

        /*TODO: send verification later*/

        return toResponse(newUser);
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
                    .password(request.getPassword())
                    .profileImageUrl(request.getProfileImageUrl())
                    .subscriptionPlan("basic")
                    .emailVarified(false)
                    .varificationToken(UUID.randomUUID().toString())
                    .varificationExpires(LocalDateTime.now().plusHours(24))
                    .build();
    }


}
