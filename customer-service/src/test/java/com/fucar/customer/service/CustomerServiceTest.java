package com.fucar.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fucar.customer.dto.AuthenticateRequest;
import com.fucar.customer.dto.AuthenticateResponse;
import com.fucar.customer.dto.CustomerRequest;
import com.fucar.customer.dto.CustomerResponse;
import com.fucar.customer.entity.Customer;
import com.fucar.customer.repository.CustomerRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequest request;

    @BeforeEach
    void setUp() {
        request = new CustomerRequest();
        request.setCustomerName("Nguyen Van A");
        request.setTelephone("0901234567");
        request.setEmail("customer@test.com");
        request.setCustomerBirthday(LocalDate.of(2000, 1, 15));
        request.setPassword("password123");
    }

    @Test
    void register_shouldHashPasswordAndReturnResponse() {
        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed-password");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setCustomerId(1);
            return customer;
        });

        CustomerResponse response = customerService.register(request);

        assertThat(response.getCustomerId()).isEqualTo(1);
        assertThat(response.getCustomerName()).isEqualTo(request.getCustomerName());
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getCustomerStatus()).isEqualTo(1);
        verify(passwordEncoder).encode(request.getPassword());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void register_shouldThrowConflictWhenEmailAlreadyExists() {
        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> customerService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void authenticate_shouldReturnUserInfoWhenPasswordMatches() {
        Customer customer = Customer.builder()
                .customerId(7)
                .customerName("Nguyen Van A")
                .telephone("0901234567")
                .email(request.getEmail())
                .customerBirthday(request.getCustomerBirthday())
                .customerStatus(1)
                .password("hashed-password")
                .build();

        AuthenticateRequest authRequest = new AuthenticateRequest();
        authRequest.setEmail(request.getEmail());
        authRequest.setPassword(request.getPassword());

        when(customerRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(authRequest.getPassword(), customer.getPassword())).thenReturn(true);

        AuthenticateResponse response = customerService.authenticate(authRequest);

        assertThat(response.getCustomerId()).isEqualTo(7L);
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
    }

    @Test
    void authenticate_shouldRejectInvalidCredentials() {
        Customer customer = Customer.builder()
                .customerId(7)
                .email(request.getEmail())
                .password("hashed-password")
                .build();

        AuthenticateRequest authRequest = new AuthenticateRequest();
        authRequest.setEmail(request.getEmail());
        authRequest.setPassword("wrong-password");

        when(customerRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(authRequest.getPassword(), customer.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> customerService.authenticate(authRequest))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}

