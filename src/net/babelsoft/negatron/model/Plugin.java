/*
 * This file is part of Negatron.
 * Copyright (C) 2015-2020 BabelSoft S.A.S.U.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.babelsoft.negatron.model;

/**
 *
 * @author capan
 */
public class Plugin implements Comparable<Plugin> {
    
    private final String name;
    private final boolean enabledByDefault;
    private boolean enabled;

    public Plugin(String name, boolean enabledByDefault) {
        this.name = name;
        this.enabledByDefault = enabledByDefault;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the enabledByDefault
     */
    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int compareTo(Plugin plugin) {
        if (this == plugin)
            return 0;
        return this.name.compareTo(plugin.name);
    }
}
