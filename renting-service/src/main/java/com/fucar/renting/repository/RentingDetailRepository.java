package com.fucar.renting.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fucar.renting.entity.RentingDetail;
import com.fucar.renting.entity.RentingDetailId;

public interface RentingDetailRepository extends JpaRepository<RentingDetail, RentingDetailId> {

    @Query("SELECT rd FROM RentingDetail rd " +
            "JOIN FETCH rd.transaction rt " +
            "WHERE rd.startDate >= :startDate AND rd.endDate <= :endDate " +
            "ORDER BY rt.totalPrice DESC")
    List<RentingDetail> findByDateRangeOrderByTotalPriceDesc(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}