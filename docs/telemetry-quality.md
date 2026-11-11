# Telemetry quality in summary reports

This fork adds reporting-quality statistics to the existing summary JSON API.
The original Traccar implementation and license are retained.

In a full position scan, each summary includes `telemetryQuality`:

```json
{"positionCount":3,"missingTimeCount":0,"outOfOrderCount":0,"maxPositionGap":600000}
```

The example represents three observed positions and a largest gap of ten minutes.
The calculation runs in the existing position loop with constant extra memory and
no additional database query. It does not change distance, speed, or fuel values.

- `positionCount`: all records scanned, including duplicate timestamps.
- `missingTimeCount`: records with no fix time.
- `outOfOrderCount`: timestamps earlier than the latest observed fix time.
- `maxPositionGap`: largest forward interval in milliseconds; null when there
  is no measurable interval. Duplicate timestamps produce a zero interval.
  Late samples do not rewind the anchor. Extreme intervals saturate at `Long.MAX_VALUE`.

Fast reports inspect only edge positions, so `telemetryQuality` is null there:
unknown quality must not appear to mean zero missing samples. The calculation
excludes gaps before the first and after the last observed fix. It describes
stored fix times, not network availability or GPS accuracy. Excel templates and
the web interface are unchanged; consumers can display the new JSON field.

Run the regression tests with:

```sh
./gradlew test --tests org.traccar.reports.common.TelemetryQualityAccumulatorTest
```
