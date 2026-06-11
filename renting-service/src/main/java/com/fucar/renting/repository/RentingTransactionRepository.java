package com.fucar.renting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.fucar.renting.entity.RentingTransaction;
import java.util.List;

public interface RentingTransactionRepository extends JpaRepository<RentingTransaction, Integer> {
    List<RentingTransaction> findByCustomerId(Integer customerId);
}
