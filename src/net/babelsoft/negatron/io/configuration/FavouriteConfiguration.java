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
package net.babelsoft.negatron.io.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TreeItem;
import javanet.staxutils.IndentingXMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import net.babelsoft.negatron.model.favourites.Favourite;

/**
 *
 * @author capan
 */
public class FavouriteConfiguration {
    
    public static final Path FAV_PATH = Paths.get("Favourites.xml");
    
    private XMLStreamWriter writer;
    
    public void save(TreeItem<Favourite> root) {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try {
            writer = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(
                Files.newBufferedWriter(FAV_PATH)
            ));
            
            writer.writeStartDocument();
            root.getValue().write(writer, true);
            writer.writeEndDocument();
        } catch (XMLStreamException | IOException ex) {
            Logger.getLogger(FavouriteConfiguration.class.getName()).log(Level.SEVERE, "An error occured while saving favourites", ex);
        } finally {
            if (writer != null) try {
                writer.close();
            } catch (XMLStreamException ex) {
                Logger.getLogger(FavouriteConfiguration.class.getName()).log(Level.SEVERE, "An error occured while saving favourites", ex);
            }
        }
    }
}
