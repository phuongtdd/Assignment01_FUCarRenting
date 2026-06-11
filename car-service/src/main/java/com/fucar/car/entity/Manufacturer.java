package com.fucar.car.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "manufacturers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Manufacturer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer manufacturerId;
    
    private String manufacturerName;
    private String description;
    private String manufacturerCountry;
}
