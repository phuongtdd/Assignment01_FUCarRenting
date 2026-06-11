package com.fucar.renting.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class RentingDetailRequest {
    @NotNull
    private Integer carId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
