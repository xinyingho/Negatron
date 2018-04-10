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
public class SoftwareListFilter {
    
    private final String softwareList;
    private final String filter;
    
    public SoftwareListFilter(String softwareList, String filter) {
        this.softwareList = softwareList;
        this.filter = filter;
    }

    /**
     * @return the softwareList
     */
    public String getSoftwareList() {
        return softwareList;
    }

    /**
     * @return the filter
     */
    public String getFilter() {
        return filter;
    }
}
