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
package net.babelsoft.negatron.model.favourites;

import java.time.LocalDateTime;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import net.babelsoft.negatron.io.XmlOutput;
import net.babelsoft.negatron.model.IconDescription;
import net.babelsoft.negatron.model.component.MachineElement;
import net.babelsoft.negatron.model.component.MachineElementList;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.model.item.Software;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.view.control.adapter.SelectionData;

/**
 *
 * @author capan
 */
public class Favourite implements XmlOutput {
    
    // ReadOnlyObjectWrapper replacement, which uses less memory and is more secure
    private final class ReadOnlyDateTimeProperty extends ReadOnlyObjectPropertyBase<LocalDateTime> {

        private LocalDateTime date;

        private void set(LocalDateTime newValue) {
            date = newValue;
            fireValueChangedEvent();
        }

        @Override
        public final LocalDateTime get() { return date; }

        @Override
        public Object getBean() { return Favourite.this; }

        @Override
        public String getName() { return "datetime"; }
    }

    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<Machine> machine = new SimpleObjectProperty<>();
    private final ObjectProperty<SoftwareConfiguration> softwareConfiguration = new SimpleObjectProperty<>();
    private final ObjectProperty<MachineConfiguration> machineConfiguration = new SimpleObjectProperty<>();
    
    private final ReadOnlyDateTimeProperty dateCreated = new ReadOnlyDateTimeProperty();
    private final ReadOnlyDateTimeProperty dateModified = new ReadOnlyDateTimeProperty();
    
    private final transient ObjectProperty<IconDescription> iconName = new SimpleObjectProperty<>();
    private transient String shortcut;
    private transient BooleanProperty invalidated;
    private transient boolean disableDateModifiedUpdate;
    private transient boolean machineEditable;
    private transient BooleanProperty mustMigrate = new SimpleBooleanProperty();
    
    protected Favourite() { // separator creation
        dateCreated.set(LocalDateTime.now());
        dateModified.set(LocalDateTime.now());
    }
    
    protected Favourite(LocalDateTime dateCreated, LocalDateTime dateModified) { // separator creation
        this.dateCreated.set(dateCreated);
        this.dateModified.set(dateModified);
    }
    
    protected Favourite(Favourite ref) {
        this(
            ref.getName(), ref.getMachine(), ref.getSoftwareConfiguration(), ref.getMachineConfiguration(),
            LocalDateTime.now(), LocalDateTime.now(),
            ref.getIconName() != null ? ref.getIconName().getIcon() : null
        );
    }
    
    public Favourite(String name, Image image) { // empty favourite creation
        this(name, image, true);
    }
    
    protected Favourite(String name, Image image, boolean machineEditable) { // folder creation
        this(name, null, null, null, LocalDateTime.now(), LocalDateTime.now(), image);
        this.machineEditable = machineEditable;
    }
    
    public Favourite(String commandLine) { // favourite creation
        this(
            Language.Manager.getString("newCommandLine"), null, null,
            new MachineConfiguration(commandLine),
            LocalDateTime.now(), LocalDateTime.now()
        );
    }
    
    public Favourite(SelectionData data) { // favourite creation
        this(
            data.getSoftwareConfiguration() != null ? data.getSoftwareConfiguration().getSoftware().getDescription() : data.getMachine().getDescription(),
            data.getMachine(), data.getSoftwareConfiguration(), data.getMachineConfiguration(),
            LocalDateTime.now(), LocalDateTime.now()
        );
    }
    
    public Favourite(
        String name, Machine machine, SoftwareConfiguration softwareConfiguration, MachineConfiguration machineConfiguration,
        LocalDateTime dateCreated, LocalDateTime dateModified
    ) { // favourite reloading
        this(
            name, machine, softwareConfiguration, machineConfiguration, dateCreated, dateModified,
            machine != null ? machine.getIconDescription().getIcon() : null
        );
    }
    
    protected Favourite(
        String name, Machine machine, SoftwareConfiguration softwareConfiguration, MachineConfiguration machineConfiguration,
        LocalDateTime dateCreated, LocalDateTime dateModified, Image image
    ) {
        setName(name);
        setMachine(machine);
        setSoftwareConfiguration(softwareConfiguration);
        setMachineConfiguration(machineConfiguration);
        
        shortcut = name.replace(" ", "").toLowerCase();
        invalidated = new SimpleBooleanProperty();
        this.dateCreated.set(dateCreated);
        this.dateModified.set(dateModified);
        
        nameProperty().addListener(l -> this.dateModified.set(LocalDateTime.now()));
        machineProperty().addListener(l -> {
            if (!disableDateModifiedUpdate)
                this.dateModified.set(LocalDateTime.now());
        });
        softwareConfigurationProperty().addListener(l -> {
            if (!disableDateModifiedUpdate)
                this.dateModified.set(LocalDateTime.now());
        });
        machineConfigurationProperty().addListener(l -> {
            if (!disableDateModifiedUpdate)
                this.dateModified.set(LocalDateTime.now());
        });
        
        setIcon(image);
        nameProperty().addListener((o, oV, newValue) -> {
            IconDescription icon = getIconName();
            if (icon == null || !icon.getDescription().equals(newValue))
                setIconName( new IconDescription(newValue, getMachine() != null ? getMachine().getIconDescription().getIcon() : null) );
            shortcut = newValue.replace(" ", "").toLowerCase();
        });
        iconNameProperty().addListener((o, oV, newValue) -> {
            if (newValue != null && !newValue.getDescription().equals(getName()))
                setName(newValue.getDescription());
        });
        
        ChangeListener<? super IconDescription> listener = (o, oV, newValue) -> setIcon(newValue.getIcon());
        if (machine != null)
            machine.iconDescriptionProperty().addListener(listener);
        machineProperty().addListener((o, oldMachine, newMachine) -> {
            if (oldMachine != null)
                oldMachine.iconDescriptionProperty().removeListener(listener);
            if (newMachine != null) {
                listener.changed(null, null, newMachine.getIconDescription());
                newMachine.iconDescriptionProperty().addListener(listener);
            }
        });
    }

    public final String getName() {
        return name.get();
    }

    public final void setName(String value) {
        name.set(value);
    }

    public final StringProperty nameProperty() {
        return name;
    }

    public final Machine getMachine() {
        return machine.get();
    }

    public final void setMachine(Machine value) {
        machine.set(value);
    }
    
    public void resetMachine() {
        machineEditable = true;
    }
    
    public void resetMachine(Machine value) {
        disableDateModifiedUpdate = true;
        setMachine(value);
        disableDateModifiedUpdate = false;
    }

    public final ObjectProperty<Machine> machineProperty() {
        return machine;
    }

    public boolean isMachineEditable() {
        return machineEditable;
    }
    
    public final SoftwareConfiguration getSoftwareConfiguration() {
        return softwareConfiguration.get();
    }

    public final void setSoftwareConfiguration(SoftwareConfiguration value) {
        softwareConfiguration.set(value);
    }
    
    public void resetSoftwareConfiguration(SoftwareConfiguration value) {
        disableDateModifiedUpdate = true;
        setSoftwareConfiguration(value);
        disableDateModifiedUpdate = false;
    }

    public final ObjectProperty<SoftwareConfiguration> softwareConfigurationProperty() {
        return softwareConfiguration;
    }

    public final MachineConfiguration getMachineConfiguration() {
        return machineConfiguration.get();
    }

    public final void setMachineConfiguration(MachineConfiguration value) {
        machineConfiguration.set(value);
    }
    
    public void resetMachineConfiguration(MachineConfiguration value) {
        disableDateModifiedUpdate = true;
        setMachineConfiguration(value);
        disableDateModifiedUpdate = false;
    }

    public final ObjectProperty<MachineConfiguration> machineConfigurationProperty() {
        return machineConfiguration;
    }
    
    public final LocalDateTime getDateCreated() {
        return dateCreated.get();
    }

    public final ReadOnlyObjectProperty<LocalDateTime> dateCreatedProperty() {
        return dateCreated;
    }

    public final LocalDateTime getDateModified() {
        return dateModified.get();
    }

    public final ReadOnlyObjectProperty<LocalDateTime> dateModifiedProperty() {
        return dateModified;
    }

    public final IconDescription getIconName() {
        return iconName.get();
    }

    public final void setIconName(IconDescription value) {
        iconName.set(value);
    }
    
    public final void setIcon(Image icon) {
        setIconName(new IconDescription(getName(), icon));
    }

    public final ObjectProperty<IconDescription> iconNameProperty() {
        return iconName;
    }
    
    public final String getShortcut() {
        return shortcut;
    }

    public boolean isInvalidated() {
        return invalidated.get();
    }

    public void setInvalidated(boolean invalidated) {
        this.invalidated.set(invalidated);
    }
    
    public ReadOnlyBooleanProperty invalidatedProperty() {
        return invalidated;
    }

    public boolean mustMigrate() {
        return mustMigrate.get();
    }

    public void setMustMigrate(boolean mustMigrate) {
        this.mustMigrate.set(mustMigrate);
    }
    
    public ReadOnlyBooleanProperty mustMigrateProperty() {
        return mustMigrate;
    }
    
    public void checkValidity() {
        setInvalidated(isMachineInvalid() || isSoftwareInvalid());
    }
    
    public boolean isMachineInvalid() {
        Machine m = getMachine();
        return m != null && m.getGroup().isEmpty();
    }
    
    public boolean isSoftwareInvalid() {
        SoftwareConfiguration s = getSoftwareConfiguration();
        return s != null && s.getSoftware() != null && s.getSoftware().getGroup().isEmpty();
    }
    
    public Favourite copy() {
        return new Favourite(this);
    }
    
    public void setChildren(ObservableList<TreeItem<Favourite>> children) {
        // by default, do nothing as favourites don't have children
        // however, favourite folders can have some (see Folder class)
    }
    
    @Override
    public void write(XMLStreamWriter writer) throws XMLStreamException {
        Machine m = getMachine();
        MachineConfiguration machineConf = getMachineConfiguration();
        SoftwareConfiguration softwareConf = getSoftwareConfiguration();
        
        if (m != null || machineConf != null || softwareConf != null)
            writer.writeStartElement("favourite");
        else
            writer.writeEmptyElement("favourite");
        writer.writeAttribute("name", getName());
        writer.writeAttribute("dateModified", getDateModified().toString());
        writer.writeAttribute("dateCreated", getDateCreated().toString());
        
        if (m != null) {
            if (machineConf != null && (machineConf.isCommandLine() || machineConf.getParameters().size() > 0))
                writer.writeStartElement("machineConfiguration");
            else
                writer.writeEmptyElement("machineConfiguration");
            writer.writeAttribute("name", m.getName());
            writer.writeAttribute("description", m.getDescription());
            
            if (machineConf != null) {
                writer.writeAttribute("configurable", Boolean.toString(machineConf.isConfigurable()));

                if (!machineConf.isCommandLine()) {
                    MachineElementList parameters = machineConf.getParameters();
                    if (parameters.size() > 0) {
                        writer.writeStartElement("parameters");
                        for (MachineElement<?> parameter : parameters)
                            parameter.write(writer);
                        writer.writeEndElement();
                    }
                } else {
                    writer.writeStartElement("commandLine");
                    writer.writeCharacters(machineConf.getCommandLine());
                    writer.writeEndElement();
                }

                if (machineConf.isCommandLine() || machineConf.getParameters().size() > 0)
                    writer.writeEndElement();
            }
        }
        
        if (softwareConf != null) {
            writer.writeEmptyElement("softwareConfiguration");
            Software software = softwareConf.getSoftware();
            writer.writeAttribute("name", software.getName());
            writer.writeAttribute("description", software.getDescription());
            writer.writeAttribute("list", software.getGroup());
            writer.writeAttribute("device", softwareConf.getDevice());
            if (softwareConf.getSoftwarePart() != null)
                writer.writeAttribute("part", softwareConf.getSoftwarePart().getName());
        }
        
        if (m != null || machineConf != null || softwareConf != null)
            writer.writeEndElement();
    }
}
