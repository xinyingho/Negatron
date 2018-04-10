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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author capan
 */
public class SoftwareList extends Item {
    private static final long serialVersionUID = 5L;
    
    private final Map<String, Software> softwareMap = new HashMap<>();
    private final Map<String, List<Software>> interfaceSoftwareMap = new HashMap<>();
    private final Map<String, List<String>> softwareInterfaceMap = new HashMap<>();
    
    public SoftwareList(String name, String description) {
        super(name);
        setDescription(description);
    }
    
    public void addSoftware(Software software) {
        softwareMap.put(software.getName(), software);
        
        List<String> interfaceFormats = new ArrayList<>(1);
        
        software.getSoftwareParts().stream().map(
            part -> part.getInterfaceFormat()
        ).distinct().forEach(interfaceFormat -> {
            interfaceFormats.add(interfaceFormat);
            
            List<Software> softwares = interfaceSoftwareMap.get(interfaceFormat);
            if (softwares == null) {
                softwares = new ArrayList<>();
                interfaceSoftwareMap.put(interfaceFormat, softwares);
            }
            softwares.add(software);
        });
        softwareInterfaceMap.put(software.getName(), interfaceFormats);
        
        // remove information, becoming useless from this point onwards, to save cache memory space
        software.trimSoftwareParts();
    }

    public Software getSoftware(String name) {
        return softwareMap.get(name);
    }
    
    public List<Software> getSoftwares(List<String> interfaceFormats, String filter) {
        return interfaceFormats.stream().flatMap(
            interfaceFormat -> getSoftwares(interfaceFormat, filter).stream()
        ).collect(Collectors.toList());
    }
    
    private List<Software> getSoftwares(String interfaceFormat, String filter) {
        List<Software> softwares = interfaceSoftwareMap.get(interfaceFormat);
        
        if (softwares != null)
            if (filter != null)
                if (filter.charAt(0) == '!') {
                    final String unwantedFormat = filter.substring(1);
                    
                    return softwares.stream().filter(
                        software -> {
                            String[] compatibility = software.getCompatibility();
                            return compatibility == null || Arrays.stream(compatibility).allMatch(
                                format -> !format.equals(unwantedFormat)
                            );
                        }
                    ).collect(Collectors.toList());
                } else {
                    final String expectedFormat = filter;
                    
                    return softwares.stream().filter(
                        software -> {
                            String[] compatibility = software.getCompatibility();
                            return compatibility == null || Arrays.stream(compatibility).anyMatch(
                                format -> format.equals(expectedFormat)
                            );
                        }
                    ).collect(Collectors.toList());
                }
            else
                return softwares;
        else
            return new ArrayList<>();
    }

    public String getInterfaceFormat(String software) {
        List<String> interfaceFormats = softwareInterfaceMap.get(software);
        
        if (interfaceFormats != null && interfaceFormats.size() > 0)
            return interfaceFormats.get(0);
        else
            return null;
    }
}
