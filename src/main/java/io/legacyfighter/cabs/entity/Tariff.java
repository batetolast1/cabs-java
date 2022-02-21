package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.money.Money;

import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;

@Embeddable
public class Tariff {

    private static final Integer BASE_FEE = 8;

    private Float kmRate;

    private String name;

    private Integer baseFee;

    public Tariff() {
    }

    private Tariff(float kmRate, String name, Integer baseFee) {
        this.kmRate = kmRate;
        this.name = name;
        this.baseFee = baseFee;
    }

    public static Tariff ofTime(LocalDateTime dateTime) {
        if ((dateTime.getMonth() == Month.DECEMBER && dateTime.getDayOfMonth() == 31) ||
                (dateTime.getMonth() == Month.JANUARY && dateTime.getDayOfMonth() == 1 && dateTime.getHour() <= 6)) {
            return new Tariff(3.50f, "Sylwester", BASE_FEE + 3);
        } else {
            // piątek i sobota po 17 do 6 następnego dnia
            if ((dateTime.getDayOfWeek() == DayOfWeek.FRIDAY && dateTime.getHour() >= 17) ||
                    (dateTime.getDayOfWeek() == DayOfWeek.SATURDAY && dateTime.getHour() <= 6) ||
                    (dateTime.getDayOfWeek() == DayOfWeek.SATURDAY && dateTime.getHour() >= 17) ||
                    (dateTime.getDayOfWeek() == DayOfWeek.SUNDAY && dateTime.getHour() <= 6)) {
                return new Tariff(2.50f, "Weekend+", BASE_FEE + 2);
            } else {
                // pozostałe godziny weekendu
                if (dateTime.getDayOfWeek() == DayOfWeek.SATURDAY ||
                        dateTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    return new Tariff(1.5f, "Weekend", BASE_FEE);
                } else {
                    // tydzień roboczy
                    return new Tariff(1.0f, "Standard", BASE_FEE + 1);
                }
            }
        }
    }

    public Money calculateCost(Distance distance) {
        BigDecimal priceBigDecimal = BigDecimal.valueOf(distance.toKmInFloat() * kmRate + baseFee)
                .setScale(2, RoundingMode.HALF_UP);

        return new Money(Integer.parseInt(String.valueOf(priceBigDecimal).replace(".", "")));
    }

    public Float getKmRate() {
        return kmRate;
    }

    public void setKmRate(Float kmRate) {
        this.kmRate = kmRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBaseFee() {
        return baseFee;
    }

    public void setBaseFee(Integer baseFee) {
        this.baseFee = baseFee;
    }
}
