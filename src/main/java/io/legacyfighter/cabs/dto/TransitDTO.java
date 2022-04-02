package io.legacyfighter.cabs.dto;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.entity.CarType;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.entity.Transit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TransitDTO {

    private Long id;

    private String tariff;

    private Transit.Status status;

    private DriverDTO driver;

    private Integer factor;

    private Distance distance;

    private String distanceUnit;

    private Float kmRate;

    private BigDecimal price;

    private BigDecimal driverFee;

    private BigDecimal estimatedPrice;

    private BigDecimal baseFee;

    private Instant date;

    private Instant dateTime;

    private Instant published;

    private Instant acceptedAt;

    private Instant started;

    private Instant completeAt;

    private ClaimDTO claimDTO;

    private List<DriverDTO> proposedDrivers = new ArrayList<>();

    private AddressDTO to;

    private AddressDTO from;

    private CarType.CarClass carClass;

    private ClientDTO clientDTO;

    public TransitDTO() {
    }

    public TransitDTO(Transit transit) {
        this(transit.getId(),
                transit.getTariff().getName(),
                transit.getStatus(),
                transit.getDriver() == null ? null : new DriverDTO(transit.getDriver()),
                transit.getKm(),
                transit.getTariff().getKmRate(),
                transit.getPrice() != null ? new BigDecimal(transit.getPrice().toInt()) : null,
                transit.getDriversFee() != null ? new BigDecimal(transit.getDriversFee().toInt()) : null,
                transit.getEstimatedPrice() != null ? new BigDecimal(transit.getEstimatedPrice().toInt()) : null,
                BigDecimal.valueOf(transit.getTariff().getBaseFee()),
                transit.getDateTime(),
                transit.getPublished(),
                transit.getAcceptedAt(),
                transit.getStarted(),
                transit.getCompleteAt(),
                null,
                new ArrayList<>(),
                new AddressDTO(transit.getFrom()),
                new AddressDTO(transit.getTo()),
                transit.getCarType(),
                new ClientDTO(transit.getClient()));

        for (Driver d : transit.getProposedDrivers()) {
            proposedDrivers.add(new DriverDTO(d));
        }
    }

    public TransitDTO(Long id,
                      String tariff,
                      Transit.Status status,
                      DriverDTO driver,
                      Distance distance,
                      Float kmRate,
                      BigDecimal price,
                      BigDecimal driverFee,
                      BigDecimal estimatedPrice,
                      BigDecimal baseFee,
                      Instant dateTime,
                      Instant published,
                      Instant acceptedAt,
                      Instant started,
                      Instant completeAt,
                      ClaimDTO claimDTO,
                      List<DriverDTO> proposedDrivers,
                      AddressDTO from,
                      AddressDTO to,
                      CarType.CarClass carClass,
                      ClientDTO clientDTO) {
        this.id = id;
        this.factor = 1;
        this.tariff = tariff;
        this.status = status;
        this.driver = driver;
        this.distance = distance;
        this.kmRate = kmRate;
        this.price = price;
        this.driverFee = driverFee;
        this.estimatedPrice = estimatedPrice;
        this.baseFee = baseFee;
        this.dateTime = dateTime;
        this.published = published;
        this.acceptedAt = acceptedAt;
        this.started = started;
        this.completeAt = completeAt;
        this.claimDTO = claimDTO;
        this.proposedDrivers = proposedDrivers;
        this.to = to;
        this.from = from;
        this.carClass = carClass;
        this.clientDTO = clientDTO;
    }

    public float getKmRate() {
        return kmRate;
    }

    private void setTariff(Transit transit) {
        this.tariff = transit.getTariff().getName();
        this.kmRate = transit.getTariff().getKmRate();
        this.baseFee = BigDecimal.valueOf(transit.getTariff().getBaseFee());
    }

    public String getTariff() {
        return tariff;
    }

    public String getDistance(String unit) {
        this.distanceUnit = unit;
        return distance.printIn(unit);
    }

    public List<DriverDTO> getProposedDrivers() {
        return proposedDrivers;
    }

    public void setProposedDrivers(List<DriverDTO> proposedDrivers) {
        this.proposedDrivers = proposedDrivers;
    }

    public ClaimDTO getClaimDTO() {
        return claimDTO;
    }

    public void setClaimDTO(ClaimDTO claimDTO) {
        this.claimDTO = claimDTO;
    }

    public AddressDTO getTo() {
        return to;
    }

    public void setTo(AddressDTO to) {
        this.to = to;
    }

    public AddressDTO getFrom() {
        return from;
    }

    public void setFrom(AddressDTO from) {
        this.from = from;
    }

    public CarType.CarClass getCarClass() {
        return carClass;
    }

    public void setCarClass(CarType.CarClass carClass) {
        this.carClass = carClass;
    }

    public ClientDTO getClientDTO() {
        return clientDTO;
    }

    public void setClientDTO(ClientDTO clientDTO) {
        this.clientDTO = clientDTO;
    }

    public Long getId() {
        return id;
    }

    public Transit.Status getStatus() {
        return status;
    }

    public void setStatus(Transit.Status status) {
        this.status = status;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getDriverFee() {
        return driverFee;
    }

    public void setDriverFee(BigDecimal driverFee) {
        this.driverFee = driverFee;
    }

    public Instant getDateTime() {
        return dateTime;
    }

    public void setDateTime(Instant dateTime) {
        this.dateTime = dateTime;
    }

    public Instant getPublished() {
        return published;
    }

    public void setPublished(Instant published) {
        this.published = published;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getStarted() {
        return started;
    }

    public void setStarted(Instant started) {
        this.started = started;
    }

    public Instant getCompleteAt() {
        return completeAt;
    }

    public void setCompleteAt(Instant completeAt) {
        this.completeAt = completeAt;
    }

    public BigDecimal getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(BigDecimal estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public DriverDTO getDriver() {
        return driver;
    }

    public void setDriver(DriverDTO driver) {
        this.driver = driver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransitDTO that = (TransitDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(tariff, that.tariff) && status == that.status && Objects.equals(driver, that.driver) && Objects.equals(factor, that.factor) && Objects.equals(distance, that.distance) && Objects.equals(distanceUnit, that.distanceUnit) && Objects.equals(kmRate, that.kmRate) && Objects.equals(price, that.price) && Objects.equals(driverFee, that.driverFee) && Objects.equals(estimatedPrice, that.estimatedPrice) && Objects.equals(baseFee, that.baseFee) && Objects.equals(date, that.date) && Objects.equals(dateTime, that.dateTime) && Objects.equals(published, that.published) && Objects.equals(acceptedAt, that.acceptedAt) && Objects.equals(started, that.started) && Objects.equals(completeAt, that.completeAt) && Objects.equals(claimDTO, that.claimDTO) && areEqual(proposedDrivers, that.proposedDrivers) && Objects.equals(to, that.to) && Objects.equals(from, that.from) && carClass == that.carClass && Objects.equals(clientDTO, that.clientDTO);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tariff, status, driver, factor, distance, distanceUnit, kmRate, price, driverFee, estimatedPrice, baseFee, date, dateTime, published, acceptedAt, started, completeAt, claimDTO, proposedDrivers, to, from, carClass, clientDTO);
    }

    private boolean areEqual(List<DriverDTO> first, List<DriverDTO> second) {
        return first.containsAll(second) && second.containsAll(first);
    }
}
