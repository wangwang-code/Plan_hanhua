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
package com.djrapitops.plan.storage.database.transactions.events;

import com.djrapitops.plan.storage.database.queries.DataStoreQueries;
import com.djrapitops.plan.storage.database.queries.HasMoreThanZeroQueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.WorldTable;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Transaction to store world name after an event.
 *
 * @author Rsl1122
 */
public class WorldNameStoreTransaction extends Transaction {

    private final UUID serverUUID;
    private final String worldName;

    public WorldNameStoreTransaction(UUID serverUUID, String worldName) {
        this.serverUUID = serverUUID;
        this.worldName = worldName;
    }

    @Override
    protected boolean shouldBeExecuted() {
        return doesWorldNameNotExist();
    }

    private boolean doesWorldNameNotExist() {
        String sql = SELECT + "COUNT(1) as c" +
                FROM + WorldTable.TABLE_NAME +
                WHERE + WorldTable.NAME + "=?" +
                AND + WorldTable.SERVER_UUID + "=?";
        return !query(new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, StringUtils.truncate(worldName, 100));
                statement.setString(2, serverUUID.toString());
            }
        });
    }

    @Override
    protected void performOperations() {
        execute(DataStoreQueries.insertWorldName(serverUUID, worldName));
    }
}