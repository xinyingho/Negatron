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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.util.Pair;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.babelsoft.negatron.io.Mame;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.model.comparing.Difference;
import net.babelsoft.negatron.model.comparing.MergedUnit;
import net.babelsoft.negatron.model.comparing.Merger;
import net.babelsoft.negatron.model.component.Bios;
import net.babelsoft.negatron.model.component.BiosSet;
import net.babelsoft.negatron.model.component.Choice;
import net.babelsoft.negatron.model.component.Device;
import net.babelsoft.negatron.model.component.MachineElement;
import net.babelsoft.negatron.model.component.Ram;
import net.babelsoft.negatron.model.component.RamOption;
import net.babelsoft.negatron.model.component.Slot;
import net.babelsoft.negatron.model.component.SlotOption;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.model.item.SoftwareList;
import net.babelsoft.negatron.util.Dom;
import net.babelsoft.negatron.view.control.form.ChoiceControl;
import net.babelsoft.negatron.view.control.form.Control;
import net.babelsoft.negatron.view.control.form.DeviceControl;
import net.babelsoft.negatron.view.control.form.SlotControl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


public class MachineLoader extends Service<List<Control<?>>> {
    
    private static final String XML_PROLOG = "<?xml version=\"1.0\"?>";
    
    public static final List<Control<?>> MAME_FATAL_ERROR = Collections.emptyList();
    
    public enum Mode {
        CREATE,
        UPDATE;
    }
    
    // https://docs.oracle.com/javase/tutorial/essential/concurrency/atomic.html
    // Reads and writes are atomic for reference variables and for most primitive variables (all types except long and double).
    // Reads and writes are atomic for all variables declared volatile (including long and double variables).
    private volatile String origin;
    private volatile Machine machine;
    private volatile List<String> parameters;
    private volatile Mode mode;
    
    private Map<String, SoftwareList> softwareLists;
    private final Object dataSync;
    
    public MachineLoader() {
        super();
        dataSync = new Object();
    }
    
    public void setInitialisationData(Machine machine, String origin, Mode mode) {
        // ensure that a machine data loader thread isn't reading the initialisation data while the JavaFX thread write to them
        synchronized (dataSync) {
            this.machine = machine;
            this.parameters = machine.parameters(origin);
            this.origin = origin;
            this.mode = mode;
        }
    }

    public void setSoftwareLists(Map<String, SoftwareList> softwareLists) {
        this.softwareLists = softwareLists;
    }
    
    public Mode getMode() {
        return mode;
    }

    @Override
    protected Task<List<Control<?>>> createTask() {
        return new Task<List<Control<?>>>() {

            @Override
            protected List<Control<?>> call() throws Exception {
                MachineDataHandler dataHandler;
                List<String> params = null;
                // ensure that the JavaFX thread isn't writing to the initialisation data while the current machine data loader thread reads them
                synchronized (dataSync) {
                    dataHandler = new MachineDataHandler(this, machine, softwareLists, origin, mode);
                    if (Configuration.Manager.isAsyncExecutionMode() || Configuration.Manager.isXmlMediaOptionAvailable()) {
                        params = new ArrayList<>(parameters);
                        if (Configuration.Manager.isAsyncExecutionMode())
                            params.add("-lx");
                        else
                            params.add("-lmx");
                    }
                }
                
                if (isCancelled())
                    return null;

                Document doc = null;
                if (Configuration.Manager.isAsyncExecutionMode() || Configuration.Manager.isXmlMediaOptionAvailable()) {
                    // MAME v0.185 and before, or NegaMAME
                    
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(false);
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    try {
                        Logger.getLogger(MachineLoader.class.getName()).log(Level.INFO, params.toString());

                        // time and memory consuming section
                        // so we prevent Negatron from having several threads simultaneously running it
                        synchronized (MachineLoader.this) {
                            boolean tryAgain = false;
                            int attemptCount = 0;

                            if (isCancelled())
                                return null;

                            do { try (InputStream input = Mame.newInputStream(params)) {
                                boolean canProceed = true;

                                if (input.markSupported()) {
                                    input.mark(30);

                                    byte[] header = new byte[33];
                                    input.read(header, 0, 21);

                                    if (!XML_PROLOG.equals(new String(header).trim())) {
                                        canProceed = false;

                                        if (attemptCount < 2) {
                                            // invalid output, so try alternative options before giving up
                                            input.reset();
                                            InputStreamReader stream = new InputStreamReader(input);
                                            BufferedReader reader = new BufferedReader(stream);

                                            String[] error = reader.readLine().split(":");
                                            if (error[0].equals("Error") && error[1].equals(" unknown option")) {
                                                String param = error[2].trim();
                                                int index = params.indexOf(param);

                                                params.remove(index);
                                                if (attemptCount == 0) { // replace faulty option -harddisk by -harddisk1 or -harddisk1 by -harddisk
                                                    if (param.endsWith("1"))
                                                        param = param.substring(0, param.length() - 1);
                                                    else
                                                        param += "1";
                                                    params.add(index, param);
                                                } else { // attemptCount == 1, remove faulty option and its parameter e.g. -harddisk <file path>
                                                    if (params.size() > index && params.get(index) != null && !params.get(index).startsWith("-"))
                                                        params.remove(index);
                                                }
                                                params.remove(0);

                                                tryAgain = true;
                                                ++attemptCount;
                                            } else
                                                tryAgain = false;
                                        } else
                                            tryAgain = false;
                                    } else {
                                        input.reset();
                                        tryAgain = false;
                                    }
                                }

                                if (canProceed)
                                    doc = db.parse(input);
                                else if (!tryAgain) {
                                    Logger.getLogger(MachineLoader.class.getName()).log(
                                        Level.WARNING, "MAME didn't output valid XML"
                                    );
                                    return MAME_FATAL_ERROR;
                                }
                            }} while (tryAgain && !isCancelled());
                        }

                        if (isCancelled())
                            return null;
                    } catch (IOException | SAXException ex) {
                        throw ex;
                    }
                }
                
                dataHandler.process(doc);
                return dataHandler.result();
            }
        };
    }
    
    private static class MachineDataHandler {

        private final Task<?> task;
        private final Machine machine;
        private final Map<String, SoftwareList> softwareLists;
        private final String origin;
        private final Mode mode;
        private List<MergedUnit<?>> differences;
        
        public MachineDataHandler(Task<?> task, Machine machine, Map<String, SoftwareList> softwareLists, String origin, Mode mode) {
            this.task = task;
            this.machine = machine;
            this.softwareLists = softwareLists;
            this.origin = origin;
            this.mode = mode;
        }
        
        private void processXmlDevices(Element machineNode, Merger merger) {
            List<Node> deviceList = Dom.getElementsByTagName(machineNode, "device");
            deviceList.stream().map(
                o -> (Element) o
            ).filter(
                deviceElement -> deviceElement.getElementsByTagName("instance").item(0) != null
            ).map(deviceElement -> {
                    NamedNodeMap attributes = deviceElement.getAttributes();

                    boolean isMandatory = false;
                    if (attributes.getNamedItem("mandatory") != null)
                        isMandatory = true;
                    Node tag = attributes.getNamedItem("tag");
                    
                    Device device = new Device(
                        deviceElement.getElementsByTagName("instance").item(0).getAttributes().getNamedItem("name").getNodeValue(),
                        attributes.getNamedItem("type").getNodeValue(),
                        tag != null ? tag.getNodeValue() : null,
                        isMandatory
                    );

                    Node interfaceFormat = attributes.getNamedItem("interface");
                    device.setInterfaceFormats(
                        interfaceFormat != null ? interfaceFormat.getNodeValue().split(",") : new String[0]
                    );
                    if (machine.getSoftwareLists() != null) {
                        machine.getSoftwareLists().stream().flatMap(softwareListFilter -> {
                            SoftwareList softwareList = softwareLists.get(softwareListFilter.getSoftwareList());
                            if (softwareList != null)
                                return softwareList.getSoftwares(device.getInterfaceFormats(), softwareListFilter.getFilter()).stream();
                            else
                                return null;
                        }).findAny().ifPresent(
                            software -> device.setCompatibleSoftwareLists(true)
                        );
                    }
                    Dom.getElementsByTagName(deviceElement, "extension").stream().map(
                        extension -> extension.getAttributes().getNamedItem("name").getNodeValue()
                    ).forEach(
                        extension -> device.addExtension(extension)
                    );

                    return device;
                }
            ).filter(
                device -> !device.getExtensions().isEmpty()
            ).forEach(
                device -> merger.add(device)
            );
        }
        
        private void processXml(final Document doc, Merger merger) {
            XPath xpath = XPathFactory.newInstance().newXPath();
            Element machineNode = null;
            try {
                machineNode = (Element) xpath.evaluate(
                    String.format("/mame/machine[@name='%s']", machine.getName()), doc, XPathConstants.NODE
                );
                if (machineNode == null) {
                    machineNode = (Element) xpath.evaluate(
                        String.format("/mame/game[@name='%s']", machine.getName()), doc, XPathConstants.NODE
                    );
                    if (machineNode == null)
                        machineNode = (Element) xpath.evaluate(
                            String.format("/mess/machine[@name='%s']", machine.getName()), doc, XPathConstants.NODE
                        );
                }
            } catch (XPathExpressionException ex) {
                Logger.getLogger(MachineLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // internal devices
            List<Node> internalDevices = Dom.getElementsByTagName(machineNode, "device_ref");
            internalDevices.stream().forEach(
                node -> {
                    String name = node.getAttributes().getNamedItem("name").getNodeValue();
                    String description = null;
                    try {
                        description = (String) xpath.evaluate(
                            String.format("/mame/machine[@name='%s']/description/text()", name),
                            doc, XPathConstants.STRING
                        );
                        if (description == null) {
                            description = (String) xpath.evaluate(
                                String.format("/mame/game[@name='%s']/description/text()", name),
                                doc, XPathConstants.STRING
                            );
                            if (description == null)
                                description = (String) xpath.evaluate(
                                    String.format("/mess/machine[@name='%s']/description/text()", name),
                                    doc, XPathConstants.STRING
                                );
                        }
                    } catch (XPathExpressionException ex) {
                        Logger.getLogger(MachineLoader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    machine.addInternalDevice(name, description);
                }
            );
            
            // software lists
            List<Node> softwareListNodes = Dom.getElementsByTagName(machineNode, "softwarelist");
            softwareListNodes.stream().forEach(
                node -> {
                    Node filter = node.getAttributes().getNamedItem("filter");
                    machine.addSoftwareList(
                        node.getAttributes().getNamedItem("name").getNodeValue(),
                        filter != null ? filter.getNodeValue() : null
                    );
                }
            );
            
            // bioses
            List<Node> biosList = Dom.getElementsByTagName(machineNode, "biosset");
            if (biosList.size() > 1) {
                Bios bios = new Bios();
                
                biosList.stream().map(
                    node -> node.getAttributes()
                ).forEach(
                    attributes -> {
                        BiosSet set = new BiosSet(
                            attributes.getNamedItem("name").getNodeValue(),
                            attributes.getNamedItem("description").getNodeValue()
                        );
                        bios.addOption(set, "yes".equals(attributes.getNamedItem("default").getNodeValue()));
                    }
                );
                
                merger.add(bios);
            }
            
            // ram size
            List<Node> ramList = Dom.getElementsByTagName(machineNode, "ramoption");
            if (ramList.size() > 1) {
                Ram ram = new Ram();
                
                ramList.stream().forEach(
                    node -> {
                        RamOption option = new RamOption(node.getTextContent());
                        ram.addOption(option, node.getAttributes().getNamedItem("default") != null);
                    }
                );
                
                merger.add(ram);
            }
            
            // devices
            processXmlDevices(machineNode, merger);
            
            // slots
            List<Node> slotList = Dom.getElementsByTagName(machineNode, "slot");
            slotList.stream().map(
                o -> (Element) o
            ).filter(
                slotElement -> slotElement.hasChildNodes() && slotElement.getChildNodes().getLength() > 1
            ).map(slotElement -> {
                    Slot slot = new Slot(slotElement.getAttribute("name"));
                    
                    Dom.getElementsByTagName(slotElement, "slotoption").stream().map(
                        node -> node.getAttributes()
                    ).forEach(
                        attributes -> {
                            String devName = attributes.getNamedItem("devname").getNodeValue();
                            try {
                                Element element = (Element) xpath.evaluate(String.format("/mame/machine[@name='%s']", devName), doc, XPathConstants.NODE);
                                if (element == null) {
                                    element = (Element) xpath.evaluate(String.format("/mame/game[@name='%s']", devName), doc, XPathConstants.NODE);
                                    if (element == null)
                                        element = (Element) xpath.evaluate(String.format("/mess/machine[@name='%s']", devName), doc, XPathConstants.NODE);
                                }
                                
                                SlotOption option = new SlotOption(
                                    attributes.getNamedItem("name").getNodeValue(),
                                    element.getElementsByTagName("description").item(0).getTextContent()
                                );
                                
                                slot.addOption(option, "yes".equals(attributes.getNamedItem("default").getNodeValue()));
                            } catch (XPathExpressionException ex) {
                                Logger.getLogger(MachineLoader.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    );
                    
                    return slot;
                }
            ).filter(
                slot -> slot.size() > 1
            ).forEach(
                slot -> merger.add(slot)
            );
        }
        
        private void copyDefaults(List<Pair<String, String>> defaults, Machine device, String root) {
            List<Pair<String, String>> deviceDefaults = device.getDefaultSlotOptions();
            if (deviceDefaults != null)
                deviceDefaults.stream().forEach(pair ->
                    defaults.add(new Pair<>(root + pair.getKey(), pair.getValue()))
                );
        }
        
        private void retrieveDefaults(
            Machine device, List<Pair<String, String>> defaults, String root, String[] splitOrigin, int start
        ) {
            copyDefaults(defaults, device, root);

            StringBuilder name = new StringBuilder();
            for (int i = start;i < splitOrigin.length; ++i) {
                if (i != 0)
                    name.append(":");
                name.append(splitOrigin[i]);

                Slot parentSlot = device.getSlots().stream().filter(
                    slot -> slot.getName().equals(name.toString())
                ).findAny().orElse(null);

                if (parentSlot != null) {
                    Machine subdevice = parentSlot.getValue().getDevice();
                    if (subdevice != null)
                        retrieveDefaults(subdevice, defaults,
                            root + name + ":" + parentSlot.getValue().getName(),
                        splitOrigin, ++i);
                }
            }
        }

        private void processSlots(List<Slot> slots, Slot rootSlot, String root, List<Pair<String, String>> defaults) {
            Machine device = rootSlot.getValue().getDevice();
            if (device != null && device.getSlots() != null)
                device.getSlots().forEach(slot -> {
                    String subroot = root + ":" + rootSlot.getValue().getName();
                    String name = subroot + slot.getName();
                    Slot clone = slot.copy(name);
                    List<Pair<String, String>> dft = new ArrayList<>(defaults);
                    copyDefaults(dft, device, subroot);

                    Pair<String, String> value = dft.stream().filter(
                        pair -> pair.getKey().equals(name)
                    ).findFirst().orElse(null);
                    clone.setValue(clone.getOptions().stream().filter(
                        option -> option.getName().equals(value.getValue())
                    ).findFirst().orElse(null));
                    
                    slots.add(clone);
                    processSlots(slots, clone, name, dft);
                });
        }
        
        private void process(Document doc, Merger merger) {
            Bios bios = machine.getBios();
            if (bios != null && bios.size() > 1)
                merger.add(bios);
            Ram ram = machine.getRam();
            if (ram != null && ram.size() > 1)
                merger.add(ram);
            
            String filter = origin + ":";
            
            if (mode == Mode.UPDATE && doc != null) {
                XPath xpath = XPathFactory.newInstance().newXPath();
                Element machineNode = null;
                try {
                    machineNode = (Element) xpath.evaluate(
                        String.format("/mame/machine[@name='%s']", machine.getName()), doc, XPathConstants.NODE
                    );
                } catch (XPathExpressionException ex) {
                    Logger.getLogger(MachineLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                processXmlDevices(machineNode, merger);
            } else {
                List<Device> devices = machine.getDevices();
                if (devices != null) {
                    devices.removeIf(candidate -> candidate.getTag().startsWith(filter));
                    devices.stream().filter(
                        device -> !device.getExtensions().isEmpty()
                    ).forEach(
                        device -> merger.add(device.copy())
                    );
                }
            }
            
            List<Slot> slots = machine.getSlots();
            if (slots != null) {
                // as the merger expects to be fed with completely new instances
                // of all the slots and be able to completely scrap the content
                // of the old instances, clone the slots accordingly
                List<Slot> copiedSlots = new ArrayList<>();
                slots.forEach(slot -> copiedSlots.add(slot.copy()));
                slots = copiedSlots;
                
                if (mode == Mode.UPDATE) {
                    // find the original slot that triggered the current reloading process
                    Slot originSlot = slots.stream().filter(
                        slot -> slot.getName().equals(origin)
                    ).findAny().orElse(null);

                    if (originSlot != null) {
                        // remove subslots and subdevices linked to the slot that has been changed
                        slots.removeIf(candidate -> candidate.getName().startsWith(filter));

                        // add the new subslots and subdevices linked to the newly selected slot option
                        if (!originSlot.getValue().getName().isEmpty()) {
                            List<Pair<String, String>> defaults = new ArrayList<>();
                            retrieveDefaults(machine, defaults, "", origin.split(":"), 0);
                            processSlots(slots, originSlot, origin, defaults);
                        }
                    }
                }
                
                slots.stream().filter(
                    slot -> slot.size() > 1
                ).sorted(
                    (s1, s2) -> s1.getName().compareTo(s2.getName())
                ).forEachOrdered(
                    slot -> merger.add(slot)
                );
            }
        }
        
        public void process(final Document doc) {
            Merger merger = machine.reset(origin);
            
            if (Configuration.Manager.isAsyncExecutionMode())
                processXml(doc, merger);
            else
                process(doc, merger);
            
            if ((merger.merge() || mode == Mode.CREATE) && !task.isCancelled())
                differences = merger.commit();
            else
                merger.rollback();
        }
        
        @SuppressWarnings("unchecked")
        public List<Control<?>> result() {
            if (differences == null)
                return null;
            
            List<Control<?>> views = new ArrayList<>();
            differences.stream().forEachOrdered(
                mergedUnit -> {
                    MachineElement<?> element;
                    Difference status = mergedUnit.getStatus();

                    switch (status) {
                        case Difference.ADDED:
                        case Difference.UNCHANGED:
                        default:
                            element = mergedUnit.getNewElement();
                            break;
                        case Difference.DELETED:
                            element = mergedUnit.getOldElement();
                            break;
                    }

                    switch (element) {
                        case Device d ->  views.add(new DeviceControl(d, status));
                        case Slot s -> views.add(new SlotControl(s, status));
                        default -> views.add(new ChoiceControl<>((Choice<?>) element, status)); // Bios / Ram
                    }
                }
            );
            return views;
        }
    }
}
