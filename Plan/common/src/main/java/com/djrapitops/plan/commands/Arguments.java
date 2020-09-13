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
package com.djrapitops.plan.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Utility for managing command arguments.
 *
 * @author Rsl1122
 */
public class Arguments {

    private final List<String> args;

    public Arguments(String[] args) {
        this.args = Arrays.asList(args);
    }

    public Arguments(List<String> args) {
        this.args = args;
    }

    public Optional<String> get(int index) {
        return index < args.size() ? Optional.of(args.get(index)) : Optional.empty();
    }

    public Optional<Integer> getInteger(int index) {
        return get(index).map(Integer::parseInt);
    }

    public Optional<String> getAfter(String argumentIdentifier) {
        for (int i = 0; i < args.size(); i++) {
            String argument = args.get(i);
            if (argumentIdentifier.equals(argument)) {
                return get(i + 1);
            }
        }
        return Optional.empty();
    }

    public boolean contains(String argument) {
        return args.contains(argument);
    }

    public List<String> asList() {
        return args;
    }
}
