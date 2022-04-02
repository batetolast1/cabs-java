package io.legacyfighter.cabs.driverreport;

import io.legacyfighter.cabs.dto.DriverReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
class LoggingDriverReportReconciliation implements DriverReportReconciliation {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingDriverReportReconciliation.class);

    @Override
    public void compare(DriverReport oldOne, DriverReport newOne) {
        LOGGER.info("Is old report equal to the new one: {}", Objects.equals(oldOne, newOne));
    }
}
