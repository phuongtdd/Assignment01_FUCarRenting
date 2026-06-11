package com.fucar.renting;

import com.fucar.renting.client.CarClient;
import com.fucar.renting.client.CustomerClient;
import com.fucar.renting.dto.CarResponse;
import com.fucar.renting.dto.CustomerResponse;
import com.fucar.renting.dto.RentingDetailRequest;
import com.fucar.renting.dto.RentingDetailResponse;
import com.fucar.renting.dto.RentingRequest;
import com.fucar.renting.entity.RentingDetail;
import com.fucar.renting.entity.RentingTransaction;
import com.fucar.renting.repository.RentingDetailRepository;
import com.fucar.renting.repository.RentingTransactionRepository;
import com.fucar.renting.service.RentingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentingServiceApplicationTests {

    @Mock
    private RentingTransactionRepository transactionRepository;

    @Mock
    private RentingDetailRepository detailRepository;

    @Mock
    private CarClient carClient;

    @Mock
    private CustomerClient customerClient;

    @InjectMocks
    private RentingService rentingService;

    private RentingRequest request;
    private RentingDetailRequest detailReq1;
    private RentingDetailRequest detailReq2;

    @BeforeEach
    void setUp() {
        detailReq1 = new RentingDetailRequest();
        detailReq1.setCarId(1);
        detailReq1.setStartDate(LocalDate.now());
        detailReq1.setEndDate(LocalDate.now().plusDays(2));

        detailReq2 = new RentingDetailRequest();
        detailReq2.setCarId(2);
        detailReq2.setStartDate(LocalDate.now());
        detailReq2.setEndDate(LocalDate.now().plusDays(3));

        request = new RentingRequest();
        request.setDetails(List.of(detailReq1, detailReq2));
    }

    @Test
    void testCreateRenting_Success() {
        // Mock Customer
        when(customerClient.getCustomerById(100)).thenReturn(new CustomerResponse());

        // Mock Cars (Available)
        CarResponse car1 = new CarResponse();
        car1.setCarId(1);
        car1.setCarStatus(1);
        car1.setCarRentingPricePerDay(BigDecimal.valueOf(100));

        CarResponse car2 = new CarResponse();
        car2.setCarId(2);
        car2.setCarStatus(1);
        car2.setCarRentingPricePerDay(BigDecimal.valueOf(200));

        when(carClient.getCarById(1)).thenReturn(car1);
        when(carClient.getCarById(2)).thenReturn(car2);

        // Mock Save
        RentingTransaction savedTx = new RentingTransaction();
        savedTx.setRentingTransactionId(10);
        when(transactionRepository.save(any(RentingTransaction.class))).thenAnswer(invocation -> {
            RentingTransaction tx = invocation.getArgument(0);
            tx.setRentingTransactionId(10);
            return tx;
        });

        // Act
        rentingService.createRenting(100, request);

        // Assert - DB Renting has data (saved)
        verify(transactionRepository, times(1)).save(any(RentingTransaction.class));

        // Assert - DB Car has car status = 0 (updated via client)
        verify(carClient, times(1)).updateCarStatus(1, "0");
        verify(carClient, times(1)).updateCarStatus(2, "0");
    }

    @Test
    void testCreateRenting_CarAlreadyRented_ThrowsException() {
        // Mock Customer
        when(customerClient.getCustomerById(100)).thenReturn(new CustomerResponse());

        // Mock Cars (Car 1 is Available, Car 2 is NOT Available)
        CarResponse car1 = new CarResponse();
        car1.setCarId(1);
        car1.setCarStatus(1);
        car1.setCarRentingPricePerDay(BigDecimal.valueOf(100));

        CarResponse car2 = new CarResponse();
        car2.setCarId(2);
        car2.setCarStatus(0); // Not available!
        car2.setCarRentingPricePerDay(BigDecimal.valueOf(200));

        when(carClient.getCarById(1)).thenReturn(car1);
        when(carClient.getCarById(2)).thenReturn(car2);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            rentingService.createRenting(100, request);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Car is not available: 2"));

        // Verify that transaction is never saved
        verify(transactionRepository, never()).save(any(RentingTransaction.class));

        // Verify that NO car status is updated
        verify(carClient, never()).updateCarStatus(anyInt(), anyString());
    }

    @Test
    void testGetReport_Success() {
        // Arrange
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(5);

        RentingTransaction tx1 = new RentingTransaction();
        tx1.setTotalPrice(BigDecimal.valueOf(500));

        RentingTransaction tx2 = new RentingTransaction();
        tx2.setTotalPrice(BigDecimal.valueOf(1000));

        RentingDetail rd1 = new RentingDetail();
        rd1.setCarId(1);
        rd1.setStartDate(start);
        rd1.setEndDate(end);
        rd1.setPrice(BigDecimal.valueOf(500));
        rd1.setTransaction(tx1);

        RentingDetail rd2 = new RentingDetail();
        rd2.setCarId(2);
        rd2.setStartDate(start);
        rd2.setEndDate(end);
        rd2.setPrice(BigDecimal.valueOf(1000));
        rd2.setTransaction(tx2);

        // Mock repository returning data sorted by totalPrice DESC (tx2 then tx1)
        when(detailRepository.findByDateRangeOrderByTotalPriceDesc(start, end))
                .thenReturn(List.of(rd2, rd1));

        // Act
        List<RentingDetailResponse> report = rentingService.getReport(start, end);

        // Assert
        assertEquals(2, report.size());
        assertEquals(2, report.get(0).getCarId());
        assertEquals(BigDecimal.valueOf(1000), report.get(0).getPrice());
        assertEquals(1, report.get(1).getCarId());
        assertEquals(BigDecimal.valueOf(500), report.get(1).getPrice());

        verify(detailRepository, times(1)).findByDateRangeOrderByTotalPriceDesc(start, end);
    }
}
