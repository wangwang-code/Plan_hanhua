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
package com.djrapitops.plan.version;

import com.djrapitops.plugin.api.utility.Version;

import java.util.Objects;

/**
 * Data class for reading version.txt in https://github.com/Rsl1122/Plan-PlayerAnalytics.
 *
 * @author Rsl1122
 */
public class VersionInfo implements Comparable<VersionInfo> {

    private final boolean release;
    private final Version version;
    private final String downloadUrl;
    private final String changeLogUrl;

    public VersionInfo(boolean release, Version version, String downloadUrl, String changeLogUrl) {
        this.release = release;
        this.version = version;
        this.downloadUrl = downloadUrl;
        this.changeLogUrl = changeLogUrl;
    }

    public boolean isRelease() {
        return release;
    }

    public Version getVersion() {
        return version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getChangeLogUrl() {
        return changeLogUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionInfo that = (VersionInfo) o;
        return release == that.release &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(release, version);
    }

    @Override
    public int compareTo(VersionInfo o) {
        return o.version.compareTo(this.version);
    }
}