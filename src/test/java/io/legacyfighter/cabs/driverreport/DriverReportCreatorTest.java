package io.legacyfighter.cabs.driverreport;

import io.legacyfighter.cabs.config.FeatureFlags;
import io.legacyfighter.cabs.dto.DriverReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.togglz.junit5.AllDisabled;
import org.togglz.testing.TestFeatureManager;

import static io.legacyfighter.cabs.config.FeatureFlags.DRIVER_REPORT_CREATION_RECONCILIATION;
import static io.legacyfighter.cabs.config.FeatureFlags.DRIVER_REPORT_SQL;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@AllDisabled(FeatureFlags.class)
class DriverReportCreatorTest {

    private static final long DRIVER_ID = 1L;
    private static final int LAST_DAYS = 3;
    private static final DriverReport SQL_BASED_DRIVER_REPORT = new DriverReport();
    private static final DriverReport OLD_DRIVER_REPORT = new DriverReport();

    @Mock
    OldDriverReportCreator oldDriverReportCreator;

    @Mock
    SqlBasedDriverReportCreator sqlBasedDriverReportCreator;

    @Mock
    DriverReportReconciliation driverReportReconciliation;

    @InjectMocks
    DriverReportCreator driverReportCreator;

    @Test
    void callsSqlBasedDriverReportCreator(TestFeatureManager testFeatureManager) {
        //given
        newSqlBasedWayReturnsReport();
        // and
        testFeatureManager.enable(DRIVER_REPORT_SQL);
        testFeatureManager.disable(DRIVER_REPORT_CREATION_RECONCILIATION);

        // when
        driverReportCreator.create(DRIVER_ID, LAST_DAYS);

        // then
        verify(sqlBasedDriverReportCreator).createReport(DRIVER_ID, LAST_DAYS);
        verifyNoInteractions(oldDriverReportCreator, driverReportReconciliation);
    }

    @Test
    void callsReconciliationAndReturnSqlBasedDriverReport(TestFeatureManager testFeatureManager) {
        //given
        bothWaysReturnReport();
        // and
        testFeatureManager.enable(DRIVER_REPORT_SQL);
        testFeatureManager.enable(DRIVER_REPORT_CREATION_RECONCILIATION);

        // when
        driverReportCreator.create(DRIVER_ID, LAST_DAYS);

        // then
        verify(oldDriverReportCreator).createReport(DRIVER_ID, LAST_DAYS);
        verify(sqlBasedDriverReportCreator).createReport(DRIVER_ID, LAST_DAYS);
        verify(driverReportReconciliation).compare(OLD_DRIVER_REPORT, SQL_BASED_DRIVER_REPORT);
    }

    @Test
    void callsOldDriverReportCreator(TestFeatureManager testFeatureManager) {
        //given
        oldWayReturnsReport();
        // and
        testFeatureManager.disable(DRIVER_REPORT_SQL);
        testFeatureManager.disable(DRIVER_REPORT_CREATION_RECONCILIATION);

        // when
        driverReportCreator.create(DRIVER_ID, LAST_DAYS);

        // then
        verify(oldDriverReportCreator).createReport(DRIVER_ID, LAST_DAYS);
        verifyNoInteractions(sqlBasedDriverReportCreator, driverReportReconciliation);
    }

    @Test
    void callsReconciliationAndReturnsOldDriverReport(TestFeatureManager testFeatureManager) {
        //given
        bothWaysReturnReport();
        // and
        testFeatureManager.disable(DRIVER_REPORT_SQL);
        testFeatureManager.enable(DRIVER_REPORT_CREATION_RECONCILIATION);

        // when
        driverReportCreator.create(DRIVER_ID, LAST_DAYS);

        // then
        verify(oldDriverReportCreator).createReport(DRIVER_ID, LAST_DAYS);
        verify(sqlBasedDriverReportCreator).createReport(DRIVER_ID, LAST_DAYS);
        verify(driverReportReconciliation).compare(OLD_DRIVER_REPORT, SQL_BASED_DRIVER_REPORT);
    }

    private void newSqlBasedWayReturnsReport() {
        when(sqlBasedDriverReportCreator.createReport(DRIVER_ID, LAST_DAYS)).thenReturn(SQL_BASED_DRIVER_REPORT);
    }

    private void oldWayReturnsReport() {
        when(oldDriverReportCreator.createReport(DRIVER_ID, LAST_DAYS)).thenReturn(OLD_DRIVER_REPORT);
    }

    private void bothWaysReturnReport() {
        newSqlBasedWayReturnsReport();
        oldWayReturnsReport();
    }
}
