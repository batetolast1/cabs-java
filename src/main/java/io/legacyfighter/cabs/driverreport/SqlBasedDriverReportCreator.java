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

        driverWithAttributes.forEach(tuple -> addAttributeToReport(driverReport, tuple));

        driverWithAttributes.stream()
                .findFirst()
                .ifPresent(tuple -> addDriverToReport(driverReport, tuple));

        Stream<Tuple> driverSessionsWithTransits = getDriverSessionsWithTransits(driverId, lastDays);

        driverReport.setSessions(mapToDriverSessions(driverSessionsWithTransits));

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

    private void addAttributeToReport(DriverReport driverReport, Tuple tuple) {
        driverReport.addAttribute(retrieveDriverAttribute(tuple));
    }

    private void addDriverToReport(DriverReport driverReport, Tuple tuple) {
        driverReport.setDriverDTO(retrieveDriver(tuple));
    }

    private Map<DriverSessionDTO, List<TransitDTO>> mapToDriverSessions(Stream<Tuple> driverSessionsWithTransits) {
        return driverSessionsWithTransits.collect(
                Collectors.toMap(
                        this::retrieveDrivingSession,
                        tuple -> {
                            List<TransitDTO> transitDTOS = new ArrayList<>();
                            transitDTOS.add(retrieveTransit(tuple));
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

    private List<DriverDTO> retrieveProposedDrivers(Tuple tuple) {
        List<DriverDTO> driverDTOS = new ArrayList<>();
        driverDTOS.add(retrieveDriver(tuple));
        return driverDTOS;
    }

    private DriverDTO retrieveDriver(Tuple tuple) {
        return new DriverDTO(
                tuple.get("DRIVER_ID") == null ? null : ((Number) tuple.get("DRIVER_ID")).longValue(),
                (String) tuple.get("DRIVER_FIRST_NAME"),
                (String) tuple.get("DRIVER_LAST_NAME"),
                (String) tuple.get("DRIVER_LICENSE"),
                (String) tuple.get("DRIVER_PHOTO"),
                tuple.get("DRIVER_STATUS") == null ? null : Driver.Status.values()[(Integer) tuple.get("DRIVER_STATUS")],
                tuple.get("DRIVER_TYPE") == null ? null : Driver.Type.values()[(Integer) tuple.get("DRIVER_TYPE")]
        );
    }

    private DriverAttributeDTO retrieveDriverAttribute(Tuple tuple) {
        return new DriverAttributeDTO(
                tuple.get("NAME") == null ? null : (DriverAttribute.DriverAttributeName.valueOf((String) tuple.get("NAME"))),
                (String) tuple.get("VALUE")
        );
    }

    private DriverSessionDTO retrieveDrivingSession(Tuple tuple) {
        return new DriverSessionDTO(
                tuple.get("LOGGED_AT") == null ? null : ((Timestamp) tuple.get("LOGGED_AT")).toInstant(),
                tuple.get("LOGGED_OUT_AT") == null ? null : ((Timestamp) tuple.get("LOGGED_OUT_AT")).toInstant(),
                (String) tuple.get("PLATES_NUMBER"),
                tuple.get("CAR_CLASS") == null ? null : CarType.CarClass.valueOf((String) tuple.get("CAR_CLASS")),
                (String) tuple.get("CAR_BRAND")
        );
    }

    private TransitDTO retrieveTransit(Tuple tuple) {
        return new TransitDTO(
                tuple.get("TRANSIT_ID") == null ? null : ((Number) tuple.get("TRANSIT_ID")).longValue(),
                (String) tuple.get("TARIFF_NAME"),
                tuple.get("TRANSIT_STATUS") == null ? null : Transit.Status.values()[((Integer) tuple.get("TRANSIT_STATUS"))],
                null,
                tuple.get("KM") == null ? null : Distance.ofKm(((Number) tuple.get("KM")).floatValue()),
                tuple.get("KM_RATE") == null ? null : ((Number) tuple.get("KM_RATE")).floatValue(),
                tuple.get("PRICE") == null ? null : new BigDecimal(((Number) tuple.get("PRICE")).intValue()),
                tuple.get("DRIVERS_FEE") == null ? null : new BigDecimal(((Number) tuple.get("DRIVERS_FEE")).intValue()),
                tuple.get("ESTIMATED_PRICE") == null ? null : new BigDecimal(((Number) tuple.get("ESTIMATED_PRICE")).intValue()),
                tuple.get("BASE_FEE") == null ? null : new BigDecimal(((Number) tuple.get("BASE_FEE")).intValue()),
                tuple.get("DATE_TIME") == null ? null : ((Timestamp) tuple.get("DATE_TIME")).toInstant(),
                tuple.get("PUBLISHED") == null ? null : ((Timestamp) tuple.get("PUBLISHED")).toInstant(),
                tuple.get("ACCEPTED_AT") == null ? null : ((Timestamp) tuple.get("ACCEPTED_AT")).toInstant(),
                tuple.get("STARTED") == null ? null : ((Timestamp) tuple.get("STARTED")).toInstant(),
                tuple.get("COMPLETE_AT") == null ? null : ((Timestamp) tuple.get("COMPLETE_AT")).toInstant(),
                retrieveClaim(tuple),
                retrieveProposedDrivers(tuple),
                retrieveFromAddress(tuple),
                retrieveToAddress(tuple),
                tuple.get("CAR_TYPE") == null ? null : CarType.CarClass.valueOf((String) tuple.get("CAR_TYPE")),
                retrieveClient(tuple));
    }

    private ClaimDTO retrieveClaim(Tuple tuple) {
        Number claimId = (Number) tuple.get("CLAIM_ID");

        if (claimId == null) {
            return null;
        }

        return new ClaimDTO(
                claimId.longValue(),
                tuple.get("OWNER_ID") == null ? null : ((Number) tuple.get("OWNER_ID")).longValue(),
                tuple.get("TRANSIT_ID") == null ? null : ((Number) tuple.get("TRANSIT_ID")).longValue(),
                (String) tuple.get("REASON"),
                (String) tuple.get("INCIDENT_DESCRIPTION"),
                tuple.get("CREATION_DATE") == null ? null : ((Timestamp) tuple.get("CREATION_DATE")).toInstant(),
                tuple.get("COMPLETION_DATE") == null ? null : ((Timestamp) tuple.get("COMPLETION_DATE")).toInstant(),
                tuple.get("CHANGE_DATE") == null ? null : ((Timestamp) tuple.get("CHANGE_DATE")).toInstant(),
                tuple.get("COMPLETION_MODE") == null ? null : Claim.CompletionMode.valueOf((String) tuple.get("COMPLETION_MODE")),
                tuple.get("CLAIM_STATUS") == null ? null : Claim.Status.valueOf((String) tuple.get("CLAIM_STATUS")),
                (String) tuple.get("CLAIM_NO")
        );
    }

    private AddressDTO retrieveFromAddress(Tuple tuple) {
        return new AddressDTO(
                (String) tuple.get("AF_COUNTRY"),
                (String) tuple.get("AF_DISTRICT"),
                (String) tuple.get("AF_CITY"),
                (String) tuple.get("AF_STREET"),
                tuple.get("AF_NUMBER") == null ? null : ((Integer) tuple.get("AF_NUMBER")),
                tuple.get("AF_ADDITIONAL_NUMBER") == null ? null : ((Integer) tuple.get("AF_ADDITIONAL_NUMBER")),
                (String) tuple.get("AF_POSTAL_CODE"),
                (String) tuple.get("AF_NAME")
        );
    }

    private AddressDTO retrieveToAddress(Tuple tuple) {
        return new AddressDTO(
                (String) tuple.get("ATO_COUNTRY"),
                (String) tuple.get("ATO_DISTRICT"),
                (String) tuple.get("ATO_CITY"),
                (String) tuple.get("ATO_STREET"),
                tuple.get("ATO_NUMBER") == null ? null : ((Integer) tuple.get("ATO_NUMBER")),
                tuple.get("ATO_ADDITIONAL_NUMBER") == null ? null : ((Integer) tuple.get("ATO_ADDITIONAL_NUMBER")),
                (String) tuple.get("ATO_POSTAL_CODE"),
                (String) tuple.get("ATO_NAME")
        );
    }

    private ClientDTO retrieveClient(Tuple tuple) {
        return new ClientDTO(
                tuple.get("CLIENT_ID") == null ? null : ((Number) tuple.get("CLIENT_ID")).longValue(),
                tuple.get("CLIENT_TYPE") == null ? null : Client.Type.values()[((Integer) tuple.get("CLIENT_TYPE"))],
                (String) tuple.get("CLIENT_NAME"),
                (String) tuple.get("CLIENT_LAST_NAME"),
                tuple.get("CLIENT_DEFAULT_PAYMENT_TYPE") == null ? null : Client.PaymentType.valueOf((String) tuple.get("CLIENT_DEFAULT_PAYMENT_TYPE")),
                tuple.get("CLIENT_CLIENT_TYPE") == null ? null : Client.ClientType.valueOf((String) tuple.get("CLIENT_CLIENT_TYPE"))
        );
    }
}
