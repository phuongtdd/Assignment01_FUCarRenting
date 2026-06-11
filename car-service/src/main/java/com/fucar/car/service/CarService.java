package com.fucar.car.service;

import com.fucar.car.dto.*;
import com.fucar.car.entity.CarInformation;
import com.fucar.car.entity.Manufacturer;
import com.fucar.car.entity.Supplier;
import com.fucar.car.exception.BadRequestException;
import com.fucar.car.exception.ResourceNotFoundException;
import com.fucar.car.repository.CarRepository;
import com.fucar.car.repository.ManufacturerRepository;
import com.fucar.car.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final SupplierRepository supplierRepository;

    @Transactional
    public CarResponse create(CarRequest request) {
        Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found"));
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        CarInformation car = CarInformation.builder()
                .carName(request.getCarName())
                .carDescription(request.getCarDescription())
                .numberOfDoors(request.getNumberOfDoors())
                .seatingCapacity(request.getSeatingCapacity())
                .fuelType(request.getFuelType())
                .year(request.getYear())
                .manufacturer(manufacturer)
                .supplier(supplier)
                .carStatus(request.getCarStatus())
                .carRentingPricePerDay(request.getCarRentingPricePerDay())
                .build();

        return toCarResponse(carRepository.save(car));
    }

    public List<CarResponse> getAll() {
        return carRepository.findAll().stream()
                .map(this::toCarResponse)
                .collect(Collectors.toList());
    }

    public CarResponse getById(Integer id) {
        CarInformation car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found"));
        return toCarResponse(car);
    }

    @Transactional
    public CarResponse update(Integer id, CarRequest request) {
        CarInformation car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found"));
        Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found"));
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        car.setCarName(request.getCarName());
        car.setCarDescription(request.getCarDescription());
        car.setNumberOfDoors(request.getNumberOfDoors());
        car.setSeatingCapacity(request.getSeatingCapacity());
        car.setFuelType(request.getFuelType());
        car.setYear(request.getYear());
        car.setManufacturer(manufacturer);
        car.setSupplier(supplier);
        car.setCarStatus(request.getCarStatus());
        car.setCarRentingPricePerDay(request.getCarRentingPricePerDay());

        return toCarResponse(carRepository.save(car));
    }

    @Transactional
    public void delete(Integer id) {
        if (!carRepository.existsById(id)) {
            throw new ResourceNotFoundException("Car not found");
        }
        carRepository.deleteById(id);
    }

    @Transactional
    public CarResponse updateStatus(Integer id, Integer status) {
        CarInformation car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found"));
        if (status != 0 && status != 1) {
            throw new BadRequestException("Invalid status value. Must be 0 or 1.");
        }
        car.setCarStatus(status);
        return toCarResponse(carRepository.save(car));
    }

    // Manufacturers
    @Transactional
    public ManufacturerResponse createManufacturer(ManufacturerRequest request) {
        Manufacturer manufacturer = Manufacturer.builder()
                .manufacturerName(request.getManufacturerName())
                .description(request.getDescription())
                .manufacturerCountry(request.getManufacturerCountry())
                .build();
        return toManufacturerResponse(manufacturerRepository.save(manufacturer));
    }

    public List<ManufacturerResponse> getAllManufacturers() {
        return manufacturerRepository.findAll().stream()
                .map(this::toManufacturerResponse)
                .collect(Collectors.toList());
    }

    // Suppliers
    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        Supplier supplier = Supplier.builder()
                .supplierName(request.getSupplierName())
                .supplierDescription(request.getSupplierDescription())
                .supplierAddress(request.getSupplierAddress())
                .build();
        return toSupplierResponse(supplierRepository.save(supplier));
    }

    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::toSupplierResponse)
                .collect(Collectors.toList());
    }

    // Mappers
    private CarResponse toCarResponse(CarInformation car) {
        return CarResponse.builder()
                .carId(car.getCarId())
                .carName(car.getCarName())
                .carDescription(car.getCarDescription())
                .numberOfDoors(car.getNumberOfDoors())
                .seatingCapacity(car.getSeatingCapacity())
                .fuelType(car.getFuelType())
                .year(car.getYear())
                .manufacturer(toManufacturerResponse(car.getManufacturer()))
                .supplier(toSupplierResponse(car.getSupplier()))
                .carStatus(car.getCarStatus())
                .carRentingPricePerDay(car.getCarRentingPricePerDay())
                .build();
    }

    private ManufacturerResponse toManufacturerResponse(Manufacturer m) {
        if (m == null) return null;
        return ManufacturerResponse.builder()
                .manufacturerId(m.getManufacturerId())
                .manufacturerName(m.getManufacturerName())
                .description(m.getDescription())
                .manufacturerCountry(m.getManufacturerCountry())
                .build();
    }

    private SupplierResponse toSupplierResponse(Supplier s) {
        if (s == null) return null;
        return SupplierResponse.builder()
                .supplierId(s.getSupplierId())
                .supplierName(s.getSupplierName())
                .supplierDescription(s.getSupplierDescription())
                .supplierAddress(s.getSupplierAddress())
                .build();
    }
}
