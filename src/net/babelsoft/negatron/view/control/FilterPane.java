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
package net.babelsoft.negatron.view.control;

import java.io.IOException;
import java.util.function.Consumer;
import javafx.fxml.FXMLLoader;
import net.babelsoft.negatron.controller.FilterPaneController;
import net.babelsoft.negatron.model.item.EmulatedItem;
import net.babelsoft.negatron.theme.Language;

/**
 *
 * @author capan
 */
public abstract class FilterPane<T extends EmulatedItem<T>, C extends FilterPaneController<T>> extends TitledWindowPane {
    
    protected final C controller;
    
    protected FilterPane(String fxmlPath) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath), Language.Manager.getBundle());
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
            controller = fxmlLoader.getController();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    public void setTreeView(EmulatedItemTreeView<T> treeView) {
        controller.setTreeView(treeView);
    }
    
    public void setOnFilter(Consumer<Boolean> onFilter) {
        controller.setOnFilter(onFilter);
    }

    public void disableStatusCriteria(boolean disable) {
        controller.disableStatusCriteria(disable);
    }

    public void bind(EmulatedItemTreePane<T> treePane) {
        treePane.unbindFilter();
        controller.bind(treePane.filterProperty());
    }
}
