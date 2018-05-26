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
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.Property;
import net.babelsoft.negatron.io.extras.Extras;
import net.babelsoft.negatron.io.extras.Images;
import net.babelsoft.negatron.model.item.Software;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.view.control.ImageViewPane;
import net.babelsoft.negatron.view.control.TitledWindowPane;
import net.babelsoft.negatron.view.control.model3d.KeepCase;
import net.babelsoft.negatron.view.control.model3d.StraightTuckEndBox;
import net.babelsoft.negatron.view.control.model3d.TuckTopSnapLockBottomBox;

/**
 * Mega Drive (cover): 173 * 123.5 * 24 mm
 * Mega Drive (case): 184 * 133 * 22 mm
 * Super Nintendo (box): 126 * 180 * 31 mm
 * http://www.nintandbox.net http://oldiescovers.free.fr/ http://www.thecoverproject.net http://neogeosoft.com/
 * @author capan
 */
public class SoftwareInformationPaneController extends InformationPaneController<Software> {

    @FXML
    private TitledWindowPane titlePane;
    
    @FXML
    private StackPane externalStackPane;
    @FXML
    private GridPane externalGrid;
    @FXML
    private ImageViewPane boxartImagePane;
    @FXML
    private ImageViewPane coverImagePane;
    @FXML
    private ImageViewPane mediaImagePane;
    @FXML
    private SubScene scene;
    
    private Image boxArtImage;
    private Image coverImage;
    private Image mediaImage;
    private Group packageShape3DGroup;
    
    private double mousePosX, mousePosY;
    private Rotate rotateX, rotateY;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        getTabPane().getSelectionModel().select(Configuration.Manager.getSoftwareInformationTabIndex());
        super.initialize(url, rb);
        getTabPane().getSelectionModel().selectedIndexProperty().addListener((o, oV, newValue) -> {
            try {
                Configuration.Manager.updateSoftwareInformationTabIndex(newValue.intValue());
            } catch (IOException ex) {
                Logger.getLogger(SoftwareInformationPaneController.class.getName()).log(Level.SEVERE, "Couldn't save software tab index", ex);
            }
        });
        
        boxArtImage = new Image(getClass().getResourceAsStream("/net/babelsoft/negatron/resource/boxArt.png"));
        coverImage = new Image(getClass().getResourceAsStream("/net/babelsoft/negatron/resource/cover.png"));
        mediaImage = new Image(getClass().getResourceAsStream("/net/babelsoft/negatron/resource/media.png"));
        
        if (Platform.isSupported(ConditionalFeature.SCENE3D)) {
            // Create and position camera
            final double translateZ = 180.0 * -400.0 / 180.0;
            rotateX = new Rotate(50, Rotate.X_AXIS);
            rotateY = new Rotate(40, Rotate.Y_AXIS);
            PerspectiveCamera camera = new PerspectiveCamera(true);
            camera.setFarClip(Double.MAX_VALUE); // disable clipping, useless here as we only display lowpoly models with a fixed view
            camera.getTransforms().addAll (
                rotateX,
                rotateY,
                new Translate(0, 0, translateZ)
            );

            // Build the scene graph
            packageShape3DGroup = new Group();

            scene = new SubScene(packageShape3DGroup, externalGrid.getWidth(), externalGrid.getHeight(), true, SceneAntialiasing.BALANCED);
            scene.widthProperty().bind(externalGrid.widthProperty());
            scene.heightProperty().bind(externalGrid.heightProperty());
            scene.setPickOnBounds(true);
            scene.setCamera(camera);
            scene.setOpacity(1.0);
            scene.getStyleClass().add("view3d-pane");
            scene.addEventHandler(MouseEvent.ANY, event -> {
                if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    mousePosX = event.getSceneX();
                    mousePosY = event.getSceneY();
                } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED && event.isPrimaryButtonDown()) {
                    double modifier = 1.0;
                    if (event.isControlDown())
                        modifier = 0.1;
                    if (event.isShiftDown())
                        modifier = 10.0;
                    modifier *= 0.6;

                    double mouseOldX = mousePosX;
                    double mouseOldY = mousePosY;
                    mousePosX = event.getSceneX();
                    mousePosY = event.getSceneY();
                    double mouseDeltaX = mousePosX - mouseOldX;
                    double mouseDeltaY = mousePosY - mouseOldY;

                    rotateY.setAngle(rotateY.getAngle() + mouseDeltaX * modifier);
                    rotateX.setAngle(rotateX.getAngle() + mouseDeltaY * modifier);
                }
            });
            scene.addEventHandler(ScrollEvent.ANY, event -> {
                if (event.getTouchCount() > 0) { // touch device scroll
                    rotateX.setAngle(rotateX.getAngle() - 0.01 * event.getDeltaX());
                    rotateY.setAngle(rotateY.getAngle() - 0.01 * event.getDeltaY());
                }
            });
            scene.setMouseTransparent(true);

            externalStackPane.getChildren().add(scene);
            
            // Wire the transition between the 2D and 3D scenes
            externalGrid.setOpacity(0.0);
            setView3dEnabled(false);
        }
        
        initialiseExternalsZooming(boxartImagePane, coverImagePane, mediaImagePane);
        initialiseIngameZooming();
        
        postInitialise(new ImageViewPane[] { boxartImagePane, coverImagePane, mediaImagePane });
    }
    
    public void setView3dEnabled(boolean view3dEnabled) {
        if (view3dEnabled) {
            Timeline timeline = new Timeline();
            timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO,
                new KeyValue(externalGrid.opacityProperty(), 1.0),
                new KeyValue(scene.opacityProperty(), 0.0)
            ));
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(300),
                event -> scene.setMouseTransparent(false),
                new KeyValue(externalGrid.opacityProperty(), 0.0),
                new KeyValue(scene.opacityProperty(), 1.0)
            ));
            timeline.play();
        } else {
            Timeline timeline = new Timeline();
            timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO,
                new KeyValue(externalGrid.opacityProperty(), 0.0),
                new KeyValue(scene.opacityProperty(), 1.0)
            ));
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(300),
                event -> scene.setMouseTransparent(true),
                new KeyValue(externalGrid.opacityProperty(), 1.0),
                new KeyValue(scene.opacityProperty(), 0.0)
            ));
            timeline.play();
        }
    }
    
    public void setOnView3dShortcut(Delegate delegate) {
        externalStackPane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY)
                delegate.fire();
        });
    }

    @Override
    protected void setText(String text) {
        titlePane.setText(text);
    }

    @Override
    protected void setGraphic(ImageView graphic) {
        titlePane.setGraphic(graphic);
    }
    
    @Override
    public void setEmulatedItem(Software emulatedItem) {
        if (emulatedItem != null)
            systemName = emulatedItem.getGroup();
        super.setEmulatedItem(emulatedItem);
    }

    @Override
    protected void updateExternalTabContent(String currentName) throws IOException {
        Image image = Images.newImage(systemName, currentName, Property.MEDIA);
        if (image == null)
            image = mediaImage;
        mediaImagePane.setImage(image, true);
        mediaImagePane.setDragCopyPath(Extras.toPrimaryPath(systemName, currentName, Property.MEDIA));
        
        image = Images.newImage(systemName, currentName, Property.COVER);
        if (image == null)
            image = coverImage;
        coverImagePane.setImage(image, true);
        coverImagePane.setDragCopyPath(Extras.toPrimaryPath(systemName, currentName, Property.COVER));
        
        image = Images.newImage(systemName, currentName, Property.BOX_ART);
        if (image == null)
            image = boxArtImage;
        boxartImagePane.setImage(image, true);
        boxartImagePane.setDragCopyPath(Extras.toPrimaryPath(systemName, currentName, Property.BOX_ART));
        
        if (!Platform.isSupported(ConditionalFeature.SCENE3D))
            return;
        
        Node packageShape3D;
        if (image != boxArtImage) {
            switch (systemName) {
                case "megadriv":
                case "sms":
                default:
                    packageShape3D = new KeepCase(
                        178f, 126.5f, 23f, Color.BLACK,
                        173f, 123.5f, 24f, image
                    );
                    break;
                case "snes":
                    packageShape3D = new StraightTuckEndBox(
                        180, 126, 31, 10, 15.5f, true, image
                        //new Image("file:\\D:\\frontend\\GBAtemp Cover Collections\\boxarts\\snes\\SuperMarioWorld_SNES-Box_AME(en)_20130311.jpg")
                    );
                    /*
                    packageShape3D = new ReverseTuckEndBox(
                        192, 106, 30, 10, 17.5f,
                        new Image("file:\\D:\\frontend\\GBAtemp Cover Collections\\boxarts\\snes\\SuperMarioWorld_SNES-Box_ASI(ja)_20130601.jpg")
                    );*/
                    break;
                case "nes":
                    packageShape3D = new TuckTopSnapLockBottomBox(
                        179, 126, 22, 10.5f, 16, 30.5f, 86, image
                        //new Image("file:\\D:\\frontend\\GBAtemp Cover Collections\\boxarts\\nes\\Aladdin_NES-Box_EUR(NES-AJ-FRA)_20080813.jpg")
                    );
                    break;
            }
        } else
            packageShape3D = new KeepCase(
                178f, 126.5f, 23f, Color.BLACK,
                173f, 123.5f, 24f, image
            );
        packageShape3DGroup.getChildren().setAll(packageShape3D);
    }
    
    @Override
    protected void updateIngameTabContent(String currentName) throws IOException {
        super.updateIngameTabContent(currentName);
        
        Image image = snapshotImagePane.getImageView().getImage();
        
        if (image.getWidth() >= image.getHeight()) {
            GridPane.setConstraints(titleImagePane, 0, 0);
            GridPane.setConstraints(snapshotImagePane, 0, 1);

            ColumnConstraints hundredPctWidth = new ColumnConstraints();
            hundredPctWidth.setPercentWidth(100);
            RowConstraints fiftyPctHeight = new RowConstraints();
            fiftyPctHeight.setPercentHeight(50);

            ingameGrid.getColumnConstraints().setAll(
                hundredPctWidth
            );
            ingameGrid.getRowConstraints().setAll(
                fiftyPctHeight, fiftyPctHeight
            );
        } else {
            GridPane.setConstraints(titleImagePane, 0, 0);
            GridPane.setConstraints(snapshotImagePane, 1, 0);

            ColumnConstraints fiftyPctWidth = new ColumnConstraints();
            fiftyPctWidth.setPercentWidth(50);
            RowConstraints hundredPctHeight = new RowConstraints();
            hundredPctHeight.setPercentHeight(100);

            ingameGrid.getColumnConstraints().setAll(
                fiftyPctWidth, fiftyPctWidth
            );
            ingameGrid.getRowConstraints().setAll(
                hundredPctHeight
            );
        }
    }
}
