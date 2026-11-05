package com.example.food_delivery_app.order.service;

import com.example.food_delivery_app.common.ErrorCode;
import com.example.food_delivery_app.exceptions.AppException;
import com.example.food_delivery_app.exceptions.ResourceNotFoundException;
import com.example.food_delivery_app.order.dto.driver.CreateDriverRequest;
import com.example.food_delivery_app.order.dto.driver.DriverResponse;
import com.example.food_delivery_app.order.entity.driver.Driver;
import com.example.food_delivery_app.order.entity.driver.DriverStatus;
import com.example.food_delivery_app.order.mapper.DriverMapper;
import com.example.food_delivery_app.order.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository drivers;
    private final DriverMapper mapper;

    @Transactional
    public DriverResponse createDriver(CreateDriverRequest request) {
        if (drivers.existsByPhone(request.phone())) {
            throw new AppException(ErrorCode.PHONE_NUMBER_ALREADY_REGISTERED , request.phone());
        }
        Driver saved = drivers.save(mapper.toDriverEntity(request)); // status defaults to AVAILABLE in the entity
        return mapper.toDriverResponse(saved);
    }

    @Transactional(readOnly = true)
    public DriverResponse getDriverById(Integer id) {
        return mapper.toDriverResponse(findDriverById(id));
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> getAllDriversByStatus(DriverStatus status) {
        List<Driver> result = (status == null) ? drivers.findAll() : drivers.findByStatus(status);
        return result.stream().map(mapper::toDriverResponse).toList();
    }

    @Transactional
    public DriverResponse updateDriverStatus(Integer id, DriverStatus newStatus) {
        Driver driver = findDriverById(id);
        driver.setStatus(newStatus); // dirty checking writes the UPDATE at commit, no save() needed
        return mapper.toDriverResponse(driver);
    }

    @Transactional
    public DriverResponse updateDriverLocation(Integer id, BigDecimal lat, BigDecimal lon) {
        Driver driver = findDriverById(id);
        driver.setLatitude(lat);
        driver.setLongitude(lon);
        return mapper.toDriverResponse(driver);
    }

    @Transactional
    public void deleteDriver(Integer id) {
        Driver driver = findDriverById(id);
        if (driver.getStatus() == DriverStatus.BUSY) {
            throw new AppException(ErrorCode.DRIVER_IS_BUSY);
        }
        drivers.delete(driver);
    }

    private Driver findDriverById(Integer id) {
        return drivers.findById(id).orElseThrow(() -> new ResourceNotFoundException("Driver", id));
    }
}