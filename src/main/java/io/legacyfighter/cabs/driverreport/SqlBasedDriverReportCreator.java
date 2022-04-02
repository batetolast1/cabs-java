package io.legacyfighter.cabs.driverreport;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.dto.*;
import io.legacyfighter.cabs.entity.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.legacyfighter.cabs.entity.DriverAttribute.DriverAttributeName.MEDICAL_EXAMINATION_REMARKS;

@Service
class SqlBasedDriverReportCreator {

    private static final String QUERY_FOR_DRIVER_WITH_ATTRIBUTES =
            "SELECT " +
                    "d.id AS DRIVER_ID, " +
                    "d.first_name AS DRIVER_FIRST_NAME, " +
                    "d.last_name AS DRIVER_LAST_NAME, " +
                    "d.driver_license AS DRIVER_LICENSE, " +
                    "d.photo AS DRIVER_PHOTO, " +
                    "d.status AS DRIVER_STATUS, " +
                    "d.type AS DRIVER_TYPE, " +
                    "" +
                    "attr.name, " +
                    "attr.value " +
                    "FROM " +
                    "driver d " +
                    "LEFT JOIN " +
                    "driver_attribute attr ON d.id = attr.driver_id " +
                    "WHERE " +
                    "d.id = :driverId AND attr.name <> :filteredAttribute";

    private static final String QUERY_FOR_DRIVER_SESSIONS_WITH_TRANSITS =
            "SELECT " +
                    "ds.logged_at, " +
                    "ds.logged_out_at, " +
                    "ds.plates_number, " +
                    "ds.car_class, " +
                    "ds.car_brand, " +
                    "" +
                    "t.id AS TRANSIT_ID, " +
                    "t.name AS TARIFF_NAME, " +
                    "t.status AS TRANSIT_STATUS, " +
                    "t.km, " +
                    "t.km_rate, " +
                    "t.price, " +
                    "t.drivers_fee, " +
                    "t.estimated_price, " +
                    "t.base_fee, " +
                    "t.date_time, " +
                    "t.published, " +
                    "t.accepted_at, " +
                    "t.started, " +
                    "t.complete_at, " +
                    "t.car_type, " +
                    "" +
                    "cl.id AS CLAIM_ID, " +
                    "cl.owner_id, " +
                    "cl.reason, " +
                    "cl.incident_description, " +
                    "cl.status AS CLAIM_STATUS, " +
                    "cl.creation_date, " +
                    "cl.completion_date, " +
                    "cl.change_date, " +
                    "cl.completion_mode, " +
                    "cl.claim_no, " +
                    "" +
                    "af.country AS AF_COUNTRY, " +
                    "af.district AS AF_DISTRICT, " +
                    "af.city AS AF_CITY, " +
                    "af.street AS AF_STREET, " +
                    "af.building_number AS AF_NUMBER, " +
                    "af.additional_number AS AF_ADDITIONAL_NUMBER, " +
                    "af.postal_code AS AF_POSTAL_CODE, " +
                    "af.name AS AF_NAME, " +
                    "" +
                    "ato.country AS ATO_COUNTRY, " +
                    "ato.district AS ATO_DISTRICT, " +
                    "ato.city AS ATO_CITY, " +
                    "ato.street AS ATO_STREET, " +
                    "ato.building_number AS ATO_NUMBER, " +
                    "ato.additional_number AS ATO_ADDITIONAL_NUMBER, " +
                    "ato.postal_code AS ATO_POSTAL_CODE, " +
                    "ato.name AS ATO_NAME, " +
                    "" +
                    "cli.id AS CLIENT_ID, " +
                    "cli.type AS CLIENT_TYPE, " +
                    "cli.name AS CLIENT_NAME, " +
                    "cli.last_name AS CLIENT_LAST_NAME, " +
                    "cli.default_payment_type AS CLIENT_DEFAULT_PAYMENT_TYPE, " +
                    "cli.client_type AS CLIENT_CLIENT_TYPE ," +
                    "" +
                    "d.id AS DRIVER_ID, " +
                    "d.first_name AS DRIVER_FIRST_NAME, " +
                    "d.last_name AS DRIVER_LAST_NAME, " +
                    "d.driver_license AS DRIVER_LICENSE, " +
                    "d.photo AS DRIVER_PHOTO, " +
                    "d.status AS DRIVER_STATUS, " +
                    "d.type AS DRIVER_TYPE " +
                    "" +
                    "FROM " +
                    "driver_session ds " +
                    "LEFT JOIN " +
                    "transit t ON t.driver_id = ds.driver_id " +
                    "LEFT JOIN " +
                    "address af ON t.from_id = af.id " +
                    "LEFT JOIN " +
                    "address ato ON t.to_id = ato.id " +
                    "LEFT JOIN " +
                    "claim cl ON t.id = cl.transit_id " +
                    "LEFT JOIN " +
                    "client cli on t.client_id = cli.id " +
                    "LEFT JOIN " +
                    "transit_proposed_driver tpd on t.id = tpd.transit_id " +
                    "LEFT JOIN " +
                    "driver d on d.id = tpd.proposed_driver_id " +
                    "WHERE " +
                    "ds.driver_id = :driverId " +
                    "AND t.status = :transitStatus " +
                    "AND ds.logged_at >= :since " +
                    "AND t.complete_at >= ds.logged_at " +
                    "AND t.complete_at <= ds.logged_out_at " +
                    "GROUP BY " +
                    "ds.id, " +
                    "t.id, " +
                    "d.id";

    private final EntityManager entityManager;

    private final Clock clock;

    SqlBasedDriverReportCreator(EntityManager entityManager,
                                Clock clock) {
        this.entityManager = entityManager;
        this.clock = clock;
    }

    DriverReport createReport(Long driverId, int lastDays) {
        DriverReport driverReport = new DriverReport();

        List<Tuple> driverWithAttributes = getDriverWithAttributes(driverId);

        driverWithAttributes.forEach(tuple -> addDriverAttributeToReport(tuple, driverReport));

        DriverDTO driverDTO = retrieveDriver(driverWithAttributes.stream()
                .findFirst()
                .get());
        addDriverToReport(driverDTO, driverReport);

        Stream<Tuple> driverSessionsWithTransits = getDriverSessionsWithTransits(driverId, lastDays);

        driverReport.setSessions(mapToDriverSessions(driverSessionsWithTransits, driverDTO));

        return driverReport;
    }

    @SuppressWarnings("unchecked")
    private List<Tuple> getDriverWithAttributes(Long driverId) {
        return entityManager.createNativeQuery(QUERY_FOR_DRIVER_WITH_ATTRIBUTES, Tuple.class)
                .setParameter("driverId", driverId)
                .setParameter("filteredAttribute", MEDICAL_EXAMINATION_REMARKS.toString())
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    private Stream<Tuple> getDriverSessionsWithTransits(Long driverId, int lastDays) {
        return entityManager.createNativeQuery(QUERY_FOR_DRIVER_SESSIONS_WITH_TRANSITS, Tuple.class)
                .setParameter("driverId", driverId)
                .setParameter("transitStatus", Transit.Status.COMPLETED.ordinal())
                .setParameter("since", calculateStartingPoint(lastDays))
                .getResultStream();
    }

    private Instant calculateStartingPoint(int lastDays) {
        Instant beggingOfToday = Instant.now(clock)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atStartOfDay()
                .toInstant(OffsetDateTime.now().getOffset());

        return beggingOfToday.minus(lastDays, ChronoUnit.DAYS);
    }

    private void addDriverAttributeToReport(Tuple driverAttribute, DriverReport driverReport) {
        driverReport.addAttribute(retrieveDriverAttribute(driverAttribute));
    }

    private void addDriverToReport(DriverDTO driverDTO, DriverReport driverReport) {
        driverReport.setDriverDTO(driverDTO);
    }

    private Map<DriverSessionDTO, List<TransitDTO>> mapToDriverSessions(Stream<Tuple> driverSessionsWithTransits, DriverDTO driverDTO) {
        return driverSessionsWithTransits.collect(
                Collectors.toMap(
                        this::retrieveDrivingSession,
                        tuple -> {
                            List<TransitDTO> transitDTOS = new ArrayList<>();
                            transitDTOS.add(retrieveTransit(tuple, driverDTO));
                            return transitDTOS;
                        },
                        (existingTransitDTOList, newTransitDTOList) -> {
                            Long newTransitDTOId = newTransitDTOList.get(0).getId();

                            boolean isTransitDTOAlreadyAdded = existingTransitDTOList.stream()
                                    .anyMatch(existingTransitDTO -> Objects.equals(newTransitDTOId, existingTransitDTO.getId()));

                            if (isTransitDTOAlreadyAdded) {
                                existingTransitDTOList.stream()
                                        .filter(existingTransitDTO -> Objects.equals(newTransitDTOId, existingTransitDTO.getId()))
                                        .findFirst()
                                        .ifPresent(transitDTO -> transitDTO.getProposedDrivers().addAll(newTransitDTOList.get(0).getProposedDrivers()));
                            } else {
                                existingTransitDTOList.addAll(newTransitDTOList);
                            }

                            return existingTransitDTOList;
                        }
                )
        );
    }

    private List<DriverDTO> retrieveDriverAsList(Tuple driver) {
        List<DriverDTO> driverDTOS = new ArrayList<>();
        driverDTOS.add(retrieveDriver(driver));
        return driverDTOS;
    }

    private DriverDTO retrieveDriver(Tuple driver) {
        return new DriverDTO(
                driver.get("DRIVER_ID") == null ? null : ((Number) driver.get("DRIVER_ID")).longValue(),
                (String) driver.get("DRIVER_FIRST_NAME"),
                (String) driver.get("DRIVER_LAST_NAME"),
                (String) driver.get("DRIVER_LICENSE"),
                (String) driver.get("DRIVER_PHOTO"),
                driver.get("DRIVER_STATUS") == null ? null : Driver.Status.values()[(Integer) driver.get("DRIVER_STATUS")],
                driver.get("DRIVER_TYPE") == null ? null : Driver.Type.values()[(Integer) driver.get("DRIVER_TYPE")]
        );
    }

    private DriverAttributeDTO retrieveDriverAttribute(Tuple driverAttribute) {
        return new DriverAttributeDTO(
                driverAttribute.get("NAME") == null ? null : (DriverAttribute.DriverAttributeName.valueOf((String) driverAttribute.get("NAME"))),
                (String) driverAttribute.get("VALUE")
        );
    }

    private DriverSessionDTO retrieveDrivingSession(Tuple drivingSession) {
        return new DriverSessionDTO(
                drivingSession.get("LOGGED_AT") == null ? null : ((Timestamp) drivingSession.get("LOGGED_AT")).toInstant(),
                drivingSession.get("LOGGED_OUT_AT") == null ? null : ((Timestamp) drivingSession.get("LOGGED_OUT_AT")).toInstant(),
                (String) drivingSession.get("PLATES_NUMBER"),
                drivingSession.get("CAR_CLASS") == null ? null : CarType.CarClass.valueOf((String) drivingSession.get("CAR_CLASS")),
                (String) drivingSession.get("CAR_BRAND")
        );
    }

    private TransitDTO retrieveTransit(Tuple transit, DriverDTO driverDTO) {
        return new TransitDTO(
                transit.get("TRANSIT_ID") == null ? null : ((Number) transit.get("TRANSIT_ID")).longValue(),
                (String) transit.get("TARIFF_NAME"),
                transit.get("TRANSIT_STATUS") == null ? null : Transit.Status.values()[((Integer) transit.get("TRANSIT_STATUS"))],
                driverDTO,
                transit.get("KM") == null ? null : Distance.ofKm(((Number) transit.get("KM")).floatValue()),
                transit.get("KM_RATE") == null ? null : ((Number) transit.get("KM_RATE")).floatValue(),
                transit.get("PRICE") == null ? null : new BigDecimal(((Number) transit.get("PRICE")).intValue()),
                transit.get("DRIVERS_FEE") == null ? null : new BigDecimal(((Number) transit.get("DRIVERS_FEE")).intValue()),
                transit.get("ESTIMATED_PRICE") == null ? null : new BigDecimal(((Number) transit.get("ESTIMATED_PRICE")).intValue()),
                transit.get("BASE_FEE") == null ? null : new BigDecimal(((Number) transit.get("BASE_FEE")).intValue()),
                transit.get("DATE_TIME") == null ? null : ((Timestamp) transit.get("DATE_TIME")).toInstant(),
                transit.get("PUBLISHED") == null ? null : ((Timestamp) transit.get("PUBLISHED")).toInstant(),
                transit.get("ACCEPTED_AT") == null ? null : ((Timestamp) transit.get("ACCEPTED_AT")).toInstant(),
                transit.get("STARTED") == null ? null : ((Timestamp) transit.get("STARTED")).toInstant(),
                transit.get("COMPLETE_AT") == null ? null : ((Timestamp) transit.get("COMPLETE_AT")).toInstant(),
                retrieveClaim(transit),
                retrieveDriverAsList(transit),
                retrieveFromAddress(transit),
                retrieveToAddress(transit),
                transit.get("CAR_TYPE") == null ? null : CarType.CarClass.valueOf((String) transit.get("CAR_TYPE")),
                retrieveClient(transit));
    }

    private ClaimDTO retrieveClaim(Tuple claim) {
        Number claimId = (Number) claim.get("CLAIM_ID");

        if (claimId == null) {
            return null;
        }

        return new ClaimDTO(
                claimId.longValue(),
                claim.get("OWNER_ID") == null ? null : ((Number) claim.get("OWNER_ID")).longValue(),
                claim.get("TRANSIT_ID") == null ? null : ((Number) claim.get("TRANSIT_ID")).longValue(),
                (String) claim.get("REASON"),
                (String) claim.get("INCIDENT_DESCRIPTION"),
                claim.get("CREATION_DATE") == null ? null : ((Timestamp) claim.get("CREATION_DATE")).toInstant(),
                claim.get("COMPLETION_DATE") == null ? null : ((Timestamp) claim.get("COMPLETION_DATE")).toInstant(),
                claim.get("CHANGE_DATE") == null ? null : ((Timestamp) claim.get("CHANGE_DATE")).toInstant(),
                claim.get("COMPLETION_MODE") == null ? null : Claim.CompletionMode.valueOf((String) claim.get("COMPLETION_MODE")),
                claim.get("CLAIM_STATUS") == null ? null : Claim.Status.valueOf((String) claim.get("CLAIM_STATUS")),
                (String) claim.get("CLAIM_NO")
        );
    }

    private AddressDTO retrieveFromAddress(Tuple addressFrom) {
        return new AddressDTO(
                (String) addressFrom.get("AF_COUNTRY"),
                (String) addressFrom.get("AF_DISTRICT"),
                (String) addressFrom.get("AF_CITY"),
                (String) addressFrom.get("AF_STREET"),
                addressFrom.get("AF_NUMBER") == null ? null : ((Integer) addressFrom.get("AF_NUMBER")),
                addressFrom.get("AF_ADDITIONAL_NUMBER") == null ? null : ((Integer) addressFrom.get("AF_ADDITIONAL_NUMBER")),
                (String) addressFrom.get("AF_POSTAL_CODE"),
                (String) addressFrom.get("AF_NAME")
        );
    }

    private AddressDTO retrieveToAddress(Tuple addressTo) {
        return new AddressDTO(
                (String) addressTo.get("ATO_COUNTRY"),
                (String) addressTo.get("ATO_DISTRICT"),
                (String) addressTo.get("ATO_CITY"),
                (String) addressTo.get("ATO_STREET"),
                addressTo.get("ATO_NUMBER") == null ? null : ((Integer) addressTo.get("ATO_NUMBER")),
                addressTo.get("ATO_ADDITIONAL_NUMBER") == null ? null : ((Integer) addressTo.get("ATO_ADDITIONAL_NUMBER")),
                (String) addressTo.get("ATO_POSTAL_CODE"),
                (String) addressTo.get("ATO_NAME")
        );
    }

    private ClientDTO retrieveClient(Tuple client) {
        return new ClientDTO(
                client.get("CLIENT_ID") == null ? null : ((Number) client.get("CLIENT_ID")).longValue(),
                client.get("CLIENT_TYPE") == null ? null : Client.Type.values()[((Integer) client.get("CLIENT_TYPE"))],
                (String) client.get("CLIENT_NAME"),
                (String) client.get("CLIENT_LAST_NAME"),
                client.get("CLIENT_DEFAULT_PAYMENT_TYPE") == null ? null : Client.PaymentType.valueOf((String) client.get("CLIENT_DEFAULT_PAYMENT_TYPE")),
                client.get("CLIENT_CLIENT_TYPE") == null ? null : Client.ClientType.valueOf((String) client.get("CLIENT_CLIENT_TYPE"))
        );
    }
}
