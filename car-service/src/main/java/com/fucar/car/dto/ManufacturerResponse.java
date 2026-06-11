package com.fucar.car.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManufacturerResponse {
    private Integer manufacturerId;
    private String manufacturerName;
    private String description;
    private String manufacturerCountry;
}
