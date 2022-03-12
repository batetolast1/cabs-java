package io.legacyfighter.cabs.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class CarTypeActiveCounter {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Id
    private CarType.CarClass carClass;

    @Column(nullable = false)
    private int activeCarsCounter;

    public CarTypeActiveCounter() {
    }

    public CarTypeActiveCounter(CarType.CarClass carClass) {
        this.carClass = carClass;
    }

    public int getActiveCarsCounter() {
        return activeCarsCounter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarTypeActiveCounter that = (CarTypeActiveCounter) o;
        return activeCarsCounter == that.activeCarsCounter && carClass == that.carClass;
    }

    @Override
    public int hashCode() {
        return Objects.hash(carClass, activeCarsCounter);
    }
}
