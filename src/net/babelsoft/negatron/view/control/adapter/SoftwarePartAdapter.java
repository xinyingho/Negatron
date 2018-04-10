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

import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import net.babelsoft.negatron.controller.DeviceController;
import net.babelsoft.negatron.model.item.Software;
import net.babelsoft.negatron.model.item.SoftwarePart;

/**
 *
 * @author capan
 */
public class SoftwarePartAdapter {
    
    public static String getName(Software software, SoftwarePart softwarePart) {
        return software.getName() + ":" + softwarePart.getName();
    }

    private final Software software;
    private final SoftwarePart softwarePart;
    private final List<DeviceController> devices;
    private ObjectProperty<DeviceController> assignment;
    
    public SoftwarePartAdapter(Software software, SoftwarePart softwarePart, List<DeviceController> devices) {
        this.software = software;
        this.softwarePart = softwarePart;
        this.devices = devices;
        assignment = new SimpleObjectProperty<>();
    }
    
    public String getName() {
        return getName(software, softwarePart);
    }
    
    public String getDescription() {
        return softwarePart.getDescription();
    }
    
    public String getPartName() {
        return softwarePart.getName();
    }
    
    public List<DeviceController> getDevices() {
        return devices;
    }
    
    public ObjectProperty<DeviceController> assignmentProperty() {
        return assignment;
    }
    
    public void setAssignment(DeviceController device) {
        assignment.set(device);
    }
    
    public DeviceController getAssignment() {
        return assignment.get();
    }
}
