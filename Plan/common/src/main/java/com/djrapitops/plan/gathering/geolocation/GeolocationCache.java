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
package com.djrapitops.plan.gathering.geolocation;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.exceptions.PreparationException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * This class contains the geolocation cache.
 * <p>
 * It caches all IPs with their matching country.
 *
 * @author Rsl1122
 * @author Fuzzlemann
 */
@Singleton
public class GeolocationCache implements SubSystem {

    private final Locale locale;
    private final PlanConfig config;
    private final PluginLogger logger;
    private final RunnableFactory runnableFactory;
    private final Cache<String, String> cache;

    private final Geolocator geoLite2Geolocator;
    private final Geolocator ip2cGeolocator;

    private Geolocator inUseGeolocator;

    @Inject
    public GeolocationCache(
            Locale locale,
            PlanConfig config,
            GeoLite2Geolocator geoLite2Geolocator,
            IP2CGeolocator ip2cGeolocator,
            PluginLogger logger,
            RunnableFactory runnableFactory
    ) {
        this.locale = locale;
        this.config = config;
        this.geoLite2Geolocator = geoLite2Geolocator;
        this.ip2cGeolocator = ip2cGeolocator;
        this.logger = logger;
        this.runnableFactory = runnableFactory;

        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public void enable() {
        if (config.isTrue(DataGatheringSettings.GEOLOCATIONS)) {
            runnableFactory.create("Geolocator init", new AbsRunnable() {
                @Override
                public void run() {
                    if (inUseGeolocator == null) tryToPrepareGeoLite2();
                    if (inUseGeolocator == null) tryToPrepareIP2CGeolocator();
                    if (inUseGeolocator == null) logger.error("Failed to enable geolocation.");
                }
            }).runTaskAsynchronously();
        } else {
            logger.info(locale.getString(PluginLang.ENABLE_NOTIFY_GEOLOCATIONS_DISABLED));
        }
    }

    public boolean canGeolocate() {
        return inUseGeolocator != null;
    }

    private void tryToPrepareIP2CGeolocator() {
        logger.warn("Fallback: using IP2C for Geolocation (doesn't support IPv6).");
        try {
            ip2cGeolocator.prepare();
            inUseGeolocator = ip2cGeolocator;
        } catch (PreparationException e) {
            logger.warn(e.getMessage());
        } catch (IOException e) {
            logger.error("Fallback to IP2C failed: " + e.getMessage());
        }
    }

    public void tryToPrepareGeoLite2() {
        try {
            geoLite2Geolocator.prepare();
            inUseGeolocator = geoLite2Geolocator;
        } catch (PreparationException e) {
            logger.info(e.getMessage());
        } catch (UnknownHostException e) {
            logger.error(locale.getString(PluginLang.ENABLE_NOTIFY_GEOLOCATIONS_INTERNET_REQUIRED));
        } catch (IOException e) {
            logger.error(locale.getString(PluginLang.ENABLE_FAIL_GEODB_WRITE) + ": " + e.getMessage());
        }
    }

    /**
     * Retrieves the country in full length (e.g. United States) from the IP Address.
     *
     * @param ipAddress The IP Address for which the country is retrieved
     * @return The name of the country in full length or null if the country could not be fetched.
     */
    public String getCountry(String ipAddress) {
        return cache.get(ipAddress, this::getUnCachedCountry);
    }

    /**
     * Retrieves the country in full length (e.g. United States) from the IP Address.
     */
    private String getUnCachedCountry(String ipAddress) {
        if (inUseGeolocator == null) return null;
        return inUseGeolocator.getCountry(ipAddress).orElse("Not Found");
    }

    /**
     * Checks if the IP Address is cached
     *
     * @param ipAddress The IP Address which is checked
     * @return true if the IP Address is cached
     */
    boolean isCached(String ipAddress) {
        return cache.getIfPresent(ipAddress) != null;
    }

    @Override
    public void disable() {
        clearCache();
    }

    /**
     * Clears the cache
     */
    public void clearCache() {
        cache.invalidateAll();
        cache.cleanUp();
    }
}
