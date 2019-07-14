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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.Domain;
import net.babelsoft.negatron.io.configuration.InputDevice;
import net.babelsoft.negatron.io.configuration.Property;
import net.babelsoft.negatron.io.configuration.SampleRate;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.view.control.form.ChdmanPathField;
import net.babelsoft.negatron.view.control.form.CheatCheckField;
import net.babelsoft.negatron.view.control.form.ExtrasRootPathField;
import net.babelsoft.negatron.view.control.form.FloatingNumberField;
import net.babelsoft.negatron.view.control.form.FontSelectionField;
import net.babelsoft.negatron.view.control.form.GenericCheckField;
import net.babelsoft.negatron.view.control.form.GridAdornment;
import net.babelsoft.negatron.view.control.form.IntegerNumberField;
import net.babelsoft.negatron.view.control.form.IntegerSpinnerField;
import net.babelsoft.negatron.view.control.form.LocalisedChoiceField;
import net.babelsoft.negatron.view.control.form.MameIniField;
import net.babelsoft.negatron.view.control.form.MameLanguageChoiceField;
import net.babelsoft.negatron.view.control.form.MamePathField;
import net.babelsoft.negatron.view.control.form.MultiPathField;
import net.babelsoft.negatron.view.control.form.MultimediaRootPathField;
import net.babelsoft.negatron.view.control.form.NegatronLanguageChoiceField;
import net.babelsoft.negatron.view.control.form.SkinChoiceField;
import net.babelsoft.negatron.view.control.form.TextField;
import net.babelsoft.negatron.view.control.form.ValueChoiceField;
import net.babelsoft.negatron.view.control.form.VlcPathField;
import net.babelsoft.negatron.view.control.form.VsyncChoiceField;

/**
 * FXML Controller class
 *
 * @author capan
 */
public class GlobalConfigurationPaneController implements Initializable {
    
    private final static double SPACING = 16.0;
    
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab foldersTab;
    @FXML
    private Tab optionsTab;
    @FXML
    private Tab inputsTab;
    @FXML
    private Tab graphicsTab;
    @FXML
    private Tab osdTab;
    
    @FXML
    private Label foldersLabel;
    @FXML
    private Label foldersLabel2;
    @FXML
    private GridPane foldersGrid;
    @FXML
    private GridPane foldersGrid2;
    @FXML
    private Label optionsLabel;
    @FXML
    private Label optionsLabel2;
    @FXML
    private GridPane optionsGrid;
    @FXML
    private GridPane optionsGrid2;
    @FXML
    private GridPane inputsGrid;
    @FXML
    private GridPane inputsGrid2;
    @FXML
    private GridPane graphicsGrid;
    @FXML
    private GridPane graphicsGrid2;

    private MamePathField mameExec;
    private MameIniField mameIni;
    private ChdmanPathField chdmanExec;
    
    private MultiPathField artwork;
    private MultiPathField cheat;
    private MultiPathField controller;
    private MultiPathField rom;
    private MultiPathField sample;
    
    private MultiPathField information;
    private MultiPathField folderView;
    
    private VlcPathField vlcPath;
    
    private ExtrasRootPathField mameExtras;
    private MultimediaRootPathField mameMultimedia;
    
    private MultiPathField icon;
    private MultiPathField manual;
    private MultiPathField snapshot;
    private MultiPathField title;
    private MultiPathField videoPreview;
    
    private MultiPathField artworkPreview;
    private MultiPathField boss;
    private MultiPathField cabinet;
    private MultiPathField controlPanel;
    private MultiPathField device;
    private MultiPathField end;
    private MultiPathField flyer;
    private MultiPathField gameOver;
    private MultiPathField howTo;
    private MultiPathField logo;
    private MultiPathField marquee;
    private MultiPathField pcb;
    private MultiPathField score;
    private MultiPathField select;
    private MultiPathField soundtrack;
    private MultiPathField versus;
    
    private MultiPathField boxArt;
    private MultiPathField cover;
    private MultiPathField media;
    
    private CheatCheckField cheatCheck;
    private VsyncChoiceField vsync;
    
    private SkinChoiceField skin;
    private FontSelectionField font;
    private NegatronLanguageChoiceField language;
    
    private GenericCheckField lightgun;
    private GenericCheckField dualLightgun;
    
    private GenericCheckField keepaspect;
    private GenericCheckField unevenstretch;
    private GenericCheckField intoverscan;
    private IntegerNumberField intscalex;
    private IntegerNumberField intscaley;
    
    private GenericCheckField throttle;
    private GenericCheckField sleep;
    private FloatingNumberField speed;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tabPane.getSelectionModel().select(Configuration.Manager.getSoftwareInformationTabIndex());
        tabPane.getSelectionModel().selectedIndexProperty().addListener((o, oV, newValue) -> {
            try {
                Configuration.Manager.updateSoftwareInformationTabIndex(newValue.intValue());
            } catch (IOException ex) {
                Logger.getLogger(GlobalConfigurationPaneController.class.getName()).log(Level.SEVERE, "Couldn't save global conf tab index", ex);
            }
        });
        
        final boolean isMess = Configuration.Manager.isMess();
        final String compressedArchives = rb.getString("compressedArchives");
        if (isMess) {
            foldersLabel.setText(foldersLabel.getText().replace("MAME", "MESS"));
            foldersLabel2.setText(foldersLabel2.getText().replace("MAME", "MESS"));
            optionsLabel.setText(optionsLabel.getText().replace("MAME", "MESS"));
            //optionsLabel2.setText(optionsLabel2.getText().replace("MAME", "MESS"));
        }
        
        // Folders: MAME column
        
        int rowIdx = 0;
        mameExec        = new MamePathField     (foldersGrid, rowIdx++, isMess);
        mameIni         = new MameIniField      (foldersGrid, rowIdx++, isMess);
        chdmanExec      = new ChdmanPathField   (foldersGrid, rowIdx++);
        
        GridAdornment.insertSpacing             (foldersGrid, rowIdx++, SPACING);
        artwork         = new MultiPathField    (foldersGrid, rowIdx++, Property.ARTWORK,          rb.getString("artwork"), rb.getString("artwork.tooltip"));
        cheat           = new MultiPathField    (foldersGrid, rowIdx++, Property.CHEAT,            rb.getString("cheat"), rb.getString("cheat.tooltip"), compressedArchives, "*.7z; *.zip");
        controller      = new MultiPathField    (foldersGrid, rowIdx++, Property.CONTROLLER,       rb.getString("controllerDefinition"), rb.getString("controllerDefinition.tooltip"));
        rom             = new MultiPathField    (foldersGrid, rowIdx++, Property.ROM,              rb.getString("rom"), rb.getString("rom.tooltip"));
        sample          = new MultiPathField    (foldersGrid, rowIdx++, Property.SAMPLE,           rb.getString("sample"), rb.getString("sample.tooltip"));
        
        GridAdornment.insertSpacing             (foldersGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle               (foldersGrid, rowIdx++, SPACING,                   isMess ? rb.getString("mameExtrasHistoryVersioning").replaceFirst("MAME", "MESS") : rb.getString("mameExtrasHistoryVersioning"));
        information     = new MultiPathField    (foldersGrid, rowIdx++, Property.INFORMATION,      rb.getString("information"), rb.getString("information.tooltip"), rb.getString("dataFiles"), "*.dat");
        folderView      = new MultiPathField    (foldersGrid, rowIdx++, Property.FOLDER_VIEW,      rb.getString("folderView"), rb.getString("folderView.tooltip"));
        
        GridAdornment.insertSpacing             (foldersGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle               (foldersGrid, rowIdx++, SPACING,                   rb.getString("miscellaneous"));
        vlcPath         = new VlcPathField      (foldersGrid, rowIdx++);
        
        // Folders: EXTRAs column
        
        rowIdx = 0;
        mameExtras      = new ExtrasRootPathField    (foldersGrid2, rowIdx++, isMess);
        mameMultimedia  = new MultimediaRootPathField(foldersGrid2, rowIdx++);
        
        GridAdornment.insertHeader              (foldersGrid2, rowIdx++, SPACING, Domain.EXTRAS_MACHINE_SOFTWARE);
        icon            = new MultiPathField    (foldersGrid2, rowIdx++, Property.ICON,             rb.getString("icon"), rb.getString("icon.tooltip"), compressedArchives, "*.zip");
        manual          = new MultiPathField    (foldersGrid2, rowIdx++, Property.MANUAL,           rb.getString("manual"), rb.getString("manual.tooltip"), compressedArchives, "*.zip");
        snapshot        = new MultiPathField    (foldersGrid2, rowIdx++, Property.SNAPSHOT,         rb.getString("snapshot"), rb.getString("snapshot.tooltip"), compressedArchives, "*.zip");
        title           = new MultiPathField    (foldersGrid2, rowIdx++, Property.TITLE,            rb.getString("title"), rb.getString("title.tooltip"), compressedArchives, "*.zip");
        videoPreview    = new MultiPathField    (foldersGrid2, rowIdx++, Property.VIDEO_PREVIEW,    rb.getString("videoPreview"), rb.getString("videoPreview.tooltip"));
        
        GridAdornment.insertHeader              (foldersGrid2, rowIdx++, SPACING, Domain.EXTRAS_MACHINE_ONLY);
        artworkPreview  = new MultiPathField    (foldersGrid2, rowIdx++, Property.ARTWORK_PREVIEW,  rb.getString("artworkPreview"), rb.getString("artworkPreview.tooltip"), compressedArchives, "*.zip");
        boss            = new MultiPathField    (foldersGrid2, rowIdx++, Property.BOSS,             rb.getString("boss"), rb.getString("boss.tooltip"), compressedArchives, "*.zip");
        cabinet         = new MultiPathField    (foldersGrid2, rowIdx++, Property.CABINET,          rb.getString("cabinet"), rb.getString("cabinet.tooltip"), compressedArchives, "*.zip");
        controlPanel    = new MultiPathField    (foldersGrid2, rowIdx++, Property.CONTROL_PANEL,    rb.getString("controlPanel"), rb.getString("controlPanel.tooltip"), compressedArchives, "*.zip");
        device          = new MultiPathField    (foldersGrid2, rowIdx++, Property.DEVICE,           rb.getString("device"), rb.getString("device.tooltip"), compressedArchives, "*.zip");
        end             = new MultiPathField    (foldersGrid2, rowIdx++, Property.END,              rb.getString("end"), rb.getString("end.tooltip"), compressedArchives, "*.zip");
        flyer           = new MultiPathField    (foldersGrid2, rowIdx++, Property.FLYER,            rb.getString("flyer"), rb.getString("flyer.tooltip"), compressedArchives, "*.zip");
        gameOver        = new MultiPathField    (foldersGrid2, rowIdx++, Property.GAME_OVER,        rb.getString("gameOver"), rb.getString("gameOver.tooltip"), compressedArchives, "*.zip");
        howTo           = new MultiPathField    (foldersGrid2, rowIdx++, Property.HOW_TO,           rb.getString("howTo"), rb.getString("howTo.tooltip"), compressedArchives, "*.zip");
        logo            = new MultiPathField    (foldersGrid2, rowIdx++, Property.LOGO,             rb.getString("logo"), rb.getString("logo.tooltip"), compressedArchives, "*.zip");
        marquee         = new MultiPathField    (foldersGrid2, rowIdx++, Property.MARQUEE,          rb.getString("marquee"), rb.getString("marquee.tooltip"), compressedArchives, "*.zip");
        pcb             = new MultiPathField    (foldersGrid2, rowIdx++, Property.PCB,              rb.getString("pcb"), rb.getString("pcb.tooltip"), compressedArchives, "*.zip");
        score           = new MultiPathField    (foldersGrid2, rowIdx++, Property.SCORE,            rb.getString("score"), rb.getString("score.tooltip"), compressedArchives, "*.zip");
        select          = new MultiPathField    (foldersGrid2, rowIdx++, Property.SELECT,           rb.getString("select"), rb.getString("select.tooltip"), compressedArchives, "*.zip");
        soundtrack      = new MultiPathField    (foldersGrid2, rowIdx++, Property.SOUNDTRACK,       rb.getString("soundtrack"), rb.getString("soundtrack.tooltip"));
        versus          = new MultiPathField    (foldersGrid2, rowIdx++, Property.VERSUS,           rb.getString("versus"), rb.getString("versus.tooltip"), compressedArchives, "*.zip");
        
        GridAdornment.insertHeader              (foldersGrid2, rowIdx++, SPACING, Domain.EXTRAS_SOFTWARE_ONLY);
        boxArt          = new MultiPathField    (foldersGrid2, rowIdx++, Property.BOX_ART,          rb.getString("boxArt"), rb.getString("boxArt.tooltip"));
        cover           = new MultiPathField    (foldersGrid2, rowIdx++, Property.COVER,            rb.getString("cover"), rb.getString("cover.tooltip"), compressedArchives, "*.zip");
        media           = new MultiPathField    (foldersGrid2, rowIdx++, Property.MEDIA,            rb.getString("media"), rb.getString("media.tooltip"));
        
        // Options: MAME column
        
        rowIdx = 0;
        new GenericCheckField                   (optionsGrid, rowIdx++, "skip_gameinfo");
        cheatCheck      = new CheatCheckField   (optionsGrid, rowIdx++, isMess);
        new GenericCheckField                   (optionsGrid, rowIdx++, "autosave");
        vsync           = new VsyncChoiceField  (optionsGrid, rowIdx++, isMess);
        new MameLanguageChoiceField             (optionsGrid, rowIdx++);
        new IntegerNumberField                  (optionsGrid, rowIdx++, "seconds_to_run",           0, 600, 60, 1, 1);
        
        GridAdornment.insertSpacing             (optionsGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle               (optionsGrid, rowIdx++, SPACING, rb.getString("soundOptions"));
        SampleRate[] sampleRates = SampleRate.values();
        new ValueChoiceField<SampleRate>        (optionsGrid, rowIdx++, "samplerate",     sampleRates);
        new GenericCheckField                   (optionsGrid, rowIdx++, "samples");
        new IntegerNumberField                  (optionsGrid, rowIdx++, "volume",                 -32, 0, 2, 1, 1);
        
        GridAdornment.insertSpacing             (optionsGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle               (optionsGrid, rowIdx++, SPACING, rb.getString("commOptions"));
        new TextField                           (optionsGrid, rowIdx++, "comm_localhost");
        new IntegerSpinnerField                 (optionsGrid, rowIdx++, "comm_localport", 65535); // 65535 = 2^16 - 1
        new TextField                           (optionsGrid, rowIdx++, "comm_remotehost");
        new IntegerSpinnerField                 (optionsGrid, rowIdx++, "comm_remoteport", 65535);
        if (Configuration.Manager.getGlobalConfiguration("comm_framesync") != null)
            new GenericCheckField               (optionsGrid, rowIdx++, "comm_framesync");

        // Options: Negatron column
        
        rowIdx = 0;
        skin            = new SkinChoiceField            (optionsGrid2, rowIdx++);
        font            = new FontSelectionField         (optionsGrid2, rowIdx++);
        language        = new NegatronLanguageChoiceField(optionsGrid2, rowIdx++);
        
        GridAdornment.insertSpacing             (optionsGrid2, rowIdx++, SPACING);
        
        // Inputs
        
        rowIdx = 0;
        GridAdornment.insertTitle               (inputsGrid, rowIdx++, SPACING, rb.getString("globalInputOptions"));
        new GenericCheckField                   (inputsGrid, rowIdx++, "mouse");
        new GenericCheckField                   (inputsGrid, rowIdx++, "joystick");
        lightgun = new GenericCheckField        (inputsGrid, rowIdx++, "lightgun");
        new GenericCheckField                   (inputsGrid, rowIdx++, "multikeyboard");
        new GenericCheckField                   (inputsGrid, rowIdx++, "multimouse");
        // Windows native only MAME options
        if (Configuration.Manager.getGlobalConfiguration("global_inputs") != null)
            new GenericCheckField               (inputsGrid, rowIdx++, "global_inputs");
        
        GridAdornment.insertSpacing             (inputsGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle               (inputsGrid, rowIdx++, SPACING, rb.getString("keyboardSpecificOptions"));
        new GenericCheckField                   (inputsGrid, rowIdx++, "steadykey");
        //new KeyField                            (inputsGrid, rowIdx++, "uimodekey");
        new GenericCheckField                   (inputsGrid, rowIdx++, "ui_active");
        new GenericCheckField                   (inputsGrid, rowIdx++, "natural");
        
        GridAdornment.insertSpacing             (inputsGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle               (inputsGrid, rowIdx++, SPACING, rb.getString("joystickSpecificOptions"));
        new FloatingNumberField                 (inputsGrid, rowIdx++, "joystick_deadzone",    "%.1f", 0.0, 1.0, 0.5, 4, 0.1);
        new FloatingNumberField                 (inputsGrid, rowIdx++, "joystick_saturation",  "%.2f", 0.0, 1.0, 0.1, 1, 0.05);
        new GenericCheckField                   (inputsGrid, rowIdx++, "joystick_contradictory");
        
        rowIdx = 0;
        GridAdornment.insertTitle               (inputsGrid2, rowIdx++, SPACING, rb.getString("machineInputSpecificOptions"));
        InputDevice[] inputDevices = InputDevice.values();
        new LocalisedChoiceField<InputDevice>   (inputsGrid2, rowIdx++, "paddle_device",     inputDevices);
        new LocalisedChoiceField<InputDevice>   (inputsGrid2, rowIdx++, "adstick_device",    inputDevices);
        new LocalisedChoiceField<InputDevice>   (inputsGrid2, rowIdx++, "pedal_device",      inputDevices);
        new LocalisedChoiceField<InputDevice>   (inputsGrid2, rowIdx++, "dial_device",       inputDevices);
        new LocalisedChoiceField<InputDevice>   (inputsGrid2, rowIdx++, "trackball_device",  inputDevices);
        new LocalisedChoiceField<InputDevice>   (inputsGrid2, rowIdx++, "lightgun_device",   inputDevices);
        new LocalisedChoiceField<InputDevice>   (inputsGrid2, rowIdx++, "positional_device", inputDevices);
        new LocalisedChoiceField<InputDevice>   (inputsGrid2, rowIdx++, "mouse_device",      inputDevices);
        
        GridAdornment.insertSpacing             (inputsGrid2, rowIdx++, SPACING);
        GridAdornment.insertTitle               (inputsGrid2, rowIdx++, SPACING, rb.getString("coinSpecificOptions"));
        new GenericCheckField                   (inputsGrid2, rowIdx++, "coin_lockout");
        new IntegerNumberField                  (inputsGrid2, rowIdx++, "coin_impulse",                0, 60, 10, 9, 1);
        
        GridAdornment.insertSpacing             (inputsGrid2, rowIdx++, SPACING);
        GridAdornment.insertTitle               (inputsGrid2, rowIdx++, SPACING, rb.getString("lightGunSpecificOptions"));
        new GenericCheckField                   (inputsGrid2, rowIdx++, "offscreen_reload");
        // Windows native only MAME options
        if (Configuration.Manager.getGlobalConfiguration("dual_lightgun") != null) {
            dualLightgun = new GenericCheckField(inputsGrid2, rowIdx++, "dual_lightgun");
            lightgun.selectedProperty().addListener((o, oV, newValue) -> {
                dualLightgun.setDisable(!newValue);
            });
            dualLightgun.setDisable(!lightgun.isSelected());
        }
        
        // Graphics
        
        rowIdx = 0;
        GridAdornment.insertTitle               (graphicsGrid, rowIdx++, SPACING, rb.getString("screenOptions"));
        new FloatingNumberField                 (graphicsGrid, rowIdx++, "brightness",         "%.2f", 0.0, 2.0, 0.5, 4, 0.1);
        new FloatingNumberField                 (graphicsGrid, rowIdx++, "contrast",           "%.2f", 0.0, 2.0, 0.5, 4, 0.1);
        new FloatingNumberField                 (graphicsGrid, rowIdx++, "gamma",              "%.2f", 0.0, 3.0, 0.5, 4, 0.1);
        new FloatingNumberField                 (graphicsGrid, rowIdx++, "pause_brightness",   "%.2f", 0.0, 1.0, 0.1, 1, 0.05);
        
        GridAdornment.insertSpacing             (graphicsGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle               (graphicsGrid, rowIdx++, SPACING, rb.getString("renderingOptions"));
        keepaspect = new GenericCheckField      (graphicsGrid, rowIdx++, "keepaspect");
        unevenstretch = new GenericCheckField   (graphicsGrid, rowIdx++, "unevenstretch");
        new GenericCheckField                   (graphicsGrid, rowIdx++, "unevenstretchx");
        new GenericCheckField                   (graphicsGrid, rowIdx++, "unevenstretchy");
        new GenericCheckField                   (graphicsGrid, rowIdx++, "autostretchxy");
        intoverscan = new GenericCheckField     (graphicsGrid, rowIdx++, "intoverscan");
        intscalex = new IntegerNumberField      (graphicsGrid, rowIdx++, "intscalex",                 0, 10, 2, 1, 1);
        intscaley = new IntegerNumberField      (graphicsGrid, rowIdx++, "intscaley",                 0, 10, 2, 1, 1);
        
        InvalidationListener listener = (o) -> {
            if (keepaspect.isSelected() || unevenstretch.isSelected()) {
                intoverscan.setDisable(true);
                intscalex.setDisable(true);
                intscaley.setDisable(true);
            } else {
                intoverscan.setDisable(false);
                intscalex.setDisable(false);
                intscaley.setDisable(false);
            }
        };
        listener.invalidated(null);
        keepaspect.selectedProperty().addListener(listener);
        unevenstretch.selectedProperty().addListener(listener);
        
        GridAdornment.insertSpacing             (graphicsGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle               (graphicsGrid, rowIdx++, SPACING, rb.getString("vectorOptions"));
        new FloatingNumberField                 (graphicsGrid, rowIdx++, "beam_width_min",          "%.2f", 0.0, 1.0, 0.1, 1, 0.1);
        new FloatingNumberField                 (graphicsGrid, rowIdx++, "beam_width_max",          "%.2f", 1.0, 10.0, 1.0, 5, 0.1);
        new FloatingNumberField                 (graphicsGrid, rowIdx++, "beam_intensity_weight",   "%.2f", -10.0, 10.0, 2.0, 5, 0.1);
        new FloatingNumberField                 (graphicsGrid, rowIdx++, "flicker",                 "%.2f", 0.0, 100.0, 10.0, 4, 1.0);
        
        rowIdx = 0;
        GridAdornment.insertTitle               (graphicsGrid2, rowIdx++, SPACING, rb.getString("performanceOptions"));
        new GenericCheckField                   (graphicsGrid2, rowIdx++, "autoframeskip");
        new IntegerNumberField                  (graphicsGrid2, rowIdx++, "frameskip",                 0, 10, 2, 1, 1);
        throttle = new GenericCheckField        (graphicsGrid2, rowIdx++, "throttle");
        sleep = new GenericCheckField           (graphicsGrid2, rowIdx++, "sleep");
        speed = new FloatingNumberField         (graphicsGrid2, rowIdx++, "speed",             "%.2f", 0.0, 100.0, 10.0, 4, 1.0);
        new GenericCheckField                   (graphicsGrid2, rowIdx++, "refreshspeed");
        throttle.selectedProperty().addListener((o, oV, newValue) -> {
            sleep.setDisable(!newValue);
            speed.setDisable(!newValue);
        });
        
        GridAdornment.insertSpacing             (graphicsGrid2, rowIdx++, SPACING);
        GridAdornment.insertTitle               (graphicsGrid2, rowIdx++, SPACING, rb.getString("rotationOptions"));
        new GenericCheckField                   (graphicsGrid2, rowIdx++, "rotate");
        new GenericCheckField                   (graphicsGrid2, rowIdx++, "ror");
        new GenericCheckField                   (graphicsGrid2, rowIdx++, "rol");
        new GenericCheckField                   (graphicsGrid2, rowIdx++, "autoror");
        new GenericCheckField                   (graphicsGrid2, rowIdx++, "autorol");
        new GenericCheckField                   (graphicsGrid2, rowIdx++, "flipx");
        new GenericCheckField                   (graphicsGrid2, rowIdx++, "flipy");
        
        GridAdornment.insertSpacing             (graphicsGrid2, rowIdx++, SPACING);
        GridAdornment.insertTitle               (graphicsGrid2, rowIdx++, SPACING, rb.getString("artworkOptions"));
        new GenericCheckField                   (graphicsGrid2, rowIdx++, "artwork_crop");
        new GenericCheckField                   (graphicsGrid2, rowIdx++, "use_backdrops");
        new GenericCheckField                   (graphicsGrid2, rowIdx++, "use_overlays");
        new GenericCheckField                   (graphicsGrid2, rowIdx++, "use_bezels");
        new GenericCheckField                   (graphicsGrid2, rowIdx++, "use_cpanels");
        new GenericCheckField                   (graphicsGrid2, rowIdx++, "use_marquees");
    }
    
    private void addRemoveTab(Tab tab) {
        if (tab.getTabPane() == tabPane)
            tabPane.getTabs().remove(tab);
        else
            tabPane.getTabs().add(tab);
    }
    
    private void addRemoveFoldersGridField(MultiPathField field) {
        Node node = field.getNode();
        if (node.getParent() != null) {
            foldersGrid.getChildren().removeAll(node, field.getLabel());
        } else
            foldersGrid.getChildren().addAll(node, field.getLabel());
    }
    
    private void addRemoveGridFields(GridPane grid, int rowCutoff) {
        grid.getChildren().forEach(node -> {
            int rowIndex = GridPane.getRowIndex(node);
            if (rowIndex > rowCutoff)
                node.setVisible(!node.isVisible());
        });
    }
    
    @FXML
    private void handleOnAdvancedOptions(ActionEvent event) {
        addRemoveTab(inputsTab);
        addRemoveTab(graphicsTab);
        addRemoveTab(osdTab);
        addRemoveFoldersGridField(artwork);
        addRemoveFoldersGridField(cheat);
        addRemoveFoldersGridField(controller);
        addRemoveGridFields(foldersGrid, 7);
        addRemoveGridFields(foldersGrid2, 1);
        addRemoveGridFields(optionsGrid, 4);
    }
    
    public void setOnRestart(Delegate delegate) {
        language.setOnRestart(delegate);
    }

    public void selectOptionsTab() {
        tabPane.getSelectionModel().select(optionsTab);
    }

    public void disableLanguageOption(boolean disable) {
        language.setDisable(disable);
    }

    public void resetVlcPath() {
        vlcPath.reset();
    }
}
