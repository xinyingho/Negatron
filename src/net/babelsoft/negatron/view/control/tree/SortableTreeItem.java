/*******************************************************************************
 * Copyright (c) 2014 EM-SOFTWARE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christoph Keimel <c.keimel@emsw.de> - initial API and implementation
 *******************************************************************************/
package net.babelsoft.negatron.view.control.tree; //e(fx)clipse 2.0 / modified to work with TreeTableView

import java.util.Comparator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TreeItem;

/**
 * An extension of {@link TreeItem} with the possibility to sort its children. To enable sorting 
 * it is necessary to set the {@link Comparator}. If no comparator is set, then
 * the tree item will attempt to bind itself to the comparator of its parent.
 *
 * @param <T> The type of the {@link #getValue() value} property within {@link TreeItem}.
 */
public class SortableTreeItem<T> extends FilterableTreeItem<T> {
    final private SortedList<TreeItem<T>> sortedList;

    private ObjectProperty<Comparator<TreeItem<T>>> comparator = new SimpleObjectProperty<>();

    public SortableTreeItem() {
        this(null);
    }

    /**
     * Creates a new {@link TreeItem} with sorted children. To enable sorting it is 
     * necessary to set the {@link TreeItemComparator}. If no comparator is set, then
     * the tree item will attempt so bind itself to the comparator of its parent.
     * 
     * @param value the value of the {@link TreeItem}
     */
    public SortableTreeItem(T value) {
        super(value);
        this.sortedList = new SortedList<>(super.getChildren());
        this.sortedList.comparatorProperty().bind(this.comparator);
        parentProperty().addListener((o, oV, nV) -> {
            if (nV != null && nV instanceof SortableTreeItem && this.comparator.get() == null) {
                this.comparator.bind(((SortableTreeItem<T>) nV).comparatorProperty());
            }
        });
        setHiddenFieldChildren(this.sortedList);
    }

    /**
     * @return the comparator property
     */
    public final ObjectProperty<Comparator<TreeItem<T>>> comparatorProperty() {
        return this.comparator;
    }

	/**
	 * @return the comparator
	 */
    public final Comparator<TreeItem<T>> getComparator() {
        return this.comparator.get();
    }

    /**
     * Set the comparator
     * @param comparator the comparator
     */
    public final void setComparator(Comparator<TreeItem<T>> comparator) {
    	this.comparator.set(comparator);
    }
}