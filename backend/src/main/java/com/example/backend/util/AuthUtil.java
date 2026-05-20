package com.example.backend.util;

import com.example.backend.entity.Staff;
import com.example.backend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;

@Component
@RequiredArgsConstructor
public class AuthUtil {
    private final StaffRepository staffRepository;

    public String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public Staff getLoggedInStaff() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return staffRepository.findByUsername(username).orElseThrow();
    }
}
