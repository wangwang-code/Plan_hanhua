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
package com.djrapitops.plan.delivery.rendering.pages;

import com.djrapitops.plan.delivery.formatting.PlaceholderReplacer;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plugin.api.Check;

/**
 * Html String generator for /login and /register page.
 *
 * @author Rsl1122
 */
public class LoginPage implements Page {

    private final String template;
    private final ServerInfo serverInfo;

    LoginPage(
            String htmlTemplate,
            ServerInfo serverInfo
    ) {
        this.template = htmlTemplate;
        this.serverInfo = serverInfo;
    }

    @Override
    public String toHtml() {
        PlaceholderReplacer placeholders = new PlaceholderReplacer();
        placeholders.put("command", getCommand());
        return placeholders.apply(template);
    }

    private String getCommand() {
        if (serverInfo.getServer().isNotProxy()) return "plan";
        if (Check.isBungeeAvailable()) return "planbungee";
        if (Check.isVelocityAvailable()) return "planvelocity";
        return "plan";
    }
}