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
package net.babelsoft.negatron.view.control.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumnBase;
import net.babelsoft.negatron.io.configuration.TableColumnBaseConfiguration;

/**
 *
 * @author capan
 */
public class TableColumnsAdapter<C extends TableColumnBase, CC extends TableColumnBaseConfiguration> {
    
    private final ObservableList<C> columns;
    private final ObservableList<C> sortedColumns;
    private final BiConsumer<C, CC> setColumnSortType;
    private final BiConsumer<CC, C> setConfigurationSortType;
    private final Class<CC> configurationType;
    private final Consumer<Map<String, CC>> updateConfiguration;
    
    public TableColumnsAdapter(
        ObservableList<C> columns, ObservableList<C> sortedColumns,
        BiConsumer<C, CC> setColumnSortType,
        BiConsumer<CC, C> setConfigurationSortType,
        Class<CC> configurationType,
        Consumer<Map<String, CC>> updateConfiguration
    ) {
        this.columns = columns;
        this.sortedColumns = sortedColumns;
        this.setColumnSortType = setColumnSortType;
        this.setConfigurationSortType = setConfigurationSortType;
        this.configurationType = configurationType;
        this.updateConfiguration = updateConfiguration;
    }
    
    public void loadLayout(Map<String, CC> confs) {
        if (confs.size() == columns.size()) {
            // re-order columns
            columns.sort((col1, col2) -> {
                CC conf1 = confs.get(col1.getId());
                CC conf2 = confs.get(col2.getId());
                
                if (conf1 != null && conf2 != null)
                    return Integer.compare(conf1.getOrder(), conf2.getOrder());
                else
                    return 0; // can't decide how to order the current 2 columns
            });
            
            // sort columns
            sortedColumns.clear();
            columns.forEach(col -> {
                CC conf = confs.get(col.getId());
                if (conf != null) {
                    if (conf.isSorted()) {
                        sortedColumns.add(col);
                        setColumnSortType.accept(col, conf);
                    }
                    col.setVisible(conf.isVisible());
                    col.setPrefWidth(conf.getWidth());
                }
            });
            
            // re-order sorted columns
            sortedColumns.sort((col1, col2) -> {
                CC conf1 = confs.get(col1.getId());
                CC conf2 = confs.get(col2.getId());
                return Integer.compare(conf1.getSortRank(), conf2.getSortRank());
            });
        }
    }
    
    public void saveColumnsLayout(String id) {
        final Map<String, CC> confMap = new HashMap<>();
        for (int i = 0;i < columns.size();++i) try {
            C col = columns.get(i);
            CC conf = configurationType.newInstance();

            conf.setName(col.getId());
            conf.setOrder(i);
            conf.setSorted(sortedColumns.contains(col));
            conf.setSortRank(sortedColumns.indexOf(col));
            setConfigurationSortType.accept(conf, col);
            conf.setVisible(col.isVisible());
            conf.setWidth(col.getWidth());

            confMap.put(col.getId(), conf);
        } catch (InstantiationException | IllegalAccessException ex) {
            // should never happen
        }
        updateConfiguration.accept(confMap);
    }
}
