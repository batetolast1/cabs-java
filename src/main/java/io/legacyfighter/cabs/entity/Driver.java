package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.common.BaseEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Driver extends BaseEntity {

    public enum Type {
        CANDIDATE, REGULAR;
    }

    public enum Status {
        ACTIVE, INACTIVE;
    }

    private Type type;

    @Column(nullable = false)
    private Status status;

    private String firstName;

    private String lastName;

    private String photo;

    @Embedded
    private DriverLicense driverLicense;

    @OneToOne
    private DriverFee fee;

    private boolean isOccupied;

    @OneToMany(mappedBy = "driver")
    private Set<DriverAttribute> attributes = new HashSet<>();

    @OneToMany(mappedBy = "driver")
    private Set<Transit> transits = new HashSet<>();

    public Driver() {
        // for JPA
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public DriverLicense getDriverLicense() {
        return driverLicense;
    }

    public void setDriverLicense(DriverLicense driverLicense) {
        this.driverLicense = driverLicense;
    }

    public DriverFee getFee() {
        return fee;
    }

    public void setFee(DriverFee fee) {
        this.fee = fee;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public Set<DriverAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<DriverAttribute> attributes) {
        this.attributes = attributes;
    }

    public Set<Transit> getTransits() {
        return transits;
    }

    public void setTransits(Set<Transit> transits) {
        this.transits = transits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Driver))
            return false;

        Driver other = (Driver) o;

        return this.getId() != null &&
                this.getId().equals(other.getId());
    }

    public BigDecimal calculateEarningsForTransit(Transit transit) {
        return null;
        // zdublować kod wyliczenia kosztu przejazdu
    }

    // for test only
    public static Driver withId(Long id) {
        Driver driver = new Driver();
        driver.id = id;
        return driver;
    }
}
