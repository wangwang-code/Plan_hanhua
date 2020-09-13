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
package com.djrapitops.plan.identification;

import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * Utility for getting server identifier from different sources.
 *
 * @author Rsl1122
 */
@Singleton
public class Identifiers {

    protected final DBSystem dbSystem;
    private final UUIDUtility uuidUtility;

    @Inject
    public Identifiers(DBSystem dbSystem, UUIDUtility uuidUtility) {
        this.dbSystem = dbSystem;
        this.uuidUtility = uuidUtility;
    }

    /**
     * Obtain UUID of the server.
     *
     * @param request for Request, URIQuery needs a 'server' parameter.
     * @return UUID of the server.
     * @throws BadRequestException If server parameter is not defined or the server is not in the database.
     */
    public UUID getServerUUID(Request request) {
        String serverIndentifier = request.getQuery().get("server")
                .orElseThrow(() -> new BadRequestException("'server' parameter was not defined."));

        Optional<UUID> parsed = UUIDUtility.parseFromString(serverIndentifier);
        return parsed.orElseGet(() -> getServerUUIDFromName(serverIndentifier));
    }

    private UUID getServerUUIDFromName(String serverName) {
        return dbSystem.getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverName))
                .map(Server::getUuid)
                .orElseThrow(() -> new BadRequestException("Given 'server' was not found in the database: '" + serverName + "'"));
    }

    /**
     * Obtain UUID of the player.
     *
     * @param request for Request, URIQuery needs a 'player' parameter.
     * @return UUID of the player.
     * @throws BadRequestException If player parameter is not defined or the player is not in the database.
     */
    public UUID getPlayerUUID(Request request) {
        String playerIdentifier = request.getQuery().get("player")
                .orElseThrow(() -> new BadRequestException("'player' parameter was not defined.")).trim();

        Optional<UUID> parsed = UUIDUtility.parseFromString(playerIdentifier);
        return parsed.orElseGet(() -> getPlayerUUIDFromName(playerIdentifier));
    }

    private UUID getPlayerUUIDFromName(String playerName) {
        return dbSystem.getDatabase()
                .query(UserIdentifierQueries.fetchPlayerUUIDOf(playerName))
                .orElseThrow(() -> new BadRequestException("Given 'player' was not found in the database: '" + playerName + "'"));
    }

    public UUID getPlayerUUID(String name) {
        return uuidUtility.getUUIDOf(name);
    }
}