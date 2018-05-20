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
package net.babelsoft.negatron.model.item;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import net.babelsoft.negatron.io.extras.Icons;
import net.babelsoft.negatron.model.IconDescription;
import net.babelsoft.negatron.model.Status;
import net.babelsoft.negatron.model.Support;

/**
 *
 * @author capan
 */
public abstract class EmulatedItem<T> extends Item {
    private static final long serialVersionUID = 4L;
    
    private String group;
    private String year;
    private boolean configurable;
    private T parent;
    private String shortcut;
    private Support support;
    private transient ObjectProperty<Status> status;
    private transient ObjectProperty<IconDescription> iconDescription;
    
    public EmulatedItem(String name, String group) {
        super(name);
        setGroup(group);
    }
    
    @Override
    public void setDescription(final String description) {
        super.setDescription(description);
        shortcut = description.replace(" ", "").toLowerCase();
    }
    
    private void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }
    
    public String getShortcut() {
        return shortcut;
    }
    
    private void setGroup(String group) {
        this.group = group;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setYear(String year) {
        this.year = year;
    }
    
    public String getYear() {
        return year;
    }
    
    public void setConfigurable(boolean configurable) {
        this.configurable = configurable;
    }
    
    public boolean isConfigurable() {
        return configurable;
    }
    
    public abstract void setNotCompatible();
    
    public abstract boolean isNotCompatible();
    
    public Support getSupport() {
        return support;
    }
    
    public void setSupport(Support support) {
        this.support = support;
    }

    public void setSupport(String support) {
        setSupport(Support.fromString(support));
    }
    
    public ObjectProperty<Status> statusProperty() {
        if (status == null)
            status = new SimpleObjectProperty<>(Status.UNKNOWN);
        return status;
    }
    
    public Status getStatus() {
        return statusProperty().get();
    }
    
    public void setStatus(Status status) {
        statusProperty().set(status);
    }
    
    public ObjectProperty<IconDescription> iconDescriptionProperty() {
        if (iconDescription == null)
            iconDescription = new SimpleObjectProperty<>(
                new IconDescription(getDescription(), Icons.DEFAULT_ICON)
            );
        return iconDescription;
    }
    
    public IconDescription getIconDescription() {
        return iconDescriptionProperty().get();
    }
    
    public void setIconDescription(IconDescription iconDescription) {
        iconDescriptionProperty().set(iconDescription);
    }
    
    public void setIcon(Image icon) {
        if (icon == null)
            icon = Icons.DEFAULT_ICON;
        setIconDescription(new IconDescription(getDescription(), icon));
    }
    
    public abstract String getCompany();

    public void setParent(T parent) {
        this.parent = parent;
    }
    
    public T getParent() {
        return parent;
    }
    
    public boolean hasParent() {
        return parent != null;
    }
    
    public boolean isRunnable() { return true; }
    
    public void reset() {}
}
