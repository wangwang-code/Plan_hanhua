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
package com.djrapitops.plan.storage.database;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import utilities.TestConstants;

import java.util.UUID;

public interface DatabaseTestPreparer {

    String[] worlds = new String[]{"TestWorld", "TestWorld2"};
    UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
    UUID player2UUID = TestConstants.PLAYER_TWO_UUID;

    Database db();

    UUID serverUUID();

    PlanSystem system();

    default void execute(Executable executable) {
        db().executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(executable);
            }
        });
    }

    default void executeTransactions(Transaction... transactions) {
        for (Transaction transaction : transactions) {
            db().executeTransaction(transaction);
        }
    }

    default void forcePersistenceCheck() {
        db().close();
        db().init();
    }
}
