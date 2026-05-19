package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class NewActorDto {

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
}
