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
import io.legacyfighter.cabs.ui.DriverReportController;
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
        Client client = fixtures.aClient();
        // and
        Driver driver = fixtures.aDriver(Driver.Status.ACTIVE, "John", "Doe", "9AAAA123456AA1AA", "photo", Driver.Type.REGULAR);
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
        Transit transit = driverHasCompletedTransitInSession(driver, client, "ABC123", CarType.CarClass.VAN, "Volkswagen Golf", from, to);
        // and
        Claim claim = fixtures.createResolvedClaim(client, transit, "too fast");

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
        assertThat(transitDTOS).hasSize(1);
        TransitDTO transitDTO = transitDTOS.get(0);
        assertThat(transitDTO.getId()).isEqualTo(transit.getId());
        assertThat(transitDTO.getTariff()).isEqualTo("Standard");
        assertThat(transitDTO.getStatus()).isEqualTo(Transit.Status.COMPLETED);
        assertThat(transitDTO.getDistance("km")).isEqualTo("42km");
        assertThat(transitDTO.getKmRate()).isEqualTo(1.0f);
        assertThat(transitDTO.getPrice()).isEqualTo(BigDecimal.valueOf(5100));
        assertThat(transitDTO.getDriverFee()).isEqualTo(BigDecimal.valueOf(5050));
        assertThat(transitDTO.getEstimatedPrice()).isEqualTo(BigDecimal.valueOf(5100));
        assertThat(transitDTO.getDateTime()).isEqualTo(TODAY);
        assertThat(transitDTO.getPublished()).isEqualTo(TODAY);
        assertThat(transitDTO.getAcceptedAt()).isEqualTo(TODAY);
        assertThat(transitDTO.getStarted()).isAfter(TODAY);
        assertThat(transitDTO.getCompleteAt()).isEqualTo(TODAY);
        assertThat(transitDTO.getClaimDTO()).isNotNull();
        assertThat(transitDTO.getProposedDrivers()).hasSize(1);
        assertThat(transitDTO.getTo()).isNotNull();
        assertThat(transitDTO.getFrom()).isNotNull();
        assertThat(transitDTO.getCarClass()).isEqualTo(CarType.CarClass.VAN);
        assertThat(transitDTO.getClientDTO()).isNotNull();

        // and
        ClaimDTO claimDTO = transitDTO.getClaimDTO();
        assertThat(claimDTO.getClaimID()).isEqualTo(claim.getId());
        assertThat(claimDTO.getClientId()).isEqualTo(client.getId());
        assertThat(claimDTO.getTransitId()).isEqualTo(transit.getId());
        assertThat(claimDTO.getReason()).isEqualTo("too fast");
        assertThat(claimDTO.getIncidentDescription()).isEqualTo("incident description");
        assertThat(claimDTO.isDraft()).isFalse();
        assertThat(claimDTO.getCreationDate()).isEqualTo(TODAY);
        assertThat(claimDTO.getCompletionDate()).isAfter(TODAY);
        assertThat(claimDTO.getChangeDate()).isAfter(TODAY);
        assertThat(claimDTO.getStatus()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimDTO.getClaimNo()).isEqualTo("0---29/03/2022");

        // and
        DriverDTO driverDTO = transitDTO.getProposedDrivers().get(0);
        assertThat(driverDTO.getId()).isEqualTo(driver.getId());
        assertThat(driverDTO.getFirstName()).isEqualTo("John");
        assertThat(driverDTO.getLastName()).isEqualTo("Doe");
        assertThat(driverDTO.getDriverLicense()).isEqualTo("9AAAA123456AA1AA");
        assertThat(driverDTO.getPhoto()).isEqualTo("photo");
        assertThat(driverDTO.getStatus()).isEqualTo(Driver.Status.ACTIVE);
        assertThat(driverDTO.getType()).isEqualTo(Driver.Type.REGULAR);

        // and
        AddressDTO toAddressDTO = transitDTO.getTo();
        assertThat(toAddressDTO.getCountry()).isEqualTo("Germany");
        assertThat(toAddressDTO.getDistrict()).isEqualTo("Downtown");
        assertThat(toAddressDTO.getCity()).isEqualTo("Berlin");
        assertThat(toAddressDTO.getStreet()).isEqualTo("Strasse");
        assertThat(toAddressDTO.getBuildingNumber()).isEqualTo(12);
        assertThat(toAddressDTO.getAdditionalNumber()).isEqualTo(2);
        assertThat(toAddressDTO.getPostalCode()).isEqualTo("61-053");
        assertThat(toAddressDTO.getName()).isEqualTo("work");

        AddressDTO fromAddressDTO = transitDTO.getFrom();
        assertThat(fromAddressDTO.getCountry()).isEqualTo("Poland");
        assertThat(fromAddressDTO.getDistrict()).isEqualTo("Antoninek");
        assertThat(fromAddressDTO.getCity()).isEqualTo("Poznań");
        assertThat(fromAddressDTO.getStreet()).isEqualTo("Bożeny");
        assertThat(fromAddressDTO.getBuildingNumber()).isEqualTo(32);
        assertThat(fromAddressDTO.getAdditionalNumber()).isEqualTo(1);
        assertThat(fromAddressDTO.getPostalCode()).isEqualTo("61-054");
        assertThat(fromAddressDTO.getName()).isEqualTo("home");

        // and
        ClientDTO clientDTO = transitDTO.getClientDTO();
        assertThat(clientDTO.getId()).isEqualTo(client.getId());
        assertThat(clientDTO.getType()).isNull();
        assertThat(clientDTO.getName()).isNull();
        assertThat(clientDTO.getLastName()).isNull();
        assertThat(clientDTO.getDefaultPaymentType()).isNull();
        assertThat(clientDTO.getClientType()).isNull();
    }

    private Transit driverHasCompletedTransitInSession(Driver driver,
                                                       Client client,
                                                       String plateNumber,
                                                       CarType.CarClass carClass,
                                                       String carBrand,
                                                       Address from,
                                                       Address to) {
        driverSessionService.logIn(driver.getId(), plateNumber, carClass, carBrand);
        driverTrackingService.registerPosition(driver.getId(), 10, 20);

        Transit transit = transitService.createTransit(client.getId(), from, to, carClass);
        transitService.publishTransit(transit.getId());
        transitService.acceptTransit(driver.getId(), transit.getId());
        transitService.startTransit(driver.getId(), transit.getId());
        transitService.completeTransit(driver.getId(), transit.getId(), to);

        driverSessionService.logOutCurrentSession(driver.getId());

        return transit;
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