/*
 * Copyright 2026 Ugwu Emmanuel Uchenna
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.reports.common;

import org.traccar.reports.model.TelemetryQuality;

import java.util.Date;

/** Constant-memory statistics for the fix-time-ordered summary position stream. */
public class TelemetryQualityAccumulator {

    private long positionCount;
    private long missingTimeCount;
    private long outOfOrderCount;
    private Long latestTime;
    private Long maxPositionGap;

    public void add(Date fixTime) {
        positionCount++;
        if (fixTime == null) {
            missingTimeCount++;
            return;
        }
        long time = fixTime.getTime();
        if (latestTime != null) {
            if (time < latestTime) {
                outOfOrderCount++;
                return; // Do not rewind the anchor and inflate the following gap.
            }
            long gap = time - latestTime;
            if (gap < 0) {
                gap = Long.MAX_VALUE; // Saturate an interval exceeding the millisecond range.
            }
            maxPositionGap = maxPositionGap == null ? gap : Math.max(maxPositionGap, gap);
        }
        latestTime = time;
    }

    public TelemetryQuality snapshot() {
        return new TelemetryQuality(positionCount, missingTimeCount, outOfOrderCount, maxPositionGap);
    }
}
