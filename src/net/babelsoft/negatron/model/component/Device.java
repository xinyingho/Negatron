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
package net.babelsoft.negatron.model.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import net.babelsoft.negatron.util.Strings;

/**
 *
 * @author capan
 */
public final class Device extends MachineElement<String> implements MachineComponent<String, StringProperty> {
    private static final long serialVersionUID = 1L;

    private final String type;
    private final String tag;
    private final boolean mandatory;
    private final List<String> extensions;
    private final List<String> interfaceFormats;
    private transient StringProperty value;
    private boolean compatibleSoftwareLists;
    
    public Device(String name, String type, String tag, boolean mandatory) {
        super(name);
        this.type = type;
        this.tag = tag;
        this.mandatory = mandatory;
        
        extensions = new ArrayList<>();
        interfaceFormats = new ArrayList<>(1);
    }
    
    protected Device(Device ref) {
        super(ref);
        type = ref.type;
        tag = ref.tag;
        mandatory = ref.mandatory;
        
        extensions = new ArrayList<>();
        interfaceFormats = new ArrayList<>(1);
        
        ref.extensions.forEach(extension -> extensions.add(extension));
        ref.interfaceFormats.forEach(extension -> interfaceFormats.add(extension));
        setValue(ref.getValue());
        compatibleSoftwareLists = ref.compatibleSoftwareLists;
    }
    
    @Override
    public String getValue() {
        return valueProperty().get();
    }

    @Override
    public void setValue(String value) {
        valueProperty().set(value);
    }
    
    @Override
    public boolean setValue(MachineElement<String> param) {
        Device device = (Device) param;
        if (
            device != null && (
                getInterfaceFormats().isEmpty() ||
                getInterfaceFormats().equals(device.getInterfaceFormats())
            ) && (
                getTag() == null ||
                getTag().equals(device.getTag())
            )
        ) {
            setValue(device.getValue());
            return true;
        } else
            return false;
    }
    
    @Override
    public StringProperty valueProperty() {
        if (value == null)
            value = new SimpleStringProperty(this, "value");
        return value;
    }
    
    public String getType() {
        return type;
    }
    
    public String getTag() {
        return tag;
    }
    
    public void setInterfaceFormats(String[] interfaceFormats) {
        this.interfaceFormats.addAll(Arrays.asList(interfaceFormats));
    }
    
    public void addInterfaceFormat(String interfaceFormat) {
        interfaceFormats.add(interfaceFormat);
    }
    
    public List<String> getInterfaceFormats() {
        return interfaceFormats;
    }

    public void addExtension(String extension) {
        extensions.add("*." + extension);
    }
    
    public List<String> getExtensions() {
        return extensions;
    }

    public void setCompatibleSoftwareLists(boolean compatibleSoftwareLists) {
        this.compatibleSoftwareLists = compatibleSoftwareLists;
    }
    
    public boolean hasCompatibleSoftwareLists() {
        return compatibleSoftwareLists;
    }
    
    @Override
    public boolean isMandatory() {
        return mandatory;
    }
    
    @Override
    public boolean isReady() {
        return !mandatory || Strings.isValid(getValue());
    }

    @Override
    public List<String> parameters() {
        String v = getValue();
        
        List<String> param = new ArrayList<>();
        if (hasDependenciesValidated()) {
            param.add("-" + getName());
            if (Strings.isEmpty(v))
                param.add("\"\"");
            else if (v.contains(" "))
                param.add("\"" + v + "\"");
            else
                param.add(v);
        }
        return param;
    }

    @Override
    public <S extends MachineComponent<String, StringProperty>> boolean canReplace(S otherComponent) {
        if (otherComponent instanceof Device) {
            Device otherDevice = (Device) otherComponent;
            
            List<String> interfaceFormat = getInterfaceFormats();
            List<String> otherInterfaceFormat = otherDevice.getInterfaceFormats();
            
            String name = getName();
            String previousName = getPreviousName();
            String otherName = otherDevice.getName();
            String otherPreviousName = otherDevice.getPreviousName();

            return interfaceFormat.equals(otherInterfaceFormat) && (
                name.equals(otherName) || name.equals(otherPreviousName) ||
               (previousName != null ? previousName.equals(otherName) : false)
            );
        } else
            return false;
    }

    @Override
    public MachineElement<?> copy() {
        return new Device(this);
    }
    
    @Override
    public void write(XMLStreamWriter writer) throws XMLStreamException {
        if (interfaceFormats.size() > 0 || extensions.size() > 0)
            writer.writeStartElement("device");
        else
            writer.writeEmptyElement("device");
        writer.writeAttribute("name", getName());
        writer.writeAttribute("type", getType());
        writer.writeAttribute("tag", getTag());
        writer.writeAttribute("mandatory", Boolean.toString(isMandatory()));
        writer.writeAttribute("compatibleSoftwareLists", Boolean.toString(hasCompatibleSoftwareLists()));
        String v = getValue();
        if (!Strings.isEmpty(v))
            writer.writeAttribute("value", v);
        if (interfaceFormats.size() > 0) {
            writer.writeStartElement("interfaces");
            for (String interfaceFormat : interfaceFormats) {
                writer.writeEmptyElement("interface");
                writer.writeAttribute("name", interfaceFormat);
            }
            writer.writeEndElement();
        }
        if (extensions.size() > 0) {
            writer.writeEmptyElement("extensions");
            writer.writeAttribute("names", extensions.stream().map(
                ext -> ext.substring(2)
            ).filter(
                ext -> !ext.equals("zip")
            ).collect(
                Collectors.joining(",")
            ));
        }
        if (interfaceFormats.size() > 0 || extensions.size() > 0)   
            writer.writeEndElement();
    }
}
