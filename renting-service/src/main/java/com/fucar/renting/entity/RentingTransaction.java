package com.fucar.renting.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "renting_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentingTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rentingTransactionId;
    private LocalDate rentingDate;
    private BigDecimal totalPrice;
    private Integer customerId; // No FK — cross-service reference
    private String rentingStatus; // "PENDING", "ACTIVE", "COMPLETED", "CANCELLED"

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RentingDetail> details;
}