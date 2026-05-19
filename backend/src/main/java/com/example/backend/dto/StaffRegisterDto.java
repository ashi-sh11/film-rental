package com.example.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffRegisterDto {

    @NotBlank(message = "First name is required")
    @Size(max = 45, message = "First name must be 45 characters or fewer")
    @Pattern(regexp = "^[A-Za-z][A-Za-z '\\-]*$",
            message = "First name may only contain letters, spaces, apostrophes and hyphens")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 45, message = "Last name must be 45 characters or fewer")
    @Pattern(regexp = "^[A-Za-z][A-Za-z '\\-]*$",
            message = "Last name may only contain letters, spaces, apostrophes and hyphens")
    private String lastName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 16, message = "Username must be 3-16 characters")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Username may only contain letters, digits and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email must be 50 characters or fewer")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 16, message = "Password must be 8-16 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,16}$",
            message = "Password must be 8-16 characters and include an uppercase letter, a lowercase letter, a digit and a special character (@$!%*?&#)"
    )
    private String password;

    @NotNull(message = "Store ID is required")
    @Positive(message = "Store ID must be positive")
    private Integer storeId;

    @NotBlank(message = "Address line is required")
    @Size(max = 50, message = "Address must be 50 characters or fewer")
    private String address;

    @Size(max = 50, message = "Address line 2 must be 50 characters or fewer")
    private String address2;

    @NotBlank(message = "District is required")
    @Size(max = 20, message = "District must be 20 characters or fewer")
    private String district;

    @NotNull(message = "City is required")
    @Positive(message = "City ID must be positive")
    private Integer cityId;

    @Pattern(regexp = "^$|^[A-Za-z0-9 \\-]{3,10}$",
            message = "Postal code must be 3-10 letters, digits, spaces or hyphens")
    private String postalCode;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone must be exactly 10 digits")
    private String phone;
}