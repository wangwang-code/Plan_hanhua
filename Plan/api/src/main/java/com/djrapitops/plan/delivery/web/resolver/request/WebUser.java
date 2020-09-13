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
package com.djrapitops.plan.delivery.web.resolver.request;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class WebUser {

    private final String playerName;
    private final String username;
    private final Set<String> permissions;

    public WebUser(String playerName) {
        this.playerName = playerName;
        this.username = playerName;
        this.permissions = new HashSet<>();
    }

    public WebUser(String playerName, String username, Collection<String> permissions) {
        this.playerName = playerName;
        this.username = username;
        this.permissions = new HashSet<>(permissions);
    }

    /**
     * @deprecated WebUser now has username and player name.
     */
    @Deprecated
    public WebUser(String playerName, String... permissions) {
        this(playerName);
        this.permissions.addAll(Arrays.asList(permissions));
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public String getName() {
        return playerName;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "WebUser{" +
                "playerName='" + playerName + '\'' +
                ", username='" + username + '\'' +
                ", permissions=" + permissions +
                '}';
    }
}
