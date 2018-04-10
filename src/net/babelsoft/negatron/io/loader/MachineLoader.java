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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.babelsoft.negatron.io.Mame;
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
            this.parameters = machine.parameters();
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
                List<String> params;
                // ensure that the JavaFX thread isn't writing to the initialisation data while the current machine data loader thread reads them
                synchronized (dataSync) {
                    dataHandler = new MachineDataHandler(this, machine, softwareLists, origin, mode);
                    params = parameters;
                    params.add("-lx");
                }
                
                if (isCancelled())
                    return null;

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(false);
                DocumentBuilder db = dbf.newDocumentBuilder();
                
                try {
                    Logger.getLogger(MachineLoader.class.getName()).log(Level.INFO, params.toString());
                    
                    // time and memory consuming section
                    // so we prevent Negatron from having several threads simultaneously running it
                    Document doc = null;
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
                    dataHandler.process(doc);
                    return dataHandler.result();
                } catch (IOException | SAXException ex) {
                    throw ex;
                }
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
        
        public void process(final Document doc) {
            Merger merger = machine.reset(origin);
            
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
                device -> device.getExtensions().size() > 0
            ).forEach(
                device -> merger.add(device)
            );
            
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
                        case ADDED:
                        case UNCHANGED:
                        default:
                            element = mergedUnit.getNewElement();
                            break;
                        case DELETED:
                            element = mergedUnit.getOldElement();
                            break;
                    }

                    if (element instanceof Device)
                        views.add(new DeviceControl((Device) element, status));
                    else // Bios / Ram / Slot
                        views.add(new ChoiceControl((Choice) element, status));
                }
            );
            return views;
        }
    }
}
