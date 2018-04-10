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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javax.xml.parsers.SAXParserFactory;
import net.babelsoft.negatron.io.Mame;
import net.babelsoft.negatron.io.cache.MachineListCache;
import net.babelsoft.negatron.io.cache.MachineListCache.Data;
import net.babelsoft.negatron.io.loader.MachineListLoader.MachineListData;
import net.babelsoft.negatron.model.item.Machine;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author capan
 */
public class MachineListLoader implements Callable<MachineListData> {
    
    public static class MachineListData {
        private final Data list;
        private final Map<String, Machine> map;
        
        public MachineListData() {
            this(new Data());
        }
        
        public MachineListData(Data list) {
            this.list = list;
            this.map = new HashMap<>();
            
            list.forEach(machine -> map.put(machine.getName(), machine));
        }
        
        public Data getList() { return list; }
        public Map<String, Machine> getMap() { return map; }
        
        public void add(Machine machine) {
            list.add(machine);
            map.put(machine.getName(), machine);
        }
    }

    private final SimpleDoubleProperty progressProperty;

    public MachineListLoader(SimpleDoubleProperty progressProperty) {
        this.progressProperty = progressProperty;
    }

    @Override
    public MachineListData call() throws Exception {
        MachineListCache cache = new MachineListCache();
        
        if (cache.exists()) try {
            progressProperty.set(0.0);

            Data data = cache.load();
            MachineListData result = new MachineListData(data);

            progressProperty.set(1.0);
            return result;
        } catch (Exception ex) {
            Logger.getLogger(MachineListLoader.class.getName()).log(
                Level.WARNING, "Cache has been corrupted, reload from source.", ex
            );
        }
        
        // Determine total number of machines to process
        long totalCount;
        try (
            InputStream input = Mame.newInputStream("-ll");
            InputStreamReader stream = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(stream);
        ) {
            totalCount = reader.lines().count();
        }
        
        // Parse MAME database
        MachineListDataHandler dataHandler = new MachineListDataHandler(totalCount);
        progressProperty.bind(dataHandler.ProgressProperty());

        try (InputStream dataStream = Mame.newInputStream("-lx")) {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(false);
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();

            xmlReader.setContentHandler(dataHandler);
            xmlReader.parse(new InputSource(dataStream));
        } catch (Exception ex) {
            Logger.getLogger(MachineListLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        MachineListData result = dataHandler.result();
        try {
            cache.save(result.getList());
        } catch (Exception ex) {
            Logger.getLogger(MachineListLoader.class.getName()).log(
                Level.WARNING, "Couldn't save cache to file.", ex
            );
        }
        return result;
    }

    private static class MachineListDataHandler extends EmulatedItemListDataHandler<Machine> {

        private final SimpleDoubleProperty progressProperty = new SimpleDoubleProperty(0.0);

        private final long totalCount;
        private long currentCount;
        private final MachineListData machines;
        
        private int biosCount;
        private int ramCount;
        private int slotOptionCount;

        public MachineListDataHandler(long totalCount) {
            super((name, group) -> new Machine(name, group));
            machines = new MachineListData();
            
            this.totalCount = totalCount;
            currentCount = 0;
        }

        public ReadOnlyDoubleProperty ProgressProperty() {
            return progressProperty;
        }

        public MachineListData result() {
            return machines;
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
                case "machine":
                case "game":
                    if ("yes".equals(atts.getValue("runnable"))) {
                        buildCurrentItem(
                            atts.getValue("name"),
                            atts.getValue("sourcefile"),
                            atts.getValue("cloneof")
                        );
                        currentItem.setMechanical("yes".equals(atts.getValue("ismechanical")));
                        biosCount = 0;
                        ramCount = 0;
                    }
                    break;
                case "manufacturer":
                    startTextElement();
                    break;
                case "input":
                    startConsumeCurrentItem(
                        Machine::setCoinSlot, atts.getValue("coins") != null
                    );
                    startConsumeCurrentItem(
                        Machine::setServiceMode, "yes".equals(atts.getValue("service"))
                    );
                    startConsumeCurrentItem(
                        Machine::setTilt, "yes".equals(atts.getValue("tilt"))
                    );
                    startConsumeCurrentItem(
                        Machine::setMaxNumberPlayers, atts.getValue("players")
                    );
                    break;
                case "control": // input > control
                    startConsumeCurrentItem(
                        Machine::addControllerType, atts.getValue("type")
                    );
                    break;
                case "sound":
                    startConsumeCurrentItem(
                        Machine::setSoundType, atts.getValue("channels")
                    );
                    break;
                case "display":
                    startConsumeCurrentItem(
                        Machine::setDisplayType, atts.getValue("type")
                    );
                    startConsumeCurrentItem(
                        Machine::setScreenOrientation, atts.getValue("rotate")
                    );
                    break;
                case "driver":
                    startConsumeCurrentItem(
                        Machine::setSupport, atts.getValue("status")
                    );
                    break;
                case "biosset":
                    if (++biosCount > 1)
                        startConsumeCurrentItem(Machine::setConfigurable, true
                        );
                    break;
                case "ramoption":
                    if (++ramCount > 1)
                        startConsumeCurrentItem(Machine::setConfigurable, true
                        );
                    break;
                case "extension": // device > extension
                    startConsumeCurrentItem(Machine::setConfigurable, true
                    );
                    break;
                case "slot":
                    slotOptionCount = 0;
                    break;
                case "slotoption": // slot > slotoption
                    if (++slotOptionCount > 1)
                        startConsumeCurrentItem(Machine::setConfigurable, true
                        );
                    break;
                case "softwarelist":
                    startConsumeCurrentItem(
                        Machine::setSoftwareEmbedded, false
                    );
                    break;
            }
        }

        @Override
        public void endElement(
            String namespaceURI,
            String localName,
            String qName
        ) throws SAXException {
            super.endElement(namespaceURI, localName, qName);
            
            switch (qName) {
                case "machine":
                case "game":
                    endConsumeCurrentItem(MachineListData::add, machines);
                    
                    ++currentCount;
                    if (currentCount % 50 == 0)
                        progressProperty.set((double) currentCount / (double) totalCount);
                    break;
                case "manufacturer":
                    endTextElement(Machine::setManufacturer);
                    break;
            }
        }
    }
}
