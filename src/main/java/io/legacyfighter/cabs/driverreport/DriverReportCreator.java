package io.legacyfighter.cabs.driverreport;

import io.legacyfighter.cabs.dto.DriverReport;
import org.springframework.stereotype.Service;

import static io.legacyfighter.cabs.config.FeatureFlags.DRIVER_REPORT_SQL;

@Service
class DriverReportCreator {

    private final SqlBasedDriverReportCreator sqlBasedDriverReportCreator;

    private final OldDriverReportCreator oldDriverReportCreator;

    DriverReportCreator(SqlBasedDriverReportCreator sqlBasedDriverReportCreator,
                        OldDriverReportCreator oldDriverReportCreator) {
        this.sqlBasedDriverReportCreator = sqlBasedDriverReportCreator;
        this.oldDriverReportCreator = oldDriverReportCreator;
    }

    DriverReport create(Long driverId, int lastDays) {
        if (shouldUseNewReport()) {
            return sqlBasedDriverReportCreator.createReport(driverId, lastDays);
        }
        return oldDriverReportCreator.createReport(driverId, lastDays);
    }

    private boolean shouldUseNewReport() {
        return DRIVER_REPORT_SQL.isActive();
    }
}
