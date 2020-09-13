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
package com.djrapitops.plan.settings.config.paths;

import com.djrapitops.plan.settings.config.paths.key.Setting;
import com.djrapitops.plan.settings.config.paths.key.StringSetting;
import com.djrapitops.plan.storage.database.DBType;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * {@link Setting} values that are in "Database" section.
 *
 * @author Rsl1122
 */
public class DatabaseSettings {

    public static final Setting<String> TYPE = new StringSetting("Database.Type", DBType::exists);
    public static final Setting<String> MYSQL_HOST = new StringSetting("Database.MySQL.Host");
    public static final Setting<String> MYSQL_PORT = new StringSetting("Database.MySQL.Port", NumberUtils::isParsable);
    public static final Setting<String> MYSQL_USER = new StringSetting("Database.MySQL.User");
    public static final Setting<String> H2_USER = new StringSetting("Database.H2.User");
    public static final Setting<String> H2_PASS = new StringSetting("Database.H2.Password");
    public static final Setting<String> MYSQL_PASS = new StringSetting("Database.MySQL.Password");
    public static final Setting<String> MYSQL_DATABASE = new StringSetting("Database.MySQL.Database");
    public static final Setting<String> MYSQL_LAUNCH_OPTIONS = new StringSetting("Database.MySQL.Launch_options");

    private DatabaseSettings() {
        /* static variable class */
    }
}