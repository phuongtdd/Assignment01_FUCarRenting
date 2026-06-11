package com.fucar.renting.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class CarResponse {
    private Integer carId;
    private String carName;
    private Integer carStatus;
    private BigDecimal carRentingPricePerDay;
}
