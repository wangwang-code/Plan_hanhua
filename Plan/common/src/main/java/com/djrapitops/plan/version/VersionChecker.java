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

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.utilities.java.Lists;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.api.utility.Version;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * System for checking if new Version is available when the System initializes.
 *
 * @author Rsl1122
 */
@Singleton
public class VersionChecker implements SubSystem {

    private final String currentVersion;
    private final Locale locale;
    private final PlanConfig config;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;
    private final PlanPlugin plugin;

    private VersionInfo newVersionAvailable;

    @Inject
    public VersionChecker(
            @Named("currentVersion") String currentVersion,
            Locale locale,
            PlanConfig config,
            PluginLogger logger,
            ErrorLogger errorLogger,
            PlanPlugin plugin
    ) {
        this.currentVersion = currentVersion;
        this.locale = locale;
        this.config = config;
        this.logger = logger;
        this.errorLogger = errorLogger;
        this.plugin = plugin;
    }

    public boolean isNewVersionAvailable() {
        return newVersionAvailable != null;
    }

    private void checkForUpdates() {
        try {
            List<VersionInfo> versions = VersionInfoLoader.load();
            if (config.isFalse(PluginSettings.NOTIFY_ABOUT_DEV_RELEASES)) {
                versions = Lists.filter(versions, VersionInfo::isRelease);
            }
            VersionInfo newestVersion = versions.get(0);
            if (Version.isNewVersionAvailable(new Version(currentVersion), newestVersion.getVersion())) {
                newVersionAvailable = newestVersion;
                String notification = locale.getString(
                        PluginLang.VERSION_AVAILABLE,
                        newestVersion.getVersion().toString(),
                        newestVersion.getChangeLogUrl()
                ) + (newestVersion.isRelease() ? "" : locale.getString(PluginLang.VERSION_AVAILABLE_DEV));
                logger.log(L.INFO_COLOR, "§a----------------------------------------");
                logger.log(L.INFO_COLOR, "§a" + notification);
                logger.log(L.INFO_COLOR, "§a----------------------------------------");
            } else {
                logger.info(locale.getString(PluginLang.VERSION_NEWEST));
            }
        } catch (IOException e) {
            errorLogger.log(L.WARN, e, ErrorContext.builder()
                    .related(locale.getString(PluginLang.VERSION_FAIL_READ_VERSIONS))
                    .whatToDo("Allow Plan to check for updates from Github/versions.txt or disable update check.")
                    .build());
        }
    }

    @Override
    public void enable() {
        if (config.isFalse(PluginSettings.CHECK_FOR_UPDATES)) {
            return;
        }
        plugin.getRunnableFactory().create("VersionChecker", new AbsRunnable() {
            @Override
            public void run() {
                checkForUpdates();
            }
        }).runTaskAsynchronously();
    }

    @Override
    public void disable() {
        /* Does not need to be closed */
    }

    public Optional<VersionInfo> getNewVersionAvailable() {
        return Optional.ofNullable(newVersionAvailable);
    }

    public Optional<String> getUpdateButton() {
        return getNewVersionAvailable()
                .map(v -> "<button class=\"btn bg-white col-plan\" data-target=\"#updateModal\" data-toggle=\"modal\" type=\"button\">" +
                        "<i class=\"fa fa-fw fa-download\"></i> v." + v.getVersion().getVersionString() +
                        "</button>"
                );
    }

    public String getCurrentVersionButton() {
        return "<button class=\"btn bg-plan\" data-target=\"#updateModal\" data-toggle=\"modal\" type=\"button\">" +
                "v." + getCurrentVersion() +
                "</button>";
    }

    public String getUpdateModal() {
        return getNewVersionAvailable()
                .map(v -> "<div class=\"modal-header\">" +
                        "<h5 class=\"modal-title\" id=\"updateModalLabel\">" +
                        "<i class=\"fa fa-fw fa-download\"></i> Version " + v.getVersion().getVersionString() + " is Available!" +
                        "</h5><button aria-label=\"Close\" class=\"close\" data-dismiss=\"modal\" type=\"button\"><span aria-hidden=\"true\">&times;</span></button>" +
                        "</div>" + // Close modal-header
                        "<div class=\"modal-body\">" +
                        "<p>A new version has been released and is now available for download." +
                        (v.isRelease() ? "" : "<br>This version is a DEV release.") + "</p>" +
                        "<a class=\"btn col-plan\" href=\"" + v.getChangeLogUrl() + "\" rel=\"noopener noreferrer\" target=\"_blank\">" +
                        "<i class=\"fa fa-fw fa-list\"></i> View Changelog</a>" +
                        "<a class=\"btn col-plan\" href=\"" + v.getDownloadUrl() + "\" rel=\"noopener noreferrer\" target=\"_blank\">" +
                        "<i class=\"fa fa-fw fa-download\"></i> Download Plan-" + v.getVersion().getVersionString() + ".jar</a>" +
                        "</div>") // Close modal-body
                .orElse("<div class=\"modal-header\">" +
                        "<h5 class=\"modal-title\" id=\"updateModalLabel\">" +
                        "<i class=\"far fa-fw fa-check-circle\"></i> You have version " + getCurrentVersion() + "" +
                        "</h5><button aria-label=\"Close\" class=\"close\" data-dismiss=\"modal\" type=\"button\"><span aria-hidden=\"true\">&times;</span></button>" +
                        "</div>" + // Close modal-header
                        "<div class=\"modal-body\">" +
                        "<p>You are running the latest version.</p>" +
                        "</div>"); // Close modal-body
    }

    public String getCurrentVersion() {
        return currentVersion;
    }
}
