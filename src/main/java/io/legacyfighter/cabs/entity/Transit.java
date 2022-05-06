package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.common.BaseEntity;
import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.money.Money;

import javax.persistence.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static io.legacyfighter.cabs.distance.Distance.ofKm;

@Entity
public class Transit extends BaseEntity {

    public enum Status {
        DRAFT,
        CANCELLED,
        WAITING_FOR_DRIVER_ASSIGNMENT,
        DRIVER_ASSIGNMENT_FAILED,
        TRANSIT_TO_PASSENGER,
        IN_TRANSIT,
        COMPLETED
    }

    public enum DriverPaymentStatus {
        NOT_PAID, PAID, CLAIMED, RETURNED;
    }

    public enum ClientPaymentStatus {
        NOT_PAID, PAID, RETURNED;
    }

    private DriverPaymentStatus driverPaymentStatus;

    private ClientPaymentStatus clientPaymentStatus;

    private Client.PaymentType paymentType;

    private Status status;

    private Instant date;

    @OneToOne
    private Address from;

    @OneToOne
    public Address to;

    public Integer pickupAddressChangeCounter = 0;

    @ManyToOne
    public Driver driver;

    public Instant acceptedAt;

    public Instant started;

    @ManyToMany
    public Set<Driver> driversRejections = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "transit_proposed_driver",
            joinColumns = @JoinColumn(name = "transit_id"),
            inverseJoinColumns = @JoinColumn(name = "proposed_driver_id")
    )
    public Set<Driver> proposedDrivers = new HashSet<>();

    public Integer awaitingDriversResponses = 0;

    private String tariffJson;

    private float km;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "price"))
    private Money price;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "estimatedPrice"))
    private Money estimatedPrice;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "driversFee"))
    private Money driversFee;

    private Instant dateTime;

    private Instant published;

    @OneToOne
    public Client client;

    @Enumerated(EnumType.STRING)
    private CarType.CarClass carType;

    private Instant completeAt;

    public Transit() {
    }

    public Transit(Long id) {
        this.id = id;
    }

    public Transit(Address from,
                   Address to,
                   Client client,
                   CarType.CarClass carClass,
                   Instant date,
                   Distance km) {
        this.client = client;
        this.from = from;
        this.to = to;
        this.carType = carClass;
        this.status = Status.DRAFT;
        this.tariffJson = TariffJsonMapper.serialize(DefaultTariff.ofTime(date.atZone(ZoneId.systemDefault()).toLocalDateTime()));
        this.dateTime = date;
        this.km = km.toKmInFloat();
        this.estimatedPrice = this.getTariff().calculateCost(ofKm(this.km));
    }

    public void changePickupTo(Address newAddress, Distance newDistance, double distanceFromPreviousPickup) {
        if (distanceFromPreviousPickup > 0.25) {
            throw new IllegalStateException("Address 'from' cannot be changed, id = " + getId());
        } else if (this.pickupAddressChangeCounter > 2) {
            throw new IllegalStateException("Address 'from' cannot be changed, id = " + getId());
        } else if (!(this.status.equals(Status.DRAFT) || (this.status.equals(Status.WAITING_FOR_DRIVER_ASSIGNMENT)))) {
            throw new IllegalStateException("Address 'from' cannot be changed, id = " + getId());
        }

        this.from = newAddress;
        this.km = newDistance.toKmInFloat();
        this.estimatedPrice = this.getTariff().calculateCost(ofKm(this.km));
        this.pickupAddressChangeCounter++;
    }

    public void changeDestinationTo(Address newAddress, Distance newDistance) {
        if (status.equals(Transit.Status.COMPLETED)) {
            throw new IllegalStateException("Address 'to' cannot be changed, id = " + getId());
        }

        this.to = newAddress;
        this.km = newDistance.toKmInFloat();
        this.estimatedPrice = this.getTariff().calculateCost(ofKm(this.km));
    }

    public boolean canCancel() {
        if (!EnumSet.of(Transit.Status.DRAFT, Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT, Transit.Status.TRANSIT_TO_PASSENGER).contains(this.status)) {
            throw new IllegalStateException("Transit cannot be cancelled, id = " + getId());
        }
        return true;
    }

    public void cancel() {
        this.status = Status.CANCELLED;
        this.driver = null;
        this.km = Distance.ZERO.toKmInFloat();
        this.estimatedPrice = this.getTariff().calculateCost(ofKm(this.km));
        this.price = null;
        this.awaitingDriversResponses = 0;
    }

    public void publishAt(Instant date) {
        this.status = Status.WAITING_FOR_DRIVER_ASSIGNMENT;
        this.published = date;
    }

    public boolean shouldNotWaitForDriverAnyMore(Instant date) {
        return this.published.plus(300, ChronoUnit.SECONDS).isBefore(date) || this.status.equals(Transit.Status.CANCELLED);
    }

    public void failDriverAssignment() {
        this.status = Status.DRIVER_ASSIGNMENT_FAILED;
        this.driver = null;
        this.km = Distance.ZERO.toKmInFloat();
        this.estimatedPrice = this.getTariff().calculateCost(ofKm(this.km));
        this.awaitingDriversResponses = 0;
    }

    public boolean canProposeTo(Driver driver) {
        return !this.driversRejections.contains(driver);
    }

    public void proposeTo(Driver driver) {
        this.proposedDrivers.add(driver);
        this.awaitingDriversResponses++;
    }

    public void acceptBy(Driver driver, Instant date) {
        if (this.driver != null) {
            throw new IllegalStateException("Transit already accepted, id = " + getId());
        }
        if (!this.proposedDrivers.contains(driver)) {
            throw new IllegalStateException("Driver out of possible drivers, id = " + getId());
        }
        if (this.driversRejections.contains(driver)) {
            throw new IllegalStateException("Driver out of possible drivers, id = " + getId());
        }

        this.driver = driver;
        this.awaitingDriversResponses = 0;
        this.acceptedAt = date;
        this.status = Status.TRANSIT_TO_PASSENGER;
    }

    public void startAt(Instant date) {
        if (!this.status.equals(Transit.Status.TRANSIT_TO_PASSENGER)) {
            throw new IllegalStateException("Transit cannot be started, id = " + getId());
        }

        this.status = Status.IN_TRANSIT;
        this.started = date;
    }

    public void rejectBy(Driver driver) {
        this.driversRejections.add(driver);
        this.awaitingDriversResponses--;
    }

    public void completeAt(Instant date, Address destinationAddress, Distance distance) {
        if (!this.status.equals(Transit.Status.IN_TRANSIT)) {
            throw new IllegalArgumentException("Cannot complete Transit, id = " + getId());
        }

        Money money = this.getTariff().calculateCost(ofKm(this.km));
        this.to = destinationAddress;
        this.km = distance.toKmInFloat();
        this.estimatedPrice = money;
        this.status = Status.COMPLETED;
        this.price = money;
        this.completeAt = date;
    }

    public CarType.CarClass getCarType() {
        return carType;
    }

    public Money estimateCost() {
        if (status.equals(Status.COMPLETED)) {
            throw new IllegalStateException("Estimating cost for completed transit is forbidden, id = " + this.getId());
        }

        this.estimatedPrice = calculateCost();
        this.price = null;

        return estimatedPrice;
    }

    public Money calculateFinalCosts() {
        if (status.equals(Status.COMPLETED)) {
            return calculateCost();
        } else {
            throw new IllegalStateException("Cannot calculate final cost if the transit is not completed");
        }
    }

    private Money calculateCost() {
        Money money = this.getTariff().calculateCost(ofKm(km));
        this.price = money;
        return money;
    }

    public Driver getDriver() {
        return driver;
    }

    public Money getPrice() {
        return price;
    }

    public Status getStatus() {
        return status;
    }

    public Instant getCompleteAt() {
        return completeAt;
    }

    public Client getClient() {
        return client;
    }

    public Instant getDateTime() {
        return dateTime;
    }

    public Instant getPublished() {
        return published;
    }

    public Distance getKm() {
        return Distance.ofKm(km);
    }

    public Integer getAwaitingDriversResponses() {
        return awaitingDriversResponses;
    }

    public Set<Driver> getDriversRejections() {
        return driversRejections;
    }

    public Set<Driver> getProposedDrivers() {
        return proposedDrivers;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public Instant getStarted() {
        return started;
    }

    public Address getFrom() {
        return from;
    }

    public Address getTo() {
        return to;
    }

    public Integer getPickupAddressChangeCounter() {
        return pickupAddressChangeCounter;
    }

    public Money getDriversFee() {
        return driversFee;
    }

    public Money getEstimatedPrice() {
        return estimatedPrice;
    }

    public Tariff getTariff() {
        return TariffJsonMapper.deserialize(tariffJson);
    }

    public void setDriversFee(Money driversFee) {
        this.driversFee = driversFee;
    }

    /**
     * for testing only
     *
     * @param price
     */
    public void setPrice(Money price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Transit))
            return false;

        Transit other = (Transit) o;

        return this.getId() != null &&
                this.getId().equals(other.getId());
    }
}
