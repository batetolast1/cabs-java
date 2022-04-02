package io.legacyfighter.cabs.dto;

import io.legacyfighter.cabs.entity.Driver;

import java.util.Objects;

public class DriverDTO {

    private Long id;

    private String firstName;

    private String lastName;

    private String driverLicense;

    private String photo;

    private Driver.Status status;

    private Driver.Type type;

    public DriverDTO(Driver driver) {
        this(driver.getId(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getDriverLicense().asString(),
                driver.getPhoto(),
                driver.getStatus(),
                driver.getType());
    }

    public DriverDTO(Long id,
                     String firstName,
                     String lastName,
                     String driverLicense,
                     String photo,
                     Driver.Status status,
                     Driver.Type type) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.driverLicense = driverLicense;
        this.photo = photo;
        this.status = status;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDriverLicense() {
        return driverLicense;
    }

    public void setDriverLicense(String driverLicense) {
        this.driverLicense = driverLicense;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Driver.Status getStatus() {
        return status;
    }

    public void setStatus(Driver.Status status) {
        this.status = status;
    }

    public Driver.Type getType() {
        return type;
    }

    public void setType(Driver.Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DriverDTO driverDTO = (DriverDTO) o;
        return Objects.equals(id, driverDTO.id) && Objects.equals(firstName, driverDTO.firstName) && Objects.equals(lastName, driverDTO.lastName) && Objects.equals(driverLicense, driverDTO.driverLicense) && Objects.equals(photo, driverDTO.photo) && status == driverDTO.status && type == driverDTO.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, driverLicense, photo, status, type);
    }
}
