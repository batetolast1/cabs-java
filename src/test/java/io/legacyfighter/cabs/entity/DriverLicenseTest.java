package io.legacyfighter.cabs.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DriverLicenseTest {

    @Test
    void cannotCreateInvalidLicense() {
        // given
        String emptyLicense = "";
        String invalidLicense = "invalid license";

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> DriverLicense.withLicense(null));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> DriverLicense.withLicense(emptyLicense));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> DriverLicense.withLicense(invalidLicense));
    }

    @Test
    void canCreateValidLicense() {
        // given
        String validLicense = "9AAAA123456AA1AA";

        // when
        DriverLicense driverLicense = DriverLicense.withLicense(validLicense);

        // then
        assertThat(driverLicense.asString()).isEqualTo(validLicense);
    }

    @Test
    void canCreateInvalidLicenseExplicitly() {
        // given
        String invalidLicense = "invalid license";

        // when
        DriverLicense invalidDriverLicense = DriverLicense.withoutValidation(invalidLicense);

        // then
        assertThat(invalidDriverLicense.asString()).isEqualTo(invalidLicense);
    }
}