package io.legacyfighter.cabs.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DriverLicenseTest {

    @Test
    void cannotCreateInvalidLicense() {
        // given
        String nullLicense = null;
        String emptyLicense = "";
        String invalidLicense = "invalid license";

        // when
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> ImmutableDriverLicense.builder().license(nullLicense).build());
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> ImmutableDriverLicense.builder().license(emptyLicense).build());
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> ImmutableDriverLicense.builder().license(invalidLicense).build());

        // then

    }

    @Test
    void canCreateValidLicense() {
        // given
        String validLicense = "9AAAA123456AA1AA";

        // when
        DriverLicense driverLicense = ImmutableDriverLicense.builder().license(validLicense).build();

        // then
        assertThat(driverLicense.getLicense()).isEqualTo(validLicense);
    }

    @Test
    void canCreateInvalidLicenseExplicitly() {
        // given
        String invalidLicense = "invalid license";

        // when
        DriverLicense invalidDriverLicense = new InvalidDriverLicense(invalidLicense);

        // then
        assertThat(invalidDriverLicense.getLicense()).isEqualTo(invalidLicense);
    }
}