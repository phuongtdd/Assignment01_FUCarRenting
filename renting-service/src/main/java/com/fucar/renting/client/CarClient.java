package com.fucar.renting.client;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.cloud.openfeign.FeignClient;

import com.fucar.renting.dto.CarResponse;

@FeignClient(name = "car-service", url = "${app.service.car}")
public interface CarClient {
    @GetMapping("/internal/cars/{id}")
    CarResponse getCarById(@PathVariable Integer id);

    @PutMapping("/internal/cars/{id}/status")
    void updateCarStatus(@PathVariable Integer id, @RequestParam("status") String status);
}
