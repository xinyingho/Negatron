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
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import net.babelsoft.negatron.io.XmlOutput;

/**
 *
 * @author capan
 */
public class Folder extends Favourite {
    
    private ObservableList<TreeItem<Favourite>> children;
    
    protected Folder(Folder ref) {
        this(ref.getName(), ref.getIconName() != null ? ref.getIconName().getIcon() : null);
    }
    
    public Folder(String name, Image image) {
        super(name, image, false);
    }
    
    public Folder(long id, String name, LocalDateTime dateCreated, LocalDateTime dateModified) {
        super(id, name, null, null, null, dateCreated, dateModified);
    }
    
    private boolean hasChildren() {
        return children != null && children.size() > 0;
    }
    
    private List<XmlOutput> getChildren() {
        return children.stream().map(item -> (XmlOutput) item.getValue()
        ).collect(
            Collectors.toList()
        );
    }
    
    @Override
    public void setChildren(ObservableList<TreeItem<Favourite>> children) {
        this.children = children;
    }
    
    @Override
    public Favourite copy() {
        return new Folder(this);
    }
    
    @Override
    public void write(XMLStreamWriter writer, boolean isRoot) throws XMLStreamException {
        String tag = isRoot ? "root" : "folder";
        if (hasChildren())
            writer.writeStartElement(tag);
        else
            writer.writeEmptyElement(tag);
        if (isRoot) {
            writer.writeDefaultNamespace("http://www.babelsoft.net/products/negatron/favourites");
            writer.writeAttribute("version", "1.2");
            writer.writeAttribute("dateTreeModified", LocalDateTime.now().toString());
        }
        writer.writeAttribute("id", String.valueOf(getId()));
        writer.writeAttribute("name", getName());
        writer.writeAttribute("dateModified", getDateModified().toString());
        writer.writeAttribute("dateCreated", getDateCreated().toString());
        
        if (hasChildren()) {
            writer.writeStartElement("children");
            for (XmlOutput child : getChildren())
                child.write(writer);
            writer.writeEndElement();
            
            writer.writeEndElement();
        }
    }
    
    @Override
    public void write(XMLStreamWriter writer) throws XMLStreamException {
        write(writer, false);
    }
}
