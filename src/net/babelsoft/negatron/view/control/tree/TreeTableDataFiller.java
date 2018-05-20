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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javafx.scene.control.TreeItem;
import net.babelsoft.negatron.model.Statistics;
import net.babelsoft.negatron.model.item.EmulatedItem;
import net.babelsoft.negatron.view.control.EmulatedItemTreeView;

/**
 *
 * @author capan
 */
public final class TreeTableDataFiller {
    
    private TreeTableDataFiller() { }
    
    public static <T extends EmulatedItem<T>> Statistics fill(
        EmulatedItemTreeView<T> tree, List<T> items, boolean mustFlatten
    ) {
        final Map<String, SortableTreeItem<T>> parentMap = new HashMap<>();
        final Map<String, SortableTreeItem<T>> itemMap = new HashMap<>();
        final Map<Character, Collection<TreeItem<T>>> shortcutMap = new HashMap<>();
        final SortableTreeItem<T> root = (SortableTreeItem<T>) tree.getRoot();
        
        final Comparator<? super TreeItem<T>> shortcutComparator = (item1, item2) ->
            item1.getValue().getName().compareTo( item2.getValue().getName() )
        ;
        
        final Statistics statistics = new Statistics();
        
        items.stream().filter(item -> item.isRunnable()).forEach(
            item -> {
                item.reset();

                // build tree
                String name = item.getName();
                SortableTreeItem<T> currentTreeItem = parentMap.get(name);
                if (currentTreeItem == null) {
                    currentTreeItem = new SortableTreeItem<>(item);
                    if (!item.hasParent()) {
                        parentMap.put(name, currentTreeItem);
                        root.getInternalChildren().add(currentTreeItem);
                        statistics.incrementParentCount();
                    }
                }

                if (item.hasParent()) {
                    String parentName = item.getParent().getName();
                    SortableTreeItem<T> parentTreeItem = parentMap.get(parentName);
                    
                    if (parentTreeItem == null) {
                        if (!mustFlatten) {
                            T parent = item.getParent();
                            if (!items.contains(parent))
                                parent.setNotCompatible();
                            parentTreeItem = new SortableTreeItem<>(parent);
                            parentMap.put(parentName, parentTreeItem);
                            root.getInternalChildren().add(parentTreeItem);
                        }
                        statistics.incrementParentCount();
                    } else
                        statistics.incrementCloneCount();
                    
                    if (!mustFlatten) {
                        parentTreeItem.getInternalChildren().add(currentTreeItem);
                        statistics.incrementCloneCount();
                    } else
                        root.getInternalChildren().add(currentTreeItem);
                }

                // update shortcut map
                Character index = item.getName().charAt(0);
                if (!shortcutMap.containsKey(index)) {
                    shortcutMap.put(index, new TreeSet<>(shortcutComparator));
                }
                shortcutMap.get(index).add(currentTreeItem);
                
                itemMap.put(item.getName(), currentTreeItem);
            }
        );
        tree.setShortcutMap(shortcutMap);
        tree.setMap(itemMap);
        
        return statistics;
    }
}
