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

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author capan
 */
public final class PathCharset {
        
    private String path;
    private String charSet;

    public PathCharset(String path, String charSet) {
        setPath(path);
        setCharSet(charSet);
    }

    /**
     * @return the path
     */
    public String getPathString() {
        return path;
    }
    
    /**
     * @return the path
     */
    public Path getPath() {
        return Paths.get(path);
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the charSet
     */
    public String getCharSet() {
        return charSet;
    }

    /**
     * @param charSet the charSet to set
     */
    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }
}
