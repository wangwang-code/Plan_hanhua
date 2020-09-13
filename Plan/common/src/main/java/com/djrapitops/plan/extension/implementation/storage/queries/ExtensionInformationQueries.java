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
package com.djrapitops.plan.extension.implementation.storage.queries;

import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.results.ExtensionInformation;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionIconTable;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionPluginTable;
import com.djrapitops.plan.utilities.java.Lists;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for information about DataExtensions stored in the database.
 *
 * @author Rsl1122
 */
public class ExtensionInformationQueries {

    private ExtensionInformationQueries() {
        /* Static method class */
    }

    public static Query<List<ExtensionInformation>> extensionsOfServer(UUID serverUUID) {
        String sql = SELECT +
                ExtensionPluginTable.TABLE_NAME + '.' + ExtensionPluginTable.ID + " as id," +
                ExtensionPluginTable.TABLE_NAME + '.' + ExtensionPluginTable.PLUGIN_NAME + " as plugin_name," +
                ExtensionIconTable.TABLE_NAME + '.' + ExtensionIconTable.ICON_NAME + " as icon_name," +
                ExtensionIconTable.COLOR + ',' +
                ExtensionIconTable.FAMILY +
                FROM + ExtensionPluginTable.TABLE_NAME +
                INNER_JOIN + ExtensionIconTable.TABLE_NAME + " on " +
                ExtensionPluginTable.ICON_ID + "=" + ExtensionIconTable.TABLE_NAME + '.' + ExtensionIconTable.ID +
                WHERE + ExtensionPluginTable.SERVER_UUID + "=?";

        return new QueryStatement<List<ExtensionInformation>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public List<ExtensionInformation> processResults(ResultSet set) throws SQLException {
                List<ExtensionInformation> information = new ArrayList<>();
                while (set.next()) {
                    information.add(extractExtensionInformationFromQuery(set));
                }
                return information;
            }
        };
    }

    private static ExtensionInformation extractExtensionInformationFromQuery(ResultSet set) throws SQLException {
        int id = set.getInt("id");
        String pluginName = set.getString("plugin_name");

        String iconName = set.getString("icon_name");
        Family iconFamily = Family.getByName(set.getString(ExtensionIconTable.FAMILY)).orElse(Family.SOLID);
        Color color = Color.getByName(set.getString(ExtensionIconTable.COLOR)).orElse(Color.NONE);
        Icon icon = new Icon(iconFamily, iconName, color);

        return new ExtensionInformation(id, pluginName, icon);
    }

    public static Query<Map<UUID, List<ExtensionInformation>>> allExtensions() {
        String sql = SELECT +
                ExtensionPluginTable.TABLE_NAME + '.' + ExtensionPluginTable.ID + " as id," +
                ExtensionPluginTable.TABLE_NAME + '.' + ExtensionPluginTable.PLUGIN_NAME + " as plugin_name," +
                ExtensionPluginTable.SERVER_UUID + ',' +
                ExtensionIconTable.TABLE_NAME + '.' + ExtensionIconTable.ICON_NAME + " as icon_name," +
                ExtensionIconTable.COLOR + ',' +
                ExtensionIconTable.FAMILY +
                FROM + ExtensionPluginTable.TABLE_NAME +
                INNER_JOIN + ExtensionIconTable.TABLE_NAME + " on " +
                ExtensionPluginTable.ICON_ID + "=" + ExtensionIconTable.TABLE_NAME + '.' + ExtensionIconTable.ID;

        return new QueryAllStatement<Map<UUID, List<ExtensionInformation>>>(sql, 100) {
            @Override
            public Map<UUID, List<ExtensionInformation>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<ExtensionInformation>> byServerUUID = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(ExtensionPluginTable.SERVER_UUID));
                    List<ExtensionInformation> information = byServerUUID.computeIfAbsent(serverUUID, Lists::create);
                    information.add(extractExtensionInformationFromQuery(set));
                }
                return byServerUUID;
            }
        };
    }

}