package com.fucar.renting.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.cloud.openfeign.FeignClient;
import com.fucar.renting.dto.CustomerResponse;

@FeignClient(name = "customer-service", url = "${app.service.customer}")
public interface CustomerClient {
    @GetMapping("/internal/customers/{id}")
    CustomerResponse getCustomerById(@PathVariable Integer id);
}