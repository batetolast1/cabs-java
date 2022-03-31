package io.legacyfighter.cabs.dto;

import io.legacyfighter.cabs.entity.CarType;
import io.legacyfighter.cabs.entity.DriverSession;

import java.time.Instant;
import java.util.Objects;

public class DriverSessionDTO {

    private Instant loggedAt;

    private Instant loggedOutAt;

    private String platesNumber;

    private CarType.CarClass carClass;

    private String carBrand;

    public DriverSessionDTO() {
    }

    public DriverSessionDTO(DriverSession session) {
        this.carBrand = session.getCarBrand();
        this.platesNumber = session.getPlatesNumber();
        this.loggedAt = session.getLoggedAt();
        this.loggedOutAt = session.getLoggedOutAt();
        this.carClass = session.getCarClass();
    }

    public DriverSessionDTO(Instant loggedAt,
                            Instant loggedOutAt,
                            String platesNumber,
                            CarType.CarClass carClass,
                            String carBrand) {
        this.loggedAt = loggedAt;
        this.loggedOutAt = loggedOutAt;
        this.platesNumber = platesNumber;
        this.carClass = carClass;
        this.carBrand = carBrand;
    }

    public String getCarBrand() {
        return carBrand;
    }

    public void setCarBrand(String carBrand) {
        this.carBrand = carBrand;
    }

    public Instant getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(Instant loggedAt) {
        this.loggedAt = loggedAt;
    }

    public Instant getLoggedOutAt() {
        return loggedOutAt;
    }

    public void setLoggedOutAt(Instant loggedOutAt) {
        this.loggedOutAt = loggedOutAt;
    }

    public String getPlatesNumber() {
        return platesNumber;
    }

    public void setPlatesNumber(String platesNumber) {
        this.platesNumber = platesNumber;
    }

    public CarType.CarClass getCarClass() {
        return carClass;
    }

    public void setCarClass(CarType.CarClass carClass) {
        this.carClass = carClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DriverSessionDTO that = (DriverSessionDTO) o;
        return Objects.equals(loggedAt, that.loggedAt) && Objects.equals(loggedOutAt, that.loggedOutAt) && Objects.equals(platesNumber, that.platesNumber) && carClass == that.carClass && Objects.equals(carBrand, that.carBrand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loggedAt, loggedOutAt, platesNumber, carClass, carBrand);
    }
}
