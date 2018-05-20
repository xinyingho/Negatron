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
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import net.babelsoft.negatron.io.cache.UIConfigurationData;
import net.babelsoft.negatron.model.Status;
import net.babelsoft.negatron.model.item.EmulatedItem;
import net.babelsoft.negatron.util.Strings;
import net.babelsoft.negatron.view.control.EmulatedItemTreeView;
import net.babelsoft.negatron.view.control.TitledWindowPane;
import net.babelsoft.negatron.view.control.YearSpinner;
import net.babelsoft.negatron.view.control.tree.SortableTreeItem;
import net.babelsoft.negatron.view.control.tree.TreeItemPredicate;

/**
 *
 * @author capan
 */
public abstract class FilterPaneController<T extends EmulatedItem<T>> implements Initializable {
    
    private static final double IN_FOCUS_OPACITY = 1.0;
    private static final double OUT_FOCUS_OPACITY = .5;
    private static final Duration ANIM_DURATION = Duration.millis(200);
    private static final Duration FILTER_DURATION = Duration.seconds(1);
    
    private Timeline inTimeline;
    private Timeline outTimeline;
    protected Timeline filterTimeline;
    
    private boolean canUpdateConfiguration;
    protected T currentItem;
    private Consumer<Boolean> onFilter;
    
    @FXML
    private TitledWindowPane root;
    @FXML
    private GridPane grid;
    
    @FXML
    private TextField description;
    @FXML
    private TextField name;
    @FXML
    private TextField company;
    @FXML
    private TextField group;
    @FXML
    private RadioButton yearFixed;
    @FXML
    private ToggleGroup yearGroup;
    @FXML
    private RadioButton yearRange;
    @FXML
    private FlowPane yearPane;
    @FXML
    private HBox yearRangePane;
    @FXML
    private TextField year;
    @FXML
    private YearSpinner yearFrom;
    @FXML
    private YearSpinner yearTo;
    @FXML
    private RadioButton versionAll;
    @FXML
    private ToggleGroup versionGroup;
    @FXML
    private RadioButton versionParents;
    @FXML
    private RadioButton versionClones;
    @FXML
    private CheckBox statusGood;
    @FXML
    private CheckBox statusBad;
    @FXML
    private CheckBox statusMissing;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inTimeline = new Timeline();
        inTimeline.getKeyFrames().add(new KeyFrame(ANIM_DURATION, new KeyValue(root.opacityProperty(), IN_FOCUS_OPACITY)));
        outTimeline = new Timeline();
        outTimeline.getKeyFrames().add(new KeyFrame(ANIM_DURATION, new KeyValue(root.opacityProperty(), OUT_FOCUS_OPACITY)));
        
        root.setOpacity(OUT_FOCUS_OPACITY);
        root.setOnMouseEntered(evt -> {
            outTimeline.stop();
            inTimeline.playFromStart();
        });
        root.setOnMouseExited(evt -> {
            inTimeline.stop();
            outTimeline.playFromStart();
        });
        
        FilterPaneController.this.setAsSelectionDisable(true);
        
        initialise();
        loadData();
        wireEvents();
    }
    
    protected void initialise() {
        yearPane.getChildren().removeAll(year, yearRangePane);
        yearGroup.selectedToggleProperty().addListener((o, oV, newValue) -> {
            if (newValue == yearFixed) {
                yearPane.getChildren().add(year);
                yearPane.getChildren().remove(yearRangePane);
            } else {
                yearPane.getChildren().remove(year);
                yearPane.getChildren().add(yearRangePane);
            }
        });
    }
    
    protected final void loadData() {
        canUpdateConfiguration = false; // temporarily set it to false to avoid having the cache being updated during below initialisation
        
        final UIConfigurationData data = loadConfiguration();
        update(
            field -> field.setText(data.getString(field.getId())),
            radio -> {
                Boolean val = data.getBoolean(radio.getId());
                if (val != null)
                    radio.setSelected(val);
                else
                    radio.getToggleGroup().selectToggle(radio.getToggleGroup().getToggles().get(0));
            },
            spinner -> spinner.getEditor().setText(data.getString(spinner.getId())),
            check -> {
                Boolean val = data.getBoolean(check.getId());
                if (val != null)
                    check.setSelected(val);
                else
                    check.setSelected(true);
            }
        );
        
        canUpdateConfiguration = true; // initialisation finished, so just set it back to true
    }
    
    protected abstract UIConfigurationData loadConfiguration();
    
    protected void wireEvents() {
        description.textProperty().addListener(o -> handleOnAction(null));
        name.textProperty().addListener(o -> handleOnAction(null));
        company.textProperty().addListener(o -> handleOnAction(null));
        group.textProperty().addListener(o -> handleOnAction(null));
        yearFrom.getEditor().textProperty().addListener(o -> handleOnAction(null));
        yearTo.getEditor().textProperty().addListener(o -> handleOnAction(null));
        year.textProperty().addListener((o, oldValue, newValue) -> {
            if (newValue == null || newValue.matches("(\\d|\\?)*"))
                handleOnAction(null);
            else
                year.setText(oldValue);
        });
    }

    public void setTreeView(EmulatedItemTreeView<T> treeView) {
        setTreeView(treeView, new Filter<>());
    }
    
    protected final void setTreeView(EmulatedItemTreeView<T> treeView, Filter<T> filter) {
        filterTimeline = new Timeline(new KeyFrame(FILTER_DURATION, event -> {
            if (canUpdateConfiguration) try {
                saveConfiguration(toUIConfiguration());
            } catch (IOException ex) {
                Logger.getLogger(MachineFilterPaneController.class.getName()).log(Level.SEVERE, "Machine filter layout configuration failed", ex);
            }
            onFilter.accept(isDefaults());
            
            treeView.beginTreeWiseOperation();
            
            SortableTreeItem<T> tree = (SortableTreeItem<T>) treeView.getRoot();
            tree.setPredicate(TreeItemPredicate.create(filter));
            
            treeView.endTreeWiseOperation();
        }));
        treeView.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) -> {
            if (newValue != null) {
                currentItem = newValue.getValue();
                if (oldValue == null)
                    setAsSelectionDisable(false);
            } else if (oldValue != null)
                setAsSelectionDisable(true);
        });
    }

    protected abstract void setAsSelectionDisable(boolean disable);
    
    protected final void setAsSelectionDisable(boolean disable, final String ref) {
        Function<Node, Boolean> setDisable = node -> {
            boolean canDisable = node instanceof Button && ((Button) node).getText().equals(ref);
            if (canDisable)
                node.setDisable(disable);
            return canDisable;
        };
        
        grid.getChildren().forEach(node -> {
            Integer index = GridPane.getColumnIndex(node);
            if (index != null && index == 2)
                if (!setDisable.apply(node))
                    ((Pane) node).getChildren().forEach(
                        child -> setDisable.apply(child)
                    );
        });
    }
    
    public void setOnFilter(Consumer<Boolean> onFilter) {
        this.onFilter = onFilter;
    }
    
    protected abstract void saveConfiguration(UIConfigurationData data) throws IOException;
    
    public void bind(Property<String> filterProperty) {
        filterProperty.bindBidirectional(description.textProperty());
        canUpdateConfiguration = false;
        filterTimeline.getKeyFrames().get(0).getOnFinished().handle(null);
        canUpdateConfiguration = true;
    }

    public void disableStatusCriteria(boolean disable) {
        statusGood.setDisable(disable);
        statusBad.setDisable(disable);
        statusMissing.setDisable(disable);
    }
    
    private void restoreDefaults(Parent parent) {
        parent.getChildrenUnmodifiable().forEach(child -> {
            if (child instanceof TextField)
                ((TextField) child).setText("");
            else if (child instanceof CheckBox)
                ((CheckBox) child).setSelected(true);
            else if (child instanceof Pane)
                restoreDefaults((Pane) child);
        });
    }
    
    private boolean isDefaults() {
        return isDefaults(description.getParent()) && (
            yearFixed.isSelected() && Strings.isEmpty(year.getText()) ||
            yearRange.isSelected() && yearFrom.getEditor().getText().equals(
                Integer.toString( ((IntegerSpinnerValueFactory) yearFrom.getValueFactory()).getMin() )
            ) && yearTo.getEditor().getText().equals(
                Integer.toString( ((IntegerSpinnerValueFactory) yearFrom.getValueFactory()).getMax() )
            )
        ) && versionAll.isSelected();
    }
    
    private boolean isDefaults(Parent parent) {
        return parent.getChildrenUnmodifiable().stream().allMatch(child -> {
            if (child instanceof TextField)
                if (((TextField) child).getId().equals("description"))
                    return true;
                else
                    return Strings.isEmpty( ((TextField) child).getText() );
            else if (child instanceof CheckBox)
                return ((CheckBox) child).isSelected();
            else if (child instanceof Pane)
                return isDefaults((Pane) child);
            else
                return true;
        });
    }

    private UIConfigurationData toUIConfiguration() {
        UIConfigurationData data = new UIConfigurationData();
        update(
            field -> data.put(field.getId(), field.getText()),
            radio -> data.put(radio.getId(), radio.isSelected()),
            spinner -> data.put(spinner.getId(), spinner.getEditor().getText()),
            check -> data.put(check.getId(), check.isSelected())
        );
        return data;
    }
    
    protected void update(
        Consumer<TextField> updateText, Consumer<RadioButton> updateRadio,
        Consumer<Spinner> updateSpinner, Consumer<CheckBox> updateCheck
    ) {
        // outline
        updateText.accept(description);
        updateText.accept(name);
        updateText.accept(company);
        updateText.accept(group);
        updateRadio.accept(yearFixed);
        updateRadio.accept(yearRange);
        updateText.accept(year);
        updateSpinner.accept(yearFrom);
        updateSpinner.accept(yearTo);
        updateRadio.accept(versionAll);
        updateRadio.accept(versionParents);
        updateRadio.accept(versionClones);
        // emulation
        updateCheck.accept(statusGood);
        updateCheck.accept(statusBad);
        updateCheck.accept(statusMissing);
    }
    
    protected class Filter<T extends EmulatedItem<T>> implements Predicate<T> {

        @Override
        public boolean test(T item) {
            BiFunction<TextField, String, Boolean> validateText = (field, val) -> {
                String filter = field.getText();
                if (Strings.isValid(filter)) {
                    filter = filter.replace(" ", "").toLowerCase();
                    return val.contains(filter);
                } else
                    return true;
            };

            boolean yearValidated = false;
            if (yearFixed.isSelected()) {
                String mask = year.getText();
                if (Strings.isValid(mask) && mask.length() == 4) {
                    String val = item.getYear();
                    if (val.length() > 4)
                        val = val.substring(0, 4); // for values like "2002?", transform them to "2002"
                    mask = mask.replace("?", ".");
                    yearValidated = val.matches(mask);
                } else if (Strings.isEmpty(mask))
                    yearValidated = true;
            } else { // yearRange.isSelected()
                if (Strings.isValid(yearFrom.getEditor().getText()) && Strings.isValid(yearTo.getEditor().getText())) {
                    int valFrom = Integer.valueOf(item.getYear().replace("?", "9"));
                    int valTo = Integer.valueOf(item.getYear().replace("?", "0"));
                    int refFrom = Integer.valueOf(yearFrom.getEditor().getText());
                    int refTo = Integer.valueOf(yearTo.getEditor().getText());
                    yearValidated = refFrom <= valFrom && valTo <= refTo;
                } else
                    yearValidated = true;
            }

            return
                // outline
                validateText.apply(description, item.getShortcut()) &&
                validateText.apply(name, item.getName()) &&
                validateText.apply(company, item.getCompany().replace(" ", "").toLowerCase()) &&
                validateText.apply(group, item.getGroup()) &&
                yearValidated &&
                (versionAll.isSelected() || !item.hasParent() && versionParents.isSelected() || item.hasParent() && versionClones.isSelected()) &&
                // emulation
                (item.getStatus() == Status.UNKNOWN && statusMissing.isSelected() || item.getStatus() == Status.GOOD && statusGood.isSelected() || item.getStatus() == Status.BAD && statusBad.isSelected())
            ;
        }
    }

    @FXML
    protected void handleOnAction(ActionEvent event) {
        filterTimeline.playFromStart();
    }

    @FXML
    protected void handleOnSetCompanyAsSelection(ActionEvent event) {
        company.setText(currentItem.getCompany());
        company.requestFocus();
    }

    @FXML
    protected void handleOnSetGroupAsSelection(ActionEvent event) {
        group.setText(currentItem.getGroup());
        group.requestFocus();
    }

    @FXML
    protected void handleOnSetYearAsSelection(ActionEvent event) {
        yearFixed.setSelected(true);
        year.setText(currentItem.getYear());
        year.requestFocus();
    }

    @FXML
    protected void handleOnSetVersionAsSelection(ActionEvent event) {
        if (currentItem.hasParent())
            versionClones.setSelected(true);
        else
            versionParents.setSelected(true);
        filterTimeline.playFromStart();
        versionAll.requestFocus();
    }

    @FXML
    protected void handleOnSetStatusAsSelection(ActionEvent event) {
        switch (currentItem.getStatus()) {
            case GOOD:
                statusGood.setSelected(true);
                statusBad.setSelected(false);
                statusMissing.setSelected(false);
                break;
            case BAD:
                statusGood.setSelected(false);
                statusBad.setSelected(true);
                statusMissing.setSelected(false);
            case UNKNOWN:
            default:
                statusGood.setSelected(false);
                statusBad.setSelected(false);
                statusMissing.setSelected(true);
                break;
        }
        filterTimeline.playFromStart();
        statusGood.requestFocus();
    }
    
    @FXML
    protected void handleOnRestoreDefaults(ActionEvent event) {
        restoreDefaults(description.getParent());
        year.setText("");
        yearFrom.getEditor().setText(
            Integer.toString( ((IntegerSpinnerValueFactory) yearFrom.getValueFactory()).getMin() )
        );
        yearTo.getEditor().setText(
            Integer.toString( ((IntegerSpinnerValueFactory) yearFrom.getValueFactory()).getMax() )
        );
        versionAll.setSelected(true);
    }
}
