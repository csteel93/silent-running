package com.steel.silent.debug;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.steel.silent.simulation.snapshot.BodyState;
import com.steel.silent.simulation.snapshot.SimulationSnapshot;
import com.steel.silent.ui.renderers.viewProxies.ProjectedBodyState;
import com.steel.silent.ui.renderers.viewProxies.WorldProjection;

public class SimulationSnapshotWriter {

    private static final String ENABLED_PROPERTY = "silent.debug.snapshots";
    private static final String DIRECTORY_PROPERTY = "silent.debug.snapshot.dir";
    private static final long DEFAULT_INTERVAL_MILLIS = 10_000L;

    private final boolean enabled;
    private final Path snapshotDirectory;
    private final long intervalMillis;

    private long lastWriteRealtimeMillis;

    public SimulationSnapshotWriter() {
        this(
                Boolean.parseBoolean(System.getProperty(ENABLED_PROPERTY, "true")),
                Path.of(System.getProperty(DIRECTORY_PROPERTY, "debug/snapshots")),
                DEFAULT_INTERVAL_MILLIS);
    }

    public SimulationSnapshotWriter(final boolean enabled,
            final Path snapshotDirectory,
            final long intervalMillis) {
        this.enabled = enabled;
        this.snapshotDirectory = snapshotDirectory;
        this.intervalMillis = intervalMillis;
        this.lastWriteRealtimeMillis = System.currentTimeMillis();
    }

    public void writeIfDue(final SimulationSnapshot snapshot,
            final long realtimeMillis,
            final WorldProjection projection) {
        if (!enabled || snapshot == null || realtimeMillis - lastWriteRealtimeMillis < intervalMillis) {
            return;
        }

        lastWriteRealtimeMillis = realtimeMillis;
        try {
            Files.createDirectories(snapshotDirectory);
            final Path file = snapshotDirectory.resolve(fileName());
            Files.writeString(file, toMarkdown(snapshot, realtimeMillis, projection));
        } catch (IOException e) {
            System.err.println("Failed to write simulation snapshot: " + e.getMessage());
        }
    }

    private String fileName() {
        return "latest-simulation-snapshot.md";
    }

    private String toMarkdown(final SimulationSnapshot snapshot,
            final long realtimeMillis,
            final WorldProjection projection) {
        final Map<UUID, BodyState> bodiesById = snapshot.bodies().stream()
                .collect(Collectors.toMap(BodyState::bodyId, Function.identity()));

        final StringBuilder markdown = new StringBuilder();
        markdown.append("# Simulation Snapshot\n\n");
        markdown.append("| Field | Value |\n");
        markdown.append("| --- | --- |\n");
        appendSummaryRow(markdown, "Written at", Instant.ofEpochMilli(realtimeMillis).toString());
        appendSummaryRow(markdown, "Realtime millis", formatLong(realtimeMillis));
        appendSummaryRow(markdown, "Epoch millis", formatLong(snapshot.epochMillis()));
        appendSummaryRow(markdown, "Elapsed simulation time", elapsedTime(snapshot.elapsedSimMillis()));
        appendSummaryRow(markdown, "Elapsed simulation millis", formatLong(snapshot.elapsedSimMillis()));
        appendSummaryRow(markdown, "Absolute simulation millis", formatLong(snapshot.absoluteSimMillis()));
        appendSummaryRow(markdown, "Body count", formatLong(snapshot.bodies().size()));

        markdown.append("\n## Bodies\n\n");
        markdown.append("| Name | Class | Primary | Radius (km) | Influence Radius (km) | ");
        markdown.append("Position X (km) | Position Y (km) | Velocity X (m/s) | Velocity Y (m/s) | ");
        markdown.append("Orientation (deg) | Color | ID |\n");
        markdown.append("| --- | --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- | --- |\n");

        snapshot.bodies().forEach(body -> appendBodyRow(markdown, body, bodiesById));

        markdown.append("\n## Projected Body States\n\n");
        markdown.append("These are display-space values after applying `WorldProjection` and `ProjectedBodyState`.\n\n");
        markdown.append("| Name | Class | Projected X | Projected Y | Render Radius | ");
        markdown.append("Influence Radius | Encounter Radius | Orientation (deg) | ID |\n");
        markdown.append("| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |\n");

        snapshot.bodies().stream()
                .map(body -> new ProjectedBodyState(body, projection, bodiesById))
                .forEach(projectedBody -> appendProjectedBodyRow(markdown, projectedBody));
        return markdown.toString();
    }

    private void appendSummaryRow(final StringBuilder markdown, final String field, final String value) {
        markdown.append("| ")
                .append(escapeMarkdown(field))
                .append(" | ")
                .append(escapeMarkdown(value))
                .append(" |\n");
    }

    private void appendBodyRow(final StringBuilder markdown,
            final BodyState body,
            final Map<UUID, BodyState> bodiesById) {
        markdown.append("| ")
                .append(escapeMarkdown(body.name()))
                .append(" | ")
                .append(escapeMarkdown(body.classification()))
                .append(" | ")
                .append(escapeMarkdown(primaryName(body, bodiesById)))
                .append(" | ")
                .append(formatKilometers(body.radiusMeters()))
                .append(" | ")
                .append(formatKilometers(body.influenceRadius()))
                .append(" | ")
                .append(formatKilometers(body.positionMeters().x()))
                .append(" | ")
                .append(formatKilometers(body.positionMeters().y()))
                .append(" | ")
                .append(formatDecimal(body.velocityMetersPerSecond().x()))
                .append(" | ")
                .append(formatDecimal(body.velocityMetersPerSecond().y()))
                .append(" | ")
                .append(formatDecimal(Math.toDegrees(body.orientationRad())))
                .append(" | ")
                .append(escapeMarkdown(body.color()))
                .append(" | `")
                .append(body.bodyId())
                .append("` |\n");
    }

    private void appendProjectedBodyRow(final StringBuilder markdown, final ProjectedBodyState body) {
        markdown.append("| ")
                .append(escapeMarkdown(body.name()))
                .append(" | ")
                .append(escapeMarkdown(body.classification()))
                .append(" | ")
                .append(formatDecimal(body.x()))
                .append(" | ")
                .append(formatDecimal(body.y()))
                .append(" | ")
                .append(formatDecimal(body.radius()))
                .append(" | ")
                .append(formatDecimal(body.getInfluenceRadius()))
                .append(" | ")
                .append(formatDecimal(body.getEncounterRadius()))
                .append(" | ")
                .append(formatDecimal(Math.toDegrees(body.orientationRad())))
                .append(" | `")
                .append(body.id())
                .append("` |\n");
    }

    private String primaryName(final BodyState body, final Map<UUID, BodyState> bodiesById) {
        if (body.primaryBodyId() == null) {
            return "-";
        }
        final BodyState primary = bodiesById.get(body.primaryBodyId());
        return primary == null ? body.primaryBodyId().toString() : primary.name();
    }

    private String elapsedTime(final long elapsedMillis) {
        final long totalSeconds = elapsedMillis / 1000L;
        final long days = totalSeconds / 86_400L;
        final long hours = (totalSeconds % 86_400L) / 3_600L;
        final long minutes = (totalSeconds % 3_600L) / 60L;
        final long seconds = totalSeconds % 60L;
        return String.format(Locale.US, "%d days, %02d:%02d:%02d", days, hours, minutes, seconds);
    }

    private String formatKilometers(final double meters) {
        return formatDecimal(meters / 1000.0);
    }

    private String formatLong(final long value) {
        return String.format(Locale.US, "%,d", value);
    }

    private String formatDecimal(final double value) {
        return String.format(Locale.US, "%,.3f", value);
    }

    private String escapeMarkdown(final String value) {
        return value
                .replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
