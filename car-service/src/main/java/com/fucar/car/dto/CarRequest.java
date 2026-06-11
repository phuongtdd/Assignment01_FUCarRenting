package com.fucar.car.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarRequest {
    @NotBlank(message = "Car name must not be blank")
    private String carName;
    
    private String carDescription;
    
    @NotNull(message = "Number of doors is required")
    private Integer numberOfDoors;
    
    @NotNull(message = "Seating capacity is required")
    private Integer seatingCapacity;
    
    @NotBlank(message = "Fuel type must not be blank")
    private String fuelType;
    
    @NotNull(message = "Year is required")
    private Integer year;
    
    @NotNull(message = "Manufacturer ID is required")
    private Integer manufacturerId;
    
    @NotNull(message = "Supplier ID is required")
    private Integer supplierId;
    
    @NotNull(message = "Car status is required")
    private Integer carStatus;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal carRentingPricePerDay;
}
