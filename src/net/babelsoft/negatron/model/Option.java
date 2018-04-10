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

/**
 *
 * @author capan
 */
public class Option<T extends Option> {
    
    private final String name;
    private String description;
    
    protected Option() {
        this("\"\"");
    }
    
    protected Option(String name) {
        this.name = name;
    }
    
    protected Option(String name, String description) {
        this(name);
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    protected void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return String.format("%s [%s]", description, name);
    }
    
    @SuppressWarnings("unchecked")
    public T copy() {
        return (T) this; // should be only called for EMPTY_OPTION
    }
}
