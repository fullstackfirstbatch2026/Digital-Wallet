package com.digitalwallet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "Name cannot be empty") @Size(max = 100, message = "Name must be at most 100 characters") String name,
        @NotBlank(message = "Email cannot be empty") @Email(message = "Email must be valid") @Size(max = 150) String email,
        @Size(max = 20, message = "Phone must be at most 20 characters") String phone) {
}
