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
package com.djrapitops.plan.extension.implementation.storage.transactions.results;

import com.djrapitops.plan.extension.implementation.providers.DataProvider;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionProviderTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;
import static com.djrapitops.plan.storage.database.sql.tables.ExtensionServerValueTable.*;

/**
 * Transaction to store Extension String data for a server.
 *
 * @author Rsl1122
 */
public class StoreServerStringResultTransaction extends ThrowawayTransaction {

    private final String pluginName;
    private final UUID serverUUID;
    private final String providerName;

    private final String value;

    public StoreServerStringResultTransaction(DataProvider<String> provider, Parameters parameters, String value) {
        this.pluginName = provider.getProviderInformation().getPluginName();
        this.providerName = provider.getProviderInformation().getName();
        this.serverUUID = parameters.getServerUUID();
        this.value = StringUtils.truncate(value, 50);
    }

    @Override
    protected void performOperations() {
        execute(storeValue());
    }

    private Executable storeValue() {
        return connection -> {
            if (!updateValue().execute(connection)) {
                return insertValue().execute(connection);
            }
            return false;
        };
    }

    private Executable updateValue() {
        String sql = "UPDATE " + TABLE_NAME +
                " SET " +
                STRING_VALUE + "=?" +
                WHERE + PROVIDER_ID + "=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID;

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, value);
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 2, providerName, pluginName, serverUUID);
            }
        };
    }

    private Executable insertValue() {
        String sql = "INSERT INTO " + TABLE_NAME + "(" +
                STRING_VALUE + "," +
                PROVIDER_ID +
                ") VALUES (?," + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID + ")";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, value);
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 2, providerName, pluginName, serverUUID);
            }
        };
    }
}