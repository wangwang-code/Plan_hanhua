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
package com.djrapitops.plan.gathering.cache;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.gathering.geolocation.GeolocationCache;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * System that holds data caches of the plugin.
 *
 * @author Rsl1122
 */
@Singleton
public class CacheSystem implements SubSystem {

    private final SessionCache sessionCache;
    private final NicknameCache nicknameCache;
    private final GeolocationCache geolocationCache;

    @Inject
    public CacheSystem(
            SessionCache sessionCache,
            NicknameCache nicknameCache,
            GeolocationCache geolocationCache
    ) {
        this.sessionCache = sessionCache;
        this.nicknameCache = nicknameCache;
        this.geolocationCache = geolocationCache;
    }

    @Override
    public void enable() {
        nicknameCache.enable();
        geolocationCache.enable();
    }

    @Override
    public void disable() {
        geolocationCache.clearCache();
    }

    public NicknameCache getNicknameCache() {
        return nicknameCache;
    }

    public GeolocationCache getGeolocationCache() {
        return geolocationCache;
    }

    public SessionCache getSessionCache() {
        return sessionCache;
    }
}
