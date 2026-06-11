package com.fucar.car.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fucar.car.dto.CarRequest;
import com.fucar.car.entity.CarInformation;
import com.fucar.car.entity.Manufacturer;
import com.fucar.car.entity.Supplier;
import com.fucar.car.repository.CarRepository;
import com.fucar.car.repository.ManufacturerRepository;
import com.fucar.car.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Manufacturer manufacturer;
    private Supplier supplier;
    private CarInformation car;

    @BeforeEach
    public void setUp() {
        carRepository.deleteAll();
        manufacturerRepository.deleteAll();
        supplierRepository.deleteAll();

        manufacturer = manufacturerRepository.save(Manufacturer.builder()
                .manufacturerName("Toyota")
                .manufacturerCountry("Japan")
                .description("Japanese Auto Manufacturer")
                .build());

        supplier = supplierRepository.save(Supplier.builder()
                .supplierName("Supplier A")
                .supplierAddress("Hanoi")
                .supplierDescription("Premium Car Supplier")
                .build());

        car = carRepository.save(CarInformation.builder()
                .carName("Camry")
                .carDescription("Sedan")
                .numberOfDoors(4)
                .seatingCapacity(5)
                .fuelType("Petrol")
                .year(2023)
                .manufacturer(manufacturer)
                .supplier(supplier)
                .carStatus(1)
                .carRentingPricePerDay(BigDecimal.valueOf(1500000))
                .build());
    }

    @Test
    public void testCreateCar_WhenAdmin_ReturnsCreated() throws Exception {
        CarRequest request = new CarRequest();
        request.setCarName("Altis");
        request.setCarDescription("Sedan");
        request.setNumberOfDoors(4);
        request.setSeatingCapacity(5);
        request.setFuelType("Petrol");
        request.setYear(2023);
        request.setManufacturerId(manufacturer.getManufacturerId());
        request.setSupplierId(supplier.getSupplierId());
        request.setCarStatus(1);
        request.setCarRentingPricePerDay(BigDecimal.valueOf(1000000));

        mockMvc.perform(post("/api/cars")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.carName").value("Altis"));
    }

    @Test
    public void testCreateCar_WhenNotAdmin_ReturnsForbidden() throws Exception {
        CarRequest request = new CarRequest();
        request.setCarName("Altis");
        request.setCarDescription("Sedan");
        request.setNumberOfDoors(4);
        request.setSeatingCapacity(5);
        request.setFuelType("Petrol");
        request.setYear(2023);
        request.setManufacturerId(manufacturer.getManufacturerId());
        request.setSupplierId(supplier.getSupplierId());
        request.setCarStatus(1);
        request.setCarRentingPricePerDay(BigDecimal.valueOf(1000000));

        mockMvc.perform(post("/api/cars")
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateCarStatus_UpdatesCorrectly() throws Exception {
        // From 1 -> 0
        mockMvc.perform(put("/internal/cars/" + car.getCarId() + "/status")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carStatus").value(0));

        CarInformation updatedCar = carRepository.findById(car.getCarId()).orElseThrow();
        assertEquals(0, updatedCar.getCarStatus());

        // From 0 -> 1
        mockMvc.perform(put("/internal/cars/" + car.getCarId() + "/status")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carStatus").value(1));

        updatedCar = carRepository.findById(car.getCarId()).orElseThrow();
        assertEquals(1, updatedCar.getCarStatus());
    }
}
