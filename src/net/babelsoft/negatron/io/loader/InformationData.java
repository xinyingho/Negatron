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
package net.babelsoft.negatron.io.loader;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 *
 * @author capan
 */
public class InformationData {
    
    private final Path path;
    private final Map<String, Map<String, String>> information;
    private final Map<String, List<String>> systemIndex;
    private final Map<String, Map<String, String>> itemIndex;
    
    public InformationData(
        Path path, Map<String, Map<String, String>> information,
        Map<String, List<String>> systemIndex, Map<String, Map<String, String>> itemIndex
    ) {
        this.path = path;
        this.information = information;
        this.systemIndex = systemIndex;
        this.itemIndex = itemIndex;
    }

    /**
     * @return the path
     */
    public Path getPath() {
        return path;
    }

    /**
     * @return the information
     */
    public Map<String, Map<String, String>> getInformation() {
        return information;
    }

    /**
     * @return the systemIndex
     */
    public Map<String, List<String>> getSystemIndex() {
        return systemIndex;
    }

    /**
     * @return the itemIndex
     */
    public Map<String, Map<String, String>> getItemIndex() {
        return itemIndex;
    }
}
