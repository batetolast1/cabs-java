package io.legacyfighter.cabs.entity;

import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.util.regex.Pattern;

@Value.Immutable
interface DriverLicense {

    Pattern DRIVER_LICENSE_PATTERN = Pattern.compile("^[A-Z9]{5}\\d{6}[A-Z9]{2}\\d[A-Z]{2}$");

    String getLicense();

    @Value.Check
    default void check() {
        Preconditions.checkNotNull(getLicense(), "Driver licence must not be null");
        Preconditions.checkArgument(DRIVER_LICENSE_PATTERN.matcher(getLicense()).matches(), "Driver licence must match licence pattern");
    }
}
