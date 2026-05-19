package com.example.backend.repository;

import com.example.backend.dto.projection.CustomerProjection;
import com.example.backend.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    @EntityGraph(attributePaths = {"store", "address.city.country"})
    Page<Customer> findByStore_StoreIdAndFirstNameContainingIgnoreCase(
        Integer storeId,
        String firstName,
        Pageable pageable
);

    @EntityGraph(attributePaths = {"store", "address.city.country"})
    Page<Customer> findByStore_StoreIdAndLastNameContainingIgnoreCase(
            Integer storeId,
            String lastName,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"store", "address.city.country"})
    Page<CustomerProjection>
    findProjectedByStore_StoreIdAndFirstNameContainingIgnoreCaseOrStore_StoreIdAndLastNameContainingIgnoreCase(
            Integer storeId1, String firstName,
            Integer storeId2, String lastName,
            Pageable pageable);

    @EntityGraph(attributePaths = {"store", "address.city.country"})
    Page<CustomerProjection>
    findProjectedByStore_StoreIdAndFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
            Integer storeId, String firstName, String lastName,
            Pageable pageable);

    @EntityGraph(attributePaths = {"store", "address.city.country"})
    Page<CustomerProjection> findProjectedByStore_StoreId(Integer storeId, Pageable pageable);


    @EntityGraph(attributePaths = {"store", "address.city.country"})
    Customer findTopByOrderByCustomerIdDesc();

    @EntityGraph(attributePaths = {"store", "address.city.country"})
    Optional<CustomerProjection> findProjectedByCustomerId(Integer customerId);


    @EntityGraph(attributePaths = {"store", "address.city.country"})
    Page<Customer> findByStore_StoreId(Integer storeId, Pageable pageable);


    @EntityGraph(attributePaths = {"store", "address.city.country"})
    Long countByStore_StoreId(Integer storeId);

}