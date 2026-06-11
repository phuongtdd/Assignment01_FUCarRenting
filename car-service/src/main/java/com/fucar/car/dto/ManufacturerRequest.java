package com.fucar.car.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManufacturerRequest {
    @NotBlank(message = "Manufacturer name is required")
    private String manufacturerName;
    
    private String description;
    
    @NotBlank(message = "Country is required")
    private String manufacturerCountry;
}
