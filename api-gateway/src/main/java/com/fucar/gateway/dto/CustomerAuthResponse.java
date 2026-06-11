package com.fucar.gateway.dto;

import lombok.Data;

/**
 * Response from customer-service /internal/customers/authenticate endpoint.
 */
@Data
public class CustomerAuthResponse {
    private Long customerId;
    private String email;
}
