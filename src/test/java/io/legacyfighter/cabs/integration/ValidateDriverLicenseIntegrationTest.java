package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.dto.DriverDTO;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.service.DriverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static io.legacyfighter.cabs.entity.Driver.Status.ACTIVE;
import static io.legacyfighter.cabs.entity.Driver.Status.INACTIVE;
import static io.legacyfighter.cabs.entity.Driver.Type.REGULAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@Transactional
class ValidateDriverLicenseIntegrationTest {

    @Autowired
    private DriverService driverService;

    @Test
    void canCreateActiveDriverWithValidLicense() {
        // given
        String validLicense = "9AAAA123456AA1AA";

        // when
        Driver driver = createActiveDriverWithLicense(validLicense);

        // then
        DriverDTO loadedDriver = load(driver);
        assertThat(loadedDriver.getDriverLicense()).isEqualTo(validLicense);
        assertThat(loadedDriver.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    void canCreateInactiveDriverWithInvalidLicense() {
        // given
        String invalidLicense = "invalid license";

        // when
        Driver driver = createInactiveDriverWithLicense(invalidLicense);

        // then
        DriverDTO loadedDriver = load(driver);
        assertThat(loadedDriver.getDriverLicense()).isEqualTo(invalidLicense);
        assertThat(loadedDriver.getStatus()).isEqualTo(INACTIVE);
    }

    @Test
    void canChangeLicenseForValidOne() {
        // given
        String validLicense = "9AAAA123456AA1AA";
        Driver driver = createActiveDriverWithLicense(validLicense);

        String validLicense2 = "AAAAA123456AA1AA";

        // when
        changeLicenseTo(validLicense2, driver);

        // then
        DriverDTO loadedDriver = load(driver);
        assertThat(loadedDriver.getDriverLicense()).isEqualTo(validLicense2);
    }

    @Test
    void cannotChangeLicenseForInvalidOne() {
        // given
        String validLicense = "9AAAA123456AA1AA";
        Driver driver = createActiveDriverWithLicense(validLicense);

        String invalidLicense = "invalid license";

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> changeLicenseTo(invalidLicense, driver));
    }

    @Test
    void canActivateDriverWithValidLicense() {
        // given
        String validLicense = "9AAAA123456AA1AA";
        Driver driver = createInactiveDriverWithLicense(validLicense);

        // when
        activate(driver);

        // then
        DriverDTO loadedDriver = load(driver);
        assertThat(loadedDriver.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    void cannotActivateDriverWithInvalidLicense() {
        // given
        String invalidLicense = "invalid license";
        Driver driver = createInactiveDriverWithLicense(invalidLicense);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> activate(driver));
    }

    Driver createActiveDriverWithLicense(String license) {
        return driverService.createDriver(license, "last name", "first name", REGULAR, ACTIVE, "photo");
    }

    Driver createInactiveDriverWithLicense(String license) {
        return driverService.createDriver(license, "last name", "first name", REGULAR, INACTIVE, "photo");
    }

    DriverDTO load(Driver driver) {
        return driverService.loadDriver(driver.getId());
    }

    void changeLicenseTo(String newLicense, Driver driver) {
        driverService.changeLicenseNumber(newLicense, driver.getId());
    }

    void activate(Driver driver) {
        driverService.changeDriverStatus(driver.getId(), ACTIVE);
    }
}