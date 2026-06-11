package com.fucar.car.controller;

import com.fucar.car.dto.*;
import com.fucar.car.service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    // --- PUBLIC/AUTHENTICATED CAR ENDPOINTS (FORWARDED BY GATEWAY) ---

    @GetMapping("/api/cars")
    public ResponseEntity<List<CarResponse>> getAllCars() {
        return ResponseEntity.ok(carService.getAll());
    }

    @GetMapping("/api/cars/{id}")
    public ResponseEntity<CarResponse> getCarById(@PathVariable Integer id) {
        return ResponseEntity.ok(carService.getById(id));
    }

    @PostMapping("/api/cars")
    public ResponseEntity<CarResponse> createCar(
            @Valid @RequestBody CarRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(carService.create(request));
    }

    @PutMapping("/api/cars/{id}")
    public ResponseEntity<CarResponse> updateCar(
            @PathVariable Integer id,
            @Valid @RequestBody CarRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(carService.update(id, request));
    }

    @DeleteMapping("/api/cars/{id}")
    public ResponseEntity<Void> deleteCar(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        carService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- MANUFACTURER ENDPOINTS ---

    @GetMapping("/api/manufacturers")
    public ResponseEntity<List<ManufacturerResponse>> getAllManufacturers() {
        return ResponseEntity.ok(carService.getAllManufacturers());
    }

    @PostMapping("/api/manufacturers")
    public ResponseEntity<ManufacturerResponse> createManufacturer(
            @Valid @RequestBody ManufacturerRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(carService.createManufacturer(request));
    }

    // --- SUPPLIER ENDPOINTS ---

    @GetMapping("/api/suppliers")
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        return ResponseEntity.ok(carService.getAllSuppliers());
    }

    @PostMapping("/api/suppliers")
    public ResponseEntity<SupplierResponse> createSupplier(
            @Valid @RequestBody SupplierRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(carService.createSupplier(request));
    }

    // --- INTERNAL ENDPOINTS (USED BY OTHER MICROSERVICES) ---

    @GetMapping("/internal/cars/{id}")
    public ResponseEntity<CarResponse> getCarInternal(@PathVariable Integer id) {
        return ResponseEntity.ok(carService.getById(id));
    }

    @PutMapping("/internal/cars/{id}/status")
    public ResponseEntity<CarResponse> updateCarStatus(
            @PathVariable Integer id,
            @RequestParam Integer status) {
        return ResponseEntity.ok(carService.updateStatus(id, status));
    }
}
