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
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.SAXParserFactory;
import net.babelsoft.negatron.io.cache.InformationCache;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.PathCharset;
import net.babelsoft.negatron.io.configuration.Property;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.model.item.SoftwareList;
import net.babelsoft.negatron.util.Strings;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author capan
 */
public class InformationLoader implements InitialisedCallable<Void> {
    
    public static final String OBS_ID = "information";
    
    private final InformationCache cache;
    private final Path datFilePath;
    private LoadingObserver observer;
    
    public InformationLoader(InformationCache cache, Path datFilePath) {
        this.cache = cache;
        this.datFilePath = datFilePath;
    }

    @Override
    public void initialise(LoadingObserver observer, Map<String, Machine> machines, Map<String, SoftwareList> softwareLists) {
        this.observer = observer;
    }

    @Override
    public Void call() throws Exception {
        observer.begin(OBS_ID, -1);
        
        Map<String, List<String>> systemIndex = new HashMap<>(); // system > system aliases
        Map<String, Map<String, String>> itemIndex = new HashMap<>(); // systems > item > item alias
        Map<String, Map<String, String>> information = new HashMap<>(); // system alias > item alias > content

        try {
            if (datFilePath.endsWith("dat")) {
                CharsetDecoder decoder;
                {
                    List<PathCharset> encodings = Configuration.Manager.getFilePaths(Property.INFORMATION);
                    Optional<PathCharset> encoding = encodings.stream().filter(
                        pathCharset -> pathCharset.getPath().equals(datFilePath)
                    ).findAny();
                    decoder = Charset.forName(encoding.get().getCharSet()).newDecoder();
                    decoder.onMalformedInput(CodingErrorAction.REPLACE);
                }

                parseDatFile(datFilePath, information, systemIndex, itemIndex, decoder);
            } else
                parseXmlFile(datFilePath, information, systemIndex, itemIndex);

            cache.save(new InformationData(datFilePath, information, systemIndex, itemIndex));
        } finally {
            observer.end(OBS_ID);
        }
        
        return null;
    }

    private void parseDatFile(
            Path datFilePath,
            Map<String, Map<String, String>> information, Map<String, List<String>> systemIndex, Map<String, Map<String, String>> itemIndex,
            CharsetDecoder decoder
    ) throws Exception {
        try (
            InputStream input = Files.newInputStream(datFilePath);
            InputStreamReader stream = new InputStreamReader(input, decoder);
            BufferedReader reader = new BufferedReader(stream);
        ) {
            String line;
            String systems = null;
            String items = null;
            boolean mustAppend = false;
            StringBuilder sb = null;

            while ((line = reader.readLine()) != null) {
                line = Strings.rtrim(line);

                if (!mustAppend && line.startsWith("$") && line.contains("=")) {
                    // start processing a new entry
                    String[] systemItems = line.substring(1).split("=");
                    if (systemItems.length == 2) { // ignore invalid entries like "$info="
                        systems = systemItems[0];
                        items = systemItems[1];

                        sb = new StringBuilder();
                        mustAppend = true;
                    }
                } else if (mustAppend && "$end".equals(line)) {
                    // end processing the entry
                    if (information.get(systems) == null)
                        information.put(systems, new HashMap<>());
                    String content = sb.toString();

                    // special processing for the badly formatted sysinfo.dat
                    if (datFilePath.endsWith("sysinfo.dat")) {
                        Pattern pattern = Pattern.compile("\\S\\n\\n");
                        Matcher matcher = pattern.matcher(content);
                        sb = new StringBuilder();
                        int from = 0;

                        while (matcher.find()) {
                            int to = matcher.start();
                            sb.append(content.substring(from, to));
                            sb.append(matcher.group().charAt(0));
                            sb.append("\n");
                            from = matcher.end();
                        }

                        if (from != content.length() - 1)
                            sb.append(content.substring(from));
                        content = sb.toString();
                        content = content.replaceAll("\\n{7}", "\n\n\n\n");
                        content = content.replaceAll("\\n{5}", "\n\n\n");
                        content = content.replaceAll("\\n{3}", "\n\n");
                    }

                    // add content to result
                    if (items.endsWith(",")) // remove trailing separators
                        items = items.substring(0, items.length() - 1);
                    if (Strings.isValid(items)) { // ignore entries like "$info=,"
                        Map<String, String> systemMap = information.get(systems);
                        if (systemMap.get(items) != null)
                            content = systemMap.get(items) + content;
                        systemMap.put(items, content);

                        if (systems.contains(","))
                            for (String system : systems.split(","))
                                if (Strings.isValid(system)) { // ignore invalid system subentries like in "$xxx,,yyy=zzz,"
                                    // some system content can be spread through several folders
                                    // e.g. odyssey2 content is split into "odyssey2" and "odyssey2,g7400" folders
                                    List<String> folders = systemIndex.get(system);
                                    if (folders == null) {
                                        folders = new ArrayList<>();
                                        systemIndex.put(system, folders);
                                    }
                                    if (!folders.contains(systems))
                                        folders.add(systems);
                                }
                        if (items.contains(","))
                            for (String item : items.split(","))
                                if (Strings.isValid(item)) { // ignore invalid item subentries, e.g. in "$info=xxx,,yyy," ignore 2nd subentry
                                    // an item of a particular system can have eponymous items in other systems
                                    // e.g. genesis is both in "info" and "pc8801_flop" systems
                                    Map<String, String> itemMap = itemIndex.get(systems);
                                    if (itemMap == null) {
                                        itemMap = new HashMap<>();
                                        itemIndex.put(systems, itemMap);
                                    }
                                    itemMap.put(item, items);
                                }
                    }
                    sb = null;
                    mustAppend = false;
                } else if (mustAppend && "$drv".equals(line)) {
                    // don't care about MAME driver history
                    sb = null;
                    mustAppend = false;
                } else if (mustAppend && !"$bio".equals(line) && !"$mame".equals(line) && !"$story".equals(line) && !"$cmd".equals(line))
                    sb.append(line).append("\n");
                // else do nothing
            }
        }
    }

    private void parseXmlFile(
            Path datFilePath,
            Map<String, Map<String, String>> information, Map<String, List<String>> systemIndex, Map<String, Map<String, String>> itemIndex
    ) throws Exception {
        try (InputStream input = Files.newInputStream(datFilePath)) {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(false);
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();

            xmlReader.setContentHandler(new InputHandler(information, systemIndex, itemIndex));
            xmlReader.parse(new InputSource(input));
        }
    }
    
    private class InputHandler extends DefaultHandler {

        private final Map<String, Map<String, String>> information;
        private final Map<String, List<String>> sysIndex;
        private final Map<String, Map<String, String>> itemIndex;
        
        private StringBuilder text;
        private Map<String, List<String>> systemItemMap;
        private String systemAlias, itemAlias;
        
        private InputHandler(Map<String, Map<String, String>> information, Map<String, List<String>> systemIndex, Map<String, Map<String, String>> itemIndex) {
            this.information = information;
            this.sysIndex = systemIndex;
            this.itemIndex = itemIndex;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case "entry" -> systemItemMap = new HashMap<>();
                case "system", "item" -> {
                    String system = attributes.getValue("list"),
                            item = attributes.getValue("name");
                    if (system == null)
                        system = "info";
                    
                    if (systemItemMap.isEmpty()) {
                        systemAlias = system;
                        itemAlias = item;
                    }
                    
                    if (!systemItemMap.containsKey(system))
                        systemItemMap.put(system, new ArrayList<>());
                    systemItemMap.get(system).add(item);
                }
                case "text" -> text = new StringBuilder();
            }
        }
        
        @Override
        public void characters(char[] chars, int start, int length) throws SAXException {
            if (text != null)
                text.append(chars, start, length);
        }
    
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("text")) {
                if (!information.containsKey(systemAlias))
                    information.put(systemAlias, new HashMap<>());
                information.get(systemAlias).put(itemAlias, text.toString());

                text = null;

                for (String system : systemItemMap.keySet()) {
                    for (String item : systemItemMap.get(system)) {
                        if (!sysIndex.containsKey(system))
                            sysIndex.put(system, new ArrayList<>());
                        List<String> systemAliases = sysIndex.get(system);
                        if (!systemAliases.contains(systemAlias))
                            systemAliases.add(systemAlias);
                        
                        if (!itemIndex.containsKey(system))
                            itemIndex.put(system, new HashMap<>());
                        itemIndex.get(system).put(item, itemAlias);
                    }
                }
            }
        }
    }
}
