package com.example.backend.service;

import com.example.backend.dto.DashboardStatsDto;
import com.example.backend.dto.projection.RecentRentalProjection;
import com.example.backend.entity.Payment;
import com.example.backend.entity.Staff;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.*;
import com.example.backend.service.dashboard.DashboardService;
import com.example.backend.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private DashboardService dashboardService;

    private Staff staff;

    @BeforeEach
    void setUp() {
        staff = new Staff();
        staff.setStaffId(1);
        staff.setUsername("user");
        staff.setStoreId(1);
    }

    private Payment payment(BigDecimal amount) {
        Payment p = new Payment();
        p.setAmount(amount);
        return p;
    }

    private RecentRentalProjection projection(Integer id, String title) {
        return new RecentRentalProjection() {
            @Override
            public Integer getRentalId() {
                return id;
            }

            @Override
            public String getMovieTitle() {
                return title;
            }

            @Override
            public String getCustomerName() {
                return "Alice Smith";
            }

            @Override
            public LocalDateTime getRentalDate() {
                return LocalDateTime.now();
            }

            @Override
            public Boolean getReturned() {
                return false;
            }
        };
    }

    @Test
    @DisplayName("getDashboardStats — should aggregate counts and revenue")
    void shouldComputeDashboardStats() {
        when(authUtil.getLoggedInUsername()).thenReturn("user");
        when(staffRepository.findByUsername("user")).thenReturn(Optional.of(staff));

        when(customerRepository.countByStore_StoreId(1)).thenReturn(100L);
        when(filmRepository.count()).thenReturn(1000L);
        when(rentalRepository.countByStaff_StoreIdAndReturnDateIsNull(1)).thenReturn(20L);
        when(paymentRepository.sumAmountByStaff_StoreId(1)).thenReturn(7.98);

        DashboardStatsDto dto = dashboardService.getDashboardStats();

        assertThat(dto.getTotalCustomers()).isEqualTo(100L);
        assertThat(dto.getTotalMovies()).isEqualTo(1000L);
        assertThat(dto.getActiveRentals()).isEqualTo(20L);
        assertThat(dto.getTotalRevenue()).isEqualTo(7.98);
    }

    @Test
    @DisplayName("getDashboardStats — should throw when staff not found")
    void shouldThrowWhenStaffMissingForDashboard() {
        when(authUtil.getLoggedInUsername()).thenReturn("unknown");
        when(staffRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.getDashboardStats())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Staff not found");
    }

    @Test
    @DisplayName("getRecentRentals — should return rentals for current store")
    void shouldReturnRecentRentals() {
        when(authUtil.getLoggedInUsername()).thenReturn("user");
        when(staffRepository.findByUsername("user")).thenReturn(Optional.of(staff));

        when(rentalRepository.findTop5ByStaff_StoreIdOrderByRentalDateDesc(1))
                .thenReturn(List.of(projection(1, "ACADEMY DINOSAUR")));

        List<RecentRentalProjection> result = dashboardService.getRecentRentals();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMovieTitle()).isEqualTo("ACADEMY DINOSAUR");
    }
}
