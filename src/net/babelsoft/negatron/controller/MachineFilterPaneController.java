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
import java.util.Arrays;
import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import net.babelsoft.negatron.io.cache.UIConfigurationData;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.model.ControllerType;
import net.babelsoft.negatron.model.DisplayType;
import net.babelsoft.negatron.model.ScreenOrientation;
import net.babelsoft.negatron.model.SoundType;
import net.babelsoft.negatron.model.Support;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.view.control.EmulatedItemTreeView;

/**
 * FXML Controller class
 *
 * @author capan
 */
public class MachineFilterPaneController extends FilterPaneController<Machine> {
    
    @FXML
    private CheckBox supportGood;
    @FXML
    private CheckBox supportImperfect;
    @FXML
    private CheckBox supportPreliminary;
    @FXML
    private CheckBox machineTypeElectronic;
    @FXML
    private CheckBox machineTypeMechanical;
    @FXML
    private CheckBox businessModelCoinOperated;
    @FXML
    private CheckBox businessModelFreeAccess;
    @FXML
    private CheckBox softwareHousingEmbedded;
    @FXML
    private CheckBox softwareHousingMedium;
    @FXML
    private CheckBox numberPlayersNone;
    @FXML
    private CheckBox numberPlayersSingle;
    @FXML
    private CheckBox numberPlayersDuo;
    @FXML
    private CheckBox numberPlayersMore;
    @FXML
    private FlowPane controllerPane;
    @FXML
    private CheckBox controllerNone;
    @FXML
    private CheckBox serviceModeAvailable;
    @FXML
    private CheckBox serviceModeUnavailable;
    @FXML
    private CheckBox tiltDetectionAvailable;
    @FXML
    private CheckBox tiltDetectionUnavailable;
    @FXML
    private CheckBox displayNone;
    @FXML
    private CheckBox displayRaster;
    @FXML
    private CheckBox displayVector;
    @FXML
    private CheckBox displayLcd;
    @FXML
    private CheckBox displaySvg;
    @FXML
    private CheckBox displayUnknown;
    @FXML
    private CheckBox screenOrientationHorizontal;
    @FXML
    private CheckBox screenOrientationVertical;
    @FXML
    private CheckBox soundNone;
    @FXML
    private CheckBox soundMono;
    @FXML
    private CheckBox soundStereo;
    @FXML
    private CheckBox soundSurround;
    
    @Override
    protected void initialise() {
        super.initialise();
        
        Arrays.stream(ControllerType.values()).forEachOrdered(controller -> {
            CheckBox checkBox = new CheckBox(controller.toString());
            checkBox.setId("controller" + controller.name());
            checkBox.setTooltip(new Tooltip(controller.getTooltipText()));
            checkBox.setUserData(controller);
            controllerPane.getChildren().add(checkBox);
        });
    }
    
    @Override
    protected UIConfigurationData loadConfiguration() {
        return Configuration.Manager.getFilterConfiguration();
    }
    
    @Override
    protected void wireEvents() {
        super.wireEvents();
        controllerPane.getChildren().stream().map(
            node -> (CheckBox) node
        ).forEach(
            check -> check.setOnAction(evt -> handleOnAction(evt))
        );
    }

    @Override
    public void setTreeView(EmulatedItemTreeView<Machine> treeView) {
        setTreeView(treeView, new MachineFilter());
    }
    
    @Override
    protected void setAsSelectionDisable(boolean disable) {
        setAsSelectionDisable(disable, Language.Manager.getString("setAsSelection.machine"));
    }
    
    @Override
    protected void saveConfiguration(UIConfigurationData data) throws IOException {
        Configuration.Manager.updateFilterConfiguration(data);
    }
    
    @Override
    protected void update(
        Consumer<TextField> updateText, Consumer<RadioButton> updateRadio,
        Consumer<Spinner> updateSpinner, Consumer<CheckBox> updateCheck
    ) {
        super.update(updateText, updateRadio, updateSpinner, updateCheck);
        updateCheck.accept(supportGood);
        updateCheck.accept(supportImperfect);
        updateCheck.accept(supportPreliminary);
        // architecture
        updateCheck.accept(machineTypeElectronic);
        updateCheck.accept(machineTypeMechanical);
        updateCheck.accept(businessModelCoinOperated);
        updateCheck.accept(businessModelFreeAccess);
        updateCheck.accept(softwareHousingEmbedded);
        updateCheck.accept(softwareHousingMedium);
        // input
        updateCheck.accept(numberPlayersNone);
        updateCheck.accept(numberPlayersSingle);
        updateCheck.accept(numberPlayersDuo);
        updateCheck.accept(numberPlayersMore);
        updateCheck.accept(controllerNone);
        controllerPane.getChildren().forEach(
            check -> updateCheck.accept((CheckBox) check)
        );
        updateCheck.accept(serviceModeAvailable);
        updateCheck.accept(serviceModeUnavailable);
        updateCheck.accept(tiltDetectionAvailable);
        updateCheck.accept(tiltDetectionUnavailable);
        // output
        updateCheck.accept(displayNone);
        updateCheck.accept(displayRaster);
        updateCheck.accept(displayVector);
        updateCheck.accept(displayLcd);
        updateCheck.accept(displaySvg);
        updateCheck.accept(displayUnknown);
        updateCheck.accept(screenOrientationHorizontal);
        updateCheck.accept(screenOrientationVertical);
        updateCheck.accept(soundNone);
        updateCheck.accept(soundMono);
        updateCheck.accept(soundStereo);
        updateCheck.accept(soundSurround);
    }
    
    protected class MachineFilter extends Filter<Machine> {

        @Override
        public boolean test(Machine machine) {
            if (!machine.isRunnable())
                return false;
            
            int nbPlayers = machine.getMaxNumberPlayers();
            boolean controllerValidated = controllerPane.getChildren().stream().map(
                node -> (CheckBox) node
            ).filter(
                check -> machine.hasControllerType((ControllerType) check.getUserData()) && check.isSelected()
            ).findAny().isPresent();
            DisplayType display = machine.getDisplayType();
            SoundType sound = machine.getSoundType();
            Support support = machine.getSupport();

            return
                super.test(machine) &&
                (support == Support.GOOD && supportGood.isSelected() || support == Support.IMPERFECT && supportImperfect.isSelected() || support == Support.PRELIMINARY && supportPreliminary.isSelected()) &&
                // architecture
                (!machine.isMechanical() && machineTypeElectronic.isSelected() || machine.isMechanical() && machineTypeMechanical.isSelected()) &&
                (machine.hasCoinSlot() && businessModelCoinOperated.isSelected() || !machine.hasCoinSlot() && businessModelFreeAccess.isSelected()) &&
                (machine.isSoftwareEmbedded() && softwareHousingEmbedded.isSelected() || !machine.isSoftwareEmbedded() && softwareHousingMedium.isSelected()) &&
                // input
                (nbPlayers == 0 && numberPlayersNone.isSelected() || nbPlayers == 1 && numberPlayersSingle.isSelected() || nbPlayers == 2 && numberPlayersDuo.isSelected() || nbPlayers > 2 && numberPlayersMore.isSelected()) &&
                (!machine.hasControllerTypes() && controllerNone.isSelected() || controllerValidated) &&
                (machine.hasServiceMode() && serviceModeAvailable.isSelected() || !machine.hasServiceMode() && serviceModeUnavailable.isSelected()) &&
                (machine.hasTilt() && tiltDetectionAvailable.isSelected() || !machine.hasTilt() && tiltDetectionUnavailable.isSelected()) &&
                // output
                (display == DisplayType.none && displayNone.isSelected() || display == DisplayType.raster && displayRaster.isSelected() || display == DisplayType.vector && displayVector.isSelected() || display == DisplayType.lcd && displayLcd.isSelected() || display == DisplayType.svg && displaySvg.isSelected() || display == DisplayType.unknown && displayUnknown.isSelected()) &&
                (display == DisplayType.none || display == DisplayType.unknown || machine.getScreenOrientation() == ScreenOrientation.HORIZONTAL && screenOrientationHorizontal.isSelected() || machine.getScreenOrientation() == ScreenOrientation.VERTICAL && screenOrientationVertical.isSelected()) &&
                (sound == SoundType.NONE && soundNone.isSelected() || sound == SoundType.MONO && soundMono.isSelected() || sound == SoundType.STEREO && soundStereo.isSelected() || sound == SoundType.SURROUND && soundSurround.isSelected())
            ;
        }
    }

    @FXML
    protected void handleOnSetSupportAsSelection(ActionEvent event) {
        supportGood.setSelected(false);
        supportImperfect.setSelected(false);
        supportPreliminary.setSelected(false);
        
        switch (currentItem.getSupport()) {
            case GOOD:
                supportGood.setSelected(true);
                break;
            case IMPERFECT:
                supportImperfect.setSelected(true);
                break;
            default: //case PRELIMINARY
                supportPreliminary.setSelected(true);
                break;
        }
        
        filterTimeline.playFromStart();
        supportGood.requestFocus();
    }

    @FXML
    private void handleOnSetMachineTypeAsSelection(ActionEvent event) {
        if (currentItem.isMechanical()) {
            machineTypeElectronic.setSelected(false);
            machineTypeMechanical.setSelected(true);
        } else {
            machineTypeElectronic.setSelected(true);
            machineTypeMechanical.setSelected(false);
        }
        
        filterTimeline.playFromStart();
        machineTypeElectronic.requestFocus();
    }

    @FXML
    private void handleOnSetBusinessModelAsSelection(ActionEvent event) {
        if (currentItem.hasCoinSlot()) {
            businessModelCoinOperated.setSelected(true);
            businessModelFreeAccess.setSelected(false);
        } else {
            businessModelCoinOperated.setSelected(false);
            businessModelFreeAccess.setSelected(true);
        }
        
        filterTimeline.playFromStart();
        businessModelCoinOperated.requestFocus();
    }

    @FXML
    private void handleOnSetSoftwareHousingTypeAsSelection(ActionEvent event) {
        if (currentItem.isSoftwareEmbedded()) {
            softwareHousingEmbedded.setSelected(true);
            softwareHousingMedium.setSelected(false);
        } else {
            softwareHousingEmbedded.setSelected(false);
            softwareHousingMedium.setSelected(true);
        }
        
        filterTimeline.playFromStart();
        softwareHousingEmbedded.requestFocus();
    }

    @FXML
    private void handleOnSetMaxNumberPlayersAsSelection(ActionEvent event) {
        numberPlayersNone.setSelected(false);
        numberPlayersSingle.setSelected(false);
        numberPlayersDuo.setSelected(false);
        numberPlayersMore.setSelected(false);
        
        switch (currentItem.getMaxNumberPlayers()) {
            case 0:
                numberPlayersNone.setSelected(true);
                break;
            case 1:
                numberPlayersSingle.setSelected(true);
                break;
            case 2:
                numberPlayersDuo.setSelected(true);
                break;
            default:
                numberPlayersMore.setSelected(true);
                break;
        }
        
        filterTimeline.playFromStart();
        numberPlayersNone.requestFocus();
    }

    @FXML
    private void handleOnSelectAllControllers(ActionEvent event) {
        controllerPane.getChildren().forEach(checkBox -> ((CheckBox) checkBox).setSelected(true));
        filterTimeline.playFromStart();
    }

    @FXML
    private void handleOnSelectNoneControllers(ActionEvent event) {
        controllerPane.getChildren().forEach(checkBox -> ((CheckBox) checkBox).setSelected(false));
        filterTimeline.playFromStart();
    }

    @FXML
    private void handleOnSetControllerAsSelection(ActionEvent event) {
        controllerPane.getChildren().stream().map(
            checkBox -> ((CheckBox) checkBox)
        ).forEach(
            checkBox -> {
                ControllerType type = (ControllerType) checkBox.getUserData();
                if (currentItem.hasControllerType(type))
                    checkBox.setSelected(true);
                else
                    checkBox.setSelected(false);
            }
        );
        if (!currentItem.hasControllerTypes())
            controllerNone.setSelected(true);
        filterTimeline.playFromStart();
    }

    @FXML
    private void handleOnSetServiceModeAsSelection(ActionEvent event) {
        if (currentItem.hasServiceMode()) {
            serviceModeAvailable.setSelected(true);
            serviceModeUnavailable.setSelected(false);
        } else {
            serviceModeAvailable.setSelected(false);
            serviceModeUnavailable.setSelected(true);
        }
        
        filterTimeline.playFromStart();
        serviceModeAvailable.requestFocus();
    }

    @FXML
    private void handleOnSetTiltSlamDetectionAsSelection(ActionEvent event) {
        if (currentItem.hasTilt()) {
            tiltDetectionAvailable.setSelected(true);
            tiltDetectionUnavailable.setSelected(false);
        } else {
            tiltDetectionAvailable.setSelected(false);
            tiltDetectionUnavailable.setSelected(true);
        }
        
        filterTimeline.playFromStart();
        tiltDetectionAvailable.requestFocus();
    }

    @FXML
    private void handleOnSetDisplayAsSelection(ActionEvent event) {
        displayNone.setSelected(false);
        displayRaster.setSelected(false);
        displayVector.setSelected(false);
        displayLcd.setSelected(false);
        displaySvg.setSelected(false);
        displayUnknown.setSelected(false);
        
        switch (currentItem.getDisplayType()) {
            case none:
                displayNone.setSelected(true);
                break;
            case raster:
                displayRaster.setSelected(true);
                break;
            case vector:
                displayVector.setSelected(true);
                break;
            case lcd:
                displayLcd.setSelected(true);
                break;
            case svg:
                displaySvg.setSelected(true);
                break;
            default: //case unknown
                displayUnknown.setSelected(true);
                break;
        }
        
        filterTimeline.playFromStart();
        displayNone.requestFocus();
    }

    @FXML
    private void handleOnSetScreenOrientationAsSelection(ActionEvent event) {
        screenOrientationHorizontal.setSelected(false);
        screenOrientationVertical.setSelected(false);
        
        switch (currentItem.getScreenOrientation()) {
            case VERTICAL:
                screenOrientationVertical.setSelected(true);
                break;
            default: //case HORIZONTAL
                screenOrientationHorizontal.setSelected(true);
                break;
        }
        
        filterTimeline.playFromStart();
        screenOrientationHorizontal.requestFocus();
    }

    @FXML
    private void handleOnSetSoundAsSelection(ActionEvent event) {
        soundNone.setSelected(false);
        soundMono.setSelected(false);
        soundStereo.setSelected(false);
        soundSurround.setSelected(false);
        
        switch (currentItem.getSoundType()) {
            case NONE:
                soundNone.setSelected(true);
                break;
            case MONO:
                soundMono.setSelected(true);
                break;
            case STEREO:
                soundStereo.setSelected(true);
                break;
            default: //case SURROUND
                soundSurround.setSelected(true);
                break;
        }
        
        filterTimeline.playFromStart();
        soundNone.requestFocus();
    }
}
