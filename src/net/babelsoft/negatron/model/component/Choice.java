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
package net.babelsoft.negatron.model.component;

import java.util.ArrayList;
import java.util.List;
import net.babelsoft.negatron.model.Option;
import net.babelsoft.negatron.model.OptionProperty;

/**
 *
 * @author capan
 */
public abstract class Choice<T extends Option<T>> extends MachineElement<T> implements MachineComponent<T, OptionProperty<T>> {
    private static final long serialVersionUID = 1L;
    
    private final List<T> options = new ArrayList<>();
    private OptionProperty<T> value;
    protected T defaultValue;
    
    private void initialise(Choice<T> ref) {
        ref.options.forEach(option -> {
            T cloned = option.copy();
            if (option == ref.defaultValue)
                defaultValue = cloned;
            options.add(cloned);
        });
        
        if (ref.value != null)
            setValue(ref.getValue());
    }
    
    protected Choice(String name) {
        super(name);
    }
    
    protected Choice(Choice<T> ref) {
        super(ref);
        initialise(ref);
    }
    
    protected Choice(String name, Choice<T> ref) {
        super(name, ref);
        initialise(ref);
    }

    @Override
    public T getValue() {
        T v = valueProperty().get();
        if (v != null)
            return v;
        else
            return getDefaultValue();
    }

    @Override
    public void setValue(T value) {
        if (!options.contains(value))
            valueProperty().set(
                options.stream().filter(
                    option -> option.getName().equals(value.getName())
                ).findAny().orElse(defaultValue)
            );
        else
            valueProperty().set(
                value
            );
    }

    @Override
    public OptionProperty<T> valueProperty() {
        if (value == null)
            value = new OptionProperty<>(this, "value");
        return value;
    }
    
    public T getDefaultValue() {
        return defaultValue;
    }
    
    public abstract void setDefaultValue();
    
    public void addOption(T option, boolean isDefault) {
        options.add(option);
        if (isDefault)
            defaultValue = option;
    }
    
    public List<T> getOptions() {
        return options;
    }
    
    public int size() {
        return options.size();
    }

    @Override
    public List<String> parameters() {
        Option v = getValue();
        
        List<String> param = new ArrayList<>();
        if (hasDependenciesValidated()) {
            param.add("-" + getName());
            param.add(v.getName());
        }
        return param;
    }
    
    @Override
    public <S extends MachineComponent<T, OptionProperty<T>>> boolean canReplace(S otherComponent) {
        return getName().equals(otherComponent.getName());
    }
}
