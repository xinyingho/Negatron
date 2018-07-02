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

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import net.babelsoft.negatron.io.cache.SoftwareListCache;
import net.babelsoft.negatron.model.item.Software;
import net.babelsoft.negatron.model.item.SoftwareList;
import net.babelsoft.negatron.model.item.SoftwarePart;
import net.babelsoft.negatron.model.statistics.SoftwareStatistics;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author capan
 */
public class SoftwareListLoader implements Callable<Void> {
    
    private final SoftwareListCache cache;
    private final Path path;
    
    public SoftwareListLoader(SoftwareListCache cache, Path path) {
        this.cache = cache;
        this.path = path;
    }

    @Override
    public Void call() {
        SoftwareListDataHandler dataHandler = new SoftwareListDataHandler(path, cache.getStatistics());
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(false);
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();

            xmlReader.setContentHandler(dataHandler);
            xmlReader.parse(new InputSource(path.toString()));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(SoftwareListLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        SoftwareList result = dataHandler.result();
        try {
            cache.save(result);
        } catch (Exception ex) {
            Logger.getLogger(SoftwareListLoader.class.getName()).log(
                Level.WARNING, "Couldn't save cache to file.", ex
            );
        }
        return null;
    }
    
    private static class SoftwareListDataHandler extends EmulatedItemListDataHandler<Software> {

        private final String name;
        private SoftwareList softwareList;
        private final SoftwareStatistics statistics;

        public SoftwareListDataHandler(Path path, final SoftwareStatistics statistics) {
            super((name, group) -> new Software(name, group));
            name = SoftwareListCache.convertPathToName(path);
            this.statistics = statistics;
        }
        
        public SoftwareList result() {
            return softwareList;
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
                case "softwarelist":
                    softwareList = new SoftwareList( name, atts.getValue("description") );
                    break;
                case "software":
                    buildCurrentItem(
                        atts.getValue("name"),
                        softwareList.getName(),
                        atts.getValue("cloneof")
                    );
                    currentItem.setSupport(atts.getValue("supported"));
                    break;
                case "publisher":
                    startTextElement();
                    break;
                case "sharedfeat":
                    switch (atts.getValue("name")) {
                        case "compatibility":
                            startConsumeCurrentItem(
                                Software::setCompatibility, atts.getValue("value").split(",")
                            );
                            break;
                        case "requirement":
                            String requirement = atts.getValue("value");
                            
                            // requirements should follow the format [<software list>:<software>] like in c64_cass.xml <sharedfeat name="requirement" value="c64_cart:music64"/>
                            // or the format [<software>] only in pcecd.xml and a2600_cass.xml <sharedfeat name="requirement" value="scdsys"/>
                            
                            if (requirement.contains(" -")) // syntax used in saturn.xml <sharedfeat name="requirement" value="sat_cart -kof95"/>
                                requirement = requirement.replace(" -", ":"); // normalisation
                            
                            startConsumeCurrentItem(
                                Software::setRequirement, requirement
                            );
                            break;
                    }
                    break;
                case "part":
                    startConsumeCurrentItem(Software::addSoftwarePart, new SoftwarePart(
                            atts.getValue("name"), atts.getValue("interface")
                        )
                    );
                    break;
                case "feature":
                    // features listed here are those used for multi-part software as of v0.168
                    switch(atts.getValue("name")) {
                        case "Disc Code":
                        case "disk_label":
                        case "part_id":
                        case "part id":
                        case "slot":
                            startConsumeCurrentItem(
                                Software::setLastPartDescription, atts.getValue("value")
                            );
                            break;
                        case "disk_serial":
                        case "exrom":
                        case "game":
                        case "mapper":
                        case "part_number":
                        default:
                            break;
                    }
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
                case "software":
                    // Do not reverse the 2 following lines as
                    // SoftwareList::addSoftware can remove data important to generate stats
                    statistics.add(currentItem);
                    endConsumeCurrentItem(SoftwareList::addSoftware, softwareList);
                    break;
                case "publisher":
                    endTextElement(Software::setPublisher);
                    break;
            }
        }
    }
}
