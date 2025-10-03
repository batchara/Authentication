package com.raoudate.Authentification.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder

public class RegistrationRequest {

    @NotEmpty(message = "firstname is required")
    @NotBlank(message = "firstname is required")
    private String firstname;

    @NotEmpty(message = "lastname is required")
    @NotBlank(message = "lastname is required")
    private String lastname;

    @NotEmpty(message = "password is required")
    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters long")
    private String password;

    @Email(message = "email is not valid --> raoudate@mail")
    @NotEmpty(message = "email is required")
    @NotBlank(message = "email is required")
    private String email;
}
