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
package net.babelsoft.negatron.theme;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 *
 * @author capan
 */
public enum Language {
    Manager;
    
    private static class FileResourceControl extends Control {

        @Override
        public ResourceBundle newBundle(
            String baseName, Locale locale, String format, ClassLoader loader, boolean reload
        ) throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            java.util.ResourceBundle bundle = null;

            final String resourceName = toResourceName(bundleName, "properties");
            if (resourceName == null) {
                return bundle;
            }
            
            final Path path = Paths.get(resourceName);
            if (Files.exists(path)) try (
                InputStream stream = Files.newInputStream(path);
                InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            ) {
                bundle = new PropertyResourceBundle(reader);
            }

            return bundle;
        }
    }
    
    private final Control CONTROL = new FileResourceControl();
    public final String ROOT_PATH = "theme/language";
    public final String MASK = "ui.*\\.properties";
    private final String FILE_PATH = ROOT_PATH + "/ui";
    
    public ResourceBundle getBundle() {
        return ResourceBundle.getBundle(FILE_PATH, CONTROL);
    }
    
    public String getString(String key) {
        return getBundle().getString(key);
    }
    
    public String tryGetString(String key) {
        try {
            return getBundle().getString(key);
        } catch (MissingResourceException ex) {
            return key;
        }
    }
    
    public boolean getBoolean(String key) {
        return Boolean.valueOf(getBundle().getString(key));
    }
}