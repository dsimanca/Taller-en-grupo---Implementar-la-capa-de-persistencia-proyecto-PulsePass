package com.pulsepass.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record RegisterUserRequest(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        @NotBlank(message = "Phone is required") String phone,
        @NotBlank(message = "City is required") String city,
        @NotNull(message = "Birth date is required") @Past(message = "Birth date must be in the past") LocalDate birthDate
) {}
