package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.money.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DriverFeeTest {

    @Test
    void shouldCalculateDriverFlatFee() {
        // given
        DriverFee driverFee = new DriverFee();
        driverFee.setFeeType(DriverFee.FeeType.FLAT);
        driverFee.setAmount(50);
        driverFee.setMin(new Money(0));

        // when
        Money fee = driverFee.calculateDriverFee(new Money(1900));

        // then
        assertThat(fee).isEqualTo(new Money(1850));
    }

    @Test
    void shouldCalculateDriverPercentageFee() {
        // given
        DriverFee driverFee = new DriverFee();
        driverFee.setFeeType(DriverFee.FeeType.PERCENTAGE);
        driverFee.setAmount(50);
        driverFee.setMin(new Money(0));

        // when
        Money fee = driverFee.calculateDriverFee(new Money(1900));

        // then
        assertThat(fee).isEqualTo(new Money(950));
    }

    @Test
    void shouldUseMinimumFee() {
        // given
        DriverFee driverFee = new DriverFee();
        driverFee.setFeeType(DriverFee.FeeType.PERCENTAGE);
        driverFee.setAmount(0);
        driverFee.setMin(new Money(1000));

        // when
        Money fee = driverFee.calculateDriverFee(new Money(1900));

        // then
        assertThat(fee).isEqualTo(new Money(1000));
    }
}
