package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.common.Dates;
import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.dto.TransitDTO;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.ui.TransitController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TariffRecognizingIntegrationTest {

    @Autowired
    Fixtures fixtures;

    @Autowired
    TransitController transitController;

    @Test
    void newYearsEveTariffShouldBeDisplayed() {
        // given
        Transit transit = fixtures.aCompletedTransitAt(Dates.NEW_YEARS_EVE);

        // when
        TransitDTO transitDTO = transitController.getTransit(transit.getId());

        // then
        assertThat(transitDTO.getKmRate()).isEqualTo(3.50f);
        assertThat(transitDTO.getTariff()).isEqualTo("Sylwester");
    }

    @Test
    void weekendTariffShouldBeDisplayed() {
        // given
        Transit transit = fixtures.aCompletedTransitAt(Dates.WEEKEND);

        // when
        TransitDTO transitDTO = transitController.getTransit(transit.getId());

        // then
        assertThat(transitDTO.getKmRate()).isEqualTo(1.50f);
        assertThat(transitDTO.getTariff()).isEqualTo("Weekend");
    }

    @Test
    void weekendPlusTariffShouldBeDisplayed() {
        // given
        Transit transit = fixtures.aCompletedTransitAt(Dates.WEEKEND_PLUS);

        // when
        TransitDTO transitDTO = transitController.getTransit(transit.getId());

        // then
        assertThat(transitDTO.getKmRate()).isEqualTo(2.50f);
        assertThat(transitDTO.getTariff()).isEqualTo("Weekend+");
    }

    @Test
    void standardTariffShouldBeDisplayed() {
        // given
        Transit transit = fixtures.aCompletedTransitAt(Dates.STANDARD_DAY);

        // when
        TransitDTO transitDTO = transitController.getTransit(transit.getId());

        // then
        assertThat(transitDTO.getKmRate()).isEqualTo(1.00f);
        assertThat(transitDTO.getTariff()).isEqualTo("Standard");
    }

    @Test
    void standardTariffShouldBeDisplayedBefore2019() {
        // given
        Transit transit = fixtures.aCompletedTransitAt(Dates.BEFORE_2019);

        // when
        TransitDTO transitDTO = transitController.getTransit(transit.getId());

        // then
        assertThat(transitDTO.getKmRate()).isEqualTo(1.00f);
        assertThat(transitDTO.getTariff()).isEqualTo("Standard");
    }
}
