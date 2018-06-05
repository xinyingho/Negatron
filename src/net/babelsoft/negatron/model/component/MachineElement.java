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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import net.babelsoft.negatron.io.XmlOutput;
import net.babelsoft.negatron.model.ParameterDependency;
import net.babelsoft.negatron.model.ParametrisedElement;

/**
 *
 * @author capan
 */
public abstract class MachineElement<T> implements ParametrisedElement, XmlOutput, Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String name;
    private String previousName;
    private List<ParameterDependency<?>> dependencies;
    
    private void initialise(MachineElement<T> ref) {
        previousName = ref.previousName;
    }
    
    protected MachineElement(String name) {
        this.name = name;
    }
    
    protected MachineElement(MachineElement<T> ref) {
        this(ref.name);
        initialise(ref);
    }
    
    protected MachineElement(String name, MachineElement<T> ref) {
        this(name);
        initialise(ref);
    }

    @Override
    public String getName() {
        return name;
    }
    
    public String getPreviousName() {
        return previousName;
    }
    
    public void setPreviousName(String name) {
        previousName = name;
    }
    
    public abstract T getValue();
    
    public abstract void setValue(T value);
    
    public boolean setValue(MachineElement<T> param) {
        if (param != null)
            setValue(param.getValue());
        return true;
    }
    
    public void addDependency(MachineElement<?> dependency) {
        if (dependencies == null)
            dependencies = new ArrayList<>();
        dependencies.add(new ParameterDependency<>(dependency));
    }
    
    public void copyDependencies(List<ParameterDependency<?>> dependencies, List<MachineElement<?>> params) {
        if (dependencies == null)
            return;
        if (this.dependencies == null)
            this.dependencies = new ArrayList<>();
        
        dependencies.forEach(dependency -> {
            String dependencyName = dependency.getDependency().getName();
            params.stream().filter(
                param -> param.getName().equals(dependencyName)
            ).findAny().ifPresent(
                param -> this.dependencies.add(
                    new ParameterDependency<>(param, dependency.getFilterValues())
                )
            );
        });
        this.dependencies.addAll(dependencies);
    }
    
    public List<ParameterDependency<?>> getDependencies() {
        return dependencies;
    }
    
    public boolean hasDependenciesValidated() {
        if (dependencies == null)
            return true;
        
        return !dependencies.stream().anyMatch(
            dependency -> !dependency.getFilterValues().equals( dependency.getDependency().parameters() )
        );
    }
    
    public abstract MachineElement<?> copy();
}
