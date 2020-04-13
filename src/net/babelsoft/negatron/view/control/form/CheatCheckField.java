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
package net.babelsoft.negatron.view.control.form;

import javafx.scene.layout.GridPane;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.theme.Language;

/**
 *
 * @author capan
 */
public class CheatCheckField extends CheckField {
    
    public CheatCheckField(GridPane grid, int row, boolean isMess) {
        super(grid, row,
            Language.Manager.getString("cheatMenu"),
            isMess ? Language.Manager.getString("cheatMenu.tooltip").replaceFirst("MAME", "MESS") : Language.Manager.getString("cheatMenu.tooltip")
        );
        checkBox.setSelected(Configuration.Manager.isCheatMenuEnabled());
        checkBox.selectedProperty().addListener((o, oV, newValue) -> updateCheatMenuEnabled(newValue));
    }
}
