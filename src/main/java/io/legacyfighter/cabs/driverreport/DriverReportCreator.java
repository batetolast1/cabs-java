package io.legacyfighter.cabs.driverreport;

import io.legacyfighter.cabs.config.FeatureFlags;
import io.legacyfighter.cabs.dto.DriverReport;
import org.springframework.stereotype.Service;

@Service
class DriverReportCreator {

    private final SqlBasedDriverReportCreator sqlBasedDriverReportCreator;

    private final OldDriverReportCreator oldDriverReportCreator;

    private final DriverReportReconciliation driverReportReconciliation;

    DriverReportCreator(SqlBasedDriverReportCreator sqlBasedDriverReportCreator,
                        OldDriverReportCreator oldDriverReportCreator,
                        DriverReportReconciliation driverReportReconciliation) {
        this.sqlBasedDriverReportCreator = sqlBasedDriverReportCreator;
        this.oldDriverReportCreator = oldDriverReportCreator;
        this.driverReportReconciliation = driverReportReconciliation;
    }

    DriverReport create(Long driverId, int lastDays) {
        DriverReport oldDriverReport = null;
        DriverReport sqlBasedDriverReport = null;

        if (shouldCompare()) {
            oldDriverReport = oldDriverReportCreator.createReport(driverId, lastDays);
            sqlBasedDriverReport = sqlBasedDriverReportCreator.createReport(driverId, lastDays);

            driverReportReconciliation.compare(oldDriverReport, sqlBasedDriverReport);
        }

        if (shouldUseNewReport()) {
            if (sqlBasedDriverReport == null) {
                sqlBasedDriverReport = sqlBasedDriverReportCreator.createReport(driverId, lastDays);
            }
            return sqlBasedDriverReport;
        }

        if (oldDriverReport == null) {
            oldDriverReport = oldDriverReportCreator.createReport(driverId, lastDays);
        }
        return oldDriverReport;
    }

    private boolean shouldUseNewReport() {
        return FeatureFlags.DRIVER_REPORT_SQL.isActive();
    }

    private boolean shouldCompare() {
        return FeatureFlags.DRIVER_REPORT_CREATION_RECONCILIATION.isActive();
    }
}
