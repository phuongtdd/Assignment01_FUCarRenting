package com.fucar.renting.service;

import com.fucar.renting.client.CarClient;
import com.fucar.renting.client.CustomerClient;
import com.fucar.renting.dto.*;
import com.fucar.renting.entity.RentingDetail;
import com.fucar.renting.entity.RentingTransaction;
import com.fucar.renting.repository.RentingDetailRepository;
import com.fucar.renting.repository.RentingTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentingService {

    private final RentingTransactionRepository transactionRepository;
    private final RentingDetailRepository detailRepository;
    private final CarClient carClient;
    private final CustomerClient customerClient;

    @Transactional
    public RentingResponse createRenting(Integer customerId, RentingRequest request) {
        // Validate customer exists
        try {
            customerClient.getCustomerById(customerId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer not found: " + customerId);
        }

        // Build transaction
        RentingTransaction transaction = RentingTransaction.builder()
                .rentingDate(LocalDate.now())
                .customerId(customerId)
                .rentingStatus("PENDING")
                .details(new ArrayList<>())
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (RentingDetailRequest detailReq : request.getDetails()) {
            // Validate car exists and is available
            CarResponse car;
            try {
                car = carClient.getCarById(detailReq.getCarId());
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Car not found: " + detailReq.getCarId());
            }
            if (car.getCarStatus() == null || car.getCarStatus() != 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Car is not available: " + detailReq.getCarId());
            }

            // Calculate price
            long days = ChronoUnit.DAYS.between(detailReq.getStartDate(), detailReq.getEndDate());
            if (days <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate must be after startDate");
            }
            BigDecimal price = car.getCarRentingPricePerDay().multiply(BigDecimal.valueOf(days));
            totalPrice = totalPrice.add(price);

            RentingDetail detail = RentingDetail.builder()
                    .carId(detailReq.getCarId())
                    .startDate(detailReq.getStartDate())
                    .endDate(detailReq.getEndDate())
                    .price(price)
                    .transaction(transaction)
                    .build();
            transaction.getDetails().add(detail);
        }

        transaction.setTotalPrice(totalPrice);
        RentingTransaction saved = transactionRepository.save(transaction);

        // Set the composite key fields for each detail and update car status
        for (RentingDetail d : saved.getDetails()) {
            d.setRentingTransactionId(saved.getRentingTransactionId());
            try {
                carClient.updateCarStatus(d.getCarId(), "0");
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update car status: " + d.getCarId());
            }
        }

        return toResponse(saved);
    }

    public List<RentingResponse> getMyRentings(Integer customerId) {
        return transactionRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<RentingResponse> getAllRentings() {
        return transactionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<RentingDetailResponse> getReport(LocalDate start, LocalDate end) {
        return detailRepository.findByDateRangeOrderByTotalPriceDesc(start, end)
                .stream()
                .map(rd -> RentingDetailResponse.builder()
                        .carId(rd.getCarId())
                        .startDate(rd.getStartDate())
                        .endDate(rd.getEndDate())
                        .price(rd.getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    private RentingResponse toResponse(RentingTransaction t) {
        List<RentingDetailResponse> details = t.getDetails() == null
                ? List.of()
                : t.getDetails().stream()
                        .map(d -> RentingDetailResponse.builder()
                                .carId(d.getCarId())
                                .startDate(d.getStartDate())
                                .endDate(d.getEndDate())
                                .price(d.getPrice())
                                .build())
                        .collect(Collectors.toList());

        return RentingResponse.builder()
                .rentingTransactionId(t.getRentingTransactionId())
                .rentingDate(t.getRentingDate())
                .totalPrice(t.getTotalPrice())
                .customerId(t.getCustomerId())
                .rentingStatus(t.getRentingStatus())
                .details(details)
                .build();
    }
}
