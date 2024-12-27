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
package net.babelsoft.negatron.view.control.tree;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;
import net.babelsoft.negatron.controller.FavouriteTreePaneController;
import net.babelsoft.negatron.model.IconDescription;
import net.babelsoft.negatron.model.favourites.Favourite;
import net.babelsoft.negatron.model.favourites.Separator;
import net.babelsoft.negatron.view.control.adapter.IconDescriptionStringConverter;

/**
 * A class containing a {@link TableCell} implementation that draws a
 * {@link TextField} node inside the cell.
 *
 * <p>By default, the IconNameTreeTableCell is rendered as a {@link Label}
 * with an {@link Image} icon on its left side when not being edited, and as a
 * TextField when in editing mode. The TextField will, by default, stretch to
 * fill the entire table cell.
 *
 * @author capan
 */
public class IconNameTreeTableCell extends FavouriteTreeTableCell<IconDescription> {
    
    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private TextField textField;
    private ImageView imageView;



    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a default IconNameTreeTableCell with a default converter.
     * 
     * @param controller The controller that coordinates row-level updates.
     */
    public IconNameTreeTableCell(FavouriteTreePaneController controller) {
        this(controller, new IconDescriptionStringConverter());
    }

    /**
     * Creates a IconDescriptionTreeTableCell that provides a {@link TextField} when put
     * into editing mode that allows editing of the cell content. This method
     * will work on any TreeTableColumn instance, regardless of its generic type.
     * However, to enable this, a {@link StringConverter} must be provided that
     * will convert the given String (from what the user typed in) into an
     * instance of type T. This item will then be passed along to the
     * {@link TreeTableColumn#onEditCommitProperty()} callback.
     *
     * @param controller The controller that coordinates row-level updates.
     * @param converter A {@link StringConverter converter} that can convert
     *      the given String (from what the user typed in) into an instance of
     *      type T.
     */
    public IconNameTreeTableCell(FavouriteTreePaneController controller, StringConverter<IconDescription> converter) {
        super(controller);
        
        getStyleClass().add("text-field-tree-table-cell");
        setConverter(converter);
        itemProperty().addListener((o, oV, newItem) -> {
            if (newItem != null)
                imageView = new ImageView(newItem.getIcon());
            else
                imageView = null;
        });
    }



    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // --- converter
    private final ObjectProperty<StringConverter<IconDescription>> converter = new SimpleObjectProperty<>(this, "converter");

    /**
     * The {@link StringConverter} property.
     */
    public final ObjectProperty<StringConverter<IconDescription>> converterProperty() {
        return converter;
    }

    /**
     * Sets the {@link StringConverter} to be used in this cell.
     */
    public final void setConverter(StringConverter<IconDescription> value) {
        converterProperty().set(value);
    }

    /**
     * Returns the {@link StringConverter} used in this cell.
     */
    public final StringConverter<IconDescription> getConverter() {
        return converterProperty().get();
    }



    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public static boolean canEdit(Favourite item) {
        return !(item instanceof Separator);
    }
    
    /** {@inheritDoc} */
    @Override
    public void startEdit() {
        super.startEdit();

        if (isEditing()) {
            if (textField == null) {
                textField = CellUtils.createTextField(this, getConverter());
            }
            
            CellUtils.startEdit(this, getConverter(), null, null, textField);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        CellUtils.cancelEdit(this, getConverter(), imageView);
    }

    /** {@inheritDoc} */
    @Override
    protected void updateItem(IconDescription item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty || item == null) {
            CellUtils.updateItem(this, getConverter(), null, getSeparator(), textField);
        } else
            CellUtils.updateItem(this, getConverter(), null, !isEditing() ? imageView : null, textField);
    }
}
