package com.example.backend.repository;

import com.example.backend.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer>, PaymentRepositoryCustom {

    @EntityGraph(attributePaths = {"customer", "staff", "rental"})
    Optional<Payment> findTopByOrderByPaymentIdDesc();

    @EntityGraph(attributePaths = {"customer", "staff", "rental"})
    Page<Payment> findByStaff_StoreId(Integer storeId, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "staff", "rental"})
    List<Payment> findByStaff_StoreId(
            Integer storeId
    );
}