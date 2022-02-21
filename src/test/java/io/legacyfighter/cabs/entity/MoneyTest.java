package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.money.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    @Test
    void canCreateMoneyFromInteger() {
        // expect
        assertThat(new Money(0).toString()).hasToString("0.00");
        assertThat(new Money(10000).toString()).hasToString("100.00");
        assertThat(new Money(1012).toString()).hasToString("10.12");
    }

    @Test
    void shouldProjectMoneyToInteger() {
        //expect
        assertThat(new Money(0).toInt()).isZero();
        assertThat(new Money(10000).toInt()).isEqualTo(10000);
        assertThat(new Money(1012).toInt()).isEqualTo(1012);
    }

    @Test
    void canAddMoney() {
        //expect
        assertThat(new Money(1000)).isEqualTo(new Money(500).add(new Money(500)));
        assertThat(new Money(1042)).isEqualTo(new Money(1020).add(new Money(22)));
        assertThat(new Money(0)).isEqualTo(new Money(0).add(new Money(0)));
        assertThat(new Money(-2)).isEqualTo(new Money(-4).add(new Money(2)));
    }

    @Test
    void canSubtractMoney() {
        //expect
        assertThat(new Money(50).subtract(new Money(50))).isEqualTo(Money.ZERO);
        assertThat(new Money(998)).isEqualTo(new Money(1020).subtract(new Money(22)));
        assertThat(new Money(-1)).isEqualTo(new Money(2).subtract(new Money(3)));
    }

    @Test
    void canCalculatePercentage() {
        //expect
        assertThat(new Money(10000).percentage(30).toString()).hasToString("30.00");
        assertThat(new Money(8800).percentage(30).toString()).hasToString("26.40");
        assertThat(new Money(8800).percentage(100).toString()).hasToString("88.00");
        assertThat(new Money(8800).percentage(0).toString()).hasToString("0.00");
        assertThat(new Money(4400).percentage(30).toString()).hasToString("13.20");
        assertThat(new Money(100).percentage(30).toString()).hasToString("0.30");
        assertThat(new Money(1).percentage(40).toString()).hasToString("0.00");
    }
}
