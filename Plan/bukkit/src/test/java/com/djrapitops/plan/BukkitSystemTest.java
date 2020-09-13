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

import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.settings.ConfigSettingKeyTest;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.config.paths.key.Setting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utilities.RandomData;
import utilities.mocks.BukkitMockComponent;

import java.nio.file.Path;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test for Bukkit PlanSystem.
 *
 * @author Rsl1122
 */
public class BukkitSystemTest {

    private final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);
    private PlanSystem system;

    @BeforeEach
    void prepareSystem(@TempDir Path temp) throws Exception {
        system = new BukkitMockComponent(temp).getPlanSystem();
        system.getConfigSystem().getConfig()
                .set(WebserverSettings.PORT, TEST_PORT_NUMBER);
    }

    @Test
    void bukkitSystemEnables() throws EnableException {
        try {
            system.enable();
            assertTrue(system.isEnabled());
        } finally {
            system.disable();
        }
    }

    @Test
    void bukkitSystemHasDefaultConfigValuesAfterEnable() throws EnableException, IllegalAccessException {
        try {
            system.enable();
            PlanConfig config = system.getConfigSystem().getConfig();

            Collection<Setting> serverSettings = ConfigSettingKeyTest.getServerSettings();
            ConfigSettingKeyTest.assertValidDefaultValuesForAllSettings(config, serverSettings);
        } finally {
            system.disable();
        }
    }
}
