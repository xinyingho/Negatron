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
package net.babelsoft.negatron.model.comparing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.babelsoft.negatron.model.component.MachineElement;
import net.babelsoft.negatron.model.component.MachineElementList;
import net.babelsoft.negatron.model.item.Machine;

/**
 *
 * @author capan
 */
public class Merger {
    
    private final Machine machine;
    private String origin;
    private MachineElement<?> dependency;
    
    private List<MergedUnit<?>> differences;
    private MachineElementList parameters;
    private MachineElementList previousParameters;
    
    private Map<String, MachineElement<?>> parameterMap;
    private Map<String, MachineElement<?>> previousParameterMap;
    private Map<String, MachineElement<?>> parameterMapBackup;
    
    boolean hasChanged;

    public Merger(final Machine machine) {
        this.machine = machine;
    }
    
    public Merger(final Machine machine, MachineElementList parameters) {
        this.machine = machine;
        this.parameters = parameters;
        parameterMap = new HashMap<>();
        parameters.forEach(
            parameter -> parameterMap.put(parameter.getName(), parameter)
        );
    }

    public void reset(String origin) {
        this.origin = origin;
        
        if (parameterMap != null) {
            hasChanged = false; // subsequent use, don't know yet if anything will be changed
            
            previousParameters = parameters;
            previousParameterMap = parameterMap;
            parameterMapBackup = new HashMap<>(parameterMap);
        } else
            hasChanged = true; // first time use, so will obviously create a new parameter set
        
        parameters = new MachineElementList(machine);
        parameterMap = new HashMap<>();
        differences = new ArrayList<>();
    }
    
    private <T> MergedUnit<T> compare(final MachineElement<T> parameter) {
        MergedUnit<T> mergedUnit;
        
        if (previousParameterMap != null) {
            String previousName = parameter.getName();

            if (!previousParameterMap.containsKey(previousName)) {
                if (previousName.matches(".*[a-z]1$")) { // has element changed name, e.g. cartridge to cartridge1?
                    previousName = previousName.substring(0, previousName.length() - 1);
                    if (!previousParameterMap.containsKey(previousName))
                        previousName = null;
                } else if (previousName.matches(".*[^\\d]$")) { // has element changed name, e.g. cartridge1 to cartridge?
                    previousName += "1";
                    if (!previousParameterMap.containsKey(previousName))
                        previousName = null;
                } else // new element
                    previousName = null;
                hasChanged = true;
            } // same old element

            if (previousName != null) {
                @SuppressWarnings("unchecked")
                MachineElement<T> previousParameter = (MachineElement<T>) previousParameterMap.remove(previousName);
                if (previousName.equals(origin))
                    dependency = parameter;
                
                if (!parameter.getName().equals(previousName))
                    parameter.setPreviousName(previousName);
                if (!parameter.setValue(previousParameter)) {
                    hasChanged = true;
                    mergedUnit = new MergedUnit<>(previousParameter, parameter, Difference.CHANGED);
                } else
                    mergedUnit = new MergedUnit<>(previousParameter, parameter, Difference.UNCHANGED);
            } else
                mergedUnit = new MergedUnit<>(null, parameter, Difference.ADDED);
        } else
            mergedUnit = new MergedUnit<>(null, parameter, Difference.CREATED);
        
        return mergedUnit;
    }
    
    public void add(final MachineElement<?> parameter) {
        differences.add(compare(parameter));
        parameters.add(parameter);
        parameterMap.put(parameter.getName(), parameter);
    }
    
    public boolean merge() {
        // add dependencies for parameter evaluation
        differences.stream().filter(
            mergedUnit ->
                mergedUnit.getStatus() == Difference.ADDED || (
                    mergedUnit.getStatus() == Difference.UNCHANGED ||
                    mergedUnit.getStatus() == Difference.CHANGED
                ) &&
                mergedUnit.getOldElement().getDependencies() != null &&
                mergedUnit.getOldElement().getDependencies().size() > 0
        ).forEach(
            mergedUnit -> {
                if (mergedUnit.getStatus() == Difference.ADDED)
                    mergedUnit.getNewElement().addDependency(dependency);
                else { // UNCHANGED & CHANGED, re-wire dependencies to updated elements
                    mergedUnit.getOldElement().getDependencies().stream().map(
                        dependency -> parameterMap.get(dependency.getDependency().getName())
                    ).forEach(
                        replacement -> mergedUnit.getNewElement().addDependency(replacement)
                    );
                }
            }
        );
        
        // take care of removed elements
        if (previousParameterMap != null)
            previousParameterMap.values().stream().forEach(
                previousParameter -> {
                    int index = previousParameters.indexOf(previousParameter);
                    if (index > 0) {
                        MachineElement<?> previousSibling = previousParameters.get(index - 1);
                        for (int i = 0; i < differences.size(); ++i) {
                            MergedUnit<?> unit = differences.get(i);
                            MachineElement<?> element = unit.getOldElement();
                            if (element != null && element == previousSibling) {
                                differences.add(++i, new MergedUnit<>(previousParameter, null, Difference.DELETED));
                                hasChanged = true;
                                break;
                            }
                        }
                    } else {
                        differences.add(0, new MergedUnit<>(previousParameter, null, Difference.DELETED));
                        hasChanged = true;
                    }
                }
            );
        
        return hasChanged;
    }
    
    private void clear() {
        previousParameters = null;
        parameterMapBackup = null;
        previousParameterMap = null;
        differences = null;
    }
    
    public List<MergedUnit<?>> commit() {
        machine.setParameters(parameters);
        machine.processInternalDevices();
        
        List<MergedUnit<?>> result = differences;
        clear();
        return result;
    }
    
    public void rollback() {
        if (previousParameterMap != null) {
            parameters = previousParameters;
            parameterMap = parameterMapBackup;
        } else {
            parameters = null;
            parameterMap = null;
        }
        clear();
    }
}
