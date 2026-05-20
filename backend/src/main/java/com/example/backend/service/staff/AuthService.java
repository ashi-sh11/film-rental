package com.example.backend.service.staff;

import com.example.backend.dto.*;
import com.example.backend.entity.Staff;
import com.example.backend.repository.StaffRepository;
import com.example.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtService.generateToken(request.getUsername(), "ROLE_STAFF");

        Staff staff = staffRepository.findByUsername(request.getUsername()).orElse(null);

        return LoginResponseDto.builder()
                .token(token)
                .username(request.getUsername())
                .role("ROLE_STAFF")
                .staffId(staff != null ? staff.getStaffId() : null)
                .storeId(staff != null ? staff.getStoreId() : null)
                .build();
    }
}
