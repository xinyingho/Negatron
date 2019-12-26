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
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.Domain;
import net.babelsoft.negatron.io.configuration.InfotipTiming;
import net.babelsoft.negatron.io.configuration.InputDevice;
import net.babelsoft.negatron.io.configuration.JoystickProvider;
import net.babelsoft.negatron.io.configuration.KeyboardProvider;
import net.babelsoft.negatron.io.configuration.LightgunProvider;
import net.babelsoft.negatron.io.configuration.MonitorProvider;
import net.babelsoft.negatron.io.configuration.MouseProvider;
import net.babelsoft.negatron.io.configuration.Output;
import net.babelsoft.negatron.io.configuration.Property;
import net.babelsoft.negatron.io.configuration.SampleRate;
import net.babelsoft.negatron.io.configuration.Sound;
import net.babelsoft.negatron.io.configuration.UIFontProvider;
import net.babelsoft.negatron.io.configuration.Video;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.view.control.form.ChdmanPathField;
import net.babelsoft.negatron.view.control.form.CheatCheckField;
import net.babelsoft.negatron.view.control.form.ExtrasRootPathField;
import net.babelsoft.negatron.view.control.form.FloatingNumberField;
import net.babelsoft.negatron.view.control.form.FontSelectionField;
import net.babelsoft.negatron.view.control.form.GenericCheckField;
import net.babelsoft.negatron.view.control.form.GridAdornment;
import net.babelsoft.negatron.view.control.form.InfotipTimingChoiceField;
import net.babelsoft.negatron.view.control.form.IntegerNumberField;
import net.babelsoft.negatron.view.control.form.IntegerSpinnerField;
import net.babelsoft.negatron.view.control.form.LocalisedChoiceField;
import net.babelsoft.negatron.view.control.form.MameIniField;
import net.babelsoft.negatron.view.control.form.MameLanguageChoiceField;
import net.babelsoft.negatron.view.control.form.MamePathField;
import net.babelsoft.negatron.view.control.form.MultiPathField;
import net.babelsoft.negatron.view.control.form.MultimediaRootPathField;
import net.babelsoft.negatron.view.control.form.NegatronLanguageChoiceField;
import net.babelsoft.negatron.view.control.form.OSDChoiceField;
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
    @FXML
    private GridPane osdGrid;
    @FXML
    private GridPane osdGrid2;

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
    
    private int rowIdx;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tabPane.getSelectionModel().select(Configuration.Manager.getGlobalConfigurationTabIndex());
        tabPane.getSelectionModel().selectedIndexProperty().addListener((o, oV, newValue) -> {
            try {
                Configuration.Manager.updateGlobalConfigurationTabIndex(newValue.intValue());
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
        }
        
        // Folders: MAME column
        
        rowIdx = 0;
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
        GridAdornment.insertTitle                           (optionsGrid, rowIdx++, SPACING, isMess ? "MESS" : "MAME");
        check(s -> new GenericCheckField                    (optionsGrid, rowIdx++, s),                                     "skip_gameinfo");
        check(s -> new GenericCheckField                    (optionsGrid, rowIdx++, s),                                     "confirm_quit");
        cheatCheck      = new CheatCheckField               (optionsGrid, rowIdx++, isMess);
        check(s -> new GenericCheckField                    (optionsGrid, rowIdx++, s),                                     "autosave");
        vsync           = new VsyncChoiceField              (optionsGrid, rowIdx++, isMess);
        new MameLanguageChoiceField                         (optionsGrid, rowIdx++);
        check(s -> new IntegerNumberField                   (optionsGrid, rowIdx++, s, 0, 600, 60, 1, 1),                   "seconds_to_run");
        
        GridAdornment.insertSpacing                         (optionsGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (optionsGrid, rowIdx++, SPACING, rb.getString("soundOptions"));
        check(s -> new ValueChoiceField<>                   (optionsGrid, rowIdx++, s, SampleRate.values()),                "samplerate");
        check(s -> new GenericCheckField                    (optionsGrid, rowIdx++, s),                                     "samples");
        check(s -> new IntegerNumberField                   (optionsGrid, rowIdx++, s, -32, 0, 4, 3, 1),                    "volume");
        
        GridAdornment.insertSpacing                         (optionsGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (optionsGrid, rowIdx++, SPACING, rb.getString("commOptions"));
        check(s -> new TextField                            (optionsGrid, rowIdx++, s),                                     "comm_localhost");
        check(s -> new IntegerSpinnerField                  (optionsGrid, rowIdx++, s, 65535),                              "comm_localport"); // 65535 = 2^16 - 1
        check(s -> new TextField                            (optionsGrid, rowIdx++, s),                                     "comm_remotehost");
        check(s -> new IntegerSpinnerField                  (optionsGrid, rowIdx++, s, 65535),                              "comm_remoteport");
        check(s -> new GenericCheckField                    (optionsGrid, rowIdx++, s),                                     "comm_framesync");
        
        GridAdornment.insertSpacing                         (optionsGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (optionsGrid, rowIdx++, SPACING, rb.getString("httpServerOptions"));
        check(s -> new GenericCheckField                    (optionsGrid, rowIdx++, s),                                     "http");
        check(s -> new IntegerSpinnerField                  (optionsGrid, rowIdx++, s, 65535),                              "http_port"); // 65535 = 2^16 - 1
        check(s -> new TextField                            (optionsGrid, rowIdx++, s),                                     "http_root");

        // Options: Negatron column
        
        rowIdx = 0;
        GridAdornment.insertTitle                           (optionsGrid2, rowIdx++, SPACING, rb.getString("negatron"));
        skin            = new SkinChoiceField               (optionsGrid2, rowIdx++);
        font            = new FontSelectionField            (optionsGrid2, rowIdx++);
        language        = new NegatronLanguageChoiceField   (optionsGrid2, rowIdx++);
        new InfotipTimingChoiceField                        (optionsGrid2, rowIdx++);
        
        GridAdornment.insertSpacing                         (optionsGrid2, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (optionsGrid2, rowIdx++, SPACING, isMess ? "MESS" : "MAME");
        check(s -> new GenericCheckField                    (optionsGrid2, rowIdx++, s),                                    "drc");
        check(s -> new GenericCheckField                    (optionsGrid2, rowIdx++, s),                                    "drc_use_c");
        check(s -> new GenericCheckField                    (optionsGrid2, rowIdx++, s),                                    "drc_log_uml");
        check(s -> new GenericCheckField                    (optionsGrid2, rowIdx++, s),                                    "drc_log_native");
        check(s -> new TextField                            (optionsGrid2, rowIdx++, s),                                    "bios");
        check(s -> new IntegerSpinnerField                  (optionsGrid2, rowIdx++, s, 2147483647),                        "ramsize"); // 2 147 483 647 = 2^32 - 1 = max int value
        check(s -> new GenericCheckField                    (optionsGrid2, rowIdx++, s),                                    "nvram_save");
        check(s -> new GenericCheckField                    (optionsGrid2, rowIdx++, s),                                    "dtd");
        
        GridAdornment.insertSpacing                         (optionsGrid2, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (optionsGrid2, rowIdx++, SPACING, rb.getString("scriptingOptions"));
        check(s -> new TextField                            (optionsGrid2, rowIdx++, s),                                    "autoboot_command");
        check(s -> new IntegerSpinnerField                  (optionsGrid2, rowIdx++, s, 2147483647),                        "autoboot_delay"); // 2 147 483 647 = 2^32 - 1 = max int value
        check(s -> new TextField                            (optionsGrid2, rowIdx++, s),                                    "autoboot_script");
        check(s -> new GenericCheckField                    (optionsGrid2, rowIdx++, s),                                    "console");
        check(s -> new GenericCheckField                    (optionsGrid2, rowIdx++, s),                                    "plugins");
        check(s -> new TextField                            (optionsGrid2, rowIdx++, s),                                    "plugin");
        check(s -> new TextField                            (optionsGrid2, rowIdx++, s),                                    "noplugin");
        
        // Inputs
        
        rowIdx = 0;
        GridAdornment.insertTitle                           (inputsGrid, rowIdx++, SPACING, rb.getString("globalInputOptions"));
        check(s -> new GenericCheckField                    (inputsGrid, rowIdx++, s),                                      "mouse");
        check(s -> new GenericCheckField                    (inputsGrid, rowIdx++, s),                                      "joystick");
        lightgun = check(s -> new GenericCheckField         (inputsGrid, rowIdx++, s),                                      "lightgun");
        check(s -> new GenericCheckField                    (inputsGrid, rowIdx++, s),                                      "multikeyboard");
        check(s -> new GenericCheckField                    (inputsGrid, rowIdx++, s),                                      "multimouse");
        // Windows native only MAME options
        check(s -> new GenericCheckField                    (inputsGrid, rowIdx++, s),                                      "global_inputs");
        
        GridAdornment.insertSpacing                         (inputsGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (inputsGrid, rowIdx++, SPACING, rb.getString("keyboardSpecificOptions"));
        check(s -> new GenericCheckField                    (inputsGrid, rowIdx++, s),                                      "steadykey");
        //check(s -> new KeyField                           (inputsGrid, rowIdx++, s),                                      "uimodekey");
        check(s -> new GenericCheckField                    (inputsGrid, rowIdx++, s),                                      "ui_active");
        check(s -> new GenericCheckField                    (inputsGrid, rowIdx++, s),                                      "natural");
        
        GridAdornment.insertSpacing                         (inputsGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (inputsGrid, rowIdx++, SPACING, rb.getString("joystickSpecificOptions"));
        check(s -> new FloatingNumberField                  (inputsGrid, rowIdx++, s, "%.1f", 0.0, 1.0, 0.5, 4, 0.1),       "joystick_deadzone");
        check(s -> new FloatingNumberField                  (inputsGrid, rowIdx++, s, "%.2f", 0.0, 1.0, 0.1, 1, 0.05),      "joystick_saturation");
        check(s -> new GenericCheckField                    (inputsGrid, rowIdx++, s),                                      "joystick_contradictory");
        
        rowIdx = 0;
        GridAdornment.insertTitle                           (inputsGrid2, rowIdx++, SPACING, rb.getString("machineInputSpecificOptions"));
        InputDevice[] inputDevices = InputDevice.values();
        check(s -> new LocalisedChoiceField<>               (inputsGrid2, rowIdx++, s, inputDevices),                       "paddle_device");
        check(s -> new LocalisedChoiceField<>               (inputsGrid2, rowIdx++, s, inputDevices),                       "adstick_device");
        check(s -> new LocalisedChoiceField<>               (inputsGrid2, rowIdx++, s, inputDevices),                       "pedal_device");
        check(s -> new LocalisedChoiceField<>               (inputsGrid2, rowIdx++, s, inputDevices),                       "dial_device");
        check(s -> new LocalisedChoiceField<>               (inputsGrid2, rowIdx++, s, inputDevices),                       "trackball_device");
        check(s -> new LocalisedChoiceField<>               (inputsGrid2, rowIdx++, s, inputDevices),                       "lightgun_device");
        check(s -> new LocalisedChoiceField<>               (inputsGrid2, rowIdx++, s, inputDevices),                       "positional_device");
        check(s -> new LocalisedChoiceField<>               (inputsGrid2, rowIdx++, s, inputDevices),                       "mouse_device");
        
        GridAdornment.insertSpacing                         (inputsGrid2, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (inputsGrid2, rowIdx++, SPACING, rb.getString("coinSpecificOptions"));
        check(s -> new GenericCheckField                    (inputsGrid2, rowIdx++, s),                                     "coin_lockout");
        check(s -> new IntegerNumberField                   (inputsGrid2, rowIdx++, s, 0, 60, 10, 9, 1),                    "coin_impulse");
        
        GridAdornment.insertSpacing                         (inputsGrid2, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (inputsGrid2, rowIdx++, SPACING, rb.getString("lightGunSpecificOptions"));
        check(s -> new GenericCheckField                    (inputsGrid2, rowIdx++, s),                                     "offscreen_reload");
        // Windows native only MAME options
        dualLightgun    = check(s -> new GenericCheckField  (inputsGrid2, rowIdx++, s),                                     "dual_lightgun");
        if (dualLightgun != null && lightgun != null) {
            lightgun.selectedProperty().addListener((o, oV, newValue) -> {
                dualLightgun.setDisable(!newValue);
            });
            dualLightgun.setDisable(!lightgun.isSelected());
        }
        
        // Graphics
        
        rowIdx = 0;
        GridAdornment.insertTitle                           (graphicsGrid, rowIdx++, SPACING, rb.getString("screenOptions"));
        check(s -> new FloatingNumberField                  (graphicsGrid, rowIdx++, s, "%.2f", 0.0, 2.0, 0.5, 4, 0.1),     "brightness");
        check(s -> new FloatingNumberField                  (graphicsGrid, rowIdx++, s, "%.2f", 0.0, 2.0, 0.5, 4, 0.1),     "contrast");
        check(s -> new FloatingNumberField                  (graphicsGrid, rowIdx++, s, "%.2f", 0.0, 3.0, 0.5, 4, 0.1),     "gamma");
        check(s -> new FloatingNumberField                  (graphicsGrid, rowIdx++, s, "%.2f", 0.0, 1.0, 0.1, 1, 0.05),    "pause_brightness");
        
        GridAdornment.insertSpacing                         (graphicsGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (graphicsGrid, rowIdx++, SPACING, rb.getString("renderingOptions"));
        keepaspect      = check(s -> new GenericCheckField  (graphicsGrid, rowIdx++, s),                                    "keepaspect");
        unevenstretch   = check(s -> new GenericCheckField  (graphicsGrid, rowIdx++, s),                                    "unevenstretch");
        check(s -> new GenericCheckField                    (graphicsGrid, rowIdx++, s),                                    "unevenstretchx");
        check(s -> new GenericCheckField                    (graphicsGrid, rowIdx++, s),                                    "unevenstretchy");
        check(s -> new GenericCheckField                    (graphicsGrid, rowIdx++, s),                                    "autostretchxy");
        intoverscan     = check(s -> new GenericCheckField  (graphicsGrid, rowIdx++, s),                                    "intoverscan");
        intscalex       = check(s -> new IntegerNumberField (graphicsGrid, rowIdx++, s, 0, 10, 2, 1, 1),                    "intscalex");
        intscaley       = check(s -> new IntegerNumberField (graphicsGrid, rowIdx++, s, 0, 10, 2, 1, 1),                    "intscaley");
        
        if (keepaspect != null && unevenstretch != null && intoverscan != null && intscalex != null && intscaley != null) {
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
        }
        
        GridAdornment.insertSpacing                         (graphicsGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (graphicsGrid, rowIdx++, SPACING, rb.getString("vectorOptions"));
        check(s -> new FloatingNumberField                  (graphicsGrid, rowIdx++, s, "%.2f", 0.0, 1.0, 0.1, 1, 0.1),     "beam_width_min");
        check(s -> new FloatingNumberField                  (graphicsGrid, rowIdx++, s, "%.2f", 1.0, 10.0, 1.0, 5, 0.1),    "beam_width_max");
        check(s -> new FloatingNumberField                  (graphicsGrid, rowIdx++, s, "%.2f", -10.0, 10.0, 2.0, 5, 0.1),  "beam_intensity_weight");
        check(s -> new FloatingNumberField                  (graphicsGrid, rowIdx++, s, "%.2f", 0.0, 100.0, 10.0, 4, 1.0),  "flicker");
        
        rowIdx = 0;
        GridAdornment.insertTitle                           (graphicsGrid2, rowIdx++, SPACING, rb.getString("performanceOptions"));
        check(s -> new GenericCheckField                    (graphicsGrid2, rowIdx++, s),                                   "autoframeskip");
        check(s -> new IntegerNumberField                   (graphicsGrid2, rowIdx++, s, 0, 10, 2, 1, 1),                   "frameskip");
        throttle        = check(s -> new GenericCheckField  (graphicsGrid2, rowIdx++, s),                                   "throttle");
        sleep           = check(s -> new GenericCheckField  (graphicsGrid2, rowIdx++, s),                                   "sleep");
        speed           = check(s -> new FloatingNumberField(graphicsGrid2, rowIdx++, s, "%.2f", 0.0, 100.0, 10.0, 4, 1.0), "speed");
        check(s -> new GenericCheckField                    (graphicsGrid2, rowIdx++, s),                                   "refreshspeed");
        if (throttle != null)
            throttle.selectedProperty().addListener((o, oV, newValue) -> {
                sleep.setDisable(!newValue);
                speed.setDisable(!newValue);
            });
        
        GridAdornment.insertSpacing                         (graphicsGrid2, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (graphicsGrid2, rowIdx++, SPACING, rb.getString("rotationOptions"));
        check(s -> new GenericCheckField                    (graphicsGrid2, rowIdx++, s),                                   "rotate");
        check(s -> new GenericCheckField                    (graphicsGrid2, rowIdx++, s),                                   "ror");
        check(s -> new GenericCheckField                    (graphicsGrid2, rowIdx++, s),                                   "rol");
        check(s -> new GenericCheckField                    (graphicsGrid2, rowIdx++, s),                                   "autoror");
        check(s -> new GenericCheckField                    (graphicsGrid2, rowIdx++, s),                                   "autorol");
        check(s -> new GenericCheckField                    (graphicsGrid2, rowIdx++, s),                                   "flipx");
        check(s -> new GenericCheckField                    (graphicsGrid2, rowIdx++, s),                                   "flipy");
        
        GridAdornment.insertSpacing                         (graphicsGrid2, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (graphicsGrid2, rowIdx++, SPACING, rb.getString("artworkOptions"));
        check(s -> new GenericCheckField                    (graphicsGrid2, rowIdx++, s),                                   "artwork_crop");
        check(s -> new GenericCheckField                    (graphicsGrid2, rowIdx++, s),                                   "use_backdrops");
        check(s -> new GenericCheckField                    (graphicsGrid2, rowIdx++, s),                                   "use_overlays");
        check(s -> new GenericCheckField                    (graphicsGrid2, rowIdx++, s),                                   "use_bezels");
        check(s -> new GenericCheckField                    (graphicsGrid2, rowIdx++, s),                                   "use_cpanels");
        check(s -> new GenericCheckField                    (graphicsGrid2, rowIdx++, s),                                   "use_marquees");
        
        rowIdx = 0;
        GridAdornment.insertTitle                           (osdGrid, rowIdx++, SPACING, rb.getString("osdInputOptions"));
        check(s -> new OSDChoiceField<>                     (osdGrid, rowIdx++, s, KeyboardProvider.values()),              "keyboardprovider");
        check(s -> new OSDChoiceField<>                     (osdGrid, rowIdx++, s, MouseProvider.values()),                 "mouseprovider");
        check(s -> new OSDChoiceField<>                     (osdGrid, rowIdx++, s, LightgunProvider.values()),              "lightgunprovider");
        check(s -> new OSDChoiceField<>                     (osdGrid, rowIdx++, s, JoystickProvider.values()),              "joystickprovider");
        
        GridAdornment.insertSpacing                         (osdGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (osdGrid, rowIdx++, SPACING, rb.getString("osdOutputOptions"));
        check(s -> new LocalisedChoiceField<>               (osdGrid, rowIdx++, s, Output.values()),                        "output");
        
        GridAdornment.insertSpacing                         (osdGrid, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (osdGrid, rowIdx++, SPACING, rb.getString("osdSoundOptions"));
        check(s -> new OSDChoiceField<>                     (osdGrid, rowIdx++, s, Sound.values()),                         "sound");
        check(s -> new IntegerNumberField                   (osdGrid, rowIdx++, s, 1, 5, 1, 0, 1),                          "audio_latency");

        rowIdx = 0;
        GridAdornment.insertTitle                           (osdGrid2, rowIdx++, SPACING, rb.getString("osdVideoOptions"));
        check(s -> new OSDChoiceField<>                     (osdGrid2, rowIdx++, s, UIFontProvider.values()),               "uifontprovider");
        check(s -> new OSDChoiceField<>                     (osdGrid2, rowIdx++, s, Video.values()),                        "video");
        check(s -> new IntegerNumberField                   (osdGrid2, rowIdx++, s, 1, 4, 1, 0, 1),                         "numscreens");
        check(s -> new GenericCheckField                    (osdGrid2, rowIdx++, s),                                        "window");
        check(s -> new GenericCheckField                    (osdGrid2, rowIdx++, s),                                        "maximize");
        check(s -> new GenericCheckField                    (osdGrid2, rowIdx++, s),                                        "switchres");
        check(s -> new FloatingNumberField                  (osdGrid2, rowIdx++, s, "%.2f", 0.0, 2.0, 0.5, 4, 0.1),         "full_screen_brightness");
        check(s -> new FloatingNumberField                  (osdGrid2, rowIdx++, s, "%.2f", 0.0, 2.0, 0.5, 4, 0.1),         "full_screen_contrast");
        check(s -> new FloatingNumberField                  (osdGrid2, rowIdx++, s, "%.2f", 0.0, 3.0, 0.5, 4, 0.1),         "full_screen_gamma");
        check(s -> new GenericCheckField                    (osdGrid2, rowIdx++, s),                                        "syncrefresh");
        check(s -> new OSDChoiceField<>                     (osdGrid2, rowIdx++, s, MonitorProvider.values()),              "monitorprovider");
        
        GridAdornment.insertSpacing                         (osdGrid2, rowIdx++, SPACING);
        GridAdornment.insertTitle                           (osdGrid2, rowIdx++, SPACING, rb.getString("osdAcceleratedVideoOptions"));
        check(s -> new GenericCheckField                    (osdGrid2, rowIdx++, s),                                        "filter");
        check(s -> new IntegerNumberField                   (osdGrid2, rowIdx++, s, 1, 3, 1, 0, 1),                         "prescale");
    }
    
    // if the option is in mame.ini, instantiate the related control else do nothing
    private <R> R check(Function<String, R> instantiate, String option) {
        if (Configuration.Manager.getGlobalConfiguration(option) != null)
            return instantiate.apply(option);
        return null;
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
        addRemoveGridFields(optionsGrid, 6);
        addRemoveGridFields(optionsGrid2, 4);
        
        try {
            Configuration.Manager.updateGlobalAdvancedOptionsEnabled(
                osdTab.getTabPane() != null
            );
        } catch (IOException ex) {
            Logger.getLogger(GlobalConfigurationPaneController.class.getName()).log(
                Level.SEVERE, "Couldn't save the flag about whether the global advanced options are enabled or not", ex
            );
        }
    }
    
    public void setOnRestart(Delegate delegate) {
        language.setOnRestart(delegate);
    }

    public void initialise(ToggleButton titleButton) {
        if (!Configuration.Manager.isGlobalAdvancedOptionsEnabled())
            handleOnAdvancedOptions(null);
        else
            titleButton.setSelected(true);
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
