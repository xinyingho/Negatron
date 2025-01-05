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
package net.babelsoft.negatron.controller;

import java.io.IOException;
import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import net.babelsoft.negatron.io.cache.UIConfigurationData;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.model.Support;
import net.babelsoft.negatron.model.item.Software;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Strings;
import net.babelsoft.negatron.view.control.EmulatedItemTreeView;

/**
 *
 * @author capan
 */
public class SoftwareFilterPaneController extends FilterPaneController<Software> {
    
    private String configurationId;
    
    @FXML
    private CheckBox supportSupported;
    @FXML
    private CheckBox supportPartial;
    @FXML
    private CheckBox supportUnsupported;

    @Override
    protected UIConfigurationData loadConfiguration() {
        return Configuration.Manager.getFilterConfiguration(configurationId);
    }

    @Override
    protected void saveConfiguration(UIConfigurationData data) throws IOException {
        if (Strings.isValid(configurationId))
            Configuration.Manager.updateFilterConfiguration(configurationId, data);
    }

    @Override
    public void setTreeView(EmulatedItemTreeView<Software> treeView) {
        setTreeView(treeView, new SoftwareFilter());
    }
    
    @Override
    protected void setAsSelectionDisable(boolean disable) {
        setAsSelectionDisable(disable, Language.Manager.getString("setAsSelection.software"));
    }
    
    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
        // reset filter form
        loadData();
    }
    
    @Override
    protected void update(
        Consumer<TextField> updateText, Consumer<RadioButton> updateRadio,
        Consumer<Spinner<Integer>> updateSpinner, Consumer<CheckBox> updateCheck
    ) {
        super.update(updateText, updateRadio, updateSpinner, updateCheck);
        updateCheck.accept(supportSupported);
        updateCheck.accept(supportPartial);
        updateCheck.accept(supportUnsupported);
    }
    
    protected class SoftwareFilter extends Filter<Software> {
        
        @Override
        public boolean test(Software software) {
            Support support = software.getSupport();
            return super.test(software) && (
                (support == Support.YES) && supportSupported.isSelected() ||
                (support == Support.PARTIAL) && supportPartial.isSelected() ||
                (support == Support.NO) && supportUnsupported.isSelected()
            );
        }
    }

    @FXML
    protected void handleOnSetSupportAsSelection(ActionEvent event) {
        supportSupported.setSelected(false);
        supportPartial.setSelected(false);
        supportUnsupported.setSelected(false);
        
        switch (currentItem.getSupport()) {
            case Support.YES -> supportSupported.setSelected(true);
            case PARTIAL -> supportPartial.setSelected(true);
            default -> supportUnsupported.setSelected(true); //case NO
        }
        
        filterTimeline.playFromStart();
        supportSupported.requestFocus();
    }
}
