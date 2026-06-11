package com.fucar.renting.controller;

import com.fucar.renting.dto.RentingDetailResponse;
import com.fucar.renting.dto.RentingRequest;
import com.fucar.renting.dto.RentingResponse;
import com.fucar.renting.service.RentingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RentingServiceControler {

    private final RentingService rentingService;

    // CUSTOMER — tạo đơn thuê xe
    @PostMapping("/api/rentings")
    public ResponseEntity<RentingResponse> createRenting(
            @Valid @RequestBody RentingRequest request,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId) {
        if (!"CUSTOMER".equals(role))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rentingService.createRenting(Integer.valueOf(userId), request));
    }

    // CUSTOMER — xem đơn thuê của mình
    @GetMapping("/api/rentings/my")
    public ResponseEntity<List<RentingResponse>> getMyRentings(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId) {
        if (!"CUSTOMER".equals(role))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(rentingService.getMyRentings(Integer.valueOf(userId)));
    }

    // ADMIN — xem tất cả đơn thuê
    @GetMapping("/api/rentings")
    public ResponseEntity<List<RentingResponse>> getAllRentings(
            @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(rentingService.getAllRentings());
    }

    // ADMIN — báo cáo thống kê
    @GetMapping("/api/rentings/report")
    public ResponseEntity<List<RentingDetailResponse>> report(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role))
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(rentingService.getReport(start, end));
    }
}