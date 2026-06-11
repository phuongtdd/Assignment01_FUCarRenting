package com.fucar.customer.service;

import com.fucar.customer.dto.AuthenticateRequest;
import com.fucar.customer.dto.AuthenticateResponse;
import com.fucar.customer.dto.CustomerRequest;
import com.fucar.customer.dto.CustomerResponse;
import com.fucar.customer.entity.Customer;
import com.fucar.customer.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerResponse register(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        Customer customer = Customer.builder()
                .customerName(request.getCustomerName())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .customerBirthday(request.getCustomerBirthday())
                .password(passwordEncoder.encode(request.getPassword()))
                .customerStatus(1)
                .build();

        return toResponse(customerRepository.save(customer));
    }

    public AuthenticateResponse authenticate(AuthenticateRequest request) {
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return new AuthenticateResponse(customer.getCustomerId().longValue(), customer.getEmail());
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CustomerResponse getById(Integer id) {
        return toResponse(findCustomerOrThrow(id));
    }

    public CustomerResponse update(Integer id, CustomerRequest request) {
        Customer customer = findCustomerOrThrow(id);

        if (!customer.getEmail().equals(request.getEmail()) && customerRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        customer.setCustomerName(request.getCustomerName());
        customer.setTelephone(request.getTelephone());
        customer.setEmail(request.getEmail());
        customer.setCustomerBirthday(request.getCustomerBirthday());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));

        return toResponse(customerRepository.save(customer));
    }

    public void delete(Integer id) {
        Customer customer = findCustomerOrThrow(id);
        customerRepository.delete(customer);
    }

    private Customer findCustomerOrThrow(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .customerName(customer.getCustomerName())
                .telephone(customer.getTelephone())
                .email(customer.getEmail())
                .customerBirthday(customer.getCustomerBirthday())
                .customerStatus(customer.getCustomerStatus())
                .build();
    }
}

