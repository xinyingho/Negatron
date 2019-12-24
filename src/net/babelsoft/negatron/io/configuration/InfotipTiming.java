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
package net.babelsoft.negatron.io.configuration;

import javafx.util.Duration;

/**
 *
 * @author capan
 */
public enum InfotipTiming {
    AGGRESSIVE("aggressive", 200, 15000, 200),
    MILD("mild", 1000, 5000, 200),
    DISABLED("disabled", 0, 0, 0);
 
    final String name;
    final Duration showDelay;
    final Duration showDuration;
    final Duration hideDelay;
    
    InfotipTiming(String name, long showDelay, long showDuration, long hideDelay) {
        this.name = name;
        this.showDelay = new Duration(showDelay);
        this.showDuration = new Duration(showDuration);
        this.hideDelay = new Duration(hideDelay);
    }
    
    public String getName() {
        return name;
    }
    
    public Duration getShowDelay() {
        return showDelay;
    }
    
    public Duration getShowDuration() {
        return showDuration;
    }
    
    public Duration getHideDelay() {
        return hideDelay;
    }
}
