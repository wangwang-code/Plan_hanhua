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

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a Server that is running Plan.
 *
 * @author Rsl1122
 */
public class Server implements Comparable<Server> {
    private final UUID uuid;
    private int id;
    private String name;
    private String webAddress;
    private int maxPlayers;

    public Server(int id, UUID uuid, String name, String webAddress, int maxPlayers) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.webAddress = webAddress;
        this.maxPlayers = maxPlayers;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getIdentifiableName() {
        return !"Plan".equalsIgnoreCase(name) ? name : "Server " + id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebAddress() {
        return webAddress;
    }

    public void setWebAddress(String webAddress) {
        this.webAddress = webAddress;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Server that = (Server) o;
        return Objects.equals(uuid, that.uuid) &&
                Objects.equals(name, that.name) &&
                Objects.equals(webAddress, that.webAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, id, name, webAddress);
    }

    @Override
    public String toString() {
        return "Server{" +
                "uuid=" + uuid +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", webAddress='" + webAddress + '\'' +
                ", maxPlayers=" + maxPlayers +
                '}';
    }

    @Override
    public int compareTo(Server other) {
        return Integer.compare(this.id, other.id);
    }

    public boolean isProxy() {
        return "BungeeCord".equals(name);
    }

    public boolean isNotProxy() {
        return !isProxy();
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
}
