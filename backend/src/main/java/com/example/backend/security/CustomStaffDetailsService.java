package com.example.backend.security;


import com.example.backend.entity.Staff;
import com.example.backend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomStaffDetailsService
        implements UserDetailsService {

    private final StaffRepository staffRepository;

    @Override
    public UserDetails loadUserByUsername(
            String username
    ) throws UsernameNotFoundException {

        // STAFF LOGIN
        Staff staff = staffRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Staff not found"
                        ));

        // MySQL's default collation (utf8mb4_0900_ai_ci) is case-insensitive,
        // so findByUsername("Alice_D") would match a row "alice_d". We want
        // strict, case-sensitive login — verify the stored username matches
        // exactly what was typed.
        if (!staff.getUsername().equals(username)) {
            throw new UsernameNotFoundException("Staff not found");
        }

        return new User(

                staff.getUsername(),

                staff.getPassword(),

                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_STAFF"
                        )
                )
        );
    }
}