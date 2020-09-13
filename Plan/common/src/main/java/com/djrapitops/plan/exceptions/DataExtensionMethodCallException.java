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
package com.djrapitops.plan.exceptions;

import com.djrapitops.plan.extension.implementation.providers.MethodWrapper;

import java.util.Optional;

/**
 * Exception that is thrown when a call to a DataExtension method throws an exception.
 *
 * @author Rsl1122
 */
public class DataExtensionMethodCallException extends IllegalStateException {

    private final String pluginName;
    // Non serializable field due to Method not being serializable.
    private final transient MethodWrapper<?> method;

    public DataExtensionMethodCallException(Throwable cause, String pluginName, MethodWrapper<?> method) {
        super(cause);
        this.pluginName = pluginName;
        this.method = method;
    }

    public String getPluginName() {
        return pluginName;
    }

    public Optional<MethodWrapper<?>> getMethod() {
        // method is transient and might be lost if flushed to disk.
        return Optional.ofNullable(method);
    }
}