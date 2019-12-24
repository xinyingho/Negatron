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
package net.babelsoft.negatron.view.control.form;

import java.util.Arrays;
import java.util.List;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.InfotipTiming;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.view.control.Infotip;

/**
 *
 * @author capan
 */
public class InfotipTimingChoiceField extends ChoiceField<InfotipTiming> {
    
    public InfotipTimingChoiceField(GridPane grid, int row) {
        super(grid, row, Language.Manager.getString("infotipTiming"), Language.Manager.getString("infotipTiming.tooltip"));
        
        List<InfotipTiming> list = Arrays.asList(InfotipTiming.values());
        choiceBox.getItems().addAll(list);

        String init = Configuration.Manager.getInfotipTiming();
        
        list.stream().filter(
            constant -> init.equals(constant.getName())
        ).findAny().ifPresent(constant -> {
            choiceBox.getSelectionModel().select(constant);
            Infotip.setGlobalTimings(constant);
        });

        choiceBox.getSelectionModel().selectedItemProperty().addListener((o, oV, newValue) -> {
            updateInfotipTiming(newValue.getName());
            Infotip.setGlobalTimings(newValue);
        });
        
        choiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(InfotipTiming infoTiming) {
                return Language.Manager.getString("infotipTiming." + infoTiming.getName());
            }

            @Override
            public InfotipTiming fromString(String string) {
                throw new UnsupportedOperationException("Should never be called.");
            }
        });
    }
}
