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
import java.util.stream.Collectors;
import javafx.util.Pair;
import net.babelsoft.negatron.io.configuration.Configuration;
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
import net.babelsoft.negatron.model.component.Bios;
import net.babelsoft.negatron.model.component.BiosSet;
import net.babelsoft.negatron.model.component.Device;
import net.babelsoft.negatron.model.component.MachineElementList;
import net.babelsoft.negatron.model.component.Ram;
import net.babelsoft.negatron.model.component.RamOption;
import net.babelsoft.negatron.model.component.Slot;
import net.babelsoft.negatron.model.component.SlotOption;

/**
 *
 * @author capan
 */
public class Machine extends EmulatedItem<Machine> implements Describable, ParametrisedElement {
    private static final long serialVersionUID = 11L;

    private boolean runnable;
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
    private Map<String, String> internalDevices;
    private List<SoftwareListFilter> softwareLists;
    private transient MachineElementList parameters;
    private transient Merger merger;
    private transient InternalDeviceComparator internalDeviceComparator;
    // MAME v0.186+
    private Bios bios;
    private Ram ram;
    private List<Device> devices;
    private List<Slot> slots;
    private List<Pair<String, String>> defaultSlotOptions; // slot name, default slot option name

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
    
    public void setRunnable(boolean isRunnable) {
        this.runnable = isRunnable;
    }

    @Override
    public boolean isRunnable() {
        return runnable;
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
        this.maxNumberPlayers = Integer.parseInt(maxNumberPlayers);
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
        if (internalDevices == null)
            internalDevices = new HashMap<>();
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
        if (getSlots() == null)
            return softwareLists;
        
        // Some selected slot options can also provide additional software lists
        List<SoftwareListFilter> additionalSoftwareLists = getSlots().stream().map(
                slot -> slot.getValue().getDevice()
        ).filter(
                device -> device != null
        ).map(
                device -> device.getSoftwareLists()
        ).filter(
                list -> list != null
        ).flatMap(
                list -> list.stream()
        ).collect(
                Collectors.toList()
        );
        
        if (!additionalSoftwareLists.isEmpty()) {
            additionalSoftwareLists.addAll(softwareLists);
            return additionalSoftwareLists;
        } else
            return softwareLists;
    }
    
    public Merger reset(String origin) {
        if (merger == null) {
            merger = new Merger(this);
            internalDeviceComparator = new InternalDeviceComparator();
        }
        merger.reset(origin);
        if (Configuration.Manager.isAsyncExecutionMode()) {
            internalDevices = new HashMap<>();
            softwareLists = null;
        }
        
        return merger;
    }
    
    public void setParameters(MachineElementList parameters) {
        this.parameters = parameters;
        
        if (Configuration.Manager.isSyncExecutionMode()) {
            if (slots != null)
                slots.clear();
            if (devices != null)
                devices.clear();
            parameters.forEach(param -> {
                if (param instanceof Slot slot) {
                    if (slots == null)
                        slots = new ArrayList<>();
                    slots.add(slot);
                } else if (param instanceof Device device) {
                    if (devices == null)
                        devices = new ArrayList<>();
                    devices.add(device);
                }
            });
        }
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
        return parameters(null);
    }
    
    public List<String> parameters(String origin) {
        if (parameters == null)
            parameters = new MachineElementList(this);
        return parameters.toParameters(origin);
    }
    
    public String toCommandLine() {
        if (parameters == null)
            parameters = new MachineElementList(this);
        return parameters.toString();
    }
    
    // MAME v0.186+
    
    public Bios getBios() {
        return bios;
    }
    
    public void addBiosSet(String name, String description, boolean isDefault) {
        BiosSet set = new BiosSet(name, description);
        if (bios == null)
            bios = new Bios();
        bios.addOption(set, isDefault);
    }
    
    public Ram getRam() {
        return ram;
    }
    
    public void addRamOption(String name, boolean isDefault) {
        RamOption option = new RamOption(name);
        if (ram == null)
            ram = new Ram();
        ram.addOption(option, isDefault);
    }
    
    public List<Device> getDevices() {
        return devices;
    }
    
    public void addDevice(String name, String type, String tag, String interfaceFormat, boolean mandatory) {
        Device device = new Device(name, type, tag, mandatory);
        device.setInterfaceFormats(
            interfaceFormat != null ? interfaceFormat.split(",") : new String[0]
        );
        if (devices == null)
            devices = new ArrayList<>();
        devices.add(device);
    }
    
    public void addExtensionToLastDevice(String name) {
        devices.get(devices.size() - 1).addExtension(name);
    }
    
    public List<Slot> getSlots() {
        return slots;
    }
    
    public void addSlot(String name) {
        Slot slot = new Slot(name);
        if (slots == null)
            slots = new ArrayList<>();
        slots.add(slot);
    }
    
    public SlotOption addSlotOptionToLastSlot(String name, boolean isDefault) {
        SlotOption option = new SlotOption(name);
        slots.get(slots.size() - 1).addOption(option, isDefault);
        return option;
    }

    public List<Pair<String, String>> getDefaultSlotOptions() {
        return defaultSlotOptions;
    }
    
    public void initialise(Map<String, SoftwareList> softwareListMap) {
        if (slots != null) {
            slots.removeIf(slot -> slot.size() == 0);
            if (!slots.isEmpty()) {
                defaultSlotOptions = new ArrayList<>();
                slots.stream().forEach(slot ->
                    // save machine's default slot options as they may override what's indicated by the devices linked to those slots
                    defaultSlotOptions.add(new Pair<>(slot.getName(), slot.getDefaultValue().getName()))
                );
            } else
                slots = null;
        }
        
        if (devices != null && softwareLists != null) {
            devices.stream().forEach(device -> {
                softwareLists.stream().flatMap(softwareListFilter -> {
                    SoftwareList softwareList = softwareListMap.get(softwareListFilter.getSoftwareList());
                    if (softwareList != null)
                        return softwareList.getSoftwares(device.getInterfaceFormats(), softwareListFilter.getFilter()).stream();
                    else
                        return null;
                }).findAny().ifPresent(
                    software -> device.setCompatibleSoftwareLists(true)
                );
            });
        }
    }
}
