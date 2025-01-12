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
package net.babelsoft.negatron.view.control.form;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.Domain;
import net.babelsoft.negatron.util.Strings;

/**
 *
 * @author capan
 */
public class MasterConfigPathField extends SinglePathField {
    
    private List<TextField> dependentSingleFields;
    private List<MultiPathField> dependentMultiFields;
    private Domain domain;
    
    public MasterConfigPathField(GridPane grid, int row, Domain domain, String styleClass, String label, String text, String prompt) {
        super(grid, row, styleClass, label, prompt);
        
        this.domain = domain;
        pathField.setText(text);
        
        pathField.focusedProperty().addListener((o, oV, newValue) -> {
            if (newValue) {
                // begin a global transaction (manual input triggering a lot of text update events)
                Configuration.Manager.beginMasterConfigTransaction();
                setDependentPathFields(pathField.getText(), "");
            } else {
                if (Strings.isEmpty(pathField.getText())) dependentSingleFields.forEach(
                    field -> ((MultiPathField) field.getUserData()).remove(field)
                );
                // end the current global transaction
                resetDependentPathFields();
                commitMasterConfigTransaction(this.domain);
            }
        });
        
        pathField.textProperty().addListener((o, oldValue, newValue) -> {
            if (dependentMultiFields == null) {
                // begin a local transaction (automatic input after folder browsing, triggering a single update event)
                Configuration.Manager.setMasterConfigPoint();
                setDependentPathFields(oldValue, newValue);
            }
            
            dependentMultiFields.forEach(
                multiField -> dependentSingleFields.addAll(multiField.addDefaultPaths(oldValue))
            );
            if (!dependentMultiFields.isEmpty())
                dependentMultiFields.clear();
            dependentSingleFields.forEach(
                field -> field.setText(field.getText().replaceFirst(
                    Matcher.quoteReplacement( Strings.orElseBlank(oldValue) ),
                    Matcher.quoteReplacement(newValue)
                ))
            );
            
            if (updateMasterConfigPath(this.domain, newValue))
                // end the local transaction
                resetDependentPathFields();
        });
        
        browseButton.setOnAction(event -> {
            DirectoryChooser dc = new DirectoryChooser();
            File f;
            try {
                dc.setInitialDirectory(new File(pathField.getText()));
                f = dc.showDialog(browseButton.getScene().getWindow());
            } catch (Exception ex) {
                dc.setInitialDirectory(new File("."));
                f = dc.showDialog(browseButton.getScene().getWindow());
            }
            if (f != null)
                pathField.setText(f.getAbsolutePath());
        });
    }
    
    private void resetDependentPathFields() {
        dependentMultiFields = null;
        dependentSingleFields = null;
    }
    
    private void setDependentPathFields(String reference, String newReference) {
        dependentMultiFields = new ArrayList<>();
        
        if (Strings.isEmpty(reference)) {
            // initialising
            dependentSingleFields = new ArrayList<>();
            MultiPathField.getList().forEach(multiField -> {
                List<TextField> fields = multiField.getPathFields();
                if ((
                    domain == Domain.MULTIMEDIA_MACHINE_SOFTWARE && multiField.isMultimediaPath() ||
                    domain == Domain.EXTRAS_MACHINE_SOFTWARE && !multiField.isMultimediaPath()
                ) && (
                    Strings.isEmpty(newReference) || !multiField.isMamePath() ||
                    fields.stream().allMatch(field -> !field.getText().startsWith(newReference))
                )) dependentSingleFields.addAll(multiField.addDefaultPaths(reference));
            });
        } else dependentSingleFields = MultiPathField.getList().stream().flatMap(
            multiField -> {
                List<TextField> fields = multiField.getPathFields();
                if (
                    domain == Domain.EXTRAS_MACHINE_SOFTWARE && multiField.isExtrasPath() &&
                    fields.stream().allMatch(field -> Strings.isEmpty(field.getText())) ||
                    domain == Domain.EXTRAS_MACHINE_SOFTWARE && multiField.isMamePath() &&
                    fields.stream().allMatch(field -> !field.getText().startsWith(reference)) ||
                    domain == Domain.MULTIMEDIA_MACHINE_SOFTWARE && multiField.isMultimediaPath() &&
                    fields.stream().allMatch(field -> Strings.isEmpty(field.getText()))
                ) {
                    dependentMultiFields.add(multiField);
                    return null;
                } else
                    return fields.stream();
            }
        ).filter(
            field -> field.getText().startsWith(reference)
        ).collect(Collectors.toList());
    }
}
