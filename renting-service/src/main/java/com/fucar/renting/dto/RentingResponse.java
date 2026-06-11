package com.fucar.renting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RentingResponse {
    private Integer rentingTransactionId;
    private LocalDate rentingDate;
    private BigDecimal totalPrice;
    private Integer customerId;
    private String rentingStatus;
    private List<RentingDetailResponse> details;
}
