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
package net.babelsoft.negatron.io.configuration;

import java.io.Serializable;

/**
 *
 * @author capan
 */
public class TableColumnBaseConfiguration<S> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private int order;
    private boolean sorted;
    private int sortRank;
    private S sortType;
    private boolean visible;
    private double width;
    
    public TableColumnBaseConfiguration() { }
    
    public TableColumnBaseConfiguration(
        String name, int order, boolean sorted, int sortRank, S sortType, boolean visible, double width
    ) {
        this.name = name;
        this.order = order;
        this.sorted = sorted;
        this.sortRank = sortRank;
        this.sortType = sortType;
        this.visible = visible;
        this.width = width;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * @return the sorted
     */
    public boolean isSorted() {
        return sorted;
    }

    /**
     * @return the sortRank
     */
    public int getSortRank() {
        return sortRank;
    }

    /**
     * @return the sortType
     */
    public S getSortType() {
        return sortType;
    }

    /**
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param order the order to set
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * @param sorted the sorted to set
     */
    public void setSorted(boolean sorted) {
        this.sorted = sorted;
    }

    /**
     * @param sortRank the sortRank to set
     */
    public void setSortRank(int sortRank) {
        this.sortRank = sortRank;
    }

    /**
     * @param sortType the sortType to set
     */
    public void setSortType(S sortType) {
        this.sortType = sortType;
    }

    /**
     * @param visible the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(double width) {
        this.width = width;
    }
}
