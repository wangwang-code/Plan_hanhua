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
package com.djrapitops.plan;

import com.djrapitops.plan.commands.PlanProxyCommand;
import com.djrapitops.plan.modules.APFModule;
import com.djrapitops.plan.modules.PlaceholderModule;
import com.djrapitops.plan.modules.ProxySuperClassBindingModule;
import com.djrapitops.plan.modules.SystemObjectProvidingModule;
import com.djrapitops.plan.modules.bungee.BungeeCommandModule;
import com.djrapitops.plan.modules.bungee.BungeePlanModule;
import com.djrapitops.plan.modules.bungee.BungeeServerPropertiesModule;
import com.djrapitops.plan.modules.bungee.BungeeSuperClassBindingModule;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Dagger Component that constructs the plugin systems running on Bungee.
 *
 * @author Rsl1122
 */
@Singleton
@Component(modules = {
        BungeePlanModule.class,
        BungeeCommandModule.class,
        SystemObjectProvidingModule.class,
        APFModule.class,
        PlaceholderModule.class,

        ProxySuperClassBindingModule.class,
        BungeeSuperClassBindingModule.class,
        BungeeServerPropertiesModule.class
})
public interface PlanBungeeComponent {

    PlanProxyCommand planCommand();

    PlanSystem system();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder plan(PlanBungee plan);

        PlanBungeeComponent build();
    }
}