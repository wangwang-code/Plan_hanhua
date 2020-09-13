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
package com.djrapitops.plan.settings.locale.lang;

/**
 * Lang enum for all text included in the javascript files.
 *
 * @author Rsl1122
 */
public enum JSLang implements Lang {

    TEXT_PREDICTED_RETENTION("This value is a prediction based on previous players"),
    TEXT_NO_SERVERS("No servers found in the database"),
    TEXT_NO_SERVER("No server to display online activity for"),
    LABEL_REGISTERED_PLAYERS("Registered Players"),
    LINK_SERVER_ANALYSIS("Server Analysis"),
    LINK_QUICK_VIEW("Quick view"),
    TEXT_FIRST_SESSION("First session"),
    LABEL_SESSION_ENDED(" Ended"),
    LINK_PLAYER_PAGE("Player Page"),
    LABEL_NO_SESSION_KILLS("None"),
    UNIT_ENTITIES("Entities"),
    UNIT_CHUNKS("Chunks"),
    LABEL_RELATIVE_JOIN_ACTIVITY("Relative Join Activity"),
    LABEL_DAY_OF_WEEK("Day of the Week"),
    LABEL_WEEK_DAYS("'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'");

    private final String defaultValue;

    JSLang(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return "HTML - " + name();
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}