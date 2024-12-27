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
package net.babelsoft.negatron.view.control.tree; //e(fx)clipse 2.0

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;
import net.babelsoft.negatron.util.ReflectionUtil;

/**
 * An extension of {@link TreeItem} with the possibility to filter its children. To enable filtering 
 * it is necessary to set the {@link TreeItemPredicate}. If a predicate is set, then the tree item
 * will also use this predicate to filter its children (if they are of the type FilterableTreeItem).
 * 
 * A tree item that has children will not be filtered. The predicate will only be evaluated, if the
 * tree item is a leaf. Since the predicate is also set for the child tree items, the tree item in question
 * can turn into a leaf if all its children are filtered.
 *
 * @param <T> The type of the {@link #getValue() value} property within {@link TreeItem}.
 */
public class FilterableTreeItem<T> extends TreeItem<T> {
    final private ObservableList<TreeItem<T>> sourceList;
    final private FilteredList<TreeItem<T>> filteredList;

    private ObjectProperty<TreeItemPredicate<T>> predicate = new SimpleObjectProperty<>();

    public FilterableTreeItem() {
        this(null);
    }

    /**
     * Creates a new {@link TreeItem} with sorted children. To enable sorting it is 
     * necessary to set the {@link TreeItemComparator}. If no comparator is set, then
     * the tree item will attempt so bind itself to the comparator of its parent.
     * 
     * @param value the value of the {@link TreeItem}
     */
    public FilterableTreeItem(T value) {
        super(value);
        this.sourceList = FXCollections.observableArrayList();
        this.filteredList = new FilteredList<>(this.sourceList);
        this.filteredList.predicateProperty().bind(Bindings.createObjectBinding(() -> {
            return child -> {
                // Set the predicate of child items to force filtering
                if (child instanceof FilterableTreeItem) {
                    FilterableTreeItem<T> filterableChild = (FilterableTreeItem<T>) child;
                    filterableChild.setPredicate(this.predicate.get());
                }
                // If there is no predicate, keep this tree item
                if (this.predicate.get() == null)
                    return true;
                // If there are children, keep this tree item
                if (!child.getChildren().isEmpty())
                    return true;
                // Otherwise ask the TreeItemPredicate
                return this.predicate.get().test(this, child.getValue());
            };
        }, this.predicate));

        setHiddenFieldChildren(this.filteredList);
    }

    /**
     * Set the hidden private field {@link TreeItem#children} through reflection and hook the hidden
     * {@link ListChangeListener} in {@link TreeItem#childrenListener} to the list
     * @param list the list to set
     */
    @SuppressWarnings({ "unchecked", "javadoc" })
    protected void setHiddenFieldChildren(ObservableList<TreeItem<T>> list) {
        ReflectionUtil.setFieldValue(this, "children", list); //$NON-NLS-1$
        ListChangeListener<? super TreeItem<T>> childrenListener =
            (ListChangeListener<? super TreeItem<T>>) ReflectionUtil.getFieldValue(this, "childrenListener"); //$NON-NLS-1$
        filteredList.removeListener(childrenListener); // prevent the childrenListener to be registered on the sortedList and the filteredList at the same time, which would trigger a lot of bugs
        list.addListener(childrenListener);
    }

    /**
     * Returns the list of children that is backing the filtered list.
     * @return underlying list of children
     */
    public ObservableList<TreeItem<T>> getInternalChildren() {
        return this.sourceList;
    }

    /**
     * @return the predicate property
     */
    public final ObjectProperty<TreeItemPredicate<T>> predicateProperty() {
        return this.predicate;
    }

	/**
	 * @return the predicate
	 */
    public final TreeItemPredicate<T> getPredicate() {
        return this.predicate.get();
    }

    /**
     * Set the predicate
     * @param predicate the predicate
     */
    public final void setPredicate(TreeItemPredicate<T> predicate) {
    	this.predicate.set(predicate);
    }
}