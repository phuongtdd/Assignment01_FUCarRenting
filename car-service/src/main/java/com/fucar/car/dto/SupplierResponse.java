package com.fucar.car.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {
    private Integer supplierId;
    private String supplierName;
    private String supplierDescription;
    private String supplierAddress;
}
