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

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.Image;
import javafx.scene.layout.RowConstraints;
import net.babelsoft.negatron.io.configuration.Property;
import net.babelsoft.negatron.io.extras.Extras;
import net.babelsoft.negatron.view.control.ImageViewPane;

/**
 *
 * @author capan
 */
public class ImageGridAdapter {
    
    private final ImageViewPane imageViewPane;
    private final RowConstraints rowConstraints;
    
    public ImageGridAdapter(
        final Image image, final ReadOnlyDoubleProperty referenceWidth, final double widthRatio,
        final String name, final Property property
    ) {
        if (image != null) {
            imageViewPane = new ImageViewPane(image);
            rowConstraints = new RowConstraints();
            
            ChangeListener<? super Number> listener = (o, oV, newValue) -> {
                rowConstraints.setMaxHeight(
                    image.getHeight() * newValue.doubleValue() * widthRatio / image.getWidth()
                );
            };
            referenceWidth.addListener(listener);
            
            listener.changed(null, null, referenceWidth.get());
            
            if (name != null && property != null)
                imageViewPane.setDragCopyPath(Extras.toPrimaryPath(name, property));
        } else {
            imageViewPane = null;
            rowConstraints = null;
        }
    }
    
    public ImageViewPane getImageViewPane() {
        return imageViewPane;
    }
    
    public RowConstraints getRowConstraints() {
        return rowConstraints;
    }
}
