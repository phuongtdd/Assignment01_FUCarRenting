package com.fucar.renting.entity;

import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentingDetailId implements Serializable {
    private Integer rentingTransactionId;
    private Integer carId;
}