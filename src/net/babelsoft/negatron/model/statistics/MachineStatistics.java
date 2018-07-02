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
package net.babelsoft.negatron.model.statistics;

import java.io.Serializable;
import net.babelsoft.negatron.model.ControllerType;
import net.babelsoft.negatron.model.item.Machine;

/**
 *
 * @author capan
 */
public class MachineStatistics implements Serializable {
    static final long serialVersionUID = 1L;
    
    private int deviceCount;

    private int gamblingPinballParentCount;
    private int arcadeGameParentCount;
    private int calculatorComputerParentCount;
    private int consoleParentCount;

    private int gamblingPinballCloneCount;
    private int arcadeGameCloneCount;
    private int calculatorComputerCloneCount;
    private int consoleCloneCount;

    public void add(Machine machine) {
        if (machine != null) {
            if (!machine.isRunnable())
                ++deviceCount;

            // very crude machine categorisation...
            else if (machine.isMechanical() ||
                machine.hasControllerType(ControllerType.gambling) ||
                machine.hasControllerType(ControllerType.hanafuda) ||
                machine.hasControllerType(ControllerType.mahjong)
            )
                if (machine.hasParent())
                    ++gamblingPinballParentCount;
                else
                    ++gamblingPinballCloneCount;
            else if (machine.hasCoinSlot())
                if (machine.hasParent())
                    ++arcadeGameParentCount;
                else
                    ++arcadeGameCloneCount;
            else if (machine.hasControllerType(ControllerType.keyboard))
                if (machine.hasParent())
                    ++calculatorComputerParentCount;
                else
                    ++calculatorComputerCloneCount;
            else
                if (machine.hasParent())
                    ++consoleParentCount;
                else
                    ++consoleCloneCount;
        } else // before MAME v0.186, non-runnable devices are ignored by Negatron, so also add them to the count from here
            ++deviceCount;
    }
    
    public int getDeviceCount() {
        return deviceCount;
    }
    
    public int getParentCount() {
        return
            getGamblingPinballParentCount() +
            getArcadeGameParentCount() +
            getCalculatorComputerParentCount() +
            getConsoleParentCount()
        ;
    }
    
    public int getCloneCount() {
        return
            getGamblingPinballCloneCount() +
            getArcadeGameCloneCount() +
            getCalculatorComputerCloneCount() +
            getConsoleCloneCount()
        ;
    }
    
    public int getTotalCount() {
        return getParentCount() + getCloneCount();
    }

    public int getGamblingPinballParentCount() {
        return gamblingPinballParentCount;
    }

    public int getArcadeGameParentCount() {
        return arcadeGameParentCount;
    }

    public int getCalculatorComputerParentCount() {
        return calculatorComputerParentCount;
    }

    public int getConsoleParentCount() {
        return consoleParentCount;
    }

    public int getGamblingPinballCloneCount() {
        return gamblingPinballCloneCount;
    }

    public int getArcadeGameCloneCount() {
        return arcadeGameCloneCount;
    }

    public int getCalculatorComputerCloneCount() {
        return calculatorComputerCloneCount;
    }

    public int getConsoleCloneCount() {
        return consoleCloneCount;
    }
}
