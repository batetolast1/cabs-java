package io.legacyfighter.cabs.common;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.dto.*;
import io.legacyfighter.cabs.entity.*;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.repository.AddressRepository;
import io.legacyfighter.cabs.repository.ClientRepository;
import io.legacyfighter.cabs.repository.DriverFeeRepository;
import io.legacyfighter.cabs.repository.TransitRepository;
import io.legacyfighter.cabs.service.CarTypeService;
import io.legacyfighter.cabs.service.ClaimService;
import io.legacyfighter.cabs.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.IntStream;

import static io.legacyfighter.cabs.entity.Driver.Status.ACTIVE;
import static io.legacyfighter.cabs.entity.Driver.Type.REGULAR;

@Component
public class Fixtures {

    @Autowired
    private TransitRepository transitRepository;

    @Autowired
    private DriverFeeRepository driverFeeRepository;

    @Autowired
    private DriverService driverService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CarTypeService carTypeService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ClaimService claimService;

    public Transit aCompletedTransitAt(Driver driver, LocalDateTime dateTime) {
        Instant date = dateTime.toInstant(OffsetDateTime.now().getOffset());
        Distance km = Distance.ofKm(10.0f);

        Transit transit = new Transit(null, null, null, null, date, km);
        transit.publishAt(date);
        transit.proposeTo(driver);
        transit.acceptBy(driver, date);
        transit.startAt(date);
        transit.completeAt(date, null, km);
        transitRepository.save(transit);
        return transit;
    }

    public Transit aCompletedTransitFor(Driver driver, Client client, int price) {
        Address from = anAddress();
        Address to = anAddress();
        Instant date = Instant.now();
        Distance km = Distance.ofKm(10.0f);

        Transit transit = new Transit(from, to, client, null, date, km);
        transit.publishAt(date);
        transit.proposeTo(driver);
        transit.acceptBy(driver, date);
        transit.startAt(date);
        transit.completeAt(date, to, km);
        transit.setPrice(new Money(price));
        return transitRepository.save(transit);
    }

    public void driverHasFee(Driver driver, DriverFee.FeeType feeType, int amount, Money minimumFee) {
        DriverFee driverFee = new DriverFee();
        driverFee.setDriver(driver);
        driverFee.setFeeType(feeType);
        driverFee.setAmount(amount);
        driverFee.setMin(minimumFee);

        driverFeeRepository.save(driverFee);
    }

    public Address anAddress() {
        Address address = new Address("country", "city", UUID.randomUUID().toString(), 1);
        return addressRepository.save(address);
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

    public Client aClient(Client.Type clientType) {
        Client client = new Client();
        client.setType(clientType);
        return clientRepository.save(client);
    }

    public Claim createClaim(Client client, Transit transit) {
        ClaimDTO claimDTO = new ClaimDTO();
        claimDTO.setClientId(client.getId());
        claimDTO.setTransitId(transit.getId());
        claimDTO.setReason("reason");
        claimDTO.setIncidentDescription("incident description");
        return claimService.create(claimDTO);
    }

    public Client aClientWithClaims(Client.Type type, int howManyClaims) {
        Client client = aClient(type);
        clientHasDoneClaims(client, howManyClaims);
        return client;
    }

    public void clientHasDoneClaims(Client client, int howManyClaims) {
        IntStream.range(0, howManyClaims)
                .forEach(i -> createAndResolveClaim(client, aCompletedTransitFor(aDriver(), client, 20)));
    }

    public void createAndResolveClaim(Client client, Transit transit) {
        Claim claim = createClaim(client, transit);
        claimService.tryToResolveAutomatically(claim.getId());
    }

    public void clientHasDoneTransits(Client client, int howManyTransits) {
        IntStream.range(0, howManyTransits)
                .forEach(i -> aCompletedTransitFor(aDriver(), client, 20));
    }
}
