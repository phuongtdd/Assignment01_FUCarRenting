package com.fucar.customer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fucar.customer.dto.AuthenticateResponse;
import com.fucar.customer.dto.CustomerRequest;
import com.fucar.customer.dto.CustomerResponse;
import com.fucar.customer.service.CustomerService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CustomerController(customerService)).build();
    }

    @Test
    void register_shouldReturnCreatedCustomer() throws Exception {
        CustomerRequest request = new CustomerRequest();
        request.setCustomerName("Nguyen Van A");
        request.setTelephone("0901234567");
        request.setEmail("customer@test.com");
        request.setPassword("password123");

        CustomerResponse response = CustomerResponse.builder()
                .customerId(1)
                .customerName(request.getCustomerName())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .customerBirthday(null)
                .customerStatus(1)
                .build();

        when(customerService.register(any(CustomerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/customers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.email").value("customer@test.com"));
    }

    @Test
    void authenticate_shouldReturnTokenDataFromInternalEndpoint() throws Exception {
        when(customerService.authenticate(any())).thenReturn(new AuthenticateResponse(9L, "customer@test.com"));

        mockMvc.perform(post("/internal/customers/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"customer@test.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(9))
                .andExpect(jsonPath("$.email").value("customer@test.com"));
    }

    @Test
    void getAll_shouldAllowAdminOnly() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of());

        mockMvc.perform(get("/api/customers")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/customers")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_shouldAllowOwnerAndRejectOthers() throws Exception {
        CustomerRequest request = new CustomerRequest();
        request.setCustomerName("Nguyen Van B");
        request.setTelephone("0909999999");
        request.setEmail("customer@test.com");
        request.setPassword("password123");

        CustomerResponse response = CustomerResponse.builder()
                .customerId(1)
                .customerName(request.getCustomerName())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .customerBirthday(null)
                .customerStatus(1)
                .build();

        when(customerService.update(1, request)).thenReturn(response);

        mockMvc.perform(put("/api/customers/1")
                        .header("X-User-Role", "CUSTOMER")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Nguyen Van B"));

        mockMvc.perform(put("/api/customers/1")
                        .header("X-User-Role", "CUSTOMER")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_shouldAllowAdminOnly() throws Exception {
        mockMvc.perform(delete("/api/customers/1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/customers/1")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }
}



