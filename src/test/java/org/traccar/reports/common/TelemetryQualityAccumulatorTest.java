package org.traccar.reports.common;

import org.junit.jupiter.api.Test;
import org.traccar.reports.model.SummaryReportItem;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TelemetryQualityAccumulatorTest {

    @Test
    public void emptyAndSingleSamplesHaveNoMeasuredGap() {
        var accumulator = new TelemetryQualityAccumulator();
        assertEquals(0, accumulator.snapshot().positionCount());
        assertNull(accumulator.snapshot().maxPositionGap());
        accumulator.add(new Date(1000));
        assertEquals(1, accumulator.snapshot().positionCount());
        assertNull(accumulator.snapshot().maxPositionGap());
    }

    @Test
    public void measuresLargestGapInMilliseconds() {
        var accumulator = new TelemetryQualityAccumulator();
        accumulator.add(new Date(1000));
        accumulator.add(new Date(61000));
        accumulator.add(new Date(661000));
        assertEquals(3, accumulator.snapshot().positionCount());
        assertEquals(600000L, accumulator.snapshot().maxPositionGap());
    }

    @Test
    public void missingTimesAreCountedWithoutBreakingTheAnchor() {
        var accumulator = new TelemetryQualityAccumulator();
        accumulator.add(null);
        accumulator.add(new Date(1000));
        accumulator.add(null);
        accumulator.add(new Date(61000));
        assertEquals(4, accumulator.snapshot().positionCount());
        assertEquals(2, accumulator.snapshot().missingTimeCount());
        assertEquals(60000L, accumulator.snapshot().maxPositionGap());
    }

    @Test
    public void lateSamplesDoNotInflateTheFollowingGap() {
        var accumulator = new TelemetryQualityAccumulator();
        accumulator.add(new Date(1000));
        accumulator.add(new Date(61000));
        accumulator.add(new Date(2000));
        accumulator.add(new Date(121000));
        assertEquals(1, accumulator.snapshot().outOfOrderCount());
        assertEquals(60000L, accumulator.snapshot().maxPositionGap());
    }

    @Test
    public void duplicateTimestampsHaveZeroGap() {
        var accumulator = new TelemetryQualityAccumulator();
        accumulator.add(new Date(1000));
        accumulator.add(new Date(1000));
        assertEquals(0L, accumulator.snapshot().maxPositionGap());
        assertEquals(0, accumulator.snapshot().outOfOrderCount());
    }

    @Test
    public void snapshotsDoNotChangeWhenMoreSamplesArrive() {
        var accumulator = new TelemetryQualityAccumulator();
        accumulator.add(new Date(1000));
        var before = accumulator.snapshot();
        accumulator.add(new Date(2000));
        assertEquals(1, before.positionCount());
        assertNull(before.maxPositionGap());
    }

    @Test
    public void fastReportsDefaultToUnknownQuality() {
        assertNull(new SummaryReportItem().getTelemetryQuality());
    }

    @Test
    public void extremelyLargeGapsSaturateInsteadOfBecomingNegative() {
        var accumulator = new TelemetryQualityAccumulator();
        accumulator.add(new Date(Long.MIN_VALUE));
        accumulator.add(new Date(Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, accumulator.snapshot().maxPositionGap());
    }
}
