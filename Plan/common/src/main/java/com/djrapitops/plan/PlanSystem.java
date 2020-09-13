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

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.delivery.DeliveryUtilities;
import com.djrapitops.plan.delivery.export.ExportSystem;
import com.djrapitops.plan.delivery.web.ResolverSvc;
import com.djrapitops.plan.delivery.web.ResourceSvc;
import com.djrapitops.plan.delivery.webserver.NonProxyWebserverDisableChecker;
import com.djrapitops.plan.delivery.webserver.WebServerSystem;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.extension.ExtensionService;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.gathering.cache.CacheSystem;
import com.djrapitops.plan.gathering.importing.ImportSystem;
import com.djrapitops.plan.gathering.listeners.ListenerSystem;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.query.QuerySvc;
import com.djrapitops.plan.settings.ConfigSystem;
import com.djrapitops.plan.settings.SettingsSvc;
import com.djrapitops.plan.settings.locale.LocaleSystem;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plan.version.VersionChecker;
import com.djrapitops.plugin.benchmarking.Benchmark;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * PlanSystem contains everything Plan needs to run.
 * <p>
 * This is an abstraction layer on top of Plugin instances so that tests can be run with less mocks.
 *
 * @author Rsl1122
 */
@Singleton
public class PlanSystem implements SubSystem {

    private boolean enabled = false;

    private final PlanFiles files;
    private final ConfigSystem configSystem;
    private final VersionChecker versionChecker;
    private final LocaleSystem localeSystem;
    private final DBSystem databaseSystem;
    private final CacheSystem cacheSystem;
    private final ListenerSystem listenerSystem;
    private final TaskSystem taskSystem;
    private final ServerInfo serverInfo;
    private final WebServerSystem webServerSystem;

    private final Processing processing;

    private final ImportSystem importSystem;
    private final ExportSystem exportSystem;
    private final DeliveryUtilities deliveryUtilities;
    private final ResolverSvc resolverService;
    private final ResourceSvc resourceService;
    private final ExtensionSvc extensionService;
    private final QuerySvc queryService;
    private final SettingsSvc settingsService;
    private final PluginLogger logger;
    private final Timings timings;
    private final ErrorLogger errorLogger;

    @Inject
    public PlanSystem(
            PlanFiles files,
            ConfigSystem configSystem,
            VersionChecker versionChecker,
            LocaleSystem localeSystem,
            DBSystem databaseSystem,
            CacheSystem cacheSystem,
            ListenerSystem listenerSystem,
            TaskSystem taskSystem,
            ServerInfo serverInfo,
            WebServerSystem webServerSystem,
            Processing processing,
            ImportSystem importSystem,
            ExportSystem exportSystem,
            DeliveryUtilities deliveryUtilities,
            ResolverSvc resolverService,
            ResourceSvc resourceService,
            ExtensionSvc extensionService,
            QuerySvc queryService,
            SettingsSvc settingsService,
            PluginLogger logger,
            Timings timings,
            ErrorLogger errorLogger,
            PlanAPI.PlanAPIHolder apiHolder
    ) {
        this.files = files;
        this.configSystem = configSystem;
        this.versionChecker = versionChecker;
        this.localeSystem = localeSystem;
        this.databaseSystem = databaseSystem;
        this.cacheSystem = cacheSystem;
        this.listenerSystem = listenerSystem;
        this.taskSystem = taskSystem;
        this.serverInfo = serverInfo;
        this.webServerSystem = webServerSystem;
        this.processing = processing;
        this.importSystem = importSystem;
        this.exportSystem = exportSystem;
        this.deliveryUtilities = deliveryUtilities;
        this.resolverService = resolverService;
        this.resourceService = resourceService;
        this.extensionService = extensionService;
        this.queryService = queryService;
        this.settingsService = settingsService;
        this.logger = logger;
        this.timings = timings;
        this.errorLogger = errorLogger;

        logger.log(L.INFO_COLOR,
                "",
                "§2           ██▌",
                "§2     ██▌   ██▌",
                "§2  ██▌██▌██▌██▌  §2Player Analytics",
                "§2  ██▌██▌██▌██▌  §fv" + versionChecker.getCurrentVersion(),
                ""
        );
    }

    @Deprecated
    public String getMainAddress() {
        return webServerSystem.getAddresses().getMainAddress().orElse(webServerSystem.getAddresses().getFallbackLocalhostAddress());
    }

    @Override
    public void enable() throws EnableException {
        extensionService.register();
        resolverService.register();
        resourceService.register();
        settingsService.register();
        queryService.register();

        enableSystems(
                files,
                configSystem,
                localeSystem,
                versionChecker,
                databaseSystem,
                webServerSystem,
                processing,
                serverInfo,
                importSystem,
                exportSystem,
                cacheSystem,
                listenerSystem,
                taskSystem
        );

        // Disables Webserver if Proxy is detected in the database
        if (serverInfo.getServer().isNotProxy()) {
            processing.submitNonCritical(new NonProxyWebserverDisableChecker(
                    configSystem.getConfig(), webServerSystem.getAddresses(), webServerSystem, logger, errorLogger
            ));
        }

        extensionService.registerExtensions();
        enabled = true;
    }

    private void enableSystems(SubSystem... systems) throws EnableException {
        for (SubSystem system : systems) {
            logger.debug("Enabling: " + system.getClass().getSimpleName());
            timings.start("subsystem-enable");
            system.enable();
            timings.end("subsystem-enable")
                    .map(Benchmark::toDurationString)
                    .map(duration -> "Took " + duration)
                    .ifPresent(logger::debug);
        }
    }

    @Override
    public void disable() {
        enabled = false;
        disableSystems(
                taskSystem,
                cacheSystem,
                listenerSystem,
                importSystem,
                exportSystem,
                processing,
                databaseSystem,
                webServerSystem,
                serverInfo,
                localeSystem,
                configSystem,
                files,
                versionChecker
        );
    }

    private void disableSystems(SubSystem... systems) {
        for (SubSystem system : systems) {
            try {
                if (system != null) {
                    system.disable();
                }
            } catch (Exception e) {
                errorLogger.log(L.WARN, e, ErrorContext.builder().related("Disabling PlanSystem: " + system).build());
            }
        }
    }

    // Accessor methods.

    public VersionChecker getVersionChecker() {
        return versionChecker;
    }

    public ConfigSystem getConfigSystem() {
        return configSystem;
    }

    public PlanFiles getPlanFiles() {
        return files;
    }

    public DBSystem getDatabaseSystem() {
        return databaseSystem;
    }

    public ListenerSystem getListenerSystem() {
        return listenerSystem;
    }

    public TaskSystem getTaskSystem() {
        return taskSystem;
    }

    public WebServerSystem getWebServerSystem() {
        return webServerSystem;
    }

    public ImportSystem getImportSystem() {
        return importSystem;
    }

    public ExportSystem getExportSystem() {
        return exportSystem;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public CacheSystem getCacheSystem() {
        return cacheSystem;
    }

    public Processing getProcessing() {
        return processing;
    }

    public LocaleSystem getLocaleSystem() {
        return localeSystem;
    }

    public DeliveryUtilities getDeliveryUtilities() {
        return deliveryUtilities;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ExtensionService getExtensionService() {
        return extensionService;
    }

    public ErrorLogger getErrorLogger() {
        return errorLogger;
    }
}