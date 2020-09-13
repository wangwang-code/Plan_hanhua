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
package com.djrapitops.plan.extension.implementation.storage.transactions;

import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionIconTable;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionPluginTable;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionTabTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.AND;
import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;

/**
 * Transaction for storing {@link TabInformation}s.
 *
 * @author Rsl1122
 */
public class StoreTabInformationTransaction extends ThrowawayTransaction {

    private final String pluginName;
    private final UUID serverUUID;
    private final TabInformation tabInformation;

    public StoreTabInformationTransaction(String pluginName, UUID serverUUID, TabInformation tabInformation) {
        this.pluginName = pluginName;
        this.serverUUID = serverUUID;
        this.tabInformation = tabInformation;
    }

    @Override
    protected void performOperations() {
        execute(storeTab());
    }

    private Executable storeTab() {
        return connection -> {
            if (!updateTab().execute(connection)) {
                return insertTab().execute(connection);
            }
            return false;
        };
    }

    private Executable updateTab() {
        String sql = "UPDATE " + ExtensionTabTable.TABLE_NAME +
                " SET " +
                ExtensionTabTable.TAB_PRIORITY + "=?," +
                ExtensionTabTable.ELEMENT_ORDER + "=?," +
                ExtensionTabTable.ICON_ID + "=" + ExtensionIconTable.STATEMENT_SELECT_ICON_ID +
                WHERE + ExtensionTabTable.PLUGIN_ID + "=" + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID +
                AND + ExtensionTabTable.TAB_NAME + "=?";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, tabInformation.getTabPriority());
                statement.setString(2, ElementOrder.serialize(tabInformation.getTabElementOrder().orElse(ElementOrder.values())));
                ExtensionIconTable.set3IconValuesToStatement(statement, 3, tabInformation.getTabIcon());
                ExtensionPluginTable.set2PluginValuesToStatement(statement, 6, pluginName, serverUUID);
                statement.setString(8, tabInformation.getTabName());
            }
        };
    }

    private Executable insertTab() {
        String sql = "INSERT INTO " + ExtensionTabTable.TABLE_NAME + "(" +
                ExtensionTabTable.TAB_NAME + "," +
                ExtensionTabTable.ELEMENT_ORDER + "," +
                ExtensionTabTable.TAB_PRIORITY + "," +
                ExtensionTabTable.ICON_ID + "," +
                ExtensionTabTable.PLUGIN_ID +
                ") VALUES (?,?,?," + ExtensionIconTable.STATEMENT_SELECT_ICON_ID + "," + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID + ")";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, tabInformation.getTabName());
                statement.setString(2, ElementOrder.serialize(tabInformation.getTabElementOrder().orElse(ElementOrder.values())));
                statement.setInt(3, tabInformation.getTabPriority());
                ExtensionIconTable.set3IconValuesToStatement(statement, 4, tabInformation.getTabIcon());
                ExtensionPluginTable.set2PluginValuesToStatement(statement, 7, pluginName, serverUUID);
            }
        };
    }
}