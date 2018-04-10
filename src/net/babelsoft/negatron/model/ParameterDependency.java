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
package net.babelsoft.negatron.model;

import java.util.List;
import net.babelsoft.negatron.model.component.MachineElement;

/**
 *
 * @author capan
 */
public class ParameterDependency<T> {
    
    private final MachineElement<T> dependency;
    private final List<String> filterValues;
    
    public ParameterDependency(MachineElement<T> dependency) {
        this.dependency = dependency;
        filterValues = dependency.parameters();
    }
    
    public ParameterDependency(MachineElement<T> dependency, List<String> filterValues) {
        this.dependency = dependency;
        this.filterValues = filterValues;
    }

    /**
     * @return the dependency
     */
    public MachineElement<T> getDependency() {
        return dependency;
    }

    /**
     * @return the filterValue
     */
    public List<String> getFilterValues() {
        return filterValues;
    }
}
