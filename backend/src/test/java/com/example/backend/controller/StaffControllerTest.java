package com.example.backend.controller;

import com.example.backend.controller.staff.StaffController;
import com.example.backend.dto.StaffRegisterDto;
import com.example.backend.dto.projection.StaffDetailProjection;
import com.example.backend.dto.projection.StaffProjection;
import com.example.backend.exception.ConflictException;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.security.CustomStaffDetailsService;
import com.example.backend.security.JwtService;
import com.example.backend.service.staff.StaffService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StaffController.class, excludeAutoConfiguration = {
        org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration.class,
        org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class StaffControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper json;

    @MockitoBean private JwtService jwtService;
    @MockitoBean private CustomStaffDetailsService userDetailsService;
    @MockitoBean private StaffService staffService;

    @Value("${test.staff.password}")
    private String testPassword;

    @Value("${test.staff.username}")
    private String testUsername;

    private StaffProjection staffP(int id, String name) {
        return new StaffProjection() {
            public Integer getStaffId() { return id; }
            public String getFullName() { return name; }
            public String getEmail() { return "x@y.com"; }
            public Boolean getActive() { return true; }
        };
    }

    private StaffDetailProjection detail() {
        return new StaffDetailProjection() {
            public Integer getStaffId() { return 1; }
            public String getFullName() { return "Mike Hillyer"; }
            public String getUsername() { return "Mike"; }
            public String getEmail() { return "Mike.Hillyer@x.com"; }
            public Integer getStoreId() { return 1; }
            public Boolean getActive() { return true; }
            public String getAddress() { return "23 Workhaven Lane"; }
            public String getAddress2() { return null; }
            public String getDistrict() { return "Alberta"; }
            public String getCity() { return "Lethbridge"; }
            public String getCountry() { return "Canada"; }
            public String getPostalCode() { return "14400"; }
            public String getPhone() { return "+1 234"; }
        };
    }

    private StaffRegisterDto validRequest() {
        StaffRegisterDto r = new StaffRegisterDto();
        r.setFirstName("Alice");
        r.setLastName("Doe");
        r.setUsername(testUsername);
        r.setEmail("alice@example.com");
        r.setPassword(testPassword);
        r.setStoreId(1);
        r.setAddress("123 Main St");
        r.setDistrict("D1");
        r.setCityId(1);
        r.setPhone("5551230000");
        return r;
    }

    @Test
    @DisplayName("GET /api/staff — list projections")
    void shouldListStaff() throws Exception {
        when(staffService.getMyStoreStaff(any()))
                .thenReturn(new PageImpl<>(List.of(staffP(1, "Mike Hillyer"))));

        mockMvc.perform(get("/api/staff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].staffId").value(1))
                .andExpect(jsonPath("$.content[0].fullName").value("Mike Hillyer"));
    }

    @Test
    @DisplayName("GET /api/staff/search?name=… delegates to searchMyStoreStaff")
    void shouldSearchStaff() throws Exception {
        when(staffService.searchMyStoreStaff(any(), any()))
                .thenReturn(new PageImpl<>(List.of(staffP(1, "Mike Hillyer"))));

        mockMvc.perform(get("/api/staff/search?name=mike"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Mike Hillyer"));
    }

    @Test
    @DisplayName("GET /api/staff/{id} — detail projection with flattened address")
    void shouldReturnDetailWithAddress() throws Exception {
        when(staffService.getStaffById(1)).thenReturn(detail());

        mockMvc.perform(get("/api/staff/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Mike Hillyer"))
                .andExpect(jsonPath("$.username").value("Mike"))
                .andExpect(jsonPath("$.city").value("Lethbridge"))
                .andExpect(jsonPath("$.country").value("Canada"));
    }

    @Test
    @DisplayName("GET /api/staff/{id} — unknown id → 404")
    void shouldReturn404ForUnknownStaff() throws Exception {
        when(staffService.getStaffById(9999))
                .thenThrow(new ResourceNotFoundException("Staff not found"));

        mockMvc.perform(get("/api/staff/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Staff not found"));
    }

    @Test
    @DisplayName("POST /api/staff — valid body returns service-supplied message")
    void shouldCreateStaff() throws Exception {
        when(staffService.createStaff(any())).thenReturn("Staff created successfully");

        mockMvc.perform(post("/api/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(content().string("Staff created successfully"));
    }

    @Test
    @DisplayName("POST /api/staff — duplicate username → 409 via ConflictException")
    void shouldReturn409OnDuplicate() throws Exception {
        when(staffService.createStaff(any()))
                .thenThrow(new ConflictException("Username 'alice_d' is already taken"));

        mockMvc.perform(post("/api/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("already taken")));
    }

    @Test
    @DisplayName("POST /api/staff — invalid username pattern → 400 with field error")
    void shouldRejectBadUsername() throws Exception {
        StaffRegisterDto r = validRequest();
        r.setUsername("has spaces!");

        mockMvc.perform(post("/api/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(r)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username").exists());
    }

    @Test
    @DisplayName("POST /api/staff — missing password → 400")
    void shouldRejectMissingPassword() throws Exception {
        StaffRegisterDto r = validRequest();
        r.setPassword(null);

        mockMvc.perform(post("/api/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(r)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }
}