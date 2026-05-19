package com.example.backend.repository;

import com.example.backend.entity.Payment;
import com.example.backend.entity.Staff;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;

public class PaymentRepositoryImpl implements PaymentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public BigDecimal sumAmountByStoreId(Integer storeId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);
        Root<Payment> payment = cq.from(Payment.class);
        Join<Payment, Staff> staff = payment.join("staff");

        cq.select(cb.coalesce(cb.sum(payment.get("amount")), BigDecimal.ZERO))
          .where(cb.equal(staff.get("storeId"), storeId));

        return entityManager.createQuery(cq).getSingleResult();
    }
}
