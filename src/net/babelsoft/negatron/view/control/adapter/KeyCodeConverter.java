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
package net.babelsoft.negatron.view.control.adapter;

import javafx.scene.input.KeyCode;

/**
 *
 * @author capan
 */
public class KeyCodeConverter {
    
    private KeyCodeConverter() {}
    
    public static String convert(KeyCode code) {
        switch (code) {
            case ESCAPE:
                return "ESC";
            case SCROLL_LOCK:
                return "SCRLOCK";
            case OPEN_BRACKET:
                return "OPENBRACE";
            case CLOSE_BRACKET:
                return "CLOSEBRACE";
            case DEAD_TILDE:
                return "TILDE";
            case BACK_SLASH:
                return "BACKSLASH";
            case LESS:
                return "BACKSLASH2";
            case SEMICOLON:
                return "COLON";
            case MINUS:
            case EQUALS:
            case BACK_SPACE:
            case TAB:
            case ENTER:
            case COLON:
            case QUOTE:
            default:
                return code.getName().toUpperCase();
        }
    }
}
