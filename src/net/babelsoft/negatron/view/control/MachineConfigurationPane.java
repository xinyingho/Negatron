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
package net.babelsoft.negatron.view.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import net.babelsoft.negatron.controller.DeviceController;
import net.babelsoft.negatron.controller.MachineComponentController;
import net.babelsoft.negatron.model.component.Device;
import net.babelsoft.negatron.model.component.MachineComponent;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.model.item.Software.Requirement;
import net.babelsoft.negatron.model.item.SoftwareList;
import net.babelsoft.negatron.theme.Components;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Editable;
import net.babelsoft.negatron.util.function.TriConsumer;
import net.babelsoft.negatron.view.control.adapter.FocusData;
import net.babelsoft.negatron.view.control.form.Control;

/**
 *
 * @author capan
 */
public class MachineConfigurationPane extends GridPane implements Editable {
    
    private static final Image NEW_BADGE = new Image(Components.class.getResourceAsStream(
        "/net/babelsoft/negatron/resource/icon/device/new.png"
    ));
    private static final Image MODIFIED_BADGE = new Image(Components.class.getResourceAsStream(
        "/net/babelsoft/negatron/resource/icon/device/mod.png"
    ));
    
    private Machine currentMachine;
    private final List<MachineComponentController<?, ?, ?>> controllers;
    private final List<Label> badgedLabels;
    private FocusData focusData;
    private boolean mustDoAnimation;
    private Consumer<String> onDataUpdated;
    private boolean editable;
    
    private final Label noSettingsLabel;
    
    public MachineConfigurationPane() {
        super();
        controllers = new ArrayList<>();
        badgedLabels = new ArrayList<>();
        
        noSettingsLabel = new Label(Language.Manager.getString("noSettings"));
        noSettingsLabel.setAlignment(Pos.CENTER);
        noSettingsLabel.setMaxWidth(Double.MAX_VALUE);
        
        editable = true;
    }
    
    private void reset() {
        focusData = null;
        controllers.stream().forEach(
            controller -> {
                if (focusData == null && controller.isFocused())
                    focusData = controller.getFocusData();
                controller.dispose();
            }
        );
        controllers.clear();
        getChildren().clear();
    }
    
    private void performEditable() {
        controllers.forEach(ctl -> ctl.setEditable(editable));
    }
    
    public void setOnDataUpdated(Consumer<String> consumer) {
        onDataUpdated = consumer;
    }
    
    public void setControls(Machine newMachine, List<Control<?>> controls) {
        boolean startedFromScratch = newMachine != currentMachine;
        currentMachine = newMachine;
        reset();
        
        List<KeyValue> keyValues1 = new ArrayList<>();
        List<KeyValue> keyValues2 = new ArrayList<>();
        List<KeyValue> keyValues3 = new ArrayList<>();
        mustDoAnimation = false;
        
        TriConsumer<Node, Double, Double> KeyValues = (node, startValue, endValue) -> {
            keyValues1.add(new KeyValue(node.scaleYProperty(), startValue));
            keyValues1.add(new KeyValue(node.opacityProperty(), startValue));
            keyValues2.add(new KeyValue(node.scaleYProperty(), endValue));
            keyValues2.add(new KeyValue(node.opacityProperty(), endValue));
            if (!mustDoAnimation)
                mustDoAnimation = true;
        };
        Consumer<Node> KeyValuesEx = node -> {
            keyValues3.add(new KeyValue(node.scaleYProperty(), 1.0));
            keyValues3.add(new KeyValue(node.opacityProperty(), 1.0));
        };
        
        controls.stream().forEachOrdered(control -> {
                MachineComponentController<?, ?, ?> controller = control.getController();
                MachineComponent<?, ?> machineComponent = controller.getMachineComponent();
                controller.setOnDataUpdated(onDataUpdated);

                ImageView image = Components.loadIcon(currentMachine, machineComponent);
                Label label = Components.loadLabel(machineComponent);
                Node node = control.getNode();

                int rowIndex = controls.indexOf(control) + 1;
                add(image, 0, rowIndex);
                add(label, 1, rowIndex);
                add(node, 2, rowIndex);
                switch (control.getStatus()) {
                    case ADDED -> {
                        label.setGraphic(new ImageView(NEW_BADGE));
                        badgedLabels.add(label);

                        KeyValues.accept(image, 0.0, 1.0);
                        KeyValues.accept(label, 0.0, 1.0);
                        KeyValues.accept(node, 0.0, 1.0);
                        
                        controllers.add(controller);
                    }
                    case DELETED -> {
                        KeyValues.accept(image, 1.0, 0.0);
                        KeyValues.accept(label, 1.0, 0.0);
                        KeyValues.accept(node, 1.0, 0.0);
                    }
                    case CHANGED -> {
                        label.setGraphic(new ImageView(MODIFIED_BADGE));
                        badgedLabels.add(label);

                        KeyValues.accept(image, 1.0, 0.0);
                        KeyValues.accept(label, 1.0, 0.0);
                        KeyValues.accept(node, 1.0, 0.0);
                        KeyValuesEx.accept(image);
                        KeyValuesEx.accept(label);
                        KeyValuesEx.accept(node);
                        
                        controllers.add(controller);
                    }
                    default -> {
                        if (startedFromScratch) {
                            KeyValues.accept(image, 0.0, 1.0);
                            KeyValues.accept(label, 0.0, 1.0);
                            KeyValues.accept(node, 0.0, 1.0);
                        }
                        
                        controllers.add(controller);
                    }
                }

                controller.requestFocus(focusData);
            }
        );
        
        performEditable();
        
        if (mustDoAnimation || controllers.size() <= 0) {
            if (controllers.size() <= 0) {
                setAlignment(Pos.CENTER);
                add(noSettingsLabel, 0, 0, 3, 1);
                KeyValues.accept(noSettingsLabel, 0.0, 1.0);
            } else
                setAlignment(Pos.TOP_LEFT);

            Timeline timeline = new Timeline();
            timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, null, null, keyValues1));
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(300), null, null, keyValues2));
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(600), null,
                event -> getChildren().removeIf(control -> control.getScaleY() == 0.0)
            , keyValues3));
            timeline.play();
        }
    }

    public void clearControls() {
        if (!getChildren().isEmpty()) {
            List<KeyValue> keyValues1 = new ArrayList<>();
            List<KeyValue> keyValues2 = new ArrayList<>();
            
            getChildren().stream().forEach(node -> {
                keyValues1.add(new KeyValue(node.scaleYProperty(), 1.0));
                keyValues1.add(new KeyValue(node.opacityProperty(), 1.0));
                keyValues2.add(new KeyValue(node.scaleYProperty(), 0.0));
                keyValues2.add(new KeyValue(node.opacityProperty(), 0.0));
            });
            
            Timeline timeline = new Timeline();
            timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, null, null, keyValues1));
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(300), null, null, keyValues2));
            timeline.play();
        }
    }

    public void clearBadges() {
        if (!badgedLabels.isEmpty()) {
            List<KeyValue> keyValues1 = new ArrayList<>();
            List<KeyValue> keyValues2 = new ArrayList<>();

            badgedLabels.stream().forEach(label -> {
                Node graphic = label.getGraphic();
                keyValues1.add(new KeyValue(graphic.scaleXProperty(), 1.0));
                keyValues1.add(new KeyValue(graphic.opacityProperty(), 1.0));
                keyValues2.add(new KeyValue(graphic.scaleXProperty(), 0.0));
                keyValues2.add(new KeyValue(graphic.opacityProperty(), 0.0));
            });
            
            Timeline timeline = new Timeline();
            timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, null, null, keyValues1));
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(300), null, event -> {
                badgedLabels.stream().forEach(label -> label.setGraphic(null));
                badgedLabels.clear();
            }, keyValues2));
            timeline.play();
        }
    }
    
    public List<MachineComponentController<?, ?, ?>> getControllers() {
        return controllers;
    }

    public void resetControllers() {
        controllers.stream().filter(
            controller -> controller instanceof DeviceController
        ).map(
            controller -> (DeviceController) controller
        ).forEach(
            controller -> controller.reset()
        );
    }

    public void satisfyRequirement(Requirement requirement, Map<String, SoftwareList> softwareLists) {
        if (requirement == null)
            return;
        
        String interfaceFormat;
        
        if (requirement.getSoftwareList() != null) {
            interfaceFormat = softwareLists.get(
                requirement.getSoftwareList()
            ).getInterfaceFormat(
                requirement.getSoftware()
            );
            
            if (interfaceFormat == null)
                return;
        } else {
            Optional<String> result = currentMachine.getSoftwareLists().stream().map(
                softwareListFilter -> softwareLists.get(softwareListFilter.getSoftwareList())
            ).filter(
                softwareList -> softwareList != null
            ).map(
                softwareList -> softwareList.getInterfaceFormat(requirement.getSoftware())
            ).filter(
                interfaceFormat_ -> interfaceFormat_ != null
            ).findAny();
            
            if (result.isPresent())
                interfaceFormat = result.get();
            else
                return;
        }
            
        controllers.stream().filter(
            controller -> controller.getMachineComponent() instanceof Device device && device.getInterfaceFormats().contains(interfaceFormat)
        ).map(
            controller -> (DeviceController) controller
        ).findAny().ifPresent(
            controller -> controller.setText(requirement.getSoftware())
        );
    }

    public void showList(String deviceName) {
        controllers.stream().filter(
            controller -> controller.getMachineComponent() instanceof Device device && deviceName.equals(device.getName())
        ).findAny().map(
            controller -> (DeviceController) controller
        ).ifPresent(
            device -> device.showList()
        );
    }
    
    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
        performEditable();
    }
}
