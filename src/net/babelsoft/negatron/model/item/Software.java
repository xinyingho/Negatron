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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.babelsoft.negatron.model.Support;

/**
 *
 * @author capan
 */
public class Software extends EmulatedItem<Software> {
    private static final long serialVersionUID = 3L;
    private static final List<SoftwarePart> EMPTY = Collections.emptyList();
    
    public static class Requirement implements Serializable {
        private static final long serialVersionUID = 2L;
        
        private String softwareList;
        private String software;
        
        private Requirement(String softwareList, String software) {
            this.softwareList = softwareList;
            this.software = software;
        }

        private Requirement(String software) {
            this.software = software;
        }

        /**
         * @return the softwareList
         */
        public String getSoftwareList() {
            return softwareList;
        }

        /**
         * @return the software
         */
        public String getSoftware() {
            return software;
        }
    }
    
    private List<SoftwarePart> parts;
    private String[] compatibility;
    private Requirement requirement;
    private String publisher;
    private transient Support displayedSupport;

    public Software(String name, String softwareList, Support support) {
        super(name, softwareList);
        setSupport(support);
    }

    public Software(String name, String softwareList, String support) {
        this(name, softwareList, Support.fromString(support));
    }

    public Software(String name, String softwareList) {
        this(name, softwareList, Support.NOT_AVAILABLE);
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    private String getPublisher() {
        return publisher;
    }
    
    public void addSoftwarePart(SoftwarePart part) {
        if (parts == null)
            parts = new ArrayList<>(1);
        parts.add(part);
    }
    
    public void setLastPartDescription(String description) {
        parts.get(parts.size() - 1).setDescription(description);
    }
    
    public void trimSoftwareParts() {
        if (parts.size() <= 1)
            parts = null;
        else
            setConfigurable(true);
    }
    
    public void setSoftwareParts(List<SoftwarePart> parts) {
        this.parts = parts;
    }
    
    public List<SoftwarePart> getSoftwareParts() {
        if (parts == null)
            return EMPTY;
        else
            return parts;
    }
    
    public void setCompatibility(String[] compatibility) {
        this.compatibility = compatibility;
    }
    
    public String[] getCompatibility() {
        return compatibility;
    }
    
    public void setRequirement(String requirementCommand) {
        if (requirementCommand.contains(":")) {
            String[] requirementArray = requirementCommand.split(":");
            requirement = new Requirement(requirementArray[0], requirementArray[1]);
        } else
            requirement = new Requirement(requirementCommand);
    }
    
    public Requirement getRequirement() {
        return requirement;
    }
    
    @Override
    public void setNotCompatible() {
        this.displayedSupport = Support.NOT_COMPATIBLE;
    }
    
    @Override
    public boolean isNotCompatible() {
        return displayedSupport == Support.NOT_COMPATIBLE;
    }

    @Override
    public Support getSupport() {
        return displayedSupport;
    }
    
    @Override
    public String getCompany() {
        return getPublisher();
    }
    
    @Override
    public void reset() {
        displayedSupport = super.getSupport();
    }
}
