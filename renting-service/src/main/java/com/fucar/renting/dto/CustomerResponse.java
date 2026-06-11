package com.fucar.renting.dto;

import lombok.Data;

@Data
public class CustomerResponse {
    private Integer customerId;
    private String customerName;
    private String email;
    private Integer customerStatus;
}
