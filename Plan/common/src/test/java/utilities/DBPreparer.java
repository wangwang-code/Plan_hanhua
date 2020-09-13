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
package utilities;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DatabaseSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plugin.utilities.Format;
import com.djrapitops.plugin.utilities.Verify;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Optional;

public class DBPreparer {

    private final PlanSystem system;
    private final int testPortNumber;

    public DBPreparer(PlanSystem system, int testPortNumber) {
        this.system = system;
        this.testPortNumber = testPortNumber;
    }

    public Optional<Database> prepareSQLite() throws EnableException {
        String dbName = DBType.SQLITE.getName();
        return Optional.of(prepareDBByName(dbName));
    }

    public Optional<Database> prepareH2() throws EnableException {
        String dbName = DBType.H2.getName();
        return Optional.of(prepareDBByName(dbName));
    }

    private SQLDB prepareDBByName(String dbName) throws EnableException {
        PlanConfig config = system.getConfigSystem().getConfig();
        config.set(WebserverSettings.PORT, testPortNumber);
        config.set(DatabaseSettings.TYPE, dbName);
        system.enable();

        DBSystem dbSystem = system.getDatabaseSystem();
        SQLDB db = (SQLDB) dbSystem.getActiveDatabaseByName(dbName);
        db.setTransactionExecutorServiceProvider(MoreExecutors::newDirectExecutorService);
        db.init();
        return db;
    }

    public Optional<String> setUpMySQLSettings(PlanConfig config) {
        String database = System.getenv(CIProperties.MYSQL_DATABASE);
        String user = System.getenv(CIProperties.MYSQL_USER);
        String pass = System.getenv(CIProperties.MYSQL_PASS);
        String port = System.getenv(CIProperties.MYSQL_PORT);
        if (Verify.containsNull(database, user)) {
            return Optional.empty();
        }

        // Attempt to Prevent SQL Injection with Environment variable.
        String formattedDatabase = new Format(database)
                .removeSymbols()
                .toString();

        String dbName = DBType.MYSQL.getName();

        config.set(DatabaseSettings.MYSQL_DATABASE, formattedDatabase);
        config.set(DatabaseSettings.MYSQL_USER, user);
        config.set(DatabaseSettings.MYSQL_PASS, pass != null ? pass : "");
        config.set(DatabaseSettings.MYSQL_HOST, "127.0.0.1");
        config.set(DatabaseSettings.MYSQL_PORT, port != null ? port : "3306");
        config.set(DatabaseSettings.TYPE, dbName);
        return Optional.of(formattedDatabase);
    }

    public Optional<Database> prepareMySQL() throws EnableException {
        PlanConfig config = system.getConfigSystem().getConfig();
        Optional<String> formattedDB = setUpMySQLSettings(config);
        if (formattedDB.isPresent()) {
            String formattedDatabase = formattedDB.get();
            SQLDB mysql = prepareDBByName(DBType.MYSQL.getName());
            mysql.executeTransaction(new Transaction() {
                @Override
                protected void performOperations() {
                    execute("DROP DATABASE " + formattedDatabase);
                    execute("CREATE DATABASE " + formattedDatabase);
                    execute("USE " + formattedDatabase);
                }
            });
            return Optional.of(mysql);
        }
        return Optional.empty();
    }
}
