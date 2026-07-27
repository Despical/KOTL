/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2026  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.despical.kotl.api;

import dev.despical.kotl.KOTL;
import dev.despical.kotl.option.BooleanOption;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Level;

/**
 * Collects optional execution timings for KOTL custom event listeners.
 * <p>
 * Profiling is disabled by default unless enabled through the plugin
 * configuration. Recorded data is held in memory for the lifetime of the
 * plugin and can be rendered to an authorized command sender.
 *
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
@ApiStatus.Internal
public final class EventProfiler {

    private boolean enabled;
    private boolean verbose;

    private final KOTL plugin;
    private final Map<Class<? extends Event>, ProfileData> profiles;

    /**
     * Creates a profiler for the plugin event dispatcher.
     *
     * @param plugin the owning KOTL instance
     */
    public EventProfiler(@NotNull KOTL plugin) {
        this.plugin = plugin;
        this.profiles = new ConcurrentHashMap<>();
        this.reload();
    }

    /**
     * Returns whether event timing collection is enabled.
     *
     * @return {@code true} when event timings are being recorded
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns whether every event invocation is logged.
     *
     * @return {@code true} when verbose profiling output is enabled
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Reloads profiler switches from the active configuration.
     */
    public void reload() {
        this.enabled = BooleanOption.EVENT_PROFILING_ENABLED.value();
        this.verbose = BooleanOption.EVENT_PROFILING_VERBOSE.value();
    }

    /**
     * Records one completed custom event invocation.
     *
     * @param event the event that was dispatched
     * @param durationNanos listener execution time in nanoseconds
     */
    public void record(@NotNull Event event, long durationNanos) {
        if (!enabled) {
            return;
        }

        profiles.computeIfAbsent(event.getClass(), k -> new ProfileData())
            .record(durationNanos);

        if (verbose) {
            logEvent(event, durationNanos);
        }
    }

    /**
     * Sends accumulated timing totals and averages to a command sender.
     *
     * @param sender the report recipient
     */
    public void sendReport(@NotNull CommandSender sender) {
        if (!enabled) {
            plugin.getChatManager().sendRawMessage(sender,
                "<#FF1744>✖ <#ff5c5c>Event timings system is currently disabled."
            );

            plugin.getChatManager().sendRawMessage(sender,
                "<gray>➥ Set <yellow>event-profiling.enabled</yellow> to <green>true</green> in config.yml"
            );
            return;
        }

        plugin.getChatManager().sendRawMessage(sender,
            "<gray><strikethrough>──────</strikethrough> <gradient:#ffcc00:#ff8c00>KOTL Event Timings</gradient> <gray><strikethrough>──────</strikethrough>"
        );

        if (profiles.isEmpty()) {
            plugin.getChatManager().sendRawMessage(sender,
                "<gray>• <#9e9e9e>No timing data collected yet.</#9e9e9e>"
            );
            return;
        }

        profiles.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue().totalTime(), a.getValue().totalTime()))
            .forEach(entry -> {
                ProfileData data = entry.getValue();

                double totalMs = data.totalTime() / 1_000_000.0;
                double avgMs = data.averageTime() / 1_000_000.0;

                String totalColor = gradientColorForMs(totalMs);
                String avgColor = gradientColorForMs(avgMs);

                String message = String.format(
                    Locale.US,
                    "<gray>• <#ffd54f>%s <gray>→ <%s>%.3f ms</%s> <gray>(avg <%s>%.3f ms</%s>, <#a5d6a7>%dx</#a5d6a7>)",
                    entry.getKey().getSimpleName(),
                    totalColor,
                    totalMs,
                    totalColor,
                    avgColor,
                    avgMs,
                    avgColor,
                    data.calls()
                );

                plugin.getChatManager().sendRawMessage(sender, message);
            });
    }

    private String gradientColorForMs(double ms) {
        double maxMs = 5.0;
        double ratio = Math.min(ms / maxMs, 1.0);

        int r = (int) (255 * ratio);
        int g = (int) (255 * (1 - ratio));
        int b = 90;

        return String.format("#%02x%02x%02x", r, g, b);
    }

    private void logEvent(Event event, long nanos) {
        double ms = nanos / 1_000_000.0;

        String detail = buildEventDetails(event);

        plugin.getLogger().log(
            Level.INFO,
            "[Profiler] {0} fired in {1} ms{2}.",
            new Object[] {
                event.getClass().getSimpleName(),
                String.format(Locale.US, "%.3f", ms),
                detail
            }
        );
    }

    private String buildEventDetails(Event event) {
        return ", (" + event.toString() + ")";
    }

    private record ProfileData(LongAdder callAdder, LongAdder timeAdder) {

        ProfileData() {
            this(new LongAdder(), new LongAdder());
        }

        void record(long nanos) {
            callAdder.add(1);
            timeAdder.add(nanos);
        }

        long calls() {
            return callAdder.sum();
        }

        long totalTime() {
            return timeAdder.sum();
        }

        long averageTime() {
            long count = calls();
            return count == 0 ? 0 : totalTime() / count;
        }
    }
}
