package com.fucar.car.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarResponse {
    private Integer carId;
    private String carName;
    private String carDescription;
    private Integer numberOfDoors;
    private Integer seatingCapacity;
    private String fuelType;
    private Integer year;
    private ManufacturerResponse manufacturer;
    private SupplierResponse supplier;
    private Integer carStatus;
    private BigDecimal carRentingPricePerDay;
}
