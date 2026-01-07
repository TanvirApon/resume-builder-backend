package com.fullstack.resumebuilder.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is Required")
    @Size(min = 2, max = 15, message="Name must be between 2 and 50 characters")
    private String name;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is Required")
    @Size(min = 8, max = 16, message="Password must be between 2 and 50 characters")
    private String password;

    private String profileImageUrl;
}
