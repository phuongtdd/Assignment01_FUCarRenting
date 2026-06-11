package com.fucar.car.repository;

import com.fucar.car.entity.CarInformation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<CarInformation, Integer> {

    // Option 1: Override findAll using EntityGraph to fetch lazy relations in a single SQL query
    @Override
    @EntityGraph(attributePaths = {"manufacturer", "supplier"})
    List<CarInformation> findAll();

//    // Option 2: Custom JPQL query with JOIN FETCH to fetch lazy relations in a single SQL query
//    @Query("SELECT c FROM CarInformation c LEFT JOIN FETCH c.manufacturer LEFT JOIN FETCH c.supplier")
//    List<CarInformation> findAllWithRelations();
}
