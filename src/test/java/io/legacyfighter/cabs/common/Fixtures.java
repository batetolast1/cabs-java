package io.legacyfighter.cabs.common;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.entity.DriverFee;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.repository.DriverFeeRepository;
import io.legacyfighter.cabs.repository.TransitRepository;
import io.legacyfighter.cabs.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;

import static io.legacyfighter.cabs.entity.Driver.Status.ACTIVE;
import static io.legacyfighter.cabs.entity.Driver.Type.REGULAR;

@Component
public class Fixtures {

    @Autowired
    TransitRepository transitRepository;

    @Autowired
    DriverFeeRepository driverFeeRepository;

    @Autowired
    DriverService driverService;

    public Transit aTransit(Driver driver, LocalDateTime dateTime) {
        Transit transit = new Transit();
        transit.setDateTime(dateTime.toInstant(ZoneOffset.UTC));
        transit.setDriver(driver);
        transit.setStatus(Transit.Status.DRAFT);
        transit.setKm(Distance.ofKm(10.0f));
        transit.setStatus(Transit.Status.COMPLETED);
        transit.setPrice(transit.calculateFinalCosts());

        return transitRepository.save(transit);
    }

    public Transit aTransit(Driver driver) {
        return aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 20, 14, 0));
    }

    public void driverHasFee(Driver driver, DriverFee.FeeType feeType, int amount, int minimumFee) {
        DriverFee driverFee = new DriverFee();
        driverFee.setDriver(driver);
        driverFee.setFeeType(feeType);
        driverFee.setAmount(amount);
        driverFee.setMin(new Money(minimumFee));

        driverFeeRepository.save(driverFee);
    }

    public Driver aDriver() {
        return driverService.createDriver("9AAAA123456AA1AA", "last name", "first name", REGULAR, ACTIVE, "photo");
    }
}
