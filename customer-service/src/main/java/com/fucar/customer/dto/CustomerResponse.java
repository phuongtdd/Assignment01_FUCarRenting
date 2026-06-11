package com.fucar.customer.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerResponse {
    private Integer customerId;
    private String customerName;
    private String telephone;
    private String email;
    private LocalDate customerBirthday;
    private Integer customerStatus;
}

