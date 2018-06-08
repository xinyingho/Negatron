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

import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParserFactory;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.FavouriteConfiguration;
import net.babelsoft.negatron.io.configuration.FavouriteTree;
import net.babelsoft.negatron.model.component.Bios;
import net.babelsoft.negatron.model.component.BiosSet;
import net.babelsoft.negatron.model.component.Device;
import net.babelsoft.negatron.model.component.MachineElementList;
import net.babelsoft.negatron.model.component.Ram;
import net.babelsoft.negatron.model.component.RamOption;
import net.babelsoft.negatron.model.component.Slot;
import net.babelsoft.negatron.model.component.SlotOption;
import net.babelsoft.negatron.model.favourites.Favourite;
import net.babelsoft.negatron.model.favourites.Folder;
import net.babelsoft.negatron.model.favourites.MachineConfiguration;
import net.babelsoft.negatron.model.favourites.Separator;
import net.babelsoft.negatron.model.favourites.SoftwareConfiguration;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.model.item.Software;
import net.babelsoft.negatron.model.item.SoftwareList;
import net.babelsoft.negatron.view.control.tree.CopyPastableTreeItem;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author capan
 */
public class FavouriteLoader implements Callable<FavouriteTree> {
    
    final private Map<String, Machine> machines;
    final private Map<String, SoftwareList> softwareLists;

    public FavouriteLoader(Map<String, Machine> machines, Map<String, SoftwareList> softwareLists) {
        this.machines = machines;
        this.softwareLists = softwareLists;
    }

    @Override
    public FavouriteTree call() throws Exception {
        if (!Files.exists(FavouriteConfiguration.FAV_PATH))
            return null;
        
        FavouriteDataHandler dataHandler = new FavouriteDataHandler();
        
        try (InputStream dataStream = Files.newInputStream(FavouriteConfiguration.FAV_PATH)) {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(false);
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();

            xmlReader.setContentHandler(dataHandler);
            xmlReader.parse(new InputSource(dataStream));
        } catch (Exception ex) {
            Logger.getLogger(MachineListLoader.class.getName()).log(Level.SEVERE, "An error occured while loading favourites", ex);
        }
        
        return dataHandler.result();
    }
    
    private class FavouriteDataHandler extends DefaultHandler {
        
        private final FavouriteTree favouriteTree;
        private CopyPastableTreeItem parentItem;
        
        private Favourite favourite;
        
        private boolean configurable;
        private MachineElementList parameters;
        private Device device;
        private StringBuilder commandLine;
        
        public FavouriteDataHandler() {
            favouriteTree = new FavouriteTree();
        }
        
        private Slot findSlot(Slot refSlot, String fullname, String name) {
            String parentName = name;
            int j = parentName.indexOf(":");
            String value = parentName.substring(0, j);
            SlotOption option = refSlot.getOptions().stream().filter(
                o -> o.getName().equals(value)
            ).findAny().orElse(null);
            if (option != null) {
                List<Slot> slots = option.getDevice().getSlots();
                if (slots != null) {
                    String slotName = name.substring(j);
                    refSlot = slots.stream().filter(
                        s -> slotName.startsWith(s.getName())
                    ).findAny().orElse(null);
                    if (refSlot != null) {
                        int k = refSlot.getName().length();
                        if (k < slotName.length())
                            return findSlot(refSlot, fullname, slotName.substring(k + 2));
                        else
                            return refSlot.copy(fullname);
                    }
                }
            }
            return null;
        }
        
        public FavouriteTree result() {
            return favouriteTree;
        }
        
        @Override
        public void startElement(
            String namespaceURI,
            String localName,
            String qName,
            Attributes atts
        ) throws SAXException {
            super.startElement(namespaceURI, localName, qName, atts);
            
            switch (qName) {
                case "root":
                    Folder root = new Folder(
                        atts.getValue("name"),
                        LocalDateTime.parse(atts.getValue("dateCreated")),
                        LocalDateTime.parse(atts.getValue("dateModified"))
                    );
                    favouriteTree.setRoot(parentItem = new CopyPastableTreeItem(root));
                    break;
                case "folder":
                    Folder folder = new Folder(
                        atts.getValue("name"),
                        LocalDateTime.parse(atts.getValue("dateCreated")),
                        LocalDateTime.parse(atts.getValue("dateModified"))
                    );
                    CopyPastableTreeItem item = new CopyPastableTreeItem(folder);
                    parentItem.getInternalChildren().add(item);
                    parentItem = item;
                    favouriteTree.addFolder(folder);
                    break;
                case "children":
                    break;
                case "separator":
                    Separator separator = new Separator(
                        LocalDateTime.parse(atts.getValue("dateCreated")),
                        LocalDateTime.parse(atts.getValue("dateModified"))
                    );
                    item = new CopyPastableTreeItem(separator);
                    parentItem.getInternalChildren().add(item);
                    break;
                case "favourite":
                    favourite = new Favourite(
                        atts.getValue("name"), null, null, null,
                        LocalDateTime.parse(atts.getValue("dateCreated")),
                        LocalDateTime.parse(atts.getValue("dateModified"))
                    );
                    item = new CopyPastableTreeItem(favourite);
                    parentItem.getInternalChildren().add(item);
                    break;
                case "machineConfiguration":
                    Machine machine = machines.get(atts.getValue("name"));
                    if (machine == null) {
                        machine = new Machine(atts.getValue("name"), "");
                        machine.setDescription(atts.getValue("description"));
                    }
                    favourite.resetMachine(machine);
                    configurable = Boolean.parseBoolean(atts.getValue("configurable"));
                    break;
                case "commandLine":
                    commandLine = new StringBuilder();
                    break;
                case "parameters":
                    parameters = new MachineElementList(favourite.getMachine());
                    break;
                case "bios":
                    Bios bios = new Bios();
                    if (atts.getValue("name") != null) {
                        BiosSet set = new BiosSet(atts.getValue("name"), atts.getValue("description"));
                        String isDefault = atts.getValue("default");
                        bios.addOption(set, isDefault != null && isDefault.equals(Boolean.TRUE.toString()));
                        
                        bios.setValue(set);
                    } else
                        bios.setDefaultValue();
                    parameters.add(bios);
                    break;
                case "ramsize":
                    Ram ram = new Ram();
                    if (atts.getValue("value") != null) {
                        RamOption option = new RamOption(atts.getValue("value"));
                        String isDefault = atts.getValue("default");
                        ram.addOption(option, isDefault != null && isDefault.equals(Boolean.TRUE.toString()));
                        
                        ram.setValue(option);
                    } else
                        ram.setDefaultValue();
                    parameters.add(ram);
                    break;
                case "device":
                    device = new Device(
                        atts.getValue("name"),
                        atts.getValue("type"),
                        atts.getValue("tag"),
                        Boolean.parseBoolean(atts.getValue("mandatory"))
                    );
                    device.setCompatibleSoftwareLists(
                        Boolean.parseBoolean(atts.getValue("compatibleSoftwareLists"))
                    );
                    if (atts.getValue("value") != null)
                        device.setValue(atts.getValue("value"));
                    parameters.add(device);
                    break;
                case "interfaces":
                    break;
                case "interface":
                    device.addInterfaceFormat(atts.getValue("name"));
                    break;
                case "extensions":
                    Arrays.stream(atts.getValue("names").split(",")).forEach(
                        ext -> device.addExtension(ext)
                    );
                    break;
                case "slot":
                    if (Configuration.Manager.isAsyncExecutionMode()) {
                        // MAME 0.185 and older
                        Slot slot = new Slot(atts.getValue("name"));
                        if (atts.getValue("value") != null) {
                            SlotOption option = new SlotOption(atts.getValue("value"), atts.getValue("description"));
                            String isDefault = atts.getValue("default");
                            slot.addOption(option, isDefault != null && isDefault.equals(Boolean.TRUE.toString()));

                            slot.setValue(option);
                        } else
                            slot.setDefaultValue();
                        parameters.add(slot);
                    } else {
                        // MAME 0.186+
                        
                        // try to find the slot in the machine default settings
                        Slot refSlot = favourite.getMachine().getSlots().stream().filter(
                            s -> s.getName().equals(atts.getValue("name"))
                        ).findAny().orElse(null);
                        
                        Slot slot = null;
                        if (refSlot == null) {
                            // didn't find it, so try to recreate the slot
                            String name = atts.getValue("name");
                            String parentName = name;
                            int i;
                            while ((i = parentName.lastIndexOf(":")) > 0) {
                                String filter = parentName.substring(0, i);
                                refSlot = favourite.getMachine().getSlots().stream().filter(
                                    s -> s.getName().equals(filter)
                                ).findAny().orElse(null);
                                if (refSlot != null) {
                                    slot = findSlot(refSlot, name, name.substring(i + 1));
                                    break;
                                }
                                parentName = filter;
                            }
                        } else
                            // found it
                            slot = refSlot.copy();
                        
                        // set the slot value
                        if (slot != null && atts.getValue("value") != null) {
                            SlotOption option = slot.getOptions().stream().filter(
                                o -> o.getName().equals(atts.getValue("value"))
                            ).findAny().orElse(null);
                            if (option != null) {
                                slot.setValue(option);
                                parameters.add(slot);
                            }
                        }
                    }
                    break;
                case "softwareConfiguration":
                    Software software = null;
                    SoftwareList softwareList = softwareLists.get(atts.getValue("list"));
                    if (softwareList != null)
                        software = softwareList.getSoftware(atts.getValue("name"));
                    if (software == null) {
                        software = new Software(atts.getValue("name"), "");
                        software.setDescription(atts.getValue("description"));
                    }
                    String deviceName = atts.getValue("device");
                    device = favourite.getMachineConfiguration().getParameters().stream().filter(
                        parameter -> parameter instanceof Device
                    ).map(
                        parameter -> (Device) parameter
                    ).filter(
                        parameter -> parameter.getName().equals(deviceName)
                    ).findAny().orElse(null);
                    
                    SoftwareConfiguration softwareConfiguration = new SoftwareConfiguration(
                        favourite.getMachine(), device, software
                    );
                    favourite.resetSoftwareConfiguration(softwareConfiguration);
                    break;
                default:
                    throw new RuntimeException("Unknown XML element during favourites parsing: " + qName);
            }
        }
    
        @Override
        public void characters(char[] chars, int start, int length) throws SAXException {
            if (commandLine != null)
                commandLine.append(chars, start, length);
        }
        
        @Override
        public void endElement(
            String namespaceURI,
            String localName,
            String qName
        ) throws SAXException {
            super.endElement(namespaceURI, localName, qName);
            
            switch (qName) {
                case "folder":
                    parentItem = (CopyPastableTreeItem) parentItem.getParent();
                    break;
                case "favourite":
                    if (favourite.getMachine() == null) {
                        favourite.resetMachine();
                        favouriteTree.addEmptyFavourite(favourite);
                    }
                    favourite.checkValidity();
                    favourite = null;
                    break;
                case "machineConfiguration":
                    if (favourite.getMachineConfiguration() == null) {
                        favourite.resetMachineConfiguration(new MachineConfiguration(
                            new MachineElementList(favourite.getMachine()), configurable
                        ));
                    }
                    break;
                case "commandLine":
                    favourite.resetMachineConfiguration(new MachineConfiguration(
                        commandLine.toString()
                    ));
                    commandLine = null;
                    break;
                case "parameters":
                    favourite.resetMachineConfiguration(new MachineConfiguration(
                        parameters, configurable
                    ));
                    parameters = null;
                    break;
                case "device":
                    if (Configuration.Manager.isSyncExecutionMode() && device.getExtensions().isEmpty())
                        favourite.setMustMigrate(true);
                    device = null;
                    break;
            }
        }
    }
}
