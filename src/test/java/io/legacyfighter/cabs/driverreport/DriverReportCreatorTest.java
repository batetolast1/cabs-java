package io.legacyfighter.cabs.driverreport;

import io.legacyfighter.cabs.config.FeatureFlags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.togglz.junit5.AllDisabled;
import org.togglz.junit5.AllEnabled;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DriverReportCreatorTest {

    private static final long DRIVER_ID = 1L;
    private static final int LAST_DAYS = 3;

    @Mock
    OldDriverReportCreator oldDriverReportCreator;

    @Mock
    SqlBasedDriverReportCreator sqlBasedDriverReportCreator;

    @InjectMocks
    DriverReportCreator driverReportCreator;

    @Test
    @AllEnabled(FeatureFlags.class)
    void callsSqlBasedDriverReportCreator() {
        // when
        driverReportCreator.create(DRIVER_ID, LAST_DAYS);

        // then
        verify(sqlBasedDriverReportCreator).createReport(DRIVER_ID, LAST_DAYS);
    }

    @Test
    @AllDisabled(FeatureFlags.class)
    void callsOldDriverReportCreator() {
        // when
        driverReportCreator.create(DRIVER_ID, LAST_DAYS);

        // then
        verify(oldDriverReportCreator).createReport(DRIVER_ID, LAST_DAYS);
    }
}