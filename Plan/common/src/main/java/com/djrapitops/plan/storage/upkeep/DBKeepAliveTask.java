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
package com.djrapitops.plan.storage.upkeep;

import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.task.AbsRunnable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The task which handles the upkeep of the {@code Connection}
 *
 * @author Fuzzlemann
 */
public class DBKeepAliveTask extends AbsRunnable {
    private final IReconnect iReconnect;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;
    private Connection connection;

    public DBKeepAliveTask(Connection connection, IReconnect iReconnect, PluginLogger logger, ErrorLogger errorLogger) {
        this.connection = connection;
        this.iReconnect = iReconnect;
        this.logger = logger;
        this.errorLogger = errorLogger;
    }

    @Override
    public void run() {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            if (connection != null && !connection.isClosed()) {
                statement = connection.createStatement();
                resultSet = statement.executeQuery("/* ping */ SELECT 1");
            }
        } catch (SQLException pingException) {
            logger.debug("Something went wrong during SQL Connection upkeep task.");
            try {
                connection = iReconnect.reconnect();
            } catch (SQLException reconnectionError) {
                errorLogger.log(L.ERROR, reconnectionError, ErrorContext.builder()
                        .whatToDo("Reload Plan and Report this if the issue persists").build());
                logger.error("SQL connection maintaining task had to be closed due to exception.");
                this.cancel();
            }
        } finally {
            MiscUtils.close(statement, resultSet);
        }
    }

    public interface IReconnect {
        Connection reconnect() throws SQLException;
    }
}
