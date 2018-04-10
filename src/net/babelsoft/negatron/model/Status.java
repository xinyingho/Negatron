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

import javafx.scene.image.Image;

/**
 *
 * @author capan
 */
public enum Status implements ImageEnum {
    UNKNOWN("system-help"),
    BAD("dialog-close"),
    GOOD("dialog-ok-apply");
    
    private final Image image;

    Status(String imgId) {
        if (imgId != null) {
            image = new Image(Status.class.getResourceAsStream("/net/babelsoft/negatron/resource/icon/status/" + imgId + ".png"));
        } else {
            image = null;
        }
    }

    @Override
    public Image getImage() {
        return image;
    }
}