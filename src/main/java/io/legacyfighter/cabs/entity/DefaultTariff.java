package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;

public class DefaultTariff implements Tariff {

    private static final Integer BASE_FEE = 8;

    private Float kmRate;

    private String name;

    private Integer baseFee;

    public DefaultTariff() {
        // for Jackson
    }

    private DefaultTariff(float kmRate, String name, Integer baseFee) {
        this.kmRate = kmRate;
        this.name = name;
        this.baseFee = baseFee;
    }

    public static Tariff ofTime(LocalDateTime dateTime) {
        Tariff tariff;
        if ((dateTime.getMonth() == Month.DECEMBER && dateTime.getDayOfMonth() == 31) ||
                (dateTime.getMonth() == Month.JANUARY && dateTime.getDayOfMonth() == 1 && dateTime.getHour() <= 6)) {
            tariff = new DefaultTariff(3.50f, "Sylwester", BASE_FEE + 3);
        } else {
            // piątek i sobota po 17 do 6 następnego dnia
            if ((dateTime.getDayOfWeek() == DayOfWeek.FRIDAY && dateTime.getHour() >= 17) ||
                    (dateTime.getDayOfWeek() == DayOfWeek.SATURDAY && dateTime.getHour() <= 6) ||
                    (dateTime.getDayOfWeek() == DayOfWeek.SATURDAY && dateTime.getHour() >= 17) ||
                    (dateTime.getDayOfWeek() == DayOfWeek.SUNDAY && dateTime.getHour() <= 6)) {
                tariff = new DefaultTariff(2.50f, "Weekend+", BASE_FEE + 2);
            } else {
                // pozostałe godziny weekendu
                if (dateTime.getDayOfWeek() == DayOfWeek.SATURDAY ||
                        dateTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    tariff = new DefaultTariff(1.5f, "Weekend", BASE_FEE);
                } else {
                    // tydzień roboczy
                    tariff = new DefaultTariff(1.0f, "Standard", BASE_FEE + 1);
                }
            }
        }

        boolean isHappyHour = dateTime.getHour() == 16;
        if (isHappyHour) {
            return DiscountedTariff.discounted(tariff, "HAPPY HOURS", 90);
        }

        return tariff;
    }

    public Money calculateCost(Distance distance) {
        BigDecimal priceBigDecimal = BigDecimal.valueOf(distance.toKmInFloat() * kmRate + baseFee)
                .setScale(2, RoundingMode.HALF_UP);

        return new Money(Integer.parseInt(String.valueOf(priceBigDecimal).replace(".", "")));
    }

    public Float getKmRate() {
        return kmRate;
    }

    public String getName() {
        return name;
    }

    public Integer getBaseFee() {
        return baseFee;
    }
}
