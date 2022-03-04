package io.legacyfighter.cabs.common;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.dto.AddressDTO;
import io.legacyfighter.cabs.dto.CarTypeDTO;
import io.legacyfighter.cabs.dto.ClientDTO;
import io.legacyfighter.cabs.dto.TransitDTO;
import io.legacyfighter.cabs.entity.*;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.repository.ClientRepository;
import io.legacyfighter.cabs.repository.DriverFeeRepository;
import io.legacyfighter.cabs.repository.TransitRepository;
import io.legacyfighter.cabs.service.CarTypeService;
import io.legacyfighter.cabs.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

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

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    CarTypeService carTypeService;

    public Transit aTransit(Driver driver, LocalDateTime dateTime) {
        Instant instant = dateTime.toInstant(OffsetDateTime.now().getOffset());
        Distance km = Distance.ofKm(10.0f);
        Transit transit = new Transit(null, null, null, null, instant, km);
        transitRepository.save(transit);
        transit.proposeTo(driver);
        transit.acceptBy(driver, instant);
        transit.startAt(instant);
        transit.completeAt(instant, null, km);
        transitRepository.save(transit);
        return transit;
    }

    public void driverHasFee(Driver driver, DriverFee.FeeType feeType, int amount, Money minimumFee) {
        DriverFee driverFee = new DriverFee();
        driverFee.setDriver(driver);
        driverFee.setFeeType(feeType);
        driverFee.setAmount(amount);
        driverFee.setMin(minimumFee);

        driverFeeRepository.save(driverFee);
    }

    public Driver aDriver() {
        return driverService.createDriver("9AAAA123456AA1AA", "last name", "first name", REGULAR, ACTIVE, "photo");
    }

    public void anActiveCarCategory(CarType.CarClass carClass) {
        CarTypeDTO carTypeDTO = new CarTypeDTO();
        carTypeDTO.setCarClass(carClass);
        CarType carType = carTypeService.create(carTypeDTO);

        for (int i = 0; i < 10; i++) {
            carTypeService.registerCar(carClass);
        }

        carTypeService.activate(carType.getId());
    }

    public TransitDTO aTransitDTO(Client client, AddressDTO from, AddressDTO to) {
        TransitDTO transitDTO = new TransitDTO();

        transitDTO.setClientDTO(new ClientDTO(client));
        transitDTO.setFrom(from);
        transitDTO.setTo(to);

        return transitDTO;
    }

    public TransitDTO aTransitDTO(AddressDTO from, AddressDTO to) {
        return aTransitDTO(aClient(), from, to);
    }

    public Client aClient() {
        return clientRepository.save(new Client());
    }
}
