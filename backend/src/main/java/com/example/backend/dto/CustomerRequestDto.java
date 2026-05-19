package com.example.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequestDto {

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

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email must be 50 characters or fewer")
    private String email;

    @NotNull(message = "Store ID is required")
    @Positive(message = "Store ID must be positive")
    private Integer storeId;

    // -------- Address fields --------
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
