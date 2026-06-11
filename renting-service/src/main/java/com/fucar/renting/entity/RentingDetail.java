package com.fucar.renting.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "renting_details")
@IdClass(RentingDetailId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentingDetail {
    @Id
    private Integer rentingTransactionId;
    @Id
    private Integer carId;

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("rentingTransactionId")
    @JoinColumn(name = "renting_transaction_id")
    private RentingTransaction transaction;
}
