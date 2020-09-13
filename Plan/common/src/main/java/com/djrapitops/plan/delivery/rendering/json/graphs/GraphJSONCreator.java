/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.rendering.json.graphs;

import com.djrapitops.plan.delivery.domain.DateMap;
import com.djrapitops.plan.delivery.domain.mutators.MutatorFunctions;
import com.djrapitops.plan.delivery.domain.mutators.PingMutator;
import com.djrapitops.plan.delivery.domain.mutators.TPSMutator;
import com.djrapitops.plan.delivery.rendering.json.graphs.bar.BarGraph;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.LineGraphFactory;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.PingGraph;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.Point;
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.Pie;
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.WorldPie;
import com.djrapitops.plan.delivery.rendering.json.graphs.special.WorldMap;
import com.djrapitops.plan.delivery.rendering.json.graphs.stack.StackGraph;
import com.djrapitops.plan.gathering.domain.Ping;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.analysis.ActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.analysis.NetworkActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.storage.database.queries.objects.*;
import com.djrapitops.plan.utilities.java.Lists;
import com.djrapitops.plan.utilities.java.Maps;
import com.djrapitops.plugin.api.TimeAmount;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Perses Graph related Data JSON.
 *
 * @author Rsl1122
 */
@Singleton
public class GraphJSONCreator {

    private final PlanConfig config;
    private final Theme theme;
    private final DBSystem dbSystem;
    private final Graphs graphs;

    @Inject
    public GraphJSONCreator(
            PlanConfig config,
            Theme theme,
            DBSystem dbSystem,
            Graphs graphs
    ) {
        this.config = config;
        this.theme = theme;
        this.dbSystem = dbSystem;
        this.graphs = graphs;
    }

    public String performanceGraphJSON(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        long now = System.currentTimeMillis();
        long halfYearAgo = now - TimeUnit.DAYS.toMillis(180L);
        TPSMutator tpsMutator = new TPSMutator(db.query(TPSQueries.fetchTPSDataOfServer(serverUUID)))
                .filterDataBetween(halfYearAgo, now);
        return '{' +
                "\"playersOnline\":" + lineGraphs.playersOnlineGraph(tpsMutator).toHighChartsSeries() +
                ",\"tps\":" + lineGraphs.tpsGraph(tpsMutator).toHighChartsSeries() +
                ",\"cpu\":" + lineGraphs.cpuGraph(tpsMutator).toHighChartsSeries() +
                ",\"ram\":" + lineGraphs.ramGraph(tpsMutator).toHighChartsSeries() +
                ",\"entities\":" + lineGraphs.entityGraph(tpsMutator).toHighChartsSeries() +
                ",\"chunks\":" + lineGraphs.chunkGraph(tpsMutator).toHighChartsSeries() +
                ",\"disk\":" + lineGraphs.diskGraph(tpsMutator).toHighChartsSeries() +
                ",\"colors\":{" +
                "\"playersOnline\":\"" + theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE) + "\"," +
                "\"cpu\":\"" + theme.getValue(ThemeVal.GRAPH_CPU) + "\"," +
                "\"ram\":\"" + theme.getValue(ThemeVal.GRAPH_RAM) + "\"," +
                "\"entities\":\"" + theme.getValue(ThemeVal.GRAPH_ENTITIES) + "\"," +
                "\"chunks\":\"" + theme.getValue(ThemeVal.GRAPH_CHUNKS) + "\"," +
                "\"low\":\"" + theme.getValue(ThemeVal.GRAPH_TPS_LOW) + "\"," +
                "\"med\":\"" + theme.getValue(ThemeVal.GRAPH_TPS_MED) + "\"," +
                "\"high\":\"" + theme.getValue(ThemeVal.GRAPH_TPS_HIGH) + "\"}" +
                ",\"zones\":{" +
                "\"tpsThresholdMed\":" + config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED) + ',' +
                "\"tpsThresholdHigh\":" + config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_HIGH) + ',' +
                "\"diskThresholdMed\":" + config.get(DisplaySettings.GRAPH_DISK_THRESHOLD_MED) + ',' +
                "\"diskThresholdHigh\":" + config.get(DisplaySettings.GRAPH_DISK_THRESHOLD_HIGH) +
                "}}";
    }

    public String playersOnlineGraph(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long halfYearAgo = now - TimeUnit.DAYS.toMillis(180L);

        List<Point> points = Lists.map(db.query(TPSQueries.fetchPlayersOnlineOfServer(halfYearAgo, now, serverUUID)),
                point -> new Point(point.getDate(), point.getValue())
        );
        return "{\"playersOnline\":" + graphs.line().lineGraph(points).toHighChartsSeries() +
                ",\"color\":\"" + theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE) + "\"}";
    }

    public String uniqueAndNewGraphJSON(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        long now = System.currentTimeMillis();
        long halfYearAgo = now - TimeUnit.DAYS.toMillis(180L);
        int timeZoneOffset = config.getTimeZone().getOffset(now);
        NavigableMap<Long, Integer> uniquePerDay = db.query(
                PlayerCountQueries.uniquePlayerCounts(halfYearAgo, now, timeZoneOffset, serverUUID)
        );
        NavigableMap<Long, Integer> newPerDay = db.query(
                PlayerCountQueries.newPlayerCounts(halfYearAgo, now, timeZoneOffset, serverUUID)
        );

        return createUniqueAndNewJSON(lineGraphs, uniquePerDay, newPerDay, TimeUnit.DAYS.toMillis(1L));
    }

    public String hourlyUniqueAndNewGraphJSON(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        long now = System.currentTimeMillis();
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);
        int timeZoneOffset = config.getTimeZone().getOffset(now);
        NavigableMap<Long, Integer> uniquePerDay = db.query(
                PlayerCountQueries.hourlyUniquePlayerCounts(weekAgo, now, timeZoneOffset, serverUUID)
        );
        NavigableMap<Long, Integer> newPerDay = db.query(
                PlayerCountQueries.newPlayerCounts(weekAgo, now, timeZoneOffset, serverUUID)
        );

        return createUniqueAndNewJSON(lineGraphs, uniquePerDay, newPerDay, TimeUnit.HOURS.toMillis(1L));
    }

    public String createUniqueAndNewJSON(LineGraphFactory lineGraphs, NavigableMap<Long, Integer> uniquePerDay, NavigableMap<Long, Integer> newPerDay, long gapFillPeriod) {
        return "{\"uniquePlayers\":" +
                lineGraphs.lineGraph(MutatorFunctions.toPoints(
                        MutatorFunctions.addMissing(uniquePerDay, gapFillPeriod, 0)
                )).toHighChartsSeries() +
                ",\"newPlayers\":" +
                lineGraphs.lineGraph(MutatorFunctions.toPoints(
                        MutatorFunctions.addMissing(newPerDay, gapFillPeriod, 0)
                )).toHighChartsSeries() +
                ",\"colors\":{" +
                "\"playersOnline\":\"" + theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE) + "\"," +
                "\"newPlayers\":\"" + theme.getValue(ThemeVal.LIGHT_GREEN) + "\"" +
                "}}";
    }

    public String uniqueAndNewGraphJSON() {
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        long now = System.currentTimeMillis();
        long halfYearAgo = now - TimeUnit.DAYS.toMillis(180L);
        int timeZoneOffset = config.getTimeZone().getOffset(now);
        NavigableMap<Long, Integer> uniquePerDay = db.query(
                PlayerCountQueries.uniquePlayerCounts(halfYearAgo, now, timeZoneOffset)
        );
        NavigableMap<Long, Integer> newPerDay = db.query(
                PlayerCountQueries.newPlayerCounts(halfYearAgo, now, timeZoneOffset)
        );

        return createUniqueAndNewJSON(lineGraphs, uniquePerDay, newPerDay, TimeUnit.DAYS.toMillis(1L));
    }

    public String hourlyUniqueAndNewGraphJSON() {
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        long now = System.currentTimeMillis();
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);
        int timeZoneOffset = config.getTimeZone().getOffset(now);
        NavigableMap<Long, Integer> uniquePerDay = db.query(
                PlayerCountQueries.hourlyUniquePlayerCounts(weekAgo, now, timeZoneOffset)
        );
        NavigableMap<Long, Integer> newPerDay = db.query(
                PlayerCountQueries.hourlyNewPlayerCounts(weekAgo, now, timeZoneOffset)
        );

        return createUniqueAndNewJSON(lineGraphs, uniquePerDay, newPerDay, TimeUnit.HOURS.toMillis(1L));
    }

    public String serverCalendarJSON(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long twoYearsAgo = now - TimeUnit.DAYS.toMillis(730L);
        int timeZoneOffset = config.getTimeZone().getOffset(now);
        NavigableMap<Long, Integer> uniquePerDay = db.query(
                PlayerCountQueries.uniquePlayerCounts(twoYearsAgo, now, timeZoneOffset, serverUUID)
        );
        NavigableMap<Long, Integer> newPerDay = db.query(
                PlayerCountQueries.newPlayerCounts(twoYearsAgo, now, timeZoneOffset, serverUUID)
        );
        NavigableMap<Long, Long> playtimePerDay = db.query(
                SessionQueries.playtimePerDay(twoYearsAgo, now, timeZoneOffset, serverUUID)
        );
        NavigableMap<Long, Integer> sessionsPerDay = db.query(
                SessionQueries.sessionCountPerDay(twoYearsAgo, now, timeZoneOffset, serverUUID)
        );
        return "{\"data\":" +
                graphs.calendar().serverCalendar(
                        uniquePerDay,
                        newPerDay,
                        playtimePerDay,
                        sessionsPerDay
                ).toCalendarSeries() +
                ",\"firstDay\":" + 1 + '}';
    }

    public Map<String, Object> serverWorldPieJSONAsMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        WorldTimes worldTimes = db.query(WorldTimesQueries.fetchServerTotalWorldTimes(serverUUID));
        WorldPie worldPie = graphs.pie().worldPie(worldTimes);

        return Maps.builder(String.class, Object.class)
                .put("world_series", worldPie.getSlices())
                .put("gm_series", worldPie.toHighChartsDrillDownMaps())
                .build();
    }

    public Map<String, Object> activityGraphsJSONAsMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long date = System.currentTimeMillis();
        Long threshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        DateMap<Map<String, Integer>> activityData = new DateMap<>();
        for (long time = date; time >= date - TimeAmount.MONTH.toMillis(2L); time -= TimeAmount.WEEK.toMillis(1L)) {
            activityData.put(time, db.query(ActivityIndexQueries.fetchActivityIndexGroupingsOn(time, serverUUID, threshold)));
        }

        return createActivityGraphJSON(activityData);
    }

    public Map<String, Object> createActivityGraphJSON(DateMap<Map<String, Integer>> activityData) {
        Map.Entry<Long, Map<String, Integer>> lastActivityEntry = activityData.lastEntry();
        Pie activityPie = graphs.pie().activityPie(lastActivityEntry != null ? lastActivityEntry.getValue() : Collections.emptyMap());
        StackGraph activityStackGraph = graphs.stack().activityStackGraph(activityData);

        return Maps.builder(String.class, Object.class)
                .put("activity_series", activityStackGraph.getDataSets())
                .put("activity_labels", activityStackGraph.getLabels())
                .put("activity_pie_series", activityPie.getSlices())
                .build();
    }

    public Map<String, Object> activityGraphsJSONAsMap() {
        Database db = dbSystem.getDatabase();
        long date = System.currentTimeMillis();
        Long threshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        DateMap<Map<String, Integer>> activityData = new DateMap<>();
        for (long time = date; time >= date - TimeAmount.MONTH.toMillis(2L); time -= TimeAmount.WEEK.toMillis(1L)) {
            activityData.put(time, db.query(NetworkActivityIndexQueries.fetchActivityIndexGroupingsOn(time, threshold)));
        }

        return createActivityGraphJSON(activityData);
    }

    public Map<String, Object> geolocationGraphsJSONAsMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        Map<String, Integer> geolocationCounts = db.query(GeoInfoQueries.serverGeolocationCounts(serverUUID));

        return createGeolocationJSON(geolocationCounts);
    }

    public Map<String, Object> createGeolocationJSON(Map<String, Integer> geolocationCounts) {
        BarGraph geolocationBarGraph = graphs.bar().geolocationBarGraph(geolocationCounts);
        WorldMap worldMap = graphs.special().worldMap(geolocationCounts);

        return Maps.builder(String.class, Object.class)
                .put("geolocation_series", worldMap.getEntries())
                .put("geolocation_bar_series", geolocationBarGraph.getBars())
                .put("colors", Maps.builder(String.class, String.class)
                        .put("low", theme.getValue(ThemeVal.WORLD_MAP_LOW))
                        .put("high", theme.getValue(ThemeVal.WORLD_MAP_HIGH))
                        .put("bars", theme.getValue(ThemeVal.GREEN))
                        .build())
                .build();
    }

    public Map<String, Object> geolocationGraphsJSONAsMap() {
        Database db = dbSystem.getDatabase();
        Map<String, Integer> geolocationCounts = db.query(GeoInfoQueries.networkGeolocationCounts());

        return createGeolocationJSON(geolocationCounts);
    }

    public String pingGraphsJSON(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        List<Ping> pings = db.query(PingQueries.fetchPingDataOfServer(now - TimeUnit.DAYS.toMillis(180L), now, serverUUID));

        PingGraph pingGraph = graphs.line().pingGraph(new PingMutator(pings).mutateToByMinutePings().all());// TODO Optimize in query

        return "{\"min_ping_series\":" + pingGraph.getMinGraph().toHighChartsSeries() +
                ",\"avg_ping_series\":" + pingGraph.getAvgGraph().toHighChartsSeries() +
                ",\"max_ping_series\":" + pingGraph.getMaxGraph().toHighChartsSeries() +
                ",\"colors\":{" +
                "\"min\":\"" + theme.getValue(ThemeVal.GRAPH_MIN_PING) + "\"," +
                "\"avg\":\"" + theme.getValue(ThemeVal.GRAPH_AVG_PING) + "\"," +
                "\"max\":\"" + theme.getValue(ThemeVal.GRAPH_MAX_PING) + "\"" +
                "}}";
    }

    public Map<String, Object> punchCardJSONAsMap(UUID serverUUID) {
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        List<Session> sessions = dbSystem.getDatabase().query(
                SessionQueries.fetchServerSessionsWithoutKillOrWorldData(monthAgo, now, serverUUID)
        );
        return Maps.builder(String.class, Object.class)
                .put("punchCard", graphs.special().punchCard(sessions).getDots())
                .put("color", theme.getValue(ThemeVal.GRAPH_PUNCHCARD))
                .build();
    }

    public Map<String, Object> serverPreferencePieJSONAsMap() {
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        String[] pieColors = theme.getPieColors(ThemeVal.GRAPH_WORLD_PIE);
        Map<String, Long> playtimePerServer = dbSystem.getDatabase().query(SessionQueries.playtimePerServer(now, monthAgo));

        return Maps.builder(String.class, Object.class)
                .put("server_pie_colors", pieColors)
                .put("server_pie_series_30d", graphs.pie().serverPreferencePie(playtimePerServer).getSlices())
                .build();
    }
}