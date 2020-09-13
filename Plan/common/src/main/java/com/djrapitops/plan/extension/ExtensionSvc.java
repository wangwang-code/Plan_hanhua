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
package com.djrapitops.plan.extension;

import com.djrapitops.plan.DebugChannels;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONCache;
import com.djrapitops.plan.exceptions.DataExtensionMethodCallException;
import com.djrapitops.plan.extension.implementation.CallerImplementation;
import com.djrapitops.plan.extension.implementation.ExtensionRegister;
import com.djrapitops.plan.extension.implementation.ExtensionWrapper;
import com.djrapitops.plan.extension.implementation.providers.MethodWrapper;
import com.djrapitops.plan.extension.implementation.providers.gathering.ProviderValueGatherer;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.ExtensionSettings;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation for {@link ExtensionService}.
 *
 * @author Rsl1122
 */
@Singleton
public class ExtensionSvc implements ExtensionService {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final Processing processing;
    private final ExtensionRegister extensionRegister;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    private final Map<String, ProviderValueGatherer> extensionGatherers;

    @Inject
    public ExtensionSvc(
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Processing processing,
            ExtensionRegister extensionRegister,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.processing = processing;
        this.extensionRegister = extensionRegister;
        this.logger = logger;
        this.errorLogger = errorLogger;

        extensionGatherers = new HashMap<>();
    }

    public void register() {
        Holder.set(this);
    }

    public void registerExtensions() {
        try {
            extensionRegister.registerBuiltInExtensions(config.getExtensionSettings().getDisabled());
        } catch (IllegalStateException failedToRegisterOne) {
            ErrorContext.Builder context = ErrorContext.builder()
                    .whatToDo("Report and/or disable the failed extensions. You can find the failed extensions in the error file.");
            for (Throwable suppressedException : failedToRegisterOne.getSuppressed()) {
                context.related(suppressedException.getMessage());
            }

            logger.warn("One or more extensions failed to register (They can be disabled in Plan config).");
            errorLogger.log(L.WARN, failedToRegisterOne, context.build());
        }
    }

    @Override
    public Optional<Caller> register(DataExtension extension) {
        ExtensionWrapper extractor = new ExtensionWrapper(extension);
        String pluginName = extractor.getPluginName();

        if (shouldNotAllowRegistration(pluginName)) return Optional.empty();

        for (String warning : extractor.getWarnings()) {
            logger.warn("DataExtension API implementation mistake for " + pluginName + ": " + warning);
        }

        ProviderValueGatherer gatherer = new ProviderValueGatherer(extractor, dbSystem, serverInfo);
        gatherer.storeExtensionInformation();
        extensionGatherers.put(pluginName, gatherer);

        processing.submitNonCritical(() -> updateServerValues(gatherer, CallEvents.SERVER_EXTENSION_REGISTER));

        logger.info("Registered extension: " + pluginName);
        return Optional.of(new CallerImplementation(gatherer, this, processing));
    }

    @Override
    public void unregister(DataExtension extension) {
        ExtensionWrapper extractor = new ExtensionWrapper(extension);
        String pluginName = extractor.getPluginName();
        if (extensionGatherers.remove(pluginName) != null) {
            logger.getDebugLogger().logOn(DebugChannels.DATA_EXTENSIONS, pluginName + " extension unregistered.");
        }
    }

    private boolean shouldNotAllowRegistration(String pluginName) {
        ExtensionSettings pluginsConfig = config.getExtensionSettings();

        if (!pluginsConfig.hasSection(pluginName)) {
            try {
                pluginsConfig.createSection(pluginName);
            } catch (IOException e) {
                errorLogger.log(L.WARN, e, ErrorContext.builder()
                        .whatToDo("Create 'Plugins." + pluginName + ".Enabled: true' setting manually.")
                        .related("Section: " + pluginName).build());
                logger.warn("Could not register DataExtension for " + pluginName + " due to " + e.toString());
                return true;
            }
        }

        if (!pluginsConfig.isEnabled(pluginName)) {
            logger.getDebugLogger().logOn(DebugChannels.DATA_EXTENSIONS, pluginName + " extension disabled in the config.");
            return true;
        }
        return false; // Should register.
    }

    public void updatePlayerValues(UUID playerUUID, String playerName, CallEvents event) {
        for (ProviderValueGatherer gatherer : extensionGatherers.values()) {
            updatePlayerValues(gatherer, playerUUID, playerName, event);
        }
    }

    public void updatePlayerValues(ProviderValueGatherer gatherer, UUID playerUUID, String playerName, CallEvents event) {
        if (gatherer.shouldSkipEvent(event)) return;
        if (playerUUID == null && playerName == null) return;

        try {
            logger.getDebugLogger().logOn(DebugChannels.DATA_EXTENSIONS, "Gathering values for: " + playerName);

            gatherer.updateValues(playerUUID, playerName);

            logger.getDebugLogger().logOn(DebugChannels.DATA_EXTENSIONS, "Gathering completed:  " + playerName);
        } catch (DataExtensionMethodCallException methodCallFailed) {
            logFailure(playerName, methodCallFailed);
            methodCallFailed.getMethod().ifPresent(gatherer::disableMethodFromUse);
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError unexpectedError) {
            ErrorContext.Builder context = ErrorContext.builder()
                    .whatToDo("Report and/or disable " + gatherer.getPluginName() + " extension in the Plan config.")
                    .related(gatherer.getPluginName())
                    .related(event)
                    .related("Player: " + playerName + " " + playerUUID);
            errorLogger.log(L.WARN, unexpectedError, context.build());
        }
    }

    private void logFailure(String playerName, DataExtensionMethodCallException methodCallFailed) {
        Throwable cause = methodCallFailed.getCause();
        ErrorContext.Builder context = ErrorContext.builder()
                .whatToDo("Report and/or disable " + methodCallFailed.getPluginName() + " extension in the Plan config.")
                .related(methodCallFailed.getPluginName())
                .related("Method:" + methodCallFailed.getMethod().map(MethodWrapper::getMethodName).orElse("-"))
                .related("Player: " + playerName);
        errorLogger.log(L.WARN, cause, context.build());
    }

    public void updateServerValues(CallEvents event) {
        for (ProviderValueGatherer gatherer : extensionGatherers.values()) {
            updateServerValues(gatherer, event);
        }
        UUID serverUUID = serverInfo.getServerUUID();
        JSONCache.invalidate(DataID.EXTENSION_NAV, serverUUID);
        JSONCache.invalidate(DataID.EXTENSION_TABS, serverUUID);
    }

    public void updateServerValues(ProviderValueGatherer gatherer, CallEvents event) {
        if (gatherer.shouldSkipEvent(event)) return;

        try {
            logger.getDebugLogger().logOn(DebugChannels.DATA_EXTENSIONS, "Gathering values for server");

            gatherer.updateValues();

            logger.getDebugLogger().logOn(DebugChannels.DATA_EXTENSIONS, "Gathering completed for server");
        } catch (DataExtensionMethodCallException methodCallFailed) {
            logFailure("server", methodCallFailed);
            methodCallFailed.getMethod().ifPresent(gatherer::disableMethodFromUse);
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError unexpectedError) {
            ErrorContext.Builder context = ErrorContext.builder()
                    .whatToDo("Report and/or disable " + gatherer.getPluginName() + " extension in the Plan config.")
                    .related(gatherer.getPluginName())
                    .related(event)
                    .related("Gathering for server");
            errorLogger.log(L.WARN, unexpectedError, context.build());
        }
    }
}