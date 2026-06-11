package com.fucar.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CustomerRequest {

    @NotBlank
    private String customerName;

    private String telephone;

    @Email
    @NotBlank
    private String email;

    private LocalDate customerBirthday;

    @NotBlank
    @Size(min = 6)
    private String password;
}

