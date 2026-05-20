package com.example.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponseDto {

    private String token;

    private String username;

    private String role;

    private Integer staffId;

    private Integer storeId;
}