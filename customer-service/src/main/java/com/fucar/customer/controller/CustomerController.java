package com.fucar.customer.controller;

import com.fucar.customer.dto.AuthenticateRequest;
import com.fucar.customer.dto.AuthenticateResponse;
import com.fucar.customer.dto.CustomerRequest;
import com.fucar.customer.dto.CustomerResponse;
import com.fucar.customer.service.CustomerService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/api/customers/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.register(request));
    }

    @PostMapping("/internal/customers/authenticate")
    public ResponseEntity<AuthenticateResponse> authenticate(@RequestBody AuthenticateRequest request) {
        return ResponseEntity.ok(customerService.authenticate(request));
    }

    @GetMapping("/internal/customers/{id}")
    public ResponseEntity<CustomerResponse> getByIdInternal(@PathVariable Integer id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @GetMapping("/api/customers")
    public ResponseEntity<List<CustomerResponse>> getAll(@RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/api/customers/{id}")
    public ResponseEntity<CustomerResponse> getById(
            @PathVariable Integer id,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId) {
        if (!"ADMIN".equals(role) && !userId.equals(String.valueOf(id))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(customerService.getById(id));
    }

    @PutMapping("/api/customers/{id}")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody CustomerRequest request,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId) {
        if (!"ADMIN".equals(role) && !userId.equals(String.valueOf(id))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @DeleteMapping("/api/customers/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

