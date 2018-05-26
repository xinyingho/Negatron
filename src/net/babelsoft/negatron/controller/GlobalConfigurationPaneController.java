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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.Domain;
import net.babelsoft.negatron.io.configuration.Property;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.view.control.form.ChdmanPathField;
import net.babelsoft.negatron.view.control.form.CheatCheckField;
import net.babelsoft.negatron.view.control.form.ExtrasRootPathField;
import net.babelsoft.negatron.view.control.form.GridAdornment;
import net.babelsoft.negatron.view.control.form.LanguageChoiceField;
import net.babelsoft.negatron.view.control.form.MameIniField;
import net.babelsoft.negatron.view.control.form.MamePathField;
import net.babelsoft.negatron.view.control.form.MultiPathField;
import net.babelsoft.negatron.view.control.form.MultimediaRootPathField;
import net.babelsoft.negatron.view.control.form.SkinChoiceField;
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
    private LanguageChoiceField language;
    
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
        
        mameExec        = new MamePathField     (foldersGrid,   0, isMess);
        mameIni         = new MameIniField      (foldersGrid,   1, isMess);
        chdmanExec      = new ChdmanPathField   (foldersGrid,   2);
        
        GridAdornment.insertSpacing             (foldersGrid,   3, SPACING);
        artwork         = new MultiPathField    (foldersGrid,   4, Property.ARTWORK,          rb.getString("artwork"), rb.getString("artwork.tooltip"));
        cheat           = new MultiPathField    (foldersGrid,   5, Property.CHEAT,            rb.getString("cheat"), rb.getString("cheat.tooltip"), compressedArchives, "*.7z; *.zip");
        controller      = new MultiPathField    (foldersGrid,   6, Property.CONTROLLER,       rb.getString("controllerDefinition"), rb.getString("controllerDefinition.tooltip"));
        rom             = new MultiPathField    (foldersGrid,   7, Property.ROM,              rb.getString("rom"), rb.getString("rom.tooltip"));
        sample          = new MultiPathField    (foldersGrid,   8, Property.SAMPLE,           rb.getString("sample"), rb.getString("sample.tooltip"));
        
        GridAdornment.insertSpacing             (foldersGrid,   9, SPACING);
        GridAdornment.insertTitle               (foldersGrid,  10, SPACING,                   isMess ? rb.getString("mameExtrasHistoryVersioning").replaceFirst("MAME", "MESS") : rb.getString("mameExtrasHistoryVersioning"));
        information     = new MultiPathField    (foldersGrid,  11, Property.INFORMATION,      rb.getString("information"), rb.getString("information.tooltip"), rb.getString("dataFiles"), "*.dat");
        folderView      = new MultiPathField    (foldersGrid,  12, Property.FOLDER_VIEW,      rb.getString("folderView"), rb.getString("folderView.tooltip"));
        
        GridAdornment.insertSpacing             (foldersGrid,  13, SPACING);
        GridAdornment.insertTitle               (foldersGrid,  14, SPACING,                   rb.getString("miscellaneous"));
        vlcPath         = new VlcPathField      (foldersGrid,  15);
        
        // Folders: EXTRAs column
        
        mameExtras      = new ExtrasRootPathField    (foldersGrid2,  0, isMess);
        mameMultimedia  = new MultimediaRootPathField(foldersGrid2,  1);
        
        GridAdornment.insertHeader              (foldersGrid2,  2, SPACING, Domain.EXTRAS_MACHINE_SOFTWARE);
        icon            = new MultiPathField    (foldersGrid2,  3, Property.ICON,             rb.getString("icon"), rb.getString("icon.tooltip"), compressedArchives, "*.zip");
        manual          = new MultiPathField    (foldersGrid2,  4, Property.MANUAL,           rb.getString("manual"), rb.getString("manual.tooltip"), compressedArchives, "*.zip");
        snapshot        = new MultiPathField    (foldersGrid2,  5, Property.SNAPSHOT,         rb.getString("snapshot"), rb.getString("snapshot.tooltip"), compressedArchives, "*.zip");
        title           = new MultiPathField    (foldersGrid2,  6, Property.TITLE,            rb.getString("title"), rb.getString("title.tooltip"), compressedArchives, "*.zip");
        videoPreview    = new MultiPathField    (foldersGrid2,  7, Property.VIDEO_PREVIEW,    rb.getString("videoPreview"), rb.getString("videoPreview.tooltip"));
        
        GridAdornment.insertHeader              (foldersGrid2,  8, SPACING, Domain.EXTRAS_MACHINE_ONLY);
        artworkPreview  = new MultiPathField    (foldersGrid2,  9, Property.ARTWORK_PREVIEW,  rb.getString("artworkPreview"), rb.getString("artworkPreview.tooltip"), compressedArchives, "*.zip");
        boss            = new MultiPathField    (foldersGrid2, 10, Property.BOSS,             rb.getString("boss"), rb.getString("boss.tooltip"), compressedArchives, "*.zip");
        cabinet         = new MultiPathField    (foldersGrid2, 11, Property.CABINET,          rb.getString("cabinet"), rb.getString("cabinet.tooltip"), compressedArchives, "*.zip");
        controlPanel    = new MultiPathField    (foldersGrid2, 12, Property.CONTROL_PANEL,    rb.getString("controlPanel"), rb.getString("controlPanel.tooltip"), compressedArchives, "*.zip");
        device          = new MultiPathField    (foldersGrid2, 13, Property.DEVICE,           rb.getString("device"), rb.getString("device.tooltip"), compressedArchives, "*.zip");
        end             = new MultiPathField    (foldersGrid2, 14, Property.END,              rb.getString("end"), rb.getString("end.tooltip"), compressedArchives, "*.zip");
        flyer           = new MultiPathField    (foldersGrid2, 15, Property.FLYER,            rb.getString("flyer"), rb.getString("flyer.tooltip"), compressedArchives, "*.zip");
        gameOver        = new MultiPathField    (foldersGrid2, 16, Property.GAME_OVER,        rb.getString("gameOver"), rb.getString("gameOver.tooltip"), compressedArchives, "*.zip");
        howTo           = new MultiPathField    (foldersGrid2, 17, Property.HOW_TO,           rb.getString("howTo"), rb.getString("howTo.tooltip"), compressedArchives, "*.zip");
        logo            = new MultiPathField    (foldersGrid2, 18, Property.LOGO,             rb.getString("logo"), rb.getString("logo.tooltip"), compressedArchives, "*.zip");
        marquee         = new MultiPathField    (foldersGrid2, 19, Property.MARQUEE,          rb.getString("marquee"), rb.getString("marquee.tooltip"), compressedArchives, "*.zip");
        pcb             = new MultiPathField    (foldersGrid2, 20, Property.PCB,              rb.getString("pcb"), rb.getString("pcb.tooltip"), compressedArchives, "*.zip");
        score           = new MultiPathField    (foldersGrid2, 21, Property.SCORE,            rb.getString("score"), rb.getString("score.tooltip"), compressedArchives, "*.zip");
        select          = new MultiPathField    (foldersGrid2, 22, Property.SELECT,           rb.getString("select"), rb.getString("select.tooltip"), compressedArchives, "*.zip");
        soundtrack      = new MultiPathField    (foldersGrid2, 23, Property.SOUNDTRACK,       rb.getString("soundtrack"), rb.getString("soundtrack.tooltip"));
        versus          = new MultiPathField    (foldersGrid2, 24, Property.VERSUS,           rb.getString("versus"), rb.getString("versus.tooltip"), compressedArchives, "*.zip");
        
        GridAdornment.insertHeader              (foldersGrid2, 25, SPACING, Domain.EXTRAS_SOFTWARE_ONLY);
        boxArt          = new MultiPathField    (foldersGrid2, 26, Property.BOX_ART,          rb.getString("boxArt"), rb.getString("boxArt.tooltip"));
        cover           = new MultiPathField    (foldersGrid2, 27, Property.COVER,            rb.getString("cover"), rb.getString("cover.tooltip"), compressedArchives, "*.zip");
        media           = new MultiPathField    (foldersGrid2, 28, Property.MEDIA,            rb.getString("media"), rb.getString("media.tooltip"));
        
        // Options: MAME column
        
        cheatCheck      = new CheatCheckField   (optionsGrid,   0, isMess);
        vsync           = new VsyncChoiceField  (optionsGrid,   1, isMess);
        
        // Options: Negatron column
        
        skin            = new SkinChoiceField   (optionsGrid2,  0);
        language        = new LanguageChoiceField(optionsGrid2, 1);
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
