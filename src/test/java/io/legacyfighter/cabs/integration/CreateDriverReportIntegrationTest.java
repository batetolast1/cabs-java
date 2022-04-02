package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.config.AppProperties;
import io.legacyfighter.cabs.dto.*;
import io.legacyfighter.cabs.entity.*;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.repository.AddressRepository;
import io.legacyfighter.cabs.service.DriverSessionService;
import io.legacyfighter.cabs.service.DriverTrackingService;
import io.legacyfighter.cabs.service.GeocodingService;
import io.legacyfighter.cabs.service.TransitService;
import io.legacyfighter.cabs.driverreport.DriverReportController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class CreateDriverReportIntegrationTest {

    private final static Instant TODAY = LocalDateTime.of(2022, Month.MARCH, 29, 19, 29).toInstant(OffsetDateTime.now().getOffset());

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private DriverSessionService driverSessionService;

    @Autowired
    private DriverTrackingService driverTrackingService;

    @Autowired
    private TransitService transitService;

    @Autowired
    private AddressRepository addressRepository;

    @MockBean
    private GeocodingService geocodingService;

    @MockBean
    private Clock clock;

    @MockBean
    private AppProperties appProperties;

    @Autowired
    private DriverReportController driverReportController;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(TODAY);

        when(appProperties.getAutomaticRefundForVipThreshold()).thenReturn(10);
        when(appProperties.getNoOfTransitsForClaimAutomaticRefund()).thenReturn(3);

        fixtures.anActiveCarCategory(CarType.CarClass.VAN);
        fixtures.anActiveCarCategory(CarType.CarClass.REGULAR);
    }

    @Test
    void canCreateDriverReport() {
        // given
        Client client = fixtures.aClient(Client.Type.NORMAL, "Alex", "Vega", Client.PaymentType.MONTHLY_INVOICE, Client.ClientType.INDIVIDUAL);
        // and
        Driver driver = fixtures.aDriver(Driver.Status.ACTIVE, "John", "Doe", "9AAAA123456AA1AA", "photo", Driver.Type.REGULAR);
        Driver additionalDriver = fixtures.aDriver(Driver.Status.ACTIVE, "Patrick", "Boyle", "AAAAA123456AA1AA", "photo", Driver.Type.REGULAR);
        // and
        fixtures.driverHasFee(driver, DriverFee.FeeType.FLAT, 50, new Money(10));
        // and
        fixtures.driverHasAttribute(driver, DriverAttribute.DriverAttributeName.PENALTY_POINTS, "10");
        fixtures.driverHasAttribute(driver, DriverAttribute.DriverAttributeName.NATIONALITY, "Polish");
        fixtures.driverHasAttribute(driver, DriverAttribute.DriverAttributeName.YEARS_OF_EXPERIENCE, "15");
        fixtures.driverHasAttribute(driver, DriverAttribute.DriverAttributeName.MEDICAL_EXAMINATION_EXPIRATION_DATE, "31.12.2022");
        fixtures.driverHasAttribute(driver, DriverAttribute.DriverAttributeName.MEDICAL_EXAMINATION_REMARKS, "private medical data");
        fixtures.driverHasAttribute(driver, DriverAttribute.DriverAttributeName.EMAIL, "johndoe@gmail.com");
        fixtures.driverHasAttribute(driver, DriverAttribute.DriverAttributeName.BIRTHPLACE, "Warsaw");
        fixtures.driverHasAttribute(driver, DriverAttribute.DriverAttributeName.COMPANY_NAME, "Uber");
        // and
        Address from = anAddress("Poland", "Poznań", "Antoninek", "Bożeny", 32, 1, "61-054", "home", 10, 20);
        Address to = anAddress("Germany", "Berlin", "Downtown", "Strasse", 12, 2, "61-053", "work", 10.1, 20.1);
        // and
        List<Transit> transits = driverHasCompletedTransitsInSession(driver, client, "ABC123", CarType.CarClass.VAN, "Volkswagen Golf", from, to, 2, additionalDriver);
        // and
        Claim claim = fixtures.createResolvedClaim(client, transits.get(0), "too fast");

        // when
        DriverReport driverReport = driverReportController.loadReportForDriver(driver.getId(), 0);

        // then
        assertThat(driverReport.getDriverDTO().getId()).isEqualTo(driver.getId());
        assertThat(driverReport.getDriverDTO().getFirstName()).isEqualTo("John");
        assertThat(driverReport.getDriverDTO().getLastName()).isEqualTo("Doe");
        assertThat(driverReport.getDriverDTO().getDriverLicense()).isEqualTo("9AAAA123456AA1AA");
        assertThat(driverReport.getDriverDTO().getPhoto()).isEqualTo("photo");
        assertThat(driverReport.getDriverDTO().getStatus()).isEqualTo(Driver.Status.ACTIVE);
        assertThat(driverReport.getDriverDTO().getType()).isEqualTo(Driver.Type.REGULAR);
        // and
        assertThat(driverReport.getAttributes()).hasSize(7);
        assertThat(driverReport.getAttributes()).contains(new DriverAttributeDTO(DriverAttribute.DriverAttributeName.PENALTY_POINTS, "10"));
        assertThat(driverReport.getAttributes()).contains(new DriverAttributeDTO(DriverAttribute.DriverAttributeName.NATIONALITY, "Polish"));
        assertThat(driverReport.getAttributes()).contains(new DriverAttributeDTO(DriverAttribute.DriverAttributeName.YEARS_OF_EXPERIENCE, "15"));
        assertThat(driverReport.getAttributes()).contains(new DriverAttributeDTO(DriverAttribute.DriverAttributeName.MEDICAL_EXAMINATION_EXPIRATION_DATE, "31.12.2022"));
        assertThat(driverReport.getAttributes()).contains(new DriverAttributeDTO(DriverAttribute.DriverAttributeName.EMAIL, "johndoe@gmail.com"));
        assertThat(driverReport.getAttributes()).contains(new DriverAttributeDTO(DriverAttribute.DriverAttributeName.BIRTHPLACE, "Warsaw"));
        assertThat(driverReport.getAttributes()).contains(new DriverAttributeDTO(DriverAttribute.DriverAttributeName.COMPANY_NAME, "Uber"));
        // and
        assertThat(driverReport.getSessions()).hasSize(1);
        List<DriverSessionDTO> driverSessionDTOS = new ArrayList<>(driverReport.getSessions().keySet());
        DriverSessionDTO driverSessionDTO1 = driverSessionDTOS.get(0);
        assertThat(driverSessionDTO1.getLoggedAt()).isEqualTo(TODAY);
        assertThat(driverSessionDTO1.getLoggedOutAt()).isEqualTo(TODAY);
        assertThat(driverSessionDTO1.getPlatesNumber()).isEqualTo("ABC123");
        assertThat(driverSessionDTO1.getCarClass()).isEqualTo(CarType.CarClass.VAN);
        assertThat(driverSessionDTO1.getCarBrand()).isEqualTo("Volkswagen Golf");
        // and
        List<TransitDTO> transitDTOS = driverReport.getSessions().get(driverSessionDTO1);
        assertThat(transitDTOS).hasSize(2);

        // and
        TransitDTO transitDTO1 = transitDTOS.get(0);
        assertThat(transitDTO1.getId()).isEqualTo(transits.get(0).getId());
        assertThat(transitDTO1.getTariff()).isEqualTo("Standard");
        assertThat(transitDTO1.getStatus()).isEqualTo(Transit.Status.COMPLETED);
        assertThat(transitDTO1.getDistance("km")).isEqualTo("42km");
        assertThat(transitDTO1.getKmRate()).isEqualTo(1.0f);
        assertThat(transitDTO1.getPrice()).isEqualTo(BigDecimal.valueOf(5100));
        assertThat(transitDTO1.getDriverFee()).isEqualTo(BigDecimal.valueOf(5050));
        assertThat(transitDTO1.getEstimatedPrice()).isEqualTo(BigDecimal.valueOf(5100));
        assertThat(transitDTO1.getDateTime()).isEqualTo(TODAY);
        assertThat(transitDTO1.getPublished()).isEqualTo(TODAY);
        assertThat(transitDTO1.getAcceptedAt()).isEqualTo(TODAY);
        assertThat(transitDTO1.getStarted()).isAfter(TODAY);
        assertThat(transitDTO1.getCompleteAt()).isEqualTo(TODAY);
        assertThat(transitDTO1.getClaimDTO()).isNotNull();
        assertThat(transitDTO1.getProposedDrivers()).hasSize(2);
        assertThat(transitDTO1.getTo()).isNotNull();
        assertThat(transitDTO1.getFrom()).isNotNull();
        assertThat(transitDTO1.getCarClass()).isEqualTo(CarType.CarClass.VAN);
        assertThat(transitDTO1.getClientDTO()).isNotNull();

        // and
        ClaimDTO claimDTO1 = transitDTO1.getClaimDTO();
        assertThat(claimDTO1.getClaimID()).isEqualTo(claim.getId());
        assertThat(claimDTO1.getClientId()).isEqualTo(client.getId());
        assertThat(claimDTO1.getTransitId()).isEqualTo(transits.get(0).getId());
        assertThat(claimDTO1.getReason()).isEqualTo("too fast");
        assertThat(claimDTO1.getIncidentDescription()).isEqualTo("incident description");
        assertThat(claimDTO1.isDraft()).isFalse();
        assertThat(claimDTO1.getCreationDate()).isEqualTo(TODAY);
        assertThat(claimDTO1.getCompletionDate()).isAfter(TODAY);
        assertThat(claimDTO1.getChangeDate()).isAfter(TODAY);
        assertThat(claimDTO1.getStatus()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimDTO1.getClaimNo()).isEqualTo("0---29/03/2022");

        // and
        DriverDTO driverDTO1_1 = transitDTO1.getProposedDrivers().get(0);
        assertThat(driverDTO1_1.getId()).isEqualTo(driver.getId());
        assertThat(driverDTO1_1.getFirstName()).isEqualTo("John");
        assertThat(driverDTO1_1.getLastName()).isEqualTo("Doe");
        assertThat(driverDTO1_1.getDriverLicense()).isEqualTo("9AAAA123456AA1AA");
        assertThat(driverDTO1_1.getPhoto()).isEqualTo("photo");
        assertThat(driverDTO1_1.getStatus()).isEqualTo(Driver.Status.ACTIVE);
        assertThat(driverDTO1_1.getType()).isEqualTo(Driver.Type.REGULAR);

        DriverDTO driverDTO1_2 = transitDTO1.getProposedDrivers().get(1);
        assertThat(driverDTO1_2.getId()).isEqualTo(additionalDriver.getId());
        assertThat(driverDTO1_2.getFirstName()).isEqualTo("Patrick");
        assertThat(driverDTO1_2.getLastName()).isEqualTo("Boyle");
        assertThat(driverDTO1_2.getDriverLicense()).isEqualTo("AAAAA123456AA1AA");
        assertThat(driverDTO1_2.getPhoto()).isEqualTo("photo");
        assertThat(driverDTO1_2.getStatus()).isEqualTo(Driver.Status.ACTIVE);
        assertThat(driverDTO1_2.getType()).isEqualTo(Driver.Type.REGULAR);

        // and
        AddressDTO toAddressDTO1 = transitDTO1.getTo();
        assertThat(toAddressDTO1.getCountry()).isEqualTo("Germany");
        assertThat(toAddressDTO1.getDistrict()).isEqualTo("Downtown");
        assertThat(toAddressDTO1.getCity()).isEqualTo("Berlin");
        assertThat(toAddressDTO1.getStreet()).isEqualTo("Strasse");
        assertThat(toAddressDTO1.getBuildingNumber()).isEqualTo(12);
        assertThat(toAddressDTO1.getAdditionalNumber()).isEqualTo(2);
        assertThat(toAddressDTO1.getPostalCode()).isEqualTo("61-053");
        assertThat(toAddressDTO1.getName()).isEqualTo("work");

        AddressDTO fromAddressDTO1 = transitDTO1.getFrom();
        assertThat(fromAddressDTO1.getCountry()).isEqualTo("Poland");
        assertThat(fromAddressDTO1.getDistrict()).isEqualTo("Antoninek");
        assertThat(fromAddressDTO1.getCity()).isEqualTo("Poznań");
        assertThat(fromAddressDTO1.getStreet()).isEqualTo("Bożeny");
        assertThat(fromAddressDTO1.getBuildingNumber()).isEqualTo(32);
        assertThat(fromAddressDTO1.getAdditionalNumber()).isEqualTo(1);
        assertThat(fromAddressDTO1.getPostalCode()).isEqualTo("61-054");
        assertThat(fromAddressDTO1.getName()).isEqualTo("home");

        // and
        ClientDTO clientDTO1 = transitDTO1.getClientDTO();
        assertThat(clientDTO1.getId()).isEqualTo(client.getId());
        assertThat(clientDTO1.getType()).isEqualTo(Client.Type.NORMAL);
        assertThat(clientDTO1.getName()).isEqualTo("Alex");
        assertThat(clientDTO1.getLastName()).isEqualTo("Vega");
        assertThat(clientDTO1.getDefaultPaymentType()).isEqualTo(Client.PaymentType.MONTHLY_INVOICE);
        assertThat(clientDTO1.getClientType()).isEqualTo(Client.ClientType.INDIVIDUAL);
    }

    private List<Transit> driverHasCompletedTransitsInSession(Driver driver,
                                                              Client client,
                                                              String plateNumber,
                                                              CarType.CarClass carClass,
                                                              String carBrand,
                                                              Address from,
                                                              Address to,
                                                              int transitAmount,
                                                              Driver additionalLoggedInDriver) {
        driverSessionService.logIn(driver.getId(), plateNumber, carClass, carBrand);
        driverTrackingService.registerPosition(driver.getId(), 10, 20);

        driverSessionService.logIn(additionalLoggedInDriver.getId(), plateNumber, carClass, carBrand);
        driverTrackingService.registerPosition(additionalLoggedInDriver.getId(), 10, 20);

        List<Transit> transits = new ArrayList<>();
        for (int i = 0; i < transitAmount; i++) {
            Transit transit = transitService.createTransit(client.getId(), from, to, carClass);
            transitService.publishTransit(transit.getId());
            transitService.acceptTransit(driver.getId(), transit.getId());
            transitService.startTransit(driver.getId(), transit.getId());
            transitService.completeTransit(driver.getId(), transit.getId(), to);
            transits.add(transit);
        }

        driverSessionService.logOutCurrentSession(driver.getId());

        driverSessionService.logOutCurrentSession(additionalLoggedInDriver.getId());

        return transits;
    }

    private Address anAddress(String country,
                              String city,
                              String district,
                              String street,
                              int buildingNumber,
                              int additionalNumber,
                              String postalCode,
                              String name,
                              double latitude,
                              double longitude) {
        Address address = new Address();
        address.setCountry(country);
        address.setCity(city);
        address.setDistrict(district);
        address.setStreet(street);
        address.setBuildingNumber(buildingNumber);
        address.setAdditionalNumber(additionalNumber);
        address.setPostalCode(postalCode);
        address.setName(name);

        when(geocodingService.geocodeAddress(address)).thenReturn(new double[]{latitude, longitude});

        return addressRepository.save(address);
    }
}
