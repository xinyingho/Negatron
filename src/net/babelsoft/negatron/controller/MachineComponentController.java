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
package net.babelsoft.negatron.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.beans.property.Property;
import javafx.fxml.Initializable;
import net.babelsoft.negatron.model.component.MachineComponent;
import net.babelsoft.negatron.util.Disposable;
import net.babelsoft.negatron.view.control.adapter.FocusData;

/**
 *
 * @author capan
 */
public abstract class MachineComponentController<T extends MachineComponent<S, P>, S, P extends Property<S>> implements Initializable, Disposable {

    private T machineComponent;
    private Consumer<String> onDataUpdated;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) { }
    
    public void setMachineComponent(T machineComponent) {
        this.machineComponent = machineComponent;
    }
    
    public T getMachineComponent() {
        return machineComponent;
    }
    
    public void setOnDataUpdated(Consumer<String> consumer) {
        onDataUpdated = consumer;
    }
    
    protected void fireDataUpdated(String origin) {
        if (onDataUpdated != null)
            onDataUpdated.accept(origin);
    }
    
    public abstract boolean isFocused();
    
    public abstract FocusData getFocusData();
    
    public boolean requestFocus(FocusData focusData) {
        if (focusData == null)
            return false;
        
        @SuppressWarnings("unchecked")
        T component = (T) focusData.getComponent();
        
        return component != null && component.canReplace(machineComponent);
    }
}
