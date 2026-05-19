package com.example.backend.repository;

import java.math.BigDecimal;

public interface PaymentRepositoryCustom {

    BigDecimal sumAmountByStoreId(Integer storeId);
}
