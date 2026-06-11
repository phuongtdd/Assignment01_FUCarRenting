package com.fucar.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticateResponse {
    private Long customerId;
    private String email;
}

