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
public enum UIFontProvider implements OSD {
    AUTO("auto", true, true, true),
    WIN("win", true, false, false),
    DWRITE("dwrite", true, false, false),
    OSX("osx", false, true, false),
    SDL("sdl", false, false, true),
    NONE("none", true, true, true);
    
    final String name;
    final boolean windowsCompatible;
    final boolean macCompatible;
    final boolean linuxCompatible;
    
    UIFontProvider(String name, boolean windowsCompatible, boolean macCompatible, boolean linuxCompatible) {
        this.name = name;
        this.windowsCompatible = windowsCompatible;
        this.macCompatible = macCompatible;
        this.linuxCompatible = linuxCompatible;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isWindowsCompatible() {
        return windowsCompatible;
    }

    @Override
    public boolean isMacCompatible() {
        return macCompatible;
    }

    @Override
    public boolean isLinuxCompatible() {
        return linuxCompatible;
    }
    
    @Override
    public String toString() {
        return Language.Manager.getString("uifontprovider." + name);
    }
}
