package com.fucar.renting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RentingDetailResponse {
    private Integer carId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal price;
}
