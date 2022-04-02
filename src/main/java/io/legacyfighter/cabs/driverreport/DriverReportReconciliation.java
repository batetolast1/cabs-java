package io.legacyfighter.cabs.driverreport;

import io.legacyfighter.cabs.dto.DriverReport;

interface DriverReportReconciliation {

    void compare(DriverReport oldOne, DriverReport newOne);
}
