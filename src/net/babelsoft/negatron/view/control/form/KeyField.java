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

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.view.control.Infotip;
import net.babelsoft.negatron.view.control.adapter.KeyCodeConverter;

/**
 *
 * @author capan
 */
public class KeyField extends Field {
    
    private final TextField text;
    
    public KeyField(GridPane grid, int row, String key) {
        Label label = new Label(Language.Manager.getString("globalConf." + key));
        grid.add(label, 0, row);
        
        text = new TextField(Configuration.Manager.getGlobalConfiguration(key));
        text.setTooltip(new Infotip(Language.Manager.tryGetString("globalConf." + key + ".tooltip")));
        text.setOnKeyReleased(evt -> {
            String code = KeyCodeConverter.convert(evt.getCode());
            text.setText(code);
            evt.consume();
        });
        grid.add(text, 1, row);
        
        // TODO: enable this with low-level keyboard input management
        text.setDisable(true);
    }
}
