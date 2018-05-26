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
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.Property;
import net.babelsoft.negatron.io.extras.Extras;
import net.babelsoft.negatron.io.extras.Images;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.view.control.ImageViewPane;
import net.babelsoft.negatron.view.control.adapter.ImageGridAdapter;

/**
 * FXML Controller class
 *
 * @author capan
 */
public class MachineInformationPaneController extends InformationPaneController<Machine> {
    @FXML
    private Label headerLabel;
    
    @FXML
    private ProgressIndicator progressIndicator;
    
    @FXML
    private ImageViewPane marqueeImagePane;
    @FXML
    private ImageViewPane cabinetImagePane;
    @FXML
    private ImageViewPane controlPanelImagePane;
    @FXML
    private ImageViewPane flyerImagePane;
    
    @FXML
    private GridPane ingameTitleGrid;
    
    @FXML
    private ImageViewPane logoImagePane;
    @FXML
    private ImageViewPane howtoImagePane;
    @FXML
    private ImageViewPane scoreImagePane;
    @FXML
    private ImageViewPane bossImagePane;
    @FXML
    private ImageViewPane gameoverImagePane;
    @FXML
    private ImageViewPane endImagePane;
    @FXML
    private ImageViewPane selectImagePane;
    @FXML
    private ImageViewPane artpreviewImagePane;
    @FXML
    private ImageViewPane versusImagePane;
    
    @FXML
    private Tab internalsTab;
    
    @FXML
    private GridPane internalsPane;
    
    private Image marqueeImage;
    private Image cabinetImage;
    private Image cpanelImage;
    private Image flyerImage;
    private Image colourTestAltImage;
    private Image deviceImage;
    
    private int internalDeviceCount;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        getTabPane().getSelectionModel().select(Configuration.Manager.getMachineInformationTabIndex());
        super.initialize(url, rb);
        getTabPane().getSelectionModel().selectedIndexProperty().addListener((o, oV, newValue) -> {
            try {
                Configuration.Manager.updateMachineInformationTabIndex(newValue.intValue());
            } catch (IOException ex) {
                Logger.getLogger(MachineInformationPaneController.class.getName()).log(Level.SEVERE, "Couldn't save machine tab index", ex);
            }
        });
        
        marqueeImage = new Image(getClass().getResourceAsStream("/net/babelsoft/negatron/resource/marquee.png"));
        cabinetImage = new Image(getClass().getResourceAsStream("/net/babelsoft/negatron/resource/cabinet.png"));
        cpanelImage = new Image(getClass().getResourceAsStream("/net/babelsoft/negatron/resource/cpanel.png"));
        flyerImage = new Image(getClass().getResourceAsStream("/net/babelsoft/negatron/resource/flyer.png"));
        colourTestAltImage = new Image(getClass().getResourceAsStream("/net/babelsoft/negatron/resource/TvColourTestCardAlt.png"));
        deviceImage = new Image(getClass().getResourceAsStream("/net/babelsoft/negatron/resource/device.png"));
        
        mediaViewPane.addAllDropTargets(
            logoImagePane, howtoImagePane, scoreImagePane, bossImagePane, gameoverImagePane, endImagePane, selectImagePane, artpreviewImagePane, versusImagePane
        );
        
        initialiseExternalsZooming(marqueeImagePane, cabinetImagePane, controlPanelImagePane, flyerImagePane);
        initialiseIngameZooming(logoImagePane, howtoImagePane, scoreImagePane, bossImagePane, gameoverImagePane, endImagePane, selectImagePane, artpreviewImagePane, versusImagePane);
        
        postInitialise(new Tab[] { internalsTab },
            new ImageViewPane[] {
                marqueeImagePane, cabinetImagePane, controlPanelImagePane, flyerImagePane,
                logoImagePane, howtoImagePane, scoreImagePane, bossImagePane, gameoverImagePane, endImagePane, selectImagePane, artpreviewImagePane, versusImagePane
            }
        );
    }
    
    @Override
    protected void setText(String text) {
        headerLabel.setText(text);
    }
    
    @Override
    protected void setGraphic(ImageView graphic) {
        headerLabel.setGraphic(graphic);
    }
    
    @Override
    public void showTab() {
        super.showTab();
        progressIndicator.setVisible(false);
    }
    
    @Override
    public void hideTab(Delegate delegate, boolean onStarted) {
        super.hideTab(delegate, onStarted);
        progressIndicator.setVisible(true);
    }
    
    @Override
    protected boolean updateTabContent(Tab selectedTab, boolean forceUpdate) {
        if (super.updateTabContent(selectedTab, forceUpdate))
            return true;
        
        String currentName = (currentEmulatedItem != null) ? currentEmulatedItem.getName() : null;
        try {
            if (selectedTab == internalsTab) {
                // machine internals
                if (
                    forceUpdate ||
                    currentEmulatedItem != internalsTab.getUserData() ||
                    currentEmulatedItem != null && currentEmulatedItem == internalsTab.getUserData() && currentEmulatedItem.hasNewInternalDevices()
                ) {
                    updateInternalsTabContent(currentName);
                    internalsTab.setUserData(currentEmulatedItem);
                }
                return true;
            }
        } catch (IOException ex) {
            Logger.getLogger(MachineInformationPaneController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    protected void updateExternalTabContent(String currentName) throws IOException {
        Image image = Images.newImage(currentName, Property.MARQUEE);
        if (image == null)
            image = marqueeImage;
        marqueeImagePane.setImage(image);
        marqueeImagePane.setDragCopyPath(Extras.toPrimaryPath(currentName, Property.MARQUEE));
        
        image = Images.newImage(currentName, Property.CABINET);
        if (image == null)
            image = cabinetImage;
        cabinetImagePane.setImage(image);
        cabinetImagePane.setDragCopyPath(Extras.toPrimaryPath(currentName, Property.CABINET));
        
        image = Images.newImage(currentName, Property.CONTROL_PANEL);
        if (image == null)
            image = cpanelImage;
        controlPanelImagePane.setImage(image);
        controlPanelImagePane.setDragCopyPath(Extras.toPrimaryPath(currentName, Property.CONTROL_PANEL));
        
        image = Images.newImage(currentName, Property.FLYER);
        if (image == null)
            image = flyerImage;
        flyerImagePane.setImage(image);
        flyerImagePane.setDragCopyPath(Extras.toPrimaryPath(currentName, Property.FLYER));
    }
    
    @Override
    protected void updateIngameTabContent(String currentName) throws IOException {
        super.updateIngameTabContent(currentName);
        
        // row 1
        Image image = Images.newImage(systemName, currentName, Property.LOGO);
        if (image == null)
            image = colourTestImage;
        logoImagePane.setImage(image);
        logoImagePane.setDragCopyPath(Extras.toPrimaryPath(systemName, currentName, Property.LOGO));
        
        image = Images.newImage(systemName, currentName, Property.HOW_TO);
        if (image == null)
            image = colourTestAltImage;
        howtoImagePane.setImage(image);
        howtoImagePane.setDragCopyPath(Extras.toPrimaryPath(systemName, currentName, Property.HOW_TO));
        
        image = Images.newImage(systemName, currentName, Property.SCORE);
        if (image == null)
            image = noiseImage;
        scoreImagePane.setImage(image);
        scoreImagePane.setDragCopyPath(Extras.toPrimaryPath(systemName, currentName, Property.SCORE));
        
        // row 2
        image = Images.newImage(systemName, currentName, Property.BOSS);
        if (image == null)
            image = colourTestImage;
        bossImagePane.setImage(image);
        bossImagePane.setDragCopyPath(Extras.toPrimaryPath(systemName, currentName, Property.BOSS));
        
        image = Images.newImage(systemName, currentName, Property.GAME_OVER);
        if (image == null)
            image = colourTestAltImage;
        gameoverImagePane.setImage(image);
        gameoverImagePane.setDragCopyPath(Extras.toPrimaryPath(systemName, currentName, Property.GAME_OVER));
        
        image = Images.newImage(systemName, currentName, Property.END);
        if (image == null)
            image = noiseImage;
        endImagePane.setImage(image);
        endImagePane.setDragCopyPath(Extras.toPrimaryPath(systemName, currentName, Property.END));
        
        // row 3
        image = Images.newImage(systemName, currentName, Property.SELECT);
        if (image == null)
            image = colourTestImage;
        selectImagePane.setImage(image);
        selectImagePane.setDragCopyPath(Extras.toPrimaryPath(systemName, currentName, Property.SELECT));
        
        image = Images.newImage(systemName, currentName, Property.ARTWORK_PREVIEW);
        if (image == null)
            image = colourTestAltImage;
        artpreviewImagePane.setImage(image);
        artpreviewImagePane.setDragCopyPath(Extras.toPrimaryPath(systemName, currentName, Property.ARTWORK_PREVIEW));
        
        image = Images.newImage(systemName, currentName, Property.VERSUS);
        if (image == null)
            image = noiseImage;
        versusImagePane.setImage(image);
        versusImagePane.setDragCopyPath(Extras.toPrimaryPath(systemName, currentName, Property.VERSUS));
        
        image = snapshotImagePane.getImageView().getImage();
        
        if (image.getWidth() >= image.getHeight()) {
            GridPane.setConstraints(ingameTitleGrid, 0, 0, 3, 1);
            GridPane.setConstraints(titleImagePane, 0, 0);
            GridPane.setConstraints(snapshotImagePane, 1, 0);
            
            GridPane.setConstraints(logoImagePane, 0, 1);
            GridPane.setConstraints(howtoImagePane, 1, 1);
            GridPane.setConstraints(scoreImagePane, 2, 1);
            
            GridPane.setConstraints(bossImagePane, 0, 2);
            GridPane.setConstraints(gameoverImagePane, 1, 2);
            GridPane.setConstraints(endImagePane, 2, 2);
            
            GridPane.setConstraints(selectImagePane, 0, 3);
            GridPane.setConstraints(artpreviewImagePane, 1, 3);
            GridPane.setConstraints(versusImagePane, 2, 3);

            ColumnConstraints fiftyPctWidth = new ColumnConstraints();
            fiftyPctWidth.setPercentWidth(50);
            RowConstraints hundredPctHeight = new RowConstraints();
            hundredPctHeight.setPercentHeight(100);

            ingameTitleGrid.getColumnConstraints().setAll(
                fiftyPctWidth, fiftyPctWidth
            );
            ingameTitleGrid.getRowConstraints().setAll(
                hundredPctHeight
            );
            
            ColumnConstraints thirdWidth = new ColumnConstraints();
            thirdWidth.setPercentWidth(100.0 / 3.0);
            RowConstraints fortyPctHeight = new RowConstraints();
            fortyPctHeight.setPercentHeight(40);
            RowConstraints twentyPctHeight = new RowConstraints();
            twentyPctHeight.setPercentHeight(20);
            
            ingameGrid.getColumnConstraints().setAll(
                thirdWidth, thirdWidth, thirdWidth
            );
            ingameGrid.getRowConstraints().setAll(
                fortyPctHeight, twentyPctHeight, twentyPctHeight, twentyPctHeight
            );
        } else {
            GridPane.setConstraints(ingameTitleGrid, 0, 0, 1, 3);
            GridPane.setConstraints(titleImagePane, 0, 0);
            GridPane.setConstraints(snapshotImagePane, 0, 1);
            
            GridPane.setConstraints(logoImagePane, 1, 0);
            GridPane.setConstraints(howtoImagePane, 1, 1);
            GridPane.setConstraints(scoreImagePane, 1, 2);
            
            GridPane.setConstraints(bossImagePane, 2, 0);
            GridPane.setConstraints(gameoverImagePane, 2, 1);
            GridPane.setConstraints(endImagePane, 2, 2);
            
            GridPane.setConstraints(selectImagePane, 3, 0);
            GridPane.setConstraints(artpreviewImagePane, 3, 1);
            GridPane.setConstraints(versusImagePane, 3, 2);

            ColumnConstraints hundredPctWidth = new ColumnConstraints();
            hundredPctWidth.setPercentWidth(100);
            RowConstraints fiftyPctHeight = new RowConstraints();
            fiftyPctHeight.setPercentHeight(50);

            ingameTitleGrid.getColumnConstraints().setAll(
                hundredPctWidth
            );
            ingameTitleGrid.getRowConstraints().setAll(
                fiftyPctHeight, fiftyPctHeight
            );
            
            ColumnConstraints fortyPctWidth = new ColumnConstraints();
            fortyPctWidth.setPercentWidth(40);
            ColumnConstraints twentyPctWidth = new ColumnConstraints();
            twentyPctWidth.setPercentWidth(20);
            RowConstraints thirdHeight = new RowConstraints();
            thirdHeight.setPercentHeight(100.0 / 3.0);
            
            ingameGrid.getColumnConstraints().setAll(
                fortyPctWidth, twentyPctWidth, twentyPctWidth, twentyPctWidth
            );
            ingameGrid.getRowConstraints().setAll(
                thirdHeight, thirdHeight, thirdHeight
            );
        }
    }

    private void addInternalsItem(String name, Image image) {
        addInternalsItem(null, name, image, null);
    }
    
    private void addInternalsItem(String code, String name, Image image, Property property) {
        final ReadOnlyDoubleProperty widthProperty = internalsTab.getTabPane().widthProperty();
        ImageGridAdapter adapter = new ImageGridAdapter(image, widthProperty, 0.8, code, property);
        Label label = new Label(name);
        label.setWrapText(true);
        
        ImageViewPane pane = adapter.getImageViewPane();
        initialise(pane);
        internalsPane.addRow(internalDeviceCount++, label, pane);
        internalsPane.getRowConstraints().add(adapter.getRowConstraints());
    }
    
    private void updateInternalsTabContent(String currentName) throws IOException {
        internalsPane.getChildren().clear();
        internalsPane.getRowConstraints().clear();
        internalDeviceCount = 0;

        if (currentEmulatedItem != null) {
            Image image = Images.newImage(currentName, Property.PCB);
            if (image == null)
                image = deviceImage;
            addInternalsItem(currentName, "Printed Circuit Board", image, Property.PCB);

            for (Entry<String, String> entry : currentEmulatedItem.getInternalDevices().entrySet()) {
                image = Images.newImage(entry.getKey(), Property.DEVICE);
                if (image == null)
                    image = deviceImage;
                addInternalsItem(entry.getKey(), entry.getValue(), image, Property.DEVICE);
            }
        } else {
            addInternalsItem("Developer at BabelSoft", new Image(
                getClass().getResourceAsStream("/net/babelsoft/negatron/resource/BabelSoft.png")
            ));
            addInternalsItem("JavaFx GUI Library", new Image(
                getClass().getResourceAsStream("/net/babelsoft/negatron/resource/JavaFx.png")
            ));
            addInternalsItem("NetBeans IDE", new Image(
                getClass().getResourceAsStream("/net/babelsoft/negatron/resource/NetBeans.png")
            ));
            addInternalsItem("Oxygen Icon Library", new Image(
                getClass().getResourceAsStream("/net/babelsoft/negatron/resource/OxygenProject.png")
            ));
            addInternalsItem("PDF viewer from IDR Solutions", new Image(
                getClass().getResourceAsStream("/net/babelsoft/negatron/resource/IDR.png")
            ));
        }
    }
}
