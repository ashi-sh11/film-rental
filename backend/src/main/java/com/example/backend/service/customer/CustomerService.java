package com.example.backend.service.customer;


import com.example.backend.dto.CustomerRequestDto;
import com.example.backend.dto.cache.CacheDtos.CustomerDto;
import com.example.backend.dto.projection.CustomerProjection;
import com.example.backend.entity.*;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.AddressRepository;
import com.example.backend.repository.CityRepository;
import com.example.backend.repository.CustomerRepository;
import com.example.backend.repository.StaffRepository;
import com.example.backend.util.AuthUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final StaffRepository staffRepository;
    private final AuthUtil authUtil;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final CityRepository cityRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Caching(evict = {
            @CacheEvict(value = "dashboardStats",   allEntries = true),
            @CacheEvict(value = "customers",        allEntries = true),
            @CacheEvict(value = "customerSearch",   allEntries = true)
    })
    @Transactional
    public String addCustomer(CustomerRequestDto dto) {
        City city = cityRepository.findById(dto.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));

        Integer nextAddressId = addressRepository
                .findTopByOrderByAddressIdDesc()
                .map(a -> a.getAddressId() + 1)
                .orElse(1);

        entityManager.createNativeQuery(
                        "INSERT INTO address " +
                                "(address_id, address, address2, district, city_id, postal_code, phone, location, last_update) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ST_GeomFromText('POINT(0 0)'), ?)")
                .setParameter(1, nextAddressId)
                .setParameter(2, dto.getAddress())
                .setParameter(3, dto.getAddress2())
                .setParameter(4, dto.getDistrict())
                .setParameter(5, city.getCityId())
                .setParameter(6, dto.getPostalCode())
                .setParameter(7, dto.getPhone())
                .setParameter(8, LocalDateTime.now())
                .executeUpdate();


        Customer customer = new Customer();
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        Store store = new Store();
        store.setStoreId(dto.getStoreId());
        customer.setStore(store);
        Address address = new Address();
        address.setAddressId(nextAddressId);
        customer.setAddress(address);
        customer.setActive(true);
        customer.setCreateDate(LocalDateTime.now());
        customer.setLastUpdate(LocalDateTime.now());

        customerRepository.save(customer);

        return String.valueOf(customer.getCustomerId());
    }

    @Cacheable(value = "customers",
            key = "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + @authUtil.getLoggedInUsername()")
    public Page<CustomerProjection> getAllCustomers(Pageable pageable) {
        Integer storeId = currentStoreId();
        return customerRepository.findProjectedByStore_StoreId(storeId, pageable)
                .map(p -> (CustomerProjection) CustomerDto.from(p));
    }

    @Cacheable(value = "customerSearch",
            key = "#name + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + @authUtil.getLoggedInUsername()")
    public Page<CustomerProjection> searchCustomers(String name, Pageable pageable) {
        Integer storeId = currentStoreId();
        String trimmed = name.trim();
        String[] parts = trimmed.split("\\s+");

        Page<CustomerProjection> raw = parts.length >= 2
                ? customerRepository
                        .findProjectedByStore_StoreIdAndFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
                                storeId, parts[0], parts[parts.length - 1], pageable)
                : customerRepository
                        .findProjectedByStore_StoreIdAndFirstNameContainingIgnoreCaseOrStore_StoreIdAndLastNameContainingIgnoreCase(
                                storeId, trimmed, storeId, trimmed, pageable);
        return raw.map(p -> (CustomerProjection) CustomerDto.from(p));
    }

    @Cacheable(value = "customerById", key = "#id")
    public CustomerProjection getCustomerById(Integer id) {
        return customerRepository.findProjectedByCustomerId(id)
                .map(CustomerDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private Integer currentStoreId() {
        String username = authUtil.getLoggedInUsername();
        Staff staff = staffRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        return staff.getStoreId();
    }
}
