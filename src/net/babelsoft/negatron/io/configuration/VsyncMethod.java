/*
 * This file is part of Negatron.
 * Copyright (C) 2015-2018 BabelSoft S.A.S.U.
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
package net.babelsoft.negatron.io.configuration;

import net.babelsoft.negatron.theme.Language;

/**
 *
 * @author capan
 */
public enum VsyncMethod {
    NONE("none", true, true),
    DOUBLE_BUFFERING("waitvsync", true, true),
    TRIPLE_BUFFERING("triplebuffer", false, true);
    
    final String name;
    final boolean windowedCompatible;
    final boolean fullscreenCompatible;
    
    VsyncMethod(String name, boolean windowedCompatible, boolean fullscreenCompatible) {
        this.name = name;
        this.windowedCompatible = windowedCompatible;
        this.fullscreenCompatible = fullscreenCompatible;
    }
    
    public boolean equals(String name) {
        return this.name.equals(name.trim());
    }
    
    @Override
    public String toString() {
        return Language.Manager.getString("vSync." + name);
    }
}
