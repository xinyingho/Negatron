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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.babelsoft.negatron.model.ControllerType;
import net.babelsoft.negatron.model.Describable;
import net.babelsoft.negatron.model.DisplayType;
import net.babelsoft.negatron.model.ParametrisedElement;
import net.babelsoft.negatron.model.ScreenOrientation;
import net.babelsoft.negatron.model.SoftwareListFilter;
import net.babelsoft.negatron.model.SoundType;
import net.babelsoft.negatron.model.Status;
import net.babelsoft.negatron.model.Support;
import net.babelsoft.negatron.model.comparing.InternalDeviceComparator;
import net.babelsoft.negatron.model.comparing.Merger;
import net.babelsoft.negatron.model.component.MachineElementList;

/**
 *
 * @author capan
 */
public class Machine extends EmulatedItem<Machine> implements Describable, ParametrisedElement {
    private static final long serialVersionUID = 8L;

    private boolean mechanical;
    private boolean coinSlot;
    private boolean serviceMode;
    private boolean tilt;
    private boolean softwareEmbedded;
    private int maxNumberPlayers;
    private String manufacturer;
    private DisplayType displayType;
    private ScreenOrientation screenOrientation;
    private SoundType soundType;
    private EnumSet<ControllerType> controllerTypes;
    private transient Map<String, String> internalDevices;
    private transient List<SoftwareListFilter> softwareLists;
    private transient MachineElementList parameters;
    private transient Merger merger;
    private transient InternalDeviceComparator internalDeviceComparator;

    public Machine(final String name, final String sourceFile) {
        super(name, sourceFileToGroup(sourceFile));
        mechanical = false;
        coinSlot = false;
        serviceMode = false;
        tilt = false;
        softwareEmbedded = true;
        maxNumberPlayers = 0;
        displayType = DisplayType.none;
        screenOrientation = ScreenOrientation.NONE;
        soundType = SoundType.NONE;
        controllerTypes = EnumSet.noneOf(ControllerType.class);
    }
    
    protected Machine(final String name) {
        super(name, null);
        setDescription(name);
        setSupport(Support.NOT_COMPATIBLE);
        setStatus(Status.GOOD);
    }
    
    private static String sourceFileToGroup(String sourceFile) {
        if (sourceFile.contains(".")) {
            int index = sourceFile.lastIndexOf(".");
            return sourceFile.substring(0, index);
        } else
            return sourceFile;
    }
    
    public boolean isFolder() {
        return getGroup() == null;
    }
    
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    private String getManufacturer() {
        return manufacturer;
    }

    public void setMechanical(boolean isMechanical) {
        this.mechanical = isMechanical;
    }

    public boolean isMechanical() {
        return mechanical;
    }

    public void setCoinSlot(boolean hasCoinSlot) {
        coinSlot = hasCoinSlot;
    }
    
    public boolean hasCoinSlot() {
        return coinSlot;
    }
    
    public void setServiceMode(boolean hasServiceMode) {
        serviceMode = hasServiceMode;
    }
    
    public boolean hasServiceMode() {
        return serviceMode;
    }
    
    public void setTilt(boolean hasTilt) {
        tilt = hasTilt;
    }
    
    public boolean hasTilt() {
        return tilt;
    }

    /**
     * @param softwareEmbedded the softwareEmbedded to set
     */
    public void setSoftwareEmbedded(boolean softwareEmbedded) {
        this.softwareEmbedded = softwareEmbedded;
    }

    /**
     * @return the softwareEmbedded
     */
    public boolean isSoftwareEmbedded() {
        return softwareEmbedded;
    }

    /**
     * @param maxNumberPlayers the maxNumberPlayers to set
     */
    public void setMaxNumberPlayers(int maxNumberPlayers) {
        this.maxNumberPlayers = maxNumberPlayers;
    }
    
    public void setMaxNumberPlayers(String maxNumberPlayers) {
        this.maxNumberPlayers = Integer.valueOf(maxNumberPlayers);
    }

    /**
     * @return the maxNumberPlayers
     */
    public int getMaxNumberPlayers() {
        return maxNumberPlayers;
    }
    
    public void setDisplayType(DisplayType displayType) {
        this.displayType = displayType;
    }
    
    public void setDisplayType(String displayType) {
        this.displayType = DisplayType.valueOf(displayType);
    }
    
    public DisplayType getDisplayType() {
        return displayType;
    }
    
    public void setScreenOrientation(ScreenOrientation screenOrientation) {
        this.screenOrientation = screenOrientation;
    }
    
    public void setScreenOrientation(String screenOrientation) {
        this.screenOrientation = ScreenOrientation.getValue(screenOrientation);
    }
    
    public ScreenOrientation getScreenOrientation() {
        return screenOrientation;
    }
    
    public void setSoundType(SoundType soundType) {
        this.soundType = soundType;
    }
    
    public void setSoundType(String soundType) {
        this.soundType = SoundType.getValue(soundType);
    }
    
    public SoundType getSoundType() {
        return soundType;
    }
    
    public void addControllerType(String controllerType) {
        ControllerType value;
        try {
            value = ControllerType.valueOf(controllerType);
        } catch (IllegalArgumentException ex) {
            return; // simply ignore unknown values
        }
        controllerTypes.add(value);
    }
    
    public boolean hasControllerTypes() {
        return !controllerTypes.isEmpty();
    }
    
    public boolean hasControllerType(ControllerType type) {
        return controllerTypes.contains(type);
    }
    
    @Override
    public void setNotCompatible() { } // should never be called
    
    @Override
    public boolean isNotCompatible() { return false; }
    
    @Override
    public String getCompany() {
        return getManufacturer();
    }
    
    public void addInternalDevice(String name, String description) {
        internalDevices.put(name, description);
    }
    
    public Map<String, String> getInternalDevices() {
        return internalDevices;
    }
    
    public void processInternalDevices() {
        internalDeviceComparator.compare(internalDevices);
    }
    
    public boolean hasNewInternalDevices() {
        return internalDeviceComparator.hasChanged();
    }
    
    public void addSoftwareList(String softwareList, String filter) {
        if (softwareLists == null)
            softwareLists = new ArrayList<>();
        softwareLists.add(new SoftwareListFilter(softwareList, filter));
    }
    
    public List<SoftwareListFilter> getSoftwareLists() {
        return softwareLists;
    }
    
    public Merger reset(String origin) {
        if (merger == null) {
            merger = new Merger(this);
            internalDeviceComparator = new InternalDeviceComparator();
        }
        merger.reset(origin);
        internalDevices = new HashMap<>();
        softwareLists = null;
        
        return merger;
    }
    
    public void setParameters(MachineElementList parameters) {
        this.parameters = parameters;
    }
    
    public MachineElementList getParameters() {
        return parameters;
    }

    public void forceParameters(MachineElementList parameters) {
        setParameters(parameters);
        merger = new Merger(this, parameters);
        internalDeviceComparator = new InternalDeviceComparator();
    }
    
    @Override
    public boolean isReady() {
        return !parameters.stream().anyMatch(param -> !param.isReady());
    }
    
    public MachineElementList copyParameters() {
        if (parameters != null)
            return parameters.copy();
        else
            return new MachineElementList(this);
    }

    @Override
    public List<String> parameters() {
        if (parameters == null)
            parameters = new MachineElementList(this);
        return parameters.toParameters();
    }
    
    public String toCommandLine() {
        if (parameters == null)
            parameters = new MachineElementList(this);
        return parameters.toString();
    }
}
