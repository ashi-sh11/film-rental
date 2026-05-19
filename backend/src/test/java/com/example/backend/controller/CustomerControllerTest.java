package com.example.backend.controller;



import com.example.backend.controller.customer.CustomerController;
import com.example.backend.dto.CustomerRequestDto;
import com.example.backend.dto.projection.CustomerProjection;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.security.CustomStaffDetailsService;
import com.example.backend.security.JwtService;
import com.example.backend.service.customer.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CustomerController.class, excludeAutoConfiguration = {
        org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration.class,
        org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired
    private ObjectMapper json;

    @MockitoBean private JwtService jwtService;
    @MockitoBean private CustomStaffDetailsService userDetailsService;
    @MockitoBean private CustomerService customerService;

    private CustomerProjection customer(int id, String name, String email) {
        return new CustomerProjection() {
            public Integer getCustomerId() { return id; }
            public String getFullName() { return name; }
            public String getEmail() { return email; }
            public Integer getStoreId() { return 1; }
            public Boolean getActive() { return true; }
        };
    }

    private CustomerRequestDto validRequest() {
        CustomerRequestDto r = new CustomerRequestDto();
        r.setFirstName("Alice");
        r.setLastName("Smith");
        r.setEmail("alice@example.com");
        r.setStoreId(1);
        r.setAddress("123 Main St");
        r.setDistrict("Some District");
        r.setCityId(1);
        r.setPhone("5551230000");
        return r;
    }

    @Test
    @DisplayName("GET /api/customers — list returns paged projection")
    void shouldListCustomers() throws Exception {
        when(customerService.getAllCustomers(any()))
                .thenReturn(new PageImpl<>(List.of(customer(1, "MARY SMITH", "mary@x.com"))));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerId").value(1))
                .andExpect(jsonPath("$.content[0].fullName").value("MARY SMITH"))
                .andExpect(jsonPath("$.content[0].active").value(true));
    }

    @Test
    @DisplayName("GET /api/customers/search?name=… delegates to searchCustomers")
    void shouldSearchCustomers() throws Exception {
        when(customerService.searchCustomers(eq("mary"), any()))
                .thenReturn(new PageImpl<>(List.of(customer(1, "MARY SMITH", "mary@x.com"))));

        mockMvc.perform(get("/api/customers/search?name=mary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("MARY SMITH"));
    }

    @Test
    @DisplayName("GET /api/customers/{id} — single projection")
    void shouldReturnSingleCustomer() throws Exception {
        when(customerService.getCustomerById(1)).thenReturn(customer(1, "MARY SMITH", "mary@x.com"));

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.email").value("mary@x.com"));
    }

    @Test
    @DisplayName("GET /api/customers/{id} — unknown id → 404")
    void shouldReturn404ForUnknownCustomer() throws Exception {
        when(customerService.getCustomerById(9999))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/customers/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    @Test
    @DisplayName("POST /api/customers — valid body → 200 + service called")
    void shouldAddCustomer() throws Exception {
        when(customerService.addCustomer(any())).thenReturn("42");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    @Test
    @DisplayName("POST /api/customers — empty body → 400 with multiple field errors")
    void shouldRejectEmptyBody() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.storeId").exists())
                .andExpect(jsonPath("$.fieldErrors.phone").exists());
    }

    @Test
    @DisplayName("POST /api/customers — invalid email → 400")
    void shouldRejectBadEmail() throws Exception {
        CustomerRequestDto r = validRequest();
        r.setEmail("not-an-email");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(r)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").value("Invalid email format"));
    }

    @Test
    @DisplayName("POST /api/customers — non-positive storeId → 400")
    void shouldRejectNonPositiveStoreId() throws Exception {
        CustomerRequestDto r = validRequest();
        r.setStoreId(0);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(r)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.storeId").exists());
    }

    @Test
    @DisplayName("GET /api/customers — page and size params are forwarded")
    void shouldForwardPaginationParams() throws Exception {
        when(customerService.getAllCustomers(PageRequest.of(2, 10)))
                .thenReturn(new PageImpl<>(List.of(customer(1, "MARY SMITH", "mary@x.com"))));

        mockMvc.perform(get("/api/customers?page=2&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerId").value(1));
    }

    @Test
    @DisplayName("GET /api/customers/search — missing name param → 400")
    void shouldReturn400WhenSearchNameMissing() throws Exception {
        mockMvc.perform(get("/api/customers/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/customers/search — empty result returns empty page")
    void shouldReturnEmptyPageWhenNoSearchResults() throws Exception {
        when(customerService.searchCustomers(eq("zzz"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/customers/search?name=zzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("POST /api/customers — invalid phone → 400")
    void shouldRejectBadPhone() throws Exception {
        CustomerRequestDto r = validRequest();
        r.setPhone(null);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(r)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.phone").exists());
    }

    @Test
    @DisplayName("POST /api/customers — missing firstName → 400")
    void shouldRejectMissingFirstName() throws Exception {
        CustomerRequestDto r = validRequest();
        r.setFirstName(null);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(r)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.firstName").exists());
    }

    @Test
    @DisplayName("POST /api/customers — missing lastName → 400")
    void shouldRejectMissingLastName() throws Exception {
        CustomerRequestDto r = validRequest();
        r.setLastName(null);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(r)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.lastName").exists());
    }

    @Test
    @DisplayName("GET /api/customers — default pagination returns page 0 size 5")
    void shouldUseDefaultPagination() throws Exception {
        when(customerService.getAllCustomers(PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(customer(1, "MARY SMITH", "mary@x.com"))));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerId").value(1));
    }
}
