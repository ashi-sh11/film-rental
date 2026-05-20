package com.example.backend.service.dashboard;

import com.example.backend.dto.DashboardStatsDto;
import com.example.backend.dto.cache.CacheDtos.RecentRentalDto;
import com.example.backend.dto.projection.RecentRentalProjection;
import com.example.backend.entity.Staff;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.*;
import com.example.backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final FilmRepository filmRepository;
    private final RentalRepository rentalRepository;
    private final PaymentRepository paymentRepository;
    private final StaffRepository staffRepository;
    private final AuthUtil authUtil;

    @Cacheable(value = "dashboardStats", key = "@authUtil.getLoggedInUsername()")
    public DashboardStatsDto getDashboardStats() {
        Integer storeId = getStoreId();

        Long totalCustomers = customerRepository.countByStore_StoreId(storeId);
        Long totalMovies = filmRepository.count();
        Long activeRentals = rentalRepository.countByStaff_StoreIdAndReturnDateIsNull(storeId);
        Double totalRevenue = paymentRepository.sumAmountByStoreId(storeId).doubleValue();

        return DashboardStatsDto.builder()
                .totalCustomers(totalCustomers)
                .totalMovies(totalMovies)
                .activeRentals(activeRentals)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Cacheable(value = "recentRentals", key = "@authUtil.getLoggedInUsername()")
    public List<RecentRentalProjection> getRecentRentals() {
        return rentalRepository.findTop5ByStaff_StoreIdOrderByRentalDateDesc(getStoreId()).stream()
                .<RecentRentalProjection>map(RecentRentalDto::from)
                .toList();
    }

    private Integer getStoreId() {
        String username = authUtil.getLoggedInUsername();
        Staff staff = staffRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        return staff.getStoreId();
    }
}
