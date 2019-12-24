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
package net.babelsoft.negatron.view.control;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import net.babelsoft.negatron.io.configuration.InfotipTiming;

/**
 *
 * @author capan
 */
public class Infotip extends Tooltip {
    
    private final static List<Infotip> infotips = new ArrayList<>();
    private static Duration showDelay = InfotipTiming.AGGRESSIVE.getShowDelay();
    private static Duration showDuration = InfotipTiming.AGGRESSIVE.getShowDuration();
    private static Duration hideDelay = InfotipTiming.AGGRESSIVE.getHideDelay();
    
    private void initialise() {
        setShowDelay(showDelay);
        setShowDuration(showDuration);
        setHideDelay(hideDelay);
        infotips.add(this);
    }
    
    public Infotip() {
        super();
        initialise();
    }
    
    public Infotip(String msg) {
        super(msg);
        initialise();
    }
    
    public final static void setGlobalTimings(InfotipTiming timing) {
        Infotip.showDelay = timing.getShowDelay();
        Infotip.showDuration = timing.getShowDuration();
        Infotip.hideDelay = timing.getHideDelay();
        
        infotips.stream().forEach(infotip -> {
            infotip.setShowDelay(Infotip.showDelay);
            infotip.setShowDuration(Infotip.showDuration);
            infotip.setHideDelay(Infotip.hideDelay);
        });
    }
}
